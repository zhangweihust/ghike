package zonesdk.in.android.games.in.fragment;

import zonesdk.in.android.games.in.widget.MainViewport.Tool;

public enum ToolAction{
	
	NOTIFY(Tool.NOTIFY), SETTING(Tool.SETTING), SERACH(Tool.SEARCH), NULL(Tool.NULL);
	
	private Tool mTool;
	
	ToolAction(Tool tool){
		mTool = tool;
	}
	
	Tool getTool(){
		return mTool;
	}
	
}

