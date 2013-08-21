package com.lvrenyang.settingactivitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import btmanager.ConnectThread;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.BtService;
import com.lvrenyang.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Connect extends Activity implements OnItemClickListener,
		OnClickListener {

	private TextView tvTopic;
	private Button btBack;

	private static ListView listView;
	public static final String ICON = "ICON";
	public static final String PRINTERNAME = "PRINTERNAME";
	public static final String PRINTERMAC = "PRINTERMAC";
	private static List<Map<String, Object>> boundedPrinters;
	private static BroadcastReceiver receiver;
	private static ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_connect);
		boundedPrinters = getBoundedPrinters();
		listView = (ListView) findViewById(R.id.listViewSettingConnect);
		listView.setAdapter(new SimpleAdapter(this, boundedPrinters,
				R.layout.list_item_printernameandmac, new String[] { ICON,
						PRINTERNAME, PRINTERMAC }, new int[] {
						R.id.btListItemPrinterIcon, R.id.tvListItemPrinterName,
						R.id.tvListItemPrinterMac }));
		listView.setOnItemClickListener(this);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setting_connectwithbindedprinter));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);

		initBroadcast();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		String mac = (String) boundedPrinters.get(position).get(PRINTERMAC);

		Pos.POS_Open(mac);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
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
		BtService.stopAutoConnect = true;
		if (Pos.POS_isServerConnecting())
			Pos.POS_Close();
	}

	@Override
	protected void onPause() {
		super.onPause();
		BtService.stopAutoConnect = false;
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

	private List<Map<String, Object>> getBoundedPrinters() {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			return list;
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				Map<String, Object> map = new HashMap<String, Object>();
				if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING)
					map.put(ICON, R.drawable.printericon);
				else if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED)
					map.put(ICON, android.R.drawable.stat_sys_data_bluetooth);
				else
					continue;

				map.put(PRINTERNAME, device.getName());
				map.put(PRINTERMAC, device.getAddress());
				list.add(map);
			}
		}
		return list;
	}

	private void uninitBroadcast() {
		if (receiver != null)
			unregisterReceiver(receiver);
	}

	private void initBroadcast() {
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (action.equals(ConnectThread.ACTION_CONNECTED)) {
					if (dialog != null) {
						dialog.cancel();
						finish();
					}
				} else if (action.equals(ConnectThread.ACTION_DISCONNECTED)) {
					if (dialog != null) {
						dialog.cancel();
					}
				} else if (action.equals(ConnectThread.ACTION_STARTCONNECTING)) {
					if (device == null)
						return;
					dialog = LayoutUtils.showDialog(Connect.this,
							getString(R.string.connecting) + device.getName());
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectThread.ACTION_DISCONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_CONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_STARTCONNECTING);
		registerReceiver(broadcastReceiver, intentFilter);
		receiver = broadcastReceiver;
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
}
