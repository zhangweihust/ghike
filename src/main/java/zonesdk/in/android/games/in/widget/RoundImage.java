package zonesdk.in.android.games.in.widget;

import zonesdk.in.android.games.in.utils.ImageUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class RoundImage extends URLImageView {

	public RoundImage(Context context) {
		super(context);
	}
	
	public RoundImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RoundImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setImageBitmap(Bitmap bitmap){
		super.setImageBitmap(ImageUtil.toRoundCorner(bitmap, 100));
	}

}
