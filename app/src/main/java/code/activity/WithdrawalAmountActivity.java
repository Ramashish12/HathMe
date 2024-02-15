package code.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityWithdrawalAmountBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.GlobalData;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class WithdrawalAmountActivity extends BaseActivity implements View.OnClickListener{

    private ActivityWithdrawalAmountBinding b;
    private double walletBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityWithdrawalAmountBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.btn25Perc.setOnClickListener(this);
        b.btn50Perc.setOnClickListener(this);
        b.btn75Perc.setOnClickListener(this);
        b.btn100Perc.setOnClickListener(this);
        b.btnWithdrawl.setOnClickListener(this);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.withdrawal));

        b.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0 && !Pattern.matches(AppConstants.numberDotRgx, s.toString())) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                } else if (s.toString().indexOf('.', s.toString().indexOf('.') + 1) != -1) {
                    s.delete(s.toString().length() - 1, s.toString().length());

                } else if (s.toString().startsWith(" ")) {
                    s.delete(s.toString().length() - 1, s.toString().length());
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.btn25Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                calculateValue(25);

                break;

            case R.id.btn50Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                calculateValue(50);

                break;

            case R.id.btn75Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                calculateValue(75);

                break;

            case R.id.btn100Perc:
                b.btn25Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn50Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn75Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.green));
                b.btn100Perc.setBackgroundTintList(ContextCompat.getColorStateList(mActivity,R.color.redDark));
                calculateValue(100);

                break;
            case R.id.btnWithdrawl:

                validate();

                break;


        }
    }

    private void validate() {

        double enteredAmount = AppUtils.returnDouble(b.etAmount.getText().toString().trim());

        if (enteredAmount ==0 ) {
            AppUtils.showToastSort(mActivity, getString(R.string.invalidAmount));
        }
        else if (enteredAmount>walletBalance) {
            AppUtils.showToastSort(mActivity, getString(R.string.notSufficientBalance));
        }
        else {

            Intent intent=new Intent(mActivity, BankListActivity.class);
            intent.putExtra("amount", String.valueOf(enteredAmount));
            intent.putExtra("pageFrom","2");
            startActivity(intent);
        }

    }


    private void calculateValue(int i) {

        double amount = (walletBalance * i) / 100;
        b.etAmount.setText(String.valueOf(amount));

    }

    private void hitGetWalletDataApi() {

        WebServices.getApi(mActivity, AppUrls.transactionHistory, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseGetDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                walletBalance = AppUtils.returnDouble(jsonObject.getJSONObject("data").getString("totalWalletBalance"));
                b.tvAmount.setText(getString(R.string.rupeeSymbol) + " " + walletBalance);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hitGetWalletDataApi();
    }
}