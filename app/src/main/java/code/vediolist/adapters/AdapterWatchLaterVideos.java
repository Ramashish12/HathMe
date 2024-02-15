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

public class AdapterWatchLaterVideos extends RecyclerView.Adapter<AdapterWatchLaterVideos.MyViewHolder>{
ArrayList<Integer> arrayList;
Context context;
ArrayList<HashMap<String,String>> arraylistdata;
    public AdapterWatchLaterVideos(ArrayList<HashMap<String,String>> arraylistdata, ArrayList<Integer> arrayList, Context context)
    {
   this.arrayList = arrayList;
   this.context = context;
   this.arraylistdata = arraylistdata;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_download_video_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        HashMap<String,String>map=arraylistdata.get(position);
        holder.txtdesc.setText(map.get(Helper.Key_desc));
        holder.txtsubdesc.setText(map.get(Helper.Key_subdesc));
        holder.txtview.setText(map.get(Helper.Key_view));
        Glide.with(context)
                .load(arrayList.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.imgdownloadthumb);
        holder.imgmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu((Activity)context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu5, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        int id = item.getItemId();
                        if(id== R.id.removewatchlater){
                            popup.dismiss();

                        }

                        else
                        {

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
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imgdownloadthumb,imgmenu;
        TextView txtdesc,txtsubdesc,txtview;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgdownloadthumb = itemView.findViewById(R.id.ivThumb);
            imgmenu = itemView.findViewById(R.id.ivMenu);
            txtdesc = itemView.findViewById(R.id.tvDesc);
            txtsubdesc = itemView.findViewById(R.id.tvTitle);
            txtview = itemView.findViewById(R.id.tvViewCount);
        }
    }
}
