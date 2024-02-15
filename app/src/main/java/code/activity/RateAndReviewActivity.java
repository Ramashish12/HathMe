package code.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityOrderDetailBinding;
import com.hathme.android.databinding.ActivityRateAndReviewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class RateAndReviewActivity extends BaseActivity implements View.OnClickListener {
    private ActivityRateAndReviewBinding b;
    String orderId = "", driverId = "";
    JSONArray jsonArray = new JSONArray();
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRateAndReviewBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.rateAndReview));
        b.tvRateNow.setOnClickListener(this);
        orderId = getIntent().getStringExtra("orderId");
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvRateNow:
                AppSettings.putString(AppSettings.isFromActivity,"1");
                startActivity(new Intent(mActivity,DriverRatingActivity.class).putExtra("orderId",orderId));
                break;
        }
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
                JSONObject driverData = jsonData.getJSONObject("driverData");
                b.tvDeliveryBoyName.setText(driverData.getString("name"));
                b.tvDeliveryBoyNumber.setText(driverData.getString("mobile"));
                b.ratingBar.setRating(AppUtils.returnFloat(jsonData.getString("rating")));
                AppUtils.loadPicassoImage(driverData.getString("profileImage"), b.ivImage);
                driverId = driverData.getString("_id");
                if (jsonData.getString("rating").equals(0) || jsonData.getString("rating").equalsIgnoreCase("0")) {
                    b.tvRateNow.setVisibility(View.VISIBLE);
                } else {
                    b.tvRateNow.setVisibility(View.GONE);
                }
                jsonArray = jsonData.getJSONArray("products");
                if (jsonData.getString("orderType").equalsIgnoreCase("2"))
                {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProduct = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("_id", jsonProduct.getString("_id"));
                        hashMap.put("requestOrderId", jsonProduct.getString("requestOrderId"));
                        hashMap.put("name", jsonProduct.getString("productName"));
                        hashMap.put("price", jsonProduct.getString("productAmount"));
                        hashMap.put("sellingPrice", jsonProduct.getString("productAmount"));
                        hashMap.put("priceWithQuantity", jsonProduct.getString("productAmount"));
                        hashMap.put("orderType", "2");
                        hashMap.put("quantity", jsonProduct.getString("quantity"));
                        hashMap.put("createdAt", jsonProduct.getString("createdAt"));
                        hashMap.put("modifiedAt", jsonProduct.getString("modifiedAt"));
                        hashMap.put("__v", jsonProduct.getString("__v"));
                        arrayList.add(hashMap);
                    }
                }
                else
                {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonProduct = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("productId", jsonProduct.getString("productId"));
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
                        hashMap.put("rating", jsonProduct.getString("rating"));
                        hashMap.put("imageOne", jsonProduct.getString("imageOne"));
                        hashMap.put("orderType", "1");

                        arrayList.add(hashMap);
                    }
                }


                adapter.notifyDataSetChanged();


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderDetails), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_rating_product_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvProductName.setText(data.get(position).get("name"));

            holder.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
            if (data.get(position).get("orderType").equalsIgnoreCase("1"))
            {
                holder.tvRateNow.setVisibility(View.VISIBLE);
                holder.ivImage.setVisibility(View.VISIBLE);
                holder.tvUnit.setVisibility(View.VISIBLE);
                holder.tvSellingPrice.setVisibility(View.VISIBLE);
                holder.ratingBar.setVisibility(View.VISIBLE);
                if (!data.get(position).get("sellingPrice").isEmpty()) {

                    holder.tvPrice.setPaintFlags(holder.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("sellingPrice"));
                }
                holder.tvDescription.setText(data.get(position).get("description"));

                if (data.get(position).get("unit").isEmpty()) {
                    holder.tvUnit.setVisibility(View.GONE);
                } else {
                    holder.tvUnit.setText(data.get(position).get("unit") + " " + data.get(position).get("unitType"));
                    holder.tvUnit.setVisibility(View.VISIBLE);
                }
                AppUtils.loadPicassoImage(data.get(position).get("imageOne"), holder.ivImage);
                holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("rating")));
                if (data.get(position).get("rating").equals(0) || data.get(position).get("rating").equalsIgnoreCase("0")) {
                    holder.tvRateNow.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRateNow.setVisibility(View.GONE);
                }
                holder.tvRateNow.setOnClickListener(v -> {
                    AppSettings.putString(AppSettings.isFromActivity,"2");
                    AppSettings.putString(AppSettings.productId,data.get(position).get("productId"));
                    startActivity(new Intent(mActivity,DriverRatingActivity.class).putExtra("orderId",orderId));
//                    showRateDialog(data.get(position).get("productId"), "2");
                });
            }
            else
            {
                holder.tvRateNow.setVisibility(View.GONE);
                holder.ivImage.setVisibility(View.GONE);
                holder.tvUnit.setVisibility(View.GONE);
                holder.tvSellingPrice.setVisibility(View.GONE);
                holder.ratingBar.setVisibility(View.GONE);

            }

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage;

            TextView tvProductName, tvPrice, tvDescription, tvUnit, tvSellingPrice, tvRateNow;

            RatingBar ratingBar;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                tvRateNow = itemView.findViewById(R.id.tvRateNow);


                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvUnit = itemView.findViewById(R.id.tvUnit);
                tvSellingPrice = itemView.findViewById(R.id.tvSellingPrice);
                ratingBar = itemView.findViewById(R.id.ratingBar);

            }
        }
    }

    @Override
    protected void onResume() {
        hitGetOrderDetailApi();
        super.onResume();
    }

    private void showRateDialog(String productId, String isFrom) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_rate_now);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        RatingBar ratingBar = bottomSheetDialog.findViewById(R.id.ratingBar);
        EditText etDescription = bottomSheetDialog.findViewById(R.id.etDescription);
        TextView tvSubmit = bottomSheetDialog.findViewById(R.id.tvSubmit);

        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ratingBar.getRating() == 0) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseRate));
                } else {
                    bottomSheetDialog.dismiss();
                     hitRatingApi(ratingBar.getRating(), etDescription.getText().toString().trim(), productId);
                }
            }
        });

    }

    private void hitRatingApi(float rating, String description, String productIds) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", orderId);
            jsonObject.put("productId", productIds);
            jsonObject.put("remark", description);
            jsonObject.put("rating", rating);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.orderReview, json, true, true, new WebServicesCallback() {

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

                hitGetOrderDetailApi();
                AppUtils.showResMsgToastSort(mActivity, jsonObject);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void hitRatingDriverApi(float rating, String description, String driverId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("orderId", orderId);
            jsonObject.put("driverId", driverId);
            jsonObject.put("remark", description);
            jsonObject.put("rating", rating);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.driverOrderRating, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

}