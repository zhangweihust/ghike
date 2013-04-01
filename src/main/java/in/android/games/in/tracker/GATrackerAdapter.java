package in.android.games.in.tracker;

import com.google.analytics.tracking.android.EasyTracker;

public class GATrackerAdapter extends ZoneTracker{
	
	GATrackerAdapter(){}
	
	@Override
	public void trackView(String viewName) {
		try{
			EasyTracker.getTracker().sendView(viewName);
		}catch(Throwable t){
			t.printStackTrace();
		}
	}

	@Override
	public void trackEvent(String category, String eventName, String label, long value) {
		try{
			EasyTracker.getTracker().sendEvent(category, eventName, label, value);
		}catch(Throwable t){
			t.printStackTrace();
		}
	}

	@Override
	public void trackTiming(String category, String eventName, String label, long value) {
		try{
			EasyTracker.getTracker().sendTiming(category, value, eventName, label);
		}catch(Throwable t){
			t.printStackTrace();
		}
	}

}
