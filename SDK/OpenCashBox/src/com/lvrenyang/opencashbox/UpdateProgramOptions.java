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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UpdateProgramOptions extends Activity implements OnClickListener,
		OnLongClickListener, OnItemSelectedListener, OnItemClickListener {

	private BroadcastReceiver broadcastReceiver;

	private ListView listView1;
	private List<String> programsPath, programsName;
	private static final String FILE_GETED = "FILE_GETED";

	private File dir = new File(Environment.getExternalStorageDirectory()
			+ "/printer/");
	private String[] extensions = new String[] { ".bin" };

	public static String programPath = "";
	public static final String PREFERENCES_ProgramPath = "PREFERENCES_ProgramPath";

	private static final int PROGRAM_MAXSIZE = 524288;
	private static final int PROGRAM_MINSIZE = 1024;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_updateprogramoptions);
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
		programPath = programsPath.get(position);
		Toast.makeText(this, programsName.get(position), Toast.LENGTH_SHORT)
				.show();
		if (null == programPath || "".equals(programPath))
			MainActivity.programfile = null;
		else
			MainActivity.programfile = programPath;
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
		programsPath = new ArrayList<String>();
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
							programsPath.add(tmp);
					} catch (IOException e) {
						// Should never happen!
						throw new RuntimeException(e);
					}
				}

				Intent intent = new Intent(FILE_GETED);
				UpdateProgramOptions.this.sendBroadcast(intent);
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

					if (programsPath == null) {
						Toast.makeText(UpdateProgramOptions.this, "ÔØÈëÊ§°Ü",
								Toast.LENGTH_SHORT).show();
						return;
					}

					programsName = transformFilePathToName(programsPath);
					listView1.setAdapter(new ArrayAdapter<String>(
							UpdateProgramOptions.this,
							android.R.layout.simple_list_item_single_choice,
							programsName));
					listView1.setItemsCanFocus(false);
					listView1.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
					if (programsPath.indexOf(programPath) != -1)
						listView1.setItemChecked(
								programsPath.indexOf(programPath), true);
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

	public static String getProgramPath() {
		return programPath;
	}

}