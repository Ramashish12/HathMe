package code.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


import com.hathme.android.R;

import code.utils.OSettings;


public class BaseActivity extends AppCompatActivity {
    protected BaseActivity mActivity;
    private boolean noAnimation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        mActivity = this;
        new OSettings(mActivity);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionExit();
    }

    @Override
    public void finish() {
        super.finish();
        //overridePendingTransitionExit();
    }

    public void finishNew() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (noAnimation)
            overridePendingTransitionEnter();
        else
            noAnimation = true;
    }

    public void startActivity(Intent intent, boolean noAnimation) {
        this.noAnimation = noAnimation;
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransitionEnter();
    }


    /**
     * Overrides the pending Activity transition method performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        //overridePendingTransition(R.anim.zoom_exit, R.anim.zoom_enter);
    }

    /**
     * Overrides the pending Activity transition method performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        //overridePendingTransition(R.anim.zoom_exit, R.anim.zoom_enter);
        //overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }
}
