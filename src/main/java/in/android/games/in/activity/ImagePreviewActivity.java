package in.android.games.in.activity;

import in.android.games.in.utils.RuntimeLog;
import in.android.games.in.widget.URLImageView;
import zonesdk.in.android.games.in.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class ImagePreviewActivity extends BaseActivity implements
		OnGestureListener {

	private ViewFlipper mFlipper;

	private int mCursor = 0;
	
	private int mCount = 0;
	
	private GestureDetector mDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_preview);
		
		RuntimeLog.log("ImagePreviewActivity.onCreate()");
		
		mFlipper = (ViewFlipper) findViewById(R.id.image_preview_flipper);
		initImages(getIntent());
		mDetector = new GestureDetector(this);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		RuntimeLog.log("ImagePreviewActivity.onPause()");
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		RuntimeLog.log("ImagePreviewActivity.onResume()");
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		RuntimeLog.log("ImagePreviewActivity.onStop()");
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		RuntimeLog.log("ImagePreviewActivity.onDestroy()");
	}
	
	private void initImages(Intent intent){
		Bundle bundle = intent.getExtras();
		String[] urls = bundle.getStringArray("urls");
		for(String url:urls){
			URLImageView image = new URLImageView(this);
			image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			image.loadFromUrl(url);
			mFlipper.addView(image);
		}
		mCount = urls.length;
		mCursor = bundle.getInt("index");
		mFlipper.setDisplayedChild(mCursor);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) { 
        return mDetector.onTouchEvent(event); 

    } 

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() - e2.getX() > 5) {
			mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
			mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
			if (mCursor < mCount - 1) {
				mFlipper.showNext();
				mCursor++;
			}
			return true;

		} else if (e1.getX() - e2.getX() < -5) {
			mFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
			mFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
			if (mCursor > 0) {
				mFlipper.showPrevious();
				mCursor--;
			}
			return true;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}
