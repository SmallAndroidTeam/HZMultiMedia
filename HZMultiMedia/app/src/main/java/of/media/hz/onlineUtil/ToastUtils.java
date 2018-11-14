package of.media.hz.onlineUtil;

import android.content.Context;
import android.widget.Toast;

import of.media.hz.Application.App;


/**
 * Toast工具类
 * Created by wcy on 2015/12/26.
 */
public class ToastUtils {
    private static Context sContext;
    private static Toast sToast;
    public static void show(int resId) {
        if(sContext==null)
        sContext= App.sContext;
        show(sContext.getString(resId));
    }

    public static void show(String text) {
        if (sToast == null) {
            sToast = Toast.makeText(App.sContext, text, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(text);
        }
        sToast.show();
    }
}
