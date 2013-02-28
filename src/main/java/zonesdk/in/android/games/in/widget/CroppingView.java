package zonesdk.in.android.games.in.widget;

import zonesdk.in.android.games.in.activity.CroppingActivity;
import zonesdk.in.android.games.in.activity.CroppingActivity.CropHandler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CroppingView extends View {
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private static final int BIGGER = 3;
	private static final int SMALLER = 4;

	private int mode = NONE;

	private int screenW;
	private int screenH;

	private Paint paint;
	private Bitmap mContentBMP;

	private Rect mBmpRect;
	private Rect mClipRect;
	private Rect mNewBmpRect;

	private int mClipRectDelX = 5;

	private int mOriginW;
	private int mOriginH;

	private float mBeforeLenght;
	private float mAfterLenght;

	private float scale = 0.04f;
	private int color = Color.argb(150, 0, 0, 0);

	private int resultW;
	private int resultH;

	private int referW;

	private boolean isLocked = true;
	private boolean isAnimate = false;
	private boolean isClip = false;

	private CropHandler mCropHandler;

	public CroppingView(Context context) {
		super(context);
	}

	public CroppingView(Context context, AttributeSet attrs) {
		super(context, attrs);

		paint = new Paint();
		mNewBmpRect = new Rect(0, 0, 0, 0);
	}

	public CroppingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setCropHandler(CropHandler ch) {
		mCropHandler = ch;
	}	

	public void init(int viewW, int viewH, int rw, int rh) {
		resultW = rw;
		resultH = rh;

		float radio = (float) rw / rh;
		int width = viewW - 2 * mClipRectDelX;
		int hight = (int) (width / radio);

		mClipRect = new Rect(mClipRectDelX, (viewH - hight) / 2, viewW
				- mClipRectDelX, (viewH - hight) / 2 + hight);		
	}
	
	public void setLocked(boolean lock){
		isLocked = lock;
	}
	
	public boolean isLocked(){
		return isLocked;
	}

	public void setBmp(Bitmap bmp) {
		mContentBMP = bmp;
		mOriginW = bmp.getWidth();
		mOriginH = bmp.getHeight();
		
		mBmpRect = createOriginBmpRect();

		referW = mBmpRect.width();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		screenW = getMeasuredWidth();
		screenH = getMeasuredHeight();

		if (mContentBMP != null) {
			if (mBmpRect != null) {
				canvas.drawBitmap(mContentBMP, null, mBmpRect, paint);
			} else {
				canvas.drawBitmap(mContentBMP, 0, 0, paint);
			}
		}

		// draw edge rectangle
		paint.setColor(color);
		paint.setStyle(Style.FILL);

		if (mClipRect != null) {
			canvas.drawRect(0, 0, screenW, mClipRect.top, paint);
			canvas.drawRect(mClipRect.right, mClipRect.top, screenW,
					mClipRect.bottom, paint);
			canvas.drawRect(0, mClipRect.top + mClipRect.height(), screenW,
					screenH, paint);
			canvas.drawRect(0, mClipRect.top, mClipRect.left, mClipRect.top
					+ mClipRect.height(), paint);
		}

		// last step,draw rectangle
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);

		if (mClipRect != null) {
			canvas.drawRect(mClipRect.left, mClipRect.top, mClipRect.right,
					mClipRect.bottom, paint);
		}
	}

	private int originX;
	private int originY;

	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (!isAnimate && !isClip && !isLocked) {
				mode = DRAG;
				originX = x;
				originY = y;
			}
			break;
		case MotionEvent.ACTION_UP:
			setClipEnable(false);
			/*if(mode != ZOOM){
				setClipEnable(false);
			}*/
			mode = NONE;			
			onTouchEnd();			
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				int deltaX = x - originX;
				int deltaY = y - originY;

				updateBmpRect(deltaX, deltaY);

				originX = x;
				originY = y;
				postInvalidate();
			}

			if (mode == ZOOM) {
				if (spacing(event) > 10f) {
					mAfterLenght = spacing(event);
					float gapLenght = mAfterLenght - mBeforeLenght;
					if (gapLenght == 0) {
						break;
					} else if (Math.abs(gapLenght) > 5f) {
						if (gapLenght > 0) {
							setScale(scale, BIGGER);
						} else {
							setScale(scale, SMALLER);
						}
						mBeforeLenght = mAfterLenght;
					}

					postInvalidate();
				}
			}
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			if (!isAnimate && !isClip && !isLocked) {
				if (spacing(event) > 10f) {
					mode = ZOOM;
					mBeforeLenght = spacing(event);
				}
			}

			break;
		}

		return true;
	}

	private void onTouchEnd() {
		int disX = 0;
		int disY = 0;
		
		if (mBmpRect.contains(mClipRect)) {
			setClipEnable(true);
			return;
		}

		if (mClipRect.width() > mBmpRect.width()
				|| mClipRect.height() > mBmpRect.height()) {			
			scaleBack(createOriginBmpRect());
			postInvalidate();
			return;
		}

		if ((mClipRect.width() > mBmpRect.width() && mClipRect.height() > mBmpRect
				.height())) {
			scaleBack(createOriginBmpRect());
			postInvalidate();
			return;
		}
		
		if (mBmpRect.left >= mClipRect.left) {
			disX = mClipRect.left - mBmpRect.left;
		} else if (mBmpRect.right <= mClipRect.right) {
			disX = mClipRect.right - mBmpRect.right;
		}

		if (mBmpRect.top >= mClipRect.top) {
			disY = mClipRect.top - mBmpRect.top;
		} else if (mBmpRect.bottom <= mClipRect.bottom) {
			disY = mClipRect.bottom - mBmpRect.bottom;
		}
		
		if(disX != 0 || disY != 0){
			int left = mBmpRect.left + disX;
			int top = mBmpRect.top + disY;

			moveBack(new Rect(left, top, left + mBmpRect.width(), top
					+ mBmpRect.height()));
			postInvalidate();		
		}else{
			setClipEnable(true);
		}	
	}

	private void scaleBack(final Rect desRect) {
		final int rangeX = desRect.left - mBmpRect.left;
		final int rangeY = desRect.top - mBmpRect.top;

		final int absDisX = Math.abs(rangeX);
		final int absDisY = Math.abs(rangeY);

		final int deltax = (int) rangeX / 15;
		final int deltay = (int) rangeY / 15;

		final int delScaleX = (int) (desRect.width() - mBmpRect.width()) / 10;
		final int delScaleY = (int) (desRect.height() - mBmpRect.height()) / 10;

		isAnimate = true;		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int newWidth = mBmpRect.width();
				int newHight = mBmpRect.height();
				boolean scaleX = true;
				boolean scaleY = true;
				boolean canMoveX = true;
				boolean canMoveY = true;
				float moveX = 0.0f;
				float moveY = 0.0f;
				int absDelX = Math.abs(deltax);
				int absDelY = Math.abs(deltay);

				while (true) {
					if (deltax != 0) {
						if ((moveX < absDisX) || scaleX) {
							moveX += absDelX;
							if (scaleX) {
								if (delScaleX != 0) {
									newWidth += delScaleX;
									if (newWidth >= desRect.width()) {
										newWidth = desRect.width();
										scaleX = false;
									}
								} else {
									newWidth = desRect.width();
									scaleX = false;
								}
							}

							if (moveX < absDisX) {
								mBmpRect.left += deltax;
							} else {
								mBmpRect.left = desRect.left;
								canMoveX = false;
							}
							mBmpRect.right = mBmpRect.left + newWidth;
						}
					} else {
						if (scaleX) {
							if (delScaleX != 0) {
								newWidth += delScaleX;
								if (newWidth >= desRect.width()) {
									newWidth = desRect.width();
									scaleX = false;
								}
							} else {
								newWidth = desRect.width();
								scaleX = false;
							}
						}
						mBmpRect.left = desRect.left;
						mBmpRect.right = mBmpRect.left + newWidth;
						canMoveX = false;
					}

					if (deltay != 0) {
						if ((moveY < absDisY) || scaleY) {
							moveY += absDelY;
							if (scaleY) {
								if (delScaleY != 0) {
									newHight += delScaleY;
									if (newHight >= desRect.height()) {
										newHight = desRect.height();
										scaleY = false;
									}
								} else {
									newHight = desRect.height();
									scaleY = false;
								}
							}

							if (moveY < absDisY) {
								mBmpRect.top += deltay;
							} else {
								mBmpRect.top = desRect.top;
								canMoveY = false;
							}
							mBmpRect.bottom = mBmpRect.top + newHight;
						}
					} else {
						if (scaleY) {
							if (delScaleY != 0) {
								newHight += delScaleY;
								if (newHight >= desRect.height()) {
									newHight = desRect.height();
									scaleY = false;
								}
							} else {
								newHight = desRect.height();
								scaleY = false;
							}
						}
						mBmpRect.top = desRect.top;
						mBmpRect.bottom = mBmpRect.top + newHight;
						canMoveY = false;
					}

					// Animation end.
					if (!scaleX && !scaleY && !canMoveX && !canMoveY) {						
						mBmpRect = desRect;
						postInvalidate();
						isAnimate = false;
						setClipEnable(true);
						break;
					}

					postInvalidate();
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();

	}

	private float movePixelX = 0.0f;
	private float movePixelY = 0.0f;
	private float absMovePixelX = 0.0f;
	private float absMovePixelY = 0.0f;
	private boolean bMoveBasedWidth = true;
	private final int deltaPxBig = 15;
	private final int deltaPxSmall = 2;

	private void preMove(int rangeX, int rangeY) {
		if (Math.abs(rangeX) >= Math.abs(rangeY)) {
			bMoveBasedWidth = true;
		} else {
			bMoveBasedWidth = false;
		}
	}

	private void updateMovePixel(int rangeX, int rangeY) {
		if (bMoveBasedWidth) {
			if (Math.abs(rangeX) >= 30) {
				absMovePixelX = deltaPxBig;
				if (rangeX < 0) {
					movePixelX = -deltaPxBig;
				} else {
					movePixelX = deltaPxBig;
				}
			} else {
				absMovePixelX = deltaPxSmall;
				if (rangeX < 0) {
					movePixelX = -deltaPxSmall;
				} else if (rangeX > 0) {
					movePixelX = deltaPxSmall;
				} else {
					movePixelX = 0;
				}
			}

			movePixelY = rangeY * movePixelX / rangeX;
			absMovePixelY = Math.abs(movePixelY);
		} else {
			if (Math.abs(rangeY) >= 30) {
				absMovePixelY = deltaPxBig;
				if (rangeY < 0) {
					movePixelY = -deltaPxBig;
				} else {
					movePixelY = deltaPxBig;
				}
			} else {
				absMovePixelY = deltaPxSmall;
				if (rangeY < 0) {
					movePixelY = -deltaPxSmall;
				} else if (rangeY > 0) {
					movePixelY = deltaPxSmall;
				} else {
					movePixelY = 0;
				}
			}

			movePixelX = rangeX * movePixelY / rangeY;
			absMovePixelX = Math.abs(movePixelX);
		}

	}

	private void moveBack(final Rect disRect) {
		final int rectW = mBmpRect.width();
		final int rectH = mBmpRect.height();

		final int absDisX = Math.abs(disRect.left - mBmpRect.left);
		final int absDisY = Math.abs(disRect.top - mBmpRect.top);

		final int startX = mBmpRect.left;
		final int startY = mBmpRect.top;
	
		isAnimate = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				float moveX = 0.0f;
				float moveY = 0.0f;

				boolean canMoveX = true;
				boolean canMoveY = true;

				float accumMoveX = 0.0f;
				float accumMoveY = 0.0f;

				preMove(disRect.left - mBmpRect.left, disRect.top
						- mBmpRect.top);
				while (true) {
					updateMovePixel(disRect.left - mBmpRect.left, disRect.top
							- mBmpRect.top);

					if (canMoveX) {
						moveX += absMovePixelX;
						accumMoveX += movePixelX;

						if (moveX < absDisX) {
							mBmpRect.left = startX + (int) accumMoveX;
							mBmpRect.right = mBmpRect.left + rectW;
						} else {
							mBmpRect.left = disRect.left;
							mBmpRect.right = mBmpRect.left + rectW;
							canMoveX = false;
						}
					}

					if (canMoveY) {
						moveY += absMovePixelY;
						accumMoveY += movePixelY;

						if (moveY < absDisY) {
							mBmpRect.top = startY + (int) accumMoveY;
							mBmpRect.bottom = mBmpRect.top + rectH;							
						} else {
							mBmpRect.top = disRect.top;
							mBmpRect.bottom = mBmpRect.top + rectH;
							canMoveY = false;
						}
					}

					// Animation end.
					if (!canMoveX && !canMoveY) {
						postInvalidate();
						isAnimate = false;
						setClipEnable(true);
						break;
					}

					postInvalidate();
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();
	}

	public Bitmap clip() {
		isClip = true;
		Bitmap newBmp = null;
		int startX = 0;
		int startY = 0;

		float radio = 1.0f;
		Matrix matrix = new Matrix();

		if (mBmpRect != null) {
			radio = (float) mBmpRect.width() / mOriginW;
			startX = mClipRect.left - mBmpRect.left;
			startY = mClipRect.top - mBmpRect.top;

			mNewBmpRect.left = mClipRect.left;
			mNewBmpRect.right = mClipRect.right;
			mNewBmpRect.top = mClipRect.top;
			mNewBmpRect.bottom = mClipRect.bottom;

			int clipWidth = (int) (mNewBmpRect.width() / radio) >= mOriginW ? mOriginW :(int) (mNewBmpRect.width() / radio);
			int clipHight = (int) (mNewBmpRect.height() / radio) >= mOriginH? mOriginH: (int) (mNewBmpRect.height() / radio);
			
			clipWidth = clipWidth > 0 ? clipWidth : 1;
			clipHight = clipHight > 0 ? clipHight : 1;

			float scaleW = (float) resultW / clipWidth;
			float scaleH = (float) resultH / clipHight;
			matrix.postScale(scaleW, scaleH);

			newBmp = Bitmap.createBitmap(mContentBMP, (int) (startX / radio),
					(int) (startY / radio), clipWidth, clipHight, matrix, true);
		}

		return newBmp;
	}

	private void setScale(float scale, int flag) {
		if (flag == BIGGER) {
			if (!canScale()) {
				return;
			}

			setBmpRect(mBmpRect.left - (int) (scale * mBmpRect.width()),
					mBmpRect.top - (int) (scale * mBmpRect.height()),
					mBmpRect.right + (int) (scale * mBmpRect.width()),
					mBmpRect.bottom + (int) (scale * mBmpRect.height()));
		} else if (flag == SMALLER) {
			setBmpRect(mBmpRect.left + (int) (scale * mBmpRect.width()),
					mBmpRect.top + (int) (scale * mBmpRect.height()),
					mBmpRect.right - (int) (scale * mBmpRect.width()),
					mBmpRect.bottom - (int) (scale * mBmpRect.height()));
		}
	}

	// based scale height
	private Rect getRectScaleBasedHeight() {
		float scale = (float) mClipRect.height() / mOriginH;
		int newWidth = (int) (mOriginW * scale);

		int top = mClipRect.top;
		int left = mClipRect.left + (int) ((mClipRect.width() - newWidth) / 2);
		int bottom = mClipRect.bottom;
		int right = left + newWidth;

		return new Rect(left, top, right, bottom);
	}

	// based scale width
	private Rect getRectScaleBasedWidth() {
		float scale = (float) mClipRect.width() / mOriginW;
		int newHight = (int) (mOriginH * scale);

		int left = mClipRect.left;
		int top = mClipRect.top + (int) ((mClipRect.height() - newHight) / 2);
		int right = mClipRect.right;
		int bottom = top + newHight;

		return new Rect(left, top, right, bottom);
	}

	private Rect createOriginBmpRect() {
		float scaleX = (float) mClipRect.width() / mOriginW;
		float scaleY = (float) mClipRect.height() / mOriginH;

		if (scaleX < scaleY) {				
			return getRectScaleBasedHeight();
		} else {			
			return getRectScaleBasedWidth();
		}
	}

	private void updateBmpRect(int deltaX, int deltaY) {
		int width = mBmpRect.width();
		int height = mBmpRect.height();

		mBmpRect.left += deltaX;
		mBmpRect.top += deltaY;

		mBmpRect.right = mBmpRect.left + width;
		mBmpRect.bottom = mBmpRect.top + height;
	}

	private void setBmpRect(int left, int top, int right, int bottom) {
		mBmpRect.left = left;
		mBmpRect.top = top;
		mBmpRect.right = right;
		mBmpRect.bottom = bottom;
	}

	private float spacing(MotionEvent event) {	
		float x = 0.0f;
		float y = 0.0f;
		try { 
			x = event.getX(0) - event.getX(1);		
			y = event.getY(0) - event.getY(1);			
		}catch (IllegalArgumentException e) {			
	        e.printStackTrace(); 	        
	    } 			
		return (float) Math.sqrt(x * x + y * y);
	}

	private boolean canScale() {
		if (mOriginW > referW) {
			if (mBmpRect.width() / mOriginW > 5) {
				return false;
			}
		} else {
			if (mBmpRect.width() / referW > 5) {
				return false;
			}
		}

		return true;
	}

	public Bitmap resizeBmp(float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		Bitmap resizeBmp = Bitmap.createBitmap(mContentBMP, 0, 0, mOriginW,
				mOriginH, matrix, true);
		return resizeBmp;
	}

	private void setClipEnable(boolean click) {		
		Message msg = mCropHandler.obtainMessage();
		Bundle data = new Bundle();
		data.putBoolean("click", click);
		msg.setData(data);
		msg.what = CroppingActivity.CLICK_SINGLE;
		mCropHandler.sendMessage(msg);
	}

}