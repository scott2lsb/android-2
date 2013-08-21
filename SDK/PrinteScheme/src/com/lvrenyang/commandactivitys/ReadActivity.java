package com.lvrenyang.commandactivitys;

import btmanager.DataUtils;
import btmanager.LayoutUtils;
import btmanager.Pos;
import btmanager.ReadThread;

import com.lvrenyang.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 各种读取信息
 * 
 * @author lvrenyang
 * 
 */
public class ReadActivity extends Activity implements OnClickListener,
		OnLongClickListener {

	private TextView tvTopic;
	private Button btBack;
	private TextView textViewHex, textViewAscii;
	private BroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(ReadActivity.this, R.layout.actionbar,
				R.layout.activity_command_read);

		findViewById(R.id.buttonRead1).setOnClickListener(this);
		findViewById(R.id.buttonRead2).setOnClickListener(this);
		findViewById(R.id.buttonRead3).setOnClickListener(this);
		findViewById(R.id.buttonRead4).setOnClickListener(this);

		textViewHex = (TextView) findViewById(R.id.textViewHex);
		textViewAscii = (TextView) findViewById(R.id.textViewAscii);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.readinfo));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);

		initBroadcast();
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
		uninitBroadcast();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.buttonRead1: {

			LayoutInflater factory = LayoutInflater.from(ReadActivity.this);
			View view = factory.inflate(R.layout.dialog_edittext_ok_cancel,
					null);
			final EditText editTextReadOffset = (EditText) view
					.findViewById(R.id.editTextReadOffset);
			Button buttonOk = (Button) view.findViewById(R.id.buttonOk);
			Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(view);
			builder.setCancelable(false);
			final AlertDialog dialog = builder.create();

			buttonOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					try {
						int offset = (Integer.decode(editTextReadOffset
								.getText().toString()));
						textViewHex.setText("");
						textViewAscii.setText("");
						Pos.POS_ReadFlash(offset, 500);
						dialog.dismiss();
					} catch (NumberFormatException e) {
						Toast.makeText(ReadActivity.this,
								getString(R.string.formatinvalid),
								Toast.LENGTH_SHORT).show();
					}

				}

			});

			buttonCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

			});
			dialog.show();

			break;
		}

		case R.id.buttonRead2: {
			break;
		}
		case R.id.buttonRead3: {
			break;
		}
		case R.id.buttonRead4: {
			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub

		return true;
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null)
					return;
				String address = device.getAddress();
				String name = device.getName();

				if (action.equals(ReadThread.ACTION_READTHREADRECEIVE)) {
					byte rc = intent.getByteExtra(
							ReadThread.EXTRA_READTHREADRECEIVEBYTE, (byte) 0);
					String tmpHeader = getString(R.string.device) + ": " + name
							+ "\n" + getString(R.string.address) + ": "
							+ address + "\n";

					String tmpHex = getString(R.string.receive) + "("
							+ getString(R.string.format_hex) + ")" + ": \n"
							+ DataUtils.byteToStr(rc) + "\n";
					String tmpAscii = getString(R.string.receive) + "("
							+ getString(R.string.format_ascii) + ")" + ": \n"
							+ (char) (rc & 0xff) + "\n";

					textViewHex.append(tmpHeader + tmpHex);
					textViewAscii.append(tmpHeader + tmpAscii);
				} else if (action.equals(ReadThread.ACTION_READTHREADRECEIVES)) {
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] rcs = intent
							.getByteArrayExtra(ReadThread.EXTRA_READTHREADRECEIVEBYTES);

					String tmpHeader = getString(R.string.device) + ": " + name
							+ "\n" + getString(R.string.address) + ": "
							+ address + "\n";

					String tmpHex = getString(R.string.receive) + "("
							+ getString(R.string.format_hex) + ")" + ": \n";
					String tmpAscii = getString(R.string.receive) + "("
							+ getString(R.string.format_ascii) + ")" + ": \n";

					if (recstatus) {
						tmpHex += DataUtils.bytesToStr(rcs) + "\n";
						tmpAscii += new String(rcs, 0, rcs.length) + "\n";
					} else {
						tmpHex += getString(R.string.failed);
						tmpAscii += getString(R.string.failed);
					}

					textViewHex.append(tmpHeader + tmpHex);
					textViewAscii.append(tmpHeader + tmpAscii);
				} else if (action
						.equals(ReadThread.ACTION_READTHREADRECEIVERESPOND)) {

					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] pcmdpackage = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDPACKAGE);
					int cmd = intent.getIntExtra(ReadThread.EXTRA_PCMDCMD, 0);
					int para = intent.getIntExtra(ReadThread.EXTRA_PCMDPARA, 0);
					int length = intent.getIntExtra(
							ReadThread.EXTRA_PCMDLENGTH, 0);
					byte[] rcs = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDDATA);

					String tmpHeader = getString(R.string.device) + ": " + name
							+ "\n" + getString(R.string.address) + ": "
							+ address + "\n" + getString(R.string.send)
							+ "--->\n" + DataUtils.bytesToStr(pcmdpackage)
							+ "\n";

					if (recstatus)
						tmpHeader += getString(R.string.getback) + ":--->\n";

					tmpHeader += getString(R.string.command) + ": 0x"
							+ Integer.toHexString(cmd) + "\n"
							+ getString(R.string.param) + ": 0x"
							+ Integer.toHexString(para) + "\n"
							+ getString(R.string.datalength) + ": 0x"
							+ Integer.toHexString(length) + "\n";
					String tmpHex = "";
					String tmpAscii = "";
					if (length > 0) {
						tmpHex += getString(R.string.receive) + "("
								+ getString(R.string.format_hex) + ")" + ": \n"
								+ DataUtils.bytesToStr(rcs) + "\n";
						tmpAscii += getString(R.string.receive) + "("
								+ getString(R.string.format_ascii) + ")"
								+ ": \n" + new String(rcs, 0, rcs.length)
								+ "\n";
					}
					textViewHex.append(tmpHeader + tmpHex);
					textViewAscii.append(tmpHeader + tmpAscii);
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVE);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVES);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVERESPOND);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

}
