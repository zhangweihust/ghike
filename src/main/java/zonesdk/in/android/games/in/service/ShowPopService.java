package zonesdk.in.android.games.in.service;



import java.io.IOException;
import java.util.List;
import java.util.Map;

import zonesdk.in.android.games.in.activity.ContentEditorActivity;
import zonesdk.in.android.games.in.activity.HikeMainActivity;
import zonesdk.in.android.games.in.db.GameHelper;
import zonesdk.in.android.games.in.proxy.GameClient;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.widget.URLImageView;
import zonesdk.in.android.games.in.R;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class ShowPopService extends BaseService {
	private Context context;
	private Intent serviceIntent = null;
	private WindowManager wm = null;
	private WindowManager.LayoutParams wmParams = null;
	private ActivityManager mActivityManager;
	private Button toMyPage;
	private View myFV;
	private static final int CREATE_VIEW = 1001;
	private static final int SHOW_VIEW = 1002;
	private static final int HIDE_VIEW = 1003;
	private static final int REMOVE_VIEW = 1004;
	
	public static ShowPopService instance;
	
	private GameHelper mGameHelper;
	
	private String topPackageName = "";
	
	private String gamePackageName = "";

	{
		instance = this;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		RuntimeLog.log("ShowPopService.onBind()");
		
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		RuntimeLog.log("ShowPopService.onCreate()");
		
		context = getApplicationContext();
		mGameHelper = new GameHelper(context);
		listenActivity();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		RuntimeLog.log("ShowPopService.onDestroy()");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		RuntimeLog.log("ShowPopService.onStart()");
		
	}
	

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CREATE_VIEW:
				createView("Comment");
				break;
			case REMOVE_VIEW:
				removeView();
				break;
			case SHOW_VIEW:
				showView();
				break;
			case HIDE_VIEW:
				hideView();
				break;
			}
		}
	};

	private void removeView() {
		RuntimeLog.log("ShowPopService.removeView");
		if (myFV != null && myFV.getParent() != null ) {
			wm.removeView(myFV);
		}
		myFV = null;
	}
	
	private void showView() {
		RuntimeLog.log("ShowPopService.showView");
		if (myFV != null && myFV.getParent() == null) {
			wm.addView(myFV, wmParams);
		}
		mHandler.sendEmptyMessageDelayed(HIDE_VIEW, 5000);
	}
	
	private void hideView() {
		RuntimeLog.log("ShowPopService.hideView");
		if (myFV != null && myFV.getParent() != null ) {
			wm.removeView(myFV);
		}
	}

	public void createView(String text) {
		RuntimeLog.log("ShowPopService.createView - text:" + text);
		initPopView(text);
		if (mHandler.hasMessages(HIDE_VIEW)) {
			mHandler.removeMessages(HIDE_VIEW);
		}
		mHandler.sendEmptyMessageDelayed(HIDE_VIEW, 5000);
	}

	private void initPopView(final String text) {
		//RuntimeLog.log("ShowPopService.initPopView - text:" + text);
		removeView();
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = LayoutParams.TYPE_PHONE;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = 40;
		wmParams.gravity = Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.height=android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		myFV = LayoutInflater.from(this).inflate(R.layout.float_view, null);

		String headUrl = this.getAccount().getHeadUrl();
		TextView userNameText = (TextView) myFV.findViewById(R.id.float_txt);

		URLImageView userPhoto = (URLImageView) myFV.findViewById(R.id.float_img);
		//userNameText.setText("Comment".equals(text)?userName:"Commented");
		if("Comment".equals(text)){
			userNameText.setText("Play Shared!");
		}
		if("View All".equals(text)){
			userNameText.setText("Commented!");
		}
//		http://devestatic.ghike.in/ui/img/touch/male110.png
//		Bitmap userAvatar = AppApplication.getInstance().getUserAvatarBitmap();
//		userPhoto.setImageBitmap(userAvatar);


		userPhoto.loadFromUrl(headUrl);
		toMyPage = (Button) myFV.findViewById(R.id.to_my_page);
		toMyPage.setText(text);
		toMyPage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent;
				switch (v.getId()) {
				case R.id.to_my_page:
					if("Comment".equals(text)){

						int height = wm.getDefaultDisplay().getHeight();
						int width = wm.getDefaultDisplay().getWidth();
				        boolean isportrait = true;
				        if(height < width){
				        	isportrait = false;
				        } else {
				        	isportrait = true;
				        }
						
						intent = new Intent(ShowPopService.this,
								ContentEditorActivity.class);
						intent.putExtra("title", "Comment");
						GameHelper GameData = new GameHelper(context);
						Map<String,Object> data = GameData.getGameInfoByPackageName(topPackageName);
						if(data == null)
							break;
						int gameId = (Integer) data.get("appId");
						int forumId = (Integer) data.get("forumId");
						String title = (String) data.get("title");
						intent.putExtra("gameId", gameId) ;
						intent.putExtra("forumId", forumId) ;
						intent.putExtra("gameName", title) ;
						intent.putExtra("isportrait", isportrait) ;
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						RuntimeLog.log("ShowPopService.Button.onClick::startActivity - Comment");
						startActivity(intent);
						hideView();
					} else if("View All".equals(text)){
						intent = new Intent(ShowPopService.this,
								HikeMainActivity.class);
						intent.setAction(HikeMainActivity.ACTION_VIEW_GAME);
						GameHelper GameData = new GameHelper(context);
						Map<String,Object> data = GameData.getGameInfoByPackageName(topPackageName);
						if(data == null)
							break;
						int gameId = (Integer) data.get("appId");
						intent.putExtra("gameId", gameId) ;
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						RuntimeLog.log("ShowPopService.Button.onClick::startActivity - View All");
						startActivity(intent);
						hideView();
					}
					
					break;
				default:
					break;
				}

			}
		});
		wm.addView(myFV, wmParams);
	}
	
	private void listenActivity() {
		ActivityListenerThread activityListenerThread = new ActivityListenerThread();
		activityListenerThread.startListener();
		activityListenerThread.start();
	}
	private long lastReport = 0L;
	private class ActivityListenerThread extends Thread {

		private ActivityListenerThread() {
			super("ActivityMonitorThread");
			lastReport = System.currentTimeMillis();
		}


		volatile private boolean isRunning = true;
		public void shutdownListener() {
			isRunning = false;
		}

		public void startListener() {
			isRunning = true;
		}

		@Override
		public void run() {
			List<RunningTaskInfo> taskInfos;
			while (isRunning) {
				try {
					sleep(100);
					lastReport = System.currentTimeMillis();
					mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					taskInfos = mActivityManager.getRunningTasks(1);
					if(taskInfos == null || taskInfos.size() == 0){
						mHandler.sendEmptyMessage(REMOVE_VIEW);
						continue;
					}
					String packageName = taskInfos.get(0).topActivity
							.getPackageName();
					if(packageName.equals(topPackageName)){
						if(mGameHelper.getGameInfoByPackageName(topPackageName) == null && 
							!context.getApplicationInfo().packageName.equals(topPackageName)){
							gamePackageName = "";
						}
						continue;
					}
					topPackageName = packageName;
					if(context.getApplicationInfo().packageName.equals(topPackageName)){	// jump to hike platform
						mHandler.sendEmptyMessage(HIDE_VIEW);
						continue;
					}
					Map<String,Object> data = mGameHelper.getGameInfoByPackageName(topPackageName);
					if(data == null){	// is not a game
						mHandler.sendEmptyMessage(REMOVE_VIEW);
						gamePackageName = "";
						continue;
					}
					String downloadUrl = (String)data.get("downloadUrl");
					if(downloadUrl != null && downloadUrl.indexOf("market://") == -1){	// is hike android game
						mHandler.sendEmptyMessage(REMOVE_VIEW);
						gamePackageName = "";
						int gameId = (Integer) data.get("appId");
						GameClient client = getProxy(GameClient.class);
						try {
							client.playGame(gameId);
						} catch (IOException e) {
							e.printStackTrace();
						}
						continue;
					}
					if(gamePackageName.equals(topPackageName)){	//just show the origin view
						gamePackageName = topPackageName;
						mHandler.sendEmptyMessage(SHOW_VIEW);
					}else{
						gamePackageName = topPackageName;
						mHandler.sendEmptyMessage(CREATE_VIEW);
						int gameId = (Integer) data.get("appId");
						SendToService sendToService = new SendToService(gameId);
						sendToService.start();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SendToService extends Thread {
		int gameId;
		public SendToService(int gameId){
			this.gameId = gameId;
		}

		@Override
		public void run() {
			super.run();
			RuntimeLog.log("ShowPopService.SendToService.run - gameId:" + String.valueOf(gameId) );
			GameClient client = getProxy(GameClient.class);
			try {
				client.playGame(gameId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
