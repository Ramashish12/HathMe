package code.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityLoginTypeBinding;

import code.utils.AppSettings;
import code.view.BaseActivity;

public class LoginTypeActivity extends BaseActivity implements View.OnClickListener {

    ActivityLoginTypeBinding b;

    private int dotsCount;

    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginTypeBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        AppSettings.putString(AppSettings.language, "en");

        inits();

    }

    private void inits() {

        b.tvLogin.setOnClickListener(this);
        b.tvSignUp.setOnClickListener(this);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        b.viewPager.setAdapter(viewPagerAdapter);
        setSliderDots(viewPagerAdapter);


        getFcmToken();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
/*
            case R.id.tvRegister:

                startActivity(new Intent(mActivity, RegisterActivity.class));

                break;*/

            case R.id.tvLogin:
            case R.id.tvSignUp:
                startActivity(new Intent(mActivity, LoginActivity.class));
                break;

        }

    }

    public static class ViewPagerAdapter extends PagerAdapter {

        private final Context context;
        private final Integer[] images = {R.mipmap.ic_slide_image_1, R.mipmap.ic_slide_image_2,
                R.mipmap.ic_slide_image_3, R.mipmap.ic_slide_image_4};

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.custom_layout, container, false);

            ImageView imageView = (ImageView) view.findViewById(R.id.ivImage);
            imageView.setImageResource(images[position]);

            ViewPager vp = (ViewPager) container;
            vp.addView(view, 0);
            return view;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

            ViewPager vp = (ViewPager) container;
            View view = (View) object;
            vp.removeView(view);

        }
    }


    private void setSliderDots(ViewPagerAdapter viewPagerAdapter) {

        dotsCount = viewPagerAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.swipe_circle_icon));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            b.llSlidersDot.addView(dots[i], params);

        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.swipe_selected_icon));

        b.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.swipe_circle_icon));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.swipe_selected_icon));


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

    private void getFcmToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    AppSettings.putString(AppSettings.fcmToken, token);
                    Log.d("TAG", token);
                });
    }
}