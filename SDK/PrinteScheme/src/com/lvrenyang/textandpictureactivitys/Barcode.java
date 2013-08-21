package com.lvrenyang.textandpictureactivitys;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import btmanager.Cmd;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

public class Barcode extends Activity implements OnClickListener,
		OnLongClickListener {

	public static final String PREFERENCES_TEXT_Barcodetype = "PREFERENCES_TEXT_Barcodetype";
	public static final String PREFERENCES_TEXT_StartOrgx = "PREFERENCES_TEXT_StartOrgx";
	public static final String PREFERENCES_TEXT_BarcodeWidth = "PREFERENCES_TEXT_BarcodeWidth";
	public static final String PREFERENCES_TEXT_BarcodeHeight = "PREFERENCES_TEXT_BarcodeHeight";
	public static final String PREFERENCES_TEXT_BarcodeFontType = "PREFERENCES_TEXT_BarcodeFontType";
	public static final String PREFERENCES_TEXT_BarcodeFontPosition = "PREFERENCES_TEXT_BarcodeFontPosition";

	private TextView tvTopic;
	private Button btBack;
	
	private Button buttonPrint;
	private EditText editTextBarcode;
	private Button buttonBarcodetype, buttonStartOrgx, buttonBarcodeWidth,
			buttonBarcodeHeight, buttonBarcodeFontType,
			buttonBarcodeFontPosition;

	public static int nBarcodetype, nStartOrgx, nBarcodeWidth, nBarcodeHeight,
			nBarcodeFontType, nBarcodeFontPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(Barcode.this, R.layout.actionbar,
				R.layout.activity_textandpicture_barcode);
		buttonPrint = (Button) findViewById(R.id.buttonPrint);
		buttonPrint.setOnClickListener(this);

		editTextBarcode = (EditText) findViewById(R.id.editTextBarcode);

		buttonBarcodetype = (Button) findViewById(R.id.buttonBarcodetype);
		buttonBarcodetype.setOnClickListener(this);
		buttonStartOrgx = (Button) findViewById(R.id.buttonStartOrgx);
		buttonStartOrgx.setOnClickListener(this);
		buttonBarcodeWidth = (Button) findViewById(R.id.buttonBarcodeWidth);
		buttonBarcodeWidth.setOnClickListener(this);
		buttonBarcodeHeight = (Button) findViewById(R.id.buttonBarcodeHeight);
		buttonBarcodeHeight.setOnClickListener(this);
		buttonBarcodeFontType = (Button) findViewById(R.id.buttonBarcodeFontType);
		buttonBarcodeFontType.setOnClickListener(this);
		buttonBarcodeFontPosition = (Button) findViewById(R.id.buttonBarcodeFontPosition);
		buttonBarcodeFontPosition.setOnClickListener(this);
		
		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.barcode));
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
		updateBarcodeUI();
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
		case R.id.buttonPrint: {
			String strBarcode = editTextBarcode.getText().toString();
			int nOrgx = nStartOrgx * 12;
			int nType = Cmd.Constant.BARCODE_TYPE_UPC_A + nBarcodetype;
			int nWidthX = nBarcodeWidth + 2;
			int nHeight = (nBarcodeHeight + 1) * 24;
			int nHriFontType = nBarcodeFontType;
			int nHriFontPosition = nBarcodeFontPosition;
			Pos.POS_S_SetBarcode(strBarcode, nOrgx, nType, nWidthX, nHeight,
					nHriFontType, nHriFontPosition);
			break;
		}

		case R.id.buttonBarcodetype: {

			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.barcodetype)
					.setItems(R.array.barcodetype,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.barcodetype);
									buttonBarcodetype.setText(items[which]);
									nBarcodetype = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonStartOrgx: {
			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.startorgx)
					.setItems(R.array.barcodestartorgx,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodestartorgx);
									buttonStartOrgx.setText(items[which]);
									nStartOrgx = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeWidth: {
			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.barcodewidth)
					.setItems(R.array.barcodewidth,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodewidth);
									buttonBarcodeWidth.setText(items[which]);
									nBarcodeWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeHeight: {
			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.barcodeheight)
					.setItems(R.array.barcodeheight,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodeheight);
									buttonBarcodeHeight.setText(items[which]);
									nBarcodeHeight = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeFontType: {
			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.barcodefonttype)
					.setItems(R.array.barcodefonttype,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodefonttype);
									buttonBarcodeFontType.setText(items[which]);
									nBarcodeFontType = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonBarcodeFontPosition: {
			AlertDialog dialog = new AlertDialog.Builder(Barcode.this)
					.setTitle(R.string.barcodefontposition)
					.setItems(R.array.barcodefontposition,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.barcodefontposition);
									buttonBarcodeFontPosition
											.setText(items[which]);
									nBarcodeFontPosition = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	private void updateBarcodeUI() {
		// Configue

		buttonBarcodetype.setText(getResources().getStringArray(
				R.array.barcodetype)[nBarcodetype]);
		buttonStartOrgx.setText(getResources().getStringArray(
				R.array.barcodestartorgx)[nStartOrgx]);
		buttonBarcodeWidth.setText(getResources().getStringArray(
				R.array.barcodewidth)[nBarcodeWidth]);
		buttonBarcodeHeight.setText(getResources().getStringArray(
				R.array.barcodeheight)[nBarcodeHeight]);
		buttonBarcodeFontType.setText(getResources().getStringArray(
				R.array.barcodefonttype)[nBarcodeFontType]);
		buttonBarcodeFontPosition.setText(getResources().getStringArray(
				R.array.barcodefontposition)[nBarcodeFontPosition]);

	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putInt(PREFERENCES_TEXT_Barcodetype, nBarcodetype);
				editor.putInt(PREFERENCES_TEXT_StartOrgx, nStartOrgx);
				editor.putInt(PREFERENCES_TEXT_BarcodeWidth, nBarcodeWidth);
				editor.putInt(PREFERENCES_TEXT_BarcodeHeight, nBarcodeHeight);
				editor.putInt(PREFERENCES_TEXT_BarcodeFontType,
						nBarcodeFontType);
				editor.putInt(PREFERENCES_TEXT_BarcodeFontPosition,
						nBarcodeFontPosition);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}
}
