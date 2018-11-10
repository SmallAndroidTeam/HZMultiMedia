package of.media.hz.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import of.media.hz.R;
import of.media.hz.fragment.GalleryFragment;
import of.media.hz.fragment.MusicFragment;
import of.media.hz.fragment.VideoFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private Button musicButton;
    private Button videoButton;
    private Button galleryButton;
    private Fragment musicFragment,videoFragment,galleryFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);
        initView();
        initEvents();
        initSelectedFragment(0);//默认选中music界面
    }

    private void initEvents() {
        musicButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
    }

    private void initView() {
        musicButton = this.findViewById(R.id.musicButton);
        videoButton = this.findViewById(R.id.videoButton);
        galleryButton = this.findViewById(R.id.galleryButton);
    }

    /**
     * 初始化被选中的界面(index =0 默认为music界面，1为video界面，2为gallery界面）
     * @param index
     */
    private void initSelectedFragment(int index){
      switch (index){
          case 0:
              musicButton.callOnClick();
              break;
          case 1:
              videoButton.callOnClick();
              break;
          case 2:
              galleryButton.callOnClick();
              break;
              default:
                  break;
      }
    }
    @Override
    public void onClick(View view) {
         final FragmentManager fragmentManager=getSupportFragmentManager();
         FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        hideAllFragment(fragmentTransaction);
        switch (view.getId()){
            case R.id.musicButton:
                if(musicFragment==null){
                    musicFragment=new MusicFragment();
                    fragmentTransaction.add(R.id.indexFrameLayout,musicFragment);
                }else{
                    fragmentTransaction.show(musicFragment);
                }
                break;
            case R.id.videoButton:
                if(videoFragment==null){
                    videoFragment=new VideoFragment();
                    fragmentTransaction.add(R.id.indexFrameLayout,videoFragment);
                }else{
                    fragmentTransaction.show(videoFragment);
                }
                break;
            case R.id.galleryButton:
                if(galleryFragment==null){
                    galleryFragment=new GalleryFragment();
                    fragmentTransaction.add(R.id.indexFrameLayout,galleryFragment);
                }else{
                    fragmentTransaction.show(galleryFragment);
                }
                break;
                default:
                    break;
        }
        fragmentTransaction.commit();
    }


    /**
     * 隐藏所有的fragment界面
     * @param fragmentTransaction
     */
    private  void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(musicFragment!=null){
            fragmentTransaction.hide(musicFragment);
        }

        if(videoFragment!=null){
            fragmentTransaction.hide(videoFragment);
        }

        if(galleryFragment!=null){
            fragmentTransaction.hide(galleryFragment);
        }
    }


}
