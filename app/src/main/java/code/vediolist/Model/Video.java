package code.vediolist.Model;

public class Video {
    private String videourl,title,desc;

    public Video(String videourl, String title, String desc) {
        this.videourl = videourl;
        this.title = title;
        this.desc = desc;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
