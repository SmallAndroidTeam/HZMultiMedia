package of.media.hz.services;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import of.media.hz.Music;
import of.media.hz.R;
import of.media.hz.db.MusicOperator;
import of.media.hz.info.FavouriteMusicListInfo;
import of.media.hz.info.MusicName;
import of.media.hz.musicFragment.LocalMusicFragment;
import of.media.hz.toast.OneToast;
import of.media.hz.until.MusicIconLoader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class MusicService extends Service {
    public static final  String SEND_PROGRESS="com.of.music.progress";//更新widget上的进度条发出的广播action
    public static final  String TOGGLE_ACTION="com.of.music.toggle";//前台服务，widget和播放界面点击控制按钮发出的action
    public static final String PREVIOUS_ACTION="com.of.music.previous";//前台服务，widget和播放界面点击前一首按钮发出的action
    public  static final  String NEXT_ACTION="com.of.music.next";//前台服务，widget和播放界面点击下一首按钮发出的action
    public static final String LOVE_ACTION="com.of.music.love";//前台服务，widget和播放界面点击收藏按钮发出的action
    public static final  String STOP_ACTION="com.of.music.stop";//点击前台服务点击关闭按钮时发出的action
    public final static String FLAG="autoplay";//自动播放的标志
    public final  static String SEND_MUSIC_SERVICE_FLAG="UserOperation";//目的是为了判断是通过操作播放界面而实现的音乐动作还是通过广播实现的
    private static MediaPlayer mMediaPlayer;
    private static List<Music> musicList=new ArrayList<>();//歌曲列表
    private  static int currentPosition=-1;//当前的播放下标
    private static Control mControl;
    private static  List<String> mLrcs = new ArrayList<String>(); // 存放歌词
    private  static  List<Long> mTimes = new ArrayList<Long>(); // 存放时间
    public final static String[] playMode=new String[]{"顺序播放","列表循环","单曲循环","随机播放"};//播放模式
    public static   String currentPlayMode=playMode[0];//当前的音乐播放模式
    private  static  boolean isExecutionMediaPrepare=false;//判断是否正在执行mMediaPlayer中的prepare函数
    private RemoteViews remoteViews;//前台服务中的布局文件
    private NotificationManager notificationManager;
    private   Notification mNotification;
    private  final int NotificationId=1000;
    private  boolean ForegroundIsExist=false;//判断前台服务是否存在
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BroadcastReceiver mIntentReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String processName=getProcessName();
            //判断进程名
            if(!TextUtils.isEmpty(processName)&&processName.contentEquals(getPackageName()))
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
        super.onCreate();
        if(mMediaPlayer==null){
            mMediaPlayer=new MediaPlayer();
        }
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(TOGGLE_ACTION);
        intentFilter.addAction(PREVIOUS_ACTION);
        intentFilter.addAction(NEXT_ACTION);
        intentFilter.addAction(LOVE_ACTION);
        intentFilter.addAction(STOP_ACTION);
        registerReceiver(mIntentReceiver,intentFilter);
        remoteViews=new RemoteViews(getPackageName(), R.layout.notification);
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                autoPlayMusic();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                nextMusic();
                return false;
            }
        });
    }

    public static void setmControl(Control mControl) {
        MusicService.mControl = mControl;
    }

    //初始化服务
    public static boolean initMusicService(List<Music> list, int Position){
        if(list==null||list.size()==0||Position<0||Position>list.size()-1){
            return false;
        }
        if(mMediaPlayer==null){
            mMediaPlayer=new MediaPlayer();
        }
        musicList.clear();
        musicList.addAll(list);
        currentPosition=Position;
        deleteNoExistLocalMusic();
        if(!isSatisfyPlayCondition()){//如果不满足播放条件
            return false;
        }
        getLrc(musicList.get(currentPosition).getLrcpath());//开启线程获取当前的歌词
        isExecutionMediaPrepare=true;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(musicList.get(currentPosition).getUri());
            mMediaPlayer.prepare();
            isExecutionMediaPrepare=false;
        } catch (IOException e) {
            e.printStackTrace();
            isExecutionMediaPrepare=false;
            return false;
        }
        return true;
    }

    /**
     * 设置当前的播放模式
     * @param type
     * type=0:顺序模式
     * type=1:列表循环
     * type=2:单曲循环
     * type=3:随机播放
     */
    public static boolean  setPlayMode(int type){
        if(type<0||type>3){
            return false;
        }else{
            currentPlayMode=playMode[type];
            return true;
        }
    }

    /**
     * 得到所有的播放模式
     * @return
     */
    public static String[] getPlayMode() {
        return playMode;
    }

    /**
     * 得到当前的播放模式
     * @return
     * 0:顺序模式
     * 1:列表循环
     * 2:单曲循环
     * 3:随机播放
     * -1:错误
     */
    public static int getCurrentPlayMode(){
        for(int i=0;i<playMode.length;i++){
            if(currentPlayMode.contentEquals(playMode[i])){
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断当前是否满足播放条件
     * @param
     * @return
     */
    public static boolean isSatisfyPlayCondition(){
        if(musicList==null||musicList.size()==0||currentPosition<0||currentPosition>(musicList.size()-1)&&mMediaPlayer!=null){
            return false;
        }else{
            return true;
        }
    }



    /**
     * 得到下一首的音乐下标
     * 返回值为是否继续播放，true继续播放，false停止播放
     * @return
     */
    private boolean getNextMusicIndex(){
        if(isSatisfyPlayCondition()){
            int type=getCurrentPlayMode();//获取当前的播放模式 ，type=0:顺序模式，type=1:列表循环，type=2:单曲循环，type=3:随机播放,type=-1：错误
                 switch (type){
                     case 0://顺序模式
                            if(currentPosition>=musicList.size()){//如果已经播放到最后一首歌就停止播放了
                                return false;
                            }else{
                                currentPosition=(currentPosition>=(musicList.size()-1))?0:(currentPosition+1);
                            }
                         break;
                     case 1://列表循环
                             currentPosition=(currentPosition>=(musicList.size()-1))?0:(currentPosition+1);
                         break;
                     case 2://单曲循环

                         break;
                     case 3://随机播放
                         int random=new Random().nextInt(musicList.size());
                         while(random==currentPosition){//如何获取的随机数和当前的播放下标一致，就换一个，因为下一首和上一首是不同的歌
                             random=new Random().nextInt(musicList.size());
                         }
                         currentPosition=random;
                         break;
                         default:
                             currentPosition=0;
                             break;
                 }
                 return true;
        }else{
            currentPosition=-1;
            return false;
        }
    }



    /**
     * 获取当前的音乐列表
     * @return
     */
    public static List<Music> getCurrentMusicList() {
        return musicList;
    }

    /**
     * 获取当前的音乐下标的对象
     * @return
     */
    public  static Music getCurrentMusic(){
        if(isSatisfyPlayCondition()){
           return musicList.get(currentPosition);
        }else{
            return null;
        }
    }

    /**
     * 获取当前音乐标题
     * @return
     */
    public  static String getCurrentMusicTitle(){
        if(isSatisfyPlayCondition()){
       return  musicList.get(currentPosition).getTitle();
        }else{
            return null;
        }
    }

    /**
     *获取当前的音乐歌手名
     * @return
     */
    public  static String getCurrentMusicArtist(){
        if(isSatisfyPlayCondition()){
     return musicList.get(currentPosition).getArtist();
          }else{
     return null;
        }
    }

    /**
     * 获取当前的音乐专辑图片路径（可能是网络图片）
     * @return
     */
   public static String getCurrentMusicAlbum(){
        if(isSatisfyPlayCondition()){
            return musicList.get(currentPosition).getImage();
        }else{
            return null;
        }
   }

    /**
     * 获取当前的音乐歌词路径
     * @return
     */
    public static  String getCurrentMusicLyric(){
         if(isSatisfyPlayCondition()){
             if(!musicList.get(currentPosition).getLrcpath().toLowerCase().startsWith("http")){//如果是本地歌词
                 if(!new File(musicList.get(currentPosition).getLrcpath()).exists()){
                    return null;
                 }
             }
             return musicList.get(currentPosition).getLrcpath();

         }else {
             return null;
         }
    }




    /**
     * 获取当前的音乐是否添加收藏了
     * @return
     */
   public static boolean getCurrentMusicIsAddLove(){
       synchronized (new Object()){
           if(isSatisfyPlayCondition()){
               return MusicOperator.getInstatce().CheckIsDataAlreadyInDBorNot(musicList.get(currentPosition).getTitle());//通过数据库获取判断的
           }else{
               return false;
           }
       }
   }
    /**
     * 获取当前的音乐进度
     * @return
     */
    public static int getMusicCurrentPosition(){
        if(mMediaPlayer!= null){
          return   mMediaPlayer.getCurrentPosition();
        }else{
            return 0;
        }
    }

    /**
     * 获取当前的音乐时间
     * @return
     */
    public  static int getCurrentDuration(){
        if(mMediaPlayer!= null&&!isExecutionMediaPrepare){//播放器已经prepare过了
            return   mMediaPlayer.getDuration();
        }else{
            return 0;
        }
    }

    /**
     * 设置播放进度
     * @param position
     */
    public static void setMusicCurrentPosition(int position){
        if(mMediaPlayer!=null&&!isExecutionMediaPrepare){
            mMediaPlayer.seekTo(position);
        }
    }

    /**
     * 设置当前的播放列表
     * @param musicList
     */
    public static void setMusicList(List<Music> musicList) {
        MusicService.musicList.clear();
        MusicService.musicList.addAll(musicList);
    }

    /**
     * 设置当前的播放下标
     * @param currentPosition
     */
    public static void setCurrentPosition(int currentPosition) {
        MusicService.currentPosition = currentPosition;
    }

    /**
     * 获取当前的播放下标
     * @param
     */
    public static int getCurrentPosition() {
        return currentPosition;
    }

    /**
     *从当前的下标开始，删除不存在的本地音乐，直到当前下标的音乐地址存在
     */
    public static synchronized  void deleteNoExistLocalMusic(){
        if(isSatisfyPlayCondition()){
            String musicAddress=musicList.get(currentPosition).getUri();
            while(musicAddress==null||!musicAddress.toLowerCase().startsWith("http")&&!new File(musicAddress).exists()){
                 musicList.remove(currentPosition);
                 if(musicList.size()==0){//如果删除之后歌曲列表为空的话
                     currentPosition=-1;
                     return;
                 }else{
                     currentPosition=(currentPosition>(musicList.size()-1))?0:currentPosition;
                     musicAddress=musicList.get(currentPosition).getUri();
                 }
            }
        }
    }




    /**
     *    判断音乐列表是否为空
     */
    public static boolean isExistMusics(){
        if(musicList==null){
            return false;
        }
        return musicList.size()!=0;
    }

    /**
     * 开始播放音乐
     */
     private void startMusic(){
        if(isSatisfyPlayCondition()){
            mMediaPlayer.start();
            if(mControl!=null){
                mControl.playButton(1);
            }
            NotificationChange(TOGGLE_ACTION);
        }
     }

    /**
     * 暂停播放
     */
    private void pauseMusic(){
         if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
             mMediaPlayer.pause();
             if(mControl!=null){
                 mControl.playButton(0);
             }
             NotificationChange(TOGGLE_ACTION);
         }
     }

    /**
     * 停止播放
     */
    private void stopMusic(){
        if(mMediaPlayer!=null){
            isExecutionMediaPrepare=true;
            mMediaPlayer.stop();
            try {
                mMediaPlayer.prepare();
                mMediaPlayer.seekTo(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(mControl!=null){
                mControl.playButton(0);
            }

            ForegroundIsExist=false;
            mNotification=null;
            stopForeground(true);//停止前台服务
            isExecutionMediaPrepare=false;
        }
     }

    /**
     * 播放下一首
     */
    private void nextMusic(){
        if(isSatisfyPlayCondition()){//如果满足播放条件
            int currentMusicIndex=currentPosition;//获取当前的播放下标
            getNextMusicIndex();//获取下一首歌音乐下标
            if(currentPosition==currentMusicIndex&&musicList.size()>1){//如果下一首的音乐下标和上一首的音乐下标一样，则下标还要进行+1操作（防止是播放模式的原因导致是同一个下标）
                currentPosition=(currentPosition>=(musicList.size()-1))?0:(currentPosition+1);
            }
            deleteNoExistLocalMusic();
            if(isSatisfyPlayCondition()){
                if(currentMusicIndex!=currentPosition)//如果和前一首下标不一致就获取歌词
                getLrc(musicList.get(currentPosition).getLrcpath());//开启线程获取当前的歌词
                isExecutionMediaPrepare=true;
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(musicList.get(currentPosition).getUri());
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();

                    if(mControl!=null){
                        mControl.playButton(1);
                        mControl.updateUI();
                    }

                    NotificationChange(NEXT_ACTION);
                } catch (IOException e) {
                    e.printStackTrace();
                    OneToast.showMessage(getApplicationContext(),"无法播放");
                }
                isExecutionMediaPrepare=false;
            }else{
                stopMusic();
                OneToast.showMessage(getApplicationContext(),"暂无歌曲1");
            }

        }else{//否则会停止播放
            stopMusic();
            OneToast.showMessage(getApplicationContext(),"暂无歌曲2");
        }
     }

    /**
     * 播放上一首
     * 0:顺序模式
     * 1:列表循环
     * 2:单曲循环
     * 3:随机播放
     * -1:错误
     */

    private void prevMusic(){
        if(isSatisfyPlayCondition()){
            int type=getCurrentPlayMode();
             switch (type){
                 case 0:
                 case 1:
                 case 2:
                   currentPosition=currentPosition==0?(musicList.size()-1):(currentPosition-1);
                     break;
                 case 3:
                     int random=new Random().nextInt(musicList.size());
                     while(random==currentPosition){//如何获取的随机数和当前的播放下标一致，就换一个，因为下一首和上一首是不同的歌
                         random=new Random().nextInt(musicList.size());
                     }
                     currentPosition=random;
                     break;
                     default:
                         currentPosition=0;
                         break;
             }

             deleteNoExistLocalMusic();
             if(isSatisfyPlayCondition()){
                 getLrc(musicList.get(currentPosition).getLrcpath());//开启线程获取当前的歌词
                 isExecutionMediaPrepare=true;
                 try {
                     mMediaPlayer.reset();
                     mMediaPlayer.setDataSource(musicList.get(currentPosition).getUri());
                     mMediaPlayer.prepare();
                     mMediaPlayer.start();
                     if(mControl!=null){
                         mControl.playButton(1);
                         mControl.updateUI();
                     }
                     NotificationChange(PREVIOUS_ACTION);
                 } catch (IOException e) {
                     e.printStackTrace();
                     OneToast.showMessage(getApplicationContext(),"无法播放");
                 }
                 isExecutionMediaPrepare=false;
             }else{
                 stopMusic();
                 OneToast.showMessage(getApplicationContext(),"暂无歌曲");
             }

        }else{
            stopMusic();
            OneToast.showMessage(getApplicationContext(),"暂无歌曲");
        }
     }

    /**
     * 自动播放歌曲
     */
    private void autoPlayMusic(){
        if(isSatisfyPlayCondition()){//如果满足播放条件
            int currentMusicIndex=currentPosition;//获取当前的播放下标
            boolean isContinuePlay=getNextMusicIndex();//获取下一首歌音乐下标,返回值为是否继续播放，还是停止播放
            deleteNoExistLocalMusic();
            if(isSatisfyPlayCondition()){
                  if(currentMusicIndex!=currentPosition){//如果和前一首下标不一致就获取歌词
                      getLrc(musicList.get(currentPosition).getLrcpath());//开启线程获取当前的歌词
                  }
                  if(isContinuePlay){//如果继续播放
                      isExecutionMediaPrepare=true;
                      try {
                          mMediaPlayer.reset();
                          mMediaPlayer.setDataSource(musicList.get(currentPosition).getUri());
                          mMediaPlayer.prepare();
                          mMediaPlayer.start();
                          if(mControl!=null){
                              mControl.playButton(1);
                              mControl.updateUI();
                          }
                          NotificationChange(NEXT_ACTION);
                      } catch (IOException e) {
                          e.printStackTrace();
                          OneToast.showMessage(getApplicationContext(),"无法播放");
                      }
                      isExecutionMediaPrepare=false;
                  }else{
                      stopMusic();
                      OneToast.showMessage(getApplicationContext(),"暂无歌曲");
                  }
                  }else{
                stopMusic();
            }
        }else{//否则会停止播放
            stopMusic();
            OneToast.showMessage(getApplicationContext(),"暂无歌曲");
        }

     }

    /**
     * 添加收藏
     */
    private  void addLoveMusic(){
        synchronized (this){
            if(isSatisfyPlayCondition()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int musicPosition=currentPosition;
                        String song=musicList.get(musicPosition).getTitle();
                        String artist=musicList.get(musicPosition).getArtist();
                        String song_Image=musicList.get(musicPosition).getImage();
                        String uri=musicList.get(musicPosition).getUri();
                        String Lrc_uri=musicList.get(musicPosition).getLrcpath();
                     if(MusicOperator.getInstatce().CheckIsDataAlreadyInDBorNot(song)){//如果是喜欢的音乐则取消喜欢
                        MusicOperator.getInstatce().delete(song);
                         LitePal.deleteAll(FavouriteMusicListInfo.class,"name=?",song);
                         if(mControl!=null){
                             mControl.updateLoveButton(musicPosition,0);
                         }
                     }else{
                         MusicName lxr=new MusicName(song,artist,song_Image,uri,Lrc_uri);
                         MusicOperator.getInstatce().add(lxr);
                         //LitePal框架存储到数据库
                         FavouriteMusicListInfo favouriteMusicListInfo1 = new FavouriteMusicListInfo();
                         favouriteMusicListInfo1.setName(song);
                         favouriteMusicListInfo1.setArtist(artist);
                         favouriteMusicListInfo1.setImage(song_Image);
                         favouriteMusicListInfo1.setUri(uri);
                         favouriteMusicListInfo1.setLrc_uri(Lrc_uri);
                         favouriteMusicListInfo1.save();
                         if(mControl!=null){
                             mControl.updateLoveButton(musicPosition,1);
                         }
                     }
                        NotificationChange(LOVE_ACTION);
                    }
                }).start();
            }
        }
    }

    /**
     * 获取音乐是否正在播放
     * @return
     */
     public  static   boolean isPlaying(){
        if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
            return true;
        }else
        {
            return false;
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

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(mNotification!=null||!ForegroundIsExist)
                            {
                                this.cancel();
                                NotificationChange(what);
                            }
                        }
                    },100);
                    return;
                }
                if(TOGGLE_ACTION.equals(what)){
                    if(isPlaying()){
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_pause_selector);
                    }else{
                        mNotification.contentView.setImageViewResource(R.id.widget_play,R.drawable.widget_play_selector);
                    }
                }else if(PREVIOUS_ACTION.equals(what)||NEXT_ACTION.equals(what))
                {  String widget_title = musicList.get(currentPosition).getTitle();
                    mNotification.contentView.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
                    if (musicList.get(currentPosition).getImage() != null) {//如果音乐专辑图片存在
                        Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(currentPosition).getImage());
                        if(bitmap!=null){
                            mNotification.contentView.setImageViewBitmap(R.id.widget_image, bitmap);
                        }else{
                            mNotification.contentView.setImageViewResource(R.id.widget_image, R.drawable.mp1);
                        }

                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_image, R.drawable.mp1);
                    }
                    if (isPlaying()) {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
                    } else {
                        mNotification.contentView.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
                    }
                    if(getCurrentMusicIsAddLove()){
                        remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                    }else{
                        remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                    }
                }else if(LOVE_ACTION.equals(what)){
                    if(getCurrentMusicIsAddLove()){
                        remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
                    }else{
                        remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image);
                    }
                }
                notificationManager.notify(NotificationId,mNotification);
            }
        }).start();
    }


    //初始前台服务
    private boolean initNotification(){
        if(!isSatisfyPlayCondition())
        {
            return false;
        }else{
            String widget_title =musicList.get(currentPosition).getTitle();
            remoteViews.setTextViewText(R.id.widget_content, widget_title);//设置歌曲名
            // widgetRemoteViews.setProgressBar(R.id.widget_progress, mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), false);

            if (musicList.get(currentPosition).getImage() != null) {//如果音乐专辑图片存在
                Bitmap bitmap = MusicIconLoader.getInstance().load(musicList.get(currentPosition).getImage());
                if(bitmap!=null){
                    remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
                }else{
                    remoteViews.setImageViewResource(R.id.widget_image, R.drawable.mp1);
                }

            } else {
                remoteViews.setImageViewResource(R.id.widget_image, R.drawable.mp1);
            }
            if (isPlaying()) {
                remoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_pause_selector);
            } else {
                remoteViews.setImageViewResource(R.id.widget_play, R.drawable.widget_play_selector);
            }

            if(getCurrentMusicIsAddLove()){
                remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image_selected);
            }else{
                remoteViews.setImageViewResource(R.id.widget_love,R.drawable.like_image);
            }
            return true;
        }

    }


    private Notification  getmNotification(){

        final  Context ForegroundContext=this;
        if(!initNotification()){//如果初始化服务失败
            return null;
        }
        //设置前台服务的绑定事件
        Intent pauseIntent = new Intent(TOGGLE_ACTION);
        //pauseIntent.putExtra("FLAG", PAUSE_FLAG);
        PendingIntent pausePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, pauseIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_play, pausePIntent);

        Intent nextIntent = new Intent(NEXT_ACTION);
        // nextIntent.putExtra("FLAG", NEXT_FLAG);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(ForegroundContext, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_next, nextPIntent);

        Intent preIntent = new Intent(PREVIOUS_ACTION);
        // preIntent.putExtra("FLAG", PREV_FLAG);
        PendingIntent prePIntent = PendingIntent.getBroadcast(ForegroundContext, 0, preIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_pre, prePIntent);

        Intent stopIntent=new Intent(STOP_ACTION);
        PendingIntent stopPIntent=PendingIntent.getBroadcast(ForegroundContext,0,stopIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.audio_stop,stopPIntent);

        Intent loveIntent=new Intent(LOVE_ACTION);
        PendingIntent lovePIntent=PendingIntent.getBroadcast(ForegroundContext,0,loveIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.widget_love,lovePIntent);
        Intent clickIntent=new Intent();
        clickIntent.setAction(Intent.ACTION_MAIN);
        clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        clickIntent.setComponent(new ComponentName(getPackageName(),"of.media.hz.activity.MainActivity"));
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent clickPIntent=PendingIntent.getActivity(ForegroundContext,0,clickIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        if(mNotification==null){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){//sdk版本大于26
                String id ="channel_2";
                String description="144";
                int improtance=NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel=new NotificationChannel(id,description,improtance);
                channel.enableLights(true);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
                mNotification=new Notification.Builder(ForegroundContext,id).setContent(remoteViews)
                        .setSmallIcon(R.drawable.ic_notification).setContentIntent(clickPIntent)
                        .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                        .build();
                //.setWhen(System.currentTimeMillis()).setShowWhen(true)
            }
            else{
                NotificationCompat.Builder builder=new NotificationCompat.Builder(ForegroundContext).setContent(remoteViews).setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_notification).setContentIntent(clickPIntent).setWhen(System.currentTimeMillis());
                mNotification=builder.build();
            }

        }else{
            mNotification.contentView=remoteViews;
        }
        return mNotification;
    }






