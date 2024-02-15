package code.vediolist.adapters;

import android.app.Activity;
import android.content.Context;
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

public class AdapterSubscriptionCategoryVideoList extends RecyclerView.Adapter<AdapterSubscriptionCategoryVideoList.MyViewHolder>{
    Context context;
    ArrayList<HashMap<String,String>> arrayList;
    ArrayList<Integer> arrimagelist;
    public AdapterSubscriptionCategoryVideoList(ArrayList<HashMap<String,String>> arrayList,ArrayList<Integer> arrimagelist, Context context)
    {
        this.context = context;
        this.arrayList = arrayList;
        this.arrimagelist = arrimagelist;
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
        HashMap<String,String>map=arrayList.get(position);
        holder.tvDesc.setText(map.get(Helper.Key_desc));
        holder.tvTotalView.setText(map.get(Helper.Key_view));
        holder.tvUploadDateTime.setText(map.get(Helper.Key_time));
        Glide.with(context)
                .load(arrimagelist.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.ivThumb);
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu((Activity)context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu4, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id== R.id.unsubscribe){
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
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb,ivMenu,ivProfileImage;
        TextView tvDesc,tvTotalView,tvUploadDateTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvTotalView = itemView.findViewById(R.id.tvTotalView);
            tvUploadDateTime = itemView.findViewById(R.id.tvUploadDateTime);
            ivMenu = itemView.findViewById(R.id.ivMenu);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);

        }
    }
}
