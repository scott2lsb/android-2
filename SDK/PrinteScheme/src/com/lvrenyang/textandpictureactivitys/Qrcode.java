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
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

public class Qrcode extends Activity implements OnClickListener,
		OnLongClickListener {

	public static final String PREFERENCES_TEXT_Qrcodetype = "PREFERENCES_TEXT_Qrcodetype";
	public static final String PREFERENCES_TEXT_QrcodeWidth = "PREFERENCES_TEXT_QrcodeWidth";
	public static final String PREFERENCES_TEXT_ErrorCorrectionLevel = "PREFERENCES_TEXT_ErrorCorrectionLevel";
	
	private TextView tvTopic;
	private Button btBack;
	
	private Button buttonPrint;
	private EditText editTextQrcode;
	private Button buttonQrcodetype, buttonQrcodeWidth,
			buttonErrorCorrectionLevel;

	public static int nQrcodetype, nQrcodeWidth, nErrorCorrectionLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(Qrcode.this, R.layout.actionbar,
				R.layout.activity_textandpicture_qrcode);
		buttonPrint = (Button) findViewById(R.id.buttonPrint);
		buttonPrint.setOnClickListener(this);

		editTextQrcode = (EditText) findViewById(R.id.editTextQrcode);

		buttonQrcodetype = (Button) findViewById(R.id.buttonQrcodetype);
		buttonQrcodetype.setOnClickListener(this);
		buttonQrcodeWidth = (Button) findViewById(R.id.buttonQrcodeWidth);
		buttonQrcodeWidth.setOnClickListener(this);
		buttonErrorCorrectionLevel = (Button) findViewById(R.id.buttonErrorCorrectionLevel);
		buttonErrorCorrectionLevel.setOnClickListener(this);
		
		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.qrcode));
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
			String strQrcode = editTextQrcode.getText().toString();
			int nWidthX = nQrcodeWidth + 2;
			int necl = nErrorCorrectionLevel + 1;
			Pos.POS_S_SetQRcode(strQrcode, nWidthX, necl);
			break;
		}

		case R.id.buttonQrcodetype: {

			AlertDialog dialog = new AlertDialog.Builder(Qrcode.this)
					.setTitle(R.string.qrcodetype)
					.setItems(R.array.qrcodetype,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.qrcodetype);
									buttonQrcodetype.setText(items[which]);
									nQrcodetype = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonQrcodeWidth: {
			AlertDialog dialog = new AlertDialog.Builder(Qrcode.this)
					.setTitle(R.string.qrcodewidth)
					.setItems(R.array.qrcodewidth,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(R.array.qrcodewidth);
									buttonQrcodeWidth.setText(items[which]);
									nQrcodeWidth = which;
								}
							}).create();

			dialog.show();
			break;
		}

		case R.id.buttonErrorCorrectionLevel: {
			AlertDialog dialog = new AlertDialog.Builder(Qrcode.this)
					.setTitle(R.string.errorcorrectionlevel)
					.setItems(R.array.errorcorrectionlevel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									/* User clicked so do some stuff */
									String[] items = getResources()
											.getStringArray(
													R.array.errorcorrectionlevel);
									buttonErrorCorrectionLevel
											.setText(items[which]);
									nErrorCorrectionLevel = which;
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

		buttonQrcodetype.setText(getResources().getStringArray(
				R.array.qrcodetype)[nQrcodetype]);
		buttonQrcodeWidth.setText(getResources().getStringArray(
				R.array.qrcodewidth)[nQrcodeWidth]);
		buttonErrorCorrectionLevel.setText(getResources().getStringArray(
				R.array.errorcorrectionlevel)[nErrorCorrectionLevel]);

	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putInt(PREFERENCES_TEXT_Qrcodetype, nQrcodetype);
				editor.putInt(PREFERENCES_TEXT_QrcodeWidth, nQrcodeWidth);
				editor.putInt(PREFERENCES_TEXT_ErrorCorrectionLevel,
						nErrorCorrectionLevel);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}

}
