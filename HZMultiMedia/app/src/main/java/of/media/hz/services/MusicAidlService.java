package of.media.hz.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;

import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import of.media.hz.Application.App;
import of.media.hz.Music;
import of.media.hz.MusicController;
import of.media.hz.MusicListChangeListener;
import of.media.hz.MusicPlayProgressListener;
import of.media.hz.R;
import of.media.hz.toast.OneToast;
import of.media.hz.until.MusicIconLoader;
import of.media.hz.until.MusicUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class MusicAidlService extends Service {
    public  MediaPlayer mediaPlayer;
    public  int playingMusicIndex=-1;//正在播放音乐的下标
    private  NotificationManager notificationManager;
    private RemoteViews widgetRemoteViews;
    private String TAG="Music";
    private   Runnable runnable;
    private Timer timer;
    public  String musicArtist;
    public  String musicIcon;
    public  String musicUri;
    public   String musicLrcpath;
    public static  final String SEND_PROGRESS="com.of.music.aidl.progress";
    public static final String TOGGLEPAUSE_ACTION = "com.of.music.aidl.togglepause";
    public static final String PREVIOUS_ACTION = "com.of.music.aidl.previous";
    public static final String NEXT_ACTION = "com.of.music.aidl.next";
    public static final String STOP_ACTION = "com.of.music.aidl.STOP_ACTION";
    public static final String META_CHANGED = "com.of.music.aidl.metachanged";
    public static final String MUSIC_CHANGED = "com.of.music.aidl.change_music";
    public static final String MY_BROCAST = "com.of.music.aidl.MY_BROADCAST";
    public static final  String WIDGET_LOVE_ACTION="com.of.music.aidl.WIDGET_LOVE_BROADCAST";
    public static final  String LOVE_ACTION="com.of.music.aidl.LOVE_BROADCAST";
    public static final  String NOTIFICATION_LOVE_ACTION="com.of.music.aidl.NOTIFICATION_LOVE_BROADCAST";
    public static final String PLAYVIEW_LOVE_ACTION="com.of.music.aidl.ADD_LOVE_ACTION";
    public static final String RECENTLY_ADDACTION = "com.of.music.aidl.recentlyfragment";
    public static final String UPDATE_ACTION="com.of.music.aidl.update";
    public final static String[] playMode=new String[]{"list_mode","circulate_mode","singlecycle_mode","randomplay_mode"};//播放模式
    public  String currentPlayMode=playMode[3];//当前的音乐播放模式
    private  final int NotificationId=1001;
    private   Notification mNotification;
    private  boolean ForegroundIsExist=false;//判断前台服务是否存在
    private boolean isInitComplete=false;//判断是否第一次初始化服务完成
    public   ArrayList<Music> musicList=new ArrayList<>();//歌曲列表
    public  ArrayList<Music> oldMusicList=new ArrayList<>();//歌曲列表的副本，用于判断歌曲列表是否变化
    private final static int INIT_MUSCIC_SERVICE=0;//初始化music服务
    private  boolean isExecutionInit=false;//判断是否正在执行初始化函数
    private List<String> mLrcs = new ArrayList<String>(); // 存放歌词
    private List<Long> mTimes = new ArrayList<Long>(); // 存放时间
    @SuppressLint("HandlerLeak")
    private final Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_MUSCIC_SERVICE://初始化服务
                    try{
                        initMusic();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }

        }

    };
    private final BroadcastReceiver mIntentReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

       String processName=getProcessName();

       //判断进程名
       if(!TextUtils.isEmpty(processName)&&processName.contentEquals(getPackageName()+":remote"))
       {
           handleCommandIntent(intent);
       }

       }
    };

    //获取进程名
    public  String getProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onCreate() {
        //注册广播
        final IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(TOGGLEPAUSE_ACTION);
        intentFilter.addAction(PREVIOUS_ACTION);
        intentFilter.addAction(SEND_PROGRESS);
        intentFilter.addAction(NEXT_ACTION);
        intentFilter.addAction(STOP_ACTION);
        intentFilter.addAction(WIDGET_LOVE_ACTION);
        intentFilter.addAction(NOTIFICATION_LOVE_ACTION);
        registerReceiver(mIntentReceiver,intentFilter);
        mediaPlayer=new MediaPlayer();
        isInitComplete=false;//第一次初始化服务未完成
        widgetRemoteViews =new RemoteViews(this.getPackageName(), R.layout.notification_aidl);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                autoPlayMusic();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.i("hz111", "onError: ");
                nextMusic();
                return false;
            }
        });
        runnable = new Runnable() {
            @Override
            public void run() {

                //执行进度条监听
                int n= musicPlayProgressListenerRemoteCallbackList.beginBroadcast();//获取监听事件的个数
                for(int i=0;i<n;i++){
                    MusicPlayProgressListener musicPlayProgressListener= musicPlayProgressListenerRemoteCallbackList.getBroadcastItem(i);
                    if(musicPlayProgressListener!=null){
                        try {
                            if(mediaPlayer!=null) {
                                //if(mediaPlayer.isPlaying()){
                                if(isInitComplete) {    //如果第一次初始化服务完成（如果初始化没完成，调用getCurrentPosition会出错)
                                   //没有在执行initMusic函数
                                    if(!isExecutionInit){
                                        musicPlayProgressListener.musicProgressListener(mediaPlayer.getCurrentPosition());//向客户端发送数据
                                    }

                                }else{
                                    musicPlayProgressListener.musicProgressListener(0);//向客户端发送数据
                                }

                            }
                            else{
                                musicPlayProgressListener.musicProgressListener(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                musicPlayProgressListenerRemoteCallbackList.finishBroadcast();

               //执行列表变化监听
                int  m=musicListChangeListenerRemoteCallbackList.beginBroadcast();
                for(int i=0;i<m;i++){
                    MusicListChangeListener musicListChangeListener=musicListChangeListenerRemoteCallbackList.getBroadcastItem(i);
                    if(musicListChangeListener!=null){
                      if(oldMusicList.size()!=0&&!musicList.containsAll(oldMusicList)){//如果音乐列表副本和音乐列表内容不一致
                          try {
                              musicListChangeListener.musicListChangeListener(true);
                              oldMusicList.clear();
                              oldMusicList.addAll(musicList);//重新备份音乐列表副本
                          } catch (Exception e) {
                              e.printStackTrace();
                          }
                      }else{
                          try {
                              if(oldMusicList.size()==0&&musicList.size()!=0){
                                  musicListChangeListener.musicListChangeListener(true);
                                  oldMusicList.clear();
                                  oldMusicList.addAll(musicList);//重新备份音乐列表副本
                                  }else{
                                  musicListChangeListener.musicListChangeListener(false);
                              }

                          } catch (Exception e) {
                              e.printStackTrace();
                          }
                      }
                    }

                }
              musicListChangeListenerRemoteCallbackList.finishBroadcast();

                handler.postDelayed(this, 100);
            }
        };

        handler.post(runnable);
        super.onCreate();
    }

    //设置音乐列表
    public  void setMusicList(ArrayList<Music> musicList) {

            this.musicList.clear();
             oldMusicList.clear();
            if(musicList!=null){
                this.musicList.addAll(musicList);
                oldMusicList.addAll(musicList);
            }
    }

    public    boolean  initMusic(){
        synchronized (this) {
            if (isSatisfyingPlayConditions()) {
                if(mediaPlayer==null){
                    mediaPlayer=new MediaPlayer();
                }

                try {
                    isExecutionInit=true;//用于判断是否正在执行此函数
                    mediaPlayer.reset();
                    String musicAddress=musicList.get(playingMusicIndex).getUri();
                    while(!musicAddress.toLowerCase().startsWith("http")&&!new File(musicAddress).exists()){
                        musicList.remove(playingMusicIndex);
                        if(musicList.size()==0){
                            playingMusicIndex=-1;
                            stopMusic();
                            return false;
                        }
                        playingMusicIndex=playingMusicIndex>=musicList.size()-1?0:playingMusicIndex+1;
                        musicAddress=musicList.get(playingMusicIndex).getUri();
                    }

                    mediaPlayer.setDataSource(musicList.get(playingMusicIndex).getUri());
                    mediaPlayer.prepare();

                    getLrc(musicList.get(playingMusicIndex).getLrcpath());//开启线程获取当前的歌词
                    musicArtist=musicList.get(playingMusicIndex).getArtist();                        //获取歌曲信息
                    musicIcon=musicList.get(playingMusicIndex).getImage();
                    musicUri=musicList.get(playingMusicIndex).getUri();
                    musicLrcpath=musicList.get(playingMusicIndex).getLrcpath();
                    Intent UpdateIntent=new Intent(UPDATE_ACTION);
                    handleCommandIntent(UpdateIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                    isInitComplete=true;//第一次初始化服务完成
                    isExecutionInit=false;//用于判断是否正在执行此函数
                    return false;
                }
                isInitComplete=true;//第一次初始化服务完成
                isExecutionInit=false;//用于判断是否正在执行此函数
            } else {
                isInitComplete=true;//第一次初始化服务完成
                isExecutionInit=false;//用于判断是否正在执行此函数
                OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
            }

        }
        isExecutionInit=false;//用于判断是否正在执行此函数
        isInitComplete=true;//第一次初始化服务完成
        return true;
    }

    //判断是否满足播放条件
    private boolean isSatisfyingPlayConditions(){
        int size=musicList.size();
        Log.i("hz111", "isSatisfyingPlayConditions: "+musicList.size()+"//"+playingMusicIndex);
        if(playingMusicIndex<0||size==0||playingMusicIndex>size-1&&mediaPlayer!=null){
            return  false;
        }
        return  true;
    }

    /**
     * 返回当前播放的进度
     * @return
     */
    public   int getMusicCurrentPosition(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return mediaPlayer.getCurrentPosition();
        }else
        {
            return 0;
        }
    }

    /**
     *获取当前播放音乐的总的播放时间
     * @return
     */
    public int getMusicDuration(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return mediaPlayer.getDuration();
        }else
        {
            return 0;
        }
    }

    /**
     *
     * @return (当前正在播放返回true,否则返回false）
     */
    public boolean isPlaying(){
        if(isSatisfyingPlayConditions()&&mediaPlayer!=null){
            return  mediaPlayer.isPlaying();
        }else{
            return  false;
        }
    }



    public void startMusic(){
        synchronized (this){
            if(!isSatisfyingPlayConditions()){
                OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                return;
            }

            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
              //  handler.removeCallbacks(runnable);
            }
            else{
                //重点
                mediaPlayer.start();
                handler.post(runnable);
                setForeground();//建立前台服务
            }
        }

    }
    public void autoPlayMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i <playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                    if (playingMusicIndex == (musicList.size() - 1)) {
                        playingMusicIndex = 0;
                        if(!initMusic()){
                            OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                            return;
                        }
                        mediaPlayer.pause();
                        NotificationChange(NEXT_ACTION);
                     //   handler.removeCallbacks(runnable);
                    } else {
                        Intent intent3=new Intent(NEXT_ACTION);
                        handleCommandIntent(intent3);
                    }
                    break;
                case 1://列表循环
                    // nextMusic();
                    Intent intent3=new Intent(NEXT_ACTION);
                    handleCommandIntent(intent3);
                    break;
                case 2://单曲循环
                    if(!initMusic()){
                        OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                        return;
                    }
                    mediaPlayer.start();
                    NotificationChange(NEXT_ACTION);
                    handler.post(runnable);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    if(!initMusic()){
                        OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                        return;
                    }
                    mediaPlayer.start();
                    NotificationChange(NEXT_ACTION);
                    handler.post(runnable);
                    break;
                default:
                    break;
            }

            //Log.i(TAG, "autoPlayMusic: "+playingMusicIndex);
        }
    }
    public void stopMusic(){
        synchronized (this){

            try {
                mediaPlayer.stop();
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
               // handler.removeCallbacks(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void nextMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i < playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex ==musicList.size() - 1) ? 0 : (playingMusicIndex + 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            Log.i(TAG, "NextMusic: " + playingMusicIndex);
            if(!initMusic()){
                //  OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
           // handler.post(runnable);
            setForeground();//建立前台服务
        }

    }


    public void prevMusic(){
        synchronized (this) {
            if (!isSatisfyingPlayConditions()) {
                OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
                return;
            }
            int i;
            for (i = 0; i <playMode.length; i++) {
                if (currentPlayMode.contentEquals(playMode[i])) {
                    break;
                }
            }
            if (i >= playMode.length) {
                i = playMode.length - 1;
            }
            //只改变播放音乐的下标
            switch (i) {
                case 0://顺序播放
                case 1://列表循环
                case 2://单曲循环
                    playingMusicIndex = (playingMusicIndex == 0) ? (musicList.size() - 1) : (playingMusicIndex - 1);
                    break;
                case 3://随机播放
                    int MusicIndex =new Random().nextInt(musicList.size());
                    while (MusicIndex == playingMusicIndex&&musicList.size()>1) {
                        MusicIndex = new Random().nextInt(musicList.size());
                    }
                    playingMusicIndex = MusicIndex;
                    break;
                default:
                    break;
            }
            if(!initMusic()){
                //OnlyOneToast.makeText(getApplicationContext(),"当前无歌曲");
                return;
            }
            mediaPlayer.start();
           // handler.post(runnable);
            setForeground();//建立前台服务
        }
    }

    public  void timing(int time){
        Timer nTimer = new Timer();
        nTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mediaPlayer.pause();
            }
        },time);
    }

    /**
     * 绑定服务
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(intent!=null&&intent.getAction().contentEquals("com.android.oflim.action")){//只有发出特定消息才会绑定服务
            App.sContext=getApplicationContext();
            return stub;
        }
        return null;
    }


    @Override
    public boolean onUnbind(Intent intent) {
         stopSelf();//如果解除绑定则摧毁服务
        return super.onUnbind(intent);
    }

    /**
     * 判断播放下标是否满足播放条件
     * @param position
     * @return
     */
    private boolean isSatisfyPlayCondition(int position){
        if(position<0||position>musicList.size()-1){
            return false;
        }else{
            return true;
        }
    }

    //创建前台服务
    public void setForeground(){
        if(!ForegroundIsExist)
        { new Thread(new Runnable() {
            @Override
            public void run() {
                ForegroundIsExist=true;
                startForeground(NotificationId,getmNotification());
            }
        }).start();

        }
    }

    //获取本地的歌曲列表
    private ArrayList<Music> getLocalMusicList(){
        MusicUtils.initMusicList();
        return MusicUtils.sMusicList;
    }

    //RemoteCallbackList是专门用于删除跨进程listener的接口，它是一个泛型，支持管理任意的AIDL接口
    private RemoteCallbackList<MusicPlayProgressListener> musicPlayProgressListenerRemoteCallbackList =new RemoteCallbackList<>();
    private RemoteCallbackList<MusicListChangeListener> musicListChangeListenerRemoteCallbackList=new RemoteCallbackList<>();
    private final MusicController.Stub stub=new MusicController.Stub() {
        @Override
        public List<Music> of_getLocalMusicList() throws RemoteException {//获取本地的音乐列表
            return getLocalMusicList();
        }

        @Override
        public List<Music> of_getCurrentMusicList() throws RemoteException {//获取当期的播放列表
            return  musicList;
        }

        @Override
        public void of_setCurrentMusicList(List<Music> list) throws RemoteException {//设置当前的播放列表
        setMusicList((ArrayList<Music>) list);
        }

        @Override
        public boolean of_setCurrentPlayIndex(int position) throws RemoteException {//设置当前的播放下标
            if(!isSatisfyPlayCondition(position)){
                return false;
            }else{
                playingMusicIndex=position;
                return true;
            }
        }

        @Override
        public int of_getCurrentPlayIndex() throws RemoteException {//获取当前的播放下标
            return playingMusicIndex;
        }

        @Override
        public List<Music> of_getUsbMusicList() throws RemoteException {//获取U盘音乐列表(目前没实现)
            return null;
        }

        @Override
        public void of_playCurrentSelectedMusic(int position) throws RemoteException {//播放选中的音乐下标的歌曲
            if(isSatisfyPlayCondition(position)){
                playingMusicIndex=position;
                if(initMusic()){//如果初始化成功
                    if(mediaPlayer!=null){
                        mediaPlayer.start();
                        handler.post(runnable);
                        setForeground();//建立前台服务
                        NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
                    }
                }
            }
        }

        @Override
        public Music of_getPlayMusicInfo() throws RemoteException {//获取当前播放的音乐的信息
            if(isSatisfyPlayCondition(playingMusicIndex)){
                return musicList.get(playingMusicIndex);
            }
            return null;
        }

        @Override
        public boolean of_musicIsPlaying() throws RemoteException {//判断音乐是否正在播放
            if(mediaPlayer!=null){
                return mediaPlayer.isPlaying();
            }
            return false;
        }

        @Override
        public void of_startCurrentPlayIndexMusic() throws RemoteException {//播放当前音乐下标的歌曲
            if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                handler.post(runnable);
                setForeground();//建立前台服务
                NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
            }
        }
        @Override
        public void of_pauseCurrentPlayIndexMusic() throws RemoteException {//暂停当前的音乐下标的歌曲
            if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                NotificationChange(TOGGLEPAUSE_ACTION);//更新前台服务
               // handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void of_stopCurrentPlayIndexMusic() throws RemoteException {//停止当前音乐下标的歌曲
            if(mediaPlayer!=null){
                mediaPlayer.stop();

                ForegroundIsExist=false;
                mNotification=null;
                stopForeground(true);//关闭前台服务

                //handler.removeCallbacks(runnable);
            }
        }

        @Override
        public void of_nextMusic() throws RemoteException {//下一首歌
            nextMusic();
            NotificationChange(NEXT_ACTION);
        }

        @Override
        public void of_prevMusic() throws RemoteException {//上一首歌
            prevMusic();
            NotificationChange(PREVIOUS_ACTION);
        }

        @Override
        public int of_getDuration() throws RemoteException {//当前播放歌曲的总的时长
            try{
                if(mediaPlayer!=null){
                    if(isInitComplete){//如果第一次初始化服务完成（如果初始化没完成，调用getDuration方法会报错)
                        return mediaPlayer.getDuration();
                    }else{
                        return 0;
                    }
                }else{
                    return 0;
                }
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }

        }

        @Override
        public int of_getCurrentPosition() throws RemoteException {//当前播放歌曲的进度
            try{
                if(mediaPlayer!=null){
                    if(isInitComplete){//如果第一次初始化服务完成（如果初始化没完成，getCurrentPosition)
                        return mediaPlayer.getCurrentPosition();
                    }else{
                        return 0;
                    }

                }else{
                    return 0;
                }
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }

        }

        @Override
        public void of_setCurrent(int pos) throws RemoteException {//设置当前播放的歌曲的进度
            try{
                if(mediaPlayer!=null){
                    mediaPlayer.seekTo(pos);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void of_initMusicService() throws RemoteException {
            handler.sendEmptyMessage(INIT_MUSCIC_SERVICE);
        }

        @Override
        public boolean of_setPlayMode(int type) throws RemoteException {//设置播放模式

            if(type<0||type>playMode.length){
                return false;
            }else{
               currentPlayMode=playMode[type];
                return true;
            }
        }

        @Override
        public int of_getCurrentMusicListSize() throws RemoteException {//获取当前的音乐列表大小
           return  musicList.size();
        }

        @Override
        public List<String> of_getCurrentPlayMusicAllLyric() throws RemoteException {//获取当前播放歌曲的所有歌词
            return mLrcs;
        }

        @Override
        public String of_getCurrentPlayMusicOneLyric() throws RemoteException {// 获取当前播放歌曲正在播放的那一句歌词
            int currentPlayMusicLyricIndex=getCurrentPlayMusicLyricIndex();//获取当前播放的歌词的下标;
            if(currentPlayMusicLyricIndex>mLrcs.size()-1){
                return null;
            }else{
                return mLrcs.get(currentPlayMusicLyricIndex);
            }
        }

        @Override
        public int of_getCurrentPlayMusicOneLyricIndex() throws RemoteException {//获取当前播放歌曲正在播放的那一句歌词在所有歌词的下标
            return getCurrentPlayMusicLyricIndex();//获取当前播放的歌词的下标;
        }

        @Override
        public void of_setMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener) throws RemoteException {
            //设置播放进度监听
            musicPlayProgressListenerRemoteCallbackList.register(musicPlayProgressListener);
        }

        @Override
        public void of_cancelMusicPlayProgressListener(MusicPlayProgressListener musicPlayProgressListener) throws RemoteException {
            //取消播放进度监听
            musicPlayProgressListenerRemoteCallbackList.unregister(musicPlayProgressListener);
        }

        @Override
        public void of_setMusicListChangeListener(MusicListChangeListener musicListChangeListener) throws RemoteException {
            //设置音乐列表变化监听
            musicListChangeListenerRemoteCallbackList.register(musicListChangeListener);
        }

        @Override
        public void of_cancelMusicListChangeListener(MusicListChangeListener musicListChangeListener) throws RemoteException {
            //取消音乐列表变化监听
            musicListChangeListenerRemoteCallbackList.unregister(musicListChangeListener);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {//只有指定包名的应用才能调用服务
            String packageName=null;
            int callingPid=getCallingPid();
            int callingUid=getCallingUid();
            String[] packagesForUid=MusicAidlService.this.getPackageManager().getPackagesForUid(callingUid);
            if(packagesForUid!=null&&packagesForUid.length>0){
                packageName=packagesForUid[0];
            }

               if(TextUtils.isEmpty(packageName)||!"of.media.hz".contentEquals(packageName)){
                return  false;
               }


            return super.onTransact(code, data, reply, flags);
        }
    };






    private void handleCommandIntent(Intent intent) {

        String action=intent.getAction();
        //可能已经在桌面建立了widget，一启动后没初始化歌单列表
        if(musicList.size()==0)
        {
            App.sContext=getApplicationContext();
            MusicUtils.initMusicList();
            if(MusicUtils.sMusicList.size()>0)
            {
                setMusicList(MusicUtils.sMusicList);
                playingMusicIndex=0;
                initMusic();
            }
        }

        if(playingMusicIndex==-1)
        {
            OneToast.showMessage(MusicAidlService.this, "暂无歌曲");
            return;
        }
        //widget创建或者刷新第一步都要执行这动作
        if(SEND_PROGRESS.equals(action)){//发送现在音乐的一些信息
            handler.post(runnable);
        }else if(TOGGLEPAUSE_ACTION.equals(action)){//按下widget中间的播放按钮

            if(mediaPlayer.isPlaying()){
              //  handler.removeCallbacks(runnable);
            }else{
                handler.post(runnable);
            }
            startMusic();
            NotificationChange(TOGGLEPAUSE_ACTION);
        }else if(PREVIOUS_ACTION.equals(action)){//按下widget上一首按钮
            prevMusic();
            NotificationChange(PREVIOUS_ACTION);
        }else if(NEXT_ACTION.equals(action)){//按下widget下一首按钮
            nextMusic();
            NotificationChange(NEXT_ACTION);
        }else if(STOP_ACTION.equals(action)){//前台服务点击关闭按钮
            ForegroundIsExist=false;
            mNotification=null;
            stopMusic();
           // handler.removeCallbacks(runnable);
            stopForeground(true);
        }
        else if(WIDGET_LOVE_ACTION.equals(action)||NOTIFICATION_LOVE_ACTION.equals(action)){
        }else if(UPDATE_ACTION.equals(action)){
            NotificationChange(UPDATE_ACTION);
        }

    }

    private   void NotificationChange(final String what){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!ForegroundIsExist)
                {
                    return;
                }else  if(mNotification==null){
                    timer=new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(mNotification!=null||!ForegroundIsExist)
                            {
                                timer.cancel();
                                NotificationChange(what);
                            }
                        }
                    },100);
                    return;
                }
                if(TOGGLEPAUSE_ACTION.equals(what)){
                    if(mediaPlayer.isPlaying()){
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_pause_selector);
                    }else{
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_play_selector);
                    }
                }else if(PREVIOUS_ACTION.equals(what)||NEXT_ACTION.equals(what))
                {  String widget_title = musicList.get(playingMusicIndex).getTitle();
                    mNotification.contentView.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
                    if (musicList.get(playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
                        Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(playingMusicIndex).getImage());
                        if(bitmap!=null){
                            mNotification.contentView.setImageViewBitmap(R.id.widget_image, bitmap);
                        }else{
                            mNotification.contentView.setImageViewResource(R.id.widget_image, R.drawable.mp1);
                        }

                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_image, R.drawable.mp1);
                    }
                    //进度
                    //执行更新精度条的线程
                    handler.post(runnable);
                    if (mediaPlayer.isPlaying()) {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
                    }
                }else if(WIDGET_LOVE_ACTION.equals(what)||NOTIFICATION_LOVE_ACTION.equals(what)){

                }
                else if(UPDATE_ACTION.equals(what)){//更新喜欢的图标的状态
                }
                notificationManager.notify(NotificationId,mNotification);
            }
        }).start();
    }


    //初始前台服务
    private void initNotification(){
        String widget_title =musicList.get(playingMusicIndex).getTitle();
        widgetRemoteViews.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
        // widgetRemoteViews.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

        if (musicList.get(playingMusicIndex).getImage() != null) {//如果音乐专辑图片存在
            Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(playingMusicIndex).getImage());
            if(bitmap!=null){
                widgetRemoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
            }else{
                widgetRemoteViews.setImageViewResource(R.id.widget_image, R.drawable.mp1);
            }

        } else {
            widgetRemoteViews.setImageViewResource(R.id.widget_image, R.drawable.mp1);
        }
        //进度
        //执行更新精度条的线程
        handler.post(runnable);
        if (mediaPlayer.isPlaying()) {
            widgetRemoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
        } else {
            widgetRemoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
        }
    }

    private Notification  getmNotification(){
        final int PAUSE_FLAG = 0x1;
        final int NEXT_FLAG = 0x2;
        final int PREV_FLAG = 0x3;
        final  Context ForegroundContext=this;
        initNotification();
        //设置前台服务的绑定事件
        Intent pauseIntent = new Intent(TOGGLEPAUSE_ACTION);
        //pauseIntent.putExtra("FLAG", PAUSE_FLAG);
        PendingIntent pausePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, pauseIntent, 0);
        widgetRemoteViews.setOnClickPendingIntent(R.id.widget_play, pausePIntent);

        Intent nextIntent = new Intent(NEXT_ACTION);
        // nextIntent.putExtra("FLAG", NEXT_FLAG);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(ForegroundContext, 0, nextIntent, 0);
        widgetRemoteViews.setOnClickPendingIntent(R.id.widget_next, nextPIntent);

        Intent preIntent = new Intent(PREVIOUS_ACTION);
        // preIntent.putExtra("FLAG", PREV_FLAG);
        PendingIntent prePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, preIntent, 0);
        widgetRemoteViews.setOnClickPendingIntent(R.id.widget_pre, prePIntent);

        Intent stopIntent=new Intent(STOP_ACTION);
        PendingIntent stopPIntent=PendingIntent.getBroadcast(ForegroundContext,0,stopIntent,0);
        widgetRemoteViews.setOnClickPendingIntent(R.id.audio_stop,stopPIntent);


        if(mNotification==null){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
                String id ="channel_1";
                String description="143";
                int improtance=NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel=new NotificationChannel(id,description,improtance);
                channel.enableLights(true);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
                mNotification=new Notification.Builder(ForegroundContext,id).setContent(widgetRemoteViews)
                        .setSmallIcon(R.drawable.ic_notification)
                       .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                        .build();
                //.setWhen(System.currentTimeMillis()).setShowWhen(true)
            }
            else{
                NotificationCompat.Builder builder=new NotificationCompat.Builder(ForegroundContext).setContent(widgetRemoteViews).setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_notification).setWhen(System.currentTimeMillis());
                mNotification=builder.build();
            }

        }else{
            mNotification.contentView=widgetRemoteViews;
        }
        return mNotification;
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {//当关闭服务时
        try{
            ForegroundIsExist=false;
            mNotification=null;
            stopMusic();
            mediaPlayer.release();
            unregisterReceiver(mIntentReceiver);
            stopForeground(true);
            handler.removeCallbacks(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }






    // 解析时间
    private Long parseTime(String time) {
        // 03:02.12
        String[] min = time.split(":");
        String[] sec = min[1].split("\\.");

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        return minInt * 60 * 1000 + secInt * 1000 + milInt;
    }


    private int getCurrentPlayMusicLyricIndex(){ //获取当前播放的歌词的下标
        synchronized (this){
            int currentProgress=getMusicCurrentPosition();//获取当前播放歌曲的进度
            if(mTimes.size()==0){
                return 0;
            }else{
                int position=0;
                for(int i=0;i<mTimes.size();i++){
                    position=i;
                    if(currentProgress<mTimes.get(i)){
                        return (position==0)?0:position-1;
                    }
                }
                if(position>mTimes.size()-1){
                    position=mTimes.size()-1;
                }
                return position;
            }
        }
    }


    // 解析每行
    private String[] parseLine(String line) {
        Matcher matcher = Pattern.compile("\\[\\d.+\\].+").matcher(line);
        // 如果形如：[xxx]后面啥也没有的，则return空
        if (!matcher.matches()) {
            System.out.println("throws " + line);
            return null;
        }
        line = line.replaceAll("\\[", "");
        String[] result = line.split("\\]");
        result[0] = String.valueOf(parseTime(result[0]));
        return result;
    }



    //通过歌词路径获取歌词，将对应的时间和歌词分别用List集合存起来
    public  void getLrc(final  String path) {
        synchronized (this){
            mLrcs.clear();
            mTimes.clear();
            if(path==null){
                return;
            }
            final File file = new File(path);
            if (!file.exists()) {
                if(path.indexOf("http")!=-1){//是网络歌词路径
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                OkHttpClient okHttpClient=new OkHttpClient();
                                Request request=new Request.Builder().url(path).build();
                                Response response=okHttpClient.newCall(request).execute();
                                String lrc=response.body().string();
                                String[] lrcLine=lrc.split("\n");
                                String[] arr;
                                for(String line:lrcLine){
                                    arr = parseLine(line);
                                    if (arr == null) continue;

                                    // 如果解析出来只有一个
                                    if (arr.length == 1) {
                                        String last = mLrcs.remove(mLrcs.size() - 1);
                                        mLrcs.add(last + arr[0]);
                                        continue;
                                    }
                                    mTimes.add(Long.parseLong(arr[0]));
                                    mLrcs.add(arr[1]);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                        String line = "";
                        String[] arr;
                        while (null != (line = reader.readLine())) {
                            arr = parseLine(line);
                            if (arr == null) continue;

                            // 如果解析出来只有一个
                            if (arr.length == 1) {
                                String last = mLrcs.remove(mLrcs.size() - 1);
                                mLrcs.add(last + arr[0]);
                                continue;
                            }
                            mTimes.add(Long.parseLong(arr[0]));
                            mLrcs.add(arr[1]);
                        }
//                        Log.i("hasfdasdf", "路径:"+path);
//                        for(int i=0;i<mTimes.size();i++){
//                            Log.i("hasfdasdf", mTimes.get(i)+"   "+mLrcs.get(i));
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if(reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

}
