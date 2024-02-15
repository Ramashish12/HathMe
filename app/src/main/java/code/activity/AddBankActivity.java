package code.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityAddBankBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class AddBankActivity extends BaseActivity implements View.OnClickListener {

    ActivityAddBankBinding b;

    private int accountType = 1;

    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddBankBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.tvHeader.setText(getString(R.string.addBank));
        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.rlCurrent.setOnClickListener(this);
        b.rlSaving.setOnClickListener(this);

        b.btnSubmit.setOnClickListener(this);
        b.etName.setInputType(InputType.TYPE_CLASS_TEXT);
        b.etName.setFilters(new InputFilter[]{new AppUtils.SpecialCharacterFilter()});

        b.etBank.setInputType(InputType.TYPE_CLASS_TEXT);
        b.etBank.setFilters(new InputFilter[]{new AppUtils.SpecialCharacterFilter()});
        getIntentValues();
    }

    private void getIntentValues() {

        if (getIntent().getExtras() != null) {

            isUpdating = true;

            Intent intent = getIntent();
            b.etBank.setText(intent.getStringExtra("bankName"));
            b.etName.setText(intent.getStringExtra("name"));
            b.etAccountNumber.setText(intent.getStringExtra("accountNumber"));
            b.etIfscCode.setText(intent.getStringExtra("ifsc"));
            accountType = AppUtils.returnInt(intent.getStringExtra("accountType"));
            if (accountType == 1) {
                b.ivCurrent.setImageResource(R.drawable.ic_radio_button_unchecked);
                b.ivSaving.setImageResource(R.drawable.ic_radio_button_checked);
            } else {
                b.ivSaving.setImageResource(R.drawable.ic_radio_button_unchecked);
                b.ivCurrent.setImageResource(R.drawable.ic_radio_button_checked);
            }

        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnSubmit:

                validate();

                break;
            case R.id.rlCurrent:

                accountType = 2;

                b.ivSaving.setImageResource(R.drawable.ic_radio_button_unchecked);
                b.ivCurrent.setImageResource(R.drawable.ic_radio_button_checked);

                break;

            case R.id.rlSaving:

                accountType = 1;
                b.ivSaving.setImageResource(R.drawable.ic_radio_button_checked);
                b.ivCurrent.setImageResource(R.drawable.ic_radio_button_unchecked);

                break;
        }
    }

    private void validate() {

        if (b.etName.getText().toString().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterName));
        } else if (b.etBank.getText().toString().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterBankName));
        } else if (b.etAccountNumber.getText().toString().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAccount));
        } else if (b.etIfscCode.getText().toString().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterIFSCCode));
        } else if (b.etIfscCode.getText().toString().trim().length() < 11) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectIFSCCode));
        } /*else if (!AppUtils.isStringOnlyAlphabet(b.etIfscCode.getText().toString().trim().substring(0, 4))) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectIFSCCode));
        } else if (!AppUtils.isStringOnlyNumbers(b.etIfscCode.getText().toString().trim().substring(4, 11))) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectIFSCCode));
        } */ else if (AppUtils.isNetworkAvailable(mActivity)) {
            hitBankDetailsApi();
        } else {
            AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
        }
    }

    private void hitBankDetailsApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            if (isUpdating)
                jsonObject.put("bankId", getIntent().getStringExtra("_id"));

            jsonObject.put("name", b.etName.getText().toString().trim());
            jsonObject.put("accountNumber", b.etAccountNumber.getText().toString().trim());
            jsonObject.put("ifsc", b.etIfscCode.getText().toString().trim());
            jsonObject.put("accountType", accountType); //1 saving , 2 Current
            jsonObject.put("bankName", b.etBank.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "";

        if (isUpdating) {
            url = AppUrls.UpdateBank;
        } else
            url = AppUrls.addBank;

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

                onBackPressed();
                AppUtils.showResMsgToastSort(mActivity, jsonObject);
            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}