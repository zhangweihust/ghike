package in.android.games.in.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

public class ContactLayout extends ViewGroup {

	public static final String TAG = "ContactLayout";

	public ContactLayout(Context context) {
		super(context);
	}

	public ContactLayout(Context context, AttributeSet set) {
		super(context, set);
	}

	public ContactLayout(Context context, AttributeSet set, int style) {
		super(context, set, style);
	}
	
	@Override
	protected void onMeasure(int w, int h) {
		Log.i(TAG, "onMeasure() begin.");
		super.onMeasure(w, h);
		int count = getChildCount();
		for(int i = 0; i<count; i++){
			getChildAt(i).measure(w, h);
		}
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i(TAG, "onLayout() begin.");
		int count = getChildCount();
		for(int i = 0; i<count; i++){
			getChildAt(i).layout(0, 0, right - left, bottom - top);
		}
	}

}


