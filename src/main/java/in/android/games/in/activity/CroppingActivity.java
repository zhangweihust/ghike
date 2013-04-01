package in.android.games.in.activity;

import in.android.games.in.client.ClientFactory;
import in.android.games.in.client.MediaInputStream;
import in.android.games.in.proxy.UserClient;
import in.android.games.in.utils.CropUtil;
import in.android.games.in.utils.DisplayManager;
import in.android.games.in.utils.IImage;
import in.android.games.in.utils.ImageUtil;
import in.android.games.in.widget.CroppingView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import zonesdk.in.android.games.in.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CroppingActivity extends Activity {
	private CroppingView mCroppingView;
	private Button mClipButton;
	private Button mBackButton;
	private LinearLayout mProgress;
	private ImageView mProgressIv;

	private Handler handler;
	private UserClient client;

	private Uri mImgURI;
	private Bitmap mContentBmp;
	private Bitmap mCropBmp;
	private String url = "";

	public static int uploadType = -1;
	public static final int UPLOAD_AVATAR = 0;
	public static final int UPLOAD_COVER = 1;

	public static final int SAVE_FINISH = 100;
	public static final int UPLOAD_FINISH = 101;
	public static final int CLICK_SINGLE = 102;
	
	public static final int LOAD_BIG_IMAGE = 200;
	
	public static final int LOAD_FINISH = 302;

	private String mAvater = "avater.png";
	private String mCover = "cover.png";
	private String mAvatarPath;
	private String mCoverPath;

	private int width;
	private int hight;

	private boolean hasMeasured = false;

	private boolean isGoback;

	private Animation loaderAnim;
	
	private int mTotalStep;
	private int mCurStep = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.cropping_lay);

		DisplayManager.setupVariables(this);

		Bundle bunde = getIntent().getExtras();
		mImgURI = (Uri) bunde.getParcelable("Uri");

		uploadType = getIntent().getIntExtra("type", UPLOAD_AVATAR);

		mAvatarPath = "/data/data/" + getPackageName() + "/files/" + mAvater;
		mCoverPath = "/data/data/" + getPackageName() + "/files/" + mCover;

		loaderAnim = AnimationUtils.loadAnimation(this, R.anim.loader_rotate);
		isGoback = false;
		handler = new Handler();
		client = ClientFactory.getInstance().get(UserClient.class);
		initView();
	}

	public void initView() {
		mClipButton = (Button) findViewById(R.id.clip);
		mBackButton = (Button) findViewById(R.id.back);
		mProgress = (LinearLayout) findViewById(R.id.progress);
		mProgressIv = (ImageView) findViewById(R.id.progress_iv);
		mCroppingView = (CroppingView) findViewById(R.id.cropping);
		mCroppingView.setCropHandler(mCropHandler);

		mCroppingView.postInvalidate();

		ViewTreeObserver vto = mCroppingView.getViewTreeObserver();

		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				if (hasMeasured == false) {
					if (getIntent().getIntExtra("type", UPLOAD_AVATAR) == UPLOAD_AVATAR) {
						width = DisplayManager.dipToPixel(320);
						hight = DisplayManager.dipToPixel(320);
					} else {
						width = DisplayManager.dipToPixel(320);
						hight = DisplayManager.dipToPixel(140);
					}
					
					mClipButton.setEnabled(false);
					mCroppingView.init(mCroppingView.getMeasuredWidth(),
							mCroppingView.getMeasuredHeight(), width, hight);
					mCroppingView.setLocked(true);
					// at last ,load image
					if (mImgURI != null) {
						new BitmapWorkerTask(mImgURI, mCurStep).execute(new Integer[] { 10 });						
					}
					hasMeasured = true;					
				}
				return true;
			}
		});

		mClipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				mCropBmp = mCroppingView.clip();
				if (mCropBmp != null) {
					try {
						mClipButton.setEnabled(false);
						mProgress.setVisibility(View.VISIBLE);
						mProgressIv.setAnimation(loaderAnim);
						save(mCropBmp);
						uploadImage();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCropBmp != null) {
			if (!mCropBmp.isRecycled()) {				
				mCropBmp.recycle();
				System.gc();
			}
		}
		if (mContentBmp != null) {
			if (!mContentBmp.isRecycled()) {				
				mContentBmp.recycle();
				System.gc();
			}
		}

	}

	public void save(final Bitmap bitmap) throws IOException {
		FileOutputStream localFileOutputStream = null;
		if (uploadType == UPLOAD_AVATAR) {
			localFileOutputStream = openFileOutput(mAvater,
					Context.MODE_PRIVATE);
		} else {
			localFileOutputStream = openFileOutput(mCover, Context.MODE_PRIVATE);

		}
		Bitmap.CompressFormat localCompressFormat = Bitmap.CompressFormat.JPEG;
		bitmap.compress(localCompressFormat, 100, localFileOutputStream);

		localFileOutputStream.close();
	}
	
	private Bitmap getBitmapFromUri(Uri uri, int step) {
		Bitmap bitmap = null;		
		int fileSize = getFileSizeFromURI(uri);
		
		if(step == 1){
			//1M
			if(fileSize < 1024 * 1024 * 1){				
				mTotalStep = 1;
				String filePath = getFilePathFromURI(uri);
				bitmap = ImageUtil.decodeFile(filePath);			
			}else{		
				mTotalStep = 2;				
				bitmap = CropUtil.makeBitmap(IImage.UNCONSTRAINED, 50*1024, uri, getContentResolver(), true);				
			}
		}else if(step == 2){						
			bitmap = CropUtil.makeBitmap(uri, getContentResolver(), width * 2, hight * 2);			
		}
		
		String path = getFilePathFromURI(uri);
		
		if(path != null){
			int degree = ImageUtil.getExifOrientation(path);

			if (degree != 0 && bitmap != null) {				
				bitmap = CropUtil.rotate(bitmap, degree);
			}
		}
		
		
		return bitmap;		
	}

	public String getFilePathFromURI(Uri uri){
		String path;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		int actual_image_column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		path = cursor.getString(actual_image_column_index);
		cursor.close();
		return path;
	}
	
	public int getFileSizeFromURI(Uri uri){
		if(uri == null)
			return 0;
		final ContentResolver cr = getContentResolver();
		int size = 0;
		if(uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_FILE)){
			String filePath = uri.getPath();
			size = (int)(new File(filePath)).length();
		}else if(uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_CONTENT)){
			Cursor cursor = cr.query(uri, new String[]{MediaStore.Images.ImageColumns.SIZE}, null, null, null);
			if(!cursor.moveToFirst()){				
				return 0;
			}
			size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
			if(cursor!=null){
				cursor.close();
			}
		}else{			
			return 0;
		}
		
		return size;
	}

	private CropHandler mCropHandler = new CropHandler();

	public class CropHandler extends Handler {
		public CropHandler() {
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == SAVE_FINISH) {
				try {
					uploadImage();
				} catch (IOException e) {
					e.printStackTrace();
				}
				;
			}
			if (msg.what == UPLOAD_FINISH) {			
				goBack();
			}
			if (msg.what == CLICK_SINGLE) {
				if (msg.getData().getBoolean("click")) {
					mClipButton.setEnabled(true);
				} else {
					mClipButton.setEnabled(false);
				}
			}
			
			if(msg.what == LOAD_BIG_IMAGE){				
				new BitmapWorkerTask(mImgURI, 2).execute(new Integer[] { 10 });
			}

		};
	}

	private void uploadImage() throws IOException {
		int size;
		FileInputStream fin = null;

		if (uploadType == UPLOAD_AVATAR) {
			fin = openFileInput(mAvater);
		} else if (uploadType == UPLOAD_COVER) {
			fin = openFileInput(mCover);
		}
		if (fin == null) {
			return;
		}

		size = fin.available();
		final InputStream is = fin;

		final int fileSize = size;
		new ImageUploadTask(is, fileSize).execute(new Integer[] { 10 });
	}

	private void onUploadError(Exception e) {
		e.printStackTrace();
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(CroppingActivity.this, "Sorry.Upload failed!",
						Toast.LENGTH_LONG).show();

				goBack();
			}
		});
	}

	private void goBack() {
		if (isGoback) {
			return;
		}
		isGoback = true;
		Intent i = new Intent();

		if (url != null && url.length() > 0) {
			Bundle b = new Bundle();
			b.putString("url", url);
			b.putInt("type", uploadType);
			i.putExtras(b);
			setResult(RESULT_OK, i);
		} else {
			setResult(RESULT_CANCELED, i);
		}

		finish();
	}

	@Override
	public void onBackPressed() {
		isGoback = true;
		Intent i = new Intent();
		setResult(RESULT_CANCELED, i);
		finish();
	}
	

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		Uri uri;
		int step;
		public BitmapWorkerTask(Uri _uri, int _step) {
			uri = _uri;
			step = _step;
	    }

	    @Override
	    protected Bitmap doInBackground(Integer... params) {	    	
	        return getBitmapFromUri(uri, step);
	    }

	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (bitmap != null) {
	        	if(mContentBmp != null && !mContentBmp.isRecycled()){
	        		mContentBmp.recycle();	  
	        		System.gc();
	        	}	 
	        	mContentBmp = bitmap;
				mCroppingView.setBmp(mContentBmp);
				mCroppingView.postInvalidate();				
			
				if(mCurStep == mTotalStep){						
					mClipButton.setEnabled(true);	
					mCroppingView.setLocked(false);
				}else{
					mCurStep++;
					mCropHandler.obtainMessage(LOAD_BIG_IMAGE).sendToTarget();
				}
	        }
	    }
	}

	class ImageUploadTask extends AsyncTask<Integer, Integer, Void> {

		InputStream inStream = null;
		int contentLength = 0;

		public ImageUploadTask(InputStream inStream, int contentLength) {
			this.inStream = inStream;
			this.contentLength = contentLength;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				String fileName = mAvater;
				if (uploadType == UPLOAD_AVATAR) {
					url = client.uploadAvatar(new MediaInputStream(fileName,
							"image/*", inStream, contentLength));
					if (!isGoback) {
						client.confirmAvatar(url);
					}
				} else if (uploadType == UPLOAD_COVER) {
					url = client.uploadCover(new MediaInputStream(fileName,
							"image/*", inStream, contentLength));
					if (!isGoback) {
						client.confirmCover(url);
					}

				}
				mCropHandler.sendEmptyMessage(UPLOAD_FINISH);
				// Log.d("finish", "finish");
			} catch (Exception e) {
				onUploadError(e);
			}
			return null;
		}
	}

}
