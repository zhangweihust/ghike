package in.android.games.in.common;

/**
 * @author job
 * @date 2012-08-08
 * @description: Constants for Hike client.
 */
public interface Constants {
	
    String ROOT_DOMAIN = "deve.ghike.in";
    
    String SOURCE = "hike";  // update "zone" or "hike"
    
    String CHANNEL = "0";  // src channel , 0 - default
    
	String DOMAIN_SNS = "m." + ROOT_DOMAIN;
	
	String DOMAIN_COOKIE = "." + ROOT_DOMAIN;
	
	String URL_TOUCH = "http://" + DOMAIN_SNS + "/touch/";
	
	String ROOT_STATIC_DOMAIN = "http://teststatic.ghike.in";
	
    String CREDENTIALS_USER_NAME = "tester";
    
    String CREDENTIALS_PASSWORD = "bsb3011";
    
    int MESSAGE_WHAT_API = 1888;
    
    int MESSAGE_WHAT_HIKE_CLIENT = 2888;
	
    String MAIN_PACKAGE_NAME = "zonesdk.in.android.games.in";
    
    String ACCOUNT_TYPE = MAIN_PACKAGE_NAME + ".user";
    
    String CMD_AUTO_LOGIN_WITH_CODE = "CMD_AUTO_LOGIN_WITH_CODE";
    
    String CMD_GET_PASSPORT = "CMD_GET_PASSPORT";
    
    String CMD_LOGOUT_AIRTEL = "CMD_LOGOUT_AIRTEL";
    
    String SITE_HOME_PAGE = "file:///android_asset/index.html";
    
    String PIN_CODE_PREFIX = "Hi,your verification code is:";
    
    String PIN_CODE_PREFIX_PAYMENT = "Please enter ";
    
    String PIN_CODE_TYPE_PAYMENT = "PAYMENT_PIN";
    
    String PIN_CODE_TYPE_LOGIN = "LOGIN_PIN";
    
    String CLOSE_ALL_ACTIVITY = "APP_CLOSE";
    
    String GAME_PATH = "&source=ghike";   // "&source=ghike" OR "";

}
