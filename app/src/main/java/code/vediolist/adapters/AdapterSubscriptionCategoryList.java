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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterSubscriptionCategoryList extends RecyclerView.Adapter<AdapterSubscriptionCategoryList.MyViewHolder>{
Context context;
ArrayList<HashMap<String,String>> arraylistdata;
    public AdapterSubscriptionCategoryList(ArrayList<HashMap<String,String>> arraylistdata,Context context)
    {
       this.context = context;
       this.arraylistdata = arraylistdata;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_video_category,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HashMap<String,String>map=arraylistdata.get(position);
        holder.txtcategory.setText(map.get(Helper.Key_category));
        if (position==0)
        {
            holder.txtcategory.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.rlbg.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.select_color));
        }
        else
        {
            holder.txtcategory.setTextColor(ContextCompat.getColor(context,R.color.black));
            holder.rlbg.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.unselect_color));
        }
    }

    @Override
    public int getItemCount() {
        return arraylistdata.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtcategory;
        RelativeLayout rlbg;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtcategory = itemView.findViewById(R.id.tvCategory);
            rlbg = itemView.findViewById(R.id.rlBg);

        }
    }
}
