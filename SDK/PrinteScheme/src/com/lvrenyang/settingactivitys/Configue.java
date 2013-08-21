package com.lvrenyang.settingactivitys;

import btmanager.Cmd;
import btmanager.LayoutUtils;
import btmanager.Pos;
import btmanager.ReadThread;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Configue extends Activity implements OnClickListener {

	public static final String PREFERENCES_SystemName = "PREFERENCES_SystemName";
	public static final String PREFERENCES_SystemSN = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Btname = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Btpwd = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Baudrate = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Language = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Darkness = "PREFERENCES_SystemName";
	public static final String PREFERENCES_DefaultFont = "PREFERENCES_SystemName";
	public static final String PREFERENCES_LFCR = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Idletime = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Powerofftime = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Maxfeedlength = "PREFERENCES_SystemName";
	public static final String PREFERENCES_Blackmarklength = "PREFERENCES_SystemName";

	private TextView tvTopic;
	private Button btBack;

	// 这写要先设置一个上次的默认值，等到读取到了数据再更新
	private Button btSaveParam, btBaudrate, btLanguage, btDarkness,
			btDefaultFont, btLFCR;
	private EditText etSystemName, etSystemSN, etBtname, etBtpwd, etIdletime,
			etPowerofftime, etMaxfeedlength, etBlackmarklength;

	public static String strsystemname = "", strsystemsn = "", strbtname = "",
			strbtpwd = "";
	public static int nbaudrate = 0, nlanguage = 0, ndarkness = 0,
			ndefaultfont = 0, nlfcr = 0, nIdletime = 0, nPwofftime = 0,
			nMaxfeedlength = 0, nBlackmarklength = 0;

	private BroadcastReceiver broadcastReceiver;
	private Dialog dialog;

	// read param
	private static int readParamRetryTimes = 0;
	private static int maxReadParamRetryTimes = 3;
	private static int readParamTimeOut = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_configue);

		btSaveParam = (Button) findViewById(R.id.btSaveParam);
		btBaudrate = (Button) findViewById(R.id.btBaudrate);
		btLanguage = (Button) findViewById(R.id.btLanguage);
		btDarkness = (Button) findViewById(R.id.btDarkness);
		btDefaultFont = (Button) findViewById(R.id.btDefaultFont);
		btLFCR = (Button) findViewById(R.id.btLFCR);
		btBaudrate.setOnClickListener(this);
		btSaveParam.setOnClickListener(this);
		btLanguage.setOnClickListener(this);
		btDarkness.setOnClickListener(this);
		btDefaultFont.setOnClickListener(this);
		btLFCR.setOnClickListener(this);

		etSystemName = (EditText) findViewById(R.id.etSystemname);
		etSystemSN = (EditText) findViewById(R.id.etSystemsn);
		etBtname = (EditText) findViewById(R.id.etBtname);
		etBtpwd = (EditText) findViewById(R.id.etBtpwd);
		etIdletime = (EditText) findViewById(R.id.etIdletime);
		etPowerofftime = (EditText) findViewById(R.id.etPowerofftime);
		etMaxfeedlength = (EditText) findViewById(R.id.etMaxfeedlength);
		etBlackmarklength = (EditText) findViewById(R.id.etBlackmarklength);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setting_set));
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
		// BtService.registReceiver(this);
		updateConfigueUI();
		readParam();

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
		case R.id.btSaveParam: {
			try {

				String strsystemname = etSystemName.getText().toString();
				String strsystemsn = etSystemSN.getText().toString();
				if (strsystemname.length() > 31 || strsystemsn.length() > 31) {
					Toast.makeText(getApplicationContext(), "名称及序列号都不能超过31个字符",
							Toast.LENGTH_SHORT).show();
					break;
				}

				String strname = etBtname.getText().toString();
				String strpwd = etBtpwd.getText().toString();
				byte[] name = strname.getBytes();
				byte[] pwd = strpwd.getBytes();

				if ((pwd.length != 4) || (name.length > 12)
						|| (name.length == 0)) {
					Toast.makeText(getApplicationContext(),
							"名字不能超过12个字节.密码为4个字节", Toast.LENGTH_SHORT).show();
					break;
				}

				String strbaudrate = btBaudrate.getText().toString().trim();
				for (int i = 0; i < Cmd.Constant.strbaudrate.length; i++) {
					if (strbaudrate.equals(Cmd.Constant.strbaudrate[i])) {
						int nBaudrate = Cmd.Constant.nbaudrate[i];
						Cmd.PCmd.setPrintParam[12] = (byte) ((nBaudrate & 0xff0000) >> 24);
						Cmd.PCmd.setPrintParam[13] = (byte) ((nBaudrate & 0xff0000) >> 16);
						Cmd.PCmd.setPrintParam[14] = (byte) ((nBaudrate & 0xff00) >> 8);
						Cmd.PCmd.setPrintParam[15] = (byte) (nBaudrate & 0xff);
						break;
					}
					if ((i + 1) == Cmd.Constant.strbaudrate.length)
						throw new Exception(
								"波特率一般为4800， 9600，19200，38400，57600，115200，中的一个，请输入正确数值。");
				}

				String language = btLanguage.getText().toString();
				for (int i = 0; i < Cmd.Constant.strcodepages.length; i++) {
					if (language.equals(Cmd.Constant.strcodepages[i])) {
						Cmd.PCmd.setPrintParam[16] = (byte) (Cmd.Constant.ncodepages[i] & 0xff);
						break;
					}
					if ((i + 1) == Cmd.Constant.strcodepages.length)
						throw new Exception("请选择一个代码页");
				}

				String darkness = btDarkness.getText().toString();
				for (int i = 0; i < Cmd.Constant.strdarkness.length; i++) {
					if (darkness.equals(Cmd.Constant.strdarkness[i])) {
						Cmd.PCmd.setPrintParam[17] = (byte) (Cmd.Constant.ndarkness[i] & 0xff);
						break;
					}
					if ((i + 1) == Cmd.Constant.strdarkness.length)
						throw new Exception("选择一个加热浓度");
				}

				String defaultfont = btDefaultFont.getText().toString();
				for (int i = 0; i < Cmd.Constant.strdefaultfont.length; i++) {
					if (defaultfont.equals(Cmd.Constant.strdefaultfont[i])) {
						Cmd.PCmd.setPrintParam[18] = (byte) (Cmd.Constant.ndefaultfont[i] & 0xff);
						break;
					}
					if ((i + 1) == Cmd.Constant.strdefaultfont.length)
						throw new Exception("选择一个默认字体");
				}

				String strlfcr = btLFCR.getText().toString();
				for (int i = 0; i < Cmd.Constant.strlinefeed.length; i++) {
					if (strlfcr.equals(Cmd.Constant.strlinefeed[i])) {
						Cmd.PCmd.setPrintParam[19] = (byte) (Cmd.Constant.nlinefeed[i] & 0xff);
						break;
					}
					if ((i + 1) == Cmd.Constant.strlinefeed.length)
						throw new Exception("选择一个换行标记");
				}

				String stridletime = etIdletime.getText().toString().trim();
				int nIdletime = Integer.parseInt(stridletime);
				if ((nIdletime < 0) || (nIdletime > 65535))
					throw new Exception("空闲等待时间可选值为0-65535.(单位秒)");
				Cmd.PCmd.setPrintParam[20] = (byte) (nIdletime & 0xff);
				Cmd.PCmd.setPrintParam[21] = (byte) ((nIdletime & 0xff00) >> 8);

				String strpwofftime = etPowerofftime.getText().toString()
						.trim();
				int nPwofftime = Integer.parseInt(strpwofftime);
				if ((nPwofftime < 0) || (nPwofftime > 65535))
					throw new Exception("自动关机时间可选值为0-65535.(单位秒)");
				Cmd.PCmd.setPrintParam[22] = (byte) (nPwofftime & 0xff);
				Cmd.PCmd.setPrintParam[23] = (byte) ((nPwofftime & 0xff00) >> 8);

				String strmaxfeedlength = etMaxfeedlength.getText().toString()
						.trim();
				int nMaxfeedlength = Integer.parseInt(strmaxfeedlength);
				if ((nMaxfeedlength < 0) || (nMaxfeedlength > 65535))
					throw new Exception("走纸按键最大走纸距离可选值为0-65535.(单位毫米)");
				Cmd.PCmd.setPrintParam[24] = (byte) (nMaxfeedlength & 0xff);
				Cmd.PCmd.setPrintParam[25] = (byte) ((nMaxfeedlength & 0xff) >> 8);

				String strblackmarklength = etBlackmarklength.getText()
						.toString().trim();
				int nBlackmarklength = Integer.parseInt(strblackmarklength);
				if ((nBlackmarklength < 0) || (nBlackmarklength > 65535))
					throw new Exception("黑标最大寻找距离可选值为0-65535.(单位毫米)");
				Cmd.PCmd.setPrintParam[26] = (byte) (nBlackmarklength & 0xff);
				Cmd.PCmd.setPrintParam[27] = (byte) ((nBlackmarklength & 0xff) >> 8);

				if (Pos.POS_isConnected()) {
					Pos.POS_SetBluetooth(name, pwd);
					Pos.POS_SetSystemInfo(strsystemname, strsystemsn);
					Pos.POS_SetPrintParam(Cmd.PCmd.setPrintParam);
					Pos.POS_Write(new byte[] { 0x12, 0x54 });
					Toast.makeText(getApplicationContext(),
							"设置完毕，请重启打印机以使设置生效。", Toast.LENGTH_SHORT).show();
					Configue.this.finish();
				} else {
					throw new Exception("连接断开");
				}

			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(), "格式错误,请对输入数字",
						Toast.LENGTH_LONG).show();
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
				break;
			}

			break;
		}

		case R.id.btBaudrate: {
			ScrollView scrollView = new ScrollView(Configue.this);
			LinearLayout llBaudrate = new LinearLayout(Configue.this);
			llBaudrate.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llBaudrate);
			AlertDialog.Builder builder = new AlertDialog.Builder(Configue.this);
			builder.setTitle("设置波特率").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Cmd.Constant.strbaudrate.length != Cmd.Constant.nbaudrate.length) {
				Toast.makeText(getApplicationContext(), "波特率映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Cmd.Constant.strbaudrate.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setLayoutParams(btSaveParam.getLayoutParams());
				button.setGravity(Gravity.LEFT);
				button.setText(Cmd.Constant.strbaudrate[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btBaudrate.setText(Cmd.Constant.strbaudrate[index]);
						dialog.dismiss();
					}

				});
				llBaudrate.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btLanguage: {
			ScrollView scrollView = new ScrollView(Configue.this);
			LinearLayout llLanguage = new LinearLayout(Configue.this);
			llLanguage.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llLanguage);
			AlertDialog.Builder builder = new AlertDialog.Builder(Configue.this);
			builder.setTitle("选择代码页").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Cmd.Constant.strcodepages.length != Cmd.Constant.ncodepages.length) {
				Toast.makeText(getApplicationContext(), "代码页映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Cmd.Constant.strcodepages.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setLayoutParams(btSaveParam.getLayoutParams());
				button.setGravity(Gravity.LEFT);
				button.setText(Cmd.Constant.strcodepages[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btLanguage.setText(Cmd.Constant.strcodepages[index]);
						dialog.dismiss();
					}

				});
				llLanguage.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btDarkness: {
			ScrollView scrollView = new ScrollView(Configue.this);
			LinearLayout llDarkness = new LinearLayout(Configue.this);
			llDarkness.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llDarkness);
			AlertDialog.Builder builder = new AlertDialog.Builder(Configue.this);
			builder.setTitle("设置浓度").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Cmd.Constant.strdarkness.length != Cmd.Constant.ndarkness.length) {
				Toast.makeText(getApplicationContext(), "浓度映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Cmd.Constant.strdarkness.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setGravity(Gravity.LEFT);
				button.setText(Cmd.Constant.strdarkness[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btDarkness.setText(Cmd.Constant.strdarkness[index]);
						dialog.dismiss();
					}

				});
				llDarkness.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btDefaultFont: {
			ScrollView scrollView = new ScrollView(Configue.this);
			LinearLayout llDefaultFont = new LinearLayout(Configue.this);
			llDefaultFont.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llDefaultFont);
			AlertDialog.Builder builder = new AlertDialog.Builder(Configue.this);
			builder.setTitle("选择默认字体").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Cmd.Constant.strdefaultfont.length != Cmd.Constant.ndefaultfont.length) {
				Toast.makeText(getApplicationContext(), "字体映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Cmd.Constant.strdefaultfont.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setLayoutParams(btSaveParam.getLayoutParams());
				button.setGravity(Gravity.LEFT);
				button.setText(Cmd.Constant.strdefaultfont[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btDefaultFont
								.setText(Cmd.Constant.strdefaultfont[index]);
						dialog.dismiss();
					}

				});
				llDefaultFont.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btLFCR: {
			ScrollView scrollView = new ScrollView(Configue.this);
			LinearLayout llLinefeed = new LinearLayout(Configue.this);
			llLinefeed.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llLinefeed);
			AlertDialog.Builder builder = new AlertDialog.Builder(Configue.this);
			builder.setTitle("选择换行标记").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Cmd.Constant.strlinefeed.length != Cmd.Constant.nlinefeed.length) {
				Toast.makeText(getApplicationContext(), "换行标记映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Cmd.Constant.strlinefeed.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setLayoutParams(btSaveParam.getLayoutParams());
				button.setGravity(Gravity.LEFT);
				button.setText(Cmd.Constant.strlinefeed[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btLFCR.setText(Cmd.Constant.strlinefeed[index]);
						dialog.dismiss();
					}

				});
				llLinefeed.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.etSystemname:
		case R.id.etSystemsn:
		case R.id.etBtname:
		case R.id.etBtpwd:
		case R.id.etIdletime:
		case R.id.etPowerofftime:
		case R.id.etMaxfeedlength:
		case R.id.etBlackmarklength: {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putString(PREFERENCES_SystemName, strsystemname);
				editor.putString(PREFERENCES_SystemSN, strsystemsn);
				editor.putString(PREFERENCES_Btname, strbtname);
				editor.putString(PREFERENCES_Btpwd, strbtpwd);
				editor.commit();
				editor.putInt(PREFERENCES_Baudrate, nbaudrate);
				editor.putInt(PREFERENCES_Language, nlanguage);
				editor.putInt(PREFERENCES_Darkness, ndarkness);
				editor.putInt(PREFERENCES_DefaultFont, ndefaultfont);
				editor.putInt(PREFERENCES_LFCR, nlfcr);
				editor.putInt(PREFERENCES_Idletime, nIdletime);
				editor.putInt(PREFERENCES_Powerofftime, nPwofftime);
				editor.putInt(PREFERENCES_Maxfeedlength, nMaxfeedlength);
				editor.putInt(PREFERENCES_Blackmarklength, nBlackmarklength);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}

	public static String getPrinterCharsetName() {
		switch (nlanguage) {
		case Cmd.Constant.CODEPAGE_CHINESE: {
			return "GBK";
		}
		case Cmd.Constant.CODEPAGE_BIG5: {
			return "BIG5";
		}
		case Cmd.Constant.CODEPAGE_UTF_8: {

			return "UTF8";
		}
		default:
			return "GBK";
		}
	}

	/**
	 * 不同的addr代表不同的参数类型，在constant里面有定义， param和data是通过协议返回的。分别代表参数和数据
	 * 
	 * @param addr
	 * @param param
	 * @param data
	 * @return
	 */
	public static boolean loadParams(int addr, int param, byte[] data) {
		switch (addr) {
		case Cmd.Constant.LAN_INFO_ADDR: {

			break;
		}
		case Cmd.Constant.PRN_INFO_ADDR: {
			int dataOffset = addr - param;
			if (data.length < (dataOffset + Cmd.Constant.PRN_INFO_LEN))
				return false;

			int tmpBaudrate, tmpCodePage, tmpDarkness, tmpDefaultFontType, tmpLineFeed, tmpIdletime, tmpPwofftime, tmpMaxfeedlength, tmpBlackmarklength;
			if ((data[dataOffset] != (byte) 0xdd)
					|| data[dataOffset + Cmd.Constant.PRN_INFO_LEN - 1] != (byte) 0xaa) {
				return false;
			} else {
				// 波特率
				dataOffset += 2;
				if ((data[dataOffset] & 0xff) < Cmd.Constant.nbaudrate.length)
					tmpBaudrate = Cmd.Constant.nbaudrate[data[dataOffset]];
				else
					return false;

				// Code Page
				dataOffset += 6;
				if (Cmd.Constant.getCodePageStr(data[dataOffset] & 0xff)
						.equals(""))
					return false;
				else
					tmpCodePage = data[dataOffset] & 0xff;

				// Darkness
				dataOffset++;
				if (Cmd.Constant.getDarknessStr(data[dataOffset] & 0xff)
						.equals(""))
					return false;
				else
					tmpDarkness = data[dataOffset] & 0xff;

				// Default Font Type
				dataOffset++;
				if (Cmd.Constant.getDefaultFontStr(data[dataOffset] & 0xff)
						.equals(""))
					return false;
				else
					tmpDefaultFontType = data[dataOffset] & 0xff;

				// CR LF
				dataOffset++;
				if (Cmd.Constant.getLineFeedStr(data[dataOffset] & 0xff)
						.equals(""))
					return false;
				else
					tmpLineFeed = data[dataOffset] & 0xff;

				// sleep Idle time
				dataOffset++;
				tmpIdletime = ((data[dataOffset] & 0xff) + ((data[dataOffset + 1] & 0xff) << 8));

				// power off time
				dataOffset += 2;
				tmpPwofftime = ((data[dataOffset] & 0xff) + ((data[dataOffset + 1] & 0xff) << 8));

				// max feed length
				dataOffset += 2;
				tmpMaxfeedlength = ((data[dataOffset] & 0xff) + ((data[dataOffset + 1] & 0xff) << 8));
				if (tmpMaxfeedlength >= 1000)
					tmpMaxfeedlength = 1000;
				else if (tmpMaxfeedlength <= 50)
					tmpMaxfeedlength = 50;

				// black mark length
				dataOffset += 2;
				tmpBlackmarklength = ((data[dataOffset] & 0xff) + ((data[dataOffset + 1] & 0xff) << 8));
				if (tmpBlackmarklength >= 1000)
					tmpBlackmarklength = 1000;

				nbaudrate = tmpBaudrate;
				nlanguage = tmpCodePage;
				ndarkness = tmpDarkness;
				ndefaultfont = tmpDefaultFontType;
				nlfcr = tmpLineFeed;
				nIdletime = tmpIdletime;
				nPwofftime = tmpPwofftime;
				nMaxfeedlength = tmpMaxfeedlength;
				nBlackmarklength = tmpBlackmarklength;
				return true;
			}

		}
		case Cmd.Constant.OEM_INFO_ADDR: {

			break;
		}
		case Cmd.Constant.USER_INFO_ADDR: {

			break;
		}
		case Cmd.Constant.BT_INFO_ADDR: {
			int dataOffset = addr - param;
			if (data.length < (dataOffset + Cmd.Constant.BT_INFO_LEN))
				return false;

			// 代表已经修改，但是并没有保存到蓝牙
			// if (data[dataOffset + Cmd.Constant.BT_INFO_LEN - 1] !=
			// (byte)0xdd)
			String tmpBtName = "";
			String tmpBtPassword = "";

			dataOffset += 2;
			for (int i = dataOffset; i < dataOffset
					+ Cmd.Constant.BT_MAX_NAME_LEN + 1; i++) {
				if (data[i] == 0) {
					tmpBtName = new String(data, dataOffset, i - dataOffset);
					break;
				}
				if (i == dataOffset + Cmd.Constant.BT_MAX_NAME_LEN + 1)
					return false;
			}

			// 以00结尾，需要跳过一个
			dataOffset += Cmd.Constant.BT_MAX_NAME_LEN + 1;
			for (int i = dataOffset; i < dataOffset
					+ Cmd.Constant.BT_MAX_PWD_LEN + 1; i++) {
				if (data[i] == 0) {
					tmpBtPassword = new String(data, dataOffset, i - dataOffset);
					break;
				}
				if (i == dataOffset + Cmd.Constant.BT_MAX_PWD_LEN + 1)
					return false;
			}

			strbtname = tmpBtName;
			strbtpwd = tmpBtPassword;
			return true;

		}
		case Cmd.Constant.IRD_INFO_ADDR: {

			break;
		}
		case Cmd.Constant.FAC_INFO_ADDR: {
			int dataOffset = addr - param;
			if (data.length < (dataOffset + Cmd.Constant.FAC_INFO_LEN))
				return false;

			String tmpSystemName = "";
			String tmpSystemSn = "";

			dataOffset += 2;
			for (int i = dataOffset; i < dataOffset
					+ Cmd.Constant.FAC_MAX_NAME_LEN + 1; i++) {
				if (data[i] == 0) {
					tmpSystemName = new String(data, dataOffset, i - dataOffset);

					// 这里因为小许的算法和前面不一样，所以跟着改一下吧
					dataOffset = i + 1;

					break;
				}
				if (i == dataOffset + Cmd.Constant.FAC_MAX_NAME_LEN + 1)
					return false;
			}

			// 以00结尾，需要跳过一个
			// dataOffset += Cmd.Constant.FAC_MAX_NAME_LEN + 1;
			for (int i = dataOffset; i < dataOffset
					+ Cmd.Constant.FAC_MAX_SN_LEN + 1; i++) {
				if (data[i] == 0) {
					tmpSystemSn = new String(data, dataOffset, i - dataOffset);

					break;
				}
				if (i == dataOffset + Cmd.Constant.FAC_MAX_SN_LEN + 1)
					return false;
			}

			strsystemname = tmpSystemName;
			strsystemsn = tmpSystemSn;
			return true;
		}
		case Cmd.Constant.USER_INFO2_ADDR: {

			break;
		}

		}
		return true;
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (action.equals(ReadThread.ACTION_READTHREADRECEIVERESPOND)) {
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);

					int cmd = intent.getIntExtra(ReadThread.EXTRA_PCMDCMD, 0);
					int para = intent.getIntExtra(ReadThread.EXTRA_PCMDPARA, 0);
					int length = intent.getIntExtra(
							ReadThread.EXTRA_PCMDLENGTH, 0);
					byte[] rcs = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDDATA);

					if (recstatus) {
						if ((cmd == 0x2c)
								&& ((para <= Cmd.Constant.PRN_INFO_ADDR))
								&& ((para + length) >= (Cmd.Constant.PRN_INFO_ADDR + Cmd.Constant.PRN_INFO_LEN))) {
							// updateParams
							// 更新Configue的相关数据，并发送广播给Configue更新界面()
							boolean tmp = Configue.loadParams(
									Cmd.Constant.PRN_INFO_ADDR, para, rcs);
							if (OptionsActivity.getDebug()) {
								Toast.makeText(
										Configue.this,
										"" + Cmd.Constant.PRN_INFO_ADDR + ": "
												+ tmp, Toast.LENGTH_SHORT)
										.show();
							}

							// 只要读取了PRN_INFO，那么我认为你也需要读蓝牙信息
							// 此处，厂商信息也一并包涵了
							Pos.POS_ReadFlash(Cmd.Constant.BT_INFO_ADDR,
									readParamTimeOut);
							return;
						}

						if ((cmd == 0x2c)
								&& ((para <= Cmd.Constant.BT_INFO_ADDR))
								&& ((para + length) >= (Cmd.Constant.BT_INFO_ADDR + Cmd.Constant.BT_INFO_LEN))) {
							boolean tmp = Configue.loadParams(
									Cmd.Constant.BT_INFO_ADDR, para, rcs);
							if (OptionsActivity.getDebug()) {
								Toast.makeText(
										Configue.this,
										"" + Cmd.Constant.BT_INFO_ADDR + ": "
												+ tmp, Toast.LENGTH_SHORT)
										.show();
							}
						}

						if ((cmd == 0x2c)
								&& ((para <= Cmd.Constant.FAC_INFO_ADDR))
								&& ((para + length) >= (Cmd.Constant.FAC_INFO_ADDR + Cmd.Constant.FAC_INFO_LEN))) {
							boolean tmp = Configue.loadParams(
									Cmd.Constant.FAC_INFO_ADDR, para, rcs);
							if (OptionsActivity.getDebug()) {
								Toast.makeText(
										Configue.this,
										"" + Cmd.Constant.FAC_INFO_ADDR + ": "
												+ tmp, Toast.LENGTH_SHORT)
										.show();
							}

							if (dialog != null) {
								updateConfigueUI();
								dialog.dismiss();
								Toast.makeText(context,
										getString(R.string.refreshsuccess),
										Toast.LENGTH_SHORT).show();
								return;
							}

						}
					} else {
						if ((cmd == (byte) 0x2c)
								&& ((para == Cmd.Constant.PRN_INFO_ADDR) || (para == Cmd.Constant.BT_INFO_ADDR))) {
							readParamRetryTimes++;
							if (readParamRetryTimes < maxReadParamRetryTimes) {
								Pos.POS_ReadFlash(para, readParamTimeOut);
							} else {
								dialog.dismiss();
								Toast.makeText(context,
										getString(R.string.refreshfailed),
										Toast.LENGTH_SHORT).show();
								// 读取成功，返回
								return;
							}
						}

					}
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVERESPOND);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private void updateConfigueUI() {
		// Configue
		etSystemName.setText(strsystemname);
		etSystemSN.setText(strsystemsn);
		etBtname.setText(strbtname);
		etBtpwd.setText(strbtpwd);
		btBaudrate.setText(Cmd.Constant.getBaudrateStr(nbaudrate));
		btLanguage.setText(Cmd.Constant.getCodePageStr(nlanguage));
		btDarkness.setText(Cmd.Constant.getDarknessStr(ndarkness));
		btDefaultFont.setText(Cmd.Constant.getDefaultFontStr(ndefaultfont));
		btLFCR.setText(Cmd.Constant.getLineFeedStr(nlfcr));
		etIdletime.setText(Integer.toString(nIdletime));
		etPowerofftime.setText(Integer.toString(nPwofftime));
		etMaxfeedlength.setText(Integer.toString(nMaxfeedlength));
		etBlackmarklength.setText(Integer.toString(nBlackmarklength));
	}

	private void readParam() {
		if (Pos.POS_isConnected()) {
			Pos.POS_ReadFlash(Cmd.Constant.PRN_INFO_ADDR, readParamTimeOut);
			dialog = LayoutUtils.showDialog(this,
					getString(R.string.refreshing));
			// 进Configue的activity就要刷新。
		}
	}

}
