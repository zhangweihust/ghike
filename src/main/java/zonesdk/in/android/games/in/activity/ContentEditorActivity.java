package zonesdk.in.android.games.in.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zonesdk.in.android.games.in.client.ResponseWrapper;
import zonesdk.in.android.games.in.proxy.CommunityClient;
import zonesdk.in.android.games.in.service.ShowPopService;
import zonesdk.in.android.games.in.utils.CookieUtil;
import zonesdk.in.android.games.in.utils.EmotionData;
import zonesdk.in.android.games.in.utils.HttpRequestUtils;
import zonesdk.in.android.games.in.utils.RuntimeLog;
import zonesdk.in.android.games.in.widget.FaceAdapter;
import zonesdk.in.android.games.in.widget.ScrollLayout;
import zonesdk.in.android.games.in.widget.ScrollLayout.PageListener;
import zonesdk.in.android.games.in.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ContentEditorActivity extends BaseActivity implements
		View.OnClickListener {

	public static final String TIP_LOADING = "Loading.";
	
	public static final String TAG = "ContentEditorActivity";
	public static final int Post = 1;
	public static final int Send = 2;
	public static final int Re_Back = 3;
	private ScrollLayout curPage;
	private ImageView sendButton;
	private ImageView backButton;
	private TextView headerTitle;

	private TextView contentCount;
	private RelativeLayout faceLay;
	private ImageView facePage;

	private GridView faceGrid;

	private int PageCount;

	private int gameId = -1;
	private int forumId = -1;
	private String gameName = "";

	private static final float PAGE_SIZE = 28.0f;
	private EditText content;
	private EditText subject;
	private ImageButton emotionLeftButton;
	private ToggleButton btnFaceInputChange;
	// private boolean isFirst = true;
	private String title = "";

	private String contentVal = "";

	private RelativeLayout atfriendsView;
	private View footerView;
	private ImageView loadbar;
	private ListView atFriendsList;
	private TextView noFriends;
	private TextView showMore;
	private static final String MY_FRIEND_LIST_URL = "newsfeed/myfriends";
	private static final int MY_FRIEND_PAGE_SIZE = 10;
	private String passportMyCookieValue;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.content_edit_layout);

		RuntimeLog.log("ContentEditorActivity.onCreate()");

		Intent d = getIntent();
		String buttonName = d.getStringExtra("buttonName");
		title = d.getStringExtra("title");
		String gameTitle = d.getStringExtra("gameTitle");
		String defaultContent = d.getStringExtra("defaultContent");
		contentVal = d.getStringExtra("contentVal");
		boolean requireSubject = Boolean.parseBoolean(d
				.getStringExtra("requireSubject"));
		gameId = d.getIntExtra("gameId", -1);
		forumId = d.getIntExtra("forumId", -1);
		gameName = d.getStringExtra("gameName");

		this.passportMyCookieValue = CookieUtil.getCookiePassport(this);
		
		mTypeface = Typeface.createFromAsset(getAssets(), "font/Edmondsans-Regular.otf");
		initViews(buttonName, title, gameTitle, defaultContent, requireSubject);
		new FriendDataTask().execute(mFriendPage, MY_FRIEND_PAGE_SIZE);
	}

	private void initViews(String buttonName, String title, String gameTitle,
			String defaultContent, boolean requireSubject) {
		curPage = (ScrollLayout) findViewById(R.id.face_bg_lay);
		sendButton = (ImageView) findViewById(R.id.ContentEditorRightButton);
		backButton = (ImageView) findViewById(R.id.ContentEditorLeftButton);
		headerTitle = (TextView) findViewById(R.id.HeadContentEditorText);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"font/Edmondsans-Regular.otf");
		headerTitle.setTypeface(typeFace);
		content = (EditText) findViewById(R.id.ContentEditText);
		subject = (EditText) findViewById(R.id.SubjectEditText);

		if (contentVal != null && !"".equals(contentVal)) {
			content.setText(contentVal);
			content.setSelection(contentVal.length());
		} else {
			content.setHint(defaultContent);
			// content.setSelection(defaultContent.length());
		}

		if (title != null && title.contains("New Topic")) {
			content.setHint(getResources().getString(R.string.add_topic));
		} else if (title != null && title.contains("Reply")) {
			content.setHint(getResources().getString(R.string.status_reply));
		} else if (title != null && title.contains("Comment")) {
			content.setHint(getResources().getString(R.string.status_comment));
		}
		if (title != null) {
			headerTitle.setText(title);
		}

		if (!requireSubject) {
			subject.setText(gameTitle + " discussion");
		}

		backButton.setOnClickListener(this);
		sendButton.setOnClickListener(this);
		emotionLeftButton = (ImageButton) findViewById(R.id.emotionLeftButton);
		emotionLeftButton.setOnClickListener(this);
		contentCount = (TextView) findViewById(R.id.emotionRightCountText);
		content.addTextChangedListener(mTextWatcher);
		content.setOnTouchListener(new EditOnTouchListener());
		((TextView) findViewById(R.id.emotionRightText)).setTypeface(typeFace);
		content.setTypeface(typeFace);
		contentCount.setTypeface(typeFace);
		faceLay = (RelativeLayout) findViewById(R.id.face_lay);
		facePage = (ImageView) findViewById(R.id.face_page);
		curPage = (ScrollLayout) findViewById(R.id.face_bg_lay);
		curPage.setPageListener(faceChagneListener);
		initFace();
		btnFaceInputChange = (ToggleButton) findViewById(R.id.keyboard_smail_btn);
		btnFaceInputChange.setOnCheckedChangeListener(new ViewChangeListener());
		// ResizeLayout layout = (ResizeLayout)
		// findViewById(R.id.layout_content);
		atfriendsView = (RelativeLayout) findViewById(R.id.view_atfriends);
		atFriendsList = (ListView) findViewById(R.id.list_atfriends);
		footerView = LayoutInflater.from(this).inflate(
				R.layout.relation_footer, null);
		showMore = (TextView) footerView.findViewById(R.id.relation_load_more);
		showMore.setTypeface(typeFace);
		atFriendsList.addFooterView(footerView, null, false);
		atFriendsList.setAdapter(mFriendsListAdapter);
		atFriendsList.setOnItemClickListener(onListListener);
		
		loadbar = (ImageView) footerView.findViewById(R.id.main_loader_img);

		noFriends = (TextView) findViewById(R.id.list_no_friends);
		noFriends.setTypeface(typeFace);
		LayoutParams layoutParams = content.getLayoutParams();
		layoutParams.height = this.getWindowManager().getDefaultDisplay()
				.getHeight() * 2 / 5;
		content.setLayoutParams(layoutParams);
	}

	private class FriendDataTask extends
			AsyncTask<Integer, Exception, ArrayList<String>> {
		
		boolean isAll = false;	
		
		int page = 0;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Animation anim = AnimationUtils.loadAnimation(ContentEditorActivity.this, R.anim.loader_rotate);
			loadbar.setVisibility(View.VISIBLE);
			footerView.setVisibility(View.VISIBLE);
			loadbar.startAnimation(anim);
		}

		@Override
		protected ArrayList<String> doInBackground(Integer... params) {
			page = params[0];
			ArrayList<String> result = new ArrayList<String>();
			ResponseWrapper responseWrapper = null;
			HashMap<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("curPage", page + "");
			paramMap.put("pSize", MY_FRIEND_PAGE_SIZE + "");
			responseWrapper = HttpRequestUtils.sendPostRequest(
					MY_FRIEND_LIST_URL, paramMap, passportMyCookieValue);
			
			if (responseWrapper == null) {
				return result;
			}

			JSONObject jsonObject = responseWrapper.getRespJson();
			try {
				JSONArray jsonArray = new JSONArray(jsonObject.optJSONArray(
						"list").toString());
				for (int i = 0; i < jsonArray.length(); i++) {
					String title = "";
					title = jsonArray.optString(i);
					result.add(title);
				}
				isAll = (Boolean) jsonObject.optBoolean("isAll", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(final ArrayList<String> result) {
			loadbar.clearAnimation();
			mFriendList.addAll(result);
			mFriendPage = this.page + 1;
			if (isAll && atFriendsList.getFooterViewsCount() > 0) {
				atFriendsList.removeFooterView(footerView);
			}
			if (isAll && result.size() == 0){
				noFriends.setVisibility(View.VISIBLE);
			}else{
				noFriends.setVisibility(View.GONE);
			}
			if (atFriendsList.getFooterViewsCount() > 0) {
				loadbar.setVisibility(View.GONE);
				footerView.setVisibility(View.VISIBLE);
				footerView.setOnClickListener(mFooterListener);
			}
			mFriendsListAdapter.notifyDataSetChanged();
			atFriendsList.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		RuntimeLog.log("ContactEditorActivity.onRestart()");
		// isFirst = true;
		showIme();
	}

	@Override
	protected void onPause() {
		super.onPause();
		RuntimeLog.log("ContactEditorActivity.onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();

		RuntimeLog.log("ContactEditorActivity.onStop()");

	}

	private class ViewChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			switch (buttonView.getId()) {

			case R.id.keyboard_smail_btn:
				if (isChecked) {
					btnFaceInputChange.setBackgroundDrawable(getResources()
							.getDrawable(R.drawable.smail));
					faceLay.setVisibility(View.GONE);
					atfriendsView.setVisibility(View.GONE);
					showIme();
				} else {
					btnFaceInputChange.setBackgroundDrawable(getResources()
							.getDrawable(R.drawable.keyboard));
					atfriendsView.setVisibility(View.GONE);
					faceLay.postDelayed(new Runnable() {

						@Override
						public void run() {
							faceLay.setVisibility(View.VISIBLE);
						}

					}, 100);
					hideIme();
				}
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ContentEditorLeftButton:
			// InputMethodManager imm
			// =(InputMethodManager)this.getSystemService(ContentEditorActivity.INPUT_METHOD_SERVICE);
			// boolean
			// flag=imm.hideSoftInputFromWindow(btnFaceInputChange.getWindowToken(),
			// 0);
			// if(flag){
			// imm.hideSoftInputFromWindow(backButton.getWindowToken(), 0);
			// }else{
			// if("Comment".equals(title)){
			// ShowPopService.instance.showView("Comment");
			// } else {
			// setResult(Activity.RESULT_CANCELED);
			// }
			// finish();
			// }
			if ("Comment".equals(title)) {
				ShowPopService.instance.createView("Comment");
			} else {
				setResult(Activity.RESULT_CANCELED);
			}
			finish();
			break;
		case R.id.ContentEditorRightButton:
			String contentStr = content.getText().toString();
			String subjectStr = subject.getText().toString();
			if (TextUtils.isEmpty(contentStr)) {
				Toast.makeText(this,
						getResources().getString(R.string.no_content_tips),
						Toast.LENGTH_LONG).show();
				return;
			}
			if ("Comment".equals(title)) {
				new SendCommentTask().execute();
			} else {
				Intent data = new Intent();
				data.putExtra("content", contentStr);
				data.putExtra("subject", subjectStr);
				setResult(Activity.RESULT_OK, data);
				finish();
			}

			break;
		case R.id.emotionLeftButton:
			changeAtfriendsView();
			break;
		default:
			break;
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			int count = content.getText().toString().length();
			contentCount.setText(count + "");
			if (count > 140) {
				Toast.makeText(ContentEditorActivity.this,
						R.string.character_limit, Toast.LENGTH_SHORT).show();
			}

			if (s.length() > 140) {
				int pos = s.length() - 1;
				s.delete(pos, pos + 1);
			}
		}
	};
	PageListener faceChagneListener = new PageListener() {

		@Override
		public void page(int page) {
			switch (page) {
			case 0:
				facePage.setBackgroundResource(R.drawable.face_page1);
				break;
			case 1:
				facePage.setBackgroundResource(R.drawable.face_page2);
				break;
			case 2:
				facePage.setBackgroundResource(R.drawable.face_page3);
				break;
			default:
				facePage.setBackgroundResource(R.drawable.face_page1);
				break;
			}
		}

	};
	
	@Override
	protected void onResume() {
		super.onResume();
		RuntimeLog.log("ContactEditorActivity.onResume()");
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager m = (InputMethodManager) content
						.getContext().getSystemService(
								Context.INPUT_METHOD_SERVICE);
				if (m.isActive()) {// If is the hidden soft keyboard to open the
									// soft keyboard
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(ContentEditorActivity.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
				}
				if (faceLay.getVisibility() == View.GONE
						&& atfriendsView.getVisibility() == View.GONE) {
					// Display the soft keyboard
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.showSoftInput(content, 0);
				}
			}
		}, 500);
	}

	private void initFace() {

		int[] faceData = EmotionData.EmotionRes;
		PageCount = (int) Math.ceil(faceData.length / PAGE_SIZE);
		if (faceGrid != null) {
			curPage.removeAllViews();
		}

		for (int i = 0; i < PageCount; i++) {
			faceGrid = new GridView(this);
			faceGrid.setAdapter(new FaceAdapter(faceData, this, i));
			faceGrid.setNumColumns(7);
			faceGrid.setHorizontalSpacing(1);
			faceGrid.setVerticalSpacing(1);
			faceGrid.setOnItemClickListener(new OnFaceInfoListener());
			curPage.addView(faceGrid);
		}
	}

	private class OnFaceInfoListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// fix chenYin's Condition
			if (content.getText().toString().length() >= 134) {
				Toast.makeText(ContentEditorActivity.this,
						R.string.character_limit, Toast.LENGTH_SHORT).show();
				return;
			}
			Integer faceRes = (Integer) parent.getAdapter().getItem(position);
			String iconName = getResources().getResourceEntryName(faceRes);
			StringBuffer iconNameStringBuffer = new StringBuffer();
			iconNameStringBuffer.append("[" + iconName + "]");
			Drawable faceIcon = ContentEditorActivity.this.getResources()
					.getDrawable(faceRes);
			int hight = ContentEditorActivity.this.getWindowManager()
					.getDefaultDisplay().getHeight();
			int width = ContentEditorActivity.this.getWindowManager()
					.getDefaultDisplay().getWidth();
			Log.d("===========width", width + "=============hight" + hight);
			faceIcon.setBounds(0, 0, width / 16, width / 16);
			ImageSpan imageSpan = new ImageSpan(faceIcon);
			SpannableString text = new SpannableString(iconNameStringBuffer);
			text.setSpan(imageSpan, 0, iconNameStringBuffer.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int cursor = content.getSelectionStart();
			content.getText().insert(cursor, text);

		}
	}

	private void hideIme() {
		InputMethodManager im = ((InputMethodManager) getSystemService("input_method"));
		im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
				0);
	}

	private void showIme() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	class EditOnTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			btnFaceInputChange.setChecked(true);
			atfriendsView.setVisibility(View.GONE);
			return false;
		}

	}

	private class SendCommentTask extends
			AsyncTask<Void, Exception, JSONObject> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			sendButton.setEnabled(false);
		}

		@Override
		protected JSONObject doInBackground(Void... params) {
			CommunityClient client = getProxy(CommunityClient.class);
			try {
				return client.sendTopic(forumId, gameId, gameName
						+ " discussion ", content.getText().toString());
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (result == null || !result.optString("status").equals("0")) {
				Toast.makeText(ContentEditorActivity.this, "Send failed!",
						Toast.LENGTH_SHORT).show();
				sendButton.setEnabled(true);
				return;
			} else {
				ShowPopService.instance.createView("View All");
				Toast.makeText(ContentEditorActivity.this,
						"Send successfully!", Toast.LENGTH_SHORT).show();
				getTracker().trackEvent("topic", "sendTopicFromPopup", content.getText().toString(), 0);
				ContentEditorActivity.this.finish();
			}

		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getAction() != KeyEvent.ACTION_UP)
			return super.dispatchKeyEvent(event);
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ){
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void changeAtfriendsView() {
		faceLay.setVisibility(View.GONE);
		atfriendsView.setVisibility(View.VISIBLE);
		hideIme();
	}

	public OnItemClickListener onListListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {
			setRalationEditText(arg0.getAdapter().getItem(position) + "",
					position - 1);
		}
	};

	private void setRalationEditText(String data, int position) {
		int cursor = content.getSelectionStart();
		String ralationEditStr = content.getText().toString();
		int count = ralationEditStr.split("@").length;
		if (count > 10) {
			content.getText().insert(cursor, "");
		} else {
			content.getText().insert(cursor, "@" + data + " ");
		}
	}
	
	int mFriendPage = 1;

	private OnClickListener mFooterListener = new OnClickListener() {

		@SuppressWarnings("unchecked")
		@Override
		public void onClick(View v) {
			new FriendDataTask().execute(mFriendPage, MY_FRIEND_PAGE_SIZE);
		}

	};
	
	private Typeface mTypeface;
	
	private ArrayList<String> mFriendList = new ArrayList<String>();

	public BaseAdapter mFriendsListAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			if (mFriendList == null)
				return 0;
			return mFriendList.size();
		}

		@Override
		public Object getItem(int position) {
			if (mFriendList == null)
				return null;
			return mFriendList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView item = null;
			if (convertView == null) {
				item = (TextView)LayoutInflater.from(ContentEditorActivity.this).inflate(
						R.layout.relation_list_item, null);
				item.setTypeface(mTypeface);
			} else {
				item = (TextView)convertView;
			}
			item.setText(mFriendList.get(position));
			return item;
		}

	};

}
