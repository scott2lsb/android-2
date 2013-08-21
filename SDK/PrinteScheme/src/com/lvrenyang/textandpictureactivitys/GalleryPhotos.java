package com.lvrenyang.textandpictureactivitys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import btmanager.FileUtils;
import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
public class GalleryPhotos extends Activity implements OnClickListener {

	private TextView tvTopic;
	private Button btBack;
	
	private BroadcastReceiver broadcastReceiver;
	private Dialog dialog;
	private List<String> photos;

	private File dir = new File(Environment.getExternalStorageDirectory()
			+ "/DCIM/");
	private String[] extensions = new String[] { ".png", ".jpg", ".bmp" };

	private static final String FILE_GETED = "FILE_GETED";

	private Gallery g;
	private ImageView imageView1;
	private Button buttonKsmooth, buttonKsoft;
	// 当前点击的bitmap
	private Bitmap mBitmap;
	private Bitmap mBinaryBitmap;
	private int whichAlgr = R.id.buttonKsmooth;
	private int POS_WIDTH_PIXELS = 384;
	private int POS_PICTURE_MODE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_textandpicture_galleryphotos);

		// Reference the Gallery view
		g = (Gallery) findViewById(R.id.gallery);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView1.setOnClickListener(this);
		imageView1.setAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));

		buttonKsmooth = (Button) findViewById(R.id.buttonKsmooth);
		buttonKsoft = (Button) findViewById(R.id.buttonKsotf);
		buttonKsmooth.setOnClickListener(this);
		buttonKsoft.setOnClickListener(this);
		
		tvTopic = (TextView) findViewById(R.id.tvTopic);
		tvTopic.setText(getString(R.string.photos));
		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);
		findViewById(R.id.btOptions).setVisibility(View.INVISIBLE);
		
		// We also want to show context menu for longpressed items in the
		// gallery
		registerForContextMenu(g);
		initBroadcast();
		getPhotos();
		dialog = LayoutUtils.showDialog(this, getString(R.string.loading));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uninitBroadcast();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(R.string.gallery_2_text);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Toast.makeText(this, "Longpress: " + info.position, Toast.LENGTH_SHORT)
				.show();
		return true;
	}

	public class ImageAdapter extends BaseAdapter {
		private static final int ITEM_WIDTH = 136;
		private static final int ITEM_HEIGHT = 88;

		private final int mGalleryItemBackground;
		private final Context mContext;

		private final float mDensity;

		public ImageAdapter(Context c) {
			mContext = c;
			// See res/values/attrs.xml for the <declare-styleable> that defines
			// Gallery1.
			TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.Gallery1_android_galleryItemBackground, 0);
			a.recycle();

			mDensity = c.getResources().getDisplayMetrics().density;
		}

		@Override
		public int getCount() {
			if (photos != null)
				return photos.size();
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 第一步，通过FileUtils得到图片数据
		// 第二步，在该方法中将图片解析成bitmap，就可以了。
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				convertView = new ImageView(mContext);

				imageView = (ImageView) convertView;
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setLayoutParams(new Gallery.LayoutParams(
						(int) (ITEM_WIDTH * mDensity + 0.5f),
						(int) (ITEM_HEIGHT * mDensity + 0.5f)));

				// The preferred Gallery item background
				imageView.setBackgroundResource(mGalleryItemBackground);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageBitmap(BitmapFactory.decodeFile(photos
					.get(position)));

			return imageView;
		}
	}

	private void getPhotos() {
		photos = new ArrayList<String>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				photos.addAll(new FileUtils().getFiles(dir, extensions));
				Intent intent = new Intent(FILE_GETED);
				GalleryPhotos.this.sendBroadcast(intent);
			}
		}).start();

	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(FILE_GETED)) {

					if (photos == null) {
						Toast.makeText(GalleryPhotos.this,
								getString(R.string.loadfailed),
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								GalleryPhotos.this,
								getString(R.string.loadok) + ": "
										+ photos.size(), Toast.LENGTH_SHORT)
								.show();
					}

					if (dialog != null)
						dialog.dismiss();

					g.setAdapter(new ImageAdapter(GalleryPhotos.this));

					// Set a item click listener, and just Toast the clicked
					// position
					g.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							mBitmap = BitmapFactory.decodeFile(photos
									.get(position));
							imageView1.setImageBitmap(mBitmap);

						}
					});

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imageView1: {

			// 选择相应算法打印
			// 也可以直接将先前算好的图打下去
			mBinaryBitmap = Pos
					.toBinaryImage(
							mBitmap,
							POS_WIDTH_PIXELS,
							whichAlgr == R.id.buttonKsmooth ? Pos.ALGORITHM_DITHER_16x16
									: Pos.ALGORITHM_DITHER_8x8);
			Pos.POS_PrintPicture(mBinaryBitmap, POS_WIDTH_PIXELS,
					POS_PICTURE_MODE);

			break;
		}

		case R.id.buttonKsmooth: {
			// 选择16x16的算法
			// 并把图像显示上去
			whichAlgr = R.id.buttonKsmooth;
			if (mBitmap != null) {
				mBinaryBitmap = Pos.toBinaryImage(mBitmap, POS_WIDTH_PIXELS,
						Pos.ALGORITHM_DITHER_16x16);
				imageView1.setImageBitmap(mBinaryBitmap);
			}
			break;
		}

		case R.id.buttonKsotf: {
			whichAlgr = R.id.buttonKsotf;
			// 选择8x8的算法
			// 并把图像显示出来
			if (mBitmap != null) {
				mBinaryBitmap = Pos.toBinaryImage(mBitmap, POS_WIDTH_PIXELS,
						Pos.ALGORITHM_DITHER_8x8);
				imageView1.setImageBitmap(mBinaryBitmap);
			}
			break;
		}
		

		case R.id.btBack: {
			finish();
			break;
		}
		}
	}
}
