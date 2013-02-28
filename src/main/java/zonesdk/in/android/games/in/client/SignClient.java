package zonesdk.in.android.games.in.client;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.proxy.VersionClient;
import zonesdk.in.android.games.in.utils.CookieUtil;
import zonesdk.in.android.games.in.utils.RuntimeLog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

public class SignClient {

	//for airtel test begin
	private static final void addAirtelHeader(AbstractHttpMessage msg){
		boolean isAirtel = false;
		if(isAirtel){
			//msg.addHeader("MSISDN", "9818332307");//for signup
			//msg.addHeader("MSISDN", "9818332301");//for signin
		}
	}
	//for airtel test end
	
	private static final String TAG = "SignManager";

	private static final String CHECKUSER_URL = Constants.URL_TOUCH + "checkuser";
	private static final String GETCODE_URL = Constants.URL_TOUCH + "android/getcode";
	private static final String SIGNUP_URL = Constants.URL_TOUCH + "signup";
	private static final String SIGNUP_NICKNAME_URL = Constants.URL_TOUCH + "signup-nickname";
	private static final String SIGNIN_URL = Constants.URL_TOUCH + "signin";
	private static final String SIGNIN_CUSTOM_PINCODE_URL = Constants.URL_TOUCH + "signin-custom-pincode";
	private static final String SIGNIN_AIRTEL_URL = Constants.URL_TOUCH + "signin-airtel";
	private static final String LOGOUT_URL = Constants.URL_TOUCH + "logout";
	
