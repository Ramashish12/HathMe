package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityAddAmountBinding;
import com.hathme.android.databinding.ActivityUpiBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.GlobalData;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class UpiActivity extends BaseActivity implements View.OnClickListener {

    ActivityUpiBinding b;

    String amount = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUpiBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(this);
        b.btnSubmit.setOnClickListener(this);
        b.ivCopy.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        amount = extras.getString("amount");

        b.header.tvHeader.setText(getString(R.string.depositing) + " " + getString(R.string.rupeeSymbol) + "" + amount);

        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.depositQRCode), b.ivQrCode);
        b.tvUpi.setText(AppSettings.getString(AppSettings.depositUPI));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ivBack:

                onBackPressed();

                break;

            case R.id.ivCopy:

                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", b.tvUpi.getText().toString().trim());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(mActivity, getString(R.string.clipboardCopied), Toast.LENGTH_SHORT).show();

                break;

            case R.id.btnSubmit:

                if (b.etUtrNumber.getText().toString().trim().isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterUtrNumber));
                } else if (b.etUtrNumber.getText().toString().trim().length() < 12) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectUtrNumber));
                } else if (AppUtils.isNetworkAvailable(mActivity)) {
                    hitDepositApi();
                } else {
                    AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
                }

                break;
        }
    }

    private void hitDepositApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("amount", amount);
            jsonObject.put("modeOfPayment", "1");
            jsonObject.put("referenceUtrNumber", b.etUtrNumber.getText().toString().trim());
            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.depositAmount;

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

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

                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 1);

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