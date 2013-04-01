package in.android.games.in.account;


public interface ZoneAccount {
	
	String ACCOUNT_TYPE = "zonesdk.in.android.games.in.user";
	
	String TYPE_AIRTEL = "TYPE_AIRTEL";
	
	String TYPE_NONAIRTEL = "TYPE_NONAIRTEL";
	
	public boolean isAirtel();
	
	String getCoverUrl();
	
	String getHeadUrl();
	
	String getNumber();
	
	String getPassport();
	
	String getPassportCookieValue();
	
	String getUserId();
	
	String getUserName();
	
	void setCoverUrl(String coverUrl);
	
	void setHeadUrl(String headUrl);
	
	void setUserName(String userName);
	
}
