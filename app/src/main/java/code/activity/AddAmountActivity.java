package code.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityAddAmountBinding;

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

public class AddAmountActivity extends BaseActivity implements View.OnClickListener {

    ActivityAddAmountBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddAmountBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }
    @SuppressLint("SetTextI18n")
    private void inits() {


        b.btn100.setOnClickListener(this);
        b.btn500.setOnClickListener(this);
        b.btn1000.setOnClickListener(this);
        b.btn2000.setOnClickListener(this);
        b.btn5000.setOnClickListener(this);
        b.btnAdd.setOnClickListener(this);

//        b.tvAmount.setText(getString(R.string.rupeeSymbol) + AppSettings.getString(AppSettings.walletBalance));

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.addMoneyWallet));

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

        if (getIntent().getStringExtra("pageFrom").equals("1")) {

            b.tvMinDeposit.setText(getString(R.string.minDeposit));
        } else if (getIntent().getStringExtra("pageFrom").equals("2")) {
            b.tvMinDeposit.setText(getString(R.string.minDepositOnline));
        } else if (getIntent().getStringExtra("pageFrom").equals("3")) {
            b.tvMinDeposit.setText(getString(R.string.minimum100));
        } else
            b.tvMinDeposit.setText("");


        Intent intent = getIntent();

        //Coming From WebViewPaymentActivity after payment
        if (intent.hasExtra("type")) {

            showAfterPaymentDialog(intent.getStringExtra("type"));

        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn100:

                sumValue(100);

                break;

            case R.id.btn500:

                sumValue(500);

                break;

            case R.id.btn1000:

                sumValue(1000);

                break;

            case R.id.btn2000:

                sumValue(2000);

                break;

            case R.id.btn5000:

                sumValue(5000);

                break;

            case R.id.btnAdd:

                double enteredAmount = 0;
                if (!b.etAmount.getText().toString().trim().isEmpty()) {
                    enteredAmount = Double.parseDouble(b.etAmount.getText().toString().trim());
                }

                if (b.etAmount.getText().toString().trim().isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAmount));
                }
                //Not upi
                else if (enteredAmount < 100 && !getIntent().getStringExtra("pageFrom").equals("3")) {
                    AppUtils.showToastSort(mActivity, getString(R.string.minimumDeposit));
                }
                //UPI
                else if (enteredAmount < 100 && getIntent().getStringExtra("pageFrom").equals("3")) {
                    AppUtils.showToastSort(mActivity, getString(R.string.minimumDeposit));
                } else if (enteredAmount > 5000000 && getIntent().getStringExtra("pageFrom").equals("1")) {
                    AppUtils.showToastSort(mActivity, getString(R.string.maximumDeposit));
                } else if (enteredAmount > 200000 && getIntent().getStringExtra("pageFrom").equals("2")) {
                    AppUtils.showToastSort(mActivity, getString(R.string.maximumDepositOnline));
                } else {

                    if (AppSettings.getString(AppSettings.depositOnHold).equals("0")) {

                        GlobalData.paymentFor = "2";

                        if (getIntent().getStringExtra("pageFrom").equals("1")) {

                            Intent intent = new Intent(mActivity, NeftActivity.class);
                            intent.putExtra("amount", b.etAmount.getText().toString().trim());
                            startActivity(intent);
                        } else if (getIntent().getStringExtra("pageFrom").equals("3")) {

                            Intent intent = new Intent(mActivity, UpiActivity.class);
                            intent.putExtra("amount", b.etAmount.getText().toString().trim());
                            startActivity(intent);
                        } else {

                            validate();
                        }

                    } else {
                        AppUtils.showMessageDialog(mActivity, getString(R.string.deposit),
                                getString(R.string.yourDepositIsPending), 2);
                    }

                }

                break;

        }
    }

    private void sumValue(int value) {
        if (!b.etAmount.getText().toString().isEmpty()) {
            double mainValue = Double.parseDouble(b.etAmount.getText().toString());

            mainValue = mainValue + value;

            b.etAmount.setText(String.valueOf(mainValue));
        } else {
            b.etAmount.setText(String.valueOf(value));
        }
    }

    private void validate() {

        double enteredAmount = AppUtils.returnDouble(b.etAmount.getText().toString().trim());

        if (enteredAmount > 200000) {
            AppUtils.showMessageDialog(mActivity, getString(R.string.deposit), getString(R.string.maximumDepositOnline), 2);
        } else {

/*            Intent intent = new Intent(mActivity, WebViewPaymentActivity.class);
            intent.putExtra("amount", b.etAmount.getText().toString().trim());
            intent.putExtra("type", "2");//1=Upi, 2=Bank
            startActivity(intent);*/
        }

    }

    private void showAfterPaymentDialog(String type) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
//        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        TextView tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);

        tvTitle.setText(getString(R.string.app_name));

        if (type.equals("1"))
            tvMessage.setText(getString(R.string.amountDepositSuccess));
        else if (type.equals("2"))
            tvMessage.setText(getString(R.string.amountDepositFailed));

//        bottomSheetDialog.findViewById(R.id.btnContinue).setOnClickListener(v -> bottomSheetDialog.dismiss());
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

                b.tvAmount.setText(getString(R.string.rupeeSymbol) + " " + jsonObject.getJSONObject("data").getString("totalWalletBalance"));

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