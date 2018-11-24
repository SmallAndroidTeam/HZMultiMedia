package of.media.hz.musicFragment;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import of.media.hz.R;
import of.media.hz.bean.Category;
import of.media.hz.config.Constant;
import of.media.hz.until.ScanFileCountUtil;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by MR.XIE on 2018/11/6.
 */
public class UsbMusicFragment extends Fragment implements EasyPermissions.PermissionCallbacks{
    private static final String TAG = "UsbMusicFragment";
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 0X01;
    private TextView tvMusicNumber;
    private FrameLayout mLoadingFrameLayout;
    private File[] mFilesArray; //文件列表数组
    private ArrayList<File> mFileList = new ArrayList<>();//文件列表list
    //private FileListAdapter mFileListAdapter;//文件ListView适配器
    private String mRootPath;//文件根路径
    public static Stack<String> mNowPathStack;
    private boolean isViewCreated; //Fragment 的view 加载完毕的标记
    private boolean isUIVisible; // Fragment 对用户可见的标记

    //存放分类数据
    private List<Category> mCategoryData = new ArrayList<>();

    //文件扫描结束的处理
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            mCategoryData.clear();
            //mPullToRefreshLayout.finishRefresh();
            Map<String, Integer> countRes = (Map<String, Integer>) msg.obj;
            for (int i = 0; i < Constant.FILE_CATEGORY_ICON.length; i++) {
                Category category = new Category();
                category.setCategoryIcon(Constant.FILE_CATEGORY_ICON[i]);
                category.setCategoryName(Constant.FILE_CATEGORY_NAME[i]);
                category.setCategoryNums(countRes.get(Constant.FILE_CATEGORY_ICON[i].substring(3)) + "项");
                mCategoryData.add(category);
            }
            mLoadingFrameLayout.setVisibility(View.GONE);
            setData();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.usb_music_fragment_item, container,false);

        initView(view);

        return view;
    }

    private void initView(View view){
        tvMusicNumber = (TextView) view.findViewById(R.id.tv_music_number);
        mLoadingFrameLayout = (FrameLayout) view.findViewById(R.id.id_loading_framelayout);
    }

    /**
     * 设置数据
     */
    private void setData() {
/*
        CommonAdapter<Category> mAdapter = new CommonAdapter<Category>(this.getContext(), mCategoryData, R.layout.category_item) {
            @Override
            public void convert(ViewHolder helper, Category item) {
                helper.setImageResource(R.id.id_category_icon, getResId(item.getCategoryIcon()));
                helper.setText(R.id.id_category_name, item.getCategoryName());
                helper.setText(R.id.id_category_nums, item.getCategoryNums());
            }
        };
        mCategoryGridView.setAdapter(mAdapter);
*/
        StringBuffer content = new StringBuffer(" ");
        for (int i =0 ;i< mCategoryData.size();i++){
             content.append(mCategoryData.get(i).getCategoryName()+" "+ mCategoryData.get(i).getCategoryNums()+" 项");

        }
        tvMusicNumber.setText(content);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        isViewCreated = true;
        lazyLoad();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //isVisibleToUser这个boolean值表示:该Fragment的UI 用户是否可见
        if (isVisibleToUser) {
            isUIVisible = true;
            lazyLoad();
        } else {
            isUIVisible = false;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }


    private void lazyLoad(){
        // 这里进行双重标记判断，是因为setUserVisibleHint会多次调用，并且会在onCreateView执行前回调
        // 必须确保onCreateView 加载完毕且页面可见，才加载数据
        if (isViewCreated && isUIVisible){
            loadData();
            //数据记载完毕，恢复标记，防止重复加载
            isViewCreated = false;
            isUIVisible = false;
        }
    }

    private void loadData() {
        if (!EasyPermissions.hasPermissions(UsbMusicFragment.this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            EasyPermissions.requestPermissions(this,"需要读取文件目录",
                    REQUEST_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else {
            //已经授权
            //initEvent();
            //扫描文件
            scanFile();
        }
    }

    /**
     * 扫描文件
     */
    private void scanFile(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return;
        }
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        //单一线程线程池
        ExecutorService singleExecutorService = Executors.newSingleThreadExecutor();
        singleExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                ScanFileCountUtil scanFileCountUtil = new ScanFileCountUtil
                        .Builder(mHandler)
                        .setFilePath(path)
                        .setCategorySuffix(Constant.CATEGORY_SUFFIX)
                        .create();
                scanFileCountUtil.scanCountFile();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //initEvent();
        //扫描文件
        scanFile();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,
                Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //页面销毁，恢复标记
        isViewCreated = false;
        isUIVisible = false;
    }
}
