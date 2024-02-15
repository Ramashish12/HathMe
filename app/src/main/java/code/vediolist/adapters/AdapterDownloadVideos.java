package code.vediolist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import code.activity.VideoPlayerActivity;
import code.utils.AppSettings;
import code.utils.AppUtils;
import code.vediolist.DownloadsActivity;
import code.vediolist.Model.VideoModel;
import code.vediolist.VideoDetailsActivity;
import code.view.BaseActivity;

public class AdapterDownloadVideos extends RecyclerView.Adapter<AdapterDownloadVideos.MyViewHolder> {
    Context context;
    private List<VideoModel> videoList;
    AdapterDownloadVideos adapter;
    public AdapterDownloadVideos(Context context, List<VideoModel> videoList,AdapterDownloadVideos adapter) {
        this.context = context;
        this.videoList = videoList;
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_download_video_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        VideoModel videoModel = videoList.get(position);
        // Bind data to the views
        String resultString = videoModel.getFileName().replaceAll("\\.mp4$", "");
        holder.tvDesc.setText(resultString);
        holder.txtview.setVisibility(View.GONE);
        Bitmap bmThumbnail;
        File downloadDir1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File files1 = new File(downloadDir1, videoModel.getFileName());
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(files1.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        holder.ivThumb.setImageBitmap(bmThumbnail);
        holder.itemView.setOnClickListener(view ->
        {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File files = new File(downloadDir, videoModel.getFileName());
            String path = files.getAbsolutePath();
            AppSettings.putString(AppSettings.KEY_selected_url, path);
            context.startActivity(new Intent(context, VideoPlayerActivity.class));

        });
        holder.imgmenu.setVisibility(View.VISIBLE);
        holder.imgmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popup = new PopupMenu((Activity) context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu3, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        int id = item.getItemId();
                        if (id == R.id.remove) {
                            popup.dismiss();
                            File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            File file = new File(download, videoModel.getFileName());
                            FileDeletion(file.getName(),download.toString());
                        } else {

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
        return videoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, imgmenu;
        TextView tvDesc, txtview;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            imgmenu = itemView.findViewById(R.id.ivMenu);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            txtview = itemView.findViewById(R.id.tvViewCount);
        }
    }
    public void FileDeletion (String fileNameToDelete,String downloadsDirPath){
            File fileToDelete = new File(downloadsDirPath, fileNameToDelete);
            if (fileToDelete.exists()) {
                boolean isDeleted = fileToDelete.delete();
                if (isDeleted) {
                    AppUtils.showToastSort(context,"your video remove from downloads successfully.");
                    Intent intent = new Intent(context,DownloadsActivity.class);
                    context.startActivity(intent);
                    ((BaseActivity)context).finish();

                } else {
                    AppUtils.showToastSort(context,"Failed to remove the video.");
                }
            } else {
                AppUtils.showToastSort(context,"video does not exist.");
            }
        }

}
