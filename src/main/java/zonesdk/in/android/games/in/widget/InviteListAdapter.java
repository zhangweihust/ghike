package zonesdk.in.android.games.in.widget;

import java.sql.Date;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import zonesdk.in.android.games.in.R;
import zonesdk.in.android.games.in.client.HttpRequestClient;
import zonesdk.in.android.games.in.client.ResponseWrapper;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.db.ContactHelper;
import zonesdk.in.android.games.in.proxy.InviteUserClient;
import zonesdk.in.android.games.in.utils.CookieUtil;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.widget.GetContactList.MyContacts;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class InviteListAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	
	public Context mContext;
	
	public ArrayList<MyContacts> myContacts = null;
	
	public ArrayList<MyContacts> itemSendContact = null;
	
	public boolean isSendOk = false;
	
	private Handler mHandler = null;
	
	private Bitmap defaultBmp = null;
	  
    public final static int SUCCESS = 0;
    
    public final static int FAIL = 1;
    
    public final static String TAG = "InviteListAdapter"; 
        
    public  ContactHelper mContactHelper = null;
    
    private InviteUserClient inviteuser = null ; 
    //public Cursor cursor = null;
	//private HashSet<Integer> checkSet;
    //private Map<String,Integer> phone_Status = new HashMap<String,Integer>() ;


	public InviteListAdapter(Context context, Handler handler, ArrayList<MyContacts> myContacts, ContactHelper mContactHelper, InviteUserClient inviteuser){
		//phone_Status = mContactHelper.queryStatus(myContacts);
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;
		this.myContacts = myContacts;
		this.inviteuser = inviteuser ;
/*		if (this.mContactHelper != null){
			this.mContactHelper.close();
			this.mContactHelper = null;
		}*/
		
		this.mContactHelper = mContactHelper;
			    
//		GetContactList mGetContactList = new GetContactList(mContext, handler);
		//myContacts = mGetContactList.getPhoneContacts();
/*		if (mContactData != null){
			mContactData.close();
			mContactData= null;
		}
		mContactData = new ContactData(mContext);	*/

/*		if (cursor != null){
			cursor.close();
			cursor = null;

		}*/
				
		mHandler = handler;
		//checkSet = new HashSet<Integer>();
		
/*		int position = 0;
		for (MyContacts item : myContacts) {
			//checkSet.add(position);
			position++;
		}*/
		
		Resources res = mContext.getResources();
		defaultBmp = BitmapFactory.decodeResource(res, R.drawable.placehoder_female);
	}
/*	public HashSet<Integer> getCheckSet() {
		return checkSet;
	}*/

/*	public void setCheckSet(HashSet<Integer> checkSet) {
		this.checkSet = checkSet;
	}*/
	public ArrayList<MyContacts> getMyContacts() {
		return myContacts;
	}

	public void setMyContacts(ArrayList<MyContacts> myContacts) {
		this.myContacts = myContacts;
	}

	public Long getCurrentTime(){
		Date  curDate = new Date(System.currentTimeMillis());
		Long time = curDate.getTime();
		Log.w(TAG,"time = " + time);
		return time;		
	}
	

	@Override
	public int getCount() {
		Log.w(TAG, "getCount = " + myContacts.size());
		return myContacts.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		final int pos = position;
		//final Cursor cursor = mContactHelper.query();
		if (myContacts == null){
			return null;
		}
		
		final MyContacts contact = myContacts.get(position);
		if (contact == null){
			return null;
		}
		
		final ViewHolder holder;
		if (convertView == null){
			convertView = inflater.inflate(R.layout.invite_list_item, null);
			holder = new ViewHolder();
			holder.nickname_textview = (TextView)convertView.findViewById(R.id.invitelist_user_nickname);
			holder.normalname_textview = (TextView)convertView.findViewById(R.id.invitelist_user_name);
			holder.phonenumber_textview = (TextView)convertView.findViewById(R.id.invitelist_user_phonenumber);
			//holder.selectItemCheckBox = (CheckBox)convertView.findViewById(R.id.invitelist_checkbox);
			//holder.selectItemCheckBox.setChecked(true);
			holder.userHeadIcon = (RoundImage)convertView.findViewById(R.id.invitelist_user_head);
			holder.userStatus = (ImageView)convertView.findViewById(R.id.invitelist_user_status);
			holder.relativelayout = (RelativeLayout)convertView.findViewById(R.id.invitelistview);
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder)convertView.getTag();
			
		}
		

		//View item = inflater.inflate(R.layout.invite_list_item, null);
	  
        Typeface typeFace = Typeface.createFromAsset(mContext.getAssets(), "font/Edmondsans-Regular.otf");
        holder.phonenumber_textview.setTypeface(typeFace);
        holder.normalname_textview.setTypeface(typeFace);
        holder.nickname_textview.setTypeface(typeFace);


