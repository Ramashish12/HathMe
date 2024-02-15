package code.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.BuildConfig;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.call.CallHistoryActivity;
import code.chat.ChatListActivity;
import code.common.MyApplication;
import code.common.SingleShotLocationProvider;
import code.groupvoicecall.main.GroupVoiceCallActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.GlobalData;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.vediolist.VideoListsActivity;
import code.view.BaseActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    ActivityMainBinding b;
    AdapterCategory adapterCategory;
    AdapterStore adapterStore;
    ArrayList<HashMap<String, String>> arrayListCategory = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListStore = new ArrayList<>();
    int totalQuantity = 0;
    String gpValue = "";
    ArrayList<HashMap<String, String>> countArrayList = new ArrayList<>();
    final int PERMISSION_REQUEST_CODE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        GlobalData.userId = AppSettings.getString(AppSettings.userId);

        b.main.rvCategories.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        adapterCategory = new AdapterCategory(arrayListCategory);
        b.main.rvCategories.setAdapter(adapterCategory);

        b.main.rvStore.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));

        adapterStore = new AdapterStore(arrayListStore);
        b.main.rvStore.setAdapter(adapterStore);

        b.main.ivMenu.setOnClickListener(this);
        b.main.ivCart.setOnClickListener(this);
        b.main.tvViewCategory.setOnClickListener(this);
        b.main.llRateUser.setOnClickListener(this);
        b.main.llAddMoney.setOnClickListener(this);
        b.main.lGreenPoints.setOnClickListener(this);
        b.main.llCall.setOnClickListener(this);
        b.main.llChat.setOnClickListener(this);
        b.main.llVTube.setOnClickListener(this);
        b.main.llFavorites.setOnClickListener(this);
        b.main.ivSearchProduct.setOnClickListener(this);

        b.ivLogout.setOnClickListener(this);
        b.rlProfile.setOnClickListener(this);
        b.rlOrders.setOnClickListener(this);
        b.rlPaymentSettings.setOnClickListener(this);
        b.rlHelpSupport.setOnClickListener(this);
        b.rlCashbackOffers.setOnClickListener(this);
        b.main.ivWallet.setOnClickListener(this);
        b.tvdummy.setOnClickListener(this);
        b.tvlivedummy.setOnClickListener(this);
        b.tvgroupchat.setOnClickListener(this);
        b.main.tvGp.setOnClickListener(this);
        b.main.tvRp.setOnClickListener(this);
        MyApplication.getInstance().initSendBirdCall();
        if (Build.VERSION.SDK_INT > 33) {
            if (!shouldShowRequestPermissionRationale("112")) {
                getNotificationPermission();
            }
        }

        AppUtils.checkAndRequestPermissions(mActivity);
        MyApplication.getInstance().setUpVoip();
        //  CallService.startService(mActivity);
        MyApplication.getInstance().sendbirdChatInit();

    }

    private void showGPSEnableDialog() {

        // You can also open the GPS settings screen
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private String getConnectionServiceId() {

        return mActivity.getPackageName() + ".connectionService";
    }

    private void hitGetCategoryApi() {

        WebServices.getApi(mActivity, AppUrls.category, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCategoryJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseCategoryJson(JSONObject response) {

        arrayListCategory.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonCategory = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonCategory.getString("_id"));
                    hashMap.put("image", jsonCategory.getString("image"));
                    hashMap.put("name", jsonCategory.getString("name"));

                    arrayListCategory.add(hashMap);

                    if (i == 4) break;
                }

                hitGetCartApi();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterCategory.notifyDataSetChanged();


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivMenu:

                openDrawer();

                break;
            case R.id.tvGp:

                popupMessage(getString(R.string.gpMsg), getString(R.string.gpCoin));

                break;
            case R.id.tvRp:

                popupMessage(getString(R.string.rpMsg), getString(R.string.rpCoin));

                break;
            case R.id.tvViewCategory:

                startActivity(new Intent(mActivity, CategoriesActivity.class));

                break;

            case R.id.ivCart:

                startActivity(new Intent(mActivity, CartActivity.class).putExtra("isFromCart", "1"));

                break;

            case R.id.tvdummy:

                closeDrawer();
                //startActivity(new Intent(mActivity, SortVideoActivity.class));

                break;
            case R.id.ivLogout:

                closeDrawer();
                showLogoutAlert();

                break;
            case R.id.tvlivedummy:
                closeDrawer();
                //startActivity(new Intent(mActivity, GroupChatActivity.class));
                break;
            case R.id.tvgroupchat:
                closeDrawer();
                startActivity(new Intent(mActivity, GroupVoiceCallActivity.class));
                break;
            case R.id.rlProfile:

                closeDrawer();
                startActivity(new Intent(mActivity, ProfileSettingsActivity.class));

                break;

            case R.id.llRateUser:

                closeDrawer();
                startActivity(new Intent(mActivity, FriendListActivity.class));

                break;

            case R.id.rlOrders:

                closeDrawer();
                startActivity(new Intent(mActivity, OrderTransactionActivity.class));

                break;

            case R.id.ivWallet:
            case R.id.llAddMoney:

                closeDrawer();
                startActivity(new Intent(mActivity, WalletActivity.class));

                break;

            case R.id.rlPaymentSettings:

                closeDrawer();
                startActivity(new Intent(mActivity, BankListActivity.class));

                break;
            case R.id.ivSearchProduct:

                closeDrawer();
                startActivity(new Intent(mActivity, SearchAllProductActivity.class));
                break;

            case R.id.lGreenPoints:

//                startActivity(new Intent(mActivity, SellActivity.class).putExtra("gpValue", gpValue));

                break;

            case R.id.llCall:
                //    AppUtils.showInProgressToast(mActivity);

                startActivity(new Intent(mActivity, CallHistoryActivity.class));

                break;

            case R.id.llChat:

                startActivity(new Intent(mActivity, ChatListActivity.class));

                break;
            case R.id.llVTube:

                if (AppSettings.getString(AppSettings.isVTube).equals("1") || BuildConfig.DEBUG) {
                    startActivity(new Intent(mActivity, VideoListsActivity.class));
                } else {
                    AppUtils.showToastSort(mActivity, getString(R.string.currentlyOnHold));
                }

                break;

            case R.id.rlHelpSupport:

                closeDrawer();
                startActivity(new Intent(mActivity, HelpSupportActivity.class));

                break;

            case R.id.llFavorites:

                closeDrawer();
                startActivity(new Intent(mActivity, FavoriteActivity.class));

                break;

            case R.id.rlCashbackOffers:

                closeDrawer();
                startActivity(new Intent(mActivity, RewardsActivity.class));

                break;
        }

    }

    private void showLogoutAlert() {

        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
        alertDialog.setTitle(getString(R.string.logout));
        alertDialog.setMessage(getString(R.string.areYouSureLogout));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            AppUtils.performLogout(mActivity);
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }


    private class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterCategory(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterCategory.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_category_2, viewGroup, false);
            return new AdapterCategory.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterCategory.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvName.setText(data.get(position).get("name"));

            AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(mActivity, StoreListActivity.class).putExtra("categoryId", data.get(position).get("_id")));

                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout llMain;

            TextView tvName;

            ImageView ivImage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                llMain = itemView.findViewById(R.id.llMain);

                tvName = itemView.findViewById(R.id.tvName);

                ivImage = itemView.findViewById(R.id.ivImage);

            }
        }
    }

    private void openDrawer() {

        if (!b.drawerLayout.isDrawerOpen(b.scrollView)) b.drawerLayout.openDrawer(b.scrollView);
    }

    //CLOSE DRAWER MAIN
    public void closeDrawer() {

        if (b.drawerLayout.isDrawerOpen(b.scrollView)) {
            b.drawerLayout.closeDrawer(b.scrollView);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {


        super.onResume();
        if (!isGPSEnabled()) {
            turnOnGps();
        } else {
            getCurrentLocation();
        }

        setUserData();
        hitGetProfileData();

        hitGetCategoryApi();
        hitGetAddressListApi();
        hitGetWalletDataApi();

    }

    private void setUserData() {
        b.tvName.setText(AppSettings.getString(AppSettings.name));
        b.tvEmail.setText(AppSettings.getString(AppSettings.email));
        b.tvMobile.setText(AppSettings.getString(AppSettings.mobile));
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.profileImage), b.ivProfile);
        AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.userQr), b.ivUserQr);
    }

    private void hitGetProfileData() {

        WebServices.getApi(mActivity, AppUrls.myProfile, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseMyProfile(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseMyProfile(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                jsonObject = jsonObject.getJSONObject("data");

                b.main.tvGp.setText(AppUtils.roundOff2Digit(jsonObject.getString("greenPoints")));
                AppSettings.putString(AppSettings.gpValue, AppUtils.roundOff2Digit(jsonObject.getString("greenPoints")));
                gpValue = b.main.tvGp.getText().toString();
                b.main.tvRp.setText(AppUtils.roundOff2Digit(jsonObject.getString("redPoints")));

                AppSettings.putString(AppSettings.name, jsonObject.getString("name"));
                AppSettings.putString(AppSettings.mobile, jsonObject.getString("mobile"));
                AppSettings.putString(AppSettings.email, jsonObject.getString("email"));
                AppSettings.putString(AppSettings.profileImage, jsonObject.getString("profileImage"));
                AppSettings.putString(AppSettings.uniqueId, jsonObject.getString("uniqueID"));
                AppSettings.putString(AppSettings.userQr, jsonObject.getString("qrUrl"));
                AppSettings.putString(AppSettings.chatId, jsonObject.getString("userChatId"));
                AppSettings.putString(AppSettings.isProfileVerified, jsonObject.getString("isProfileVerified"));
                AppSettings.putString(AppSettings.isWithdrawPinCreated, jsonObject.getString("withdrawalPin"));

                setUserData();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //hitGetCartApi();
        hitGetCategoryApi();
    }

    private void getCurrentLocation() {

        SingleShotLocationProvider.requestSingleUpdate(mActivity, (SingleShotLocationProvider.GPSCoordinates location) -> {

            hitGetCartApi();
            hitNearByStoreApi(location.latitude, location.longitude);

        });
    }

    private void hitNearByStoreApi(float latitude, float longitude) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("latitude", String.valueOf(latitude));
            jsonObject.put("longitude", String.valueOf(longitude));
            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.getNearbyMerchants, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseNearByMerchant(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseNearByMerchant(JSONObject response) {
        arrayListStore.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("merchantId", jsonObject1.getString("merchantId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("profileImageMerchant", jsonObject1.getString("profileImage"));
                    hashMap.put("rating", jsonObject1.getString("rating"));

                    arrayListStore.add(hashMap);

                }

            } /*else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.noService),4);
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterStore.notifyDataSetChanged();

    }

    private class AdapterStore extends RecyclerView.Adapter<AdapterStore.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterStore(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterStore.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_store, viewGroup, false);
            return new AdapterStore.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterStore.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.setIsRecyclable(false);
            holder.tvStoreName.setText(data.get(position).get("name"));
            holder.tvRating.setText(AppUtils.roundOff2Digit(data.get(position).get("rating")));
            AppUtils.loadPicassoImage(data.get(position).get("profileImageMerchant"), holder.ivImageStore);

            holder.cvMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(mActivity, StoreActivity.class).putExtra("merchantId", data.get(position).get("merchantId")));
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            CardView cvMain;

            ImageView ivImageStore;

            TextView tvStoreName, tvRating, tvDistance;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                cvMain = itemView.findViewById(R.id.cvMain);

                ivImageStore = itemView.findViewById(R.id.ivImage);
                tvStoreName = itemView.findViewById(R.id.tvStoreName);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvDistance = itemView.findViewById(R.id.tvDistance);
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void turnOnGps() {

        LocationRequest locationRequest = LocationRequest.create().setInterval(1000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build()).addOnSuccessListener(this, (LocationSettingsResponse response) -> {
            // startUpdatingLocation(...);
            getCurrentLocation();
        }).addOnFailureListener(this, ex -> {
            if (ex instanceof ResolvableApiException) {
                // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) ex;
                    resolvable.startResolutionForResult(mActivity, 2);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void loginForChat() {

       /* SendbirdChat.connect(AppSettings.getString(AppSettings.userId), new ConnectHandler() {
            @Override
            public void onConnected(@Nullable User user, @Nullable SendbirdException e) {

                Log.v("ksjbqjb1", String.valueOf(e.getCode()));
                Log.v("ksjbqjb2", e.getLocalizedMessage().toString());
            }
        });*/

    }

    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 33) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //deny

                    showMessageDialog(mActivity, "Notification Permission", "Give permission manually by settings");
                }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the user has granted the permission after returning from the settings
            // Perform any necessary actions here
        }
    }


    private void showMessageDialog(Activity mActivity, String title, String message) {

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
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        });

    }

    private void hitGetCartApi() {

        WebServices.getApi(mActivity, AppUrls.getProductFromCart, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetCart(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseGetCart(JSONObject response) {

        countArrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                JSONArray jsonArray = jsonData.getJSONArray("items");
                totalQuantity = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    String productQuantityStr = json.getString("quantity");
                    int productQuantity = Integer.parseInt(productQuantityStr);
                    totalQuantity += productQuantity;

                    countArrayList.add(hashMap);
                }
                b.main.tvCartQty.setText("" + totalQuantity);


            } else {
                b.main.tvCartQty.setText("0");
//                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
//                        jsonObject.getString(AppConstants.resMsg), 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void popupMessage(String msg, String title) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setIcon(R.drawable.ic_info);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setNegativeButton("ok", null);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void hitGetAddressListApi() {

        WebServices.getApi(mActivity, AppUrls.GetAddress, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAddressList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddressList(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

            } else {
                showSelectAddressDialog();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showSelectAddressDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_select_address);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();

        bottomSheetDialog.findViewById(R.id.tvAddAddress).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(mActivity, AddressListActivity.class));
        });

    }

    private void hitGetWalletDataApi() {

        WebServices.getApi(mActivity, AppUrls.transactionHistory, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseGetDetail(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppSettings.putString(AppSettings.walletBalance, jsonObject.getJSONObject("data").getString("totalWalletBalance"));

            } /*else
                AppUtils.showMessageDialog(mActivity, getString(R.string.wallet),
                        jsonObject.getString(AppConstants.resMsg), 2);*/

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}