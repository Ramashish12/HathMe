package code.utils;

import android.content.Intent;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.hathme.android.BuildConfig;

import org.json.JSONObject;

import code.view.BaseActivity;

public class WebServices {

    public static void postApi(final BaseActivity mActivity,
                               String url,
                               JSONObject jsonObject,
                               final boolean loader,
                               boolean softKeyboard, final WebServicesCallback apiCallback) {

        if (AppUtils.isNetworkAvailable(mActivity)) {

            if (loader)
                AppUtils.showRequestDialog(mActivity);

            if (softKeyboard)
                AppUtils.hideSoftKeyboard(mActivity);

            AppUtils.printLog("postApi-URL", url);

            if (jsonObject != null)
                AppUtils.printLog("postApi-jsonObject", jsonObject.toString());
            AppUtils.printLog("postApi-token", AppSettings.getString(AppSettings.token));

            AndroidNetworking.post(url)
                    .addHeaders("appVersion", BuildConfig.VERSION_NAME)
                    .addHeaders("apiVersion", AppConstants.apiVersion)
                    .addHeaders("languageCode", AppSettings.getString(AppSettings.language))
                    .addHeaders("loginRegion", "IN")
                    .addHeaders("token", AppSettings.getString(AppSettings.token))
                    .addHeaders("deviceId", AppUtils.getDeviceID(mActivity))
                    .addHeaders("deviceType", AppConstants.deviceType)
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (loader)
                                AppUtils.hideDialog();

                            AppUtils.printLog("postApi-response", response.toString());

                            apiCallback.OnJsonSuccess(response);

                        }

                        @Override
                        public void onError(ANError anError) {

                            AppUtils.hideDialog();

                            if (anError.getErrorCode() == 401) {

                                AppSettings.clearSharedPreference();

                               /* Intent intent = new Intent(mActivity, LoginTypeActivity.class);
                                mActivity.startActivity(intent);
                                mActivity.finishAffinity();*/

                            } else
                                apiCallback.OnFail(anError.getErrorBody());

                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorCode()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorBody()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorDetail()));
                        }
                    });


        } else
            AppUtils.showNoInternetToastSort(mActivity);

    }







    public static void deleteApi(final BaseActivity mActivity,
                                 String url,
                                 JSONObject jsonObject,
                                 final boolean loader,
                                 boolean softKeyboard, final WebServicesCallback apiCallback) {

        if (AppUtils.isNetworkAvailable(mActivity)) {

            if (loader)
                AppUtils.showRequestDialog(mActivity);

            if (softKeyboard)
                AppUtils.hideSoftKeyboard(mActivity);

            AppUtils.printLog("postApi-URL", url);

            if (jsonObject != null)
                AppUtils.printLog("postApi-jsonObject", jsonObject.toString());
            AppUtils.printLog("postApi-token", AppSettings.getString(AppSettings.token));

            AndroidNetworking.delete(url)
                    .addHeaders("appVersion", BuildConfig.VERSION_NAME)
                    .addHeaders("apiVersion", AppConstants.apiVersion)
                    .addHeaders("languageCode", AppSettings.getString(AppSettings.language))
                    .addHeaders("loginRegion", "IN")
                    .addHeaders("token", AppSettings.getString(AppSettings.token))
                    .addHeaders("deviceId", AppUtils.getDeviceID(mActivity))
                    .addHeaders("deviceType", AppConstants.deviceType)
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (loader)
                                AppUtils.hideDialog();

                            AppUtils.printLog("postApi-response", response.toString());

                            apiCallback.OnJsonSuccess(response);

                        }

                        @Override
                        public void onError(ANError anError) {

                            AppUtils.hideDialog();

                            if (anError.getErrorCode() == 401) {

                                AppSettings.clearSharedPreference();

                               /* Intent intent = new Intent(mActivity, LoginTypeActivity.class);
                                mActivity.startActivity(intent);
                                mActivity.finishAffinity();*/

                            } else
                                apiCallback.OnFail(anError.getErrorBody());

                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorCode()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorBody()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorDetail()));
                        }
                    });

        } else
            AppUtils.showNoInternetToastSort(mActivity);

    }

    public static void putApi(final BaseActivity mActivity,
                              String url,
                              JSONObject jsonObject,
                              final boolean loader,
                              boolean softKeyboard, final WebServicesCallback apiCallback) {

        if (AppUtils.isNetworkAvailable(mActivity)) {

            if (loader)
                AppUtils.showRequestDialog(mActivity);

            if (softKeyboard)
                AppUtils.hideSoftKeyboard(mActivity);

            AppUtils.printLog("postApi-URL", url);

            if (jsonObject != null)
                AppUtils.printLog("postApi-jsonObject", jsonObject.toString());
            AppUtils.printLog("postApi-token", AppSettings.getString(AppSettings.token));

            AndroidNetworking.put(url)
                    .addHeaders("appVersion", BuildConfig.VERSION_NAME)
                    .addHeaders("apiVersion", AppConstants.apiVersion)
                    .addHeaders("languageCode", AppSettings.getString(AppSettings.language))
                    .addHeaders("loginRegion", "IN")
                    .addHeaders("token", AppSettings.getString(AppSettings.token))
                    .addHeaders("deviceId", AppUtils.getDeviceID(mActivity))
                    .addHeaders("deviceType", AppConstants.deviceType)
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (loader)
                                AppUtils.hideDialog();

                            AppUtils.printLog("postApi-response", response.toString());

                            apiCallback.OnJsonSuccess(response);

                        }

                        @Override
                        public void onError(ANError anError) {

                            AppUtils.hideDialog();

                            if (anError.getErrorCode() == 401) {

                                AppSettings.clearSharedPreference();

                               /* Intent intent = new Intent(mActivity, LoginTypeActivity.class);
                                mActivity.startActivity(intent);
                                mActivity.finishAffinity();*/

                            } else
                                apiCallback.OnFail(anError.getErrorBody());

                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorCode()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorBody()));
                            AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorDetail()));
                        }
                    });

        } else
            AppUtils.showNoInternetToastSort(mActivity);

    }


    public static void getApi(final BaseActivity mActivity, String url,
                              final boolean loader,
                              boolean softKeyboard,
                              final WebServicesCallback apiCallback) {


        if (loader)
            AppUtils.showRequestDialog(mActivity);

        if (softKeyboard)
            AppUtils.hideSoftKeyboard(mActivity);

        AppUtils.printLog("postApi - apiUrl", url);
        AppUtils.printLog("postApi-token", AppSettings.getString(AppSettings.token));

        AndroidNetworking.get(url)
                .setPriority(Priority.IMMEDIATE)
                .addHeaders("appVersion", BuildConfig.VERSION_NAME)
                .addHeaders("apiVersion", AppConstants.apiVersion)
                .addHeaders("languageCode", AppSettings.getString(AppSettings.language))
                .addHeaders("loginRegion", "IN")
                .addHeaders("token", AppSettings.getString(AppSettings.token))
                .addHeaders("deviceId", AppUtils.getDeviceID(mActivity))
                .addHeaders("deviceType", AppConstants.deviceType)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (loader)
                            AppUtils.hideDialog();

                        AppUtils.printLog("postApi-response", url + "--" + response.toString());

                        apiCallback.OnJsonSuccess(response);

                    }

                    @Override
                    public void onError(ANError anError) {

                        AppUtils.hideDialog();

                        if (anError.getErrorCode() == 401) {

                            AppSettings.clearSharedPreference();

                          /*  Intent intent = new Intent(mActivity, LoginTypeActivity.class);
                            mActivity.startActivity(intent);
                            mActivity.finishAffinity();*/

                        } else
                            apiCallback.OnFail(anError.getErrorBody());

                        AppUtils.printLog("postApi-error", url + "  --  " + String.valueOf(anError.getErrorCode()));
                        AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorBody()));
                        AppUtils.printLog("postApi-error", String.valueOf(anError.getErrorDetail()));
                    }
                });
    }
}
