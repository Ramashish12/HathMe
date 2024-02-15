package code.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityProductSearchBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.adapters.AdapterItem;
import code.common.OnItemClickListener;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ProductSearchActivity extends BaseActivity implements View.OnClickListener {
    private ActivityProductSearchBinding binding;
    private String merchantId = "";
    private AdapterItem adapterAllProduct;
    private ArrayList<HashMap<String, String>> arrayListAllProduct = new ArrayList<>();
    private ArrayList<String> arralready = new ArrayList<String>();
    private ArrayList<HashMap<String, String>> arrsearchtemp = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inits();
    }

    private void inits() {
        binding.header.ivBack.setOnClickListener(this);
        binding.etSearch.setOnClickListener(this);
        binding.header.tvHeader.setText(getString(R.string.allProduct));
        merchantId = getIntent().getStringExtra("merchantId");
        hitGetMerchantProductApi();
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String strsearch = binding.etSearch.getText().toString().trim();
                if (binding.etSearch.equals("")) {
                    loadList(arrayListAllProduct);
                } else {
                    arrsearchtemp = new ArrayList<HashMap<String, String>>();
                    arralready = new ArrayList<String>();
                    for (int i = 0; i < arrayListAllProduct.size(); i++) {
                        HashMap<String, String> cmap = arrayListAllProduct.get(i);
                        String name = arrayListAllProduct.get(i).get("name").toLowerCase();
                        if (name.contains(strsearch.toLowerCase())) {
                            HashMap<String, String> map = cmap;

                            if (!arralready.contains(arrayListAllProduct.get(i).get("name"))) {
                                arralready.add(arrayListAllProduct.get(i).get("name"));
                                arrsearchtemp.add(map);
                            }
                        }


                    }
                    loadList(arrsearchtemp);
                }
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub
            }
        });
    }

    private void hitGetMerchantProductApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("merchantId", merchantId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.productListByMerchantId, json, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseMerchantListJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseMerchantListJson(JSONObject response) {

        arrayListAllProduct.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                JSONArray jsonArrayAllProduct = jsonData.getJSONArray("allProducts");

                for (int i = 0; i < jsonArrayAllProduct.length(); i++) {

                    JSONObject jsonObject1 = jsonArrayAllProduct.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("productId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("description", jsonObject1.getString("description"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("productQuantity", jsonObject1.getString("productQuantity"));
                    hashMap.put("sellingPrice", jsonObject1.getString("sellingPrice"));
                    hashMap.put("offerPrice", jsonObject1.getString("offerPrice"));
                    hashMap.put("productImageOne", jsonObject1.getString("productImageOne"));
                    hashMap.put("productImageTwo", jsonObject1.getString("productImageTwo"));
                    hashMap.put("productImageThree", jsonObject1.getString("productImageThree"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    hashMap.put("merchantId", merchantId);

                    if (jsonObject1.has("quantity"))
                        hashMap.put("quantity", jsonObject1.getString("quantity"));
                    else
                        hashMap.put("quantity", "");


                    arrayListAllProduct.add(hashMap);

                }

            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);
            }
            loadList(arrayListAllProduct);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // adapterAllProduct.notifyDataSetChanged();

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
                hitGetMerchantProductApi();

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
            hitGetMerchantProductApi();
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

    public void loadList(final ArrayList<HashMap<String, String>> arrList) {
        adapterAllProduct = new AdapterItem(arrList, mActivity, new OnItemClickListener() {
            @Override
            public void onItemClickListener(String productId, String quantity,String productQuantity) {
                if (Integer.parseInt(quantity)>Integer.parseInt(productQuantity) ) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
                }
                else {
                    hitAddToCartApi(productId, quantity);
                }
            }
        });
        binding.rvAllProduct.setAdapter(adapterAllProduct);
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