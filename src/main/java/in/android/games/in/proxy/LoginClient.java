package in.android.games.in.proxy;

import in.android.games.in.client.annotation.FormParam;
import in.android.games.in.client.annotation.GET;
import in.android.games.in.client.annotation.POST;
import in.android.games.in.client.annotation.QueryParam;

import java.io.IOException;


import org.json.JSONObject;


public interface LoginClient {

	@GET("checkuser")
	public JSONObject checkUser() throws IOException;
	
	/*
	 * performGetSignInCode  type: SIGNIN_CODE
	 * performGetSignUpCode  type: SIGNUP_CODE
	 * */
	@GET("android/getcode")
	public JSONObject getSignPinCode(
			@QueryParam("phone") final String number,
			@QueryParam("codeType") final String type
			) throws IOException;
	
	
	@POST("signup")
	public JSONObject signUp(
			@FormParam("phone") final String number,
			@FormParam("pinCode") final String pinCode
			) throws IOException;
	
	@POST("signup-nickname")
	public JSONObject signUpNickname(
			@FormParam("nickname") final String nickname
			) throws IOException;
	
	
	@POST("signin")
	public JSONObject signIn(
			@FormParam("phone") final String number,
			@FormParam("pinCode") final String pinCode
			) throws IOException;

	
	@POST("signin-custom-pincode")
	public JSONObject signInCustomPinCode(
			@FormParam("phone") final String number,
			@FormParam("customPinCode") final String pinCode
			) throws IOException;
	
	
	@POST("signin-airtel")
	public JSONObject signInAirtel(
			) throws IOException;
	
	@POST("loginout")
	public JSONObject logout(
			) throws IOException;
}
