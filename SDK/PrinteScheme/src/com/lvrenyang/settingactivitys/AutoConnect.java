package com.lvrenyang.settingactivitys;

// 首选项，在哪里设置，就保存在哪里
// onCreate 注册 onResume 视图 onStop 保存 onDestroy释放

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AutoConnect extends Activity implements OnItemClickListener,
		OnClickListener {
	
	private TextView tvTopic;
	private Button btBack;
	
	private static RadioButton radioActiveConnect, radioWaitConnect,
			radioNotSet;
	private static TextView textView;
	private static ListView listView;
	public static final String ICON = "ICON";
	public static final String PRINTERNAME = "PRINTERNAME";
	public static final String PRINTERMAC = "PRINTERMAC";
	private static List<Map<String, Object>> boundedPrinters;

	public static final String PREFERENCES_autoConnectName = "PREFERENCES_autoConnectName";
	public static final String PREFERENCES_autoConnectMac = "PREFERENCES_autoConnectMac";
	public static final String PREFERENCES_autoConnectMode = "PREFERENCES_autoConnectMode";
	public static final String VALUE_autoConnectModeActive = "PREFERENCESVALUE_autoConnectModeActive";
	public static final String VALUE_autoConnectModeWait = "PREFERENCESVALUE_autoConnectModeWait";
	public static final String VALUE_autoConnectModeNotSet = "PREFERENCESVALUE_autoConnectModeNotSet";

	public static String autoConnectMac = "";
	public static String autoConnectName = "";
	public static String autoConnectMode = VALUE_autoConnectModeNotSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_autoconnect);
		boundedPrinters = getBoundedPrinters();
		listView = (ListView) findViewById(R.id.listViewSettingAutoConnect);
		listView.setAdapter(new SimpleAdapter(this, boundedPrinters,
				R.layout.list_item_printernameandmac, new String[] { ICON,
						PRINTERNAME, PRINTERMAC }, new int[] {
						R.id.btListItemPrinterIcon, R.id.tvListItemPrinterName,
						R.id.tvListItemPrinterMac }));
		listView.setOnItemClickListener(this);

		radioActiveConnect = (RadioButton) findViewById(R.id.radioSettingActiveConnect);
		radioWaitConnect = (RadioButton) findViewById(R.id.radioSettingWaitConnect);
		radioNotSet = (RadioButton) findViewById(R.id.radioSettingNotSet);
		radioActiveConnect.setOnClickListener(this);
		radioWaitConnect.setOnClickListener(this);
		radioNotSet.setOnClickListener(this);
		textView = (TextView) findViewById(R.id.textViewSettingAutoConnectDevice);
		
		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.autoconnect));
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

		// set the autoConnectMac and autoConnectName
		textView.setText(autoConnectName);

		if (autoConnectMode.equals(VALUE_autoConnectModeActive))
			radioActiveConnect.setChecked(true);
		else if (autoConnectMode.equals(VALUE_autoConnectModeWait))
			radioWaitConnect.setChecked(true);
		else
			radioNotSet.setChecked(true);

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
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		autoConnectMac = (String) boundedPrinters.get(position).get(PRINTERMAC);
		autoConnectName = (String) boundedPrinters.get(position).get(
				PRINTERNAME);
		textView.setText(autoConnectName);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.radioSettingActiveConnect: {
			if (autoConnectMac.length() == 0) {
				radioNotSet.setChecked(true);
			} else {
				autoConnectMode = VALUE_autoConnectModeActive;
				if (Pos.POS_isConnecting())
					Pos.POS_Close();
				finish();
			}
			break;
		}

		case R.id.radioSettingWaitConnect: {
			if (autoConnectMac.length() == 0) {
				radioNotSet.setChecked(true);
			} else {
				autoConnectMode = VALUE_autoConnectModeWait;
				if (Pos.POS_isConnecting())
					Pos.POS_Close();
				finish();
			}
			break;
		}

		case R.id.radioSettingNotSet: {
			autoConnectMac = "";
			autoConnectName = "";
			textView.setText("");
			autoConnectMode = VALUE_autoConnectModeNotSet;
			if (Pos.POS_isConnecting())
				Pos.POS_Close();
			finish();
			break;
		}
		
		case R.id.btBack: {
			finish();
			break;
		}
		
		}
	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			SharedPreferences.Editor editor = this.getSharedPreferences(
					MainActivity.PREFERENCES_FILE, 0).edit();
			editor.putString(PREFERENCES_autoConnectMac, autoConnectMac);
			editor.putString(PREFERENCES_autoConnectName, autoConnectName);
			editor.putString(PREFERENCES_autoConnectMode, autoConnectMode);
			editor.commit();
		}
	}

	public static String getAutoConnectMode() {
		return autoConnectMode;
	}

	public static String getAutoConnectMac() {
		return autoConnectMac;
	}
}
