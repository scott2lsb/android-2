package com.lvrenyang.pos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.ErrorCode;

/**
 * 升级程序
 * @author Administrator
 *
 */
public class Update extends Thread {

	private static UpdateHandler updateHandler = null;

	private static final int WHAT_UPDATE = 1001;
	private static final int WHAT_UPDATE_FONT = 1002;
	private static final int WHAT_UPDATE_SMALL_FONT = 1003;
	private static final int WHAT_PRINT_TEST = 1004;

	public static final String DEBUG = "com.lvrenyang.usbtool.debug";
	public static final String EXTRA_DEBUG = "com.lvrenyang.usbtool.extra_debug";
	private static boolean debug = false;

	private static Pos mPos;
	private static Context mContext;
	private static ReadThread readthread;

	public Update(Pos mPos, Context mContext) {
		Update.mPos = mPos;
		Update.mContext = mContext;
	}

	/**
	 * 一直循环等待，直到readHandler不为null。 该函数只在需要start之后立刻使用handler的情况下调用。
	 * 
	 * @return
	 */
	public void waitForHandlerReady() {
		while (null == updateHandler)
			;
	}

	@Override
	public void run() {
		Looper.prepare();

		readthread = new ReadThread(mPos);
		readthread.start();
		readthread.waitforloopready();

		updateHandler = new UpdateHandler();
		Looper.loop();
	}

	private static String data58binfile = null;
	private static String database9x24binfile = null;
	private static String dataencry9x24binfile = null;
	private static String programfile = null;
	private static String fontfile = null;

	public void updateSmallFont(String database9x24binfile,
			String dataencry9x24binfile) {
		Update.database9x24binfile = database9x24binfile;
		Update.dataencry9x24binfile = dataencry9x24binfile;
		if (null == updateHandler)
			return;
		if (null == mPos)
			return;

		if (mPos.POS_IsOpen()) {
			debug("updateSmallFont: " + Update.database9x24binfile + " "
					+ Update.dataencry9x24binfile + "\n");
			Message msg = updateHandler.obtainMessage();
			msg.what = WHAT_UPDATE_SMALL_FONT;
			updateHandler.sendMessage(msg);
		}
	}

	public void updateFont(String fontfile) {
		Update.fontfile = fontfile;
		if (null == updateHandler)
			return;
		if (null == mPos)
			return;

		if (mPos.POS_IsOpen()) {
			debug("updateFont: " + Update.fontfile + "\n");
			Message msg = updateHandler.obtainMessage();
			msg.what = WHAT_UPDATE_FONT;
			updateHandler.sendMessage(msg);
		}
	}

	public void updateProgram(String programfile) {
		Update.programfile = programfile;
		if (null == updateHandler)
			return;
		if (null == mPos)
			return;

		if (mPos.POS_IsOpen()) {
			debug("updateProgram: " + Update.programfile + "\n");
			Message msg = updateHandler.obtainMessage();
			msg.what = WHAT_UPDATE;
			updateHandler.sendMessage(msg);
		}
	}

	public int fontTest(String data58binfile) {
		Update.data58binfile = data58binfile;
		if (null == updateHandler)
			return ErrorCode.NULLPOINTER;
		if (null == mPos)
			return ErrorCode.NULLPOINTER;

		if (mPos.POS_IsOpen()) {
			debug("fontTest: " + Update.data58binfile + "\n");
			Message msg = updateHandler.obtainMessage();
			msg.what = WHAT_PRINT_TEST;
			updateHandler.sendMessage(msg);
			return 0;
		}
		return ErrorCode.ERROR;
	}

	/**
	 * 将发送和接受的数据dump到指定文件，置为空表示不进行dump。
	 * 
	 * @param dumpfile
	 */
	public void setDumpFile(String dumpfile) {
		UpdateHandler.dumpfile = dumpfile;
	}

	public void setDebug(boolean debug) {
		Update.debug = debug;
	}

