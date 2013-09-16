package com.lvrenyang.pos;

import java.io.File;
import java.io.RandomAccessFile;

import com.lvrenyang.rw.PL2303Driver;
import com.lvrenyang.rw.USBSerialPort;
import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.ErrorCode;

import android.content.Context;

/**
 * 该类继承自GMPos 注意，GMPos中并没有POS_Write和POS_Read的具体实现 必须Override才能实际生效
 * Pos新增的三个函数，是需要重写的部分 这个是基础类，其他的都需要用到这个东西 com.lvrenyang.rw包是和具体连接相关的。
 * 
 * @author Administrator
 * 
 */
public class Pos extends GMPos {

	Context mContext;
	USBSerialPort serialPort;
	PL2303Driver mSerial;

	public Pos(Context context, USBSerialPort serialPort, PL2303Driver mSerial) {
		mContext = context;
		this.serialPort = serialPort;
		this.mSerial = mSerial;

	}

	@Override
	public int POS_Write(byte[] buffer, int offset, int count, int timeout) {
		if (mSerial == null)
			return ErrorCode.NULLPOINTER;

		int cnt = mSerial.pl2303_write(serialPort, buffer, offset, count,
				timeout);
		if (rwSaveToFile)
			POS_WriteToFile(buffer, offset, cnt, writeSaveTo);
		return cnt;
	}

	@Override
	public int POS_Read(byte[] buffer, int offset, int count, int timeout) {
		if (mSerial == null)
			return ErrorCode.NULLPOINTER;

		int cnt = mSerial.pl2303_read(serialPort, buffer, offset, count,
				timeout);
		if (rwSaveToFile)
			POS_WriteToFile(buffer, offset, cnt, readSaveTo);
		return cnt;
	}

	@Override
	public boolean POS_IsOpen() {
		if (mSerial == null)
			return false;
		return mSerial.pl2303_isOpen(serialPort);
	}

}

class GMPos {

	public int timeout = 500;

	public boolean rwSaveToFile = false;
	public String readSaveTo;
	public String writeSaveTo;

	public int POS_Write(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	public int POS_Read(byte[] buffer, int offset, int count, int timeout) {

		return ErrorCode.NOTIMPLEMENTED;
	}

	public boolean POS_IsOpen() {

		return false;
	}

	public void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		POS_Write(data, 0, data.length, timeout);
	}

	/**
	 * 将数据存到文件
	 */
	public void POS_WriteToFile(byte[] buffer, int offset, int count,
			String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == buffer)
			return;
		if (offset < 0 || count <= 0)
			return;

		byte[] data = new byte[count];
		DataUtils.copyBytes(buffer, offset, data, 0, count);

		String str = DataUtils.bytesToStr(data) + "\r\n";
		// 每次写入时，都换行写
		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(str.getBytes());
			raf.close();
		} catch (Exception e) {
		}
	}

	public void POS_WriteToFile(String text, String dumpfile) {
		if (null == dumpfile)
			return;
		if (null == text)
			return;
		if ("".equals(text))
			return;

		String str = text + "\r\n";
		// 每次写入时，都换行写
		try {
			File file = new File(dumpfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(str.getBytes());
			raf.close();
		} catch (Exception e) {
		}
	}

	public void POS_SaveToFile(boolean saveToFile, String readSaveTo,
			String writeSaveTo) {
		this.rwSaveToFile = saveToFile;
		this.readSaveTo = readSaveTo;
		this.writeSaveTo = writeSaveTo;
	}

	/**
	 * 会用先前的波特率返回数据
	 * 
	 * @param baudrate
	 */
	public void POS_SetBaudrate(int baudrate) {
		int baudrates[] = { 9600, 19200, 38400, 57600, 115200, 230400 };
		int i;
		for (i = 0; i < baudrates.length; i++)
			if (baudrates[i] == baudrate)
				break;
		if (i == baudrates.length)
			return;
		byte[] data = Cmd.PCmd.setBaudrate;
		data[4] = (byte) (baudrate & 0xff);
		data[5] = (byte) ((baudrate >> 8) & 0xff);
		data[6] = (byte) ((baudrate >> 16) & 0xff);
		data[7] = (byte) ((baudrate >> 24) & 0xff);
		data[10] = DataUtils.bytesToXor(data, 0, 10);
		POS_Write(data, 0, data.length, timeout);
	}
}