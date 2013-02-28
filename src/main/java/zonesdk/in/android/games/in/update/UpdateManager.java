package zonesdk.in.android.games.in.update;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import zonesdk.in.android.games.in.client.Callback;
import zonesdk.in.android.games.in.client.ClientFactory;

import zonesdk.in.android.games.in.client.SignClient;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.dialog.CheckUpdateDialog;
import zonesdk.in.android.games.in.proxy.VersionClient;

import zonesdk.in.android.games.in.utils.DateUtils;
import zonesdk.in.android.games.in.utils.RuntimeLog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;

import android.util.Log;

public class UpdateManager {

	private Activity mContext;
	
	private Handler mHandler;
	
	private CheckUpdateDialog checkDialog;

	public UpdateManager(Activity context, Handler handler) {
/*	public UpdateManager(Activity context) {*/
		this.mContext = context;
		this.mHandler = handler;
/*		checkDialog = new CheckUpdateDialog(this, mContext, handler);*/
		checkDialog = new CheckUpdateDialog(this, mContext);
	}
	
	public void CloseDialog(){
		if(checkDialog!=null){
			checkDialog.cancel();
			checkDialog = null;
		}

	}
	
/*	protected void finalize(){
		checkDialog.cancel();
		checkDialog = null;
	}*/
	
	/*
	 *  when not login
	 *  1. fetch data from server
	 *  2. check needUpdate
	 *  3. if 2, show TYPE_UPDATE_FORCE dialog
	 *  
	 *             by wei.zhang 2013-01-22
	 * */
	public void ForceCheckUpdates(){
		SignClient.performGetCheckVersion(mContext, mHandler, Constants.SOURCE, new Callback() {
			public void callVoid() {
				//update dialog
				int flag = needUpdate(true);
				
				RuntimeLog.log("needUpdate :" + flag);
				
				//flag = 2;//for debug
				
				if(flag == 2){
					checkDialog.initView(CheckUpdateDialog.TYPE_UPDATE_FORCE);
				}


			}
		});
	}
	
	/*
	 *  forceCheck:
	 *  true: when user manually run check update,need to fetch data right now
	 *  false: when the app starts, only read from local cache
	 *  
	 *              by wei.zhang 2013-01-22
	 * */
	public void requestCheckUpdatesDialog(boolean forceCheck){
		RuntimeLog.log("UpdateManager - requestCheckUpdatesDialog - forceCheck:" + forceCheck);
		if(forceCheck){
			//show dialog getting updates from server
			//perform loading
			checkDialog.setDismiss(false);
			checkDialog.initView(CheckUpdateDialog.TYPE_LOADING);
			
			SignClient.performGetCheckVersion(mContext, mHandler, Constants.SOURCE, new Callback() {
				public void callVoid() {
					//update dialog
					int flag = needUpdate(true);
					
					RuntimeLog.log("needUpdate :" + flag);
					
					//flag = 2;//for debug
					
					if(flag == 0){
						checkDialog.initView(CheckUpdateDialog.TYPE_NO_UPDATE);
					}else if(flag == 1){
						checkDialog.initView(CheckUpdateDialog.TYPE_UPDATE_UNFORCE);
					}else if(flag == 2){
						checkDialog.initView(CheckUpdateDialog.TYPE_UPDATE_FORCE);
					}


				}
			});
		}else{
			//only read from local sp
			int flag = needUpdate(false);
			RuntimeLog.log("needUpdate :" + flag);
			
			if(flag == 1){
				//un forced way				
				checkDialog.initView(CheckUpdateDialog.TYPE_UPDATE_UNFORCE);
			}else if(flag == 2){
				//forced way				
				checkDialog.initView(CheckUpdateDialog.TYPE_UPDATE_FORCE);
			}
			
		}
	}
	
	
	
	/*
	 * 
	 *    return:
	 *    0: no need update
	 *    1: need update  -- > modify the promtRecord SP
	 *    2: must update (forced way)
	 * 
	 * */
	private int needUpdate(boolean force) {
		int localVersionCode = getVersionCode();
		

		int remoteVersionCode = getremoteVersionCode();
		int MinVersionCode = getremoteMinVersionCode();
		
		Date nowdate = new Date();
		

		if(localVersionCode<MinVersionCode){
			return 2;
		}else if(localVersionCode>=remoteVersionCode){
			return 0;
		}else{
			//localVersionCode < remoteVersionCode --> need update
			if(force)
				return 1;
			
			//1. check the last promtDate whether older than today
		    SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getDateInstance();
		    sdf.applyPattern("yyyy-MM-dd");
			
			SharedPreferences sp = mContext.getSharedPreferences("promtRecord", Context.MODE_PRIVATE);
			try{
				Date date = sdf.parse(sp.getString("promtDate", "2000-01-01"));

				
				if(DateUtils.compareDay(nowdate, date)<=0){
					RuntimeLog.log("has promt once today, no-op");
					return 0;
				}
			}catch(ParseException e){
				e.printStackTrace();
			}
			
			//2. if older, then check whether the promtNum is smaller than 3
			int promtNum = sp.getInt("promtNum", 0);
			if(promtNum>3){
				//no need to promt 
				RuntimeLog.log("ApplicationManager - promtNum>3");
				return 0;
			}else{
				promtNum++;
				Editor editor = sp.edit();
				
				//reset promtNum to 0
				editor.putInt("promtNum", promtNum);
				editor.putString("promtDate", sdf.format(nowdate));
				editor.commit();
				
			}
			
			sdf = null;
			return 1;
		}
			
	}
	
	
	public String getVersionName(){
		String pk = mContext.getApplicationInfo().packageName;
		PackageManager pm = mContext.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(pk, PackageManager.GET_ACTIVITIES);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			return null;
		}
	}
	
	public int getVersionCode(){
		String pk = mContext.getApplicationInfo().packageName;
		PackageManager pm = mContext.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(pk, PackageManager.GET_ACTIVITIES);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			return 0;
		}
	}

	
	public int getremoteVersionCode(){
		SharedPreferences sp = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
		return sp.getInt("versionCode", 0);
	}
	
	public String getremoteVersionName(){
		SharedPreferences sp = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
		return sp.getString("versionName", null);
	}
	
	public int getremoteMinVersionCode(){
		SharedPreferences sp = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
		return sp.getInt("minversionCode", 0);
	}
	
	public String getremoteDescription(){
		SharedPreferences sp = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
		return sp.getString("description", null);
	}
	
	public String getremoteDownloadurl(){
		SharedPreferences sp = mContext.getSharedPreferences("version", Context.MODE_PRIVATE);
		return sp.getString("downloadurl", null);
	}
	

}
