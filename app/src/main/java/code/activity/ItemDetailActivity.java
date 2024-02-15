package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityItemDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class ItemDetailActivity extends BaseActivity implements View.OnClickListener {

    private ActivityItemDetailBinding b;

    private Adapter adapter;
    ViewPagerAdapter pagerAdapter;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> countArrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> imageArrayList = new ArrayList<>();
    int totalQuantity = 0;
    private String productId = "", merchantId = "", productQuantity = "";

    private boolean isFav = false;
    private boolean isExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.rlSeeAll.setOnClickListener(this);
        b.tvAdd.setOnClickListener(this);
        b.ivPlus.setOnClickListener(this);
        b.ivMinus.setOnClickListener(this);
        b.header.ivFavorite.setOnClickListener(this);
        b.header.ivCart.setOnClickListener(this);
        adapter = new Adapter(arrayList);
        b.rvMoreItems.setAdapter(adapter);

        if (getIntent().getExtras() != null && getIntent().hasExtra("productId")) {
            productId = getIntent().getStringExtra("productId");
            merchantId = getIntent().getStringExtra("merchantId");
            productQuantity = getIntent().getStringExtra("productQuantity");
        }
        b.lnMoreData.setVisibility(View.GONE);
        b.tvLoadMoreData.setText(getString(R.string.showMore));
        b.ivArrow2.setRotation(180f);
        b.rlLoadMore.setOnClickListener(this);

        pagerAdapter = new ViewPagerAdapter(imageArrayList);
        b.viewPager.setAdapter(pagerAdapter);
        b.dot1.setViewPager(b.viewPager);

        hitGetProductDetailApi();

    }

    private void hitGetProductDetailApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();
        try {

            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.productDetailedById, json, true, true, new WebServicesCallback() {
            @Override
            public void OnJsonSuccess(JSONObject response) {
                parseProductDetailJson(response);
            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseProductDetailJson(JSONObject response) {

        arrayList.clear();
        imageArrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                isFav = jsonData.getString("favoriteStatus").equals("1");

                setFavorite();

//              AppUtils.loadPicassoImage(jsonData.getString("productImageOne"), b.ivImage);
                JSONArray jsonArrayImages = jsonData.getJSONArray("images");
                for (int i = 0; i < jsonArrayImages.length(); i++) {

                    JSONObject jsonImages = jsonArrayImages.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonImages.getString("_id"));
                    hashMap.put("imageUrl", jsonImages.getString("imageUrl"));
                    imageArrayList.add(hashMap);
                }

                b.tvItemName.setText(jsonData.getString("name"));
                if (jsonData.getString("description").equalsIgnoreCase("") ||
                        jsonData.getString("description").equalsIgnoreCase(null) ||
                        jsonData.getString("description").equalsIgnoreCase("null")) {
                    b.rlDescription.setVisibility(View.GONE);
                } else {
                    b.rlDescription.setVisibility(View.VISIBLE);
                    b.tvDescription.setText(jsonData.getString("description"));
                }
                if (!jsonData.getString("specialFeature").isEmpty()) {
                    b.tvSpecialFeature.setText(jsonData.getString("specialFeature"));
                    b.rlSpecialFeature.setVisibility(View.VISIBLE);
                } else {
                    b.rlSpecialFeature.setVisibility(View.GONE);
                }


                //extra added fields data

                setProductDetails(jsonData);
                if (b.tvDescription.getText().toString().equalsIgnoreCase("")&&
                        b.tvWeight.getText().toString().equalsIgnoreCase("")&&
                        b.tvSpecialFeature.getText().toString().equalsIgnoreCase("")&&
                        b.tvColor.getText().toString().equalsIgnoreCase("")&&
                        b.tvDisclaimer.getText().toString().equalsIgnoreCase("")&&
                        b.tvContent.getText().toString().equalsIgnoreCase("")&&
                        b.tvIngredients.getText().toString().equalsIgnoreCase("")&&
                        b.tvSeller.getText().toString().equalsIgnoreCase("")&&
                        b.tvCountryOfOrigin.getText().toString().equalsIgnoreCase("")&&
                        b.tvFssaiLicense.getText().toString().equalsIgnoreCase("")&&
                        b.tvShelfLife.getText().toString().equalsIgnoreCase("")&&
                        b.tvManufacturer.getText().toString().equalsIgnoreCase("")&&
                        b.tvMaterialType.getText().toString().equalsIgnoreCase("")&&
                        b.tvDistributorName.getText().toString().equalsIgnoreCase("")&&
                        b.tvBatchNumber.getText().toString().equalsIgnoreCase("")&&
                        b.tvExpiryDate.getText().toString().equalsIgnoreCase("")&&
                        b.tvPackedType.getText().toString().equalsIgnoreCase("")&&
                        b.tvBrand.getText().toString().equalsIgnoreCase("")&&
                        // holder.tvPrice.getText().toString().equalsIgnoreCase("")&&
                        b.tvUnit.getText().toString().equalsIgnoreCase("")&&
                        b.tvSize.getText().toString().equalsIgnoreCase("")

                )
                {
                    b.rlLoadMore.setVisibility(View.GONE);
                }
                else
                {
                    b.rlLoadMore.setVisibility(View.VISIBLE);
                }
                /*if (jsonData.getString("sellingPrice").isEmpty()) {
                    b.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("price"));
                    b.tvPrice.setText("");

                } else {
                    b.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("sellingPrice"));
                    b.tvPrice.setText(jsonData.getString("price"));
                    b.tvPrice.setPaintFlags(b.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                }

                if (!jsonData.getString("offerPrice").isEmpty()){
                    b.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("offerPrice"));
                    b.tvPrice.setText(jsonData.getString("sellingPrice"));
                    b.tvPrice.setPaintFlags(b.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                }*/
                if (productQuantity.equalsIgnoreCase("") || productQuantity.equalsIgnoreCase("0") || productQuantity.isEmpty()) {
                    b.rlOutOfStock.setVisibility(View.VISIBLE);
                    b.rlAdd.setVisibility(View.GONE);
                    b.tvAdd.setVisibility(View.GONE);
                } else {
                    b.rlOutOfStock.setVisibility(View.GONE);
                    b.rlAdd.setVisibility(View.VISIBLE);
                    b.tvAdd.setVisibility(View.VISIBLE);
                    if (jsonData.has("quantity") && !jsonData.getString("quantity").isEmpty() && !jsonData.getString("quantity").equals("0")) {
                        b.tvQuantity.setText(jsonData.getString("quantity"));
                        // b.header.tvCartQty.setText(jsonData.getString("quantity"));
                        b.rlAdd.setVisibility(View.VISIBLE);
                        b.tvAdd.setVisibility(View.GONE);
                    }
                }
                JSONArray jsonArray = jsonData.getJSONArray("moreProducts");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonProduct = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonProduct.getString("productId"));
                    hashMap.put("name", jsonProduct.getString("name"));
                    hashMap.put("productImageOne", jsonProduct.getString("images"));
                    hashMap.put("rating", jsonProduct.getString("rating"));
                    hashMap.put("price", jsonProduct.getString("price"));
                    hashMap.put("sellingPrice", jsonProduct.getString("sellingPrice"));
                    hashMap.put("preparationTime", jsonProduct.getString("preparationTime"));
                    hashMap.put("productQuantity", jsonProduct.getString("quantity"));

                    arrayList.add(hashMap);
                }
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.productNotFound), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
        pagerAdapter.notifyDataSetChanged();
        hitGetCartApi();
    }

    private void setProductDetails(JSONObject jsonData) {
        try {
            if (jsonData.getString("weight").equalsIgnoreCase("") || jsonData.getString("weight").equalsIgnoreCase(null) || jsonData.getString("weight").equalsIgnoreCase("null")) {
                b.rlWeight.setVisibility(View.GONE);
            } else {
                b.rlWeight.setVisibility(View.VISIBLE);
                b.tvWeight.setText(jsonData.getString("weight"));
            }
            if (jsonData.getString("color").equalsIgnoreCase("") || jsonData.getString("color").equalsIgnoreCase(null) || jsonData.getString("color").equalsIgnoreCase("null")) {
                b.rlColor.setVisibility(View.GONE);
            } else {
                b.rlColor.setVisibility(View.VISIBLE);
                b.tvColor.setText(jsonData.getString("color"));
            }
            if (jsonData.getString("size").equalsIgnoreCase("") || jsonData.getString("size").equalsIgnoreCase(null) || jsonData.getString("size").equalsIgnoreCase("null")) {
                b.rlSize.setVisibility(View.GONE);
            } else {
                b.rlSize.setVisibility(View.VISIBLE);
                b.tvSize.setText(jsonData.getString("size"));
            }
            if (jsonData.getString("unit").equalsIgnoreCase("") || jsonData.getString("unit").equalsIgnoreCase(null) || jsonData.getString("unit").equalsIgnoreCase("null")) {
                b.rlUnit.setVisibility(View.GONE);
            } else {
                b.rlUnit.setVisibility(View.VISIBLE);
                b.tvUnit.setText(jsonData.getString("unit") + " " + jsonData.getString("unitType"));
            }
            if (jsonData.getString("sellingPrice").equalsIgnoreCase("")
                    || jsonData.getString("sellingPrice").equalsIgnoreCase(null)) {
                b.rlPrice.setVisibility(View.VISIBLE);
                b.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("price"));
                b.tvSellingPrice.setVisibility(View.GONE);
            } else {
                b.rlPrice.setVisibility(View.VISIBLE);
                b.tvSellingPrice.setVisibility(View.VISIBLE);
                b.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("sellingPrice"));
                b.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + jsonData.getString("price"));
                b.tvPrice.setPaintFlags(b.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (jsonData.getString("brand").equalsIgnoreCase("") || jsonData.getString("brand").equalsIgnoreCase(null) || jsonData.getString("brand").equalsIgnoreCase("null")) {
                b.rlBrand.setVisibility(View.GONE);
            } else {
                b.rlBrand.setVisibility(View.VISIBLE);
                b.tvBrand.setText(jsonData.getString("brand"));
            }

            if (jsonData.getString("packedType").equalsIgnoreCase("") || jsonData.getString("packedType").equalsIgnoreCase(null) || jsonData.getString("packedType").equalsIgnoreCase("null")) {
                b.rlPackedType.setVisibility(View.GONE);
            } else {
                b.rlPackedType.setVisibility(View.VISIBLE);
                b.tvPackedType.setText(jsonData.getString("packedType"));
            }


            if (jsonData.getString("expiryDate").equalsIgnoreCase("") || jsonData.getString("expiryDate").equalsIgnoreCase(null) || jsonData.getString("expiryDate").equalsIgnoreCase("null"))
                b.rlExpiryDate.setVisibility(View.GONE);
            else
                b.rlExpiryDate.setVisibility(View.VISIBLE);
            b.tvExpiryDate.setText(jsonData.getString("expiryDate"));
            if (jsonData.getString("batchNumber").equalsIgnoreCase("") || jsonData.getString("batchNumber").equalsIgnoreCase(null) || jsonData.getString("batchNumber").equalsIgnoreCase("null"))
                b.rlBatchNumber.setVisibility(View.GONE);
            else
                b.rlBatchNumber.setVisibility(View.VISIBLE);
            b.tvBatchNumber.setText(jsonData.getString("batchNumber"));
            if (jsonData.getString("distributorName").equalsIgnoreCase("") || jsonData.getString("distributorName").equalsIgnoreCase(null) || jsonData.getString("distributorName").equalsIgnoreCase("null"))
                b.rlDistributorName.setVisibility(View.GONE);
            else
                b.rlDistributorName.setVisibility(View.VISIBLE);
            b.tvDistributorName.setText(jsonData.getString("distributorName"));
//            if (jsonData.getString("materialType").equalsIgnoreCase("") || jsonData.getString("materialType").equalsIgnoreCase(null) || jsonData.getString("materialType").equalsIgnoreCase("null"))
//                b.rlMaterialType.setVisibility(View.GONE);
//            else
//                b.rlMaterialType.setVisibility(View.VISIBLE);
//               b.tvMaterialType.setText(jsonData.getString("materialType"));
            if (jsonData.getString("manufacturer").equalsIgnoreCase("") || jsonData.getString("manufacturer").equalsIgnoreCase(null) || jsonData.getString("manufacturer").equalsIgnoreCase("null"))
                b.rlManufacturer.setVisibility(View.GONE);
            else
                b.rlManufacturer.setVisibility(View.VISIBLE);
            b.tvManufacturer.setText(jsonData.getString("manufacturer"));
            if (jsonData.getString("shelfLife").equalsIgnoreCase("") || jsonData.getString("shelfLife").equalsIgnoreCase(null) || jsonData.getString("shelfLife").equalsIgnoreCase("null"))
                b.rlShelfLife.setVisibility(View.GONE);
            else
                b.rlShelfLife.setVisibility(View.VISIBLE);
            b.tvShelfLife.setText(jsonData.getString("shelfLife"));
            if (jsonData.getString("fssaiLicense").equalsIgnoreCase("") || jsonData.getString("fssaiLicense").equalsIgnoreCase(null) || jsonData.getString("fssaiLicense").equalsIgnoreCase("null"))
                b.rlFssaiLicense.setVisibility(View.GONE);
            else
                b.rlFssaiLicense.setVisibility(View.VISIBLE);
            b.tvFssaiLicense.setText(jsonData.getString("fssaiLicense"));
            if (jsonData.getString("countryOfOrigin").equalsIgnoreCase("") || jsonData.getString("countryOfOrigin").equalsIgnoreCase(null) || jsonData.getString("countryOfOrigin").equalsIgnoreCase("null"))
                b.rlCountryOfOrigin.setVisibility(View.GONE);
            else
                b.rlCountryOfOrigin.setVisibility(View.VISIBLE);
            b.tvCountryOfOrigin.setText(jsonData.getString("countryOfOrigin"));
            if (jsonData.getString("seller").equalsIgnoreCase("") || jsonData.getString("seller").equalsIgnoreCase(null) || jsonData.getString("seller").equalsIgnoreCase("null"))
                b.rlSeller.setVisibility(View.GONE);
            else
                b.rlSeller.setVisibility(View.VISIBLE);
            b.tvSeller.setText(jsonData.getString("seller"));
            if (jsonData.getString("ingredients").equalsIgnoreCase("") || jsonData.getString("ingredients").equalsIgnoreCase(null) || jsonData.getString("ingredients").equalsIgnoreCase("null"))
                b.rlIngredients.setVisibility(View.GONE);
            else
                b.rlIngredients.setVisibility(View.VISIBLE);
            b.tvIngredients.setText(jsonData.getString("ingredients"));
            if (jsonData.getString("content").equalsIgnoreCase("") || jsonData.getString("content").equalsIgnoreCase(null) || jsonData.getString("content").equalsIgnoreCase("null"))
                b.rlContent.setVisibility(View.GONE);
            else
                b.rlContent.setVisibility(View.VISIBLE);
            b.tvContent.setText(jsonData.getString("content"));

            if (jsonData.getString("disclaimer").equalsIgnoreCase("") || jsonData.getString("disclaimer").equalsIgnoreCase(null) || jsonData.getString("disclaimer").equalsIgnoreCase("null"))
                b.rlDisclaimer.setVisibility(View.GONE);
            else
                b.rlDisclaimer.setVisibility(View.VISIBLE);
            b.tvDisclaimer.setText(jsonData.getString("disclaimer"));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void setFavorite() {


        if (isFav) {
            b.header.ivFavorite.setImageResource(R.drawable.ic_heart_colored);
        } else {
            b.header.ivFavorite.setImageResource(R.drawable.ic_heart_grey);

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.rlLoadMore:
                if (isExpanded) {
                    b.lnMoreData.setVisibility(View.VISIBLE);
                    b.tvLoadMoreData.setText(getText(R.string.showLess));
                    b.ivArrow2.setRotation(360f);
                } else {
                    b.lnMoreData.setVisibility(View.GONE);
                    b.ivArrow2.setRotation(180f);
                    b.tvLoadMoreData.setText(R.string.showMore);
                }
                isExpanded = !isExpanded;
                break;
            case R.id.rlSeeAll:

                startActivity(new Intent(mActivity, StoreActivity.class).putExtra("merchantId", merchantId).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                break;

            case R.id.tvAdd:
                if (productQuantity.contains("-")) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantityMinus), 4);
                } else {
                    hitAddToCartApi(productId, "1");
                    b.tvQuantity.setText("1");
                    // b.header.tvCartQty.setText("1");
                    b.rlAdd.setVisibility(View.VISIBLE);
                    b.tvAdd.setVisibility(View.GONE);
                }
                break;

            case R.id.ivPlus:

                int qty = AppUtils.returnInt(b.tvQuantity.getText().toString().trim());
                qty = qty + 1;
                if (qty > AppUtils.returnInt(productQuantity)) {
                    AppUtils.showMessageDialog(mActivity, getString(R.string.quantity),getString(R.string.only)+" "+
                            productQuantity+" "+getString(R.string.quantityAvailable), 4);
                   // AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
                } else {
                    b.tvQuantity.setText(String.valueOf(qty));
                    hitAddToCartApi(productId, String.valueOf(qty));
                }


                break;

            case R.id.ivMinus:

                qty = AppUtils.returnInt(b.tvQuantity.getText().toString().trim());
                qty = qty - 1;
                b.tvQuantity.setText(String.valueOf(qty));
                //  b.header.tvCartQty.setText(String.valueOf(qty));
                hitAddToCartApi(productId, String.valueOf(qty));

                if (qty == 0) {
                    b.rlAdd.setVisibility(View.GONE);
                    b.tvAdd.setVisibility(View.VISIBLE);
                }

                break;

            case R.id.ivFavorite:

                hitFavApi();

                break;

            case R.id.ivCart:

                startActivity(new Intent(mActivity, CartActivity.class));

                break;
        }
    }

    private void hitFavApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "";

        if (isFav) {

            url = AppUrls.deleteFavorite;
        } else {
            url = AppUrls.setFavorite;

        }

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseFavJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseFavJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                isFav = !isFav;
                setFavorite();

                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_more_item, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvName.setText(data.get(position).get("name"));
            holder.tvRating.setText(data.get(position).get("rating"));
            holder.tvPrepTime.setText(getTime(data.get(position).get("preparationTime")));
            // holder.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
           /* if (data.get(position).get("sellingPrice").equalsIgnoreCase("")
                    || data.get(position).get("sellingPrice").equalsIgnoreCase(null)) {
                holder.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
                holder.tvSellingPrice.setVisibility(View.GONE);
            } else {
                holder.tvSellingPrice.setVisibility(View.VISIBLE);
                holder.tvSellingPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("sellingPrice"));
                holder.tvPrice.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
                holder.tvPrice.setPaintFlags(holder.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }*/
            AppUtils.loadPicassoImage(data.get(position).get("productImageOne"), holder.ivImage);

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (data.get(position).get("productQuantity").equalsIgnoreCase("")) {

                    } else {
                        startActivity(new Intent(mActivity, ItemDetailActivity.class)
                                .putExtra("productId", data.get(position).get("productId"))
                                .putExtra("merchantId", data.get(position).get("merchantId")).putExtra("productQuantity",
                                        data.get(position).get("productQuantity")));
                        finish();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage;

            TextView tvName, tvRating, tvPrepTime/*tvPrice, tvSellingPrice*/;

            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);

                tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
                tvName = itemView.findViewById(R.id.tvName);
