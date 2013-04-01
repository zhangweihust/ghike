package in.android.games.in.db;


import in.android.games.in.utils.RuntimeLog;
import in.android.games.in.widget.GetContactList.MyContacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
import android.util.Log;
public class ContactHelper {  
    private static final String DB_NAME = "contact.db";  
    private static final int DATABASE_VERSION = 1;
    private static final String TBL_NAME = "ContactTable";  
    private static final String CREATE_TBL = " create table "  
            + " ContactTable(_id integer primary key autoincrement, status INTEGER, time INTEGER, phonenumber text) ";
    
    private static final String KEY_ROWID = "_id"; 
    private static final String KEY_STATUS = "status";//0 = sendAlready ,1 = not send
    private static final String KEY_TIME = "time";
    private static final String KEY_PHONENUMBER = "phonenumber";
    private Context mContext = null;
    //////////////////////////////////////////////////////////////
    

    private SQLiteDatabase mSQLiteDatabase = null;
    private DatabaseHelper mDatabaseHelper = null;
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

	        DatabaseHelper(Context context){
	
	           super(context, DB_NAME, null, DATABASE_VERSION);
	           RuntimeLog.log("DatabaseHelper(Context)");
	        }
	
	
	        @Override
	        public void onCreate(SQLiteDatabase db){
	           RuntimeLog.log("onCreate(SQLiteDatabase)");
	           
	           db.execSQL(CREATE_TBL);
	        }
	
	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	
	           db.execSQL("DROP TABLE IF EXISTS ContactTable");
	           onCreate(db);
	        }
     }
    
    public ContactHelper(Context context){
   	RuntimeLog.log("ContactHelper(Context)");
       mContext = context;
    }
    
    public void open(){
       RuntimeLog.log("ContactHelper - open()");
   	   //SQLiteDatabase db = getWritableDatabase();
       mDatabaseHelper = new DatabaseHelper(mContext);
       mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
    }
    
    public void close(){

        mDatabaseHelper.close();
  }
    
    public void insert(ContentValues values) {        
        mSQLiteDatabase.insert(TBL_NAME, null, values);  
        
     }  
    
     public void insert(ArrayList<ContentValues> values) {        

        for(ContentValues value:values){
        	mSQLiteDatabase.insert(TBL_NAME, null, value); 
        }

     } 
    
     public void deleteItem(String phonenumber) {  

    	mSQLiteDatabase.delete(TBL_NAME, "phonenumber="+phonenumber, null);   

     } 
    

    
    public Cursor query() {  
    	
/*		Exception e = new Exception();
		e.printStackTrace();*/
		    
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, null); 
		if (c != null){
		
		   c.moveToFirst();
		}
		
		//db.close();
		return c;
    } 
    
    public Cursor query(String order) {  
    	
/*		Exception e = new Exception();
		e.printStackTrace();*/
		        
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, order); 
		if (c != null){
			c.moveToFirst();
		}
		
		//db.close();
		return c;  
    }  
    
    public int queryStatus(String phonenumber){
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        String[] selectionArgs = new  String[]{ phonenumber};   
       // Cursor c = db.query(TBL_NAME, null, "phonenumber=?", selectionArgs, null, null, null);
        Cursor c = db.rawQuery("select * from ContactTable where phonenumber = ?", selectionArgs);
        
        int index_status;
        int status;
        
        if(c.moveToFirst()){
        	index_status = c.getColumnIndex("status");
        	status = c.getInt(index_status);            
        }else{
        	status = 1;
        }
        
        if(c!=null){
        	c.close();
        }
        
        return status;

      }
    
    public Map<String,Integer> queryStatus(ArrayList<MyContacts> myContacts){
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();  
        Map<String,Integer> resultMap = new HashMap<String, Integer>();
        if(myContacts==null){
            return null;	
        }
        Cursor c = db.query("ContactTable", new String[]{"phonenumber", "status"}, null, null, null, null, null);
        while(c.moveToNext()){
            int index_staus = c.getColumnIndex("status");
            int index_PhoneNum = c.getColumnIndex("phonenumber");
            int status = c.getInt(index_staus);
            String PhoneNum =c.getString(index_PhoneNum);
            resultMap.put(PhoneNum,status);
        }
        filterResult(resultMap, myContacts);
        c.close();
        //db.close();
        return resultMap;
      }
    
     private void filterResult( Map<String,Integer> resultMap, ArrayList<MyContacts> myContacts){
    	 Set<String> removeSet = new HashSet<String>();
    	 k:for(String key:resultMap.keySet()){
    		 for(MyContacts c:myContacts){
    			 if(c.userPhoneNumber.equals(key))
    				 continue k;
    		 }
    		 removeSet.add(key);
    	 }
    	 for(String key:removeSet){
    		 resultMap.remove(key);
    	 }
     }

    
    
    public void deleteItem(int id) {  
/*        if (db == null) { 
            db = getWritableDatabase();  
        }*/
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.delete(TBL_NAME, "_id=?", new String[] { String.valueOf(id) });   
/*        if (db != null){
        	 db.close();
        }*/
    }  
      
    public void deleteAll(){
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.delete(TBL_NAME, null, null);   
    }
        
    public void updateTime(int id, long time){
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
    	ContentValues args = new ContentValues(); 
        args.put(KEY_TIME, time); 
        db.update(TBL_NAME, args, "_id="+id, null);
/*        if (db != null){
            db.close();
        }*/
    	
    }
    
    public void updateTime(String phonenumber, long time){

    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
    	ContentValues args = new ContentValues(); 
        args.put(KEY_TIME, time); 
        db.update(TBL_NAME, args, "phonenumber='"+phonenumber+"'", null);
/*        if (db != null){
            db.close();
        }*/
    	
    }
    
    public void updateStatus(int id, int status){    	
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase(); 
    	//db.beginTransaction();
    	ContentValues values = new ContentValues();    	
    	values.put(KEY_STATUS, status); 
    	db.update(TBL_NAME, values, "_id="+id, null);
        //db.setTransactionSuccessful();
        //db.endTransaction();
/*    	if (db != null){   		
    		db.close();
    	}*/
    	    	
    }
    
    public void updateStatus(String phonenumber, int status){    	
    	SQLiteDatabase db = mDatabaseHelper.getWritableDatabase(); 
    	//db.beginTransaction();
    	ContentValues values = new ContentValues();    	
    	values.put(KEY_STATUS, status); 
    	db.update(TBL_NAME, values, "phonenumber='"+phonenumber+"'", null);
        //db.setTransactionSuccessful();
        //db.endTransaction();
/*    	if (db != null){
    		db.close();
    	}*/
    	    	
    }
    
   
/*    public void destroyDataBase(){
    	mContext.deleteDatabase(DB_NAME);
    	
    }*/
} 
