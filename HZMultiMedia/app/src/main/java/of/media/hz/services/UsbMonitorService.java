package of.media.hz.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.storage.StorageManager;
//import android.os.storage.VolumeInfo;
//import android.os.storage.StorageEventListener;
import java.io.File;
import java.util.ArrayList;

//import android.os.storage.DiskInfo;

public class UsbMonitorService extends Service {
    private static final String TAG = "UsbMonitorService";
    private StorageManager mStorageManager;

    public void onCreate(){
        super.onCreate();

        mStorageManager = getApplicationContext().getSystemService(StorageManager.class);
        //mStorageManager.registerListener(mStorageListener);
    }
/*
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            android.util.Log.d(TAG, "onVolumeStateChanged: oldState="+oldState+"newState="+newState);
            if (isInteresting(vol)) {
                //refresh();
                android.util.Log.d(TAG, "onVolumeStateChanged: isInteresting");
                if( isInteresting(vol) && oldState != newState && newState == VolumeInfo.STATE_MOUNTED){
                    android.util.Log.d(TAG, "onVolumeStateChanged: isMounted");
                    List<VolumeInfo> volumes = mStorageManager.getVolumes();
                    List<VolumeInfo> publicVolumes = new ArrayList<>();
                    publicVolumes.clear();
                    for (VolumeInfo info : volumes) {
                        int type = info.getType();
                        // 获取当前存储设备的路径
                        File path = info.getPath();
                        android.util.Log.d(TAG, "onVolumeStateChanged: path="+path);
                        // 同样的，只关心外置存储设备。
                        if (info.getType() == VolumeInfo.TYPE_PUBLIC) {
                            publicVolumes.add(info);
                        }else if(info.getType() == VolumeInfo.TYPE_PRIVATE){
                            // 获取内置存储设备
                        }
                    }

                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            //refresh();
        }
    };

    private static boolean isInteresting(VolumeInfo vol) {
        switch(vol.getType()) {
            // 内置存储设备
            case VolumeInfo.TYPE_PRIVATE:
                // 外置存储设备
            case VolumeInfo.TYPE_PUBLIC:
                return true;
            default:
                return false;
        }
    }
*/
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
