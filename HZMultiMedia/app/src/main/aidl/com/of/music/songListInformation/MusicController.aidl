// MusicController.aidl
package com.of.music.songListInformation;

import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicPlayProgressListener;
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
  List<Music> getLocalMusicList();//获取本地的音乐列表

  List<Music> getCurrentMusicList();//获取当期的播放列表

  void setCurrentMusicList(inout List<Music> list);//设置当前的播放列表

  boolean setCurrentPlayIndex(int position);//设置当前的播放下标

  int getCurrentPlayIndex();//获取当前的播放下标

  List<Music> getUsbMusicList();//获取U盘音乐列表

  void initMusicService();//初始化音乐服务

  void playCurrentSelectedMusic(int position);//播放选中的音乐下标的歌曲

  Music getPlayMusicInfo();//获取当前播放的音乐的信息

  boolean musicIsPlaying();//判断音乐是否正在播放

  void startCurrentPlayIndexMusic();//播放当前音乐下标的歌曲

  void pauseCurrentPlayIndexMusic();//暂停当前的音乐下标的歌曲

  void stopCurrentPlayIndexMusic();//停止当前音乐下标的歌曲

  void of_nextMusic();//下一首歌

  void of_prevMusic();//上一首歌

  int of_getDuration();//当前播放歌曲的总的时长

  int of_getCurrentPosition();//当前播放歌曲的进度

  void of_setCurrent(int pos);//设置当前播放的歌曲的进度

  boolean setPlayMode(int type);//设置播放模式 type={0 顺序模式 ,1 列表模式 ,2 单曲模式 ,3 随机模式}

  int getCurrentMusicListSize();//获取当前的音乐列表大小

  void setMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener);//设置播放进度监听

  void cancelMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener);//取消播放进度监听


}
