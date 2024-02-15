package code.common;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.hathme.android.R;
import com.sendbird.android.LogLevel;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.handler.ConnectHandler;
import com.sendbird.android.handler.InitResultHandler;
import com.sendbird.android.params.InitParams;
import com.sendbird.android.params.UserUpdateParams;
import com.sendbird.android.user.User;
import com.sendbird.calls.AuthenticateParams;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.handler.CompletionHandler;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.handler.SendBirdCallListener;

import java.util.UUID;

import code.call.CallService;
import code.utils.AppSettings;
import code.utils.BroadcastUtils;

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class.getSimpleName();
    public static boolean isAppOpen = false;
    private static MyApplication mainApplication;

    public static final String VERSION = "1.4.0";
    public static final String APP_ID = "07D50B3C-A234-4916-9808-62A9B1EAFECA";

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
        //  registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        /*Log.v("qlksjbqsn", String.valueOf(initSendBirdCall()));*/

        initSendBirdCall();
    }

    public boolean initSendBirdCall() {
        Log.i(MyApplication.TAG, "[BaseApplication] initSendBirdCall(appId: " + APP_ID + ")");
        Context context = getApplicationContext();


        if (SendBirdCall.init(context, APP_ID)) {
            SendBirdCall.removeAllListeners();
            SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
                @Override
                public void onRinging(DirectCall call) {
                    int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                    Log.i(TAG, "[BaseApplication] onRinging() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                    if (ongoingCallCount >= 2) {
                        call.end();
                        return;
                    }

                    call.setListener(new DirectCallListener() {
                        @Override
                        public void onConnected(DirectCall call) {
                        }

                        @Override
                        public void onRemoteAudioSettingsChanged(DirectCall call) {
                        }

                        @Override
                        public void onEstablished(DirectCall call) {
                        }

                        @Override
                        public void onEnded(DirectCall call) {

                            int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                            Log.i(TAG, "[BaseApplication] onEnded() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                            BroadcastUtils.sendCallLogBroadcast(context, call.getCallLog());

                            if (ongoingCallCount == 0) {
                                CallService.stopService(context);
                            }
                        }
                    });

                    CallService.onRinging(context, call);
                    //  call.accept(new AcceptParams());
                }
            });

            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.DIALING, R.raw.dialing);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RINGING, R.raw.ringing);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTING, R.raw.reconnecting);
            SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTED, R.raw.reconnected);
            return false;
        }
        return true;
    }

    public void setUpVoip() {
        AuthenticateParams params = new AuthenticateParams(AppSettings.getString(AppSettings.userId))
                .setAccessToken(AppSettings.getString(AppSettings.fcmToken));
        SendBirdCall.authenticate(params, (user, e) -> {
            if (e == null) {
                // The user has been authenticated successfully and is connected to Sendbird server.
                SendBirdCall.registerPushToken(AppSettings.getString(AppSettings.fcmToken),
                        false, new CompletionHandler() {
                            @Override
                            public void onResult(@Nullable SendBirdException e) {

                                if (e == null) {


                                }

                            }
                        });
            }
        });
//        Intent serviceIntent = new Intent(this, CallService.class);
//        startService(serviceIntent);
    }

    public void sendbirdChatInit() {
        InitParams initParams = new InitParams(APP_ID, this, false);
        initParams.setLogLevel(LogLevel.ERROR);

        SendbirdChat.init(initParams, new InitResultHandler() {
            @Override
            public void onMigrationStarted() {


            }

            @Override
            public void onInitFailed(@NonNull SendbirdException e) {

            }

            @Override
            public void onInitSucceed() {

                new Handler(Looper.myLooper()).postDelayed(() -> {

                    connectSendBirdChat();

                }, 1000);

            }
        });

    }

    public void connectSendBirdChat() {

        InitParams initParams = new InitParams(APP_ID, this, false);
        initParams.setLogLevel(LogLevel.ERROR);

        SendbirdChat.connect(AppSettings.getString(AppSettings.userId), new ConnectHandler() {
            @Override
            public void onConnected(@Nullable User user, @Nullable SendbirdException e) {
                String newNickname = AppSettings.getString(AppSettings.userName);
                String newProfileUrl = AppSettings.getString(AppSettings.profileImage);
//                SendBird.updateCurrentUserInfo(newNickname, newProfileUrl, e1 -> {
//                    if (e1 != null) {
//                        // Handle error
//                        e1.printStackTrace();
//                    } else {
//                        // User information updated successfully
//                    }
//                });

            }
        });

    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

    }

    public static synchronized MyApplication getInstance() {
        return mainApplication;
    }


}
