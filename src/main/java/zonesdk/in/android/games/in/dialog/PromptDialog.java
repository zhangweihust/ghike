package zonesdk.in.android.games.in.dialog;

import zonesdk.in.android.games.in.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PromptDialog extends Dialog{

	private Builder mBuilder;
	
	private TextView mTitle;
	
	private TextView mDisplayText;
	
	private EditText mContentEditText;

	private Button mConfirmButton;
	
	private Button mCancelButton;
	
	private OnConfirmListener mOnConfirmListener;
	
	private PromptDialog(Builder builder){
		super(builder.mContext, R.style.dialog);
		mBuilder = builder;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(true);
		setCanceledOnTouchOutside(false);
		initItems();
		applyItems();
	}
	
	private void initItems(){
		setContentView(R.layout.dialog_layout);
		Typeface t1 = Typeface.createFromAsset(getContext().getAssets(), "font/Edmondsans-Regular.otf");
		Typeface t2 = Typeface.createFromAsset(getContext().getAssets(), "font/HelveticaNeue-Roman.otf");
		mTitle = (TextView)findViewById(R.id.dialog_title_text);
		mTitle.setTypeface(t1);
		mDisplayText = (TextView)findViewById(R.id.dialog_display_text);
		mDisplayText.setTypeface(t1);
		mContentEditText = (EditText)findViewById(R.id.dialog_input_content);
		mContentEditText.setTypeface(t1);
		mConfirmButton = (Button)findViewById(R.id.dialog_confirm_button);
		mConfirmButton.setTypeface(t2);
		mCancelButton = (Button)findViewById(R.id.dialog_cancel_button);
		mCancelButton.setTypeface(t2);
	}
	
	private void applyItems(){
		// title
		if(mBuilder.mTitle != null){
			mTitle.setText(mBuilder.mTitle);
			mTitle.setVisibility(View.VISIBLE);
		}else{
			mTitle.setVisibility(View.GONE);
		}
		// display text
		if(mBuilder.mText != null){
			mDisplayText.setText(mBuilder.mText);
			mDisplayText.setVisibility(View.VISIBLE);
		}else{
			mDisplayText.setVisibility(View.GONE);
		}
		// input text
		if(mBuilder.mEditable){
			if(mBuilder.mContent != null)
				mContentEditText.setText(mBuilder.mContent);
			if(mBuilder.mPlaceholder != null)
				mContentEditText.setHint(mBuilder.mPlaceholder);
			mContentEditText.setVisibility(View.VISIBLE);
		}else{
			mContentEditText.setVisibility(View.GONE);
		}
		// left button
		if(mBuilder.mConfirmButton != null){
			mConfirmButton.setText(mBuilder.mConfirmButton);
			mConfirmButton.setVisibility(View.VISIBLE);
		}else{
			mConfirmButton.setVisibility(View.GONE);
		}
		mConfirmButton.setOnClickListener(mConfirmClickListener);
		
		// right button
		if(mBuilder.mCancelButton != null){
			mCancelButton.setText(mBuilder.mCancelButton);
			mCancelButton.setVisibility(View.VISIBLE);
		}else{
			mCancelButton.setVisibility(View.GONE);
		}
		mCancelButton.setOnClickListener(mCancelClickListener);
	}
	
	public void setOnConfirmListener(OnConfirmListener listener){
		mOnConfirmListener = listener;
	}

	private View.OnClickListener mCancelClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			cancel();
		}
		
	};
	
	private View.OnClickListener mConfirmClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(mOnConfirmListener != null){
				String content = mContentEditText.getEditableText().toString();
				mOnConfirmListener.onConfirm(PromptDialog.this, content);
			}
			dismiss();
		}
		
	};
	
	public static interface OnConfirmListener {
		
		public void onConfirm(PromptDialog dialog, String value);
		
	}

	public static class Builder {
		
		private Context mContext;
		
		private String mTitle;
		
		private String mText;
		
		private boolean mEditable;
		
		private String mContent;
		
		private String mPlaceholder;

		private String mConfirmButton;
		
		private String mCancelButton;
		
		public Builder(Context context){
			mContext = context;
		}

		public void setTitle(String title) {
			mTitle = title;
		}

		public void setText(String text) {
			mText = text;
		}

		public void setEditable(boolean editable) {
			mEditable = editable;
		}

		public void setContent(String content) {
			mContent = content;
		}

		public void setPlaceholder(String placeholder) {
			mPlaceholder = placeholder;
		}

		public void setConfirmButton(String confirmButton) {
			mConfirmButton = confirmButton;
		}

		public void setCancelButton(String cancelButton) {
			mCancelButton = cancelButton;
		}
		
		public PromptDialog create(){
			return new PromptDialog(this);
		}
	}
	
}
