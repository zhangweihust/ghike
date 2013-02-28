package zonesdk.in.android.games.in.activity;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager.OnLogoutListener;
import zonesdk.in.android.games.in.service.SMSReceiverService;
import android.content.Intent;
import android.os.Bundle;

public abstract class LoginRequiredActivity extends BaseActivity implements OnLogoutListener {

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//the service must run before login
		startService(new Intent(this, SMSReceiverService.class));
		
		//onCreateForUpdates(); //check updates before login 
		
		getAccountManager().addLogoutListener(this);
		ZoneAccount account = checkRequireLogin();
		if(account != null)
			onCreateWithAccount(savedInstanceState, account);
	}
	
	@Override
	public final void onResume(){
		super.onResume();
		ZoneAccount account = checkRequireLogin();
		if(account != null)
			onResumeWithAccount(account);
	}
	
	@Override
	public void onAccountLogout() {
		finish();
	}

	private ZoneAccount checkRequireLogin(){
		ZoneAccount account = getAccount();
		if(account == null){
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			
			return null;
		}
		return account;
	}
	
	protected void onCreateWithAccount(Bundle savedInstanceState, ZoneAccount account){}
	
	protected void onResumeWithAccount(ZoneAccount account){}
	
	protected void onCreateForUpdates(){}
	
}
