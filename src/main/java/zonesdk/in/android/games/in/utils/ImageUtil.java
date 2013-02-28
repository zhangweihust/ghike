package zonesdk.in.android.games.in.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ImageUtil {

	private Context mContext;
	
	public ImageUtil(Context context){
		mContext = context;
	}
	
	public Bitmap loadFromCache(URL url){
		String temp = MD5.encode(url.toString().getBytes());
		File tempFile = mContext.getFileStreamPath(temp);
		try{
			if(tempFile.exists()){
				FileInputStream fs = new FileInputStream(tempFile);
				Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 2;
				return BitmapFactory.decodeStream(fs, null, opt);
			}
		}catch(IOException e){}
		return null;
	}
	
	public Bitmap loadFromHttp(URL url) throws IOException{
		String temp = MD5.encode(url.toString().getBytes());
		final File tempFile = mContext.getFileStreamPath(temp);
		try{
			tempFile.createNewFile();
			InputStream input = url.openConnection().getInputStream();
			FileOutputStream fo = new FileOutputStream(tempFile);
			byte[] buf = new byte[4096];
			int len = -1;
			while((len = input.read(buf)) > 0){
				fo.write(buf, 0, len);
			}
			fo.close();
			input.close();
			FileInputStream fs = new FileInputStream(tempFile);Options opt = new BitmapFactory.Options();
			opt.inSampleSize = 2;
			return BitmapFactory.decodeStream(fs, null, opt);
		}catch(IOException e){
			if(tempFile.exists()){
				tempFile.delete();
			}
			throw e;
		}
	}
	

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
	
	public static boolean isInternetConnected(Context ctx) {
		ConnectivityManager manager = (ConnectivityManager) ctx.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected())
			return false;
		else
			return true;
	}
	
	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
			Log.e("CroppingActivity", "cannot read exif", ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			}
		}
		return degree;
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
	
	public static Bitmap decodeFile(String filePath) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
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
		if (filePath != null) {
			bitmap = BitmapFactory.decodeFile(filePath, options);
		}
		return bitmap;
	}
	
	

	
}
