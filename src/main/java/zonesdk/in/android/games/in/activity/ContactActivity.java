package zonesdk.in.android.games.in.activity;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;

import zonesdk.in.android.games.in.R;
import zonesdk.in.android.games.in.db.ContactHelper;
import zonesdk.in.android.games.in.proxy.InviteUserClient;
import zonesdk.in.android.games.in.utils.ComparatorContactList;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.widget.GetContactList;
import zonesdk.in.android.games.in.widget.GetContactList.MyContacts;
import zonesdk.in.android.games.in.widget.InviteListAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactActivity extends BaseActivity{
	
	private static final String TAG = "ContactActivity";
	public  Activity mActivity = this;
	private View mCurrentView;
	private InviteFriendsViewWrapper mInviteFriendsViewWrapper;
	private InviteFriendsListViewWrapper mInviteFriendsListViewWrapper;
	public static boolean checked = true;
	private Handler mHandler;
	public boolean isAllSendOk = false;
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;
	public static final int PEOPLEISNULL = 2;
	public ContactHelper mContactHelper = null;
	public ArrayList<MyContacts> myContacts = null;
	public static final int time_interval = 24*60*60*1000;//24hours
	public ProgressDialog mProgressDialog = null;
	public static boolean isSendFromAllCheckBox = false;
	
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//delete title
		setContentView();
		
		RuntimeLog.log("ContactActivity.onCreate()");
		
		initView();
		mInviteFriendsViewWrapper.active();	
		
		
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		RuntimeLog.log("ContactActivity.onResume()");
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		RuntimeLog.log("ContactActivity.onPause()");
	}
	
	public void setContentView(){
		setContentView(R.layout.contact);
	}
	
	public void initView(){
		mInviteFriendsViewWrapper = new InviteFriendsViewWrapper();
		mInviteFriendsListViewWrapper = new InviteFriendsListViewWrapper();
		
		if (mContactHelper != null){
			mContactHelper.close();
			mContactHelper = null;
			
		}
		mContactHelper = new ContactHelper(this);	
		mContactHelper.open();
		
	}
	
	public abstract class ViewWrapper {
		final View thisView;
		final ViewWrapper thisViewWrapper;
		private boolean inited = false;
		
		public ViewWrapper(int viewId){
			thisView = mActivity.findViewById(viewId);
			thisViewWrapper = this;
		}
		
		public void active(){
			Log.w(TAG,"active");
        
			if(!inited){
				Log.w(TAG,"active1");
		        init();
				inited = true;
			}
			switchCurrentView(thisView);			
		}
		

		
		public void switchCurrentView(View next){
			Log.w(TAG,"switchCurrentView");

			if (mCurrentView != null){
				mCurrentView.setVisibility(View.INVISIBLE);
				next.setVisibility(View.VISIBLE);
				mCurrentView = next;
			}else{
				next.setVisibility(View.VISIBLE);
				mCurrentView = next;
			}
		}
		
		public abstract void init();
		
		
		public boolean isActive(){
			return (thisView == mCurrentView);
		}
	}
	
	public class InviteFriendsViewWrapper extends ViewWrapper{
		private final TextView textview1 = (TextView)findViewById(R.id.invitefriends_textview1);
		private final TextView textview2 = (TextView)findViewById(R.id.invitefriends_textview2);
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");
		public InviteFriendsViewWrapper(){
			super(R.id.invitefriendsView);
			textview1.setTypeface(typeFace);
			textview2.setTypeface(typeFace);

		}
		@Override
		public void init() {
			Log.w(TAG, "init() begin.");
			 Button useContactBtn =(Button)mActivity.findViewById(R.id.invitefriends_button1);
			 Button notNowBtn =(Button)mActivity.findViewById(R.id.invitefriends_button2);
			 ImageButton backImageBtn = (ImageButton)mActivity.findViewById(R.id.invitefriends_left_button);
			 

			
			useContactBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					   Log.w(TAG, "is clicked.");					   
					   			
					   mInviteFriendsListViewWrapper.active();

				}
			});
			
			notNowBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActivity.finish();
					
				}
			});
			
			backImageBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mActivity.finish();
					
				}
			});			
			
		}
		


	}
    //private CheckBox selectAllCheckBox ;
	private InviteListAdapter mInviteListAdapter;
	public class InviteFriendsListViewWrapper extends ViewWrapper{
		private final TextView head_textview = (TextView)findViewById(R.id.invitefriends_list_textview);
		private final TextView select_textview = (TextView)findViewById(R.id.signtext);
		private final TextView nocontact_textview = (TextView)findViewById(R.id.nocontactsfind);
		private final ImageView nocontact_imageview = (ImageView)findViewById(R.id.nocontactsfindicon);

        Typeface typeFace = Typeface.createFromAsset(getAssets(),"font/Edmondsans-Regular.otf");		
		public InviteFriendsListViewWrapper(){
			super(R.id.invitefriendslistView);
			head_textview.setTypeface(typeFace);
			select_textview.setTypeface(typeFace);
			nocontact_textview.setTypeface(typeFace);
			}
		
		public ListView inviteListView = null;
	    ArrayList<MyContacts> readyToSendContact = new ArrayList<MyContacts>();

		@Override
		public void init() {
			Log.w(TAG, "init() begin.");
		    inviteListView = (ListView)mActivity.findViewById(R.id.invitefriends_list);
		     //final ImageButton sendAllBtn = (ImageButton)mActivity.findViewById(R.id.invitefriends_list_btn_right);
			 //selectAllCheckBox = (CheckBox)mActivity.findViewById(R.id.invitefriends_list_checkBox);
			 ImageButton listBackImageBtn = (ImageButton)mActivity.findViewById(R.id.invitefriends_list_btn_left);			 
			 //final TextView selectTextView = (TextView)mActivity.findViewById(R.id.signtext);
		     //selectTextView.setText(R.string.select);
		     
		     showProgressDialog("Get Contacts");		     
		     setHandler();
		     
		     new Thread(new Runnable(){
		    	 public void run() {
				     setContactData();
				     mHandler.post(new Runnable(){
						@Override
						public void run() {
						     setInviteListView();
				        	 mProgressDialog.dismiss();	
				        	 mProgressDialog = null;
						}
				    	 
				     });      	 
		         }
		     }).start();
		
		listBackImageBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				mActivity.finish();
				
			}
		});
		}
		public void showProgressDialog(String title){
			mProgressDialog = new ProgressDialog(mActivity);
       	    mProgressDialog.setTitle(title);
       	    mProgressDialog.setMessage("Please Wait...");
       	    mProgressDialog.setIndeterminate(false);
       	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
       	    mProgressDialog.setCancelable(false);   
       	    mProgressDialog.getCurrentFocus();
       	    mProgressDialog.show();
			
		}
		

		
		public void setHandler(){
			mHandler = new Handler();
/*			mHandler = new Handler(){
				 @Override
				 public void handleMessage(Message msg){
					 
					 switch (msg.what) {
					case SUCCESS:	
						Cursor cursor = mContactHelper.query();
	                	//Toast.makeText(mActivity, R.string.sentsucess, Toast.LENGTH_LONG).show();							
	                	Log.w(TAG,"success count != 0");
	                	if (cursor.moveToFirst()){
	                		int count = cursor.getCount();
	                		if (count != 0){
	                			Log.w(TAG,"success count = " + count);
	                			for (int i = 0; i < count; i++){
	                				cursor.moveToPosition(i);
	                				int number_column = cursor.getColumnIndex("phonenumber");
	                				String phonenumber = cursor.getString(number_column);
	                				int size = readyToSendContact.size();
	                				Log.w(TAG,"readyToSendContac count successs = " + count);
	                				
	                				for (int j = 0; j < size; j++){
	                					String contact_phonenumber = readyToSendContact.get(j).userPhoneNumber;
	                					if (phonenumber.equals(contact_phonenumber)){
	                						mContactHelper.updateStatus(i+1, 0);    	                				
	                						mContactHelper.updateTime(i+1, getCurrentTime());	    	                			   	                						
	                					}	                					
	                				}	                				
	                			}
	                			
	                		}
	                		
	                		
	                		
	                	}
	                	
	                	if(cursor!=null){
	                		cursor.close();
	                	}
						Toast.makeText(mActivity, R.string.sentsucess, Toast.LENGTH_SHORT).show();
						
						//mContactHelper.close();
	                	readyToSendContact.clear();
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
	                	mActivity.finish();
						break;
					case PEOPLEISNULL:
	                	Log.w(TAG,"PEOPLEISNULL");
						Toast.makeText(mActivity, R.string.sentfail, Toast.LENGTH_SHORT).show();

						//mContactHelper.close();
						mProgressDialog.dismiss();
						mProgressDialog = null;
						mActivity.finish();
						break;
					case CHANGE_NOTIFY:
						Log.d("=================", "========CHANGE_NOTIFY=========");
						//selectAllCheckBox.setChecked(mInviteListAdapter.isSelectAll());
					break;

					default:
	                	Log.w(TAG,"default");
	                	Toast.makeText(mActivity, R.string.sentfail, Toast.LENGTH_SHORT).show();	
                		int count = cursor.getCount();
                		if (count != 0){
                			Log.w(TAG, "count != 0");
                			for (int i = 0; i < count; i++){
                				
                				mContactHelper.updateStatus(i+1, 1);
                			}
                			
                		}
						if (cursor != null){
							cursor.close();
							cursor = null;
						}
						//mContactHelper.close();
                		readyToSendContact.clear();        			    
                		mActivity.finish();
						break;
					}
					 
				 }
				 
			 };*/
			
		}
	
		public void setInviteListView(){
			InviteUserClient  inviteuser = getProxy(InviteUserClient.class);
			mInviteListAdapter = new InviteListAdapter(mActivity, mHandler, myContacts, mContactHelper,inviteuser);
			inviteListView.setCacheColorHint(0);
			inviteListView.setAdapter(mInviteListAdapter);
			if (myContacts != null && myContacts.size() > 0){
				nocontact_textview.setVisibility(View.GONE);
				nocontact_imageview.setVisibility(View.GONE);
			}
			else{
				nocontact_textview.setVisibility(View.VISIBLE);
				nocontact_imageview.setVisibility(View.VISIBLE);
			}


		}
		

		
		public void setContactData(){	 
			RuntimeLog.log("setContactData - In");
			
			// get the contactList from phone with verified by server
		    GetContactList mGetContactList = new GetContactList(mActivity, mHandler);
		  
		    InviteUserClient  inviteuser = getProxy(InviteUserClient.class);
		    
		    myContacts = mGetContactList.getPhoneContacts(inviteuser);
		    
		    Log.w(TAG,"time_interval =" +time_interval);
		    
		    Cursor contactCursor = mContactHelper.query("phonenumber"); 
		    if(contactCursor.moveToFirst() && myContacts.size()>0){
				int phonenumber_index = contactCursor.getColumnIndex("phonenumber");
				
				
				boolean flag = true;
    			boolean flagA = true;
    			boolean flagB = true;
				int index = 0;
				long curtime = getCurrentTime();

		    	//sync the mContactHelper and myContacts
				//add the phoneNum which myContacts has but mContactHelper not
				//remove the phoneNum which mContactHelper has but myContacts
				//the processing time is O(n), make sure the two queue must be ordered!
		    	while(flag){

		    		String phonenumberA = contactCursor.getString(phonenumber_index);
	    			String phonenumberB = myContacts.get(index).userPhoneNumber;
	    			
	    			int compare = phonenumberA.compareTo(phonenumberB);
	    			if(compare == 0){
                          //no-op
	    				flagA = contactCursor.moveToNext();
	    				flagB = (++index) < myContacts.size();
	    			}else if(compare<0){
	    				//the A has the elem, but B hasn't, del it from A
	    				mContactHelper.deleteItem(phonenumberA);
	    				flagA = contactCursor.moveToNext();
	    				flagB = true;
	    				
	    			}else{
	    				//B has the elem, but A hasn't , add it to A
	    				ContentValues values = new ContentValues();
	    				values.put("status", 1);
	    				values.put("time", curtime);
	    				values.put("phonenumber", phonenumberB);
	    				mContactHelper.insert(values);
	    				flagA = true;
	    				flagB = (++index) < myContacts.size();
	    			}

		    		flag = flagA && flagB;
		    		
		    	}
		    	
		    	//now check the rest queue
		    	//1. remove the rest elems from A which B hasn't
		    	while(flagA){
		    		String phonenumberA = contactCursor.getString(phonenumber_index);
		    		mContactHelper.deleteItem(phonenumberA);
		    		flagA = contactCursor.moveToNext();
		    		
		    	}

		    	
		    	//2.add the rest B elems into A, which A hasn't
		    	while(flagB){
		    		String phonenumberB = myContacts.get(index).userPhoneNumber;
		    		ContentValues values = new ContentValues();
    				values.put("status", 1);
    				values.put("time", curtime);
    				values.put("phonenumber", phonenumberB);
    				mContactHelper.insert(values);
    				flagB = (++index) < myContacts.size();
		    		
		    	}
		    	
		    	//close contactCursor first
		    	contactCursor.close();
		    	contactCursor = null;
		    	
		    	//check time_interval and reset status when time out
				Cursor contactCursor2 = mContactHelper.query(); 

				if(contactCursor2.moveToFirst()){
					//int phonenumber_index = contactCursor2.getColumnIndex("phonenumber");
					int time_index = contactCursor2.getColumnIndex("time");
					long curTime = getCurrentTime();
					
					do{
						long time = contactCursor2.getLong(time_index);
						String ContactHelper_number = contactCursor2.getString(phonenumber_index);
						
						if (( curTime - time) >= time_interval){								

							mContactHelper.updateStatus(ContactHelper_number, 1);
							mContactHelper.updateTime(ContactHelper_number, curTime);	
							Log.w(TAG,"phoneNum:" + ContactHelper_number + "time > = " +  (getCurrentTime() - time));

						}

					}while(contactCursor2.moveToNext());

				}
				
				if(contactCursor2!=null){
					
					contactCursor2.close();
				}

		    }else if(!contactCursor.moveToFirst() && myContacts.size()>0){
		    	//contactCursor empty, but myContacts not
		    	//copy myContacts to contactCursor
		    	boolean flagB = true;
		    	int index=0;
		    	long curtime = getCurrentTime();
		    	while(flagB){
		    		String phonenumberB = myContacts.get(index).userPhoneNumber;
		    		ContentValues values = new ContentValues();
    				values.put("status", 1);
    				values.put("time", curtime);
    				values.put("phonenumber", phonenumberB);
    				mContactHelper.insert(values);
    				flagB = (++index) < myContacts.size();
		    		
		    	}
		    	
		    }else if(contactCursor.moveToFirst() && myContacts.size()<=0){
		    	//remove the elems in contactCursor 
		    	//mContactHelper.close();
		    	//contactCursor.close();
		    	//return;
		    	RuntimeLog.log("setContactData - remove the elems in contactCursor ");
		    	mContactHelper.deleteAll();
		    	
		    	
		    }else{//both is empty, just return with no-op
		    	//return;
		    	RuntimeLog.log("setContactData - both is empty, just return with no-op");

		    }	
		    
		    if(contactCursor!=null){
		    	contactCursor.close();
		    }
		    
		    //sort it by default order, first is_ghike ,second name 
		    Collections.sort(myContacts, new ComparatorContactList());
		    
		    for(MyContacts aContacts:myContacts){
		    	RuntimeLog.log("setContactData1:" + aContacts.userPhoneNumber 
		    			+ " isghike:" + aContacts.isgHike 
		    			+ " nick:" + aContacts.nickName);
		    }
		    
		    RuntimeLog.log("setContactData - Out");
		} //end setContactData()
		
	} //end class InviteFriendsListViewWrapper();
		
		public Long getCurrentTime(){
			Date  curDate = new Date(System.currentTimeMillis());
			Long time = curDate.getTime();
			return time;		
		}
		
		
	@Override
	protected void onStop(){
		super.onStop();
		RuntimeLog.log("ContactActivity.onStop()");
	}
		

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		RuntimeLog.log("ContactActivity.onDestroy()");
		
		if (myContacts != null){
			myContacts.clear();
		}
		
		checked = true;	
		
		if (mContactHelper != null){
			mContactHelper.close();
			//mContactHelper = null;
					
		}
	}
	
	public static final int CHANGE_NOTIFY = 10001;
/*	private Handler mHandler1 = new Handler() {

		private boolean changingDir;

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			
			case CHANGE_NOTIFY:

	
					selectAllCheckBox.setChecked(mInviteListAdapter.isSelectAll());

				break;

			default:
				break;
			}
		}
	};*/

}
