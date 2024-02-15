package code.livestreaming.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

import code.livestreaming.LiveActivity;
import code.utils.AppSettings;
import code.utils.AppUtils;
import code.view.BaseActivity;

public class AdapterLiveList extends RecyclerView.Adapter<AdapterLiveList.MyViewHolder>{
    Context context;
    ArrayList<HashMap<String, String>> data;
    public AdapterLiveList(ArrayList<HashMap<String, String>> data, Context context)
    {
     this.context = context;
     this.data = data;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.inflate_live_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvName.setText(data.get(position).get("name"));
        holder.tvTime.setText(AppUtils.changeDateTimeFormat(data.get(position).get("startDateTime")));
        AppUtils.loadPicassoImage(data.get(position).get("profileImage"), holder.ivImage);
        holder.itemView.setOnClickListener(v -> {
            GoLive(AppSettings.getString(AppSettings.name),data.get(position).get("userId"),
                    data.get(position).get("channelId"));
        });
        if (data.get(position).get("status").equalsIgnoreCase("1"))
        {
          holder.imgstatus.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.green));
          holder.tvStatus.setText(R.string.online);
        }
        else
        {
          holder.imgstatus.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.red));
          holder.tvStatus.setText(R.string.offline);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage,imgstatus;
        TextView tvName,tvStatus,tvTime,tvgolive;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgstatus = itemView.findViewById(R.id.imgstatus);
            tvgolive = itemView.findViewById(R.id.tvgolive);
        }
    }

    //create channel api
public void GoLive(final String strusername,final String struserid,final String strliveid)
{
    Intent intent = new Intent((BaseActivity) context, LiveActivity.class);
    intent.putExtra(AppSettings.host, false);
    intent.putExtra(AppSettings.userName, strusername);
    intent.putExtra(AppSettings.userID, struserid);
    intent.putExtra(AppSettings.liveId, strliveid); //unique
    context.startActivity(intent);
}
}
