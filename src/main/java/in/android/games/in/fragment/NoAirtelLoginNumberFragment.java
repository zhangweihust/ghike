package in.android.games.in.fragment;

import in.android.games.in.activity.LoginActivity;
import in.android.games.in.activity.LoginActivity.InputValidator;
import in.android.games.in.activity.LoginActivity.LoginTrace;
import in.android.games.in.activity.LoginActivity.UserType;
import in.android.games.in.client.Callback;
import in.android.games.in.client.ResponseWrapper;
import in.android.games.in.client.ServerSideErrorMsg;
import in.android.games.in.client.SignClient;
import in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
//import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;

public class NoAirtelLoginNumberFragment extends Fragment {
	LoginActivity mActivity;
	
	private   EditText numberEditText;
	private  Button nextBtn;
	//private  ImageButton helpBtn;
	public  TextView msgTextView;
	public  TextView msgTextView1;
	private Typeface typeface; 

	private String number;
	private boolean inputLocked;
	private AnimationDrawable verifyingPhoneNumberAnimation;
	
	//private SharedPreferences settings ;
	//private String loginTrace;
	
	public NoAirtelLoginNumberFragment() {
	}

	// Called once the Fragment has been created in order for it to
	// create its user interface.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create, or inflate the Fragment's UI, and return it.
		// If this Fragment has no UI then return null.
		View view = inflater.inflate(R.layout.fragment_noairtelloginnumber,
				container, false);
		
		numberEditText =(EditText)view.findViewById(R.id.mSignUpNumberNumberEditText);
		nextBtn =(Button)view.findViewById(R.id.mSignUpNumberNextBtn);
		//helpBtn =(ImageButton)view.findViewById(R.id.mSignUpNumberHelpBtn);
		msgTextView =(TextView)view.findViewById(R.id.mSignUpNumberTextView);
		msgTextView1 = (TextView)view.findViewById(R.id.mSignUpNumberTextView1);
		Typeface.createFromAsset(mActivity.getAssets(), "font/Edmondsans-Regular.otf");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		RuntimeLog.log("NoAirtelLoginNumberFragment - onActivityCreated");
		
		msgTextView.setTypeface(typeface);
		msgTextView1.setTypeface(typeface);
		
/*		helpBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//history.go(mHelpViewWrapper);
			}
		});*/
		
