package code.vediolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityDownloadsBinding;
import com.hathme.android.databinding.ActivityWatchLaterBinding;

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
import code.view.BaseActivity;

public class WatchLaterActivity extends BaseActivity implements View.OnClickListener {
    ActivityWatchLaterBinding binding;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    String isFrom = "";
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchLaterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inite();
    }


    private void inite() {
        binding.header.ivBack.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.watch_later));

        adapter = new Adapter(arrayList);
        binding.rvVideoList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
        }
    }


    private void hitGetVideoListApi() {
        WebServices.getApi(mActivity, AppUrls.getwatchLaterVideos, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
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
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", jsonObjUploaderDetail.getString("name"));
                    hashMap.put("profileImage", jsonObjUploaderDetail.getString("profileImage").toString());
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
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_download_video_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.tvDesc.setText(data.get(position).get("description"));
            holder.tvTotalView.setText(data.get(position).get("viewCount") + " " + getString(R.string.view));
            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivMenu.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(mActivity, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu5, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        int id = item.getItemId();
                        if (id == R.id.removewatchlater) {
                            isFrom = "2";
                            hitNotInterestedApi(data.get(position).get("_id"));
                        }
                        else if (id==R.id.hide)
                        {
                            isFrom = "1";
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
                        else {

                        }
                        return false;
                    }
                });
                popup.show();//

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
            ImageView ivThumb, ivMenu;
            TextView tvDesc, tvTotalView, tvTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivThumb);
                tvDesc = itemView.findViewById(R.id.tvDesc);
                tvTotalView = itemView.findViewById(R.id.tvViewCount);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                ivMenu = itemView.findViewById(R.id.ivMenu);
            }
        }
    }

    @Override
    protected void onResume() {
        hitGetVideoListApi();
        super.onResume();
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
            url = AppUrls.HideWatchLaterVideos;
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