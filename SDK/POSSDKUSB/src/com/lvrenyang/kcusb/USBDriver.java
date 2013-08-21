package com.lvrenyang.kcusb;

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

public class USBDriver {

	private UsbManager mUsbManager;
	private Context mContext;
	private PendingIntent mPermissionIntent;

	private int timeOut = 500;
	private UsbDevice mUsbDevice;
	private UsbInterface mUsbInterface;
	private UsbEndpoint mUsbEndpointOut, mUsbEndpointIn;
	private UsbDeviceConnection mUsbDeviceConnection;

	public USBDriver(UsbManager usbManager, Context context, String sAppName) {
		mUsbManager = usbManager;
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
		if (this.timeOut > 0)
			this.timeOut = timeOut;
	}

	/**
	 * 过滤USB设备，如果返回值为空，则说明该设备被过滤掉，否则，该设备可以显示
	 * 
	 * @param usbDevice
	 * @return
	 */
	private UsbDevice filterUsbDevice(UsbDevice usbDevice) {

		/* add filters */
		return usbDevice;
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

		// mUsbDevice = null;
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
				final UsbDevice device = filterUsbDevice(deviceIterator.next());
				if (null == device)
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

	/**
	 * 返回true则表示连接成功
	 * 
	 * @return
	 */
	public boolean connect() {
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
				if ((null != mUsbEndpointOut) && (null != mUsbEndpointIn))
					break outer;
			}
		}
		if (null == mUsbInterface)
			return false;
		if ((null == mUsbEndpointOut) && (null == mUsbEndpointIn))
			return false;
		mUsbDeviceConnection = mUsbManager.openDevice(mUsbDevice);
		if (null == mUsbDeviceConnection)
			return false;
		mUsbDeviceConnection.claimInterface(mUsbInterface, true);

		return true;
	}

	public void disconnect() {
		if ((null != mUsbInterface) && (null != mUsbDeviceConnection)) {
			mUsbDeviceConnection.releaseInterface(mUsbInterface);
			mUsbDeviceConnection.close();
		}
	}

	public int write(byte[] buffer, int length) {
		if ((null == mUsbEndpointOut) || (null == mUsbDeviceConnection)
				|| (null == buffer))
			return -1;
		if (length <= 0 || length > buffer.length)
			return -1;
		return mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, buffer,
				length, timeOut);
	}

	public int read(byte[] buffer, int length) {
		if ((null == mUsbEndpointIn) || (null == mUsbDeviceConnection)
				|| (null == buffer))
			return -1;

		if (length <= 0 || length > buffer.length)
			return -1;
		return mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, buffer,
				length, timeOut);
	}

}
