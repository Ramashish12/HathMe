package code.basic;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.BuildConfig;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySignupBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import code.activity.MainActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    ActivitySignupBinding b;
    String encodedImage = "", picturePath = "";

    private static final int selectPicture = 1;
    private static final int capturePicture = 100;

    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {
        if (checkAndRequestPermissions()) {

        } else {
            checkAndRequestPermissions();
        }
        b.tvContinue.setOnClickListener(this);
        b.ivProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.tvContinue:

                validate();

                break;
            case R.id.ivProfile:

//                if (AppUtils.checkAndRequestPermissions(mActivity)) {
//                    AlertCameraGallery();
//                } else {
//                    AppUtils.showToastSort(mActivity, getString(R.string.provideRequiredPermission));
//                }
                AlertCameraGallery();
                break;

        }

    }

    private void validate() {

        if (b.etName.getText().toString().trim().isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterName));
        } else if (!AppUtils.isEmailValid(b.etEmail.getText().toString().trim())) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCorrectEmail));
        }
        else if (encodedImage.isEmpty()) {
            AppUtils.showToastSort(mActivity, getString(R.string.selectProfileImage));
        }
        else {
            hitSaveDataApi();
        }
    }

    private void hitSaveDataApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("email", b.etEmail.getText().toString().trim());
            jsonObject.put("name", b.etName.getText().toString().trim());
            if (!encodedImage.isEmpty())
                jsonObject.put("profileImage", encodedImage);
            else
                jsonObject.put("profileImage", "");
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.personalDetail, json, true, true, new WebServicesCallback() {
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

                AppSettings.putString(AppSettings.isProfileCompleted, "1");
                startActivity(new Intent(mActivity, MainActivity.class));
                finish();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                encodedImage = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                b.ivProfile.setImageBitmap(rotatedBitmap);

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

                    encodedImage = AppUtils.getEncoded64ImageStringFromBitmap(rotatedBitmap);
                    b.ivProfile.setImageBitmap(rotatedBitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                Toast.makeText(mActivity, "Unable to Select the Image", Toast.LENGTH_SHORT).show();

            }

        }
    }
    private boolean checkAndRequestPermissions() {


        int permissionCamera
                = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionReadExternalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        else
            permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        int permissionWriteExtarnalStorage;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO);
        else
            permissionWriteExtarnalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

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

            int permissionVideoStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO);
            if (permissionVideoStorage != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO);

            }

            int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);

            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {

                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);

            }

        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<>();
            // Initialize the map with both permissions


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.put(Manifest.permission.READ_MEDIA_IMAGES, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_MEDIA_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_MEDIA_VIDEO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.POST_NOTIFICATIONS, PackageManager.PERMISSION_GRANTED);


            } else {
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            }


            // Fill with actual results from user
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {


                    if (perms.get(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

                    ) {
                        //else any one or both the permissions are not granted
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)


                        ) {
                            showDialogOK("Necessary Permissions required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            // permissionSettingScreen ( );
                                            //  finish();
                                            break;
                                    }
                                }
                            });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            permissionSettingScreen();

                        }
                    }


                } else {


                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

                    ) {
                        //else any one or both the permissions are not granted
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showDialogOK("Necessary Permissions required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            Toast.makeText(mActivity, "Necessary Permissions required for this app", Toast.LENGTH_LONG).show();
                                            // permissionSettingScreen ( );
                                            //  finish();
                                            break;
                                    }
                                }
                            });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                         //   permissionSettingScreen();

                        }
                    }


                }


            }
        }

    }
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this).setMessage(message).setPositiveButton(getString(R.string.ok), okListener).setNegativeButton(getString(R.string.cancel), okListener).create().show();
    }
    private void permissionSettingScreen() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
        // finishAffinity();
        finish();

    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
