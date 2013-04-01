package in.android.games.in.fragment;

import in.android.games.in.activity.LoginActivity;
import in.android.games.in.common.Constants;
import in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class TermOfServiceFragment extends Fragment {
	
	LoginActivity mActivity;
	
    Typeface typeFace;
    private WebView mHikeWebView;
    private TextView mHeaderTextView;
    
	public TermOfServiceFragment() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		RuntimeLog.log("TermOfServiceFragment - onAttach");

		try {
			mActivity = (LoginActivity) activity;
			// onGotoPageListener = (OnGotoPageListener) activity;
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
		View view = inflater.inflate(R.layout.fragment_termofservice,
				container, false);

		typeFace = Typeface.createFromAsset(mActivity.getAssets(),
				"font/Edmondsans-Regular.otf");
		mHikeWebView = (WebView) view.findViewById(R.id.service_web_view);
		mHeaderTextView = (TextView) view
				.findViewById(R.id.TermofService_textview);
		


		return view;
	}
	
/*	@Override
	public void onSaveInstanceState(Bundle outState) {
	    //No call for super(). Bug on API Level > 11.
	}
	*/
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		mHeaderTextView.setTypeface(typeFace);
		mHikeWebView.loadUrl(Constants.ROOT_STATIC_DOMAIN+"/ui/tpl/termService.html");
	}
}
