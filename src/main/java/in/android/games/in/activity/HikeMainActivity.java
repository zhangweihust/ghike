package in.android.games.in.activity;

import in.android.games.in.account.ZoneAccount;
import in.android.games.in.common.Constants;
import in.android.games.in.dialog.PaymentDialog;
import in.android.games.in.dialog.PromptDialog;
import in.android.games.in.dialog.PromptDialog.Builder;
import in.android.games.in.dialog.PromptDialog.OnConfirmListener;
import in.android.games.in.jsbridge.ImageUploader;
import in.android.games.in.jsbridge.PaymentDialogProvider;
import in.android.games.in.jsbridge.adapter.ActivityInvokerAdapter;
import in.android.games.in.jsbridge.adapter.AppManagerAdapter;
import in.android.games.in.jsbridge.adapter.LocalGamesAdapter;
import in.android.games.in.jsbridge.adapter.NativeUIAdapter;
import in.android.games.in.jsbridge.adapter.PlatformAdapter;
import in.android.games.in.jsbridge.adapter.TrackerAdapter;
import in.android.games.in.service.GameCheckService;
import in.android.games.in.service.SMSReceiverService;
import in.android.games.in.service.ShowPopService;
import in.android.games.in.service.VersionCheckService;
import in.android.games.in.update.UpdateManager;
import in.android.games.in.utils.ImageUtil;
import in.android.games.in.utils.RuntimeLog;
import in.android.games.in.widget.HikeWebView;
import in.android.games.in.widget.MainViewport;
import in.android.games.in.widget.HikeWebView.ContentEditProvider;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;
import zonesdk.in.android.games.in.R;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.os.Handler;
import android.os.Message;

import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import android.widget.Toast;

