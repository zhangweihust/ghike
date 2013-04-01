package in.android.games.in.tracker;

import android.util.Log;

public abstract class ZoneTracker {

	private static ZoneTracker instance;
	
	public abstract void trackView(String viewName);
	
	public abstract void trackEvent(String category, String eventName, String label, long value);
	
	public abstract void trackTiming(String category, String eventName, String label, long value);
	
	public static ZoneTracker getInstance(){
		if(instance == null)
			instance = new CompositeTracker(new GATrackerAdapter());
		return instance;
	}
	
	private static class CompositeTracker extends ZoneTracker {

		ZoneTracker[] mTrackers;
		
		CompositeTracker(ZoneTracker... trackers) {
			mTrackers = trackers;
		}
		
		@Override
		public void trackView(String viewName) {
			for(ZoneTracker tracker:mTrackers){
				try{
					tracker.trackView(viewName);
				}catch(Exception e){
					Log.e("ZoneTracker", "Failed to tracker view", e);
				}
			}
		}

		@Override
		public void trackEvent(String category, String eventName, String label, long value) {
			for(ZoneTracker tracker:mTrackers){
				try{
					tracker.trackEvent(category, eventName, label, value);
				}catch(Exception e){
					Log.e("ZoneTracker", "Failed to tracker event", e);
				}
			}
		}

		@Override
		public void trackTiming(String category, String eventName, String label, long value) {
			for(ZoneTracker tracker:mTrackers){
				try{
					tracker.trackTiming(category, eventName, label, value);
				}catch(Exception e){
					Log.e("ZoneTracker", "Failed to tracker timing", e);
				}
			}
		}
		
	}
	
}
