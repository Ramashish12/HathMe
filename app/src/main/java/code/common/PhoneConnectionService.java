package code.common;

import static android.telecom.TelecomManager.PRESENTATION_ALLOWED;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Created by Sebastian Schmid on 2019-10-22.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PhoneConnectionService extends ConnectionService {


    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {

        return createCall(request.getAddress());
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {

        return createCall(request.getAddress());
    }

    @NonNull
    private CallConnection createCall(Uri phoneNumber) {

        final CallConnection conn = new CallConnection();

        Bundle extras = new Bundle();

        conn.setAddress(phoneNumber, PRESENTATION_ALLOWED);
        conn.setVideoState(VideoProfile.STATE_AUDIO_ONLY);
        conn.setExtras(extras);
        conn.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_DEFLECT);
        conn.setRingbackRequested(true);

        conn.setActive();

        return conn;
    }

}
