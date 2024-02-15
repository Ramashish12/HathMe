package code.vediolist.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;

import java.util.ArrayList;

import code.utils.AppSettings;
import code.videoEditor.VideoEditorActivity;


public class AdapterVideoCategoryList extends RecyclerView.Adapter<AdapterVideoCategoryList.MViewHolder> {
private ArrayList<String> arrayList;
private Context context;
    public AdapterVideoCategoryList(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;

    }
    @NonNull
    @Override
    public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_video_category,parent,false);
        return new MViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
        holder.tvCategory.setText(arrayList.get(position));
        if (position==0)
        {
            holder.rlBg.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.black));
            holder.tvCategory.setTextColor(ContextCompat.getColorStateList(context,R.color.white));
        }
        else
        {

        }
        holder.itemView.setOnClickListener(v -> {
            if (position==0)
            {
                AppSettings.putString(AppSettings.isFrom,"Long");
                holder.rlBg.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.black));
                holder.tvCategory.setTextColor(ContextCompat.getColorStateList(context,R.color.white));
                context.startActivity(new Intent(context, VideoEditorActivity.class));
            }
            else
            {
                AppSettings.putString(AppSettings.isFrom,"Short");
                context.startActivity(new Intent(context, VideoEditorActivity.class));
            }

        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        RelativeLayout rlBg;
        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            rlBg = itemView.findViewById(R.id.rlBg);

        }
    }
}
