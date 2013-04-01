package in.android.games.in.proxy;

import in.android.games.in.client.annotation.FormParam;
import in.android.games.in.client.annotation.POST;

import java.io.IOException;


import org.json.JSONObject;


public interface CommunityClient {

	@POST("community/topic/add")
	public JSONObject sendTopic(
		@FormParam("forumId") int forumId,
		@FormParam("gameId") int gameId,
		@FormParam("subject") String subject,
		@FormParam("content") String content) throws IOException;
	
}
