package code.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityOrderTransactionBinding;

import code.view.BaseActivity;

public class OrderTransactionActivity extends BaseActivity implements View.OnClickListener {

    ActivityOrderTransactionBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityOrderTransactionBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.ordersTransactions));
        b.rlOrderHistory.setOnClickListener(this);
        b.rlTransaction.setOnClickListener(this);
        b.rlRequestOrder.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlOrderHistory:

                startActivity(new Intent(mActivity, OrderHistoryActivity.class));

                break;

            case R.id.rlRequestOrder:
                startActivity(new Intent(mActivity, RequestOrderListActivity.class));
                break;


        }

    }
}