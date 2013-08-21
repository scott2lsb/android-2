package com.lvrenyang.customprintactivitys;

import btmanager.LayoutUtils;

import com.lvrenyang.R;

import android.app.Activity;
import android.os.Bundle;

public class CustomPrintRead extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_custom_print_read);

	}
}
