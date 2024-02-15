package code.vediolist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterStoriesList extends RecyclerView.Adapter<AdapterStoriesList.MyViewHolder>{
ArrayList<Integer> arrayList;
Context context;
ArrayList<HashMap<String,String>> arraylistdata;
    public AdapterStoriesList(ArrayList<HashMap<String,String>> arraylistdata, ArrayList<Integer> arrayList, Context context)
    {
   this.arrayList = arrayList;
   this.context = context;
   this.arraylistdata = arraylistdata;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_stories_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HashMap<String,String>map=arraylistdata.get(position);
        holder.txtName.setText(map.get(Helper.Key_name));
        Glide.with(context)
                .load(arrayList.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.imgthumnail);
        Glide.with(context)
                .load(arrayList.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.imgprofile);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgthumnail,imgmenu,imgprofile;
        TextView txttitle,txtName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgthumnail = itemView.findViewById(R.id.imgthumnail);
            imgprofile = itemView.findViewById(R.id.imgprofile);
            imgmenu = itemView.findViewById(R.id.imgmenu);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
