package code.activity;


import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityNotificationsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class NotificationsActivity extends BaseActivity implements View.OnClickListener {
    private ActivityNotificationsBinding binding;
    String status = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inits();
    }

    private void inits() {
        binding.ivBack.setOnClickListener(this);
        binding.ivOnOff.setOnClickListener(this);
        hitGetNotificationStatusApi();
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

        WebServices.putApi(mActivity, AppUrls.updateNotificationStatus, json, true, true, new WebServicesCallback() {

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


        WebServices.getApi(mActivity, AppUrls.getNotificationStatus, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseNotificationStatusJson(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
            }
        });
    }

    private void parseNotificationStatusJson(JSONObject response) {

        try {
            // JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.getString("success").equals("1")) {

                if (jsonObject.getString("notificationStatus").equals("1")) {
                    binding.ivOnOff.setImageResource(R.drawable.ic_switch_on);
                    status = "0";
                } else {
                    binding.ivOnOff.setImageResource(R.drawable.ic_switch_off);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            case R.id.ivOnOff:
                hitOnOffApi(status);
                break;
        }
    }

    @Override
    protected void onResume() {
        hitGetNotificationStatusApi();
        super.onResume();
    }
}