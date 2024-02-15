package code.vediolist.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;

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
import code.view.BaseActivity;

public class AdapterShorts extends RecyclerView.Adapter<AdapterShorts.MyViewHolder> {
    ArrayList<HashMap<String, String>> arrayList;
    private ArrayList<HashMap<String, String>> arrayCommentsList = new ArrayList<>();
    Context context;
    String playPause = "";
    BottomSheetDialog bottomSheetDialog;
    EditText etComment, etComments;
    ImageView ivSend;
    AdapterComment adapter;
    ViewPager2 vPager;
    private CountDownTimer countDownTimer;
    public AdapterShorts(ArrayList<HashMap<String, String>> arrayList, Context context, ViewPager2 vPager) {
        this.arrayList = arrayList;
        this.context = context;
        this.vPager = vPager;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_video_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoView.setVideoPath(arrayList.get(position).get("videoUrl"));
        holder.textVideoTitle.setText(arrayList.get(position).get("title"));
        holder.textVideoDescription.setText(arrayList.get(position).get("description"));
        holder.tvName.setText(arrayList.get(position).get("name"));
        holder.tvCommentCount.setText(arrayList.get(position).get("totalCommentsCount"));
        AppUtils.loadPicassoImage(arrayList.get(position).get("profileImage"), holder.ivProfileImage);
        if (arrayList.get(position).get("isUserLiked").equalsIgnoreCase("1"))
        {
            holder.ivLike.setColorFilter(ContextCompat.getColor(context,R.color.red));
        }
        else
        {
            holder.ivLike.setColorFilter(ContextCompat.getColor(context,R.color.white));
        }
        if (arrayList.get(position).get("isUserDisLike").equalsIgnoreCase("1"))
        {
            holder.ivDisLike.setColorFilter(ContextCompat.getColor(context,R.color.red));
        }
        else
        {
            holder.ivDisLike.setColorFilter(ContextCompat.getColor(context,R.color.white));
        }
        hitViewVideoApi(arrayList.get(position).get("_id"),holder.videoView,holder.ivPlay);
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.videoProgressBar.setVisibility(View.GONE);
                float videoRatio = mediaPlayer.getVideoWidth() / (float) mediaPlayer.getVideoHeight();
                float screenRatio = holder.videoView.getWidth() / (float) holder.videoView.getHeight();
                float scale = videoRatio / screenRatio;
                if (scale >= 1f) {
                    holder.videoView.setScaleX(scale);
                } else {
                    holder.videoView.setScaleY(1f / scale);
                }
                mediaPlayer.start();


            }
        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                holder.videoProgressBar.setVisibility(View.GONE);
                mediaPlayer.start();

            }
        });
        holder.itemView.setOnClickListener(view -> {
            if (holder.videoView.isPlaying()) {
                holder.videoView.pause();
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.ivPlay.setImageResource(R.drawable.pause);
                new Handler(Looper.myLooper()).postDelayed(() -> {
                    holder.ivPlay.setVisibility(View.GONE);
                }, 1000);


        } else {
                holder.videoView.start();
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.ivPlay.setImageResource(R.drawable.play);
                new Handler(Looper.myLooper()).postDelayed(() -> {
                    holder.ivPlay.setVisibility(View.GONE);
                }, 1000);
            }
        });
        holder.ivLike.setOnClickListener(view -> {
            hitLikeDislikeApi("1",arrayList.get(position).get("_id"),holder.ivLike,holder.ivDisLike);
        });
        holder.ivDisLike.setOnClickListener(view -> {
            hitLikeDislikeApi("2",arrayList.get(position).get("_id"),holder.ivLike,holder.ivDisLike);
        });
        holder.ivComment.setOnClickListener(view -> {
            showBottomSheetDialog(arrayList.get(position).get("_id"),holder.tvCommentCount);
        });
        holder.ivShare.setOnClickListener(view -> {
            hitShareVideoApi(arrayList.get(position).get("_id"));
        });

    }

    @Override
    public int getItemCount() {
       return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        TextView textVideoTitle, textVideoDescription,tvName,tvCommentCount;
        ProgressBar videoProgressBar;
        ImageView ivPlay,ivLike,ivDisLike,ivProfileImage,ivComment,ivShare;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            textVideoDescription = itemView.findViewById(R.id.textVideoDescription);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            ivLike = itemView.findViewById(R.id.ivLike);
            ivDisLike = itemView.findViewById(R.id.ivDisLike);
            tvName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            ivComment = itemView.findViewById(R.id.ivComment);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivShare = itemView.findViewById(R.id.ivShare);
        }
    }

    private void hitLikeDislikeApi(String isFrom,String videoId,ImageView ivLike,ImageView ivDislike) {
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
            url = AppUrls.likeVideo;
        } else if (isFrom.equals("2")) {
            url = AppUrls.dislikeVideo;
        }
        WebServices.postApi((BaseActivity) context, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonLikeDislike(response,isFrom,ivLike,ivDislike);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonLikeDislike(JSONObject response,String isFrom,ImageView ivLike,ImageView ivDislike) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONObject jsonObj = jsonObject.getJSONObject("data");
                if (isFrom.equals("1")) {
                    ivLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
                    ivDislike.setColorFilter(ContextCompat.getColor(context,R.color.white));
                }
                else if (isFrom.equals("2")) {
                    ivLike.setColorFilter(ContextCompat.getColor(context,R.color.white));
                    ivDislike.setColorFilter(ContextCompat.getColor(context,R.color.red));
                }
            } else {
                AppUtils.showToastSort(context, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void showBottomSheetDialog(String videoId,TextView tvCommentCount) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_comment_list);
        bottomSheetDialog.setCancelable(true);
        RecyclerView rvList = bottomSheetDialog.findViewById(R.id.rvList);
        ImageView ivProfile = bottomSheetDialog.findViewById(R.id.ivProfileImage);
        ivSend = bottomSheetDialog.findViewById(R.id.ivSend);
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), ivProfile);
        hitCommentListApi(videoId);
        adapter = new AdapterComment(arrayCommentsList);
        rvList.setAdapter(adapter);
        etComment = bottomSheetDialog.findViewById(R.id.etComments);
        if (!AppSettings.getString(AppSettings.commentsMsg).equalsIgnoreCase("")) {
            etComment.setText(AppSettings.getString(AppSettings.commentsMsg));
            ivSend.setVisibility(View.VISIBLE);
        } else {
            ivSend.setVisibility(View.GONE);
        }
        etComment.setOnClickListener(view -> {
            showBottomSheetDialogSendComment(videoId,tvCommentCount);
        });
        bottomSheetDialog.show();

        // getAllVideoComments
    }

    private void showBottomSheetDialogSendComment(final String videoId,TextView tvCommentCount) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
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
                AppUtils.showMessageDialog((Activity) context, context.getString(R.string.app_name), context.getString(R.string.pleaseEnterYourComment), 8);
            } else {
                hitCommentApi(videoId,AppSettings.getString(AppSettings.commentsMsg),tvCommentCount);
            }
        });
        bottomSheetDialog.show();

        // getAllVideoComments
    }

    private void hitCommentListApi(final String videoId) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.listAllVideoComment;

        WebServices.postApi((BaseActivity) context, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonComment(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });

    }

    private void parseJsonComment(JSONObject response) {
        arrayCommentsList.clear();
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

                    arrayCommentsList.add(hashMap);

                }
            } else {
                AppUtils.showToastSort(context, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void hitCommentApi(String videoId,String comment,TextView tvCommentCount) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            jsonObject.put("comment", comment);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.commentVideo;

        WebServices.postApi((BaseActivity) context, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonComments(response,videoId,tvCommentCount);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonComments(JSONObject response,String videoId,TextView tvCommentCount) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppSettings.putString(AppSettings.commentsMsg, "");
                JSONObject jsonObj = jsonObject.getJSONObject("data");
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.dismiss();
                    etComment.setText("");
                    ivSend.setVisibility(View.GONE);
                    tvCommentCount.setText(jsonObj.getString("totalCommentCount"));
                    //  binding.rlCommentView.setVisibility(View.VISIBLE);
                    hitCommentListApi(videoId);
                }
            } else {
                AppUtils.showToastSort(context, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterComment(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterComment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_comment_list, viewGroup, false);
            return new AdapterComment.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterComment.MyViewHolder holder, final int position) {
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
    private void shareDeepLink(String videoId) {

        String deepLinkUri = context.getString(R.string.httpsDeepLinkShort) + videoId;

        // Share deep link using an Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, deepLinkUri);

        // Create a chooser to show available sharing options
        Intent chooserIntent = Intent.createChooser(shareIntent, "Share your Link");

        // Check if there are activities available to handle the intent
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooserIntent);
        }

        //  AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),data.toString(),9);
    }
    private void hitViewVideoApi(String videoId,VideoView videoView,ImageView ivPlay) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("videoId", videoId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.viewVideo;

        WebServices.postApi((BaseActivity) context, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonViewVideo(response,videoView,ivPlay);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonViewVideo(JSONObject response,VideoView videoView,ImageView ivPlay) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppSettings.putString(AppSettings.isScroll,"1");
                vPager.setUserInputEnabled(true);
            } else {
                AppSettings.putString(AppSettings.isScroll,"2");
                vPager.setUserInputEnabled(false);
                startTimer(videoView,ivPlay);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        WebServices.postApi((BaseActivity) context, AppUrls.shareVideo, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonShareVideo(response,id);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }
    private void parseJsonShareVideo(JSONObject response,String id) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                shareDeepLink(id);
            }
            else
            {
                AppUtils.showToastSort(context,jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void startTimer(VideoView videoView,ImageView ivPlay) {
        long durationInMillis = 5000; // 5 seconds
        countDownTimer = new CountDownTimer(durationInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                updateTimerText(millisUntilFinished);
            }

            public void onFinish() {
                // Timer finished, perform actions if needed
                // updateTimerText(0);
                videoView.pause();
                ivPlay.setVisibility(View.VISIBLE);
                ivPlay.setImageResource(R.drawable.pause);
                new Handler(Looper.myLooper()).postDelayed(() -> {
                    ivPlay.setVisibility(View.GONE);
                }, 1000);
                AppSettings.putString(AppSettings.isScroll,"1");
                AppUtils.showMessageDialog((Activity) context,context.getString(R.string.app_name),context.getString(R.string.insufficientBalance),1);
            }
        };

        countDownTimer.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        long seconds = millisUntilFinished / 1000;
        String timerText = String.format("%2d", seconds);

    }
}