//    public static final  String SEND_PROGRESS="com.of.music.progress";//更新widget上的进度条发出的广播action
//    public static final  String TOGGLE_ACTION="com.of.music.toggle";//前台服务，widget和播放界面点击控制按钮发出的action
//    public static final String PREVIOUS_ACTION="com.of.music.previous";//前台服务，widget和播放界面点击前一首按钮发出的action
//    public  static final  String NEXT_ACTION="com.of.music.next";//前台服务，widget和播放界面点击下一首按钮发出的action
//    public static final String LOVE_ACTION="com.of.music.love";//前台服务，widget和播放界面点击收藏按钮发出的action
//    public static final  String STOP_ACTION="com.of.music.stop";//点击前台服务点击关闭按钮时发出的action
//    public final static String FLAG="autoplay";//自动播放的标志
//    public final  static String SEND_MUSIC_SERVICE_FLAG="UserOperation";//目的是为了判断是通过操作播放界面而实现的音乐动作还是通过广播实现的
    private void  handleCommandIntent(Intent intent) {
       if(intent==null||intent.getAction()==null){
           return;
       }
       String action=intent.getAction();
        if(isSatisfyPlayCondition()){

           if(action.contentEquals(TOGGLE_ACTION)){//切换播放状态
               if(isPlaying()){
                   pauseMusic();
               }else{
                   startMusic();
               }
           }else if(action.contentEquals(STOP_ACTION)){//停止播放
               stopMusic();
           }else if(action.contentEquals(PREVIOUS_ACTION)){
                 prevMusic();

           }else if(action.contentEquals(NEXT_ACTION)){
               nextMusic();

           }else if(action.contentEquals(LOVE_ACTION)){
               addLoveMusic();
           }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null||intent.getAction()==null){
            return super.onStartCommand(intent, flags, startId);
        }
        if(!isExistMusics()){
            OneToast.showMessage(getApplicationContext(),"暂无歌曲");
        }else{
            if(isSatisfyPlayCondition()){
                if(!ForegroundIsExist)
                { new Thread(new Runnable() {
                    @Override
                    public void run() {
                         if(getmNotification()!=null){
                             startForeground(NotificationId,getmNotification());
                             ForegroundIsExist=true;
                         }
                    }
                }).start();
                }
                handleCommandIntent(intent);
            }else{
                OneToast.showMessage(getApplicationContext(),"无法播放");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    // 解析时间
    private static Long parseTime(String time) {
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


  public static String changeTime(int digit){//将播放时间转变为00:00的格式
      if(digit<0){
          return "00:00";
      }else{
          @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat=new SimpleDateFormat("mm:ss");
          return simpleDateFormat.format(digit);
      }

    }


    //获取歌词
    public static List<String> getmLrcs() {
        return mLrcs;
    }

    public static  int getCurrentPlayMusicLyricIndex(){ //获取当前播放的歌词的下标
        synchronized (new Object()){
            int currentProgress=getMusicCurrentPosition();//获取当前播放歌曲的进度
            if(mTimes.size()==0){
                return -1;
            }else{
                int position=0;
                for(int i=0;i<mTimes.size();i++){
                    position=i;
                    if(currentProgress<mTimes.get(i)){
                        return (position==0)?0:(position-1);
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
    private static String[] parseLine(String line) {
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
    public  static  void getLrc(final  String path) {
        synchronized (new Object()){
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

    public interface Control{
        void playButton(int index);//改变播放按钮的形状(0暂停，1,播放）
        void updateUI();//更新播放界面的信息
        void updateLoveButton(int musicPosition,int type);//改变收藏按钮的形状（type:0没收藏，1收藏)(muscicPostion:添加收藏的音乐下标)
    }

}
