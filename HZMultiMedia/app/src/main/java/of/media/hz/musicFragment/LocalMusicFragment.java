package of.media.hz.musicFragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;


import of.media.hz.R;
import of.media.hz.adapter.LryicAdapter;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class LocalMusicFragment extends Fragment implements View.OnClickListener {

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
    private int currentPlayIndex=-1;//当前的播放下标
    private boolean isPersonTouch=false;//判断是否为人为滑动进度条
    private boolean isResetBind=false;//是否重新绑定
    private  final List<String> noLrcs=new ArrayList<>();//无歌词
    private  ObjectAnimator roateAnimation;
    private LryicAdapter lryicAdapter;

    @SuppressLint("HandlerLeak")
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INIT_UI:
                    break;
                case UPDATE_PROGRESS://更新进度条
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
        noLrcs.add("暂无歌词");
        initView(view);
        initEvents();
        return view;
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
        lryicAdapter = new LryicAdapter();
        lryicAdapter.setmLrcs(noLrcs);
        musicLyric.setAdapter(lryicAdapter);
    }

    private void initEvents() {
        musicAlbum.setOnClickListener(this);
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

            }
        });
        musicLyric.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                musicLyric.setVisibility(View.GONE);
                musicAlbum.setVisibility(View.VISIBLE);
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
            case R.id.playImageView:

                break;
            case R.id.prevImageView:
                break;
            case R.id.nextImageView:

                break;
                default:
                    break;


        }
    }
}
