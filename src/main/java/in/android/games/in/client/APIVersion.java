package in.android.games.in.client;

public enum APIVersion {

	DEFAULT("");
	
	private String mVersion;
	
	private APIVersion(String version){
		mVersion = version;
	}
	
	public String toString(){
		return mVersion;
	}
	
}
