package of.media.hz.until;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import com.github.promeg.pinyinhelper.Pinyin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import of.media.hz.Application.App;
import of.media.hz.Music;
import of.media.hz.info.AlbumInfo;
import of.media.hz.info.ArtistInfo;
import of.media.hz.info.MusicInfo;

import of.media.hz.onlineUtil.PreferencesUtility;
import of.media.hz.ui.IConstants;

public class MusicUtils {

	public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
	public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟

	private static String[] proj_music = new String[]{
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE};

	private static String[] proj_album = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ARTIST};

	private static String[] proj_artist = new String[]{
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
			MediaStore.Audio.Artists._ID};

	// 存放歌曲列表
	public  static ArrayList<Music> sMusicList = new ArrayList<Music>();
	public static void initMusicList() {
		// 获取歌曲列表
		sMusicList.clear();
		sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
	}

	/**
	 * 获取内存卡根
	 * @return
	 */
	public static String getBaseDir() {
		String dir = null;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator;
		}

		return dir;
	}

	/**
	 * 获取应用程序使用的本地目录
	 * @return
	 */
	public static String getAppLocalDir() {
		String dir = null;

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator
					+ "liteplayer" + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator + "liteplayer" + File.separator;
		}

		return mkdir(dir);
	}

	/**
	 * 获取音乐存放目录
	 * @return
	 */
	public static String getMusicDir() {
		String musicDir = getAppLocalDir() + "music" + File.separator;
		return mkdir(musicDir);
}

	/**
	 * 获取歌词存放目录
	 * 
	 * @return
	 */
	public static String getLrcDir() {
		
		String lrcDir = getBaseDir() + "/music/lrc" + File.separator;
		return mkdir(lrcDir);
	}

	/**
	 * 创建文件夹
	 * @param dir
	 * @return
	 */
	public static String mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			for (int i = 0; i < 5; i++) {
				if(f.mkdirs()) return dir;
			}
			return null;
		}
		
		return dir;
	}


	/**
	 * @param from    不同的界面进来要做不同的查询
	 */
	public static List<MusicInfo> queryMusic(Context context, int from) {
		return queryMusic(context, null, from);
	}


	public static ArrayList<MusicInfo> queryMusic(Context context, String id, int from) {

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();

		StringBuilder select = new StringBuilder(" 1=1 and title != ''");
		// 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
		select.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
		select.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

		String selectionStatement = "is_music=1 AND title != ''";
		final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();


		switch (from) {
			case IConstants.START_FROM_LOCAL:
				ArrayList<MusicInfo> list3 = getMusicListCursor(cr.query(uri, proj_music,
						select.toString(), null,
						songSortOrder));
				return list3;
			case IConstants.START_FROM_ARTIST:
				select.append(" and " + MediaStore.Audio.Media.ARTIST_ID + " = " + id);
				return getMusicListCursor(cr.query(uri, proj_music, select.toString(), null,
						PreferencesUtility.getInstance(context).getArtistSongSortOrder()));
			case IConstants.START_FROM_ALBUM:
				select.append(" and " + MediaStore.Audio.Media.ALBUM_ID + " = " + id);
				return getMusicListCursor(cr.query(uri, proj_music,
						select.toString(), null,
						PreferencesUtility.getInstance(context).getAlbumSongSortOrder()));
			case IConstants.START_FROM_FOLDER:
				ArrayList<MusicInfo> list1 = new ArrayList<>();
				ArrayList<MusicInfo> list = getMusicListCursor(cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj_music,
						select.toString(), null,
						null));
				for (MusicInfo music : list) {
					if (music.data.substring(0, music.data.lastIndexOf(File.separator)).equals(id)) {
						list1.add(music);
					}
				}
				return list1;
			default:
				return null;
		}

	}

	public static ArrayList<MusicInfo> getMusicListCursor(Cursor cursor) {
		if (cursor == null) {
			return null;
		}

		ArrayList<MusicInfo> musicList = new ArrayList<>();
		while (cursor.moveToNext()) {
			MusicInfo music = new MusicInfo();
			music.songId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media._ID));
			music.albumId = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			music.albumName = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
			music.albumData = getAlbumArtUri(music.albumId) + "";
			music.duration = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));
			music.musicName = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			music.artist = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			music.artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
			String filePath = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA));
			music.data = filePath;
			music.folder = filePath.substring(0, filePath.lastIndexOf(File.separator));
			music.size = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE));
			music.islocal = true;
			music.sort = Pinyin.toPinyin(music.musicName.charAt(0)).substring(0, 1).toUpperCase();
			musicList.add(music);
		}
		cursor.close();
		return musicList;
	}


	/**
	 * 获取歌手信息
	 */
	public static List<ArtistInfo> queryArtist(Context context) {

		Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
		ContentResolver cr = context.getContentResolver();
		StringBuilder where = new StringBuilder(MediaStore.Audio.Artists._ID
				+ " in (select distinct " + MediaStore.Audio.Media.ARTIST_ID
				+ " from audio_meta where (1=1 )");
		where.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
		where.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

		where.append(")");

		List<ArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
				where.toString(), null, PreferencesUtility.getInstance(context).getArtistSortOrder()));

		return list;
	}

	public static Uri getAlbumArtUri(long albumId) {
		return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
	}

	/**
	 * 获取专辑信息
	 */
	public static List<AlbumInfo> queryAlbums(Context context) {

		ContentResolver cr = context.getContentResolver();
		StringBuilder where = new StringBuilder(MediaStore.Audio.Albums._ID
				+ " in (select distinct " + MediaStore.Audio.Media.ALBUM_ID
				+ " from audio_meta where (1=1)");
		where.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
		where.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

		where.append(" )");

		// Media.ALBUM_KEY 按专辑名称排序
		List<AlbumInfo> list = getAlbumList(cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, proj_album,
				where.toString(), null, PreferencesUtility.getInstance(context).getAlbumSortOrder()));
		return list;

	}

	public static List<AlbumInfo> getAlbumList(Cursor cursor) {
		List<AlbumInfo> list = new ArrayList<>();
		while (cursor.moveToNext()) {
			AlbumInfo info = new AlbumInfo();
			info.album_name = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
			info.album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
			info.number_of_songs = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
			info.album_art = getAlbumArtUri(info.album_id) + "";
			info.album_artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
			info.album_sort = Pinyin.toPinyin(info.album_name.charAt(0)).substring(0, 1).toUpperCase();
			list.add(info);
		}
		cursor.close();
		return list;
	}

	public static List<ArtistInfo> getArtistList(Cursor cursor) {
		List<ArtistInfo> list = new ArrayList<>();
		while (cursor.moveToNext()) {
			ArtistInfo info = new ArtistInfo();
			info.artist_name = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
			info.number_of_tracks = cursor.getInt(cursor
					.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
			info.artist_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
			info.artist_sort = Pinyin.toPinyin(info.artist_name.charAt(0)).substring(0, 1).toUpperCase();
			list.add(info);
		}
		cursor.close();
		return list;
	}
}
