package code.activity;

import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityHelpSupportBinding;

import code.utils.AppUrls;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class HelpSupportActivity extends BaseActivity implements View.OnClickListener {

    private ActivityHelpSupportBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHelpSupportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.helpSupport24));
        b.rlFaqs.setOnClickListener(this);
        b.rlPrivacyPolicy.setOnClickListener(this);
        b.rlTnc.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.rlFaqs:

                AppUtils.openChromeCustomTabUrl(AppUrls.tnc, mActivity);

                break;

            case R.id.rlPrivacyPolicy:

                AppUtils.openChromeCustomTabUrl(AppUrls.privacyPolicy, mActivity);

                break;

            case R.id.rlTnc:
                AppUtils.openChromeCustomTabUrl(AppUrls.tnc, mActivity);


                break;


        }

    }
}