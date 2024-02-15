package code.vediolist;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityVedioHistoryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.vediolist.adapters.AdapterDownloadVideos;
import code.vediolist.adapters.AdapterTodayVideoList;
import code.vediolist.adapters.Helper;
import code.view.BaseActivity;

public class VideoHistoryActivity extends BaseActivity implements View.OnClickListener {
    private ActivityVedioHistoryBinding binding;
    private AdapterLongVideo adapterLong;
    AdapterReport adapterReport;
    private AdapterShortVideo adapterShortVideo;
    TextView tvReport, tvCancel;
    private String report = "", isFrom = "";
    private ArrayList<HashMap<String, String>> arrayListLong = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayListShort = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVedioHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inite();

    }


    private void inite() {
        binding.header.ivBack.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.history));
        adapterLong = new AdapterLongVideo(arrayListLong);
        binding.rvLongList.setAdapter(adapterLong);
        adapterShortVideo = new AdapterShortVideo(arrayListShort);
        binding.rvShortList.setAdapter(adapterShortVideo);
    }

    private void hitGetVideoListApi() {
        WebServices.getApi(mActivity, AppUrls.showWatchHistory, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {
                Log.v("response", response);
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), response, 9);
            }
        });
    }

    private void parseJson(JSONObject response) {
        arrayListLong.clear();
        arrayListShort.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonObj = jsonObject.getJSONObject("data");
                JSONArray jsonArrayLongVideos = jsonObj.getJSONArray("longVideos");
                for (int i = 0; i < jsonArrayLongVideos.length(); i++) {
                    JSONObject jsonObjLong = jsonArrayLongVideos.getJSONObject(i);
                    JSONObject jsonObjUploaderDetails = jsonObjLong.getJSONObject("uploadersDetail");
                    JSONObject jsonVideoDetail = jsonObjLong.getJSONObject("videoDetails");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", jsonObjUploaderDetails.getString("name"));
                    hashMap.put("profileImage", jsonObjUploaderDetails.getString("profileImage"));
                    //hashMap.put("title", jsonVideoDetail.getString("title"));
                    hashMap.put("videoUrl", jsonVideoDetail.getString("videoUrl"));
                    hashMap.put("videoThumbnail", jsonVideoDetail.getString("videoThumbnail"));
                    hashMap.put("description", jsonVideoDetail.getString("description"));
                    hashMap.put("viewCount", jsonVideoDetail.getString("viewCount"));
                    hashMap.put("likes", jsonVideoDetail.getString("likes"));
                    hashMap.put("_id", jsonVideoDetail.getString("id"));
                    hashMap.put("dislikes", jsonVideoDetail.getString("dislikes"));
                    hashMap.put("createdAt", jsonVideoDetail.getString("createdAt"));
                    hashMap.put("isUserLiked", jsonVideoDetail.getString("isUserLiked"));
                    hashMap.put("isUserDisLike", jsonVideoDetail.getString("isUserDisLike"));
                    hashMap.put("totalCommentsCount", jsonObjLong.getString("totalCommentsCount"));
                    arrayListLong.add(hashMap);

                }
                JSONArray jsonArrayShortVideos = jsonObj.getJSONArray("shortVideos");
                for (int i = 0; i < jsonArrayShortVideos.length(); i++) {
                    JSONObject jsonObjShort = jsonArrayShortVideos.getJSONObject(i);
                    JSONObject jsonObjUploaderDetails = jsonObjShort.getJSONObject("uploadersDetail");
                    JSONObject shortDetails = jsonObjShort.getJSONObject("videoDetails");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", jsonObjUploaderDetails.getString("name"));
                    hashMap.put("profileImage", jsonObjUploaderDetails.getString("profileImage"));
                    hashMap.put("title", shortDetails.getString("title"));
                    hashMap.put("videoUrl", shortDetails.getString("videoUrl"));
                    hashMap.put("videoThumbnail", shortDetails.getString("videoThumbnail"));
                    hashMap.put("description", shortDetails.getString("description"));
                    hashMap.put("viewCount", shortDetails.getString("viewCount"));
                    hashMap.put("likes", shortDetails.getString("likes"));
                    hashMap.put("_id", shortDetails.getString("id"));
                    hashMap.put("dislikes", shortDetails.getString("dislikes"));
                    hashMap.put("createdAt", shortDetails.getString("createdAt"));
                    hashMap.put("isUserLiked", shortDetails.getString("isUserLiked"));
                    hashMap.put("isUserDisLike", shortDetails.getString("isUserDisLike"));
                    // hashMap.put("totalCommentsCount", jsonObjShort.getString("totalCommentsCount"));
                    arrayListShort.add(hashMap);

                }

            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapterLong.notifyDataSetChanged();
        adapterShortVideo.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
        }
    }

    private class AdapterLongVideo extends RecyclerView.Adapter<AdapterLongVideo.MyViewHolder> {
        ArrayList<HashMap<String, String>> data;

        private AdapterLongVideo(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterLongVideo.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_video_list, viewGroup, false);
            return new AdapterLongVideo.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterLongVideo.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvUploadDateTime.setText(AppUtils.changeDateTimeFormat(data.get(position).get("createdAt")));
            holder.tvDesc.setText(data.get(position).get("description"));
            holder.tvTotalView.setText(getString(R.string.view) + " " + data.get(position).get("viewCount") + " ,");
            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);
            AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivProfileImage);
            holder.ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mActivity, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if(id== R.id.report){
                                reportDialog(mActivity,data.get(position).get("_id"));
                            }
                            else if (id==R.id.notinterested)
                            {
                                isFrom = "1";
                                hitNotInterestedApi(data.get(position).get("_id"));
                            }
                            else if (id==R.id.watchlater)
                            {
                                isFrom = "2";
                                hitNotInterestedApi(data.get(position).get("_id"));
                            }
                            else if (id==R.id.share)
                            {
                                hitShareVideoApi(data.get(position).get("_id"),data.get(position).get("videoUrl"));
                            }


                            else
                            {

                            }
                            return false;
                        }
                    });
                    popup.show();//
                }
            });
            holder.itemView.setOnClickListener(v -> {
                startActivity(new Intent(mActivity, VideoDetailsActivity.class).putExtra("videoId",
                        data.get(position).get("_id")));
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb, ivMenu, ivProfileImage;
            TextView tvDesc, tvTotalView, tvUploadDateTime;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivThumb);
                tvDesc = itemView.findViewById(R.id.tvDesc);
                tvTotalView = itemView.findViewById(R.id.tvTotalView);
                tvUploadDateTime = itemView.findViewById(R.id.tvUploadDateTime);
                ivMenu = itemView.findViewById(R.id.ivMenu);
                ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            }
        }
    }

    private class AdapterShortVideo extends RecyclerView.Adapter<AdapterShortVideo.MyViewHolder> {
        ArrayList<HashMap<String, String>> data;

        private AdapterShortVideo(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterShortVideo.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_short_video_list, viewGroup, false);
            return new AdapterShortVideo.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterShortVideo.MyViewHolder holder, final int position) {
            holder.tvDesc.setText(data.get(position).get("description"));
            holder.tvName.setText(data.get(position).get("name"));
            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);
            holder.itemView.setOnClickListener(view -> {
                startActivity(new Intent(mActivity, ShortActivity.class).putExtra("videoId", data.get(position).get("_id")));
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb, ivMenu;
            TextView tvDesc, tvName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivThumb);
                tvDesc = itemView.findViewById(R.id.tvDesc);
                tvName = itemView.findViewById(R.id.tvName);
                ivMenu = itemView.findViewById(R.id.ivMenu);
            }
        }
    }

    @Override
    protected void onResume() {
        hitGetVideoListApi();
        super.onResume();
    }


    //    private void setAdapter() {
