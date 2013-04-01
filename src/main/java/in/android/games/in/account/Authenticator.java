package in.android.games.in.account;

import in.android.games.in.activity.LoginActivity;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class Authenticator extends AbstractAccountAuthenticator {
	
	private ZoneAccountManager mZoneAccountManager;
	
	private Context mContext;
	
	private Handler mHandler;
	
    public Authenticator(Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler(context.getMainLooper());
        mZoneAccountManager =  new ZoneAccountManager(context);
    }

	@Override
	public Bundle editProperties(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			String paramString) {
		return null;
	}

	@Override
	public Bundle addAccount(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			String paramString1, String paramString2,
			String[] paramArrayOfString, Bundle paramBundle)
			throws NetworkErrorException {
		ZoneAccount zoneAccount = mZoneAccountManager.getAccount();
		Bundle result = new Bundle();
		if(zoneAccount != null){
			result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_CANCELED);
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "There is already a zone account");
			mHandler.post(new Runnable(){
				@Override
				public void run(){
					Toast.makeText(mContext,  "There is already a zone account", Toast.LENGTH_SHORT).show();
				}
			});
		}else{
			Intent intent = new Intent(mContext, LoginActivity.class);
			result.putParcelable(AccountManager.KEY_INTENT, intent);
		}
		return result;
	}

	@Override
	public Bundle confirmCredentials(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			Account paramAccount, Bundle paramBundle)
			throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle getAuthToken(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			Account paramAccount, String paramString, Bundle paramBundle)
			throws NetworkErrorException {
		return null;
	}

	@Override
	public String getAuthTokenLabel(String paramString) {
		return null;
	}

	@Override
	public Bundle updateCredentials(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			Account paramAccount, String paramString, Bundle paramBundle)
			throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(
			AccountAuthenticatorResponse paramAccountAuthenticatorResponse,
			Account paramAccount, String[] paramArrayOfString)
			throws NetworkErrorException {
		return null;
	}

}
