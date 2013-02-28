package zonesdk.in.android.games.in.receiver;

import zonesdk.in.android.games.in.common.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	private static final String TAG = "SMSReceiver";
	
	private  Context mActivity;

	private static boolean pincodeFlag;
	
	public  SMSReceiver(Context Activity){
		mActivity = Activity;
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		Log.i(TAG, "onReceive begin");
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Object[] extra = (Object[]) extras.get("pdus");
			if (extra!=null){
				for (int i = 0; i < extra.length; ++i) {
					SmsMessage sms = SmsMessage.createFromPdu((byte[]) extra[i]);
					String body = sms.getMessageBody();
					
					pincodeFlag = processSMSPinCode(body);
					Log.i(TAG, "set  pincode's valid is: " + pincodeFlag);
					if (pincodeFlag) {	
						//check if the SMS timeout, true: keep-forward, false: abort it
						SharedPreferences settings = mActivity.getSharedPreferences("SMS", context.MODE_WORLD_READABLE);
						if(settings.getBoolean("ABORTSMS", false)){
							this.abortBroadcast();
						}

						break;
					}
				}
			}
		}
		

		Log.i(TAG, "onReceive finish successful");
		
	}
	
	
	private boolean processSMSPinCode(String msg){
		Log.i(TAG, "processSMSPinCode - getSMSPinCode msg=" + msg);
		String longcode ="";
		if(msg.contains(Constants.PIN_CODE_PREFIX_PAYMENT)){
            String[] codes = msg.split(Constants.PIN_CODE_PREFIX_PAYMENT);
            if(codes.length<1){
            	//check the pin is null
            	return false;
            }
            longcode = codes[codes.length -1];
            if(longcode != null){
            	sendMessage(Constants.PIN_CODE_TYPE_PAYMENT, longcode.substring(0,4));
            	return true;
            }

		}else if(msg.contains(Constants.PIN_CODE_PREFIX)){
	        String[] codes = msg.split(Constants.PIN_CODE_PREFIX);
            if(codes.length<1){
            	//check the pin is null
            	return false;
            }
	        String code = codes[codes.length -1];
	        sendMessage(Constants.PIN_CODE_TYPE_LOGIN, code);
	        return true;
	    }
		
	    return false;
	    
	}
	
	
	private void sendMessage(String type, String value) {
		  Log.d("sender", "sendMessage - type: " + type + " value:" + value);
		  Intent intent = new Intent(type);
		  // You can also include some extra data.
		  intent.putExtra("pincode", value);
		  LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
	}


}
