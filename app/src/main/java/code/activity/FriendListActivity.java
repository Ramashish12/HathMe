package code.activity;

import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_ID;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_NAME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityFriendListActivityBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.call.CallService;
import code.chat.ChatActivity;
import code.common.RecyclerTouchListener;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class FriendListActivity extends BaseActivity implements View.OnClickListener {

    ActivityFriendListActivityBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListCount = new ArrayList<>();
    Adapter adapter;

    RecyclerTouchListener touchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFriendListActivityBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        b.tvRequest.setOnClickListener(this);

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);
        // setTouchListener();

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                arrayList.clear();
                adapter.notifyDataSetChanged();

                if (b.etSearch.getText().toString().isEmpty()) {
                    if (touchListener != null) {
                        b.rvList.addOnItemTouchListener(touchListener);
                    }
                    hitGetFriendListApi();
                } else {

                    if (touchListener != null) {
                        b.rvList.removeOnItemTouchListener(touchListener);
                        b.rvList.removeOnItemTouchListener(touchListener);
                    }

                    hitSearchUserApi();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


//        mRtmCallManager = MyApplication.getInstance().rtmCallManager();

    }

    private void setTouchListener() {

        touchListener = new RecyclerTouchListener(this, b.rvList);


        touchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {

            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {

            }
        }).setSwipeOptionViews(R.id.ivAudioCall, R.id.ivVideoCall, R.id.ivChat).setSwipeable(R.id.rlFg, R.id.llBg,
                (viewID, position) -> {
                    switch (viewID) {

                        case R.id.ivAudioCall:
                            voiceCall(arrayList.get(position).get("userId"),
                                    arrayList.get(position).get("name"),
                                    arrayList.get(position).get("profileImage"));
                            //AppSettings.putString(AppSettings.callId,arrayList.get(position).get("userId"));
                            break;

                        case R.id.ivVideoCall:

                            videoCall(arrayList.get(position).get("userId"),
                                    arrayList.get(position).get("name"),
                                    arrayList.get(position).get("profileImage"));

                            break;

                        case R.id.ivChat:

                            Intent intent = new Intent(mActivity, ChatActivity.class);
                            intent.putExtra(INTENT_KEY_RECEIVER_ID, arrayList.get(position).get("userId"));
                            intent.putExtra(INTENT_KEY_CHANNEL_URL, arrayList.get(position).get("channelUrl"));
                            intent.putExtra(INTENT_KEY_CHANNEL_TITLE, arrayList.get(position).get("channelName"));
                            intent.putExtra(INTENT_KEY_RECEIVER_NAME, arrayList.get(position).get("name"));
                            startActivity(intent);

                            break;

                    }
                });

        b.rvList.addOnItemTouchListener(touchListener);
    }


    // start call
    private void hitStartCallApi(final String strCallUserId, final String name, final String profileUrl) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("callUserId", strCallUserId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.startCall, json, true, true, new WebServicesCallback() {
            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonStartCall(response, strCallUserId, name, profileUrl);
            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonStartCall(JSONObject response, String userId, String name, String profileUrl) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject data = jsonObject.getJSONObject("data");
                AppSettings.putString(AppSettings.callId, data.getString("callId"));
                AppSettings.putString(AppSettings.startDateTime, data.getString("startDateTime"));
                AppSettings.putString(AppSettings.endDateTime, data.getString("endDateTime"));
                AppSettings.putString(AppSettings.callStatus, data.getString("status"));
                AppSettings.putString(AppSettings.createdAt, data.getString("createdAt"));
                AppSettings.putString(AppSettings._id, data.getString("_id"));
                AppSettings.putString(AppSettings.updatedAt, data.getString("updatedAt"));
                AppSettings.putString(AppSettings.__v, data.getString("__v"));
                voiceCall(userId, name, profileUrl);
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void voiceCall(String userId, String name, String profileUrl) {
        CallService.dial(mActivity, userId, name, profileUrl, false);
    }

    private void videoCall(String userId, String name, String profileUrl) {

        CallService.dial(mActivity, userId, name, profileUrl, true);

    }

    private void hitGetFriendListApi() {

        if (touchListener != null) {
            b.rvList.addOnItemTouchListener(touchListener);
        }

        WebServices.getApi(mActivity, AppUrls.myFriends, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseFriendListJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseFriendListJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("userId", jsonObject1.getString("userId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("profileImage", jsonObject1.getString("profileImage"));
                    hashMap.put("mutualFriend", jsonObject1.getString("mutualFriend"));
                    hashMap.put("count", jsonObject1.getString("count"));
                    hashMap.put("userChatId", jsonObject1.getString("userChatId"));
                    hashMap.put("AverageRating", jsonObject1.getString("AverageRating"));
                    hashMap.put("selfRating", jsonObject1.getString("selfRating"));
                    hashMap.put("remark", jsonObject1.getString("remark"));
                    hashMap.put("totalRating", jsonObject1.getString("totalRating"));
                    hashMap.put("channelName", jsonObject1.getString("channelName"));
                    hashMap.put("channelUrl", jsonObject1.getString("channelUrl"));
                    hashMap.put("type", "1");

                    arrayList.add(hashMap);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    private void hitSearchUserApi() {


        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("keyword", b.etSearch.getText().toString().trim());
            jsonObject.put("type", "1");//1=By name or mail

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.search, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSearchJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSearchJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("userId", jsonObject1.getJSONObject("user").getString("userId"));
                    hashMap.put("name", jsonObject1.getJSONObject("user").getString("name"));
                    hashMap.put("AverageRating", jsonObject1.getJSONObject("user").getString("AverageRating"));
                    hashMap.put("profileImage", jsonObject1.getJSONObject("user").getString("profileImage"));
                    hashMap.put("mutualFriend", jsonObject1.getString("mutualFriend"));
                    hashMap.put("count", jsonObject1.getString("count"));
                    hashMap.put("friendStatus", jsonObject1.getString("friendStatus"));
                    hashMap.put("channelName", "");
                    hashMap.put("channelUrl", "");
                    hashMap.put("totalRating", "0");
                    hashMap.put("selfRating", "0");
                    hashMap.put("remark", "");
                    hashMap.put("type", "2");

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

        switch (view.getId()) {

            case R.id.tvRequest:

                startActivity(new Intent(mActivity, FriendRequestListActivity.class));

                break;

        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_user_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.setIsRecyclable(false);

            holder.tvName.setText(data.get(position).get("name"));
            holder.ivProfile.setImageResource(R.drawable.ic_user_default);
            AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivProfile);
            holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("AverageRating")));

            if (data.get(position).get("type").equals("1") && data.get(position).get("selfRating").equals("0")) {
                holder.tvRateUser.setVisibility(View.VISIBLE);

            } else {
                holder.tvRateUser.setVisibility(View.GONE);
            }

            if (data.get(position).get("type").equals("1")) {
                holder.ivAction.setVisibility(View.GONE);
                holder.llBg.setVisibility(View.VISIBLE);
            } else {
                holder.ivAction.setVisibility(View.VISIBLE);
                holder.llBg.setVisibility(View.GONE);
                if (data.get(position).get("friendStatus").equals("0")) {
                    holder.ivAction.setImageResource(R.mipmap.send_request);
                } else if (data.get(position).get("friendStatus").equals("1")) {
                    holder.ivAction.setImageResource(0);
                } else {
                    holder.ivAction.setImageResource(0);
                }

            }

            if (!data.get(position).get("count").equals("0")) {

                holder.tvMutualFriends.setVisibility(View.VISIBLE);
                holder.rlMutualProfile.setVisibility(View.VISIBLE);
                holder.tvMutualFriends.setText(data.get(position).get("count") + " " + getString(R.string.mutualFriends));

                try {
                    JSONArray jsonArray = new JSONArray(data.get(position).get("mutualFriend"));

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (i == 0) {
                            AppUtils.loadPicassoImage(jsonObject.getString("profileImage"), holder.ivMutualProfile1);
                            holder.ivMutualProfile1.setVisibility(View.VISIBLE);
                        }
                        if (i == 1) {
                            AppUtils.loadPicassoImage(jsonObject.getString("profileImage"), holder.ivMutualProfile2);
                            holder.ivMutualProfile2.setVisibility(View.VISIBLE);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                holder.rlMutualProfile.setVisibility(View.GONE);
                holder.tvMutualFriends.setVisibility(View.GONE);
            }
            holder.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.size() >= position + 1) {
                        if (data.get(position).get("friendStatus").equals("0")) {
                            hitSendFriendRequestApi(data.get(position).get("userId"));
                        }
                    }

                }
            });
            holder.tvRateUser.setOnClickListener(v -> {

                Intent intent = new Intent(mActivity, RateUserActivity.class);
                intent.putExtra("userId", data.get(position).get("userId"));
                intent.putExtra("name", data.get(position).get("name"));
                intent.putExtra("profileImage", data.get(position).get("profileImage"));
                intent.putExtra("selfRating", data.get(position).get("selfRating"));
                intent.putExtra("remark", data.get(position).get("remark"));
                intent.putExtra("totalRating", data.get(position).get("totalRating"));
                startActivity(intent);

            });
            holder.ivAudioCall.setOnClickListener(v -> {

                hitStartCallApi(arrayList.get(position).get("userId"),
                        arrayList.get(position).get("name"),
                        arrayList.get(position).get("profileImage"));
                //  AppSettings.putString(AppSettings.callId,arrayList.get(position).get("userId"));
            });
            holder.ivVideoCall.setOnClickListener(v -> {
                videoCall(arrayList.get(position).get("userId"),
                        arrayList.get(position).get("name"),
                        arrayList.get(position).get("profileImage"));
            });
            holder.ivChat.setOnClickListener(v -> {
                Intent intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra("receiverId", arrayList.get(position).get("userId"));
                intent.putExtra(INTENT_KEY_RECEIVER_ID, arrayList.get(position).get("userId"));
                intent.putExtra(INTENT_KEY_CHANNEL_URL, arrayList.get(position).get("channelUrl"));
                intent.putExtra(INTENT_KEY_CHANNEL_TITLE, arrayList.get(position).get("channelName"));
                intent.putExtra(INTENT_KEY_RECEIVER_NAME, arrayList.get(position).get("name"));
                startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout rlFg, rlMutualProfile;

            ImageView ivProfile, ivAction, ivMutualProfile1, ivMutualProfile2, ivAudioCall, ivVideoCall, ivChat;

            TextView tvName, tvMutualFriends, tvRateUser;

            RatingBar ratingBar;
            LinearLayout llBg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                rlFg = itemView.findViewById(R.id.rlFg);
                rlMutualProfile = itemView.findViewById(R.id.rlMutualProfile);

                ivProfile = itemView.findViewById(R.id.ivProfile);
                ivAction = itemView.findViewById(R.id.ivAction);
                ivMutualProfile1 = itemView.findViewById(R.id.ivMutualProfile1);
                ivMutualProfile2 = itemView.findViewById(R.id.ivMutualProfile2);

                tvName = itemView.findViewById(R.id.tvName);
                tvMutualFriends = itemView.findViewById(R.id.tvMutualFriends);
                tvRateUser = itemView.findViewById(R.id.tvRateUser);

                ratingBar = itemView.findViewById(R.id.ratingBar);
                ivAudioCall = itemView.findViewById(R.id.ivAudioCall);
                ivVideoCall = itemView.findViewById(R.id.ivVideoCall);
                ivChat = itemView.findViewById(R.id.ivChat);
                llBg = itemView.findViewById(R.id.llBg);
            }
        }
    }

    private void hitSendFriendRequestApi(String id) {

        WebServices.postApi(mActivity, AppUrls.sendFriendRequest + "?id=" + id, null, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSendRequestJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSendRequestJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitSearchUserApi();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.friends), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hitGetFriendListApi();
        hitGetFriendRequestsCountApi();

    }
    private void hitGetFriendRequestsCountApi() {

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

        arrayListCount.clear();

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

                    arrayListCount.add(hashMap);
                }
                if (arrayListCount.size() != 0) {
                    b.tvRequest.setText(getString(R.string.requests)+"(" + arrayListCount.size()+")");
                } else
                {
                    b.tvRequest.setText(getString(R.string.requests));
                }

            }
            else{
                b.tvRequest.setText(getString(R.string.requests));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}