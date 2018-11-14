package of.media.hz.Application;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.litepal.LitePal;

public class App extends Application {
	public static Context sContext;
	public static int sScreenWidth;
	public static int sScreenHeight;
	private String TAG="App";
	@Override
	public void onCreate() {
		super.onCreate();
		LitePal.initialize(this);
		//sContext =getApplicationContext();
		Log.i(TAG, "onCreate: "+(sContext!=null));
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
	}
}
