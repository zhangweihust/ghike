package zonesdk.in.android.games.in.jsbridge;

import org.json.JSONObject;

public interface NativeUI {

	String CONFIG_DIALOG_TITLE = "title";
	
	String CONFIG_DIALOG_TEXT = "text";
	
	String CONFIG_DIALOG_EDITABLE = "editable";
	
	String CONFIG_DIALOG_CONTENT = "content";
	
	String CONFIG_DIALOG_PLACEHOLDER = "placeHolder";
	
	String CONFIG_DIALOG_CONFIRM_BUTTON = "confirmButton";
	
	String CONFIG_DIALOG_CANCEL_BUTTON = "cancelButton";
	
	void setUserInfo(String userName, String headUrl, String coverUrl);
	
	void setNavigator(String navigator);
	
	void setTool(String button);
	
	void setTabs(String names, int activeTab, boolean showTips);
	
	void setNotifyCount(int friends, int games, int messages, int total);
	
	void setHeader(String title);
	
	void requestDialog(JSONObject config);
	
	void requestLogout();
	
	void onBeforeRequest();
	
	void onRequestSuccess();
	
	void onRequestError();
	
	void onBeforeOpenView();
	
	void onAfterOpenView();

	void onAvatarUpload(String url);

	void onCoverUpload(String url);
	
	void previewImages(String urls, int index);
	
	void showError(String errorInfo);
	
}
