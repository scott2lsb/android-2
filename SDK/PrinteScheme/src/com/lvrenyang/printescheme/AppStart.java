package com.lvrenyang.printescheme;

import com.lvrenyang.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class AppStart extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_private);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(AppStart.this, MainActivity.class);
				// Intent intent = new Intent(AppStart.this,
				// TestSpinners.class);
				startActivity(intent);
				AppStart.this.finish();
			}
		}, 1000);
	}

}
