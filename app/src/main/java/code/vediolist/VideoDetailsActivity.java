package code.vediolist;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityVideoDetailsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import code.activity.MainActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.vediolist.Model.VideoModel;
import code.vediolist.adapters.AdapterShortVideosList;
import code.vediolist.adapters.AdapterStoriesList;
import code.vediolist.adapters.AdapterVideoCategoryList;
import code.vediolist.adapters.Helper;
import code.view.BaseActivity;

public class VideoDetailsActivity extends BaseActivity implements View.OnClickListener {
    private ActivityVideoDetailsBinding binding;
    private String videoId = "", videoUrl = "", channelId = "", isFromSubscribe = "", subscriptionCount = "",
            isFrom = "1", report = "",viewYesNo = "",goBack = "";
    BottomSheetDialog bottomSheetDialog;
    private ExoPlayer simpleExoPlayer;
    private Adapter adapter;
    private AdapterLongVideo adapterLongVideo;
    private AdapterShortVideo adapterShortVideo;
    EditText etComment, etComments;
    ImageView ivSend;
    String desc = "";
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayListLong = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayListShort = new ArrayList<>();
    private Uri uri;
    AdapterReport adapterReport;
    TextView tvReport, tvCancel;
    private CountDownTimer countDownTimer;
     BottomSheetDialog bottomSheetDialog2;
    boolean isPlaying = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inite();
    }

    private void inite() {
        binding.header.tvHeader.setText(getString(R.string.videosDetails));
        binding.playerView.setOnClickListener(this);
        binding.header.ivBack.setOnClickListener(this);
        binding.ivBackWord.setOnClickListener(this);
        binding.ivPlay.setOnClickListener(this);
        binding.ivPause.setOnClickListener(this);
        binding.ivForward.setOnClickListener(this);
        binding.ivLandScap.setOnClickListener(this);
        binding.rlLike.setOnClickListener(this);
        binding.rlDislike.setOnClickListener(this);
        binding.rlCommentView.setOnClickListener(this);
        binding.rlCopyLink.setOnClickListener(this);
        binding.rlShareVideo.setOnClickListener(this);
        binding.tvSubscribe.setOnClickListener(this);
        binding.rlDownload.setOnClickListener(this);

        adapterLongVideo = new AdapterLongVideo(arrayListLong);
        binding.rvLongVideoList.setAdapter(adapterLongVideo);
        binding.ProgressBar.setVisibility(View.VISIBLE);

        uri = getIntent().getData();
        if (uri != null) {
            List<String> params = uri.getPathSegments();
            String id = params.get(params.size() - 1);
            videoId = "" + id;
        } else {
            // checking if the uri is null or not.
            videoId = getIntent().getStringExtra("videoId");
        }
        adapterShortVideo = new AdapterShortVideo(arrayListShort);
        binding.rvSort.setAdapter(adapterShortVideo);
        for (int i = 0; i < binding.lnAllBtn.getChildCount(); i++) {
            View view = binding.lnAllBtn.getChildAt(i);
            view.setEnabled(false); // Or whatever you want to do with the view.
        }
        for (int i = 0; i < binding.llComments.getChildCount(); i++) {
            View view = binding.llComments.getChildAt(i);
            view.setEnabled(false); // Or whatever you want to do with the view.
        }
        bottomSheetDialog2 = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog2.setContentView(R.layout.dialog_message);
    }

    private void hitGetVideoDetailsApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.videoDetail, json, true, true, new WebServicesCallback() {

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
        arrayListLong.clear();
        arrayListShort.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject jsonObj = jsonObject.getJSONObject("data");
                JSONObject jsonObjUploaderDetail = jsonObj.getJSONObject("uploadersDetail");
                JSONObject jsonObjVideoDetails = jsonObj.getJSONObject("videoDetails");
                JSONObject jsonObjChannelDetail = jsonObj.getJSONObject("channelDetail");
                JSONObject jsonLatestComment = jsonObj.getJSONObject("latestComment");
                if (jsonObjVideoDetails.getString("isUserLiked").equalsIgnoreCase("1")) {
                    binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
                } else {
                    binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                }
                if (jsonObjVideoDetails.getString("isUserDisLike").equalsIgnoreCase("1")) {
                    binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
                } else {
                    binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                }
                if (jsonObj.getString("isSubscribed").equalsIgnoreCase("0")) {
                    binding.tvSubscribe.setText(getString(R.string.subscribe));
                } else {
                    binding.tvSubscribe.setText(getString(R.string.unsubscribe));
                }
                binding.tvTotalView.setText(jsonObjVideoDetails.getString("viewCount") + " " + getString(R.string.view) + "  " + jsonObjVideoDetails.getString("createdAt"));
                binding.tvDesc.setText(jsonObjVideoDetails.getString("description"));
                binding.tvTotaleLikeCount.setText(jsonObjVideoDetails.getString("likes"));
                binding.tvDisLikeCount.setText(jsonObjVideoDetails.getString("dislikes"));
                binding.tvChannelName.setText(jsonObjUploaderDetail.getString("name"));
                AppUtils.loadPicassoImage(jsonObjUploaderDetail.getString("profileImage"), binding.ivProfile);
                binding.tvTotalComments.setText(jsonObj.getString("totalCommentsCount"));
                if (jsonLatestComment.getString("comment").equalsIgnoreCase("")) {
                    binding.tvCommentMsg.setText(getString(R.string.NoCommentYet));
                    binding.tvCommentShortName.setText(AppSettings.getString(AppSettings.name));
                } else {
                    binding.tvCommentMsg.setText(jsonLatestComment.getString("comment"));
                    binding.tvCommentShortName.setText(jsonLatestComment.getString("commenterName"));
                }
                subscriptionCount = " " + jsonObjChannelDetail.getString("subscribersCount");
                binding.tvTotalSubscriber.setText(subscriptionCount);
                videoUrl = jsonObjVideoDetails.getString("videoUrl");
                channelId = jsonObjChannelDetail.getString("channelId");
                desc = jsonObjVideoDetails.getString("description");
                initializePlayer(videoUrl);

                JSONArray jsonArrayLongVideos = jsonObj.getJSONArray("longVideos");
                for (int i = 0; i < jsonArrayLongVideos.length(); i++) {
                    JSONObject jsonObjLong = jsonArrayLongVideos.getJSONObject(i);
                    JSONObject jsonObjUploaderDetails = jsonObjLong.getJSONObject("uploadersDetail");
                    JSONObject jsonVideoDetail = jsonObjLong.getJSONObject("videoDetails");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", jsonObjUploaderDetails.getString("name"));
                    hashMap.put("profileImage", jsonObjUploaderDetails.getString("profileImage"));
                    hashMap.put("title", jsonVideoDetail.getString("title"));
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
                JSONArray jsonArrayShortVideos = jsonObj.getJSONArray("ShortVideos");
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
        adapterLongVideo.notifyDataSetChanged();
        adapterShortVideo.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                if (countDownTimer!=null)
                {
                    countDownTimer.cancel();
                }
                onBackPressed();
                break;
            case R.id.rlLike:
                isFrom = "2";
                hitLikeDislikeApi();
                break;
            case R.id.rlDislike:
                isFrom = "3";
                hitLikeDislikeApi();
                break;
            case R.id.rlCommentView:
                showBottomSheetDialog();
                break;
            case R.id.rlCopyLink:
                copyToClipboard(videoUrl);
                break;
            case R.id.rlShareVideo:
                hitShareVideoApi(videoId);
                break;
            case R.id.tvSubscribe:
                if (binding.tvSubscribe.getText().toString().equalsIgnoreCase("Subscribe")) {
                    isFromSubscribe = "1";
                    hitSubscribeApi();

                } else {
                    isFromSubscribe = "2";
                    hitUnSubscribeApi();
                }
                break;

            case R.id.rlDownload:
//                File subdirectory = FileUtils.createCacheSubdirectory(this, "videos");
//
//                if (subdirectory != null) {
                // Use the subdirectory as needed
                // For example, you can save downloaded videos in this directory
                AppSettings.putString(AppSettings.KEY_selected_type, Environment.DIRECTORY_DCIM);
                AppSettings.putString(AppSettings.KEY_selected_filename, desc+".mp4");
                downloadFile();


                break;

        }
    }

    private void shareDeepLink(String videoId) {
        String url = videoUrl;
        String deepLinkUri = url;
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

    private void initializePlayer(String videoUrl) {
        DataSource.Factory mediaDataSourceFactory = new DefaultDataSource.Factory(this);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));
        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(mediaDataSourceFactory);
        simpleExoPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
        simpleExoPlayer.addMediaSource(mediaSource);
        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    if (viewYesNo.equalsIgnoreCase("No"))
                    {
                        startTimer();
                    }
                    binding.ProgressBar.setVisibility(View.GONE);
                }
            }
        });
        simpleExoPlayer.prepare();
        isPlaying = simpleExoPlayer.getPlayWhenReady();
        simpleExoPlayer.setPlayWhenReady(true);
        binding.playerView.setPlayer(simpleExoPlayer);
        binding.playerView.requestFocus();
    }

    private void releasePlayer() {
        simpleExoPlayer.release();
    }

    private void hitLikeDislikeApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = "";

        if (isFrom.equals("1")) {
            url = AppUrls.viewVideo;
        } else if (isFrom.equals("2")) {
            url = AppUrls.likeVideo;
        } else if (isFrom.equals("3")) {
            url = AppUrls.dislikeVideo;
        }
        WebServices.postApi(mActivity, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonLikeDislike(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonLikeDislike(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject jsonObj = jsonObject.getJSONObject("data");
                binding.tvTotaleLikeCount.setText(jsonObj.getString("TotalLikedCount"));
                binding.tvDisLikeCount.setText(jsonObj.getString("TotalDisLikeCount"));
                if (isFrom.equals("2")) {
                    if (jsonObj.getString("message").equalsIgnoreCase("Liked"))
                    {
                        binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.red));
                        binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                    }
                    else
                    {
                        binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                        binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                    }

                } else if (isFrom.equals("3")) {
                    if (jsonObj.getString("message").equalsIgnoreCase("DisLike"))
                    {
                        binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                        binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.black));
                    }
                    else
                    {
                        binding.ivLike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                        binding.ivDislike.setColorFilter(ContextCompat.getColor(mActivity, R.color.gray));
                    }

                }

            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        hitViewVideoApi();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (bottomSheetDialog2!=null)
        {
            bottomSheetDialog2.dismiss();
        }
        if (simpleExoPlayer!=null)
        {
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (simpleExoPlayer!=null)
        {
            releasePlayer();
        }
        if (bottomSheetDialog2!=null)
        {
            bottomSheetDialog2.dismiss();
        }
        super.onStop();
    }



    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_comment_list);
        bottomSheetDialog.setCancelable(true);
        RecyclerView rvList = bottomSheetDialog.findViewById(R.id.rvList);
        ImageView ivProfile = bottomSheetDialog.findViewById(R.id.ivProfileImage);
        ivSend = bottomSheetDialog.findViewById(R.id.ivSend);
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), ivProfile);
        hitCommentListApi();
        adapter = new Adapter(arrayList);
        rvList.setAdapter(adapter);
        etComment = bottomSheetDialog.findViewById(R.id.etComments);
        if (!AppSettings.getString(AppSettings.commentsMsg).equalsIgnoreCase("")) {
            etComment.setText(AppSettings.getString(AppSettings.commentsMsg));
            ivSend.setVisibility(View.VISIBLE);
        } else {
            ivSend.setVisibility(View.GONE);
        }
        etComment.setOnClickListener(view -> {
            showBottomSheetDialogSendComment();
        });
        bottomSheetDialog.show();

        // getAllVideoComments
    }

    private void showBottomSheetDialogSendComment() {
        bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_send_comment);
        bottomSheetDialog.setCancelable(true);
        ImageView ivProfile = bottomSheetDialog.findViewById(R.id.ivProfileImage);
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), ivProfile);
        etComments = bottomSheetDialog.findViewById(R.id.etComments);
        ImageView ivSend = bottomSheetDialog.findViewById(R.id.ivSend);
        if (!AppSettings.getString(AppSettings.commentsMsg).equalsIgnoreCase("")) {
            etComments.setText(AppSettings.getString(AppSettings.commentsMsg));

        }
        etComments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                AppSettings.putString(AppSettings.commentsMsg, etComments.getText().toString().trim());
                etComment.setText(etComments.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ivSend.setOnClickListener(view -> {
            if (AppSettings.getString(AppSettings.commentsMsg).equalsIgnoreCase("")) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.pleaseEnterYourComment), 8);
            } else {
                hitCommentApi(AppSettings.getString(AppSettings.commentsMsg));
            }
        });
        bottomSheetDialog.show();

        // getAllVideoComments
    }

    private void hitCommentListApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.listAllVideoComment;

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonComment(response);

            }

            @Override
            public void OnFail(String response) {
                Log.v("error", response);
            }
        });

    }

    private void parseJsonComment(JSONObject response) {
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject jsonObjects = jsonObject.getJSONObject("data");
                JSONArray jsonArray = jsonObjects.getJSONArray("comments");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    JSONObject jsonLatestComment = jsonObj.getJSONObject("commentByUser");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObj.getString("_id"));
                    hashMap.put("videoId", jsonObj.getString("videoId"));
                    hashMap.put("commentByUserId", jsonObj.getString("commentByUserId"));
                    hashMap.put("comment", jsonObj.getString("comment"));
                    hashMap.put("status", jsonObj.getString("status"));
                    hashMap.put("createdAt", jsonObj.getString("createdAt"));
                    hashMap.put("updatedAt", jsonObj.getString("updatedAt"));
                    hashMap.put("name", jsonLatestComment.getString("name"));
                    hashMap.put("profileImage", jsonLatestComment.getString("profileImage"));

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

    private void hitCommentApi(String comment) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            jsonObject.put("comment", comment);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("body", json.toString());
        String url = AppUrls.commentVideo;

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonComments(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonComments(JSONObject response) {
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppSettings.putString(AppSettings.commentsMsg, "");
                JSONObject jsonObj = jsonObject.getJSONObject("data");
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.dismiss();
                    etComment.setText("");
                    ivSend.setVisibility(View.GONE);
                    binding.tvCommentMsg.setText(jsonObj.getString("comment"));
                    binding.tvTotalComments.setText(jsonObj.getString("totalCommentCount"));
                    binding.tvCommentShortName.setText(jsonObj.getString("name"));
                    //  binding.rlCommentView.setVisibility(View.VISIBLE);
                    hitCommentListApi();
                }
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_comment_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {
//            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivProfileImage);

            holder.tvCommentMsg.setText(data.get(position).get("comment"));
            holder.tvCommentShortName.setText(AppUtils.capitalizeFirstLetter(data.get(position).get("name")));
            holder.tvName.setText(data.get(position).get("name") + " " + AppUtils.changeDateTimeFormat(data.get(position).get("createdAt")));

            //AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivProfileImage);
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivProfileImage;
            TextView tvName, tvCommentMsg, tvCommentShortName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvCommentMsg = itemView.findViewById(R.id.tvCommentMsg);
                tvCommentShortName = itemView.findViewById(R.id.tvCommentShortName);
                ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            }
        }
    }

    private void hitViewVideoApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.viewVideo;

        WebServices.postApi(mActivity, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonViewVideo(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonViewVideo(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitGetVideoDetailsApi();
                for (int i = 0; i < binding.lnAllBtn.getChildCount(); i++) {
                    View view = binding.lnAllBtn.getChildAt(i);
                    view.setEnabled(true); // Or whatever you want to do with the view.
                }
                for (int i = 0; i < binding.llComments.getChildCount(); i++) {
                    View view = binding.llComments.getChildAt(i);
                    view.setEnabled(true); // Or whatever you want to do with the view.
                }
               viewYesNo = "Yes";
               goBack = "1";
            } else {
                viewYesNo = "No";
                hitGetVideoDetailsApi();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void copyToClipboard(String textToCopy) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("", textToCopy);
        AppUtils.showToastSort(mActivity, getString(R.string.copyLink));
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
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
                            if (id == R.id.report) {
                                reportDialog(mActivity, data.get(position).get("_id"));
                            } else if (id == R.id.notinterested) {
                                isFrom = "1";
                                hitNotInterestedApi(data.get(position).get("_id"));
                            } else if (id == R.id.watchlater) {
                                isFrom = "2";
                                hitNotInterestedApi(data.get(position).get("_id"));
                            } else if (id == R.id.share) {
                                hitShareVideoApi(data.get(position).get("_id"));
                            } else {

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
                if (countDownTimer!=null)
                {
                    countDownTimer.cancel();
                }
                if (viewYesNo.equalsIgnoreCase("Yes"))
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

    private void hitSubscribeApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("channelId", channelId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.subscribeChannel, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonSubscribe(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), response, 9);
            }
        });
    }

    private void hitUnSubscribeApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("channelId", channelId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.unSubscribeChannel, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonSubscribe(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), response, 9);
            }
        });
    }

    private void parseJsonSubscribe(JSONObject response) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
                if (isFromSubscribe.equalsIgnoreCase("1")) {

                    binding.tvSubscribe.setText(getString(R.string.unsubscribe));
                } else {
                    binding.tvSubscribe.setText(getString(R.string.subscribe));
                }
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
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

            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
            }


        } catch (JSONException e) {
            e.printStackTrace();
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


    private void downloadFile() {
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

    public static String generateRandomFileName() {
        // Get current timestamp
        long timestamp = System.currentTimeMillis();

        // Format timestamp to include date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date(timestamp));

        // Generate a random number to add to the file name
        Random random = new Random();
        int randomNumber = random.nextInt(10000); // Adjust the range as needed

        // Combine timestamp and random number to create a unique file name
        String randomFileName = "file_" + formattedTimestamp + "_" + randomNumber + ".mp4";

        return randomFileName;
    }

    private void hitShareVideoApi(final String id) {
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

                parseJsonShareVideo(response, id);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonShareVideo(JSONObject response, String id) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                shareDeepLink(id);
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        long durationInMillis = 5000; // 5 seconds
        countDownTimer = new CountDownTimer(durationInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                updateTimerText(millisUntilFinished);
            }

            public void onFinish() {
                // Timer finished, perform actions if needed
                // updateTimerText(0);
                togglePlayPause();
                showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.insufficientBalance), 1);
                goBack = "1";
            }
        };

        countDownTimer.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        String timerText = String.format("%2d", seconds);

    }

    private void togglePlayPause() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(false);
        }
    }
    private void showMessageDialog(Activity mActivity, String title, String message, int from) {

        bottomSheetDialog2.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog2.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog2.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog2.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog2.findViewById(R.id.tvContinue);

        tvTitle.setText(title);
        tvMessage.setText(message);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog2.dismiss();

            if (from == 1) {
                mActivity.onBackPressed();
            } else if (from == 3) {
                mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                mActivity.finishAffinity();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (countDownTimer!=null)
        {
            countDownTimer.cancel();
        }
        super.onBackPressed();
    }
}

