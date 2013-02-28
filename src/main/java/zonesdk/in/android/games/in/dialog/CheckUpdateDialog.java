package zonesdk.in.android.games.in.dialog;

import zonesdk.in.android.games.in.R;
import zonesdk.in.android.games.in.activity.HikeMainActivity;
import zonesdk.in.android.games.in.common.Constants;
import zonesdk.in.android.games.in.update.UpdateManager;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CheckUpdateDialog extends Dialog implements View.OnClickListener {

	Context mContext;

	View textEntryView;
	private TextView title;
	private TextView content;
	private Button sureBtn;
	private Button cancelBtn;
	private Button onlyBtn;
	private View partLine;
	ImageView aniImage;
	Animation animation;
	UpdateManager appMgr;

	RelativeLayout twobtn_Layout;
	RelativeLayout onebtn_Layout;

	int dialogType;

	private Handler mHandler;

	final static public int TYPE_LOADING = 0;
	final static public int TYPE_UPDATE_UNFORCE = 1;
	final static public int TYPE_UPDATE_FORCE = 2;
	final static public int TYPE_NO_UPDATE = 3;

	private boolean isDismiss = false;

	/*
	 * public CheckUpdateDialog(UpdateManager appM, Context context, Handler
	 * handler) {
	 */
	public CheckUpdateDialog(UpdateManager appM, Context context) {
		super(context, R.style.dialog);
		// TODO Auto-generated constructor stub
		Typeface typeFace = Typeface.createFromAsset(context.getAssets(),
				"font/Edmondsans-Regular.otf");
		Typeface typeFace1 = Typeface.createFromAsset(context.getAssets(),
				"font/HelveticaNeue-Roman.otf");

		mContext = context;
		appMgr = appM;
		// mHandler = handler;

		LayoutInflater factory = LayoutInflater.from(context);
		textEntryView = factory.inflate(R.layout.version_dialog_layout, null);
		title = (TextView) textEntryView.findViewById(R.id.titleView);
		content = (TextView) textEntryView.findViewById(R.id.contentView);
		partLine = (View) textEntryView.findViewById(R.id.partline);
		aniImage = (ImageView) textEntryView.findViewById(R.id.loadanim);

		sureBtn = (Button) textEntryView.findViewById(R.id.sureBtn);
		cancelBtn = (Button) textEntryView.findViewById(R.id.cancelBtn);
		onlyBtn = (Button) textEntryView.findViewById(R.id.onlyBtn);
		onebtn_Layout = (RelativeLayout) textEntryView
				.findViewById(R.id.linearLayout_onebtn);
		twobtn_Layout = (RelativeLayout) textEntryView
				.findViewById(R.id.linearLayout_twobtn);

		title.setTypeface(typeFace1);
		sureBtn.setTypeface(typeFace1);
		cancelBtn.setTypeface(typeFace1);

		content.setTypeface(typeFace);

		sureBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		onlyBtn.setOnClickListener(this);
		// initView(textEntryView);
		setContentView(textEntryView);
	}

	public void initView(int type) {
		RuntimeLog.log("CheckUpdateDialog - initView - type: " + type);

		dialogType = type;
		if (dialogType != TYPE_UPDATE_FORCE) {
			setCanceledOnTouchOutside(true);
		}else{
			setCanceledOnTouchOutside(false);
		}
		
		title.setVisibility(View.VISIBLE);
		content.setVisibility(View.VISIBLE);

		// type 0: loading style
		// type 1: update but no force
		// type 2: update but force
		// type 3: no update
		/*
		 * TYPE_LOADING = 0; TYPE_UPDATE_UNFORCE = 1; TYPE_UPDATE_FORCE = 2;
		 * TYPE_NO_UPDATE = 3;
		 */
		if (dialogType == TYPE_LOADING) {
			/*
			 * sureBtn.setVisibility(View.GONE);
			 * cancelBtn.setVisibility(View.GONE);
			 * partLine.setVisibility(View.GONE);
			 */
			onebtn_Layout.setVisibility(View.GONE);
			twobtn_Layout.setVisibility(View.GONE);

			aniImage.setVisibility(View.VISIBLE);
			animation = AnimationUtils.loadAnimation(mContext,
					R.anim.loader_rotate);
			aniImage.startAnimation(animation);

			title.setText(R.string.checkupdate_title);
			content.setText(R.string.checkupdate_contents);

		} else if (type == TYPE_UPDATE_UNFORCE) {			
			String contentTxt = "A new version is available!" + "\n\nVersion: "
					+ appMgr.getremoteVersionName()
					+ "\n\nPlease update to the latest version!";
			onebtn_Layout.setVisibility(View.GONE);
			twobtn_Layout.setVisibility(View.VISIBLE);

			aniImage.setVisibility(View.GONE);
			// animation = AnimationUtils.loadAnimation(mContext,
			// R.anim.loader_rotate);
			// aniImage.startAnimation(animation);
			aniImage.clearAnimation();

			/*
			 * sureBtn.setVisibility(View.VISIBLE);
			 * cancelBtn.setVisibility(View.VISIBLE);
			 */
			partLine.setVisibility(View.VISIBLE);

			title.setText(R.string.newversion_title);
			content.setText(contentTxt);
			sureBtn.setText(R.string.btn_update);
			cancelBtn.setText(R.string.btn_notnow);

		} else if (type == TYPE_UPDATE_FORCE) {
			String contentTxt = "A new version is available!" + "\n\nVersion: "
					+ appMgr.getremoteVersionName()
					+ "\n\nzone has to be updated or it cannot work."
					+ "\nSorry for any inconvenience caused.";

			onebtn_Layout.setVisibility(View.VISIBLE);
			twobtn_Layout.setVisibility(View.GONE);

			aniImage.setVisibility(View.GONE);
			aniImage.clearAnimation();

			partLine.setVisibility(View.VISIBLE);
			title.setText(R.string.newversion_title);
			content.setText(contentTxt);

			onlyBtn.setText(R.string.btn_update);
			onlyBtn.setBackgroundResource(R.drawable.surebtn_shape_white);

		} else if (type == TYPE_NO_UPDATE) {
			String contentTxt = "Your version is currently up-to-date.";
			/*
			 * onebtn_Layout.setVisibility(View.VISIBLE);
			 * twobtn_Layout.setVisibility(View.GONE);
			 * 
			 * aniImage.setVisibility(View.GONE); aniImage.clearAnimation();
			 * 
			 * partLine.setVisibility(View.VISIBLE);
			 * 
			 * onlyBtn.setTextColor(0x000000);
			 * onlyBtn.setText(R.string.btn_update);
			 * //onlyBtn.setBackgroundResource
			 * (R.drawable.cancelbtn_shape_white);
			 * 
			 * title.setText(R.string.checkupdate_title);
			 * content.setText(contentTxt);
			 */
			onebtn_Layout.setVisibility(View.VISIBLE);
			twobtn_Layout.setVisibility(View.GONE);

			aniImage.setVisibility(View.GONE);
			aniImage.clearAnimation();

			partLine.setVisibility(View.GONE);

			title.setText(R.string.checkupdate_title);
			content.setText(contentTxt);

			onlyBtn.setText(R.string.btn_ok);
			Resources resource = (Resources) mContext.getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.gray);
			onlyBtn.setTextColor(csl);
			/* onlyBtn.setBackgroundResource(R.drawable.surebtn_shape_white); */
			onlyBtn.setBackgroundResource(R.drawable.cancelbtn_shape_white);

		}

		show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (dialogType == TYPE_UPDATE_FORCE) {
				// mHandler.obtainMessage(HikeMainActivity.HANDLER_CLOSEAPP).sendToTarget();
				RuntimeLog.log("dispatchKeyEvent  KEYCODE_BACK");
				Intent intent = new Intent(Constants.CLOSE_ALL_ACTIVITY);
				// You can also include some extra data.
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(
						intent);
			} else {
				isDismiss = true;
				dismiss();
			}
		}
		//

		return super.dispatchKeyEvent(event);
	}

	public boolean isDismiss() {
		return isDismiss;
	}

	public void setDismiss(boolean dismiss) {
		isDismiss = dismiss;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sureBtn: {
			doUpdate();
			break;
		}

		case R.id.cancelBtn: {
			cancel();
			break;
		}

		case R.id.onlyBtn: {
			// only TYPE_UPDATE_FORCE or TYPE_NO_UPDATE
			if (dialogType == TYPE_UPDATE_FORCE) {
				doUpdate();
				Intent intent = new Intent(Constants.CLOSE_ALL_ACTIVITY);
				// You can also include some extra data.
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(
						intent);
			} else if (dialogType == TYPE_NO_UPDATE) {
				cancel();
			}

		}

		}
	}

	private void doUpdate() {
		if (dialogType != TYPE_UPDATE_FORCE) {
			cancel();
		}

		String Downloadurl = appMgr.getremoteDownloadurl();
		if (Downloadurl == null) {
			RuntimeLog
					.log("CheckUpdateDialog - doUpdate, Downloadurl is null!");
			return;
		}
		
		Uri uri = Uri.parse(Downloadurl);
		
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			mContext.startActivity(intent);
		}catch(Exception e) {
			e.printStackTrace();			
		}

	}
}