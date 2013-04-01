package in.android.games.in.client;


import in.android.games.in.utils.CookieUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;

public class ResponseWrapper {

	private static final String TAG = "ResponseWrapper";

	private boolean valid;
	private int status=1;
	private JSONObject respJson;
	private boolean noError;

	private CookieStore cookieStore;
	private Map<String,String> cookieMap;
	
	public ResponseWrapper(HttpResponse resp, CookieStore cookieStore) {
		try {
			String respStr = EntityUtils.toString(resp.getEntity());
			Log.i(TAG, "ResponseWrapper() respStr=" + respStr);
			this.respJson = new JSONObject(respStr);
			Log.i(TAG, "ResponseWrapper() respJson=" + this.respJson);
			this.status = this.respJson.getInt("status");
			Log.i(TAG, "ResponseWrapper() status=" + status);
			this.noError = (status != Status.FAILED.getValue());
			
			this.cookieStore = cookieStore;
			this.cookieMap = new HashMap<String,String>();
			if(cookieStore!=null){
				List<Cookie> cookies = cookieStore.getCookies();
				for(Cookie cookie:cookies){
					cookieMap.put(cookie.getName(), cookie.getValue());
					Log.i(TAG, "ResponseWrapper() " + cookie.getName() + "=" + cookie.getValue());
				}
			}
			
			this.valid = true;
			
			return;
		} catch (ParseException e) {
			Log.e(TAG, "", e);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		} catch (JSONException e) {
			Log.e(TAG, "", e);
		}
	}

	public boolean isValid() {
		return valid;
	}

	public int getStatus() {
		return status;
	}
	
	public JSONObject getRespJson() {
		return respJson;
	}

	public boolean isNoError() {
		return noError;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public Map<String, String> getCookieMap() {
		return cookieMap;
	}
	
	public String getStringFromResp(String key){
		try {
			return respJson.getString(key);
		} catch (JSONException e) {
			Log.e(TAG, "",e);
			throw new RuntimeException(e);
		}
	}
	
	public String getStringFromCookie(String key){
		return cookieMap.get(key);
	}
	
	public String getStringFromCookieCrossDomain(String key){
		return cookieMap.get(CookieUtil.crossCookieName(key));
	}
	
	public Cookie getCookie(String name){
		if(cookieStore!=null){
			List<Cookie> cookies = cookieStore.getCookies();
			for(Cookie cookie:cookies){
				if(cookie.getName().equals(name)){
					return cookie;
				}
			}
		}
		
		return null;
	}
	
	public Cookie getCookieCrossDomain(String name){
		return getCookie(CookieUtil.crossCookieName(name));
	}
}
