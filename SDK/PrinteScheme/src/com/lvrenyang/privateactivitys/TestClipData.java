package com.lvrenyang.privateactivitys;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import btmanager.LayoutUtils;

import com.lvrenyang.R;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class TestClipData extends Activity implements OnClickListener,
		OnLongClickListener {

	private Button button1, button2;
	private android.content.ClipboardManager modernClipboard;
	private android.text.ClipboardManager oldClipboard;
	private String uri;
	private String clipText;
	private String intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(TestClipData.this, R.layout.actionbar,
				R.layout.activity_private_testclipdata);
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(this);

		button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(this);

		if (Build.VERSION.SDK_INT >= 11)
			modernClipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		else
			oldClipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1: {

			try {
				updateClipBoard();
				Toast.makeText(
						this,
						"clipText: " + clipText + "\nuri: " + uri
								+ "\nintent: " + intent, Toast.LENGTH_SHORT)
						.show();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		}

		case R.id.button2: {

			break;
		}
		}
	}

	// 如果没有，就会返回""。
	// 更新clipboard的数据到全局
	private void updateClipBoard() throws NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if ((Build.VERSION.SDK_INT >= 11) && (modernClipboard != null)) {

			Class<? extends android.content.ClipboardManager> ClipboardManagerRef = modernClipboard
					.getClass();
			Method getPrimaryClip = ClipboardManagerRef
					.getMethod("getPrimaryClip");
			ClipData clip = (ClipData) getPrimaryClip.invoke(modernClipboard);

			Class<? extends ClipData> ClipDataRef = clip.getClass();
			Method getItemCount = ClipDataRef.getMethod("getItemCount");
			Method getItemAt = ClipDataRef.getMethod("getItemAt", int.class);

			int itemCount = (Integer) getItemCount.invoke(clip);
			if (itemCount > 0) {
				Item item = (Item) getItemAt.invoke(clip, 0);
				Class<? extends Item> ItemRef = item.getClass();
				Method getText = ItemRef.getMethod("getText");
				Method getIntent = ItemRef.getMethod("getIntent");
				Method getUri = ItemRef.getMethod("getUri");

				clipText = (String) getText.invoke(item);
				Intent tmpIntent = (Intent) getIntent.invoke(item);
				if (tmpIntent != null)
					intent = tmpIntent.toUri(0);
				else
					intent = "";
				Uri tmpUri = (Uri) getUri.invoke(item);
				if (tmpUri != null)
					uri = tmpUri.toString();
				else
					uri = "";

			}
		} else if ((Build.VERSION.SDK_INT < 11) && (oldClipboard != null)) {
			Class<? extends android.text.ClipboardManager> ClipboardManagerRef = oldClipboard
					.getClass();
			Method hasText = ClipboardManagerRef.getMethod("hasText");
			Method getText = ClipboardManagerRef.getMethod("getText");
			if ((Boolean) hasText.invoke(oldClipboard)) {
				clipText = (String) getText.invoke(oldClipboard);
			}
		}

	}

}
