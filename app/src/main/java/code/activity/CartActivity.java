package code.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
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

public class CartActivity extends BaseActivity implements View.OnClickListener {

    ActivityCartBinding b;

    AdapterCart adapterCart;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    int deliveryTip = 0;
    String isFromCart = "0";
    String couponApplied = "", statusCOD = "", statusOnlinePayment = "", statusWallet = "";

    double totalPayableAmount = 0, totalAmount = 0;

    String addressId = "", suggestion = "";

    public static boolean isMapTouched = false;

    private boolean isAnyItemUnAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> finish());

        b.rlAddress.setOnClickListener(this);
        b.tvMakePayment.setOnClickListener(this);

        b.rlApplyCoupon.setOnClickListener(this);

        b.llSuggestion.setOnClickListener(this);
        b.tvContinue.setOnClickListener(this);

        adapterCart = new AdapterCart(arrayList);
        b.rvCart.setAdapter(adapterCart);

        b.tvTip10.setOnClickListener(this);
        b.tvTip20.setOnClickListener(this);
        b.tvTip50.setOnClickListener(this);
        b.tvTipOther.setOnClickListener(this);

        b.tvOverview.setOnClickListener(this);
        b.tvReadPolicy.setOnClickListener(this);

        b.ivClear.setOnClickListener(this);
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
                //calculateTotalAmount();
                hitGetCartApi();
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

    @SuppressLint("SetTextI18n")
    private void calculateTip() {
        if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip10")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
//            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 10;
            b.tvTip10.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            //calculateTotalAmount();
//            hitGetCartApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip20")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
//            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 20;
            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            //calculateTotalAmount();
//            hitGetCartApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("Tip50")) {
            b.etTip.setVisibility(View.GONE);
            b.etTip.setText("0");
//            totalPayableAmount = totalPayableAmount - deliveryTip;
            deliveryTip = 50;

            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + deliveryTip);
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
            //calculateTotalAmount();
