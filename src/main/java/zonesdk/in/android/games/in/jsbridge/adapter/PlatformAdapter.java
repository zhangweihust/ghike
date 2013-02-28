package zonesdk.in.android.games.in.jsbridge.adapter;

import android.app.Activity;
import zonesdk.in.android.games.in.jsbridge.Platform;
import zonesdk.in.android.games.in.update.UpdateManager;

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