//                tvSellingPrice = itemView.findViewById(R.id.tvSellingPrice);
                tvRating = itemView.findViewById(R.id.tvRating);
//                tvPrice = itemView.findViewById(R.id.tvPrice);

                llMain = itemView.findViewById(R.id.llMain);
            }
        }
    }

    private String getTime(String preparationTime) {

        if (preparationTime.isEmpty()) return getString(R.string.na);
        else {
            int hours = (int) (AppUtils.returnFloat(preparationTime) / 60);
            int remainingMinutes = (int) (AppUtils.returnFloat(preparationTime) % 60);


            return hours < 10 ? "0" + hours + " : " + remainingMinutes + " " + getString(R.string.mins)
                    : hours + " : " + remainingMinutes + " " + getString(R.string.mins);
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

                parseAddToCartJson(response, quantity);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddToCartJson(JSONObject response, String quantity) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitGetCartApi();
            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

                showAlreadyCartDialog(jsonObject.getString(AppConstants.resMsg), quantity, productId);
            } else {
                b.header.tvCartQty.setText("0");
                hitClearCartApi(quantity, productId);
                AppUtils.showMessageDialog(mActivity, getString(R.string.addToCart), jsonObject.getString(AppConstants.resMsg), 2);
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
            b.rlAdd.setVisibility(View.GONE);
            b.tvAdd.setVisibility(View.VISIBLE);
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

            }
//
//            else
//                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);

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
                b.header.tvCartQty.setText("" + totalQuantity);


            } else {
                b.header.tvCartQty.setText("0");
                //  hitClearCartApi(quantity, productId);
//                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
//                        jsonObject.getString(AppConstants.resMsg), 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        hitGetProductDetailApi();
        hitGetCartApi();
        super.onResume();
    }

    private class ViewPagerAdapter extends PagerAdapter {

        ArrayList<HashMap<String, String>> data;


        private ViewPagerAdapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(mActivity.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.viewpager_item_layout, container, false);

            ImageView imageView = itemView.findViewById(R.id.imageView);
            AppUtils.loadPicassoImage(data.get(position).get("imageUrl"), imageView);
            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}