//            hitGetCartApi();
        } else if (AppSettings.getString(AppSettings.tipCase).equalsIgnoreCase("other")) {
            b.etTip.setVisibility(View.VISIBLE);
            b.etTip.setText(AppSettings.getString(AppSettings.deliveryTip));
            b.tvTip10.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip20.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTip50.setBackgroundResource(R.drawable.bg_white_no_radius);
            b.tvTipOther.setBackgroundResource(R.drawable.bg_color_border_no_radius1);
            b.tvDeliveryTip.setText(getString(R.string.rupeeSymbol) + " " + AppSettings.getString(AppSettings.deliveryTip));
            AppSettings.putString(AppSettings.deliveryTip, String.valueOf(deliveryTip));
//            totalPayableAmount = totalPayableAmount - deliveryTip;
//            deliveryTip = 0;
//            hitGetCartApi();
            // calculateTotalAmount();
        } else {
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

                    if (jsonObject1.getString("defaultAddress").equals("1")) {
                        addressId = jsonObject1.getString("addressId");
                        String completeAddress = jsonObject1.getString("completeAddress")
                                + " " + jsonObject1.getString("floor") + " " + jsonObject1.getString("landmark");
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

            } else {

                b.tvAddressType.setText(getString(R.string.noAddressSelected));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                //making false here then if any product unavailable make true in adapter because condition is checking there
                isAnyItemUnAvailable = false;

                b.llMain.setVisibility(View.VISIBLE);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                statusCOD = jsonData.getString("statusCOD");
                statusWallet = jsonData.getString("statusWallet");
                statusOnlinePayment = jsonData.getString("statusOnlinePayment");
                JSONArray jsonArray = jsonData.getJSONArray("items");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject json = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", json.getString("name"));
                    hashMap.put("priceWithQuantity", json.getString("priceWithQuantity"));
                    if (json.has("productQuantity"))
                        hashMap.put("productQuantity", json.getString("productQuantity"));
                    else
                        hashMap.put("productQuantity", "");
                    hashMap.put("quantity", json.getString("quantity"));
                    hashMap.put("productId", json.getString("productId"));
                    hashMap.put("itemStatus", json.getString("itemStatus"));
                    arrayList.add(hashMap);
                }

                b.tvStoreName.setText(jsonData.getString("merchantName"));
                b.tvItemCount.setText(jsonArray.length() + " " + getString(R.string.items));

                JSONObject jsonBill = jsonData.getJSONObject("billDetails");

                couponApplied = jsonBill.getString("couponApplied");

                //Coupon Applied
                if (jsonBill.getString("couponApplied").equals("1")) {

                    JSONObject jsonCoupon = jsonData.getJSONObject("couponDetails");

                    b.tvCouponCode.setText("'" + jsonCoupon.getString("couponCode") + "' " + getString(R.string.applied));

                    b.ivClear.setRotation(0);
                    b.ivClear.setImageResource(R.drawable.ic_clear);
                } else {
                    b.tvCouponCode.setText(getString(R.string.applyCoupon));
                    b.ivClear.setRotation(90);
                    b.ivClear.setImageResource(R.drawable.ic_arrow_up);
                }

                setBillAmount(jsonBill);

            } else {
                b.llMain.setVisibility(View.GONE);
                b.rlEmptyCart.setVisibility(View.VISIBLE);
//                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
//                        jsonObject.getString(AppConstants.resMsg), 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterCart.notifyDataSetChanged();

    }

    @SuppressLint("SetTextI18n")
    private void setBillAmount(JSONObject jsonBill) {

        String rupee = getString(R.string.rupeeSymbol) + " ";

        try {
            b.tvItemTotal.setText(rupee + jsonBill.getString("totalAmount"));
            b.tvDeliveryFee.setText(rupee + jsonBill.getString("delivery"));

            if (jsonBill.has("discount")) {
                b.tvDiscountValue.setText("- " + rupee + jsonBill.getString("discount"));
            } else {
                b.tvDiscountValue.setText("- " + rupee + "0");
            }

            b.tvTax.setText(rupee + jsonBill.getString("taxesAndCharges"));

            b.tvTotal.setText(rupee + jsonBill.getString("toPay"));
            totalAmount = AppUtils.returnDouble(jsonBill.getString("toPay"));

            totalPayableAmount = AppUtils.returnDouble(jsonBill.getString("toPay"));

            b.tvToPay.setText(rupee + (totalPayableAmount + deliveryTip));
            b.tvTotalAmount.setText(rupee + (totalPayableAmount + deliveryTip));


        } catch (JSONException e) {
            e.printStackTrace();
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
                // calculateTotalAmount();
                hitGetCartApi();
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
                // calculateTotalAmount();
                hitGetCartApi();
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
                // calculateTotalAmount();
                hitGetCartApi();
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
                //   calculateTotalAmount();
                // hitGetCartApi();
                break;

            case R.id.rlAddress:
                AppSettings.putString(AppSettings.isFromPage, "Cart");
                startActivity(new Intent(mActivity, AddressListActivity.class).putExtra("type", "3"));
                break;

            case R.id.rlApplyCoupon:
                startActivity(new Intent(mActivity, CouponListActivity.class).putExtra("totalAmount", String.valueOf(totalAmount)));
                break;

            case R.id.ivClear:
                if (couponApplied.equalsIgnoreCase("1")) {
                    hitClearCouponApi();
                } else {
                    startActivity(new Intent(mActivity, CouponListActivity.class).putExtra("totalAmount", String.valueOf(totalAmount)));
                }
                break;
            case R.id.tvContinue:
                hitClearCartApi(AppSettings.getString(AppSettings.quantity), AppSettings.getString(AppSettings.productId));
                break;

            case R.id.tvMakePayment:

                if (addressId.isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectAddress));
                } else if (isAnyItemUnAvailable) {
                    AppUtils.showToastSort(mActivity, getString(R.string.someItemsUnAvailable));
                } else {

                    startActivity(new Intent(mActivity, PaymentActivity.class)
                            .putExtra("totalAmount", String.valueOf(totalPayableAmount+deliveryTip))
                            .putExtra("addressId", addressId)
                            .putExtra("tip", String.valueOf(deliveryTip)));
//                    showPaymentDialog(statusCOD, statusOnlinePayment, statusWallet);
                }

                break;

            case R.id.llSuggestion:

                showSuggestionDialog();

                break;

            case R.id.tvOverview:

                WebViewActivity.from = 1;
                startActivity(new Intent(mActivity, WebViewActivity.class));

                break;

            case R.id.tvReadPolicy:

                WebViewActivity.from = 2;
                startActivity(new Intent(mActivity, WebViewActivity.class));

                break;

        }
    }

    private void showPaymentDialog(String statusCOD, String statusOnlinePayment, String statusWallet) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_payment_mode);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        final int[] mode = {0};

        TextView tvCash, tvWallet, tvPlaceOrder, tvOnlinePayment;

        tvCash = bottomSheetDialog.findViewById(R.id.tvCash);
        tvWallet = bottomSheetDialog.findViewById(R.id.tvWallet);
        tvOnlinePayment = bottomSheetDialog.findViewById(R.id.tvOnlinePayment);
        tvPlaceOrder = bottomSheetDialog.findViewById(R.id.tvPlaceOrder);
        if (statusCOD.equalsIgnoreCase("1")) {
            tvCash.setVisibility(View.VISIBLE);
        } else {
            tvCash.setVisibility(View.GONE);
        }
        if (statusOnlinePayment.equalsIgnoreCase("1")) {
            tvOnlinePayment.setVisibility(View.VISIBLE);
        } else {
            tvOnlinePayment.setVisibility(View.GONE);
        }
        if (statusCOD.equalsIgnoreCase("1")) {
            tvCash.setVisibility(View.VISIBLE);
        } else {
            tvCash.setVisibility(View.GONE);
        }
        tvCash.setOnClickListener(view -> {
            tvCash.setBackgroundResource(R.drawable.rectangular_border_less_radius);
            tvWallet.setBackgroundResource(R.drawable.et_rectangular_border);
            tvOnlinePayment.setBackgroundResource(R.drawable.et_rectangular_border);
            tvCash.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvWallet.setTextColor(getResources().getColor(R.color.textBlack));
            tvOnlinePayment.setTextColor(getResources().getColor(R.color.textBlack));
            mode[0] = 3;
        });

        tvWallet.setOnClickListener(view -> {
            tvWallet.setBackgroundResource(R.drawable.rectangular_border_less_radius);
            tvCash.setBackgroundResource(R.drawable.et_rectangular_border);
            tvOnlinePayment.setBackgroundResource(R.drawable.et_rectangular_border);
            tvWallet.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvCash.setTextColor(getResources().getColor(R.color.textBlack));
            tvOnlinePayment.setTextColor(getResources().getColor(R.color.textBlack));
            mode[0] = 2;
        });
        tvOnlinePayment.setOnClickListener(view -> {
            tvOnlinePayment.setBackgroundResource(R.drawable.rectangular_border_less_radius);
            tvWallet.setBackgroundResource(R.drawable.et_rectangular_border);
            tvCash.setBackgroundResource(R.drawable.et_rectangular_border);
            tvOnlinePayment.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvCash.setTextColor(getResources().getColor(R.color.textBlack));
            tvWallet.setTextColor(getResources().getColor(R.color.textBlack));
            AppUtils.showToastSort(mActivity, getString(R.string.coming_soon));
            // mode[0] = 1;
        });
        tvPlaceOrder.setOnClickListener(view -> {
            if (String.valueOf(mode[0]).equalsIgnoreCase("0")) {
                AppUtils.showToastSort(mActivity, getString(R.string.select_payment_mode));
            } else {
                bottomSheetDialog.dismiss();
                hitPlaceOrderApi(mode[0]);
            }

        });
    }

    private void showSuggestionDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_suggestion);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        EditText etSuggestion = bottomSheetDialog.findViewById(R.id.etSuggestion);

        if (!suggestion.isEmpty()) {
            etSuggestion.setText(suggestion);
        }

        bottomSheetDialog.findViewById(R.id.ivClose).setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetDialog.findViewById(R.id.tvSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etSuggestion.getText().toString().trim().isEmpty()) {
                    b.tvSuggestion.setText(getString(R.string.writeSuggestion));
                    suggestion = "";
                    bottomSheetDialog.dismiss();
                } else {
                    b.tvSuggestion.setText(etSuggestion.getText().toString().trim());
                    suggestion = etSuggestion.getText().toString().trim();
                    bottomSheetDialog.dismiss();
                }

            }
        });
    }

    private void hitPlaceOrderApi(int paymentMode) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("addressId", addressId);
            jsonObject.put("tip", String.valueOf(deliveryTip));
            jsonObject.put("paymentMode", String.valueOf(paymentMode));
            if (b.tvSuggestion.getText().toString().trim().equals(getString(R.string.writeSuggestion))) {
                jsonObject.put("suggestion", "");
            } else {
                jsonObject.put("suggestion", b.tvSuggestion.getText().toString().trim());
            }
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.placeOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parsePlaceOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parsePlaceOrder(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                AppSettings.putString(AppSettings.deliveryTip, "");
                AppSettings.putString(AppSettings.tipCase, "");
                AppSettings.putString(AppSettings.totalPayableAmount, "");
                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
                        getString(R.string.orderPlacedSuccessfully), 3);

            } else {
                if (jsonObject.getString(AppConstants.resMsg).equalsIgnoreCase("order cancel")) {
                    showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.itemUnavailable));
                } else {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
                            jsonObject.getString(AppConstants.resMsg), 2);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hitClearCouponApi() {

        WebServices.getApi(mActivity, AppUrls.removeCoupon, true, true, new WebServicesCallback() {

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

                hitGetCartApi();

            } else {

                hitGetCartApi();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private class AdapterCart extends RecyclerView.Adapter<AdapterCart.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private AdapterCart(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterCart.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_cart, viewGroup, false);
            return new AdapterCart.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterCart.MyViewHolder holder, final int position) {

            holder.tvItemName.setText(data.get(position).get("name"));
            holder.tvQuantity.setText(data.get(position).get("quantity"));
            holder.tvAmount.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("priceWithQuantity"));

            if (data.get(position).get("itemStatus").equals("2")) {
                holder.tvErrorMessage.setVisibility(View.VISIBLE);

                holder.tvErrorMessage.setText(getString(R.string.itemUnavailable));
                isAnyItemUnAvailable = true;
            } else if (AppUtils.returnInt(data.get(position).get("productQuantity")) < AppUtils.returnInt(data.get(position).get("quantity"))) {
                holder.tvErrorMessage.setVisibility(View.VISIBLE);
                holder.tvErrorMessage.setText(getString(R.string.only) + " " + data.get(position).get("productQuantity") + " " + getString(R.string.available));
                isAnyItemUnAvailable = true;

            } else {
                holder.tvErrorMessage.setVisibility(View.GONE);
            }

            holder.tvPlus.setOnClickListener(view -> {
                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
                qty = qty + 1;
                if (qty > AppUtils.returnInt(data.get(position).get("productQuantity"))) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.only) + " " +
                            data.get(position).get("productQuantity") + " " + getString(R.string.quantityAvailable), 4);

                } else {
                    holder.tvQuantity.setText(String.valueOf(qty));
                    hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));
                }

            });

            holder.tvMinus.setOnClickListener(view -> {

                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());

                if (qty > 0) {
                    qty = qty - 1;
               /* if (qty == 0) {
                    b.llMain.setVisibility(View.GONE);
                    b.rlEmptyCart.setVisibility(View.VISIBLE);
                }*/
                    holder.tvQuantity.setText(String.valueOf(qty));
                    hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));
                }
            });
        }

        @Override
        public int getItemCount() {
//            AppUtils.showToastSort(mActivity,"cart "+data.size());
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvItemName, tvMinus, tvQuantity, tvPlus, tvAmount, tvErrorMessage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvMinus = itemView.findViewById(R.id.tvMinus);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvPlus = itemView.findViewById(R.id.tvPlus);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvErrorMessage = itemView.findViewById(R.id.tvErrorMessage);
            }
        }
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

                hitGetCartApi();

            } else {
//                showMessageDialog(mActivity, getString(R.string.cart),
//                        jsonObject.getString(AppConstants.resMsg), 2,quantity,productId);
                AppSettings.putString(AppSettings.quantity, quantity);
                AppSettings.putString(AppSettings.productId, productId);
                AppSettings.putString(AppSettings.tipCase, "");
                AppSettings.putString(AppSettings.deliveryTip, "");
                AppSettings.putString(AppSettings.totalPayableAmount, "");
                hitGetCartApi();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        hitGetCartApi();
        hitGetAddressListApi();
    }

    private void showMessageDialog(Activity mActivity, String title, String message) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_message);
        bottomSheetDialog.setCancelable(false);
        //bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvTitle, tvMessage, tvContinue;

        tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvTitle.setText(title);
        tvMessage.setText(message);

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();
            hitClearCartApi("", "");
        });

    }

    private void hitClearCartApi(String quantity, String productId) {

        WebServices.getApi(mActivity, AppUrls.RemoveProductsFromCart, false, false, new WebServicesCallback() {

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

            } else {
                if (isFromCart.equalsIgnoreCase("1")) {
                    mActivity.startActivity(new Intent(mActivity, MainActivity.class));
                    mActivity.finishAffinity();
                } else {
                    finish();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}