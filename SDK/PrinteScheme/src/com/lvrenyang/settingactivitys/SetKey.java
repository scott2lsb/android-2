package com.lvrenyang.settingactivitys;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import btmanager.LayoutUtils;
import btmanager.Pos;
import btmanager.ReadThread;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

public class SetKey extends Activity implements OnClickListener {

	public static final String PREFERENCES_deskey = "PREFERENCES_deskey";

	private BroadcastReceiver broadcastReceiver;

	private TextView tvTopic;
	private Button btBack;

	private EditText editTextInputKey;
	private Button buttonSetKey;

	public static String deskey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_setkey);

		editTextInputKey = (EditText) findViewById(R.id.editTextInputKey);
		buttonSetKey = (Button) findViewById(R.id.buttonSetKey);
		buttonSetKey.setOnClickListener(this);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setkey));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);

		initBroadcast();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSetKeyUI();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// BtService.unregistReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
		savePreferences();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uninitBroadcast();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonSetKey: {
			String tmpkey = editTextInputKey.getText().toString();
			if (tmpkey != null) {
				byte[] tmpkeybytes = tmpkey.getBytes();
				if (tmpkeybytes.length != 8) {
					Toast.makeText(this, getString(R.string.deskeyformat),
							Toast.LENGTH_SHORT).show();
				} else {
					deskey = tmpkey;
					Pos.POS_SetKey(tmpkeybytes);
					ReadThread.setKey(tmpkeybytes);
					Toast.makeText(this, getString(R.string.setkeyend),
							Toast.LENGTH_SHORT).show();
				}
			}

			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (ReadThread.ACTION_READTHREADRECEIVES.equals(action)) {

				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVES);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private void updateSetKeyUI() {
		editTextInputKey.setText(deskey);
	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putString(PREFERENCES_deskey, deskey);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}

}
