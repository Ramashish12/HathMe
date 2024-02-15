package code.vediolist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityYourVideoBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.vediolist.adapters.AdapterProfileVideoList;
import code.vediolist.adapters.AdapterVideoList;
import code.view.BaseActivity;

public class YourVideoActivity extends BaseActivity implements View.OnClickListener {
    private ActivityYourVideoBinding binding;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    Adapter adapter;
    String period = "1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_video);
        binding = ActivityYourVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inite();
    }

    private void inite() {
        binding.header.ivBack.setOnClickListener(this);
        binding.rlAll.setOnClickListener(this);
        binding.rlShort.setOnClickListener(this);
        binding.rlVideos.setOnClickListener(this);
        binding.rlLive.setOnClickListener(this);
        binding.rlLike.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.your_video));
        binding.tvtotalreviews.setPaintFlags(binding.tvtotalreviews.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        GridLayoutManager manager = new GridLayoutManager(mActivity, 3);
        binding.rvList.setLayoutManager(manager);
        adapter = new Adapter(arrayList);
        binding.rvList.setAdapter(adapter);
    }
    private void hitGetVideoListApi(String period) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("period", AppUtils.returnInt(period));
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.listAllVideos, json, true, true, new WebServicesCallback() {

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
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject object = jsonObject.getJSONObject("data");
                JSONArray jsonArray = object.getJSONArray("allVideos");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    JSONObject jsonObjUploaderDetail = jsonObj.getJSONObject("uploadersDetail");
                    JSONObject jsonVideoDetails = jsonObj.getJSONObject("videoDetails");
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name", jsonObjUploaderDetail.getString("name"));
                        hashMap.put("profileImage", jsonObjUploaderDetail.getString("profileImage"));
                        hashMap.put("videoUrl", jsonVideoDetails.getString("videoUrl"));
                        hashMap.put("videoThumbnail", jsonVideoDetails.getString("videoThumbnail"));
                        hashMap.put("description", jsonVideoDetails.getString("description"));
                        hashMap.put("viewCount", jsonVideoDetails.getString("viewCount"));
                        hashMap.put("likes", jsonVideoDetails.getString("likes"));
                        hashMap.put("_id", jsonVideoDetails.getString("id"));
                        hashMap.put("type", jsonVideoDetails.getString("type"));
                        hashMap.put("dislikes", jsonVideoDetails.getString("dislikes"));
                        hashMap.put("createdAt", jsonVideoDetails.getString("createdAt"));
                        hashMap.put("isUserLiked", jsonVideoDetails.getString("isUserLiked"));
                        hashMap.put("isUserDisLike", jsonVideoDetails.getString("isUserDisLike"));
                        hashMap.put("totalCommentsCount", jsonObj.getString("totalCommentsCount"));
                        arrayList.add(hashMap);

                }
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString("resMsg"));

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_profile_video_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);

            holder.itemView.setOnClickListener(v -> {
                if (data.get(position).get("type").equalsIgnoreCase("1"))
                {
                    startActivity(new Intent(mActivity, VideoDetailsActivity.class).putExtra("videoId",
                            data.get(position).get("_id")));
                }
                else
                {
                    startActivity(new Intent(mActivity, ShortActivity.class).putExtra("videoId", data.get(position).get("_id")));
                }

            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivThumb);

            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.rlAll:
                hitGetVideoListApi("1");
                break;
            case R.id.rlShort:
                hitGetVideoListApi("2");
                break;
            case R.id.rlVideos:
                hitGetVideoListApi("4");
                break;
            case R.id.rlLive:

                break;
            case R.id.rlLike:
                hitGetVideoListApi("5");
                break;

        }
    }

    @Override
    protected void onResume() {
        hitGetDetailsApi(period);
        hitGetVideoListApi(period);
        super.onResume();
    }

    private void hitGetDetailsApi(String period) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("period", AppUtils.returnInt(period));
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.listAllVideos, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonDetails(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }
    private void parseJsonDetails(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject object = jsonObject.getJSONObject("data");
                JSONObject objectUserDetails = object.getJSONObject("userDetails");
                AppUtils.loadPicassoImage(objectUserDetails.getString("profileImage"), binding.ivProfile);
                binding.tvUserName.setText(objectUserDetails.getString("name"));
                binding.tvEmailId.setText(" : "+objectUserDetails.getString("email"));
                binding.tvMobileNo.setText("+91-"+objectUserDetails.getString("mobile"));
                binding.tvTotalPost.setText(objectUserDetails.getString("totalPosts"));
                binding.tvTotalFollowers.setText(objectUserDetails.getString("followers"));
                binding.tvTotalFollowing.setText(objectUserDetails.getString("following"));
                binding.tvReview.setText(objectUserDetails.getString("rating"));
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString("resMsg"));

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}