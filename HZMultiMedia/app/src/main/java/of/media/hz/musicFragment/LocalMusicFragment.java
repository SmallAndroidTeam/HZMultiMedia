package of.media.hz.musicFragment;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.of.music.songListInformation.Music;
import com.of.music.songListInformation.MusicController;
import com.of.music.songListInformation.MusicIconLoader;
import com.of.music.songListInformation.MusicPlayProgressListener;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import of.media.hz.R;
import of.media.hz.toast.OneToast;
import of.media.hz.ui.LrcView;
import of.media.hz.until.Format;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class LocalMusicFragment extends Fragment implements View.OnClickListener {

    private TextView musicTitle;
    private ImageView musicAlbum;
    private LrcView musicLyric;
    private ImageView prevImageView;
    private ImageView playImageView;
    private ImageView nextImageView;
    private TextView musicCurrentPosition;
    private TextView musicDuration;
    private SeekBar musicSeekbar;

    private MusicController musicController;
    private final static String TAG="hz111";
    private TextView musicArtist;
    private final static int INIT_UI=0;//初始化播放Ui界面
    private final static int UPDATE_PROGRESS=1;//更新进度条
    private int currentPlayIndex=-1;//当前的播放下标
    private boolean isPersonTouch=false;//判断是否为人为滑动进度条
    private boolean isResetBind=false;//是否重新绑定
    private List<Music> musicList=null;
    @SuppressLint("HandlerLeak")
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INIT_UI:
                    Music music= (Music) msg.obj;
                    if(music!=null){
                        musicTitle.setText(music.getTitle());//设置标题
                        musicArtist.setText(music.getArtist());//设置歌手名
                        if(music.getImage()!=null){//图片地址存在
                            if(music.getImage().toLowerCase().startsWith("http")){//如果是网络图片
                                Glide.with(Objects.requireNonNull(getContext())).load(music.getImage()).into(musicAlbum);
                            }else if(new File(music.getImage()).exists()){//如果是本地图片，且对应地址的文件存在
                                Bitmap bitmap = MusicIconLoader.getInstance().load(music.getImage());
                                musicAlbum .setImageBitmap(bitmap);
                            }else{//对应地址的文件不存在
                                   musicAlbum.setImageResource(R.drawable.mp1);
                            }
                        }else{
                            musicAlbum.setImageResource(R.drawable.mp1);
                        }
                    }
                    try {
                        if(musicController!=null){
                            musicCurrentPosition.setText(Format.changeToTime(musicController.of_getCurrentPosition()));//设置开始的进度
                            musicDuration.setText(Format.changeToTime(musicController.of_getDuration()));//设置总的播放时间
                            musicSeekbar.setMax(musicController.of_getDuration());//设置进度条的最大值
                            currentPlayIndex=musicController.getCurrentPlayIndex();//获取当前的播放下标
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UPDATE_PROGRESS://更新进度条
                    if(!isPersonTouch){//如果不是人为拖动进度条
                        musicSeekbar.setProgress((Integer) msg.obj);//设置进度条进度
                    }
                    musicCurrentPosition.setText(Format.changeToTime((Integer) msg.obj));//设置播放的时间
                        try {
                            if(musicController!=null) {
                                if (musicController.getCurrentPlayIndex() != currentPlayIndex){//如果播放下标改变，更新播放界面的信息
                                    Music music1=musicController.getPlayMusicInfo();//得到当前下标的音乐信息
                                    if(music1!=null){
                                        mhandler.obtainMessage(INIT_UI,music1).sendToTarget();
                                    }
                                }
                                if(musicSeekbar.getMax()==0){//如果进度条的最大值为0，则重新设置最大值
                                    musicSeekbar.setMax(musicController.of_getDuration());//设置进度条的最大值
                                    musicDuration.setText(Format.changeToTime(musicController.of_getDuration()));//设置总的播放时间
                                }

                                if(musicController.musicIsPlaying()){
                                    playImageView.setImageResource(R.drawable.play_imageview);
                                }else{
                                    playImageView.setImageResource(R.drawable.pause_imageview);
                                }

                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    break;
                    default:
                        break;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.local_music_fragment_item, container,false);
        initView(view);
        initEvents();
        bindService();
        initData();
        return view;
    }

    //adb push G:\Trinity_project\视频和音乐资源\music /stoage/emulated/0/music/
    //初始化数据
    private void initData() {


  new Timer().schedule(new TimerTask() {//通过一个定时器，当连接服务后更新播放界面信息
      @Override
      public void run() {
          if(musicController!=null){
              try {
                  musicController.setCurrentMusicList(musicController.getLocalMusicList());//设置当前的播放列为本地列表
                  musicController.setCurrentPlayIndex(0);//设置当前的音乐播放下标
                  musicController.setPlayMode(0);//设置当前的播放模式为顺序播放
                  musicController.initMusicService();//初始化服务
                  Music music=musicController.getPlayMusicInfo();//得到当前下标的音乐信息
                  if(music!=null){
                     mhandler.obtainMessage(INIT_UI,music).sendToTarget();
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
              this.cancel();
          }
      }
  },100,100);

    }


    //播放音乐
    private void staticMusic(){
        if(musicController!=null){
            try {
                musicController.startCurrentPlayIndexMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

   //暂停音乐
    private void  pauseMusic(){
        if(musicController!=null){
            try {
                musicController.pauseCurrentPlayIndexMusic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //设置播放模式 type={0 顺序模式 ,1 列表模式 ,2 单曲模式 ,3 随机模式}

    private void setPlayMode(int type){
        if(musicController!=null){
            try {
                musicController.setPlayMode(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //前一首
    private void prevMusic(){
        if(musicController!=null){
            try {

                musicController.of_prevMusic();
                playImageView.setImageResource(R.drawable.play_imageview);
                Music music=musicController.getPlayMusicInfo();//得到当前下标的音乐信息
                if(music!=null){
                    mhandler.obtainMessage(INIT_UI,music).sendToTarget();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //下一首
    private void nextMusic(){
        if(musicController!=null){
            try {
                musicController.of_nextMusic();
                playImageView.setImageResource(R.drawable.play_imageview);
                Music music=musicController.getPlayMusicInfo();//得到当前下标的音乐信息
                if(music!=null){
                    mhandler.obtainMessage(INIT_UI,music).sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //绑定服务
    private void bindService() {
        Intent intent=new Intent();
        intent.setPackage("com.of.music");
        intent.setAction("com.android.oflim.action");
        Objects.requireNonNull(getContext()).bindService(intent,serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private MusicPlayProgressListener musicPlayProgressListener=new MusicPlayProgressListener.Stub() {//进度条监听
        @Override
        public void musicProgressListener(int progress) throws RemoteException {
          mhandler.obtainMessage(UPDATE_PROGRESS,progress).sendToTarget();//更新进度条
        }
    };
    private ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicController= MusicController.Stub.asInterface(iBinder);
            try {
                //设置死亡代理,目的是防止断开连接
                musicController.asBinder().linkToDeath(deathRecipient,0);
                musicController.setMusicPlayProgressListener(musicPlayProgressListener);//添加播放进度监听

                //如果重新绑定服务
                //(服务主动关闭的过程：打开music,再打开HZMultiMedia应用，点击播放按钮，然后关闭前台服务，再关闭music应用，那么服务就会关闭了)
                if(isResetBind){
                    Log.i("ha1111", "onServiceConnected: "+musicController.getCurrentMusicListSize());
                    if(musicController.getCurrentMusicListSize()==0){//当前的播放列表为空（是由于服务主动关闭导致的，此时设置当前的播放列表为本地列表，且播放下标为1)
                        initData();
                    }
                    isResetBind=false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("ha1111", "onServiceDisconnected: ");
            try {
                if(musicController!=null)
                musicController.cancelMusicPlayProgressListener(musicPlayProgressListener);//取消播放进度监听
            } catch (Exception e) {
                e.printStackTrace();
            }
            musicController=null;
        }
    };

    /**
     * 监听Biner是否死亡，如果是则重新绑定
     */
    private  IBinder.DeathRecipient deathRecipient=new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i("ha1111", "binderDied: ");
            if(musicController==null){
                return;
            }
            isResetBind=true;//重新绑定字段
            musicController.asBinder().unlinkToDeath(deathRecipient,0);
            musicController=null;
            //重新绑定
            Log.i("ha1111", "binderDied: 重新绑定");
            bindService();

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(musicController!=null&&musicPlayProgressListener.asBinder().isBinderAlive()) {
            try {
                musicController.cancelMusicPlayProgressListener(musicPlayProgressListener);//取消播放进度监听
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Objects.requireNonNull(getContext()).unbindService(serviceConnection);
    }

    private void initView(View view) {
        musicTitle = view.findViewById(R.id.musicTitle);
        musicArtist = view.findViewById(R.id.musicArtist);
        musicAlbum = view.findViewById(R.id.musicAlbum);
        musicLyric = view.findViewById(R.id.musicLyric);
        prevImageView = view.findViewById(R.id.prevImageView);
        playImageView = view.findViewById(R.id.playImageView);
        nextImageView = view.findViewById(R.id.nextImageView);
        musicCurrentPosition = view.findViewById(R.id.musicCurrentPosition);
        musicDuration = view.findViewById(R.id.musicDuration);
        musicSeekbar = view.findViewById(R.id.musicSeekbar);
    }

    private void initEvents() {
        musicAlbum.setOnClickListener(this);
        musicLyric.setOnClickListener(this);
        playImageView.setOnClickListener(this);
        prevImageView.setOnClickListener(this);
        nextImageView.setOnClickListener(this);
        musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//设置进度条拖动监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPersonTouch=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
              if(musicController!=null){
                  try {
                      musicController.of_setCurrent(musicSeekbar.getProgress());
                  } catch (RemoteException e) {
                      e.printStackTrace();
                  }
              }
                isPersonTouch=false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.musicAlbum:
                musicLyric.setVisibility(View.VISIBLE);
                musicAlbum.setVisibility(View.GONE);
                break;
            case R.id.musicLyric:
                musicLyric.setVisibility(View.GONE);
                musicAlbum.setVisibility(View.VISIBLE);
                break;
            case R.id.playImageView:
                if(musicController!=null){
                    try {
                        if(musicController.musicIsPlaying()){
                            pauseMusic();
                            playImageView.setImageResource(R.drawable.pause_imageview);
                        }else{
                            if(musicController.getCurrentMusicListSize()==0){//判断当前音乐列表是否为空
                            OneToast.showMessage(getContext(),"当前无歌曲");
                            }else{
                                staticMusic();
                                playImageView.setImageResource(R.drawable.play_imageview);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.prevImageView:
                if(musicController!=null){
                    try {
                        if(musicController.getCurrentMusicListSize()==0){//判断当前音乐列表是否为空
                            Log.i(TAG, "onClick: 22");
                         OneToast.showMessage(getContext(),"当前无歌曲");
                        }else{
                            prevMusic();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.nextImageView:
                if(musicController!=null){
                    try {
                        if(musicController.getCurrentMusicListSize()==0){//判断当前音乐列表是否为空
                            Log.i(TAG, "onClick: 11");
                           OneToast.showMessage(getContext(),"当前无歌曲");
                        }else{
                            nextMusic();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default:
                    break;


        }
    }
}
