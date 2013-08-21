package btManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class WriteThread extends Thread {
	// 该线程一直存在
	public static Handler writeHandler = null;
	public static final int WHAT_WRITE = 10000;
	public static final int WHAT_PRINTPIC = 10001;
	public static final int WHAT_PRINTPICBYSTRING = 10002;
	public static final int WHAT_PRINTTEXT = 10003;
	public static final int WHAT_QUIT = 1000;
	public static final int WHAT_READTEST = 20000;
	public static final int WHAT_CONNECTTEST = 20001;
	public static final int WHAT_GUESTTEST = 20002;
	public static final int WHAT_SETKEY = 30001;
	public static final int WHAT_CHECKKEY = 30002;
	public static final int WHAT_MYJOB = 30005;
	public static final int WHAT_MYJOB_CODEPAGES = 30006;
	public static final int WHAT_SETBAUDRATE = 30007;
	public static final int WHAT_SETPRINTPARAM = 30008;
	public static final int WHAT_SETBLUETOOTH = 30009;
	public static final int timeout = 500;
	private static final int perPackageSize = 2048;
	private static final int perPackageWaite = 200;
	private static final int retryTimes = 3;

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
				write(data);
				break;
			}

			case WHAT_READTEST: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				try {
					Pos.os.write(Pos.Pro.test);
					long time = System.currentTimeMillis();
					while (true) {
						if (System.currentTimeMillis() - time > timeout) {
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"TEST失败");
							break;
						}

						if (ReadThread.KcCmd == 0x20) {
							ReadThread.KcCmd = 0;
							ReadThread.KcPara = 0;
							// 如果收到正确的的返回数据，那么就要readAll.updateStatus变更为imaupdate

							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"TEST成功");
							break;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "TEST失败");
					Pos.POS_Close();
				}
				Pos.mContext.sendBroadcast(intent);
				break;
			}

			case WHAT_CONNECTTEST: {
				if (checkStatusWithRetryTimes(10)) {
					// if (true) {
					Pos.mContext
							.sendBroadcast(new Intent(Pos.ACTION_CONNECTED));
				} else {
					Pos.Connected = false;
					Pos.POS_Close();
					Pos.mContext.sendBroadcast(new Intent(
							Pos.ACTION_CONNECTINGFAILED));
				}
				break;
			}

			case WHAT_GUESTTEST: {
				if (checkStatusWithRetryTimes(msg.arg1)) {
					Pos.mContext.sendBroadcast(new Intent(
							Pos.ACTION_TESTSUCCESS));
				} else {
					Pos.mContext
							.sendBroadcast(new Intent(Pos.ACTION_TESTFAILED));
				}

				break;
			}

			case WHAT_PRINTPIC: {
				Bitmap mBitmap = (Bitmap) msg.obj;
				if (mBitmap == null)
					break;
				int nWidth = msg.arg1;
				int nMode = msg.arg2;

				int width = ((nWidth + 7) / 8) * 8;
				int height = mBitmap.getHeight() * width / mBitmap.getWidth();
				Bitmap rszBitmap = Pos.resizeImage(mBitmap, width, height);
				// 再保存缩放的位图以便调试
				// saveMyBitmap(rszBitmap);
				byte[] data = Pos.pixToCmd(Pos.bitmapToBWPix(rszBitmap),
						width, nMode);
				write(data);
				break;
			}

			case WHAT_PRINTPICBYSTRING: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				String path = msg.obj.toString();

				if (path == null)
					break;

				if (!checkStatusWithRetryTimesAndRestoreTimesAndCount(1, 9,
						8192)) {
					intent.putExtra(
							UpdateThread.EXTRA_DEBUGINFO,
							"未发送：\n"
									+ path.substring(path
											.lastIndexOf(File.separatorChar)));
					Pos.mContext.sendBroadcast(intent);
					break;
				}

				Bitmap mBitmap = BitmapFactory.decodeFile(path);
				if (mBitmap == null)
					break;

				int nWidth = msg.arg1;
				int nMode = msg.arg2;
				int width = ((nWidth + 7) / 8) * 8;
				int height = mBitmap.getHeight() * width / mBitmap.getWidth();
				Bitmap rszBitmap = Pos.toGrayscale( Pos.resizeImage(mBitmap, width, height) );
				// 再保存缩放的位图以便调试
				Pos.saveMyBitmap(rszBitmap);
				byte[] data = Pos.pixToCmd(Pos.bitmapToBWPix(rszBitmap),
						width, nMode);
				write(data);
				if (checkStatusWithRetryTimesAndRestoreTimesAndCount(
						retryTimes, retryTimes, data.length)) {
					intent.putExtra(
							UpdateThread.EXTRA_DEBUGINFO,
							"发送成功：\n"
									+ path.substring(path
											.lastIndexOf(File.separatorChar)));
					Pos.mContext.sendBroadcast(intent);
					break;
				} else {
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "发送失败，请断开重连!");
					Pos.mContext.sendBroadcast(intent);
				}
				break;
			}

			case WHAT_PRINTTEXT: {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy MM dd  HH:mm:ss", Locale.CHINA);
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String str = formatter.format(curDate);
				String path = msg.obj.toString();
				String head = "\n" + str + "\nPath: " + path + "\n";
				/**
				 * 如果是中文，还需要选择语言，用getBytes()
				 */
				write(head.getBytes());
				try {
					FileInputStream fis = new FileInputStream(path);
					int length = fis.available();
					byte[] data = new byte[length];
					fis.read(data);
					write(data);
					fis.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				write(new byte[] { 0x0a });
				break;
			}

			/**
			 * obj里是要下发的命令 把字节转回char是我要做的事情
			 */
			case WHAT_SETKEY: {
				byte[] key = (byte[]) msg.obj;
				{
					byte[] data = Pos.Cmd.DES_SETKEY;
					for (int i = 0; i < key.length; i++) {
						data[i + 5] = key[i];
					}
					// 设置DES密钥，打印机不会返回，需要发送命令设置
					write(data);
				}
				{
					int nlength = 8;
					// 产生8位随机数，发送给打印机
					byte[] random = getRandomByteArray(nlength);
					byte blengthl = (byte) (random.length % 0x100);
					byte blengthh = (byte) (random.length / 0x100);
					byte[] data = Pos
							.byteArraysToBytes(new byte[][] {
									Pos.Cmd.DES_ENCRYPT,
									{ blengthl, blengthh }, random });

					// 发送随即明文数据，等待返回
					write(data);

					Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
					// 对比返回的数据
					for (int i = 0; i < retryTimes; i++) {
						/**
						 * 返回的加密后的数据
						 */
						byte[] encryptData = getDes();

						/**
						 * 对数据进行解密
						 */
						DES2 des2 = new DES2();
						// 初始化密钥
						des2.yxyDES2_InitializeKey(key);
						des2.yxyDES2_DecryptData(encryptData);
						byte[] decodeData = des2.getPlaintext();

						if (bytesEquals(random, decodeData)) {
							// 添加信息
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"密钥设置成功");
							break;
						} else {
							// 不管怎么样，check完一次，count都要置零
							ReadThread.Des_Buf.Count = 0;
							write(data);
						}
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "密钥设置失败");
					}

					// 发送广播
					Pos.mContext.sendBroadcast(intent);
				}
				break;
			}

			case WHAT_CHECKKEY: {
				byte[][] keyAndData = (byte[][]) msg.obj;
				byte[] key = keyAndData[0];
				byte[] data = keyAndData[1];
				byte blengthl = (byte) (data.length % 0x100);
				byte blengthh = (byte) (data.length / 0x100);
				byte[] dataCmd = Pos.byteArraysToBytes(new byte[][] {
						Pos.Cmd.DES_ENCRYPT, { blengthl, blengthh }, data });

				// 发送随即明文数据，等待返回加密后的数据
				write(dataCmd);

				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				// 对比返回的数据
				for (int i = 0; i < retryTimes; i++) {
					/**
					 * 返回的加密后的数据
					 */
					byte[] encryptData = getDes();

					/**
					 * 对数据进行解密
					 */
					DES2 des2 = new DES2();
					// 初始化密钥
					des2.yxyDES2_InitializeKey(key);
					des2.yxyDES2_DecryptData(encryptData);
					byte[] decodeData = des2.getPlaintext();

					if (bytesEquals(data, decodeData)) {
						// 添加信息
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "密钥正确");
						break;
					} else {
						// 不管怎么样，check完一次，count都要置零
						ReadThread.Des_Buf.Count = 0;
						write(data);
					}
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "密钥错误");
				}

				// 发送广播
				Pos.mContext.sendBroadcast(intent);

				break;
			}

			case WHAT_MYJOB: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "开始生成文件");
				Pos.mContext.sendBroadcast(intent);
				File dir = new File(msg.obj.toString());
				File[] allFile = dir.listFiles();
				ArrayList<File> alAllFile = new ArrayList<File>();
				for (int i = 0; i < allFile.length; i++)
					alAllFile.add(allFile[i]);
				Collections.sort(alAllFile, new WriteThread.myFileComparator());
				for (int i = 0; i < alAllFile.size(); i++) {
					Log.i("alAllFile", alAllFile.get(i).getName());
				}
				FileOutputStream fos = null;
				FileInputStream fis = null;
				try {
					fos = new FileOutputStream(
							Environment.getExternalStorageDirectory()
									+ "/ziku/new/" + dir.getName() + ".bin");
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "创建输出文件");
					Pos.mContext.sendBroadcast(intent);
				} catch (FileNotFoundException e) {
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "输出文件创建失败");
					Pos.mContext.sendBroadcast(intent);
					break;
				}

				for (int i = 0; i < alAllFile.size(); i++) {
					try {
						fis = new FileInputStream(alAllFile.get(i));
						int length = fis.available();
						byte[] buffer = new byte[length];
						fis.read(buffer);
						fis.close();
						byte[] writeBuffer = new byte[112 * 16];// 112*16
						// 0,1,2
						Pos.copyBytes(buffer, 0, writeBuffer, 0, 112 * 3);
						// 3,4,5
						Pos.copyBytes(buffer, 112 * 4, writeBuffer, 112 * 3,
								112 * 3);
						// 6,7,8
						Pos.copyBytes(buffer, 112 * 8, writeBuffer, 112 * 6,
								112 * 3);
						// 9,10,11
						Pos.copyBytes(buffer, 112 * 12, writeBuffer, 112 * 9,
								112 * 3);
						// 12,13,14
						Pos.copyBytes(buffer, 112 * 16, writeBuffer, 112 * 12,
								112 * 3);
						// 15
						Pos.copyBytes(buffer, 112 * 20, writeBuffer, 112 * 15,
								112);
						if (alAllFile.get(i).getName().contains("FF")) {
							for (int k = 112 * 15; k < 112 * 16; k++) {
								writeBuffer[k] = (byte) 0xff;
							}
						}
						blackWhiteReverse(writeBuffer);

						fos.write(writeBuffer);

						// 这里最好是记录在txt文件里面保险一点

						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
								alAllFile.size() + ": " + i + " "
										+ alAllFile.get(i).getName());
						Pos.mContext.sendBroadcast(intent);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "读取文件失败");
						Pos.mContext.sendBroadcast(intent);
						break;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "读取文件失败");
						Pos.mContext.sendBroadcast(intent);
						break;
					}
				}
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "写入完成");
				Pos.mContext.sendBroadcast(intent);

				break;
			}

			// 给定一个目录，自动将目录下所有文件生成字库，存放
			case WHAT_MYJOB_CODEPAGES: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "开始生成文件");
				Pos.mContext.sendBroadcast(intent);
				File dir = new File(msg.obj.toString());
				File[] allDir = dir.listFiles();
				intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "目录数："
						+ allDir.length);
				Pos.mContext.sendBroadcast(intent);
				// 每个字符的字节数
				int perCharByte = 56;
				// 每个文件有多少个有效字符
				int perFileChar = 64;
				// 每块的字符数
				int perBlockCount = 7;
				// 每次跳过的字符数
				int skip = 2;
				int m = 0;
				for (m = 0; m < allDir.length; m++) {
					File[] allFile = allDir[m].listFiles();
					ArrayList<File> alAllFile = new ArrayList<File>();
					for (int i = 0; i < allFile.length; i++)
						alAllFile.add(allFile[i]);
					Collections.sort(alAllFile,
							new WriteThread.myFileComparator());
					FileOutputStream fos = null;
					FileInputStream fis = null;

					try {
						fos = new FileOutputStream(
								Environment.getExternalStorageDirectory()
										+ "/ziku/new/" + allDir[m].getName()
										+ ".bin");
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "创建输出文件");
						Pos.mContext.sendBroadcast(intent);
					} catch (FileNotFoundException e) {
						intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
								"输出文件创建失败");
						Pos.mContext.sendBroadcast(intent);
						break;
					}

					for (int i = 0; i < alAllFile.size(); i++) {
						try {
							fis = new FileInputStream(alAllFile.get(i));
							int length = fis.available();
							byte[] buffer = new byte[length];
							fis.read(buffer);
							fis.close();
							byte[] writeBuffer = new byte[perCharByte
									* perFileChar];
							int orgOffset = 0;
							int desOffset = 0;
							while (desOffset < writeBuffer.length) {

								if ((desOffset + perCharByte * perBlockCount) <= writeBuffer.length) {
									Pos.copyBytes(buffer, orgOffset,
											writeBuffer, desOffset, perCharByte
													* perBlockCount);
									orgOffset += perCharByte
											* (perBlockCount + skip);
									desOffset += perCharByte * perBlockCount;
								} else {
									Pos.copyBytes(buffer, orgOffset,
											writeBuffer, desOffset,
											writeBuffer.length - desOffset);
									desOffset = writeBuffer.length;
								}

							}
							blackWhiteReverse(writeBuffer);
							fos.write(writeBuffer);

							// 这里最好是记录在txt文件里面保险一点

							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									alAllFile.size() + ": " + i + " "
											+ alAllFile.get(i).getName());
							Pos.mContext.sendBroadcast(intent);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"读取文件失败");
							Pos.mContext.sendBroadcast(intent);
							break;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"读取文件失败");
							Pos.mContext.sendBroadcast(intent);
							break;
						}
					}
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "写入完成"
							+ allDir[m].getName());
					Pos.mContext.sendBroadcast(intent);
				}
				if (m == allDir.length) {
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "所有文件写入完成");
					Pos.mContext.sendBroadcast(intent);
				} else {
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "生成出错");
					Pos.mContext.sendBroadcast(intent);
				}
				break;
			}

			/**
			 * 设置打印参数
			 */
			case WHAT_SETPRINTPARAM: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				try {
					byte[] setparams = (byte[]) msg.obj;
					setparams[10] = Pos.bytesToXor(setparams, 0, 10);
					setparams[11] = Pos.bytesToXor(setparams, 12, 16);
					ReadThread.Cmd = 0;
					Pos.os.write(setparams);
					long time = System.currentTimeMillis();
					while (true) {
						if (System.currentTimeMillis() - time > timeout) {
							String tmp = "\n发送: ";
							for (int i = 0; i < Pos.Pro.setPrintParam.length; i++) {
								tmp += Integer
										.toHexString(Pos.Pro.setPrintParam[i] & 0xff)
										+ " ";
							}
							tmp += "\n接收: ";
							for (int i = 0; i < 12; i++) {
								tmp += Integer
										.toHexString(ReadThread.Para_Buf.Buffer[i] & 0xff)
										+ " ";
							}
							intent.putExtra(
									UpdateThread.EXTRA_DEBUGINFO,
									"设置失败\n"
											+ "Cmd_Para:"
											+ Integer
													.toHexString(ReadThread.Cmd)
											+ tmp);
							break;
						}

						if (ReadThread.Cmd == 0x60) {
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"设置成功");
							break;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "设置失败");
					Pos.POS_Close();
				}
				Pos.mContext.sendBroadcast(intent);
				break;
			}

			case WHAT_SETBLUETOOTH: {
				Intent intent = new Intent(UpdateThread.ACTION_DEBUGINFO);
				try {
					byte[] setBt = (byte[]) msg.obj;
					int datalength = setBt.length - 12;
					setBt[8] = (byte) (datalength & 0xff);
					setBt[9] = (byte) ((datalength & 0xff00) >> 8);
					setBt[10] = Pos.bytesToXor(setBt, 0, 10);
					setBt[11] = Pos.bytesToXor(setBt, 12, datalength);
					ReadThread.Cmd = 0;
					Pos.os.write(setBt);
					long time = System.currentTimeMillis();
					while (true) {
						if (System.currentTimeMillis() - time > timeout) {
							String tmp = "\n发送: ";
							for (int i = 0; i < Pos.Pro.setPrintParam.length; i++) {
								tmp += Integer
										.toHexString(Pos.Pro.setPrintParam[i] & 0xff)
										+ " ";
							}
							tmp += "\n接收: ";
							for (int i = 0; i < 12; i++) {
								tmp += Integer
										.toHexString(ReadThread.Para_Buf.Buffer[i] & 0xff)
										+ " ";
							}
							intent.putExtra(
									UpdateThread.EXTRA_DEBUGINFO,
									"设置失败\n"
											+ "Cmd_Para:"
											+ Integer
													.toHexString(ReadThread.Cmd)
											+ tmp);
							break;
						}

						if (ReadThread.Cmd == 0x61) {
							intent.putExtra(UpdateThread.EXTRA_DEBUGINFO,
									"设置成功");
							break;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					intent.putExtra(UpdateThread.EXTRA_DEBUGINFO, "设置失败");
					Pos.POS_Close();
				}
				Pos.mContext.sendBroadcast(intent);
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

	private static void write(byte[] data) {
		Pos.POS_SavingFile(data, 1);
		try {
			if (Pos.POS_Connected() && (Pos.os != null) && data != null) {
				if (data.length > perPackageSize) {
					int count = data.length;
					int offset = 0;
					do {
						long time = System.currentTimeMillis();
						if (Pos.POS_Connected() && Pos.os != null)
							Pos.os.write(data, offset, perPackageSize);
						else
							throw new IOException();
						count -= perPackageSize;
						offset += perPackageSize;
						while (true) {
							if (System.currentTimeMillis() - time > perPackageWaite)
								break;
						}
					} while (count > perPackageSize);
					if (Pos.POS_Connected() && (Pos.os != null))
						Pos.os.write(data, offset, count);
				} else
					Pos.os.write(data);

			}
		} catch (IOException e) {
			Pos.POS_Close();
		}
	}

	static byte[] getDes() {
		long time = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - time > timeout) {
				return new byte[8];
			}

			/**
			 * 如果数据正确接收完毕，可以开始比对
			 */
			if (ReadThread.Des_Buf.Count != 0
					&& ReadThread.Des_Buf.Count == ReadThread.DesPara + 5) {
				byte[] data = new byte[ReadThread.DesPara];
				for (int i = 0; i < data.length; i++) {
					data[i] = ReadThread.Des_Buf.Buffer[5 + i];
				}
				return data;
			}

		}
	}

	static boolean bytesEquals(byte[] d1, byte[] d2) {
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

	static char[] bytestochars(byte[] data) {
		char[] cdata = new char[data.length];
		for (int i = 0; i < cdata.length; i++)
			cdata[i] = (char) (data[i] & 0xff);
		return cdata;
	}

	static boolean checkStatus() {
		try {
			if (Pos.POS_Connected() && (Pos.os != null)) {
				Pos.os.write(Pos.Pro.test);
				long time = System.currentTimeMillis();
				while (true) {
					if (System.currentTimeMillis() - time > timeout) {
						return false;
					}
					if ((ReadThread.KcCmd == 0x20)) {
						ReadThread.KcCmd = 0;
						ReadThread.KcPara = 0;
						// 如果收到正确的的返回数据，那么就要readAll.updateStatus变更为imaupdate
						return true;
					}
				}
			}
		} catch (IOException e) {
			Pos.POS_Close();
		}
		return false;
	}

	/**
	 * 
	 * @param nRetryTimes
	 * @return
	 */
	static boolean checkStatusWithRetryTimes(int nRetryTimes) {
		for (int i = 0; i < nRetryTimes; i++) {
			if (checkStatus())
				return true;
		}
		return false;
	}

	private static boolean checkStatusWithRetryTimesAndRestoreTimesAndCount(
			int nRetryTimes, int nRestoreTimes, int nRestoreCount) {
		for (int i = 0; i < nRestoreTimes; i++) {
			if (checkStatusWithRetryTimes(retryTimes)) {
				return true;
			} else {
				write(new byte[nRestoreCount]);
				if (checkStatusWithRetryTimes(retryTimes)) {
					return true;
				}
			}
		}
		return false;
	}

	private static byte[] getRandomByteArray(int nlength) {
		byte[] data = new byte[nlength];
		Random rmByte = new Random(System.currentTimeMillis());
		for (int i = 0; i < nlength; i++) {
			// 该方法的作用是生成一个随机的int值，该值介于[0,n)的区间，也就是0到n之间的随机int值，包含0而不包含n
			data[i] = (byte) rmByte.nextInt(256);
		}
		return data;
	}

	private static void blackWhiteReverse(byte data[]) {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) ~(data[i] & 0xff);
		}
	}

	private static class myFileComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			// TODO Auto-generated method stub
			Collator collator = Collator.getInstance();

			return collator.compare(lhs.getAbsolutePath(),
					rhs.getAbsolutePath());
		}

	}

}
