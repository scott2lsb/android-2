package com.lvrenyang.kcusb;

public class ReadThread extends Thread {

	public static final String DEBUG = "com.lvrenyang.usbtool.debug";
	public static final String EXTRA_DEBUG = "com.lvrenyang.usbtool.extra_debug";
	public static final Object lock = new Object();

	private PL2303Driver mSerial;

	public boolean quit = false;

	byte[] buffer = new byte[4096];
	byte[] tmpbuf = new byte[4096];
	int cnt = 0;
	int timeout = 50;

	public ReadThread(PL2303Driver serial) {
		mSerial = serial;
	}

	@Override
	public void run() {
		int rec = 0;
		while (true) {
			if (quit)
				break;
			if ((rec = mSerial.read(tmpbuf, tmpbuf.length, timeout)) > 0) {
				synchronized (lock) {
					DataUtils.copyBytes(tmpbuf, 0, buffer, cnt, rec);
					cnt += rec;
					if (cnt >= buffer.length)
						cnt = 0;
				}
			}
		}
	}

	public int avaliable() {
		return cnt;
	}

	public void clear() {
		synchronized (lock) {
			cnt = 0;
		}
	}

	public int read(byte[] buf, int timeout) {
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < timeout) {
			if (cnt >= buf.length) {
				synchronized (lock) {
					DataUtils.copyBytes(buffer, 0, buf, 0, buf.length);
					cnt -= buf.length;
				}
				return buf.length;
			}
		}
		int tmpcnt = cnt;
		synchronized (lock) {
			DataUtils.copyBytes(buffer, 0, buf, 0, tmpcnt);
			cnt -= tmpcnt;
		}
		return tmpcnt;
	}

}
