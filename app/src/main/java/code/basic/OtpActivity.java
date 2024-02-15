package code.basic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityOtpBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.activity.MainActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class OtpActivity extends BaseActivity implements View.OnClickListener {

    ActivityOtpBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    @SuppressLint("SetTextI18n")
    private void inits() {

//        b.ivBack.setOnClickListener(view -> onBackPressed());

        b.tvContinue.setOnClickListener(this);
        b.tvResendOtp.setOnClickListener(this);

        b.tvMsg.setText(getString(R.string.otpMsgtext) + " " + getIntent().getStringExtra("mobile"));

        startTimer();

        b.etOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    b.etOtp1.requestFocus();
                } else
                    b.etOtp2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etOtp1.requestFocus();
                } else
                    b.etOtp3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etOtp2.requestFocus();
                } else
                    b.etOtp4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etOtp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (count == 0) {
                    b.etOtp3.requestFocus();
                } else
                    b.etOtp5.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        b.etOtp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (count == 0) {
                    b.etOtp4.requestFocus();
                } else
                    b.etOtp6.requestFocus();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        b.etOtp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (count == 0) {
                    b.etOtp5.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvContinue:

                validate();

                break;

            case R.id.tvResendOtp:
                startTimer();
                hitResendOtpApi();

                break;

        }

    }

    private void hitResendOtpApi() {

        WebServices.getApi(mActivity, AppUrls.resendOtp, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseResendOtp(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseResendOtp(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            AppUtils.showResMsgToastSort(mActivity, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void validate() {

        String otp = b.etOtp1.getText().toString().trim() +
                b.etOtp2.getText().toString().trim() +
                b.etOtp3.getText().toString().trim() +
                b.etOtp4.getText().toString().trim() +
                b.etOtp5.getText().toString().trim() +
                b.etOtp6.getText().toString().trim();


        if (otp.length() < 6)
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterOtp));

        else {

            hitVerifyOtpApi(otp);

        }
    }

    private void hitVerifyOtpApi(String otp) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("mobile", getIntent().getStringExtra("mobile"));
            jsonObject.put("otp", otp);
            jsonObject.put("fcmId", AppSettings.getString(AppSettings.fcmToken));
            jsonObject.put("manufacturer", Build.MANUFACTURER);
            jsonObject.put("deviceName", Build.MODEL);
            jsonObject.put("deviceVersion", Build.VERSION.RELEASE);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.otpVerification, json, true, true, new WebServicesCallback() {

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

                JSONObject data = jsonObject.getJSONObject("data");


                AppSettings.putString(AppSettings.token, data.getString("token"));
                AppSettings.putString(AppSettings.isProfileCompleted, data.getString("isProfileCompleted"));
                AppSettings.putString(AppSettings.name, data.getString("name"));
                AppSettings.putString(AppSettings.mobile, data.getString("mobile"));
                AppSettings.putString(AppSettings.email, data.getString("email"));
                AppSettings.putString(AppSettings.userId, data.getString("userId"));
                AppSettings.putString(AppSettings.isProfileCompleted, data.getString("isProfileCompleted"));
                if (data.getString("isProfileCompleted").equals("0")) {

                    startActivity(new Intent(mActivity, SignUpActivity.class));
                } else {

                    signUpInFirebase();

                    startActivity(new Intent(mActivity, MainActivity.class));
                }


                finish();
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.otp),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void signUpInFirebase() {


    }

    private void startTimer() {

        b.tvResendOtp.setEnabled(false);

        new CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {

                String msg = String.valueOf(millisUntilFinished / 1000);
                b.tvResendOtp.setText(msg + "s");
            }

            @Override
            public void onFinish() {
                b.tvResendOtp.setEnabled(true);
                b.tvResendOtp.setText(getString(R.string.resendOtp));
            }
        }.start();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}