package zonesdk.in.android.games.in.fragment;

import zonesdk.in.android.games.in.R;
import zonesdk.in.android.games.in.activity.LoginActivity;
import zonesdk.in.android.games.in.activity.LoginActivity.InputValidator;
import zonesdk.in.android.games.in.activity.LoginActivity.LoginTrace;

import zonesdk.in.android.games.in.client.Callback;
import zonesdk.in.android.games.in.client.ResponseWrapper;
import zonesdk.in.android.games.in.client.ServerSideErrorMsg;
import zonesdk.in.android.games.in.client.SignClient;
import zonesdk.in.android.games.in.utils.RuntimeLog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;

public class NoAirtelLoginPincodeFragment extends Fragment {

	LoginActivity mActivity;

	
	private  boolean inputLocked;
	private  AnimationDrawable verifyingSmsPinAnimation;
	private  EditText pinCodeEditText;
	private  Button nextBtn;
	private  TextView msgTextView;
	private  TextView msgTextView1;
	private  TextView tapTextView;
	Typeface typeFace;
	//public String phonenumber;
	//public String number;

	//private String number;
	//private SharedPreferences settings ;
	
	
	public NoAirtelLoginPincodeFragment() {
	}

	// Called once the Fragment has been created in order for it to
	// create its user interface.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create, or inflate the Fragment's UI, and return it.
		// If this Fragment has no UI then return null.
		View view = inflater.inflate(R.layout.fragment_signuppincode,
				container, false);
		

		pinCodeEditText =(EditText)view.findViewById(R.id.mSignUpPincodePincodeEditText);
		nextBtn =(Button)view.findViewById(R.id.mSignUpPincodeNextBtn);
		msgTextView =(TextView)view.findViewById(R.id.mSignUpPincodeTextView);
		msgTextView1 = (TextView)view.findViewById(R.id.mSignUpPincodeTextView1);
		tapTextView = (TextView)view.findViewById(R.id.tap);
		typeFace = Typeface.createFromAsset(mActivity.getAssets(),"font/Edmondsans-Regular.otf");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		RuntimeLog.log("SignInPincodeFragment - onActivityCreated");
		
		msgTextView.setTypeface(typeFace);
		msgTextView1.setTypeface(typeFace);
		
		tapTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//history.goBack();
				mActivity.goToPage(mActivity.LOGIN_VIEW_NOAIRTEL_LOGINNUM, false);
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
				final int workingFlag = mActivity.userIntentFlag;
				SignClient.performSignUp(mActivity.getNumber(), pinCode, mActivity.mHandler, new Callback() {
					public void call(ResponseWrapper resp) {
						if(workingFlag==mActivity.userIntentFlag){
							mActivity.handleNetworkError(resp);
							if(resp!=null && resp.isValid() && resp.isNoError()){
								String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
								if(errMsg!=null){
									unLockInput(errMsg);
								}else{
									String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
									
									//mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
									//LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
									//String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
									String loginNickNameTraceCheck = "number:"+mActivity.getNumber()+":"+"op:"+LoginTrace.OP_SIGNUP_NICKNAME;
									
									//let loginNickNameTraceCheck into sharePreference 				
									SharedPreferences.Editor localEditor = mActivity.settings.edit();
							        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
							        localEditor.putString("loginTrace", loginTrace);
							        localEditor.putString("phonenumber", mActivity.getNumber());
							        localEditor.putBoolean("loginIsPincodeCheck", true);
							        localEditor.commit();
							        
									//history.go(mNoAirtelLoginNicknameViewWrapper);
							        mActivity.goToPage(mActivity.LOGIN_VIEW_SIGNUP_NICK, false);
							        //mActivity.mSignupNicknameFragment.passPhoneNumberToNicknameView(number);
									//mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
							        //mActivity.setNumber(mActivity.getNumber());
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
					mActivity.mUIHelper.enableBtn(nextBtn);
				}else{
					mActivity.mUIHelper.disableBtn(nextBtn);
				}
			}
		});
		
		pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
		
		
		updatePinCode();
		
		mActivity.mUIHelper.showSoftInput();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		RuntimeLog.log("WelcomeFragment - onAttach");

		try {
			mActivity = (LoginActivity) activity;
			// onGotoPageListener = (OnGotoPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " ClassCastException");
		}
	}
	
/*	@Override
	public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("pincode", pinCodeEditText.getText().toString());

	}
	
	@Override
    public void onViewStateRestored(Bundle outState){
        //super.onViewStateRestored(outState);
        
		if(pinCodeEditText!=null){
			pinCodeEditText.setText(outState.getString("pincode"));
		}
    }*/
	

	private void lockInput(){
		if(!inputLocked){
			pinCodeEditText.setKeyListener(null);
			mActivity.mUIHelper.disableBtn(nextBtn);
			nextBtn.setText("");	
			nextBtn.setBackgroundResource(R.anim.verifying_sms_pin);
			verifyingSmsPinAnimation = (AnimationDrawable)nextBtn.getBackground();
			verifyingSmsPinAnimation.start();
			tapTextView.setClickable(false);
			inputLocked = true;
		}
	}
	
	private void unLockInput(String errMsg){

		if(inputLocked){
			if (verifyingSmsPinAnimation.isRunning()){
				mActivity.mUIHelper.disableBtn(nextBtn);
				verifyingSmsPinAnimation.stop();
				
			}
			if(errMsg!=null){
				msgTextView.setText(errMsg);
				msgTextView.setTextColor(getResources().getColor(R.color.darkred));
				msgTextView1.setVisibility(View.GONE);
			}else{
				mActivity.mUIHelper.enableBtn(nextBtn);
			}
			pinCodeEditText.setKeyListener(InputValidator.KEY_LISTENER_PINCODE);
			nextBtn.setText("Next");
			tapTextView.setClickable(true);
			inputLocked = false;
		}
	}
	
/*	public void setNumber(String number){
		this.number = number;
	}*/
	
	private void updatePinCode(){
		if(pinCodeEditText!=null){
			pinCodeEditText.setText(mActivity.getPinCode());
		}
		
	}
}
