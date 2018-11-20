package of.media.hz.musicFragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


import of.media.hz.R;
import of.media.hz.adapter.LryicAdapter;
import of.media.hz.model.Imusic;
import of.media.hz.services.MusicAidlService;
import of.media.hz.services.MusicService;
import of.media.hz.toast.OneToast;
import of.media.hz.until.MusicIconLoader;
import of.media.hz.until.MusicUtils;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class LocalMusicFragment extends Fragment implements View.OnClickListener, MusicService.Control {

    private TextView musicTitle;
    private ImageView musicAlbum;
    private ListView musicLyric;
    private ImageView prevImageView;
    private ImageView playImageView;
    private ImageView nextImageView;
    private TextView musicCurrentPosition;
    private TextView musicDuration;
    private SeekBar musicSeekbar;
    private final static String TAG="hz11111";
    private TextView musicArtist;
    private final static int INIT_UI=0;//初始化播放Ui界面
    private final static int UPDATE_PROGRESS=1;//更新进度条
    private final static int UPDATE_LRYIC=2;//更新歌词进度
    private final static int UPDATE_LOVE_BUTTON=3;//更新收藏按钮
    private final static  int UPDATE_PLAY_MODE=4;//更新播放模式图标
    private final static int SCROLL_CURRENT_LRYIC_POSITION=5;//滚动到当前歌词的位置
    private int currentPlayIndex=-1;//当前的播放下标
    private boolean isPersonTouch=false;//判断是否为人为滑动进度条
    private boolean isResetBind=false;//是否重新绑定
    private  final List<String> noLrcs=new ArrayList<>();//无歌词
    private  ObjectAnimator roateAnimation;
    private LryicAdapter lryicAdapter;
    private ImageView addLikeImage;
    private ImageView playModel;
    private float motion_y;//点击歌词界面的初始Y轴距离
    private boolean lryicPersonScroll=false;//歌词界面是否人为滚动，如果是，则歌词界面不会自动滚动到当前播放的那一行，知道没有人为滚动
    private  int lryicScrollDelayTime=3000;//给出滚动到当前歌词位置的延迟时间
    private final static int[] modeImage=new int[]{R.drawable.list_play,R.drawable.list_cycle,R.drawable.single_cycle,R.drawable.random_play};//对应播放模式的图标
    @SuppressLint("HandlerLeak")
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INIT_UI:
                    //设置标题
                    if(MusicService.getCurrentMusicTitle()!=null){
                        musicTitle.setText(MusicService.getCurrentMusicTitle());
                    }else{
                        musicTitle.setText(getResources().getString(R.string.unknown));
                    }
                    //设置歌手名
                    if(MusicService.getCurrentMusicArtist()!=null){
                        musicArtist.setText(MusicService.getCurrentMusicArtist());
                    }else{
                        musicArtist.setText(getResources().getString(R.string.unknown));
                    }
                    //设置专辑
                    if(MusicService.getCurrentMusicAlbum()!=null){
                        if(Objects.requireNonNull(MusicService.getCurrentMusicAlbum()).toLowerCase().startsWith("http")){//如果是网络图片
                            Glide.with(Objects.requireNonNull(getContext())).load(MusicService.getCurrentMusicAlbum()).into(musicAlbum);
                        }else{
                            Bitmap bitmap= MusicIconLoader.getInstance().load(MusicService.getCurrentMusicAlbum());
                            if(bitmap!=null){
                                musicAlbum.setImageBitmap(bitmap);
                            }else{
                                musicAlbum.setImageResource(R.drawable.mp1);
                            }
                        }
                    }else{
                        musicAlbum.setImageResource(R.drawable.mp1);
                    }

                    if(lryicAdapter==null){
                        lryicAdapter=new LryicAdapter();
                        musicLyric.setAdapter(lryicAdapter);
                    }
                    //设置歌词
                     if(MusicService.getmLrcs().size()==0){
                         lryicAdapter.setIndex(-1);
                         lryicAdapter.setmLrcs(noLrcs);
                         lryicAdapter.notifyDataSetChanged();
                     }else{
                        lryicAdapter.setmLrcs(MusicService.getmLrcs());
                        lryicAdapter.setIndex(MusicService.getCurrentPlayMusicLyricIndex());
                        lryicAdapter.notifyDataSetChanged();
                     }

                     //设置播放的进度
                    musicSeekbar.setMax(MusicService.getCurrentDuration());
                    musicSeekbar.setProgress(MusicService.getMusicCurrentPosition());
                    //设置显示的播放时间
                    musicCurrentPosition.setText("00:00");
                    musicDuration.setText(MusicService.changeTime(MusicService.getCurrentDuration()));
                    //显示是否收藏
                    if(MusicService.getCurrentMusicIsAddLove()){
                        addLikeImage.setImageResource(R.drawable.like_image_selected);
                    }else {
                        addLikeImage.setImageResource(R.drawable.like_image);
                    }
                    //改变控制按钮图标
                    if(MusicService.isPlaying()){
                        playImageView.setImageResource(R.drawable.play_imageview);
                    }else{
                        playImageView.setImageResource(R.drawable.pause_imageview);
                }
                    break;
                case UPDATE_PLAY_MODE://更新播放模式图标
                    int currentPlayMode=MusicService.getCurrentPlayMode();
                    Log.i("Jfksjdfkj", "handleMessage: "+currentPlayMode);
                    if(currentPlayMode!=-1){//当前的播放没有设置错误
                        int playModeLength=MusicService.getPlayMode().length;
                        if(playModeLength>0){//如果总的播放模式种类大于0
                            if(currentPlayMode<modeImage.length){
                                    playModel.setImageResource(modeImage[currentPlayMode]);
                            }
                        }
                    }else {
                        if(MusicService.getPlayMode().length>0){//如果总的播放模式种类大于0
                            if(modeImage.length>0){
                                if(MusicService.setPlayMode(0)){//如果设置模式成功
                                    playModel.setImageResource(modeImage[0]);
                                }
                            }

                        }
                    }
                    break;
                case UPDATE_PROGRESS://更新进度条
                    musicCurrentPosition.setText(MusicService.changeTime(MusicService.getMusicCurrentPosition()));
                    musicDuration.setText(MusicService.changeTime(MusicService.getCurrentDuration()));
                    musicSeekbar.setMax(MusicService.getCurrentDuration());
                    if(!isPersonTouch)//如果当前无人为拖动进度条
                    musicSeekbar.setProgress(MusicService.getMusicCurrentPosition());
                    mhandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
                    break;
                case UPDATE_LRYIC:
                    lryicAdapter.notifyDataSetChanged();
                    break;
                case SCROLL_CURRENT_LRYIC_POSITION:
                    if(msg.obj==null){
                        return;
                    }
                    int index= (int) msg.obj;
                    musicLyric.smoothScrollToPositionFromTop(index,128,500);//移动歌词
                    break;
                case UPDATE_LOVE_BUTTON:
                     if(msg.obj==null){
                         return;
                     }
                     int type= (int) msg.obj;
                    switch (type){
                        case 0:
                            addLikeImage.setImageResource(R.drawable.like_image);
                            OneToast.showMessage(getContext(),getResources().getString(R.string.cancel_love_music));
                            break;
                        case 1:
                            addLikeImage.setImageResource(R.drawable.like_image_selected);
                            OneToast.showMessage(getContext(),getResources().getString(R.string.add_love_music));
                            break;
                        default:
                            break;
                    }
                    default:
                        break;
            }
        }
    };



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.local_music_fragment_item, container,false);
        noLrcs.add("       ");
        noLrcs.add("       ");
        noLrcs.add("       ");
        noLrcs.add("暂无歌词");
        noLrcs.add("       ");
        noLrcs.add("       ");
        noLrcs.add("       ");

        initView(view);
        initEvents();
        MusicService.setmControl(this);
        initData();
        return view;
    }

    //初始化数据
    private void initData() {
        if(!MusicService.isSatisfyPlayCondition()){//如果服务满足播放条件，则初始化
            MusicUtils.initMusicList();
            if(MusicUtils.sMusicList.size()==0){
                return;
            }else{
                MusicService.initMusicService(MusicUtils.sMusicList,0);
            }
        }
       mhandler.sendEmptyMessage(INIT_UI);//初始化播放Ui界面
        mhandler.sendEmptyMessage(UPDATE_PLAY_MODE);//更新播放模式按钮
        //获取歌词
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if(MusicService.getmLrcs().size()>0){
                        if(!lryicAdapter.getmLrcs().containsAll(MusicService.getmLrcs())){
                            lryicAdapter.setmLrcs(MusicService.getmLrcs());
                            mhandler.sendEmptyMessage(UPDATE_LRYIC);
                        }
                        if(lryicAdapter.getIndex()!=MusicService.getCurrentPlayMusicLyricIndex()){
                            lryicAdapter.setIndex(MusicService.getCurrentPlayMusicLyricIndex());
                            mhandler.sendEmptyMessage(UPDATE_LRYIC);
                            if(!lryicPersonScroll){
                                mhandler.removeMessages(SCROLL_CURRENT_LRYIC_POSITION);
                                Message scrollMessage=new Message();
                                scrollMessage.obj=MusicService.getCurrentPlayMusicLyricIndex();
                                scrollMessage.what=SCROLL_CURRENT_LRYIC_POSITION;
                                mhandler.sendMessageDelayed(scrollMessage,lryicScrollDelayTime);
                                if( lryicScrollDelayTime>0){//代表此前用户手动滚动
                                    lryicScrollDelayTime=0;
                                }
                            }else{
                                mhandler.removeMessages(SCROLL_CURRENT_LRYIC_POSITION);
                            }
                        }
                    }else{
                        lryicAdapter.setIndex(-1);
                        lryicAdapter.setmLrcs(noLrcs);
                        mhandler.sendEmptyMessage(UPDATE_LRYIC);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },100,300);

         mhandler.sendEmptyMessage(UPDATE_PROGRESS);

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
        addLikeImage = view.findViewById(R.id.addLikeImage);
        playModel = view.findViewById(R.id.playModel);
        musicDuration = view.findViewById(R.id.musicDuration);
        musicSeekbar = view.findViewById(R.id.musicSeekbar);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initEvents() {
        musicAlbum.setOnClickListener(this);
        playImageView.setOnClickListener(this);
        prevImageView.setOnClickListener(this);
        nextImageView.setOnClickListener(this);
        addLikeImage.setOnClickListener(this);
        playModel.setOnClickListener(this);
        musicSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//设置进度条拖动监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(!MusicService.isExistMusics()){//如果没有音乐则不能拖动进度条
                    musicSeekbar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPersonTouch=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               MusicService.setMusicCurrentPosition(musicSeekbar.getProgress());
                isPersonTouch=false;
            }
        });

        musicLyric.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                 synchronized (this){
                     switch (motionEvent.getAction()){
                         case MotionEvent.ACTION_DOWN:
                             motion_y=motionEvent.getY();
                             lryicPersonScroll=true;
                             lryicScrollDelayTime=3000;
                             return  true;
                         case MotionEvent.ACTION_MOVE:

                             break;
                         case MotionEvent.ACTION_UP:
                             if(Math.abs(motionEvent.getY()-motion_y)<=3.0){//判断为单击
                                 musicLyric.setVisibility(View.INVISIBLE);
                                 musicAlbum.setVisibility(View.VISIBLE);
                             }
                             lryicPersonScroll=false;
                             break;

                     }
                     return false;
                 }

             }
         });
    }


    @Override
    public void onClick(View view) {
        synchronized (this){
            switch (view.getId()){
                case R.id.musicAlbum://点击专辑图片显示歌词或者暂无歌词
                    //Fade fade=new Fade();
                   // TransitionManager.beginDelayedTransition((ViewGroup) musicAlbum.getParent(),fade);
                    musicAlbum.setVisibility(View.INVISIBLE);
                    musicLyric.setVisibility(View.VISIBLE);
                    break;
                case R.id.playImageView://切换播放模式
                    Intent intent=new Intent(getContext(),MusicService.class);
                    intent.setAction(MusicService.TOGGLE_ACTION);
                    Objects.requireNonNull(getContext()).startService(intent);
                    break;
                case R.id.prevImageView://前一首
                    OneToast.hideToast();
                    Intent intent1=new Intent(getContext(),MusicService.class);
                    intent1.setAction(MusicService.PREVIOUS_ACTION);
                    Objects.requireNonNull(getContext()).startService(intent1);
                    break;
                case R.id.nextImageView://后一首
                    OneToast.hideToast();
                    Intent intent2=new Intent(getContext(),MusicService.class);
                    intent2.setAction(MusicService.NEXT_ACTION);
                    Objects.requireNonNull(getContext()).startService(intent2);
                    break;
                case R.id.addLikeImage://取消或收藏音乐
                    Intent intent3=new Intent(getContext(),MusicService.class);
                    intent3.setAction(MusicService.LOVE_ACTION);
                    Objects.requireNonNull(getContext()).startService(intent3);
                    break;
                case R.id.playModel://切换播放模式
                    int currentPlayMode=MusicService.getCurrentPlayMode();
                    if(currentPlayMode!=-1){//当前的播放没有设置错误
                        int playModeLength=MusicService.getPlayMode().length;
                        if(playModeLength>0){//如果总的播放模式种类大于0
                            currentPlayMode=currentPlayMode>=playModeLength-1?0:currentPlayMode+1;
                            if(currentPlayMode<modeImage.length){
                                if(MusicService.setPlayMode(currentPlayMode)){//如果设置模式成功
                                    playModel.setImageResource(modeImage[currentPlayMode]);
                                    OneToast.showMessage(getContext(),MusicService.getPlayMode()[currentPlayMode]);
                                    int currentPlayMode1=MusicService.getCurrentPlayMode();

                                }

                            }

                        }
                    }else {
                        if(MusicService.getPlayMode().length>0){//如果总的播放模式种类大于0
                            if(modeImage.length>0){
                                if(MusicService.setPlayMode(0)){//如果设置模式成功
                                    playModel.setImageResource(modeImage[0]);
                                    OneToast.showMessage(getContext(),MusicService.getPlayMode()[0]);
                                }
                            }

                        }
                    }
                    break;
                default:
                    break;
            }
        }

    }

    @Override
    public void playButton(int index) {//控制播放按钮的形状(0暂停，1,播放）
        switch (index){
            case 0:
               playImageView.setImageResource(R.drawable.pause_imageview);
                break;
            case 1:
                playImageView.setImageResource(R.drawable.play_imageview);
                break;
                default:
                    break;
        }
    }


    @Override
    public void updateUI() {
        mhandler.sendEmptyMessage(INIT_UI);
    }

    @Override
    public void updateLoveButton(int musicPosition,int type) {//改变收藏按钮的形状（0没收藏，1收藏)

        if(musicPosition==MusicService.getCurrentPosition()) {
            mhandler.obtainMessage(UPDATE_LOVE_BUTTON,type).sendToTarget();
        }else{
            OneToast.hideToast();
        }
    }
}
