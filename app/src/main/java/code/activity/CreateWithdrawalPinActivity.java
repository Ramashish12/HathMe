package code.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.hathme.android.R;
import com.hathme.android.databinding.ActivityCreateWithdrawalPinBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class CreateWithdrawalPinActivity extends BaseActivity implements View.OnClickListener {

    private ActivityCreateWithdrawalPinBinding b;

    private boolean isNewPasswordVisible = false, isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.changeLanguage(mActivity);
        b = ActivityCreateWithdrawalPinBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.createWithdrawalPin));

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


        b.etConfirmPin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    b.etConfirmPin1.requestFocus();
                } else
                    b.etConfirmPin2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etConfirmPin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etConfirmPin1.requestFocus();
                } else
                    b.etConfirmPin3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etConfirmPin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etConfirmPin2.requestFocus();
                } else
                    b.etConfirmPin4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.etConfirmPin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count == 0) {
                    b.etConfirmPin3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        b.btnSubmit.setOnClickListener(this);


        b.ivNewPassword.setOnClickListener(this);
        b.ivConfirmPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnSubmit:

                validate();

                break;
            case R.id.ivNewPassword:


                if (isNewPasswordVisible) {

                    b.etPin1.setTransformationMethod(new PasswordTransformationMethod());
                    b.etPin2.setTransformationMethod(new PasswordTransformationMethod());
                    b.etPin3.setTransformationMethod(new PasswordTransformationMethod());
                    b.etPin4.setTransformationMethod(new PasswordTransformationMethod());

                    isNewPasswordVisible = false;
                    b.ivNewPassword.setImageResource(R.drawable.ic_visible_password);

                } else {

                    b.etPin1.setTransformationMethod(null);
                    b.etPin2.setTransformationMethod(null);
                    b.etPin3.setTransformationMethod(null);
                    b.etPin4.setTransformationMethod(null);
                    isNewPasswordVisible = true;
                    b.ivNewPassword.setImageResource(R.drawable.ic_password_hidden);
                }
                break;

            case R.id.ivConfirmPassword:

                if (isConfirmPasswordVisible) {
                    b.etConfirmPin1.setTransformationMethod(new PasswordTransformationMethod());
                    b.etConfirmPin2.setTransformationMethod(new PasswordTransformationMethod());
                    b.etConfirmPin3.setTransformationMethod(new PasswordTransformationMethod());
                    b.etConfirmPin4.setTransformationMethod(new PasswordTransformationMethod());
                    isConfirmPasswordVisible = false;
                    b.ivConfirmPassword.setImageResource(R.drawable.ic_visible_password);

                } else {

                    b.etConfirmPin1.setTransformationMethod(null);
                    b.etConfirmPin2.setTransformationMethod(null);
                    b.etConfirmPin3.setTransformationMethod(null);
                    b.etConfirmPin4.setTransformationMethod(null);
                    isConfirmPasswordVisible = true;
                    b.ivConfirmPassword.setImageResource(R.drawable.ic_password_hidden);
                }
                break;

        }

    }

    private void changePinNotVisible() {

    }

    private void validate() {

        String pin = b.etPin1.getText().toString().trim() +
                b.etPin2.getText().toString().trim() +
                b.etPin3.getText().toString().trim() +
                b.etPin4.getText().toString().trim();

        String confirmPin = b.etConfirmPin1.getText().toString().trim() +
                b.etConfirmPin2.getText().toString().trim() +
                b.etConfirmPin3.getText().toString().trim() +
                b.etConfirmPin4.getText().toString().trim();


        if (pin.length() < 4) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPin));
        } else if (confirmPin.isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterConfirmPin));
        } else if (!pin.equals(confirmPin)) {
            AppUtils.showToastSort(mActivity, getString(R.string.confirmPinNotMatch));
        } else {

            if (AppUtils.isNetworkAvailable(mActivity)) {
                hitCreatePinApi(pin);
            } else
                AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));

        }
    }

    private void hitCreatePinApi(String pin) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("withdrawalPin", pin);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.CreateWithdrawalPin, json, true, true, new WebServicesCallback() {

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

                AppSettings.putString(AppSettings.isWithdrawPinCreated, "1");
                if (getIntent().hasExtra("pageFrom") && getIntent().getStringExtra("pageFrom").equals("1")) {
                    AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
                    onBackPressed();
                } else {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),  jsonObject.getString(AppConstants.resMsg),
                            3);
                }


            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg),
                        0);
//                showMessageDialog(getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg),"2");
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