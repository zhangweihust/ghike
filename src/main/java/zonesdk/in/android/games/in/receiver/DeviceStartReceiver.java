package zonesdk.in.android.games.in.receiver;

import zonesdk.in.android.games.in.service.GameCheckService;
import zonesdk.in.android.games.in.service.SMSReceiverService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceStartReceiver extends BroadcastReceiver {

	private static final String TAG = "DeviceStartReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive begin");
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			Intent i = new Intent(context, GameCheckService.class);
			//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(i);
			Log.i(TAG,"GameCheckService begin to start");
			
			
			Intent j = new Intent(context, SMSReceiverService.class);
			context.startService(j);
			Log.i(TAG,"SMSReceiverService begin to start");
		}

	}


	
}
