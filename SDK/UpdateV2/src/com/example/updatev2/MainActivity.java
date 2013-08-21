package com.example.updatev2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private Button btUpdate;
	private Button btHelp;
	private Button btText;
	private Button btPic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		btPic = (Button) findViewById(R.id.button1);
		btPic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						PicPrintActivity.class);
				startActivity(intent);
			}

		});

		btText = (Button) findViewById(R.id.button2);
		btText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						TextPrintActivity.class);
				startActivity(intent);
			}

		});

		btUpdate = (Button) findViewById(R.id.button3);
		btUpdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						UpdateActivity.class);
				startActivity(intent);
			}

		});

		btHelp = (Button) findViewById(R.id.button4);
		btHelp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				LayoutInflater factory = LayoutInflater.from(MainActivity.this);
				View view = factory.inflate(R.layout.help, null);
				builder.setTitle("°ïÖúËµÃ÷").setView(view);
				AlertDialog dialog = builder.create();
				dialog.show();
			}

		});
	}
}
