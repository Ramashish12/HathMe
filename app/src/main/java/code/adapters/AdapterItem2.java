package code.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.ItemDetailActivity;
import code.common.OnItemClickListener;
import code.utils.AppUtils;
import code.view.BaseActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterItem2 extends RecyclerView.Adapter<AdapterItem2.MyViewHolder> {
    ArrayList<HashMap<String, String>> data;
    Activity activity;
    OnItemClickListener onItemClickListener;
    public AdapterItem2(ArrayList<HashMap<String, String>> arrayList,
                        BaseActivity mActivity,OnItemClickListener onItemClickListener) {
        data = arrayList;
        activity = mActivity;
        this.onItemClickListener = onItemClickListener;

    }

    @NonNull
    @Override
    public AdapterItem2.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_item2, viewGroup, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterItem2.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.tvItemName.setText(data.get(position).get("name"));
        holder.tvDescription.setText(data.get(position).get("description"));
        holder.tvMerchantName.setText(data.get(position).get("merchetName"));
        holder.tvMerchantAddress.setText(data.get(position).get("MerchantAddress"));
        holder.tvPrice.setText(activity.getString(R.string.rupeeSymbol) + " " + data.get(position).get("price"));

        AppUtils.loadPicassoImage(data.get(position).get("productImageOne"), holder.ivImage);
        if (!data.get(position).get("quantity").isEmpty() && !data.get(position).get("quantity").equals("0")) {

            holder.tvQuantity.setText(data.get(position).get("quantity"));

            holder.rlAdd.setVisibility(View.VISIBLE);
            holder.tvAdd.setVisibility(View.GONE);

        } else {
            holder.rlAdd.setVisibility(View.GONE);
            holder.tvAdd.setVisibility(View.VISIBLE);
        }

        holder.llMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                activity.startActivity(new Intent(activity, ItemDetailActivity.class)
                        .putExtra("productId", data.get(position).get("productId"))
                        .putExtra("merchantId", data.get(position).get("merchantId"))
                        .putExtra("productQuantity", data.get(position).get("productQuantity")));
            }
        });



        holder.tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (data.get(position).get("productQuantity").contains("-"))
                {
                    AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity), activity.getString(R.string.productQuantityMinus), 4);
                }
                else
                {
                    onItemClickListener.onItemClickListener(data.get(position).get("productId"), "1",data.get(position).get("productQuantity"));
                    holder.tvQuantity.setText("1");
                    holder.rlAdd.setVisibility(View.VISIBLE);
                    holder.tvAdd.setVisibility(View.GONE);
                }


            }
        });

        holder.ivPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
                qty = qty + 1;
                if (qty>Integer.valueOf(data.get(position).get("productQuantity")) ) {
                    AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity), activity.getString(R.string.productQuantity), 4);
                } else {
                    holder.tvQuantity.setText(String.valueOf(qty));
                    onItemClickListener.onItemClickListener(data.get(position).get("productId"), String.valueOf(qty),data.get(position).get("productQuantity"));
                }
//                hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));
            }
        });

        holder.ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int qty = AppUtils.returnInt(holder.tvQuantity.getText().toString().trim());
                qty = qty - 1;

//                if (qty>Integer.valueOf(data.get(position).get("productQuantity")) ) {
//                    AppUtils.showMessageDialog(activity, activity.getString(R.string.quantity), activity.getString(R.string.productQuantity), 4);
//                } else {
                    holder.tvQuantity.setText(String.valueOf(qty));
                    onItemClickListener.onItemClickListener(data.get(position).get("productId"), String.valueOf(qty),data.get(position).get("productQuantity"));
                //}
                // hitAddToCartApi(data.get(position).get("productId"), String.valueOf(qty));

                if (qty == 0) {
                    holder.rlAdd.setVisibility(View.GONE);
                    holder.tvAdd.setVisibility(View.VISIBLE);
                }
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
        RelativeLayout rlAdd;

        TextView tvAdd, tvItemName, tvPrice, tvDescription, tvQuantity,tvMerchantName,tvMerchantAddress;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            llMain = itemView.findViewById(R.id.llMain);

            ivImage = itemView.findViewById(R.id.ivImage);
            ivMinus = itemView.findViewById(R.id.ivMinus);
            ivPlus = itemView.findViewById(R.id.ivPlus);

            rlAdd = itemView.findViewById(R.id.rlAdd);
            tvMerchantName = itemView.findViewById(R.id.tvMerchantName);
            tvMerchantAddress = itemView.findViewById(R.id.tvMerchantAddress);

            tvAdd = itemView.findViewById(R.id.tvAdd);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);

        }
    }

}

