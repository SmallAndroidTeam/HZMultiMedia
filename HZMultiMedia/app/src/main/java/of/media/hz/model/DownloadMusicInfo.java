package of.media.hz.model;

/**
 * Created by hzwangchenyan on 2017/8/11.
 */
public class DownloadMusicInfo {
    private String title;
    private String musicPath;
    private String coverPath;
    private String lrcPath;
    private String artist;
    private String time;
    public DownloadMusicInfo(){};
    public DownloadMusicInfo(String title, String musicPath, String coverPath,String artist,String lrcPath,String time) {
        this.title = title;
        this.musicPath = musicPath;
        this.coverPath = coverPath;
        this.lrcPath=lrcPath;
        this.artist=artist;
        this.time=time;
    }
    public  String getLrcPath(){return lrcPath;}
    public String getTitle() {
        return title;
    }
    
    public String getMusicPath() {
        return musicPath;
    }
    
    public String getCoverPath() {
        return coverPath;
    }
    public String getArtist(){return artist;}
    public String getTime(){return time;}
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setArtist(String artist) {
        this.artist = artist;
    }
    
    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
    
    public void setLrcPath(String lrcPath) {
        this.lrcPath = lrcPath;
    }
    
    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
}
