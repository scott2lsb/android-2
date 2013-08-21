package com.lvrenyang.textandpictureactivitys;

import btmanager.Cmd;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SetAndShow extends Activity implements OnClickListener,
		OnLongClickListener, OnSeekBarChangeListener {

	public static final String PREFERENCES_TEXT_nDarkness = "PREFERENCES_TEXT_nDarkness";
	public static final String PREFERENCES_TEXT_nFontSize = "PREFERENCES_TEXT_nFontSize";
	public static final String PREFERENCES_TEXT_nTextAlign = "PREFERENCES_TEXT_nTextAlign";
	public static final String PREFERENCES_TEXT_nScaleTimesWidth = "PREFERENCES_TEXT_nScaleTimesWidth";
	public static final String PREFERENCES_TEXT_nScaleTimesHeight = "PREFERENCES_TEXT_nScaleTimesHeight";
	public static final String PREFERENCES_TEXT_nFontStyle = "PREFERENCES_TEXT_nFontStyle";
	public static final String PREFERENCES_TEXT_nLineHeight = "PREFERENCES_TEXT_nLineHeight";
	public static final String PREFERENCES_TEXT_nRightSpace = "PREFERENCES_TEXT_nRightSpace";

	private TextView tvTopic;
	private Button btBack;

	private Button btPrint, btDarkness, btFontSize, btTextAlign,
			btScaleTimesWidth, btScaleTimesHeight;
	private CheckBox cbBlackWhiteReverse, cbBold, cbUpsideDown, cbTurnRight90,
			cbUnderLine1, cbUnderLine2;
	private SeekBar sbLineHeight, sbRightSpace;
	private TextView tvLineHeight, tvRightSpace;

	public static int nDarkness, nFontSize, nTextAlign, nScaleTimesWidth,
			nScaleTimesHeight, nFontStyle, nLineHeight = 32, nRightSpace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(SetAndShow.this, R.layout.actionbar,
				R.layout.activity_textandpicture_showandsetting);

		btPrint = (Button) findViewById(R.id.btPrint);
		btDarkness = (Button) findViewById(R.id.btDarkness);
		btFontSize = (Button) findViewById(R.id.btFontSize);
		btTextAlign = (Button) findViewById(R.id.btTextAlign);
		btScaleTimesWidth = (Button) findViewById(R.id.btScaleTimesWidth);
		btScaleTimesHeight = (Button) findViewById(R.id.btScaleTimesHeight);

		btPrint.setOnClickListener(this);
		btDarkness.setOnClickListener(this);
		btFontSize.setOnClickListener(this);
		btTextAlign.setOnClickListener(this);
		btScaleTimesWidth.setOnClickListener(this);
		btScaleTimesHeight.setOnClickListener(this);

		cbBlackWhiteReverse = (CheckBox) findViewById(R.id.cbBlackWhiteReverse);
		cbBold = (CheckBox) findViewById(R.id.cbBold);
		cbUpsideDown = (CheckBox) findViewById(R.id.cbUpsideDown);
		cbTurnRight90 = (CheckBox) findViewById(R.id.cbTurnRight90);
		cbUnderLine1 = (CheckBox) findViewById(R.id.cbUnderLine1);
		cbUnderLine2 = (CheckBox) findViewById(R.id.cbUnderLine2);

		cbBlackWhiteReverse.setOnClickListener(this);
		cbBold.setOnClickListener(this);
		cbUpsideDown.setOnClickListener(this);
		cbTurnRight90.setOnClickListener(this);
		cbUnderLine1.setOnClickListener(this);
		cbUnderLine2.setOnClickListener(this);

		sbLineHeight = (SeekBar) findViewById(R.id.sbLineHeight);
		sbLineHeight.setMax(255);
		sbLineHeight.setOnSeekBarChangeListener(this);
		sbRightSpace = (SeekBar) findViewById(R.id.sbRightSpace);
		sbRightSpace.setMax(255);
		sbRightSpace.setOnSeekBarChangeListener(this);

		tvLineHeight = (TextView) findViewById(R.id.tvLineHeight);
		tvRightSpace = (TextView) findViewById(R.id.tvRightSpace);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setandshow));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);
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
		updateSetAndShowUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
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

		case R.id.btPrint: {

			SetAndShow
					.printWithAllStyle(getString(R.string.sample_setandshow_01));
			Pos.POS_FeedLine();
			SetAndShow
					.printWithAllStyle(getString(R.string.sample_setandshow_02));
			Pos.POS_FeedLine();
			SetAndShow
					.printWithAllStyle(getString(R.string.sample_setandshow_03));
			for (int i = 0; i < 3; i++)
				Pos.POS_FeedLine();

			break;
		}

		case R.id.btDarkness: {

			AlertDialog dialog = new AlertDialog.Builder(SetAndShow.this)
					.setTitle(R.string.darkness)
					.setItems(R.array.darkness,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.darkness);
									btDarkness.setText(items[which]);
									nDarkness = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btFontSize: {

			AlertDialog dialog = new AlertDialog.Builder(SetAndShow.this)
					.setTitle(R.string.fontsize)
					.setItems(R.array.fontsize,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.fontsize);
									btFontSize.setText(items[which]);
									nFontSize = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btTextAlign: {
			AlertDialog dialog = new AlertDialog.Builder(SetAndShow.this)
					.setTitle(R.string.textalign)
					.setItems(R.array.textalign,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.textalign);
									btTextAlign.setText(items[which]);
									nTextAlign = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btScaleTimesWidth: {
			AlertDialog dialog = new AlertDialog.Builder(SetAndShow.this)
					.setTitle(R.string.width)
					.setItems(R.array.scaletimes_width_max2,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.scaletimes_width_max2);
									btScaleTimesWidth.setText(items[which]);
									nScaleTimesWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btScaleTimesHeight: {

			AlertDialog dialog = new AlertDialog.Builder(SetAndShow.this)
					.setTitle(R.string.height)
					.setItems(R.array.scaletimes_height_max2,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.scaletimes_height_max2);
									btScaleTimesHeight.setText(items[which]);
									nScaleTimesHeight = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.cbBlackWhiteReverse:
		case R.id.cbBold:
		case R.id.cbUpsideDown:
		case R.id.cbTurnRight90:
		case R.id.cbUnderLine1:
		case R.id.cbUnderLine2: {
			updateFontStyle();
			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}

	}

	// ¸üÐÂnFontStyle
	private void updateFontStyle() {
		nFontStyle = Cmd.Constant.FONTSTYLE_NORMAL;
		if (cbBlackWhiteReverse.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_BLACKWHITEREVERSE;
		if (cbBold.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_BOLD;
		if (cbUpsideDown.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UPSIDEDOWN;
		if (cbTurnRight90.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_TURNRIGHT90;
		if (cbUnderLine1.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UNDERLINE1;
		if (cbUnderLine2.isChecked())
			nFontStyle |= Cmd.Constant.FONTSTYLE_UNDERLINE2;

	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putInt(PREFERENCES_TEXT_nDarkness, nDarkness);
				editor.putInt(PREFERENCES_TEXT_nFontSize, nFontSize);
				editor.putInt(PREFERENCES_TEXT_nTextAlign, nTextAlign);
				editor.putInt(PREFERENCES_TEXT_nScaleTimesWidth,
						nScaleTimesWidth);
				editor.putInt(PREFERENCES_TEXT_nScaleTimesHeight,
						nScaleTimesHeight);
				editor.putInt(PREFERENCES_TEXT_nFontStyle, nFontStyle);
				editor.putInt(PREFERENCES_TEXT_nLineHeight, nLineHeight);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}

	private void updateSetAndShowUI() {
		// Configue

		btDarkness
				.setText(getResources().getStringArray(R.array.darkness)[nDarkness]);
		btFontSize
				.setText(getResources().getStringArray(R.array.fontsize)[nFontSize]);
		btTextAlign
				.setText(getResources().getStringArray(R.array.textalign)[nTextAlign]);
		btScaleTimesWidth.setText(getResources().getStringArray(
				R.array.scaletimes_width_max2)[nScaleTimesWidth]);
		btScaleTimesHeight.setText(getResources().getStringArray(
				R.array.scaletimes_height_max2)[nScaleTimesHeight]);

		cbBlackWhiteReverse
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_BLACKWHITEREVERSE) != 0);
		cbBold.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_BOLD) != 0);
		cbUpsideDown
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UPSIDEDOWN) != 0);
		cbTurnRight90
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_TURNRIGHT90) != 0);
		cbUnderLine1
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UNDERLINE1) != 0);
		cbUnderLine2
				.setChecked((nFontStyle & Cmd.Constant.FONTSTYLE_UNDERLINE2) != 0);

		tvLineHeight
				.setText(getString(R.string.lineheight) + "\n" + nLineHeight);
		sbLineHeight.setProgress(nLineHeight);
		tvRightSpace.setText(getString(R.string.rightspace) + "\n"
				+ nRightSpace);
		sbRightSpace.setProgress(nRightSpace);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		switch (seekBar.getId()) {
		case R.id.sbLineHeight: {
			nLineHeight = progress;
			tvLineHeight.setText(getString(R.string.lineheight) + "\n"
					+ nLineHeight);
			break;
		}

		case R.id.sbRightSpace: {
			nRightSpace = progress;
			tvRightSpace.setText(getString(R.string.rightspace) + "\n"
					+ nRightSpace);
			break;
		}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public static void printWithAllStyle(String text) {
		Pos.POS_S_Align(nTextAlign);
		Pos.POS_SetRightSpacing(nRightSpace);
		Pos.POS_SetLineHeight(nLineHeight);
		Pos.POS_S_TextOut(text, 0, nScaleTimesWidth, nScaleTimesHeight,
				nFontSize, nFontStyle);
	}
}
