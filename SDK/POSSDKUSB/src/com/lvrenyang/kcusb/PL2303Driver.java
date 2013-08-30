package com.lvrenyang.kcusb;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class PL2303Driver {

	private boolean debug = false;
	public static final String DEBUG = "com.lvrenyang.usbtool.debug";
	public static final String EXTRA_DEBUG = "com.lvrenyang.usbtool.extra_debug";

	private UsbManager mUsbManager;
	private Context mContext;// context 单例模式
	private PendingIntent mPermissionIntent;

	private int timeOut = 500;
	private UsbDevice mUsbDevice;
	private UsbInterface mUsbInterface;
	private UsbEndpoint mUsbEndpointOut, mUsbEndpointIn;
	private UsbDeviceConnection mUsbDeviceConnection;

	public ReadThread readthread;

	static class SupportDevices {
		static final int[] PL2303HXD = { 0x067b, 0x2303 };
	};

	public PL2303Driver(UsbManager manager, Context context, String sAppName) {
		mUsbManager = manager;
		mContext = context;
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				sAppName), 0);
	}

	/**
	 * 设置超时时间，该时间用于所有的端口读写。
	 * 
	 * @param timeOut
	 *            超时时间
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
		if (this.timeOut <= 0)
			this.timeOut = 500;
	}

	/**
	 * 枚举以发现设备，并设置即将要连接的设备。
	 * 
	 * @return 如果发现了，则返回true；否则，返回false。
	 */
	public boolean enumerate() {
		if (null == mUsbManager)
			return false;
		if (null == mContext)
			return false;

		// 以后如果需要支持多串口，只需添加另一个函数来扩展一下即可
		HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		if (deviceList.size() > 0) {

			// 初始化选择对话框布局，并添加按钮和事件
			LinearLayout llSelectDevice = new LinearLayout(mContext);
			llSelectDevice.setOrientation(LinearLayout.VERTICAL);
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("选择设备").setView(llSelectDevice);
			final AlertDialog dialog = builder.create();

			while (deviceIterator.hasNext()) {
				final UsbDevice device = deviceIterator.next();
				if (device.getVendorId() != SupportDevices.PL2303HXD[0])
					continue;
				Button btDevice = new Button(llSelectDevice.getContext());
				btDevice.setGravity(Gravity.LEFT);
				btDevice.setText("ID: " + device.getDeviceId());
				btDevice.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						mUsbDevice = device;
						dialog.dismiss();
						// 等下添加到事件中去
						if (!mUsbManager.hasPermission(mUsbDevice)) {
							mUsbManager.requestPermission(mUsbDevice,
									mPermissionIntent);
						}
					}
				});
				llSelectDevice.addView(btDevice);
			}
			if (llSelectDevice.getChildCount() == 1)
				llSelectDevice.getChildAt(0).callOnClick();
			else
				dialog.show();
			return true;
		}

		return false;
	}

	public void startRead() {
		if (null == readthread)
			readthread = new ReadThread(this);
		readthread.quit = false;
		readthread.start();
	}

	public void endRead() {
		if (null != readthread)
			readthread.quit = true;
	}

	public boolean connectByDefualtValue() {
		if (null == mUsbDevice)
			return false;
		if (null == mUsbManager)
			return false;
		if (!mUsbManager.hasPermission(mUsbDevice))
			return false;

		// 枚举，把读写控制端口什么的给弄过来。然后set
		outer: for (int i = 0; i < mUsbDevice.getInterfaceCount(); i++) {
			mUsbInterface = mUsbDevice.getInterface(i);
			mUsbEndpointOut = null;
			mUsbEndpointIn = null;
			for (int j = 0; j < mUsbInterface.getEndpointCount(); j++) {
				UsbEndpoint endpoint = mUsbInterface.getEndpoint(j);
				if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT
						&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					mUsbEndpointOut = endpoint;
				} else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN
						&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					mUsbEndpointIn = endpoint;
				}

				// 如果在第一个接口就找到了符合要求的端点，那么break;
				if ((null != mUsbEndpointOut) && (null != mUsbEndpointIn)) {
					debug("Out:\n" + endpointInfo(mUsbEndpointOut) + "\nIn:\n"
							+ endpointInfo(mUsbEndpointIn));
					break outer;
				}
			}
		}
		if (null == mUsbInterface)
			return false;
		if ((null == mUsbEndpointOut) || (null == mUsbEndpointIn))
			return false;
		mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
		if (null == mUsbDeviceConnection)
			return false;
		mUsbDeviceConnection.claimInterface(mUsbInterface, true);

		pl2303_startup();
		pl2303_open();
		return true;
	}

	public void disconnect() {
		if ((null != mUsbInterface) && (null != mUsbDeviceConnection)) {
			mUsbDeviceConnection.releaseInterface(mUsbInterface);
			mUsbDeviceConnection.close();
		}
	}

	public int write(byte[] buffer, int length) {
		if (null == mUsbEndpointOut)
			return -2;
		if (null == mUsbDeviceConnection)
			return -3;
		if (null == buffer)
			return -4;
		if (length <= 0 || length > buffer.length)
			return -5;
		return mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, buffer,
				length, timeOut);
	}

	public int read(byte[] buffer, int length) {
		if (null == mUsbEndpointIn)
			return -2;
		if (null == mUsbDeviceConnection)
			return -3;
		if (null == buffer)
			return -4;
		if (length <= 0 || length > buffer.length)
			return -5;
		return mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer,
				length, timeOut);
	}

	public int read(byte[] buffer, int length, int timeout) {
		if (null == mUsbEndpointIn)
			return -2;
		if (null == mUsbDeviceConnection)
			return -3;
		if (null == buffer)
			return -4;
		if (length <= 0 || length > buffer.length)
			return -5;
		return mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer,
				length, timeout);
	}

	private static final int SET_LINE_REQUEST_TYPE = 0x21;
	private static final int SET_LINE_REQUEST = 0x20;

	private static final int SET_CONTROL_REQUEST_TYPE = 0x21;
	private static final int SET_CONTROL_REQUEST = 0x22;
	private static final int CONTROL_DTR = 0x01;
	private static final int CONTROL_RTS = 0x02;

	@SuppressWarnings("unused")
	private static final int BREAK_REQUEST_TYPE = 0x21;
	@SuppressWarnings("unused")
	private static final int BREAK_REQUEST = 0x23;
	@SuppressWarnings("unused")
	private static final int BREAK_ON = 0xffff;
	@SuppressWarnings("unused")
	private static final int BREAK_OFF = 0x0000;

	@SuppressWarnings("unused")
	private static final int GET_LINE_REQUEST_TYPE = 0xa1;
	@SuppressWarnings("unused")
	private static final int GET_LINE_REQUEST = 0x21;

	private static final int VENDOR_WRITE_REQUEST_TYPE = 0x40;
	private static final int VENDOR_WRITE_REQUEST = 0x01;

	private static final int VENDOR_READ_REQUEST_TYPE = 0xc0;
	private static final int VENDOR_READ_REQUEST = 0x01;

	@SuppressWarnings("unused")
	private static final int UART_STATE = 0x08;
	@SuppressWarnings("unused")
	private static final int UART_STATE_TRANSIENT_MASK = 0x74;
	@SuppressWarnings("unused")
	private static final int UART_DCD = 0x01;
	@SuppressWarnings("unused")
	private static final int UART_DSR = 0x02;
	@SuppressWarnings("unused")
	private static final int UART_BREAK_ERROR = 0x04;
	@SuppressWarnings("unused")
	private static final int UART_RING = 0x08;
	@SuppressWarnings("unused")
	private static final int UART_FRAME_ERROR = 0x10;
	@SuppressWarnings("unused")
	private static final int UART_PARITY_ERROR = 0x20;
	@SuppressWarnings("unused")
	private static final int UART_OVERRUN_ERROR = 0x40;
	@SuppressWarnings("unused")
	private static final int UART_CTS = 0x80;

	private pl2303_type type = pl2303_type.HX;
	private byte[] termios = new byte[] { (byte) 0x80, 0x25, 0x0, 0x0, 0x0,
			0x0, 0x8 };

	enum pl2303_type {
		type_0, /* don't know the difference between type 0 and */
		type_1, /* type 1, until someone from prolific tells us... */
		HX, /* HX version of the pl2303 chip */
	};

	int pl2303_vendor_read(int value, int index, byte[] buffer) {
		if (null == mUsbDeviceConnection)
			return -1;

		int retval = mUsbDeviceConnection.controlTransfer(
				VENDOR_READ_REQUEST_TYPE, VENDOR_READ_REQUEST, value, index,
				buffer, 1, 100);

		return retval;
	}

	int pl2303_vendor_write(int value, int index) {
		if (null == mUsbDeviceConnection)
			return -1;

		int retval = mUsbDeviceConnection.controlTransfer(
				VENDOR_WRITE_REQUEST_TYPE, VENDOR_WRITE_REQUEST, value, index,
				null, 0, 100);
		return retval;
	}

	int pl2303_startup() {
		type = pl2303_type.type_0;
		byte[] buf = new byte[256];
		UsbEndpoint endpoint = null;
		if (null != mUsbEndpointOut)
			endpoint = mUsbEndpointOut;
		else if (null != mUsbEndpointIn)
			endpoint = mUsbEndpointIn;
		else
			return -1;

		if (mUsbDevice.getDeviceClass() == 0x02)
			type = pl2303_type.type_0;
		else if (endpoint.getMaxPacketSize() == 0x40)
			type = pl2303_type.HX;
		else if (mUsbDevice.getDeviceClass() == 0x00)
			type = pl2303_type.type_1;
		else if (mUsbDevice.getDeviceClass() == 0xFF)
			type = pl2303_type.type_1;

		pl2303_vendor_read(0x8484, 0, buf);
		pl2303_vendor_write(0x0404, 0);
		pl2303_vendor_read(0x8484, 0, buf);
		pl2303_vendor_read(0x8383, 0, buf);
		pl2303_vendor_read(0x8484, 0, buf);
		pl2303_vendor_write(0x0404, 1);
		pl2303_vendor_read(0x8484, 0, buf);
		pl2303_vendor_read(0x8383, 0, buf);
		pl2303_vendor_write(0, 1);
		pl2303_vendor_write(1, 0);
		if (type == pl2303_type.HX)
			pl2303_vendor_write(2, 0x44);
		else
			pl2303_vendor_write(2, 0x24);

		return 0;
	}

	int set_control_lines(int value) {
		if (null == mUsbDeviceConnection)
			return -1;

		int retval = mUsbDeviceConnection.controlTransfer(
				SET_CONTROL_REQUEST_TYPE, SET_CONTROL_REQUEST, value, 0, null,
				0, 100);

		return retval;
	}

	int pl2303_set_termios() {

		if (null == mUsbDeviceConnection)
			return -1;

		int retval = mUsbDeviceConnection.controlTransfer(
				SET_LINE_REQUEST_TYPE, SET_LINE_REQUEST, 0, 0, termios, 7, 100);

		/* change control lines if we are switching to or from B0 */

		set_control_lines(CONTROL_DTR | CONTROL_RTS);

		if (type == pl2303_type.HX)
			pl2303_vendor_write(0x0, 0x61);
		else
			pl2303_vendor_write(0x0, 0x41);

		return retval;
	}

	int pl2303_open() {
		if (type != pl2303_type.HX) {

		} else {
			pl2303_vendor_write(8, 0);
			pl2303_vendor_write(9, 0);
		}
		pl2303_set_termios();
		return 0;
	}

	class PL2303_H {
		static final int BENQ_VENDOR_ID = 0x04a5;
		static final int BENQ_PRODUCT_ID_S81 = 0x4027;

		static final int PL2303_VENDOR_ID = 0x067b;
		static final int PL2303_PRODUCT_ID = 0x2303;
		static final int PL2303_PRODUCT_ID_RSAQ2 = 0x04bb;
		static final int PL2303_PRODUCT_ID_DCU11 = 0x1234;
		static final int PL2303_PRODUCT_ID_PHAROS = 0xaaa0;
		static final int PL2303_PRODUCT_ID_RSAQ3 = 0xaaa2;
		static final int PL2303_PRODUCT_ID_ALDIGA = 0x0611;
		static final int PL2303_PRODUCT_ID_MMX = 0x0612;
		static final int PL2303_PRODUCT_ID_GPRS = 0x0609;
		static final int PL2303_PRODUCT_ID_HCR331 = 0x331a;
		static final int PL2303_PRODUCT_ID_MOTOROLA = 0x0307;

		static final int ATEN_VENDOR_ID = 0x0557;
		static final int ATEN_VENDOR_ID2 = 0x0547;
		static final int ATEN_PRODUCT_ID = 0x2008;

		static final int IODATA_VENDOR_ID = 0x04bb;
		static final int IODATA_PRODUCT_ID = 0x0a03;
		static final int IODATA_PRODUCT_ID_RSAQ5 = 0x0a0e;

		static final int ELCOM_VENDOR_ID = 0x056e;
		static final int ELCOM_PRODUCT_ID = 0x5003;
		static final int ELCOM_PRODUCT_ID_UCSGT = 0x5004;

		static final int ITEGNO_VENDOR_ID = 0x0eba;
		static final int ITEGNO_PRODUCT_ID = 0x1080;
		static final int ITEGNO_PRODUCT_ID_2080 = 0x2080;

		static final int MA620_VENDOR_ID = 0x0df7;
		static final int MA620_PRODUCT_ID = 0x0620;

		static final int RATOC_VENDOR_ID = 0x0584;
		static final int RATOC_PRODUCT_ID = 0xb000;

		static final int TRIPP_VENDOR_ID = 0x2478;
		static final int TRIPP_PRODUCT_ID = 0x2008;

		static final int RADIOSHACK_VENDOR_ID = 0x1453;
		static final int RADIOSHACK_PRODUCT_ID = 0x4026;

		static final int DCU10_VENDOR_ID = 0x0731;
		static final int DCU10_PRODUCT_ID = 0x0528;

		static final int SITECOM_VENDOR_ID = 0x6189;
		static final int SITECOM_PRODUCT_ID = 0x2068;

		/* Alcatel OT535/735 USB cable */
		static final int ALCATEL_VENDOR_ID = 0x11f7;
		static final int ALCATEL_PRODUCT_ID = 0x02df;

		/* Samsung I330 phone cradle */
		static final int SAMSUNG_VENDOR_ID = 0x04e8;
		static final int SAMSUNG_PRODUCT_ID = 0x8001;

		static final int SIEMENS_VENDOR_ID = 0x11f5;
		static final int SIEMENS_PRODUCT_ID_SX1 = 0x0001;
		static final int SIEMENS_PRODUCT_ID_X65 = 0x0003;
		static final int SIEMENS_PRODUCT_ID_X75 = 0x0004;
		static final int SIEMENS_PRODUCT_ID_EF81 = 0x0005;

		static final int SYNTECH_VENDOR_ID = 0x0745;
		static final int SYNTECH_PRODUCT_ID = 0x0001;

		/* Nokia CA-42 Cable */
		static final int NOKIA_CA42_VENDOR_ID = 0x078b;
		static final int NOKIA_CA42_PRODUCT_ID = 0x1234;

		/* CA-42 CLONE Cable www.ca-42.com chipset: Prolific Technology Inc */
		static final int CA_42_CA42_VENDOR_ID = 0x10b5;
		static final int CA_42_CA42_PRODUCT_ID = 0xac70;

		static final int SAGEM_VENDOR_ID = 0x079b;
		static final int SAGEM_PRODUCT_ID = 0x0027;

		/* Leadtek GPS 9531 (ID 0413:2101) */
		static final int LEADTEK_VENDOR_ID = 0x0413;
		static final int LEADTEK_9531_PRODUCT_ID = 0x2101;

		/* USB GSM cable from Speed Dragon Multimedia, Ltd */
		static final int SPEEDDRAGON_VENDOR_ID = 0x0e55;
		static final int SPEEDDRAGON_PRODUCT_ID = 0x110b;

		/* DATAPILOT Universal-2 Phone Cable */
		static final int DATAPILOT_U2_VENDOR_ID = 0x0731;
		static final int DATAPILOT_U2_PRODUCT_ID = 0x2003;

		/* Belkin "F5U257" Serial Adapter */
		static final int BELKIN_VENDOR_ID = 0x050d;
		static final int BELKIN_PRODUCT_ID = 0x0257;

		/* Alcor Micro Corp. USB 2.0 TO RS-232 */
		static final int ALCOR_VENDOR_ID = 0x058F;
		static final int ALCOR_PRODUCT_ID = 0x9720;

		/* Willcom WS002IN Data Driver (by NetIndex Inc.) */
		static final int WS002IN_VENDOR_ID = 0x11f6;
		static final int WS002IN_PRODUCT_ID = 0x2001;

		/* Corega CG-USBRS232R Serial Adapter */
		static final int COREGA_VENDOR_ID = 0x07aa;
		static final int COREGA_PRODUCT_ID = 0x002a;

		/* Y.C. Cable U.S.A., Inc - USB to RS-232 */
		static final int YCCABLE_VENDOR_ID = 0x05ad;
		static final int YCCABLE_PRODUCT_ID = 0x0fba;

		/* "Superial" USB - Serial */
		static final int SUPERIAL_VENDOR_ID = 0x5372;
		static final int SUPERIAL_PRODUCT_ID = 0x2303;

		/* Hewlett-Packard LD220-HP POS Pole Display */
		static final int HP_VENDOR_ID = 0x03f0;
		static final int HP_LD220_PRODUCT_ID = 0x3524;

		/* Cressi Edy (diving computer) PC interface */
		static final int CRESSI_VENDOR_ID = 0x04b8;
		static final int CRESSI_EDY_PRODUCT_ID = 0x0521;

		/* Zeagle dive computer interface */
		static final int ZEAGLE_VENDOR_ID = 0x04b8;
		static final int ZEAGLE_N2ITION3_PRODUCT_ID = 0x0522;

		/* Sony, USB data cable for CMD-Jxx mobile phones */
		static final int SONY_VENDOR_ID = 0x054c;
		static final int SONY_QN3USB_PRODUCT_ID = 0x0437;

		/* Sanwa KB-USB2 multimeter cable (ID: 11ad:0001) */
		static final int SANWA_VENDOR_ID = 0x11ad;
		static final int SANWA_PRODUCT_ID = 0x0001;

		/* ADLINK ND-6530 RS232,RS485 and RS422 adapter */
		static final int ADLINK_VENDOR_ID = 0x0b63;
		static final int ADLINK_ND6530_PRODUCT_ID = 0x6530;

		/* SMART USB Serial Adapter */
		static final int SMART_VENDOR_ID = 0x0b8c;
		static final int SMART_PRODUCT_ID = 0x2303;

	}

	// 立刻写入如果连接没有建立，则返回false，否则返回true表示消息发送到了服务
	public int POS_Write(byte[] data) {
		return write(data, data.length);
	}

	public int POS_Read(byte[] data) {
		return read(data, data.length);
	}

	/**
	 * 复位打印机
	 */
	public void POS_Reset() {
		byte[] data = Cmd.ESCCmd.ESC_ALT;
		POS_Write(data);
	}

	// nFontType 0 标准 1 压缩 其他不指定
	public void POS_S_TextOut(String pszString, int nOrgx, int nWidthTimes,
			int nHeightTimes, int nFontType, int nFontStyle) {
		if (nOrgx > 65535 | nOrgx < 0 | nWidthTimes > 7 | nWidthTimes < 0
				| nHeightTimes > 7 | nHeightTimes < 0 | nFontType < 0
				| nFontType > 4)
			return;

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);

		byte[] intToWidth = { 0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70 };
		byte[] intToHeight = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
		Cmd.ESCCmd.GS_exclamationmark_n[2] = (byte) (intToWidth[nWidthTimes] + intToHeight[nHeightTimes]);

		byte[] tmp_ESC_M_n = Cmd.ESCCmd.ESC_M_n;
		if ((nFontType == 0) || (nFontType == 1))
			tmp_ESC_M_n[2] = (byte) nFontType;
		else
			tmp_ESC_M_n = new byte[0];

		// 字体风格
		// 暂不支持平滑处理
		Cmd.ESCCmd.GS_E_n[2] = (byte) ((nFontStyle >> 3) & 0x01);

		Cmd.ESCCmd.ESC_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);
		Cmd.ESCCmd.FS_line_n[2] = (byte) ((nFontStyle >> 7) & 0x03);

		Cmd.ESCCmd.ESC_lbracket_n[2] = (byte) ((nFontStyle >> 9) & 0x01);

		Cmd.ESCCmd.GS_B_n[2] = (byte) ((nFontStyle >> 10) & 0x01);

		Cmd.ESCCmd.ESC_V_n[2] = (byte) ((nFontStyle >> 12) & 0x01);

		byte[] pbString = null;
		try {
			pbString = pszString.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_exclamationmark_n,
				tmp_ESC_M_n, Cmd.ESCCmd.GS_E_n, Cmd.ESCCmd.ESC_line_n,
				Cmd.ESCCmd.FS_line_n, Cmd.ESCCmd.ESC_lbracket_n,
				Cmd.ESCCmd.GS_B_n, Cmd.ESCCmd.ESC_V_n, pbString });

		POS_Write(data);

	}

	public void POS_FeedLine() {
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] { Cmd.ESCCmd.CR,
				Cmd.ESCCmd.LF });
		// byte[] data = Cmd.ESCCmd.LF;
		POS_Write(data);
	}

	public void POS_S_Align(int align) {
		if (align < 0 || align > 2)
			return;
		byte[] data = Cmd.ESCCmd.ESC_a_n;
		data[2] = (byte) align;
		POS_Write(data);
	}

	/**
	 * 设置打印区域宽度
	 * 
	 * @param nWidth
	 */
	public void POS_S_SetAreaWidth(int nWidth) {
		if (nWidth < 0 | nWidth > 65535)
			return;

		byte nL = (byte) (nWidth % 0x100);
		byte nH = (byte) (nWidth / 0x100);
		Cmd.ESCCmd.GS_W_nL_nH[2] = nL;
		Cmd.ESCCmd.GS_W_nL_nH[3] = nH;
		byte[] data = Cmd.ESCCmd.GS_W_nL_nH;
		POS_Write(data);
	}
	
	public void POS_SetLineHeight(int nHeight) {
		if (nHeight < 0 || nHeight > 255)
			return;
		byte[] data = Cmd.ESCCmd.ESC_3_n;
		data[2] = (byte) nHeight;
		POS_Write(data);
	}
	
	public void POS_S_SetBarcode(String strCodedata, int nOrgx, int nType,
			int nWidthX, int nHeight, int nHriFontType, int nHriFontPosition) {
		if (nOrgx < 0 | nOrgx > 65535 | nType < 0x41 | nType > 0x49
				| nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
		Cmd.ESCCmd.ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_h_n[2] = (byte) nHeight;
		Cmd.ESCCmd.GS_f_n[2] = (byte) (nHriFontType & 0x01);
		Cmd.ESCCmd.GS_H_n[2] = (byte) (nHriFontPosition & 0x03);
		Cmd.ESCCmd.GS_k_m_n_[2] = (byte) nType;
		Cmd.ESCCmd.GS_k_m_n_[3] = (byte) bCodeData.length;

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.ESC_dollors_nL_nH, Cmd.ESCCmd.GS_w_n,
				Cmd.ESCCmd.GS_h_n, Cmd.ESCCmd.GS_f_n, Cmd.ESCCmd.GS_H_n,
				Cmd.ESCCmd.GS_k_m_n_, bCodeData });
		POS_Write(data);

	}

	public void POS_S_SetQRcode(String strCodedata, int nWidthX,
			int nErrorCorrectionLevel) {

		if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1
				| nErrorCorrectionLevel > 4)
			return;

		byte[] bCodeData = null;
		try {
			bCodeData = strCodedata.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			return;
		}
		;

		Cmd.ESCCmd.GS_w_n[2] = (byte) nWidthX;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
		Cmd.ESCCmd.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);

		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.ESCCmd.GS_w_n, Cmd.ESCCmd.GS_k_m_v_r_nL_nH, bCodeData });
		POS_Write(data);

	}

	/**
	 * 设置字符集和代码页
	 * 
	 * @param nCharSet
	 * @param nCodePage
	 */
	public void POS_SetCharSetAndCodePage(int nCharSet, int nCodePage) {
		if (nCharSet < 0 | nCharSet > 15 | nCodePage < 0 | nCodePage > 19
				| (nCodePage > 10 & nCodePage < 16))
			return;

		Cmd.ESCCmd.ESC_R_n[2] = (byte) nCharSet;
		Cmd.ESCCmd.ESC_t_n[2] = (byte) nCodePage;
		POS_Write(Cmd.ESCCmd.ESC_R_n);
		POS_Write(Cmd.ESCCmd.ESC_t_n);
	}
	
	/**
	 * 设置移动单位
	 * 
	 * @param nHorizontalMU
	 * @param nVerticalMU
	 */
	public void POS_SetMotionUnit(int nHorizontalMU, int nVerticalMU) {
		if (nHorizontalMU < 0 || nHorizontalMU > 255 || nVerticalMU < 0
				|| nVerticalMU > 255)
			return;

		byte[] data = Cmd.ESCCmd.GS_P_x_y;
		data[2] = (byte) nHorizontalMU;
		data[3] = (byte) nVerticalMU;
		POS_Write(data);
	}
	
	/**
	 * 设置字符右间距
	 * 
	 * @param nDistance
	 */
	public void POS_SetRightSpacing(int nDistance) {
		if (nDistance < 0 | nDistance > 255)
			return;

		Cmd.ESCCmd.ESC_SP_n[2] = (byte) nDistance;
		byte[] data = Cmd.ESCCmd.ESC_SP_n;
		POS_Write(data);
	}
	
	// 暂时先这样，可以用request，验证返回数据
	public void POS_SetSystemInfo(String name, String sn) {
		if ((name == null) || (sn == null))
			return;

		byte[] bname = name.getBytes();
		byte[] bsn = sn.getBytes();

		if (bname.length > Cmd.Constant.FAC_MAX_NAME_LEN
				|| bsn.length > Cmd.Constant.FAC_MAX_SN_LEN)
			return;
		int nlength = bname.length + bsn.length + 2;
		byte[] zero = { 0 };
		byte[] data = DataUtils.byteArraysToBytes(new byte[][] {
				Cmd.PCmd.setSystemInfo, bname, zero, bsn, zero });
		data[8] = (byte) (nlength & 0xff);
		data[9] = (byte) ((nlength & 0xff00) >> 8);
		data[10] = DataUtils.bytesToXor(data, 0, 10);
		data[11] = DataUtils.bytesToXor(data, 12, nlength);
		POS_Write(data);
	}
	
	public void debug(String msg) {
		if (!debug)
			return;
		if ((null == mContext) || (null == msg))
			return;
		Intent intent = new Intent(DEBUG);
		intent.putExtra(EXTRA_DEBUG, msg);
		mContext.sendBroadcast(intent);
	}

	private String endpointInfo(UsbEndpoint ep) {
		if (null == ep)
			return "NULL";

		String epInfo = "Description: " + ep.toString();
		epInfo += "\nEndpoint Type: " + ep.getType();
		epInfo += "\nEndpoint Number: " + ep.getEndpointNumber();
		epInfo += "\nEndpoint Direction: "
				+ (ep.getDirection() == 0 ? "Out" : "In");
		epInfo += "\n";
		return epInfo;
	}

}
