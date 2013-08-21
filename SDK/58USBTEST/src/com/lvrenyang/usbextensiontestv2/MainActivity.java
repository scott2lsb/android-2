package com.lvrenyang.usbextensiontestv2;

import java.io.IOException;
import java.io.InputStream;

import com.lvrenyang.kcusb.PL2303Driver;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {

	private Button button1, button2, button3, button4, button5, button6;
	private TextView textView1;
	private EditText editText1;
	private Spinner spinner2, spinner3, spinner4, spinner5, spinner6;

	private Button button7;
	private Button button8, button9;
	private PL2303Driver mSerial1;
	private BroadcastReceiver broadcastReceiver;
	private static final String ACTION_USB_PERMISSION = "com.lvrenyang.usbextensiontestv2.PL2303Driver.USB_PERMISSION";

	private int testfile = 1;
	private byte[] data58bin = null;
	private static boolean test = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);
		button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(this);
		button3 = (Button) findViewById(R.id.button3);
		button3.setOnClickListener(this);
		button4 = (Button) findViewById(R.id.button4);
		button4.setOnClickListener(this);
		button5 = (Button) findViewById(R.id.button5);
		button5.setOnClickListener(this);
		button6 = (Button) findViewById(R.id.button6);
		button6.setOnClickListener(this);
		button7 = (Button) findViewById(R.id.button7);
		button7.setOnClickListener(this);
		button8 = (Button) findViewById(R.id.button8);
		button8.setOnClickListener(this);
		button9 = (Button) findViewById(R.id.button9);
		button9.setOnClickListener(this);

		textView1 = (TextView) findViewById(R.id.textView1);
		editText1 = (EditText) findViewById(R.id.editText1);
		editText1.setText("0");

		spinner2 = (Spinner) findViewById(R.id.spinner2);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.baudrate, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner2.setAdapter(adapter2);
		spinner2.setSelection(0);
		spinner2.setOnItemSelectedListener(this);

		spinner3 = (Spinner) findViewById(R.id.spinner3);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
				this, R.array.databits, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner3.setAdapter(adapter3);
		spinner3.setSelection(3);
		spinner3.setOnItemSelectedListener(this);

		spinner4 = (Spinner) findViewById(R.id.spinner4);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
				this, R.array.stopbits, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner4.setAdapter(adapter4);
		spinner4.setSelection(0);
		spinner4.setOnItemSelectedListener(this);

		spinner5 = (Spinner) findViewById(R.id.spinner5);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(
				this, R.array.parity, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner5.setAdapter(adapter5);
		spinner5.setSelection(0);
		spinner5.setOnItemSelectedListener(this);

		spinner6 = (Spinner) findViewById(R.id.spinner6);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter6 = ArrayAdapter
				.createFromResource(this, R.array.flowcontrol,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner6.setAdapter(adapter6);
		spinner6.setSelection(1);
		spinner6.setOnItemSelectedListener(this);

		initBroadcast();
		mSerial1 = new PL2303Driver(
				(UsbManager) getSystemService(Context.USB_SERVICE), this,
				ACTION_USB_PERMISSION);
		/*
		 * mSerial2 = new PL2303Driver( (UsbManager)
		 * getSystemService(Context.USB_SERVICE), this, ACTION_USB_PERMISSION);
		 */
		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.button1: {
			boolean ret = mSerial1.enumerate();
			textView1.setText("\nenumerate: " + ret);
			break;
		}
		case R.id.button2: {
			boolean ret = mSerial1.connectByDefualtValue();
			textView1.append("\nconnect: " + ret);
			break;
		}
		case R.id.button3: {
			printTest();
			/*
			 * byte[] data = new byte[] { 0x30, 0x0a }; int ret =
			 * mSerial1.write(data, data.length); textView1.append("\nwrite: " +
			 * ret);
			 */
			/*
			 * ret = mSerial2.write(data, data.length);
			 * textView1.append("\nwrite: " + ret);
			 */break;
		}
		case R.id.button4: {
			byte[] data = new byte[] { 0x10, 0x04, 0x01 };
			byte[] rec = new byte[1];
			int ret = mSerial1.write(data, data.length);
			textView1.append("\nwrite: " + ret);
			ret = mSerial1.read(rec, 1);
			textView1.append("\nread: " + ret + "\trec[0] = " + rec[0]);
			/*
			 * ret = mSerial2.write(data, data.length);
			 * textView1.append("\nwrite: " + ret); ret = mSerial2.read(rec, 1);
			 * textView1.append("\nread: " + ret + "\trec[0] = " + rec[0]);
			 */
			break;
		}
		case R.id.button5: {
			mSerial1.disconnect();
			/* mSerial2.disconnect(); */
			break;
		}
		case R.id.button6: {

			break;
		}
		case R.id.button7: {
			textView1.setText("");
			break;
		}
		/*
		 * case R.id.button8: { boolean ret = mSerial2.enumerate();
		 * textView1.append("\nenumerate: " + ret); break; }
		 * 
		 * case R.id.button9: { boolean ret = mSerial2.connectByDefualtValue();
		 * textView1.append("\nconnect: " + ret); break; }
		 */
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
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
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	private void handleIntent(Intent intent) {
		boolean ret;
		ret = mSerial1.enumerate();
		textView1.setText(ret ? "枚举成功\n" : "枚举失败\n");
		if (!ret)
			return;
		ret = mSerial1.connectByDefualtValue();
		textView1.append(ret ? "连接成功\n" : "连接失败\n");
		if (!ret)
			return;
		if (test)
			printTest();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (parent.getId()) {
		case R.id.spinner2: {
			switch (position) {
			case 0: {
				break;
			}
			case 1: {
				break;
			}
			case 2: {
				break;
			}
			case 3: {
				break;
			}
			case 4: {

				break;
			}
			}
			break;
		}

		case R.id.spinner3: {
			switch (position) {
			case 0: {

				break;
			}
			case 1: {

				break;
			}
			case 2: {

				break;
			}
			case 3: {

				break;
			}
			}
			break;
		}

		case R.id.spinner4: {
			switch (position) {
			case 0: {

				break;
			}
			case 1: {

				break;
			}
			case 2: {

				break;
			}
			}
			break;
		}

		case R.id.spinner5: {
			switch (position) {
			case 0: {

				break;
			}
			case 1: {

				break;
			}
			case 2: {

				break;
			}
			}
			break;
		}

		case R.id.spinner6: {
			switch (position) {
			case 0: {

				break;
			}
			case 1: {

				break;
			}
			}
			break;
		}

		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
					boolean ret;
					ret = mSerial1.enumerate();
					textView1.setText(ret ? "枚举成功\n" : "枚举失败\n");
					if (!ret)
						return;
					ret = mSerial1.connectByDefualtValue();
					textView1.append(ret ? "连接成功\n" : "连接失败\n");
					if (!ret)
						return;
					if (test)
						printTest();

				} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
					textView1.setText("");
				} else if (PL2303Driver.DEBUG.equals(action)) {
					textView1.append(intent
							.getStringExtra(PL2303Driver.EXTRA_DEBUG));
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		intentFilter.addAction(PL2303Driver.DEBUG);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private void printTest() {
		if (testfile == 0) {
			Time mTime = new Time();
			mTime.setToNow();
			mSerial1.POS_Reset();
			mSerial1.POS_S_TextOut("测试页", 156, 0, 0, 0, 0x80);
			mSerial1.POS_FeedLine();
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_TextOut("Width = 2; Height = 2;", 0, 1, 1, 0, 0x00);
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_TextOut("BlackWhiteReverse", 0, 0, 0, 0, 0x400);
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_TextOut("small font", 0, 0, 0, 1, 0x00);
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_TextOut("UPC-A", 0, 0, 0, 0, 0);
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_SetBarcode("01234567890", 0, 0x41, 3, 100, 0x00,
					0x02);
			mSerial1.POS_FeedLine();
			mSerial1.POS_S_TextOut(mTime.format("%Y-%m-%d %H:%M:%S"), 0, 0, 0,
					0, 0x00);
			mSerial1.POS_FeedLine();
			mSerial1.POS_FeedLine();
		} else if (testfile == 1) {
			if (null == data58bin) {
				InputStream is = getResources().openRawResource(R.raw.bin58);
				try {
					data58bin = new byte[is.available()];
					is.read(data58bin);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (null == data58bin) {
				Toast.makeText(this, "读取失败", Toast.LENGTH_SHORT).show();
			} else {
				mSerial1.POS_Write(data58bin);
			}
		}
		byte[] data = new byte[] { 0x0a };
		int ret = mSerial1.write(data, data.length);
		textView1.append(ret > 0 ? "发送成功\n" : "发送失败\n");
	}
}
