package com.lvrenyang.usbtool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.lvrenyang.kcusb.Cmd;
import com.lvrenyang.kcusb.DataUtils;
import com.lvrenyang.kcusb.PL2303Driver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String ACTION_USB_PERMISSION = "com.lvrenyang.usbtool.PL2303Driver.USB_PERMISSION";

	private TextView textView1;
	private CheckBox checkBoxTest, checkBoxProgram, checkBoxFont;
	private Button buttonTest, buttonProgram, buttonFont;
	private static ProgressBar progressBar1;
	private static PL2303Driver mSerial;
	private BroadcastReceiver broadcastReceiver;
	private static byte[] data58bin = null;
	private static byte[] database9x24bin = null;
	private static byte[] dataencry9x24bin = null;
	public static String programfile = null;
	public static String fontfile = null;
	private boolean debug_main = false;

	private static Intent startProgramOptionActivityIntent = null;
	private static Intent startFontOptionActivityIntent = null;
	private static MediaPlayer mpok;
	private static MediaPlayer mperror;
	private static Context mContext;

	private static int timeout = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		checkBoxTest = (CheckBox) findViewById(R.id.checkBoxTest);
		checkBoxTest.setOnClickListener(this);
		checkBoxProgram = (CheckBox) findViewById(R.id.checkBoxProgram);
		checkBoxProgram.setOnClickListener(this);
		checkBoxFont = (CheckBox) findViewById(R.id.checkBoxFont);
		// checkBoxFont.setOnClickListener(this);
		buttonTest = (Button) findViewById(R.id.buttonTest);
		buttonTest.setOnClickListener(this);
		buttonProgram = (Button) findViewById(R.id.buttonProgram);
		buttonProgram.setOnClickListener(this);
		buttonFont = (Button) findViewById(R.id.buttonFont);
		buttonFont.setOnClickListener(this);

		textView1 = (TextView) findViewById(R.id.textView1);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

		startProgramOptionActivityIntent = new Intent(MainActivity.this,
				UpdateProgramOptions.class);
		startFontOptionActivityIntent = new Intent(MainActivity.this,
				UpdateFontOptions.class);
		mpok = new MediaPlayer();
		mperror = new MediaPlayer();
		try {
			// R.raw.error 是ogg格式的音频 放在res/raw/下
			AssetFileDescriptor afd = getApplicationContext().getResources()
					.openRawResourceFd(R.raw.ok);
			mpok.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			mpok.setAudioStreamType(AudioManager.STREAM_RING);
			afd.close();
			mpok.prepare();

			afd = getApplicationContext().getResources().openRawResourceFd(
					R.raw.error);
			mperror.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			mperror.setAudioStreamType(AudioManager.STREAM_RING);
			afd.close();
			mperror.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mContext = this;
		initBroadcast();
		new UpdateProgram().start();
		mSerial = new PL2303Driver(
				(UsbManager) getSystemService(Context.USB_SERVICE), this,
				ACTION_USB_PERMISSION);
		mSerial.startRead();
		handleIntent();
		debug_toast("onCreate");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent();
		debug_toast("onNewIntent");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ((null == programfile) || ("".equals(programfile)))
			checkBoxProgram.setChecked(false);

		// if ((null == fontfile) || ("".equals(fontfile)))
		// checkBoxFont.setChecked(false);
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

		disconnected = true;
		Message msg = UpdateProgram.updateHandler.obtainMessage();
		msg.what = UpdateProgram.WHAT_QUIT;
		UpdateProgram.updateHandler.sendMessageAtFrontOfQueue(msg);

		mSerial.endRead();
		uninitBroadcast();
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
				if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
					debug_toast("ACTION_USB_DEVICE_ATTACHED");
				} else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED
						.equals(action)) {
					debug_toast("ACTION_USB_ACCESSORY_ATTACHED");
				} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

					textView1.setText("");
					disconnected = true;
					mSerial.disconnect();
				} else if (UpdateProgram.DEBUG.equals(action)) {
					textView1.append(intent
							.getStringExtra(UpdateProgram.EXTRA_DEBUG));
				} else if (PL2303Driver.DEBUG.equals(action)) {
					textView1.append(intent
							.getStringExtra(PL2303Driver.EXTRA_DEBUG));
				} else if (UpdateProgram.UPDATE_INFO.equals(action)) {
					/* 最小值，开始，最大值，结束，正值，正常 */
					int index = intent.getIntExtra(
							UpdateProgram.EXTRA_UPDATE_INFO, -1);
					if (index == Integer.MIN_VALUE) {
						progressBar1.setMax(times);
						progressBar1.setProgress(0);
						progressBar1.setVisibility(View.VISIBLE);
					} else if (index < 0 || index == Integer.MAX_VALUE)
						progressBar1.setVisibility(View.GONE);
					else
						progressBar1.setProgress(index);

				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		intentFilter.addAction(UpdateProgram.DEBUG);
		intentFilter.addAction(PL2303Driver.DEBUG);
		intentFilter.addAction(UpdateProgram.UPDATE_INFO);
		mContext.registerReceiver(broadcastReceiver, intentFilter);
		debug_toast("initBroadcast");
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			mContext.unregisterReceiver(broadcastReceiver);
	}

	private void handleIntent() {

		boolean ret;
		ret = mSerial.enumerate();
		textView1.setText(ret ? "枚举成功\n" : "枚举失败\n");
		if (!ret) {
			disconnected = true;
			return;
		}
		ret = mSerial.connectByDefualtValue();
		textView1.append(ret ? "连接成功\n" : "连接失败\n");
		if (!ret) {
			disconnected = true;
			return;
		}
		disconnected = false;
		if (checkBoxFont.isChecked()) {
			// updatefont();
			updatesmallfont();
		}

		if (checkBoxTest.isChecked())
			printTest();

		if (checkBoxProgram.isChecked())
			updateprogram();

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.checkBoxProgram: {
			if (checkBoxProgram.isChecked()) {
				programfile = null;
				if (startProgramOptionActivityIntent != null)
					startActivity(startProgramOptionActivityIntent);
			}
		}
			break;

		case R.id.checkBoxFont: {
			if (checkBoxFont.isChecked()) {
				fontfile = null;
				if (startFontOptionActivityIntent != null)
					startActivity(startFontOptionActivityIntent);
			}
		}
			break;

		case R.id.buttonTest: {
			printTest();
		}
			break;

		case R.id.buttonProgram: {
			if (checkBoxProgram.isChecked())
				updateprogram();
		}
			break;
		case R.id.buttonFont: {

			// if (checkBoxFont.isChecked())
			// updatefont();

			updatesmallfont();
		}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return false;

	}

	private void printTest() {
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
		}
		if (disconnected)
			return;
		Message msg = UpdateProgram.updateHandler.obtainMessage();
		msg.what = UpdateProgram.WHAT_PRINT_TEST;
		UpdateProgram.updateHandler.sendMessage(msg);
	}

	/* 记得升级完了要叮一下 */
	private static boolean disconnected = false;

	private static int MaxRetryTimes = 4;
	private static int retryedTimes = 0;

	private static int index = 0;// 份数的索引，从0开始
	private static int orgoffset = 0;

	private static int times = 0;// 如果数据正常初始化,那么这些都会存好.份数
	private static int nCount = 0;
	private static int orglen = 0;
	private static byte[] orgdata = new byte[0];

	private static final int perCmdRespondLength = 12;
	private static final int MutiPackageCount = 8;

	private static byte[] mutiBuf = new byte[MutiPackageCount
			* perCmdRespondLength];
	private static byte[] commonBuf = new byte[perCmdRespondLength];
	private static byte[] countBuf;

	private void updateprogram() {
		if (disconnected)
			return;
		Message msg = UpdateProgram.updateHandler.obtainMessage();
		msg.what = UpdateProgram.WHAT_UPDATE;
		UpdateProgram.updateHandler.sendMessage(msg);
	}

	@SuppressWarnings("unused")
	private void updatefont() {
		if (disconnected)
			return;
		Message msg = UpdateProgram.updateHandler.obtainMessage();
		msg.what = UpdateProgram.WHAT_UPDATE_FONT;
		UpdateProgram.updateHandler.sendMessage(msg);
	}

	private void updatesmallfont() {

		if (null == database9x24bin || null == dataencry9x24bin) {
			try {
				InputStream is = getResources().openRawResource(
						R.raw._0x1b1000_base9x24);
				int binflen = is.available();
				int orglen = (binflen + 255) / 256 * 256;
				database9x24bin = new byte[orglen];
				is.read(database9x24bin, 0, binflen);
				is.close();
				is = getResources().openRawResource(R.raw._0x194000_encry9x24);
				binflen = is.available();
				orglen = (binflen + 255) / 256 * 256;
				dataencry9x24bin = new byte[orglen];
				is.read(dataencry9x24bin, 0, binflen);
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (null == database9x24bin || null == dataencry9x24bin) {
			Toast.makeText(this, "读取失败", Toast.LENGTH_SHORT).show();
		}
		if (disconnected)
			return;
		Message msg = UpdateProgram.updateHandler.obtainMessage();
		msg.what = UpdateProgram.WHAT_UPDATE_SMALL_FONT;
		UpdateProgram.updateHandler.sendMessage(msg);

	}

	static class UpdateProgram extends Thread {

		public static Handler updateHandler = null;

		public static final int WHAT_QUIT = 1000;
		public static final int WHAT_UPDATE = 1001;
		public static final int WHAT_UPDATE_FONT = 1002;
		public static final int WHAT_UPDATE_SMALL_FONT = 1003;
		public static final int WHAT_PRINT_TEST = 1004;

		public static final String DEBUG = "com.lvrenyang.usbtool.debug";
		public static final String EXTRA_DEBUG = "com.lvrenyang.usbtool.extra_debug";
		public static final String UPDATE_INFO = "com.lvrenyang.usbtool.update_info";
		public static final String EXTRA_UPDATE_INFO = "com.lvrenyang.usbtool.extra_update_info";
		public static boolean debug = false;

		@Override
		public void run() {
			Looper.prepare();
			updateHandler = new updateHandler();
			debug("UpdateProgram: loop()");
			Looper.loop();
		}

		private static class updateHandler extends Handler {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				/**
				 * 首先读去数据，读到数据之后，发送出去 该方法进入之后，就永远不会退出
				 */
				case WHAT_UPDATE: {

					if (null == programfile) {
						return;
					}
					/* 需要重新读取file，并填充数据 */
					// textView1.append(file + "\n");
					try {
						FileInputStream fis = new FileInputStream(programfile);
						int binflen = fis.available();
						times = (binflen + 255) / 256;
						nCount = times % MutiPackageCount;
						orglen = times * 256;
						orgdata = new byte[orglen];
						fis.read(orgdata, 0, binflen);
						fis.close();
						// textView1.append("times: " + times + " nCount: " +
						// nCount +
						// "\n");
					} catch (FileNotFoundException e) {
						orgdata = new byte[0];
					} catch (IOException e) {
						orgdata = new byte[0];
					}
					countBuf = new byte[nCount * perCmdRespondLength];

					if (mSerial == null)
						break;
					sendupdateinfo(Integer.MIN_VALUE);
					/* test */
					{
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
							if (disconnected) {
								/* ERRRO */
								error();
								return;
							}
							mSerial.readthread.clear();
							int tmp;
							tmp = mSerial.POS_Write(Cmd.PCmd.test);
							debug("write: " + tmp + "byte(s).\n");
							tmp = mSerial.readthread.read(commonBuf, timeout);
							debug("read: " + tmp + "byte(s).\n");
							if (perCmdRespondLength == tmp) {
								debug("test ok!\n");
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes * 1000) {
							/* ERROR */
							error();
							return;
						}
					}
					/* startUpdate */
					{
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERRRO */
								error();
								return;
							}
							mSerial.readthread.clear();
							mSerial.POS_Write(Cmd.PCmd.startUpdate);
							if (perCmdRespondLength == mSerial.readthread.read(
									commonBuf, timeout)) {
								debug("start update\n");
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}

					index = 0;
					/* update */
					while (index + MutiPackageCount <= times) {
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERROR */
								error();
								return;
							}
							mSerial.readthread.clear();
							mSerial.POS_Write(getUpdateCmd(index,
									MutiPackageCount));
							if (perCmdRespondLength * MutiPackageCount == mSerial.readthread
									.read(mutiBuf, timeout)) {
								index += MutiPackageCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}

					/* last some (nCount) */
					if ((index + MutiPackageCount > times) && (index < times)) {
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERROR */
								error();
								return;
							}
							mSerial.readthread.clear();
							mSerial.POS_Write(getUpdateCmd(index, nCount));
							if (perCmdRespondLength * nCount == mSerial.readthread
									.read(countBuf, timeout)) {
								index += nCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}

					/* endupdate */
					{
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERRRO */
								error();
								return;
							}
							mSerial.POS_Write(Cmd.PCmd.endUpdate);
							if (perCmdRespondLength == mSerial.readthread.read(
									commonBuf, timeout)) {
								debug("end update!\n");
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}
					/* OK */
					ok();
					break;
				}

				case WHAT_UPDATE_FONT: {
					if (null == fontfile) {
						return;
					}
					/* 需要重新读取file，并填充数据 */
					// textView1.append(file + "\n");
					try {
						FileInputStream fis = new FileInputStream(fontfile);
						int binflen = fis.available();
						times = (binflen + 255) / 256;
						nCount = times % MutiPackageCount;
						orglen = times * 256;
						orgdata = new byte[orglen];
						fis.read(orgdata, 0, binflen);
						fis.close();
						// textView1.append("times: " + times + " nCount: " +
						// nCount +
						// "\n");
					} catch (FileNotFoundException e) {
						orgdata = new byte[0];
					} catch (IOException e) {
						orgdata = new byte[0];
					}

					countBuf = new byte[nCount * perCmdRespondLength];

					if (mSerial == null)
						break;
					sendupdateinfo(Integer.MIN_VALUE);
					/* test */
					{
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
							if (disconnected) {
								/* ERRRO */
								error();
								return;
							}
							mSerial.readthread.clear();
							int tmp;
							tmp = mSerial.POS_Write(Cmd.PCmd.test);
							debug("write: " + tmp + "byte(s).\n");
							tmp = mSerial.readthread.read(commonBuf, timeout);
							debug("read: " + tmp + "byte(s).\n");
							if (perCmdRespondLength == tmp) {
								debug("test ok!\n");
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes * 1000) {
							/* ERROR */
							error();
							return;
						}
					}

					index = 0;
					/* update */
					while (index + MutiPackageCount <= times) {
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERROR */
								error();
								return;
							}
							mSerial.readthread.clear();
							mSerial.POS_Write(getFontCmd(index,
									MutiPackageCount));
							if (perCmdRespondLength * MutiPackageCount == mSerial.readthread
									.read(mutiBuf, timeout)) {
								index += MutiPackageCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}

					/* last some (nCount) */
					if ((index + MutiPackageCount > times) && (index < times)) {
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (disconnected) {
								/* ERROR */
								error();
								return;
							}
							mSerial.readthread.clear();
							mSerial.POS_Write(getFontCmd(index, nCount));
							if (perCmdRespondLength * nCount == mSerial.readthread
									.read(countBuf, timeout)) {
								index += nCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes) {
							/* ERROR */
							error();
							return;
						}
					}
					/* OK */
					ok();
					break;
				}

				case WHAT_UPDATE_SMALL_FONT: {

					/* test */
					{
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
							if (disconnected) {
								/* ERRRO */
								error();
								return;
							}
							mSerial.readthread.clear();
							int tmp;
							tmp = mSerial.POS_Write(Cmd.PCmd.test);
							debug("write: " + tmp + "byte(s).\n");
							tmp = mSerial.readthread.read(commonBuf, timeout);
							debug("read: " + tmp + "byte(s).\n");
							if (perCmdRespondLength == tmp) {
								debug("test ok!\n");
								break;
							}
						}
						if (retryedTimes == MaxRetryTimes * 1000) {
							/* ERROR */
							error();
							return;
						}
					}

					for (int i = 0; i < 2; i++) {
						int baseoffset = 0;
						if (i == 0) {
							baseoffset = 0x194000;
							orgdata = dataencry9x24bin;
						} else {
							baseoffset = 0x1b1000;
							orgdata = database9x24bin;
						}
						int binflen = orgdata.length;
						times = (binflen + 255) / 256;
						nCount = times % MutiPackageCount;
						orglen = times * 256;
						countBuf = new byte[nCount * perCmdRespondLength];

						if (mSerial == null)
							break;
						sendupdateinfo(Integer.MIN_VALUE);
						index = 0;
						/* update */
						while (index + MutiPackageCount <= times) {
							for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
								if (disconnected) {
									/* ERROR */
									error();
									return;
								}
								mSerial.readthread.clear();
								mSerial.POS_Write(getFontCmd(index,
										MutiPackageCount, baseoffset));
								if (perCmdRespondLength * MutiPackageCount == mSerial.readthread
										.read(mutiBuf, timeout)) {
									index += MutiPackageCount;
									debug("index = " + index + "\n");
									sendupdateinfo(index);
									break;
								}
							}
							if (retryedTimes == MaxRetryTimes) {
								/* ERROR */
								error();
								return;
							}
						}

						/* last some (nCount) */
						if ((index + MutiPackageCount > times)
								&& (index < times)) {
							for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
								if (disconnected) {
									/* ERROR */
									error();
									return;
								}
								mSerial.readthread.clear();
								mSerial.POS_Write(getFontCmd(index, nCount,
										baseoffset));
								if (perCmdRespondLength * nCount == mSerial.readthread
										.read(countBuf, timeout)) {
									index += nCount;
									debug("index = " + index + "\n");
									sendupdateinfo(index);
									break;
								}
							}
							if (retryedTimes == MaxRetryTimes) {
								/* ERROR */
								error();
								return;
							}
						}

					}
					/* OK */
					ok();
					break;
				}

				case WHAT_PRINT_TEST: {
					if (null == mSerial || data58bin == null)
						return;
					mSerial.write(data58bin, data58bin.length);
					break;
				}

				case WHAT_QUIT:
					Looper.myLooper().quit();
					break;

				default:
					break;
				}

			}
		}

		private static byte[] getUpdateCmd(int index, int count) {
			byte[] data = new byte[268 * count];
			for (int i = 0; i < count; i++) {
				orgoffset = (index + i) * 256;
				byte[] perdata = new byte[268];// 需要发送到下面的数据,这样可以确保每一个perdate都不会被覆盖掉
				perdata[0] = 0x03;
				perdata[1] = (byte) 0xff;
				perdata[2] = 0x2e;
				perdata[3] = 0x00;
				perdata[4] = (byte) (orgoffset & 0xff);// 低位字节在前面
				perdata[5] = (byte) ((orgoffset >> 8) & 0xff);
				perdata[6] = (byte) ((orgoffset >> 16) & 0xff);
				perdata[7] = (byte) ((orgoffset >> 24) & 0xff);
				perdata[8] = 0x00;
				perdata[9] = 0x01;
				perdata[10] = DataUtils.bytesToXor(perdata, 0, 10);
				perdata[11] = DataUtils.bytesToXor(orgdata, orgoffset, 256);
				DataUtils.copyBytes(orgdata, orgoffset, perdata, 12, 256);
				DataUtils.copyBytes(perdata, 0, data, i * 268, 268);
			}
			return data;
		}

		private static byte[] getFontCmd(int index, int count) {
			byte[] data = new byte[268 * count];
			for (int i = 0; i < count; i++) {
				orgoffset = (index + i) * 256;
				byte[] perdata = new byte[268];// 需要发送到下面的数据,这样可以确保每一个perdate都不会被覆盖掉
				perdata[0] = 0x03;
				perdata[1] = (byte) 0xff;
				perdata[2] = 0x63;
				perdata[3] = 0x00;
				perdata[4] = (byte) (orgoffset & 0xff);// 低位字节在前面
				perdata[5] = (byte) ((orgoffset >> 8) & 0xff);
				perdata[6] = (byte) ((orgoffset >> 16) & 0xff);
				perdata[7] = (byte) ((orgoffset >> 24) & 0xff);
				perdata[8] = 0x00;
				perdata[9] = 0x01;
				perdata[10] = DataUtils.bytesToXor(perdata, 0, 10);
				perdata[11] = DataUtils.bytesToXor(orgdata, orgoffset, 256);
				DataUtils.copyBytes(orgdata, orgoffset, perdata, 12, 256);
				DataUtils.copyBytes(perdata, 0, data, i * 268, 268);
			}
			return data;
		}

		private static byte[] getFontCmd(int index, int count, int baseoffset) {
			byte[] data = new byte[268 * count];
			for (int i = 0; i < count; i++) {
				orgoffset = (index + i) * 256;
				byte[] perdata = new byte[268];// 需要发送到下面的数据,这样可以确保每一个perdate都不会被覆盖掉
				perdata[0] = 0x03;
				perdata[1] = (byte) 0xff;
				perdata[2] = 0x63;
				perdata[3] = 0x00;
				perdata[4] = (byte) ((baseoffset + orgoffset) & 0xff);// 低位字节在前面
				perdata[5] = (byte) (((baseoffset + orgoffset) >> 8) & 0xff);
				perdata[6] = (byte) (((baseoffset + orgoffset) >> 16) & 0xff);
				perdata[7] = (byte) (((baseoffset + orgoffset) >> 24) & 0xff);
				perdata[8] = 0x00;
				perdata[9] = 0x01;
				perdata[10] = DataUtils.bytesToXor(perdata, 0, 10);
				perdata[11] = DataUtils.bytesToXor(orgdata, orgoffset, 256);
				DataUtils.copyBytes(orgdata, orgoffset, perdata, 12, 256);
				DataUtils.copyBytes(perdata, 0, data, i * 268, 268);
			}
			return data;
		}

		private static void debug(String msg) {
			if (!debug)
				return;
			if (mContext != null) {
				Intent intent = new Intent(DEBUG);
				intent.putExtra(EXTRA_DEBUG, msg);
				mContext.sendBroadcast(intent);
			}
		}

		private static void sendupdateinfo(int index) {
			if (mContext != null) {
				Intent intent = new Intent(UPDATE_INFO);
				intent.putExtra(EXTRA_UPDATE_INFO, index);
				mContext.sendBroadcast(intent);
			}
		}

		private static void ok() {
			if (mpok.isPlaying())
				mpok.pause();
			mpok.seekTo(0);
			mpok.setVolume(1000, 1000);// 设置声音
			mpok.start();
			sendupdateinfo(Integer.MAX_VALUE);
		}

		private static void error() {
			if (mperror.isPlaying())
				mperror.pause();
			mperror.seekTo(0);
			mperror.setVolume(1000, 1000);// 设置声音
			mperror.start();
			sendupdateinfo(-1);
		}
	}

	// 菜单项被选择事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings: {

			break;
		}
		}
		return false;
	}

	private void debug_toast(String msg) {
		if (debug_main)
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
	}
}
