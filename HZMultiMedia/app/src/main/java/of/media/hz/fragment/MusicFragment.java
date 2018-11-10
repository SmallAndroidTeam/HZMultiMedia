package of.media.hz.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

    private Button localRadioButton;
    private Button onlineRadioButton;
    private Button localMusicButton;
    private Button bluetoothMusicButton;
    private Button usbMusicButton;
    private Button onlineMusicButton;
    private Fragment localRadioFragment,onlineRadioFragment,localMusicFragment,bluetoothMusicFragment,
    usbMusicFragment,onlineMusicFragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.music_fragment_item,container,false);
        initView(view);
        initEvents();
        setInitFragement();//设置默认显示的fragemnt;
        return view;
    }

    private void setInitFragement() {
        localMusicButton.callOnClick();
    }

    private void initEvents() {
        localRadioButton.setOnClickListener(this);
        onlineRadioButton.setOnClickListener(this);
        localMusicButton.setOnClickListener(this);
        bluetoothMusicButton.setOnClickListener(this);
        usbMusicButton.setOnClickListener(this);
        onlineMusicButton.setOnClickListener(this);
    }

    private void initView(View view) {
        localRadioButton = view.findViewById(R.id.localRadioButton);
        onlineRadioButton = view.findViewById(R.id.onlineRadioButton);
        localMusicButton = view.findViewById(R.id.localMusicButton);
        bluetoothMusicButton = view.findViewById(R.id.bluetoothMusicButton);
        usbMusicButton = view.findViewById(R.id.usbMusicButton);
        onlineMusicButton = view.findViewById(R.id.onlineMusicButton);
    }

    @Override
    public void onClick(View view) {
        final FragmentManager fragmentManager= Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        final  FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideAllFragement(fragmentTransaction);
       switch (view.getId()){
           case R.id.localRadioButton:
               if(localRadioFragment==null){
                   localRadioFragment=new LocalRadioFragment();
                    fragmentTransaction.add(R.id.musicFrameLayout,localRadioFragment);
               }else{
                   fragmentTransaction.show(localRadioFragment);
               }
               break;
           case R.id.onlineRadioButton:
               if(onlineRadioFragment==null){
                   onlineRadioFragment=new OnlineRadioFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,onlineRadioFragment);
               }else{
                   fragmentTransaction.show(onlineRadioFragment);
               }
               break;
           case R.id.localMusicButton:
               if(localMusicFragment==null){
                   localMusicFragment=new LocalMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,localMusicFragment);
               }else{
                   fragmentTransaction.show(localMusicFragment);
               }
               break;
           case R.id.bluetoothMusicButton:
               if(bluetoothMusicFragment==null){
                   bluetoothMusicFragment=new BluetoothMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,bluetoothMusicFragment);
               }else{
                   fragmentTransaction.show(bluetoothMusicFragment);
               }
               break;
           case R.id.usbMusicButton:
               if(usbMusicFragment==null){
                   usbMusicFragment=new UsbMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,usbMusicFragment);
               }else{
                   fragmentTransaction.show(usbMusicFragment);
               }
               break;
           case R.id.onlineMusicButton:
               if(onlineMusicFragment==null){
                   onlineMusicFragment=new OnlineMusicFragment();
                   fragmentTransaction.add(R.id.musicFrameLayout,onlineMusicFragment);
               }else{
                   fragmentTransaction.show(onlineMusicFragment);
               }
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
