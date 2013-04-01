package in.android.games.in.proxy;

import in.android.games.in.client.annotation.FormParam;
import in.android.games.in.client.annotation.POST;

import java.io.IOException;

import org.json.JSONObject;


public interface InviteUserClient {

	
	@POST("invite/contacts/v1")
	public JSONObject getInviteList(@FormParam("uids") String uids) throws IOException; 
	
	
	@POST("invite/friends/v1")
	public JSONObject inviteFriends(@FormParam("uids") String uids,@FormParam("plat") String plat) throws IOException;
	
	
}
