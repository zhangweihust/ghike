package zonesdk.in.android.games.in.proxy;

import java.io.IOException;

import zonesdk.in.android.games.in.client.MediaInputStream;
import zonesdk.in.android.games.in.client.annotation.FileUpload;
import zonesdk.in.android.games.in.client.annotation.FormParam;
import zonesdk.in.android.games.in.client.annotation.POST;


public interface UserClient {
	
	@FileUpload("andriodprofile/upload-cover")
	public String uploadCover(MediaInputStream inStream) throws IOException;
	
	@FileUpload("andriodprofile/upload-avata")
	public String uploadAvatar(MediaInputStream inStream) throws IOException;
	
	@POST("profile/comfirm-avata")
	public void confirmAvatar(@FormParam("imgUrl") String avataUrl) throws IOException;
	
	@POST("andriodprofile/comfirm-cover") 
	public void confirmCover(@FormParam("imgUrl") String avataUrl) throws IOException;
}
