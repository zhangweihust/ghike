package zonesdk.in.android.games.in.jsbridge;

public interface Tracker {
	
	String INTERFACE_NAME = "Tracker";

	void trackEvent(String category, String eventName);

	void trackTiming(String type, String name, long time);

	void trackView(String view);
	
}
