package code.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityFavoriteBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.adapters.AdapterItem;
import code.adapters.AdapterStore;
import code.common.OnItemClickListener;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class FavoriteActivity extends BaseActivity implements View.OnClickListener {

    private ActivityFavoriteBinding b;

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    private AdapterStore adapterStore;

    private AdapterItem adapterItem;

    private int type = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());

        b.header.tvHeader.setText(getString(R.string.favorites));

        b.llMerchants.setOnClickListener(this);
        b.llProducts.setOnClickListener(this);

        adapterStore = new AdapterStore(arrayList, mActivity);

        adapterItem = new AdapterItem(arrayList, mActivity, new OnItemClickListener() {
            @Override
            public void onItemClickListener(String productId, String quantity, String productQuantity) {
                if (Integer.valueOf(quantity)>Integer.valueOf(productQuantity) ) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
                } else {
                    hitAddToCartApi(productId, quantity);
                }
            }
        });
        b.rvList.setAdapter(adapterStore);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.llMerchants:

                b.viewMerchants.setVisibility(View.VISIBLE);
                b.viewProducts.setVisibility(View.INVISIBLE);

                arrayList.clear();
                b.rvList.setAdapter(adapterStore);

                type = 1;
                hitGetFavListApi();

                break;

            case R.id.llProducts:

                b.viewMerchants.setVisibility(View.INVISIBLE);
                b.viewProducts.setVisibility(View.VISIBLE);

                arrayList.clear();
                b.rvList.setAdapter(adapterItem);

                type = 2;
                hitGetFavListApi();

                break;
        }
    }

    private void hitGetFavListApi() {

        String url = "";

        if (type == 1) {
            url = AppUrls.favoriteMerchantList;
        } else {
            url = AppUrls.favoriteProductList;

        }

        WebServices.getApi(mActivity, url, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                if (type == 1) {
                    parseMerchantList(response);
                } else {
                    parseProductList(response);

                }

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseProductList(JSONObject response) {
        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("productId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("description", jsonObject1.getString("description"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("productImageOne", jsonObject1.getString("productImageOne"));
                    hashMap.put("productImageTwo", jsonObject1.getString("productImageTwo"));
                    hashMap.put("productImageThree", jsonObject1.getString("productImageThree"));
                    hashMap.put("merchantId", jsonObject1.getString("merchantId"));

                    hashMap.put("rating", jsonObject1.getString("rating"));
                    if (jsonObject1.has("productQuantity"))
                        hashMap.put("productQuantity", jsonObject1.getString("productQuantity"));
                    else
                        hashMap.put("productQuantity", "");
                    if (jsonObject1.has("sellingPrice"))
                        hashMap.put("sellingPrice", jsonObject1.getString("sellingPrice"));
                    else
                        hashMap.put("sellingPrice", "");
                    if (jsonObject1.has("offerPrice"))
                        hashMap.put("offerPrice", jsonObject1.getString("offerPrice"));
                    else
                        hashMap.put("offerPrice", "");
                    if (jsonObject1.has("quantity"))
                        hashMap.put("quantity", jsonObject1.getString("quantity"));
                    else
                        hashMap.put("quantity", "");

                    arrayList.add(hashMap);

                }


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterItem.notifyDataSetChanged();

    }

    private void parseMerchantList(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("merchantId", jsonObject1.getString("merchantId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("profileImage", jsonObject1.getString("profileImage"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    arrayList.add(hashMap);

                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterStore.notifyDataSetChanged();

    }

    private void hitAddToCartApi(String productId, String quantity) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("quantity", quantity);
            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.addToCart, json, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAddToCartJson(response, quantity, productId);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddToCartJson(JSONObject response, String quantity, String productId) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {


            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

                showAlreadyCartDialog(jsonObject.getString(AppConstants.resMsg), quantity, productId);
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAlreadyCartDialog(String message, String quantity, String productId) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_already_added);
        bottomSheetDialog.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvCancel, tvMessage, tvContinue;

        tvCancel = bottomSheetDialog.findViewById(R.id.tvCancel);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvMessage.setText(message);

        tvCancel.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            type = 2;
            hitGetFavListApi();
        });

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

            hitClearCartApi(quantity, productId);
        });

    }

    private void hitClearCartApi(String quantity, String productId) {

        WebServices.getApi(mActivity, AppUrls.RemoveProductsFromCart, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseClearCart(response, quantity, productId);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseClearCart(JSONObject response, String quantity, String productId) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitAddToCartApi(productId, quantity);

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hitGetFavListApi();
    }
}