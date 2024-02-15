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

public class AdapterTodayVideoList extends RecyclerView.Adapter<AdapterTodayVideoList.MViewHolder> {
private ArrayList<String> arrcategorylist;
ArrayList<Integer> arrimagelist;
private Context context;
    public AdapterTodayVideoList(ArrayList<String> arrcategorylist,ArrayList<Integer> arrimagelist, Context context) {
        this.arrcategorylist = arrcategorylist;
        this.arrimagelist = arrimagelist;
        this.context = context;

    }
    @NonNull
    @Override
    public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_today_vlist,parent,false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        holder.txttitle.setText(arrcategorylist.get(position));
        Glide.with(context)
                .load(arrimagelist.get(position))
                .error(R.drawable.noimagefound)
                .placeholder(R.drawable.noimagefound).into(holder.imgtodaythumb);
    }

    @Override
    public int getItemCount() {
        return arrcategorylist.size();
    }

    public class MViewHolder extends RecyclerView.ViewHolder {
        TextView txttitle;
        ImageView imgtodaythumb;
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            txttitle = itemView.findViewById(R.id.txttitle);
            imgtodaythumb = itemView.findViewById(R.id.imgtodaythumb);
        }
    }
}
