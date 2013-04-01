package in.android.games.in.jsbridge.adapter;

import in.android.games.in.db.GameHelper;
import in.android.games.in.jsbridge.LocalGames;
import in.android.games.in.jsbridge.adapter.BaseAdapter;
import in.android.games.in.utils.ComparatorLoacalGameUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

public class LocalGamesAdapter extends BaseAdapter implements LocalGames{
	
	private GameHelper mGameDB;
	
	private Context mContext;
	
	public LocalGamesAdapter(Activity activity){
		super(activity);
		mGameDB = new GameHelper(activity);
		mContext = activity;
	}
   
	public String getAllGameJson(){
		List<JSONObject> games;
		games = mapToJson(mGameDB.getAllGames());
		JSONArray object = new JSONArray(games);
		return object.toString();
	}
	

	public String getAllInstalledGameJson(){
		List<JSONObject> games;
		games = mapToJson(mGameDB.getAllGames());
		try {
			fileterInstalled(games);
			JSONArray array = new JSONArray(games);
			return array.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "[]";
	}
	
	private void fileterInstalled(List<JSONObject> games) throws JSONException{
		PackageManager pm = mContext.getPackageManager();
		for(int i=0;i<games.size();){
			JSONObject game = games.get(i);
			String packageName = game.getString("packageName");
			try{
				pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
				i ++ ;
			}catch(Exception e){
				games.remove(i);
			}
		}
		Collections.sort(games,new ComparatorLoacalGameUtil());
	}
	
	private List<JSONObject> mapToJson(List<Map<String, Object>> gameMaps){
		List<JSONObject> list = new ArrayList<JSONObject>();
		for(Map<String, Object> game:gameMaps){
			list.add(new JSONObject(game));
		}
		return list;
	}
	
}
