package code.activity;

import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityFriendRequestListBinding;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.android.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import code.chat.ChatActivity;
import code.common.MyApplication;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class FriendRequestListActivity extends BaseActivity implements View.OnClickListener {

    ActivityFriendRequestListBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFriendRequestListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        findViewById(R.id.ivBack).setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetFriendRequestsApi();

        if (SendbirdChat.getCurrentUser()==null){
            MyApplication.getInstance().connectSendBirdChat();
        }
    }

    private void hitGetFriendRequestsApi() {

        WebServices.getApi(mActivity, AppUrls.pendingRequestList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseRequestList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseRequestList(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("pendingList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("requestId", jsonObject1.getString("requestId"));
                    hashMap.put("id", jsonObject1.getString("id"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("profileImage", jsonObject1.getString("profileImage"));
                    hashMap.put("averageRating", jsonObject1.getString("averageRating"));

                    arrayList.add(hashMap);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_freind_request, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvName.setText(data.get(position).get("name"));
            AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivProfile);
            holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("averageRating")));
            holder.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Inflating the Popup using xml file
                    PopupMenu popup = new PopupMenu(mActivity, holder.ivAction);

                    popup.getMenuInflater()
                            .inflate(R.menu.popup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.accept) {
                                if (AppUtils.isNetworkAvailable(mActivity)) {
                                    String requestId = data.get(position).get("requestId");
                                    String id = data.get(position).get("id");
                                    createChannelAndAcceptRequest(requestId,
                                            id);
                                } else {
                                    AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
                                }

                            } else if (item.getItemId() == R.id.reject) {
                                hitRejectFriendApi(data.get(position).get("requestId"));
                            }

                            return true;
                        }
                    });

                    popup.show(); //showing popup menu
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout rlMain;

            ImageView ivProfile, ivAction;

            TextView tvName;
            RatingBar ratingBar;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                rlMain = itemView.findViewById(R.id.rlMain);

                ivProfile = itemView.findViewById(R.id.ivProfile);
                ivAction = itemView.findViewById(R.id.ivAction);
                ratingBar = itemView.findViewById(R.id.ratingBar);

                tvName = itemView.findViewById(R.id.tvName);
            }
        }
    }

    private void createChannelAndAcceptRequest(String requestId, String id) {

        OpenChannelCreateParams params = new OpenChannelCreateParams();
        params.setName(""+id+""+requestId);
        User currentUser = SendbirdChat.getCurrentUser();
        List<User> list = new ArrayList<>();
        list.add(currentUser);
        params.setOperators(list);

        OpenChannel.createChannel(params, (openChannel, e) -> {

            if (openChannel != null) {

                hitAcceptRequestApi(openChannel.getName(), openChannel.getUrl(), requestId);

            } else {
                AppUtils.showToastSort(mActivity, getString(R.string.retry));
            }

        });

    }

    private void hitAcceptRequestApi(String channelName, String channelUrl, String requestId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("channelName", channelName);
            jsonObject.put("channelUrl", channelUrl);
            jsonObject.put("requestId", requestId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        WebServices.postApi(mActivity, AppUrls.acceptFriendRequest , json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAcceptJson(response, channelName, channelUrl);

            }

            @Override
            public void OnFail(String response) {

            }
        });

    }

    private void parseAcceptJson(JSONObject response, String channelName, String channelUrl) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

               onBackPressed();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.friends),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hitRejectFriendApi(String user_id) {


        WebServices.deleteApi(mActivity, AppUrls.rejectRequest + "?requestId=" + user_id, null, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });


    }

    private void parseJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetFriendRequestsApi();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.friends),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}