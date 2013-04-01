package in.android.games.in.proxy;

import in.android.games.in.client.annotation.GET;
import in.android.games.in.client.annotation.PathParam;

import java.io.IOException;


import org.json.JSONObject;


public interface VersionClient {
	
	@GET("getversion/{product}")
	public JSONObject getVersion(@PathParam("product") String product) throws IOException;
	
}
