package in.android.games.in.jsbridge;

public interface Platform {
	
	public int getVersionCode();
	
	public String getVersionName();
	
	public void requestCheckUpdatesDialog(boolean forceCheck);
	
}
