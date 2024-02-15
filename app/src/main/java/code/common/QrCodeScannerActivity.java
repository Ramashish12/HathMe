package code.common;

import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.Result;

import code.utils.AppConstants;
import code.view.BaseActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeScannerActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        // Do something with the result here
        // Prints scan results

        // Prints the scan format (qrcode, pdf417 etc.)

        //If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
        Intent intent = new Intent();
        intent.putExtra(AppConstants.KEY_QR_CODE, result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }
}