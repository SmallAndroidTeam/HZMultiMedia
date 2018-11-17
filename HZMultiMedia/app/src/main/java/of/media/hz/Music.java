package of.media.hz;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class Music implements Parcelable{
	// id title singer data time image
	private int id; // 音乐id
	private String title; // 音乐标题
	private String uri; // 音乐路径
	private int length; // 长度
	private String image; // icon
	private String artist; // 艺术家
	private String lrcpath;//歌词路径
	private long duration;
	private String Album;
	public Music() {
	}

	public Music(String title, String uri, String image, String artist, String lrcpath) {//本地歌曲的构造函数
		this.title = title;
		this.uri = uri;
		this.image = image;
		this.artist = artist;
		this.lrcpath = lrcpath;
	}

	public Music(String title, String image, String artist, String lrcpath) {
		this.title = title;
		this.image = image;
		this.artist = artist;
		this.lrcpath = lrcpath;
	}



	protected Music(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.uri = in.readString();
        this.length = in.readInt();
        this.image = in.readString();
        this.artist = in.readString();
        this.lrcpath = in.readString();
        this.Album=in.readString();
        this.duration=in.readLong();
	}
	public static final Creator<Music> CREATOR = new Creator<Music>() {
		@Override
		public Music createFromParcel(Parcel in) {

			return new Music(in);
		}

		@Override
		public Music[] newArray(int size) {
			return new Music[size];
		}
	};

	public String getLrcpath() {
		return lrcpath;
	}

	public void setLrcpath(String lrcpath) {
		this.lrcpath = lrcpath;
	}

	public int getId() {
		return id;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getAlbum() {
		return Album;
	}

	public void setAlbum(String album) {
		Album = album;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
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


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeString(title);
		parcel.writeString(uri);
		parcel.writeInt(length);
		parcel.writeString(image);
		parcel.writeString(artist);
		parcel.writeString(lrcpath);
		parcel.writeString(Album);
		parcel.writeLong(duration);
	}
	//必须添加此函数
	public void readFromParcel(Parcel in){
		this.id = in.readInt();
		this.title = in.readString();
		this.uri = in.readString();
		this.length = in.readInt();
		this.image = in.readString();
		this.artist = in.readString();
		this.lrcpath = in.readString();
		this.Album=in.readString();
		this.duration=in.readLong();
	}
}
