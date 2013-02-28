package zonesdk.in.android.games.in.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GameHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "game.db";
	
	private static final int DATABASE_VERSION = 4;
	
	private static final String TBL_NAME = "GameTable";
	
	private static final String CREATE_TBL = " create table "
			+ " GameTable(_id integer primary key autoincrement, app_id INTEGER, forum_id INTEGER, appIcon text , appIconCover text, title text, create_time text, description text, platform_type INTEGER, package_name text, download_url text, apk_size double, enable_screenshot INTEGER) ";

	private static final String KEY_ROWID = "_id";
	private static final String KEY_APP_ID = "app_id";
	private static final String KEY_FORUM_ID = "forum_id";
	private static final String KEY_APPICON = "appIcon";
	private static final String KEY_APPICON_COVER = "appIconCover";
	private static final String KEY_TITLE = "title";
	private static final String KEY_CREATE_TIME = "create_time";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_PLATFORM_TYPE = "platform_type";
	private static final String KEY_PACKAGE_NAME = "package_name";
	private static final String KEY_DOWNLOAD_URL = "download_url";
	private static final String KEY_APK_SIZE = "apk_size";
	private static final String KEY_ENABLE_SCREENSHOT = "enable_screenshot";

	private static List<Map<String, Object>> sCachedGames;
	
	private static boolean sIsDirty = true;

	static {
		sCachedGames = new Vector<Map<String, Object>>();
	}

	private Context mContext = null;

	public GameHelper(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		synchronized(GameHelper.class){
			db.execSQL(CREATE_TBL);
			sIsDirty = true;
		}
	}

	public void insert(ContentValues values) {
		synchronized(GameHelper.class){
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			db.insert(TBL_NAME, null, values);
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			sIsDirty = true;
		}
	}
	
		
	public void insert(ArrayList<ContentValues> values) {
		synchronized(GameHelper.class){
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			for(ContentValues value: values){
				db.insert(TBL_NAME, null, value);
			}
			
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			sIsDirty = true;
		}
	}

	public void deleteItem(int id) {
		synchronized(GameHelper.class){
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			db.delete(TBL_NAME, "_id=" + id, null);
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			sIsDirty = true;
		}
		
	}

	public void deleteAll() {
		synchronized(GameHelper.class){
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			db.delete(TBL_NAME, " 1=1 ", null);
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			sIsDirty = true;
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		synchronized(GameHelper.class){
			db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
			onCreate(db);
			sIsDirty = true;
		}
	}

	public void destroyDataBase() {
		synchronized(GameHelper.class){
			mContext.deleteDatabase(DB_NAME);
			sIsDirty = true;
		}
	}

	public List<String> getAllPackageName() {
		prepareGames();
		ArrayList<String> names = new ArrayList<String>();
		for(Map<String, Object> game:sCachedGames){
			names.add((String)game.get("packageName"));
		}
		return names;
	}

	public Map<String, Object> getGameInfoByPackageName(String packageName) {
		prepareGames();
		for(Map<String, Object> game:sCachedGames){
			if(packageName.equals(game.get("packageName"))){
				return game;
			}
		}
		return null;
	}
	
	public List<Map<String, Object>> getAllGames(){
		prepareGames();
		return Collections.unmodifiableList(sCachedGames);
	}

	private void prepareGames() {
		if(!sIsDirty)
			return;
		synchronized(GameHelper.class){
			Log.d("GameHelper", "Reload games from database");
			sCachedGames.clear();
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query(TBL_NAME, null, " platform_type = 1 ", null, null, null, null);
			while (c.moveToNext()) {
				Map<String, Object> game = new HashMap<String, Object>();
				game.put("appId", c.getInt(c.getColumnIndex(KEY_APP_ID)));
				game.put("forumId", c.getInt(c.getColumnIndex(KEY_FORUM_ID)));
				game.put("appIcon", c.getString(c.getColumnIndex(KEY_APPICON)));
				game.put("appIconCover",
						c.getString(c.getColumnIndex(KEY_APPICON_COVER)));
				game.put("title", c.getString(c.getColumnIndex(KEY_TITLE)));
				game.put("createTime", c.getString(c.getColumnIndex(KEY_CREATE_TIME)));
				game.put("description",
						c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
				game.put("platformType",
						c.getInt(c.getColumnIndex(KEY_PLATFORM_TYPE)));
				game.put("packageName",
						c.getString(c.getColumnIndex(KEY_PACKAGE_NAME)));
				game.put("downloadUrl",
						c.getString(c.getColumnIndex(KEY_DOWNLOAD_URL)));
				game.put("apkSize", c.getDouble(c.getColumnIndex(KEY_APK_SIZE)));
				game.put("enableScreenshot",
						c.getInt(c.getColumnIndex(KEY_ENABLE_SCREENSHOT)));
				sCachedGames.add(game);
			}
			c.close();
			db.close();
			sIsDirty = false;
		}
	}
}
