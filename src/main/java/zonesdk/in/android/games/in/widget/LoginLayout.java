package zonesdk.in.android.games.in.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class LoginLayout extends ViewGroup {

	public static final String TAG_DEV = "test";
	
	private OnSoftInputListener onSoftInputListener;

	public LoginLayout(Context context) {
		super(context);
	}

	public LoginLayout(Context context, AttributeSet set) {
		super(context, set);
	}

	public LoginLayout(Context context, AttributeSet set, int style) {
		super(context, set, style);
	}
	
	@Override
	protected void onMeasure(int width, int height) {
		super.onMeasure(width, height);
		int count = getChildCount();
		for(int i = 0; i<count; i++){
			getChildAt(i).measure(width, height);
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int count = getChildCount();
		for(int i = 0; i<count; i++){
			getChildAt(i).layout(0, 0, right - left, bottom - top);
		}
	}

	@Override
	protected void onSizeChanged(int width, int heigth, int oldWidth, int oldHeight) {
		if(onSoftInputListener!=null){
			if (heigth < oldHeight) { 
				onSoftInputListener.onShow();
			}else{
				onSoftInputListener.onHide();
			}
		}
	}
	
	public void setOnSoftInputListener(OnSoftInputListener onSoftInputListener) {
		this.onSoftInputListener = onSoftInputListener;
	}
	
	public interface OnSoftInputListener{
		
		void onShow();
		
		void onHide();
		
	}
}
