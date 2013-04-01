package in.android.games.in.proxy;

import in.android.games.in.client.annotation.GET;
import in.android.games.in.client.annotation.PathParam;

import java.io.IOException;


import org.json.JSONObject;


public interface GameClient {

	@GET("gameforum/list")
	public JSONObject getAllGames() throws IOException;
	
	@GET("play/{gameId}/new")
	public JSONObject playGame(@PathParam("gameId") int gameId) throws IOException;
	
}
