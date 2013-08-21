package com.lvrenyang.settingactivitys;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import btmanager.Cmd;
import btmanager.DataUtils;
import btmanager.LayoutUtils;
import btmanager.Pos;
import btmanager.ReadThread;

import com.lvrenyang.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateProgram extends Activity implements OnClickListener,
		OnLongClickListener {

	private TextView tvTopic;
	private Button btBack;

	private Button buttonUpdateProgram;
	private Button btOptions;
	private TextView textViewFileName;

	private BroadcastReceiver broadcastReceiver;

	private static int MaxRetryTimes = 4;
	private static int retryedTimes = 0;
	private static int timeout = 500;

	private int index = 0;// 份数的索引，从0开始
	private int times = 0;// 如果数据正常初始化,那么这些都会存好.份数
	private int nCount = 0;
	private int orglen = 0;
	private byte[] orgdata = new byte[0];
	private int orgoffset = 0;

	private static final int perCmdRespondLength = 12;
	private static int MutiPackageCount = 8;

	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(UpdateProgram.this, R.layout.actionbar,
				R.layout.activity_setting_updateprogram);
		btOptions = (Button) findViewById(R.id.btOptions);
		btOptions.setText(getString(R.string.selectfile));
		btOptions.setOnClickListener(this);
		buttonUpdateProgram = (Button) findViewById(R.id.buttonUpdateProgram);
		buttonUpdateProgram.setOnClickListener(this);
		textViewFileName = (TextView) findViewById(R.id.textViewFileName);

		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.setting_updateprogram));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);

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
		updateUi();
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
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.btOptions: {
			Intent intent = new Intent(UpdateProgram.this,
					UpdateProgramOptions.class);
			startActivity(intent);
			break;
		}

		case R.id.buttonUpdateProgram: {
			if ((UpdateProgramOptions.programPath != null)
					&& (!"".equals(UpdateProgramOptions.programPath)))
				Pos.POS_Request(Cmd.PCmd.startUpdate, timeout);
			break;
		}

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
	}

	private void updateUi() {
		textViewFileName.setText(UpdateProgramOptions.getProgramPath());
	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if (action.equals(ReadThread.ACTION_READTHREADRECEIVES)) {
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] pcmdpackage = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDPACKAGE);
					int length = intent.getIntExtra(
							ReadThread.EXTRA_PCMDLENGTH, 0);
					if (recstatus) {
						updateProgress();

						retryedTimes = 0;
						if ((index + MutiPackageCount > times)
								&& (index < times)) {
							Pos.POS_Request(getUpdateCmd(index, nCount),
									perCmdRespondLength * nCount, timeout
											* MutiPackageCount);
							index += nCount;
						} else if (index + MutiPackageCount <= times) {
							Pos.POS_Request(
									getUpdateCmd(index, MutiPackageCount),
									perCmdRespondLength * MutiPackageCount,
									timeout * MutiPackageCount);
							index += MutiPackageCount;
						} else if (index == times) {
							Pos.POS_Write(Cmd.PCmd.endUpdate);
							endProgress();
							Toast.makeText(UpdateProgram.this,
									getString(R.string.endupdate),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						retryedTimes++;
						if (retryedTimes < MaxRetryTimes) {
							Pos.POS_Request(pcmdpackage, length, timeout
									* MutiPackageCount);
						} else {
							endProgress();
							Toast.makeText(UpdateProgram.this,
									getString(R.string.updatefailed),
									Toast.LENGTH_SHORT).show();
						}
					}
				} else if (action
						.equals(ReadThread.ACTION_READTHREADRECEIVERESPOND)) {
					boolean recstatus = intent.getBooleanExtra(
							ReadThread.EXTRA_READTHREADRECEIVECORRECT, false);
					byte[] pcmdpackage = intent
							.getByteArrayExtra(ReadThread.EXTRA_PCMDPACKAGE);
					int cmd = intent.getIntExtra(ReadThread.EXTRA_PCMDCMD, 0);
					int para = intent.getIntExtra(ReadThread.EXTRA_PCMDPARA, 0);
					if (recstatus) {
						retryedTimes = 0;

						if ((cmd == (byte) 0x2f) && (para == 0x0)) {
							if ((UpdateProgramOptions.programPath != null)
									&& (!"".equals(UpdateProgramOptions.programPath))) {
								try {
									FileInputStream fis = new FileInputStream(
											UpdateProgramOptions.programPath);
									int binflen = fis.available();
									times = (binflen + 255) / 256;
									nCount = times % MutiPackageCount;
									orglen = times * 256;
									orgdata = new byte[orglen];
									fis.read(orgdata, 0, binflen);
									fis.close();
								} catch (FileNotFoundException e) {
									orgdata = new byte[0];
								} catch (IOException e) {
									orgdata = new byte[0];
								}

								initProgress();
								index = 0;
								Pos.POS_Request(
										getUpdateCmd(index, MutiPackageCount),
										perCmdRespondLength * MutiPackageCount,
										timeout * MutiPackageCount);
							}

						}

					} else {
						retryedTimes++;
						if (retryedTimes < MaxRetryTimes) {
							Pos.POS_Request(pcmdpackage, timeout);
						} else {
							if ((cmd == (byte) 0x2f) && (para == 0x0)) {
								endProgress();
								Toast.makeText(UpdateProgram.this,
										getString(R.string.updatenotstart),
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVES);
		intentFilter.addAction(ReadThread.ACTION_READTHREADRECEIVERESPOND);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	private byte[] getUpdateCmd(int index, int count) {
		byte[] data = new byte[268 * count];
		for (int i = 0; i < count; i++) {
			orgoffset = (index + i) * 256;
			byte[] perdata = new byte[268];// 需要发送到下面的数据,这样可以确保每一个perdate都不会被覆盖掉
			perdata[0] = 0x03;
			perdata[1] = (byte) 0xff;
			perdata[2] = 0x2e;
			perdata[3] = 0x00;
			perdata[4] = (byte) (orgoffset & 0xff);// 低位字节在前面
			perdata[5] = (byte) ((orgoffset >> 8) & 0xff);
			perdata[6] = (byte) ((orgoffset >> 16) & 0xff);
			perdata[7] = (byte) ((orgoffset >> 24) & 0xff);
			perdata[8] = 0x00;
			perdata[9] = 0x01;
			perdata[10] = DataUtils.bytesToXor(perdata, 0, 10);
			perdata[11] = DataUtils.bytesToXor(orgdata, orgoffset, 256);
			DataUtils.copyBytes(orgdata, orgoffset, perdata, 12, 256);
			DataUtils.copyBytes(perdata, 0, data, i * 268, 268);
		}
		return data;
	}

	private void initProgress() {
		mProgressDialog = new ProgressDialog(UpdateProgram.this);
		mProgressDialog.setMessage(getString(R.string.updating));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(times);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	private void updateProgress() {
		if (mProgressDialog != null)
			if (index <= times)
				mProgressDialog.setProgress(index);
			else
				mProgressDialog.dismiss();

	}

	private void endProgress() {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
	}

}
