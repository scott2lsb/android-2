package com.lvrenyang.rw;

/**
 * TTY终端参数，包括波特率，流控制，校验位，停止位，数据位等。
 * @author Administrator
 *
 */
public class TTYTermios {
	/**
	 * 波特率，只支持9600,19200,38400,57600,115200
	 * 但是，SDK里面并没有做检查，需要客户端程序自行检查
	 */
	public int baudrate = 9600;
	
	/**
	 * 流控制，NONE或者DTR_RTS
	 */
	public FlowControl flowControl = FlowControl.NONE;
	
	/**
	 * 校验位，支持无校验，奇校验，偶校验，表奇校验，空白校验
	 */
	public Parity parity = Parity.NONE;
	
	/**
	 * 停止位，1,1.5,2
	 */
	public StopBits stopBits = StopBits.ONE;
	/**
	 * 数据位，支持5,6,7,8，但是SDK里面并没有做检查，需要客户端程序自行检查
	 */
	public int dataBits = 8;

	public TTYTermios(int baudrate, FlowControl flowControl, Parity parity,
			StopBits stopBits, int dataBits) {
		this.baudrate = baudrate;
		this.flowControl = flowControl;
		this.parity = parity;
		this.stopBits = stopBits;
		this.dataBits = dataBits;
	}

	/**
	 * 流控制枚举
	 * @author Administrator
	 *
	 */
	public enum FlowControl {
		NONE, DTR_RTS
	}

	/**
	 * 校验位枚举
	 * @author Administrator
	 *
	 */
	public enum Parity {
		NONE, ODD, EVEN, SPACE, MARK
	}

	/**
	 * 停止位枚举
	 * @author Administrator
	 *
	 */
	public enum StopBits {
		ONE, ONEPFIVE, TWO
	}
}
