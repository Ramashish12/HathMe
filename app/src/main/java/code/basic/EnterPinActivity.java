package code.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityEnterPinBinding;

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

public class EnterPinActivity extends BaseActivity implements View.OnClickListener {

    ActivityEnterPinBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEnterPinBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

       b.header.ivBack.setOnClickListener(view -> onBackPressed());
       b.header.tvHeader.setText(getString(R.string.enterPin));
        b.etPin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    b.etPin1.requestFocus();
                } else
                    b.etPin2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etPin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etPin1.requestFocus();
                } else
                    b.etPin3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etPin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etPin2.requestFocus();
                } else
                    b.etPin4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etPin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etPin3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.tvSubmit.setOnClickListener(this);
        //b.btnForgot.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tvSubmit:

                validate();

                break;

          /*  case R.id.btnForgot:

                startActivity(new Intent(mActivity, ForgotPinActivity.class));

                break;*/

        }

    }


    private void validate() {

        String pin = b.etPin1.getText().toString().trim() +
                b.etPin2.getText().toString().trim() +
                b.etPin3.getText().toString().trim() +
                b.etPin4.getText().toString().trim();


        if (pin.length() < 4)
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPin));

        else {

            if (AppUtils.isNetworkAvailable(mActivity)) {
                hitVerifyPinApi(pin);
            } else {
                AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
            }

        }
    }

    private void hitVerifyPinApi(String pin) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("pin", pin);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.verifyPin, json, true, true, new WebServicesCallback() {

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

                String currentDate = AppUtils.getCurrentDate();
                currentDate = currentDate.replaceAll("-", "");

                AppSettings.putString(AppSettings.currentDate, currentDate);

                startActivity(new Intent(mActivity, MainActivity.class));
                finishAffinity();

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

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