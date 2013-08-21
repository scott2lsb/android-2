package com.lvrenyang.printescheme;

import com.lvrenyang.settingactivitys.AutoConnect;

import android.content.Intent;
import btmanager.ConnectThread;
import btmanager.Pos;
import btmanager.ReadThread;
import btmanager.WriteThread;

public class DaemonThread extends Thread {

	@Override
	public void run() {

		_initHandler();
		_manageConnection();

	}

	private static void _initHandler() {
		while (true) {
			if ((ConnectThread.connectHandler != null)
					&& (WriteThread.writeHandler != null)
					&& (ReadThread.readHandler != null)) {
				BtService.getServiceContext().sendBroadcast(
						new Intent(BtService.ACTION_SERVICEREADY));
				break;
			}
		}
	}

	/**
	 * 判断什么时候需要发送关闭连接信号，什么时候需要发送等待连接信号 等待什么的，太烦了，直接主动连。
	 * 
	 * @param t
	 * @param runnable
	 * 
	 */
	private static void _manageConnection() {
		while (BtService.isRunning()) {
			// 别的线程临时需要
			if (!BtService.stopAutoConnect) {
				// 说明开启了自动连接
				if (AutoConnect.getAutoConnectMode().equals(
						AutoConnect.VALUE_autoConnectModeActive)) {
					// 应该判断连接线程的状态，
					// 如果正在进行主动连接，不能发送该消息，因为该消息会取消掉前面的
					// 而POS_Open不同，因为既然是主动点，肯定要取消掉前面的
					/**
					 * 判断需要进行连接的情况，调用函数进行打开
					 */

					if (!Pos.POS_isConnected()) {
						// 没有连接，才能再进行自动连接，无法判断连接是主动的还是被动建立的，
						// 底下都是同等对待

						if (!Pos.POS_isConnecting()) {
							// 如果也不是正在进行连接，而是处于其他状态
							// 可以打开

							Pos.POS_Open(AutoConnect.getAutoConnectMac());
						}

					}

				} else if (AutoConnect.getAutoConnectMode().equals(
						AutoConnect.VALUE_autoConnectModeWait)) {
					if (!Pos.POS_isConnected()) {
						if (!Pos.POS_isConnecting()) {
							Pos.POS_OpenAsServer(AutoConnect
									.getAutoConnectMac());
						}

					}
				}
				// TimeUtils.waitTime(2000);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
}
