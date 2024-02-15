package code.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityBucketCartBinding;
import com.hathme.android.databinding.ActivityCartBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.common.WebViewActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class BucketCartActivity extends BaseActivity implements View.OnClickListener {
    ActivityBucketCartBinding b;
    AdapterCart adapterCart;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    int totalQuantity = 0;
    int deliveryTip = 0;
    String isFromCart = "0",requestOrderId = "";
    String couponApplied = "";
    double totalPayableAmount = 0, totalAmount = 0,discountAmount = 0;

    String addressId = "", suggestion = "";

    public static boolean isMapTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityBucketCartBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        inits();

    }

    private void inits() {
        if (getIntent().getExtras() != null && getIntent().hasExtra("requestOrderId")) {
            requestOrderId = getIntent().getStringExtra("requestOrderId");
        }
        b.ivBack.setOnClickListener(view -> finish());
        b.tvStoreName.setText(AppSettings.getString(AppSettings.merchantName));
        b.rlAddress.setOnClickListener(this);
        b.tvMakePayment.setOnClickListener(this);
        b.llSuggestion.setOnClickListener(this);
        b.tvContinue.setOnClickListener(this);
        b.ivClear.setOnClickListener(this);
        b.rlApplyCoupon.setOnClickListener(this);
        adapterCart = new AdapterCart(arrayList);
        b.rvCart.setAdapter(adapterCart);

        b.tvTip10.setOnClickListener(this);
        b.tvTip20.setOnClickListener(this);
        b.tvTip50.setOnClickListener(this);
        b.tvTipOther.setOnClickListener(this);
        b.tvOverview.setOnClickListener(this);
        b.tvReadPolicy.setOnClickListener(this);

        if (getIntent().getExtras() != null && getIntent().hasExtra("isFromCart")) {
            isFromCart = getIntent().getStringExtra("isFromCart");
        }
        b.etTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                totalPayableAmount = totalPayableAmount - deliveryTip;
                deliveryTip = AppUtils.returnInt(b.etTip.getText().toString().trim());
                b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
                AppSettings.putString(AppSettings.deliveryTip, b.etTip.getText().toString().trim());
                hitGetOrderDetailApi();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (getIntent().getExtras() != null && getIntent().hasExtra("addressId")) {
            addressId = getIntent().getStringExtra("addressId");
        }
        calculateTip();

    }
    private void calculateTip() {
        if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip10")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 10;
            b.tvTip10.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            hitGetOrderDetailApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip20")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 20;
            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            hitGetOrderDetailApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip50")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 50;
            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            hitGetOrderDetailApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("other")) {
            b.etTip.setVisibility(View.VISIBLE);
            b.etTip.setText(AppSettings.getString(AppSettings.deliveryTip));
            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + AppSettings.getString(AppSettings.deliveryTip));
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            totalPayableAmount = totalPayableAmount - deliveryTip;
//            deliveryTip = 0;
            hitGetOrderDetailApi();
        } else {
        }
    }
    private void hitGetOrderDetailApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("requestOrderId", requestOrderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.bucketProductList, json, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrderDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseOrderDetail(JSONObject response) {
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject json = jsonObject.getJSONObject("data");

                totalAmount = AppUtils.returnDouble(json.getString("totalAmount"));

                b.tvItemTotal.setText(getString(R.string.rupeeSymbol) + " " + totalAmount);

                b.tvDeliveryFee.setText(getString(R.string.rupeeSymbol) + " " + json.getString("delivery"));
                b.tvTax.setText(getString(R.string.rupeeSymbol) + " " + json.getString("taxesAndCharges"));
                couponApplied = json.getString("couponCode");
                //Coupon Applied
                if (!json.getString("couponCode").equals("")) {

                    JSONObject jsonCoupon = json.getJSONObject("couponDetails");

                    b.tvCouponCode.setText("'" + jsonCoupon.getString("couponCode") + "' " + getString(R.string.applied));

                    //Percentage
                    if (jsonCoupon.getString("type").equals("1")) {

                        double maxDisc = AppUtils.returnDouble(jsonCoupon.getString("maxDiscount"));

                        discountAmount = (totalAmount * AppUtils.returnDouble(jsonCoupon.getString("amount"))) / 100;
                        if (discountAmount > maxDisc) discountAmount = maxDisc;
                    }
                    //Flat
                    else {
                        discountAmount = AppUtils.returnDouble(jsonCoupon.getString("amount"));
                    }

                    b.ivClear.setRotation(0);
                    b.ivClear.setImageResource(R.drawable.ic_clear);
                }
                else {
                    discountAmount = 0;
                    b.tvCouponCode.setText(getString(R.string.applyCoupon));
                    b.ivClear.setRotation(90);
                    b.ivClear.setImageResource(R.drawable.ic_arrow_up);
                }

                b.tvDiscountValue.setText(getString(R.string.rupeeSymbol)+" "+String.valueOf(discountAmount));
                if (discountAmount == 0.0) {
                    double totalAmt = AppUtils.returnDouble(json.getString("totalAmount")) +
                            AppUtils.returnDouble(json.getString("delivery")) +
                            AppUtils.returnDouble(json.getString("taxesAndCharges"));
                    b.tvTotal.setText(getString(R.string.rupeeSymbol) + " " + String.valueOf(totalAmt));
                    totalPayableAmount = totalAmt;
                    totalAmount = totalAmt;
                    calculateTotalAmount();
                } else {

                    double totalAmt = AppUtils.returnDouble(json.getString("totalAmount")) +
                            AppUtils.returnDouble(json.getString("delivery")) +
                            AppUtils.returnDouble(json.getString("taxesAndCharges")) - discountAmount;
                    totalPayableAmount = totalAmt;
                    totalAmount = totalAmt;
                    calculateTotalAmount();
                    b.tvTotal.setText(getString(R.string.rupeeSymbol) + " " + String.valueOf(totalAmt));
                }

                JSONArray jsonData = json.getJSONArray("result");
                totalQuantity = 0;
                for (int i = 0; i < jsonData.length(); i++) {
                    JSONObject jsonObject1 = jsonData.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productName", jsonObject1.getString("productName"));
                    hashMap.put("productAmount", jsonObject1.getString("productAmount"));
                    hashMap.put("orderBucketId", jsonObject1.getString("orderBucketId"));
                    arrayList.add(hashMap);
                }
                 totalQuantity =  arrayList.size();
                 b.tvItemCount.setText(""+totalQuantity+" "+getString(R.string.items));
            }
            else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapterCart.notifyDataSetChanged();
    }
    private class AdapterCart extends RecyclerView.Adapter<AdapterCart.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterCart(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterCart.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_buckect_cart, viewGroup, false);
            return new AdapterCart.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterCart.MyViewHolder holder, final int position) {

            holder.tvItemName.setText(data.get(position).get("productName"));
            holder.tvAmount.setText(getString(R.string.rupeeSymbol) + data.get(position).get("productAmount"));
            holder.tvEachItemAmt.setText(data.get(position).get("productAmount"));
            holder.lnQtyPrice.setVisibility(View.GONE);
           }

        @Override
        public int getItemCount() {
//            AppUtils.showToastSort(mActivity,"cart "+data.size());
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvItemName, tvEachItemAmt, tvQuantity, tvAmount;
            LinearLayout lnQtyPrice;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvItemName = itemView.findViewById(R.id.tvItemName);
                lnQtyPrice = itemView.findViewById(R.id.lnQtyPrice);
                tvEachItemAmt = itemView.findViewById(R.id.tvEachItemAmt);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvTip10:
                AppSettings.putString(AppSettings.tipCase, "Tip10");
                b.etTip.setVisibility(View.GONE);
                totalPayableAmount = totalPayableAmount - deliveryTip;
                deliveryTip = 10;
                b.tvTip10.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
                b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
                AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
                 //calculateTotalAmount();
                hitGetOrderDetailApi();
                break;

            case R.id.tvTip20:
                AppSettings.putString(AppSettings.tipCase, "Tip20");
                b.etTip.setVisibility(View.GONE);
                totalPayableAmount = totalPayableAmount - deliveryTip;
                deliveryTip = 20;
                b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip20.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
                b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
                AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
                hitGetOrderDetailApi();
               // hitGetCartApi();
                break;

            case R.id.tvTip50:
                AppSettings.putString(AppSettings.tipCase, "Tip50");
                b.etTip.setVisibility(View.GONE);
                totalPayableAmount = totalPayableAmount - deliveryTip;
                deliveryTip = 50;

                b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip50.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
                b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
                AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
                hitGetOrderDetailApi();
              //  hitGetCartApi();
                break;

            case R.id.tvTipOther:
                AppSettings.putString(AppSettings.tipCase, "other");
                b.etTip.setVisibility(View.VISIBLE);
                b.etTip.setText("");
                b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
                b.tvTipOther.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
                b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + 0);
                totalPayableAmount = totalPayableAmount - deliveryTip;
                deliveryTip = 0;
                hitGetOrderDetailApi();
                // hitGetCartApi();
                break;

            case R.id.rlAddress:

                if (AppUtils.checkAndRequestPermissions(mActivity))
                {
                    AppSettings.putString(AppSettings.isFromPage,"Bucket");
                    AppSettings.putString(AppSettings.isFromBucket,"1");
                    startActivity(new Intent(mActivity, AddressListActivity.class).putExtra("type", "3").putExtra("requestOrderId",requestOrderId));
                }
                else {
                    AppUtils.showToastSort(mActivity, getString(R.string.gpsPermission));
                }
                break;


            case R.id.tvMakePayment:

                if (addressId.isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectAddress));
                } else {
                    showPaymentDialog();
                }

                break;



            case R.id.tvOverview:

                WebViewActivity.from = 1;
                startActivity(new Intent(mActivity, WebViewActivity.class));

                break;

            case R.id.tvReadPolicy:

                WebViewActivity.from = 2;
                startActivity(new Intent(mActivity, WebViewActivity.class));

                break;
            case R.id.rlApplyCoupon:
                startActivity(new Intent(mActivity, BucketCouponListActivity.class).putExtra("totalAmount",String.valueOf(totalAmount)).putExtra("requestOrderId",requestOrderId));
                break;

            case R.id.ivClear:
                if (!couponApplied.equalsIgnoreCase("")) {
                    hitClearCouponApi();
                } else {
                    startActivity(new Intent(mActivity, BucketCouponListActivity.class).putExtra("totalAmount",String.valueOf(totalAmount)).putExtra("requestOrderId",requestOrderId));
                }


                break;
        }
    }

    private void showPaymentDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_payment_mode);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        final int[] mode = {1};

        TextView tvCash, tvWallet, tvPlaceOrder;

        tvCash = bottomSheetDialog.findViewById(R.id.tvCash);
        tvWallet = bottomSheetDialog.findViewById(R.id.tvWallet);
        tvPlaceOrder = bottomSheetDialog.findViewById(R.id.tvPlaceOrder);

        tvCash.setOnClickListener(view -> {

            tvCash.setBackgroundResource(R.drawable.rectangular_border_less_radius);
            tvWallet.setBackgroundResource(R.drawable.et_rectangular_border);

            tvCash.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvWallet.setTextColor(getResources().getColor(R.color.textBlack));
            mode[0] = 1;
        });

        tvWallet.setOnClickListener(view -> {

            tvWallet.setBackgroundResource(R.drawable.rectangular_border_less_radius);
            tvCash.setBackgroundResource(R.drawable.et_rectangular_border);

            tvWallet.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvCash.setTextColor(getResources().getColor(R.color.textBlack));

            mode[0] = 2;
        });

        tvPlaceOrder.setOnClickListener(view -> {

            bottomSheetDialog.dismiss();
            hitPlaceBucketOrderApi(mode[0]);

        });
    }
    private void hitPlaceBucketOrderApi(int paymentMode) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("requestOrderId", requestOrderId);
            jsonObject.put("addressId", addressId);
            jsonObject.put("tip", String.valueOf(deliveryTip));
            jsonObject.put("paymentMode", String.valueOf(paymentMode));
            if (b.tvSuggestion.getText().toString().trim().equals(getString(R.string.writeSuggestion)))
                jsonObject.put("suggestion", "");
            else
                jsonObject.put("suggestion", b.tvSuggestion.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.bucketOrderPlace, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parsePlaceBucketOrder(response);

            }

            @Override
            public void OnFail(String response) {
            AppUtils.showToastSort(mActivity,response);
            }
        });
    }

    private void parsePlaceBucketOrder(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppSettings.putString(AppSettings.deliveryTip, "");
                AppSettings.putString(AppSettings.tipCase, "");
                AppSettings.putString(AppSettings.totalPayableAmount, "");
                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
                        getString(R.string.orderPlacedSuccessfully), 3);

            }
            else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
                        jsonObject.getString(AppConstants.resMsg), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void hitGetAddressListApi() {

        WebServices.getApi(mActivity, AppUrls.GetAddress, false, true, new WebServicesCallback() {

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

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    if (addressId.isEmpty()) {
                        if (jsonObject1.getString("status").equalsIgnoreCase("1"))
                        {
                            addressId = jsonObject1.getString("addressId");
                            String completeAddress = jsonObject1.getString("areaName")+", "+jsonObject1.getString("completeAddress")
                                    +" "+jsonObject1.getString("floor")+" "+jsonObject1.getString("landmark");
                            b.tvAddress.setText(completeAddress);

                            switch (jsonObject1.getString("addressType")) {

                                case "1":
                                    b.tvAddressType.setText(getString(R.string.home));
                                    break;

                                case "2":
                                    b.tvAddressType.setText(getString(R.string.work));
                                    break;

                                case "3":
                                    b.tvAddressType.setText(getString(R.string.hotel));
                                    break;

                                case "4":
                                    b.tvAddressType.setText(getString(R.string.other));
                                    break;
                            }
                            break;
                        }

                    } else {

                        if (addressId.equals(jsonObject1.getString("addressId"))) {
                            String completeAddress = jsonObject1.getString("areaName")+", "+jsonObject1.getString("completeAddress")
                                    +" "+jsonObject1.getString("floor")+" "+jsonObject1.getString("landmark");
                            b.tvAddress.setText(completeAddress);

                            switch (jsonObject1.getString("addressType")) {

                                case "1":
                                    b.tvAddressType.setText(getString(R.string.home));
                                    break;

                                case "2":
                                    b.tvAddressType.setText(getString(R.string.work));
                                    break;

                                case "3":
                                    b.tvAddressType.setText(getString(R.string.hotel));
                                    break;

                                case "4":
                                    b.tvAddressType.setText(getString(R.string.other));
                                    break;
                            }
                            break;
                        }
                    }
                }

            } else {

                b.tvAddressType.setText(getString(R.string.noAddressSelected));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        hitGetOrderDetailApi();
        hitGetAddressListApi();
    }
    @SuppressLint("SetTextI18n")
    private void calculateTotalAmount() {
        totalPayableAmount = totalPayableAmount + AppUtils.returnInt(AppSettings.getString(AppSettings.deliveryTip));
        b.tvToPay.setText(getString(R.string.rupeeSymbol) + " " + totalPayableAmount);
        b.tvTotalAmount.setText(getString(R.string.rupeeSymbol) + " " + totalPayableAmount);
    }
    private void hitClearCouponApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("requestOrderId", requestOrderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
       Log.v("requestOrderId",requestOrderId);
        WebServices.postApi(mActivity, AppUrls.RemoveBucketCoupon, json,true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJsonClear(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseJsonClear(JSONObject response) {


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);
            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitGetOrderDetailApi();
            } else {
             AppUtils.showMessageDialog(mActivity,getString(R.string.coupon), jsonObject.getString(AppConstants.resMsg),1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}