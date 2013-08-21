package com.lvrenyang.webactivitys;

import java.io.UnsupportedEncodingException;

import btmanager.LayoutUtils;
import btmanager.Pos;

import com.lvrenyang.printescheme.MainActivity;
import com.lvrenyang.printescheme.OptionsActivity;
import com.lvrenyang.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * 能够实现网页print函数当然好，兼容性高了很多。不能实现的话，就自己加几个按钮，来把网页中的文字，图片打印出来吧。
 * loadUrl，把它的源码找出来，就知道怎么load了。 打印都是先设置纸张，生成图片，然后打印。 一：整页打印，需要想办法将网页生成图片，然后打印。
 * 二：调用window.print()打印，需要将自己写的print函数插入到html中。
 * 
 * @author 书旗
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class GuideActivity extends Activity implements OnClickListener,
		OnLongClickListener {
	private BroadcastReceiver broadcastReceiver;
	private static final String HTML_GETED = "HTML_GETED";
	private static final String EXTRA_GETED_HTML = "EXTRA_GETED_HTML";
	private static final String myJsPrintFunction = "function print(){  printer.print(); } ";
	private static final String jsFunctionTag = "<script type=\"text/javascript\">";
	private static final String httphead = "http://";
	private static final String searchInBaiduHead = "http://www.baidu.com/s?wd=";

	public static final String baiduwebsite = "http://www.baidu.com/";
	public static final String baiduname = "baidu";

	public static final String PREFERENCES_WEB_bookmark1website = "PREFERENCES_WEB_bookmark1website";
	public static final String PREFERENCES_WEB_bookmark2website = "PREFERENCES_WEB_bookmark2website";
	public static final String PREFERENCES_WEB_bookmark3website = "PREFERENCES_WEB_bookmark3website";
	public static final String PREFERENCES_WEB_bookmark4website = "PREFERENCES_WEB_bookmark4website";
	public static final String PREFERENCES_WEB_bookmark5website = "PREFERENCES_WEB_bookmark5website";
	public static final String PREFERENCES_WEB_bookmark6website = "PREFERENCES_WEB_bookmark6website";
	public static final String PREFERENCES_WEB_bookmark7website = "PREFERENCES_WEB_bookmark7website";
	public static final String PREFERENCES_WEB_bookmark8website = "PREFERENCES_WEB_bookmark8website";
	public static final String PREFERENCES_WEB_bookmark1name = "PREFERENCES_WEB_bookmark1name";
	public static final String PREFERENCES_WEB_bookmark2name = "PREFERENCES_WEB_bookmark2name";
	public static final String PREFERENCES_WEB_bookmark3name = "PREFERENCES_WEB_bookmark3name";
	public static final String PREFERENCES_WEB_bookmark4name = "PREFERENCES_WEB_bookmark4name";
	public static final String PREFERENCES_WEB_bookmark5name = "PREFERENCES_WEB_bookmark5name";
	public static final String PREFERENCES_WEB_bookmark6name = "PREFERENCES_WEB_bookmark6name";
	public static final String PREFERENCES_WEB_bookmark7name = "PREFERENCES_WEB_bookmark7name";
	public static final String PREFERENCES_WEB_bookmark8name = "PREFERENCES_WEB_bookmark8name";

	private RelativeLayout relativeLayoutActionBar;

	private WebView webView;
	private ScrollView scrollViewInsideWebSite;
	private ProgressBar progressBar1;
	private Button buttonSaveBookmark;
	private Button btForward, btBackward, btOthers, btBookmarks, btPrint;
	private EditText editTextInputWebSite, editTextSearchInBaidu;
	private Button buttonWebsite1, buttonWebsite2, buttonWebsite3,
			buttonWebsite4, buttonWebsite5, buttonWebsite6, buttonWebsite7,
			buttonWebsite8;

	public static String[] bookmarkswebsite = new String[] { baiduwebsite,
			baiduwebsite, baiduwebsite, baiduwebsite, baiduwebsite,
			baiduwebsite, baiduwebsite, baiduwebsite };
	public static String[] bookmarksname = new String[] { baiduname, baiduname,
			baiduname, baiduname, baiduname, baiduname, baiduname, baiduname };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutUtils.initContentView(GuideActivity.this, R.layout.actionbar,
				R.layout.activity_web_guide);
		relativeLayoutActionBar = (RelativeLayout) findViewById(R.id.relativeLayoutActionBar);
		relativeLayoutActionBar.setVisibility(View.GONE);
		initWidgets();

		initWebSettings(webView);
		webView.addJavascriptInterface(new JsPrintObj(), "jop");
		webView.addJavascriptInterface(new WebAppInterface(this), "printer");
		webView.setWebChromeClient(new MyWebChromeClient(this));
		webView.setWebViewClient(new MyWebViewClient(this));
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
		updateWebGuideUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
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
		case R.id.btBackward: {
			if (webView.canGoBack()) {
				webView.goBack();
			} else {
				progressBar1.setVisibility(View.GONE);
				buttonSaveBookmark.setVisibility(View.GONE);
				webView.setVisibility(View.GONE);
				scrollViewInsideWebSite.setVisibility(View.VISIBLE);
				editTextInputWebSite.setText("");
			}
			break;
		}

		case R.id.btForward: {
			if (webView.getVisibility() == View.GONE) {
				progressBar1.setVisibility(View.GONE);
				buttonSaveBookmark.setVisibility(View.VISIBLE);
				webView.setVisibility(View.VISIBLE);
				scrollViewInsideWebSite.setVisibility(View.GONE);
			} else {
				if (webView.canGoForward())
					webView.goForward();
			}
			break;
		}

		case R.id.btOthers: {

			break;
		}

		case R.id.btBookmarks: {
			Intent intent = new Intent(this, BookmarksActivity.class);
			startActivityForResult(intent, R.layout.activity_web_guide);
			break;
		}

		case R.id.btPrint: {
			if (webView.getVisibility() == View.VISIBLE) {
				Bitmap mBitmap = captureWebView(webView);
				Pos.POS_PrintPicture(mBitmap, 384, 0);
			}
			break;
		}

		case R.id.buttonWebsite1: {
			webView.loadUrl(bookmarkswebsite[0]);
			break;
		}
		case R.id.buttonWebsite2: {
			webView.loadUrl(bookmarkswebsite[1]);
			break;
		}
		case R.id.buttonWebsite3: {
			webView.loadUrl(bookmarkswebsite[2]);
			break;
		}
		case R.id.buttonWebsite4: {
			webView.loadUrl(bookmarkswebsite[3]);
			break;
		}
		case R.id.buttonWebsite5: {
			webView.loadUrl(bookmarkswebsite[4]);
			break;
		}
		case R.id.buttonWebsite6: {
			webView.loadUrl(bookmarkswebsite[5]);
			break;
		}
		case R.id.buttonWebsite7: {
			webView.loadUrl(bookmarkswebsite[6]);
			break;
		}
		case R.id.buttonWebsite8: {
			webView.loadUrl(bookmarkswebsite[7]);
			break;
		}

		case R.id.buttonSaveBookmark: {
			AlertDialog dialog = new AlertDialog.Builder(GuideActivity.this)
					.setTitle(R.string.savebookmarks)
					.setItems(R.array.savebookmarks,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									bookmarkswebsite[which] = webView.getUrl();
									bookmarksname[which] = webView.getUrl();
									updateWebGuideUI();
								}
							}).create();

			dialog.show();
			break;
		}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ((requestCode == R.layout.activity_web_guide) && (resultCode == 0)) {
			if (data != null) {
				String url = data
						.getStringExtra(BookmarksActivity.EXTRA_RESULTFORACTIVITY);
				webView.loadUrl(url);
			}
		}
	}

	/**
	 * 
	 * @author Administrator
	 * @see 网页中调用java代码，并传递参数
	 */
	public class JsPrintObj {

		@JavascriptInterface
		public String print(String arg1, int arg2) {
			String tmp = "";
			try {
				tmp += arg1;
				Pos.POS_Write(arg1.getBytes("GBK"));
				Pos.POS_FeedLine();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				tmp += "编码错误\n";
			}
			return tmp;
		}
	}

	public class WebAppInterface {
		private Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		// 暂不使用
		/** Show a toast from the web page */
		@JavascriptInterface
		public void print() {
			if (OptionsActivity.getDebug())
				Toast.makeText(mContext, "print", Toast.LENGTH_SHORT).show();
		}

		/** get the html */
		@JavascriptInterface
		public void getHTML(String html) {
			if (OptionsActivity.getDebug())
				Toast.makeText(mContext, html, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(HTML_GETED);
			intent.putExtra(EXTRA_GETED_HTML, html);
			sendBroadcast(intent);
		}

		@JavascriptInterface
		public void getInerHTML(String inerHtml) {
			if (OptionsActivity.getDebug())
				Toast.makeText(mContext, inerHtml, Toast.LENGTH_SHORT).show();
		}

		@JavascriptInterface
		public void show(String inerHtml) {
			Toast.makeText(mContext, inerHtml, Toast.LENGTH_SHORT).show();
		}
	}

	public class MyWebViewClient extends WebViewClient {
		private Context mContext;

		public MyWebViewClient(Context context) {
			mContext = context;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Toast.makeText(mContext, "Oh no! " + description,
					Toast.LENGTH_SHORT).show();
		}

		// 或许使用loaddatawithbaseurl可以提前加载一段js代码
		// 不管怎样，现在目的是要可以选择以图片方式打印或者以纯文本方式打印
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			progressBar1.setVisibility(View.VISIBLE);
			buttonSaveBookmark.setVisibility(View.VISIBLE);
			webView.setVisibility(View.VISIBLE);
			scrollViewInsideWebSite.setVisibility(View.GONE);
			editTextInputWebSite.setText(url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			progressBar1.setVisibility(View.GONE);
			// view.loadUrl("javascript:window.printer.getHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
			// view.loadUrl("javascript:window.printer.getInerHTML(document.body.innerHTML);");
		}
	}

	public class MyWebChromeClient extends WebChromeClient {
		private Context mContext;

		public MyWebChromeClient(Context context) {
			mContext = context;
		}

		// GuideActivity
		@Override
		public void onProgressChanged(WebView view, int progress) {
			// Activities and WebViews measure progress with different scales.
			// The progress meter will automatically disappear when we reach
			// 100%
			GuideActivity.this.setProgress(progress * 1000);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			if (OptionsActivity.getDebug())
				Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
			result.confirm();
			return true;
		}

		@Override
		public void onCloseWindow(WebView window) {
			if (OptionsActivity.getDebug())
				Toast.makeText(mContext, "Close", Toast.LENGTH_SHORT).show();
		}

	}

	private void initWebSettings(WebView webView) {

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	}

	// 截屏
	@SuppressWarnings("unused")
	private Bitmap captureScreen(Activity context) {
		View cv = context.getWindow().getDecorView();
		Bitmap bmp = Bitmap.createBitmap(cv.getWidth(), cv.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		cv.draw(canvas);
		return bmp;
	}

	// 打印整页
	private Bitmap captureWebView(WebView webView) {
		Picture snapShot = webView.capturePicture();

		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(),
				snapShot.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bmp);
		snapShot.draw(canvas);
		Pos.saveMyBitmap(bmp);
		return bmp;
	}

	private void initWidgets() {
		webView = (WebView) findViewById(R.id.webView1);

		scrollViewInsideWebSite = (ScrollView) findViewById(R.id.scrollViewInsideWebSite);

		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		buttonSaveBookmark = (Button) findViewById(R.id.buttonSaveBookmark);
		buttonSaveBookmark.setOnClickListener(this);

		btForward = (Button) findViewById(R.id.btForward);
		btForward.setOnClickListener(this);
		btBackward = (Button) findViewById(R.id.btBackward);
		btBackward.setOnClickListener(this);
		btOthers = (Button) findViewById(R.id.btOthers);
		btOthers.setOnClickListener(this);
		btBookmarks = (Button) findViewById(R.id.btBookmarks);
		btBookmarks.setOnClickListener(this);
		btPrint = (Button) findViewById(R.id.btPrint);
		btPrint.setOnClickListener(this);

		editTextInputWebSite = (EditText) findViewById(R.id.editTextInputWebSite);
		editTextInputWebSite.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (KeyEvent.KEYCODE_ENTER == keyCode
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					webView.loadUrl(praseWebSite(editTextInputWebSite.getText()
							.toString()));
					return true;
				}
				return false;
			}

		});
		editTextSearchInBaidu = (EditText) findViewById(R.id.editTextSearchInBaidu);
		editTextSearchInBaidu.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub

				if (KeyEvent.KEYCODE_ENTER == keyCode
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					webView.loadUrl(praseWordToSearchInBaidu(editTextSearchInBaidu
							.getText().toString()));
					return true;
				}
				return false;
			}

		});
		buttonWebsite1 = (Button) findViewById(R.id.buttonWebsite1);
		buttonWebsite1.setOnClickListener(this);
		buttonWebsite2 = (Button) findViewById(R.id.buttonWebsite2);
		buttonWebsite2.setOnClickListener(this);
		buttonWebsite3 = (Button) findViewById(R.id.buttonWebsite3);
		buttonWebsite3.setOnClickListener(this);
		buttonWebsite4 = (Button) findViewById(R.id.buttonWebsite4);
		buttonWebsite4.setOnClickListener(this);
		buttonWebsite5 = (Button) findViewById(R.id.buttonWebsite5);
		buttonWebsite5.setOnClickListener(this);
		buttonWebsite6 = (Button) findViewById(R.id.buttonWebsite6);
		buttonWebsite6.setOnClickListener(this);
		buttonWebsite7 = (Button) findViewById(R.id.buttonWebsite7);
		buttonWebsite7.setOnClickListener(this);
		buttonWebsite8 = (Button) findViewById(R.id.buttonWebsite8);
		buttonWebsite8.setOnClickListener(this);

	}

	private String praseWebSite(String orgWeb) {
		if (orgWeb.contains(httphead))
			return orgWeb;
		else
			return httphead + orgWeb;
	}

	private String praseWordToSearchInBaidu(String word) {
		return searchInBaiduHead + word;
	}

	@SuppressWarnings("unused")
	private String insertMyJsPrintFunction(String html) {

		String myHtml = html.substring(0, html.indexOf(jsFunctionTag));
		myHtml += jsFunctionTag + "\n" + myJsPrintFunction;
		myHtml += html.substring(html.indexOf(jsFunctionTag)
				+ jsFunctionTag.length());
		return myHtml;

	}

	private void initBroadcast() {
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (action.equals(HTML_GETED)) {
					String orghtml = intent.getStringExtra(EXTRA_GETED_HTML);
					if (OptionsActivity.getDebug())
						Toast.makeText(GuideActivity.this, orghtml,
								Toast.LENGTH_SHORT).show();
				}
			}

		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(HTML_GETED);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	private void uninitBroadcast() {
		if (broadcastReceiver != null)
			unregisterReceiver(broadcastReceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();
				return true;
			} else if (scrollViewInsideWebSite.getVisibility() == View.GONE) {
				progressBar1.setVisibility(View.GONE);
				buttonSaveBookmark.setVisibility(View.GONE);
				webView.setVisibility(View.GONE);
				scrollViewInsideWebSite.setVisibility(View.VISIBLE);
				editTextInputWebSite.setText("");
				return true;
			}
		}
		// If it wasn't the Back key or there's no web page history, bubble up
		// to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	private void savePreferences() {
		synchronized (MainActivity.lock_preferences) {
			try {
				SharedPreferences.Editor editor = this.getSharedPreferences(
						MainActivity.PREFERENCES_FILE, 0).edit();
				editor.putString(PREFERENCES_WEB_bookmark1website,
						bookmarkswebsite[0]);
				editor.putString(PREFERENCES_WEB_bookmark2website,
						bookmarkswebsite[1]);
				editor.putString(PREFERENCES_WEB_bookmark3website,
						bookmarkswebsite[2]);
				editor.putString(PREFERENCES_WEB_bookmark4website,
						bookmarkswebsite[3]);
				editor.putString(PREFERENCES_WEB_bookmark5website,
						bookmarkswebsite[4]);
				editor.putString(PREFERENCES_WEB_bookmark6website,
						bookmarkswebsite[5]);
				editor.putString(PREFERENCES_WEB_bookmark7website,
						bookmarkswebsite[6]);
				editor.putString(PREFERENCES_WEB_bookmark8website,
						bookmarkswebsite[7]);
				editor.putString(PREFERENCES_WEB_bookmark1name,
						bookmarksname[0]);
				editor.putString(PREFERENCES_WEB_bookmark2name,
						bookmarksname[1]);
				editor.putString(PREFERENCES_WEB_bookmark3name,
						bookmarksname[2]);
				editor.putString(PREFERENCES_WEB_bookmark4name,
						bookmarksname[3]);
				editor.putString(PREFERENCES_WEB_bookmark5name,
						bookmarksname[4]);
				editor.putString(PREFERENCES_WEB_bookmark6name,
						bookmarksname[5]);
				editor.putString(PREFERENCES_WEB_bookmark7name,
						bookmarksname[6]);
				editor.putString(PREFERENCES_WEB_bookmark8name,
						bookmarksname[7]);
				editor.commit();
			} catch (Exception e) {
				if (OptionsActivity.getDebug())
					Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
							.show();
			}
		}
	}

	private void updateWebGuideUI() {
		buttonWebsite1.setText(bookmarksname[0]);
		buttonWebsite2.setText(bookmarksname[1]);
		buttonWebsite3.setText(bookmarksname[2]);
		buttonWebsite4.setText(bookmarksname[3]);
		buttonWebsite5.setText(bookmarksname[4]);
		buttonWebsite6.setText(bookmarksname[5]);
		buttonWebsite7.setText(bookmarksname[6]);
		buttonWebsite8.setText(bookmarksname[7]);
	}
}
