package code.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityRequestOrderDetailsBinding;

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

public class RequestOrderDetailsActivity extends BaseActivity implements View.OnClickListener {
    ActivityRequestOrderDetailsBinding binding;
    String requestOrderId = "", type = "";
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    ArrayList<Integer> checkedList = new ArrayList<>();
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        inite();
    }

    private void inite() {
        binding.tvHeader.setText(getString(R.string.requestOrderDetails));
        binding.ivBack.setOnClickListener(this);
        binding.tvSubmit.setOnClickListener(this);
        binding.ivRemoveItem.setOnClickListener(this);
        binding.ivImage.setOnClickListener(this);
        adapter = new Adapter(arrayList, binding.rvList);
        binding.rvList.setAdapter(adapter);
        requestOrderId = getIntent().getStringExtra("requestOrderId");

        hitGetOrderDetailApi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivImage:

                showImageInFull();

                break;

            case R.id.ivBack:
                finish();
                break;
            case R.id.ivRemoveItem:
                removeAlert(requestOrderId, type);
                break;
            case R.id.tvSubmit:
                // AppUtils.showToastSort(mActivity, getString(R.string.underProcess));
                startActivity(new Intent(mActivity, BucketCartActivity.class).putExtra("requestOrderId", requestOrderId));
                break;
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
        WebServices.postApi(mActivity, AppUrls.bucketProductList, json, true, true, new WebServicesCallback() {

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

                JSONArray jsonData = json.getJSONArray("result");
                for (int i = 0; i < jsonData.length(); i++) {
                    JSONObject jsonObject1 = jsonData.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("orderBucketId", jsonObject1.getString("orderBucketId"));
                    hashMap.put("productName", jsonObject1.getString("productName"));
                    hashMap.put("productAmount", jsonObject1.getString("productAmount"));

                    arrayList.add(hashMap);
                }
                binding.tvTotalBill.setText(getString(R.string.totalBill)+" "+getString(R.string.rupeeSymbol)+json.getString("totalAmount"));
                binding.lnProfileDetails.setVisibility(View.VISIBLE);
                binding.tvName.setText(AppSettings.getString(AppSettings.merchantName));
                binding.tvDateTime.setText(AppSettings.getString(AppSettings.createdAt));
                AppUtils.loadPicassoImage(AppSettings.getString(AppSettings.document), binding.ivImage);
                binding.tvSubmit.setVisibility(View.VISIBLE);
                binding.views.setVisibility(View.VISIBLE);
            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.orderDetails), jsonObject.getString(AppConstants.resMsg), 1);
                binding.tvSubmit.setVisibility(View.GONE);
                binding.ivMenu.setVisibility(View.GONE);
                binding.ivRemoveItem.setVisibility(View.GONE);
                binding.lnProfileDetails.setVisibility(View.GONE);
                binding.views.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    //remove bucket item
    private void removeAlert(final String removeId, final String type) {
        new AlertDialog.Builder(mActivity)
                .setTitle(type.equals("1")?getString(R.string.removeItem):getString(R.string.removeItem)+"s")
                .setMessage(type.equals("1")?getString(R.string.areYouSureRemoveItem)+"?":getString(R.string.areYouSureRemoveItem)+"s?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    hitRemoveProductApi(removeId,type);
                })


                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void hitRemoveProductApi(String removeId, String type) {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("removeId", removeId);
            jsonObject.put("type", type);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.removeOrderItem, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseRemoveBucketItem(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });

    }


    @SuppressLint("SetTextI18n")
    private void parseRemoveBucketItem(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetOrderDetailApi();

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.requestOrderDetails), jsonObject.getString(AppConstants.resMsg), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;
        RecyclerView recyclerView;

        private Adapter(ArrayList<HashMap<String, String>> arrayList, RecyclerView recyclerView) {

            data = arrayList;
            this.recyclerView = recyclerView;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_request_order_menu, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            holder.setIsRecyclable(false);
//            if (data.get(position).get("sellingPrice").equalsIgnoreCase("") || data.get(position).get("sellingPrice").equalsIgnoreCase("0")) {
//                double price = AppUtils.returnDouble(data.get(position).get("price"));
//                double qty = AppUtils.returnDouble(data.get(position).get("productQuantity"));
//                double totalAmt = price * qty;
//                holder.tvTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//                holder.tvItemPrice.setText(getString(R.string.rupeeSymbol) + data.get(position).get("price"));
//            } else {
//                double sellingPrice = AppUtils.returnDouble(data.get(position).get("sellingPrice"));
//                double qty = AppUtils.returnDouble(data.get(position).get("productQuantity"));
//                double totalAmt = sellingPrice * qty;
//                holder.tvTotal.setText(getString(R.string.rupeeSymbol) + totalAmt);
//                holder.tvItemPrice.setText(getString(R.string.rupeeSymbol) + data.get(position).get("sellingPrice"));
//            }
            holder.tvItemPrice.setText(data.get(position).get("productAmount"));
            holder.tvItemName.setText(data.get(position).get("productName"));
            holder.tvTotal.setText(getString(R.string.rupeeSymbol)+data.get(position).get("productAmount"));
            holder.checkAll.setVisibility(checkedList.size() > 0 ? View.VISIBLE : View.GONE);
            holder.ivRemove.setVisibility(checkedList.size() > 0 ? View.GONE : View.VISIBLE);
            binding.ivRemoveItem.setVisibility(checkedList.size() > 0 ? View.VISIBLE : View.GONE);
            binding.ivMenu.setVisibility(checkedList.size() > 0 ? View.GONE : View.VISIBLE);
            holder.checkAll.setChecked(true);

            holder.ivRemove.setOnClickListener(v -> {
                type = "1";
                removeAlert(data.get(position).get("orderBucketId"), type);
            });
            binding.ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(mActivity, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_request, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.selectAll) {
                                type = "2";
                                if (checkedList.contains(holder.getAdapterPosition())) {
                                    checkedList.remove(Integer.valueOf(holder.getAdapterPosition()));
                                    if (checkedList.size() == 0) {
                                        notifyDataSetChanged();
                                    } else {
                                        notifyItemChanged(holder.getAdapterPosition());
                                    }
                                } else {
                                    checkedList.add(holder.getAdapterPosition());
                                    if (checkedList.size() == 1) {
                                        recyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                notifyDataSetChanged();
                                            }
                                        });
                                    } else {
                                        notifyItemChanged(holder.getAdapterPosition());
                                    }

                                }

                                popup.dismiss();
                            } else {
                            }
                            return false;
                        }
                    });
                    popup.show();//
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvQuantity, tvTotal, tvItemPrice;
            ImageView ivRemove;
            CheckBox checkAll;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                ivRemove = itemView.findViewById(R.id.ivRemove);
                checkAll = itemView.findViewById(R.id.checkAll);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvTotal = itemView.findViewById(R.id.tvTotal);
                tvItemPrice = itemView.findViewById(R.id.tvItemPrice);
            }
        }


    }

    private void showImageInFull() {

        Dialog dialog = new Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image);
        dialog.setCancelable(false);

        //Get the ImageView from the dialog layout
        ImageView dialogImage = dialog.findViewById(R.id.ivImage);
        ImageView ivBack = dialog.findViewById(R.id.ivBack);

        //Set the drawable to the dialog ImageView
        dialogImage.setImageDrawable(binding.ivImage.getDrawable());

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
}