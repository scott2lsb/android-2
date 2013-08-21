package btmanager;

public class TimeUtils {

	/**
	 * 等待delay ms 是一个阻塞函数，但不会睡眠
	 * 
	 * @param delay
	 */
	public static void waitTime(int delay) {
		long time = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - time > delay) {
				return;
			}
		}
	}
	

}
