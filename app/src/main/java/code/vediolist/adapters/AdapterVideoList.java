package code.vediolist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUtils;
import code.vediolist.VideoDetailsActivity;
import code.vediolist.YourVideoActivity;

public class AdapterVideoList extends RecyclerView.Adapter<AdapterVideoList.MyViewHolder>{
    Context context;
    ArrayList<HashMap<String,String>> data;
    public AdapterVideoList(ArrayList<HashMap<String,String>> data, Context context)
    {
     this.context = context;
     this.data = data;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_video_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tvDateTime.setText(AppUtils.changeDateTimeFormat(data.get(position).get("createdAt")));
        AppUtils.loadPicassoImage(data.get(position).get("videoThumbnail"), holder.ivThumb);
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu((Activity)context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id== R.id.report){
                            popup.dismiss();
                        }
                        else
                        {}
                        return false;
                    }
                });
                popup.show();//
            }
        });
        holder.itemView.setOnClickListener(v -> {
            context.startActivity(new Intent(context, VideoDetailsActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb,ivMenu;
        TextView tvDesc,tvTotalView,tvDateTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvTotalView = itemView.findViewById(R.id.tvTotalView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}