/*		if (cursor != null){
			cursor.moveToPosition(position)	;		
		}*/

	    
/*	    holder.selectItemCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
					myContacts.get(pos).isChoosen = isChecked;
					//holder.selectItemCheckBox.setChecked(isChecked);
					Log.w("dahai","choosen is " + isChecked);
					Message message = new Message();
					message.what = ContactActivity.CHANGE_NOTIFY;
					if (isChecked) {
						checkSet.add(pos);
					} else {
						checkSet.remove(pos);
					}
					mHandler.sendMessage(message);
				
			}
		});*/

	    
/*	    holder.selectItemCheckBox.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(holder.selectItemCheckBox.isChecked()){
					holder.selectItemCheckBox.setChecked(false);
					checkSet.remove(pos);
					myContacts.get(pos).isChoosen = false;
				}
				else{
					holder.selectItemCheckBox.setChecked(true);
					checkSet.add(pos);
					myContacts.get(pos).isChoosen = true;

				}
				return false;
			}
		});*/
	    

	    
	    //int status_index = cursor.getColumnIndex("status");
	    //final int status = cursor.getInt(status_index);
	    //int phonenumber_index = cursor.getColumnIndex("phonenumber");
	    //String phonenumber = cursor.getString(phonenumber_index);
	    
	    String phonenumber = myContacts.get(position).userPhoneNumber;

	    int status = mContactHelper.queryStatus(phonenumber);
	    //int status = phone_Status.containsKey(phonenumber)?phone_Status.get(phonenumber):1;
	    Log.w(TAG,"queryStatus status = " + status);
	    
		if (status != 0){
			if (myContacts.get(position).isgHike && myContacts.get(position).userPhoneNumber.equals(phonenumber)){
				holder.userStatus.setBackgroundResource(R.drawable.contact_add);	
			}
			else{
				holder.userStatus.setBackgroundResource(R.drawable.contact_invite);
			}
						
/*			if (checkSet.contains(position)) {
				//holder.selectItemCheckBox.setChecked(true);
				contact.isChoosen = true;
			} else {
				//holder.selectItemCheckBox.setChecked(false);
				contact.isChoosen = false;
			}		 
			Log.w("dahai","selectItemCheckBox is true");*/
		}
		else {
			if (myContacts.get(position).isgHike){
				holder.userStatus.setBackgroundResource(R.drawable.contact_added);	
			}
			else{
				holder.userStatus.setBackgroundResource(R.drawable.contact_invited);	
			}
						
			//holder.selectItemCheckBox.setChecked(true);
			//holder.selectItemCheckBox.setClickable(false);
			Log.w("dahai","selectItemCheckBox is false");
			
		}
		
/*	    holder.selectItemCheckBox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    int status_index = cursor.getColumnIndex("status");
			    final int status = cursor.getInt(status_index);
				Log.w("dahai","holder.selectItemCheckBox is clicked");
				cursor.moveToPosition(pos);
			    int status_index = cursor.getColumnIndex("status");
			    final int status = cursor.getInt(status_index);
			    if (status != 0){
					if(myContacts.get(pos).isChoosen){
						holder.selectItemCheckBox.setChecked(false);
						checkSet.remove(pos);
						myContacts.get(pos).isChoosen = false;
					}
					else{
						holder.selectItemCheckBox.setChecked(true);
						checkSet.add(pos);
						myContacts.get(pos).isChoosen = true;

					}
			    }
			    else{
                	checkSet.remove(pos);
                	myContacts.get(pos).isChoosen = false;
                	holder.selectItemCheckBox.setChecked(true);
			    	
			    }


				
			}
		});*/
	    
