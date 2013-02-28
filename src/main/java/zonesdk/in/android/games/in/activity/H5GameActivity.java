package zonesdk.in.android.games.in.activity;

import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class H5GameActivity extends Activity {
	
	WebView gameWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game_webview);
		
		RuntimeLog.log("H5GameActivity.onCreate()");
		
		gameWebView = (WebView) findViewById(R.id.GameWebView);
		gameWebView.getSettings().setJavaScriptEnabled(true);
		Intent intent = getIntent();
		String url = intent.getStringExtra("playUrl");
		String tempPath ="";
		if(!"zone".equals(Constants.SOURCE)){
			tempPath = "&source=ghike";
		}
		url += (((url.indexOf("?") == -1 )?"?": tempPath + "&") + "isNative=true");
		final Activity activity = this;
		final String playUrl = url;

		gameWebView.setVerticalScrollbarOverlay (true);
		gameWebView.setVerticalScrollBarEnabled(true);
		gameWebView.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url == null){
					return false;
				}else if(url.equals(playUrl)){
					view.loadUrl(url);
					return true;
				}else if("http://stopGame/".equals(url)){
					activity.finish();
					return true;
				}else if(url.startsWith("http://gameDetail")){
					String id = url.substring("http://gameDetail".length() + 1);
					Intent intent = new Intent();
					intent.setAction(HikeMainActivity.ACTION_VIEW_GAME);
					intent.putExtra("gameId", id);
					setResult(Activity.RESULT_OK, intent);
					finish();
					return true;
				}else if(url.startsWith("http://hikeHome")){
					Intent intent = new Intent();
					intent.setAction(HikeMainActivity.ACTION_GOHOME);
					setResult(Activity.RESULT_OK, intent);
					finish();
					return true;
				}
				return false;
			}
			public void onReceivedHttpAuthRequest(WebView view,
					HttpAuthHandler handler, String host, String realm) {
				handler.proceed(Constants.CREDENTIALS_USER_NAME,
						Constants.CREDENTIALS_PASSWORD);
			}
		});
		gameWebView.setWebChromeClient(new WebChromeClient(){
			
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, final JsPromptResult result) {
				if(message != null && message.startsWith("openPayment:")){
					Intent intent = new Intent();
					intent.setClass(activity, PaymentActivity.class);
					intent.putExtra("paymentUrl", message.substring(12, message.length()));
					RuntimeLog.log("H5GameActivity.GameWebView.onJsPrompt.startActivity - paymentUrl");
					activity.startActivity(intent);
					result.cancel();
					return true;
				}
				return false;
			}
		});
		gameWebView.loadUrl(playUrl);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		RuntimeLog.log("H5GameActivity.onResume()");
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		RuntimeLog.log("H5GameActivity.onPause()");
	}
	
	
	@Override
	protected void onRestart(){
		super.onRestart();
		RuntimeLog.log("H5GameActivity.onRestart()");
	}
	
	
	@Override
	protected void onStop(){
		super.onStop();
		RuntimeLog.log("H5GameActivity.onStop()");
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		RuntimeLog.log("H5GameActivity.onDestroy()");
		if(gameWebView != null)
			gameWebView.destroy();
	}
	
}
