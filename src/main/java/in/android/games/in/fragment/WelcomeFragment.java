package in.android.games.in.fragment;

import in.android.games.in.activity.LoginActivity;
import in.android.games.in.client.ResponseWrapper;
import in.android.games.in.common.Constants;
import in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.Fragment;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WelcomeFragment  extends Fragment {
	LoginActivity mActivity;
	//OnGotoPageListener onGotoPageListener;
	
	//UI components
	private String airtelNumber;
	private AnimationDrawable loadingAnimation;
	Typeface typeFace; 
	Typeface typeFace1;
	private Button mTermTextView;
	private Button welcomeAcceptBtn;
	private RelativeLayout mRelativeLayout;
	private ImageView welcomeView; 

	public WelcomeFragment() {
	}


	// Called once the Fragment has been created in order for it to
	// create its user interface.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RuntimeLog.log("WelcomeFragment - onCreateView");
		// Create, or inflate the Fragment's UI, and return it.
		// If this Fragment has no UI then return null.
		View view =  inflater.inflate(R.layout.fragment_welcome, container, false);
		//view.setId(0x7F04FFF0);
		typeFace = Typeface.createFromAsset(mActivity.getAssets(),"font/Edmondsans-Regular.otf");
		typeFace1 = Typeface.createFromAsset(mActivity.getAssets(), "font/HelveticaNeue-Roman.otf");
		mTermTextView = (Button)view.findViewById(R.id.mWelcomeTermOfUseImageView);
		welcomeAcceptBtn =(Button)view.findViewById(R.id.mWelcomeAcceptBtn);
		welcomeView = (ImageView)view.findViewById(R.id.login_welcome_image);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		RuntimeLog.log("WelcomeFragment - onActivityCreated");
		welcomeAcceptBtn.setTypeface(typeFace1);
		
		mTermTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				mActivity.goToPage(mActivity.LOGIN_VIEW_TERMOFSERVICE, true);
				
			}
		});
	
		welcomeAcceptBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				lockInput();
				mActivity.checkUser();

			}
		});
	}

/*	@Override
	public void onSaveInstanceState(Bundle outState) {
	    //No call for super(). Bug on API Level > 11.
	}*/
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		RuntimeLog.log("WelcomeFragment - onAttach");

		try {
			mActivity =  (LoginActivity)activity;
			//onGotoPageListener = (OnGotoPageListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnGotoPageListener");
		}
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
}
