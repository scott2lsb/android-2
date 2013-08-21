package com.lvrenyang.privateactivitys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class PicSignature extends Activity implements OnClickListener,
		OnLongClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(PicSignature.this, R.layout.actionbar,
				R.layout.activity_private_signature);

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
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	/**
	 * 读取指定的图片，按照指定的宽度和黑白字符，将图片转成签名档。
	 * 
	 * @param signaturePath
	 * @param nWidth
	 * @param black
	 * @param white
	 */
	@SuppressWarnings("unused")
	private void POS_PicSignature(String signaturePath, int nWidth,
			int nDither, String black, String white) {

		Bitmap mBitmap = BitmapFactory.decodeFile(signaturePath);

		int width = ((nWidth + 7) / 8) * 8;
		int height = mBitmap.getHeight() * width / mBitmap.getWidth() / 2;
		Bitmap rszBitmap = Pos.resizeImage(mBitmap, width, height);
		// 再保存缩放的位图以便调试

		int[] pixels = Pos.bitmapToBWPix_int(rszBitmap, nDither);

		byte[] b = black.getBytes();
		byte[] w = white.getBytes();
		byte[] cr = { 0x0d, 0x0a };
		String savePath = signaturePath.substring(0,
				signaturePath.lastIndexOf('.'))
				+ ".txt";
		File picSignature = new File(savePath);

		try {
			picSignature.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(picSignature);
			for (int i = 0; i < pixels.length; i++) {

				if ((i % width) == 0)
					fOut.write(cr);

				if ((pixels[i] & 0x1) == 1)
					fOut.write(w);
				else
					fOut.write(b);

			}
			fOut.flush();
			fOut.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
}
