package in.android.games.in.account;

import in.android.games.in.common.Constants;
import in.android.games.in.utils.CookieUtil;

import java.util.HashSet;
import java.util.Set;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.os.Bundle;

public class ZoneAccountManager {
	
	private static final String KEY_CURRENT_ACCOUNTNUMBER = "CURRENT_ACCOUNTNUMBER";
	
	private static final String KEY_TYPE = "type";
	
	private static final String KEY_PASSPORT = "passport";
	
	private static final String KEY_USER_ID = "userId";
	
	private static final String KEY_USER_NAME = "userName";
	
	private static final String KEY_HEAD_URL = "headUrl";
	
	private static final String KEY_COVER_URL = "coverUrl";
	
	private AccountManager mAccountManager;
	
	private AccountAdapter mAccountAdapter;
	
	private Set<OnLoginListener> mLoginListeners;
	
	private Set<OnLogoutListener> mLogoutListeners;
	
	public ZoneAccountManager(Context context){
		mLoginListeners = new HashSet<OnLoginListener>();
		mLogoutListeners = new HashSet<OnLogoutListener>();
		mAccountManager = AccountManager.get(context);
		prepareAccount();
	}
	
	public void startListen(){
		mAccountManager.addOnAccountsUpdatedListener(mAccountsUpdateListener, null, false);
	}
	
	public void stopListen(){
		mAccountManager.removeOnAccountsUpdatedListener(mAccountsUpdateListener);
	}
	
	private void prepareAccount(){
		Account[] accounts = mAccountManager.getAccountsByType(ZoneAccount.ACCOUNT_TYPE);
		if(accounts.length > 0)
			mAccountAdapter = new AccountAdapter(accounts[0]);
		else
			mAccountAdapter = null;
	}
	
	public ZoneAccount login(String userId, String number, String userName, String type, 
			String headUrl, String coverUrl, String passport) {
		if(mAccountAdapter != null)
			logout();
		Account newAccount = new Account(userName, Constants.ACCOUNT_TYPE);
		Bundle bundle = new Bundle();
		bundle.putString(KEY_CURRENT_ACCOUNTNUMBER, number);
		bundle.putString(KEY_TYPE, type);
    	bundle.putString(KEY_PASSPORT, passport);
    	bundle.putString(KEY_USER_ID, userId);
    	bundle.putString(KEY_USER_NAME, userName);
    	bundle.putString(KEY_HEAD_URL, headUrl);
    	bundle.putString(KEY_COVER_URL, coverUrl);
    	mAccountManager.addAccountExplicitly(newAccount, null, bundle);
    	mAccountAdapter = new AccountAdapter(newAccount);
    	publishLogin();
    	return mAccountAdapter;
	}
	
	public void logout(){
		mAccountManager.removeAccount(mAccountAdapter.mAccount, null, null);
		publishLogout();
		mAccountAdapter = null;
	}
	
	public ZoneAccount getAccount(){
		prepareAccount();
		return mAccountAdapter;
	}
	
	public void addLoginListener(OnLoginListener listener){
		mLoginListeners.add(listener);
	}
	
	private void publishLogin(){
    	for(OnLoginListener l:mLoginListeners){
    		l.onAccountLogin(mAccountAdapter);
    	}
	}
	
	public void addLogoutListener(OnLogoutListener listener){
		mLogoutListeners.add(listener);
	}
	
	private void publishLogout(){
    	for(OnLogoutListener l:mLogoutListeners){
    		l.onAccountLogout();
    	}
	}
	
	private OnAccountsUpdateListener mAccountsUpdateListener = new OnAccountsUpdateListener(){

		@Override
		public void onAccountsUpdated(Account[] accounts) {
			if(mAccountAdapter == null){
				for(Account account : accounts){
					if(account.type.equals(ZoneAccount.ACCOUNT_TYPE)){
						prepareAccount();
						publishLogin();
						break;
					}
				}
			}else{
				prepareAccount();
				if(mAccountAdapter == null)
					publishLogout();
			}
			
		}
		
	};
	
	public static interface OnLoginListener {
		
		void onAccountLogin(ZoneAccount account);
		
	}
	
	public static interface OnLogoutListener {
		
		void onAccountLogout();
		
	}
	
	private class AccountAdapter implements ZoneAccount{

		private Account mAccount;
		
		AccountAdapter(Account account){
			mAccount = account;
		}
		
		@Override
		public String getCoverUrl() {
			return mAccountManager.getUserData(mAccount, KEY_COVER_URL);
		}

		@Override
		public String getHeadUrl() {
			return mAccountManager.getUserData(mAccount, KEY_HEAD_URL);
		}

		@Override
		public String getNumber() {
			return mAccountManager.getUserData(mAccount, KEY_CURRENT_ACCOUNTNUMBER);
		}

		@Override
		public String getPassport() {
			return mAccountManager.getUserData(mAccount, KEY_PASSPORT);
		}

		@Override
		public String getPassportCookieValue() {
			return CookieUtil.crossCookieName("passport") + "=\"" + getPassport() + "\"; Domain=" + Constants.ROOT_DOMAIN;
		}

		@Override
		public String getUserId() {
			return mAccountManager.getUserData(mAccount, KEY_USER_ID);
		}

		@Override
		public String getUserName() {
			return mAccountManager.getUserData(mAccount, KEY_USER_NAME);
		}

		@Override
		public boolean isAirtel() {
			return ZoneAccount.TYPE_AIRTEL.equals(mAccountManager.getUserData(mAccount, KEY_TYPE));
		}

		@Override
		public void setCoverUrl(String coverUrl) {
			mAccountManager.setUserData(mAccount, KEY_COVER_URL, coverUrl);
		}

		@Override
		public void setHeadUrl(String headUrl) {
			mAccountManager.setUserData(mAccount, KEY_HEAD_URL, headUrl);
		}

		@Override
		public void setUserName(String userName) {
			mAccountManager.setUserData(mAccount, KEY_USER_NAME, userName);
		}
		
	}
	
}
