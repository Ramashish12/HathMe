package code.vediolist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityDownloadsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import code.activity.VideoPlayerActivity;
import code.utils.AppSettings;
import code.utils.AppUtils;
import code.vediolist.Model.VideoModel;
import code.vediolist.adapters.AdapterDownloadVideos;
import code.vediolist.adapters.Helper;
import code.view.BaseActivity;

public class DownloadsActivity extends BaseActivity implements View.OnClickListener {
    private ActivityDownloadsBinding b;
    private List<VideoModel> videoList = new ArrayList<>();
    Adapter adapter;
    String thumbnailPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDownloadsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inite();
    }
    private void inite() {
        b.header.ivBack.setOnClickListener(this);
        b.header.tvHeader.setText(getString(R.string.downloads));
        adapter = new Adapter(videoList);
        b.rvList.setAdapter(adapter);



        scanDownloadDirectory();
    }

    private void scanDownloadDirectory() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        if (downloadDir.exists() && downloadDir.isDirectory()) {
            File[] files = downloadDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isVideoFile(file.getName())) {
                        new ThumbnailTask().execute(file.getAbsolutePath());
                        String fileName = file.getName();
                        String filePath = file.getAbsolutePath();
                        thumbnailPath = generateThumbnailPath(filePath);
                        VideoModel videoModel = new VideoModel(fileName, filePath, thumbnailPath);
                        videoList.add(videoModel);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }
    }

    private boolean isVideoFile(String fileName) {
        // Add logic to determine if the file is a video file based on file extension
        return fileName.toLowerCase().endsWith(".mp4") || fileName.toLowerCase().endsWith(".avi");
    }

    private String generateThumbnailPath(String videoPath) {
        // Check if the videoPath is not null or empty
        if (videoPath != null && !videoPath.isEmpty()) {
            // Extract the file name without extension
            String fileNameWithoutExtension = videoPath.substring(videoPath.lastIndexOf(File.separator) + 1);
            int extensionIndex = fileNameWithoutExtension.lastIndexOf(".");
            if (extensionIndex > 0) {
                fileNameWithoutExtension = fileNameWithoutExtension.substring(0, extensionIndex);
            }

            // Generate the thumbnail file name
            String thumbnailFileName = "thumb_" + fileNameWithoutExtension + ".png";

            // Create a File object representing the cache directory and the thumbnail file
            File cacheDir = mActivity.getCacheDir();
            String thumbnailFilePath = Environment.getExternalStorageDirectory() + "/thumbnails/thumbnail.png";
            File thumbnailFile = new File(cacheDir, thumbnailFileName);

            return thumbnailFile.getAbsolutePath();
        }

        return null;
    }




    @Override
    public void onClick(View v) {
        onBackPressed();
    }



    private class ThumbnailTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 0) {
                String videoUrl = params[0];
                Bitmap thumbnail = generateThumbnail(videoUrl);
                return saveThumbnailToStorage(thumbnail);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String thumbnailUrl) {
            if (thumbnailUrl != null) {
                // Load and display the stored thumbnail using an image loading library
                // For example, you can use Glide or Picasso
                // Glide.with(ThumbnailActivity.this).load(thumbnailUrl).into(thumbnailImageView);
                // Picasso.get().load(thumbnailUrl).into(thumbnailImageView);
            }
        }
    }

    private Bitmap generateThumbnail(String videoUrl) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            // Set the data source to the video URL
            retriever.setDataSource(videoUrl, new HashMap<>());

            // Retrieve the thumbnail as a Bitmap
            return retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Release the MediaMetadataRetriever to free up resources
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String saveThumbnailToStorage(Bitmap thumbnail) {
        if (thumbnail != null) {
            try {
                // Create a file to save the thumbnail
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM);
                File thumbnailFile = File.createTempFile("thumbnail", ".png", storageDir);

                // Save the thumbnail to the file
                try (FileOutputStream fos = new FileOutputStream(thumbnailFile)) {
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }

                // Return the path of the saved thumbnail
                thumbnailPath = thumbnailFile.getAbsolutePath();
                return thumbnailFile.getAbsolutePath();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        private List<VideoModel> videoList;


        private Adapter(List<VideoModel> videoList) {
        this.videoList = videoList;
        }
        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_download_video_list, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
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
                startActivity(new Intent(mActivity, VideoPlayerActivity.class));

            });
            holder.imgmenu.setVisibility(View.VISIBLE);
            holder.imgmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PopupMenu popup = new PopupMenu(mActivity, view);
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
    }
    public void FileDeletion (String fileNameToDelete,String downloadsDirPath){
        File fileToDelete = new File(downloadsDirPath, fileNameToDelete);
        if (fileToDelete.exists()) {
            boolean isDeleted = fileToDelete.delete();
            if (isDeleted) {
                AppUtils.showToastSort(mActivity,"your video remove from downloads successfully.");
                videoList.clear();
                scanDownloadDirectory();
            } else {
                AppUtils.showToastSort(mActivity,"Failed to remove the video.");
            }
        } else {
            AppUtils.showToastSort(mActivity,"video does not exist.");
        }
    }
}