//        AdapterTodayVideoList adapter;
//        LinearLayoutManager manager = new LinearLayoutManager(VideoHistoryActivity.this, RecyclerView.HORIZONTAL, false);
//        binding.rvtodayList.setLayoutManager(manager);
//        adapter = new AdapterTodayVideoList(arrayList,arrimagelist, VideoHistoryActivity.this);
//        binding.rvtodayList.setAdapter(adapter);
//    }
//    private void setAdapter2() {
//        AdapterDownloadVideos adapter;
//        LinearLayoutManager manager = new LinearLayoutManager(VideoHistoryActivity.this, RecyclerView.VERTICAL, false);
//        binding.rvtodayothreList.setLayoutManager(manager);
//        adapter = new AdapterDownloadVideos(getlist(),arrimagelist1, VideoHistoryActivity.this);
//        binding.rvtodayothreList.setAdapter(adapter);
//    }
//    public ArrayList<HashMap<String, String>> getlist() {
//        ArrayList<HashMap<String, String>> arrganderlist = new ArrayList<HashMap<String, String>>();
//        String[] strdesc = {"Compilation | Everything Belongs to Allah 33 Min | Omar..","Built it in Figma:Responsive website design Part 1","Compilation | Everything Belongs to Allah 33 Min | Omar..", "Built it in Figma:Responsive website design Part 1","Compilation | Everything Belongs to Allah 33 Min | Omar.."};
//        String[] strview = {"5.5M views", "3.5M views","5.5M views", "3.5M views","5.5M views"};
//        String[] strsubdisc = {"Omar & Hana - Islamic", "Omar & Hana - Islamic","Omar & Hana - Islamic", "Omar & Hana - Islamic","Omar & Hana - Islamic"};
//
//        for (int i = 0; i < strdesc.length; i++) {
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.put(Helper.Key_view, strview[i]);
//            map.put(Helper.Key_desc, strdesc[i]);
//            map.put(Helper.Key_subdesc, strsubdisc[i]);
//            arrganderlist.add(map);
//        }
//        return arrganderlist;
//
//
//    }
    private class AdapterReport extends RecyclerView.Adapter<AdapterReport.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;

        private int selectedPosition = RecyclerView.NO_POSITION;

        private AdapterReport(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterReport.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_report, viewGroup, false);
            return new AdapterReport.MyViewHolder(view);
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        @Override
        public void onBindViewHolder(@NonNull AdapterReport.MyViewHolder holder, final int position) {
            HashMap<String, String> map = data.get(position);
            holder.tvReport.setText(map.get(Helper.report));
            holder.radioButton.setChecked(position == selectedPosition);
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                report = map.get(Helper.report);
                tvReport.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.purple_700));
                notifyDataSetChanged();
            });
            holder.radioButton.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                tvReport.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.purple_700));
                report = map.get(Helper.report);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvReport;
            RadioButton radioButton;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvReport = itemView.findViewById(R.id.tvReport);
                radioButton = itemView.findViewById(R.id.reportBtn);
            }
        }
    }

    private ArrayList<HashMap<String, String>> getList() {
        ArrayList<HashMap<String, String>> arrReport = new ArrayList<HashMap<String, String>>();
        String[] strReport = {"Sexual content"
                , "Violent or repulsive content", "Hateful or abusive content"
                , "Harmful or dangerous acts", "Spam or misleading", "Child abuse"};

        for (int i = 0; i < strReport.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Helper.report, strReport[i]);
            arrReport.add(map);
        }
        return arrReport;


    }

    private void reportDialog(Activity mActivity, String videoId) {
        final Dialog bottomSheetDialog = new Dialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_report);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
        RecyclerView rvReportList;
        tvReport = bottomSheetDialog.findViewById(R.id.tvReport);
        rvReportList = bottomSheetDialog.findViewById(R.id.rvReportList);
        tvCancel = bottomSheetDialog.findViewById(R.id.tvCancel);
        adapterReport = new AdapterReport(getList());
        rvReportList.setAdapter(adapterReport);
        tvCancel.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

        });
        tvReport.setOnClickListener(v -> {
            if (report.equalsIgnoreCase("")) {

            } else {
                hitReportApi(videoId);
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void hitReportApi(final String videoId) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            jsonObject.put("reportMessage", report);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.reportVideo, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonReport(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonReport(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
                // JSONArray jsonArray = jsonObject.getJSONArray("data");
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitNotInterestedApi(final String videoId) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = "";
        if (isFrom.equalsIgnoreCase("1")) {
            url = AppUrls.notIntersted;
        } else {
            url = AppUrls.WatchLater;
        }
        WebServices.postApi(mActivity, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonNotInterested(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonNotInterested(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
                // JSONArray jsonArray = jsonObject.getJSONArray("data");
                hitGetVideoListApi();
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void hitShareVideoApi(final String id,final String videoUrl) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("sharedToUserId", AppSettings.getString(AppSettings.userId));
            jsonObject.put("videoId", id);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.shareVideo, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonShareVideo(response,id,videoUrl);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }
    private void parseJsonShareVideo(JSONObject response,String id,String videoUrl) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                shareDeepLink(id,videoUrl);
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void shareDeepLink(String videoId,String videoUrl) {
        String url = getString(R.string.httpDeepLink);
        String deepLinkUri = videoUrl;
        // Share deep link using an Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, deepLinkUri);

        // Create a chooser to show available sharing options
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share your Link");

        // Check if there are activities available to handle the intent
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        }

        //  AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),data.toString(),9);
    }
}