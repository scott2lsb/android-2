package btmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 
 * @author 当用户请求连接时，先判断当前是否已经连上 如果已经连上，判断已经连上的设备是不是正在连接的设备 如果没连上，进行连接。
 *         考虑到连接是线性的，所以不用考虑正在连接的问题
 */
public class ConnectThread extends Thread {

	public static Handler connectHandler = null;

	/**
	 * command that handles, also use for indicate state
	 */
	public static final int WHAT_QUIT = 1000;
	public static final int WHAT_CONNECTASCLIENT = 1001;
	public static final int WHAT_CONNECTASSERVICE = 1002;
	public static final int WHAT_DISCONNECT = 1003;

	public static final int WHAT_NONE = 999;
	public static final int WHAT_NOIGNORE = 998;

	private static int WHAT_STATE = WHAT_NONE;
	private static int WHAT_IGNORE = WHAT_NOIGNORE;

	/**
	 * these field indecate the connect status
	 */
	public static final String ACTION_CONNECTED = "ACTION_CONNECTED";
	public static final String ACTION_DISCONNECTED = "ACTION_DISCONNECTED";
	public static final String ACTION_STARTCONNECTING = "ACTION_STARTCONNECTING";
	public static final String ACTION_WAITCONNECTING = "ACTION_WAITCONNECTING";

	private static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static BluetoothSocket mmSocket;
	private static BluetoothServerSocket mmServerSocket;

	// 已经连接的设备mac，如果没有没有连上或正在连接，则为""
	private static String connectedMac = "";

	// 正在进行连接的设备mac，如果没有连上或已经连上，则为""
	private static String connectingMac = "";

	private static Context context;
	private static BroadcastReceiver broadcastReceiver;
	public static boolean autoPairing = false;
	public static String pairingCode = "0000";

	private static final int acceptTimeout = 3600000;

	public ConnectThread(Context context) {
		ConnectThread.context = context;
		if (context != null)
			initBroadcast();
	}

	@Override
	public void run() {
		Looper.prepare();
		connectHandler = new ConnectHandler();
		Looper.loop();
	}

	private static class ConnectHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			// 如果当前操作被忽略，直接返回
			if (getIgnoreState() == msg.what)
				return;

			// 设置whatstate以表明线程正在执行什么任务
			setWhatState(msg.what);

