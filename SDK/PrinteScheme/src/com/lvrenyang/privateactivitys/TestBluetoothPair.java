package com.lvrenyang.privateactivitys;

import java.io.IOException;
import java.util.UUID;

import btmanager.ClassReflectDebug;
import btmanager.ClsUtils;

import com.lvrenyang.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * sourceInsight ËÑË÷¹Ø¼ü´Ê mConnectAfterPairing
 * 
 * @author lvrenyang
 * 
 */
public class TestBluetoothPair extends Activity implements OnClickListener,
		OnLongClickListener {

	private BroadcastReceiver broadcastReceiver;
	private Button button1, button2, button3, button4;
	private TextView textView1;

	private static String mac = "00:02:0A:01:60:6E";
	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothAdapter adapter;
	private BluetoothDevice device;
	private BluetoothSocket socket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_private_buttontextviewedittext);
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
		button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(this);
		button3 = (Button) findViewById(R.id.button3);
		button3.setOnClickListener(this);
		button4 = (Button) findViewById(R.id.button4);
		button4.setOnClickListener(this);

		textView1 = (TextView) findViewById(R.id.textView1);

		adapter = BluetoothAdapter.getDefaultAdapter();
		device = adapter.getRemoteDevice(mac);
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
		case R.id.button1: {
			textView1.setText(ClassReflectDebug
					.printClassMethod(BluetoothDevice.class));
			break;
		}

		case R.id.button2: {

			int bondState = device.getBondState();
			if (bondState == BluetoothDevice.BOND_BONDED) {
				try {
					socket = device.createRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
				}
				try {
					socket.connect();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (bondState == BluetoothDevice.BOND_NONE) {
				pair();
			}
			break;
		}

		case R.id.button3: {
			break;
		}

		case R.id.button4: {
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

			}

		};
		IntentFilter intentFilter = new IntentFilter();
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private boolean pair() {
		if (adapter.isDiscovering()) {
			adapter.cancelDiscovery();
		}

		try {
			if (ClsUtils.createBond(BluetoothDevice.class, device)) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
