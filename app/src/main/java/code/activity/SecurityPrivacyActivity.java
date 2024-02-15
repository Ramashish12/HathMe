package code.activity;

import android.os.Bundle;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySecurityPrivacyBinding;

import code.utils.AppUrls;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class SecurityPrivacyActivity extends BaseActivity implements View.OnClickListener {
    private ActivitySecurityPrivacyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecurityPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inits();
    }

    private void inits() {
        binding.header.ivBack.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.securityPrivacy));
        binding.rlFaqs.setOnClickListener(this);
        binding.rlPrivacyPolicy.setOnClickListener(this);
        binding.rlTermsConditions.setOnClickListener(this);
        binding.rlAboutUs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            case R.id.rlFaqs:
                AppUtils.openChromeCustomTabUrl(AppUrls.faq, mActivity);
                break;
            case R.id.rlPrivacyPolicy:
                AppUtils.openChromeCustomTabUrl(AppUrls.privacyPolicy, mActivity);
                break;
            case R.id.rlTermsConditions:
                AppUtils.openChromeCustomTabUrl(AppUrls.tnc, mActivity);
                break;
            case R.id.rlAboutUs:
                AppUtils.openChromeCustomTabUrl(AppUrls.About, mActivity);
                break;
        }
    }
}