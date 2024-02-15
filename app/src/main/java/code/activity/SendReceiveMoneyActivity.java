package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySendReceiveMoneyBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import code.common.QrCodeScannerActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SendReceiveMoneyActivity extends BaseActivity implements View.OnClickListener{

    private ActivitySendReceiveMoneyBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b =ActivitySendReceiveMoneyBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.sendReceiveMoney));

        b.llSendView.setOnClickListener(this);
        b.llReceiveView.setOnClickListener(this);

        b.btnSendTab.setOnClickListener(this);
        b.btnReceiveTab.setOnClickListener(this);
        b.ivCopy.setOnClickListener(this);
        b.ivQr.setOnClickListener(this);
        b.btn25Perc.setOnClickListener(this);
        b.btn50Perc.setOnClickListener(this);
        b.btn75Perc.setOnClickListener(this);
        b.btn100Perc.setOnClickListener(this);
        b.btnSend.setOnClickListener(this);

        String url = "https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=" + AppSettings.getString(AppSettings.mobile) + "&choe=UTF-8";

        AppUtils.loadPicassoImage(url, b.ivQrCode);
        b.tvMobile.setText(AppSettings.getString(AppSettings.mobile));

    }

    @Override
    protected void onResume() {
        super.onResume();

        hitDetailApi();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.btnSendTab:

                setSend();

                break;

            case R.id.btnReceiveTab:

                setReceive();

                break;

            case R.id.btn25Perc:

                calculateValue(25);

                break;

            case R.id.btn50Perc:

                calculateValue(50);

                break;

            case R.id.btn75Perc:

                calculateValue(75);

                break;

            case R.id.btn100Perc:

                calculateValue(100);

                break;

            case R.id.ivCopy:

                ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Text", b.tvMobile.getText().toString().trim());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(mActivity, getString(R.string.clipboardMobileCopied), Toast.LENGTH_SHORT).show();

                break;

            case R.id.btnSend:

                validate();

                break;

            case R.id.ivQr:

                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    Intent intent = new Intent(mActivity, QrCodeScannerActivity.class);
                    startActivityForResult(intent, 0);

                } else {

                    AppUtils.showToastSort(mActivity, getString(R.string.requiredPermissionsMissing));

                }

                break;
        }

    }

    private void setSend() {

        b.btnSendTab.setTextColor(getResources().getColor(R.color.white));
        b.btnReceiveTab.setTextColor(getResources().getColor(R.color.black));
        b.btnSendTab.setBackgroundResource(R.drawable.rectangular_bg_left_radius);
        b.btnReceiveTab.setBackgroundResource(R.drawable.rectangular_bg_white_right_radius);
        b.llSendView.setVisibility(View.VISIBLE);
        b.llReceiveView.setVisibility(View.GONE);
    }

    private void setReceive() {

        b.btnReceiveTab.setTextColor(getResources().getColor(R.color.white));
        b.btnSendTab.setTextColor(getResources().getColor(R.color.black));
        b.btnSendTab.setBackgroundResource(R.drawable.rectangular_bg_white_left_radius);
        b.btnReceiveTab.setBackgroundResource(R.drawable.rectangular_bg_right_radius);
        b.llSendView.setVisibility(View.GONE);
        b.llReceiveView.setVisibility(View.VISIBLE);
    }

    private void calculateValue(int i) {

        double wallet = AppUtils.getStringToDouble(AppSettings.getString(AppSettings.walletBalance));

        double amount = (wallet * i) / 100;

        String result = new DecimalFormat("##.##").format(amount);
        b.etAmountRec.setText(result);

    }

    private void validate() {
        AppUtils.disableDoubleClick(this, b.btnSend);

        double wallet = 0.0;
        double walletBalance = 0.0;
        try {
            wallet = Double.parseDouble(b.etAmountRec.getText().toString().trim());
            walletBalance = Double.parseDouble(AppSettings.getString(AppSettings.walletBalance));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (b.etMobile.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterMobile));
        } else if (b.etReferenceNameSend.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterReceiverName));
        } else if (b.etAmountRec.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAmount));
        } else if (wallet > walletBalance) {
            AppUtils.showToastSort(mActivity, getString(R.string.enteredAmountShouldBeLess));
        } else {
            if (AppSettings.getString(AppSettings.isWithdrawPinCreated).equals("0")) {

                AppUtils.showToastSort(mActivity, getString(R.string.pleaseCreateWithdrawPin));
                startActivity(new Intent(mActivity, CreateWithdrawalPinActivity.class).putExtra("pageFrom","1"));

            } else {

                Intent intent = new Intent(mActivity, WithdrawalPinActivity.class);
                intent.putExtra("pageFrom", "1");
                intent.putExtra("mobile", b.etMobile.getText().toString().trim());
                intent.putExtra("name", b.etReferenceNameSend.getText().toString().trim());
                intent.putExtra("amount", b.etAmountRec.getText().toString().trim());
                startActivity(intent);

            }
        }
    }


    private void hitDetailApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDetailJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseDetailJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppSettings.putString(AppSettings.walletBalance,jsonObject.getJSONObject("data").getString("walletBalance"));
                b.tvAvailableBalance.setText(getString(R.string.availableBalance) + " : " + AppUtils.ifEmptyReturn0(AppSettings.getString(AppSettings.walletBalance)));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra(AppConstants.KEY_QR_CODE);
                b.etMobile.setText(contents);
            }
            if (resultCode == RESULT_CANCELED) {
                //handle cancel
            }
        }
    }
}