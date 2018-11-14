// MusicPlayPositonListener.aidl
package of.media.hz;

// Declare any non-default types here with import statements

interface MusicPlayProgressListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void musicProgressListener(int progress);//播放进度监听
}
