package com.lvrenyang.printescheme;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import btmanager.LayoutUtils;
import btmanager.Pos;
import btmanager.ReadThread;

import com.lvrenyang.commandactivitys.ReadActivity;
import com.lvrenyang.R;
import com.lvrenyang.settingactivitys.AutoConnect;
import com.lvrenyang.settingactivitys.Configue;
import com.lvrenyang.settingactivitys.Connect;
import com.lvrenyang.settingactivitys.Help;
import com.lvrenyang.settingactivitys.SearchAndConnect;
import com.lvrenyang.settingactivitys.SetKey;
import com.lvrenyang.settingactivitys.UpdateProgram;
import com.lvrenyang.settingactivitys.UpdateProgramOptions;
import com.lvrenyang.textandpictureactivitys.Barcode;
import com.lvrenyang.textandpictureactivitys.GalleryPhotos;
import com.lvrenyang.textandpictureactivitys.Qrcode;
import com.lvrenyang.textandpictureactivitys.SetAndShow;
import com.lvrenyang.textandpictureactivitys.Text;
import com.lvrenyang.webactivitys.GuideActivity;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.content.CursorLoader;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 用户退出MainActivity的时候，需要Notification，以便回到这个地方 分享打印，剪切板打印
 * 
 * 
 * @author lvrenyang
 * 
 */
public class MainActivity extends Activity implements OnClickListener {

	private BroadcastReceiver broadcastReceiver;
	public static final String EXTRA_CONTEXT = "EXTRA_CONTEXT";

	// MainActivity widget
	// private Button btTextAndPicture, btWeb, btCommand, btSetting;
	private Button btBack, btOptions;
	private TextView tvTopic;
	private RelativeLayout relativeLayoutMain;

	// Preferences field
	public static final String PREFERENCES_FILE = "com.lvrenyang.preferencesfile";

	private static int showLayoutId = R.id.btTextAndPicture;

