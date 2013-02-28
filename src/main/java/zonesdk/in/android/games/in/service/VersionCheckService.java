package zonesdk.in.android.games.in.service;


import org.json.JSONObject;

import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.proxy.VersionClient;
import zonesdk.in.android.games.in.receiver.VersionCheckReceiver;
import zonesdk.in.android.games.in.utils.RuntimeLog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class VersionCheckService extends BaseService {

	private static final String TAG = "VersionCheckService";
	
	private final long time = 1*60*60*1000;  //1 hour
		
	//private FetchGameThread mThread = null;
	
	static public VersionCheckService mService = null;
	
	private  AlarmManager alarms;
	private  PendingIntent alarmIntent;
	
	private FetchVersionTask lastLookup; 
	
		
	@Override
	public IBinder onBind(Intent arg0) {			
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		mService = this;
		RuntimeLog.log("VersionCheckService.onCreate");
		
	    alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

	    String ALARM_ACTION;
	    ALARM_ACTION = VersionCheckReceiver.ACTION_REFRESH_VERSIONCHECK_ALARM; 
	    Intent intentToFire = new Intent(ALARM_ACTION);
	    alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		RuntimeLog.log("VersionCheckService.onStart");
	}
	
	public void onDestroy(){
		super.onDestroy();	
		RuntimeLog.log("VersionCheckService.onDestroy");
		//mThread.mAlive = false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) { 
		
	    int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
	    long timeToRefresh = SystemClock.elapsedRealtime() + time;
	    alarms.setRepeating(alarmType, timeToRefresh, time, alarmIntent);  
	      
	    refreshVersionCheck();
	    
	      //alarms.cancel(alarmIntent);
		return Service.START_NOT_STICKY;
	}
	
	public void refreshVersionCheck() {
	    if (lastLookup==null ||
	    		lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
	      lastLookup = new FetchVersionTask();
	      lastLookup.execute((Void[])null);

	    }
	}
	
/*	public void refreshVersionCheck(Activity mContext) {
	    if (lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
	    	//lastLookup = new FetchVersionTask();
	    	lastLookup.execute((Void[])null);
	    }else{
	        lastLookup.cancel(true);
	    	lastLookup.execute((Void[])null);
	    }
	}*/
	
	private class FetchVersionTask extends AsyncTask<Void,Void,Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			Log.i("VersionCheckService", "doInBackground -  fetching Version.");
			boolean flag = false;
			
			try {
				RuntimeLog.log("VersionCheckService.FetchVersionThread - run, sleep very time");
				VersionClient client = mService.getProxy(VersionClient.class);
				JSONObject jsonObject = client.getVersion(Constants.SOURCE);
				
				int status = jsonObject.getInt("status");
				Log.w(TAG,"status =" + status);
				//JSONArray AllgameForum = new JSONArray();		

				switch (status) {
				case 0:
					
					//AllgameForum = versionForum.getJSONArray("gameforumList");

  					//JSONObject jsonObject = versionForum.getJSONObject(0);
  					String versionName =  jsonObject.getString("versionName");
  					int versionCode = jsonObject.getInt("versionCode");
  					int minversionCode = jsonObject.getInt("minversionCode");
  					String description = jsonObject.getString("description");
  					String downloadurl = jsonObject.getString("downloadUrl");

  					SharedPreferences sp = mService.getSharedPreferences("version", MODE_PRIVATE);
  					
  					Editor editor = sp.edit();
  					
  					if(sp.getInt("versionCode", 0)<versionCode){
  						//a newer version detected!
  						
  						//reset promtNum to 0
  						editor.putInt("promtNum", 0);
  						
  					}

  					editor.putString("versionName", versionName);
  					editor.putInt("versionCode", versionCode);
  					editor.putInt("minversionCode", minversionCode);
  					editor.putString("description", description);
  					editor.putString("downloadurl", downloadurl);
  					editor.commit();

  					flag = true;
					break;
				default:
					break;
				}

			}catch(Exception e){
				e.printStackTrace();
			}finally{
				
			}
						
			return flag;
		}
		
		

		@Override  
		protected void onPostExecute(Boolean result) {  

			Log.i(TAG, "onPostExecute(Result result) called");  

		}  


		  
	}

	


}
