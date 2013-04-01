package in.android.games.in.widget;


import in.android.games.in.utils.ImageUtil;
import in.android.games.in.utils.RuntimeLog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class URLImageView extends ImageView implements Runnable {

	private static final ExecutorService service;
	
	private URL url;
	
	private static final String LOG_TAG = URLImageView.class.getName();
	
	static {
		service = Executors.newFixedThreadPool(3, new ThreadFactory(){
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "HikeImageLoader");
			}
		});
	}
	
	public URLImageView(Context context) {
		super(context);
	}
	
	public URLImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public URLImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void loadFromUrl(URL url){
		if(url.equals(this.url))
			return;
		this.url = url;
		ImageUtil util = new ImageUtil(getContext());
		Bitmap bitmap = util.loadFromCache(url);
		if(bitmap != null){
			RuntimeLog.log("HIT - url:" + url);
			setImageBitmap(bitmap);
		}else{
			service.execute(this);
		}
	}
	
	public void loadFromUrl(String urlStr){
		try {
			loadFromUrl(new URL(urlStr));
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Cannot load image from " + url, e);
		}
	}

	@Override
	public void run() {
		ImageUtil util = new ImageUtil(getContext());
		final Bitmap bitmap;
		try {
			bitmap = util.loadFromHttp(url);
			post(new Runnable(){

				@Override
				public void run() {
					setImageBitmap(bitmap);
				}
				
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public URL getURL(){
		return url;
	}
}
