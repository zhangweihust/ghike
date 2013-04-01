package in.android.games.in.utils;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class CropUtil {
	private static final String TAG = "Util";
	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_RIGHT = 1;
	public static final int DIRECTION_UP = 2;
	public static final int DIRECTION_DOWN = 3;

	private CropUtil() {
	}

	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		return b;
	}

	public static BitmapFactory.Options getOptions(Uri uri, ContentResolver cr) {
		BitmapFactory.Options options = null;
		ParcelFileDescriptor pfd = null;
		try {
			pfd = cr.openFileDescriptor(uri, "r");
			if (pfd == null)
				pfd = makeInputStream(uri, cr);
			if (pfd == null)
				return null;
			if (options == null)
				options = new BitmapFactory.Options();

			FileDescriptor fd = pfd.getFileDescriptor();
			options.inJustDecodeBounds = true;
			BitmapManager.instance().decodeFileDescriptor(fd, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			return options;
		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			closeSilently(pfd);
		}
	}
	
	public static Bitmap makeBitmap(Uri uri, ContentResolver cr,
			int reqWidth, int reqHeight) {
		int sampleSize = calculateInSampleSize(uri, cr, reqWidth, reqHeight);			
		
		
		Log.d("makeBitmap","makeBitmap:" + sampleSize);
		Log.d("makeBitmap","reqWidth:" + reqWidth);
		Log.d("makeBitmap","reqHeight:" + reqHeight);
		
		return makeBitmap(uri, cr, sampleSize);
	}
	
	public static int calculateInSampleSize(Rect outPadding,
			int reqWidth, int reqHeight) {		
		final int height = outPadding.height();
		final int width = outPadding.width();

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static int calculateInSampleSize(Uri uri, ContentResolver cr,
			int reqWidth, int reqHeight) {
		BitmapFactory.Options options = null;
		ParcelFileDescriptor pfd = null;
		FileDescriptor fd = null;

		try {
			pfd = cr.openFileDescriptor(uri, "r");
			if (pfd == null)
				pfd = makeInputStream(uri, cr);
			if (pfd == null)
				return 1;
			if (options == null)
				options = new BitmapFactory.Options();

			fd = pfd.getFileDescriptor();
			options.inJustDecodeBounds = true;
			BitmapManager.instance().decodeFileDescriptor(fd, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return 1;
			}
		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return 1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 1;
		} finally {
			closeSilently(pfd);
		}

		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap makeBitmap(Uri uri, ContentResolver cr,
			int sampleSize) {
		ParcelFileDescriptor input = null;
		try {
			input = cr.openFileDescriptor(uri, "r");

			return makeBitmap(uri, cr, input, sampleSize);
		} catch (IOException ex) {
			return null;
		} finally {
			closeSilently(input);
		}
	}

	public static Bitmap makeBitmap(Uri uri, ContentResolver cr,
			ParcelFileDescriptor pfd, int sampleSize) {
		try {
			if (pfd == null)
				pfd = makeInputStream(uri, cr);
			if (pfd == null)
				return null;			

			final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    
			FileDescriptor fd = pfd.getFileDescriptor();
			
			BitmapManager.instance().decodeFileDescriptor(fd, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = sampleSize;
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			return BitmapManager.instance().decodeFileDescriptor(fd, options);
		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return null;
		} finally {
			closeSilently(pfd);
		}
	}


	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == IImage.UNCONSTRAINED) ? 1
				: (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == IImage.UNCONSTRAINED) ? 128
				: (int) Math.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == IImage.UNCONSTRAINED)
				&& (minSideLength == IImage.UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == IImage.UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static final boolean RECYCLE_INPUT = true;
	public static final boolean NO_RECYCLE_INPUT = false;

	public static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp, boolean recycle) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		if (recycle && b1 != source) {
			source.recycle();
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);

		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}

		return b2;
	}

	public static <T> int indexOf(T[] array, T s) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public static void closeSilently(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
		}
	}

	public static void closeSilently(ParcelFileDescriptor c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
		}
	}

	public static Bitmap makeBitmap(byte[] jpegData, int maxNumOfPixels) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory
					.decodeByteArray(jpegData, 0, jpegData.length, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options,
					IImage.UNCONSTRAINED, maxNumOfPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length,
					options);
		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return null;
		}
	}

	public static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels,
			Uri uri, ContentResolver cr, boolean useNative) {
		ParcelFileDescriptor input = null;
		try {
			input = cr.openFileDescriptor(uri, "r");
			BitmapFactory.Options options = null;
			if (useNative) {
				options = createNativeAllocOptions();
			}
			return makeBitmap(minSideLength, maxNumOfPixels, uri, cr, input,
					options);
		} catch (IOException ex) {
			return null;
		} finally {
			closeSilently(input);
		}
	}

	public static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels,
			ParcelFileDescriptor pfd, boolean useNative) {
		BitmapFactory.Options options = null;
		if (useNative) {
			options = createNativeAllocOptions();
		}
		return makeBitmap(minSideLength, maxNumOfPixels, null, null, pfd,
				options);
	}

	public static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels,
			Uri uri, ContentResolver cr, ParcelFileDescriptor pfd,
			BitmapFactory.Options options) {
		try {
			if (pfd == null)
				pfd = makeInputStream(uri, cr);
			if (pfd == null)
				return null;
			if (options == null)
				options = new BitmapFactory.Options();

			FileDescriptor fd = pfd.getFileDescriptor();
			options.inJustDecodeBounds = true;
			BitmapManager.instance().decodeFileDescriptor(fd, options);
			if (options.mCancel || options.outWidth == -1
					|| options.outHeight == -1) {
				return null;
			}
			options.inSampleSize = computeSampleSize(options, minSideLength,
					maxNumOfPixels);
			options.inJustDecodeBounds = false;

			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			return BitmapManager.instance().decodeFileDescriptor(fd, options);
		} catch (OutOfMemoryError ex) {
			Log.e(TAG, "Got oom exception ", ex);
			return null;
		} finally {
			closeSilently(pfd);
		}
	}

	private static ParcelFileDescriptor makeInputStream(Uri uri,
			ContentResolver cr) {
		try {
			return cr.openFileDescriptor(uri, "r");
		} catch (IOException ex) {
			return null;
		}
	}

	public static void Assert(boolean cond) {
		if (!cond) {
			throw new AssertionError();
		}
	}

	public static boolean equals(String a, String b) {
		return a == b || a.equals(b);
	}

	public static BitmapFactory.Options createNativeAllocOptions() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		try {
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(
					options, true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return options;
	}

}