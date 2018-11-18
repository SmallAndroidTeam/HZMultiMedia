package of.media.hz.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import of.media.hz.Music;
import of.media.hz.R;
import of.media.hz.adapter.LocalMusicAdapter;
import of.media.hz.musicFragment.LocalMusicFragment;
import of.media.hz.services.MusicService;
import of.media.hz.toast.OneToast;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class MusicFragment  extends Fragment implements View.OnClickListener {


    private Fragment localRadioFragment,onlineRadioFragment,localMusicFragment,bluetoothMusicFragment,
    usbMusicFragment,onlineMusicFragment;
    private LinearLayout localMusicLayout;
    private LinearLayout bluetoothMusicLayout;
    private LinearLayout musicLayout;
    private RelativeLayout musicCoveringLayer;
    private LinearLayout menuLayout;
    private  Dialog localMusicListDialog;//本地音乐列表弹出框
    private TextView localMusicTitle;//本地音乐列表弹出框的标题
    private ListView localMusicList;//本地音乐列表弹出框的列表显示
    private LocalMusicAdapter localMusicAdapter;

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
        menuLayout.setOnClickListener(this);
        musicCoveringLayer.setOnClickListener(this);
    }

    private void initView(View view) {
        localMusicLayout = view.findViewById(R.id.localMusicLayout);
        bluetoothMusicLayout = view.findViewById(R.id.bluetoothMusicLayout);
        musicLayout = view.findViewById(R.id.musicLayout);
        menuLayout = view.findViewById(R.id.menuLayout);
        musicCoveringLayer = view.findViewById(R.id.musicCoveringLayer);
    }



    private Dialog  localMusicListDialog(){
        View view=LayoutInflater.from(getContext()).inflate(R.layout.local_music_dialog,null);
        localMusicTitle = view.findViewById(R.id.localMusicTitle);
        localMusicList = view.findViewById(R.id.localMusicList);
        localMusicTitle.setText(getResources().getString(R.string.localMusicTitle));
        localMusicAdapter=new LocalMusicAdapter();
        List<Music> localMusic=MusicService.getLocalMusicList();
        if(localMusic.size()==0){//无音乐

        }else{
            localMusicAdapter.setMusicList(localMusic);
        }
        localMusicList.setAdapter(localMusicAdapter);
        localMusicTitle.setText(localMusicTitle.getText()+"("+localMusic.size()+"首)");
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
        MusicService.setUpdateMusicList(new MusicService.UpdateMusicList() {//用于更新音乐列表显示当前播放的音乐
            @Override
            public void updateUI(int index) {
               if(localMusicAdapter!=null&&localMusicAdapter.getCount()>index&&index>=0){
                   localMusicAdapter.setPlayIndex(index);
                   localMusicAdapter.notifyDataSetChanged();
                  // localMusicList.smoothScrollToPositionFromTop(index,140,500);
               }
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


}
