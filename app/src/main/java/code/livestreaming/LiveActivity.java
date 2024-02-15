package code.livestreaming;

import android.os.Bundle;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityLiveBinding;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoDialogInfo;
import com.zegocloud.uikit.prebuilt.livestreaming.core.ZegoLiveStreamingEndListener;
import org.json.JSONException;
import org.json.JSONObject;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class LiveActivity extends BaseActivity {
    private boolean isHost;
    private String mLiveid, userid, uname;
    ActivityLiveBinding binding;
    ZegoUIKitPrebuiltLiveStreamingFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isHost = getIntent().getBooleanExtra(AppSettings.host, false);
        userid = AppSettings.getString(AppSettings.userId);
        uname = AppSettings.getString(AppSettings.name);
        mLiveid = getIntent().getStringExtra(AppSettings.liveId);
        addFragment();
    }

    private void addFragment() {
        long appID = AppConstants.AppID;
        String appSign = AppConstants.appSign;
        String liveID = mLiveid;
        String userID = userid;
        String userName = uname;
        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if (isHost) {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
        } else {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.audience();
        }
        config.translationText.noHostOnline = "Live is End";
        config.zegoLiveStreamingEndListener = new ZegoLiveStreamingEndListener() {
            @Override
            public void onLiveStreamingEnded() {
                AppUtils.showToastSort(mActivity, "Host leave channel");
            }
        };
        config.turnOnCameraWhenJoining = false;
        config.needConfirmWhenOthersTurnOnYourCamera = true;
        ZegoDialogInfo confirmDialogInfo = new ZegoDialogInfo();
        confirmDialogInfo.title= "Stop the live";
        confirmDialogInfo.message= "Do you want to leave?";
        confirmDialogInfo.cancelButtonName= "Cancel";
        confirmDialogInfo.confirmButtonName = "Confirm";
        config.confirmDialogInfo = confirmDialogInfo;
        fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                appID, appSign, userID, userName, liveID, config);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }

    //create channel api
    private void hitLeaveChannelApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("channelId", mLiveid);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.stopVideoChat, json, true, true, new WebServicesCallback() {
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
               finish();
            } else {
                AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        if (isHost) {
            hitLeaveChannelApi();
        } else {
            finish();
        }
        super.onDestroy();
    }
}
