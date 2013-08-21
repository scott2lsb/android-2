package com.lvrenyang.printescheme;

import com.lvrenyang.R;
import com.lvrenyang.textandpictureactivitys.SetAndShow;

import btmanager.ClipboardUtils;
import btmanager.ConnectThread;
import btmanager.DataUtils;
import btmanager.NotifyUtils;
import btmanager.Pos;
import btmanager.ReadThread;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 
 * @author lvrenyang
 * 
 */
public class BtService extends Service {

	private NotifyUtils myNotify;
	private NotifyUtils myNotifyCustom;
	private PendingIntent resultPendingIntent;
	private static ClipboardUtils clip;

	private static boolean serviceIsRunning = false;
	private static Context serviceContext;
	// 表明所有的线程都已经准备完毕
	public static final String ACTION_SERVICEREADY = "ACTION_SERVICEREADY";
	public static final String ACTION_PRINTCLIPBOARD = "ACTION_PRINTCLIPBOARD";

	private BroadcastReceiver broadcastReceiver;

	public static boolean stopAutoConnect = false;

	@Override
	public void onCreate() {
		serviceIsRunning = true;
		serviceContext = this;
		/**
		 * 注册广播，等下测试一下在子线程发送广播能否收到
		 */
		initBroadcast();
		initOthers();
		Pos.APP_Init(this);
		Pos.check = false;
		Pos.POS_SetAutoPairing(true, "0000");

		// If we get killed, after returning from here, restart

		clip = new ClipboardUtils(this);

		// Notifications
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		resultPendingIntent = PendingIntent.getActivity(this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		myNotify = new NotifyUtils(this, resultPendingIntent);
		myNotify.showNotification(R.drawable.ic_launcher,
				getString(R.string.hello), "", "", "", 0, 0, false, true);
		myNotifyCustom = new NotifyUtils(this, resultPendingIntent,
				initRemoteViews());
		myNotifyCustom.showNotification(R.drawable.ic_launcher, true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		Pos.APP_UnInit();
		uninitBroadcast();
		myNotify.cancelNotification();
		myNotifyCustom.cancelNotification();
		if (OptionsActivity.getDebug())
			Toast.makeText(this, getString(R.string.servicestop),
					Toast.LENGTH_SHORT).show();
		serviceIsRunning = false;
	}

	/**
	 * 调试信息，都在BtService里面进行 别的广播接收者，如果需要对数据进行分析与重发，自己去实现即可
	 */
	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub

				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String address = "";
				String name = "";
				if (device != null) {
					address = device.getAddress();
					name = device.getName();
				}
				if (action.equals(ConnectThread.ACTION_CONNECTED)) {
					myNotify.showNotification(R.drawable.ic_launcher,
							getString(R.string.connected), name, address,
							getString(R.string.connected), 100, 100, false,
							true);
					// 连接成功，测试读取数据，如果开了调试，就能看到
				} else if (action.equals(ConnectThread.ACTION_DISCONNECTED)) {
					myNotify.showNotification(R.drawable.ic_launcher,
							getString(R.string.disconnected), name, address,
							"", 0, 0, false, true);
				} else if (action.equals(ConnectThread.ACTION_STARTCONNECTING)) {
					myNotify.showNotification(R.drawable.ic_launcher,
							getString(R.string.startconnecting), name, address,
							"", 0, 0, true, true);
				} else if (action.equals(ConnectThread.ACTION_WAITCONNECTING)) {
					myNotify.showNotification(R.drawable.ic_launcher,
							getString(R.string.waitconnecting), name, address,
							"", 0, 0, true, true);
				} else if (action.equals(ReadThread.ACTION_READTHREADRECEIVE)) {
					byte rc = intent.getByteExtra(
							ReadThread.EXTRA_READTHREADRECEIVEBYTE, (byte) 0);
					String tmp = getString(R.string.device) + ": " + name
							+ "\n" + getString(R.string.address) + ": "
							+ address + "\n" + getString(R.string.receive)
							+ ": \n" + DataUtils.byteToStr(rc);
					Toast.makeText(BtService.this, tmp, Toast.LENGTH_SHORT)
							.show();
				} else if (action.equals(ReadThread.ACTION_READTHREADRECEIVES)) {
					if (OptionsActivity.getDebug()) {
						boolean recstatus = intent.getBooleanExtra(
								ReadThread.EXTRA_READTHREADRECEIVECORRECT,
								false);
						if (recstatus) {
							byte[] rcs = intent
									.getByteArrayExtra(ReadThread.EXTRA_READTHREADRECEIVEBYTES);
							String tmp = getString(R.string.device) + ": "
									+ name + "\n" + getString(R.string.address)
									+ ": " + address + "\n"
									+ getString(R.string.receive) + ": \n"
									+ DataUtils.bytesToStr(rcs);
							Toast.makeText(BtService.this, tmp,
									Toast.LENGTH_SHORT).show();
						} else {
							String tmp = getString(R.string.device) + ": "
									+ name + "\n" + getString(R.string.address)
									+ ": " + address + "\n"
									+ getString(R.string.receive) + ": \n"
									+ getString(R.string.failed);
							Toast.makeText(BtService.this, tmp,
									Toast.LENGTH_SHORT).show();
						}
					}
				} else if (action
						.equals(ReadThread.ACTION_READTHREADRECEIVERESPOND)) {
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] pcmdpackage = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDPACKAGE);
					int cmd = intent.getIntExtra(ReadThread.EXTRA_PCMDCMD, 0);
					int para = intent.getIntExtra(ReadThread.EXTRA_PCMDPARA, 0);
					int length = intent.getIntExtra(
							ReadThread.EXTRA_PCMDLENGTH, 0);
					byte[] rcs = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDDATA);

					if (OptionsActivity.getDebug()) {
						String tmp1 = getString(R.string.device) + ": " + name
								+ "\n" + getString(R.string.address) + ": "
								+ address + "\n";

						String tmp2 = getString(R.string.send) + ":\n"
								+ DataUtils.bytesToStr(pcmdpackage);
						if (recstatus)
							tmp2 += "\n" + getString(R.string.getback) + ":\n";

						String tmp3 = getString(R.string.command) + ": 0x"
								+ Integer.toHexString(cmd) + "\n"
								+ getString(R.string.param) + ": 0x"
								+ Integer.toHexString(para) + "\n"
								+ getString(R.string.datalength) + ": 0x"
								+ Integer.toHexString(length);
						if (length > 0) {
							tmp3 += "\n" + getString(R.string.receive) + ": \n"
									+ DataUtils.bytesToStr(rcs);
						}
						Toast.makeText(BtService.this, tmp1 + tmp2 + tmp3,
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectThread.ACTION_CONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_DISCONNECTED);
		intentFilter.addAction(ConnectThread.ACTION_STARTCONNECTING);
		intentFilter.addAction(ConnectThread.ACTION_WAITCONNECTING);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVE);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVES);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVERESPOND);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private void initOthers() {
		new DaemonThread().start();
	}

	public static boolean isRunning() {
		return serviceIsRunning;
	}

	public static Context getServiceContext() {
		return serviceContext;
	}

	private RemoteViews initRemoteViews() {
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.notification);
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(ACTION_PRINTCLIPBOARD);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		views.setOnClickPendingIntent(R.id.buttonPrintClipboard, pendingIntent);
		return views;
	}

	public static void printClipBoard() {
		String tmp = clip.getText();
		if (!"".equals(tmp)) {
			SetAndShow.printWithAllStyle(tmp);
			Pos.POS_FeedLine();
		}
	}

}
