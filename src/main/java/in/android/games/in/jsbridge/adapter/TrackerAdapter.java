package in.android.games.in.jsbridge.adapter;

import in.android.games.in.jsbridge.Tracker;
import in.android.games.in.tracker.ZoneTracker;
import android.app.Activity;

public class TrackerAdapter extends BaseAdapter implements Tracker{
	
	public TrackerAdapter(Activity activity){
		super(activity);
	}
	
	@Override
	public void trackEvent(String category, String eventName){
		ZoneTracker.getInstance().trackEvent(category, eventName, "webview", 0);
	}

	@Override
	public void trackView(String view){
		ZoneTracker.getInstance().trackView(view);
	}

	@Override
	public void trackTiming(String action, String url, long value) {
		ZoneTracker.getInstance().trackTiming("WebView/Ajax", action, url, value);
	}
	
}