			switch (msg.what) {
			case WHAT_QUIT: {
				context.unregisterReceiver(broadcastReceiver);
				cancel();
				Looper.myLooper().quit();
				break;
			}

			/**
			 * 作为客户端连接
			 */
			case WHAT_CONNECTASCLIENT: {
				String address = (String) msg.obj;
				if (!isConnected()) {
					// 没有连接
					setConnectingDevice(address);
					connectAsClient(address);
				} else {
					// 已经连接
					if (address.equals(getConnectedDevice())) {
						// 如果尝试对已经连接的设备进行连接 直接发送连接建立的广播
						Intent intent = new Intent();
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
						sendBroadcast(intent.setAction(ACTION_CONNECTED));
					} else {
						// 连接另一台设备
						cancel();
						setConnectingDevice(address);
						connectAsClient(address);
					}
				}
				setConnectingDevice("");
				break;
			}

			/**
			 * 作为服务器连接
			 */
			case WHAT_CONNECTASSERVICE: {
				String address = (String) msg.obj;
				if (!isConnected()) {
					setConnectingDevice(address);
					connectAsServer(address, acceptTimeout);
				} else {
					// 已经连接
					if (address.equals(getConnectedDevice())) {
						// 如果尝试对已经连接的设备进行连接 直接发送连接建立的广播
						Intent intent = new Intent();
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
						sendBroadcast(intent.setAction(ACTION_CONNECTED));
					} else {
						cancel();
						setConnectingDevice(address);
						connectAsServer(address, acceptTimeout);
					}
				}
				setConnectingDevice("");
				break;
			}

			case WHAT_DISCONNECT: {
				cancel();

				break;
			}
			}
			setWhatState(WHAT_NONE);
		}
	}

	private static boolean connectAsClient(String address) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter == null)
			return false;

		Intent intent = new Intent();
		intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
				bluetoothAdapter.getRemoteDevice(address));
		sendBroadcast(intent.setAction(ACTION_STARTCONNECTING));

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

		try {
			mmSocket = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
		}

		bluetoothAdapter.cancelDiscovery();
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mmSocket.connect();
			sendBroadcast(intent.setAction(ACTION_CONNECTED));
			setConnectedDevice(address);
			return true;
		} catch (IOException connectException) {
			cancelSocket();
			sendBroadcast(intent.setAction(ACTION_DISCONNECTED));
			setConnectedDevice("");
			return false;
		}
	}

	// 如果开启了服务，需要手动调用cancel结束这个connect
	private static boolean connectAsServer(String address, int timeout) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (bluetoothAdapter == null)
			return false;

		Intent intent = new Intent();
		intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
				bluetoothAdapter.getRemoteDevice(address));
		sendBroadcast(intent.setAction(ACTION_WAITCONNECTING));

		try {
			while (true) {
				mmServerSocket = bluetoothAdapter
						.listenUsingRfcommWithServiceRecord(
								"Android Bluetooth Printer", uuid);

				mmSocket = mmServerSocket.accept(timeout);

				// 如果是我们需要连接的设备，就返回，否则，continue
				if (address.equals(mmSocket.getRemoteDevice().getAddress())) {
					cancelServerSocket();
					sendBroadcast(intent.setAction(ACTION_CONNECTED));
					setConnectedDevice(address);
					return true;
				} else {
					cancel();
					continue;
				}
			}
		} catch (IOException e) {
			cancel();
			sendBroadcast(intent.setAction(ACTION_DISCONNECTED));
			setConnectedDevice("");
			return false;
		}

	}

	static void cancel() {

		if (mmSocket != null) {
			if (isConnected())
				sendBroadcast(new Intent(ACTION_DISCONNECTED));
			cancelSocket();
		}

		if (mmServerSocket != null) {
			if (isConnected())
				sendBroadcast(new Intent(ACTION_DISCONNECTED));
			cancelServerSocket();
		}

		setConnectedDevice("");
		setConnectingDevice("");
	}

	private static void cancelServerSocket() {
		try {
			mmServerSocket.close();
		} catch (IOException e) {
		}
	}

	private static void cancelSocket() {
		try {
			mmSocket.close();
		} catch (IOException e) {
		}
	}

	private static void sendBroadcast(Intent intent) {
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}

	private static void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
					if (device == null)
						return;
					if (device.getAddress().equals(getConnectedDevice())) {
						// 远端设备断开连接，我们这边也要更新状态了。
						cancel();
						sendBroadcast(intent.setAction(ACTION_DISCONNECTED));
					}
				} else if (action
						.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
					if (autoPairing) {
						if (device == null)
							return;
						try {
							ClsUtils.setPin(BluetoothDevice.class, device,
									pairingCode);
							ClsUtils.cancelBondProcess(BluetoothDevice.class,
									device);
						} catch (Exception e) {
						}

					}
				}
			}

		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		intentFilter
				.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		context.registerReceiver(broadcastReceiver, intentFilter);
	}

	/**
	 * 指示我们的线程正在执行什么任务
	 * 
	 * @param state
	 */
	private static void setWhatState(int state) {
		WHAT_STATE = state;
	}

	public static int getWhatState() {
		return WHAT_STATE;
	}

	public static void setIgnoreState(int state) {
		WHAT_IGNORE = state;
	}

	public static int getIgnoreState() {
		return WHAT_IGNORE;
	}

	public static boolean isConnected() {
		return !connectedMac.equals("");
	}

	public static boolean isConnecting() {
		return !connectingMac.equals("");
	}

	public static boolean isServerConnecting() {
		return isConnecting() && (getWhatState() == WHAT_CONNECTASSERVICE);
	}

	public static boolean isClientConnecting() {
		return isConnecting() && (getWhatState() == WHAT_CONNECTASCLIENT);
	}

	public static String getConnectedDevice() {
		return connectedMac;
	}

	private static void setConnectedDevice(String device) {
		connectedMac = device;
	}

	public static String getConnectingDevice() {
		return connectingMac;
	}

	private static void setConnectingDevice(String device) {
		connectingMac = device;
	}

	public static OutputStream getOutputStream() {
		if (mmSocket != null) {
			try {
				return mmSocket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static InputStream getInputStream() {
		if (mmSocket != null) {
			try {
				return mmSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
