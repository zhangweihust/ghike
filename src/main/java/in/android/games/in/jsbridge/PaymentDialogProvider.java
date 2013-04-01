package in.android.games.in.jsbridge;

public interface PaymentDialogProvider {

	public void requestPayment(String hikeName,String mobileNumber,String rechargeAcount,String hikeCoin,
			String id,String token,String needPincode,String orderId);
			
}
