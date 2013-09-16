package com.lvrenyang.rw;

import com.lvrenyang.utils.DataUtils;
import com.lvrenyang.utils.ErrorCode;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbEndpoint;

/**
 * 第一层，USB驱动 围绕着USBPort来进行
 * 
 * @author Administrator
 * 
 */
public class USBDriver {

	String description;
	

	int probe(USBPort port, USBDeviceId id[]) {
		if (null == port || null == id)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbManager)
			return ErrorCode.NULLPOINTER;
		if (null == port.mContext)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbDevice)
			return ErrorCode.NULLPOINTER;
		if (null == port.mPermissionIntent)
			return ErrorCode.NULLPOINTER;

		for (int i = 0; i < id.length; i++)
			if (id[i].idVendor == port.mUsbDevice.getVendorId()
					&& id[i].idProduct == port.mUsbDevice.getProductId()) {
				if (!port.mUsbManager.hasPermission(port.mUsbDevice))
					port.mUsbManager.requestPermission(port.mUsbDevice,
							port.mPermissionIntent);

				if (!port.mUsbManager.hasPermission(port.mUsbDevice))
					return ErrorCode.NOPERMISSION;

				// 枚举，把读写控制端口什么的给弄过来。然后set
				outer: for (int k = 0; k < port.mUsbDevice.getInterfaceCount(); k++) {
					port.mUsbInterface = port.mUsbDevice.getInterface(i);
					port.mUsbEndpointOut = null;
					port.mUsbEndpointIn = null;
					for (int j = 0; j < port.mUsbInterface.getEndpointCount(); j++) {
						UsbEndpoint endpoint = port.mUsbInterface
								.getEndpoint(j);
						if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT
								&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
							port.mUsbEndpointOut = endpoint;
						} else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN
								&& endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
							port.mUsbEndpointIn = endpoint;
						}

						// 如果在第一个接口就找到了符合要求的端点，那么break;
						if ((null != port.mUsbEndpointOut)
								&& (null != port.mUsbEndpointIn))
							break outer;
					}
				}
				if (null == port.mUsbInterface)
					return ErrorCode.NULLPOINTER;
				if ((null == port.mUsbEndpointOut)
						|| (null == port.mUsbEndpointIn))
					return ErrorCode.NULLPOINTER;
				port.mUsbDeviceConnection = port.mUsbManager
						.openDevice(port.mUsbDevice);
				if (null == port.mUsbDeviceConnection)
					return ErrorCode.NULLPOINTER;
				port.mUsbDeviceConnection.claimInterface(port.mUsbInterface,
						true);
				return 0;
			}

		return ErrorCode.ERROR;
	}

	void disconnect(USBPort port) {
		if (null == port)
			return;
		if ((null != port.mUsbInterface) && (null != port.mUsbDeviceConnection)) {
			port.mUsbDeviceConnection.releaseInterface(port.mUsbInterface);
			port.mUsbDeviceConnection.close();
		}
	}

	int write(USBPort port, byte[] buffer, int offset, int count, int timeout) {
		if (null == port || null == buffer)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbEndpointOut)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbDeviceConnection)
			return ErrorCode.NULLPOINTER;
		if (count < 0 || offset < 0 || timeout <= 0)
			return ErrorCode.INVALPARAM;
		byte[] data = new byte[count];
		DataUtils.copyBytes(buffer, offset, data, 0, count);
		return port.mUsbDeviceConnection.bulkTransfer(port.mUsbEndpointOut,
				data, data.length, timeout);
	}

	int read(USBPort  port, byte[] buffer, int offset, int count, int timeout) {
		if (null == port || null == buffer)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbEndpointIn)
			return ErrorCode.NULLPOINTER;
		if (null == port.mUsbDeviceConnection)
			return ErrorCode.NULLPOINTER;
		if (count < 0 || offset < 0 || timeout <= 0)
			return ErrorCode.INVALPARAM;
		byte[] data = new byte[count];
		int recnt = port.mUsbDeviceConnection.bulkTransfer(port.mUsbEndpointIn,
				data, data.length, timeout);
		DataUtils.copyBytes(data, 0, buffer, offset, recnt);
		return recnt; // 返回读取的字节数
	}

	int ctl(USBPort port, int requestType, int request, int value, int index,
			byte[] buffer, int length, int timeout) {
		if (null == port)
			return ErrorCode.INVALPARAM;
		if (null == port.mUsbDeviceConnection)
			return ErrorCode.INVALPARAM;

		return port.mUsbDeviceConnection.controlTransfer(requestType, request,
				value, index, buffer, length, timeout);
	}
}

class USBDeviceId {
	int idVendor;
	int idProduct;

	public USBDeviceId(int vid, int pid) {
		idVendor = vid;
		idProduct = pid;
	}
}