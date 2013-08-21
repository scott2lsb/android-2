package com.example.updatev2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import btManager.Pos;
import btManager.UpdateThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Administrator 事有轻重缓急
 */
public class PicPrintActivity extends Activity implements OnClickListener,
		OnItemClickListener {
	private Button btConnect, btPic;
	private Button btSearch, btDisconnect;
	private TextView tv1, tv2, tv3;
	private TextView[] tv;
	private TextView tvInfo;
	private ProgressBar pbar;
	private LinearLayout llConnect, llOther, llDevices;
	private ListView lv1;
	private BluetoothAdapter myBTAdapter;
	private List<Map<String, Object>> listData;
	private static final String IMG = "IMG";
	private static final String TITLE = "TITLE";
	private static final String INFO = "INFO";
	private static final String PATH = "PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mypicprinter);
		llConnect = (LinearLayout) findViewById(R.id.linearLayout01);
		llOther = (LinearLayout) findViewById(R.id.linearLayout02);
		llDevices = (LinearLayout) findViewById(R.id.linearLayoutDevice);
		btConnect = (Button) findViewById(R.id.btconnect);
		btPic = (Button) findViewById(R.id.btpic);
		btSearch = (Button) findViewById(R.id.btsearch);
		btDisconnect = (Button) findViewById(R.id.btdisconnect);
		btConnect.setOnClickListener(this);
		btPic.setOnClickListener(this);
		btSearch.setOnClickListener(this);
		btDisconnect.setOnClickListener(this);
		tv1 = (TextView) findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
		tv = new TextView[] { tv1, tv2, tv3 };
		tvInfo = (TextView) findViewById(R.id.textViewInfo);
		pbar = (ProgressBar) findViewById(R.id.progressBar1);
		lv1 = (ListView) findViewById(R.id.listView1);
		listData = getMutiData();
		lv1.setAdapter(new SimpleAdapter(this, listData, R.layout.piclist,
				new String[] { IMG, TITLE, INFO }, new int[] { R.id.ivSmallImg,
						R.id.tvTitle, R.id.tvInfo }));
		lv1.setOnItemClickListener(this);

		Pos.APP_Init(getApplicationContext());
		// Pos.POS_BeginSavingFile(Environment.getExternalStorageDirectory()
		// + "/picprint.log");
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					Toast.makeText(getApplicationContext(), "搜索完毕",
							Toast.LENGTH_SHORT).show();
					if (!Pos.POS_Connecting()) {
						tvInfo.setText("");
						pbar.setVisibility(View.INVISIBLE);
						btSearch.setEnabled(true);

					}
				} else if (action
						.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
					llDevices.removeAllViews();
					tvInfo.setText("正在搜索");
					pbar.setVisibility(View.VISIBLE);
					btSearch.setEnabled(false);
				} else if (action
						.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {

				} else if (action.equals(Pos.ACTION_DISCONNECTED)) {
					Pos.POS_Close();
					Toast.makeText(getApplicationContext(), "连接断开",
							Toast.LENGTH_SHORT).show();
					tvInfo.setText("");
					tvInfo.setTextColor(Color.BLUE);
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(true);
					btDisconnect.setEnabled(false);
				} else if (action.equals(Pos.ACTION_CONNECTED)) {
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					tvInfo.setTextColor(Color.RED);
					tvInfo.setText("已经连接");
					pbar.setVisibility(View.VISIBLE);
					btSearch.setEnabled(false);
					btDisconnect.setEnabled(true);
				} else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					// 这里不该只是一个按钮，应该要更好看一点的视图
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
					llDevices.addView(button);
				} else if (action
						.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				} else if (action.equals(Pos.ACTION_BEGINCONNECTING)) {
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(false);
					btDisconnect.setEnabled(false);
					tvInfo.setText("正在连接");
					pbar.setVisibility(View.VISIBLE);
				} else if (action.equals(Pos.ACTION_CONNECTINGFAILED)) {
					Toast.makeText(getApplicationContext(), "连接失败",
							Toast.LENGTH_SHORT).show();
					tvInfo.setText("");
					pbar.setVisibility(View.INVISIBLE);
					btSearch.setEnabled(true);
				} else if (action.equals(UpdateThread.ACTION_DEBUGINFO)) {
					tvInfo.setText(intent
							.getStringExtra(UpdateThread.EXTRA_DEBUGINFO));
				} else if (action.equals(UpdateThread.ACTION_ENDUPDATE)) {

				}
			}

		};
		IntentFilter intentFilter = new IntentFilter(
				BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
		intentFilter.addAction(Pos.ACTION_DISCONNECTED);
		intentFilter.addAction(Pos.ACTION_CONNECTED);
		intentFilter.addAction(Pos.ACTION_BEGINCONNECTING);
		intentFilter.addAction(Pos.ACTION_CONNECTINGFAILED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(UpdateThread.ACTION_DEBUGINFO);
		intentFilter.addAction(UpdateThread.ACTION_ENDUPDATE);
		this.registerReceiver(broadcastReceiver, intentFilter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		myBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!myBTAdapter.isEnabled()) {
			myBTAdapter.enable();
		}
		if (myBTAdapter.isDiscovering()) {
			myBTAdapter.cancelDiscovery();
		}
		if (!Pos.Connecting && Pos.Connected) {
			btSearch.setEnabled(false);
			btDisconnect.setEnabled(true);

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 创建退出对话框
			AlertDialog isExit = new AlertDialog.Builder(this).create();
			// 设置对话框标题
			isExit.setTitle("系统提示");
			// 设置对话框消息
			isExit.setMessage("不需要打印图片了吗？");
			// 添加选择按钮并注册监听
			isExit.setButton(DialogInterface.BUTTON_POSITIVE, "退出", listener);
			isExit.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", listener);
			// 显示对话框
			isExit.show();

		}
		return false;

	}

	/** 监听对话框里面的button点击事件 */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:// "确认"按钮退出程序
				Pos.APP_UnInit();
				System.exit(0);
				break;
			case DialogInterface.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btconnect: {
			setBackgroundAndVisible(1);
			break;
		}
		case R.id.btpic: {
			setBackgroundAndVisible(2);
			break;
		}

		case R.id.btsearch: {
			if (myBTAdapter != null)
				if (!myBTAdapter.isDiscovering())
					myBTAdapter.startDiscovery();
			break;
		}

		case R.id.btdisconnect: {
			Pos.POS_Close();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		/**
		 * 这个函数好，可以直接得到点中的ID。
		 */
		if (Pos.POS_Connected()) {
			Pos.POS_StartListening();
			Pos.POS_PrintPic(listData.get(position).get(PATH).toString(), 384,
					0);
		}
	}

	/**
	 * 
	 * @param n1
	 *            1-5
	 */
	private void setBackgroundAndVisible(int n1) {
		for (int i = 0; i < tv.length; i++)
			tv[i].setBackgroundResource(R.drawable.fenge_normal);

		tv[n1 - 1].setBackgroundResource(R.drawable.fenge_selected);
		tv[n1].setBackgroundResource(R.drawable.fenge_selected);
		if (n1 == 1) {
			llConnect.setVisibility(View.VISIBLE);
			llOther.setVisibility(View.INVISIBLE);
		} else {
			llConnect.setVisibility(View.INVISIBLE);
			llOther.setVisibility(View.VISIBLE);
		}
	}

	private List<String> getData() {
		String[] extensions = new String[] { ".png", ".jpg", ".bmp" };
		List<String> listFile = new ArrayList<String>();
		File dir1 = new File(Environment.getExternalStorageDirectory()
				+ "/printer/");
		File dir2 = new File(Environment.getExternalStorageDirectory()
				+ "/DCIM/");
		File dir3 = new File(Environment.getExternalStorageDirectory()
				+ "/我听的歌/");
		listFile.addAll(new ListFiles().getFiles(dir1, extensions));
		listFile.addAll(new ListFiles().getFiles(dir2, extensions));
		listFile.addAll(new ListFiles().getFiles(dir3, extensions));

		return listFile;
	}

	private List<Map<String, Object>> getMutiData() {
		List<String> org = getData();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < org.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			String tempPath = org.get(i);
			map.put(PATH, tempPath);
			map.put(IMG, R.drawable.back_frame_icon_1);
			map.put(TITLE, tempPath.substring(tempPath
					.lastIndexOf(File.separatorChar)));
			map.put(INFO, "图片");
			list.add(map);
		}
		return list;
	}

}
