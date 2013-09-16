package com.lvrenyang.opencashbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lvrenyang.utils.FileUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class UpdateFontOptions extends Activity implements OnClickListener,
		OnLongClickListener, OnItemSelectedListener, OnItemClickListener {

	private BroadcastReceiver broadcastReceiver;

	private ListView listView1;
	private List<String> fontsPath, fontsName;
	private static final String FILE_GETED = "FILE_GETED";

	public static String fontPath = "";
	private File dir = new File(Environment.getExternalStorageDirectory()
			+ "/printer/");
	private String[] extensions = new String[] { ".bin" };

	private static final int PROGRAM_MAXSIZE = 10485760;
	private static final int PROGRAM_MINSIZE = 1024;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_updatefontoptions);
		listView1 = (ListView) findViewById(R.id.listView1);
		listView1.setOnItemClickListener(this);

		initBroadcast();
		getPrograms();

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
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		fontPath = fontsPath.get(position);
		Toast.makeText(this, fontsName.get(position), Toast.LENGTH_SHORT)
				.show();
		if (null == fontPath || "".equals(fontPath))
			MainActivity.fontfile = null;
		else
			MainActivity.fontfile = fontPath;
		finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	private void getPrograms() {
		fontsPath = new ArrayList<String>();
		final List<String> tmpProgramsPath = new ArrayList<String>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				tmpProgramsPath.addAll(new FileUtils()
						.getFiles(dir, extensions));

				for (int i = 0; i < tmpProgramsPath.size(); i++) {
					try {
						String tmp = tmpProgramsPath.get(i);
						File file = new File(tmp);
						FileInputStream fis = new FileInputStream(file);
						int length = fis.available();
						fis.close();
						if ((length > PROGRAM_MINSIZE)
								&& (length < PROGRAM_MAXSIZE))
							fontsPath.add(tmp);
					} catch (IOException e) {
						// Should never happen!
						throw new RuntimeException(e);
					}
				}

				Intent intent = new Intent(FILE_GETED);
				UpdateFontOptions.this.sendBroadcast(intent);
			}
		}).start();

	}

	private List<String> transformFilePathToName(List<String> programPaths) {
		List<String> programsName = new ArrayList<String>();
		for (int i = 0; i < programPaths.size(); i++) {
			String tmp = programPaths.get(i);
			programsName
					.add(tmp.substring(tmp.lastIndexOf(File.separator) + 1));
		}
		return programsName;

	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(FILE_GETED)) {

					if (fontsPath == null) {
						Toast.makeText(UpdateFontOptions.this, "ÔØÈëÊ§°Ü",
								Toast.LENGTH_SHORT).show();
						return;
					}

					fontsName = transformFilePathToName(fontsPath);
					listView1.setAdapter(new ArrayAdapter<String>(
							UpdateFontOptions.this,
							android.R.layout.simple_list_item_single_choice,
							fontsName));
					listView1.setItemsCanFocus(false);
					listView1.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
					if (fontsPath.indexOf(fontPath) != -1)
						listView1.setItemChecked(fontsPath.indexOf(fontPath),
								true);
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(FILE_GETED);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	public static String getFontPath() {
		return fontPath;
	}

}
