package code.vediolist.Model;

public class VideoModel {
    private String fileName;
    private String filePath;
    private String thumbnailPath;

    public VideoModel(String fileName, String filePath, String thumbnailPath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }
}
