package zonesdk.in.android.games.in.proxy;

import java.io.IOException;


import org.json.JSONObject;

import zonesdk.in.android.games.in.client.annotation.FormParam;
import zonesdk.in.android.games.in.client.annotation.POST;

public interface CommunityClient {

	@POST("community/topic/add")
	public JSONObject sendTopic(
		@FormParam("forumId") int forumId,
		@FormParam("gameId") int gameId,
		@FormParam("subject") String subject,
		@FormParam("content") String content) throws IOException;
	
}
