package code.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class ImageUtils {

    public static void displayCircularImageFromUrl(Context context, String imageUrl, ImageView imageView) {
        if (context == null || TextUtils.isEmpty(imageUrl) || imageView == null) {
            return;
        }

        if (isValidContextForGlide(context)){

            Glide.with(context)
                    .asBitmap()
                    .apply(new RequestOptions().centerCrop().dontAnimate())
                    .load(imageUrl)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }

    }
    private static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