	public static Thread performCheckUser(final String passport, final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				List<Header> headers = new ArrayList<Header>();
				if(passport!=null && !passport.equals("")){
					headers.add(genPassportCookieHeader(passport));
				}
				
				final ResponseWrapper resp = sendGetRequest(CHECKUSER_URL, null, headers);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	
	public static ResponseWrapper performingGetSignUpCode(final String number){
		ResponseWrapper resp = null;
		final String PARAM_NUMBER = "phone";
		final String PARAM_CODE_TYPE = "codeType";

		String url;
		try{
			url = GETCODE_URL + "?" + PARAM_NUMBER + "=" + URLEncoder.encode(number, "utf-8") + "&" + PARAM_CODE_TYPE + "=SIGNUP_CODE";
			resp = sendGetRequest(url, null, null);
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return resp;
		
	}
	
	public static Thread performGetSignUpCode(final String number, final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				final String PARAM_NUMBER = "phone";
				final String PARAM_CODE_TYPE = "codeType";

				String url;
				try {
					url = GETCODE_URL + "?" + PARAM_NUMBER + "=" + URLEncoder.encode(number, "utf-8") + "&" + PARAM_CODE_TYPE + "=SIGNUP_CODE";
					final ResponseWrapper resp = sendGetRequest(url, null, null);
					if(handler!=null){
						handler.post(new Runnable() {
							public void run() {
								callback.call(resp);
							}
						});
					}
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "", e);
					if(handler!=null){
						handler.post(new Runnable() {
							public void run() {
								callback.call(null);
							}
						});
					}
				}
			}
		});
	}
	public static Thread performSignUp(final String number, final String pinCode,
			final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				String[] paraKeys = { "phone", "pinCode" };
				String[] paraValues = { number, pinCode };

				final ResponseWrapper resp = sendPostRequest(SIGNUP_URL, paraKeys, paraValues, null, null);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	public static Thread performSignUpNickname(final String nickname, final String loginTrace,
			final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				String[] paraKeys = { "nickname" };
				String[] paraValues = { nickname };
				
				List<Header> headers = new ArrayList<Header>();
				if(loginTrace!=null && !loginTrace.equals("")){
					headers.add(genLoginTraceCookieHeader(loginTrace));
				}

				final ResponseWrapper resp = sendPostRequest(SIGNUP_NICKNAME_URL, paraKeys, paraValues, null, headers);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	
	public static ResponseWrapper performingGetSignInCode(final String number){
		ResponseWrapper resp = null;
		final String PARAM_NUMBER = "phone";
		final String PARAM_CODE_TYPE = "codeType";

		String url;
		try {
			url = GETCODE_URL + "?" + PARAM_NUMBER + "=" + URLEncoder.encode(number, "utf-8") + "&" + PARAM_CODE_TYPE + "=SIGNIN_CODE";
			resp = sendGetRequest(url, null, null);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	
	public static Thread performGetSignInCode(final String number, final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				final String PARAM_NUMBER = "phone";
				final String PARAM_CODE_TYPE = "codeType";

				String url;
				try {
					url = GETCODE_URL + "?" + PARAM_NUMBER + "=" + URLEncoder.encode(number, "utf-8") + "&" + PARAM_CODE_TYPE + "=SIGNIN_CODE";
					final ResponseWrapper resp = sendGetRequest(url, null, null);
					if(handler!=null){
						handler.post(new Runnable() {
							public void run() {
								callback.call(resp);
							}
						});
					}
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "", e);
					if(handler!=null){
						handler.post(new Runnable() {
							public void run() {
								callback.call(null);
							}
						});
					}
				}
				
			}
		});
	}
	
	
	public static Thread performSignIn(final String number, final String pinCode,
			final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				String[] paraKeys = { "phone", "pinCode" };
				String[] paraValues = { number, pinCode };

				final ResponseWrapper resp = sendPostRequest(SIGNIN_URL, paraKeys, paraValues, null, null);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	public static Thread performSignInCustomPinCode(final String number, final String customPinCode,
			final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				String[] paraKeys = { "phone", "customPinCode" };
				String[] paraValues = { number, customPinCode };

				final ResponseWrapper resp = sendPostRequest(SIGNIN_CUSTOM_PINCODE_URL, paraKeys, paraValues, null, null);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	public static Thread performSignInAirtel(final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				final ResponseWrapper resp = sendPostRequest(SIGNIN_AIRTEL_URL, null, null, null, null);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	public static Thread performLogout(final Handler handler, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				final ResponseWrapper resp = sendPostRequest(LOGOUT_URL, null, null, null, null);
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.call(resp);
						}
					});
				}
			}
		});
	}
	
	public static Thread performGetCheckVersion(final Activity context, final Handler handler, final String src, final Callback callback) {
		return performOnBackgroundThread(new Runnable() {
			public void run() {
				try {
					RuntimeLog.log("checkingUpdates - In");
					ClientFactory sClientFactory;
					sClientFactory = ClientFactory.getInstance();
					VersionClient client = sClientFactory.get(VersionClient.class);
					JSONObject jsonObject = client.getVersion(Constants.SOURCE);
					
					int status = jsonObject.getInt("status");
					switch (status) {
					case 0:
							String versionName =  jsonObject.getString("versionName");
							int versionCode = jsonObject.getInt("versionCode");
							int minversionCode = jsonObject.getInt("minversionCode");
							String description = jsonObject.getString("description");
							String downloadurl = jsonObject.getString("downloadUrl");

							SharedPreferences sp = context.getSharedPreferences("version", Context.MODE_PRIVATE);
							
							Editor editor = sp.edit();
							
							if(sp.getInt("versionCode", 0)<versionCode){
								//a newer version detected!
								SharedPreferences spPromt = context.getSharedPreferences("promtRecord", Context.MODE_PRIVATE);
								Editor editorPromt = spPromt.edit();
								//reset promtNum to 0
								editorPromt.putInt("promtNum", 0);
							    editorPromt.putString("promtDate", "2000-01-01");
							    editorPromt.commit();
								
							}
							
							editor.putString("versionName", versionName);
							editor.putInt("versionCode", versionCode);
							editor.putInt("minversionCode", minversionCode);
							editor.putString("description", description);
							editor.putString("downloadurl", downloadurl);
							editor.commit();


						break;
					default:
						break;
					}

				} catch (Exception e) {
					e.printStackTrace();

				}
				
				if(handler!=null){
					handler.post(new Runnable() {
						public void run() {
							callback.callVoid();
						}
					});
				}
				
			}
		});
	}
	
	private static Thread performOnBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} finally {
				}
			}
		};
		t.start();
		return t;
	}
	private static ResponseWrapper sendGetRequest(String url, CookieStore reqCookieStore, List<Header> headers) {
		Log.i(TAG, "sendGetRequest() begin url=" + url + ",reqCookieStore=" + reqCookieStore);
		HttpGet httpGet = new HttpGet(url);
		if(headers!=null){
			for(Header header:headers){
				httpGet.addHeader(header);
			}
		}
		DefaultHttpClient httpClient = newDefaultHttpClient();
		if(reqCookieStore!=null){
			httpClient.setCookieStore(reqCookieStore);
		}
		HttpResponse resp;
		CookieStore cookieStore;
		addAirtelHeader(httpGet);
		try {
			resp = httpClient.execute(httpGet);
			cookieStore = httpClient.getCookieStore();
		} catch (ClientProtocolException e) {
			Log.e(TAG, "", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		
		int statusCode = resp.getStatusLine().getStatusCode();
		Log.i(TAG, "sendGetRequest() statusCode=" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			return new ResponseWrapper(resp, cookieStore);
		}else{
			return null;
		}
	}
	private static ResponseWrapper sendPostRequest(String url, String[] paraKeys, String[] paraValues, CookieStore reqCookieStore, List<Header> headers) {
		Log.i(TAG, "sendPostRequest() begin, url=" + url);
		HttpPost httpPost = new HttpPost(url);
		if(headers!=null){
			for(Header header:headers){
				httpPost.addHeader(header);
			}
		}
		if(paraKeys!=null && paraKeys.length>0 && paraValues!=null && paraValues.length>0){
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			int paraLen = paraKeys.length;
			for (int i = 0; i < paraLen; i++) {
				Log.i(TAG, "sendPostRequest() " + paraKeys[i] + "=" + paraValues[i]);
				params.add(new BasicNameValuePair(paraKeys[i], paraValues[i]));
			}
			HttpEntity entity = null;
			try {
				entity = new UrlEncodedFormEntity(params);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "", e);
				return null;
			}
			httpPost.setEntity(entity);
		}
		
		DefaultHttpClient httpClient = newDefaultHttpClient();
		if(reqCookieStore!=null){
			httpClient.setCookieStore(reqCookieStore);
		}
		HttpResponse resp;
		CookieStore respCookieStore;
		addAirtelHeader(httpPost);
		try {
			resp = httpClient.execute(httpPost);
			respCookieStore = httpClient.getCookieStore();
		} catch (ClientProtocolException e) {
			Log.e(TAG, "", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		} finally {
			//httpClient.getConnectionManager().shutdown();
		}
		
		int statusCode = resp.getStatusLine().getStatusCode();
		Log.i(TAG, "sendPostRequest() statusCode=" + statusCode);
		if (statusCode == HttpStatus.SC_OK) {
			return new ResponseWrapper(resp, respCookieStore);
		}else{
			return null;
		}
	}
	private static DefaultHttpClient newDefaultHttpClient(){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		ConnManagerParams.setTimeout(params, 1000);
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 30000);
		params.setBooleanParameter("http.protocol.expect-continue", false);
		BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
        		Constants.CREDENTIALS_USER_NAME, Constants.CREDENTIALS_PASSWORD));
        httpClient.setCredentialsProvider(bcp);
        HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
		return httpClient;
	}
	private static Header genPassportCookieHeader(String passport){
		Header header = new BasicHeader("Cookie",CookieUtil.crossCookieName("passport") + "=\"" + passport + "\"");
		return header;
	}
	private static Header genLoginTraceCookieHeader(String loginTrace){
		Header header = new BasicHeader("Cookie",CookieUtil.crossCookieName("logintrace") + "=\"" + loginTrace + "\"");
		return header;
	}
	
	
	
	
	
}
