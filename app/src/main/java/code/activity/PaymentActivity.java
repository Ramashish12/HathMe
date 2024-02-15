package code.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityPaymentBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class PaymentActivity extends BaseActivity implements View.OnClickListener {

    private ActivityPaymentBinding b;

    private double gpBalance = 0, walletBalance = 0, gpPrice = 0, gpPriceAfterComm = 0, totalAmount = 0, gpValueInInr = 0;

    private float gpCommission = 10;

    //1 => Wallet,   2 => Online,  3 => COD,   4=> GP, 5=>wallet+GP,6=>wallet+Online,7=>GP+Online, 8=>Wallet + GP+ Online (edited)
    private int paymentMode = 0;

    private boolean isGpSelected, isWalletSelected;

    private String addressId = "", tip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.paymentMode));

        b.rlGp.setOnClickListener(this);
        b.rlWallet.setOnClickListener(this);
        b.rlCash.setOnClickListener(this);

        b.ivInfoGp.setOnClickListener(this);

        b.tvProceed.setOnClickListener(this);

        getIntentValues();

        hitMyProfileApi();

    }

    @SuppressLint("SetTextI18n")
    private void getIntentValues() {

        if (getIntent().getExtras() != null) {

            Intent intent = getIntent();

            totalAmount = AppUtils.returnDouble(intent.getStringExtra("totalAmount"));
            addressId = intent.getStringExtra("addressId");
            tip = intent.getStringExtra("tip");

            b.tvTotalAmount.setText(getString(R.string.totalAmount) + ": " + getString(R.string.rupeeSymbol) + " " + totalAmount);

        }
    }

    private void hitMyProfileApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseMyProfile(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseMyProfile(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                jsonObject = jsonObject.getJSONObject("data");

                gpBalance = AppUtils.returnDouble(jsonObject.getString("greenPoints"));
                walletBalance = AppUtils.returnDouble(jsonObject.getString("walletBalance"));
                gpPrice = AppUtils.returnDouble(jsonObject.getString("ganderPrice"));

                gpCommission = AppUtils.returnFloat(jsonObject.getString("percentageGp"));

                gpPriceAfterComm = gpPrice - (gpPrice / gpCommission);

                gpValueInInr = gpBalance * gpPriceAfterComm;

                b.tvAvailableGp.setText(getString(R.string.availableGp) + ": " + gpBalance);
                b.tvWalletBalance.setText(getString(R.string.availableWalletBalance) + ": " + getString(R.string.rupeeSymbol) + " " + walletBalance);

                b.tvCurrentGpValue.setText(getString(R.string.currentValue) + ": " + gpBalance + "*" + getString(R.string.rupeeSymbol) + " " + AppUtils.roundOff2Digit(String.valueOf(gpPriceAfterComm)) + " = " + getString(R.string.rupeeSymbol) + " " +
                        AppUtils.roundOff2Digit(String.valueOf(gpValueInInr)));

                b.rlCash.setVisibility(jsonObject.getString("statusCOD").equals("1") ? View.VISIBLE : View.GONE);
                b.rlWallet.setVisibility(jsonObject.getString("statusWallet").equals("1") ? View.VISIBLE : View.GONE);
                b.rlGp.setVisibility(jsonObject.getString("statusGp").equals("1") ? View.VISIBLE : View.GONE);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivInfoGp:

                showInfo();

                break;

            case R.id.rlGp:

                setGpMode();

                break;

            case R.id.rlWallet:

                setWalletMode();

                break;

            case R.id.rlCash:
                setDefault();
                b.ivRadioCash.setImageResource(R.drawable.ic_radio_button_checked);
                paymentMode = 3;
                isGpSelected = false;
                isWalletSelected = false;

                break;

            case R.id.tvProceed:

                validate();

                break;

        }

    }

    private void validate() {

        //1 => Wallet,   2 => Online,  3 => COD,   4=> GP, 5=>wallet+GP,6=>wallet+Online,7=>GP+Online, 8=>Wallet + GP+ Online (edited)

        if (paymentMode == 1 && walletBalance < totalAmount) {
            AppUtils.showToastSort(mActivity, getString(R.string.insufficientWalletBalance));
        } else if (paymentMode == 4 && gpValueInInr < totalAmount) {
            AppUtils.showToastSort(mActivity, getString(R.string.insufficientGpBalance));
        } else if (paymentMode == 5 && (gpValueInInr + walletBalance) < totalAmount) {
            AppUtils.showToastSort(mActivity, getString(R.string.insufficientBalance));
        } else {
            hitPlaceOrderApi();
        }

    }

    private void hitPlaceOrderApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("addressId", addressId);
            jsonObject.put("tip", tip);
            jsonObject.put("paymentMode", String.valueOf(paymentMode));
            jsonObject.put("suggestion", "");

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.placeOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parsePlaceOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parsePlaceOrder(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppSettings.putString(AppSettings.deliveryTip, "");
                AppSettings.putString(AppSettings.tipCase, "");
                AppSettings.putString(AppSettings.totalPayableAmount, "");
                AppUtils.showMessageDialog(mActivity, getString(R.string.cart), getString(R.string.orderPlacedSuccessfully), 3);

            } else {
                if (jsonObject.getString(AppConstants.resMsg).equalsIgnoreCase("order cancel")) {
                    showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.itemUnavailable));
                } else {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.cart), jsonObject.getString(AppConstants.resMsg), 2);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showMessageDialog(Activity mActivity, String title, String message) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
        //bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvTitle.setText(title);
        tvMessage.setText(message);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();
            startActivity(new Intent(mActivity, MainActivity.class));
            finishAffinity();

        });

    }


    private void setWalletMode() {

        setDefault();
        paymentMode = 1;
        b.ivRadioWallet.setImageResource(R.drawable.ic_radio_button_checked);

        if (isGpSelected && gpValueInInr < totalAmount) {
            b.ivRadioGp.setImageResource(R.drawable.ic_radio_button_checked);
            isGpSelected = true;
            paymentMode = 5;
        }

        isWalletSelected = true;
        b.ivRadioCash.setImageResource(R.drawable.ic_radio_button_unchecked);

    }

    private void setGpMode() {

        setDefault();
        b.ivRadioGp.setImageResource(R.drawable.ic_radio_button_checked);
        paymentMode = 4;

        //GP+wallet
        if (isWalletSelected && walletBalance < totalAmount) {
            paymentMode = 5;
            b.ivRadioWallet.setImageResource(R.drawable.ic_radio_button_checked);
            isWalletSelected = true;

        }

        isGpSelected = true;
        b.ivRadioCash.setImageResource(R.drawable.ic_radio_button_unchecked);

    }

    private void setDefault() {

        b.ivRadioGp.setImageResource(R.drawable.ic_radio_button_unchecked);
        b.ivRadioWallet.setImageResource(R.drawable.ic_radio_button_unchecked);
        b.ivRadioCash.setImageResource(R.drawable.ic_radio_button_unchecked);


    }

    @SuppressLint("SetTextI18n")
    private void showInfo() {

        Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_gp_info);
        dialog.show();

        TextView tvCurrentGpValue, tvGpCommission, tvGpCalculated;

        tvCurrentGpValue = dialog.findViewById(R.id.tvCurrentGpValue);
        tvGpCommission = dialog.findViewById(R.id.tvGpCommission);
        tvGpCalculated = dialog.findViewById(R.id.tvGpCalculated);

        tvCurrentGpValue.setText(getString(R.string.currentValue) + ": " + getString(R.string.rupeeSymbol) + " " + gpPrice);
        tvGpCommission.setText(getString(R.string.commission) + ": " + gpCommission + "%");
        tvGpCalculated.setText(getString(R.string.caculatedValue) + ": " + getString(R.string.rupeeSymbol) + " " + gpPriceAfterComm);

    }
}