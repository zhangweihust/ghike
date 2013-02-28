package zonesdk.in.android.games.in.fragment;

import zonesdk.in.android.games.in.R;
import zonesdk.in.android.games.in.activity.LoginActivity;
import zonesdk.in.android.games.in.activity.LoginActivity.InputValidator;
import zonesdk.in.android.games.in.client.Callback;
import zonesdk.in.android.games.in.client.ResponseWrapper;
import zonesdk.in.android.games.in.client.ServerSideErrorMsg;
import zonesdk.in.android.games.in.client.SignClient;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

public class SignupNicknameFragment extends Fragment {

	LoginActivity mActivity;
	
	private boolean inputLocked;
	//public String number;
	private  EditText nicknameEditText;
	private  Button nextBtn;
	private  TextView msgTextView;
	private  TextView phoneNumber;
	private  TextView whatisyourname;

	Typeface typeFace;
	
	private String loginTrace;

	public SignupNicknameFragment() {
	}

	// Called once the Fragment has been created in order for it to
	// create its user interface.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create, or inflate the Fragment's UI, and return it.
		// If this Fragment has no UI then return null.
		View view = inflater.inflate(R.layout.fragment_signupnickname, container,
				false);
		nicknameEditText =(EditText)view.findViewById(R.id.mSignUpNicknameNicknameEditText);
		nextBtn =(Button)view.findViewById(R.id.mSignUpNicknameNextBtn);
		msgTextView =(TextView)view.findViewById(R.id.mSignUpNicknameTextView);
		phoneNumber = (TextView)view.findViewById(R.id.mSignUpNicknameTextView1);
		whatisyourname = (TextView)view.findViewById(R.id.mSignUpNicknameTextView2);

		typeFace = Typeface.createFromAsset(mActivity.getAssets(),"font/Edmondsans-Regular.otf");
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		RuntimeLog.log("SignupNicknameFragment - onActivityCreated");
		
		msgTextView.setTypeface(typeFace);
		phoneNumber.setTypeface(typeFace);  
		whatisyourname.setTypeface(typeFace);
		// setting loginTrace
		SharedPreferences setting = mActivity.getSharedPreferences("loginXML", 0);
		loginTrace = setting.getString("loginTrace", null);
		
		
		String phonenumber = mActivity.getNumber();
		if (phonenumber == null || phonenumber.length() <=0){
			phonenumber = setting.getString("phonenumber", null);
			
		}
		
		if (phonenumber != null && phonenumber.length() >0){
			String front = phonenumber.substring(0, 3);
			String middle = phonenumber.substring(3,6);
			String last = phonenumber.substring(6);
			phoneNumber.setText("+91"+front+"-"+middle+"-"+last);
		}
		
		nextBtn.setBackgroundResource(R.drawable.btn_bg_disable);
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lockInput();
				String nickname = nicknameEditText.getText().toString();
				String errMsg = InputValidator.validateNickname(nickname);
				if(errMsg==null){
					final int workingFlag = mActivity.userIntentFlag;					
					SignClient.performSignUpNickname(nickname, loginTrace, mActivity.mHandler, new Callback() {
						public void call(ResponseWrapper resp) {
							if(workingFlag==mActivity.userIntentFlag){
								mActivity.handleNetworkError(resp);
								if(resp!=null && resp.isValid() && resp.isNoError()){
									String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
									if(errMsg!=null){
										unLockInput(errMsg);
									}else{
										mActivity.finishSignIn(resp, false);
										//  clean LogintaceTiketCheck  from  sharepre
										SharedPreferences.Editor localEditor = mActivity.settings.edit();
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
				    mActivity.mUIHelper.enableBtn(nextBtn);
				}else{
					mActivity.mUIHelper.disableBtn(nextBtn);
				}
			}
		});
		
		
		nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
		
		mActivity.mUIHelper.showSoftInput();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		RuntimeLog.log("SignupNicknameFragment - onAttach");

		try {
			mActivity = (LoginActivity) activity;
			// onGotoPageListener = (OnGotoPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " ClassCastException");
		}
	}
	
	
	private void lockInput(){
		if(!inputLocked){
			nicknameEditText.setKeyListener(null);
			mActivity.mUIHelper.disableBtn(nextBtn);
			mActivity.mUIHelper.hideSoftInput();
			inputLocked = true;
			
		}
	}
	
/*	public void passPhoneNumberToNicknameView(String number){
		this.phonenumber = number;
		String front = phonenumber.substring(0, 3);
		String middle = phonenumber.substring(3,6);
		String last = phonenumber.substring(6);
		phoneNumber.setText("+91"+front+"-"+middle+"-"+last);
		
	}*/
	
	private void unLockInput(String errMsg){
		if(inputLocked){
			if(errMsg!=null){
				msgTextView.setText(errMsg);
				msgTextView.setTextColor(getResources().getColor(R.color.darkred));
				phoneNumber.setVisibility(View.GONE);
				whatisyourname.setVisibility(View.GONE);
				mActivity.mUIHelper.showSoftInput();
				
			}else{
				mActivity.mUIHelper.enableBtn(nextBtn);
			}
			nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
			inputLocked = false;
		}
	}
	
	public void setLoginTrace(String loginTrace) {
		this.loginTrace = loginTrace;
	}
}
