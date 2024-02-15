package code.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityProfileSettingsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ProfileSettingsActivity extends BaseActivity implements View.OnClickListener {

    ActivityProfileSettingsBinding b;
    String status = "0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.rlUpdateProfile.setOnClickListener(this);
        b.rlUserVerification.setOnClickListener(this);
        b.rlAddresses.setOnClickListener(this);
        b.rlSecurityPrivacy.setOnClickListener(this);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(R.string.profileSettings);
        b.ivOnOff.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlUpdateProfile:

                startActivity(new Intent(mActivity, UpdateProfileActivity.class));
                break;

            case R.id.rlUserVerification:

                startActivity(new Intent(mActivity, UserVerificationActivity.class));
                break;

            case R.id.rlAddresses:
                AppSettings.putString(AppSettings.isFromPage,"Profile");
                startActivity(new Intent(mActivity, AddressListActivity.class));

                break;
            case R.id.rlSecurityPrivacy:
                startActivity(new Intent(mActivity, SecurityPrivacyActivity.class));

                break;

            case R.id.ivOnOff:
                hitOnOffApi(status);
                break;
        }

    }
    private void hitOnOffApi(String status) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("isNotification", status);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.putApi(mActivity, AppUrls.updateNotificationStatus, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOnOffJson(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
            }
        });
    }

    private void parseOnOffJson(JSONObject response) {
        try {
            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.getString("success").equals("1")) {
                hitGetNotificationStatusApi();

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString("resMsg"), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void hitGetNotificationStatusApi() {


        WebServices.getApi(mActivity, AppUrls.getNotificationStatus, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseNotificationStatusJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseNotificationStatusJson(JSONObject response) {

        try {
            // JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.getString("success").equals("1")) {

                if (jsonObject.getString("notificationStatus").equals("1")) {
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_on);
                    status = "0";
                } else {
                    b.ivOnOff.setImageResource(R.drawable.ic_switch_off);
                    status = "1";
                }


            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString("resMsg"), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        hitGetNotificationStatusApi();
        super.onResume();
    }
}