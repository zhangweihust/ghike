package in.android.games.in.client;


import in.android.games.in.utils.HttpRequestUtils;
import in.android.games.in.widget.GetContactList.MyContacts;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;




import android.os.Handler;
import android.util.Log;

public class HttpRequestClient {
	
	private static final String TAG = "SignManager";
	
	private static final String Contacts_URL = "invite/contacts";
	private static final String Contact_INVITE_URL = "invite/friends";
	private static final String Crashlog_URL = "crashLog/upload";
	
	//-----------------------------------------invite friend thread---------------------------------------------------
		/**
		 * 	 
		 * @param contactList
		 * @param handler
		 * @return
		 */
		public static ResponseWrapper performInvitelist(
				final ArrayList<MyContacts> contactList, final Handler handler,String passportMyCookieValue) {
			Log.i(TAG, "invite post  begin.");
			
			String contacts = ""; 
			for (MyContacts MyContact : contactList) {
				contacts = contacts + MyContact.userPhoneNumber + ",";
			}
			if(contactList.size()==0){
				Log.i(TAG, "Send contacts is null.");
				return null; 
			}
			contacts = contacts.substring(0, contacts.length() - 1);
			Log.i(TAG, " let phonenum spit by , ");
			
			Map<String, String> paramMap = new HashMap<String, String>();
			
			paramMap.put("uids", contacts);
			
			final ResponseWrapper resp = HttpRequestUtils.sendPostRequest(Contact_INVITE_URL,
					paramMap, passportMyCookieValue);

			Log.i(TAG, "contact resp=" + resp);
			return  resp ;

		}
		
		
		public static ResponseWrapper performCrashlog(final Map<String, String> infos, final String Crashlog,
				final Handler handler, String passportMyCookieValue) {
			try {

				Map<String, String> paramMap = new HashMap<String, String>();
				
				for (Map.Entry<String, String> entry : infos.entrySet()) {  
			           String key = entry.getKey();  
			           String value = entry.getValue();  
			           paramMap.put(key, value);  
			    }

				paramMap.put("Crashlog", Crashlog);
				//paramMap.put("Crashlog", "this is Crash log");

				final ResponseWrapper resp = HttpRequestUtils.sendPostRequest(
						Crashlog_URL, paramMap, passportMyCookieValue);
				return resp;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}


}
