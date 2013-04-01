package in.android.games.in.dialog;

import in.android.games.in.account.ZoneAccount;
import in.android.games.in.account.ZoneAccountManager;
import in.android.games.in.client.Callback;
import in.android.games.in.client.ResponseWrapper;
import in.android.games.in.common.Constants;
import in.android.games.in.jsbridge.PaymentCallback;
import in.android.games.in.receiver.SMSReceiver;
import in.android.games.in.utils.CookieUtil;
import in.android.games.in.utils.RuntimeLog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;


import zonesdk.in.android.games.in.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentDialog extends Dialog implements View.OnClickListener {

	private static final String TAG="PaymentDialog";
	private final int GET_CODE_DELAY = 1000 * 15;
	private String tip_message_prefix="Get Pin Code ";
	private String tip_message_footer="s later.";
	private String getpincode_Text="Get Code";
	public static long seconds_cutdown_start=0;
	private final int WAIT_SECOND=60;//120;
	protected static final String VERIFY_URL = "http://"+Constants.DOMAIN_SNS+"/pay/verifyPincode";
	protected static final String GET_PIN_CODE_URL = "http://"+Constants.DOMAIN_SNS+"/pay/recharge/getpincode";
	
	//handler code:
	protected static final int TIME_TICK = 0;
	protected static final int PULL_PIN = 1;
	protected static final int PULL_MST = 2;
	protected static final int SMS_RCVD = 3;
	protected static final int PULL_OUT = 4;
	protected static final int VRF_PIN = 5;
	protected static final int VRF_OUT = 6; //time out
	protected static final int VRF_DONE = 7; //process done, no need it 
	protected static final int PIN_NOP = 8;  // from pin receive 


	View textEntryView;
	private String hikeCoin,hikeName, mobileNumber, rechargeAcount, id, token,needPincode;
	private TextView hikeNameView, mobileNumberView, rechargeAcountView,actualPaymentView,paymentPincodeText,
	RechargeCountCoinsText,actualPaymentRupeesText,hikeNameid,mobileNumberid,RechargeCountId,actualPaymentId,
	PaymentMethodText,PaymentMethodId,titleView,PaymentTransactionId,PaymentTransactionText;
	public Button paymentSure, paymentCancel;
	//public Button paymentGetCode;
	public Button pincodeError;
	private AnimationDrawable paymentSureAnimation;
	
	private PaymentCallback callback;
	private EditText paymentPincode;
	private String pinCode;

	private static Timer timer;
	private static MyHandler handler;

	//private Handler handler1;
	//private Dialog dialog;
	private Context context;
	private Intent intent ;
	private Typeface typeFace,typeFaceroman;
	//private  SMSReceiver smsReciver;
	private Drawable drawable;

	private static Activity  mActivity;
	private String orderId;

	//use it to listen pincode localbroadcast
	private BroadcastReceiver mMessageReceiver; 
	private static Timer SMStimer; 
	private  boolean pin_noop = false;
	//private static boolean SMSRCVD = false;
	private final int PULL_PIN_DELAY = 60*1000;
	enum State { 
		  INIT      
        ,PULL_IN      
        ,PULL_OUT  
        ,PULL_RCVD

    };
    private  State userType = State.INIT;



	

	


	/**
	 * @author zhangwei
	 * This Handler class should be static or leaks might occur
	 */
	static class MyHandler extends Handler{ 
        WeakReference<PaymentDialog> mActivity;

        MyHandler(PaymentDialog activity) {
                mActivity = new WeakReference<PaymentDialog>(activity);
        }
        
        @Override 
        public void handleMessage(Message msg) { 
        	PaymentDialog theActivity = mActivity.get();
            //super.handleMessage(msg); 
        	switch(msg.what){
	/*        	case GET_PIN:
	        		theActivity.paymentPincode.setText((String)(msg.obj));
	                break;*/
	        	case TIME_TICK:
		            long secondInterval=(System.currentTimeMillis()-seconds_cutdown_start)/1000;
		            if(secondInterval<=theActivity.WAIT_SECOND)
		            {
		            	long time=theActivity.WAIT_SECOND-secondInterval;
		            	//theActivity.paymentGetCode.setClickable(false);
		            	//theActivity.paymentGetCode.setBackgroundColor(Color.GRAY);
		            	//theActivity.paymentGetCode.setText( "0"+time/60+":"+(time%60>=10?time%60:"0"+time%60)); 
		            } else { 
		            	//theActivity.paymentGetCode.setText(theActivity.getpincode_Text); 
		            	theActivity.timer.cancel(); 
		            	//theActivity.paymentGetCode.setBackgroundDrawable(theActivity.drawable);
		            	//theActivity.paymentGetCode.setClickable(true);
		            } 
		            break;
	        	case PULL_PIN:
	        		RuntimeLog.log("case PULL_PIN - lockinput");
	        		theActivity.pincodeError.setVisibility(View.GONE);
	        		theActivity.lockinput(R.anim.pin_wait);
	        		theActivity.captureSMS(true);
	        		theActivity.userType=State.PULL_IN;
	                break;
	                
	        	case PULL_MST:
	        		RuntimeLog.log("case PULL_MST - lockinput");
	        		theActivity.pincodeError.setVisibility(View.GONE);
	        		theActivity.changeAnimation(R.anim.pin_almost);
	                break;
	                
	        	case PULL_OUT: 		
	        		//sms not reviced, stop animation and let the user input					
					RuntimeLog.log("case PULL_OUT - unlockinput");
					theActivity.unlockinput();
					theActivity.captureSMS(false);
					theActivity.pincodeError.setVisibility(View.VISIBLE);
					theActivity.pincodeError.setText(R.string.pincodeerror);
					theActivity.userType=State.PULL_OUT;
					//theActivity.pincodeError.setText(resid);
					//theActivity.paymentPincode.setEnabled(true);
					break;

	        	case SMS_RCVD:
	        		RuntimeLog.log("case SMS_RCVD - unlockinput");

					
					theActivity.unlockinput();			
					theActivity.captureSMS(false);
	        		if(theActivity.userType==State.PULL_IN){
	        			theActivity.paymentPincode.setText((String)(msg.obj));
	        		}
					theActivity.userType=State.PULL_RCVD;
	        		//theActivity.paymentPincode.setEnabled(true);
					break;
					
	        	case VRF_PIN:
	        		RuntimeLog.log("case VRF_PIN - lockinput" );
	        		theActivity.lockinput(R.anim.verifying_sms_pin);
	        		theActivity.pincodeError.setVisibility(View.GONE);
	                break;
	                
	        	case VRF_OUT:	        		
	        		RuntimeLog.log("case VRF_OUT - unlockinput" );
	        		theActivity.unlockinput();
	                break;
	                
	        	case VRF_DONE: 
	        		RuntimeLog.log("case VRF_DONE - unlockinput" );
	        		theActivity.unlockinput();

	                break;
	                
	        	case PIN_NOP: 
	        		RuntimeLog.log("case PIN_NOP - pin_noop" );
	        		theActivity.pin_noop = true;

	        		break;
	         
	        	default:
	        		RuntimeLog.log("case default - msg.what:" + msg.what);
	        		break;


        	}

        } 
    };
    
    private void lockinput(int ResID){
		pincodeError.setVisibility(View.GONE);
		paymentPincode.setClickable(false);
		paymentPincode.setFocusable(false);
		paymentPincode.setFocusableInTouchMode(false);
		paymentSure.setText(null);    		
		paymentSure.setBackgroundResource(ResID);
		
		paymentSureAnimation = (AnimationDrawable)paymentSure.getBackground();
		paymentSureAnimation.start();
	}

	private void unlockinput(){
		//pincodeError.setVisibility(View.GONE);
		paymentPincode.setClickable(true);
		paymentPincode.setFocusable(true);
		paymentPincode.setFocusableInTouchMode(true);
		paymentSure.setText(R.string.gotorecharge);    		
		paymentSure.setBackgroundResource(R.drawable.paymentbtn);
		
		if (paymentSureAnimation.isRunning()){
			paymentSureAnimation.stop();
			
		}
	}
	
	private void changeAnimation(int ResID){
		if(paymentSureAnimation.isRunning()){			
			paymentSureAnimation.stop();
			paymentSure.setBackgroundResource(ResID);
			paymentSureAnimation = (AnimationDrawable)paymentSure.getBackground();
			paymentSureAnimation.start();
			
		}

	}



	public PaymentDialog(Context context,Intent intent, String hikeName, String mobileNumber,

			String rechargeAcount,String hikeCoin, String id, String token, String needPincode,String orderId) {

		super(context, R.style.dialog);
		RuntimeLog.log("PaymentDialog - In");

		this.context = context;
		this.intent = intent;
		this.hikeName = hikeName;
		this.mobileNumber = mobileNumber;
		this.rechargeAcount = rechargeAcount;
		this.id = id;
		this.token = token;
		this.needPincode = needPincode;
		this.orderId=orderId;
		this.hikeCoin=hikeCoin;
        LayoutInflater factory = LayoutInflater.from(context);
        textEntryView = factory.inflate(R.layout.payment_dialog_layout, null);
		initView(textEntryView);
		setOnClickListener();
	    //dialog = new Dialog(context,R.style.dialog);
		setContentView(textEntryView);
	    mActivity = (Activity)(context);		   

		WindowManager windowmanager = mActivity.getWindowManager();
		Display display = windowmanager.getDefaultDisplay();
	    Window dialogWindow = getWindow();
	    WindowManager.LayoutParams p = getWindow().getAttributes(); 
	    p.height = (int)(display.getHeight()*0.78); 
	    dialogWindow.setAttributes(p);
		show();
		setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				callback.onCancelPayment();
			}
			
		});

		/**
		 * @author zhangwei
		 * This BroadcastReceiver class used to get pincode from MySMSRecevier
		 */
		mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Get extra data included in the Intent
				String message = intent.getStringExtra("pincode");
				Log.d("receiver", "Got message: " + message);
				//paymentPincode.setText(message);
                Message msg = new Message(); 
                msg.what = SMS_RCVD;
                msg.obj = message;
                handler.sendMessage(msg); 
                handler.removeMessages(PULL_OUT);
				
			}
		};

		
		// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "custom-event-name".
		LocalBroadcastManager.getInstance(context).registerReceiver(
				mMessageReceiver,
				new IntentFilter(Constants.PIN_CODE_TYPE_PAYMENT));
		
		
		SMStimer = new Timer();
		
		pullingPinCode();

	}
	
	protected void finalize() {
		RuntimeLog.log("PaymentDialog - finalize");
		// Unregister since the activity is about to be closed.
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				mMessageReceiver);

		SMStimer.cancel();
		SMStimer=null;
	}

	public void initView(View view) {
		paymentCancel = (Button)view.findViewById(R.id.paymentCancelBtn);
		paymentSure = (Button) view.findViewById(R.id.paymentSureBtn);



		//paymentGetCode = (Button) view.findViewById(R.id.paymentGetCodeBtn);
		//drawable = paymentGetCode.getBackground();
		//paymentGetCode.setClickable(false);
    	//paymentGetCode.setBackgroundColor(Color.GRAY);

		pincodeError = (Button) view.findViewById(R.id.pincodeErrorText);

		long secondInterval=(System.currentTimeMillis()-seconds_cutdown_start)/1000;
		if(secondInterval<=WAIT_SECOND)
		{
			//paymentGetCode.setClickable(false);
        	//paymentGetCode.setBackgroundColor(Color.GRAY);
        	pincodeError.setVisibility(View.VISIBLE);
			pincodeError.setText(tip_message_prefix+(WAIT_SECOND-secondInterval)+tip_message_footer);
		}else
		{
			//paymentGetCode.setClickable(true);
			//paymentGetCode.setText(getpincode_Text);
        	//paymentGetCode.setBackgroundDrawable(drawable);
        	pincodeError.setVisibility(View.GONE);
		}
		

	    handler = new MyHandler(this) ;

	    
	    //smsReciver =new SMSReceiver(handler);
		
		hikeNameView = (TextView) view.findViewById(R.id.hikeNameText);
		mobileNumberView = (TextView) view.findViewById(R.id.mobileNumberText);
		rechargeAcountView = (TextView) view.findViewById(R.id.RechargeCountText);
		actualPaymentView = (TextView) view.findViewById(R.id.actualPaymentText);
		PaymentMethodText = (TextView) view.findViewById(R.id.PaymentMethodText);
		PaymentTransactionText=(TextView) view.findViewById(R.id.PaymentOrderIdText);
		hikeNameid = (TextView) view.findViewById(R.id.hikeNameId);
		mobileNumberid = (TextView) view.findViewById(R.id.mobileNumberId);
		RechargeCountId = (TextView) view.findViewById(R.id.RechargeCountId);
		actualPaymentId = (TextView) view.findViewById(R.id.actualPaymentId);
		PaymentMethodId = (TextView) view.findViewById(R.id.PaymentMethodId);
		PaymentTransactionId= (TextView) view.findViewById(R.id.PaymentOrderId);
		paymentPincodeText=	(TextView) view.findViewById(R.id.paymentPincodeText);
		RechargeCountCoinsText = (TextView) view.findViewById(R.id.RechargeCountCoinsText);
		actualPaymentRupeesText = (TextView) view.findViewById(R.id.actualPaymentRupeesText);
		paymentPincode = (EditText) view.findViewById(R.id.paymentPincodeText);
		titleView = (TextView) view.findViewById(R.id.titleView);
		typeFace = Typeface.createFromAsset(context.getAssets(),"font/Edmondsans-Regular.otf");
		typeFaceroman = Typeface.createFromAsset(context.getAssets(),"font/HelveticaNeue-Roman.otf");
		titleView.setTypeface(typeFace);
		PaymentTransactionId.setTypeface(typeFace);
		paymentCancel.setTypeface(typeFaceroman);
		paymentSure.setTypeface(typeFaceroman);
		pincodeError.setTypeface(typeFaceroman);
		paymentPincodeText.setTypeface(typeFaceroman);
		
		hikeNameView.setTypeface(typeFace);
		hikeNameid.setTypeface(typeFace);
		mobileNumberView.setTypeface(typeFace);
		mobileNumberid.setTypeface(typeFace);
		PaymentMethodText.setTypeface(typeFace);
		PaymentTransactionText.setTypeface(typeFace);
		rechargeAcountView.setTypeface(typeFace);
		RechargeCountId.setTypeface(typeFace);
		actualPaymentView.setTypeface(typeFace);
		actualPaymentId.setTypeface(typeFace);
		PaymentMethodId.setTypeface(typeFace);
		
		rechargeAcountView.setTypeface(typeFace);
		actualPaymentView.setTypeface(typeFace);
		RechargeCountCoinsText.setTypeface(typeFace);
		actualPaymentRupeesText.setTypeface(typeFace);

    
    	if (hikeName != null) {
			hikeNameView.setText(hikeName);
		}
		if (null != mobileNumber) {
			mobileNumberView.setText(mobileNumber);
		}
		if(null !=orderId)
		{
			PaymentTransactionText.setText(orderId);
		}
		if (null != rechargeAcount&&Integer.parseInt(rechargeAcount) > 1) {
			actualPaymentView.setText(rechargeAcount);
			actualPaymentRupeesText.setText(" Rupees");
			rechargeAcountView.setText(hikeCoin);
			RechargeCountCoinsText.setText(" Coins");
		}else{
			actualPaymentView.setText(rechargeAcount);
			actualPaymentRupeesText.setText(" Rupee");
			rechargeAcountView.setText(hikeCoin);
			RechargeCountCoinsText.setText(" Coin");
		}
		if (null != hikeCoin&&Integer.parseInt(hikeCoin) > 1) {
			rechargeAcountView.setText(hikeCoin);
			RechargeCountCoinsText.setText(" Coins");
		}else{
			rechargeAcountView.setText(hikeCoin);
			RechargeCountCoinsText.setText(" Coin");
		}
		

	}
	
	private void showExpire(View view){
		RelativeLayout linearLayoutHikeName = (RelativeLayout) view.findViewById(R.id.linearLayoutHikeName);
		RelativeLayout linearLayoutMobileNumber = (RelativeLayout) view.findViewById(R.id.linearLayoutMobileNumber);
		RelativeLayout linearLayoutRechargeCount = (RelativeLayout) view.findViewById(R.id.linearLayoutRechargeCount);
		RelativeLayout linearLayoutPayment = (RelativeLayout) view.findViewById(R.id.linearLayoutPayment);
		RelativeLayout linearLayoutPaymentMethod = (RelativeLayout) view.findViewById(R.id.linearLayoutPaymentMethod);
		RelativeLayout linearLayoutUserMethod = (RelativeLayout) view.findViewById(R.id.linearLayoutUserMethod);
		
		linearLayoutHikeName.setVisibility(View.GONE);
		linearLayoutMobileNumber.setVisibility(View.GONE);
		linearLayoutRechargeCount.setVisibility(View.GONE);
		linearLayoutRechargeCount.setVisibility(View.GONE);
		linearLayoutPayment.setVisibility(View.GONE);
		linearLayoutPaymentMethod.setVisibility(View.GONE);
		linearLayoutUserMethod.setVisibility(View.GONE);
		
		TextView titleView = (TextView) view.findViewById(R.id.titleView);
		titleView.setText(R.string.pinexpinfo);
		
	}
	
	private void pullingPinCode(){
		RuntimeLog.log("pullingPinCode - In");
		pin_noop=false;


	    
        Message msg = new Message(); 
        msg.what = PULL_PIN;
        handler.sendMessage(msg);
        handler.sendEmptyMessageDelayed(PULL_MST, PULL_PIN_DELAY*2/3);
        handler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);