	public static final Object lock_preferences = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (BluetoothAdapter.getDefaultAdapter() == null) {
			Toast.makeText(this, getString(R.string.bluetoothrequired),
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			BluetoothAdapter.getDefaultAdapter().enable();
		}

		LayoutUtils.initContentView(MainActivity.this, R.layout.actionbar,
				R.layout.main);

		findViewById(R.id.btTextAndPicture).setOnClickListener(this);
		findViewById(R.id.btWeb).setOnClickListener(this);
		findViewById(R.id.btCommand).setOnClickListener(this);
		findViewById(R.id.btSetting).setOnClickListener(this);

		btBack = (Button) findViewById(R.id.btBack);
		btOptions = (Button) findViewById(R.id.btOptions);
		btBack.setOnClickListener(this);
		btOptions.setOnClickListener(this);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		relativeLayoutMain = (RelativeLayout) findViewById(R.id.relativeLayoutMain);

		readAllPreferences();
		initBroadcast();
		// 从别的线程转到这个线程时，别的线程已经更新

		if (!BtService.isRunning()) {
			Intent intent = new Intent(this, BtService.class);
			startService(intent);
		}
		
		handleIntent(getIntent());

	}

	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.

	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		onClickFunction(showLayoutId);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		onClickFunction(arg0.getId());
	}

	/**
	 * 这样可以设置默认值
	 * 
	 * @param id
	 */
	private void onClickFunction(int id) {
		switch (id) {
		// 这4个button是来更换布局的
		case R.id.btTextAndPicture: {
			showLayoutId = id;
			tvTopic.setText(getString(R.string.main_textandpicture));
			View view = LayoutInflater.from(this).inflate(
					R.layout.linearlayouttextandpicture, null);
			initInflatedView(view);
			relativeLayoutMain.removeAllViews();
			relativeLayoutMain.addView(view);
			break;
		}

		case R.id.btWeb: {
			showLayoutId = id;
			tvTopic.setText(getString(R.string.main_web));
			View view = LayoutInflater.from(this).inflate(
					R.layout.linearlayoutweb, null);
			initInflatedView(view);
			relativeLayoutMain.removeAllViews();
			relativeLayoutMain.addView(view);
			break;
		}

		case R.id.btCommand: {
			showLayoutId = id;
			tvTopic.setText(getString(R.string.main_command));
			View view = LayoutInflater.from(this).inflate(
					R.layout.linearlayoutcommand, null);
			initInflatedView(view);
			relativeLayoutMain.removeAllViews();
			relativeLayoutMain.addView(view);
			break;
		}

		case R.id.btSetting: {
			showLayoutId = id;
			tvTopic.setText(getString(R.string.main_setting));
			View view = LayoutInflater.from(this).inflate(
					R.layout.linearlayoutsetting, null);
			initInflatedView(view);
			relativeLayoutMain.removeAllViews();
			relativeLayoutMain.addView(view);
			updateSettings();
			break;
		}

		case R.id.btBack: {

			break;
		}

		case R.id.btOptions: {
			Intent intent = new Intent(this, OptionsActivity.class);
			startActivity(intent);

			break;
		}

		}

	}

	private void initInflatedView(View view) {
		switch (view.getId()) {
		case R.id.layoutTextAndPicture: {
			Button btText01 = (Button) view.findViewById(R.id.btText01);
			Button btPicture01 = (Button) view.findViewById(R.id.btPicture01);
			Button btSetAndShow = (Button) view.findViewById(R.id.btSetAndShow);
			Button btBarcode = (Button) view.findViewById(R.id.btBarcode);
			Button btQrcode = (Button) view.findViewById(R.id.btQrcode);
			btText01.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, Text.class);
					startActivity(intent);
				}

			});

			btPicture01.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							GalleryPhotos.class);
					// Intent intent = new Intent(MainActivity.this,
					// ImageGridActivity.class);
					startActivity(intent);
				}

			});

			btSetAndShow.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							SetAndShow.class);
					startActivity(intent);
				}

			});

			btBarcode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, Barcode.class);
					startActivity(intent);
				}

			});

			btQrcode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, Qrcode.class);
					startActivity(intent);
				}

			});
			break;
		}
		case R.id.layoutWeb: {
			Button btFavourite = (Button) view.findViewById(R.id.btFavourite);
			Button btHistory = (Button) view.findViewById(R.id.btHistory);
			Button btGuide = (Button) view.findViewById(R.id.btGuide);
			btFavourite.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});
			btHistory.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});
			btGuide.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							GuideActivity.class);
					startActivity(intent);
				}

			});
			break;
		}
		case R.id.layoutCommand: {
			Button buttonCmd1 = (Button) view.findViewById(R.id.buttonCmd1);
			Button buttonCmd2 = (Button) view.findViewById(R.id.buttonCmd2);
			Button buttonCmd3 = (Button) view.findViewById(R.id.buttonCmd3);
			Button buttonCmd4 = (Button) view.findViewById(R.id.buttonCmd4);
			buttonCmd1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});
			buttonCmd2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});
			buttonCmd3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							ReadActivity.class);
					startActivity(intent);
				}

			});
			buttonCmd4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});

			break;
		}
		case R.id.layoutSetting: {
			Button btSetting01 = (Button) view.findViewById(R.id.btSetting01);
			Button btSetting02 = (Button) view.findViewById(R.id.btSetting02);
			Button btSetting03 = (Button) view.findViewById(R.id.btSetting03);
			Button btSetting04 = (Button) view.findViewById(R.id.btSetting04);
			Button btSetting05 = (Button) view.findViewById(R.id.btSetting05);
			Button btSetting06 = (Button) view.findViewById(R.id.btSetting06);
			Button btSetting07 = (Button) view.findViewById(R.id.btSetting07);
			Button btSetting08 = (Button) view.findViewById(R.id.btSetting08);
			btSetting01.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, Connect.class);
					startActivity(intent);
				}

			});
			btSetting02.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, SearchAndConnect.class);
					startActivity(intent);
				}

			});
			btSetting03.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							Configue.class);
					startActivity(intent);
				}

			});
			btSetting04.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							UpdateProgram.class);
					startActivity(intent);
				}

			});
			btSetting05.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}

			});
			btSetting06.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this,
							AutoConnect.class);
					startActivity(intent);
				}

			});
			btSetting07.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, SetKey.class);
					startActivity(intent);
				}

			});
			btSetting08.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, Help.class);
					startActivity(intent);
				}

			});
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
				if (action.equals(BtService.ACTION_SERVICEREADY)) {
					// 子线程handler准备完毕
					Pos.POS_Read();

				} else if (action.equals(OptionsActivity.FINISH)) {
					finish();
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BtService.ACTION_SERVICEREADY);
		intentFilter.addAction(OptionsActivity.FINISH);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		unregisterReceiver(broadcastReceiver);
	}

	/**
	 * 每次从暂停状态回来都要读去首选项配置 因为不知到别的activity到底改了什么preferences
	 * 每次点击选择页框的时候，都要init相关布局 因为不同布局都是零时生成的，点击别的地方就消失了，配置就必须重新读取
	 * 
	 * 因为自动连接之需要mac地址，更改布局并不会有影响，只有进了setting活动，更改了选项才有可能
	 * 所以之需要在onResume中监听mac地址变化即可
	 */

	private void updateSettings() {
		Button button = (Button) findViewById(R.id.btSetting06);
		if (button != null) {
			if (AutoConnect.autoConnectMode
					.equals(AutoConnect.VALUE_autoConnectModeActive))
				button.setText(getString(R.string.setting_autoconnection)
						+ getString(R.string.blank)
						+ AutoConnect.autoConnectName);
			else if (AutoConnect.autoConnectMode
					.equals(AutoConnect.VALUE_autoConnectModeWait))
				button.setText(getString(R.string.setting_autoconnection)
						+ getString(R.string.blank)
						+ AutoConnect.autoConnectName);
			else
				button.setText(getString(R.string.setting_autoconnection)
						+ getString(R.string.blank)
						+ getString(R.string.notset));
		}
	}

	private void readAllPreferences() {
		synchronized (MainActivity.lock_preferences) {
			// 这个异常是正常的，不影响实际，因为有默认值
			try {
				SharedPreferences mSharedPreferences = this
						.getSharedPreferences(PREFERENCES_FILE, 0);

				// AutoConnect
				AutoConnect.autoConnectName = mSharedPreferences.getString(
						AutoConnect.PREFERENCES_autoConnectName, "");
				AutoConnect.autoConnectMac = mSharedPreferences.getString(
						AutoConnect.PREFERENCES_autoConnectMac, "");
				AutoConnect.autoConnectMode = mSharedPreferences.getString(
						AutoConnect.PREFERENCES_autoConnectMode,
						AutoConnect.VALUE_autoConnectModeNotSet);

				// OptionsActivity
				OptionsActivity.setDebug(mSharedPreferences.getBoolean(
						OptionsActivity.PREFERENCES_debug, false));

				// Configue
				Configue.strsystemname = mSharedPreferences.getString(
						Configue.PREFERENCES_SystemName,
						getString(R.string.defaultprintername));
				Configue.strsystemsn = mSharedPreferences.getString(
						Configue.PREFERENCES_SystemSN,
						getString(R.string.defaultprintersn));
				Configue.strbtname = mSharedPreferences.getString(
						Configue.PREFERENCES_Btname,
						getString(R.string.defaultbtname));
				Configue.strbtpwd = mSharedPreferences.getString(
						Configue.PREFERENCES_Btpwd,
						getString(R.string.defaultbtpwd));
				Configue.nbaudrate = mSharedPreferences.getInt(
						Configue.PREFERENCES_Baudrate, 9600);
				Configue.nlanguage = mSharedPreferences.getInt(
						Configue.PREFERENCES_Language, 255);
				Configue.ndarkness = mSharedPreferences.getInt(
						Configue.PREFERENCES_Darkness, 0);
				Configue.ndefaultfont = mSharedPreferences.getInt(
						Configue.PREFERENCES_DefaultFont, 0);
				Configue.nlfcr = mSharedPreferences.getInt(
						Configue.PREFERENCES_LFCR, 0);
				Configue.nIdletime = mSharedPreferences.getInt(
						Configue.PREFERENCES_Idletime, 180);
				Configue.nPwofftime = mSharedPreferences.getInt(
						Configue.PREFERENCES_Powerofftime, 1800);
				Configue.nMaxfeedlength = mSharedPreferences.getInt(
						Configue.PREFERENCES_Maxfeedlength, 300);
				Configue.nBlackmarklength = mSharedPreferences.getInt(
						Configue.PREFERENCES_Blackmarklength, 300);

				// SetAndShow
				SetAndShow.nDarkness = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nDarkness, 0);
				SetAndShow.nFontSize = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nFontSize, 0);
				SetAndShow.nTextAlign = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nTextAlign, 0);
				SetAndShow.nScaleTimesWidth = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nScaleTimesWidth, 0);
				SetAndShow.nScaleTimesHeight = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nScaleTimesHeight, 0);
				SetAndShow.nFontStyle = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nFontStyle, 0);
				SetAndShow.nLineHeight = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nLineHeight, 32);
				SetAndShow.nRightSpace = mSharedPreferences.getInt(
						SetAndShow.PREFERENCES_TEXT_nRightSpace, 0);
				// UpdateProgramOptions
				UpdateProgramOptions.programPath = mSharedPreferences
						.getString(
								UpdateProgramOptions.PREFERENCES_ProgramPath,
								"");

				// Barcode
				Barcode.nBarcodetype = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_Barcodetype, 0);
				Barcode.nStartOrgx = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_StartOrgx, 0);
				Barcode.nBarcodeWidth = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_BarcodeWidth, 0);
				Barcode.nBarcodeHeight = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_BarcodeHeight, 0);
				Barcode.nBarcodeFontType = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_BarcodeFontType, 0);
				Barcode.nBarcodeFontPosition = mSharedPreferences.getInt(
						Barcode.PREFERENCES_TEXT_BarcodeFontPosition, 0);

				// Qrcode
				Qrcode.nQrcodetype = mSharedPreferences.getInt(
						Qrcode.PREFERENCES_TEXT_Qrcodetype, 0);
				Qrcode.nQrcodeWidth = mSharedPreferences.getInt(
						Qrcode.PREFERENCES_TEXT_QrcodeWidth, 0);
				Qrcode.nErrorCorrectionLevel = mSharedPreferences.getInt(
						Qrcode.PREFERENCES_TEXT_ErrorCorrectionLevel, 0);

				// SetKey
				SetKey.deskey = mSharedPreferences.getString(
						SetKey.PREFERENCES_deskey, "12345678");
				ReadThread.setKey(SetKey.deskey.getBytes());

				// GuideActivity bookmarks
				GuideActivity.bookmarkswebsite[0] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark1website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[1] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark2website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[2] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark3website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[3] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark4website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[4] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark5website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[5] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark6website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[6] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark7website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarkswebsite[7] = mSharedPreferences
						.getString(
								GuideActivity.PREFERENCES_WEB_bookmark8website,
								GuideActivity.baiduwebsite);
				GuideActivity.bookmarksname[0] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark1name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[1] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark2name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[2] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark3name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[3] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark4name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[4] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark5name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[5] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark6name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[6] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark7name,
						GuideActivity.baiduname);
				GuideActivity.bookmarksname[7] = mSharedPreferences.getString(
						GuideActivity.PREFERENCES_WEB_bookmark8name,
						GuideActivity.baiduname);

			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}

	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendImage(intent); // Handle single image being sent
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendMultipleText(intent); // Handle text being sent
			} else if (type.startsWith("image/")) {
				handleSendMultipleImages(intent); // Handle multiple images
													// being sent
			}
		} else if (BtService.ACTION_PRINTCLIPBOARD.equals(action)) {
			handlePrintClipBoard();
		}

	}

	private void handleSendText(Intent intent) {
		Uri textUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (textUri != null) {
			// Update UI to reflect text being shared
			try {
				FileInputStream fis = new FileInputStream(textUri.getPath());
				InputStreamReader isr = new InputStreamReader(fis, "GBK");
				BufferedReader br = new BufferedReader(isr);
				String tmp = "";
				while ((tmp = br.readLine()) != null) {
					SetAndShow.printWithAllStyle(tmp);
				}
				Pos.POS_FeedLine();
				br.close();
				finish();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			// Update UI to reflect image being shared
			Bitmap mBitmap = BitmapFactory
					.decodeFile(getRealPathFromURI(imageUri));
			if (mBitmap != null)
				Pos.POS_PrintPicture(mBitmap, 384, 0);
			finish();
		}
	}

	private void handleSendMultipleText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		if (sharedText != null) {
			// Update UI to reflect text being shared
		}
	}

	private void handleSendMultipleImages(Intent intent) {
		ArrayList<Uri> imageUris = intent
				.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
		if (imageUris != null) {
			// Update UI to reflect multiple images being shared
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		CursorLoader loader = new CursorLoader(this, contentUri, proj, null,
				null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private void handlePrintClipBoard() {
		BtService.printClipBoard();
		finish();
	}

}
