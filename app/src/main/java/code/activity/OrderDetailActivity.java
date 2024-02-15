package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityOrderDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import code.maplivetracking.LiveTrackingActivity;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class OrderDetailActivity extends BaseActivity implements View.OnClickListener {

    ActivityOrderDetailBinding b;
    private CountDownTimer countDownTimer;
    private String isCsrachCard = "";
    String orderId = "", merchantMobile = "", orderType = "", cancellationReturnType = "";

    Adapter adapter;
    AdapterEachItem adapterEachItem;
    JSONArray jsonArray = new JSONArray();
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.ivBack.setOnClickListener(view -> onBackPressed());

        orderId = getIntent().getStringExtra("orderId");
        orderType = getIntent().getStringExtra("orderType");

        b.rvItems.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvItems.setAdapter(adapter);
        adapterEachItem = new AdapterEachItem(arrayList);
        b.rvEachItemsPrice.setAdapter(adapterEachItem);

        b.llRepeatOrder.setOnClickListener(this);
        b.tvCancelOrder.setOnClickListener(this);
        b.tvCallStore.setOnClickListener(this);
        b.tvScratchCardNow.setOnClickListener(this);
        b.tvRateNow.setOnClickListener(this);
        b.fabTrack.setOnClickListener(this);

        hitGetOrderDetailApi();

    }

    private void hitGetOrderDetailApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.orderDetail, json, true, true, new WebServicesCallback() {

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

                JSONObject jsonData = jsonObject.getJSONObject("data");
                JSONObject jsonAddressesResult = jsonData.getJSONObject("addressesResult");
                String completeAddress = "" + jsonAddressesResult.getString("completeAddress")
                        + " " + jsonAddressesResult.getString("floor") + " " + jsonAddressesResult.getString("landmark");
                b.tvOrderAddress.setText(completeAddress);
                b.tvOrderNo.setText(jsonData.getString("orderNo"));
                b.tvStoreName.setText(jsonData.getString("merchantName"));
                b.tvAddress.setText(jsonData.getString("merchantAddress"));
                cancellationReturnType = jsonData.getString("cancellationReturnType");
//                if (jsonData.getString("discount").equalsIgnoreCase("") ||
//                        jsonData.getString("discount").equalsIgnoreCase(null) ||
//                        jsonData.getString("discount").equalsIgnoreCase("null")) {
//                    double totalAmt = AppUtils.returnDouble(jsonData.getString("finalAmount"));
//                    b.tvTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//                    b.tvGrandTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//
//                } else {
//                    double finalAmt = AppUtils.returnDouble(jsonData.getString("finalAmount"));
//                    double discount = AppUtils.returnDouble(jsonData.getString("discount"));
//                    double totalAmt = finalAmt - discount;
//                    b.tvTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//                    b.tvGrandTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//                }
                double totalAmt = AppUtils.returnDouble(jsonData.getString("finalAmount"));
                b.tvTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
                b.tvGrandTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
                b.tvTip.setText(getString(R.string.rupeeSymbol) + AppUtils.ifEmptyReturn0(jsonData.getString("tip")));
                jsonArray = jsonData.getJSONArray("products");
                if (jsonData.getString("orderType").equalsIgnoreCase("1")) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProduct = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("name", jsonProduct.getString("name"));
                        hashMap.put("quantity", jsonProduct.getString("quantity"));
                        hashMap.put("priceOfEachItem", jsonProduct.getString("priceOfEachItem"));
                        hashMap.put("priceWithQuantity", jsonProduct.getString("priceWithQuantity"));
                        hashMap.put("weight", jsonProduct.getString("weight"));
                        hashMap.put("bestBefore", jsonProduct.getString("bestBefore"));
                        hashMap.put("description", jsonProduct.getString("description"));
                        hashMap.put("price", jsonProduct.getString("price"));
                        hashMap.put("category", jsonProduct.getString("category"));
                        hashMap.put("subCategory", jsonProduct.getString("subCategory"));
                        hashMap.put("status", jsonProduct.getString("status"));
                        hashMap.put("sellingPrice", jsonProduct.getString("sellingPrice"));
                        hashMap.put("offerPrice", jsonProduct.getString("offerPrice"));
                        hashMap.put("specialFeature", jsonProduct.getString("specialFeature"));
                        hashMap.put("brand", jsonProduct.getString("brand"));
                        hashMap.put("color", jsonProduct.getString("color"));
                        hashMap.put("size", jsonProduct.getString("size"));
                        hashMap.put("isVeg", jsonProduct.getString("isVeg"));
                        hashMap.put("unit", jsonProduct.getString("unit"));
                        hashMap.put("unitType", jsonProduct.getString("unitType"));
                        hashMap.put("mrp", jsonProduct.getString("mrp"));
                        hashMap.put("packedType", jsonProduct.getString("packedType"));
                        hashMap.put("expiryDate", jsonProduct.getString("expiryDate"));
                        hashMap.put("batchNumber", jsonProduct.getString("batchNumber"));
                        hashMap.put("distributorName", jsonProduct.getString("distributorName"));
                        hashMap.put("materialType", jsonProduct.getString("materialType"));
                        hashMap.put("aboutThisItem", jsonProduct.getString("aboutThisItem"));
                        hashMap.put("manufacturer", jsonProduct.getString("manufacturer"));
                        hashMap.put("disclaimer", jsonProduct.getString("disclaimer"));
                        hashMap.put("shelfLife", jsonProduct.getString("shelfLife"));
                        hashMap.put("fssaiLicense", jsonProduct.getString("fssaiLicense"));
                        hashMap.put("countryOfOrigin", jsonProduct.getString("countryOfOrigin"));
                        hashMap.put("seller", jsonProduct.getString("seller"));
                        hashMap.put("ingredients", jsonProduct.getString("ingredients"));
                        hashMap.put("content", jsonProduct.getString("content"));
                        hashMap.put("isProductApproved", jsonProduct.getString("isProductApproved"));
                        hashMap.put("recommended", jsonProduct.getString("recommended"));

                        isCsrachCard = "1";
                        hashMap.put("orderType", "1");
                        arrayList.add(hashMap);
                    }
                } else {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProduct = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("_id", jsonProduct.getString("_id"));
                        hashMap.put("requestOrderId", jsonProduct.getString("requestOrderId"));
                        hashMap.put("name", jsonProduct.getString("productName"));
                        hashMap.put("price", jsonProduct.getString("productAmount"));
                        hashMap.put("sellingPrice", jsonProduct.getString("productAmount"));
                        hashMap.put("priceWithQuantity", jsonProduct.getString("productAmount"));
                        hashMap.put("quantity", jsonProduct.getString("quantity"));
                        hashMap.put("createdAt", jsonProduct.getString("createdAt"));
                        hashMap.put("modifiedAt", jsonProduct.getString("modifiedAt"));
                        hashMap.put("__v", jsonProduct.getString("__v"));
                        hashMap.put("orderType", "2");
                        isCsrachCard = "2";
//                        cancellationReturnType = "1";
                        arrayList.add(hashMap);
                    }
                }


                adapter.notifyDataSetChanged();
                adapterEachItem.notifyDataSetChanged();

                //if coupon applied
                if (jsonData.getString("couponApplied").equals("1")) {
                    b.rlPromo.setVisibility(View.VISIBLE);
                    b.tvPromo.setText(getString(R.string.promo) + " - (" + jsonData.getString("couponCode") + ")");

                    if (jsonData.getString("discount").isEmpty()) {
                        DecimalFormat decimalFormat = new DecimalFormat("#.####");
                        String formattedValue = decimalFormat.format(AppUtils.returnDouble(jsonData.getString("flat")));
                        b.tvDiscount.setText(getString(R.string.youSaved) + " " + getString(R.string.rupeeSymbol) + formattedValue);

                    } else {
                        DecimalFormat decimalFormat = new DecimalFormat("#.####");
                        String formattedValue = decimalFormat.format(AppUtils.returnDouble(jsonData.getString("discount")));
                        b.tvDiscount.setText(getString(R.string.youSaved) + " " + getString(R.string.rupeeSymbol) + formattedValue);
                    }

                } else {
                    b.rlPromo.setVisibility(View.GONE);
                }


                if (!jsonData.getString("taxesAndCharges").isEmpty())
                    b.tvTax.setText(getString(R.string.rupeeSymbol) + jsonData.getString("taxesAndCharges"));

                if (!jsonData.getString("delivery").isEmpty()) {
                    b.tvDeliveryFee.setText(getString(R.string.rupeeSymbol) + jsonData.getString("delivery"));
                }


                //b.tvOrderAddress.setText(jsonData.getString("completeAddress"));
                b.tvPhoneNumber.setText(jsonData.getString("phoneNumber"));
                b.tvDate.setText(AppUtils.changeDateFormat2(jsonData.getString("createdAt")));
                merchantMobile = jsonData.getString("phoneNumber");
                if (jsonData.getString("status").equals("1")) {
                    b.tvStatus.setText(getString(R.string.pending));
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.orange));
                    if (cancellationReturnType.equalsIgnoreCase("1")) {
                        b.tvCancelOrder.setVisibility(View.GONE);
                    } else if (cancellationReturnType.equalsIgnoreCase("2")) {
                        if (!jsonData.getString("cancelTime").equalsIgnoreCase("")) {
                            startTimer(jsonData.getString("createdAt"), jsonData.getString("currentDateTime"), jsonData.getString("cancelTime"));
                        } else {
                            b.tvCancelOrder.setVisibility(View.GONE);
                        }

                    } else if (cancellationReturnType.equalsIgnoreCase("3")) {
                        b.tvCancelOrder.setVisibility(View.VISIBLE);
                    }

                    if (jsonData.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.fabTrack.setVisibility(View.GONE);
                    } else {
                        b.fabTrack.setVisibility(View.VISIBLE);
                    }
                }
                else if (jsonData.getString("status").equals("2")) {
                    b.tvStatus.setText(getString(R.string.accepted));
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    b.tvCancelOrder.setVisibility(View.GONE);
                    if (jsonData.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.fabTrack.setVisibility(View.GONE);
                    } else {
                        b.fabTrack.setVisibility(View.VISIBLE);
                    }

                }
                else if (jsonData.getString("status").equals("3")) {

                    if (jsonData.getString("cancelledBy").equals("1")) {
                        b.tvStatus.setText(getString(R.string.cancelled));
                    } else {
                        b.tvStatus.setText(getString(R.string.rejected));
                    }
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.red));
                    b.tvCancelOrder.setVisibility(View.GONE);
                    b.fabTrack.setVisibility(View.GONE);
                } else if (jsonData.getString("status").equals("4")) {
                    b.tvStatus.setText(getString(R.string.reachedAtPickup));
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    b.tvCancelOrder.setVisibility(View.GONE);
                    if (jsonData.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.fabTrack.setVisibility(View.GONE);
                    } else {
                        b.fabTrack.setVisibility(View.VISIBLE);
                    }
                } else if (jsonData.getString("status").equals("5")) {
                    b.tvStatus.setText(getString(R.string.pickedUp));
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    b.tvCancelOrder.setVisibility(View.GONE);
                    if (jsonData.getString("driverLatitude").equalsIgnoreCase("")) {
                        b.fabTrack.setVisibility(View.GONE);
                    } else {
                        b.fabTrack.setVisibility(View.VISIBLE);
                    }
                } else if (jsonData.getString("status").equals("6")) {
                    b.tvStatus.setText(getString(R.string.delivered));
                    b.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    if (orderType.equalsIgnoreCase("1")) {
                        b.llRepeatOrder.setVisibility(View.VISIBLE);
                    } else {
                        b.llRepeatOrder.setVisibility(View.GONE);

                    }
                    b.rldownloadinnvice.setVisibility(View.GONE);
                    b.fabTrack.setVisibility(View.GONE);
                    b.tvRateNow.setVisibility(View.VISIBLE);
                    b.viewRateNow.setVisibility(View.VISIBLE);
                    if (cancellationReturnType.equalsIgnoreCase("1")) {
                        b.tvScratchCardNow.setVisibility(View.GONE);
                    } else if (cancellationReturnType.equalsIgnoreCase("2")) {
                        b.tvScratchCardNow.setVisibility(View.GONE);
                    } else {
                        if (jsonData.getString("isPointAccepted").equals("0")) {
                            b.tvScratchCardNow.setVisibility(View.VISIBLE);
                        } else {
                            b.tvScratchCardNow.setVisibility(View.GONE);
                        }
                    }
                } else {
//                    if (isCsrachCard.equalsIgnoreCase("2"))
//                    {
//                        b.tvScratchCardNow.setVisibility(View.GONE);
//                    }
//                    else
//                    {
//                        b.tvScratchCardNow.setVisibility(View.VISIBLE);
//                    }
                    if (cancellationReturnType.equalsIgnoreCase("1")) {
                        b.tvScratchCardNow.setVisibility(View.GONE);
                    } else if (cancellationReturnType.equalsIgnoreCase("2")) {
                        b.tvScratchCardNow.setVisibility(View.GONE);
                    } else {
                        if (jsonData.getString("isPointAccepted").equals("0")) {
                            b.tvScratchCardNow.setVisibility(View.VISIBLE);
                        } else {
                            b.tvScratchCardNow.setVisibility(View.GONE);
                        }
                    }
                    b.rldownloadinnvice.setVisibility(View.GONE);
                    b.fabTrack.setVisibility(View.GONE);
                }
                //1 => Wallet,   2 => Online,  3 => COD,   4=> GP, 5=>wallet+GP,6=>wallet+Online,7=>GP+Online, 8=>Wallet + GP+ Online (edited)

                String rupeeSymbol = getString(R.string.rupeeSymbol);
                String walletDeductAmt = " (" + rupeeSymbol+jsonData.getString("walletDeductedAmount") + ")";
                String onlineDeductAmt = " (" + rupeeSymbol+jsonData.getString("onlineDeductedAmount") + ")";
                String gpDeductAmt = " (" + jsonData.getString("gpDeductedQuantity") + "*" + rupeeSymbol+jsonData.getString("gpQuantitySellPrice")
                        + " = " + rupeeSymbol+jsonData.getString("gpDeductedAmount") + ")";

                switch (jsonData.getString("paymentMethod")) {

                    case "1":

                        b.tvPayment.setText(getString(R.string.wallet) + walletDeductAmt);

                        break;
                    case "2":

                        b.tvPayment.setText(getString(R.string.online) + onlineDeductAmt);
                        break;
                    case "3":

                        b.tvPayment.setText(getString(R.string.cash));
                        break;
                    case "4":

                        b.tvPayment.setText(getString(R.string.gp) + gpDeductAmt);
                        break;
                    case "5":

                        b.tvPayment.setText(getString(R.string.wallet) + walletDeductAmt + " + " + getString(R.string.gp) + gpDeductAmt);
                        break;
                    case "6":

                        b.tvPayment.setText(getString(R.string.wallet) + walletDeductAmt + " + " + getString(R.string.online) + onlineDeductAmt);
                        break;
                    case "7":

                        b.tvPayment.setText(getString(R.string.gp) + gpDeductAmt + " + " + getString(R.string.online) + onlineDeductAmt);
                        break;
                    case "8":

                        b.tvPayment.setText(getString(R.string.wallet) + walletDeductAmt + " + " + getString(R.string.gp) + gpDeductAmt + " + " + getString(R.string.online) + onlineDeductAmt);
                        break;
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderDetails), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.llRepeatOrder:

                hitRepeatOrderApi();

                break;
            case R.id.tvCancelOrder:
                hitCancelOrderApi(orderId);
                break;

            case R.id.tvCallStore:

                AppUtils.makeCall(mActivity, merchantMobile);

                break;

            case R.id.tvScratchCardNow:

                showAlert();

                break;

            case R.id.fabTrack:

                startActivity(new Intent(mActivity, LiveTrackingActivity.class).putExtra("orderId", orderId));

                break;

            case R.id.tvRateNow:
                startActivity(new Intent(mActivity, RateAndReviewActivity.class).putExtra("orderId", orderId));
                //showRateDialog("");

                break;

        }
    }

    private void showAlert() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_get_scratch);
        bottomSheetDialog.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);
        TextView tvCancel = bottomSheetDialog.findViewById(R.id.tvCancel);

        tvContinue.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            hitAcceptScratchApi();
        });

        tvCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

    }

    private void hitAcceptScratchApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.acceptPoints, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseScratch(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseScratch(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            AppUtils.showMessageDialog(mActivity, getString(R.string.orderDetails), jsonObject.getString(AppConstants.resMsg), 2);
            hitGetOrderDetailApi();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitRepeatOrderApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.repeatOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseRepeatOrder(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseRepeatOrder(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                startActivity(new Intent(mActivity, CartActivity.class));

            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

                AppUtils.showToastSort(mActivity, jsonObject.getString(AppConstants.resMsg));

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderDetails), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;
        private boolean isExpanded = false;

        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_order_menu, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvItemName.setText(data.get(position).get("name"));
            holder.tvQuantity.setText(data.get(position).get("quantity"));
            holder.tvItemPrice.setText(getString(R.string.rupeeSymbol) + data.get(position).get("sellingPrice"));
            holder.tvTotal.setText(getString(R.string.rupeeSymbol) + data.get(position).get("priceWithQuantity"));
            if (data.get(position).get("orderType").equalsIgnoreCase("1")) {
                holder.rlLoadMore.setVisibility(View.VISIBLE);
                holder.lnMoreData.setVisibility(View.GONE);
                holder.tvLoadMoreData.setText(getString(R.string.showMore));
                holder.ivArrow.setRotation(180f);
                holder.setProductDetails(data, position);

                if (holder.tvDescription.getText().toString().equalsIgnoreCase("")&&
                        holder.tvWeight.getText().toString().equalsIgnoreCase("")&&
                        holder.tvSpecialFeature.getText().toString().equalsIgnoreCase("")&&
                        holder.tvColor.getText().toString().equalsIgnoreCase("")&&
                        holder.tvDisclaimer.getText().toString().equalsIgnoreCase("")&&
                        holder.tvContent.getText().toString().equalsIgnoreCase("")&&
                        holder.tvIngredients.getText().toString().equalsIgnoreCase("")&&
                        holder.tvSeller.getText().toString().equalsIgnoreCase("")&&
                        holder.tvCountryOfOrigin.getText().toString().equalsIgnoreCase("")&&
                        holder.tvFssaiLicense.getText().toString().equalsIgnoreCase("")&&
                        holder.tvShelfLife.getText().toString().equalsIgnoreCase("")&&
                        holder.tvManufacturer.getText().toString().equalsIgnoreCase("")&&
                        holder.tvMaterialTypeType.getText().toString().equalsIgnoreCase("")&&
                        holder.tvDistributorName.getText().toString().equalsIgnoreCase("")&&
                        holder.tvBatchNumber.getText().toString().equalsIgnoreCase("")&&
                        holder.tvExpiryDate.getText().toString().equalsIgnoreCase("")&&
                        holder.tvPackedType.getText().toString().equalsIgnoreCase("")&&
                        holder.tvBrand.getText().toString().equalsIgnoreCase("")&&
                       // holder.tvPrice.getText().toString().equalsIgnoreCase("")&&
                        holder.tvUnit.getText().toString().equalsIgnoreCase("")&&
                        holder.tvSize.getText().toString().equalsIgnoreCase("")

                )
                {
                    holder.rlLoadMore.setVisibility(View.GONE);
                }
                else
                {
                    holder.rlLoadMore.setVisibility(View.VISIBLE);
                }
                holder.rlLoadMore.setOnClickListener(v -> {
                    if (isExpanded) {
                        holder.lnMoreData.setVisibility(View.VISIBLE);
                        holder.tvLoadMoreData.setText(getText(R.string.showLess));
                        holder.ivArrow.setRotation(360f);
                    } else {
                        holder.lnMoreData.setVisibility(View.GONE);
                        holder.ivArrow.setRotation(180f);
                        holder.tvLoadMoreData.setText(R.string.showMore);
                    }
                    isExpanded = !isExpanded;
                });
            } else {
                holder.rlLoadMore.setVisibility(View.GONE);
            }


        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvItemName, tvQuantity, tvItemPrice, tvDescription, tvLoadMoreData, tvTotal, tvWeight, tvSpecialFeature, tvColor, tvDisclaimer, tvContent, tvIngredients, tvSeller, tvCountryOfOrigin, tvFssaiLicense, tvShelfLife, tvManufacturer, tvMaterialTypeType, tvDistributorName, tvBatchNumber, tvExpiryDate, tvPackedType, tvBrand, tvPrice, tvUnit, tvSize;
            LinearLayout lnMoreData;
            ImageView ivArrow;
            RelativeLayout rlSpecialFeature, rlSize, rlWeight, rlColor, rlDisclaimer, rlDescription, rlContent, rlIngredients, rlSeller, rlCountryOfOrigin, rlFssaiLicense, rlShelfLife, rlManufacturer, rlMaterialType, rlDistributorName, rlBatchNumber, rlExpiryDate, rlPackedType, rlBrand, rlPrice, rlUnit, rlLoadMore;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);


                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                ivArrow = itemView.findViewById(R.id.ivArrow);
                tvLoadMoreData = itemView.findViewById(R.id.tvLoadMoreData);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                lnMoreData = itemView.findViewById(R.id.lnMoreData);
                rlDescription = itemView.findViewById(R.id.rlDescription);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                rlSize = itemView.findViewById(R.id.rlSize);
                rlWeight = itemView.findViewById(R.id.rlWeight);
                tvWeight = itemView.findViewById(R.id.tvWeight);
                rlColor = itemView.findViewById(R.id.rlColor);
                tvColor = itemView.findViewById(R.id.tvColor);
                tvDisclaimer = itemView.findViewById(R.id.tvDisclaimer);
                tvContent = itemView.findViewById(R.id.tvContent);
                tvIngredients = itemView.findViewById(R.id.tvIngredients);
                tvSeller = itemView.findViewById(R.id.tvSeller);
                tvCountryOfOrigin = itemView.findViewById(R.id.tvCountryOfOrigin);
                tvFssaiLicense = itemView.findViewById(R.id.tvFssaiLicense);
                tvShelfLife = itemView.findViewById(R.id.tvShelfLife);
                tvManufacturer = itemView.findViewById(R.id.tvManufacturer);
                tvMaterialTypeType = itemView.findViewById(R.id.tv_material_type);
                tvDistributorName = itemView.findViewById(R.id.tvDistributorName);
                tvBatchNumber = itemView.findViewById(R.id.tvBatchNumber);
                tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
                tvPackedType = itemView.findViewById(R.id.tvPackedType);
                tvBrand = itemView.findViewById(R.id.tvBrand);
                tvUnit = itemView.findViewById(R.id.tvUnit);
                tvSize = itemView.findViewById(R.id.tvSize);
                rlDisclaimer = itemView.findViewById(R.id.rlDisclaimer);
                rlContent = itemView.findViewById(R.id.rlContent);
                rlIngredients = itemView.findViewById(R.id.rlIngredients);
                rlSeller = itemView.findViewById(R.id.rlSeller);
                rlCountryOfOrigin = itemView.findViewById(R.id.rlCountryOfOrigin);
                rlFssaiLicense = itemView.findViewById(R.id.rlFssaiLicense);
                rlShelfLife = itemView.findViewById(R.id.rlShelfLife);
                rlManufacturer = itemView.findViewById(R.id.rlManufacturer);
                rlMaterialType = itemView.findViewById(R.id.rlMaterialType);
                rlDistributorName = itemView.findViewById(R.id.rlDistributorName);
                rlBatchNumber = itemView.findViewById(R.id.rlBatchNumber);
                rlExpiryDate = itemView.findViewById(R.id.rlExpiryDate);
                rlPackedType = itemView.findViewById(R.id.rlPackedType);
                rlBrand = itemView.findViewById(R.id.rlBrand);

                rlUnit = itemView.findViewById(R.id.rlUnit);
                rlLoadMore = itemView.findViewById(R.id.rlLoadMore);
                rlSpecialFeature = itemView.findViewById(R.id.rlSpecialFeature);
                tvSpecialFeature = itemView.findViewById(R.id.tvSpecialFeature);
            }

            @SuppressLint("SuspiciousIndentation")
            private void setProductDetails(ArrayList<HashMap<String, String>> data, int position) {


                if (data.get(position).get("weight").equalsIgnoreCase("") || data.get(position).get("weight").equalsIgnoreCase(null) || data.get(position).get("weight").equalsIgnoreCase("null")) {
                    rlWeight.setVisibility(View.GONE);
                } else {
                    rlWeight.setVisibility(View.VISIBLE);
                    tvWeight.setText(data.get(position).get("weight"));
                }
                if (data.get(position).get("color").equalsIgnoreCase("") || data.get(position).get("color").equalsIgnoreCase(null) || data.get(position).get("color").equalsIgnoreCase("null")) {
                    rlColor.setVisibility(View.GONE);
                } else {
                    rlColor.setVisibility(View.VISIBLE);
                    tvColor.setText(data.get(position).get("color"));
                }
                if (data.get(position).get("specialFeature").equalsIgnoreCase("") || data.get(position).get("specialFeature").equalsIgnoreCase(null) || data.get(position).get("specialFeature").equalsIgnoreCase("null")) {
                    rlSpecialFeature.setVisibility(View.GONE);
                } else {
                    rlSpecialFeature.setVisibility(View.VISIBLE);
                    tvSpecialFeature.setText(data.get(position).get("specialFeature"));
                }

                if (data.get(position).get("size").equalsIgnoreCase("") || data.get(position).get("size").equalsIgnoreCase(null) || data.get(position).get("size").equalsIgnoreCase("null")) {
                    rlSize.setVisibility(View.GONE);
                } else {
                    rlSize.setVisibility(View.VISIBLE);
                    tvSize.setText(data.get(position).get("size"));
                }
                if (data.get(position).get("unit").equalsIgnoreCase("") || data.get(position).get("unit").equalsIgnoreCase(null) || data.get(position).get("unit").equalsIgnoreCase("null")) {
                    rlUnit.setVisibility(View.GONE);
                } else {
                    rlUnit.setVisibility(View.VISIBLE);
                    tvUnit.setText(data.get(position).get("unit") + " " + data.get(position).get("unitType"));
                }

                if (data.get(position).get("brand").equalsIgnoreCase("") || data.get(position).get("brand").equalsIgnoreCase(null) || data.get(position).get("brand").equalsIgnoreCase("null")) {
                    rlBrand.setVisibility(View.GONE);
                } else {
                    rlBrand.setVisibility(View.VISIBLE);
                    tvBrand.setText(data.get(position).get("brand"));
                }

                if (data.get(position).get("packedType").equalsIgnoreCase("") || data.get(position).get("packedType").equalsIgnoreCase(null) || data.get(position).get("packedType").equalsIgnoreCase("null")) {
                    rlPackedType.setVisibility(View.GONE);
                } else {
                    rlPackedType.setVisibility(View.VISIBLE);
                    tvPackedType.setText(data.get(position).get("packedType"));
                }


                if (data.get(position).get("expiryDate").equalsIgnoreCase("") || data.get(position).get("expiryDate").equalsIgnoreCase(null) || data.get(position).get("expiryDate").equalsIgnoreCase("null"))
                    rlExpiryDate.setVisibility(View.GONE);
                else
                    rlExpiryDate.setVisibility(View.VISIBLE);
                tvExpiryDate.setText(data.get(position).get("expiryDate"));
                if (data.get(position).get("batchNumber").equalsIgnoreCase("") || data.get(position).get("batchNumber").equalsIgnoreCase(null) || data.get(position).get("batchNumber").equalsIgnoreCase("null"))
                    rlBatchNumber.setVisibility(View.GONE);
                else
                    rlBatchNumber.setVisibility(View.VISIBLE);
                tvBatchNumber.setText(data.get(position).get("batchNumber"));
                if (data.get(position).get("distributorName").equalsIgnoreCase("") || data.get(position).get("distributorName").equalsIgnoreCase(null) || data.get(position).get("distributorName").equalsIgnoreCase("null"))
                    rlDistributorName.setVisibility(View.GONE);
                else
                    rlDistributorName.setVisibility(View.VISIBLE);
                tvDistributorName.setText(data.get(position).get("distributorName"));
                if (data.get(position).get("materialType").equalsIgnoreCase("") || data.get(position).get("materialType").equalsIgnoreCase(null) || data.get(position).get("materialType").equalsIgnoreCase("null"))
                    rlMaterialType.setVisibility(View.GONE);
                else
                    rlMaterialType.setVisibility(View.VISIBLE);
                tvMaterialTypeType.setText(data.get(position).get("materialType"));
                if (data.get(position).get("manufacturer").equalsIgnoreCase("") || data.get(position).get("manufacturer").equalsIgnoreCase(null) || data.get(position).get("manufacturer").equalsIgnoreCase("null"))
                    rlManufacturer.setVisibility(View.GONE);
                else
                    rlManufacturer.setVisibility(View.VISIBLE);
                tvManufacturer.setText(data.get(position).get("manufacturer"));
                if (data.get(position).get("shelfLife").equalsIgnoreCase("") || data.get(position).get("shelfLife").equalsIgnoreCase(null) || data.get(position).get("shelfLife").equalsIgnoreCase("null"))
                    rlShelfLife.setVisibility(View.GONE);
                else
                    rlShelfLife.setVisibility(View.VISIBLE);
                tvShelfLife.setText(data.get(position).get("shelfLife"));
                if (data.get(position).get("fssaiLicense").equalsIgnoreCase("") || data.get(position).get("fssaiLicense").equalsIgnoreCase(null) || data.get(position).get("fssaiLicense").equalsIgnoreCase("null"))
                    rlFssaiLicense.setVisibility(View.GONE);
                else
                    rlFssaiLicense.setVisibility(View.VISIBLE);
                tvFssaiLicense.setText(data.get(position).get("fssaiLicense"));
                if (data.get(position).get("countryOfOrigin").equalsIgnoreCase("") || data.get(position).get("countryOfOrigin").equalsIgnoreCase(null) || data.get(position).get("countryOfOrigin").equalsIgnoreCase("null"))
                    rlCountryOfOrigin.setVisibility(View.GONE);
                else
                    rlCountryOfOrigin.setVisibility(View.VISIBLE);
                tvCountryOfOrigin.setText(data.get(position).get("countryOfOrigin"));
                if (data.get(position).get("seller").equalsIgnoreCase("") || data.get(position).get("seller").equalsIgnoreCase(null) || data.get(position).get("seller").equalsIgnoreCase("null"))
                    rlSeller.setVisibility(View.GONE);
                else
                    rlSeller.setVisibility(View.VISIBLE);
                tvSeller.setText(data.get(position).get("seller"));
                if (data.get(position).get("ingredients").equalsIgnoreCase("") || data.get(position).get("ingredients").equalsIgnoreCase(null) || data.get(position).get("ingredients").equalsIgnoreCase("null"))
                    rlIngredients.setVisibility(View.GONE);
                else
                    rlIngredients.setVisibility(View.VISIBLE);
                tvIngredients.setText(data.get(position).get("ingredients"));
                if (data.get(position).get("content").equalsIgnoreCase("") || data.get(position).get("content").equalsIgnoreCase(null) || data.get(position).get("content").equalsIgnoreCase("null"))
                    rlContent.setVisibility(View.GONE);
                else
                    rlContent.setVisibility(View.VISIBLE);
                tvContent.setText(data.get(position).get("content"));

                if (data.get(position).get("disclaimer").equalsIgnoreCase("") || data.get(position).get("disclaimer").equalsIgnoreCase(null) || data.get(position).get("disclaimer").equalsIgnoreCase("null"))
                    rlDisclaimer.setVisibility(View.GONE);
                else
                    rlDisclaimer.setVisibility(View.VISIBLE);
                tvDisclaimer.setText(data.get(position).get("disclaimer"));
                if (data.get(position).get("description").equalsIgnoreCase("") ||
                        data.get(position).get("description").equalsIgnoreCase(null) ||
                        data.get(position).get("description").equalsIgnoreCase("null"))
                    rlDescription.setVisibility(View.GONE);
                else
                    rlDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(data.get(position).get("description"));

            }
        }

    }

    private class AdapterEachItem extends RecyclerView.Adapter<AdapterEachItem.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;
        private boolean isExpanded = false;

        private AdapterEachItem(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public AdapterEachItem.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_order_menu_each_item_price, viewGroup, false);
            return new AdapterEachItem.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull AdapterEachItem.MyViewHolder holder, final int position) {
            holder.tvItemName.setText(data.get(position).get("name"));
            holder.tvQuantity.setText(data.get(position).get("quantity"));
            holder.tvItemPrice.setText(getString(R.string.rupeeSymbol) + data.get(position).get("sellingPrice"));
            holder.tvTotal.setText(getString(R.string.rupeeSymbol) + data.get(position).get("priceWithQuantity"));
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvItemName, tvQuantity, tvItemPrice, tvTotal;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
                tvTotal = itemView.findViewById(R.id.tvTotal);

            }
        }

    }


    private void hitCancelOrderApi(String orderId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.CancelOrder, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCancelOrder(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
            }
        });
    }

    private void parseCancelOrder(JSONObject response) {

        try {
            JSONObject jsonObject = new JSONObject(response.toString());

//            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
//
//                AppUtils.showMessageDialog(mActivity, getString(R.string.orderHistory),
//                        jsonObject.getString(AppConstants.resMsg), 1);
//
//            } else {
//                AppUtils.showMessageDialog(mActivity, getString(R.string.orderHistory),
//                        jsonObject.getString(AppConstants.resMsg), 2);
//            }
            AppUtils.showMessageDialog(mActivity, getString(R.string.orderHistory),
                    jsonObject.getString("message"), 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startTimer(String createdAt, String currentDateTime, String cancelTimeSecond) {


        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            Date date1 = format.parse(createdAt);
            Date date2 = format.parse(currentDateTime);

            Calendar gcal = new GregorianCalendar();
            gcal.setTime(date1);
            gcal.add(Calendar.SECOND, AppUtils.returnInt(cancelTimeSecond));
            date1 = gcal.getTime();

            long differenceInMilliSeconds = date1.getTime() - date2.getTime();

            long differenceInSeconds = (differenceInMilliSeconds) / 1000;


            countDownTimer = new CountDownTimer(differenceInSeconds * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    // Update the UI with the remaining time
                    long secondsRemaining = millisUntilFinished / 1000;

                    b.tvCancelOrder.setVisibility(View.VISIBLE);

                    //  timerTextView.setText("Time Remaining: " + secondsRemaining + " seconds");
                }

                public void onFinish() {
                    // Timer finished, you can perform any actions needed here
                    // timerTextView.setText("Timer Finished!");


                    b.tvCancelOrder.setVisibility(View.GONE);


                }
            }.start();

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        hitGetOrderDetailApi();
        super.onResume();
    }
}