/*		getPincode(orderId, handler, new Callback() {
		public void call(ResponseWrapper resp) {
			handleNetworkError(resp);
			if (resp != null && resp.isValid()&& resp.isNoError()) {

				handler.sendEmptyMessageDelayed(PIN_NOP, PULL_PIN_DELAY*5);
	             
				try{   
					 pinCode = resp.getStringFromResp("pinCode");
					 Log.e(TAG, "pinCode###########"+pinCode);
					 pinCode = null;
					 if(pinCode!=null && !pinCode.trim().equals(""))
					 {
						 //emulate user, from network
						 paymentPincode.setText(pinCode);
						 
			             Message msg = new Message(); 
			             msg.what = SMS_RCVD;
			             msg.obj = pinCode;
			             handler.sendMessage(msg); 
			             handler.removeMessages(PULL_OUT);
					 }else
					 {
						 //real user, from sms
					     //abort sms  when the right sms come in one minute


					 }
					 
				}catch(Exception e){ 
						Log.i(TAG, "pinCode = " + pinCode);
						Log.w(TAG, e.toString());
						return;
				}
			}else{
				return;
			} 
		}
		});*/
		
		//update UI
/*		timer = new Timer(); 
        TimerTask timerTask = new TimerTask() { 
            @Override 
            public void run() { 
                Message msg = new Message(); 
                msg.what = TIME_TICK;
                handler.sendMessage(msg); 
            } 
        }; 
        timer.schedule(timerTask, 1000, 1000);*/
        

       
	
	}
	
	private void captureSMS(boolean flag){
		RuntimeLog.log("captureSMS - In");
		
		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean("ABORTSMS", flag);
		edit.commit();
	}
	
