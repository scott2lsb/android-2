package btManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

/**
 * 
 * @author Administrator 连接线程：这次就不弄那么多循环和啥七七八八的looper了。
 *         连接，就等收到广播后，用getInputStream和getOutputStream来获取输入输出流
 *         连接的时候，也不要捕获异常了，毕竟如果收到了广播，就算有异常，也算是连上了。 能进来的都是Connected=false的
 * 
 */
public class ConnectThread extends Thread {

	private UUID uuid2 = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private UUID uuid4 = UUID
	// .fromString("0000fff0-0000-1000-8000-00805F9B34FB");
	private UUID uuid = uuid2;
	private BluetoothAdapter myBTAdapter = BluetoothAdapter.getDefaultAdapter();
	private String btAddress;

	public ConnectThread(String btAddress) {
		this.btAddress = btAddress;
	}

	@Override
	public void run() {

		Pos.mContext.sendBroadcast(new Intent(Pos.ACTION_BEGINCONNECTING));

		if (myBTAdapter.isDiscovering())
			myBTAdapter.cancelDiscovery();

		BluetoothDevice rmDevice = myBTAdapter.getRemoteDevice(btAddress);
		try {
			Pos.cSocket = rmDevice.createRfcommSocketToServiceRecord(uuid);
			// Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
			// String tmp = "uuids: \n";
			// for (int i = 0; i < rmDevice.getUuids().length; i++)
			// tmp += (rmDevice.getUuids())[i].toString() + "\n";
			// intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, tmp);
			// Pos.mContext.sendBroadcast(intent);
		} catch (IOException e) {
			Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
			intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
					"createRfcommSocket ERROR!");
			Pos.mContext.sendBroadcast(intent);
			Pos.POS_Close();
		}

		if (Pos.cSocket != null) {
			try {
				Pos.cSocket.connect();
			} catch (IOException e1) {
				switch (rmDevice.getBondState()) {
				case BluetoothDevice.BOND_NONE:
				case BluetoothDevice.BOND_BONDED:
					Pos.POS_Close();
					break;
				default:
					// Pos.POS_Close();
					break;
				}
			}
		}

		if (Pos.cSocket != null) {
			try {
				Pos.os = Pos.cSocket.getOutputStream();
				Pos.bis = new BufferedInputStream(Pos.cSocket.getInputStream());

				/**
				 * 判断一下，是否正确建立连接了连接
				 */
				Pos.Connected = true;
				Pos.mContext.sendBroadcast(new Intent(
						Pos.ACTION_CONNECTEDUNTEST));
			} catch (IOException e) {
				Pos.Connected = false;
				Pos.POS_Close();
				Pos.mContext.sendBroadcast(new Intent(
						Pos.ACTION_CONNECTINGFAILED));
			}
		} else {
			Pos.mContext.sendBroadcast(new Intent(Pos.ACTION_CONNECTINGFAILED));
		}

		Pos.Connecting = false;
	}
}
