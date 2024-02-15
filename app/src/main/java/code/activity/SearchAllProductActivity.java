package code.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivitySearchAllProductBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.adapters.AdapterItem;
import code.adapters.AdapterItem2;
import code.common.OnItemClickListener;
import code.common.RecyclerTouchListener;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class SearchAllProductActivity extends BaseActivity implements View.OnClickListener{
    ActivitySearchAllProductBinding binding;
    ArrayList<HashMap<String, String>> arrayListAllProduct = new ArrayList<>();
    AdapterItem2 adapter;
    RecyclerTouchListener touchListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchAllProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inits();
    }

    private void inits() {
        binding.header.ivBack.setOnClickListener(this);
        binding.etSearch.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.allProduct));
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.etSearch.getText().toString().isEmpty()) {
                    if (touchListener != null) {
                        binding.rvAllProduct.addOnItemTouchListener(touchListener);
                    }
                    else
                    {
                        arrayListAllProduct.clear();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (touchListener != null) {
                        binding.rvAllProduct.removeOnItemTouchListener(touchListener);
                        binding.rvAllProduct.removeOnItemTouchListener(touchListener);
                    }
                    hitSearchProductApi();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        adapter = new AdapterItem2(arrayListAllProduct, mActivity, new OnItemClickListener() {
            @Override
            public void onItemClickListener(String productId, String quantity, String productQuantity) {
                if (Integer.valueOf(quantity)>Integer.valueOf(productQuantity) ) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
                }
                else {
                    hitAddToCartApi(productId, quantity);
                    //adapter.notifyDataSetChanged();
                }
            }
        });
        binding.rvAllProduct.setAdapter(adapter);
    }
    private void hitSearchProductApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            jsonObject.put("keyword", binding.etSearch.getText().toString());
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.searchAllproductList,
                json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseSearchJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseSearchJson(JSONObject response) {
        arrayListAllProduct.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1"))
            {
                JSONArray jsonArrayAllProduct = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArrayAllProduct.length(); i++) {
                    JSONObject jsonObject1 = jsonArrayAllProduct.getJSONObject(i);
                    JSONObject objProduct = jsonObject1.getJSONObject("product");
                    JSONObject objMerchentDetails = jsonObject1.getJSONObject("merchantdetails");
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", objProduct.getString("productId"));
                    hashMap.put("name", objProduct.getString("name"));
                    hashMap.put("description", objProduct.getString("description"));
                    hashMap.put("price", objProduct.getString("price"));
                    hashMap.put("sellingPrice", objProduct.getString("sellingPrice"));
                    hashMap.put("offerPrice", objProduct.getString("offerPrice"));
                    hashMap.put("productQuantity", objProduct.getString("productQuantity"));
                    hashMap.put("productImageOne", objProduct.getString("productImageOne"));
                    hashMap.put("productImageTwo", objProduct.getString("productImageTwo"));
                    hashMap.put("productImageThree", objProduct.getString("productImageThree"));
                    hashMap.put("merchantId", objProduct.getString("merchantId"));
                    hashMap.put("merchetName", objMerchentDetails.getString("name"));
                   //hashMap.put("profileImage", objMerchentDetails.getString("profileImage"));
                    hashMap.put("businessName", objMerchentDetails.getString("businessName"));
                    hashMap.put("storeImage", objMerchentDetails.getString("storeImage"));
                    hashMap.put("MerchantAddress", objMerchentDetails.getString("address"));
                    if (objProduct.has("quantity"))
                        hashMap.put("quantity",
                        objProduct.getString("quantity"));
                    else
                        hashMap.put("quantity", "");
                    arrayListAllProduct.add(hashMap);
                }

            }
            else {
                String resCode = jsonObject.getString(AppConstants.resCode);
                String msg = jsonObject.getString(AppConstants.resMsg);
                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));
            }
           // loadList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

         adapter.notifyDataSetChanged();

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
                hitSearchProductApi();

            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {
                showAlreadyCartDialog(jsonObject.getString(AppConstants.resMsg), quantity, productId);
            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);
            }
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

        });

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

            hitClearCartApi(quantity, productId);
        });

    }
//    private void hitGetMerchantProductApi() {
//
//        JSONObject jsonObject = new JSONObject();
//        JSONObject json = new JSONObject();
//
//        try {
//            jsonObject.put("merchantId", "");
//
//            json.put(AppConstants.projectName, jsonObject);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        WebServices.postApi(mActivity, AppUrls.productListByMerchantId, json, true, true, new WebServicesCallback() {
//
//            @Override
//            public void OnJsonSuccess(JSONObject response) {
//
//                parseMerchantListJson(response);
//
//            }
//
//            @Override
//            public void OnFail(String response) {
//
//            }
//        });
//    }
//
//    private void parseMerchantListJson(JSONObject response) {
//
//        arrayListAllProduct.clear();
//
//        try {
//            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
//
//            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
//
//                JSONObject jsonData = jsonObject.getJSONObject("data");
//
//                JSONArray jsonArrayAllProduct = jsonData.getJSONArray("allProducts");
//
//                for (int i = 0; i < jsonArrayAllProduct.length(); i++) {
//
//                    JSONObject jsonObject1 = jsonArrayAllProduct.getJSONObject(i);
//                    HashMap<String, String> hashMap = new HashMap<>();
//                    hashMap.put("productId", jsonObject1.getString("productId"));
//                    hashMap.put("name", jsonObject1.getString("name"));
//                    hashMap.put("description", jsonObject1.getString("description"));
//                    hashMap.put("price", jsonObject1.getString("price"));
//                    hashMap.put("productImageOne", jsonObject1.getString("productImageOne"));
//                    hashMap.put("productImageTwo", jsonObject1.getString("productImageTwo"));
//                    hashMap.put("productImageThree", jsonObject1.getString("productImageThree"));
//                    hashMap.put("merchantId", "");
//                    if (jsonObject1.has("quantity"))
//                        hashMap.put("quantity", jsonObject1.getString("quantity"));
//                    else
//                        hashMap.put("quantity", "");
//
//                    arrayListAllProduct.add(hashMap);
//
//                }
//
//            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {
//
//            } else {
//                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
//                        jsonObject.getString(AppConstants.resMsg), 2);
//            }
//           // loadList(arrayListAllProduct);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        // adapterAllProduct.notifyDataSetChanged();
//
//    }

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
        }
    }
}