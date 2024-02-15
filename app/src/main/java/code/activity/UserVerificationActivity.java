package code.activity;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static code.utils.AppUtils.panCardCheck;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.BuildConfig;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityUserVerificationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.regex.Pattern;

import code.common.VerhoeffAlgorithm;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class UserVerificationActivity extends BaseActivity implements View.OnClickListener {

    ActivityUserVerificationBinding b;

    private String encodedPan = "", encodedAadharFront = "", encodedAadharBack = "", picturePath = "";

    private static final int selectPicture = 1, capturePicture = 100;

    private Uri fileUri;

    private int from = 0;

    boolean isPanUploaded = false, isAadharFrontUploaded = false, isAadharBackUploaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUserVerificationBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.userVerification));
        b.rlPanCard.setOnClickListener(this);
        b.rlAadharFront.setOnClickListener(this);
        b.rlAadharBack.setOnClickListener(this);

        b.etDob.setOnClickListener(this);

        b.tvSave.setOnClickListener(this);

        AppUtils.checkAndRequestPermissions(mActivity);

        hitGetUserDetailApi();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlPanCard:

                from = 1;

                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    AlertCameraGallery();
                } else {
                    AlertCameraGallery();
                  //  AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
                }

                break;

            case R.id.rlAadharFront:

                from = 2;

                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    AlertCameraGallery();
                } else {
                    AlertCameraGallery();
                   // AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
                }

                break;

            case R.id.rlAadharBack:

                from = 3;

                if (AppUtils.checkAndRequestPermissions(mActivity)) {
                    AlertCameraGallery();
                } else {
                    AlertCameraGallery();
                    //AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
                }

                break;

            case R.id.etDob:

                AppUtils.showDateDialog(b.etDob, mActivity);

                break;

            case R.id.tvSave:

                validate();

                break;

        }

    }

    private void validate() {

        if (b.etFullName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterFullName));
        } else if (b.etDob.getText().toString().trim().isEmpty()) {

            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterDob));
        } else if (b.etPanCard.getText().toString().isEmpty()) {
            b.etPanCard.requestFocus();
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterPan));
        } else if (!panCardCheck(b.etPanCard.getText().toString())) {

            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectPan));
        } else if (!isPanUploaded) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseUploadPanCard));
        } else if (b.etAadharCard.getText().toString().isEmpty()) {

            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterAadhar));
        }
