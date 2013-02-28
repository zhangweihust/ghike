package zonesdk.in.android.games.in.activity;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager;
import com.google.analytics.tracking.android.EasyTracker;
import com.inmobi.adtracker.androidsdk.IMAdTracker;

import zonesdk.in.android.games.in.client.ClientFactory;
import zonesdk.in.android.games.in.client.RequestProvider;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.tracker.ZoneTracker;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


public abstract class BaseActivity extends FragmentActivity  {
	
	private static ClientFactory sClientFactory;
	
	private static RequestProvider sProvider;
	
	private ZoneAccountManager mAccountManager;
	
	private static final String USER_AGENT_FORMAT = "Zone/$1%s (Android $2%s)";
	
	private BroadcastReceiver mMessageReceiver;
	
	static {
		sClientFactory = ClientFactory.getInstance();
		sProvider = sClientFactory.getProvider();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAccountManager = new ZoneAccountManager(this);
		ZoneAccount account = getAccount();
		if(account != null){
			sProvider.setPassport(account.getPassport());
		}
		sProvider.setUserAgent(getUserAgent());
		
		RuntimeLog.log(this.getClass().toString() + " - BaseActivity - onCreate");

		mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Get extra data included in the Intent
				if(intent.getAction().compareTo(Constants.CLOSE_ALL_ACTIVITY)==0){
					RuntimeLog.log("mMessageReceiver - Got Broadcast: finish" );
					finish();
				}

			}
		};

		
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "custom-event-name".
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(Constants.CLOSE_ALL_ACTIVITY));
	}

	protected ZoneAccount getAccount(){
		return mAccountManager.getAccount();
	}
	
	protected ZoneAccountManager getAccountManager(){
		return mAccountManager;
	}
	
	protected <T> T getProxy(Class<T> type){
		return sClientFactory.get(type);
	}
	
	protected ZoneTracker getTracker(){
		//super.onStop();
		return ZoneTracker.getInstance();
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		mAccountManager.startListen();
	    EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		mAccountManager.stopListen();
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		RuntimeLog.log(this.getClass().toString() + " - onDestroy - unregisterReceiver mMessageReceiver");
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);
	}
	
	private String getUserAgent(){
		String pk = getApplicationInfo().packageName;
		PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(pk, PackageManager.GET_ACTIVITIES);
			return String.format(USER_AGENT_FORMAT, pi.versionName, Build.VERSION.RELEASE);
		} catch (NameNotFoundException e) {
			return null;
		}
	}
	
	
/*	public BaseActivity(){

	}
	
	
	protected void finalize() {


	}*/
	
	

	
}
