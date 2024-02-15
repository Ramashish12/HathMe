package code.utils;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.BuildConfig;
import com.hathme.android.R;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import code.activity.MainActivity;
import code.basic.LoginTypeActivity;
import code.common.MyApplication;
import code.view.BaseActivity;


public class AppUtils {
    public static Toast mToast;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    static ProgressDialog progressDialog;

    public static float convertDpToPixel(float dp) {
        return dp * (((float) Resources.getSystem().getDisplayMetrics().densityDpi) / 160.0f);
    }

    public static float convertPixelsToDp(float px) {
        return px / (((float) Resources.getSystem().getDisplayMetrics().densityDpi) / 160.0f);
    }

    public static String print(String mString) {
        return mString;
    }

    public static String printD(String Tag, String mString) {
        return mString;
    }

    public static String printE(String Tag, String mString) {
        return mString;
    }

    public static int startPosition(String word, String sourceString) {
        int startingPosition = sourceString.indexOf(word);
        print("startingPosition" + word + " " + startingPosition);
        return startingPosition;
    }

    public static int endPosition(String word, String sourceString) {
        int endingPosition = sourceString.indexOf(word) + word.length();
        print("startingPosition" + word + " " + endingPosition);
        return endingPosition;
    }

    public static void showToastSort(Context context, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mToast != null) {
                mToast.addCallback(new Toast.Callback() {
                    @Override
                    public void onToastShown() {
                        super.onToastShown();
                        mToast.cancel();
                    }
                });
            }
        } else {

            if (mToast != null && mToast.getView().isShown()) {
                mToast.cancel();
            }
        }
        mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void showResMsgToastSort(Context context, JSONObject jsonObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mToast != null) {
                mToast.addCallback(new Toast.Callback() {
                    @Override
                    public void onToastShown() {
                        super.onToastShown();
                        mToast.cancel();
                    }
                });
            }
        } else {

            if (mToast != null && mToast.getView().isShown()) {
                mToast.cancel();
            }
        }
        try {
            mToast = Toast.makeText(context, jsonObject.getString(AppConstants.resMsg), Toast.LENGTH_LONG);
        } catch (JSONException e) {
            e.printStackTrace();
            mToast = Toast.makeText(context, context.getString(R.string.somethingError), Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    public static void showInProgressToast(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mToast != null) {
                mToast.addCallback(new Toast.Callback() {
                    @Override
                    public void onToastShown() {
                        super.onToastShown();
                        mToast.cancel();
                    }
                });
            }
        } else {

            if (mToast != null && mToast.getView().isShown()) {
                mToast.cancel();
            }
        }
        mToast = Toast.makeText(context, context.getString(R.string.inProgress), Toast.LENGTH_LONG);

        mToast.show();
    }

    public static void showNoInternetToastSort(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mToast != null) {
                mToast.addCallback(new Toast.Callback() {
                    @Override
                    public void onToastShown() {
                        super.onToastShown();
                        mToast.cancel();
                    }
                });
            }
        } else {

            if (mToast != null && mToast.getView().isShown()) {
                mToast.cancel();
            }
        }
        mToast = Toast.makeText(context, context.getString(R.string.noInternetConnection), Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            try {
                @SuppressLint("WrongConstant") InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
                View view = activity.getCurrentFocus();
                if (view != null) {
                    IBinder binder = view.getWindowToken();
                    if (binder != null) {
                        inputMethodManager.hideSoftInputFromWindow(binder, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static float convertDpToPixel(float dp, Context context) {
        return (((float) getDisplayMetrics(context).densityDpi) / 160.0f) * dp;
    }

    public static int convertDpToPixelSize(float dp, Context context) {
        float pixels = convertDpToPixel(dp, context);
        int res = (int) (0.5f + pixels);
        if (res != 0) {
            return res;
        }
        if (pixels == 0.0f) {
            return 0;
        }
        if (pixels > 0.0f) {
            return 1;
        }
        return -1;
    }

    public static boolean isValidPhone(String pass) {
        return pass != null && pass.length() == 10;
    }


    public static void setCustomFont(Activity mActivity, TextView mTextView, String asset) {
        mTextView.setTypeface(Typeface.createFromAsset(mActivity.getAssets(), asset));
    }

    public static void showRequestDialog(Activity activity) {

        try {
            if (!activity.isFinishing()) {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }


                progressDialog = new ProgressDialog(activity);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(activity.getString(R.string.pleaseWait));
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void showRequestDialog(Activity activity, String message) {
        if (progressDialog == null) {
            //progressDialog = new ProgressDialog(activity, R.style.MyAlertDialogStyle);
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            progressDialog.show();
        }
    }

    public static void hideDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTncDate() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    /*public static void showErrorMessage(View mView, String errorMessage, Context mActivity) {
        Snackbar snackbar = Snackbar.make(mView, errorMessage, Snackbar.LENGTH_SHORT);
        TextView tv = (TextView) (snackbar.getView()).findViewById(android.support.design.R.id.snackbar_text);
        *//*Typeface font = Typeface.createFromAsset(mActivity.getAssets(), "centurygothic.otf");
        tv.setTypeface(font);*//*

        snackbar.show();
    }*/


    public static String toCamelCaseSentence(String s) {
        if (s == null) {
            return "";
        }
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String toCamelCaseWord : words) {
            sb.append(toCamelCaseWord(toCamelCaseWord));
        }
        return sb.toString().trim();
    }

    public static String toCamelCaseWord(String word) {
        if (word == null) {
            return "";
        }
        switch (word.length()) {
            case 0:
                return "";
            case 1:
                return word.toUpperCase(Locale.getDefault()) + " ";
            default:
                return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.getDefault()) + " ";
        }
    }

    public static String split(String str) {
        String result = "";
        if (str.contains(" ")) {
            return toCamelCaseWord(str.split("\\s+")[0]);
        }
        return toCamelCaseWord(str);
    }

    public static void expand(final View v) {
        v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? WindowManager.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {

        //v.setVisibility(View.GONE);

        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    // GetDeviceId
    public static String getDeviceID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDateCurrentTimeZone(long timestamp) {

        timestamp = timestamp * 1000;

        //DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy, hh:mm aa");
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        //System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getDateFromTimestamp(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("dd MMM hh:mm");

        //System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        }
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getTimeLineDate(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");

        //System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        }
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getTimeLineTime(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("hh:mm aa");

        //System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        }
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getTimeFromDate(String dateTime) {

        String result = "";

        String[] time = dateTime.split(" ");

        return time[1];
    }

    public static String getTimeFromTimestamp(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("hh:mm aa");

        //System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        }
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getCurrentDate() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current Date => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getCurrentMonth() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current Date => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);

        String[] parts = formattedDate.split("-");

        formattedDate = parts[0] + "-" + parts[1] + "-" + "01";

        return formattedDate;
    }

    public static String changeDateFormat(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }

  public static String changeDateFormat2(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }

    public static String changeDateTimeFormat(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }
    public static String changeDateFormatNew(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }

    public static String changeTimeFormat(String prev_date) {

        //"endDate":"2021-03-12","endTime":"17:32:00"

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("hh:mma", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }

    public static String changeTimeFormatNew(String prev_date) {

        //"endDate":"2021-03-12","endTime":"17:32:00"

        DateFormat originalFormat = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }


    public static String getCurrentTime() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current Time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getCurrentTimeIn12Hour() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current Time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("hh:mm aa");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getCurrentDateNew() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);

        return formattedDate;
    }


    public static String getCurrentDateTime() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy hh:mm aa", Locale.ENGLISH);
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getCurrentDateYMD(int addDays) {

        Calendar mcurrentDate = Calendar.getInstance();
        mcurrentDate.add(Calendar.DAY_OF_MONTH, addDays);

        Date c = mcurrentDate.getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getCurrentDateDMY(int addDays) {

        Calendar mcurrentDate = Calendar.getInstance();
        mcurrentDate.add(Calendar.DAY_OF_MONTH, addDays);

        Date c = mcurrentDate.getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static String getNewDateTimeFromTimestamp(long timestamp) {

        //DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        //System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String parseDateToFormat(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String parseDateToDMYFormat(String oldDate) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "yyyy-MM-dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(oldDate);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDateTimeFromTimestampNew(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yy hh:mm aa");

        System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();

        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        } else {
            calendar.setTimeInMillis(timestamp);
        }

        System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String getDateTimeFromTimestamp(long timestamp) {

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");

        System.out.println(timestamp);

        Calendar calendar = Calendar.getInstance();

        if (timestamp < 1000000000000L) {
            calendar.setTimeInMillis(timestamp * 1000);
        } else {
            calendar.setTimeInMillis(timestamp);
        }

        System.out.println(formatter.format(calendar.getTime()));

        String ret = formatter.format(calendar.getTime());

        return ret;
    }

    public static String parseDateTime(String prevDate) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(prevDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prevDate;

        return formattedDate;
    }

    public static String covertTimeToHours(String createdAt) {

        // Date Calculation
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        String currenttime = dateFormat.format(cal.getTime());

        Date CreatedAt = null;
        Date current = null;
        try {
            CreatedAt = dateFormat.parse(createdAt);
            current = dateFormat.parse(currenttime);
        } catch (ParseException e) {
            // TODO Auto-generated catch tableName
            e.printStackTrace();
        }

        // Get msec from each, and subtract.
        long diff = current.getTime() - CreatedAt.getTime();
        //long diffSeconds = diff / 1000;
        //long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String time = "0";
        if (diffDays > 0) {
            diffDays = diffDays * 24;
        }

        if (diffHours > 0) {
            if (diffHours == 1) {
                time = String.valueOf(diffHours + diffDays);
            } else {
                time = String.valueOf(diffHours + diffDays);
            }

        }
        return time;
    }

    public static String parseDate(String givenDateString) {
        if (givenDateString.equalsIgnoreCase("")) {
            return "";
        }

        long timeInMilliseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
        try {

            Date mDate = sdf.parse(givenDateString);
            timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        String result = "0";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");

        String todayDate = formatter.format(new Date());
        Calendar calendar = Calendar.getInstance();

        long dayagolong = timeInMilliseconds;
        calendar.setTimeInMillis(dayagolong);
        String agoformater = formatter.format(calendar.getTime());

        Date CurrentDate = null;
        Date CreateDate = null;

        try {
            CurrentDate = formatter.parse(todayDate);
            CreateDate = formatter.parse(agoformater);

            long different = Math.abs(CurrentDate.getTime() - CreateDate.getTime());

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            if (elapsedDays > 0) {
                elapsedDays = elapsedDays * 24;
            }

            if (elapsedHours > 0) {
                result = String.valueOf(elapsedHours + elapsedDays);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.v("result-Data", result);

        return result;
    }

    public static boolean isEmailValid(String email) {

        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static String getmiliTimeStamp() {

        long LIMIT = 10000000000L;

        long t = Calendar.getInstance().getTimeInMillis();

        return String.valueOf(t).substring(0, 10);
    }

    public static String changeHrFormat(String time) {



        String input = time;
        //Format of the date defined in the input String
        DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        //Desired format: 24 hour format: Change the pattern as per the need
        DateFormat outputformat = new SimpleDateFormat("HH:mm aa", Locale.ENGLISH);
        Date date = null;
        String output = null;
        try {
            //Converting the input String to Date
            date = df.parse(input);
            //Changing the format of date and storing it in String
            output = outputformat.format(date);
            //Displaying the date
            System.out.println(output);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        return output;
    }

    public static String getDifference(String del, String lmp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = sdf.parse(del);
            Date now = sdf.parse(lmp);
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            if (days < 7)
                return days + " Days";
            else
                return days / 7 + " Weeks";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public static int getWeekDifference(String lmpDate, String delDate) {
        int week = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = sdf.parse(lmpDate);
            Date now = sdf.parse(delDate);
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            if (days < 7)
                week = 0;
                //return hours + " Days";
            else
                week = (int) (days / 7);
            //return hours / 7 + " Weeks";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return week;
    }


    public static String getTimeDifference(String time1, String time2) {

        String timeDiff = "-1";
        if (!time1.isEmpty() && !time2.isEmpty()) {

            String[] morNight1 = time1.split(" ");
            String[] morNight2 = time2.split(" ");

            if (morNight1[1].equalsIgnoreCase("PM") && morNight2[1].equalsIgnoreCase("PM")) {
                if (morNight1[0].length() == 4) {
                    morNight1[0] = "0" + morNight1[0];
                }

                int b = Integer.parseInt(("" + morNight1[0]).substring(0, 2));

                if (b == 12) {
                    timeDiff = "1";
                } else {
                    int newTime1 = Integer.parseInt(morNight1[0].replaceAll(":", ""));
                    int newTime2 = Integer.parseInt(morNight2[0].replaceAll(":", ""));

                    if (newTime2 > newTime1) {
                        timeDiff = "1";
                    } else {
                        timeDiff = "-1";
                    }
                }

            } else if (morNight1[1].equalsIgnoreCase("AM") && morNight2[1].equalsIgnoreCase("AM")) {
                if (morNight1[0].length() == 4) {
                    morNight1[0] = "0" + morNight1[0];
                }

                int b = Integer.parseInt(("" + morNight1[0]).substring(0, 2));

                if (b == 12) {
                    timeDiff = "1";
                } else {
                    int newTime1 = Integer.parseInt(morNight1[0].replaceAll(":", ""));
                    int newTime2 = Integer.parseInt(morNight2[0].replaceAll(":", ""));

                    if (newTime2 > newTime1) {
                        timeDiff = "1";
                    } else {
                        timeDiff = "-1";
                    }
                }
            } else if (morNight1[1].equalsIgnoreCase("PM") && morNight2[1].equalsIgnoreCase("AM")) {
                timeDiff = "-1";
            } else {
                timeDiff = "1";
            }

        }


        return timeDiff;
    }

    public static String getDateAgo(String del) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(del);
            Date now = new Date(System.currentTimeMillis());
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            return days + " Days";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    public static String getDateDifference(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dt);
            Date now = new Date(System.currentTimeMillis());
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            long daysDiff = TimeUnit.MILLISECONDS.toDays(days);
            return String.valueOf(daysDiff);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String getDateDiff(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dt);
            Date now = new Date(System.currentTimeMillis());
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            return String.valueOf(days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static String getDateTimeDiff(String dt1, String dt2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
        try {
            Date delDate = sdf.parse(dt1);
            Date feedDate = sdf.parse(dt2);
            //Date now = new Date(System.currentTimeMillis());
            long days = getDateDiff(delDate, feedDate, TimeUnit.MINUTES);

            Log.d("days", String.valueOf(days));

            //long daysDiff = TimeUnit.MILLISECONDS.toDays(days);
            return String.valueOf(days);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0";
    }


    public static String getWeightDaysDiff(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dt);
            Date now = new Date(System.currentTimeMillis());
            long days = getDateDiff(date, now, TimeUnit.DAYS);
            return String.valueOf(days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Current Activity instance will go through its lifecycle to onDestroy() and a new instance then created after it.
     */
   /* @SuppressLint("NewApi")
    public static final void recreateActivityCompat(final Activity a) {
        GetBackFragment.ClearStack();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            a.recreate();
        } else {
            final Intent intent = a.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            a.finishAffinity();
            a.overridePendingTransition(0, 0);
            a.startActivity(intent);
            a.overridePendingTransition(0, 0);
        }
    }
*/
    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getCurrentLocale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return activity.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return activity.getResources().getConfiguration().locale;
        }
    }

    public static long dateDifference(String dob) {
        long day = 0;
        try {
            Date userDob = null;
            try {
                userDob = new SimpleDateFormat("dd-MM-yyyy HH:mm aa").parse(dob);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date today = new Date();
            long diff = today.getTime() - userDob.getTime();
            day = diff / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            e.printStackTrace();
            return day;
        }

        return day;
    }

    public static long getCurrentTimestamp() {

        return System.currentTimeMillis();
    }

    public static long currentTimestamp() {

        long timestamp = 0;

        Calendar mcurrentDate = Calendar.getInstance();

        // 2) get a java.util.Date from the calendar instance.
        //    this date will represent the current instant, or "now".
        Date now = mcurrentDate.getTime();

        // 3) a java current time (now) instance
        Timestamp currentTimestamp = new Timestamp(now.getTime());

        //timestamp = mcurrentDate.getTimeInMillis();
        timestamp = currentTimestamp.getTime() / 1000L;

        return timestamp;
    }


    public static String currentTimestampFormat() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c);

        return formattedDate;
    }


    public static void enableDisable(ViewGroup layout, boolean b) {
        layout.setEnabled(b);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                enableDisable((ViewGroup) child, b);
            } else {
                child.setEnabled(b);
            }
        }
    }

    public static String getDateInFormat() {

        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return sdf.format(date);
    }

    public static String dateToTimestamp(String time) {

        Timestamp ts = null;  //declare timestamp
        Date d = new Date(time); // Intialize date with the string date
        if (d != null) {  // simple null check
            ts = new Timestamp(d.getTime()); // convert gettime from date and assign it to your timestamp.
        }

        return ts.toString();
    }

    public static String changeDateToTimestamp(String time) {

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy, hh:mm aa");
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long output = date.getTime() / 1000L;
        String str = Long.toString(output);
        long timestamp = Long.parseLong(str) * 1000;

        return String.valueOf(timestamp);
    }

    public static String getMMMddFromDate(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("MMM dd");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }

    public static String getddMMMFromDate(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd MMM");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }


    public static String get24to12HourTime(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("HH dd");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        String formattedDate = "";
        if (date != null)
            formattedDate = targetFormat.format(date);
        else
            formattedDate = prev_date;

        return formattedDate;
    }


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }


        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static float getSSTTime(String stDnT, String endSnT) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(stDnT);
            Log.i("startDate", startDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = null;
        try {
            endDate = simpleDateFormat.parse(endSnT);
            Log.i("endDate", endDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long difference = endDate.getTime() - startDate.getTime();
        Log.i("log_tag", "difference: " + difference);

        float days = (difference / (1000 * 60 * 60 * 24));
        float hours = ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
        float min = (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
        Log.i("log_tag", "Hours: " + hours + ", Mins: " + min);

        return hours;
    }


    public static String getAge(String dobString) {

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) return "0";

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month + 1, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            if (age != 0)
                age--;
        }

        return String.valueOf(age);
    }


    public static String getAgeFromDOB(String dobDate) {

        int age = 0;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dobDate);

            try {

                if (dobDate != null) {

                    Date currDate = Calendar.getInstance().getTime();
                    // Log.d("Curr year === "+currDate.getYear()+" DOB Date == "+dobDate.getYear());
                    age = currDate.getYear() - date.getYear();
                    Log.d("Calculated Age == ", "" + age);
                }

            } catch (Exception e) {
                //Log.d(SyncStateContract.Constants.kApiExpTag, e.getMessage()+ "at Get Age From DOB mehtod.");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(date); // Sat Jan 02 00:00:00 GMT 2010

        return String.valueOf(age);

    }

    /*public static String getAge(String dateOfBirth){

        Log.d("dateOfBirth",dateOfBirth);

        int age = 0;

        if (!dateOfBirth.isEmpty()){

            int year,  month,  day;

            year= Integer.parseInt(dateOfBirth.substring(0,4));
            month= Integer.parseInt(dateOfBirth.substring(6,7));
            day= Integer.parseInt(dateOfBirth.substring(9,10));

            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            dob.set(year, month, day);

            if(today.get(Calendar.YEAR) > dob.get(Calendar.YEAR))
            {
                Log.d("dateOfBirth-0", String.valueOf(year));

                Log.d("dateOfBirth-1", String.valueOf(today.get(Calendar.YEAR)));

                Log.d("dateOfBirth-2", String.valueOf(dob.get(Calendar.YEAR)));

                age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            }
            else
            {
                age = 0;
            }

            Log.d("age",""+age);

            *//*if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }*//*

            //Log.d("age",""+age);

            //Integer ageInt = new Integer(age);
            //String ageS = ageInt.toString();

            return String.valueOf(age);
        }
        else
            return String.valueOf(age);

    }*/

    public static void changeLanguage(Activity mActivity) {

      /*  String languageToLoad  = AppSettings.getString(AppSettings.language); // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        mActivity.getResources().updateConfiguration(config,mActivity.getResources().getDisplayMetrics());*/
    }

    public static void deleteDirectory(File path) {
        path.delete();
    }

    public static boolean isValidMobileNo(String number) {
        // The given argument to compile() method
        // is regular expression. With the help of
        // regular expression we can validate mobile
        // number.
        // 1) Begins with 0 or 91
        // 2) Then contains 7 or 8 or 9.
        // 3) Then contains 9 digits
        Pattern p = Pattern.compile("(0/91)?[6-9][0-9]{9}");

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression
        Matcher m = p.matcher(number);
        return (m.find() && m.group().equals(number));
    }

    public static final String md5(String str) {

        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(str.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }

            //Log.v("md5",hexString.toString());
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setAutoOrientationEnabled(Context context, boolean enabled) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
    }

    public static String getMd5(String result, String breakFrom) {

        String md5String = "";

        String[] separated = result.split(breakFrom);
        String newResult = separated[1];

        String[] separated2 = newResult.split("],");

        String finalResult = separated2[0] + "]";

        Log.d(breakFrom, finalResult);
        Log.d("md5", AppUtils.md5(finalResult));

        md5String = md5(finalResult);

        return md5String;
    }

    /**
     * This method can be check internet connection is available or not.
     *
     * @param mActivity reference of activity.
     * @return
     */
    public static boolean isNetworkAvailable(@NonNull Context mActivity) {

        boolean available = false;
        /** Getting the system's connectivity service */
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        /** Getting active network interface to get the network's staffMobile */
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                available = true;
                AppUtils.print("====activeNetwork" + activeNetwork.getTypeName());
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                available = true;
                AppUtils.print("====activeNetwork" + activeNetwork.getTypeName());
            }
        } else {
            // not connected to the internet
            available = false;
            AppUtils.print("====not connected to the internet");
        }
        /** Returning the staffMobile of the network */
        return available;
    }

    public static Boolean checkServer() {
        return AppUrls.baseUrl.contains("Development");
    }

    public static String getRandomNumber() {

        long number = (long) Math.floor(Math.random() * AppUtils.getCurrentTimestamp()) + 1_000_000_000L;

        return String.valueOf(number);
    }


    public static int getStringToInt(String string) {

        int result = 0;

        if (!string.isEmpty())
            result = Integer.parseInt(string);

        return result;
    }


    public static float getStringToFloat(String string) {

        float result = 0;

        if (!string.isEmpty())
            result = Float.parseFloat(string);

        return result;
    }

    public static double getStringToDouble(String string) {

        double result = 0;

        if (!string.isEmpty())
            result = Double.parseDouble(string);

        return result;
    }

    public static long getDateToTimeStamp(String given_date) {

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = formatter.parse(given_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timestamp = date.getTime();

        return timestamp;

    }

    public static String getYearFromDate(String prev_date) {

        DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);

        return formattedDate;
    }

    public static String getMonthFromDate(String prev_date) {


        DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("MM");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);

        return formattedDate;
    }

    public static String getDayFromDate(String prev_date) {


        DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd");
        Date date = null;
        try {
            date = originalFormat.parse(prev_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);

        return formattedDate;
    }

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static String getFileType(String path) {
        String fileType = null;
        fileType = path.substring(path.indexOf('.', path.lastIndexOf('/')) + 1).toLowerCase();
        return fileType;
    }

    public static String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }

    public static File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "HathMe");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HathMe", "Oops! Failed create "
                        + "HathMe" + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    /* Creating file uri to store image/video*/
    public static Uri getOutputMediaFileUri(int type, Activity mActivity) {
        return FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".provider", AppUtils.getOutputMediaFile(type));
    }

    public static boolean checkAndRequestPermissions(Activity mActivity) {


        int openCamera = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA);
        int openMic = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO);
        int openGps = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION);
        int openGps1 = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionReadExternalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_MEDIA_IMAGES);
        else
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);


        int permissionWriteExtarnalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_MEDIA_AUDIO);
        else
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);



        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionWriteExtarnalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
            else listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        }

        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            else listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int permissionVideoStorage = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_MEDIA_VIDEO);
            if (permissionVideoStorage != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO);

            }

            int notificationPermission = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.POST_NOTIFICATIONS);

            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);

            }
        }

        if (openCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (openGps != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (openGps1 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (openMic != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    1);
            return false;
        }
        return true;
    }

/*
    public static void dialogFullImage(Activity mActivity, String header, ImageView imageView) {

        final Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_iv);

        ImageView ivFullImageDl = dialog.findViewById(R.id.ivFullImage);
        ImageView ivBack = dialog.findViewById(R.id.ivBack);
        TextView tvHeader = dialog.findViewById(R.id.tvHeader);

        ivFullImageDl.setImageDrawable(imageView.getDrawable());
        tvHeader.setText(header);

        ivFullImageDl.setVisibility(View.VISIBLE);

        dialog.show();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

            }
        });

    }
*/

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static void showDateDialog(final EditText editText, Activity mActivity) {

        int mYear, mMonth, mDay;

        // Get Current Date
        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("SetTextI18n") DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity,
                (view, year, monthOfYear, dayOfMonth) -> {

                    String month = "";


                    month = String.valueOf(monthOfYear + 1);

                    String day = String.valueOf(dayOfMonth);

                    if (monthOfYear + 1 < 10)
                        month = "0" + month;

                    if (dayOfMonth < 10)
                        day = "0" + day;


                    editText.setText(day + "-" + month + "-" + year);

                },
                mYear, mMonth, mDay);


        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        c.add(Calendar.YEAR, -60);
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        datePickerDialog.show();
    }

    public static void disableEnableView(final View view) {

        view.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                view.setEnabled(true);

            }
        }, 500);

    }

    public static void loadPicassoImage(String url, ImageView imageView) {

        if (!url.trim().isEmpty())
            Picasso.get().load(url.trim()).memoryPolicy(MemoryPolicy.NO_CACHE).into(imageView);
    }

    public static boolean checkPermissions(Activity mActivity) {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            return false;
        }

        return true;
    }

    public static void makeCall(Activity mActivity, String number) {

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        mActivity.startActivity(intent);

    }

    public static String getUniqueId() {

        UUID uuid = UUID.randomUUID();

        return String.valueOf(uuid);

    }

    public static String ifEmptyReturn0(String s) {

        return s.isEmpty() ? "0" : s;

    }

    public static String ifMoreThan1ReturnWithS(String no, String s) {

        if (no.equals("0") || no.equals("1") || no.isEmpty())
            return s;
        else return s + "s";

    }

    public static String ifEmptyReturnNa(String s) {

        return s.isEmpty() ? "N/A" : s;

    }

    public static double returnDouble(String s) {

        Log.v("lqjshlqhs", s);

        if (s.isEmpty())
            return 0;
        else {
            double amount = 0;
            try {
                amount = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return amount;
        }
    }

    public static long returnLong(String s) {

        if (s.isEmpty())
            return 0;
        else {
            long amount = 0;
            try {
                amount = Long.parseLong(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return amount;
        }
    }


    public static String if0ReturnNa(String s) {
        return s.equals("0") ? "N/A" : s;
    }

    public static boolean isPackageInstalled(String packageName, Activity mActivity) {
        try {

            PackageManager pm = mActivity.getPackageManager();

            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static long getDiffBetweenTime(String stDnT, String endSnT) {

        Log.v("lqjkshjklq", stDnT);
        //endSnT = endSnT + ", 00:00:00 AM";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd, HH:mm:ss");
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(stDnT);
            Log.i("startDate", startDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date endDate = null;
        try {
            endDate = simpleDateFormat.parse(endSnT);
            Log.i("endDate", endDate.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long difference = endDate.getTime() - startDate.getTime();
        Log.i("log_tag", "difference: " + difference);


        return difference;
    }

    public static String getCurrentDateTimeSeconds() {

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd, HH:mm:ss");
        String formattedDate = df.format(c);

        return formattedDate;
    }

    public static void showMessageDialog(Activity mActivity, String title, String message, int from) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvTitle.setText(title);
        tvMessage.setText(message);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

            if (from == 1) {
                mActivity.onBackPressed();
            } else if (from == 3) {
                mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                mActivity.finishAffinity();
            }
        });

    }

    public static void performLogout(BaseActivity mActivity) {
        if (AppUtils.isNetworkAvailable(mActivity))
            hitLogoutApi(mActivity);
        else
            AppUtils.showToastSort(mActivity, mActivity.getString(R.string.noInternetConnection));

    }

    private static void hitLogoutApi(final BaseActivity mActivity) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("deviceId", AppUtils.getDeviceID(mActivity));

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        WebServices.postApi(mActivity, AppUrls.logout, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject jsonObject) {

                SendBirdCall.unregisterPushToken(AppSettings.getString(AppSettings.fcmToken), e -> {
                    if (e != null) {
                        Log.i(MyApplication.TAG, "[AuthenticationUtils] unregisterPushToken() => Failed (e: " + e.getMessage() + ")");

                    }

                    SendBirdCall.deauthenticate((SendBirdException ex) -> {
                        if (ex != null) {
                            Log.i(MyApplication.TAG, "[AuthenticationUtils] deauthenticate() => Failed (e: " + ex.getMessage() + ")");
//                            showToastErrorMessage(context, e);
                        } else {
                            Log.i(MyApplication.TAG, "[AuthenticationUtils] deauthenticate() => OK");
                        }
                    });

                });

                AppSettings.clearSharedPreference();
                mActivity.startActivity(new Intent(mActivity, LoginTypeActivity.class));
                mActivity.finishAffinity();

            }

            @Override
            public void OnFail(String responce) {

            }
        });
    }

    public static boolean ifJsonHasEmpty(JSONObject jsonObject, String key) {

        if (jsonObject.has(key)) {

            try {
                return jsonObject.getString(key).isEmpty();

            } catch (JSONException e) {
                e.printStackTrace();
                return true;
            }

        } else
            return true;

    }

    public static String roundOff4Digit(String value) {

        if (value.contains(".")) {
            String valueAfter = value.split("\\.")[1];

            if (valueAfter.length() <= 4) {
                return value;
            }

            double i2 = 0;
            try {
                i2 = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            String result = new DecimalFormat("##.######").format(i2);

            return result;
        } else {
            return value;
        }

    }

    public static String roundOff2Digit(String value) {

        if (value.contains(".")) {
            String valueAfter = value.split("\\.")[1];

            if (valueAfter.length() <= 2) {
                return value;
            }

            double i2 = 0;
            try {
                i2 = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            String result = new DecimalFormat("##.##").format(i2);

            return result;
        } else {
            return value;
        }

    }


    public static String calculateWalletBalance(JSONObject jsonObject) {

        double creditBalance = 0, finalWalletBalance = 0;

        BigDecimal walletBalance;

        try {

            walletBalance = BigDecimal.valueOf(AppUtils.returnDouble(jsonObject.getString("walletBalance")));
            // creditBalance = returnDouble(jsonObject.getString("creditBalance"));

            return String.valueOf(walletBalance);

        } catch (JSONException e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static String getFormattedNumber(Activity activity, String number) {

        if (number.startsWith(activity.getString(R.string.rupeeSymbol)))
            number = number.replace(activity.getString(R.string.rupeeSymbol), "");

        BigDecimal bigDecimal = new BigDecimal(number);

        return NumberFormat.getNumberInstance(Locale.US).format(bigDecimal);
    }

    public static String roundOff2DigitString(String value) {

        double d = 0;

        if (!value.isEmpty()) {

            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return String.format(Locale.ENGLISH, "%.4f", d);
    }

    public static boolean panCardCheck(String panCard) {

        Pattern pattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}");

        Matcher matcher = pattern.matcher(panCard);
        // Check if pattern matches
        if (matcher.matches()) {
            Log.i("Matching", "Yes");
            return true;
        }

        return false;
    }


    private static void showAppMaintenanceDialog(BaseActivity mActivity) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        ImageView ivClose = bottomSheetDialog.findViewById(R.id.ivClose);
        ivClose.setVisibility(View.GONE);

        tvTitle.setText(mActivity.getString(R.string.app_name));
        tvMessage.setText(mActivity.getString(R.string.appUnderMaintenance));

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();
            mActivity.finishAffinity();

        });

    }

    public static void printLog(String key, String value) {

        if (BuildConfig.DEBUG) {
            Log.v(key, value);
        }
    }


    public static float returnFloat(String s) {

        if (s.isEmpty())
            return 0;
        else {
            float amount = 0;
            try {
                amount = Float.parseFloat(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return amount;
        }
    }

    public static int returnInt(String s) {

        if (s.isEmpty())
            return 0;
        else {
            int amount = 0;
            try {
                amount = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return amount;
        }
    }

    public static String random9DigitNumber() {

        long timeSeed = System.nanoTime(); // to get the current date time value

        double randSeed = Math.random() * 1000; // random number generation

        long midSeed = (long) (timeSeed * randSeed * returnLong(AppSettings.getString(AppSettings.userId))); // mixing up the time and

        String s = midSeed + "";

        return s.substring(0, 9);

    }

    public static void checkAppUpdate(BaseActivity mActivity) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("appVersion", BuildConfig.VERSION_NAME);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.settings, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            private void parseJson(JSONObject response) {

                try {
                    JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

                    if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");

                        if (jsonObject1.getString("appMaintenance").equals("1")) {

                            showAppMaintenanceDialog(mActivity);

                        }

                        AppSettings.putString(AppSettings.depositOnHold, jsonObject1.getString("depositOnHold"));
                        AppSettings.putString(AppSettings.upiDepositOnHold, jsonObject1.getString("upiOnHold"));
                        AppSettings.putString(AppSettings.bankDepositOnHold, jsonObject1.getString("bankTransferOnHold"));
                        AppSettings.putString(AppSettings.onlineDepositOnHold, jsonObject1.getString("onlineOnHold"));
                        AppSettings.putString(AppSettings.depositUPI, jsonObject1.getString("depositUPI"));
                        AppSettings.putString(AppSettings.depositQRCode, jsonObject1.getString("depositQRCode"));
                        AppSettings.putString(AppSettings.isVTube, jsonObject1.getString("isVTube"));

                        AppSettings.putString(AppSettings.depositAccountHolderName, jsonObject1.getString("depositAccountHolderName"));
                        AppSettings.putString(AppSettings.depositBankName, jsonObject1.getString("depositBankName"));
                        AppSettings.putString(AppSettings.depositBankAccountNumber, jsonObject1.getString("depositBankAccountNumber"));
                        AppSettings.putString(AppSettings.depositBankIfscCode, jsonObject1.getString("depositBankIfscCode"));

                        // AppSettings.putString(AppSettings.depositBankType, jsonObject1.getString("bankType"));

                        String currentVersion = BuildConfig.VERSION_NAME;

                        if (!currentVersion.equals(jsonObject1.getString("androidVersion")) && jsonObject1.getString("showPopupAndroid").equals("1")) {
                            showUpdatePopup(jsonObject1.getString("androidHardReset"));
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void showUpdatePopup(String type) {

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
                bottomSheetDialog.setContentView(R.layout.dialog_app_update);
                bottomSheetDialog.setCancelable(false);
                bottomSheetDialog.show();

                TextView tvUpdate, tvCancel;

                tvUpdate = bottomSheetDialog.findViewById(R.id.tvUpdate);
                tvCancel = bottomSheetDialog.findViewById(R.id.tvCancel);


                tvUpdate.setOnClickListener(v -> {

                    bottomSheetDialog.dismiss();
                    final String appPackageName = mActivity.getPackageName(); // package name of the app
                    try {
                        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }

                });

                tvCancel.setOnClickListener(v -> {
                    bottomSheetDialog.dismiss();
                    if (type.equals("1")) {
                        mActivity.finishAffinity();
                    }

                });

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    public static void openChromeCustomTabUrl(String webUrl, Activity activity) {
        try {
            if (isPackageInstalled("com.android.chrome", activity)) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                int coolorInt = Color.parseColor("#0A1D37");
                builder.setToolbarColor(coolorInt);
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage("com.android.chrome");
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(activity, Uri.parse(webUrl));
            } else {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                int coolorInt = Color.parseColor("#0A1D37");
                builder.setToolbarColor(coolorInt);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(activity, Uri.parse(webUrl));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static String getTimeString(long periodMs) {
        final String result;
        int totalSec = (int) (periodMs / 1000);
        int hour = 0, min, sec;

        if (totalSec >= 3600) {
            hour = totalSec / 3600;
            totalSec = totalSec % 3600;
        }

        min = totalSec / 60;
        sec = totalSec % 60;

        if (hour > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);
        } else if (min > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d", min, sec);
        } else {
            result = String.format(Locale.getDefault(), "0:%02d", sec);
        }
        return result;
    }
    public static void disableDoubleClick(Activity activity, Button button) {

        button.setEnabled(false);

        Timer buttonTimer = new Timer();
        buttonTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                activity.runOnUiThread(() -> button.setEnabled(true));
            }
        }, 500); // delay button enable for 0.5 sec

    }
    public static String Double1Digits(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(number);
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return unchanged string if input is null or empty
        }

        // Convert the first character to uppercase and concatenate the rest of the string
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static class SpecialCharacterFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            String regex = "^[a-zA-Z0-9]+$";
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);

                // Check if the character is a special character
                if (!Character.isLetterOrDigit(character) && !Character.isSpaceChar(character)) {
                    // If it's a special character, prevent it from being entered
                    return "";
                }
            }

            // If no special characters found, allow the input
            return null;
        }
    }

}
