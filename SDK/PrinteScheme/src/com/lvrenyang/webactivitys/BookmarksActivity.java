package com.lvrenyang.webactivitys;

import btmanager.LayoutUtils;
import com.lvrenyang.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.ImageView;

public class BookmarksActivity extends Activity implements OnItemClickListener,
		OnClickListener {

	public static final String EXTRA_RESULTFORACTIVITY = "EXTRA_RESULTFORACTIVITY";

	private static final String[] BOOKMARKS_PROJECTION = new String[] {
			BookmarkColumns._ID, BookmarkColumns.FAVICON,
			BookmarkColumns.TITLE, BookmarkColumns.URL };

	private static final String[] from = new String[] {
			BookmarkColumns.FAVICON, BookmarkColumns.TITLE, BookmarkColumns.URL };

	private static final int[] to = new int[] { R.id.imageViewBookmarkIcon,
			R.id.textViewBookmarkTitle, R.id.textViewBookmarkUrl };

	private ListView listView;
	private SimpleCursorAdapter myAdapter;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(this, R.layout.actionbar,
				R.layout.activity_setting_connect);

		Cursor mCursor = getContentResolver().query(Browser.BOOKMARKS_URI,
				BOOKMARKS_PROJECTION, null, null, null);

		listView = (ListView) findViewById(R.id.listViewSettingConnect);
		myAdapter = new SimpleCursorAdapter(this, R.layout.bookmarks_item,
				mCursor, from, to);
		myAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				// TODO Auto-generated method stub
				String columnName = cursor.getColumnName(columnIndex);
				if (BookmarkColumns.FAVICON.equals(columnName)) {
					byte[] data = cursor.getBlob(columnIndex);
					if (data != null) {
						((ImageView) view).setImageBitmap(BitmapFactory
								.decodeByteArray(data, 0, data.length));
					}else
						return false;
				} else if (BookmarkColumns.TITLE.equals(columnName)
						|| BookmarkColumns.URL.equals(columnName)) {
					String str = cursor.getString(columnIndex);
					if (str != null) {
						((TextView) view).setText(str);
					} else
						return false;
				}
				return true;
			}

		});
		listView.setAdapter(myAdapter);

		listView.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		String url = ((TextView) view.findViewById(R.id.textViewBookmarkUrl))
				.getText().toString();
		if (url != null) {
			Intent intent = new Intent();
			intent.putExtra(EXTRA_RESULTFORACTIVITY, url);
			setResult(0, intent);
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		// Hear we have nothing to do
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
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
