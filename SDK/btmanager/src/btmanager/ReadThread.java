package btmanager;

import java.io.IOException;
import java.io.InputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

// 读去线程一直在读，只是不同情况下数据解析格式不同
// 要某通过另外一个线程，来循环判断读取是否成功，并做相应处理
// 要某通过一个广播，把结果播出去
public class ReadThread extends Thread {

	public static Handler readHandler = null;
	private static Context context;
	private static BroadcastReceiver broadcastReceiver;

	public static final int WHAT_QUIT = 1000;
	public static final int WHAT_READ = 100000;

	public static final String ACTION_READTHREADRECEIVE = "ACTION_READTHREADRECEIVE";
	public static final String EXTRA_READTHREADRECEIVEBYTE = "EXTRA_READTHREADRECEIVEBYTE";

	public static final String ACTION_READTHREADRECEIVES = "ACTION_READTHREADRECEIVES";
	public static final String EXTRA_READTHREADRECEIVEBYTES = "EXTRA_READTHREADRECEIVEBYTES";

	public static final String ACTION_READTHREADRECEIVERESPOND = "ACTION_READTHREADRECEIVERESPOND";
	public static final String EXTRA_PCMDCMD = "EXTRA_PCMDCMD";
	public static final String EXTRA_PCMDPARA = "EXTRA_PCMDPARA";
	public static final String EXTRA_PCMDLENGTH = "EXTRA_PCMDLENGTH";
	public static final String EXTRA_PCMDDATA = "EXTRA_PCMDDATA";

	// EXTRA_PCMDPACKAGE 指发送出去的命令包
	public static final String EXTRA_PCMDPACKAGE = "EXTRA_PCMDPACKAGE";
	public static final String EXTRA_READTHREADRECEIVECORRECT = "EXTRA_READTHREADRECEIVECORRECT";

	// ANALYSISMODE_NONE 指定解析格式为无，只是接收给定长度
	private static final int ANALYSISMODE_LENGTH = 100001;
	// ANALYSISMODE_RECAUOT 指定解析格式为串口通讯协议，长度通过协议确定
	private static final int ANALYSISMODE_RECAUOT = 100002;
	// 当前的ANALYSISMODE, 读取的时候，根据当前的模式调用相应的函数
	private static int ANALYSISMODE = ANALYSISMODE_LENGTH;

	private static final int LBUF_MAXSIZE = 1024;

	// 只要不quit， 就永不退出
	//
	private static boolean needread = false;
	private static boolean reading = false;

	private static boolean debug = false;

