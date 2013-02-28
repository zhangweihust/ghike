package zonesdk.in.android.games.in.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;


public class DrawerLayout extends ViewGroup{

	private static int DRAWER_EDGE = 75;
	
	private static int DRAWER_DURATION = 280;

	public boolean mIsLeftDrawerOpened = false;

	protected View mLeftDrawer;

	protected View mMainView;
	
	public int mWidth = 0;
	
	public int mHeight = 0;

	private SlideFinishedCallback mCallback;

	private CloseLeftListener mCloseLeftListener;

	private OpenLeftListener mOpenLeftListener;
	
	private int mMainViewOffset = 0;
	
	private Paint mPaint;
	
	public DrawerLayout(Context context) {
		super(context);
		setWillNotDraw(false);
	}

	public DrawerLayout(Context context, AttributeSet set) {
		super(context, set);
		setWillNotDraw(false);
	}

	public DrawerLayout(Context context, AttributeSet set, int style) {
		super(context, set, style);
		setWillNotDraw(false);
	}

	@Override
	protected void onFinishInflate() {
		mLeftDrawer = getChildAt(0);
		mMainView = getChildAt(1);
		mCloseLeftListener = new CloseLeftListener();
		mOpenLeftListener = new OpenLeftListener();
		mPaint = new Paint();
	}

	@Override
	protected void onMeasure(int w, int h) {
		mLeftDrawer.measure(w, h);
		mMainView.measure(w, h);
		super.onMeasure(w, h);
		super.measureChildren(w, h);
	}
	
	@Override
	public void draw(Canvas canvas){
		super.draw(canvas);
		if(mIsLeftDrawerOpened){
			int height = getHeight();
			for(int i=1;i<=6;i++){
				int left = mMainView.getLeft() - i;
				int alpha = 150 - i * 25;
				mPaint.setColor(Color.argb(alpha, 0, 0, 0));
				canvas.drawLine(left, 0, left, height, mPaint);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		DRAWER_EDGE = (int)getMeasuredWidth()/6;
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		int maxPos = getMeasuredWidth() - DRAWER_EDGE;
		mLeftDrawer.layout(0, 0, right - left, bottom - top);
		if (!mIsLeftDrawerOpened) {
			mMainView.layout(left, 0, right, bottom);
		} else if (mIsLeftDrawerOpened) {
			mMainView.layout(left + maxPos, 0, right + maxPos, bottom);
		}
	}

	public void toggleLeftMenu() {
		if (!mIsLeftDrawerOpened) {
			openDrawer();
		} else {
			closeDrawer();
		}
	}
	
	public void openDrawer() {
		openDrawer(null);
	}

	public void openDrawer(SlideFinishedCallback callback) {
		if (mMainView.getAnimation() != null) {
			return;
		}
		mCallback = callback;
		mMainViewOffset = (getMeasuredWidth() - DRAWER_EDGE);
		TranslateAnimation animation = new TranslateAnimation(0,
				(getMeasuredWidth() - DRAWER_EDGE), 0, 0);
		animation.setDuration(DRAWER_DURATION);
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setAnimationListener(mOpenLeftListener);
		mMainView.startAnimation(animation);
	}

	public void closeDrawer() {
		closeDrawer(null);
	}

	public void closeDrawer(SlideFinishedCallback callback) {
		if (mMainView.getAnimation() != null) {
			return;
		}
		mCallback = callback;
		mMainViewOffset = 0;
		mIsLeftDrawerOpened = false;
		TranslateAnimation animation = new TranslateAnimation(0,
				- (getMeasuredWidth() - DRAWER_EDGE), 0, 0);
		animation.setDuration(DRAWER_DURATION);
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setAnimationListener(mCloseLeftListener);
		mMainView.startAnimation(animation);
	}

	private class OpenLeftListener implements Animation.AnimationListener {

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {
			mLeftDrawer.setVisibility(VISIBLE);
		}

		public void onAnimationEnd(Animation animation) {
			mMainView.clearAnimation();
			mIsLeftDrawerOpened = true;
			requestLayout();
			doCallback();
			mMainView.invalidate();
		}
	}

	private class CloseLeftListener implements Animation.AnimationListener {
		
		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

		public void onAnimationEnd(Animation animation) {
			mMainView.clearAnimation();
			mIsLeftDrawerOpened = false;
			mLeftDrawer.setVisibility(GONE);
			requestLayout();
			doCallback();
			mMainView.invalidate();
		}
	}

	public boolean isLeftDrawerOpened() {
		return mIsLeftDrawerOpened;
	}
	
	public void doCallback(){
		if(mCallback != null)
			mCallback.onSlideFinished(this);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent evt){
		if(evt.getAction() != MotionEvent.ACTION_DOWN){
			return false;
		}
		if(!mIsLeftDrawerOpened){
			return false;
		}
		if(!shouldTouchToSlide(evt.getX())){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent evt){
		if(!mIsLeftDrawerOpened){
			return false;
		}
		if((evt.getAction() != MotionEvent.ACTION_UP) || !shouldTouchToSlide(evt.getX())){
			return true;
		}
		if(isLeftDrawerOpened())
			closeDrawer();
		return true;
	}
	
	private boolean shouldTouchToSlide(float x){
		if(isLeftDrawerOpened() && x > mMainViewOffset)
			return true;
		return false;
	}

	public static interface SlideFinishedCallback {

		public void onSlideFinished(DrawerLayout layout);

	}

}
