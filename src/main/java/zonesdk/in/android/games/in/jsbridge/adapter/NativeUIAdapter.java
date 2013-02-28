package zonesdk.in.android.games.in.jsbridge.adapter;

import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.widget.Toast;
import zonesdk.in.android.games.in.account.ZoneAccount;
import zonesdk.in.android.games.in.activity.ImagePreviewActivity;
import zonesdk.in.android.games.in.dialog.PromptDialog;
import zonesdk.in.android.games.in.dialog.PromptDialog.Builder;
import zonesdk.in.android.games.in.dialog.PromptDialog.OnConfirmListener;
import zonesdk.in.android.games.in.jsbridge.NativeUI;
import zonesdk.in.android.games.in.widget.HikeWebView;
import zonesdk.in.android.games.in.widget.MainViewport;

import static zonesdk.in.android.games.in.widget.MainViewport.*;

public class NativeUIAdapter extends BaseAdapter implements NativeUI {
	
	private MainViewport mViewport;
	
	private ZoneAccount mAccount;
	
	private HikeWebView mWebView;
	
	private PromptDialog mDialog;
	
	private boolean mInited;
	
	public NativeUIAdapter(Activity activity, MainViewport viewport, HikeWebView webView, ZoneAccount account) {
		super(activity);
		mViewport = viewport;
		mAccount = account;
		mWebView = webView;
		mInited = false;
	}

	@Override
	public void setUserInfo(String userName, String headUrl, String coverUrl) {
		mAccount.setUserName(userName);
		mAccount.setHeadUrl(headUrl);
		mAccount.setCoverUrl(coverUrl);
		mViewport.setUserNameText(userName);
		mViewport.setAvatarUrl(headUrl);
		mViewport.setCoverUrl(coverUrl);
	}

	@Override
	public void setNavigator(String navigator) {
		if("back".equals(navigator)){
			mViewport.setNavigation(Navigation.GO_BACK);
		}else if("menu".equals(navigator)){
			mViewport.setNavigation(Navigation.TOGGLE_MENU);
		}else if(navigator == null){
			mViewport.setNavigation(Navigation.NULL);
		}
	}

	@Override
	public void setTool(String tool) {
		if(tool == null){
			mViewport.setTool(Tool.NULL);
		}if("notify".equals(tool)){
			mViewport.setTool(Tool.NOTIFY);
		}else if("setting".equals(tool)){
			mViewport.setTool(Tool.SETTING);
		}else if("search".equals(tool)){
			mViewport.setTool(Tool.SEARCH);
		}
	}

	@Override
	public void setTabs(String names, int activeTab, boolean showTips) {
		mViewport.setTabs(names, activeTab, showTips);
	}

	@Override
	public void setNotifyCount(int friends, int games, int messages, int total) {
		mViewport.setNotifyCount(friends, games, messages, total);
	}

	@Override
	public void setHeader(String title) {
		if("tab".equals(title)){
			mViewport.showHeaderAsTabs();
		} else if (title != null) {
			mViewport.showHeaderAsText(title);
		} else  {
			mViewport.hideHeader();
		}
	}
	
	@Override
	public void requestDialog(JSONObject config) {
		Builder builder = new PromptDialog.Builder(getActivity());
		builder.setTitle(config.optString(CONFIG_DIALOG_TITLE, null));
		builder.setText(config.optString(CONFIG_DIALOG_TEXT, null));
		builder.setEditable(config.optBoolean(CONFIG_DIALOG_EDITABLE, false));
		builder.setContent(config.optString(CONFIG_DIALOG_CONTENT, null));
		builder.setPlaceholder(config.optString(CONFIG_DIALOG_PLACEHOLDER, null));
		builder.setConfirmButton(config.optString(CONFIG_DIALOG_CONFIRM_BUTTON, null));
		builder.setCancelButton(config.optString(CONFIG_DIALOG_CANCEL_BUTTON, null));
		mDialog = builder.create();
		mDialog.setOnCancelListener(mDialogCancelListener);
		mDialog.setOnConfirmListener(mDialogConfirmListener);
		mDialog.show();
	}

	private OnCancelListener mDialogCancelListener = new OnCancelListener(){

		@Override
		public void onCancel(DialogInterface dialog) {
			mViewport.postScript("onDialogCancel", "");
			mDialog = null;
		}
		
	};
	
	private OnConfirmListener mDialogConfirmListener = new OnConfirmListener(){

		@Override
		public void onConfirm(PromptDialog dialog, String content) {
			mViewport.postScript("onDialogConfirm", content);
			mDialog = null;
		}
		
	};
	
	@Override
	public void requestLogout() {}

	@Override
	public void onBeforeRequest() {
		mViewport.showLoading();
	}

	@Override
	public void onRequestSuccess() {
		mViewport.hideLoading();
	}

	@Override
	public void onRequestError() {
		mViewport.hideLoading();
		Toast.makeText(getActivity(), "Network Problem!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBeforeOpenView() {
		if(mInited){
			mViewport.showMask();
			return;
		}
	}

	@Override
	public void onAfterOpenView() {
		if(mInited){
			mViewport.hideMask();
			return;
		}
		mViewport.finishWelcome();
		mInited = true;
	}

	@Override
	public void onAvatarUpload(String url) {
		mViewport.setAvatarUrl(url);
		mAccount.setHeadUrl(url);
		mWebView.loadUrl("javascript:window.onAvatarUploaded('" + url + "')");
	}

	@Override
	public void onCoverUpload(String url) {
		mViewport.setCoverUrl(url);
		mAccount.setCoverUrl(url);
		mWebView.loadUrl("javascript:window.onCoverUploaded('" + url + "')");
	}

	@Override
	public void previewImages(String urls, int index) {
		Intent i = new Intent();
		Activity act = getActivity();
		i.setClass(act, ImagePreviewActivity.class);
		i.putExtra("urls", urls.split("%"));
		i.putExtra("index", index);
		act.startActivity(i);
	}

	@Override
	public void showError(String errorInfo) {
		Toast.makeText(getActivity(), errorInfo, Toast.LENGTH_SHORT).show();
	}

}
