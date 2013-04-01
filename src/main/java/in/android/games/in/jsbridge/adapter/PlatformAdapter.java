package in.android.games.in.jsbridge.adapter;

import in.android.games.in.jsbridge.Platform;
import in.android.games.in.update.UpdateManager;
import android.app.Activity;

public class PlatformAdapter extends BaseAdapter implements Platform {

	private UpdateManager mManager;
	
	public PlatformAdapter(Activity activity, UpdateManager manager) {
		super(activity);
		mManager = manager;
	}

	@Override
	public int getVersionCode() {
		return mManager.getVersionCode();
	}

	@Override
	public String getVersionName() {
		return mManager.getVersionName();
	}

	@Override
	public void requestCheckUpdatesDialog(boolean forceCheck) {
		mManager.requestCheckUpdatesDialog(forceCheck);
	}

}
