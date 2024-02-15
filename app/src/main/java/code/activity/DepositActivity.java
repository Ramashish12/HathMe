package code.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityDepositBinding;

import code.utils.AppSettings;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class DepositActivity extends BaseActivity implements View.OnClickListener {

    ActivityDepositBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDepositBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {


        b.rlOnline.setOnClickListener(this);
        b.rlBank.setOnClickListener(this);
        b.rlUpi.setOnClickListener(this);
        b.header.ivBack.setOnClickListener(this);

        setHold();

    }

    private void setHold() {

        if (AppSettings.getString(AppSettings.upiDepositOnHold).equals("1") ||
                AppSettings.getString(AppSettings.depositUPI).isEmpty()) {
            b.tvUpiHold.setVisibility(View.VISIBLE);
//            b.tvRecommendedMethod.setVisibility(View.GONE);
        } else {
            b.tvUpiHold.setVisibility(View.GONE);
        }

        b.tvBankHold.setVisibility(AppSettings.getString(AppSettings.bankDepositOnHold).equals("1") ? View.VISIBLE : View.GONE);
        b.tvOnlineHold.setVisibility(AppSettings.getString(AppSettings.onlineDepositOnHold).equals("1") ? View.VISIBLE : View.GONE);
        b.tvUpiHold.setVisibility(AppSettings.getString(AppSettings.upiDepositOnHold).equals("1") ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlBank:

                if (AppSettings.getString(AppSettings.bankDepositOnHold).equals("1")) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.deposit), getString(R.string.bankTransferOnHold), 2);
                } else
                    startActivity(new Intent(mActivity, AddAmountActivity.class).putExtra("pageFrom", "1"));

                break;

            case R.id.ivBack:
                finish();
               break;
            case R.id.rlUpi:

                if (AppSettings.getString(AppSettings.upiDepositOnHold).equals("1")) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.deposit), getString(R.string.upiPaymentOnHold), 2);
                } else
                    startActivity(new Intent(mActivity, AddAmountActivity.class).putExtra("pageFrom", "3"));

                break;
        }

    }
}