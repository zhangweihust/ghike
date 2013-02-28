package zonesdk.in.android.games.in.service;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager;
import zonesdk.in.android.games.in.activity.LoginActivity;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.common.SDKStatus;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class PassportService extends Service {

	private static final String TAG = "PassportService";
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		return new Messenger(new GetPassportHandler(getApplicationContext())).getBinder();
		
	}
	
	private static final class GetPassportHandler extends Handler{
		
		private final Context context;
		
		public GetPassportHandler(Context context){
			this.context = context;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if(msg.what != Constants.MESSAGE_WHAT_API){
				super.handleMessage(msg);
				return;
			}else if(msg.replyTo==null){
				return;
			}
			try{
				ZoneAccountManager zoneAccountManager = new ZoneAccountManager(context);
				ZoneAccount account = zoneAccountManager.getAccount();
				String passport = (account!=null && account.getPassport()!=null)?account.getPassport():null;
				if(passport != null){
					Message reply = Message.obtain(null, Constants.MESSAGE_WHAT_HIKE_CLIENT, 500, 0);
					Bundle data = new Bundle();
					data.putInt("status", SDKStatus.SUCESS.getValue());
					data.putString("passport", passport);
					reply.setData(data);
					try {
						msg.replyTo.send(reply);
						Log.d(TAG, "Reply passport to SDK");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}else{
					Intent intent = new Intent(context, LoginActivity.class);
					intent.putExtra("cmd", Constants.CMD_GET_PASSPORT);
					intent.putExtra("messenger", msg.replyTo);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);				
				}
			}catch(Exception e){
				e.printStackTrace();
				Message reply = Message.obtain(null, Constants.MESSAGE_WHAT_HIKE_CLIENT, 500, 0);
				Bundle data = new Bundle();
				data.putInt("status", SDKStatus.ERROR.getValue());
				reply.setData(data);
				try {
					msg.replyTo.send(reply);
					Log.w(TAG, "Cannot reply passport to SDK");
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		}
		
	}
	
}
