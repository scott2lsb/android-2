package btmanager;

import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class WriteThread extends Thread {
	// 该线程一直存在
	public static Handler writeHandler = null;
	public static final int WHAT_WRITE = 10000;
	public static final int WHAT_WRITEUNTIL = 10001;
	public static final int WHAT_QUIT = 1000;

	// broadcast
	public static final String ACTION_WRITESUCCESS = "ACTION_WRITESUCCESS";
	public static final String ACTION_WRITEFAILED = "ACTION_WRITEFAILED";

	public static final String EXTRA_WRITEJOBID = "EXTRA_WRITEJOBID";
	private static final int perPackageSize = 1536;
	private static final int perPackageWaitTime = 120;
	private static Context context;

	public WriteThread(Context context) {
		WriteThread.context = context;
	}

	@Override
	public void run() {
		Looper.prepare();
		writeHandler = new WriteHandler();
		Looper.loop();
	}

	private static class WriteHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_WRITE: {

				byte[] data = (byte[]) msg.obj;
				int jobId = msg.arg1;

				Intent intent = new Intent();
				intent.putExtra(EXTRA_WRITEJOBID, jobId);

				if (writeBig(data)) {
					sendBroadcast(intent.setAction(ACTION_WRITESUCCESS));
				} else {
					sendBroadcast(intent.setAction(ACTION_WRITEFAILED));
				}

				break;
			}

			case WHAT_WRITEUNTIL: {

				byte[] data = (byte[]) msg.obj;
				int jobId = msg.arg1;
				int jobTimeout = msg.arg2;

				Intent intent = new Intent();
				intent.putExtra(EXTRA_WRITEJOBID, jobId);

				if (writeUntil(data, jobTimeout)) {
					sendBroadcast(intent.setAction(ACTION_WRITESUCCESS));
				} else {
					sendBroadcast(intent.setAction(ACTION_WRITEFAILED));
				}

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

	private static boolean write(byte[] data) {
		if (!Pos.POS_isConnected())
			return false;
		OutputStream os = ConnectThread.getOutputStream();
		try {
			if (os != null) {
				os.write(data);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (os != null)
				try {
					os.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		}
		return false;
	}

	private static boolean writeBig(byte[] data) {
		if (data.length <= perPackageSize) {
			return write(data);
		}

		int count = data.length;
		int offset = 0;

		do {
			if (!Pos.POS_isConnected())
				return false;
			OutputStream os = ConnectThread.getOutputStream();
			try {
				if (os != null) {
					if (count < perPackageSize) {
						os.write(data, offset, count);
						return true;
					}
					os.write(data, offset, perPackageSize);
					TimeUtils.waitTime(perPackageWaitTime);
				} else {
					return false;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (os != null)
					try {
						os.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				return false;
			}
			count -= perPackageSize;
			offset += perPackageSize;
		} while (count > 0);

		return true;
	}

	/**
	 * 写入数据，如果连接还未建立，会一直等待。
	 * 
	 * @param data
	 * @param writeTimeout
	 * @return
	 */
	private static boolean writeUntil(byte[] data, int writeTimeout) {
		long time = System.currentTimeMillis();
		while (true) {

			if (System.currentTimeMillis() - time > writeTimeout) {
				return false;
			}
			if (!ConnectThread.isConnected())
				continue;

			OutputStream os = ConnectThread.getOutputStream();
			try {
				if (os != null) {
					os.write(data);
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (os != null)
					try {
						os.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				return false;
			}

		}
	}

	private static void sendBroadcast(Intent intent) {
		if (context != null) {
			context.sendBroadcast(intent);
		}
	}

}
