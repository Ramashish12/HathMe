package code.basic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.hathme.android.R;

import code.activity.MainActivity;
import code.activity.StoreActivity;
import code.utils.AppSettings;
import code.view.BaseActivity;

public class SplashActivity extends BaseActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler(Looper.myLooper()).postDelayed(() -> {

            if (AppSettings.getString(AppSettings.userId).isEmpty()) {

                startActivity(new Intent(mActivity, LoginTypeActivity.class));
            } else if (AppSettings.getString(AppSettings.isProfileCompleted).equals("0")) {
                startActivity(new Intent(mActivity, SignUpActivity.class));
            } else {

                checkDeepLink();

            }


            finishAffinity();

        }, 1000);
    }

    private void checkDeepLink() {

        if (getIntent().getExtras() != null) {

            Intent intent = getIntent();
            Uri uri = intent.getData();


            if (uri.getQueryParameter("merchantId")!=null){
                startActivity(new Intent(mActivity, StoreActivity.class).putExtra("merchantId",
                        uri.getQueryParameter("merchantId")));
            }
            else{
            startActivity(new Intent(mActivity, MainActivity.class));

            }

        } else {
            startActivity(new Intent(mActivity, MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {

    }
}