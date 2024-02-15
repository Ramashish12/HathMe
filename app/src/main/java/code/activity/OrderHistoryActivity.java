package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityOrderHistoryBinding;

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

public class OrderHistoryActivity extends BaseActivity implements View.OnClickListener {

    ActivityOrderHistoryBinding b;

    Adapter adapter;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        b.header.tvHeader.setText(getString(R.string.orderHistory));
        b.header.ivBack.setOnClickListener(v -> onBackPressed());


        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetOrderHistoryApi();

    }

    private void hitGetOrderHistoryApi() {

        WebServices.getApi(mActivity, AppUrls.orderList, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseOrderList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseOrderList(JSONObject response) {

        arrayList.clear();

        try {

            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderId", jsonObject1.getString("orderId"));
                    hashMap.put("products", jsonObject1.getString("products"));
                    hashMap.put("createdAt", jsonObject1.getString("createdAt"));
                    hashMap.put("storeName", jsonObject1.getString("storeName"));
//                    hashMap.put("orderNo", jsonObject1.getString("orderNo"));
                    hashMap.put("merchantAddress", jsonObject1.getString("merchantAddress"));
                    hashMap.put("finalAmount", jsonObject1.getString("finalAmount"));
                    hashMap.put("discount", jsonObject1.getString("discount"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    hashMap.put("status", jsonObject1.getString("status"));
                    hashMap.put("orderOtp", jsonObject1.getString("orderOtp"));
                    hashMap.put("orderType", jsonObject1.getString("orderType"));
                    hashMap.put("orderNo", jsonObject1.getString("orderNumber"));
                    hashMap.put("cancelledBy", jsonObject1.getString("cancelledBy"));
                    hashMap.put("imagesBussiness", jsonObject1.getString("imagesBussiness"));
                    if (jsonObject1.getString("orderType").equalsIgnoreCase("2")) {
                        hashMap.put("document", jsonObject1.getString("document"));
                    }
                    hashMap.put("isBucketOrderStatus", jsonObject1.getString("isBucketOrderStatus"));

                    arrayList.add(hashMap);
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderHistory),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_order, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            holder.tvStoreName.setText(data.get(position).get("storeName"));
            holder.tvAddress.setText(data.get(position).get("merchantAddress"));
            holder.tvOrderNo.setText(getString(R.string.orderNo) + " : " + data.get(position).get("orderNo"));
            double totalAmt = AppUtils.returnDouble(data.get(position).get("finalAmount"));
            holder.tvAmount.setText(getString(R.string.rupeeSymbol) + "" + totalAmt);
            if (!data.get(position).get("rating").equals("0") && !data.get(position).get("rating").isEmpty()) {
                holder.tvRating.setText(data.get(position).get("tvYouRated"));
                holder.tvYouRated.setVisibility(View.VISIBLE);
                holder.tvRating.setVisibility(View.VISIBLE);
            } else {
                holder.tvYouRated.setVisibility(View.GONE);
                holder.tvRating.setVisibility(View.GONE);
            }

            try {
                JSONArray jsonArray = new JSONArray(data.get(position).get("products"));
                JSONArray jsonArrayBusiness = new JSONArray(data.get(position).get("imagesBussiness"));
                if (data.get(position).get("orderType").equalsIgnoreCase("1")) {
                    String items = "";

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if (items.isEmpty())
                            items = jsonObject.getString("quantity") + " X " + jsonObject.getString("name");
                        else
                            items = items + "\n" + jsonObject.getString("quantity") + " X " + jsonObject.getString("name");

                        holder.tvItemName.setText(items);

                        if (i == 0) {
                            AppUtils.loadPicassoImage(jsonObject.getString("imageOne"), holder.ivImage);
                        }

                    }

                } else {
                    String items = "";

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        if (items.isEmpty())
                            items = jsonObject.getString("quantity") + " X " + jsonObject.getString("productName");
                        else
                            items = items + "\n" + jsonObject.getString("quantity") + " X " + jsonObject.getString("productName");

                        holder.tvItemName.setText(items);

//

                    }
                    for (int i = 0; i < jsonArrayBusiness.length(); i++) {
                        JSONObject jsonObjectBusiness = jsonArrayBusiness.getJSONObject(i);
                        if (i == 0) {
                            AppUtils.loadPicassoImage(jsonObjectBusiness.getString("imageUrl"), holder.ivImage);
                        }
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.tvDateTime.setText(AppUtils.changeDateFormat2(data.get(position).get("createdAt")));

            holder.cvMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, OrderDetailActivity.class);
                    intent.putExtra("orderId", data.get(position).get("orderId"));
                    intent.putExtra("orderType", data.get(position).get("orderType"));
                    startActivity(intent);
                }
            });

            switch (data.get(position).get("status")) {

                case "1":
                    holder.tvStatus.setText(getString(R.string.pending));
                    holder.tvReOrder.setVisibility(View.GONE);
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.orange));
                    break;
                case "2":
                    holder.tvStatus.setText(getString(R.string.accepted));
                    holder.tvReOrder.setVisibility(View.VISIBLE);
                    holder.tvReOrder.setText(getString(R.string.otp) + " - " + data.get(position).get("orderOtp"));
                    holder.tvReOrder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    break;
                case "3":

                    if (data.get(position).get("cancelledBy").equals("1")){
                        holder.tvStatus.setText(getString(R.string.cancelled));
                    }
                    else{
                        holder.tvStatus.setText(getString(R.string.rejected));
                    }

                    holder.tvReOrder.setVisibility(View.GONE);
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.redDark));
                    break;
                case "4":
                    holder.tvStatus.setText(getString(R.string.reachedAtPickup));
                    holder.tvReOrder.setVisibility(View.VISIBLE);
                    holder.tvReOrder.setText(getString(R.string.otp) + " - " + data.get(position).get("orderOtp"));
                    holder.tvReOrder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    break;
                case "5":
                    holder.tvStatus.setText(getString(R.string.pickedUp));
                    holder.tvReOrder.setVisibility(View.VISIBLE);
                    holder.tvReOrder.setText(getString(R.string.otp) + " - " + data.get(position).get("orderOtp"));
                    holder.tvReOrder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    break;
                case "6":
                    if (data.get(position).get("orderType").equalsIgnoreCase("1")) {
                        holder.tvStatus.setText(getString(R.string.delivered));
                        holder.tvReOrder.setVisibility(View.VISIBLE);
                        holder.tvReOrder.setText(getString(R.string.reorder));
                        holder.tvReOrder.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_re_order, 0, 0, 0);
                    } else {
                        holder.tvStatus.setText(getString(R.string.delivered));
                        holder.tvReOrder.setVisibility(View.GONE);
                    }
                    holder.tvStatus.setTextColor(ContextCompat.getColorStateList(mActivity, R.color.green));
                    break;

            }

            holder.tvReOrder.setOnClickListener(view -> {
                if (data.get(position).get("status").equals("6")) {
                    hitRepeatOrderApi(data.get(position).get("orderId"));
                }
            });

            //   hitGetOrderDetailApi(data.get(position).get("orderId"),holder.tvOrderNo);
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage;

            TextView tvStoreName, tvAddress, tvStatus, tvItemName, tvDateTime, tvAmount, tvYouRated, tvRating, tvReOrder, tvOrderNo;

            CardView cvMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                tvStoreName = itemView.findViewById(R.id.tvStoreName);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvYouRated = itemView.findViewById(R.id.tvYouRated);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvReOrder = itemView.findViewById(R.id.tvReOrder);

                cvMain = itemView.findViewById(R.id.cvMain);
                tvOrderNo = itemView.findViewById(R.id.tvOrderNo);

            }
        }
    }

    private void hitRepeatOrderApi(String orderId) {

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

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderHistory),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hitGetOrderDetailApi(final String orderId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("orderId", orderId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.orderDetail, json, false, false, new WebServicesCallback() {

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
                AppUtils.showToastSort(mActivity, jsonData.getString("orderNo"));
                //tvOrderNo.setText(jsonData.getString("orderNo"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        hitGetOrderHistoryApi();
        super.onResume();
    }
}