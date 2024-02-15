package code.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.hathme.android.databinding.ActivityImageViewerBinding;

import java.io.IOException;

import code.utils.AppUtils;
import code.view.BaseActivity;

public class ImageViewerActivity extends BaseActivity {

    private ActivityImageViewerBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());

        if (getIntent().getExtras() != null) {

            Intent intent = getIntent();

            if (intent.getStringExtra("type").equals("1")) {
                AppUtils.loadPicassoImage(intent.getStringExtra("data"), b.ivImage);
            } else {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(),
                            Uri.parse("file://" + intent.getStringExtra("data")));
                    b.ivImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }


        }
    }
}