//        else if (!validateAadharNumber(b.etAadharCard.getText().toString())) {
//            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterProperAadhar));
//        }
        else if (!isAadharFrontUploaded) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseUploadFrontAadhar));
        } else if (!isAadharBackUploaded) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseUploadBackAadhar));
        } else {
            hitUserVerificationApi();
        }
    }

    private void hitUserVerificationApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("fullName", b.etFullName.getText().toString().trim());
            jsonObject.put("dateOfBirth", b.etDob.getText().toString().trim());
            jsonObject.put("panCardNumber", b.etPanCard.getText().toString().trim());
            jsonObject.put("aadharCardNumber", b.etAadharCard.getText().toString().trim());
            jsonObject.put("panCardPicture", encodedPan);
            jsonObject.put("aadharCardFrontPicture", encodedAadharFront);
            jsonObject.put("aadharCardBackPicture", encodedAadharBack);
            jsonObject.put("dlNumber", "");

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.userVerify, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.userVerification),
                        jsonObject.getString(AppConstants.resMsg), 1);

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.userVerification),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void hitGetUserDetailApi() {

        WebServices.getApi(mActivity, AppUrls.myProfile, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseUserDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }


    private void parseUserDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");
                if (!jsonData.getString("panCardPicture").isEmpty()) {
                    AppUtils.loadPicassoImage(jsonData.getString("panCardPicture"), b.ivPanCard);
                    isPanUploaded = true;
                }

                if (!jsonData.getString("aadharCardBackPicture").isEmpty()) {
                    AppUtils.loadPicassoImage(jsonData.getString("aadharCardBackPicture"), b.ivAadharCardBack);
                    isAadharBackUploaded = true;
                }
                if (!jsonData.getString("aadharCardFrontPicture").isEmpty()) {
                    AppUtils.loadPicassoImage(jsonData.getString("aadharCardFrontPicture"), b.ivAadharCardFront);
                    isAadharFrontUploaded = true;
                }

                b.etPanCard.setText(jsonData.getString("panCardNumber"));
                b.etAadharCard.setText(jsonData.getString("aadharCardNumber"));
                b.etDob.setText(jsonData.getString("dateOfBirth"));
                b.etFullName.setText(jsonData.getString("name"));

                b.tvSave.setVisibility(View.VISIBLE);
                switch (jsonData.getString("isProfileVerified")) {

                    case "1":
                        b.tvStatus.setText(getString(R.string.approved));
                        b.tvStatus.setTextColor(getResources().getColor(R.color.green));
                        b.tvSave.setVisibility(View.GONE);

                        break;
                    case "0":
                        b.tvStatus.setText(getString(R.string.underProcess));
                        b.tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                        b.tvSave.setVisibility(View.GONE);

                        break;
                    case "2":
                        b.tvStatus.setText(getString(R.string.rejected));
                        b.tvStatus.setTextColor(getResources().getColor(R.color.red));
                        b.tvSave.setVisibility(View.VISIBLE);

                        break;}


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.userVerification),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void AlertCameraGallery() {

        final BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_camera_gallery);

        dialog.setCancelable(true);

        dialog.show();

        RelativeLayout rlCancel = dialog.findViewById(R.id.rlCancel);
        LinearLayout llCamera = dialog.findViewById(R.id.llCamera);
        LinearLayout llGallery = dialog.findViewById(R.id.llGallery);

        rlCancel.setOnClickListener(v -> dialog.dismiss());

        llCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                captureImage();
                dialog.dismiss();
            }
        });

        llGallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //photoPickerIntent.putExtra("crop", "true");
            startActivityForResult(photoPickerIntent, selectPicture);

            dialog.dismiss();
        });


    }

    private void captureImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID + ".provider", AppUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE));
            AppSettings.putString(AppSettings.imagePath, String.valueOf(fileUri));
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(it, capturePicture);
        } else {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = AppUtils.getOutputMediaFileUri(MEDIA_TYPE_IMAGE, mActivity); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
            // start the image capture Intent
            startActivityForResult(intent, capturePicture);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;
        Bitmap rotatedBitmap;
        if (requestCode == capturePicture) {
            if (resultCode == RESULT_OK) {

                if (fileUri == null) {

                    fileUri = Uri.parse(AppSettings.getString(AppSettings.imagePath));
                    picturePath = fileUri.getPath();

                } else {
                    if (!fileUri.equals(""))
                        picturePath = fileUri.getPath();
                }

                String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);

                String selectedImagePath = picturePath;

                String ext = "jpg";

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 500;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

                Matrix matrix = new Matrix();
                matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                byte[] ba = bao.toByteArray();

                if (from == 1) {
                    encodedPan = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                    b.ivPanCard.setImageBitmap(rotatedBitmap);
                    isPanUploaded = true;
                } else if (from == 2) {
                    encodedAadharFront = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                    b.ivAadharCardFront.setImageBitmap(rotatedBitmap);
                    isAadharFrontUploaded = true;
                } else if (from == 3) {
                    encodedAadharBack = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                    b.ivAadharCardBack.setImageBitmap(rotatedBitmap);
                    isAadharBackUploaded = true;
                }

            }
        } else if (requestCode == selectPicture) {
            if (data != null) {

                try {
                    //get the Uri for the captured image
                    Uri picUri = data.getData();

                    Uri contentURI = data.getData();

                    if (contentURI.toString().contains("content://com.google.android.apps.photos")) {
                        bitmap = getBitmapFromUri(contentURI);
                    } else {

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = mActivity.getContentResolver().query(contentURI, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        picturePath = cursor.getString(columnIndex);
                        System.out.println("Image Path : " + picturePath);
                        cursor.close();
                        String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);

                        String ext = AppUtils.getFileType(picturePath);

                        String selectedImagePath = picturePath;

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(selectedImagePath, options);
                        final int REQUIRED_SIZE = 500;
                        int scale = 1;
                        while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                            scale *= 2;
                        options.inSampleSize = scale;
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                    }

                    Matrix matrix = new Matrix();
                    matrix.postRotate(AppUtils.getImageOrientation(picturePath));
                    rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
                    byte[] ba = bao.toByteArray();

                    if (from == 1) {
                        encodedPan = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivPanCard.setImageBitmap(rotatedBitmap);
                        isPanUploaded = true;
                    } else if (from == 2) {
                        encodedAadharFront = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivAadharCardFront.setImageBitmap(rotatedBitmap);
                        isAadharFrontUploaded = true;
                    } else if (from == 3) {
                        encodedAadharBack = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                        b.ivAadharCardBack.setImageBitmap(rotatedBitmap);
                        isAadharBackUploaded = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Toast.makeText(mActivity, "Unable to Select the Image", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public static boolean validateAadharNumber(String aadharNumber) {
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhar = aadharPattern.matcher(aadharNumber).matches();
        if (isValidAadhar) {
            isValidAadhar = VerhoeffAlgorithm.validateVerhoeff(aadharNumber);
        }
        return isValidAadhar;
    }
}