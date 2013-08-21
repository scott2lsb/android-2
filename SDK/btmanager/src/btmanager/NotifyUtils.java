package btmanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class NotifyUtils {

	private static int index = Integer.MAX_VALUE;
	private int notifyId;
	private NotificationCompat.Builder mBuilder;
	private NotificationManager mNotificationManager;

	public static final String CLEAR_STACK = "CLEAR_STACK";

	public NotifyUtils(Context context, PendingIntent resultPendingIntent) {
		notifyId = index--;
		mBuilder = new NotificationCompat.Builder(context);

		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public NotifyUtils(Context context, PendingIntent resultPendingIntent,
			RemoteViews views) {
		notifyId = index--;
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setContent(views);
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	/**
	 * 显示通知
	 * 
	 * @param drawableIcon
	 *            图标
	 * @param title
	 *            标题
	 * @param text
	 *            文本
	 * @param info
	 *            信息
	 * @param maxProgress
	 *            滚动条最大值
	 * @param progress
	 *            滚动条当前值
	 * @param indeterminate
	 *            是否动作
	 * @param ongoing
	 */
	public void showNotification(int drawableIcon, CharSequence title,
			CharSequence text, CharSequence info, CharSequence ticker,
			int maxProgress, int progress, boolean indeterminate,
			boolean ongoing) {
		mBuilder.setSmallIcon(drawableIcon);
		mBuilder.setContentTitle(title);
		mBuilder.setContentText(text);
		mBuilder.setContentInfo(info);
		if (ticker.length() != 0)
			mBuilder.setTicker(ticker);
		mBuilder.setProgress(maxProgress, progress, indeterminate);
		mBuilder.setOngoing(ongoing);
		mNotificationManager.notify(notifyId, mBuilder.build());
	}

	public void showNotification(int drawableIcon, boolean ongoing) {
		mBuilder.setSmallIcon(drawableIcon);
		mBuilder.setOngoing(ongoing);
		mNotificationManager.notify(notifyId, mBuilder.build());
	}

	public void cancelNotification() {
		mNotificationManager.cancel(notifyId);
	}
}
