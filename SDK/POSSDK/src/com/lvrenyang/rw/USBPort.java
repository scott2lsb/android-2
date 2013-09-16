package com.lvrenyang.rw;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * 该类只有一个构造函数和一些变量
 * 
 * @author Administrator
 * 
 */
public class USBPort {

	UsbManager mUsbManager;
	Context mContext;
	UsbDevice mUsbDevice;
	PendingIntent mPermissionIntent;

	UsbInterface mUsbInterface;
	UsbEndpoint mUsbEndpointOut, mUsbEndpointIn;
	UsbDeviceConnection mUsbDeviceConnection;

	public USBPort(UsbManager usbManager, Context context, UsbDevice usbDevice,
			PendingIntent permissionIntent) {
		this.mUsbManager = usbManager;
		this.mContext = context;
		this.mUsbDevice = usbDevice;
		this.mPermissionIntent = permissionIntent;
	}
}
