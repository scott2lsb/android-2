package com.lvrenyang.utils;

public class ErrorCode {

	public static final int ERROR = -1000;
	/**
	 * 空指针错误
	 */
	public static final int NULLPOINTER = -1001;
	public static final int NOPERMISSION = -1002;

	/**
	 * 参数不符合要求，如n需要大于等于0但给出的是小于0
	 */
	public static final int INVALPARAM = -1003;
	
	public static final int EXCEPTION = -1004;
	
	public static final int NOTCONNECTED = -1005;
	
	public static final int NOTOPENED = -1006;
	
	public static final int NOTIMPLEMENTED = -1007;
}
