package zonesdk.in.android.games.in.jsbridge.adapter;

import android.app.Activity;

public abstract class BaseAdapter {

	private Activity mActivity;
	
	public BaseAdapter(Activity activity){
		mActivity = activity;
	}
	
	protected final Activity getActivity(){
		return mActivity;
	}
	
	public void onDestroy(){
		mActivity = null;
	}
	
}
