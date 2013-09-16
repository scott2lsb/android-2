package com.lvrenyang.opencashbox;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import com.lvrenyang.pos.Pos;
import com.lvrenyang.pos.Update;
import com.lvrenyang.rw.PL2303Driver;
import com.lvrenyang.rw.TTYTermios;
import com.lvrenyang.rw.USBPort;
import com.lvrenyang.rw.USBSerialPort;
import com.lvrenyang.rw.TTYTermios.FlowControl;
import com.lvrenyang.rw.TTYTermios.Parity;
import com.lvrenyang.rw.TTYTermios.StopBits;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 连接 断开是usb层面的，每一次载字体，程序，小程序之前，都要更改波特率，载完之后要把波特率改回来。
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity implements OnItemSelectedListener,
		OnClickListener {

	public static String programfile = null;
	public static String fontfile = null;
	public static String data58binfile = null;
	public static String dataencry9x24binfile = null;
	public static String database9x24binfile = null;
	public static String readsaveto = null;
	public static String writesaveto = null;
	public static String dumpto = null;

	private BroadcastReceiver broadcastReceiver;
	private Spinner spinnerBaudrate, spinnerParity, spinnerBaudrateUpdate,
			spinnerParityUpdate;
	private Button buttonDisconnect, buttonConnect;
	private TextView textView1;
	private CheckBox checkBoxTest, checkBoxProgram, checkBoxFont,
			checkBoxSmallFont;
	private Button buttonTest, buttonProgram, buttonFont, buttonSmallFont,
			buttonUpdateBaudrate;
	private ProgressBar progressBar1;

	private Intent startProgramOptionActivityIntent = null;
	private Intent startFontOptionActivityIntent = null;

	/** onCreate的时候，会把这些值作为spinner的默认值 */
	private int startBaudrateIndex = 0;
	private int startParityIndex = 0;
	private int updateBaudrateIndex = 4;
	private int updateParityIndex = 0;
	private Parity paritys[] = { Parity.NONE, Parity.ODD, Parity.EVEN,
			Parity.MARK, Parity.SPACE };
	private int baudrates[] = { 9600, 19200, 38400, 57600, 115200, 230400 };

	private boolean debug_main = true;

	private static USBSerialPort serialPort;
	private static PL2303Driver mSerial;
	private static Pos mPos;
	private static Update mUpdate;
	private static Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		spinnerBaudrate = (Spinner) findViewById(R.id.spinnerBaudrate);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.baudrate, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerBaudrate.setAdapter(adapter);
		spinnerBaudrate.setOnItemSelectedListener(this);
		spinnerBaudrate.setSelection(startBaudrateIndex);

		spinnerParity = (Spinner) findViewById(R.id.spinnerParity);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		adapter = ArrayAdapter.createFromResource(this, R.array.parity,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerParity.setAdapter(adapter);
		spinnerParity.setOnItemSelectedListener(this);
		spinnerParity.setSelection(startParityIndex);

		spinnerBaudrateUpdate = (Spinner) findViewById(R.id.spinnerBaudrateUpdate);
		adapter = ArrayAdapter.createFromResource(this, R.array.baudrate,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerBaudrateUpdate.setAdapter(adapter);
		spinnerBaudrateUpdate.setOnItemSelectedListener(this);
		spinnerBaudrateUpdate.setSelection(updateBaudrateIndex);

		spinnerParityUpdate = (Spinner) findViewById(R.id.spinnerParityUpdate);
		adapter = ArrayAdapter.createFromResource(this, R.array.parity,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerParityUpdate.setAdapter(adapter);
		spinnerParityUpdate.setOnItemSelectedListener(this);
		spinnerParityUpdate.setSelection(updateParityIndex);

		textView1 = (TextView) findViewById(R.id.textView1);
		buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
		buttonDisconnect.setOnClickListener(this);
		buttonDisconnect.setEnabled(false);
		buttonConnect = (Button) findViewById(R.id.buttonConnect);
		buttonConnect.setOnClickListener(this);
		buttonConnect.setEnabled(true);

		checkBoxTest = (CheckBox) findViewById(R.id.checkBoxTest);
		checkBoxTest.setOnClickListener(this);
		checkBoxProgram = (CheckBox) findViewById(R.id.checkBoxProgram);
		checkBoxProgram.setOnClickListener(this);
		checkBoxFont = (CheckBox) findViewById(R.id.checkBoxFont);
		checkBoxFont.setOnClickListener(this);
		checkBoxSmallFont = (CheckBox) findViewById(R.id.checkBoxSmallFont);
		checkBoxSmallFont.setOnClickListener(this);
		buttonTest = (Button) findViewById(R.id.buttonTest);
		buttonTest.setOnClickListener(this);
		buttonProgram = (Button) findViewById(R.id.buttonProgram);
		buttonProgram.setOnClickListener(this);
		buttonFont = (Button) findViewById(R.id.buttonFont);
		buttonFont.setOnClickListener(this);
		buttonSmallFont = (Button) findViewById(R.id.buttonSmallFont);
		buttonSmallFont.setOnClickListener(this);
		buttonUpdateBaudrate = (Button) findViewById(R.id.buttonSetUpdateBaudrate);
		buttonUpdateBaudrate.setOnClickListener(this);

		textView1 = (TextView) findViewById(R.id.textView1);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

		startProgramOptionActivityIntent = new Intent(MainActivity.this,
				UpdateProgramOptions.class);
		startFontOptionActivityIntent = new Intent(MainActivity.this,
				UpdateFontOptions.class);

		data58binfile = Environment.getExternalStorageDirectory()
				+ "/printer/bin58";
		dataencry9x24binfile = Environment.getExternalStorageDirectory()
				+ "/printer/_0x194000_encry9x24";
		database9x24binfile = Environment.getExternalStorageDirectory()
				+ "/printer/_0x1b1000_base9x24";
		readsaveto = Environment.getExternalStorageDirectory()
				+ "/printer/readsaveto.txt";
		writesaveto = Environment.getExternalStorageDirectory()
				+ "/printer/writesaveto.txt";
		dumpto = Environment.getExternalStorageDirectory()
				+ "/printer/dumpto.txt";

		mContext = getApplicationContext();
		mSerial = new PL2303Driver();
		serialPort = new USBSerialPort(null, null);
		mPos = new Pos(serialPort, mSerial);
		mUpdate = new Update(mPos, mContext);
		mUpdate.start();
		mUpdate.waitForHandlerReady();
		mUpdate.setDumpFile(dumpto);
		mUpdate.setDebug(true);

		initBroadcast();
		handleIntent();
		debug_toast("onCreate");
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
					textView1.setText("");
					buttonDisconnect.callOnClick();

				} else if (Update.DEBUG.equals(action)) {
					textView1.append(intent.getStringExtra(Update.EXTRA_DEBUG));
				} else if (Update.UpdateHandler.UPDATE_INFO.equals(action)) {
					/* 最小值，开始，最大值，结束，正值，正常 */
					int index = intent.getIntExtra(
							Update.UpdateHandler.EXTRA_UPDATE_INFO, -1);
					if (index == Integer.MIN_VALUE) {
						progressBar1.setMax(intent.getIntExtra(
								Update.UpdateHandler.EXTRA_UPDATE_INFO_ARG, 0));
						progressBar1.setProgress(0);
						progressBar1.setVisibility(View.VISIBLE);
					} else if (index < 0 || index == Integer.MAX_VALUE) {
						progressBar1.setVisibility(View.INVISIBLE);
					} else
						progressBar1.setProgress(index);

				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		intentFilter.addAction(Update.DEBUG);
		intentFilter.addAction(Update.UpdateHandler.UPDATE_INFO);
		mContext.registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		mContext.unregisterReceiver(broadcastReceiver);
	}

	private void handleIntent() {

	}

	private void debug_toast(String msg) {
		if (debug_main)
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent();
		debug_toast("onNewIntent()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ((null == programfile) || ("".equals(programfile)))
			checkBoxProgram.setChecked(false);

		if ((null == fontfile) || ("".equals(fontfile)))
			checkBoxFont.setChecked(false);
		debug_toast("onResume");
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
		debug_toast("onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonConnect:
			/**
			 * writesaveto和readsaveto都删掉
			 */
			File file = new File(writesaveto);
			if (file.exists())
				file.delete();
			file = new File(readsaveto);
			if (file.exists())
				file.delete();
			file = new File(dumpto);
			if (file.exists())
				file.delete();

			connect();
			break;

		case R.id.buttonDisconnect:
			disconnect();
			break;

		case R.id.checkBoxProgram:
			if (checkBoxProgram.isChecked()) {
				programfile = null;
				if (startProgramOptionActivityIntent != null)
					startActivity(startProgramOptionActivityIntent);
			}
			break;

		case R.id.checkBoxFont:
			if (checkBoxFont.isChecked()) {
				fontfile = null;
				if (startFontOptionActivityIntent != null)
					startActivity(startFontOptionActivityIntent);
			}
			break;

		case R.id.buttonTest:
			open(baudrates[startBaudrateIndex], paritys[startParityIndex]);
			int ret;
			ret = mUpdate.fontTest(data58binfile);
			debug_toast("test font: " + ret);
			break;

		case R.id.buttonProgram:
			if (checkBoxProgram.isChecked()) {
				open(baudrates[startBaudrateIndex], paritys[startParityIndex]);
				mUpdate.updateProgram(programfile);
			}

			break;

		case R.id.buttonFont:
			if (checkBoxFont.isChecked()) {
				open(baudrates[startBaudrateIndex], paritys[startParityIndex]);
				mUpdate.updateFont(fontfile);
			}

			break;

		case R.id.buttonSmallFont:
			open(baudrates[startBaudrateIndex], paritys[startParityIndex]);
			mUpdate.updateSmallFont(database9x24binfile, dataencry9x24binfile);
			break;

		}

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.spinnerBaudrate:
			startBaudrateIndex = spinnerBaudrate.getSelectedItemPosition();
			debug_toast("startBaudrateIndex: " + startBaudrateIndex);
			break;
		case R.id.spinnerParity:
			startParityIndex = spinnerParity.getSelectedItemPosition();
			debug_toast("startParityIndex: " + startParityIndex);
			break;
		case R.id.spinnerBaudrateUpdate:
			updateBaudrateIndex = spinnerBaudrateUpdate
					.getSelectedItemPosition();
			debug_toast("updateBaudrateIndex: " + updateBaudrateIndex);
			break;
		case R.id.spinnerParityUpdate:
			updateParityIndex = spinnerParityUpdate.getSelectedItemPosition();
			debug_toast("updateParityIndex: " + updateParityIndex);
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	/**
	 * 打开和关闭按钮，就是用来连接USB的，打开就是probe，关闭就是disconnect
	 * probe会填充port字段，open会根据termios字段来打开串口
	 * 
	 */

	private void connect() {
		final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		if (deviceList.size() > 0) {
			// 初始化选择对话框布局，并添加按钮和事件
			LinearLayout llSelectDevice = new LinearLayout(this);
			llSelectDevice.setOrientation(LinearLayout.VERTICAL);
			llSelectDevice.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("选择设备").setView(llSelectDevice);
			final AlertDialog dialog = builder.create();

			while (deviceIterator.hasNext()) { // 这里是if不是while，说明我只想支持一种device
				final UsbDevice device = deviceIterator.next();
				Button btDevice = new Button(llSelectDevice.getContext());
				btDevice.setLayoutParams(new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				btDevice.setGravity(Gravity.LEFT);
				btDevice.setText("ID: " + device.getDeviceId());
				btDevice.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						PendingIntent mPermissionIntent = PendingIntent
								.getBroadcast(
										MainActivity.this,
										0,
										new Intent(
												MainActivity.this
														.getApplicationInfo().packageName),
										0);
						serialPort.port = new USBPort(mUsbManager,
								MainActivity.this, device, mPermissionIntent);
						int ret = mSerial.pl2303_probe(serialPort);
						if (ret == 0) {
							textView1.setText(System.currentTimeMillis() + ": "
									+ "连接成功 " + mSerial.type + "\n");
							buttonConnect.setEnabled(false);
							buttonDisconnect.setEnabled(true);
						} else {
							textView1.setText(System.currentTimeMillis() + ": "
									+ "连接失败(" + ret + ")\n");
							buttonConnect.setEnabled(true);
							buttonDisconnect.setEnabled(false);
						}
					}
				});
				llSelectDevice.addView(btDevice);
			}
			if (llSelectDevice.getChildCount() == 1)
				llSelectDevice.getChildAt(0).callOnClick();
			else
				dialog.show();
		}
	}

	private void open(final int baudrate, final Parity parity) {

		serialPort.termios = new TTYTermios(baudrate, FlowControl.NONE, parity,
				StopBits.ONE, 8);
		int ret = mSerial.pl2303_open(serialPort);
		if (ret == 0)
			textView1.setText(System.currentTimeMillis() + ": " + "打开成功"
					+ serialPort.termios.baudrate + " "
					+ serialPort.termios.parity + "\n");
		else
			textView1.setText(System.currentTimeMillis() + ": " + "打开失败(" + ret
					+ ")\n");

	}

	private void close() {
		mSerial.pl2303_close(serialPort);
	}

	private void disconnect() {
		close();
		mSerial.pl2303_disconnect(serialPort);
		textView1.setText("");
		buttonConnect.setEnabled(true);
		buttonDisconnect.setEnabled(false);

	}
}
