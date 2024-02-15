package code.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import code.activity.ItemDetailActivity;
import code.common.OnItemClickListener;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class AdapterItem extends RecyclerView.Adapter<AdapterItem.MyViewHolder> {

    ArrayList<HashMap<String, String>> data;

    Activity activity;

    OnItemClickListener onItemClickListener;

    public AdapterItem(ArrayList<HashMap<String, String>> arrayList, BaseActivity mActivity, OnItemClickListener onItemClickListener) {

        data = arrayList;
        activity = mActivity;
        this.onItemClickListener = onItemClickListener;

    }

    @NonNull
    @Override
    public AdapterItem.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterItem.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.tvItemName.setText(data.get(position).get("name"));
        holder.tvDescription.setText(data.get(position).get("description"));
        if (data.get(position).get("sellingPrice").equalsIgnoreCase("")) {
            holder.tvPrice.setText(activity.getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
            holder.tvSellingPrice.setVisibility(View.GONE);
        } else {
            data.get(position).get("sellingPrice");
            holder.tvSellingPrice.setVisibility(View.VISIBLE);
            holder.tvSellingPrice.setText(activity.getString(R.string.rupeeSymbol) + " " + data.get(position).get("sellingPrice"));
            holder.tvPrice.setText(activity.getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));
            holder.tvPrice.setPaintFlags(holder.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }


        AppUtils.loadPicassoImage(data.get(position).get("productImageOne"), holder.ivImage);

        holder.ratingBar.setRating(AppUtils.returnFloat(data.get(position).get("rating")));
        holder.tvRatingCount.setText("" + AppUtils.roundOff2Digit("" + AppUtils.returnFloat(data.get(position).get("rating"))));
        if (data.get(position).get("productQuantity").equalsIgnoreCase("") || Objects.requireNonNull(data.get(position).get("productQuantity")).equalsIgnoreCase("0") || data.get(position).get("productQuantity").isEmpty()) {
            holder.rlOutOfStock.setVisibility(View.VISIBLE);
            holder.rlAdd.setVisibility(View.GONE);
            holder.tvAdd.setVisibility(View.GONE);
        } else {
            holder.rlOutOfStock.setVisibility(View.GONE);
            holder.rlAdd.setVisibility(View.VISIBLE);
            holder.tvAdd.setVisibility(View.VISIBLE);
            if (!data.get(position).get("quantity").isEmpty() && !data.get(position).get("quantity").equals("0")) {
                holder.tvQuantity.setText(data.get(position).get("quantity"));
                holder.rlAdd.setVisibility(View.VISIBLE);
                holder.tvAdd.setVisibility(View.GONE);

            } else {
                // StoreActivity.tvCartQty.setText("0");
                holder.rlAdd.setVisibility(View.GONE);
                holder.tvAdd.setVisibility(View.VISIBLE);
            }

        }

        holder.llMain.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, ItemDetailActivity.class)
                .putExtra("productId", data.get(position).get("productId"))
                .putExtra("merchantId", data.get(position).get("merchantId")).putExtra("productQuantity",
                        data.get(position).get("productQuantity"))));

        holder.tvAdd.setOnClickListener(view -> {
            if (data.get(position).get("productQuantity").contains("-")) {
                AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity), activity.getString(R.string.productQuantity), 4);
            } else {
                onItemClickListener.onItemClickListener(data.get(position).get("productId"), "1", data.get(position).get("productQuantity"));
                holder.tvQuantity.setText("1");
                holder.rlAdd.setVisibility(View.VISIBLE);
                holder.tvAdd.setVisibility(View.GONE);
            }


        });

        holder.ivPlus.setOnClickListener(view -> {

            int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
            qty = qty + 1;
            if (qty > Integer.valueOf(data.get(position).get("productQuantity"))) {
                AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity),activity.getString(R.string.only)+" "+
                        data.get(position).get("productQuantity")+" "+activity.getString(R.string.quantityAvailable), 4);
            } else {
                holder.tvQuantity.setText(String.valueOf(qty));
                onItemClickListener.onItemClickListener(data.get(position).get("productId"), String.valueOf(qty), data.get(position).get("productQuantity"));
            }
//                hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));
        });

        holder.ivMinus.setOnClickListener(view -> {

            int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
            qty = qty - 1;

//                if (qty>Integer.valueOf(data.get(position).get("productQuantity")) ) {
//                    AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity), activity.getString(R.string.productQuantity), 4);
//                } else {
            holder.tvQuantity.setText(String.valueOf(qty));
            onItemClickListener.onItemClickListener(data.get(position).get("productId"), String.valueOf(qty), data.get(position).get("productQuantity"));
            //  }
            // hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));

            if (qty == 0) {
                holder.rlAdd.setVisibility(View.GONE);
                holder.tvAdd.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llMain;

        ImageView ivImage, ivMinus, ivPlus;

        RelativeLayout rlAdd, rlOutOfStock;

        TextView tvAdd, tvItemName, tvPrice, tvDescription, tvQuantity, tvSellingPrice, tvRatingCount;

        RatingBar ratingBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            llMain = itemView.findViewById(R.id.llMain);

            ivImage = itemView.findViewById(R.id.ivImage);
            ivMinus = itemView.findViewById(R.id.ivMinus);
            ivPlus = itemView.findViewById(R.id.ivPlus);

            rlAdd = itemView.findViewById(R.id.rlAdd);
            rlOutOfStock = itemView.findViewById(R.id.rlOutOfStock);
            tvRatingCount = itemView.findViewById(R.id.tvRatingCount);

            tvAdd = itemView.findViewById(R.id.tvAdd);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSellingPrice = itemView.findViewById(R.id.tvSellingPrice);

            ratingBar = itemView.findViewById(R.id.ratingBar);

        }
    }

    private double getPrice(String price, String offerPrice) {

        double value = 0;
//
//        if (sellingPrice.isEmpty() && offerPrice.isEmpty()) {
//            value = price;
//        } else if (offerPrice.isEmpty()) {
//            value = sellingPrice;
//        } else {
//            value = offerPrice;
//        }
        value = Double.valueOf(price) - Double.valueOf(offerPrice);
        return value;

    }
}

