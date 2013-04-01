package in.android.games.in.utils;


import in.android.games.in.client.ResponseWrapper;
import in.android.games.in.common.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;


import android.util.Log;

/**
 * HTTP ���� ������  
 * @author ISS_mwrao
 * 2012-10-24 
 */
public class HttpRequestUtils {
	
	private static final String TAG = "HTTPManageer";
	
	
	public static ResponseWrapper sendPostRequest(String url,
			Map<String, String> HttpParam, String passportMyCookieValue) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		Log.i(TAG, "sendPostRequest() begin, url=" + url);
		HttpPost httpPost = new HttpPost(Constants.URL_TOUCH+url);
		List<Header> headers = new ArrayList<Header>();

		if(passportMyCookieValue.length()!=0){
			headers.add(CookieUtil.genPassportCookieHeader(passportMyCookieValue));
		}

		if(headers!=null){
			for(Header header:headers){
				httpPost.addHeader(header);
			}
		}
		if (HttpParam != null && HttpParam.size() > 0) {
			Iterator<Entry<String, String>> HttpParams = HttpParam.entrySet()
					.iterator();
			Log.i(TAG, "sendInviteGetRequest() " + HttpParams);
			for (; HttpParams.hasNext();) {
				Map.Entry<String, String> entry = (Map.Entry) HttpParams.next();
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
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
		
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
		
		HttpResponse resp;

		try {
			Log.i(TAG, "send request ");
			resp = httpClient.execute(httpPost);
			int statusCode = resp.getStatusLine().getStatusCode();
			Log.i(TAG, "sendPostRequest() statusCode=" + statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				return new ResponseWrapper(resp, null);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "", e);
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

	}
	
	
	public static DefaultHttpClient newDefaultHttpClient(){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
        		Constants.CREDENTIALS_USER_NAME, Constants.CREDENTIALS_PASSWORD));
        httpClient.setCredentialsProvider(bcp);
        // add a parameter to avoid request fail first time by job @20121129
        HttpParams params = httpClient.getParams();
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);

		return httpClient;
	}
	
}
