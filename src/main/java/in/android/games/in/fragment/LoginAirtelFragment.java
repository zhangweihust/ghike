package in.android.games.in.fragment;

import in.android.games.in.activity.LoginActivity;
import in.android.games.in.activity.LoginActivity.InputValidator;
import in.android.games.in.client.Callback;
import in.android.games.in.client.ResponseWrapper;
import in.android.games.in.client.ServerSideErrorMsg;
import in.android.games.in.client.SignClient;
import in.android.games.in.common.Constants;
import in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginAirtelFragment extends Fragment {
	LoginActivity mActivity;
	
	private boolean inputLocked;
	private EditText nicknameEditText;
	private Button nextBtn;
	private TextView msgTextView1;
	private TextView msgTextView;
	private TextView msgTextView2;
	private Typeface typeFace; 

	//private String airtelNumber;

	public LoginAirtelFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		RuntimeLog.log("LoginAirtelFragment - onAttach");

		try {
			mActivity =  (LoginActivity)activity;
			//onGotoPageListener = (OnGotoPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGotoPageListener");
		}
	}
	
	// Called once the Fragment has been created in order for it to
	// create its user interface.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Create, or inflate the Fragment's UI, and return it.
		// If this Fragment has no UI then return null.
		View view = inflater.inflate(R.layout.fragment_airtelsignup, container, false);
		
        nicknameEditText = (EditText) view.findViewById(R.id.mSignUpAirtelNicknameEditText);
		nextBtn = (Button) view.findViewById(R.id.mSignUpAirtelNextBtn);
		msgTextView1 = (TextView) view.findViewById(R.id.mSignUpAirtelMsgText);
		msgTextView = (TextView) view.findViewById(R.id.mSignUpAirtelNumberText);
		msgTextView2 = (TextView) view.findViewById(R.id.mSignUpAirtelMsgText_1);
		typeFace = Typeface.createFromAsset(mActivity.getAssets(), "font/Edmondsans-Regular.otf");
		

	
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		//set typeface
		msgTextView1.setTypeface(typeFace);
		msgTextView.setTypeface(typeFace);
		msgTextView2.setTypeface(typeFace);
		
		//set listener
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lockInput();
				String nickname = nicknameEditText.getText().toString();
				String errMsg = InputValidator.validateNickname(nickname);
				if(errMsg==null){
					final int workingFlag = mActivity.userIntentFlag;
					SignClient.performSignUpNickname(nickname, null, mActivity.mHandler, new Callback() {
						public void call(ResponseWrapper resp) {
							if(workingFlag==mActivity.userIntentFlag){
								mActivity.handleNetworkError(resp);
								if(resp!=null && resp.isValid() && resp.isNoError()){
									String errMsg = ServerSideErrorMsg.getMsg(resp.getStatus());
									if(errMsg!=null){
										unLockInput(errMsg);
									}else{
										msgTextView2.setText(R.string.enteryournickname);
										msgTextView2.setTextColor(getResources().getColor(R.color.darkgray));
										mActivity.finishSignIn(resp, true);
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
		
		
		//show
		unLockInput(null);
		nicknameEditText.setText(null);
		if(mActivity.getNumber()!=null){
			msgTextView.setText("+91" + mActivity.getNumber());
		}
		mActivity.mUIHelper.disableBtn(nextBtn);
		mActivity.mUIHelper.showSoftInput();
	}
	
	@Override
	public void onStart(){
		super.onStart();
	}
	
/*	public void passAirtelPhoneNumber(String phonenumber){
		this.airtelNumber = phonenumber;
	}*/
	
	public void lockInput(){
		if(!inputLocked){
			nicknameEditText.setKeyListener(null);
			mActivity.mUIHelper.disableBtn(nextBtn);
			inputLocked = true;
		}
	}
	
	
	public void unLockInput(String errMsg){
		if(inputLocked){
			if(errMsg!=null){
				msgTextView2.setText(errMsg);
				msgTextView2.setTextColor(getResources().getColor(R.color.darkred));
			}else{
				mActivity.mUIHelper.enableBtn(nextBtn);
			}
			nicknameEditText.setKeyListener(InputValidator.KEY_LISTENER_NICKNAME);
			inputLocked = false;
		}
	}

	public boolean onShow(boolean isNew){
		if(isNew){
			if(mActivity.getNumber()==null){
				return false;
			}else{
				unLockInput(null);
				nicknameEditText.setText(null);
				msgTextView.setText("+91" + mActivity.getNumber());
				mActivity.mUIHelper.disableBtn(nextBtn);
				mActivity.mUIHelper.showSoftInput();
				return true;
			}
		}else{
			mActivity.mUIHelper.showSoftInput();
			return true;
		}
	}

/*	public void setAirtelNumber(String airtelNumber) {
		this.airtelNumber = airtelNumber;
	}*/


}