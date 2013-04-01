package in.android.games.in.jsbridge;

import android.app.Dialog;

public interface PaymentCallback {

	public void onDialogGoToCharge(Dialog dialog, String button,String message);

	public void onCancelPayment();
	
}