package com.lvrenyang.rw;

/**
 * 该类只有2个构造函数和2个公共变量，再没有其他内容 这两个变量，可以在new的时候指定，也可以在new的时候置空，稍后通过访问成员来指定
 * 
 * @author Administrator
 * 
 */
public class USBSerialPort {

	public USBPort port;
	public TTYTermios termios;

	public USBSerialPort(USBPort port, TTYTermios termios) {
		this.port = port;
		this.termios = termios;
	}
}
