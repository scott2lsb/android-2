package com.lvrenyang.settingactivitys;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import btmanager.ConnectThread;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.R;

public class SearchAndConnect extends Activity implements OnClickListener,
		OnLongClickListener, OnItemSelectedListener {

	private TextView tvTopic;
	private Button btBack;

	private BroadcastReceiver broadcastReceiver;
	private LinearLayout linearlayoutdevices;
	private Button buttonSearch;
	private ProgressBar progressBar1;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_searchandconnect);
		buttonSearch = (Button) findViewById(R.id.buttonSearch);
		buttonSearch.setOnClickListener(this);
		linearlayoutdevices = (LinearLayout) findViewById(R.id.linearlayoutdevices);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setting_searchandconnect));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);

		initBroadcast();
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
		uninitBroadcast();
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
		case R.id.buttonSearch: {
			Pos.POS_Close();
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			linearlayoutdevices.removeAllViews();
			BluetoothAdapter.getDefaultAdapter().startDiscovery();
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

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					if (device == null)
						return;
					final String address = device.getAddress();
					String name = device.getName();
					if (name == null)
						name = "蓝牙设备";
					else if (name.equals(address))
						name = "蓝牙设备";
					Button button = new Button(context);
					button.setText(name + ": " + address);
					button.setGravity(android.view.Gravity.CENTER_VERTICAL
							| Gravity.LEFT);
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							// 只有没有连接且没有在用，这个才能改变状态
							Pos.POS_Open(address);
						}
					});
					button.getBackground().setAlpha(100);
					linearlayoutdevices.addView(button);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {
					progressBar1.setVisibility(View.VISIBLE);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {
					progressBar1.setVisibility(View.GONE);
				} else if (action.equals(ConnectThread.ACTION_CONNECTED)) {
					if (dialog != null) {
						dialog.cancel();
					}
					Toast.makeText(getApplicationContext(),
							getString(R.string.connected), Toast.LENGTH_SHORT)
							.show();
					printTest();
				} else if (action.equals(ConnectThread.ACTION_DISCONNECTED)) {
					if (dialog != null) {
						dialog.cancel();
					}
					Toast.makeText(getApplicationContext(),
							getString(R.string.disconnected),
							Toast.LENGTH_SHORT).show();
				} else if (action.equals(ConnectThread.ACTION_STARTCONNECTING)) {
					if (device == null)
						return;
					dialog = LayoutUtils.showDialog(SearchAndConnect.this,
							getString(R.string.connecting) + device.getName());
				}

			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(ConnectThread.ACTION_DISCONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_CONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_STARTCONNECTING);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	private void printTest() {
		Time mTime = new Time();
		mTime.setToNow();
		Pos.POS_Reset();
		Pos.POS_S_TextOut("测试页", 156, 0, 0, 0, 0x80);
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("Width = 2; Height = 2;", 0, 1, 1, 0, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("BlackWhiteReverse", 0, 0, 0, 0, 0x400);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("small font", 0, 0, 0, 1, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut("UPC-A", 0, 0, 0, 0, 0);
		Pos.POS_FeedLine();
		Pos.POS_S_SetBarcode("01234567890", 0, 0x41, 3, 100, 0x00, 0x02);
		Pos.POS_FeedLine();
		Pos.POS_S_TextOut(mTime.format("%Y-%m-%d %H:%M:%S"), 0, 0, 0, 0, 0x00);
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
		Pos.POS_FeedLine();
	}

}