	// checkup当收到连接建立的广播时，就会进行checkup
	// 如果check正确，就没问题，check不正确，就将checkup置false;
	private static boolean checkup = true;
	private static int checkuptimeout = 500;
	private static int checkupretrytimes = 3;
	private static int checkupcount = 0;
	private static byte[] defaultkey = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36,
			0x37, 0x38 };
	private static byte[] randomdata;

	public ReadThread(Context context) {
		ReadThread.context = context;
		initBroadcast();
	}

	@Override
	public void run() {
		Looper.prepare();
		readHandler = new ReadHandler();
		Looper.loop();
	}

	private static class ReadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			/**
			 * 首先读去数据，读到数据之后，发送出去 该方法进入之后，就永远不会退出
			 */
			case WHAT_READ: {
				setReading(true);

				while (getNeedReadFlag()) {
					int rec = readOne();
					if (getDebug() && (rec != -1)) {
						Intent intent = new Intent(ACTION_READTHREADRECEIVE);
						intent.putExtra(EXTRA_READTHREADRECEIVEBYTE, (byte) rec);
						intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, true);
						String address = ConnectThread.getConnectedDevice();
						if (address != null) {
							if (BluetoothAdapter.checkBluetoothAddress(address))
								intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
										BluetoothAdapter.getDefaultAdapter()
												.getRemoteDevice(address));
						}
						sendBroadcast(intent);
					}
					analysisData(rec);
				}

				setReading(false);
				break;
			}

			case WHAT_QUIT:
				uninitBroadcast();
				Looper.myLooper().quit();
				break;

			default:
				break;
			}

		}
	}

	// 指定接收长度
	// 初始化相应数据段
	static void Request(byte[] data, int length, int timeout) {
		ANALYSISMODE = ANALYSISMODE_LENGTH;
		lbuf.constructtime = System.currentTimeMillis();
		lbuf.timeout = timeout;
		lbuf.length = length;
		lbuf.buf_count = 0;
		lbuf.cmd = data;
		lbuf.requested = false;
		lbuf.lbufHandlerStart();
	}

	// 返回数据自动识别
	// 调用这个函数的线程和read线程并不是同路，
	// 在这里设置，在readThread里生效
	static void Request(byte[] data, int timeout) {
		ANALYSISMODE = ANALYSISMODE_RECAUOT;
		rcbuf.constructtime = System.currentTimeMillis();
		rcbuf.timeout = timeout;
		rcbuf.cmd = data;
		rcbuf.cpk.clean();
		rcbuf.requested = false;
		rcbuf.rcbufHandlerStart();
	}

	private static int getAnalysisMode() {
		return ANALYSISMODE;
	}

	static void setNeedReadFlag(boolean readornot) {
		needread = readornot;
	}

	static boolean getNeedReadFlag() {
		return needread;
	}

	static void setDebug(boolean debugornot) {
		debug = debugornot;
	}

	static boolean getDebug() {
		return debug;
	}

	static boolean isReading() {
		return reading;
	}

	static void setReading(boolean readingornot) {
		reading = readingornot;
	}

	// 果然广播就是要在里面发送，就像connectthread一样，在连接的时候发送
	private static void analysisData(int rec) {

		switch (getAnalysisMode()) {

		case ANALYSISMODE_LENGTH: {

			// 满足了这两方面，说明命令已经处理完毕，再收到数据可以忽略
			if (!lbuf.lbufHandlerEnd()) {
				if (!lbuf.requested) {
					Pos.POS_Write(lbuf.cmd);
					lbuf.requested = true;
					break;
				}
				lbuf.lbufHandler(rec);
			}

			break;
		}

		case ANALYSISMODE_RECAUOT: {

			if (!rcbuf.rcbufHandlerEnd()) {
				if (!rcbuf.requested) {
					Pos.POS_Write(rcbuf.cmd);
					rcbuf.requested = true;
					break;
				}
				rcbuf.rcbufHandler(rec);
			}
			break;
		}

		}
	}

	// no block
	private static int readOne() {
		if (!ConnectThread.isConnected())
			return -1;
		InputStream is = ConnectThread.getInputStream();
		try {
			if (is != null) {

				if (is.available() > 0)
					return is.read();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (is != null)
				try {
					is.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
		return -1;
	}

	private static void sendBroadcast(Intent intent) {
		if (context != null)
			context.sendBroadcast(intent);
	}

	// ANALYSISMODE_REQUEST缓冲区数据结构
	private static class lbuf {
		private static boolean requested = true;

		private static final int handleSuccess = 1;
		private static final int handleOngoing = 2;
		private static final int handleFailed = 3;
		public static int handleStatus = handleSuccess;

		public static byte[] cmd;
		public static byte[] buf = new byte[LBUF_MAXSIZE];
		// 需要返回的数据的长度
		public static int length = 0;
		public static int buf_count = 0;
		public static long constructtime = 0;
		public static int timeout = 0;

		public static void lbufHandler(int rec) {
			if ((System.currentTimeMillis() - lbuf.constructtime > lbuf.timeout)) {
				handleStatus = handleFailed;
				Intent intent = new Intent(ACTION_READTHREADRECEIVES);
				intent.putExtra(EXTRA_PCMDPACKAGE, cmd);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_READTHREADRECEIVEBYTES, new byte[0]);
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, false);
				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				sendBroadcast(intent);
				return;
			}

			if (rec != -1)
				buf[buf_count++] = (byte) rec;

			if (lbuf.buf_count == lbuf.length) {
				handleStatus = handleSuccess;
				Intent intent = new Intent(ACTION_READTHREADRECEIVES);
				intent.putExtra(EXTRA_PCMDPACKAGE, cmd);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_READTHREADRECEIVEBYTES,
						DataUtils.getSubBytes(buf, 0, length));
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, true);
				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				sendBroadcast(intent);
				return;
			}
		}

		public static boolean lbufHandlerEnd() {
			return handleStatus != handleOngoing;
		}

		public static void lbufHandlerStart() {
			handleStatus = handleOngoing;
		}

	}

	// UpdateThread用到
	public static boolean lbufhandlerSuccess() {
		return lbuf.handleStatus == lbuf.handleSuccess;
	}

	// ANALYSISMODE_REQUESTCOMMAND缓冲区数据结构
	private static class rcbuf {

		// 是否发送了请求，一般是先清空缓冲区，然后再发送数据，才将该变量置真
		private static boolean requested = true;

		private static final int handleSuccess = 1;
		private static final int handleOngoing = 2;
		private static final int handleFailed = 3;
		public static int handleStatus = handleSuccess;

		// 任务开始时间
		public static long constructtime = 0;
		// 超时
		public static int timeout = 0;

		// 需要发送的命令
		public static byte[] cmd;

		private static class cpk {

			private static final byte PACKAGESTART = 0x03;
			private static final byte PACKAGEDIRIN = (byte) 0xFE;

			// 如果开始收到0x03，表明started
			private static byte started = 0;

			// 收到0xfe表明direction in
			private static byte direction = 0;

			// 命令
			private static byte[] cmd = new byte[2];
			private static int cmd_bslen = 0;

			// 参数
			private static byte[] para = new byte[4];
			private static int para_bslen = 0;

			// 需要返回的数据的长度
			private static volatile byte[] length = new byte[2];
			private static volatile int length_bslen = 0;

			// 应该在全部接收完毕再来判断是否成功
			private static byte checkSumH = 0;
			private static boolean cshReceived = false;

			private static byte checkSumD = 0;
			private static boolean csdReceived = false;

			// 接收的命令的数据缓冲区
			private static byte[] buf = new byte[0];
			private static int buf_bslen = 0;

			public static void clean() {
				started = 0;
				direction = 0;
				cmd_bslen = 0;
				para_bslen = 0;
				length_bslen = 0;
				buf_bslen = 0;
				cshReceived = false;
				csdReceived = false;
			}

			public static byte[] getCmdArray() {
				return DataUtils.byteArraysToBytes(new byte[][] {
						{ started, direction }, cmd, para, length,
						{ checkSumH, checkSumD } });
			}

			public static boolean check() {
				byte[] cmd = getCmdArray();
				return (cmd[10] == DataUtils.bytesToXor(cmd, 0, 10))
						&& (cmd[11] == DataUtils.bytesToXor(buf, 0, buf.length));
			}

			public static void sendCmdBroadcast(boolean extraCorrect) {
				Intent intent = new Intent(ACTION_READTHREADRECEIVERESPOND);
				int cmd;
				int para;
				int length;
				byte[] data = new byte[0];

				// 原命令包
				intent.putExtra(EXTRA_PCMDPACKAGE, rcbuf.cmd);
				if (extraCorrect) {
					cmd = (cpk.cmd[0] & 0xff) + ((cpk.cmd[1] & 0xff) << 8);
					para = (cpk.para[0] & 0xff) + ((cpk.para[1] & 0xff) << 8)
							+ ((cpk.para[2] & 0xff) << 16)
							+ ((cpk.para[3] & 0xff) << 24);
					length = (cpk.length[0] & 0xff)
							+ ((cpk.length[1] & 0xff) << 8);
					data = DataUtils.cloneBytes(buf);
				} else {
					// 命令出错，会将原命令返回

					cmd = (rcbuf.cmd[2] & 0xff) + ((rcbuf.cmd[3] & 0xff) << 8);
					para = (rcbuf.cmd[4] & 0xff) + ((rcbuf.cmd[5] & 0xff) << 8)
							+ ((rcbuf.cmd[6] & 0xff) << 16)
							+ ((rcbuf.cmd[7] & 0xff) << 24);
					length = (rcbuf.cmd[8] & 0xff)
							+ ((rcbuf.cmd[9] & 0xff) << 8);
					data = DataUtils.getSubBytes(rcbuf.cmd, 12,
							rcbuf.cmd.length - 12);
				}

				intent.putExtra(EXTRA_PCMDCMD, cmd);
				intent.putExtra(EXTRA_PCMDPARA, para);
				intent.putExtra(EXTRA_PCMDLENGTH, length);
				intent.putExtra(EXTRA_PCMDDATA, data);
				intent.putExtra(EXTRA_READTHREADRECEIVECORRECT, extraCorrect);

				String address = ConnectThread.getConnectedDevice();
				if (address != null) {
					if (BluetoothAdapter.checkBluetoothAddress(address))
						intent.putExtra(BluetoothDevice.EXTRA_DEVICE,
								BluetoothAdapter.getDefaultAdapter()
										.getRemoteDevice(address));
				}
				ReadThread.sendBroadcast(intent);
			}

		}

		public static void rcbufHandler(int nrec) {
			if ((System.currentTimeMillis() - rcbuf.constructtime > rcbuf.timeout)) {
				handleStatus = handleFailed;
				cpk.sendCmdBroadcast(false);
				return;
			}

			if (nrec == -1)
				return;

			byte rec = (byte) nrec;

			if (cpk.started != cpk.PACKAGESTART) {
				if (rec == cpk.PACKAGESTART)
					cpk.started = rec;
				else
					cpk.clean();
				return;

			}

			if (cpk.direction != cpk.PACKAGEDIRIN) {
				if (rec == cpk.PACKAGEDIRIN)
					cpk.direction = rec;
				else
					cpk.clean();
				return;
			}

			// 当收到第一个字节命令时，cmd_bslen = 0, < 2
			// 于是保存，然后cmd_bslen ++,
			// 当收到第二个字节命令时，cmd_bslen = 1, < 2
			// 于是保存，然后cmd_bslen ++, 此时cmd_bslen = 2，
			// 已经不满足这个条件了，于是循环没有问题
			if (cpk.cmd_bslen < cpk.cmd.length) {
				cpk.cmd[cpk.cmd_bslen++] = rec;
				return;
			}

			if (cpk.para_bslen < cpk.para.length) {
				cpk.para[cpk.para_bslen++] = rec;
				return;
			}

			if (cpk.length_bslen < cpk.length.length) {
				cpk.length[cpk.length_bslen++] = rec;
				return;
			}

			if (!cpk.cshReceived) {
				cpk.checkSumH = rec;
				cpk.cshReceived = true;
				return;
			}

			if (!cpk.csdReceived) {
				cpk.checkSumD = rec;
				cpk.csdReceived = true;
				return;
			}

			if (cpk.buf_bslen == 0) {
				// buf_bslen == 0
				cpk.buf = new byte[(cpk.length[0] & 0xff)
						+ ((cpk.length[1] & 0xff) << 8)];

				// 是还没有初始化还是已经结束了呢，
				// 说明还没有初始化，现在初始化
				if (cpk.buf.length == 0) {
					// 已经结束了命令是否正确，这里判断，只需返回数据即可，如果没有就返回new byte[0]
					handleStatus = handleSuccess;
					cpk.sendCmdBroadcast(cpk.check());
					return;
				} else {
					cpk.buf[cpk.buf_bslen++] = rec;
					return;
				}
			} else {
				// 已经初始化，判断是否结束
				if (cpk.buf_bslen < cpk.buf.length) {
					cpk.buf[cpk.buf_bslen++] = rec;

					if (cpk.buf_bslen == cpk.buf.length) {
						handleStatus = handleSuccess;
						cpk.sendCmdBroadcast(cpk.check());
						return;
					} else {
						return;
					}

				}

			}
		}

		public static boolean rcbufHandlerEnd() {
			return handleStatus != handleOngoing;
		}

		public static void rcbufHandlerStart() {
			handleStatus = handleOngoing;
		}
	}

	public static boolean getCheckUp() {
		return checkup;
	}

	public static byte[] getKey() {
		return defaultkey;
	}

	public static void setKey(byte[] newkey) {
		for (int i = 0; i < defaultkey.length; i++)
			defaultkey[i] = newkey[i];
	}

	private static void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (ConnectThread.ACTION_CONNECTED.equals(action)) {
					// 连接建立，发送指令checkup
					randomdata = DataUtils.getRandomByteArray(8);
					byte blengthl = (byte) (randomdata.length % 0x100);
					byte blengthh = (byte) (randomdata.length / 0x100);
					byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
							Cmd.ESCCmd.DES_ENCRYPT, { blengthl, blengthh },
							randomdata });
					Request(data, data.length, checkuptimeout);
				} else if (ACTION_READTHREADRECEIVES.equals(action)) {
					// 收到回复，进行校验
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] cmd = intent.getByteArrayExtra(EXTRA_PCMDPACKAGE);
					int length = intent.getIntExtra(EXTRA_PCMDLENGTH,
							Integer.MAX_VALUE);
					if (cmd == null)
						return;
					if ((cmd.length > 3)
							&& (cmd[0] == Cmd.ESCCmd.DES_ENCRYPT[0])
							&& (cmd[1] == Cmd.ESCCmd.DES_ENCRYPT[1])
							&& (cmd[2] == Cmd.ESCCmd.DES_ENCRYPT[2])) {
						if (recstatus) {
							checkupcount = 0;
							byte[] rcs = intent
									.getByteArrayExtra(ReadThread.EXTRA_READTHREADRECEIVEBYTES);
							byte[] encrypted = DataUtils.getSubBytes(rcs, 5,
									rcs.length - 5);
							/**
							 * 对数据进行解密
							 */
							DES2 des2 = new DES2();
							// 初始化密钥
							des2.yxyDES2_InitializeKey(defaultkey);
							des2.yxyDES2_DecryptData(encrypted);
							byte[] decodeData = des2.getPlaintext();

							if (DataUtils.bytesEquals(randomdata, decodeData)) {
								checkup = true;
							} else {
								checkup = false;
							}
						} else {
							checkupcount++;
							if (checkupcount < checkupretrytimes)
								Request(cmd, length, checkuptimeout);
							else
								checkup = false;
						}
						if (getDebug())
							Toast.makeText(context, "" + checkup,
									Toast.LENGTH_SHORT).show();
					}
				}
			}

		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectThread.ACTION_CONNECTED);
		intentFilter.addAction(ACTION_READTHREADRECEIVES);
		intentFilter
				.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		context.registerReceiver(broadcastReceiver, intentFilter);
	}

	private static void uninitBroadcast() {
		if (context != null)
			context.unregisterReceiver(broadcastReceiver);
	}
}
