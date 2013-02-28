package zonesdk.in.android.games.in.proxy;

import java.io.IOException;


import org.json.JSONObject;

import zonesdk.in.android.games.in.client.annotation.GET;
import zonesdk.in.android.games.in.client.annotation.PathParam;

public interface GameClient {

	@GET("gameforum/list")
	public JSONObject getAllGames() throws IOException;
	
	@GET("play/{gameId}/new")
	public JSONObject playGame(@PathParam("gameId") int gameId) throws IOException;
	
}