	/**
	 * 和start相对应，当new完一个对象之后 调用start可以开启线程，调用quit退出线程 这里我不单单是调用就完了，我会等待调用确切完成。
	 */
	public void quit() {

		Looper.myLooper().quit();
		updateHandler = null;

		if (readthread != null) {
			readthread.quit();
		}
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

	public static class UpdateHandler extends Handler {

		public static final String UPDATE_INFO = "com.lvrenyang.usbtool.update_info";
		public static final String EXTRA_UPDATE_INFO = "com.lvrenyang.usbtool.extra_update_info";
		public static final String EXTRA_UPDATE_INFO_ARG = "com.lvrenyang.usbtool.extra_update_info_arg";

		static String dumpfile;

		private static byte[] data58bin = null;
		private static byte[] database9x24bin = null;
		private static byte[] dataencry9x24bin = null;

		private static int MaxRetryTimes = 4;
		private static int retryedTimes = 0;
		private static int timeout = 500;

		private static int index = 0;// 份数的索引，从0开始
		private static int orgoffset = 0;

		private static int times = 0;// 如果数据正常初始化,那么这些都会存好.份数
		private static int nCount = 0;
		private static int orglen = 0;
		private static byte[] orgdata = new byte[0];

		private static final int perCmdRespondLength = 12;
		private static final int MutiPackageCount = 4;
		private static final int testCmdRespondLength = 20;

		private static byte[] mutiBuf = new byte[MutiPackageCount
				* perCmdRespondLength];
		private static byte[] commonBuf = new byte[perCmdRespondLength];
		private static byte[] countBuf;
		private static byte[] testBuf = new byte[20];

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
				if (null == readthread)
					return;
				readthread.startread();

				/* 需要重新读取file，并填充数据 */
				try {
					FileInputStream fis = new FileInputStream(programfile);
					int binflen = fis.available();
					times = (binflen + 255) / 256;
					nCount = times % MutiPackageCount;
					orglen = times * 256;
					orgdata = new byte[orglen];
					fis.read(orgdata, 0, binflen);
					fis.close();
				} catch (FileNotFoundException e) {
					orgdata = new byte[0];
				} catch (IOException e) {
					orgdata = new byte[0];
				}
				countBuf = new byte[nCount * perCmdRespondLength];

				if (mPos == null)
					break;
				sendupdateinfo(Integer.MIN_VALUE, times);
				/* test */
				{
					for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
						if (!mPos.POS_IsOpen()) {
							/* ERRRO */
							error();
							return;
						}
						readthread.clear();
						int cnt;
						byte[] data = Cmd.PCmd.test;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(testBuf, 0, testBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, testBuf, 0, cnt);
						if (testCmdRespondLength == cnt) {
							if (checkTestReply(testBuf)) {
								debug("test ok!\n");
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERRRO */
							error();
							return;
						}
						readthread.clear();
						byte[] data = Cmd.PCmd.startUpdate;
						int cnt;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(commonBuf, 0, commonBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, commonBuf, 0, cnt);
						if (perCmdRespondLength == cnt) {
							if (checkMutiReply(commonBuf, 1,
									perCmdRespondLength)) {
								debug("start update\n");
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERROR */
							error();
							return;
						}
						readthread.clear();
						byte[] data = getUpdateCmd(index, MutiPackageCount);
						int cnt;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(mutiBuf, 0, mutiBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, mutiBuf, 0, cnt);
						if (perCmdRespondLength * MutiPackageCount == cnt) {
							if (checkMutiReply(mutiBuf, MutiPackageCount,
									perCmdRespondLength)) {
								index += MutiPackageCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERROR */
							error();
							return;
						}
						readthread.clear();
						byte[] data = getUpdateCmd(index, nCount);
						int cnt;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(countBuf, 0, countBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, countBuf, 0, cnt);
						if (perCmdRespondLength * nCount == cnt) {
							if (checkMutiReply(countBuf, nCount,
									perCmdRespondLength)) {
								index += nCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERRRO */
							error();
							return;
						}
						byte[] data = Cmd.PCmd.endUpdate;
						int cnt;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(commonBuf, 0, commonBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, commonBuf, 0, cnt);
						if (perCmdRespondLength == cnt) {
							if (checkMutiReply(commonBuf, 1,
									perCmdRespondLength)) {
								debug("end update!\n");
								break;
							}
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
					debug("null fontfile\n");
					return;
				}
				if (null == readthread) {
					debug("null readthread\n");
					return;
				}
				readthread.startread();

				/* 需要重新读取file，并填充数据 */
				try {
					FileInputStream fis = new FileInputStream(fontfile);
					int binflen = fis.available();
					times = (binflen + 255) / 256;
					nCount = times % MutiPackageCount;
					orglen = times * 256;
					orgdata = new byte[orglen];
					fis.read(orgdata, 0, binflen);
					fis.close();
				} catch (FileNotFoundException e) {
					orgdata = new byte[0];
				} catch (IOException e) {
					orgdata = new byte[0];
				}

				countBuf = new byte[nCount * perCmdRespondLength];

				if (mPos == null) {
					debug("null mPos\n");
					return;
				}
				sendupdateinfo(Integer.MIN_VALUE, times);
				/* test */
				{
					for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
						if (!mPos.POS_IsOpen()) {
							/* ERRRO */
							error();
							return;
						}
						readthread.clear();
						int cnt;

						byte[] data = Cmd.PCmd.test;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(testBuf, 0, testBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, testBuf, 0, cnt);
						if (testCmdRespondLength == cnt) {
							if (checkTestReply(testBuf)) {
								debug("test ok!\n");
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERROR */
							error();
							return;
						}
						readthread.clear();
						int cnt;
						byte[] data = getFontCmd(index, MutiPackageCount);
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(mutiBuf, 0, mutiBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, mutiBuf, 0, cnt);
						if (perCmdRespondLength * MutiPackageCount == cnt) {
							if (checkMutiReply(mutiBuf, MutiPackageCount,
									perCmdRespondLength)) {
								index += MutiPackageCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
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
						if (!mPos.POS_IsOpen()) {
							/* ERROR */
							error();
							return;
						}
						readthread.clear();
						int cnt;
						byte[] data = getFontCmd(index, nCount);
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						dumpwrite(dumpfile, data, 0, cnt);
						cnt = readthread.read(countBuf, 0, countBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						dumpread(dumpfile, countBuf, 0, cnt);
						if (perCmdRespondLength * nCount == cnt) {
							if (checkMutiReply(countBuf, nCount,
									perCmdRespondLength)) {
								index += nCount;
								debug("index = " + index + "\n");
								sendupdateinfo(index);
								break;
							}
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

				if (database9x24binfile == null || dataencry9x24binfile == null)
					return;
				if (null == readthread)
					return;
				readthread.startread();

				if (null == database9x24bin || null == dataencry9x24bin) {
					try {
						InputStream is = new FileInputStream(
								database9x24binfile);
						int binflen = is.available();
						int orglen = (binflen + 255) / 256 * 256;
						database9x24bin = new byte[orglen];
						is.read(database9x24bin, 0, binflen);
						is.close();
						is = new FileInputStream(dataencry9x24binfile);
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

				sendupdateinfo(Integer.MIN_VALUE, times);
				/** 额外加的一个 */
				/* test */
				{
					for (retryedTimes = 0; retryedTimes < MaxRetryTimes * 1000; retryedTimes++) {
						if (!mPos.POS_IsOpen()) {
							/* ERRRO */
							error();
							return;
						}
						readthread.clear();
						int cnt;
						byte[] data = Cmd.PCmd.test;
						cnt = mPos.POS_Write(data, 0, data.length, timeout);
						debug("write: " + cnt + "byte(s).\n");
						cnt = readthread.read(testBuf, 0, testBuf.length,
								timeout);
						debug("read: " + cnt + "byte(s).\n");
						if (testCmdRespondLength == cnt) {
							if (checkTestReply(testBuf)) {
								debug("test ok!\n");
								break;
							}
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

					if (mPos == null)
						break;
					sendupdateinfo(Integer.MIN_VALUE, times);
					index = 0;
					/* update */
					while (index + MutiPackageCount <= times) {
						for (retryedTimes = 0; retryedTimes < MaxRetryTimes; retryedTimes++) {
							if (!mPos.POS_IsOpen()) {
								/* ERROR */
								error();
								return;
							}
							readthread.clear();
							byte[] data = getFontCmd(index, MutiPackageCount,
									baseoffset);
							mPos.POS_Write(data, 0, data.length, timeout);
							if (perCmdRespondLength * MutiPackageCount == readthread
									.read(mutiBuf, 0, mutiBuf.length, timeout)) {
								if (checkMutiReply(mutiBuf, MutiPackageCount,
										perCmdRespondLength)) {
									index += MutiPackageCount;
									debug("index = " + index + "\n");
									sendupdateinfo(index);
									break;
								}
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
							if (!mPos.POS_IsOpen()) {
								/* ERROR */
								error();
								return;
							}
							readthread.clear();
							byte[] data = getFontCmd(index, nCount, baseoffset);
							mPos.POS_Write(data, 0, data.length, timeout);
							if (perCmdRespondLength * nCount == readthread
									.read(countBuf, 0, countBuf.length, timeout)) {
								if (checkMutiReply(countBuf, nCount,
										perCmdRespondLength)) {
									index += nCount;
									debug("index = " + index + "\n");
									sendupdateinfo(index);
									break;
								}
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
				if (null == data58binfile) {
					debug("null data58binfile\n");
					return;
				}
				if (null == data58bin) {
					try {
						InputStream is = new FileInputStream(data58binfile);
						data58bin = new byte[is.available()];
						is.read(data58bin);
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						debug("print test exception\n" + e.toString());
					}
				}

				if (null == mPos || data58bin == null) {
					debug("null mPos or data58bin\n");
					return;
				}
				byte[] data = data58bin;
				int cnt;
				cnt = mPos.POS_Write(data, 0, data.length, timeout);
				debug("write: " + cnt + "byte(s).\n");
				dumpwrite(dumpfile, data, 0, cnt);
				break;
			}

			default:
				break;
			}

		}

		private static boolean checkTestReply(byte[] buffer) {
			if (buffer.length != 20)
				return false;
			if (buffer[0] != (byte) 0x03 || buffer[1] != (byte) 0xfe)
				return false;
			for (int i = 0; i < 20; i++) {
				if (buffer[i] != (byte) 0x03)
					continue;
				if (buffer[i + 1] != (byte) 0xfe)
					continue;
				else
					return true;
			}
			return false;
		}

		private static boolean checkMutiReply(byte[] buffer, int count,
				int percmdreplylen) {
			if (buffer.length != count * percmdreplylen)
				return false;
			for (int i = 0; i < count * percmdreplylen; i++) {
				if (buffer[i] != (byte) 0x03)
					continue;
				if (buffer[i + 1] != (byte) 0xfe)
					continue;
				else
					return true;
			}
			return false;
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
				perdata[2] = 0x2d;
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
				perdata[2] = 0x2d;
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

		private static void dumpread(String dumpfile, byte[] buffer,
				int offset, int count) {
			if (null == mPos)
				return;
			if (null == dumpfile)
				return;
			if (null == buffer)
				return;
			if (offset < 0 || count <= 0)
				return;
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy年MM月dd日   HH:mm:ss", Locale.CHINESE);
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String str = formatter.format(curDate);
			mPos.POS_WriteToFile("read: " + str, dumpfile);
			mPos.POS_WriteToFile(buffer, offset, count, dumpfile);
		}

		private static void dumpwrite(String dumpfile, byte[] buffer,
				int offset, int count) {
			if (null == mPos)
				return;
			if (null == dumpfile)
				return;
			if (null == buffer)
				return;
			if (offset < 0 || count <= 0)
				return;
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy年MM月dd日   HH:mm:ss", Locale.CHINESE);
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String str = formatter.format(curDate);
			mPos.POS_WriteToFile("write: " + str, dumpfile);
			mPos.POS_WriteToFile(buffer, offset, count, dumpfile);
		}

		private static void sendupdateinfo(int index) {
			if (mContext != null) {
				Intent intent = new Intent(UPDATE_INFO);
				intent.putExtra(EXTRA_UPDATE_INFO, index);
				mContext.sendBroadcast(intent);
			}
		}

		private static void sendupdateinfo(int index, int arg) {
			if (mContext != null) {
				Intent intent = new Intent(UPDATE_INFO);
				intent.putExtra(EXTRA_UPDATE_INFO, index);
				intent.putExtra(EXTRA_UPDATE_INFO_ARG, arg);
				mContext.sendBroadcast(intent);
			}
		}

		private static void ok() {
			sendupdateinfo(Integer.MAX_VALUE);
			if (null != readthread)
				readthread.pauseread();
		}

		private static void error() {
			sendupdateinfo(-1);
			if (null != readthread)
				readthread.pauseread();
		}

	}

}

/**
 * 只有升级程序和字库需要用到ReadThread
 * 
 * @author Administrator
 * 
 */
class ReadThread extends Thread {

	private static final Object lock = new Object();
	private boolean read = false;
	private boolean quit = false;
	private boolean inloop = false;

	private byte[] buffer = new byte[4096];
	/** 一次只读一个字节 */
	private int percnt = 2048;
	private byte[] tmpbuf = new byte[percnt];
	private int cnt = 0;
	private int timeout = 30;

	private Pos mPos;

	/**
	 * mPos是构造函数里面引入的
	 * 
	 * @param mPos
	 */
	public ReadThread(Pos mPos) {
		this.mPos = mPos;
	}

	/**
	 * 调用完start之后，并不能立刻开始startread之类的，
	 * 因为线程并不一定完全准备好，需要调用waitforloopready来等待线程准备好，
	 * 之后才可以调用startread，pauseread，quit之类的函数。
	 */
	@Override
	public void run() {
		synchronized (lock) {
			inloop = false;
			read = false;
			quit = false;
		}

		int rec = 0;
		while (true) {
			synchronized (lock) {
				inloop = true;
				if (quit)
					break;
				if (!read)
					continue;
			}

			if (mPos == null)
				continue;

			if ((rec = mPos.POS_Read(tmpbuf, 0, tmpbuf.length, timeout)) > 0) {
				synchronized (lock) {
					DataUtils.copyBytes(tmpbuf, 0, buffer, cnt, rec);
					cnt += rec;
					if (cnt >= buffer.length)
						cnt = 0;
				}
			}
		}

		synchronized (lock) {
			inloop = false;
		}
	}

	/**
	 * 当start一个线程之后，调用该函数可以等待线程进入loop状态。 之后才能调用其他函数如startread和pauseread
	 */
	public void waitforloopready() {
		while (!inloop)
			;
	}

	public void startread() {
		synchronized (lock) {
			read = true;
		}
	}

	public void pauseread() {
		synchronized (lock) {
			read = false;
		}
	}

	public void quit() {
		synchronized (lock) {
			read = false;
			quit = true;
			while (inloop)
				;
		}

	}

	/**
	 * 不需要阻塞就能读取的数据
	 * 
	 * @return
	 */
	public int avaliable() {
		return cnt;
	}

	/**
	 * 清除缓冲区数据
	 */
	public void clear() {
		synchronized (lock) {
			cnt = 0;
		}
	}

	/**
	 * 从缓冲区读取数据
	 * 
	 * @param buf
	 * @param offset
	 * @param count
	 * @param timeout
	 * @return 读到的字节数
	 */
	public int read(byte[] buf, int offset, int count, int timeout) {

		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < timeout) {
			if (cnt >= count) {
				synchronized (lock) {
					DataUtils.copyBytes(buffer, 0, buf, offset, count);
					cnt -= count;
				}
				return count;
			}
		}
		int tmpcnt = cnt;
		synchronized (lock) {
			DataUtils.copyBytes(buffer, 0, buf, offset, tmpcnt);
			cnt -= tmpcnt;
		}
		return tmpcnt;
	}

}