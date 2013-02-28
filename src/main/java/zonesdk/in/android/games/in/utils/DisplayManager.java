package zonesdk.in.android.games.in.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class DisplayManager {
	
	private static final String TAG = "DisplayManager";

	public static final float DENSITY = Resources.getSystem().getDisplayMetrics().density;

	private final static int sTabPageSize = 2;
	public static int[] sTabMetrics = new int[3];
	
	public static int SCREEN_WIDTH = 0;
	public static int SCREEN_HEIGHT = 0;
	
	public static void setupVariables(Activity activity){
		DisplayMetrics metrics = new DisplayMetrics();
		
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		SCREEN_WIDTH = metrics.widthPixels;
		
		SCREEN_HEIGHT = metrics.heightPixels;
	}

	public static int[] getTabMetrics(Activity activity) {
		int tabWidth = SCREEN_WIDTH / sTabPageSize;
		int tabHeight = SCREEN_HEIGHT / 15;
		int offset = tabWidth * 4 / 5;
		// int offset = tabWidth;

		sTabMetrics[0] = tabWidth;
		sTabMetrics[1] = tabHeight;
		sTabMetrics[2] = offset;
		
		return sTabMetrics;
	}


	public static int dipToPixel(int i) {
		float f1 = i;
		float f2 = DENSITY;
		return (int) (f1 * f2 + 0.5F);
	}
	
	public static void setFullScreen(Activity window){
		window.requestWindowFeature(Window.FEATURE_NO_TITLE);
		window.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}
