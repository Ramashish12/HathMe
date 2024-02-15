package code.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityForgotWithdrawalPinBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.basic.LoginActivity;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ForgotWithdrawalPinActivity extends BaseActivity implements View.OnClickListener {

    private ActivityForgotWithdrawalPinBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.changeLanguage(mActivity);
        b = ActivityForgotWithdrawalPinBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.forgotPinQ));
        b.btnSubmit.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btnSubmit:

                validate();

                break;

        }

    }
    private void validate() {

        if (b.etEmail.getText().toString().trim().isEmpty()) {
            b.etEmail.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterEmail));
        }
        else if (AppUtils.isNetworkAvailable(mActivity))
        {
            hitForgotPinApi();
        }
        else
        {
            AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
        }
    }

    private void hitForgotPinApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("userEmail", b.etEmail.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.ResetWithdrawalPin , json, true, true, new WebServicesCallback() {

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

                showMessageDialog(getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg),"1");

            } else {
                showMessageDialog(getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg),"2");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showMessageDialog(String title, String message, String type) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvTitle.setText(title);
        tvMessage.setText(message);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

            if (type.equals("1")){
                onBackPressed();
                finish();
            }
        });

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