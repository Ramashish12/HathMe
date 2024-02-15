package code.vediolist;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySubscriptionBinding;

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
import code.vediolist.adapters.AdapterSubscriptionCategoryList;
import code.vediolist.adapters.AdapterSubscriptionCategoryVideoList;
import code.vediolist.adapters.AdapterSubscriptionsList;
import code.vediolist.adapters.AdapterVideoList;
import code.vediolist.adapters.Helper;
import code.view.BaseActivity;

public class SubscriptionActivity extends BaseActivity implements View.OnClickListener {
    private ActivitySubscriptionBinding binding;
    private AdapterSubscriber adapterSubscriber;
    private AdapterVideos adapterVideos;
    private String channelId = "", period = "1";
    private ArrayList<HashMap<String, String>> arraySubscriberList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayVideoList = new ArrayList<>();
    AdapterReport adapterReport;
    TextView tvReport, tvCancel;
    private ArrayList<String> arrayCatList;
    private String report = "",isFrom = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubscriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inite();
    }


    private void inite() {
        binding.header.ivBack.setOnClickListener(this);
        binding.rlAll.setOnClickListener(this);
        binding.rlToday.setOnClickListener(this);
        binding.rlYesterday.setOnClickListener(this);
        binding.rlThisMonth.setOnClickListener(this);
        binding.rlLastMonth.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.subscription));
        adapterSubscriber = new AdapterSubscriber(arraySubscriberList);
        binding.rvSubscriberList.setAdapter(adapterSubscriber);

        adapterVideos = new AdapterVideos(arrayVideoList);
        binding.rvVideoList.setAdapter(adapterVideos);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.rlAll:
                hitSubscribeVideoApi(channelId, "1");
                binding.rlAll.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.rlToday.setBackgroundResource(R.drawable.bg_category);
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
                break;
            case R.id.rlToday:
                hitSubscribeVideoApi(channelId, "2");
                binding.rlToday.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.rlAll.setBackgroundResource(R.drawable.bg_category);
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
                break;
            case R.id.rlYesterday:
                hitSubscribeVideoApi(channelId, "3");
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.rlAll.setBackgroundResource(R.drawable.bg_category);
                binding.rlToday.setBackgroundResource(R.drawable.bg_category);
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
                break;
            case R.id.rlThisMonth:
                hitSubscribeVideoApi(channelId, "4");
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.rlAll.setBackgroundResource(R.drawable.bg_category);
                binding.rlToday.setBackgroundResource(R.drawable.bg_category);
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
                break;
            case R.id.rlLastMonth:
                hitSubscribeVideoApi(channelId, "5");
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.rlAll.setBackgroundResource(R.drawable.bg_category);
                binding.rlToday.setBackgroundResource(R.drawable.bg_category);
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
                break;
        }

    }

    private void hitGetSubscriberListApi() {
        WebServices.getApi(mActivity, AppUrls.mySubscriptionList, true, true, new WebServicesCallback() {

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
        arraySubscriberList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObj.getString("channelId"));
                    hashMap.put("channelName", jsonObj.getString("channelName"));
                    hashMap.put("channelProfileImage", jsonObj.getString("channelProfileImage"));
                    arraySubscriberList.add(hashMap);

                }

            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (arraySubscriberList.isEmpty()) {
        binding.rlEmpty.setVisibility(View.VISIBLE);
        binding.nestedView.setVisibility(View.GONE);
        } else {
            binding.rlEmpty.setVisibility(View.GONE);
            binding.nestedView.setVisibility(View.VISIBLE);
        }
        adapterSubscriber.notifyDataSetChanged();
    }

    private void hitSubscribeVideoApi(String channelId, String period) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("channelId", channelId);
            jsonObject.put("period", AppUtils.returnInt(period));
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.channelVideos;

        WebServices.postApi(mActivity, url, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonVideoList(response);

            }

            @Override
            public void OnFail(String response) {
                Log.v("error", response);
            }
        });
    }

    private void parseJsonVideoList(JSONObject response) {
        arrayVideoList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObj.getString("_id"));
                    hashMap.put("videoUrl", jsonObj.getString("videoUrl"));
                    hashMap.put("type", jsonObj.getString("type"));
                    hashMap.put("videoThumbnail", jsonObj.getString("videoThumbnail"));
                    hashMap.put("viewCount", jsonObj.getString("viewCount"));
                    hashMap.put("description", jsonObj.getString("description"));
                    hashMap.put("createdAt", jsonObj.getString("createdAt"));
                    hashMap.put("updatedAt", jsonObj.getString("updatedAt"));
                    arrayVideoList.add(hashMap);

                }
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapterVideos.notifyDataSetChanged();
    }

    private class AdapterSubscriber extends RecyclerView.Adapter<AdapterSubscriber.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterSubscriber(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterSubscriber.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_subscriptions_person_list, viewGroup, false);
            return new AdapterSubscriber.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterSubscriber.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.tvName.setText(data.get(position).get("channelName"));
            AppUtils.loadPicassoImage(data.get(position).get("channelProfileImage"), holder.ivProfile);
            holder.itemView.setOnClickListener(v -> {
                period = "1";
                channelId = data.get(position).get("_id");
                hitSubscribeVideoApi(data.get(position).get("_id"), period);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivProfile;
            TextView tvName;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivProfile = itemView.findViewById(R.id.ivProfile);
                tvName = itemView.findViewById(R.id.tvName);

            }
        }
    }

    private class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterCategory(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterCategory.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_video_category, viewGroup, false);
            return new AdapterCategory.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterCategory.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            HashMap<String, String> map = data.get(position);
            holder.tvCategory.setText(map.get(Helper.Key_category));
            if (position == 0) {
                holder.tvCategory.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
                holder.rlBg.setBackgroundTintList(ContextCompat.getColorStateList(mActivity, R.color.select_color));
            } else {
                holder.tvCategory.setTextColor(ContextCompat.getColor(mActivity, R.color.black));
                holder.rlBg.setBackgroundTintList(ContextCompat.getColorStateList(mActivity, R.color.unselect_color));
            }
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory;
            RelativeLayout rlBg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                rlBg = itemView.findViewById(R.id.rlBg);
            }
        }
    }

    private class AdapterVideos extends RecyclerView.Adapter<AdapterVideos.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterVideos(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterVideos.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_download_video_list, viewGroup, false);
            return new AdapterVideos.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterVideos.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.tvDesc.setText(data.get(position).get("description"));
            holder.tvTotalView.setText(data.get(position).get("viewCount") + " " + getString(R.string.view));
            AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);
            if (data.get(position).get("type").equalsIgnoreCase("1"))
            {
               holder.ivMenu.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.ivMenu.setVisibility(View.GONE);
            }
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
                if (data.get(position).get("type").equalsIgnoreCase("1"))
                {
                    startActivity(new Intent(mActivity, VideoDetailsActivity.class).putExtra("videoId", data.get(position).get("_id")));
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

    private ArrayList<HashMap<String, String>> getCategoryList() {
        ArrayList<HashMap<String, String>> arrCat = new ArrayList<HashMap<String, String>>();
        String[] strcat = {"All", "Today", "Yesterday", "This month", "Last month"};

        for (int i = 0; i < strcat.length; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Helper.Key_category, strcat[i]);
            arrCat.add(map);
        }
        return arrCat;


    }


    @Override
    protected void onResume() {
        hitGetSubscriberListApi();
        binding.rlAll.setBackgroundResource(R.drawable.bg_category_selected);
        binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
        binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
        binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
        binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
        binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
        binding.rlToday.setBackgroundResource(R.drawable.bg_category);
        binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
        binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
        binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
        hitSubscribeVideoApi(channelId, period);
        super.onResume();
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
                binding.rlAll.setBackgroundResource(R.drawable.bg_category_selected);
                binding.tvAll.setTextColor(ContextCompat.getColor(mActivity,R.color.white));
                binding.tvToday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvYesterday.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvThisMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.tvLastMonth.setTextColor(ContextCompat.getColor(mActivity,R.color.black));
                binding.rlToday.setBackgroundResource(R.drawable.bg_category);
                binding.rlYesterday.setBackgroundResource(R.drawable.bg_category);
                binding.rlThisMonth.setBackgroundResource(R.drawable.bg_category);
                binding.rlLastMonth.setBackgroundResource(R.drawable.bg_category);
                period = "1";
                hitSubscribeVideoApi(channelId, period);
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void shareDeepLink(String videoId,String videoUrl) {
        String url = "";
        if ("https".equals(videoUrl)) {
            url = getString(R.string.httpsDeepLink);
        } else if ("http".equals(videoUrl)) {
            url = getString(R.string.httpDeepLink);
        }
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