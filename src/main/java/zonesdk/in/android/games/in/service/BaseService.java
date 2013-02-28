package zonesdk.in.android.games.in.service;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager;
import zonesdk.in.android.games.in.client.ClientFactory;
import zonesdk.in.android.games.in.client.RequestProvider;

import android.app.Service;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public abstract class BaseService extends Service {
	
	private static ClientFactory sClientFactory;
	
	private static RequestProvider sProvider;
	
	private ZoneAccountManager mAccountManager;
	
	private static final String USER_AGENT_FORMAT = "Zone/$1%s (Android $2%s)";

	static {
		sClientFactory = ClientFactory.getInstance();
		sProvider = sClientFactory.getProvider();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mAccountManager = new ZoneAccountManager(this);
		ZoneAccount account = getAccount();
		if(account != null){
			sProvider.setPassport(account.getPassport());
		}
		sProvider.setUserAgent(getUserAgent());
	}

	protected <T> T getProxy(Class<T> type){
		return sClientFactory.get(type);
	}
	
	protected ZoneAccount getAccount(){
		return mAccountManager.getAccount();
	}
	
	protected ZoneAccountManager getAccountManager(){
		return mAccountManager;
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
	
}
