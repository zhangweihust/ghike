package in.android.games.in;

import zonesdk.in.android.games.in.R;
import android.app.Application;
import android.content.res.Resources;

public class ZoneApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Resources res = getResources();
		if (res.getBoolean(R.bool.crashlog_open)) {
			CrashHandler crashHandler = CrashHandler.getInstance();
			crashHandler.init(getApplicationContext());
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
}
