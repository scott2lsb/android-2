package com.example.updatev2;

import btManager.Pos;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class SetParamsActivity extends Activity implements OnClickListener {

	private Button btSaveParams;
	private Button btBaudrate, btLanguage, btDarkness, btDefaultFont, btLFCR;
	private EditText etSystemName, etSystemSN, etBtname, etBtpwd, etIdletime,
			etPowerofftime, etMaxfeedlength, etBlackmarklength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rcusetparam);

		btSaveParams = (Button) findViewById(R.id.btSaveParam);
		btBaudrate = (Button) findViewById(R.id.btBaudrate);
		btLanguage = (Button) findViewById(R.id.btLanguage);
		btDarkness = (Button) findViewById(R.id.btDarkness);
		btDefaultFont = (Button) findViewById(R.id.btDefaultFont);
		btLFCR = (Button) findViewById(R.id.btLFCR);
		btBaudrate.setOnClickListener(this);
		btSaveParams.setOnClickListener(this);
		btLanguage.setOnClickListener(this);
		btDarkness.setOnClickListener(this);
		btDefaultFont.setOnClickListener(this);
		btLFCR.setOnClickListener(this);

		etSystemName = (EditText) findViewById(R.id.etSystemname);
		etSystemSN = (EditText) findViewById(R.id.etSystemsn);
		etBtname = (EditText) findViewById(R.id.etBtname);
		etBtpwd = (EditText) findViewById(R.id.etBtpwd);
		etIdletime = (EditText) findViewById(R.id.etIdletime);
		etPowerofftime = (EditText) findViewById(R.id.etPowerofftime);
		etMaxfeedlength = (EditText) findViewById(R.id.etMaxfeedlength);
		etBlackmarklength = (EditText) findViewById(R.id.etBlackmarklength);

		if (UpdateActivity.SystemInfo != null) {
			// 将SystemInfo解析，并填充本页内容
			etSystemName.setText(UpdateActivity.SystemInfo[0]);
			etSystemSN.setText(UpdateActivity.SystemInfo[1]);
		}

		if (UpdateActivity.btParams != null) {
			// 将btParams解析，并填充本页内容
			etBtname.setText(UpdateActivity.btParams[0]);
			etBtpwd.setText(UpdateActivity.btParams[1]);
		}

		if (UpdateActivity.otherParams != null) {
			// 将otherParams解析，并填充本页内容
			btBaudrate.setText(UpdateActivity.otherParams[0]);
			btLanguage.setText(UpdateActivity.otherParams[1]);
			btDarkness.setText(UpdateActivity.otherParams[2]);
			btDefaultFont.setText(UpdateActivity.otherParams[3]);
			btLFCR.setText(UpdateActivity.otherParams[4]);
			etIdletime.setText(UpdateActivity.otherParams[5]);
			etPowerofftime.setText(UpdateActivity.otherParams[6]);
			etMaxfeedlength.setText(UpdateActivity.otherParams[7]);
			etBlackmarklength.setText(UpdateActivity.otherParams[8]);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btSaveParam: {
			try {

				String strsystemname = etSystemName.getText().toString();
				String strsystemsn = etSystemSN.getText().toString();
				if (strsystemname.length() > 31 || strsystemsn.length() > 31) {
					Toast.makeText(getApplicationContext(), "名称及序列号都不能超过31个字符",
							Toast.LENGTH_SHORT).show();
					break;
				}

				String strname = etBtname.getText().toString();
				String strpwd = etBtpwd.getText().toString();
				byte[] name = strname.getBytes();
				byte[] pwd = strpwd.getBytes();

				if ((pwd.length != 4) || (name.length > 12)
						|| (name.length == 0)) {
					Toast.makeText(getApplicationContext(),
							"名字不能超过12个字节.密码为4个字节", Toast.LENGTH_SHORT).show();
					break;
				}

				String strbaudrate = btBaudrate.getText().toString().trim();
				for (int i = 0; i < Pos.Constant.strbaudrate.length; i++) {
					if (strbaudrate.equals(Pos.Constant.strbaudrate[i])) {
						int nBaudrate = Pos.Constant.nbaudrate[i];
						Pos.Pro.setPrintParam[12] = (byte) ((nBaudrate & 0xff0000) >> 24);
						Pos.Pro.setPrintParam[13] = (byte) ((nBaudrate & 0xff0000) >> 16);
						Pos.Pro.setPrintParam[14] = (byte) ((nBaudrate & 0xff00) >> 8);
						Pos.Pro.setPrintParam[15] = (byte) (nBaudrate & 0xff);
						break;
					}
					if ((i + 1) == Pos.Constant.strbaudrate.length)
						throw new Exception(
								"波特率一般为9600，19200，38400，57600，115200，中的一个，请输入正确数值。");
				}

				String language = btLanguage.getText().toString();
				for (int i = 0; i < Pos.Constant.strcodepages.length; i++) {
					if (language.equals(Pos.Constant.strcodepages[i])) {
						Pos.Pro.setPrintParam[16] = (byte) (Pos.Constant.ncodepages[i] & 0xff);
						break;
					}
					if ((i + 1) == Pos.Constant.strcodepages.length)
						throw new Exception("请选择一个代码页");
				}

				String darkness = btDarkness.getText().toString();
				for (int i = 0; i < Pos.Constant.strdarkness.length; i++) {
					if (darkness.equals(Pos.Constant.strdarkness[i])) {
						Pos.Pro.setPrintParam[17] = (byte) (Pos.Constant.ndarkness[i] & 0xff);
						break;
					}
					if ((i + 1) == Pos.Constant.strdarkness.length)
						throw new Exception("选择一个加热浓度");
				}

				String defaultfont = btDefaultFont.getText().toString();
				for (int i = 0; i < Pos.Constant.strdefaultfont.length; i++) {
					if (defaultfont.equals(Pos.Constant.strdefaultfont[i])) {
						Pos.Pro.setPrintParam[18] = (byte) (Pos.Constant.ndefaultfont[i] & 0xff);
						break;
					}
					if ((i + 1) == Pos.Constant.strdefaultfont.length)
						throw new Exception("选择一个默认字体");
				}

				String strlfcr = btLFCR.getText().toString();
				for (int i = 0; i < Pos.Constant.strlinefeed.length; i++) {
					if (strlfcr.equals(Pos.Constant.strlinefeed[i])) {
						Pos.Pro.setPrintParam[19] = (byte) (Pos.Constant.nlinefeed[i] & 0xff);
						break;
					}
					if ((i + 1) == Pos.Constant.strlinefeed.length)
						throw new Exception("选择一个换行标记");
				}

				String stridletime = etIdletime.getText().toString().trim();
				int nIdletime = Integer.parseInt(stridletime);
				if ((nIdletime < 0) || (nIdletime > 65535))
					throw new Exception("空闲等待时间可选值为0-65535.(单位秒)");
				Pos.Pro.setPrintParam[20] = (byte) ((nIdletime & 0xff00) >> 8);
				Pos.Pro.setPrintParam[21] = (byte) (nIdletime & 0xff);

				String strpwofftime = etPowerofftime.getText().toString()
						.trim();
				int nPwofftime = Integer.parseInt(strpwofftime);
				if ((nPwofftime < 0) || (nPwofftime > 65535))
					throw new Exception("自动关机时间可选值为0-65535.(单位秒)");
				Pos.Pro.setPrintParam[22] = (byte) ((nPwofftime & 0xff00) >> 8);
				Pos.Pro.setPrintParam[23] = (byte) (nPwofftime & 0xff);

				String strmaxfeedlength = etMaxfeedlength.getText().toString()
						.trim();
				int nMaxfeedlength = Integer.parseInt(strmaxfeedlength);
				if ((nMaxfeedlength < 0) || (nMaxfeedlength > 65535))
					throw new Exception("走纸按键最大走纸距离可选值为0-65535.(单位毫米)");
				Pos.Pro.setPrintParam[24] = (byte) ((nMaxfeedlength & 0xff) >> 8);
				Pos.Pro.setPrintParam[25] = (byte) (nMaxfeedlength & 0xff);

				String strblackmarklength = etBlackmarklength.getText()
						.toString().trim();
				int nBlackmarklength = Integer.parseInt(strblackmarklength);
				if ((nBlackmarklength < 0) || (nBlackmarklength > 65535))
					throw new Exception("黑标最大寻找距离可选值为0-65535.(单位毫米)");
				Pos.Pro.setPrintParam[26] = (byte) ((nBlackmarklength & 0xff) >> 8);
				Pos.Pro.setPrintParam[27] = (byte) (nBlackmarklength & 0xff);

				if (Pos.POS_Connected()) {
					Pos.POS_SetBluetooth(name, pwd);
					Pos.POS_SetSystemInfo(strsystemname, strsystemsn);
					Pos.POS_SetPrintParam(Pos.Pro.setPrintParam);
					Pos.POS_Write(new byte[] { 0x12, 0x54 });
					Toast.makeText(getApplicationContext(),
							"设置完毕。蓝牙信息需要重启才能更新", Toast.LENGTH_SHORT).show();
					SetParamsActivity.this.finish();
				} else {
					throw new Exception("连接断开");
				}

			} catch (NumberFormatException e) {
				Toast.makeText(getApplicationContext(), "格式错误,请对输入数字",
						Toast.LENGTH_LONG).show();
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
				break;
			}

			break;
		}

		case R.id.btBaudrate: {
			ScrollView scrollView = new ScrollView(SetParamsActivity.this);
			LinearLayout llBaudrate = new LinearLayout(SetParamsActivity.this);
			llBaudrate.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llBaudrate);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetParamsActivity.this);
			builder.setTitle("设置波特率").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Pos.Constant.strbaudrate.length != Pos.Constant.nbaudrate.length) {
				Toast.makeText(getApplicationContext(), "波特率映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Pos.Constant.strbaudrate.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setText(Pos.Constant.strbaudrate[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btBaudrate.setText(Pos.Constant.strbaudrate[index]);
						dialog.dismiss();
					}

				});
				llBaudrate.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btLanguage: {
			ScrollView scrollView = new ScrollView(SetParamsActivity.this);
			LinearLayout llLanguage = new LinearLayout(SetParamsActivity.this);
			llLanguage.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llLanguage);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetParamsActivity.this);
			builder.setTitle("选择代码页").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Pos.Constant.strcodepages.length != Pos.Constant.ncodepages.length) {
				Toast.makeText(getApplicationContext(), "代码页映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Pos.Constant.strcodepages.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setText(Pos.Constant.strcodepages[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btLanguage.setText(Pos.Constant.strcodepages[index]);
						dialog.dismiss();
					}

				});
				llLanguage.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btDarkness: {
			ScrollView scrollView = new ScrollView(SetParamsActivity.this);
			LinearLayout llDarkness = new LinearLayout(SetParamsActivity.this);
			llDarkness.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llDarkness);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetParamsActivity.this);
			builder.setTitle("设置浓度").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Pos.Constant.strdarkness.length != Pos.Constant.ndarkness.length) {
				Toast.makeText(getApplicationContext(), "浓度映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Pos.Constant.strdarkness.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setText(Pos.Constant.strdarkness[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btDarkness.setText(Pos.Constant.strdarkness[index]);
						dialog.dismiss();
					}

				});
				llDarkness.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btDefaultFont: {
			ScrollView scrollView = new ScrollView(SetParamsActivity.this);
			LinearLayout llDefaultFont = new LinearLayout(
					SetParamsActivity.this);
			llDefaultFont.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llDefaultFont);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetParamsActivity.this);
			builder.setTitle("选择默认字体").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Pos.Constant.strdefaultfont.length != Pos.Constant.ndefaultfont.length) {
				Toast.makeText(getApplicationContext(), "字体映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Pos.Constant.strdefaultfont.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setText(Pos.Constant.strdefaultfont[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btDefaultFont
								.setText(Pos.Constant.strdefaultfont[index]);
						dialog.dismiss();
					}

				});
				llDefaultFont.addView(button);
			}

			dialog.show();
			break;
		}

		case R.id.btLFCR: {
			ScrollView scrollView = new ScrollView(SetParamsActivity.this);
			LinearLayout llLinefeed = new LinearLayout(SetParamsActivity.this);
			llLinefeed.setOrientation(LinearLayout.VERTICAL);
			scrollView.addView(llLinefeed);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetParamsActivity.this);
			builder.setTitle("选择换行标记").setView(scrollView);
			final AlertDialog dialog = builder.create();
			if (Pos.Constant.strlinefeed.length != Pos.Constant.nlinefeed.length) {
				Toast.makeText(getApplicationContext(), "换行标记映射错误",
						Toast.LENGTH_SHORT).show();
				break;
			}

			for (int i = 0; i < Pos.Constant.strlinefeed.length; i++) {
				final int index = i;
				Button button = new Button(this);
				button.setText(Pos.Constant.strlinefeed[index]);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						btLFCR.setText(Pos.Constant.strlinefeed[index]);
						dialog.dismiss();
					}

				});
				llLinefeed.addView(button);
			}

			dialog.show();
			break;
		}

		}
	}
}
