package in.android.games.in.widget;

import in.android.games.in.jsbridge.PaymentCallback;
import in.android.games.in.utils.ImageUtil;
import in.android.games.in.utils.Thumbnail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONObject;

import zonesdk.in.android.games.in.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainViewport extends DrawerLayout implements Tabs.OnTabChangeListener, PaymentCallback{
	
	private static MenuItem[] ITEMS = {MenuItem.TOP_GAMES, MenuItem.RECENTLY_PLAYED, MenuItem.TIMELINE, MenuItem.FRIENDS, MenuItem.PROFILE, MenuItem.ABOUT};
	
	public enum MenuItem {
		
		TOP_GAMES(R.string.nav_top_games, R.drawable.menu_item_top_games, "javascript:UIProxy.openTopGames()"),
		
		RECENTLY_PLAYED(R.string.nav_my_games, R.drawable.menu_item_my_games, "javascript:UIProxy.openMyGames()"),
		
		TIMELINE(R.string.nav_timeline, R.drawable.menu_item_timeline, "javascript:UIProxy.openTimeline()"),
		
		FRIENDS(R.string.nav_friends, R.drawable.menu_item_friends, "javascript:UIProxy.openMyFriends()"),
		
		PROFILE(R.string.nav_profile, R.drawable.menu_item_profile, "javascript:UIProxy.openProfile()"),
		
		ABOUT(R.string.nav_about, R.drawable.menu_item_about, "javascript:UIProxy.openAbout()");
		
		private int resid;
		
		private int textid;
		
		private String script;
		
		private MenuItem(int textid, int resid, String script){
			this.resid = resid;
			this.textid = textid;
			this.script = script;
		}
		
		public int getText(){
			return textid;
		}
		
		public int getDrawable(){
			return resid;
		}
		
		public String getScript(){
			return script;
		}
		
	}
	
	public enum Navigation {
		
		NULL(0),
		
		TOGGLE_MENU(R.drawable.nav_toggle_menu),
		
		GO_BACK(R.drawable.nav_back);
		
		private int resid;
		
		private Navigation(int resid){
			this.resid = resid;
		}
		
		public int getDrawable(){
			return resid;
		}
		
	}
	
	public enum Tool {
		
		NULL(null, -1),
		
		NOTIFY("javascript:UIProxy.openNotify()", R.drawable.tool_notify),
		
		SEARCH("javascript:UIProxy.openSearch()", R.drawable.tool_search),
		
		SETTING("javascript:UIProxy.openSetting()", R.drawable.tool_setting) ;
		
		private String script;
		
		private int resid;
		
		private Tool(String script, int resid){
			this.script = script;
			this.resid = resid;
		}
		
		public String getScript(){
			return script;
		}
		
		public int getDrawable(){
			return resid;
		}
		
	}

	private ImageView navigatorButton;
	
	private ImageView toolButton;
	
	private TextView headerText;
	
	private ImageView headerIcon;
	
	private HikeWebView webView;
	
	private URLImageView userAvatar;
	
	private TextView userNameView;

	private TextView notifyTip;
	
	private ListView menuList;
	
	private Navigation navigation = Navigation.NULL;
	
	private Tool tool = Tool.NULL;
	
	private Typeface typeface;
	
	private Tabs tabs;
	
	private View loader;
	
	private View masker;
	
	private View userProfile;
	
	private View mWelcome;
	
	private Animation maskAnim;
	
	private Animation loaderAnim;
	
	private boolean blocking;
	
	private LayoutInflater mInflater;
	
	public MainViewport(Context context) {
		super(context);
	}
	
	public MainViewport(Context context, AttributeSet set) {
		super(context, set);
		
	}
	
	public MainViewport(Context context, AttributeSet set, int style) {
		super(context, set, style);
	}
	
	@Override
	protected void onFinishInflate(){
		super.onFinishInflate();
		typeface = Typeface.createFromAsset(getContext().getAssets(), "font/Edmondsans-Regular.otf");
		
		toolButton = (ImageView) findViewById(R.id.toolbar_tool_button);
		toolButton.setVisibility(View.INVISIBLE);
		toolButton.setOnClickListener(mViewClickListener);
		
		headerText = (TextView) findViewById(R.id.toolbar_header_text);
		headerText.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(), "font/Edmondsans-Regular.otf"));
		
		headerIcon = (ImageView) findViewById(R.id.toolbar_hike_icon);
		
		webView = (HikeWebView) findViewById(R.id.main_web_view);
		
		tabs = (Tabs) findViewById(R.id.toolbar_tabs);
		tabs.setOnTabChangeListener(this);

		userAvatar = (URLImageView) findViewById(R.id.userAvatar);
		userNameView = (TextView) findViewById(R.id.userName);
		userNameView.setTypeface(typeface);

		navigatorButton = (ImageView) findViewById(R.id.toolbar_navigator_button);
		navigatorButton.setVisibility(View.INVISIBLE);
		navigatorButton.setOnClickListener(mViewClickListener);

		notifyTip = (TextView)findViewById(R.id.topbar_notify_tip);

		menuList = (ListView) findViewById(R.id.main_menu_list);
		menuList.setOnItemClickListener(mMenuClickListener);
		menuList.setAdapter(mItemAdapter);

		loader = findViewById(R.id.main_loader);
		loader.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return blocking;
			}
			
		});
		
		masker = findViewById(R.id.main_masker);
		
		maskAnim = new AlphaAnimation(1, 0);
		maskAnim.setDuration(200);
		maskAnim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				masker.setVisibility(GONE);
				blocking = false;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {
				blocking = true;
			}
			
		});
		
		loaderAnim = AnimationUtils.loadAnimation(getContext(), R.anim.loader_rotate);    
		ImageView img = (ImageView)loader.findViewById(R.id.main_loader_img);
		img.setAnimation(loaderAnim);
		
		userProfile = findViewById(R.id.userProfile);
		mWelcome = findViewById(R.id.main_welcome);
		mInflater = LayoutInflater.from(getContext());
	}
	
	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		loader.measure(w, h);
		masker.measure(w, h);
		mWelcome.measure(w, h);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		loader.layout(left, top, right, bottom);
		masker.layout(left, top, right, bottom);
		mWelcome.layout(0, 0, right, bottom);
	}
	
	public void finishWelcome(boolean smoothly){
		if(mWelcome.getVisibility() == View.GONE)
			return;
		if(mWelcome.getAnimation() != null)
			return;
		if(!smoothly){
			mWelcome.setVisibility(View.GONE);
			return;
		}
		Animation anim = new AlphaAnimation(1, 0);
		anim.setDuration(750);
		anim.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				mWelcome.setVisibility(View.GONE);
				requestLayout();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
			
		});
		mWelcome.startAnimation(anim);
	}
	
	public void finishWelcome(){
		finishWelcome(true);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent evt){
		if(blocking)
			return true;
		else return super.onInterceptTouchEvent(evt);
	}
	
	public void hideHeader(){
		headerIcon.setVisibility(VISIBLE);
		headerText.setVisibility(GONE);
		tabs.setVisibility(GONE);
		mMainView.requestLayout();
	}
	
	public void showHeaderAsText(String text){
		headerText.setText(text);
		headerText.setVisibility(VISIBLE);
		headerIcon.setVisibility(GONE);
		tabs.setVisibility(GONE);
		mMainView.requestLayout();
	}
	
	public void showHeaderAsTabs(){
		headerText.setVisibility(GONE);
		headerIcon.setVisibility(GONE);
		tabs.setVisibility(VISIBLE);
		mMainView.requestLayout();
	}
	
	private OnClickListener mViewClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if(view == navigatorButton){
				onNavigatorClick();
			}else if(view == toolButton){
				onToolButtonClick();
			}
		}
		
	};
	
	private OnItemClickListener mMenuClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final MenuItem item = (MenuItem)mItemAdapter.getItem(position);
			final String script = item.getScript();
			if(script == null)
				return;
			closeDrawer(new SlideFinishedCallback(){

				@Override
				public void onSlideFinished(DrawerLayout layout) {
					webView.loadUrl(script);
				}
				
			});
		}
	};
	
	public void invokeAction(final String action, final JSONObject dataset){
		if(isLeftDrawerOpened()){
			closeDrawer(new SlideFinishedCallback(){
	
				@Override
				public void onSlideFinished(DrawerLayout layout) {
					webView.invokeAction(action, dataset);
				}
				
			});
		}else{
			webView.invokeAction(action, dataset);
		}
	}
	
	private void onNavigatorClick(){
		if(navigation == Navigation.NULL)
			return;
		switch(navigation){
		case GO_BACK:{
			if(!blocking)
				webView.loadUrl("javascript:hike.backward();");
			break;
		}
		case TOGGLE_MENU:{
			toggleLeftMenu();
			break;
		}
		}
	}
	
	private void onToolButtonClick(){
		if(tool == Tool.NULL)
			return;
		if(!blocking)
			webView.loadUrl(tool.getScript());
	}

	public void postScript(String function, JSONObject json){
		webView.loadUrl("javascript:" + function + "(" + json.toString() + ")");
	}

	public void postScript(String function, String params){
		webView.loadUrl("javascript:" + function + "(\'" + params + "\')");
	}

	public boolean requestBackward(){
		if(blocking){
			return true;
		}
		if(navigation == Navigation.GO_BACK){
			webView.loadUrl("javascript:hike.backward();");
			return true;
		}else if(isLeftDrawerOpened()){
			closeDrawer();
			return true;
		}
		return false;
	}
	
	public boolean requestMenu(){
		if(navigation != Navigation.TOGGLE_MENU)
			return false;
		if(!blocking)
			toggleLeftMenu();
		return true;
	}
	
	public void setUserNameText(String userName){
		userNameView.setText(userName);
	}
	
	public void setAvatarUrl(String url){
		if(url == null)
			return;
		userAvatar.loadFromUrl(url);
	}
	
	public void setCoverUrl(String url){
		loadCoverFromURL(url);
	}
	
	public void setNavigation(Navigation navigation){
		this.navigation = navigation;
		if(navigation == Navigation.NULL){
			navigatorButton.setVisibility(View.INVISIBLE);
			return;
		}
		navigatorButton.setVisibility(View.VISIBLE);
		navigatorButton.setImageDrawable(this.getResources().getDrawable(navigation.getDrawable()));
	}
	
	public void setTool(Tool tool){
		this.tool = tool;
		if(tool == Tool.NOTIFY && !"0".equals(notifyTip.getText().toString())){
			notifyTip.setVisibility(VISIBLE);
		}else{
			notifyTip.setVisibility(GONE);
		}
		if(tool == Tool.NULL){
			toolButton.setVisibility(INVISIBLE);
			return;
		}
		toolButton.setImageDrawable(this.getResources().getDrawable(tool.getDrawable()));
		toolButton.setVisibility(VISIBLE);
		mMainView.requestLayout();
	}

	public void setTabs(String names, int activeTab, boolean showTips) {
		tabs.setTabs(names.split("_"), activeTab, showTips);
		tabs.setVisibility(VISIBLE);
		headerText.setVisibility(GONE);
		headerIcon.setVisibility(GONE);
	}

	@Override
	public boolean onTabChange(int tabIndex) {
		if(blocking)
			return false;
		webView.loadUrl("javascript:UIProxy.setActiveTab(" + tabIndex + ")");
		webView.scrollTo(0, 0);
		return true;
	}

	public void setNotifyCount(int friends, int games, int messages, int total) {
		tabs.setNotifyCount(friends, games, messages);
		notifyTip.setText(String.valueOf(total));
		if(tool != Tool.NOTIFY){
			notifyTip.setVisibility(GONE);
		}else if(total > 0){
			notifyTip.setVisibility(VISIBLE);
		}else {
			notifyTip.setVisibility(GONE);
		}
	}
	
	public void showLoading(){
		showLoader();
		blocking = true;
	}
	
	public void hideLoading(){
		hideLoader();
		blocking = false;
	}
	
	public void showMask(){
		masker.setVisibility(VISIBLE);
		blocking = true;
	}
	
	public void hideMask(){
		masker.startAnimation(maskAnim);
	}
	
	private void loadCoverFromURL(String urlStr){
		if(urlStr == null){
			userProfile.setBackgroundResource(R.drawable.menu_profile_bg);
			return;
		}
		try {
			new ImageLoadTask().execute(new URL(urlStr));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private void showLoader(){
		loader.setVisibility(VISIBLE);
		ImageView img = (ImageView)loader.findViewById(R.id.main_loader_img);
		img.startAnimation(loaderAnim);
	}
	
	private void hideLoader(){
		loader.setVisibility(GONE);
	}
	
	private class ImageLoadTask extends AsyncTask<URL, Void, Bitmap>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(URL... params) {
			URL url = params[0];
			ImageUtil util = new ImageUtil(getContext());
			Bitmap bitmap = util.loadFromCache(url);
			if(bitmap != null)
				return bitmap;
			try {
				return util.loadFromHttp(url);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if(result == null)
				return;
			BitmapDrawable drawable = new BitmapDrawable(scaleForUserInfo(result));
			drawable.setGravity(android.view.Gravity.FILL);
			userProfile.setBackgroundDrawable(drawable);
		}
		
		private Bitmap scaleForUserInfo(Bitmap src){
			return Thumbnail.extract(src, userProfile.getWidth(), userProfile.getHeight());
		}
		
	}
	
	private BaseAdapter mItemAdapter = new BaseAdapter(){
		
		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public int getCount() {
			return ITEMS.length;
		}

		@Override
		public Object getItem(int position) {
			return ITEMS[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView menuItem;
			if (convertView == null) {		
				menuItem = (TextView)mInflater.inflate(R.layout.menu_item, null);
				menuItem.setTypeface(typeface);
			}else {		
				menuItem = (TextView) convertView;			
			}
			menuItem.setText(ITEMS[position].getText());
			menuItem.setBackgroundResource(ITEMS[position].getDrawable());
			return menuItem;
		}
		
	};

	@Override
	public void onDialogGoToCharge(Dialog dialog, String button, String message) {
		webView.loadUrl("javascript:window.onDialogGoToCharge('" + message + "')");
	}

	@Override
	public void onCancelPayment() {
	}
	
}
