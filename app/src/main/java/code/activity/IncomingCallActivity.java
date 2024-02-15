package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityIncomingCallBinding;

public class IncomingCallActivity extends AppCompatActivity {

    ActivityIncomingCallBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


    }
}