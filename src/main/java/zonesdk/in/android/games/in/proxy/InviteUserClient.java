package zonesdk.in.android.games.in.proxy;

import java.io.IOException;

import org.json.JSONObject;

import zonesdk.in.android.games.in.client.annotation.FormParam;
import zonesdk.in.android.games.in.client.annotation.POST;

public interface InviteUserClient {

	
	@POST("invite/contacts/v1")
	public JSONObject getInviteList(@FormParam("uids") String uids) throws IOException; 
	
	
	@POST("invite/friends/v1")
	public JSONObject inviteFriends(@FormParam("uids") String uids,@FormParam("plat") String plat) throws IOException;
	
	
}
