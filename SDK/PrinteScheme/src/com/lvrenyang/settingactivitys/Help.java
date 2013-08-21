package com.lvrenyang.settingactivitys;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import btmanager.LayoutUtils;

import com.lvrenyang.R;

public class Help extends Activity implements OnClickListener,
		OnLongClickListener {

	private TextView tvTopic;
	private Button btBack;

	private LinearLayout linearLayoutHelp;

	private String helpItem[];
	private String helpContent[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_help);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setting_set));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);

		linearLayoutHelp = (LinearLayout) findViewById(R.id.linearLayoutHelp);

		initHelpItemAndContent();

		for (int i = 0; i < helpItem.length; i++) {
			final Button button = new Button(this);
			button.setText(i + ": " + helpItem[i]);
			button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			button.setGravity(Gravity.LEFT);

			final TextView textView = new TextView(this);
			textView.setText("    " + helpContent[i]);
			textView.setVisibility(View.GONE);

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (textView.getVisibility() == View.GONE)
						textView.setVisibility(View.VISIBLE);
					else
						textView.setVisibility(View.GONE);
				}

			});
			linearLayoutHelp.addView(button);
			linearLayoutHelp.addView(textView);

		}

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
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	private void initHelpItemAndContent() {
		helpItem = new String[] { getString(R.string.text),
				getString(R.string.photos), getString(R.string.setandshow),
				getString(R.string.barcode), getString(R.string.qrcode),
				getString(R.string.web_guide), getString(R.string.readinfo),
				getString(R.string.setting_connectwithbindedprinter),
				getString(R.string.setting_set),
				getString(R.string.setting_updateprogram),
				getString(R.string.setting_autoconnection),
				getString(R.string.setkey) };
		helpContent = new String[] { getString(R.string.helpcontenttext),
				getString(R.string.helpcontentpicture),
				getString(R.string.helpcontentsetandshow),
				getString(R.string.helpcontentbarcode),
				getString(R.string.qrcode),
				getString(R.string.helpcontentguide),
				getString(R.string.helpcontentreadinfo),
				getString(R.string.helpcontentconnectwithbonded),
				getString(R.string.helpcontentsetting),
				getString(R.string.helpcontentupdateprogram),
				getString(R.string.helpcontentautoconnect),
				getString(R.string.helpcontentsetkey) };
	}
}
