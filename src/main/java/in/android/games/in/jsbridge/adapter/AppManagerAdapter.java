package in.android.games.in.jsbridge.adapter;

import in.android.games.in.activity.H5GameActivity;
import in.android.games.in.jsbridge.AppManager;
import in.android.games.in.widget.MainViewport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import zonesdk.in.android.games.in.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

public class AppManagerAdapter extends BaseAdapter implements AppManager{
	
	private static final String MIME_TYPE = "application/vnd.android.package-archive";
	
	private MainViewport mViewport;
	
	private Map<String, DownloadTask> tasks;
	
	private Activity mActivity;
	
	{
		tasks = Collections.synchronizedMap(new HashMap<String, DownloadTask>());
	}

	public AppManagerAdapter(Activity activity, MainViewport viewport) {
		super(activity);
		mActivity = activity;
		mViewport = viewport;
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		mActivity.registerReceiver(mInstallReceiver, filter);
	}
	
	private BroadcastReceiver mInstallReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String dataString = intent.getDataString();
			if (!dataString.startsWith("package:"))
				return;
			String installedPackageName = dataString.substring(8, dataString.length());
			try{
				JSONObject json = new JSONObject()
					.put(KEY_PACKAGE_NAME, installedPackageName);
				File apkFile = getApkFile(installedPackageName);
				if(apkFile.exists())
					apkFile.delete();
				//context.unregisterReceiver(this);
				mViewport.postScript(FUNCTION_ON_FINISHED_INSTALL, json);
			}catch(JSONException e){
				e.printStackTrace();
			} catch (IOException e) {}
		}
		
	};

	@Override
	public void onDestroy() {
		mActivity.unregisterReceiver(mInstallReceiver);
	}

	public boolean isAppInstalled(String packageName) {
		PackageManager packageMgr = mActivity.getPackageManager();
		PackageInfo info;
		try {
			info = packageMgr.getPackageInfo(packageName, 0);
			return info != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	
	public int getProgress(String packageName){
		File apkFile;
		try {
			apkFile = getApkFile(packageName);
		} catch (IOException e) {
			return -1;
		}
		if(apkFile.exists())
			return 100;
		DownloadTask task = tasks.get(packageName);
		if(task == null)
			return -1;
		return task.getProgress();
	}

	public void download(String appName, String packageName, String location) {
		if(tasks.containsKey(packageName))
			return;
		DownloadTask task = new DownloadTask(appName, packageName, location);
		tasks.put(packageName, task);
		task.execute();
	}
	
	public void downloadFromMarket(String packageName, String location){
		Intent i = new Intent();
		i.setData(Uri.parse(location));
		i.setAction(Intent.ACTION_VIEW);
		try{
			mActivity.startActivity(i);
		}catch(ActivityNotFoundException e){
			Toast.makeText(mActivity, R.string.error_no_market, Toast.LENGTH_SHORT).show();
		}
		return;
	}
	
	public void install(String appName, String packageName, String location) {
		File apkFile;
		try {
			apkFile = getApkFile(packageName);
		} catch (IOException e) {
			Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		Uri uri = Uri.fromFile(apkFile);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, MIME_TYPE);
		
		mActivity.startActivity(intent);
		try {
			mViewport.postScript(FUNCTION_ON_START_INSTALL, new JSONObject()
				.put(KEY_APP_NAME, appName)
				.put(KEY_PACKAGE_NAME, packageName));
		} catch (JSONException e) {}
	}
	
	public void start(String packageName){
		Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(packageName);
		mActivity.startActivity(intent);
	}
	
	public void startURL(String url){
		Intent intent = new Intent();
		intent.setClass(mActivity, H5GameActivity.class);
		intent.putExtra("playUrl", url);
		mActivity.startActivityForResult(intent, 202);
	}
	
	public void startForCompete(String url,String packageName){
		Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(packageName);
		Bundle mb = new Bundle();
		mb.putString("challengeId", url);
		intent.putExtras(mb);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		mActivity.startActivity(intent);
	}

	public void pause(String packageName) {
		// TODO
	}
	
	public void cancel(String packageName) {
		
	}
	
	@SuppressLint("WorldReadableFiles")
	private class DownloadTask extends AsyncTask<Void, Integer, Throwable>{

		private String mAppName, mPackageName, mLocation;
		
		private File mTempFile, mApkFile;
		
		private int mProgress = 0;
		
		DownloadTask(String appName, String packageName, String location){
			mAppName = appName;
			mPackageName = packageName;
			mLocation = location;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				mViewport.postScript(FUNCTION_ON_BEGIN_DOWNLOAD, new JSONObject()
					.put(KEY_APP_NAME, mAppName)
					.put(KEY_PACKAGE_NAME, mPackageName)
					.put(KEY_LOCATION, mLocation));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected Throwable doInBackground(Void... params) {
			FileOutputStream outStream = null;
			InputStream inStream = null;
			byte[] buffer = new byte[256 * 1024];
			int len = 0;
			try {
				mTempFile = getTempFile(mPackageName);
				mApkFile = getApkFile(mPackageName);
				if(mTempFile.exists())
					mTempFile.delete();
				mTempFile.createNewFile();
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet request = new HttpGet(mLocation);
				HttpResponse response = httpclient.execute(request);
				if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
					throw new IOException(mActivity.getString(R.string.error_network));
				}
				HttpEntity entity = response.getEntity();
				double contentLength = entity.getContentLength();
				if(contentLength == 0){
					return new IOException(mActivity.getString(R.string.error_network));
				}
				if(getAvailableSize() < contentLength){
					return new IOException(mActivity.getString(R.string.error_no_space));
				}
				inStream = entity.getContent();
				outStream = new FileOutputStream(mTempFile);
				int cursor = 0, progress = 0;
				while ((len = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
					cursor += len;
					progress = (int)((cursor / contentLength) * 100);
					publishProgress(progress);
				}
				outStream.flush();
				outStream.close();
				mTempFile.renameTo(mApkFile);
				return null;
			} catch (Exception e) {
				if(mApkFile != null && mApkFile.exists())
					mApkFile.delete();
				return e;
			} finally{
				if(mTempFile != null && mTempFile.exists())
					mTempFile.delete();
				if(outStream != null){
					try {
						outStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				tasks.remove(mPackageName);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mProgress = values[0];
			try {
				mViewport.postScript(FUNCTION_ON_DOWNLOAD_PROGRESS, new JSONObject()
					.put(KEY_APP_NAME, mAppName)
					.put(KEY_PACKAGE_NAME, mPackageName)
					.put(KEY_LOCATION, mLocation)
					.put(KEY_PROGRESS, mProgress));
			} catch (JSONException e) {}
		}

		@Override
		protected void onPostExecute(Throwable result) {
			super.onPostExecute(result);
			try {
				if(result != null){
					result.printStackTrace();
					mViewport.postScript(FUNCTION_ON_DOWNLOAD_ERROR, new JSONObject()
						.put(KEY_APP_NAME, mAppName)
						.put(KEY_PACKAGE_NAME, mPackageName)
						.put(KEY_LOCATION, mLocation)
						.put(KEY_ERROR_MSG, result.getMessage()));
					return;
				}
				mViewport.postScript(FUNCTION_ON_FINISHED_DOWNLOAD, new JSONObject()
					.put(KEY_APP_NAME, mAppName)
					.put(KEY_PACKAGE_NAME, mPackageName)
					.put(KEY_LOCATION, mLocation));
			} catch (JSONException e) {}
		}
		
		private int getProgress(){
			return mProgress;
		}
		
	}
	
	private double getAvailableSize(){
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		StatFs statFs = new StatFs(sdcard);
		long blockSize = statFs.getBlockSize();
		long blocks = statFs.getAvailableBlocks();
		return blockSize * blocks;
	}
	
	private File getTempFile(String packageName) throws IOException{
		return getFile(packageName, "tmp");
	}
	
	private File getApkFile(String packageName) throws IOException{
		return getFile(packageName, "apk");
	}

	private File getFile(String packageName, String type) throws IOException{
		String tempPath = getDownloadPath() + "/" + packageName + "." + type;
		File tempFile = new File(tempPath);
		return tempFile;
	}
	
	private String getDownloadPath() throws IOException {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			throw new IOException(mActivity.getString(R.string.error_no_sdcard));
		}
		String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/zone/download";
		new File(downloadPath).mkdirs();
		return downloadPath;
	}

}
