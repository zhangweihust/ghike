package zonesdk.in.android.games.in.activity;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adtracker.androidsdk.IMAdTracker;

import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.account.ZoneAccountManager;
import zonesdk.in.android.games.in.client.Callback;
import zonesdk.in.android.games.in.client.ResponseWrapper;
import zonesdk.in.android.games.in.client.SignClient;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.common.SDKStatus;
import zonesdk.in.android.games.in.fragment.LoginAirtelFragment;
import zonesdk.in.android.games.in.fragment.NoAirtelLoginNumberFragment;
import zonesdk.in.android.games.in.fragment.NoAirtelLoginPincodeFragment;
import zonesdk.in.android.games.in.fragment.SignInPincodeFragment;
import zonesdk.in.android.games.in.fragment.SignupNicknameFragment;
import zonesdk.in.android.games.in.fragment.TermOfServiceFragment;
import zonesdk.in.android.games.in.fragment.WelcomeFragment;
import zonesdk.in.android.games.in.receiver.SMSReceiver;
import zonesdk.in.android.games.in.update.UpdateManager;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.widget.LoginLayout;
import zonesdk.in.android.games.in.widget.LoginLayout.OnSoftInputListener;
import zonesdk.in.android.games.in.R;
import android.app.Activity;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends BaseActivity {

	private static final int CHECK_NOT_CHECK_PINCODE = 0; // to welcome 
	private static final int CHECK_PASSPORT_LOGIN_SUCESS = 1 ; //  
	private static final int CHECK_TRACE_NICKNAME_LOGIN_SUCESS = 2 ; // to nickname 
	
	public static final int PULL_PIN = 0;
	public static final int PULL_MST = 1;
	public static final int PULL_OUT = 2;
	public static final int SMS_RCVD = 3;
	public static final int CLOSE_LOGIN = 4;
	
	public final int PULL_PIN_DELAY = 60*1000;
	
	public  Activity mActivity = this;
	private static final String TAG = "LoginActivity";
	
	public  MyHandler mHandler;// = new MyHandler(this);
	
	public  final int LOGIN_VIEW_WELCOME = 0; // to welcome 
	public  final int LOGIN_VIEW_TERMOFSERVICE = 1 ; //  
	public  final int LOGIN_VIEW_AIRTEL_LOGINNUM = 2 ; // 
	public  final int LOGIN_VIEW_NOAIRTEL_LOGINNUM = 3 ; //  
	public  final int LOGIN_VIEW_SIGNIN_PIN = 4 ; // 
	public  final int LOGIN_VIEW_SIGNUP_PIN = 5 ; //  
	public  final int LOGIN_VIEW_SIGNUP_NICK = 6; //  

	
	public WelcomeFragment mWelcomeFragment;
	public TermOfServiceFragment mTermOfServiceFragment;
	public LoginAirtelFragment mLoginAirtelFragment;
	public NoAirtelLoginNumberFragment mNoAirtelLoginNumberFragment;
	public SignInPincodeFragment mSignInPincodeFragment;
	public NoAirtelLoginPincodeFragment mNoAirtelLoginPincodeFragment;
	public SignupNicknameFragment mSignupNicknameFragment;
	
	private FragmentManager fm ;
	private  SMSReceiver smsReciver;
	private ZoneAccountManager mHikeAccountManager;
	
	public UIHelper mUIHelper;
	private String pincode;
	private String phonenumber;
	private List<Messenger> mGetPassportReceivers = new ArrayList<Messenger>();
	
	public Context mContext ;
	private BroadcastReceiver mMessageReceiver; 
	//private static Timer SMStimer; 
	
	private UpdateManager mUpdateManager;
	
	public SharedPreferences settings ;

	
	private static final String APP_ID = "031a9f42-fb36-42a5-aa73-d2254139a153";
	
	public int userIntentFlag;
	public enum UserType { 
		INIT        //ignore sms received
        ,REGISTED      //user  registered before
        ,NOT_REGISTED  //user not registered before

    };

    
	enum State { 
		  INIT      
      ,PULL_IN      
      ,PULL_OUT  
    };
  
  public  State stateType = State.INIT;
  public  UserType userType = UserType.INIT;
	
	//================================activity lifecycle callback begin==================================
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate() begin.");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_layout);
		
		RuntimeLog.log("LoginActivity.onCreate()");
		
		//passDataToSharedPreferences();
		mHikeAccountManager = new ZoneAccountManager(this);
		mUIHelper = new UIHelper();
		
		mHandler = new MyHandler(this);
		
		/**
		 * @author zhangwei
		 * This BroadcastReceiver class used to get pincode from MySMSRecevier
		 */
		mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Get extra data included in the Intent
				if(intent.getAction().compareTo(Constants.CLOSE_ALL_ACTIVITY)==0){
/*					String message = intent.getStringExtra("pincode");
					Log.d("receiver", "Got Broadcast: CLOSE_CODE_TYPE_LOGIN" + message);
					
					mHandler.sendEmptyMessage(CLOSE_LOGIN);*/
				}else if(intent.getAction().compareTo(Constants.PIN_CODE_TYPE_LOGIN)==0){
					String message = intent.getStringExtra("pincode");
					Log.d("receiver", "Got Broadcast: PIN_CODE_TYPE_LOGIN message:" + message);
					
					Message msg = new Message();
					msg.what = SMS_RCVD;
					msg.obj = message;
					mHandler.sendMessage(msg);
				}

			}
		};
	
		
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "custom-event-name".
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver,
				new IntentFilter(Constants.PIN_CODE_TYPE_LOGIN));

		settings = getSharedPreferences("loginXML", 0);

		
		mWelcomeFragment = new WelcomeFragment();
		mTermOfServiceFragment =new TermOfServiceFragment();
		mLoginAirtelFragment = new LoginAirtelFragment();
		mNoAirtelLoginNumberFragment = new NoAirtelLoginNumberFragment();
		mSignInPincodeFragment = new SignInPincodeFragment();
		mNoAirtelLoginPincodeFragment = new NoAirtelLoginPincodeFragment();
		mSignupNicknameFragment =  new SignupNicknameFragment();
		
	    fm = getSupportFragmentManager();
	    //fm = getFragmentManager();
	    //fm = getChildFragmentManager();
		
		boolean isLogoutAirtel = false;
		Bundle extras = getIntent().getExtras();
		int checkValue = checkLoginStatus();
		
		if (extras != null) {
			String cmd = extras.getString("cmd");
			isLogoutAirtel = Constants.CMD_LOGOUT_AIRTEL.equals(cmd);
		}
		
		RuntimeLog.log("checkValue:" + checkValue + "isLogoutAirtel:" + isLogoutAirtel);
		
		if(isLogoutAirtel){
			//history.go(mLogoutAlreadyViewWrapper);
		}else{
			//initView(checkValue);
		}
		
		
		if(VERSION.SDK_INT != 9 && VERSION.SDK_INT != 10){
			IMAdTracker.getInstance().init(getApplicationContext(), APP_ID);
			IMAdTracker.getInstance().reportAppDownloadGoal();
		}
		
		initView(checkValue);

		
		mUpdateManager = new UpdateManager(this, mHandler);
		mUpdateManager.ForceCheckUpdates();
	}
	
	@Override
	public void onStart(){
		super.onStart();
		RuntimeLog.log("LoginActivity.onStart()");
	}
	
	private int checkLoginStatus(){
		SharedPreferences setting = mActivity.getSharedPreferences("loginXML", 0);
		ZoneAccount account = mHikeAccountManager.getAccount();
		String passportMyCookieValue = (account!=null && account.getPassportCookieValue()!=null)?account.getPassportCookieValue():null;
		
		// val passporttest.ghike.in=1935:1354086654:672e2b780dc086d4f5a88a65addc778f
		Log.i(TAG, "passportMyCookieValue value is "+passportMyCookieValue);
		if(passportMyCookieValue!=null&&passportMyCookieValue.length()>0){
			String[] passport = passportMyCookieValue.split(":");
			if(passport.length==3){
				return this.CHECK_PASSPORT_LOGIN_SUCESS; 
			}
		}
		boolean loginIsPincodeCheck = (boolean)setting.getBoolean("loginIsPincodeCheck", false);
		Log.i(TAG, "loginIsPincodeCheck value is "+loginIsPincodeCheck);
		if(!loginIsPincodeCheck){
			return this.CHECK_NOT_CHECK_PINCODE;
		}
		String loginNickNameTraceCheck = setting.getString("loginNickNameTraceCheck", null);
		//number:1236549835:op:signupnickname
		Log.i(TAG, "loginNickNameTraceCheck value is "+loginNickNameTraceCheck);
		String loginPhoneNum= setting.getString("phonenumber", "");
		if(loginPhoneNum==null||loginPhoneNum.length()<=0){
			return this.CHECK_NOT_CHECK_PINCODE; 
		}
		Log.i(TAG, "loginPhoneNum value is "+loginPhoneNum);
		if(loginNickNameTraceCheck!= null){
			String[] loginNickNameCheck = loginNickNameTraceCheck.split(":");
			if(loginNickNameCheck.length!=4){
				return this.CHECK_NOT_CHECK_PINCODE; 
			}
			String tracePhoneNum = loginNickNameCheck[1];
			if(loginPhoneNum.equals(tracePhoneNum)){
				return this.CHECK_TRACE_NICKNAME_LOGIN_SUCESS;
			}
		}
		 return this.CHECK_NOT_CHECK_PINCODE;
	}
	
	@Override
	protected void onResume(){
		//Log.i(TAG, "onResume begin");
		super.onResume();
		RuntimeLog.log("LoginActivity.onResume()");
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String cmd = extras.getString("cmd");
			Log.i(TAG, "onResume cmd=" + cmd);
			if (Constants.CMD_AUTO_LOGIN_WITH_CODE.equals(cmd)) {
				String code = extras.getString("code");
				if(code!=null && !code.equals("")){
/*					if(history.isCurrent(mSignInNumberViewWrapper)){
						mSignInNumberViewWrapper.autoSignIn(code);
					}else if(history.isCurrent(mSignUpNumberViewWrapper)){
						mSignUpNumberViewWrapper.autoSignUp(code);
					}*/
					Log.i(TAG, "onResume autologin with code finish");
				}
			}
			if (Constants.CMD_GET_PASSPORT.equals(cmd)) {
				Messenger receiver = extras.getParcelable("messenger");
				if(receiver!=null){
					mGetPassportReceivers.add(receiver);
					Log.i(TAG, "onResume send passport finish");	
				}
			}
		}
		
		Log.i(TAG, "onResume finish successful");
	}
	@Override
	protected void onDestroy(){
		//Log.i(TAG, "onDestroy begin");
		super.onDestroy();
		RuntimeLog.log("LoginActivity.onResume()");
		
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

		
		if(mGetPassportReceivers.size()>0){
			sendGetPassportError();
			Log.i(TAG, "onDestroy sendGetPassportError has been done");
		}
		
		//SMStimer.cancel();
		//SMStimer=null;
		
		if(mUpdateManager!=null){
			mUpdateManager.CloseDialog();
			mUpdateManager = null;
		}

	}
	
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {/*
			if(history.canGoBack()){
				if (history.currentViewWrapper == mTermsOfServiceViewWrapper){
					history.goBack();
					return true;
				}
				else{
					//history.goBack();
					finish();
					return true;
				}

			}else{
				if(mGetPassportReceivers.size()>0){
					sendGetPassportCANCELED();
				}
				finish();
				return true;
			}
		*/}
		//
		
		return super.dispatchKeyEvent(event);
	}
	//================================activity lifecycle callback end==================================
	
	//================================busness process begin==================================
	/**
	 * @author zhangwei
	 * This Handler class should be static or leaks might occur
	 */
	
	public static class MyHandler extends Handler{ 


		WeakReference<LoginActivity> wActivity;

        MyHandler(LoginActivity activity) {
        	wActivity = new WeakReference<LoginActivity>(activity);
        }

        @Override 
        public void handleMessage(Message msg) { 
        	LoginActivity theActivity = wActivity.get();
        	
        	if(theActivity==null){
        		RuntimeLog.log("handleMessage - theActivity null");
        		return;
        	}
        	
        	
            //super.handleMessage(msg); 
        	RuntimeLog.log("handleMessage - msg.what:" + msg.what);
        	switch(msg.what){
	        	case PULL_PIN:
	        		//theActivity.mNoAirtelLoginNumberViewWrapper.lockInput();
	        		theActivity.mNoAirtelLoginNumberFragment.lockInput();
	        		theActivity.stateType=State.PULL_IN;
	        		theActivity.captureSMS(true); 
	                break;
	                
	        	case PULL_MST:
	        		RuntimeLog.log("case PULL_MST - lockinput");
	        		//theActivity.mNoAirtelLoginNumberViewWrapper.lockInput();
	        		//theActivity.mNoAirtelLoginNumberViewWrapper.changeAnimation(R.anim.pin_almost);
	        		theActivity.mNoAirtelLoginNumberFragment.lockInput();
	        		theActivity.mNoAirtelLoginNumberFragment.changeAnimation(R.anim.pin_almost);
	                break;
	                
	        	case PULL_OUT:
	        		//theActivity.mNoAirtelLoginNumberViewWrapper.unLockInput("Can't received SMS");
	        		if(theActivity.userType==UserType.REGISTED){
	        			//theActivity.history.go(theActivity.mSignInPincodeViewWrapper);
	        			theActivity.goToPage(theActivity.LOGIN_VIEW_SIGNIN_PIN, false);
	        		}else{
	        			//theActivity.history.go(theActivity.mNoAirtelLoginPincodeViewWrapper);	
	        			theActivity.goToPage(theActivity.LOGIN_VIEW_SIGNUP_PIN, false);
	        		}
	        		//theActivity.mNoAirtelLoginNumberViewWrapper.unLockInput(null);
	        		theActivity.mNoAirtelLoginNumberFragment.unLockInput(null);	        		
	        		theActivity.captureSMS(false); 
	        		theActivity.stateType=State.PULL_OUT;
	                break;
	                

	        	case SMS_RCVD:
	        		if ((String)msg.obj == null){
	        			RuntimeLog.log("handleMessage - SMS_RCVD - obj is null");
	        			break;
	        		}
	        		
	        		if(theActivity.userType == UserType.INIT ){
	        			RuntimeLog.log("handleMessage - SMS_RCVD - UserType.NOT_INIT");
	        			break;
	        		}
	        		
	        		boolean autoComplt = false;
	        		
					SharedPreferences settings = theActivity.getSharedPreferences("SMS", Context.MODE_WORLD_READABLE);
	        		if(theActivity.stateType==State.PULL_IN){
		        		if(theActivity.userType==UserType.REGISTED){
		        			RuntimeLog.log("handleMessage - SMS_RCVD - REGISTED");
		        			//theActivity.mSignInPincodeViewWrapper.setPinCode((String)msg.obj);
		        			//theActivity.mSignInPincodeFragment.setPinCode((String)msg.obj);
		        			theActivity.setPinCode((String)msg.obj);
		        			//go to mSignInPincodeViewWrapper and autoLogin
		        			RuntimeLog.log("condition: theActivity.mNoAirtelLoginNumberFragment.islock:" + theActivity.mNoAirtelLoginNumberFragment.islock());
		        			RuntimeLog.log("condition: settings.getBoolean(ABORTSMS)" + settings.getBoolean("ABORTSMS", true));
			        		//if(theActivity.mNoAirtelLoginNumberViewWrapper.islock() 
		        			if(theActivity.mNoAirtelLoginNumberFragment.islock() 
			        				&& settings.getBoolean("ABORTSMS", true)){
			    				removeMessages(PULL_MST);
			    				removeMessages(PULL_OUT);
			        			//theActivity.mSignInPincodeViewWrapper.autoLogin((String)msg.obj);
			        			//theActivity.mNoAirtelLoginNumberViewWrapper.autoLogin((String)msg.obj);
			        			theActivity.mNoAirtelLoginNumberFragment.autoLogin((String)msg.obj);
			        			autoComplt = true;
			        			
			        		}
			        		
			        		
		        		}else{
		        			//theActivity.mNoAirtelLoginPincodeViewWrapper.setPinCode((String)msg.obj);
		        			//theActivity.mSignInPincodeFragment.setPinCode((String)msg.obj);
		        			theActivity.setPinCode((String)msg.obj);
		        			//if(theActivity.mNoAirtelLoginNumberViewWrapper.islock() 
		        			if(theActivity.mNoAirtelLoginNumberFragment.islock() 
		        					&& settings.getBoolean("ABORTSMS", true)){
			    				removeMessages(PULL_MST);
			    				removeMessages(PULL_OUT);
		        				//theActivity.mNoAirtelLoginNumberViewWrapper.autoJumpToNickName((String)msg.obj);
		        				theActivity.mNoAirtelLoginNumberFragment.autoJumpToNickName((String)msg.obj);
		        				autoComplt = true;
		        			}						
		        		}
	        		}
		
	        		
	        		theActivity.userType=UserType.INIT;
	        		RuntimeLog.log("case SMS_RECV reset userType = UserType.INIT");
	        		theActivity.captureSMS(false);

	        		//set NoAirtelLoginNumberViewWrapper if the user come back(PRD del)
	        		if(autoComplt==false){
	        			//restore the UI State of the mNoAirtelLoginNumberViewWrapper
		        		theActivity.mNoAirtelLoginNumberFragment.msgTextView.setText(R.string.cannotpull);
		        		theActivity.mNoAirtelLoginNumberFragment.msgTextView.setPadding(0, 0, 0, 0);
		        		theActivity.mNoAirtelLoginNumberFragment.msgTextView.setTextColor(theActivity.getResources().getColor(R.color.darkgray));
		        		theActivity.mNoAirtelLoginNumberFragment.msgTextView1.setVisibility(View.VISIBLE);
		        		theActivity.mNoAirtelLoginNumberFragment.unLockInput(null);
	        			
	        		}

					//mSignInPincodeViewWrapper.setNumber(number);
					//mSignInPincodeViewWrapper.setPinCode(pinCode);
					//history.go(mSignInPincodeViewWrapper); 
	        		break;
	        		
	        	case CLOSE_LOGIN:
	        		theActivity.finish(); 
	                break;
                
                default:
                	RuntimeLog.log("case default - msg.what:" + msg.what);
                	break;
        	}

        } 
    };
    
	public void setNumber(String number){
		this.phonenumber = number;
	}
	
	public void setPinCode(String pinCode){
		this.pincode = pinCode;
	}
	
	public String getNumber(){
		return this.phonenumber;
	}
	
	public String getPinCode(){
		return this.pincode;
	}
	
	
	public void checkUser(){
		ZoneAccount account = mHikeAccountManager.getAccount();
		final String passportCookieValue = (account!=null && account.getPassportCookieValue()!=null)?account.getPassportCookieValue():null;
		if(passportCookieValue!=null && !passportCookieValue.equals("")){
			new Timer().schedule(new TimerTask() {
	            @Override  
	            public void run() {
	    			launchHikeMain(passportCookieValue);
	    			finish();
	            }
	        }, 10);
		}else{
			SignClient.performCheckUser(null, mHandler, new Callback() {
				public void call(ResponseWrapper resp) {
					if(resp==null){
						mWelcomeFragment.showBtns(null);
					}
					//handleNetworkError(resp);
					mWelcomeFragment.handleNetWorkError(resp);
					if(resp!=null && resp.isValid() && resp.isNoError()){
						int status = resp.getStatus();
						RuntimeLog.log("performCheckUser status: " + status);
						switch(status){
						case 207://auto login with airtel,  airtel user and registered
							SignClient.performSignInAirtel(mHandler, new Callback() {
								public void call(ResponseWrapper resp) {
									//handleNetworkError(resp);
									mWelcomeFragment.handleNetWorkError(resp);
									if(resp!=null && resp.isValid() && resp.isNoError()){
										finishSignIn(resp, true);
									}
								}
							});							
						case 208://make welcome signup btn point to airtel signup view
							//airtel user but not registered
							String number = resp.getStringFromResp("number");
							Log.w(TAG,"resp's number = " + number);
							mWelcomeFragment.showBtns(number);
							//mLoginAirtelViewWrapper.passAirtelPhoneNumber(number);
							//mLoginAirtelFragment.passAirtelPhoneNumber(number);
							setNumber(number);
							goToPage(LOGIN_VIEW_AIRTEL_LOGINNUM, false);
							//history.go(mLoginAirtelViewWrapper);							
							mWelcomeFragment.unLockInput();
							break;
						default://make welcome signup btn point to non-airtel signup page
							//not airtel user
							mWelcomeFragment.showBtns(null);
							//history.go(mNoAirtelLoginNumberViewWrapper);
							mWelcomeFragment.unLockInput();
							goToPage(LOGIN_VIEW_NOAIRTEL_LOGINNUM, false);
							break;
						}
					}
				}
			});
		}
	}
	
	public void finishSignIn(ResponseWrapper resp, boolean isAirtel){
		try{
			String number = resp.getStringFromResp("number");
			String type = (isAirtel ? ZoneAccount.TYPE_AIRTEL : ZoneAccount.TYPE_NONAIRTEL);
			JSONObject json = resp.getRespJson();
			String passport = json.getString("passport");//resp.getStringFromResp("passport");
			Cookie passportCookie = resp.getCookieCrossDomain("passport");
			String passportCookieValue = passportCookie.getName() + "=\"" + passportCookie.getValue() + "\"; Domain=" + passportCookie.getDomain();
			JSONObject user = json.getJSONObject("user");
			String userId = user.getString("userId");
			String userName = user.getString("nickname");
			String headUrl = user.getString("headUrl");
			String coverUrl = user.has("coverUrl")?user.getString("coverUrl"):null;
			//mHikeAccountManager.loginAccount(number, type, passport, passportCookieValue, userId, userName, headUrl, coverUrl);
			mHikeAccountManager.login(userId, number, userName, type, headUrl, coverUrl, passport);
			if(mGetPassportReceivers.size()>0){
				sendGetPassportSuccess(passport);
			}else{
				launchHikeMain(passportCookieValue);
			}
			finish();
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	
	public void launchHikeMain(String passportCookieValue){
		Intent intent = new Intent(this,HikeMainActivity.class);
		if(passportCookieValue!=null){
			intent.putExtra("passportCookieValue", passportCookieValue);
			intent.putExtra("showWelcome", false);
		}
		RuntimeLog.log("LoginActivity.launchHikeMain.startActivity");
		startActivity(intent);
	}
	private void sendGetPassportSuccess(String passport){
		Message successMsg = null;
		
    	try {
			successMsg = Message.obtain(null, Constants.MESSAGE_WHAT_HIKE_CLIENT, 500, 0);
			Bundle data = new Bundle();
			data.putInt("status", SDKStatus.SUCESS.getValue());
			data.putString("passport", passport);
			successMsg.setData(data);
		} catch (Exception e) {
			successMsg = null;
			sendGetPassportError();
			Log.i(TAG, "sendGetPassportSuccess sendGetPassportError has been done");
			Log.e(TAG, "",e);
		}
    	
    	if(successMsg!=null){
    		Log.i(TAG, "sendGetPassportSuccess receivers.size()=" + mGetPassportReceivers.size());
			for(Messenger receiver:mGetPassportReceivers){
				try {
					receiver.send(successMsg);
				} catch (Exception e) {
					Log.e(TAG, "",e);
				}
			}
			mGetPassportReceivers = new ArrayList<Messenger>();
			Log.i(TAG, "sendGetPassportSuccess passport has been sent");
    	}
	}
	private void sendGetPassportCANCELED(){
		Message errMsg = Message.obtain(null, Constants.MESSAGE_WHAT_HIKE_CLIENT, 500, 0);
		Bundle data = new Bundle();
		data.putInt("status", SDKStatus.CANCELED.getValue());
		errMsg.setData(data);
		
		for(Messenger receiver:mGetPassportReceivers){
			try {
				receiver.send(errMsg);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		mGetPassportReceivers = new ArrayList<Messenger>();
	}
	private void sendGetPassportError(){
		Message errMsg = Message.obtain(null, Constants.MESSAGE_WHAT_HIKE_CLIENT, 500, 0);
		Bundle data = new Bundle();
		data.putInt("status", SDKStatus.ERROR.getValue());
		errMsg.setData(data);
		
		for(Messenger receiver:mGetPassportReceivers){
			try {
				receiver.send(errMsg);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		mGetPassportReceivers = new ArrayList<Messenger>();
	}
	public void handleNetworkError(ResponseWrapper resp){
		if(resp==null){
			Toast.makeText(this,"Network Problem!", Toast.LENGTH_LONG).show();
		}
	}
	
	
	private void captureSMS(boolean flag){
		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean("ABORTSMS", flag);
		edit.commit();
	}
	
/*	private void captureSMS(){
		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean("ABORTSMS", true);
		edit.commit();
		
		SMStimer.purge();

		SMStimer.schedule(new TimerTask(){  
			  
            @Override  
            public void run() {
        		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
        		Editor edit = preferences.edit();
        		edit.putBoolean("ABORTSMS", false);
        		edit.commit();
            }  
        }, PULL_PIN_DELAY);   
			
		
	}*/
	
	//================================busness process begin==================================
	
	//================================busness view begin==================================
	/*	CHECK_NOT_CHECK_PINCODE = 0; // to welcome 
		CHECK_PASSPORT_LOGIN_SUCESS = 1 ; //  
		CHECK_TRACE_NICKNAME_LOGIN_SUCESS = 2 ; // to nickname 
	*/	
	private void initView(int type){
		RuntimeLog.log("LoginActivity - initView - type:" + type);
		
		switch(type){
	    	case CHECK_NOT_CHECK_PINCODE:
	    		goToPage(LOGIN_VIEW_WELCOME, false);
	            break;
			
			case CHECK_TRACE_NICKNAME_LOGIN_SUCESS:
				goToPage(LOGIN_VIEW_SIGNUP_NICK, false);
		        break;
			
			case CHECK_PASSPORT_LOGIN_SUCESS:
				//can not go here 
				//showFragment(LOGIN_VIEW_WELCOME, false);
				goToPage(LOGIN_VIEW_WELCOME, false);
			    break;
			    
			 default: 
				 goToPage(LOGIN_VIEW_WELCOME, false);
			     break;
		}
			
	}
	
	
	/*	LOGIN_VIEW_WELCOME = 0; // to welcome 
		LOGIN_VIEW_TERMOFSERVICE = 1 ; //  
		LOGIN_VIEW_NOAIRTEL_LOGINNUM = 2 ; //  
		LOGIN_VIEW_SIGNIN_PIN = 3 ; //  
		LOGIN_VIEW_SIGNUP_NICK = 4; //  
		LOGIN_VIEW_AIRTEL_LOGINNUM = 5; //  
	*/	

	public void goToPage(int type, boolean record){
		Fragment dst;
		String mViewName;
		dst = mWelcomeFragment;
		if(type == LOGIN_VIEW_WELCOME){
			
			dst = mWelcomeFragment;
			mViewName = "welcome";
			
		}else if(type == LOGIN_VIEW_TERMOFSERVICE){
			
			dst = mTermOfServiceFragment;
			mViewName = "terms_of_service";
			
		}else if(type == LOGIN_VIEW_AIRTEL_LOGINNUM){
			
			dst = mLoginAirtelFragment;
			mViewName = "login/airtel_enter_nickname";
			
		}else if(type == LOGIN_VIEW_NOAIRTEL_LOGINNUM){
			
			dst = mNoAirtelLoginNumberFragment;
			mViewName = "login/enter_number";
			
		}else if(type == LOGIN_VIEW_SIGNIN_PIN){
			
			dst = mSignInPincodeFragment;
			mViewName = "login/enter_pincode";
			
		}else if(type == LOGIN_VIEW_SIGNUP_PIN){
			
			dst = mNoAirtelLoginPincodeFragment;
			mViewName = "login/enter_pincode";
			
		}else if(type == LOGIN_VIEW_SIGNUP_NICK){
			
			dst = mSignupNicknameFragment ;
			mViewName = "login/enter_nickname";
			
		}else {
			
			dst = mWelcomeFragment;
			mViewName = "welcome";
			RuntimeLog.log("type is not supported yet, just use welcome! type:" + type);
		}

		
        FragmentTransaction ft = fm.beginTransaction(); 

		//check login_container if null
        if (fm.findFragmentById(R.id.login_container) != null) {
        	RuntimeLog.log("replace login_container dst:" + dst.toString());
        	ft.replace(R.id.login_container, dst);

        }else{
        	RuntimeLog.log("add login_container dst:" + dst.toString());
			ft.add(R.id.login_container, dst);
			//ft.replace(R.id.login_container, dst);
        }
        
        if(record){
        	ft.addToBackStack(null);
        }

        
        ft.commit();
        //ft.commitAllowingStateLoss();
        
		getTracker().trackView(mViewName);
	}
	
/*	private class ViewWrapperHistory {
		private static final String TAG = "history";
		private List<ViewWrapper> stack = new LinkedList<ViewWrapper>();
		private ViewWrapper currentViewWrapper;
		private final OnClickListener goBackOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		};
		public ViewWrapperHistory() {
	    }
	    private ViewWrapper push(ViewWrapper item) {
	    	Log.i(TAG, "push() item=" + item.getClass().getName());
	    	Log.i(TAG, "push() statck.size()=" + stack.size());
	    	stack.add(item);
	    	Log.i(TAG, "push() statck.size()=" + stack.size());
	    	return item;
	    }
	    private ViewWrapper pop() {
	    	Log.i(TAG, "pop() statck.size()=" + stack.size());
	    	int size = stack.size();
	    	if(size>0){
	    		size--;
	    		ViewWrapper	obj = stack.get(size);
	    		stack.remove(size);
	    		Log.i(TAG, "pop() statck.size()=" + stack.size());
	    		Log.i(TAG, "pop() item=" + obj.getClass().getName());
	    		return obj;
	    	}else{
	    		return null;
	    	}
	    }
	    public boolean isCurrent(ViewWrapper viewWrapper){
	    	Log.i(TAG, "isCurrent() viewWrapper=" + viewWrapper.getClass().getName());
	    	return viewWrapper == currentViewWrapper;
	    }
	    public void go(ViewWrapper goal){
	    	Log.i(TAG, "go() goal=" + goal.getClass().getName());
	    	if(currentViewWrapper!=null){
	    		Log.i(TAG, "go() currentViewWrapper=" + currentViewWrapper.getClass().getName());
	    	}
	    	
	    	if(!isCurrent(goal)){
	    		if(currentViewWrapper!=null){
	    			push(currentViewWrapper);
	    			currentViewWrapper.hide();
	    		}
	    		currentViewWrapper = goal;
	    		
	    		Log.i(TAG, "go() goal=" + goal.getClass().getName());
	    		goal.show(true);
	    		userIntentFlag++;
	    	}
	    }
	    
	    public boolean canGoBack(){
	    	Log.i(TAG, "canGoBack() statck.size()=" + stack.size());
	    	return stack.size() > 0;
	    }
	    public void goBack(){
	    	if(canGoBack()){
	    		Log.i(TAG, "goBack() currentViewWrapper=" + currentViewWrapper.getClass().getName());
	    		currentViewWrapper.hide();
	    		currentViewWrapper = pop();
	    		currentViewWrapper.show(false);
	    		userIntentFlag++;
	    		//Log.i(TAG, "goBack() currentViewWrapper=" + currentViewWrapper.getClass().getName());
	    	}
	    }
	    public OnClickListener getGoBackOnClickListener() {
			return goBackOnClickListener;
		}
	}*/
/*	public abstract class ViewWrapper {
		private final View thisView;
		protected final ViewWrapper thisViewWrapper;
		public SharedPreferences settings ;
		private String mViewName;
		
		public ViewWrapper(int viewId, String viewName){
			thisView = findViewById(viewId);
			thisViewWrapper = this;
			mViewName = viewName;
			settings = LoginActivity.this.getSharedPreferences("loginXML", 0);
		}
		
		public void show(boolean isNew){
			if(onShow(isNew)){
				thisView.setVisibility(View.VISIBLE);
				getTracker().trackView(mViewName);
			}
		}
		
		public void hide(){
			if(onHide()){
				thisView.setVisibility(View.INVISIBLE);
			}
		}
		
		public boolean onShow(boolean isNew){
			return true;
		}
		
		
		public boolean onHide(){
			return true;
		}
		
	}*/
/*	public class TermsOfServiceViewWrapper extends ViewWrapper{
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
        private WebView mHikeWebView = (WebView)findViewById(R.id.service_web_view);
        private TextView mHeaderTextView = (TextView)findViewById(R.id.TermofService_textview);

		
		public TermsOfServiceViewWrapper(){
			super(R.id.mTermofService, "terms_of_service");
			mHeaderTextView.setTypeface(typeFace);
			mHikeWebView.loadUrl(Constants.ROOT_STATIC_DOMAIN+"/ui/tpl/termService.html");
			mHeaderTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					history.go(mWelcomeViewWrapper);
					
				}
			});

		}
	}*/

	
/*	public class WelcomeViewWrapper extends ViewWrapper{
		private String airtelNumber;
		private AnimationDrawable loadingAnimation;
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		Typeface typeFace1 = Typeface.createFromAsset(getAssets(), "font/HelveticaNeue-Roman.otf");
		private Button mTermTextView = (Button)findViewById(R.id.mWelcomeTermOfUseImageView);
		private Button welcomeAcceptBtn =(Button)findViewById(R.id.mWelcomeAcceptBtn);
		private RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.mWelcomeView);
		private ImageView welcomeView = (ImageView)findViewById(R.id.login_welcome_image);

		public WelcomeViewWrapper(){
			super(R.id.mWelcomeView, "welcome");
			welcomeAcceptBtn.setTypeface(typeFace1);
			mTermTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.w(TAG, "mTermTextView is clicked");					
					history.go(mTermsOfServiceViewWrapper);
					
				}
			});
			
			mTermTextView.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					Log.w(TAG, "mTermTextView is clicked");					
					history.go(mTermsOfServiceViewWrapper);
					return false;
				}
			});
			

			
			welcomeAcceptBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockInput();
					checkUser();

				}
			});
			


		}
		
		
		public void handleNetWorkError(ResponseWrapper resp){
			if (resp == null){
				unLockInput();
				welcomeAcceptBtn.setText(R.string.tryagain);
				welcomeAcceptBtn.setClickable(true);
				mTermTextView.setVisibility(View.GONE);
				//mRelativeLayout.setBackgroundResource(R.drawable.login_welcome_wifi);
				welcomeView.setImageResource(R.drawable.login_welcome_wifi);
			}
		}
		@Override
		public boolean onShow(boolean isNew){
			mUIHelper.hideSoftInput();
			return true;
		}
		
		private void lockInput(){
			welcomeAcceptBtn.setText("");
			welcomeAcceptBtn.setClickable(false);			
			welcomeAcceptBtn.setBackgroundResource(R.anim.welcome_loading_process);
			loadingAnimation = (AnimationDrawable)welcomeAcceptBtn.getBackground();
			loadingAnimation.start();
			
		}
		
		public void unLockInput(){
			welcomeAcceptBtn.setText(R.string.accept);
			welcomeAcceptBtn.setClickable(true);
			welcomeAcceptBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_welcome_bg_btn));	
			if (loadingAnimation != null && loadingAnimation.isRunning()){
				loadingAnimation.stop();
			}
			
		}
		

		public void showBtns(String airtelNumber){
			this.airtelNumber = airtelNumber;
			//final Button signUpBtn =(Button)findViewById(R.id.mWelcomeSignUpBtn);
			//final Button signInBtn =(Button)findViewById(R.id.mWelcomeSignInBtn);
			//signUpBtn.setVisibility(View.VISIBLE);
			//signInBtn.setVisibility(View.VISIBLE);
		}
	}*/
/*	public class LogoutAlreadyViewWrapper extends ViewWrapper{
		public LogoutAlreadyViewWrapper(){
			super(R.id.mLogoutAlreadyView);
			
			final Button formerBtn =(Button)findViewById(R.id.mLogoutAlreadyFormerBtn);
			final Button anotherBtn =(Button)findViewById(R.id.mLogoutAlreadyAnotherBtn);
			formerBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					formerBtn.setClickable(false);
					anotherBtn.setClickable(false);
					SignClient.performSignInAirtel(mHandler, new Callback() {
						@Override
						public void call(ResponseWrapper resp) {
							handleNetworkError(resp);
							formerBtn.setClickable(true);
							anotherBtn.setClickable(true);
							if(resp!=null && resp.isValid() && resp.isNoError()){
								finishSignIn(resp, true);
							}
						}
					});
				}
			});
			anotherBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					history.go(mAirtelSignInNumberViewWrapper);
				}
			});
		}
		@Override
		public boolean onShow(boolean isNew){
			mUIHelper.hideSoftInput();
			return true;
		}
	}*/
/*	public class NoAirtelLoginNumberViewWrapper extends ViewWrapper{
		private final int GET_CODE_DELAY = 1000 * 15;
		private boolean inputLocked;
		private AnimationDrawable verifyingPhoneNumberAnimation;
		public final EditText numberEditText =(EditText)findViewById(R.id.mSignUpNumberNumberEditText);
		private final Button nextBtn =(Button)findViewById(R.id.mSignUpNumberNextBtn);
		private final ImageButton helpBtn =(ImageButton)findViewById(R.id.mSignUpNumberHelpBtn);
		private final TextView msgTextView =(TextView)findViewById(R.id.mSignUpNumberTextView);
		private final TextView msgTextView1 = (TextView)findViewById(R.id.mSignUpNumberTextView1);
		Typeface typeface = Typeface.createFromAsset(getAssets(), "font/Edmondsans-Regular.otf");
		private Context mContext ;
		
		
		private String number;
		public NoAirtelLoginNumberViewWrapper(Context context){
			super(R.id.mSignUpNumberView, "login/enter_number");
			mContext = context;
			msgTextView.setTypeface(typeface);
			msgTextView1.setTypeface(typeface);
			
			helpBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					history.go(mHelpViewWrapper);
				}
			});
			nextBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					////////////////////
					//step1  validateNumber:
					lockInput();
					number = numberEditText.getText().toString();
					String errMsg = InputValidator.validateNumber(number);
					if(errMsg!=null){
						unLockInput(errMsg);
					}else{
						//the num is validate, signup to server
						ResponseWrapper resp = SignClient.performingGetSignUpCode(number); // get pin from server
						if(resp!=null && resp.isValid() && resp.isNoError()){
							if(resp.getStatus() == 203){
								
								//maybe it's wrong
								errMsg = "You don't have a gHike account, yet!";
								unLockInput(errMsg);
								
							}else if (resp.getStatus() == 201){
								//registered and nickname ok before
								userType = UserType.REGISTED;
								
								//here check the resp2 simple
								ResponseWrapper resp2 = SignClient.performingGetSignInCode(number);
								if(resp2==null){
									unLockInput("Network Problem");

								}else{
									//send ok
									final String pinCode = resp2.getStringFromResp("pinCode");
									//tel server to give me pincode, at this, server return ok, then:
									//set sms capture, and set timer to rm it 60 sec later
									
									mHandler.sendEmptyMessage(PULL_PIN);
									
									
									//set the pull pin time out event
									mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
									
									mSignInPincodeViewWrapper.setNumber(number);
									
									if(pinCode!=null && !pinCode.trim().equals("")){
										//for emulate user: the server return pin through network																	
										mSignInPincodeViewWrapper.setPinCode(pinCode);
										
										Message msg = new Message();
										msg.what = SMS_RCVD;
										msg.obj = pinCode;
										mHandler.sendMessage(msg);
										mHandler.removeMessages(PULL_OUT);
									}else{

										//the real user, user local receiver to listen the sms
										
									}
								}


								
							}else{ //resp.getStatus() != 201
								//not registered , need  fill nickname
								userType = UserType.NOT_REGISTED;
								
								//send ok
								String pinCode = resp.getStringFromResp("pinCode");
								//tel server to give me pincode, at this, server return ok, then:
								//set sms capture, and set timer to rm it 60 sec later
								
								mHandler.sendEmptyMessage(PULL_PIN);
								
								//set the pull pin time out event
								mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
								
								//pinCode = null; //emulate real user
								
								mNoAirtelLoginPincodeViewWrapper.setNumber(number);
								
								if(pinCode!=null && !pinCode.trim().equals("")){
									//for emulate user: the server return pin through network																	
									mSignInPincodeViewWrapper.setPinCode(pinCode);
									//mSignInPincodeViewWrapper.setNumber(number);
									
									Message msg = new Message();
									msg.what = SMS_RCVD;
									msg.obj = pinCode;
									mHandler.sendMessage(msg);
									mHandler.removeMessages(PULL_OUT);
								}else{
									//set the pull pin time out event
									//mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
									//the real user, user local receiver to listen the sms
									
								}
							}
								
						}
						
						//unLockInput(null);
							
					}

					
					/////////////////////
					lockInput();
					number = numberEditText.getText().toString();
					String errMsg = InputValidator.validateNumber(number);
					if(errMsg==null){
						final int workingFlag = userIntentFlag;
						SignClient.performGetSignUpCode(number, mHandler, new Callback() {
							@Override
							public void call(ResponseWrapper resp) {
								if(workingFlag==userIntentFlag){
									handleNetworkError(resp);
									if(resp!=null && resp.isValid() && resp.isNoError()){
										String errMsg =  ServerSideErrorMsg.getMsg(resp.getStatus());
										//the number is occupyed
										Log.w(TAG,"resp.getStatus() = " +resp.getStatus());
										if (resp.getStatus() == 201){
											//registered and nickname ok before
											userType = UserType.REGISTED;
											RuntimeLog.log("201 registered and nickname ok before, set userType = UserType.REGISTED");
											

											
											
											//pinCode = null; //emulate real user
											
											
											//registered and nickname ok before
											SignClient.performGetSignInCode(number, mHandler, new Callback() {
												@Override
												public void call(ResponseWrapper resp) {
													if(workingFlag==userIntentFlag){
														handleNetworkError(resp);
														if(resp!=null && resp.isValid() && resp.isNoError()){
															String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
															if(resp.getStatus()==203){
																
																errMsg = "You don't have a gHike account, yet!";
															}
															if(errMsg!=null){
																unLockInput(errMsg);

																
															}else{
																mHandler.sendEmptyMessage(PULL_PIN);
																
																//set the pull pin time out event
																mHandler.sendEmptyMessageDelayed(PULL_MST, PULL_PIN_DELAY*2/3);
																mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
																
																mNoAirtelLoginPincodeViewWrapper.setNumber(number);
																mSignInPincodeViewWrapper.setNumber(number);
																
																//send ok
																String pinCode = resp.getStringFromResp("pinCode");
																//tel server to give me pincode, at this, server return ok, then:
																//set sms capture, and set timer to rm it 60 sec later
																//captureSMS(); 
																//pinCode = null;
																if(pinCode!=null && !pinCode.trim().equals("")){
																	//for emulate user: the server return pin through network																	
																	mSignInPincodeViewWrapper.setPinCode(pinCode);
																	Message msg = new Message();
																	msg.what = SMS_RCVD;
																	msg.obj = pinCode;
																	mHandler.sendMessage(msg);
																}else{
																	//set the pull pin time out event
																	//mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
																	//the real user, user local receiver to listen the sms
																	
																}
																

																
																
																mHandler.postDelayed(new Runnable(){
																	public void run() {
																		if(workingFlag==userIntentFlag){
					
																			mSignInPincodeViewWrapper.setNumber(number);
																			//get pincode from server response and auto fill it for tester
																			if(pinCode!=null && !pinCode.trim().equals(""))
																			{
																				mSignInPincodeViewWrapper.setPinCode(pinCode);
																			}else
																			{
																				//for real user
																				pincode=smsReciver.getPinCode(LoginActivity.this, getIntent());
																						Log.i(TAG, "pincode="+pincode);
																				mSignInPincodeViewWrapper.setPinCode(pincode);
																			}
																			history.go(mSignInPincodeViewWrapper);
																			if(pincode != null){
//																				mSignInPincodeViewWrapper.AutoJumpToNickNameView(pincode);
																				mSignInPincodeViewWrapper.autoLogin(pincode);
																			}
																			
																			
																			msgTextView.setText(R.string.cannotpull);
																			msgTextView.setPadding(0, 0, 0, 0);
																			msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
																			msgTextView1.setVisibility(View.VISIBLE);
																			//mSignInPincodeViewWrapper.setNumber(number);
																			//mSignInPincodeViewWrapper.setPinCode(pinCode);
																			//history.go(mSignInPincodeViewWrapper);

																		}
																		unLockInput(null);
																	}
																}, GET_CODE_DELAY);
															}
														}else{
															unLockInput(null);
														}
													}else{
														unLockInput(null);
													}
												}
											});
											
										}
										else{
											//not registered , need  fill nickname
											if(errMsg!=null){
												unLockInput(errMsg);
											}else{
												//final String pinCode = resp.getStringFromResp("pinCode");
												
												userType = UserType.NOT_REGISTED;
												RuntimeLog.log("not(201)registered , need  fill nickname, set userType = UserType.NOT_REGISTED ");
												
												//send ok
												String pinCode = resp.getStringFromResp("pinCode");
												//pinCode = null;
												
												//tel server to give me pincode, at this, server return ok, then:
												//set sms capture, and set timer to rm it 60 sec later
												
												mHandler.sendEmptyMessage(PULL_PIN);
												
												//set the pull pin time out event
												mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY); 
												mHandler.sendEmptyMessageDelayed(PULL_MST, PULL_PIN_DELAY*2/3);
												
												//pinCode = null; //emulate real user
												mNoAirtelLoginPincodeViewWrapper.setNumber(number);
												mSignInPincodeViewWrapper.setNumber(number);
												
												if(pinCode!=null && !pinCode.trim().equals("")){
													//for emulate user: the server return pin through network																	
													mSignInPincodeViewWrapper.setPinCode(pinCode);
													//mSignInPincodeViewWrapper.setNumber(number);
													
													Message msg = new Message();
													msg.what = SMS_RCVD;
													msg.obj = pinCode;
													mHandler.sendMessage(msg);
													//mHandler.removeMessages(PULL_MST);
													//mHandler.removeMessages(PULL_OUT);
												}else{
													//set the pull pin time out event
													//mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
													//the real user, user local receiver to listen the sms
													
												}
												
												mHandler.postDelayed(new Runnable(){
													public void run() {
														if(workingFlag==userIntentFlag){
															mNoAirtelLoginPincodeViewWrapper.setNumber(number);
															//get pincode from server response and auto fill it for tester
															if(pinCode!=null && !pinCode.trim().equals("")){
																mNoAirtelLoginPincodeViewWrapper.setPinCode(pinCode);
															}else{
																//for real user
																pincode=smsReciver.getPinCode(LoginActivity.this, getIntent());
																mNoAirtelLoginPincodeViewWrapper.setPinCode(pincode);
															}
															history.go(mNoAirtelLoginPincodeViewWrapper);
															if (pincode != null){
																mNoAirtelLoginPincodeViewWrapper.autoJumpToNickName();
															}

															
															msgTextView.setText(R.string.cannotpull);
															msgTextView.setPadding(0, 0, 0, 0);
															msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
															msgTextView1.setVisibility(View.VISIBLE);
														}
														unLockInput(null);
													}
												}, GET_CODE_DELAY);
											}
											
										}

									}else{
										unLockInput(null);
									}
								}else{
									unLockInput(null);
								}
							}
						});
					}else{
						unLockInput(errMsg);
					}
				}
			});
			

			

			numberEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					 if (s.length() > 0) {
				            int pos = s.length() - 1;
				            char c = s.charAt(pos);
				            if ((c == '+')&& (pos != 0)) {
				                s.delete(pos,pos+1);
				            }
				        }
					toggleBtn(nextBtn,numberEditText);
				}
			});
			termCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					toggleBtn(nextBtn, termCheckBox, numberEditText);
				}
			});
			numberEditText.setKeyListener(InputValidator.KEY_LISTENER_PHONENUMBER);
		}
		
		private boolean islock(){
			return inputLocked;
		}
		
		
		private void changeAnimation(int ResID){
			if(verifyingPhoneNumberAnimation.isRunning()){			
				verifyingPhoneNumberAnimation.stop();
				nextBtn.setBackgroundResource(ResID);
				verifyingPhoneNumberAnimation = (AnimationDrawable)nextBtn.getBackground();
				verifyingPhoneNumberAnimation.start();
				
			}

		}
		
		private void lockInput(){
			if(!inputLocked){
				numberEditText.setKeyListener(null);
				//termCheckBox.setClickable(false);
				mUIHelper.disableBtn(nextBtn);
				nextBtn.setText("");	
				nextBtn.setBackgroundResource(R.anim.verifying_phonenumber);
				verifyingPhoneNumberAnimation = (AnimationDrawable)nextBtn.getBackground();
				verifyingPhoneNumberAnimation.start();
				helpBtn.setClickable(false);
				inputLocked = true;
			}
		}
		

		
		private void unLockInput(String errMsg){
			if(inputLocked){
				if (verifyingPhoneNumberAnimation.isRunning()){
					verifyingPhoneNumberAnimation.stop();
					mUIHelper.disableBtn(nextBtn);
				}
				
				if(errMsg!=null){
					msgTextView.setText(errMsg);
					msgTextView.setTextColor(getResources().getColor(R.color.darkred));
					String msg = "Your phone number should be 10-14 digits long, only numbers and \"+\".";
					if (errMsg.equals(msg)){
						msgTextView.setPadding(40, 0, 0, 0);
					}
					else{
						msgTextView.setPadding(20, 0, 0, 0);
					}
					
					msgTextView1.setVisibility(View.GONE);
				}else{
					mUIHelper.enableBtn(nextBtn);
				}
				numberEditText.setKeyListener(InputValidator.KEY_LISTENER_PHONENUMBER);
				//termCheckBox.setClickable(true);
				nextBtn.setText("Next");
				helpBtn.setClickable(true);
				inputLocked = false;
			}
		}
		@Override
		public boolean onShow(boolean isNew){
			if(isNew){
				unLockInput(null);
				numberEditText.setText(null);
				//termCheckBox.setChecked(true);
				msgTextView.setText(R.string.cannotpull);
				msgTextView1.setText(R.string.getstart);
				mUIHelper.disableBtn(nextBtn);
			}
			mUIHelper.showSoftInput();
			return true;
		}
		@Override
		public boolean onHide(){
			return true;
		}
		private void toggleBtn(Button nextBtn, EditText numberEditText){
			boolean charCountZero = (numberEditText.getText().toString().length()==0);
			if(!charCountZero){
				mUIHelper.enableBtn(nextBtn);
			}else{
				mUIHelper.disableBtn(nextBtn);
			}
		}
		public void autoSignUp(String pinCode){
			if(history.isCurrent(this)){
<<<<<<< OURS
				mSignUpPincodeViewWrapper.setNumber(number);
				mSignUpPincodeViewWrapper.setPinCode(pinCode);
				history.go(mSignUpPincodeViewWrapper);
=======
				mNoAirtelLoginPincodeViewWrapper.setNumber(number);
				history.go(mNoAirtelLoginPincodeViewWrapper);
			}
		}
		
		//for registered user
		private void autoLogin(final String pinCode){
			lockInput();
//			String pinCode = pinCodeEditText.getText().toString();
			final int workingFlag = userIntentFlag;
			SignClient.performSignIn(number, pinCode, mHandler, new Callback() {
				@Override
				public void call(ResponseWrapper resp) {
					if(workingFlag==userIntentFlag){
						handleNetworkError(resp);
						if(resp!=null && resp.isValid() && resp.isNoError()){
							String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
							if(errMsg!=null){
								RuntimeLog.log("autoLogin - performSignIn - errMsg:" + errMsg);
								unLockInput(null);
								history.go(mSignInPincodeViewWrapper);
								mSignInPincodeViewWrapper.unLockInput(errMsg);
								
								
							}else{
								finishSignIn(resp, false);
								//  clean  loginstraceTickeCheck from  sharepre
								SharedPreferences.Editor localEditor = settings.edit();
								localEditor.remove("loginNickNameTraceCheck");
								localEditor.commit();
							}
						}else{
							unLockInput(null);
						}
					}else{
						unLockInput(null);
					}
				}
			});
		}
		
		private void autoJumpToNickName(final String Pincode){
			lockInput();
			//String Pincode = pinCodeEditText.getText().toString();
			final int workingFlag = userIntentFlag;
			SignClient.performSignUp(number, Pincode, mHandler, new Callback() {
				@Override
				public void call(ResponseWrapper resp) {
					if(workingFlag==userIntentFlag){
						handleNetworkError(resp);
						if(resp!=null && resp.isValid() && resp.isNoError()){
							String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
							if(errMsg!=null){
								//unLockInput(errMsg);
								unLockInput(null);
								history.go(mSignInPincodeViewWrapper);
								mSignInPincodeViewWrapper.unLockInput(errMsg);
							}else{
								String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
								
								mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
								LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
								String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
								
								//let loginNickNameTraceCheck into sharePreference 				
								SharedPreferences.Editor localEditor = settings.edit();
						        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
						        localEditor.putString("loginTrace", loginTrace);
						        localEditor.putString("phonenumber", number);
						        localEditor.putBoolean("loginIsPincodeCheck", true);
						        localEditor.commit();
						        
								history.go(mNoAirtelLoginNicknameViewWrapper);
								mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
								msgTextView.setText(R.string.ifgetpin);
								msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
								msgTextView1.setVisibility(View.VISIBLE);
								unLockInput(null);
							}
						}else{
							unLockInput(null);
						}
					}else{
						unLockInput(null);
					}
				}
			});
		}
	}
	*/
	public class LoginTrace{
		public static final String OP_SIGNUP_NICKNAME = "signupnickname";
		
		private String number;
		private String op;
		
		public LoginTrace(String number,String op){
			this.number = number ;
			this.op = op ; 
		}
		public String getcheck_op(){
			return "number:"+number+":"+"op:"+op;
		}
		
	}
/*	public class NoAirtelLoginPincodeViewWrapper extends ViewWrapper{
		private boolean inputLocked;
		private AnimationDrawable verifyingSmsPinAnimation;
		private final EditText pinCodeEditText =(EditText)findViewById(R.id.mSignUpPincodePincodeEditText);
		private final Button nextBtn =(Button)findViewById(R.id.mSignUpPincodeNextBtn);
		private final TextView msgTextView =(TextView)findViewById(R.id.mSignUpPincodeTextView);
		private final TextView msgTextView1 = (TextView)findViewById(R.id.mSignUpPincodeTextView1);
		private final TextView tapTextView = (TextView)findViewById(R.id.tap);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		public String phonenumber;

		public String number;
		

		
		public NoAirtelLoginPincodeViewWrapper(){
			super(R.id.mSignUpPincodeView, "login/enter_pincode");
			msgTextView.setTypeface(typeFace);
			msgTextView1.setTypeface(typeFace);
			
			tapTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					RuntimeLog.log("NoAirtelLoginPincodeViewWrapper - tapTextView - onClick");
					history.goBack();
					msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
					msgTextView.setText(R.string.ifgetpin);
					msgTextView1.setVisibility(View.VISIBLE);
					pinCodeEditText.setText(null);
					
				}
			});
			

			
			nextBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockInput();
					String pinCode = pinCodeEditText.getText().toString();
					final int workingFlag = userIntentFlag;
					SignClient.performSignUp(number, pinCode, mHandler, new Callback() {
						public void call(ResponseWrapper resp) {
							if(workingFlag==userIntentFlag){
								handleNetworkError(resp);
								if(resp!=null && resp.isValid() && resp.isNoError()){
									String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
									if(errMsg!=null){
										unLockInput(errMsg);
									}else{
										String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
										
										mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
										LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
										String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
										
										//let loginNickNameTraceCheck into sharePreference 				
										SharedPreferences.Editor localEditor = settings.edit();
								        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
								        localEditor.putString("loginTrace", loginTrace);
								        localEditor.putString("phonenumber", number);
								        localEditor.putBoolean("loginIsPincodeCheck", true);
								        localEditor.commit();
								        
										history.go(mNoAirtelLoginNicknameViewWrapper);
										mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
										msgTextView.setText(R.string.ifgetpin);
										msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
										msgTextView1.setVisibility(View.VISIBLE);
										unLockInput(null);
									}
								}else{
									unLockInput(null);
								}
							}else{
								unLockInput(null);
							}
						}
					});
				}
			});
			pinCodeEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					if(pinCodeEditText.getText().toString().length()!=0){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
				}
			});
			pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
		}
		
		private void lockInput(){
			if(!inputLocked){
				pinCodeEditText.setKeyListener(null);
				mUIHelper.disableBtn(nextBtn);
				nextBtn.setText("");	
				nextBtn.setBackgroundResource(R.anim.verifying_sms_pin);
				verifyingSmsPinAnimation = (AnimationDrawable)nextBtn.getBackground();
				verifyingSmsPinAnimation.start();
				tapTextView.setClickable(false);
				inputLocked = true;
			}
		}
		
		private void autoJumpToNickName(){
			lockInput();
			String Pincode = pinCodeEditText.getText().toString();
			final int workingFlag = userIntentFlag;
			SignClient.performSignUp(number, Pincode, mHandler, new Callback() {
				public void call(ResponseWrapper resp) {
					if(workingFlag==userIntentFlag){
						handleNetworkError(resp);
						if(resp!=null && resp.isValid() && resp.isNoError()){
							String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
							if(errMsg!=null){
								unLockInput(errMsg);
							}else{
								String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
								
								mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
								LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
								String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
								
								//let loginNickNameTraceCheck into sharePreference 				
								SharedPreferences.Editor localEditor = settings.edit();
						        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
						        localEditor.putString("loginTrace", loginTrace);
						        localEditor.putString("phonenumber", number);
						        localEditor.putBoolean("loginIsPincodeCheck", true);
						        localEditor.commit();
						        
								history.go(mNoAirtelLoginNicknameViewWrapper);
								mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
								msgTextView.setText(R.string.ifgetpin);
								msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
								msgTextView1.setVisibility(View.VISIBLE);
								unLockInput(null);
							}
						}else{
							unLockInput(null);
						}
					}else{
						unLockInput(null);
					}
				}
			});
		}
		
		public void passPhonenumberToPincodeView(String phonenumber){
			this.phonenumber = phonenumber;
		}
		
		private void unLockInput(String errMsg){

			if(inputLocked){
				if (verifyingSmsPinAnimation.isRunning()){
					mUIHelper.disableBtn(nextBtn);
					verifyingSmsPinAnimation.stop();
					
				}
				if(errMsg!=null){
					msgTextView.setText(errMsg);
					msgTextView.setTextColor(getResources().getColor(R.color.darkred));
					msgTextView1.setVisibility(View.GONE);
				}else{
					mUIHelper.enableBtn(nextBtn);
				}
				pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
				nextBtn.setText("Next");
				tapTextView.setClickable(true);
				inputLocked = false;
			}
		}
		@Override
		public boolean onShow(boolean isNew){
			if(isNew){
				if(this.number==null){
					return false;
				}else{
					unLockInput(null);
<<<<<<< OURS
					msgTextView.setText(R.string.smspin);
					if(pinCodeEditText.getText()!=null && !"".equals(pinCodeEditText.getText().toString())){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
=======
					//pinCodeEditText.setText(null);
					//msgTextView.setText(R.string.smspin);
					//mUIHelper.disableBtn(nextBtn);
					mUIHelper.enableBtn(nextBtn);
					mUIHelper.showSoftInput();
					return true;
				}
			}else{
				mUIHelper.showSoftInput();
				return true;
			}
		}
		@Override
		public boolean onHide(){
			return true;
		}
		public void setNumber(String number){
			this.number = number;
		}
		public void setPinCode(String pinCode){
			pinCodeEditText.setText(pinCode);
		}
	}*/
	
/*	public class NoAirtelLoginNicknameViewWrapper extends ViewWrapper{
		private boolean inputLocked;
		public String phonenumber;
		private final EditText nicknameEditText =(EditText)findViewById(R.id.mSignUpNicknameNicknameEditText);
		private final Button nextBtn =(Button)findViewById(R.id.mSignUpNicknameNextBtn);
		private final TextView msgTextView =(TextView)findViewById(R.id.mSignUpNicknameTextView);
		private final TextView phoneNumber = (TextView)findViewById(R.id.mSignUpNicknameTextView1);
		private final TextView whatisyourname = (TextView)findViewById(R.id.mSignUpNicknameTextView2);
		
			
		
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		private String loginTrace;
		public NoAirtelLoginNicknameViewWrapper(){
			super(R.id.mSignUpNicknameView, "login/enter_nickname");
			msgTextView.setTypeface(typeFace);
			phoneNumber.setTypeface(typeFace);  
			whatisyourname.setTypeface(typeFace);
			// setting loginTrace
			SharedPreferences setting = mActivity.getSharedPreferences("loginXML", 0);
			loginTrace = setting.getString("loginTrace", null);
			
			if (phonenumber == null || phonenumber.length() <=0){
				String number = setting.getString("phonenumber", null);
				if (number != null && number.length() >0){
					String front = number.substring(0, 3);
					String middle = number.substring(3,6);
					String last = number.substring(6);
					phoneNumber.setText("+91"+front+"-"+middle+"-"+last);
				}
							
			}
			
			nextBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockInput();
					String nickname = nicknameEditText.getText().toString();
					String errMsg = InputValidator.validateNickname(nickname);
					if(errMsg==null){
						final int workingFlag = userIntentFlag;					
						SignClient.performSignUpNickname(nickname, loginTrace, mHandler, new Callback() {
							public void call(ResponseWrapper resp) {
								if(workingFlag==userIntentFlag){
									handleNetworkError(resp);
									if(resp!=null && resp.isValid() && resp.isNoError()){
										String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
										if(errMsg!=null){
											unLockInput(errMsg);
										}else{
											finishSignIn(resp, false);
											//  clean LogintaceTiketCheck  from  sharepre
											SharedPreferences.Editor localEditor = settings.edit();
											localEditor.remove("loginNickNameTraceCheck");
											localEditor.commit();
											msgTextView.setText(R.string.seton);
											msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
											phoneNumber.setVisibility(View.VISIBLE);
											whatisyourname.setVisibility(View.VISIBLE);
										}
									}else{
										unLockInput(null);
									}
								}else{
									unLockInput(null);
								}
							}
						});
					}else{
						unLockInput(errMsg);
					}
				}
			});
			nicknameEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					 if (s.length() > 0) {
				            int pos = s.length() - 1;
				            char c = s.charAt(pos);
				            if ((c == '_')&& (pos == 0)) {
				                s.delete(pos,pos+1);
				            }
				        }
					
					if(nicknameEditText.getText().toString().length()!=0){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
				}
			});
			nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
		}
		
		
		
		
		private void lockInput(){
			if(!inputLocked){
				nicknameEditText.setKeyListener(null);
				mUIHelper.disableBtn(nextBtn);
				inputLocked = true;
				
			}
		}
		
		public void passPhoneNumberToNicknameView(String number){
			this.phonenumber = number;
			String front = phonenumber.substring(0, 3);
			String middle = phonenumber.substring(3,6);
			String last = phonenumber.substring(6);
			phoneNumber.setText("+91"+front+"-"+middle+"-"+last);
			
		}
		private void unLockInput(String errMsg){
			if(inputLocked){
				if(errMsg!=null){
					msgTextView.setText(errMsg);
					msgTextView.setTextColor(getResources().getColor(R.color.darkred));
					phoneNumber.setVisibility(View.GONE);
					whatisyourname.setVisibility(View.GONE);
					
				}else{
					mUIHelper.enableBtn(nextBtn);
				}
				nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
				inputLocked = false;
			}
		}
		@Override
		public boolean onShow(boolean isNew){
			if(isNew){
				if(this.loginTrace==null){
					return false;
				}else{
					unLockInput(null);
					nicknameEditText.setText(null);
					//msgTextView.setText(R.string.enternickname);
					mUIHelper.disableBtn(nextBtn);
					mUIHelper.showSoftInput();
					return true;
				}
			}else{
				mUIHelper.showSoftInput();
				return true;
			}
		}
		@Override
		public boolean onHide(){
			return true;
		}
		public void setLoginTrace(String loginTrace) {
			this.loginTrace = loginTrace;
		}
	}*/
/*	public class LoginAirtelViewWrapper extends ViewWrapper{
		private boolean inputLocked;
		private final EditText nicknameEditText =(EditText)findViewById(R.id.mSignUpAirtelNicknameEditText);
		private final Button nextBtn =(Button)findViewById(R.id.mSignUpAirtelNextBtn);
		private final TextView msgTextView1 = (TextView)findViewById(R.id.mSignUpAirtelMsgText);
		private final TextView msgTextView =(TextView)findViewById(R.id.mSignUpAirtelNumberText);
		private final TextView msgTextView2 = (TextView)findViewById(R.id.mSignUpAirtelMsgText_1);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");

		private String airtelNumber;
		public LoginAirtelViewWrapper(){
			super(R.id.mSignUpAirtelView, "login/airtel_enter_nickname");
			msgTextView1.setTypeface(typeFace);
			msgTextView.setTypeface(typeFace);
			msgTextView2.setTypeface(typeFace);
			nextBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockInput();
					String nickname = nicknameEditText.getText().toString();
					String errMsg = InputValidator.validateNickname(nickname);
					if(errMsg==null){
						final int workingFlag = userIntentFlag;
						SignClient.performSignUpNickname(nickname, null, mHandler, new Callback() {
							public void call(ResponseWrapper resp) {
								if(workingFlag==userIntentFlag){
									handleNetworkError(resp);
									if(resp!=null && resp.isValid() && resp.isNoError()){
										String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
										if(errMsg!=null){
											unLockInput(errMsg);
										}else{
											msgTextView2.setText(R.string.enteryournickname);
											msgTextView2.setTextColor(getResources().getColor(R.color.darkgray));
											finishSignIn(resp, true);
										}
									}else{
										unLockInput(null);
									}
								}else{
									unLockInput(null);
								}
							}
						});
					}else{
						unLockInput(errMsg);
					}
				}
			});
			nicknameEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					 if (s.length() > 0) {
				            int pos = s.length() - 1;
				            char c = s.charAt(pos);
				            if ((c == '_')&& (pos == 0)) {
				                s.delete(pos,pos+1);
				            }
				        }
					if(nicknameEditText.getText().toString().length()!=0){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
				}
			});
			nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
		}
		private void lockInput(){
			if(!inputLocked){
				nicknameEditText.setKeyListener(null);
				mUIHelper.disableBtn(nextBtn);
				inputLocked = true;
			}
		}
		
		private void passAirtelPhoneNumber(String phonenumber){
			this.airtelNumber = phonenumber;
		}
		private void unLockInput(String errMsg){
			if(inputLocked){
				if(errMsg!=null){
					msgTextView2.setText(errMsg);
					msgTextView2.setTextColor(getResources().getColor(R.color.darkred));
				}else{
					mUIHelper.enableBtn(nextBtn);
				}
				nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
				inputLocked = false;
			}
		}
		@Override
		public boolean onShow(boolean isNew){
			Log.w(TAG,"isNew = " + isNew);
			if(isNew){
				if(airtelNumber==null){
					return false;
				}else{
					unLockInput(null);
					nicknameEditText.setText(null);
					msgTextView.setText("+91" + airtelNumber);
					Log.w(TAG,"airtelNumber = " + airtelNumber);
					mUIHelper.disableBtn(nextBtn);
					mUIHelper.showSoftInput();
					return true;
				}
			}else{
				mUIHelper.showSoftInput();
				return true;
			}
		}
		@Override
		public boolean onHide(){
			return true;
		}
		public void setAirtelNumber(String airtelNumber) {
			this.airtelNumber = airtelNumber;
		}
	}*/
	
/*	public class SignInPincodeViewWrapper extends ViewWrapper{
		private boolean inputLocked;
		private final EditText pinCodeEditText =(EditText)findViewById(R.id.mSignInPincodePincodeEditText);
		private final Button nextBtn =(Button)findViewById(R.id.mSignInPincodeNextBtn);
		private final TextView msgTextView =(TextView)findViewById(R.id.mSignInPincodeTextView);
		private final TextView msgTextView1 = (TextView)findViewById(R.id.mSignInincodeTextView1);
		private final TextView tapTextView = (TextView)findViewById(R.id.SignIntap);
		private AnimationDrawable verifyingSignInSmsPinAnimation;
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");

		private String number;
		public SignInPincodeViewWrapper(){
			super(R.id.mSignInPincodeView, "login/signin_enterpin");
			msgTextView.setTypeface(typeFace);
			msgTextView1.setTypeface(typeFace);
			
			tapTextView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					history.goBack();
					msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
					msgTextView.setText(R.string.ifgetpin);
					msgTextView1.setVisibility(View.VISIBLE);
					pinCodeEditText.setText(null);
					
				}
			});
			
			nextBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					lockInput();
					String pinCode = pinCodeEditText.getText().toString();
					final int workingFlag = userIntentFlag;
					SignClient.performSignIn(number, pinCode, mHandler, new Callback() {
						public void call(ResponseWrapper resp) {
							if(workingFlag==userIntentFlag){
								handleNetworkError(resp);
								if(resp!=null && resp.isValid() && resp.isNoError()){
									String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
									if(errMsg!=null){
										unLockInput(errMsg);
									}else{
										finishSignIn(resp, false);
										//  clean  loginstraceTickeCheck from  sharepre
										SharedPreferences.Editor localEditor = settings.edit();
										localEditor.remove("loginNickNameTraceCheck");
										localEditor.commit();
									}
								}else{
									unLockInput(null);
								}
							}else{
								unLockInput(null);
							}
						}
					});
				}
			});
			pinCodeEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					if(pinCodeEditText.getText().toString().length()!=0){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
				}
			});
			pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
		}

		//today start
		
		
		
		private void autoLogin(final String pinCode){
			lockInput();
//			String pinCode = pinCodeEditText.getText().toString();
			final int workingFlag = userIntentFlag;
			SignClient.performSignIn(number, pinCode, mHandler, new Callback() {
				public void call(ResponseWrapper resp) {
					if(workingFlag==userIntentFlag){
						handleNetworkError(resp);
						if(resp!=null && resp.isValid() && resp.isNoError()){
							String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
							if(errMsg!=null){
								unLockInput(errMsg);
							}else{
								finishSignIn(resp, false);
								//  clean  loginstraceTickeCheck from  sharepre
								SharedPreferences.Editor localEditor = settings.edit();
								localEditor.remove("loginNickNameTraceCheck");
								localEditor.commit();
							}
						}else{
							unLockInput(null);
						}
					}else{
						unLockInput(null);
					}
				}
			});
		}
		
		
		
		
		
		
		
		
				private void AutoJumpToNickNameView(final String sms_pincode){
					mHandler.post(new  Runnable() {
						public void run() {
//							String sms_pincode=smsReciver.getPinCode(LoginActivity.this, getIntent());
							Log.i(TAG, "pincode="+sms_pincode);
					        
					        if (sms_pincode != null && sms_pincode.length() > 0){
					        	mNoAirtelLoginPincodeViewWrapper.setPinCode(pincode);
								lockInput();
								//String pinCode = pinCodeEditText.getText().toString();
								final int workingFlag = userIntentFlag;
								SignClient.performSignUp(number, sms_pincode, mHandler, new Callback() {
									public void call(ResponseWrapper resp) {
										if(workingFlag==userIntentFlag){
											handleNetworkError(resp);
											if(resp!=null && resp.isValid() && resp.isNoError()){
												String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
												if(errMsg!=null){
													unLockInput(errMsg);
												}else{
													String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
													
													mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
													LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
													String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
													
													//let loginNickNameTraceCheck into sharePreference 				
													SharedPreferences.Editor localEditor = settings.edit();
											        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
											        localEditor.putString("loginTrace", loginTrace);
											        localEditor.putString("phonenumber", number);
											        localEditor.putBoolean("loginIsPincodeCheck", true);
											        localEditor.commit();
											        
													history.go(mNoAirtelLoginNicknameViewWrapper);
													mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
													msgTextView.setText(R.string.ifgetpin);
													msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
													msgTextView1.setVisibility(View.VISIBLE);
													unLockInput(null);
												}
											}else{
												unLockInput(null);
											}
										}else{
											unLockInput(null);
										}
									}
								});
					        	
					        }
					        else{
								lockInput();
								String pinCode = pinCodeEditText.getText().toString();
								final int workingFlag = userIntentFlag;
								SignClient.performSignUp(number, pinCode, mHandler, new Callback() {
									public void call(ResponseWrapper resp) {
										if(workingFlag==userIntentFlag){
											handleNetworkError(resp);
											if(resp!=null && resp.isValid() && resp.isNoError()){
												String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
												if(errMsg!=null){
													unLockInput(errMsg);
												}else{
													String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
													
													mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
													LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
													String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
													
													//let loginNickNameTraceCheck into sharePreference 				
													SharedPreferences.Editor localEditor = settings.edit();
											        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
											        localEditor.putString("loginTrace", loginTrace);
											        localEditor.putString("phonenumber", number);
											        localEditor.putBoolean("loginIsPincodeCheck", true);
											        localEditor.commit();
											        
													history.go(mNoAirtelLoginNicknameViewWrapper);
													mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
													msgTextView.setText(R.string.ifgetpin);
													msgTextView.setTextColor(getResources().getColor(R.color.darkgray));
													msgTextView1.setVisibility(View.VISIBLE);
													unLockInput(null);
												}
											}else{
												unLockInput(null);
											}
										}else{
											unLockInput(null);
										}
									}
								});
					        	
					        }
						}
					});
					
				}
				//today end
				
		private void lockInput(){
			//if(!inputLocked){
				pinCodeEditText.setKeyListener(null);
				mUIHelper.disableBtn(nextBtn);
				nextBtn.setText("");	
				nextBtn.setBackgroundResource(R.anim.verifying_sms_pin);
				verifyingSignInSmsPinAnimation = (AnimationDrawable)nextBtn.getBackground();
				verifyingSignInSmsPinAnimation.start();
				tapTextView.setClickable(false);
				inputLocked = true;
			//}
		}
		private void unLockInput(String errMsg){
			//if(inputLocked){
				if (verifyingSignInSmsPinAnimation!=null && verifyingSignInSmsPinAnimation.isRunning()){
					verifyingSignInSmsPinAnimation.stop();
					mUIHelper.disableBtn(nextBtn);
				}
				if(errMsg!=null){
					msgTextView.setText(errMsg);
					msgTextView.setTextColor(getResources().getColor(R.color.darkred));
					msgTextView1.setVisibility(View.GONE);
				}else{
					mUIHelper.enableBtn(nextBtn);
				}
				pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
				nextBtn.setText("Next");
				tapTextView.setClickable(true);
				inputLocked = false;
			//}
		}
		@Override
		public boolean onShow(boolean isNew){
			if(isNew){
				if(this.number==null){
					return false;
				}else{
					unLockInput(null);
					msgTextView.setText(R.string.ifgetpin);
					if(pinCodeEditText.getText()!=null && !"".equals(pinCodeEditText.getText().toString())){
						mUIHelper.enableBtn(nextBtn);
					}else{
						mUIHelper.disableBtn(nextBtn);
					}
					mUIHelper.showSoftInput();
					return true;
				}
			}else{
				mUIHelper.showSoftInput();
				return true;
			}
		}
		@Override
		public boolean onHide(){
			return true;
		}
		public void setNumber(String number){
			this.number = number;
		}
		public void setPinCode(String pinCode){
			pinCodeEditText.setText(pinCode);
		}
	}*/
	
/*	public class HelpViewWrapper extends ViewWrapper{
		private final TextView help_textview1 = (TextView)findViewById(R.id.help_content);
		private final TextView help_textview2 = (TextView)findViewById(R.id.help_content1);
		private final TextView help_textview3 = (TextView)findViewById(R.id.help_content2);
		private final TextView help_title = (TextView)findViewById(R.id.help);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Helvetica.ttf");
		Typeface typeFace1 = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		public HelpViewWrapper(){
			super(R.id.mHelpView, "login/help");
			help_textview1.setTypeface(typeFace);
			help_textview2.setTypeface(typeFace);
			help_textview3.setTypeface(typeFace);
			help_title.setTypeface(typeFace1);
			
			((ImageButton)findViewById(R.id.mHelpBackBtn)).setOnClickListener(history.getGoBackOnClickListener());
		}
		@Override
		public boolean onShow(boolean isNew){
			mUIHelper.hideSoftInput();
			return true;
		}
	}*/
/*	public class HelpCustomPinViewWrapper extends ViewWrapper{
		private final TextView custom_help_title = (TextView)findViewById(R.id.custom_help);
		private final TextView custom_help_textview = (TextView)findViewById(R.id.custom_help_content);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		Typeface typeFace1 = Typeface.createFromAsset(getAssets(),"font/Helvetica.ttf");

		public HelpCustomPinViewWrapper(){
			super(R.id.mHelpCustomPinView, "login/help_custom_pin");
			custom_help_textview.setTypeface(typeFace1);
			custom_help_title.setTypeface(typeFace);
			((ImageButton)findViewById(R.id.mHelpCustomPinBackBtn)).setOnClickListener(history.getGoBackOnClickListener());
		}
		@Override
		public boolean onShow(boolean isNew){
			mUIHelper.hideSoftInput();
			return true;
		}
	}*/
	//================================busness view end==================================

	public class UIHelper{
		private boolean isActive;
		private final Resources mResouces;
		private final Drawable mEnableBtnBackGround;
		private final Drawable mDisableBtnBackGround;
		private final InputMethodManager mInputMethodManager;
		
		public UIHelper(){
			mResouces = getResources();
			mEnableBtnBackGround = mResouces.getDrawable(R.drawable.login_blue_btn);
			mDisableBtnBackGround = mResouces.getDrawable(R.drawable.btn_bg_disable);
			LoginLayout layout = (LoginLayout) findViewById(R.id.LoginLayout);
			layout.setOnSoftInputListener(new OnSoftInputListener() {
				
				@Override
				public void onShow() {
					isActive = true;
				}
				
				@Override
				public void onHide() {
					isActive = false;
				}
			});
			mInputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		
		public void disableBtn(Button btn){
			btn.setClickable(false);
			btn.setBackgroundDrawable(mDisableBtnBackGround);
			btn.setTextColor(getResources().getColor(R.color.gray));
		}
		public void enableBtn(Button btn){
			btn.setClickable(true);
			btn.setBackgroundDrawable(mEnableBtnBackGround);
			btn.setTextColor(getResources().getColor(R.color.white));
		}
		public void showSoftInput(){
			new Timer().schedule(new TimerTask() {
	            @Override  
	            public void run() {
	            	if(!isActive){
	            		mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
	            	}
	            }
	        }, 500);
		}
		public void hideSoftInput(){
			new Timer().schedule(new TimerTask() {
	            @Override  
	            public void run() {
	            	if(isActive){
	            		mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
	            	}
	            }
	        }, 500);
		}
	}
	public static final class InputValidator{
		public static final NumberKeyListener KEY_LISTENER_PHONENUMBER = new NumberKeyListener() {
			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_PHONE;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
			}
		};
		public static final NumberKeyListener KEY_LISTENER_PINCODE = new NumberKeyListener() {
			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_NUMBER;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
			}
		};
		public static final NumberKeyListener KEY_LISTENER_PINCODE_CUSTOM = new NumberKeyListener() {
			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
						'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
			}
		};
		public static final NumberKeyListener KEY_LISTENER_NICKNAME = new NumberKeyListener() {
			@Override
			public int getInputType() {
				return InputType.TYPE_CLASS_TEXT;
			}
			
			@Override
			protected char[] getAcceptedChars() {
				return new char[]{'_','0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
						'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
			}
		};
		
		public static String validateNumber(String number){
	    	//Pattern pattern=Pattern.compile("(^\\+91\\d{10}$|^0091\\d{10}$|^0\\d{10}$|^\\d{10}$)");
	    	Pattern pattern=Pattern.compile("(^\\d{10}$)");
			Matcher matcher=pattern.matcher(number);
			if(!matcher.find()){
				//return "Your phone number should be 10-14 digits long, only numbers and \"+\".";
				return "Your phone number should be 10 digits long, only numbers.";
			}
			return null;
		}
		public static String validateNickname(String nickname){
			if(nickname.length()<3){
				return "Nickname cannot be less than 3 letters.";
			}
			char last = nickname.charAt(nickname.length()-1);
			if (last == '_'){
				return "Nickname's last letter can't be '_'";
			}
			
			
			return null;
		}
		public static String validateCustomPin(String customePin){
			return null;
		}
	}
}
