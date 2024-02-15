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

public class AdapterShortVideosList extends RecyclerView.Adapter<AdapterShortVideosList.MyViewHolder>{
ArrayList<Integer> arrayList;
Context context;
ArrayList<HashMap<String,String>> arraylistdata;
    public AdapterShortVideosList(ArrayList<HashMap<String,String>> arraylistdata, ArrayList<Integer> arrayList, Context context)
    {
   this.arrayList = arrayList;
   this.context = context;
   this.arraylistdata = arraylistdata;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_short_video_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HashMap<String,String>map=arraylistdata.get(position);
        holder.txttitle.setText(map.get(Helper.Key_desc));
        holder.txtName.setText(map.get(Helper.Key_name));
        Glide.with(context)
                .load(arrayList.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.imgthumnail);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgthumnail,imgmenu;
        TextView txttitle,txtName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgthumnail = itemView.findViewById(R.id.imgthumnail);
            imgmenu = itemView.findViewById(R.id.imgmenu);
            txttitle = itemView.findViewById(R.id.txttitle);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
