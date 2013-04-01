package in.android.games.in.jsbridge;

public interface AppManager {

	String KEY_APP_NAME = "appName";

	String KEY_PACKAGE_NAME = "packageName";

	String KEY_LOCATION = "location";

	String KEY_ERROR_MSG = "errorMsg";

	String KEY_PROGRESS = "progress";
	
	String FUNCTION_ON_BEGIN_DOWNLOAD = "window.onBeginDownload";
	
	String FUNCTION_ON_FINISHED_DOWNLOAD = "window.onFinishedDownload";
	
	String FUNCTION_ON_START_INSTALL = "window.onStartInstall";
	
	String FUNCTION_ON_FINISHED_INSTALL = "window.onFinishedInstall";
	
	String FUNCTION_ON_DOWNLOAD_PROGRESS = "window.onDownloadProgress";
	
	String FUNCTION_ON_DOWNLOAD_ERROR = "window.onDownloadError";

	public boolean isAppInstalled(String packageName);
	
	public int getProgress(String packageName);

	public void download(String appName, String packageName, String location);
	
	public void downloadFromMarket(String packageName, String location);
	
	public void install(String appName, String packageName, String location);
	
	public void start(String packageName);
	
	public void startURL(String url);
	
	public void startForCompete(String url,String packageName);

	public void pause(String packageName);
	
	public void cancel(String packageName);
	
}
