package in.android.games.in.receiver;

import in.android.games.in.service.GameCheckService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GameCheckReceiver extends BroadcastReceiver {

  public static final String ACTION_REFRESH_GAMECHECK_ALARM = "zonesdk.in.android.games.in.receiver.ACTION_REFRESH_GAMECHECK_ALARM";
	
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent startIntent = new Intent(context, GameCheckService.class);
    context.startService(startIntent);
  }
}