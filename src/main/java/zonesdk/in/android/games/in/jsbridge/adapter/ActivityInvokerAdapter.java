package zonesdk.in.android.games.in.jsbridge.adapter;

import zonesdk.in.android.games.in.activity.ContactActivity;
import zonesdk.in.android.games.in.jsbridge.ActivityInvoker;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.app.Activity;
import android.content.Intent;

public class ActivityInvokerAdapter extends BaseAdapter implements ActivityInvoker {

	public ActivityInvokerAdapter(Activity activity){
		super(activity);
	}
	
	@Override
	public void startContactActivity(){
		Activity act = getActivity();
		Intent intent = new Intent();
		intent.setClass(act, ContactActivity.class);
		act.startActivity(intent);
	}

	@Override
	public void finish(){
		getActivity().finish();
	}
	
}
