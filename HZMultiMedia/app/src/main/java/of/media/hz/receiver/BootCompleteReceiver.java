package of.media.hz.receiver;

import android.content.Context;
import android.content.Intent;

import android.content.BroadcastReceiver;
import of.media.hz.services.UsbMonitorService;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent hvacController = new Intent(context, UsbMonitorService.class);
        context.startService(hvacController);
    }
}
