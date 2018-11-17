package of.media.hz.fragment;

import android.app.Dialog;
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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Objects;

import of.media.hz.R;
import of.media.hz.musicFragment.BluetoothMusicFragment;
import of.media.hz.musicFragment.LocalMusicFragment;
import of.media.hz.musicFragment.LocalRadioFragment;
import of.media.hz.musicFragment.OnlineMusicFragment;
import of.media.hz.musicFragment.OnlineRadioFragment;
import of.media.hz.musicFragment.UsbMusicFragment;

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
    private  Dialog musicListDialog;
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

    private Dialog  musicListDialog(){
        View view=LayoutInflater.from(getContext()).inflate(R.layout.music_dialog,null);
        Dialog dialog=new Dialog(Objects.requireNonNull(getActivity()),R.style.MyDialog);
        Window window=dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(view);
        window.setWindowAnimations(R.style.dialogAnimation);

        WindowManager.LayoutParams lp=window.getAttributes();
        lp.width=getResources().getDisplayMetrics().widthPixels/2;
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
               if(musicListDialog==null)
               musicListDialog=musicListDialog();
               musicListDialog.show();
               break;
           case R.id.bluetoothMusicLayout:
               hideAllFragement(fragmentTransaction);
               if(localMusicFragment==null){
                   localMusicFragment=new LocalMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,localMusicFragment);
               }else{
                   fragmentTransaction.show(localMusicFragment);
               }
               if(musicListDialog==null)
                   musicListDialog=musicListDialog();
               musicListDialog.show();
               break;
           case R.id.menuLayout:
               Slide slide=new Slide(Gravity.LEFT);
               TransitionSet transitionSet=new TransitionSet();
               transitionSet.addTransition(slide).addTransition(new Fade());
               TransitionManager.beginDelayedTransition(musicLayout,transitionSet);
               TransitionManager.beginDelayedTransition(menuLayout,transitionSet);
                   musicLayout.setVisibility(View.VISIBLE);
               menuLayout.setVisibility(View.INVISIBLE);
               musicCoveringLayer.setVisibility(View.VISIBLE);
               break;
           case R.id.musicCoveringLayer:
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
