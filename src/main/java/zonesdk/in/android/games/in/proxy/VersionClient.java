package zonesdk.in.android.games.in.proxy;

import java.io.IOException;


import org.json.JSONObject;

import zonesdk.in.android.games.in.client.annotation.GET;
import zonesdk.in.android.games.in.client.annotation.PathParam;

public interface VersionClient {
	
	@GET("getversion/{product}")
	public JSONObject getVersion(@PathParam("product") String product) throws IOException;
	
}
