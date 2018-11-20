package of.media.hz.info;

import org.litepal.crud.LitePalSupport;

public class FavouriteMusicListInfo extends LitePalSupport {
    private int id;
    private String name;
    private String artist;
    private String image;
    private String uri;
    private String Lrc_uri;

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

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArtist() {

        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
