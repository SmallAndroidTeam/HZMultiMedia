// MusicController.aidl
package of.media.hz;

import of.media.hz.Music;
import of.media.hz.MusicPlayProgressListener;
import of.media.hz.MusicListChangeListener;
// Declare any non-default types here with import statements
interface MusicController {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//     用户首先通过getLocalMusicList或getUsbMusicList获取音乐列表，
//     然后通过setCurrentMusicList设置播放的音乐列表，
//     其次调用setCurrentPlayIndex设置当前播放的音乐下标
//     之后调用initMusicService初始化音乐服务，为了一开始设置默认的歌曲
//     最后就可以调用startCurrentPlayIndexMusic播放当前音乐下标的歌曲
  List<Music> of_getLocalMusicList();//获取本地的音乐列表

   List<Music> of_getCurrentMusicList();//获取当前的播放列表
  void of_setCurrentMusicList(inout List<Music> list);//设置当前的播放列表

  boolean of_setCurrentPlayIndex(int position);//设置当前的播放下标

  int of_getCurrentPlayIndex();//获取当前的播放下标

  List<Music> of_getUsbMusicList();//获取U盘音乐列表

  void of_initMusicService();//初始化音乐服务

  void of_playCurrentSelectedMusic(int position);//播放选中的音乐下标的歌曲

  Music of_getPlayMusicInfo();//获取当前播放的音乐的信息

  boolean of_musicIsPlaying();//判断音乐是否正在播放

  void of_startCurrentPlayIndexMusic();//播放当前音乐下标的歌曲

  void of_pauseCurrentPlayIndexMusic();//暂停当前的音乐下标的歌曲

  void of_stopCurrentPlayIndexMusic();//停止当前音乐下标的歌曲

  void of_nextMusic();//下一首歌

  void of_prevMusic();//上一首歌

  int of_getDuration();//当前播放歌曲的总的时长

  int of_getCurrentPosition();//当前播放歌曲的进度

  void of_setCurrent(int pos);//设置当前播放的歌曲的进度

   boolean of_setPlayMode(int type);//设置播放模式 type={0 顺序模式 ,1 列表模式 ,2 单曲模式 ,3 随机模式}

  int of_getCurrentMusicListSize();//获取当前的音乐列表大小

   List<String> of_getCurrentPlayMusicAllLyric();//获取当前播放歌曲的所有歌词

   String of_getCurrentPlayMusicOneLyric();// 获取当前播放歌曲正在播放的那一句歌词

   int of_getCurrentPlayMusicOneLyricIndex();//获取当前播放歌曲正在播放的那一句歌词在所有歌词的下标

  void of_setMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener);//设置播放进度监听

  void of_cancelMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener);//取消播放进度监听

    void of_setMusicListChangeListener(MusicListChangeListener musicListChangeListener);//设置音乐列表变化监听

    void of_cancelMusicListChangeListener(MusicListChangeListener musicListChangeListener);//取消音乐列表变化监听
}
