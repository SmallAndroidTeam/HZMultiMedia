package of.media.hz.onlineUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class PreferencesUtility {
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";

    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";

    private static PreferencesUtility sInstance;
    private static SharedPreferences mPreferences;

    //构造函数
    public PreferencesUtility(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //返回单例对象
    public static final PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }

    public final String getArtistSongSortOrder() {
        return mPreferences.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z);
    }

    public final String getAlbumSongSortOrder() {
        return mPreferences.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public final String getArtistSortOrder() {
        return mPreferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }
}