		nextBtn.setBackgroundResource(R.drawable.btn_bg_disable);
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				/////////////////////
				lockInput();
				number = numberEditText.getText().toString();
				String errMsg = InputValidator.validateNumber(number);
				if(errMsg==null){
					final int workingFlag = mActivity.userIntentFlag;
					SignClient.performGetSignUpCode(number, mActivity.mHandler, new Callback() {
						@Override
						public void call(ResponseWrapper resp) {
							if(workingFlag==mActivity.userIntentFlag){
								mActivity.handleNetworkError(resp);
								if(resp!=null && resp.isValid() && resp.isNoError()){
									String errMsg =  ServerSideErrorMsg.getMsg(resp.getStatus());
									//the number is occupyed
									RuntimeLog.log("performGetSignUpCode - resp.getStatus() = " +resp.getStatus());
									if (resp.getStatus() == 201){
										//registered and nickname ok before
										mActivity.userType = UserType.REGISTED;
										RuntimeLog.log("201 registered and nickname ok before, set userType = UserType.REGISTED");

										//registered and nickname ok before
										SignClient.performGetSignInCode(number, mActivity.mHandler, new Callback() {
											@Override
											public void call(ResponseWrapper resp) {
												if(workingFlag==mActivity.userIntentFlag){
													mActivity.handleNetworkError(resp);
													if(resp!=null && resp.isValid() && resp.isNoError()){
														String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
														if(resp.getStatus()==203){
															
															errMsg = "You don't have a gHike account, yet!";
														}
														if(errMsg!=null){
															unLockInput(errMsg);

															
														}else{
															mActivity.mHandler.sendEmptyMessage(LoginActivity.PULL_PIN);
															
															//set the pull pin time out event
															mActivity.mHandler.sendEmptyMessageDelayed(LoginActivity.PULL_MST, mActivity.PULL_PIN_DELAY*2/3);
															mActivity.mHandler.sendEmptyMessageDelayed(LoginActivity.PULL_OUT, mActivity.PULL_PIN_DELAY);
															
															//mActivity.mSignInPincodeFragment.setNumber(number);
															mActivity.setNumber(number);
															//mSignInPincodeViewWrapper.setNumber(number);
															
															//send ok
															String pinCode = resp.getStringFromResp("pinCode");
															//tel server to give me pincode, at this, server return ok, then:
															//set sms capture, and set timer to rm it 60 sec later
															//captureSMS(); 
															//pinCode = null; //++++++++++++
															if(pinCode!=null && !pinCode.trim().equals("")){
																//for emulate user: the server return pin through network																	
																//mActivity.mSignInPincodeFragment.setPinCode(pinCode);
																mActivity.setPinCode(pinCode);
																Message msg = new Message();
																msg.what = LoginActivity.SMS_RCVD;
																msg.obj = pinCode;
																mActivity.mHandler.sendMessage(msg);
															}else{
																//set the pull pin time out event
																//mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
																//the real user, user local receiver to listen the sms
																
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
										
									}
									else{
										//not registered , need  fill nickname
										if(errMsg!=null){
											unLockInput(errMsg);
										}else{
											//final String pinCode = resp.getStringFromResp("pinCode");
											
											mActivity.userType = UserType.NOT_REGISTED;
											RuntimeLog.log("not(201)registered , need  fill nickname, set userType = UserType.NOT_REGISTED ");
											
											//send ok
											String pinCode = resp.getStringFromResp("pinCode");

											//tel server to give me pincode, at this, server return ok, then:
											//set sms capture, and set timer to rm it 60 sec later
											
											mActivity.mHandler.sendEmptyMessage(LoginActivity.PULL_PIN);
											mActivity.mHandler.sendEmptyMessageDelayed(LoginActivity.PULL_OUT, mActivity.PULL_PIN_DELAY); 
											mActivity.mHandler.sendEmptyMessageDelayed(LoginActivity.PULL_MST, mActivity.PULL_PIN_DELAY*2/3);
											
											//pinCode = null; //+++++++++++  emulate real user
											//mNoAirtelLoginPincodeViewWrapper.setNumber(number);
											//mSignInPincodeViewWrapper.setNumber(number);
											//mActivity.mSignInPincodeFragment.setNumber(number);
											mActivity.setNumber(number);
											
											if(pinCode!=null && !pinCode.trim().equals("")){
												//for emulate user: the server return pin through network																	
												//mActivity.mSignInPincodeFragment.setPinCode(pinCode);
												mActivity.setPinCode(pinCode);
												//mSignInPincodeViewWrapper.setNumber(number);
												
												Message msg = new Message();
												msg.what = LoginActivity.SMS_RCVD;
												msg.obj = pinCode;
												mActivity.mHandler.sendMessage(msg);

											}else{
												//set the pull pin time out event
												//mHandler.sendEmptyMessageDelayed(PULL_OUT, PULL_PIN_DELAY);
												//the real user, user local receiver to listen the sms
												
											}
											

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
		
		mActivity.mUIHelper.showSoftInput();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		RuntimeLog.log("NoAirtelLoginNumberFragment - onAttach");

		try {
			mActivity = (LoginActivity) activity;
			// onGotoPageListener = (OnGotoPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "ClassCastException");
		}
	}
	
	
	public boolean islock(){
		return inputLocked;
	}
	
	
	public void changeAnimation(int ResID){
		if(verifyingPhoneNumberAnimation.isRunning()){			
			verifyingPhoneNumberAnimation.stop();
			nextBtn.setBackgroundResource(ResID);
			verifyingPhoneNumberAnimation = (AnimationDrawable)nextBtn.getBackground();
			verifyingPhoneNumberAnimation.start();
			
		}

	}
	
	public void lockInput(){
		if(!inputLocked){
			numberEditText.setKeyListener(null);
			//termCheckBox.setClickable(false);
			mActivity.mUIHelper.disableBtn(nextBtn);
			mActivity.mUIHelper.hideSoftInput();
			nextBtn.setText("");	
			nextBtn.setBackgroundResource(R.anim.verifying_phonenumber);
			verifyingPhoneNumberAnimation = (AnimationDrawable)nextBtn.getBackground();
			verifyingPhoneNumberAnimation.start();
			//helpBtn.setClickable(false);
			inputLocked = true;
		}
	}
	

	
	public void unLockInput(String errMsg){
		if(inputLocked){
			if (verifyingPhoneNumberAnimation.isRunning()){
				verifyingPhoneNumberAnimation.stop();
				mActivity.mUIHelper.disableBtn(nextBtn);
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
				mActivity.mUIHelper.enableBtn(nextBtn);
			}
			numberEditText.setKeyListener(InputValidator.KEY_LISTENER_PHONENUMBER);
			//termCheckBox.setClickable(true);
			nextBtn.setText("Next");
			//helpBtn.setClickable(true);
			inputLocked = false;
			mActivity.mUIHelper.showSoftInput();
		}
	}
	
	public void toggleBtn(Button nextBtn, EditText numberEditText){
		boolean charCountZero = (numberEditText.getText().toString().length()==0);
		if(!charCountZero){
			mActivity.mUIHelper.enableBtn(nextBtn);
		}else{
			mActivity.mUIHelper.disableBtn(nextBtn);
		}
	}
	
	
/*	public void autoSignUp(String pinCode){
		if(history.isCurrent(this)){
<<<<<<< OURS
			mSignUpPincodeViewWrapper.setNumber(number);
			mSignUpPincodeViewWrapper.setPinCode(pinCode);
			history.go(mSignUpPincodeViewWrapper);
=======
			mNoAirtelLoginPincodeViewWrapper.setNumber(number);
			history.go(mNoAirtelLoginPincodeViewWrapper);
		}
	}*/
	
	//for registered user
	public void autoLogin(final String pinCode){
		lockInput();
//		String pinCode = pinCodeEditText.getText().toString();
		final int workingFlag = mActivity.userIntentFlag;
		SignClient.performSignIn(number, pinCode, mActivity.mHandler, new Callback() {
			@Override
			public void call(ResponseWrapper resp) {
				if(workingFlag==mActivity.userIntentFlag){
					mActivity.handleNetworkError(resp);
					if(resp!=null && resp.isValid() && resp.isNoError()){
						String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
						if(errMsg!=null){
							RuntimeLog.log("autoLogin - performSignIn - errMsg:" + errMsg);
							unLockInput(null);
							//history.go(mSignInPincodeViewWrapper);
							//mSignInPincodeViewWrapper.unLockInput(errMsg);
							mActivity.goToPage(mActivity.LOGIN_VIEW_SIGNIN_PIN, false);
							mActivity.mSignInPincodeFragment.unLockInput(errMsg);
							
							
						}else{
							mActivity.finishSignIn(resp, false);
							//  clean  loginstraceTickeCheck from  sharepre
							SharedPreferences.Editor localEditor = mActivity.settings.edit();
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
	
	public void autoJumpToNickName(final String Pincode){
		lockInput();
		//String Pincode = pinCodeEditText.getText().toString();
		final int workingFlag = mActivity.userIntentFlag;
		SignClient.performSignUp(number, Pincode, mActivity.mHandler, new Callback() {
			@Override
			public void call(ResponseWrapper resp) {
				if(workingFlag==mActivity.userIntentFlag){
					mActivity.handleNetworkError(resp);
					if(resp!=null && resp.isValid() && resp.isNoError()){
						String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
						if(errMsg!=null){
							//unLockInput(errMsg);
							unLockInput(null);
							//history.go(mSignInPincodeViewWrapper);
							//mSignInPincodeViewWrapper.unLockInput(errMsg);
							mActivity.goToPage(mActivity.LOGIN_VIEW_SIGNIN_PIN, false);
							mActivity.mSignInPincodeFragment.unLockInput(errMsg);
						}else{
							String loginTrace = resp.getStringFromCookieCrossDomain("logintrace");
							
							//mNoAirtelLoginNicknameViewWrapper.setLoginTrace(loginTrace);
							//setLoginTrace(loginTrace);
							//LoginTrace loginNickNameTrace = new LoginTrace(number, LoginTrace.OP_SIGNUP_NICKNAME);
							//String loginNickNameTraceCheck = loginNickNameTrace.getcheck_op();
							String loginNickNameTraceCheck = "number:"+number+":"+"op:"+LoginTrace.OP_SIGNUP_NICKNAME;
							//let loginNickNameTraceCheck into sharePreference 				
							SharedPreferences.Editor localEditor = mActivity.settings.edit();
					        localEditor.putString("loginNickNameTraceCheck", loginNickNameTraceCheck);
					        localEditor.putString("loginTrace", loginTrace);
					        localEditor.putString("phonenumber", number);
					        localEditor.putBoolean("loginIsPincodeCheck", true);
					        localEditor.commit();
					        
							//history.go(mNoAirtelLoginNicknameViewWrapper);
							//mNoAirtelLoginNicknameViewWrapper.passPhoneNumberToNicknameView(number);
							mActivity.goToPage(mActivity.LOGIN_VIEW_SIGNUP_NICK, false);
							//mActivity.mSignupNicknameFragment.passPhoneNumberToNicknameView(number);
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
	
/*	public void setLoginTrace(String loginTrace) {
		this.loginTrace = loginTrace;
	}*/

}
