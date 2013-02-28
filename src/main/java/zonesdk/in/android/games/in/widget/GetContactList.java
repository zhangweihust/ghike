package zonesdk.in.android.games.in.widget;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import zonesdk.in.android.games.in.proxy.InviteUserClient;
import zonesdk.in.android.games.in.utils.ComparatorContactListWithPhoneNumber;
import zonesdk.in.android.games.in.utils.ComparatorContactListWithPhoneNumberReserver;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;


public class GetContactList {
	
	
	public static final int PHONES_DISPLAY_NAME_INDEX = 0;
    public static final int PHONES_NUMBER_INDEX = 1;
    public static final int PHONES_PHOTO_ID_INDEX = 2;
    public static final int PHONES_CONTACT_ID_INDEX = 3;
    public  Context mContext;
    
	private final Handler mHandler;
	
    public GetContactList(Context mContext, Handler handler){
    	this.mContext = mContext;
    	this.mHandler = handler;
    	
    }
    public static final String[] PHONES_PROJECTION = new String[]
    {
        Phone.DISPLAY_NAME,
        Phone.NUMBER,
        Photo.PHOTO_ID,
        Phone.CONTACT_ID      
        
    }; 
    
    //public static ArrayList<MyContacts> mycontacts = null;
    
    public static String passportMyCookieValue = "" ; 
    
    
    public class MyContacts{
    	public String userName = null;
    	public String userPhoneNumber = null;
    	public boolean ismyFriend = false;
    	//public InputStream userIconUrl = null;
    	public String userIconUrl = null;
    	public String nickName = null;
    	public boolean isChoosen = false;
    	public boolean isgHike = false;
    	
    	
    	public MyContacts(String userName, String userPhoneNumber, boolean ismyFriend, String userIconUrl,String nickname, boolean isChoosen,boolean isgHike){
    		this.userName = userName;
    		this.userPhoneNumber = userPhoneNumber;
    		this.userIconUrl = userIconUrl;
    		this.nickName = nickname;
    		this.ismyFriend = ismyFriend;
    		this.isChoosen = isChoosen;
    		this.isgHike =isgHike ;
    	}
    }
    

    public ArrayList<MyContacts> getPhoneContacts(InviteUserClient inviteuser) { 
   	
    	ArrayList<MyContacts>  sercontacts = new ArrayList<MyContacts>();
    	ArrayList<MyContacts>  myPhoneContactsUniq  = new ArrayList<MyContacts>();
    	ArrayList<MyContacts>  myPhoneContactsUniqAfter  = new ArrayList<MyContacts>();
    	ArrayList<MyContacts>  myPhoneContacts   = new ArrayList<MyContacts>();
    	ArrayList<MyContacts>  myPhoneContactsRtn  = new ArrayList<MyContacts>();  				
	    // TODO Auto-generated method stub
		MyContacts myContact = null;
		ContentResolver resolver = mContext.getContentResolver();
        Pattern pattern = Pattern.compile("(^\\+91\\d{10}$|^0091\\d{10}$|^\\d{10}$)");

		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null); 
				     
/*				    if (phoneCursor != null) { 
				    	Pattern pattern=Pattern.compile("(^\\+91\\d{10}$|^0091\\d{10}$|^\\d{10}$)");
				    	//int count = 0;
					    while (phoneCursor.moveToNext()) {
					    	try{

						        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX); 
						        phoneNumber = phoneNumber.replaceAll(" ", "");
						        if (TextUtils.isEmpty(phoneNumber)) 
						            continue;       
						        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
						        contactName =contactName.substring(contactName.indexOf(contactName.trim().substring(0, 1)));
						        Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);          
						        Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);          
						        String nickName = null;
						   
						        boolean isgHikeUser = false;        
						        String userIconUrl = null;
						             
						        //photoid 
						        if (photoid > 0 ) { 
						            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid); 
						            InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
						            if(is != null){
						            	userIconUrl = is.toString();
						            }else{
						            	userIconUrl = null;
						            }
						        }else { 
						        	userIconUrl = null;
						        } 
						        				      	
						    	//Pattern pattern=Pattern.compile("(^\\+91\\d{10}$|^0091\\d{10}$|^0\\d{10}$|^\\d{10}$)");
						    	//Pattern pattern = Pattern.compile("(^\\+86\\d{11}$|^1\\d{10}$)");
								Matcher matcher = pattern.matcher(phoneNumber);
										
								if (matcher.find()){
									myContact = new MyContacts(contactName, phoneNumber, isgHikeUser, userIconUrl, nickName, true);
									mycontacts.add(myContact);
								}
					    	}catch(Exception e){
					    		e.printStackTrace();
					    	}     
					     } 
				         phoneCursor.close(); 
				    }  */
		
