package com.lvrenyang.textandpictureactivitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.R;

public class Text extends Activity implements OnClickListener,
		OnLongClickListener {

	private TextView tvTopic;
	private Button btBack;

	private EditText editTextInput;
	private Button buttonPrint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(Text.this, R.layout.actionbar,
				R.layout.activity_textandpicture_text);

		editTextInput = (EditText) findViewById(R.id.editTextInput);
		editTextInput.setOnClickListener(this);

		buttonPrint = (Button) findViewById(R.id.buttonPrint);
		buttonPrint.setOnClickListener(this);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.text));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.editTextInput: {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			break;
		}

		case R.id.buttonPrint: {

			String tmp = editTextInput.getText().toString();
			if ((tmp != null) && (!"".equals(tmp))) {
				Pos.POS_S_TextOut(tmp, 0, SetAndShow.nScaleTimesWidth,
						SetAndShow.nScaleTimesHeight, SetAndShow.nFontSize,
						SetAndShow.nFontStyle);
				Pos.POS_FeedLine();
			}

			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

}
