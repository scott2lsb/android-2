package com.lvrenyang.printescheme;

// 从设置项activity退出时，要将相应的preferences写入
// 别的activity永远都从原始设置activity得到preferences
// mainAcitivity首次启动时，更新所有的preferences
// mainActivity每次启动时，也要更新自己的preferences

import btmanager.LayoutUtils;

import com.lvrenyang.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class OptionsActivity extends Activity implements OnClickListener {

	public static final String PREFERENCES_debug = "PREFERENCES_debug";
	private static boolean debug = false;

	public static final String FINISH = "FINISH";

	private static ToggleButton toggleButtonDebug;
	
	private TextView tvTopic;
	private Button btBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(OptionsActivity.this, R.layout.actionbar,
				R.layout.activity_options);

		toggleButtonDebug = (ToggleButton) findViewById(R.id.toggleButtonDebug);
		toggleButtonDebug.setOnClickListener(this);
		findViewById(R.id.buttonStopService).setOnClickListener(this);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.actionbar_options));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		toggleButtonDebug.setChecked(debug);
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
		savePreferences();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.toggleButtonDebug: {
			setDebug(toggleButtonDebug.isChecked());
			break;
		}

		case R.id.buttonStopService: {
			stopService(new Intent(this, BtService.class));
			getApplicationContext().sendBroadcast(new Intent(FINISH));
			finish();
			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	public static boolean getDebug() {
		return debug;
	}

	public static void setDebug(boolean isdebug) {
		debug = isdebug;
	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			SharedPreferences.Editor editor = this.getSharedPreferences(
					MainActivity.PREFERENCES_FILE, 0).edit();
			editor.putBoolean(PREFERENCES_debug, debug);
			editor.commit();
		}
	}
}
