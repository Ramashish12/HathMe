package code.vediolist;
import android.content.Context;
import java.io.File;

public class FileUtils {

    public static File createCacheSubdirectory(Context context, String subdirectoryName) {
        File cacheDir = context.getCacheDir();
        File subdirectory = new File(cacheDir, subdirectoryName);

        // Create the subdirectory if it doesn't exist
        if (!subdirectory.exists()) {
            if (!subdirectory.mkdirs()) {
                // Handle the case where directory creation failed
                return null;
            }
        }

        return subdirectory;
    }
}
