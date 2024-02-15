package code.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySellBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SellActivity extends BaseActivity implements View.OnClickListener {

    private ActivitySellBinding b;

    private double price = 0, walletBalance = 0;
    String gpValue = "";
    String checkGpValue = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySellBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.sellGander));
        gpValue = getIntent().getStringExtra("gpValue");
        walletBalance = AppUtils.returnDouble(gpValue);
        b.tvBalanceAmount.setText(gpValue);
//        type = getIntent().getStringExtra("type");

        b.btn25Perc.setOnClickListener(this);
        b.btn50Perc.setOnClickListener(this);
        b.btn75Perc.setOnClickListener(this);
        b.btn100Perc.setOnClickListener(this);
        b.btnSubmit.setOnClickListener(this);

        b.etInGyre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() > 0 && !Pattern.matches(AppConstants.numberDotRgx, s.toString())) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                } else if (s.toString().indexOf('.', s.toString().indexOf('.') + 1) != -1) {
                    s.delete(s.toString().length() - 1, s.toString().length());

                } else if (s.toString().startsWith(" ")) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                } else if (s.toString().startsWith(".")) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                    s.append("0.");
                }
                checkGpValue = s.toString();
               // AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),s.toString(),9);
                calculateValues();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        hitCoinDetailApi();

    }

    private void hitCoinDetailApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCoinDetailJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseCoinDetailJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject dataObject = jsonObject.getJSONObject("data");


                price = AppUtils.returnDouble(dataObject.getString("ganderPrice"));



            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn25Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                calculateValue(25);

                break;

            case R.id.btn50Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                calculateValue(50);

                break;

            case R.id.btn75Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                calculateValue(75);

                break;

            case R.id.btn100Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.blue));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                calculateValue(100);

                break;
            case R.id.btnSubmit:

                validate();

                break;


        }
    }

    private void validate() {

        double valueEntered = 0;

        if (!b.etInGyre.getText().toString().trim().isEmpty()) {
            valueEntered = AppUtils.returnDouble(b.etInGyre.getText().toString().trim());
        }
        if (b.etInGyre.getText().toString().trim().isEmpty()) {
            b.etInGyre.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterValue));
        } else if (valueEntered<=0) {
            b.etInGyre.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectValue));
        } else if (valueEntered > walletBalance) {
            b.etInGyre.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.enteredCoinGreaterValue));
        } else {
            hitSellApi();
        }
    }

    private void hitSellApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("greenPointsSold", b.etInGyre.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        WebServices.postApi(mActivity, AppUrls.sellGreenPoints, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSellJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSellJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppUtils.showMessageDialog(mActivity, getString(R.string.successfullySold), jsonObject.getString(AppConstants.resMsg), 3);

            } else {

                AppUtils.showMessageDialog(mActivity, getString(R.string.sell), jsonObject.getString(AppConstants.resMsg), 2);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void calculateValues() {

        BigDecimal coinValue;
        try {

            coinValue = BigDecimal.valueOf(AppUtils.returnDouble(b.etInGyre.getText().toString().trim()))
                    .multiply(BigDecimal.valueOf(price));
            if (checkGpValue.equalsIgnoreCase("0")||checkGpValue.equalsIgnoreCase("00")||checkGpValue.equalsIgnoreCase("000")||checkGpValue.equalsIgnoreCase("0000")||
                    checkGpValue.equalsIgnoreCase("00000")||checkGpValue.equalsIgnoreCase("000000")||
                    checkGpValue.equalsIgnoreCase("0000000")||checkGpValue.equalsIgnoreCase("00000000")
                    ||checkGpValue.equalsIgnoreCase("000000000")||checkGpValue.equalsIgnoreCase("0000000000"))
            {
                b.etRupee.setText("");
            }
            else
            {
                b.etRupee.setText(String.valueOf(coinValue));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (b.etInGyre.getText().toString().trim().isEmpty()) b.etRupee.setText("");

    }

    private void calculateValue(int i) {
        if (walletBalance!=0)
        {
            double amount = (walletBalance * i) / 100;
            b.etInGyre.setText(String.valueOf(amount));
        }



    }
}