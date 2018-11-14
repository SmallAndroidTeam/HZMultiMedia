package of.media.hz.info;

public class MusicName {
    private int id;
    private String name;
    private String artist;
    private String image;

    public String getUri() {
        return uri;
    }

    public String getLrc_uri() {
        return Lrc_uri;
    }

    public void setLrc_uri(String lrc_uri) {
        Lrc_uri = lrc_uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private String uri;
    private String Lrc_uri;

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

    public MusicName() {
        super();
    }

    public MusicName(String name) {

        this.name = name;
    }

    public MusicName(String name, String artist) {
        this.artist = artist;
        this.name = name;
    }

    public MusicName(String name, String artist, String image) {
        this.artist = artist;
        this.name = name;
        this.image = image;
    }

    public MusicName(String name, String artist, String image, String uri, String lrc_uri) {
        this.artist = artist;
        this.name = name;
        this.image = image;
        this.uri = uri;
        this.Lrc_uri = lrc_uri;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setName(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
