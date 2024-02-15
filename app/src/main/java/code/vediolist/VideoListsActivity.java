package code.vediolist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityVedioListsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.vediolist.adapters.AdapterVideoCategoryList;
import code.vediolist.adapters.Helper;
import code.videoEditor.VideoEditorActivity;
import code.view.BaseActivity;

public class VideoListsActivity extends BaseActivity implements View.OnClickListener {
    private ActivityVedioListsBinding b;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    Adapter adapter;
    AdapterReport adapterReport;
    TextView tvReport, tvCancel;
    private ArrayList<String> arrayCatList;
    private String report = "",isFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVedioListsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        inite();
    }

    private void setAdapter() {
        AdapterVideoCategoryList adapter;
        LinearLayoutManager manager = new LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false);
        b.rvCategory.setLayoutManager(manager);
        adapter = new AdapterVideoCategoryList(arrayCatList, mActivity);
        b.rvCategory.setAdapter(adapter);
    }


    private void inite() {
        b.ivBack.setOnClickListener(view -> onBackPressed());
        b.ivSettings.setOnClickListener(this);
        b.ivProfile.setOnClickListener(this);
        b.ivCreate.setOnClickListener(this);
        b.ivBack.setOnClickListener(this);
        arrayCatList = new ArrayList<>();
        arrayCatList.add("Add Video");
        arrayCatList.add("Create Video");
        setAdapter();
        adapter = new Adapter(arrayList);
        b.rvVideoList.setAdapter(adapter);


    }

    private void hitGetVideoListApi() {
//        listAllVideos
        WebServices.getApi(mActivity, AppUrls.getVideoList, true, true, new WebServicesCallback() {

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

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    JSONObject jsonObjUploaderDetail = jsonObj.getJSONObject("uploadersDetail");
                    JSONObject jsonVideoDetails = jsonObj.getJSONObject("videoDetails");
                    if (jsonVideoDetails.getString("type").equalsIgnoreCase("1"))
                    {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name", jsonObjUploaderDetail.getString("name"));
                        hashMap.put("profileImage", jsonObjUploaderDetail.getString("profileImage"));
                        hashMap.put("title", jsonVideoDetails.getString("title"));
                        hashMap.put("videoUrl", jsonVideoDetails.getString("videoUrl"));
                        hashMap.put("videoThumbnail", jsonVideoDetails.getString("videoThumbnail"));
                        hashMap.put("description", jsonVideoDetails.getString("description"));
                        hashMap.put("viewCount", jsonVideoDetails.getString("viewCount"));
                        hashMap.put("likes", jsonVideoDetails.getString("likes"));
                        hashMap.put("_id", jsonVideoDetails.getString("id"));
                        hashMap.put("dislikes", jsonVideoDetails.getString("dislikes"));
                        hashMap.put("createdAt", jsonVideoDetails.getString("createdAt"));
                        hashMap.put("isUserLiked", jsonVideoDetails.getString("isUserLiked"));
                        hashMap.put("isUserDisLike", jsonVideoDetails.getString("isUserDisLike"));
                        hashMap.put("totalCommentsCount", jsonObj.getString("totalCommentsCount"));
                        arrayList.add(hashMap);
                    }
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_video_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvUploadDateTime.setText(AppUtils.changeDateFormat2(data.get(position).get("createdAt")));
            holder.tvDesc.setText(data.get(position).get("description"));
            holder.tvTotalView.setText(getString(R.string.view) +" " + data.get(position).get("viewCount")+" ,");
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
                            else if (id==R.id.downloadvideo)
                            {
                                AppSettings.putString(AppSettings.KEY_selected_type, Environment.DIRECTORY_DCIM);
                                AppSettings.putString(AppSettings.KEY_selected_filename, data.get(position).get("description")+".mp4");
                                downloadFile(data.get(position).get("videoUrl"));
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
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb,ivMenu,ivProfileImage;
            TextView tvDesc,tvTotalView,tvUploadDateTime;
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
            HashMap<String,String>map=data.get(position);
            holder.tvReport.setText(map.get(Helper.report));
            holder.radioButton.setChecked(position == selectedPosition);
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                report = map.get(Helper.report);
                tvReport.setTextColor(ContextCompat.getColorStateList(mActivity,R.color.purple_700));
                notifyDataSetChanged();
            });
            holder.radioButton.setOnClickListener(v -> {
                selectedPosition = holder.getAdapterPosition();
                tvReport.setTextColor(ContextCompat.getColorStateList(mActivity,R.color.purple_700));
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
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSettings:
                PopupMenu popup = new PopupMenu(mActivity, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu2, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id== R.id.watchlater){
                            startActivity(new Intent(mActivity, WatchLaterActivity.class));
                            popup.dismiss();

                        }
                        else if(id== R.id.history){
                            startActivity(new Intent(mActivity, VideoHistoryActivity.class));
                            popup.dismiss();

                        }
                        else if(id== R.id.createVideo){
                            showDialog();
                            popup.dismiss();

                        }

                        else if(id== R.id.download){
                            startActivity(new Intent(mActivity, DownloadsActivity.class));
                            popup.dismiss();

                        }
                        else if(id== R.id.subscription){
                            startActivity(new Intent(mActivity, SubscriptionActivity.class));
                            popup.dismiss();

                        }
                        else if(id== R.id.yourvideos){
                            startActivity(new Intent(mActivity, YourVideoActivity.class));
                            popup.dismiss();

                        }

                        else if(id== R.id.shorts){
                            startActivity(new Intent(mActivity, ShortActivity.class).putExtra("videoId",""));
                            popup.dismiss();
                        }
                        else
                        {

                        }
                        return false;
                    }
                });
                popup.show();//
                break;
            case R.id.ivProfile:
                startActivity(new Intent(mActivity, YourVideoActivity.class));
                break;
            case R.id.ivCreate:
                showDialog();
                break;
            case R.id.ivBack:
                onBackPressed();
                break;


        }
    }
    private void showDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_select_option);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        TextView tvShortVideo, tvNormal;

        tvShortVideo = bottomSheetDialog.findViewById(R.id.tvShortVideo);
        tvNormal = bottomSheetDialog.findViewById(R.id.tvNormal);


        tvShortVideo.setOnClickListener(view -> {
            AppSettings.putString(AppSettings.isFrom,"Short");
            startActivity(new Intent(mActivity, VideoEditorActivity.class));
            bottomSheetDialog.dismiss();
        });
        tvNormal.setOnClickListener(view -> {
            AppSettings.putString(AppSettings.isFrom,"Long");
            startActivity(new Intent(mActivity, VideoEditorActivity.class));
            bottomSheetDialog.dismiss();
        });


    }

    @Override
    protected void onResume() {
        hitGetVideoListApi();
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), b.ivProfile);
        super.onResume();
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
    private void reportDialog(Activity mActivity,String videoId) {
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
          if (report.equalsIgnoreCase(""))
          {

          }
          else
          {
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
                AppUtils.showToastSort(mActivity,jsonObject.getString(AppConstants.resMsg));
               // JSONArray jsonArray = jsonObject.getJSONArray("data");
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString(AppConstants.resMsg));
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
        if (isFrom.equalsIgnoreCase("1"))
        {
            url =  AppUrls.notIntersted;
        }
        else
        {
            url =  AppUrls.WatchLater;
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
                AppUtils.showToastSort(mActivity,jsonObject.getString(AppConstants.resMsg));
                // JSONArray jsonArray = jsonObject.getJSONArray("data");
                hitGetVideoListApi();
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString(AppConstants.resMsg));
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
    private void downloadFile(String videoUrl) {
        String type = AppSettings.getString(AppSettings.KEY_selected_type);
        String fileName = AppSettings.getString(AppSettings.KEY_selected_filename);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait while your file is downloading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setDestinationInExternalPublicDir(type, fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

        new Thread(new Runnable() {
            @SuppressLint("Range")
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    Cursor cursor = downloadManager.query(query);
                    cursor.moveToFirst();
                    @SuppressLint("Range") int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    @SuppressLint("Range") int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                        progressDialog.dismiss();

                        File downloadDir = Environment.getExternalStoragePublicDirectory(type);
                        File files = new File(downloadDir, fileName);
                        String path = files.getAbsolutePath();
                        AppSettings.putString(AppSettings.KEY_selected_url, path);
//                        Intent i = new Intent(getApplicationContext(), VideoPlayerActivity.class);
//                        startActivity(i);

                    }

                    int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                    runOnUiThread(() -> progressDialog.setProgress(progress));
                    cursor.close();
                }
            }
        }).start();
    }
}