/*	    holder.relativelayout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				cursor.moveToPosition(pos);
			    int status_index = cursor.getColumnIndex("status");
			    final int status = cursor.getInt(status_index);
                if (status != 0){
    				if(holder.selectItemCheckBox.isChecked()){
    					holder.selectItemCheckBox.setChecked(false);
    					checkSet.remove(pos);
    					myContacts.get(pos).isChoosen = false;
    				}
    				else{
    					holder.selectItemCheckBox.setChecked(true);
    					checkSet.add(pos);
    					myContacts.get(pos).isChoosen = true;

    				}
                	
                }
                else{
                	checkSet.remove(pos);
                	myContacts.get(pos).isChoosen = false;
                	holder.selectItemCheckBox.setChecked(true);
                }

				
				return false;
			}
		});*/
	    
/*	    holder.userHeadIcon.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				cursor.moveToPosition(pos);
			    int status_index = cursor.getColumnIndex("status");
			    final int status = cursor.getInt(status_index);
			    if (status != 0){
					if(holder.selectItemCheckBox.isChecked()){
						holder.selectItemCheckBox.setChecked(false);
						checkSet.remove(pos);
						myContacts.get(pos).isChoosen = false;
					}
					else{
						holder.selectItemCheckBox.setChecked(true);
						checkSet.add(pos);
						myContacts.get(pos).isChoosen = true;

					}
			    	
			    }
                else{
                	checkSet.remove(pos);
                	myContacts.get(pos).isChoosen = false;
                	holder.selectItemCheckBox.setChecked(true);
                }

				return false;
			}
		});*/

		

				
		holder.normalname_textview.setText(myContacts.get(position).userName);
			if ( myContacts.get(position).userIconUrl != null && !myContacts.get(position).userIconUrl.equals("null")&&!"".equals(myContacts.get(position).userIconUrl.trim())){
				holder.userHeadIcon.loadFromUrl(myContacts.get(position).userIconUrl);
				Log.w("dahai","myContacts.get(position).userIconUrl = " + myContacts.get(position).userIconUrl);				
			}
			else{
				//BitmapDrawable userHead = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.female));				
				//userHeadIcon.setBackgroundDrawable(userHead);	
				//Resources res = mContext.getResources();
				//Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.placehoder_female);
				//holder.userHeadIcon.setImageDrawable(new BitmapDrawable(bmp));
				holder.userHeadIcon.setImageDrawable(new BitmapDrawable(defaultBmp));
			}
			
			if (myContacts.get(position).nickName == null || myContacts.get(position).nickName.equals("")){
				Log.w("dahai","myContacts.get(position).nickName == null");
				holder.nickname_textview.setText(null);
			}
			else{
				holder.nickname_textview.setText(myContacts.get(position).nickName);
				Log.w("dahai","myContacts.get(position).nickName1 = " + myContacts.get(position).nickName);
			}
			holder.phonenumber_textview.setText(myContacts.get(position).userPhoneNumber);
			
				holder.relativelayout.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub		
					//cursor.moveToPosition(pos);
					
					final String contact_phonenumber = myContacts.get(pos).userPhoneNumber;
					final boolean contact_isgHike = myContacts.get(pos).isgHike;

					final 
	                    Thread t = new Thread(new Runnable() {  
	                        @Override  
	                       public void run() { 

								itemSendContact.add(myContacts.get(pos));
								JSONObject respJson = null;
								int interface_status = 1 ;
								try{
									respJson = inviteuser.inviteFriends(contact_phonenumber, Constants.SOURCE);
									if (respJson != null) {
										interface_status = Integer.valueOf(respJson.getString("status")).intValue() ;
									}
								}catch (Exception e) {
									e.printStackTrace();
						        }
						Log.w("dahai", "interface_status =" + interface_status);
						switch (interface_status) {
						case 0: {
							mHandler.post(new Runnable() {
								public void run() {
									// String phonenumber =
									// cursor.getString(cursor.getColumnIndex("phonenumber"));
/*									if(myContacts.size()<=pos){
										RuntimeLog.log(" == myContacts.size" + myContacts.size() + " < pos:" + pos);
										return;
									}*/
									//String contact_phonenumber = myContacts.get(pos).userPhoneNumber;
									Log.w(TAG, "My contact_phonenumber = "+ contact_phonenumber);

									if (contact_isgHike) {
										holder.userStatus.setBackgroundResource(R.drawable.contact_added);
									} else {
										holder.userStatus.setBackgroundResource(R.drawable.contact_invited);
									}
									mContactHelper.updateStatus(contact_phonenumber, 0);

									// phone_Status.put(contact_phonenumber, 0);
									mContactHelper.updateTime(contact_phonenumber,getCurrentTime());
									// mContactHelper.close();
									/*
									 * if (cursor != null){ cursor.close(); }
									 */
									// cursor = mContactHelper.query();
								}
							});
							break;
						}
						default: {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// String phonenumber =
									// cursor.getString(cursor.getColumnIndex("phonenumber"));
/*									if(myContacts.size()<=pos){
										RuntimeLog.log(" ==2 myContacts.size" + myContacts.size() + " < pos:" + pos);
										return;
									}*/
									//String contact_phonenumber = myContacts.get(pos).userPhoneNumber;
									mContactHelper.updateStatus(contact_phonenumber, 1);
									// phone_Status.put(contact_phonenumber, 1);
									Toast.makeText(mContext, "Send Failed",Toast.LENGTH_SHORT).show();
									// mContactHelper.close();
									/*
									 * if (cursor != null){ cursor.close();
									 * //cursor = null; }
									 */
									// cursor = mContactHelper.query();
								}
							});
							break;
						}
						}
	                        }  
	                        });
	                     
					     //Cursor cursor = mContactHelper.query();
					     //cursor.moveToPosition(pos);
					   String phonenumber = myContacts.get(pos).userPhoneNumber;
					   int status = mContactHelper.queryStatus(phonenumber);
					   //int status = phone_Status.containsKey(phonenumber)?phone_Status.get(phonenumber):1;
						 //int status = cursor.getInt(cursor.getColumnIndex("status"));
						 //cursor.close();

							if (itemSendContact != null){
								itemSendContact.clear();
								itemSendContact = null;
							}
							itemSendContact =  new ArrayList<MyContacts>();
							
						if (status!= 0){
							t.start();

							
						}
						else{
							Log.w("dahai","status == 0");
						}


						

	                    
	                         

				}
			});

				return convertView;
			
			

	}
	
	static class ViewHolder{
		     //CheckBox selectItemCheckBox ;
			 TextView phonenumber_textview ;
			 TextView normalname_textview ;
			 TextView nickname_textview ; 
		     RoundImage userHeadIcon ;
			 ImageView userStatus ; 
			 RelativeLayout relativelayout;

		
	}
	
	public void setHandler(){

		
	}
	
	public  Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return null;
		}

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
	
/*	public boolean isSelectAll() {
		if (myContacts == null) {
			return false;
		}

		for (int i = 0; i < myContacts.size(); i++) {
			if (!checkSet.contains(i)) {
				return false;
			}
		}
		return true;
	}*/
	
/*	public void setSelectAll(boolean isSelectAll) {
		if (isSelectAll) {
			int position = 0;
			for (MyContacts item : myContacts) {
				item.isChoosen = true;
				checkSet.add(position);
				position++;
			}
		} else {
			checkSet.clear();
			for (MyContacts item : myContacts) {
				item.isChoosen = false;

			}
		}
		//this.notifyDataSetChanged();
	}*/

	
}
