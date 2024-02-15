package code.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityRequestOrderListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class RequestOrderListActivity extends BaseActivity implements View.OnClickListener {
    ActivityRequestOrderListBinding binding;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayListCheck = new ArrayList<>();
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        inits();
    }

    private void inits() {
        binding.header.tvHeader.setText(getString(R.string.requestOrder));
        binding.header.ivBack.setOnClickListener(this);
        adapter = new Adapter(arrayList);
        binding.rvList.setAdapter(adapter);
        hitGetRequestOrderListApi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
        }
    }

    private void hitGetRequestOrderListApi() {

        WebServices.getApi(mActivity, AppUrls.RequestUserOrderList, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseRequestOrder(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
            }
        });
    }

    private void parseRequestOrder(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("document", jsonObject1.getString("document"));
                    hashMap.put("status", jsonObject1.getString("status"));
                    hashMap.put("createdAt", jsonObject1.getString("createdAt"));
                    arrayList.add(hashMap);
                }

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.requestOrder),
                        jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_request_order_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.setIsRecyclable(false);
            AppUtils.loadPicassoImage(data.get(position).get("document"), holder.ivImage);
            holder.tvStoreName.setText(data.get(position).get("name"));
            holder.tvDateTime.setText(data.get(position).get("createdAt"));
            if (data.get(position).get("status").equalsIgnoreCase("1")) {
                holder.tvStatus.setText(getString(R.string.pending));
            } else if (data.get(position).get("status").equalsIgnoreCase("2")) {
                holder.tvStatus.setText(getString(R.string.assign));
            }
            holder.ivImage.setOnClickListener(v -> {
                showImageInFull(holder.ivImage);
            });

            holder.itemView.setOnClickListener(v ->
            {
                hitGetOrderDetailApi(data.get(position).get("_id"),data.get(position).get("name"),
                        data.get(position).get("document"),data.get(position).get("createdAt"));

            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvStoreName, tvDateTime, tvStatus;
            ImageView ivImage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivImage);
                tvStoreName = itemView.findViewById(R.id.tvStoreName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
    private void hitGetOrderDetailApi(String requestOrderId,String name,String document,String createdAt) {

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

                parseOrderDetail(response,requestOrderId,name,document,createdAt);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseOrderDetail(JSONObject response, String requestOrderId, String name, String document, String createdAt) {
        arrayListCheck.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject json = jsonObject.getJSONObject("data");

                JSONArray jsonData = json.getJSONArray("result");
                Intent intent = new Intent(mActivity, RequestOrderDetailsActivity.class);
                intent.putExtra("requestOrderId", requestOrderId);
                AppSettings.putString(AppSettings.merchantName,name);
                AppSettings.putString(AppSettings.document,document);
                AppSettings.putString(AppSettings.createdAt,createdAt);
                startActivity(intent);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), getString(R.string.orderNotAssign), 5);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void showImageInFull(ImageView ivImage) {

        Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        dialog.setCancelable(false);

        //Get the ImageView from the dialog layout
        ImageView dialogImage = dialog.findViewById(R.id.ivImage);
        ImageView ivBack = dialog.findViewById(R.id.ivBack);

        //Set the drawable to the dialog ImageView
        dialogImage.setImageDrawable(ivImage.getDrawable());

        //Set an OnClickListener for the dialog ImageView to dismiss the dialog
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Show the dialog
        dialog.show();

    }
    @Override
    protected void onResume() {
        hitGetRequestOrderListApi();
        super.onResume();
    }
}