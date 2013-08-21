package btmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LayoutUtils {

	public static RelativeLayout initContentView(Activity activity,
			int layoutActionBar, int layoutMain) {
		RelativeLayout rl = new RelativeLayout(activity);
		View viewActionBar = LayoutInflater.from(activity).inflate(
				layoutActionBar, null);
		rl.addView(viewActionBar);

		View viewMain = LayoutInflater.from(activity).inflate(layoutMain, null);
		RelativeLayout.LayoutParams relLayoutParams = new android.widget.RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		relLayoutParams.addRule(RelativeLayout.BELOW, viewActionBar.getId());
		rl.addView(viewMain, relLayoutParams);

		activity.setContentView(rl);
		return rl;
	}

	public static ProgressDialog showDialog(Context context,
			CharSequence message) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage(message);
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();
		return dialog;
	}

	/**
	 * 从给定的assets中读取数据，并按照一定的格式解析数据 将得到的布局添加到parent中
	 * 
	 * @param parent
	 *            out 输出型参数
	 * @param name
	 *            in
	 */
	public static void readLayoutFromAssets(LinearLayout parent, String name) {
		try {
			InputStreamReader isr = new InputStreamReader(parent.getContext()
					.getAssets().open(name), "GBK");
			BufferedReader br = new BufferedReader(isr);
			String[] line = new String[1024];
			int i = 0;
			while ((line[i] = br.readLine()) != null) {
				i++;
			}
			String[] realline = new String[i];
			for (i = 0; i < realline.length; i++)
				realline[i] = line[i];

			readLayoutFromString(parent, realline);

		} catch (IOException e) {
			// Should never happen!
			throw new RuntimeException(e);
		}
	}

	private static int desStartIndex = 0;

	private static void readLayoutFromString(LinearLayout parent,
			String[] descriptions) {
		desStartIndex = 0;
		__readLayoutFromString(parent, descriptions);
	}

	/**
	 * 给定一个父布局和一组描述字符串，填充相应布局
	 * 
	 * @param parent
	 * @param descriptions
	 */
	private static void __readLayoutFromString(LinearLayout parent,
			String[] descriptions) {
		while (desStartIndex < descriptions.length) {

			if (endLinearLayout(descriptions[desStartIndex])) {
				desStartIndex++;
				return;
			}

			// 有一个textview
			if (startTextView(descriptions[desStartIndex])) {
				TextView textview = new TextView(parent.getContext());
				String text = getContainText(descriptions[desStartIndex]);
				if (text != null)
					textview.setText(text);
				parent.addView(textview);
				desStartIndex++;

			} else if (startEditText(descriptions[desStartIndex])) {
				EditText editText = new EditText(parent.getContext());
				editText.setLayoutParams(getLayoutParams(descriptions[desStartIndex]));
				parent.addView(editText);
				desStartIndex++;

			} else if (startLinearLayout(descriptions[desStartIndex])) {
				LinearLayout linearLayout = new LinearLayout(
						parent.getContext());
				linearLayout
						.setOrientation(getOrientation(descriptions[desStartIndex]));
				linearLayout
						.setLayoutParams(getLayoutParams(descriptions[desStartIndex]));
				linearLayout
						.setWeightSum(getLayoutWeight(descriptions[desStartIndex]));
				parent.addView(linearLayout);
				desStartIndex++;
				__readLayoutFromString(linearLayout, descriptions);
			} else {
				desStartIndex++;
			}
		}

	}

	private static final String TEXTVIEWPREFIX = "<TextView";
	private static final String EDITTEXTPREFIX = "<EditText";
	private static final String LINEARLAYOUTPREFIX = "<LinearLayout";
	private static final String LINEARLAYOUTENDFIX = "</LinearLayout>";

	private static final String TEXTPREFIX = "android:text=";
	private static final String LAYOUT_WEIGHT = "android:layout_weight=";

	private static final String ORIENTATION_VERTICAL = "android:orientation=\"vertical\"";
	private static final String LAYOUT_WIDTH_MATCH_PARENT = "android:layout_width=\"match_parent\"";
	private static final String LAYOUT_HEIGHT_MATCH_PARENT = "android:layout_height=\"match_parent\"";
	private static final String MARKS = "\"";

	private static boolean startTextView(String description) {
		return description.contains(TEXTVIEWPREFIX);
	}

	private static boolean startEditText(String description) {

		return description.contains(EDITTEXTPREFIX);
	}

	private static boolean startLinearLayout(String description) {

		return description.contains(LINEARLAYOUTPREFIX);
	}

	private static boolean endLinearLayout(String description) {

		return description.contains(LINEARLAYOUTENDFIX);
	}

	private static int getOrientation(String description) {
		if (description.contains(ORIENTATION_VERTICAL))
			return LinearLayout.VERTICAL;
		else
			return LinearLayout.HORIZONTAL;
	}

	private static LayoutParams getLayoutParams(String description) {
		int layout_width = LayoutParams.WRAP_CONTENT;
		int layout_height = LayoutParams.WRAP_CONTENT;

		if (description.contains(LAYOUT_WIDTH_MATCH_PARENT))
			layout_width = LayoutParams.MATCH_PARENT;
		if (description.contains(LAYOUT_HEIGHT_MATCH_PARENT))
			layout_height = LayoutParams.MATCH_PARENT;

		return new LayoutParams(layout_width, layout_height);

	}

	private static int getLayoutWeight(String description) {
		if (description.contains(LAYOUT_WEIGHT)) {
			int start = description.indexOf(LAYOUT_WEIGHT)
					+ LAYOUT_WEIGHT.length() + MARKS.length();
			int end = description.indexOf(MARKS, start);
			return Integer.parseInt(description.subSequence(start, end)
					.toString());

		} else {
			return 0;
		}
	}

	private static String getContainText(String description) {
		if (description.contains(TEXTPREFIX)) {
			int start = description.indexOf(TEXTPREFIX) + TEXTPREFIX.length()
					+ MARKS.length();
			return description.subSequence(start,
					description.lastIndexOf(MARKS)).toString();
		} else {
			return "";
		}
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static final int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static final int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

}
