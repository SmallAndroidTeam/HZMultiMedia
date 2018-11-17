package of.media.hz.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import org.litepal.LitePal;

import java.lang.annotation.Annotation;

import of.media.hz.Application.App;
import of.media.hz.R;
import of.media.hz.fragment.GalleryFragment;
import of.media.hz.fragment.MusicFragment;
import of.media.hz.fragment.VideoFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener {


    private RelativeLayout coveringLayer;
    private FloatingActionButton selectButton;
     private boolean isAdd=false;
    private FloatingActionButton musicButton;
    private FloatingActionButton videoButton;
    private FloatingActionButton galleryButton;
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
        hideFloatingButton();
        App.sContext=this;
        LitePal.initialize(this);
    }

    private void initEvents() {
       selectButton.setOnClickListener(this);
        musicButton.setOnClickListener(this);
        videoButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        coveringLayer.setOnClickListener(this);
    }

    private void initView() {
        coveringLayer = this.findViewById(R.id.coveringLayer);
        selectButton = this.findViewById(R.id.selectButton);
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
        final FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        switch (view.getId()){
        case   R.id.selectButton:
             if(isAdd){//如果现在是展开状态
                 hideFloatingButton();
             }else{
                 showFloatingButton();
             }
            break;
            case R.id.musicButton:
             hideFloatingButton();
                hideAllFragment(fragmentTransaction);
                if(musicFragment==null){
                    musicFragment=new MusicFragment();
                    fragmentTransaction.add(R.id.multimediaLayout,musicFragment);
                }else{
                    fragmentTransaction.show(musicFragment);
                }
             break;
            case R.id.videoButton:
                hideFloatingButton();
                hideAllFragment(fragmentTransaction);
                if(videoFragment==null){
                    videoFragment=new VideoFragment();
                    fragmentTransaction.add(R.id.multimediaLayout,videoFragment);
                }else{
                    fragmentTransaction.show(videoFragment);
                }
                break;
            case R.id.galleryButton:
                hideFloatingButton();
                hideAllFragment(fragmentTransaction);
                if(galleryFragment==null){
                    galleryFragment=new GalleryFragment();
                    fragmentTransaction.add(R.id.multimediaLayout,galleryFragment);
                }else{
                    fragmentTransaction.show(galleryFragment);
                }
                break;
            case R.id.coveringLayer:
                hideFloatingButton();
                break;
                default:
                    break;
        }
      fragmentTransaction.commit();
    }

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

    //隐藏悬浮按钮
     private  void hideFloatingButton(){
         //设置延迟动画
//         ChangeBounds changeBounds=new ChangeBounds();
//         ArcMotion arcMotion=new ArcMotion();
//         arcMotion.setMinimumVerticalAngle(90);
//         changeBounds.setPathMotion(arcMotion);
//         TransitionManager.beginDelayedTransition(coveringLayer,changeBounds);
         Slide slide=new Slide(Gravity.BOTTOM);
         TransitionSet transitionSet=new TransitionSet();
         transitionSet.addTransition(slide).addTransition(new Fade());
         TransitionManager.beginDelayedTransition((ViewGroup) musicButton.getParent(),transitionSet);
         setFloatingButtonPositon(musicButton,36,36);
         setFloatingButtonPositon(videoButton,36,36);
         setFloatingButtonPositon(galleryButton,36,36);
         musicButton.hide();
         videoButton.hide();
         galleryButton.hide();
         selectButton.setImageResource(R.drawable.show_button);
         coveringLayer.setVisibility(View.GONE);
         isAdd=false;
     }

     //显示悬浮按钮
     private void showFloatingButton(){
         //设置延迟动画
//         ChangeBounds changeBounds=new ChangeBounds();
//         ArcMotion arcMotion=new ArcMotion();
//         arcMotion.setMinimumVerticalAngle(90);
//         changeBounds.setPathMotion(arcMotion);
//         TransitionManager.beginDelayedTransition(coveringLayer,changeBounds);
         Slide slide=new Slide(Gravity.BOTTOM);
         TransitionSet transitionSet=new TransitionSet();
         transitionSet.addTransition(slide).addTransition(new Fade());
         TransitionManager.beginDelayedTransition((ViewGroup) musicButton.getParent(),transitionSet);
         musicButton.show();
         videoButton.show();
         galleryButton.show();
         setFloatingButtonPositon(galleryButton,150,36);
         setFloatingButtonPositon(videoButton,250,36);
         setFloatingButtonPositon(musicButton,350,36);
         selectButton.setImageResource(R.drawable.hide_button);
         coveringLayer.setVisibility(View.VISIBLE);
         isAdd=true;
     }

     //设置悬浮按钮的位置
     private void setFloatingButtonPositon(View view,int bottom,int right){
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.rightMargin=right;
        layoutParams.bottomMargin=bottom;
        view.setLayoutParams(layoutParams);

     }

}
