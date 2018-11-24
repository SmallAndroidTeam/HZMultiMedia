package of.media.hz.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Magnifier;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import of.media.hz.Music;
import of.media.hz.R;
import of.media.hz.adapter.LocalMusicAdapter;
import of.media.hz.musicFragment.LocalMusicFragment;
import of.media.hz.musicFragment.UsbMusicFragment;
import of.media.hz.services.MusicService;
import of.media.hz.toast.OneToast;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class MusicFragment  extends Fragment implements View.OnClickListener {


    private Fragment localRadioFragment,onlineRadioFragment,localMusicFragment,bluetoothMusicFragment,
    usbMusicFragment,onlineMusicFragment;
    private LinearLayout localMusicLayout,bluetoothMusicLayout,llUsbMuiscLaytou;
    private LinearLayout musicLayout;
    private RelativeLayout musicCoveringLayer;
    private LinearLayout menuLayout;
    private  Dialog localMusicListDialog,usbMusicListDialog;//本地音乐列表弹出框
    private TextView localMusicTitle;//本地音乐列表弹出框的标题
    private ListView localMusicList;//本地音乐列表弹出框的列表显示
    private LocalMusicAdapter localMusicAdapter;
    public static final int UPDATE_LOCAL_MUSIC_LIST=0;//更新本地列表
    @SuppressLint("HandlerLeak")
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
          switch (msg.what){
              case UPDATE_LOCAL_MUSIC_LIST:
                  if(msg.obj!=null){
                     int type= (int) msg.obj;
                     if(type==0){//点击列表而导致刷新
                         if(localMusicAdapter!=null){
                             localMusicAdapter.notifyDataSetChanged();
                         }
                     }else if(type==1){//下拉刷新
                         if(localMusicAdapter!=null){
                             if(localMusicAdapter.getCount()==0){
                                // localMusicList.setVisibility(View.GONE);
                                 noLocalMusicTip.setVisibility(View.VISIBLE);
                             }else{
                                 localMusicList.setVisibility(View.VISIBLE);
                                // noLocalMusicTip.setVisibility(View.GONE);
                             }
                             localMusicTitle.setText(getResources().getString(R.string.localMusicTitle)+"("+localMusicAdapter.getCount()+"首)");
                             localMusicAdapter.notifyDataSetChanged();
                         }
                         localMusicListRefresh.setRefreshing(false);
                         OneToast.showMessage(getContext(),"刷新成功");
                     }

                  }

                  break;
                  default:
                      break;
          }
        }
    };
    private SwipeRefreshLayout localMusicListRefresh;
    private TextView noLocalMusicTip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.music_fragment_item,container,false);
        initView(view);
        initEvents();
        setInitFragment();//设置默认显示的fragemnt;
        return view;
    }

    private void setInitFragment() {
        final FragmentManager fragmentManager= Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        final  FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideAllFragement(fragmentTransaction);
        if(localMusicFragment==null){
            localMusicFragment=new LocalMusicFragment();
            fragmentTransaction.add(R.id.musicFrameLayout,localMusicFragment);
        }else{
            fragmentTransaction.show(localMusicFragment);
        }
        fragmentTransaction.commit();
    }

    private void initEvents() {
        localMusicLayout.setOnClickListener(this);
        bluetoothMusicLayout.setOnClickListener(this);
        llUsbMuiscLaytou.setOnClickListener(this);

        menuLayout.setOnClickListener(this);
        musicCoveringLayer.setOnClickListener(this);
    }

    private void initView(View view) {
        localMusicLayout = view.findViewById(R.id.localMusicLayout);
        bluetoothMusicLayout = view.findViewById(R.id.bluetoothMusicLayout);
        llUsbMuiscLaytou = view.findViewById(R.id.ll_usbmusicLayout);

        musicLayout = view.findViewById(R.id.musicLayout);
        menuLayout = view.findViewById(R.id.menuLayout);
        musicCoveringLayer = view.findViewById(R.id.musicCoveringLayer);
    }



    private Dialog  localMusicListDialog(){
        View view=LayoutInflater.from(getContext()).inflate(R.layout.local_music_dialog,null);
        localMusicTitle = view.findViewById(R.id.localMusicTitle);
        localMusicList = view.findViewById(R.id.localMusicList);
        localMusicAdapter=new LocalMusicAdapter();
        localMusicListRefresh = view.findViewById(R.id.localMusicListRefresh);
        noLocalMusicTip = view.findViewById(R.id.noLocalMusicTip);
        localMusicListRefresh.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        localMusicListRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,android.R.color.holo_orange_light,android.R.color.holo_red_light);

        List<Music> localMusic=MusicService.getLocalMusicList();
        if(localMusic.size()==0){//无音乐
            localMusicList.setVisibility(View.GONE);
            noLocalMusicTip.setVisibility(View.VISIBLE);
        }else{
            localMusicList.setVisibility(View.VISIBLE);
            localMusicAdapter.setMusicList(localMusic);
            noLocalMusicTip.setVisibility(View.GONE);
        }
        localMusicList.setAdapter(localMusicAdapter);
        localMusicTitle.setText(getResources().getString(R.string.localMusicTitle)+"("+localMusic.size()+"首)");
       //本地音乐弹窗中的列表点击事件
            localMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     if(localMusicAdapter.getPlayIndex()==i){//如果点击二次正在相同的播放的音乐，就会隐藏弹出框
                       if(localMusicListDialog!=null){
                           localMusicListDialog.hide();
                       }
                     }else{
                         Intent intent=new Intent(getActivity(),MusicService.class);
                         intent.setAction(MusicService.STOP_ACTION);
                         Objects.requireNonNull(getActivity()).startService(intent);
                         if(MusicService.initMusicService(localMusicAdapter.getMusicList(),i)){//如果初始化音乐服务成功
                             Intent intent1=new Intent(getActivity(),MusicService.class);
                             intent1.setAction(MusicService.TOGGLE_ACTION);
                             intent1.putExtra(MusicService.UPDATE_FLAG,MusicService.UPDATE_FLAG);//添加更新UI界面的key值
                             Objects.requireNonNull(getActivity()).startService(intent1);
                             localMusicAdapter.setPlayIndex(i);
                             localMusicAdapter.notifyDataSetChanged();
                         }else{
                             Intent intent2=new Intent(getActivity(),MusicService.class);
                             intent2.setAction(MusicService.TOGGLE_ACTION);
                             Objects.requireNonNull(getActivity()).startService(intent2);
                         }

                     }
                }
            });
      //下拉刷新
        localMusicListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//更新本地列表
            @Override
            public void onRefresh() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            if(localMusicAdapter!=null){
                                localMusicAdapter.setMusicList(MusicService.getLocalMusicList());
                                Message message=new Message();
                                message.what=UPDATE_LOCAL_MUSIC_LIST;
                                message.obj=1;
                                mhandler.sendMessageDelayed(message,500);
                            }
                        }
                    }
                }).start();
            }
        });

        noLocalMusicTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        MusicService.setUpdateMusicList(new MusicService.UpdateMusicList() {//用于更新音乐列表显示当前播放的音乐
            @Override
            public void updateUI(final int index) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            //如果适配器中的音乐列表包含服务器中的音乐列表,且对应index下标的音乐信息的地址相同
                            if(localMusicAdapter!=null&&localMusicAdapter.getCount()>index&&index>=0&&localMusicAdapter.getMusicList().containsAll(MusicService.getCurrentMusicList())
                                    &&localMusicAdapter.getMusicList().get(index).toString().contentEquals(MusicService.getCurrentMusicList().get(index).toString())){
                                localMusicAdapter.setPlayIndex(index);
                                mhandler.obtainMessage(UPDATE_LOCAL_MUSIC_LIST,0).sendToTarget();
                                // localMusicList.smoothScrollToPositionFromTop(index,140,500);
                            }else{
                                localMusicAdapter.setPlayIndex(-1);
                                mhandler.obtainMessage(UPDATE_LOCAL_MUSIC_LIST,0).sendToTarget();
                            }
                        }
                    }
                }).start();

            }
        });

        Dialog dialog=new Dialog(Objects.requireNonNull(getActivity()),R.style.MyDialog);
        Window window=dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(view);
        window.setWindowAnimations(R.style.dialogAnimation);
        WindowManager.LayoutParams lp=window.getAttributes();
        lp.width= (int) (getResources().getDisplayMetrics().widthPixels/1.5);
        lp.height=getResources().getDisplayMetrics().heightPixels/2;
        window.setAttributes(lp);

        return dialog;
    }
    @Override
    public void onClick(View view) {
        final FragmentManager fragmentManager= Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        final  FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
       switch (view.getId()){
           case R.id.localMusicLayout:
               hideAllFragement(fragmentTransaction);
               if(localMusicFragment==null){
                   localMusicFragment=new LocalMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,localMusicFragment);
               }else{
                   fragmentTransaction.show(localMusicFragment);
               }
               if(localMusicListDialog==null)
                   localMusicListDialog=localMusicListDialog();
               localMusicListDialog.show();
               break;
           case R.id.bluetoothMusicLayout:
               hideAllFragement(fragmentTransaction);
               if(localMusicFragment==null){
                   localMusicFragment=new LocalMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,localMusicFragment);
               }else{
                   fragmentTransaction.show(localMusicFragment);
               }
               OneToast.showMessage(getContext(),"蓝牙音乐");
               break;

           case R.id.ll_usbmusicLayout:
               hideAllFragement(fragmentTransaction);
               if(usbMusicFragment==null){
                   usbMusicFragment=new UsbMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,usbMusicFragment);
               }else{
                   fragmentTransaction.show(usbMusicFragment);
               }
               if(usbMusicListDialog==null)
                   usbMusicListDialog=UsbMusicListDialog();
               usbMusicListDialog.show();

               break;

           case R.id.menuLayout://点击菜单按钮
               Slide slide=new Slide(Gravity.LEFT);
               TransitionSet transitionSet=new TransitionSet();
               transitionSet.addTransition(slide).addTransition(new Fade());
               TransitionManager.beginDelayedTransition(musicLayout,transitionSet);
               TransitionManager.beginDelayedTransition(menuLayout,transitionSet);
                   musicLayout.setVisibility(View.VISIBLE);
               menuLayout.setVisibility(View.INVISIBLE);
               musicCoveringLayer.setVisibility(View.VISIBLE);
               break;
           case R.id.musicCoveringLayer://遮罩层
               Slide slide1=new Slide(Gravity.RIGHT);
               TransitionSet transitionSet1=new TransitionSet();
               transitionSet1.addTransition(slide1).addTransition(new Fade());
               TransitionManager.beginDelayedTransition(menuLayout,transitionSet1);
               TransitionManager.beginDelayedTransition(musicLayout,transitionSet1);
               musicLayout.setVisibility(View.INVISIBLE);
               menuLayout.setVisibility(View.VISIBLE);
               musicCoveringLayer.setVisibility(View.GONE);
               break;
           default:
               break;
       }
       fragmentTransaction.commit();
    }

    /**
     * 隐藏所有的fragment
     * @param fragmentTransaction
     */
    private void hideAllFragement(FragmentTransaction fragmentTransaction){
        if(localRadioFragment!=null){
            fragmentTransaction.hide(localRadioFragment);
        }
        if(onlineRadioFragment!=null){
            fragmentTransaction.hide(onlineRadioFragment);
        }
        if(localMusicFragment!=null){
            fragmentTransaction.hide(localMusicFragment);
        }
        if(bluetoothMusicFragment!=null){
            fragmentTransaction.hide(bluetoothMusicFragment);
        }
        if(usbMusicFragment!=null){
            fragmentTransaction.hide(usbMusicFragment);
        }
        if(onlineMusicFragment!=null){
            fragmentTransaction.hide(onlineMusicFragment);
        }
    }

    private Dialog  UsbMusicListDialog(){
        View view=LayoutInflater.from(getContext()).inflate(R.layout.local_music_dialog,null);
        localMusicTitle = view.findViewById(R.id.localMusicTitle);
        localMusicList = view.findViewById(R.id.localMusicList);
        localMusicAdapter=new LocalMusicAdapter();
        localMusicListRefresh = view.findViewById(R.id.localMusicListRefresh);
        noLocalMusicTip = view.findViewById(R.id.noLocalMusicTip);
        localMusicListRefresh.setProgressBackgroundColorSchemeResource(R.color.RefreshProgressBackground);
        localMusicListRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,android.R.color.holo_orange_light,android.R.color.holo_red_light);

        List<Music> localMusic=MusicService.getLocalMusicList();
        if(localMusic.size()==0){//无音乐
            localMusicList.setVisibility(View.GONE);
            noLocalMusicTip.setVisibility(View.VISIBLE);
        }else{
            localMusicList.setVisibility(View.VISIBLE);
            localMusicAdapter.setMusicList(localMusic);
            noLocalMusicTip.setVisibility(View.GONE);
        }
        localMusicList.setAdapter(localMusicAdapter);
        localMusicTitle.setText(getResources().getString(R.string.localMusicTitle)+"("+localMusic.size()+"首)");
        //本地音乐弹窗中的列表点击事件
        localMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(localMusicAdapter.getPlayIndex()==i){//如果点击二次正在相同的播放的音乐，就会隐藏弹出框
                    if(localMusicListDialog!=null){
                        localMusicListDialog.hide();
                    }
                }else{
                    Intent intent=new Intent(getActivity(),MusicService.class);
                    intent.setAction(MusicService.STOP_ACTION);
                    Objects.requireNonNull(getActivity()).startService(intent);
                    if(MusicService.initMusicService(localMusicAdapter.getMusicList(),i)){//如果初始化音乐服务成功
                        Intent intent1=new Intent(getActivity(),MusicService.class);
                        intent1.setAction(MusicService.TOGGLE_ACTION);
                        intent1.putExtra(MusicService.UPDATE_FLAG,MusicService.UPDATE_FLAG);//添加更新UI界面的key值
                        Objects.requireNonNull(getActivity()).startService(intent1);
                        localMusicAdapter.setPlayIndex(i);
                        localMusicAdapter.notifyDataSetChanged();
                    }else{
                        Intent intent2=new Intent(getActivity(),MusicService.class);
                        intent2.setAction(MusicService.TOGGLE_ACTION);
                        Objects.requireNonNull(getActivity()).startService(intent2);
                    }

                }
            }
        });
        //下拉刷新
        localMusicListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {//更新本地列表
            @Override
            public void onRefresh() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            if(localMusicAdapter!=null){
                                localMusicAdapter.setMusicList(MusicService.getLocalMusicList());
                                Message message=new Message();
                                message.what=UPDATE_LOCAL_MUSIC_LIST;
                                message.obj=1;
                                mhandler.sendMessageDelayed(message,500);
                            }
                        }
                    }
                }).start();
            }
        });

        noLocalMusicTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        MusicService.setUpdateMusicList(new MusicService.UpdateMusicList() {//用于更新音乐列表显示当前播放的音乐
            @Override
            public void updateUI(final int index) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            //如果适配器中的音乐列表包含服务器中的音乐列表,且对应index下标的音乐信息的地址相同
                            if(localMusicAdapter!=null&&localMusicAdapter.getCount()>index&&index>=0&&localMusicAdapter.getMusicList().containsAll(MusicService.getCurrentMusicList())
                                    &&localMusicAdapter.getMusicList().get(index).toString().contentEquals(MusicService.getCurrentMusicList().get(index).toString())){
                                localMusicAdapter.setPlayIndex(index);
                                mhandler.obtainMessage(UPDATE_LOCAL_MUSIC_LIST,0).sendToTarget();
                                // localMusicList.smoothScrollToPositionFromTop(index,140,500);
                            }else{
                                localMusicAdapter.setPlayIndex(-1);
                                mhandler.obtainMessage(UPDATE_LOCAL_MUSIC_LIST,0).sendToTarget();
                            }
                        }
                    }
                }).start();

            }
        });

        Dialog dialog=new Dialog(Objects.requireNonNull(getActivity()),R.style.MyDialog);
        Window window=dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(view);
        window.setWindowAnimations(R.style.dialogAnimation);
        WindowManager.LayoutParams lp=window.getAttributes();
        lp.width= (int) (getResources().getDisplayMetrics().widthPixels/1.5);
        lp.height=getResources().getDisplayMetrics().heightPixels/2;
        window.setAttributes(lp);

        return dialog;
    }
}
