package in.android.games.in.widget;

import in.android.games.in.common.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HikeWebView extends WebView{
	
	private ContentEditProvider editProvider;
	
	private Hashtable<String, Object> interfaces;
	
	private String mLauncher;
	
	public HikeWebView(Context context) {
		super(context);
		initWebView(context);
	}
	
	public HikeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWebView(context);
	}
	
	public HikeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWebView(context);
	}

	public void setLauncher(String action, JSONObject dataset){
		JSONObject launcher = new JSONObject();
		try {
			launcher.put("action", action);
			launcher.put("dataset", dataset);
			mLauncher = launcher.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void replaceState(String action){
		loadUrl("javascript:hike.replaceState('" + action + "')");
	}
	
	private void initWebView(Context context){
		interfaces = new Hashtable<String, Object>();
		WebSettings settings = getSettings();
		settings.setLightTouchEnabled(false);
		settings.setJavaScriptEnabled(true);
		settings.setAllowFileAccess(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setDatabasePath(getContext().getDatabasePath("webview").getAbsolutePath());
		setVerticalScrollbarOverlay (true);
		setVerticalScrollBarEnabled(true);
		setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
		setWebViewClient(new ClientExtension());
		setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});
		setWebChromeClient(new ChromeExtension());
	}

	public void invokeAction(String action, JSONObject dataset){
		loadUrl("javascript:hike.invokeAction('" + action + "', " + dataset.toString() + ")");
	}
	
	private class ClientExtension extends WebViewClient{
		
		@Override
        public boolean shouldOverrideUrlLoading(WebView view, String origUrl) {
        	if(origUrl.indexOf(Constants.SITE_HOME_PAGE) == 0 && !Constants.SITE_HOME_PAGE.equals(origUrl)){
		        view.loadUrl(origUrl);
		        return true;
		    }
            return false;
        }
		
		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			handler.proceed(Constants.CREDENTIALS_USER_NAME,
					Constants.CREDENTIALS_PASSWORD);
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon){
			super.onPageFinished(view, url);
		}
		
		
		@Override
		public void onPageFinished(WebView view, String url){
			super.onPageFinished(view, url);
		}
		
	}
	
	private class ChromeExtension extends WebChromeClient implements ValueCallback<String>{
		
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result){
			if("Launch".equals(message)){
				String launcher = HikeWebView.this.mLauncher;
				HikeWebView.this.mLauncher = null;
				result.confirm(launcher);
				return true;
			}
			if("ContentEditor".equals(message)){
				editProvider.requestEdit(defaultValue, this);
				result.confirm();
				return true;
			}
			try {
				String[] part = message.split("&");
				if (part.length < 2)
					return false;
				String objName = part[0];
				String methodName = part[1];
				List<String> args = new ArrayList<String>();
				for (int i = 2; i < part.length; i++) {
					args.add(part[i]);
				}
				if (!interfaces.containsKey(objName))
					return false;
				Object returnValue = tryToCallObject(interfaces.get(objName), methodName, args.toArray(new String[]{}));
				result.confirm(returnValue != null ? returnValue.toString()
						: null);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		public void onReceiveValue(String value) {
			HikeWebView.this.loadUrl("javascript:window.onEditCompleted(" + value + ")");
		}
	}
	
	public void addJavascriptInterface(Object obj, String interfaceName){
		interfaces.put(interfaceName, obj);
	}
	
	public void setContentEditProvider(ContentEditProvider editProvider){
		this.editProvider = editProvider;
	}
	
	private Object tryToCallObject(Object target, String methodName,
			String[] args) {
		Method method = matchMethod(target.getClass(), methodName, args);
		if (method == null)
			return null;
		return tryToInvokeMethod(target, method, args);
	}

	private Method matchMethod(Class<?> clazz, String methodName,
			String[] args) {
		Method[] ms = clazz.getMethods();
		for (Method m : ms) {
			if (m.getName().equals(methodName)
					&& m.getParameterTypes().length == args.length)
				return m;
		}
		return null;
	}

	private Object tryToInvokeMethod(Object target, Method method,
			String[] args) {
		List<Object> params = new ArrayList<Object>();
		Class<?>[] clazzes = method.getParameterTypes();
		try {
			for (int i = 0; i < clazzes.length; i++) {
				Class<?> cls = clazzes[i];
				String value = args[i];
				if (value == null || "null".equals(value)
						|| "undefined".equals(value)) {
					params.add(null);
				} else if (cls == String.class) {
					params.add(value);
				} else if (cls == Integer.class || cls == Integer.TYPE) {
					params.add(Integer.parseInt(value));
				} else if (cls == Long.class || cls == Long.TYPE) {
					params.add(Long.parseLong(value));
				} else if (cls == Double.class || cls == Double.TYPE) {
					params.add(Double.parseDouble(value));
				} else if (cls == Boolean.class || cls == Boolean.TYPE) {
					params.add(Boolean.parseBoolean(value));
				} else if (JSONObject.class.isAssignableFrom(cls)) {
					params.add(new JSONObject(value));
				}
			}
			return method.invoke(target, params.toArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static interface ContentEditProvider{
		public void requestEdit(String jsonParams, ValueCallback<String> callback);
		
	}

}
