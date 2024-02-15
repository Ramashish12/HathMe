package code.common;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import code.activity.CartActivity;


public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                CartActivity.isMapTouched = true;
                break;

            case MotionEvent.ACTION_UP:
                CartActivity.isMapTouched = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}