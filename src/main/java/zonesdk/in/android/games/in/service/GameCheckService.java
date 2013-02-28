package zonesdk.in.android.games.in.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager;
import zonesdk.in.android.games.in.account.ZoneAccountManager.OnLoginListener;
import zonesdk.in.android.games.in.account.ZoneAccountManager.OnLogoutListener;
import zonesdk.in.android.games.in.db.GameHelper;
import zonesdk.in.android.games.in.proxy.GameClient;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public class GameCheckService extends BaseService {
	
	private static final String KEY_EXPECT_TIME = "expect-load-time";

	private static final long LOAD_PERIOD = 24 * 1 * 60 * 60 * 1000;  //1 day

	private static final long LOAD_FAILED_PERIOD = 10 * 60 * 1000;  //10 min
		
	private FetchGameThread mThread = null;
	
	public Context mContext = this;
		
	@Override
	public IBinder onBind(Intent arg0) {			
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		initAccountListener();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		startThread();
	}
	
	public void onDestroy(){
		super.onDestroy();
		stopThread();
	}
	
	private void startThread(){
		if(getAccount() == null)
			return;
		if(mThread != null)
			return;
		mThread = new FetchGameThread();
		mThread.start();
	}
	
	private void stopThread(){
		if(mThread != null){
			mThread.mAlive = false;
			synchronized(this){
				notify();
			}
		}
		mThread = null;
	}
	
	private void initAccountListener(){
		ZoneAccountManager manager = getAccountManager();
		manager.addLoginListener(mLoginListener);
		manager.addLogoutListener(mLogoutListener);
	}
	
	private OnLoginListener mLoginListener = new OnLoginListener(){

		@Override
		public void onAccountLogin(ZoneAccount account) {
			startThread();
		}
		
	};
	
	private OnLogoutListener mLogoutListener = new OnLogoutListener(){

		@Override
		public void onAccountLogout() {
			stopThread();
		}
		
	};
	
	public class FetchGameThread extends Thread{
		
		private boolean mAlive = true;
		
		FetchGameThread(){
			super("FetchGameThread");
		}
		
		@Override
		public void run(){
			while(mAlive){
				while(mAlive){
					long waitTime = checkWaitTime();
					if(waitTime > 0){
						try{
							synchronized(GameCheckService.this){
								GameCheckService.this.wait(waitTime);
							}
						}catch(Exception e){
							e.printStackTrace();
							return;
						}
					}else{
						break;
					}
				}
				try {
					loadGamesFromProxy();
					logExpectTime(System.currentTimeMillis() + LOAD_PERIOD);
				}catch(Exception e){
					e.printStackTrace();
					logExpectTime(System.currentTimeMillis() + LOAD_FAILED_PERIOD);
				}
			}
		}
		
		private void loadGamesFromProxy() throws Exception {
			GameHelper mGameData = new GameHelper(mContext);
			mGameData.deleteAll();
			mGameData.close();
			ArrayList<ContentValues> contentList = new ArrayList<ContentValues>();
			GameClient client = getProxy(GameClient.class);
			JSONObject gameForum = client.getAllGames();
			int status = gameForum.getInt("status");
			JSONArray AllgameForum = new JSONArray();
			switch (status) {
			case 0:
				AllgameForum = gameForum.getJSONArray("gameforumList");
				for (int i = 0; i < AllgameForum.length(); i++) {
					ContentValues values = new ContentValues();
					JSONObject jsonObject = AllgameForum.getJSONObject(i);
					int appId = jsonObject.getInt("appId");
					int forumId = jsonObject.getInt("forumId");
					String appIcon =  jsonObject.getString("appIcon");
					String appIconCover = jsonObject.getString("appIconCover");
					String title = jsonObject.getString("title");
					String createtime = jsonObject.getString("createTime");
					String description = jsonObject.getString("description");
					int platformtype = jsonObject.getInt("platformType");
					String packagename = jsonObject.getString("packageName");
					String downloadurl = jsonObject.getString("downloadUrl");
					double apksize = jsonObject.getDouble("apkSize");
					int enableScreenshot = jsonObject.getInt("enableScreenshot");				
					values.put("app_id", appId);
					values.put("forum_id", forumId);
					values.put("title", title);
					values.put("create_time", createtime);
					values.put("appIcon", appIcon);
					values.put("appIconCover", appIconCover);
					values.put("description", description);
					values.put("platform_type", platformtype);
					values.put("package_name", packagename);
					values.put("download_url", downloadurl);
					values.put("apk_size", apksize);
					values.put("enable_screenshot", enableScreenshot);
					contentList.add(values);
				}
				mGameData.insert(contentList);
				break;
			default:
				break;
			}
		}
		
		private void logExpectTime(long expectTime){
			SharedPreferences pref = mContext.getSharedPreferences("gamecheck", MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putLong(KEY_EXPECT_TIME, expectTime);
			editor.commit();
		}
		
		private long checkWaitTime(){
			SharedPreferences pref = mContext.getSharedPreferences("gamecheck", MODE_PRIVATE);
			long currentTime = System.currentTimeMillis();
			long expectTime = pref.getLong(KEY_EXPECT_TIME, currentTime);
			return Math.max(expectTime - currentTime, 0);
		}
		
	}
}
