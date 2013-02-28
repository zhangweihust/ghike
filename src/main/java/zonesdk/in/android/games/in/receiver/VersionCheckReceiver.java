package zonesdk.in.android.games.in.receiver;

import zonesdk.in.android.games.in.service.VersionCheckService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VersionCheckReceiver extends BroadcastReceiver {

  public static final String ACTION_REFRESH_VERSIONCHECK_ALARM = "zonesdk.in.android.games.in.receiver.ACTION_REFRESH_VERSIONCHECK_ALARM";
	
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent startIntent = new Intent(context, VersionCheckService.class);
    context.startService(startIntent);
  }
}