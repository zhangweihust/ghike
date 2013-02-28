package zonesdk.in.android.games.in.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.utils.CookieUtil;

public class RequestProvider {
	
	private static final String PREFIX = "--";
	
	private static final String BOUNDARY = UUID.randomUUID().toString();
	
	private static final String LINE_END = "\r\n";

	private static final String CONTENT_TYPE = "multipart/form-data";
	
	private static final int BUFFER_LENGTH = 64 * 1024;

	private String mPassport;
	
	private String mUserAgent;
	
	public void setPassport(String passport){
		mPassport = passport;
	}
	
	public void clearPassport(){
		mPassport = null;
	}
	
	public void setUserAgent(String userAgent){
		mUserAgent = userAgent;
	}
	
	public String get(String url) throws ClientProtocolException, IOException, ParseException{
		HttpGet get = new HttpGet(url);
		return executeRequest(get);
	}
	
	public String post(String url, Map<String, String> values) throws ClientProtocolException, IOException, ParseException{
		HttpPost post = new HttpPost(url);
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		if(values != null){
			for(String key : values.keySet()){
				params.add(new BasicNameValuePair(key, values.get(key)));
			}
		}
		HttpEntity entity = new UrlEncodedFormEntity(params);
		post.setEntity(entity);
		return executeRequest(post);
	}
	
	public String upload(String urlStr, String fileName, String mimeType, InputStream inStream, int contentLength) throws IOException{
		URL url = new URL(urlStr);
		String result;
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
				+ BOUNDARY);
		conn.setRequestProperty("Authorization",
				"Basic dGVzdGVyOmJzYjMwMTE=");
		conn.setRequestProperty("Cookie", CookieUtil.crossCookieName("passport")
				+ "=\"" + mPassport + "\"");
		StringBuffer sb = new StringBuffer();
		sb.append(PREFIX);
		sb.append(BOUNDARY);
		sb.append(LINE_END);
		sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
				+ fileName + "\"" + LINE_END);
		sb.append(LINE_END);
		byte[] end = (LINE_END + PREFIX + BOUNDARY + PREFIX + LINE_END)
				.getBytes();
		conn.setFixedLengthStreamingMode(contentLength + sb.length() + end.length);
		DataOutputStream dos = new DataOutputStream(
				conn.getOutputStream());
		dos.write(sb.toString().getBytes());

		byte[] bytes = new byte[BUFFER_LENGTH];
		int len = 0;
		while ((len = inStream.read(bytes)) != -1) {
			dos.write(bytes, 0, len);
			dos.flush();
		}
		dos.write(end);
		dos.flush();

		int res = conn.getResponseCode();
		if (res == 200) {
			InputStream input = conn.getInputStream();
			StringBuffer sb1 = new StringBuffer();
			int ss;
			while ((ss = input.read()) != -1) {
				sb1.append((char) ss);
			}
			result = sb1.toString();
		} else {
			throw new IOException("Upload fail, response code = " + res);
		}
		
		return result;
	}
	
	private String executeRequest(HttpUriRequest req) throws ClientProtocolException, IOException, ParseException{
		HttpClient http = createHttpClient(mUserAgent);
		if(mPassport != null){
			Header header = new BasicHeader("Cookie", CookieUtil.crossCookieName("passport") + "=\"" + mPassport + "\"");
			req.addHeader(header);
		}
		HttpResponse res = http.execute(req);
		int sc = res.getStatusLine().getStatusCode();
		if (sc == HttpStatus.SC_OK) {
			return EntityUtils.toString(res.getEntity());
		}else{
			throw new IOException("Request to " + req.getURI() + "failed. Server respond " + sc);
		}
	}
	
	private static HttpClient createHttpClient(String userAgent){
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		params.setBooleanParameter("http.protocol.expect-continue", false);
		params.setParameter("http.useragent", userAgent);
		BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
        		Constants.CREDENTIALS_USER_NAME, Constants.CREDENTIALS_PASSWORD));
        httpClient.setCredentialsProvider(bcp);
        HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
        return httpClient;
	}
	
	public static final RequestProvider DEFAULT_PROVIDER = new RequestProvider();
	
}
