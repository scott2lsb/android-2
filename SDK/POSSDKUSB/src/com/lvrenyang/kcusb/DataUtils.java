package com.lvrenyang.kcusb;

import java.util.Locale;
import java.util.Random;

/**
 * 
 * @author 书旗
 * 
 */
public class DataUtils {

	public static boolean bytesEquals(byte[] d1, byte[] d2) {
		if (d1 == null && d2 == null)
			return true;
		else if (d1 == null || d2 == null)
			return false;

		if (d1.length != d2.length)
			return false;

		for (int i = 0; i < d1.length; i++)
			if (d1[i] != d2[i])
				return false;

		return true;
	}

	public static char[] bytestochars(byte[] data) {
		char[] cdata = new char[data.length];
		for (int i = 0; i < cdata.length; i++)
			cdata[i] = (char) (data[i] & 0xff);
		return cdata;
	}

	public static byte[] getRandomByteArray(int nlength) {
		byte[] data = new byte[nlength];
		Random rmByte = new Random(System.currentTimeMillis());
		for (int i = 0; i < nlength; i++) {
			// 该方法的作用是生成一个随机的int值，该值介于[0,n)的区间，也就是0到n之间的随机int值，包含0而不包含n
			data[i] = (byte) rmByte.nextInt(256);
		}
		return data;
	}

	public static void blackWhiteReverse(byte data[]) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) ~(data[i] & 0xff);
		}
	}

	public static byte[] getSubBytes(byte[] org, int start, int length) {
		byte[] ret = new byte[length];
		for (int i = 0; i < length; i++) {
			ret[i] = org[i + start];
		}
		return ret;
	}

	public static String byteToStr(byte rc) {
		String rec;
		String tmp = Integer.toHexString(rc & 0xff);
		tmp = tmp.toUpperCase(Locale.getDefault());

		if (tmp.length() == 1) {
			rec = "0x0" + tmp;
		} else {
			rec = "0x" + tmp;
		}

		return rec;
	}

	public static String bytesToStr(byte[] rcs) {
		StringBuilder stringBuilder = new StringBuilder();
		String tmp;
		for (int i = 0; i < rcs.length; i++) {
			tmp = Integer.toHexString(rcs[i] & 0xff);
			tmp = tmp.toUpperCase(Locale.getDefault());
			if (tmp.length() == 1) {
				stringBuilder.append("0x0" + tmp);
			} else {
				stringBuilder.append("0x" + tmp);
			}

			if ((i % 16) != 15) {
				stringBuilder.append(" ");
			} else {
				stringBuilder.append("\n");
			}
		}
		return stringBuilder.toString();
	}

	public static byte[] cloneBytes(byte[] data) {
		byte[] ret = new byte[data.length];
		for (int i = 0; i < data.length; i++)
			ret[i] = data[i];
		return ret;
	}

	public static byte bytesToXor(byte[] data, int start, int length) {
		if (length == 0)
			return 0;
		else if (length == 1)
			return data[start];
		else {
			int result = data[start] ^ data[start + 1];
			for (int i = start + 2; i < start + length; i++)
				result ^= data[i];
			return (byte) result;
		}
	}

	/**
	 * 将多个字节数组按顺序合并
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] byteArraysToBytes(byte[][] data) {

		int length = 0;
		for (int i = 0; i < data.length; i++)
			length += data[i].length;
		byte[] send = new byte[length];
		int k = 0;
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[i].length; j++)
				send[k++] = data[i][j];
		return send;
	}

	/**
	 * 
	 * @param orgdata
	 * @param orgstart
	 * @param desdata
	 * @param desstart
	 * @param copylen
	 */
	public static void copyBytes(byte[] orgdata, int orgstart, byte[] desdata,
			int desstart, int copylen) {
		for (int i = 0; i < copylen; i++) {
			desdata[desstart + i] = orgdata[orgstart + i];
		}
	}

}