/*	private void captureSMS(){
		RuntimeLog.log("captureSMS - In");
		
		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean("ABORTSMS", true);
		edit.commit();
		
		//SMStimer.cancel();
		SMStimer.purge();
		SMStimer.schedule(new TimerTask(){ 
			  
            @Override  
            public void run() {
            	RuntimeLog.log("SMStimer - run()");
        		SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
        		Editor edit = preferences.edit();
        		edit.putBoolean("ABORTSMS", false);
        		edit.commit();
            }  
        }, PULL_PIN_DELAY);   
			
		
	}*/

	public void setOnClickListener() {
		paymentCancel.setOnClickListener(this);
		paymentSure.setOnClickListener(this);
		//paymentGetCode.setOnClickListener(this);
		
/*        setOnKeyListener(new DialogInterface.OnKeyListener() {
            
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                // Cancel task.
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                	cancel();
                }
                return false;
            }
        });*/
		
	}
	
	
    
	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.paymentCancelBtn: {
			cancel();
			break;
		}

/*		case R.id.paymentGetCodeBtn: {
//			if(!paymentGetCode.getBackground().equals(drawable))
//			{
//				break;				
//			}
//			seconds_cutdown_start=System.currentTimeMillis();
//			paymentGetCode.setClickable(false);
=======
/*		case R.id.paymentGetCodeBtn: {
			if(!paymentGetCode.getBackground().equals(drawable))
			{
				break;				
			}
			seconds_cutdown_start=System.currentTimeMillis();
			paymentGetCode.setClickable(false);
>>>>>>> d1d444f payment routine modify and opt
			paymentGetCode.setBackgroundColor(Color.GRAY);
			pincodeError.setVisibility(View.GONE);
			getPincode(orderId,new Handler(),new Callback() {
			@Override
			public void call(ResponseWrapper resp) {
				handleNetworkError(resp);
				if (resp != null && resp.isValid()&& resp.isNoError()) {
					try{ 
						 
						 pinCode = resp.getStringFromResp("pinCode");
						 Log.e(TAG, "pinCode###########"+pinCode);
						 if(pinCode!=null && !pinCode.trim().equals(""))
						 {
							 paymentPincode.setText(pinCode);
						 }else
						 {
							 //use sms capture
							 
							 
							 handler.postDelayed(new Runnable(){
									public void run() {
										String pincode1=smsReciver.getPinCode(context,intent);
										Log.e(TAG, "pincode1###########"+pincode1);
										paymentPincode.setText(pincode1);
									}
								}, GET_CODE_DELAY); 
						 }
						 
						}catch(Exception e){ 
							Log.i(TAG, "pinCode = " + pinCode);
							Log.w(TAG,e.toString());
							return;
						}
				}else{
					return;
				} 
			}
			});
			
<<<<<<< Upstream, based on origin/20130125
//			timer = new Timer(); 
//	        TimerTask timerTask = new TimerTask() { 
//	            @Override 
//	            public void run() { 
//	                Message msg = new Message(); 
//	                msg.what = TIME_TICK;
//	                handler.sendMessage(msg); 
//	            } 
//	        }; 
//	        timer.schedule(timerTask, 1000, 1000);
=======
			//update UI
			timer = new Timer(); 
	        TimerTask timerTask = new TimerTask() { 
	            @Override 
	            public void run() { 
	                Message msg = new Message(); 
	                msg.what = TIME_TICK;
	                handler.sendMessage(msg); 
	            } 
	        }; 
	        timer.schedule(timerTask, 1000, 1000);
	        
	        //abort sms flag when the right sms come in
        	SharedPreferences preferences = mActivity.getSharedPreferences("SMS", Context.MODE_PRIVATE);
        	Editor edit = preferences.edit();
        	edit.putBoolean("ABORTSMS", true);
        	edit.commit();
>>>>>>> d1d444f payment routine modify and opt
	        break;
		}*/
		case R.id.paymentSureBtn: {
			//first check the pin if exp
			if(pin_noop){
				showExpire(textEntryView);
			}
			
			Log.e(TAG, "***********enter pay confirm");
			pincodeError.setVisibility(View.GONE);
			if (needPincode.equals("true")&&pinCode==null&&paymentPincode.getText().toString().equals("")) {
				paymentSure.setClickable(false);
				pincodeError.setVisibility(View.VISIBLE);
				pincodeError.setText(R.string.emptycode);
			} else if (needPincode.equals("true")&&paymentPincode.getText().toString()!=null){
				Log.e(TAG, "*************before call pay confirm");

				//verifyPincode(mobileNumber,orderId,paymentPincode.getText().toString(), new Handler(),new Callback() {

                Message msg = new Message(); 
                msg.what = VRF_PIN;
                handler.sendMessage(msg); 
                handler.sendEmptyMessageDelayed(VRF_OUT, PULL_PIN_DELAY);
				
				verifyPincode(mobileNumber, orderId, paymentPincode.getText().toString(), new Handler(),new Callback() {

					public void call(ResponseWrapper resp) {
		                Message msg = new Message(); 
		                msg.what = VRF_DONE;
		                handler.removeMessages(VRF_OUT);
		                handler.sendMessage(msg); 

		                
						if (resp != null) {
								int status = resp.getStatus();
								if(status!=0)
								{
									Log.e(TAG, "*************status" +status);
									pincodeError.setVisibility(View.VISIBLE);
									pincodeError.setText(R.string.errorcode);
									paymentPincode.setText(null);
									
								}else
								{
									JSONObject json = new JSONObject();
									try {
										json.put("hikeName", hikeName);
										json.put("mobileNumber", mobileNumber);
										json.put("rechargeAcount", rechargeAcount);
										json.put("actualPayment", rechargeAcount);
										json.put("id", id);
										json.put("token", token);
										json.put("needPincode", paymentPincode.getText().toString());
										json.put("orderId", orderId);
										Log.e(TAG, "*************json" +json.toString());
									} catch (JSONException e) {
										e.printStackTrace();
									}
									

									
									onSubmit(paymentSure.getText().toString(), json.toString());
								}
						}else{
							return;
						} 
					}
				});
			}
			paymentSure.setClickable(true);
			//paymentGetCode.setClickable(true);
			break;
		}
	}
	}

	private void handleNetworkError(ResponseWrapper resp){
		if(resp==null){
			Toast.makeText(context,"Network Problem!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void onSubmit(String buttonName, String message) {
		callback.onDialogGoToCharge(this, buttonName, message);
		dismiss();
	}

	public void setPaymentCallback(PaymentCallback callback) {
		this.callback = callback;
	}

	public Thread verifyPincode(final String mobileNo,final String orderId, final String pincode,final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				ZoneAccount account = initAccount();
				List<Header> headers = new ArrayList<Header>();
				headers.add(genPassportCookieHeader(account.getPassport()));
				
				String[] paraKeys = {"phoneNum", "orderId", "pincode" };
				String[] paraValues = { mobileNo,orderId, pincode };

				final ResponseWrapper resp = sendPostRequest(VERIFY_URL, paraKeys, paraValues, null, headers);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	
	public Thread getPincode(final String orderId,final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				ZoneAccount account = initAccount();
				List<Header> headers = new ArrayList<Header>();
				headers.add(genPassportCookieHeader(account.getPassport()));
				final ResponseWrapper resp = sendGetRequest(GET_PIN_CODE_URL+"?orderId="+orderId, null, headers);
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	
	private static Thread performOnBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
				}
			}
		};
		t.start();
		return t;
	}
	
	private ResponseWrapper sendGetRequest(String url, CookieStore reqCookieStore, List<Header> headers) {
		Log.i(TAG, "sendGetRequest() begin url=" + url + ",reqCookieStore=" + reqCookieStore);
		
		HttpGet httpGet = new HttpGet(url);
		if(headers!=null){
			for(Header header:headers){
				httpGet.addHeader(header);
			}
		}
		DefaultHttpClient httpClient = newDefaultHttpClient();
		if(reqCookieStore!=null){
			httpClient.setCookieStore(reqCookieStore);
		}
		HttpResponse resp;
		CookieStore cookieStore;
//		addAirtelHeader(httpGet);
		try {
			resp = httpClient.execute(httpGet);
			cookieStore = httpClient.getCookieStore();
		} catch (ClientProtocolException e) {
			Log.e(TAG, "", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		int statusCode = resp.getStatusLine().getStatusCode();
		Log.i(TAG, "sendGetRequest() statusCode=" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			return new ResponseWrapper(resp, cookieStore);
		}else{
			return null;
		}
	}
	
	private ResponseWrapper sendPostRequest(String url, String[] paraKeys, String[] paraValues, CookieStore reqCookieStore, List<Header> headers) {
		Log.i(TAG, "sendPostRequest() begin, url=" + url);
		HttpPost httpPost = new HttpPost(url);
		if(headers!=null){
			for(Header header:headers){
				httpPost.addHeader(header);
			}
		}
		if(paraKeys!=null && paraKeys.length>0 && paraValues!=null && paraValues.length>0){
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			int paraLen = paraKeys.length;
			for (int i = 0; i < paraLen; i++) {
				Log.i(TAG, "sendPostRequest() " + paraKeys[i] + "=" + paraValues[i]);
				params.add(new BasicNameValuePair(paraKeys[i], paraValues[i]));
			}
			HttpEntity entity = null;
			try {
				entity = new UrlEncodedFormEntity(params);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "", e);
				return null;
			}
			httpPost.setEntity(entity);
		}
		
		DefaultHttpClient httpClient = newDefaultHttpClient();
		if(reqCookieStore!=null){
			httpClient.setCookieStore(reqCookieStore);
		}
		HttpResponse resp;
		CookieStore respCookieStore;
		//addAirtelHeader(httpPost);
		try {
			resp = httpClient.execute(httpPost);
			respCookieStore = httpClient.getCookieStore();
		} catch (ClientProtocolException e) {
			Log.e(TAG, "", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		} finally {
			//httpClient.getConnectionManager().shutdown();
		}
		int statusCode = resp.getStatusLine().getStatusCode();
		Log.i(TAG, "sendPostRequest() statusCode=" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			return new ResponseWrapper(resp, respCookieStore);
		}else{
			return null;
		}
	}
	
	
	private static DefaultHttpClient newDefaultHttpClient(){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		ConnManagerParams.setTimeout(params, 5000);
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 30000);
		params.setBooleanParameter("http.protocol.expect-continue", false);
		BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
        		Constants.CREDENTIALS_USER_NAME, Constants.CREDENTIALS_PASSWORD));
        httpClient.setCredentialsProvider(bcp);
        HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
		return httpClient;
	}
	
	private ZoneAccount initAccount(){
		ZoneAccountManager hikeAccountManager = new ZoneAccountManager(context);
		ZoneAccount account = hikeAccountManager.getAccount();
		final String passportCookieValue = (account!=null && account.getPassportCookieValue()!=null)?account.getPassportCookieValue():null;
		if(passportCookieValue!=null && !passportCookieValue.equals("")){
			return account;
		}else{
			return null;
		}
	}
	private static Header genPassportCookieHeader(String passport){
		Header header = new BasicHeader("Cookie",CookieUtil.crossCookieName("passport") + "=\"" + passport + "\"");
		return header;
	}
}
