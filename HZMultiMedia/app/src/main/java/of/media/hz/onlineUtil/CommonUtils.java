package of.media.hz.onlineUtil;

import android.os.Build;

public class CommonUtils {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
