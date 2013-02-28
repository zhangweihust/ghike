package zonesdk.in.android.games.in.activity;

import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PaymentActivity extends Activity {
	
	WebView webView;
	
	private static final String TAG = "hike-client";
	
	@Override
	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.payment_webview);
		
		RuntimeLog.log("PaymentActivity.onCreate()");
		
		webView = (WebView) findViewById(R.id.PaymentWebView);
		webView.getSettings().setJavaScriptEnabled(true);
		Intent intent = getIntent();
		final String paymentUrl = intent.getStringExtra("paymentUrl");
		final Activity activity = this;

		webView.setVerticalScrollbarOverlay (true);
		webView.setVerticalScrollBarEnabled(true);
		webView.setWebViewClient(new WebViewClient(){
			public void onReceivedHttpAuthRequest(WebView view,
					HttpAuthHandler handler, String host, String realm) {
				handler.proceed(Constants.CREDENTIALS_USER_NAME,
						Constants.CREDENTIALS_PASSWORD);
			}
			public void onPageFinished(WebView view, String url){
				Log.i(TAG, url);
			}
		});
		webView.setWebChromeClient(new WebChromeClient(){
			public boolean onJsPrompt(WebView view, String url, String message,
					String defaultValue, final JsPromptResult result) {
				if("closePayment".equals(message)){
					activity.finish();
					return true;
				}
				return false;
			}
		});
		webView.loadUrl(paymentUrl);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		RuntimeLog.log("PaymentActivity.onPause()");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		RuntimeLog.log("PaymentActivity.onResume()");
	}
	
	@Override
	protected void onRestart(){
		super.onRestart();
		RuntimeLog.log("PaymentActivity.onRestart()");
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		RuntimeLog.log("PaymentActivity.onStop()");
	}
	

	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		RuntimeLog.log("PaymentActivity.onDestroy()");
		
		if(webView != null)
			webView.destroy();
	}
	
}
