package code.common;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebViewClient;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityWebViewBinding;

import code.utils.AppUrls;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding b;
    public static int from = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.webView.getSettings().setJavaScriptEnabled(true);
        b.webView.setWebViewClient(new WebViewClient());

        if (from==1)
        {
            b.header.tvHeader.setText(getString(R.string.overview));
            b.webView.loadUrl(AppUrls.overview);
        }
        else if (from==2)
        {
            b.header.tvHeader.setText(getString(R.string.readPolicy));
            b.webView.loadUrl(AppUrls.policyUrl);
        }

    }
}