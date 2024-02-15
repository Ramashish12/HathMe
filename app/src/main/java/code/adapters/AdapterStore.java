package code.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

import code.activity.StoreActivity;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class AdapterStore extends RecyclerView.Adapter<AdapterStore.MyViewHolder> {

    ArrayList<HashMap<String, String>> data;

    Activity activity;


    public AdapterStore(ArrayList<HashMap<String, String>> arrayList, BaseActivity mActivity) {

        data = arrayList;
        activity = mActivity;
    }

    @NonNull
    @Override
    public AdapterStore.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_store, viewGroup, false);
        return new AdapterStore.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterStore.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.setIsRecyclable(false);

        holder.tvStoreName.setText(data.get(position).get("name"));
        holder.tvRating.setText(AppUtils.Double1Digits(AppUtils.returnDouble(data.get(position).get("rating"))));
        AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivImage);
        holder.cvMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity, StoreActivity.class).putExtra("merchantId", data.get(position).get("merchantId")));
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cvMain;

        ImageView ivImage;

        TextView tvStoreName, tvRating, tvDistance;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cvMain = itemView.findViewById(R.id.cvMain);

            ivImage = itemView.findViewById(R.id.ivImage);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistance = itemView.findViewById(R.id.tvDistance);
        }
    }
}