public class HikeMainActivity extends LoginRequiredActivity implements 
		ContentEditProvider, ImageUploader, PaymentDialogProvider {
	
	private MainViewport viewport;
	
	private HikeWebView webView;
	
	private ValueCallback<Uri> uploadCallback;

	private ValueCallback<String> editCallback;
	
	private PaymentDialog paymentDialog;
	
	private static final int CALLBACK_CHOOSE_FILE = 201;
	
	private static final int CALLBACK_GAME = 202;
	
	private static final int CALLBACK_EDIT_CONTENT = 203;
	
	private static final int CALLBACK_CHOOSE_IMAGE = 204;
	
	private static final int CALLBACK_UPLOAD_FINISH = 205;
	
	public static final String ACTION_VIEW_GAME = "openDetail";
	
	public static final String ACTION_GOHOME = "openHome";
	
	public static final int HANDLER_CLOSEAPP = 300;

	private AppManagerAdapter mAppManagerAdapter;
	
	private NativeUIAdapter mNativeUIAdapter;
	
	private ActivityInvokerAdapter mActivityInvokerAdapter;
	
	private LocalGamesAdapter mLocalGamesAdapter;
	
	private TrackerAdapter mTrackerAdapter;
	
	private UpdateManager mUpdateManager;
	
	Timer promttimer = null;
	
	private  MyHandler mHandler;
	
	private PlatformAdapter mPlatformAdapter;
	
	
	/**
	 * @author zhangwei
	 * This Handler class should be static or leaks might occur
	 */
	
	static class MyHandler extends Handler{ 


		WeakReference<HikeMainActivity> wActivity;

        MyHandler(HikeMainActivity activity) {
        	wActivity = new WeakReference<HikeMainActivity>(activity);
        }

        @Override 
        public void handleMessage(Message msg) { 
        	HikeMainActivity theActivity = wActivity.get();
            //super.handleMessage(msg); 
        	RuntimeLog.log("(del)handleMessage - msg.what:" + msg.what);
        	
        	if(msg.what == HANDLER_CLOSEAPP){
        		//theActivity.closeApp();
        	}
        	switch(msg.what){
        	
                default:
                	RuntimeLog.log("case default - msg.what:" + msg.what);
                	break;
        	}

        } 
    };
    
    @Override
	protected void onCreateForUpdates(){

    }
	
    @Override
	protected void onCreateWithAccount(Bundle savedInstanceState, ZoneAccount account) {
		super.onCreateWithAccount(savedInstanceState, account);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//checkVersion();
		setContentView(R.layout.hike_main);
		mHandler = new MyHandler(this);
		mUpdateManager = new UpdateManager(this, mHandler);
		//mUpdateManager.ForceCheckUpdates();

		
		startService(new Intent(this, SMSReceiverService.class));

		viewport = (MainViewport) findViewById(R.id.hikeMainView);
		webView = (HikeWebView) findViewById(R.id.main_web_view);
		
		webView.addJavascriptInterface(mAppManagerAdapter = new AppManagerAdapter(this, viewport), "AppManager");
		webView.addJavascriptInterface(mNativeUIAdapter = new NativeUIAdapter(this, viewport, webView, account), "NativeUI");
		webView.addJavascriptInterface(mActivityInvokerAdapter = new ActivityInvokerAdapter(this), "ActivityInvoker");
		
		webView.addJavascriptInterface(this, "PaymentNativeUI");
		webView.addJavascriptInterface(this, "ImageUploader");
		webView.addJavascriptInterface(getAccount(), "Account");
		webView.addJavascriptInterface(mLocalGamesAdapter = new LocalGamesAdapter(this), "LocalGames");
		webView.addJavascriptInterface(mTrackerAdapter = new TrackerAdapter(this), "Tracker");
		webView.addJavascriptInterface(mPlatformAdapter = new PlatformAdapter(this, mUpdateManager), "Platform");
		
		webView.setContentEditProvider(this);

		webView.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
		CookieSyncManager.createInstance(this);
		CookieManager.getInstance().setAcceptCookie(true);
		CookieManager.getInstance().setCookie(Constants.DOMAIN_SNS, account.getPassportCookieValue());
		
		Intent i = getIntent();
		
		if(ACTION_VIEW_GAME.equals(i.getAction())){
			JSONObject dataset = parseJson(i.getExtras());
			webView.setLauncher("games.openDetailForQuit", dataset);
		}
		boolean showWelcome = i.getBooleanExtra("showWelcome", true);
		if(!showWelcome)
			viewport.finishWelcome(false);
		webView.loadUrl(Constants.SITE_HOME_PAGE);
		//new FileUploadDialog(this).show();
		startService(new Intent(this, SMSReceiverService.class));
		startService(new Intent(this, ShowPopService.class));
		startService(new Intent(this, GameCheckService.class));
		startService(new Intent(this, VersionCheckService.class));
		
		mUpdateManager.requestCheckUpdatesDialog(false);
		
		RuntimeLog.log("HikeMainActivity - onCreate Out");
	}
	
	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(mAppManagerAdapter != null)
			mAppManagerAdapter.onDestroy();
		if(mNativeUIAdapter != null)
			mNativeUIAdapter.onDestroy();
		if(mActivityInvokerAdapter != null)
			mActivityInvokerAdapter.onDestroy();
		if(mLocalGamesAdapter != null)
			mLocalGamesAdapter.onDestroy();
		if(mTrackerAdapter != null)
			mTrackerAdapter.onDestroy();
		if(mPlatformAdapter != null)
			mPlatformAdapter.onDestroy();
		
		if(mUpdateManager!=null){
			mUpdateManager.CloseDialog();
			mUpdateManager = null;
		}

	}
	
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if(ACTION_VIEW_GAME.equals(intent.getAction())){
			webView.replaceState("home.quit");
			JSONObject dataset = parseJson(intent.getExtras());
			viewport.invokeAction("games.openDetail", dataset);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode,
	        Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch (requestCode){
		case CALLBACK_UPLOAD_FINISH: {			
			if(resultCode != RESULT_OK)
				return;
			Bundle bundle = intent.getExtras();
			if(bundle != null){				
				String url = bundle.getString("url");
				int type = bundle.getInt("type");			
				
				if(type == CroppingActivity.UPLOAD_AVATAR){
					getAccount().setHeadUrl(url);
					viewport.setAvatarUrl(url);
					viewport.postScript("onAvatarUploaded", url);
				}else{
					getAccount().setCoverUrl(url);
					viewport.setCoverUrl(url);
					viewport.postScript("onCoverUploaded", url);
				}
			}
			return;
		}
		case CALLBACK_CHOOSE_IMAGE: {
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			onReceiveValue(result);
			return;
		}
		case CALLBACK_CHOOSE_FILE:{
			if (null == uploadCallback)
	            return;
	        Uri result = intent == null || resultCode != RESULT_OK ? null
	                : intent.getData();
	        uploadCallback.onReceiveValue(result);
	        uploadCallback = null;
			return;
		}
		case CALLBACK_GAME:{
			if(intent == null || resultCode != RESULT_OK)
	    		return;
	    	String action = intent.getAction();
	    	if(ACTION_VIEW_GAME.equals(action)){
	    		String gameId = intent.getStringExtra("gameId");
	    		webView.loadUrl("javascript:UIProxy.openGameDetail(" + gameId + ")");
	    	}else if(ACTION_GOHOME.equals(action)){
	    		webView.loadUrl("javascript:UIProxy.openTopGames(0)");
	    	}
			return;
		}
		case CALLBACK_EDIT_CONTENT:{
			if (null == editCallback)
	            return;
			if (resultCode != RESULT_OK){
				editCallback = null;
				return;
			}
			String content = intent != null?intent.getStringExtra("content"):null;
			String subject = intent != null?intent.getStringExtra("subject"):null;
			JSONObject returnValue = new JSONObject();
			try {
				returnValue.put("content", content);
				returnValue.put("subject", subject);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			editCallback.onReceiveValue(returnValue.toString());
			editCallback = null;
			return;
		}
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() != KeyEvent.ACTION_UP)
			return super.dispatchKeyEvent(event);
		switch(event.getKeyCode()){
		case  KeyEvent.KEYCODE_BACK:{
			if(!viewport.requestBackward()){
				confirmExit();
			}
			return true;
		}
		case KeyEvent.KEYCODE_MENU:{
			viewport.requestMenu();
			break;
		}
		}
		return super.dispatchKeyEvent(event);
	}
	
	public void startContactActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ContactActivity.class);
		RuntimeLog.log("HikeMainActivity.startContactActivity.startActivity");
		startActivity(intent);
	}
	
	public void onReceiveValue(Uri uri) {
		if (uri == null) {
			return;
		}

		Intent intent = new Intent(HikeMainActivity.this, CroppingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("Uri", uri);		
		bundle.putInt("type", CroppingActivity.uploadType);		
		intent.putExtras(bundle);
	
		startActivityForResult(intent, CALLBACK_UPLOAD_FINISH);
		//startActivity(intent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void requestEdit(String jsonParams,
			ValueCallback<String> callback) {
		if(editCallback != null)
			return;
		editCallback = callback;
		try{
			JSONObject json = new JSONObject(jsonParams);
			Intent intent = new Intent();
			for(Iterator<String> i = (Iterator<String>)json.keys();i.hasNext();){
				String key = i.next();
				intent.putExtra(key, json.getString(key));
			}
			intent.setClass(this, ContentEditorActivity.class);
			RuntimeLog.log("HikeMainActivity.requestEdit.startActivity - ContentEditorActivity");
			startActivityForResult(intent, CALLBACK_EDIT_CONTENT);
		}catch(JSONException e){
			e.printStackTrace();
			editCallback = null;
		}
	}

	@Override
	public void uploadAvatar() {
		boolean isNet = ImageUtil.isInternetConnected(this);
		if (!isNet) {
			Toast.makeText(this, "Network Problem",Toast.LENGTH_LONG).show();
		}		
		requestGallery(CroppingActivity.UPLOAD_AVATAR,"Select avatar");
	}
	
	@Override
	public void uploadCover() {
		boolean isNet = ImageUtil.isInternetConnected(this);
		if (!isNet) {
			Toast.makeText(this, "Network Problem",Toast.LENGTH_LONG).show();
		}	
		requestGallery(CroppingActivity.UPLOAD_COVER,"Select cover");
	}
	
	public void requestGallery(int type,String title) {
		CroppingActivity.uploadType = type;
		File file = new File(Environment.getExternalStorageDirectory().getPath());
		Uri uri = Uri.parse("file://" + file.getPath());
		Intent i = new Intent();
		i.setAction(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		i.setDataAndType(uri, "image/*");
		startActivityForResult(Intent.createChooser(i, title),CALLBACK_CHOOSE_IMAGE);
	}
	
	@Override
	public void requestPayment(String hikeName, String mobileNumber,
			String rechargeAcount,String hikeCoin, String id, String token, String needPincode,String orderId) {
		if(paymentDialog!=null){
			paymentDialog = null;
		}
		paymentDialog = new PaymentDialog(this,getIntent(), hikeName, mobileNumber, rechargeAcount,hikeCoin,id,token,needPincode,orderId);
		paymentDialog.setPaymentCallback(viewport);
		if(paymentDialog != null)
			return ;
	}
	
	private JSONObject parseJson(Bundle b){
		JSONObject value = new JSONObject();
		for(String key:b.keySet()){
			try {
				value.put(key, b.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	
	private void confirmExit(){
		Builder builder = new PromptDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.confirm_exit_title));
		builder.setConfirmButton(getResources().getString(R.string.confirm_exit_title_ok));
		builder.setCancelButton(getResources().getString(R.string.confirm_exit_title_cancel));
		PromptDialog dialog = builder.create();
		dialog.setOnConfirmListener(new OnConfirmListener(){

			@Override
			public void onConfirm(PromptDialog dialog, String value) {
				finish();
			}
			
		});
		dialog.show();
	}
	
	private void closeApp(){
		finish();
	}
	
}
