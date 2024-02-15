package code.common;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hathme.android.R;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.user.User;
import com.sendbird.calls.SendBirdCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import code.activity.MainActivity;
import code.utils.AppSettings;

public class FirebaseMessageService extends FirebaseMessagingService {
    private static final String TAG = "MyFMService";
    // String CHANNEL_ID = "com.hathmemerchant.cabs";
    String CHANNEL_ID = "com.hathme.android";
    private NotificationManager mManager;

    NotificationCompat.Builder notification;
    String message = "", title = "", push_tracking_id = "", user_id = "";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (SendBirdCall.handleFirebaseMessageData(remoteMessage.getData())) {
            Log.v("msg", remoteMessage.toString());
        } else {
            remoteMessage.getData();
            Map<String, String> params = remoteMessage.getData();
            JSONObject json = new JSONObject(params);
            Log.v("msg", json.toString());

            try {
                if (json.has("messages"))
                    message = json.getString("messages");
                if (json.has("title"))
                    title = json.getString("title");
                createNotification(message, title);
                Intent intent = new Intent("RefreshDetails");
                //intent.putExtra("message", message);
                sendBroadcast(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public void createNotification(String msg, String title) {

        Intent intent = null;
        intent = new Intent(this, MainActivity.class);


        PendingIntent contentIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            contentIntent = PendingIntent.getActivity(this,
                    0, intent,
                    PendingIntent.FLAG_IMMUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(this,
                    0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        playNotificationSound();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel androidChannel = new NotificationChannel(CHANNEL_ID,
                    title, NotificationManager.IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.RED);
            //androidChannel.setSound(null, null);

            // Sets whether notifications posted to this channel appear on the lockScreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(androidChannel);

            @SuppressLint({"NewApi", "LocalSuppress"}) NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.mipmap.notification_logo)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setTicker(title)
                    .setSound(defaultUri)
                    .setContentIntent(contentIntent);

            int timestamp = 1000;

            getManager().notify(timestamp, notification.build());

        } else {
            try {
                @SuppressLint({"NewApi", "LocalSuppress"}) NotificationCompat.Builder notification = new NotificationCompat.Builder(this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.notification_logo))
                        .setSmallIcon(R.mipmap.notification_logo)
                        .setColor(ContextCompat.getColor(this,
                                R.color.colorPrimary))
                        .setContentTitle(title)
                        .setTicker(title)
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setLights(0xFF760193, 300, 1000)
                        .setAutoCancel(true).setVibrate(new long[]{200, 400});

                int timestamp = 1000;

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(timestamp/* ID of notification */, notification.build());

            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }


    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.ENGLISH);
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(this, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(MyApplication.TAG, "[MyFirebaseMessagingService] onNewToken(token: " + token + ")");

        if (SendBirdCall.getCurrentUser() != null) {
//              PushUtils.registerPushToken(getApplicationContext(), token, e -> {
//                    if (e != null) {
//                        Log.i(MyApplication.TAG, "[MyFirebaseMessagingService] registerPushTokenForCurrentUser() => e: " + e.getMessage());
//                    }
//                });
        } else {
            //PrefUtils.setPushToken(getApplicationContext(), token);
        }
    }

    private void loginForChat() {

        SendbirdChat.connect(AppSettings.getString(AppSettings.userId), new ConnectHandler() {
            @Override
            public void onConnected(@Nullable User user, @Nullable SendbirdException e) {


            }
        });

    }
}


