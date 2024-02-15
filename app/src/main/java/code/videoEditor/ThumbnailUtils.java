package code.videoEditor;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ThumbnailUtils {

    public static void createThumbnail(Context context, String videoFilePath, String thumbnailFilePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFilePath);

        // Retrieve the thumbnail as a Bitmap
        Bitmap thumbnail = retriever.getFrameAtTime();

        // Release the MediaMetadataRetriever to free up resources
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Save the thumbnail to a file
        saveBitmapToFile(thumbnail, thumbnailFilePath);
    }

    private static void saveBitmapToFile(Bitmap bitmap, String filePath) {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadThumbnail(Context context, String thumbnailFilePath, ImageView imageView) {
        File thumbnailFile = new File(thumbnailFilePath);

        // Load and set the thumbnail to the ImageView using your preferred image loading library
        // For example, you can use Glide or Picasso
        // Glide.with(context).load(thumbnailFile).into(imageView);
        // Picasso.get().load(thumbnailFile).into(imageView);
    }
}
