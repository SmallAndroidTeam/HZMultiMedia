// MusicListChangeListener.aidl
package of.media.hz;
import of.media.hz.Music;
// Declare any non-default types here with import statements

interface MusicListChangeListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void musicListChangeListener(boolean result);//监听列表变化
}
