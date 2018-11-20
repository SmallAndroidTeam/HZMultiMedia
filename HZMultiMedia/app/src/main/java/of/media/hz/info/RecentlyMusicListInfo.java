package of.media.hz.info;

import org.litepal.crud.LitePalSupport;

public class RecentlyMusicListInfo extends LitePalSupport {
    
    private String name;
    private String artist;
    private String image;
    private String uri;
    private String Lrc_uri;
    private String playTime;
    
    public String getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getName() {
        
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLrc_uri() {
        
        return Lrc_uri;
    }
    
    public void setLrc_uri(String lrc_uri) {
        Lrc_uri = lrc_uri;
    }
    
    public String getImage() {
        
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
   
    
    
    
    public String getArtist() {
        
        return artist;
    }
    
    public void setArtist(String artist) {
        this.artist = artist;
    }
}