		if (phoneCursor.moveToFirst()) { 
			  do{ 
				  String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
				  if (TextUtils.isEmpty(phoneNumber)) 
					  continue;       
				  phoneNumber = phoneNumber.replace("-", ""); //when add contacts, it may add '-' into num automatic
				  phoneNumber = phoneNumber.replaceAll(" ", ""); //when add contacts, it may add ' ' into num automatic
			      String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
			      if (!TextUtils.isEmpty(contactName))
		          	contactName =contactName.substring(contactName.indexOf(contactName.trim().substring(0, 1)));//delete front ' ' in contact name
		          long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);          
		          long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);          
		          String nickName = null;
		          boolean isgHikeUser = false;        
		          String userIconUrl = null;
		          
		          //photoid 
/*		          if (photoid > 0 ) { 
		        	  Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid); 
		        	  userIconUrl = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri).toString(); 
		          }else { 
		        	  userIconUrl = null;
		          } */
		          //Pattern pattern=Pattern.compile("(^\\+91\\d{10}$|^0091\\d{10}$|^0\\d{10}$|^\\d{10}$)");
		          Matcher matcher = pattern.matcher(phoneNumber);

		          if (matcher.find()){
		        	  myContact = new MyContacts(contactName, phoneNumber, isgHikeUser, userIconUrl, nickName, true,false);
		        	  myPhoneContacts.add(myContact);
		        	  RuntimeLog.log("getPhoneContacts - mycontacts.add:" 
		        			  + phoneNumber );
		           }    
		          
		      } while (phoneCursor.moveToNext());
			  
			  
		}
		
		if(phoneCursor!=null){
			phoneCursor.close(); 
		}
		
	    Uri uri = Uri.parse("content://icc/adn");  //simcard URI
	    Cursor SIMCursor = resolver.query(uri, PHONES_PROJECTION, null, null, null);  
	    if (SIMCursor != null) { 
	    	SIMCursor.moveToFirst();
	        while (SIMCursor.moveToNext()) {  
	        	try {
			        String sim_phoneNumber = SIMCursor.getString(PHONES_NUMBER_INDEX);
			        if(TextUtils.isEmpty(sim_phoneNumber))
			        	continue;
			        sim_phoneNumber = sim_phoneNumber.replaceAll("-", ""); //when add contacts, it may add '-' into num automatic
			        sim_phoneNumber = sim_phoneNumber.replaceAll(" ", ""); //when add contacts, it may add ' ' into num automatic

			        String sim_contactName = SIMCursor.getString(PHONES_DISPLAY_NAME_INDEX); 
			        if(!TextUtils.isEmpty(sim_contactName))
			        	sim_contactName = sim_contactName.substring(sim_contactName.indexOf(sim_contactName.trim().substring(0, 1)));//delete front ' ' in contact name
			        String  nickName = null;					   
			        boolean isgHikeUser = false;        
			        String  userIconUrl = null;				        
			        Matcher sim_matcher = pattern.matcher(sim_phoneNumber);
			        
			        if (sim_matcher.find()){
			        	myContact = new MyContacts(sim_contactName, sim_phoneNumber, isgHikeUser, userIconUrl, nickName, true,false);
			        	myPhoneContacts.add(myContact);
			        }
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

	        }  

	        
	        SIMCursor.close();  

	    }  
		

		
		
		// add the compare of  mycontacts first is Hike then order by userName(contactName) ;
        RuntimeLog.log("Collections.sort  - WithPhoneNumberReserver!!!");
		//Collections.sort(myPhoneContacts, new ComparatorContactListWithPhoneNumber());
        Collections.sort(myPhoneContacts, new ComparatorContactListWithPhoneNumberReserver());
		//Collections.sort(mySIMContacts, new ComparatorContactListWithPhoneNumber());
		
	    for (int i = 0; i < myPhoneContacts.size(); i++){
	    	RuntimeLog.log("WithPhoneNumberReserver:" + myPhoneContacts.get(i).userPhoneNumber);
	    }
	    

	    //remove the same elements(keep only one) in the big PhoneContacts
	    //+911299999900 == 1299999900 == 179511299999900, keep only one and remove the others 
	    String lastOne = "xxx";
	    
	    for(int i = 0; i < myPhoneContacts.size();i++){
	    	//if(myPhoneContacts.get(i).userPhoneNumber.compareTo(lastOne)==0){
	    	if(ComparatorContactListWithPhoneNumberReserver.compareto(myPhoneContacts.get(i).userPhoneNumber, lastOne)==0){
	    		//i++;
	    		if(i>=myPhoneContacts.size()){
	    			break ;
	    		}
	    	}else{
	    		myPhoneContactsUniq.add(myPhoneContacts.get(i));
	    		lastOne = myPhoneContacts.get(i).userPhoneNumber;
	    	}
	    	
	    		    	
	    }	
	    
	    for (int i = 0; i < myPhoneContactsUniq.size(); i++){
	    	RuntimeLog.log("WithPhoneNumberReserver22:" + myPhoneContactsUniq.get(i).userPhoneNumber);
	    }
	    

	     	    
	   if (myPhoneContactsUniq.size() == 0){
		 return myPhoneContactsUniq;
	   }
	   Log.i("INVITEManager","start get contactList from service  ");    
	   
	    if (sercontacts != null ){
	    	sercontacts.clear();
	        try {
	        	sercontacts = getServiceContactList(myPhoneContactsUniq,inviteuser) ;
	    	} catch (Exception e) {
	    		sercontacts.clear();
	    		e.printStackTrace();
	    	}
	    }
	    
	    Collections.sort(sercontacts, new ComparatorContactListWithPhoneNumberReserver());
	    
	    for(MyContacts aContacts:sercontacts){
	    	RuntimeLog.log("sercontacts:" + aContacts.userPhoneNumber 
	    			+ " isghike:" + aContacts.isgHike 
	    			+ " nick:" + aContacts.nickName);
	    }
	    
	    for(MyContacts aContacts:myPhoneContactsUniq){
	    	RuntimeLog.log("myPhoneContactsUniq before:" + aContacts.userPhoneNumber 
	    			+ " isghike:" + aContacts.isgHike 
	    			+ " nick:" + aContacts.nickName);
	    }
	    
	    
	    int i,j;
	    
	    MyContacts myContactTemp = null ;

	    for(i=0, j=0; i<myPhoneContactsUniq.size() && j<sercontacts.size(); ){
	    	String myContacts_phone = myPhoneContactsUniq.get(i).userPhoneNumber;
	    	String serContacts_phone = sercontacts.get(j).userPhoneNumber;
	    	int compareFlag = ComparatorContactListWithPhoneNumberReserver.compareto(myContacts_phone, serContacts_phone);
			if(compareFlag==0){
				// todo: modify this , merge local and server
				myContactTemp = getContact(myPhoneContactsUniq.get(i) ,sercontacts.get(j));
				if (myContactTemp.ismyFriend == true) {
					RuntimeLog.log("myContactTemp ismyFriend: " + myContactTemp.userPhoneNumber + " " + myContactTemp.nickName);
					//myPhoneContactsUniq.remove(i);
					
				} else {
					//myPhoneContactsUniq.set(i, myContactTemp);
					myPhoneContactsUniqAfter.add(myContactTemp);

				}
				i++;
				j++;
			}else if(compareFlag>0){
				myPhoneContactsUniqAfter.add(sercontacts.get(j));
				j++;
			}else{
				myPhoneContactsUniqAfter.add(myPhoneContactsUniq.get(i));
				i++;
			}
	    }
	    
	    
	    while(i<myPhoneContactsUniq.size()){
	    	//myPhoneContactsUniq.add(sercontacts.get(j));
	    	myPhoneContactsUniqAfter.add(myPhoneContactsUniq.get(i));
	    	i++;
	    }
	    
	    while(j<sercontacts.size()){
	    	//myPhoneContactsUniq.add(sercontacts.get(j));
	    	myPhoneContactsUniqAfter.add(sercontacts.get(j));
	    	j++;
	    }
	    
/*	    for(MyContacts aContacts:myPhoneContactsUniq){
	    	RuntimeLog.log("myPhoneContactsUniq:" + aContacts.userPhoneNumber 
	    			+ " isghike:" + aContacts.isgHike 
	    			+ " nick:" + aContacts.nickName);
	    }*/
	    
	    RuntimeLog.log("Collections.sort  - WithPhoneNumber!!!");
	    Collections.sort(myPhoneContactsUniqAfter, new ComparatorContactListWithPhoneNumber());
	    
	    //Collections.sort(myPhoneContactsUniq, new ComparatorContactList());
	    
	    
	    for(MyContacts myContacts:myPhoneContactsUniqAfter){
	    	RuntimeLog.log("myPhoneContactsUniqAfter :" +myContacts.userPhoneNumber);
	    	if(myContacts.isgHike){
	    		if(myContacts.nickName.length()>=10)myContacts.nickName =myContacts.nickName.substring(0,7)+"..."; 
	    		if(myContacts.userName.length()>=10)myContacts.userName =myContacts.userName.substring(0,7)+"...";
	    		myContacts.nickName ="("+myContacts.nickName+")" ;
	    	}else{
	    		if(myContacts.userName.length()>=20)myContacts.userName =myContacts.userName.substring(0,17)+"..."; 
	    	}
	    	myPhoneContactsRtn.add(myContacts);
	    }
	    
	    
/*	    for(MyContacts aContacts:myPhoneContactsRtn){
	    	RuntimeLog.log("myPhoneContactsRtn:" + aContacts.userPhoneNumber 
	    			+ " isghike:" + aContacts.isgHike 
	    			+ " nick:" + aContacts.nickName);
	    }*/
	    
    	return myPhoneContactsRtn;
        
    }
    
    private MyContacts getContact(MyContacts myContact, MyContacts myContacts2) {
    	return  new MyContacts(myContact.userName, myContact.userPhoneNumber, myContacts2.ismyFriend, myContacts2.userIconUrl, 
    			myContacts2.nickName, myContact.isChoosen,myContacts2.isgHike);
    	
	}

	/**
     * return contact List which is hike user List 
     * @param contactList
     * @param inviteuser
     * @return
     */
    private ArrayList<MyContacts> getServiceContactList(ArrayList<MyContacts> contactList,InviteUserClient inviteuser) {
		List<String> contacts = new ArrayList<String>();
		ArrayList<MyContacts> rtnContacts = new ArrayList<MyContacts>();
		try {

			for (MyContacts MyContact : contactList) {
				contacts.add(MyContact.userPhoneNumber);
			}
			JSONArray json = new JSONArray(contacts);
			JSONObject userList = inviteuser.getInviteList(json.toString());
			
			String status = userList.getString("status") ;
			if(!"0".equals(status)){
				return null;
			}
			JSONArray myContactJson = userList.getJSONArray("Ulist");
			// {"status":0,"Ulist":[{"f":false,"nN":"nanping","uI":"users/2012/12/25/ece28187-1e48-40db-90a6-6d1561448c0d.jpg","uPN":"1865318632"}]}
			// f means friend , nN means nickName ,uI means userIcon , uPN means userphoneNum
			MyContacts myContact = null;
			JSONObject jsonObject = null;
			for (int i = 0; i < myContactJson.length(); i++) {
				jsonObject = myContactJson.getJSONObject(i);
				myContact = new MyContacts("", jsonObject.getString("uPN"),
						jsonObject.getBoolean("f"), jsonObject.getString("uI"),
						jsonObject.getString("nN"), true,true);
				rtnContacts.add(myContact);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnContacts;
	} 

}  
