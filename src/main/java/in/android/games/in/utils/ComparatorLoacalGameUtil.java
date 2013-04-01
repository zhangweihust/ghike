package in.android.games.in.utils;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

public class ComparatorLoacalGameUtil implements Comparator<JSONObject>{
	@Override
	public int compare(JSONObject jsonGame1, JSONObject jsonGame2) {
		int flag = 0;
		try {
			flag = jsonGame1.getString("title").toLowerCase().compareTo(jsonGame2.getString("title").toLowerCase());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return flag;
	}

}
