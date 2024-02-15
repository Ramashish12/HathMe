package code.livestreaming;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityLivelistBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.livestreaming.adapter.AdapterLiveList;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class LiveListActivity extends BaseActivity implements View.OnClickListener{
    ActivityLivelistBinding binding;
    ArrayList<HashMap<String, String>> arrayLiveList = new ArrayList<>();
    int is_first = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inite();

    }
    private void inite() {
        binding.mSwiperFreshLayout.setColorScheme(R.color.red,
                R.color.green, R.color.colorPrimary, R.color.colorPrimary);
        binding.mSwiperFreshLayout.setRefreshing(false);
        binding.rlCreateChannel.setOnClickListener(this);
        binding.mSwiperFreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hitGetLiveListApi();
                binding.mSwiperFreshLayout.setRefreshing(false);
            }
        });
        binding.ivBack.setOnClickListener(view -> onBackPressed());
        hitGetLiveListApi();
    }
    private void hitGetLiveListApi() {

        WebServices.getApi(mActivity, AppUrls.videoChatList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseLiveListJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }
    private void parseLiveListJson(JSONObject response) {

        arrayLiveList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonCategory = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("channelId", jsonCategory.getString("channelId"));
                    hashMap.put("userId", jsonCategory.getString("userId"));
                    hashMap.put("name", jsonCategory.getString("name"));
                    hashMap.put("profileImage", jsonCategory.getString("profileImage"));
                    hashMap.put("status", jsonCategory.getString("status"));
                    hashMap.put("startDateTime", jsonCategory.getString("startDateTime"));
                    arrayLiveList.add(hashMap);

                }
            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString("resMsg"));
            }
            loadList(arrayLiveList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void loadList(final ArrayList<HashMap<String,String>>arrList) {
        AdapterLiveList Adapter = new AdapterLiveList(arrList, mActivity);
        binding.rvLiveList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        binding.rvLiveList.setAdapter(Adapter);
    }
    //create channel api
    private void hitCreateChannelApi() {
        WebServices.getApi(mActivity, AppUrls.createVideoChannel, true, true, new WebServicesCallback() {
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
                jsonObject = jsonObject.getJSONObject("data");
                Intent intent = new Intent(this,LiveActivity.class);
                intent.putExtra(AppSettings.host, true);
                intent.putExtra(AppSettings.userName, AppSettings.getString(AppSettings.name));
                intent.putExtra(AppSettings.userID, jsonObject.getString("userId"));
                intent.putExtra(AppSettings.liveId, jsonObject.getString("_id")); //unique
                startActivity(intent);

            }
            else
            {
                AppUtils.showToastSort(mActivity,jsonObject.getString("resMsg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        if (is_first==0)
        {
            is_first++;
        }
        else
        {
            hitGetLiveListApi();
        }
        super.onResume();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlCreateChannel:
            hitCreateChannelApi();
            break;
        }
    }
}