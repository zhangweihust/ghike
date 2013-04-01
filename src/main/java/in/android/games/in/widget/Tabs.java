package in.android.games.in.widget;

import zonesdk.in.android.games.in.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Tabs extends LinearLayout{
	
	private LayoutInflater inflater;
	
	private Typeface typeface;
	
	private OnTabChangeListener listener;

	private int friendUnread;
	
	private int gameUnread;
	
	private int messageUnread;
	
	private boolean showTip;
	
	public Tabs(Context context) {
		super(context);
	}
	
	public Tabs(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate(){
		super.onFinishInflate();
		typeface = Typeface.createFromAsset(getContext().getAssets(), "font/Edmondsans-Regular.otf");
		inflater = LayoutInflater.from(getContext());
	}
	
	void setTabs(String[] names, int activeTab, boolean showTip){
		setVisibility(INVISIBLE);
		removeAllViews();
		this.showTip = showTip;
		for(int i=0;i<names.length;i++){
			final int tabIndex = i;
			String name = names[i];
			View tab = inflater.inflate(R.layout.hike_main_topbar_tab, null);
			TextView tabText = (TextView)tab.findViewById(R.id.topbar_tab_text);
			TextView tabTip = (TextView)tab.findViewById(R.id.topbar_tab_tip);
			tabText.setTypeface(typeface);
			tabText.setText(name);
			tab.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					onTabClick(tabIndex);
				}
			});
			if(i == activeTab){
				tabText.setBackgroundResource(R.color.topbar_tab_bg_highlight);
			}
			if(showTip){
				int count = 0;
				switch(i){
				case 0:{
					count = friendUnread;
					break;
				}
				case 1:{
					count = gameUnread;
					break;
				}
				case 2:{
					count = messageUnread;
					break;
				}
				}
				tabTip.setText(String.valueOf(count));
				tabTip.setVisibility(showTip && count > 0?VISIBLE:GONE);
			}
			addView(tab);
		}
		setVisibility(VISIBLE);
	}
	
	private void onTabClick(int tabIndex){
		if(!listener.onTabChange(tabIndex)){
			return;
		}
		int cc = getChildCount();
		for(int i=0;i<cc;i++){
			View tab = getChildAt(i);
			TextView tabText = (TextView)tab.findViewById(R.id.topbar_tab_text);
			if(i == tabIndex)
				tabText.setBackgroundResource(R.color.topbar_tab_bg_highlight);
			else
				tabText.setBackgroundResource(R.color.topbar_tab_bg);
		}
	}
	
	void setNotifyCount(int friend, int game, int message){
		friendUnread = friend;
		gameUnread = game;
		messageUnread = message;
		updateNotifyCount();
	}
	
	private void updateNotifyCount(){
		int cc = getChildCount();
		if(cc != 3)
			return;
		updateNotifyTip((TextView)getChildAt(0).findViewById(R.id.topbar_tab_tip), friendUnread);
		updateNotifyTip((TextView)getChildAt(1).findViewById(R.id.topbar_tab_tip), gameUnread);
		updateNotifyTip((TextView)getChildAt(2).findViewById(R.id.topbar_tab_tip), messageUnread);
	}
	
	private void updateNotifyTip(TextView view, int count){
		view.setText(String.valueOf(count));
		view.setVisibility(showTip && count > 0?VISIBLE:GONE);
	}
	
	void setOnTabChangeListener(OnTabChangeListener listener){
		this.listener = listener;
	}
	
	public static interface OnTabChangeListener{
		
		public boolean onTabChange(int tabIndex);
		
	}
	
}
