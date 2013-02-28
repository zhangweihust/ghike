package zonesdk.in.android.games.in.client;


public class ServerSideErrorMsg {

	private ServerSideErrorMsg() {
	}

	public static String getMsg(int statusCode) {
		switch(statusCode){
		case 205:
			return "Phone number can't be empty!";
		case 203:
			return "Phone number error!";
		case 201:
			return "This number has already been occupied!";
		case 206:
			return "Verification code can't be empty!";
		case 202:
			return "Your entered a wrong PIN!try again!";
		case 401:
			return "Nickname cannot be less than 3 letters!";
		case 402:
			return "Nickname cannot be less than 3 letters!";
		case 410:
			return "Nickname can't more than 20 letters!";
		case 406:
			return "Nickname contains illegal characters!";
		case 407:
			return "This name has already been occupied!";
		case 413:
			return "The first or last letter can't be underline.";
		case 210:
			return "Custom PIN can't be empty!";
		case 211:
			return "Custom PIN error!";
		default:
			return null;
		}
	}
}
