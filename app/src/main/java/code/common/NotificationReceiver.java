package code.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import code.activity.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle the notification click here
        // Open your application or specific activity
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(openAppIntent);
    }
}

