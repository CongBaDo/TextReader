package com.yamin.reader.activity;

import java.io.File;

import org.geometerplus.android.fbreader.NavigationPopup;
import org.geometerplus.android.fbreader.PopupPanel;
import org.geometerplus.android.fbreader.SelectionPopup;
import org.geometerplus.android.fbreader.ShowLibraryAction;
import org.geometerplus.android.fbreader.ShowNavigationAction;
import org.geometerplus.android.fbreader.ShowPreferencesAction;
import org.geometerplus.android.fbreader.ShowTOCAction;
import org.geometerplus.android.fbreader.TextSearchPopup;
import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ChangeFontSizeAction;
import org.geometerplus.fbreader.fbreader.ColorProfile;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBRreshAction;
import org.geometerplus.fbreader.fbreader.SwitchProfileAction;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ikaratruyen.IApplication;
import com.ikaratruyen.R;
import com.ikaratruyen.activity.IChapListActivity;
import com.ikaratruyen.model.Chapter;
import com.ikaratruyen.model.GetChapterRequest;
import com.ikaratruyen.model.GetChapterResponse;
import com.ikaratruyen.request.IGetChapterRequest;
import com.ikaratruyen.request.IGetChapterRequest.IChapterPostCallBack;
import com.ikaratruyen.utils.IKaraDbHelper;
import com.ikaratruyen.utils.ISettings;
import com.ikaratruyen.utils.IkaraConstant;
import com.ikaratruyen.utils.KaraUtils;
import com.ikaratruyen.utils.Server;
import com.yamin.reader.utils.ToolUtils;
import com.yamin.reader.view.SwitchButton;

/**
 * 
 * 
 */
public class CoreReadActivity extends FragmentActivity implements OnSeekBarChangeListener, OnClickListener{
	private static final String TAG = "CoreReadActivity";
	
	public static final String ACTION_OPEN_BOOK = "android.easyreader.action.VIEW";
	public static final String BOOK_KEY = "esayreader.book";
	public static final String BOOKMARK_KEY = "esayreader.bookmark";
	public static final String BOOK_PATH_KEY = "esayreader.book.path";
	public static final int REQUEST_PREFERENCES = 1;
	public static final int REQUEST_CANCEL_MENU = 2;
	private static final int NIGHT_UPDATEUI = 0;
	private static final int DAY_UPDATEUI = 1;
	private static final int GREEN_UPDATEUI = 2;
	private static final int BROWN_UPDATEUI = 3;
	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;
	private static final String PLUGIN_ACTION_PREFIX = "___";
	private ZLIntegerRangeOption option;
	private boolean isNight = false;
	private ImageView imgChangeState;
	private TextView fontBigButton;
	private TextView fontSmallButton;
	private ImageView bookHomeButton;
	private SeekBar seek;
	private LinearLayout topLL;
	private LinearLayout bottomLL;
	private SeekBar brightness_slider;
	private SwitchButton dayornightSwitch;
	private ScrollView popuMenuLL;
	private TextView tvChapIndex, tvIndex, tvQuyenIndex;
	private TextView tvIncrease, tvDecrease, tvChapIndexTop;
	private LinearLayout navigation_settings;
	private String chapId;
	private int readerState = IkaraConstant.READER_STATE.NIGHT;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case NIGHT_UPDATEUI:
				Log.v(TAG, "NIGHT");
				topLL.setBackgroundColor(getResources().getColor(R.color.black));
				bottomLL.setBackgroundColor(getResources().getColor(
						R.color.black));
				break;
			case DAY_UPDATEUI:
				Log.v(TAG, "DAY");
				topLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_1));
				bottomLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_1));
				break;

//			case BROWN_UPDATEUI:
//				myFBReaderApp.runAction(ActionCode.SWITCH_TO_BG3,
//						new SwitchProfileAction(myFBReaderApp,
//								ColorProfile.THIRD));
//				myFBReaderApp.runAction(ActionCode.JUST_REFRESH,
//						new FBRreshAction(myFBReaderApp, 0));
//				topLL.setBackgroundColor(getResources().getColor(
//						R.color.main_bg_3));
//				bottomLL.setBackgroundColor(getResources().getColor(
//						R.color.main_bg_3));
//				break;
//			case GREEN_UPDATEUI:
//				myFBReaderApp.runAction(ActionCode.SWITCH_TO_BG2,
//						new SwitchProfileAction(myFBReaderApp,
//								ColorProfile.SECOND));
//				myFBReaderApp.runAction(ActionCode.JUST_REFRESH,
//						new FBRreshAction(myFBReaderApp, 0));
//
//				topLL.setBackgroundColor(getResources().getColor(
//						R.color.main_bg_2));
//				bottomLL.setBackgroundColor(getResources().getColor(
//						R.color.main_bg_2));
//				break;
			}
			super.handleMessage(msg);
		}
	};

	private static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary) ZLAndroidLibrary.Instance();
	}

	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;

	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;
	private boolean isBottomAndTopMenuShow = false;
	private String chapTitle;
	private String bookId;
	private String bookTitle;
	private int currentChapIndex = 0;
	private boolean isOpenBook;

	private synchronized void openBook(Intent intent, Runnable action,
			boolean force) {
		
		Log.i(TAG, "openBook");
		if (!force && myBook != null) {
			return;
		}
		if (myBook == null) {
			
			String path ;//= Environment.getExternalStorageDirectory().getAbsolutePath() +"/nemodotest.fb2";
			path = KaraUtils.getChapContentFromSdcard(bookId, currentChapIndex+1);
			Log.e(TAG, "openBook "+path);
			if(path == null){
				loadChapContent(chapId);
			}else{
				this.myBook = myFBReaderApp.Collection.getBookByFile(BookUtil.getBookFileFromSDCard(path));
			}
			
		}
		myFBReaderApp.openBook(myBook, null, action);
	}
	
	public Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Log.e(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.core_main);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		getZLibrary().setActivity(CoreReadActivity.this);
		
		currentChapIndex = getIntent().getExtras().getInt("chap_index");
		chapTitle = getIntent().getExtras().getString("chap_title");
		bookId = getIntent().getExtras().getString("book_id");
		isOpenBook = getIntent().getExtras().getBoolean("open_book");
		bookTitle = getIntent().getExtras().getString("book_title");
		chapId = getIntent().getExtras().getString("chap_id");
		
		
		Log.v(TAG, "oncreate "+chapTitle +" "+bookId+" "+chapId);
		//
		option = ZLTextStyleCollection.Instance().getBaseStyle().FontSizeOption;
		//
		myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(CoreReadActivity.this,
					new BookCollectionShadow());
		}
		getCollection().bindToService(this, null);
		myBook = null;

		final ZLAndroidApplication androidApplication = (ZLAndroidApplication) getApplication();
		if (androidApplication.myMainWindow == null) {
			androidApplication.myMainWindow = new ZLAndroidApplicationWindow(
					myFBReaderApp);
			myFBReaderApp.initWindow();
		}
		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}

		myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(
				this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES,
				new ShowPreferencesAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this,
				myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION,
				new ShowNavigationAction(this, myFBReaderApp));
		initView();
		setListener();
	}

	/** Init UI View*/
	public void initView() {
		myRootView = (RelativeLayout) findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget) findViewById(R.id.main_view);
		imgChangeState = (ImageView)findViewById(R.id.img_change_state_reader);
		imgChangeState.setOnClickListener(this);
		topLL = (LinearLayout) findViewById(R.id.topMenuLL);
		bottomLL = (LinearLayout) findViewById(R.id.bottomMenuLL);
		seek = (SeekBar) findViewById(R.id.sk_page);
		seek.setOnSeekBarChangeListener(this);
		fontBigButton = (TextView) findViewById(R.id.tv_increase);
		fontSmallButton = (TextView) findViewById(R.id.tv_decrease);
		tvIndex = (TextView)findViewById(R.id.tv_index);
		((ImageView) findViewById(R.id.img_back)).setOnClickListener(this);
		((ImageView) findViewById(R.id.img_share)).setOnClickListener(this);
		((TextView) findViewById(R.id.tv_title_bar)).setText(bookTitle);
		((ImageView) findViewById(R.id.img_share))
				.setBackgroundResource(R.drawable.view_state_clipboard_button);
		tvChapIndex = (TextView) findViewById(R.id.tv_chapter_index);
		tvChapIndexTop = (TextView) findViewById(R.id.tv_chap_top);
		tvQuyenIndex = (TextView) findViewById(R.id.tv_book_quyen_index);
		tvChapIndexTop.setVisibility(View.VISIBLE);
		tvChapIndexTop.setText((currentChapIndex + 1) + "");
//		TypedValue tv = new TypedValue();
//		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
//			int actionBarHeight = TypedValue.complexToDimensionPixelSize(
//					tv.data, getResources().getDisplayMetrics());
//			topLL.setLayoutParams(new LayoutParams(
//					LayoutParams.MATCH_PARENT, actionBarHeight));
//		}
		
		isBottomAndTopMenuShow = false;
		topLL.setVisibility(View.GONE);
		bottomLL.setVisibility(View.GONE);
		
		if (myFBReaderApp.getColorProfileName() != null
				&& myFBReaderApp.getColorProfileName().equals(
						ColorProfile.NIGHT)) {
			topLL.setBackgroundColor(getResources().getColor(R.color.black));
			bottomLL.setBackgroundColor(getResources().getColor(R.color.black));
		} else {
			if (myFBReaderApp.getColorProfileName() != null
					&& myFBReaderApp.getColorProfileName().equals(
							ColorProfile.SECOND)) {
				topLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_2));
				bottomLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_2));

			} else if (myFBReaderApp.getColorProfileName() != null
					&& myFBReaderApp.getColorProfileName().equals(
							ColorProfile.THIRD)) {
				topLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_3));
				bottomLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_3));
			} else {
				topLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_1));
				bottomLL.setBackgroundColor(getResources().getColor(
						R.color.main_bg_1));
			}
		}
	}

	public ZLAndroidWidget getMainView() {
		return myMainView;
	}

	private void setListener() {
		
		fontBigButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (option.getValue() <= 55) {
					myFBReaderApp.runAction(ActionCode.INCREASE_FONT,
							new ChangeFontSizeAction(myFBReaderApp, +2));
				}
			}
		});
		fontSmallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (option.getValue() >= 30) {
					myFBReaderApp.runAction(ActionCode.DECREASE_FONT,
							new ChangeFontSizeAction(myFBReaderApp, -2));
				}
			}
		});
	}

//	@Override
//	protected void onNewIntent(final Intent intent) {
//		Log.i("TAG", "onNewIntent()");
//		final String action = intent.getAction();
//		final Uri data = intent.getData();
//
//		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
//			super.onNewIntent(intent);
//		} else if (Intent.ACTION_VIEW.equals(action) && data != null
//				&& "fbreader-action".equals(data.getScheme())) {
//			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(),
//					data.getFragment());
//		} else if (ACTION_OPEN_BOOK.equals(action)) {
//
//			getCollection().bindToService(this, new Runnable() {
//				public void run() {
//					Log.i("TAG", "openBook()");
//					//openBook(intent, null, true);
//				}
//			});
//		} else {
//			super.onNewIntent(intent);
//		}
//	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.e(TAG, "onStart");
		getCollection().bindToService(this, new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						openBook(getIntent(), null, false);
						myFBReaderApp.getViewWidget().repaint();
					}
				}.start();

				myFBReaderApp.getViewWidget().repaint();
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IApplication.getInstance().setCurrentActivity(CoreReadActivity.this);
		PopupPanel.restoreVisibilities(myFBReaderApp);
		ApiServerImplementation.sendEvent(this,
				ApiListener.EVENT_READ_MODE_OPENED);

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				final BookModel model = myFBReaderApp.Model;
				if (model == null || model.Book == null) {
					return;
				}
				onPreferencesUpdate(myFBReaderApp.Collection
						.getBookById(model.Book.getId()));
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		PopupPanel.removeAllWindows(myFBReaderApp, this);
		Log.i("MAIN", "onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getCollection().unbind();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		myFBReaderApp.hideActivePopup();
		final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					myFBReaderApp.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(myFBReaderApp.TextSearchPatternOption.getValue(), true,
				null, false);
		return true;
	}
	
	public void backPress() {
		int y = myFBReaderApp.getTextView().pagePosition().Current;
		int z = myFBReaderApp.getTextView().pagePosition().Total;
		Log.i("MAIN", y + "" + "/" + z + ToolUtils.myPercent(y, z));

		Log.i("MAIN", "" + myFBReaderApp.getTextView().getEndCursor() +" "+myBook.getId());
		myFBReaderApp.Collection.storePosition(myBook.getId(), myFBReaderApp
				.getTextView().getEndCursor());
		startActivity(new Intent(CoreReadActivity.this, MainActivity.class));
		CoreReadActivity.this.overridePendingTransition(R.anim.activity_enter,
				R.anim.activity_exit);
		CoreReadActivity.this.finish();
	}

	public void showSelectionPanel() {
		final ZLTextView view = myFBReaderApp.getTextView();
		((SelectionPopup) myFBReaderApp.getPopupById(SelectionPopup.ID)).move(
				view.getSelectionStartY(), view.getSelectionEndY());
		myFBReaderApp.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myFBReaderApp.hideActivePopup();
		}
	}

	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_CANCEL_MENU:
			myFBReaderApp.runCancelAction(resultCode - 1);
			break;
		}
	}

	public void navigate() {
		if (!isBottomAndTopMenuShow) {
			isBottomAndTopMenuShow = true;
			topLL.setVisibility(View.VISIBLE);
			bottomLL.setVisibility(View.VISIBLE);
			topLL.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.layout_enter));
			bottomLL.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.layout_enter));
		} else {
			isBottomAndTopMenuShow = false;
			topLL.setVisibility(View.GONE);
			bottomLL.setVisibility(View.GONE);
			topLL.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.layout_exit));
			bottomLL.startAnimation(AnimationUtils.loadAnimation(this,
					R.anim.layout_exit));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//			//
//			backPress();
//			this.onBackPressed();
//			return true;
//		}
//		return (myMainView != null && myMainView.onKeyDown(keyCode, event))
//				|| super.onKeyDown(keyCode, event);
//	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;

	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
							.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
									"FBReader");
					myWakeLock.acquire();
				}
			}
		}
	}

	public void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 10;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
	}

	public int getScreenBrightness() {
		final int level = (int) (100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow) myFBReaderApp.Collection;
	}
	
	private void gotoPage(int page) {
		final ZLTextView view = myFBReaderApp.getTextView();
		if (page == 1) {
			view.gotoHome();
		} else {
			view.gotoPage(page);
		}
		myFBReaderApp.getViewWidget().reset();
		myFBReaderApp.getViewWidget().repaint();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		final int page = progress + 1;
		gotoPage(page);
		tvIndex.setText(myFBReaderApp.getTextView().pagePosition().Current + "/" + (myFBReaderApp.getTextView().pagePosition().Total + 1));
	}
	
	public void stopLoading(){
		Log.e(TAG, "stopLoading "+currentChapIndex);
		seek.setMax(myFBReaderApp.getTextView().pagePosition().Total - 1);
		if (ISettings.getInstance().getChapListContents().get(currentChapIndex).volume != null) {
			long volume = ISettings.getInstance().getChapListContents()
					.get(currentChapIndex).volume;
			tvQuyenIndex.setVisibility(View.VISIBLE);
			tvQuyenIndex.setText(getResources().getString(R.string.book_value)
					+ " " + volume);
		}
		
		Log.v(TAG, "TOATAL "+myFBReaderApp.getTextView().pagePosition().Total+" "+myFBReaderApp.getTextView().pagePosition().Current);
		tvChapIndex.setText(getResources().getString(R.string.chapter_value)
				+ " " + (currentChapIndex + 1));
		
		tvIndex.setText(myFBReaderApp.getTextView().pagePosition().Current + "/" + (myFBReaderApp.getTextView().pagePosition().Total - 1));
	}
	
	public void reloadPostition(){
		tvIndex.setText(myFBReaderApp.getTextView().pagePosition().Current + "/" + (myFBReaderApp.getTextView().pagePosition().Total - 1));
		seek.setProgress(myFBReaderApp.getTextView().pagePosition().Current);
//		isBottomAndTopMenuShow = false;
//		topLL.setVisibility(View.GONE);
//		bottomLL.setVisibility(View.GONE);
//		topLL.startAnimation(AnimationUtils.loadAnimation(this,
//				R.anim.layout_exit));
//		bottomLL.startAnimation(AnimationUtils.loadAnimation(this,
//				R.anim.layout_exit));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.img_back:
//			stopNewService();
//			saveIndexPage();
			backPress();
			finish();
			break;

		case R.id.tv_chap_top:
		case R.id.img_share:

			Intent intent = new Intent(getApplicationContext(),
					IChapListActivity.class);
			intent.putExtra("current_index_chap", currentChapIndex);
			intent.putExtra("option_view", IChapListActivity.CHAPTER);
			startActivityForResult(intent, 100);
			break;

		case R.id.img_change_state_reader:
			if(readerState == IkaraConstant.READER_STATE.DAY){
				readerState = IkaraConstant.READER_STATE.NIGHT;
				myFBReaderApp.runAction(
						ActionCode.SWITCH_TO_NIGHT_PROFILE,
						new SwitchProfileAction(myFBReaderApp,
								ColorProfile.NIGHT));
				Message message = new Message();
				message.what = NIGHT_UPDATEUI;
				mHandler.sendMessage(message);
				isNight = true;
			}else{
				readerState = IkaraConstant.READER_STATE.DAY;
				myFBReaderApp.runAction(
						ActionCode.SWITCH_TO_DAY_PROFILE,
						new SwitchProfileAction(myFBReaderApp,
								ColorProfile.DAY));
				Message message = new Message();
				message.what = DAY_UPDATEUI;
				mHandler.sendMessage(message);
				isNight = false;
			}
			break;

		case R.id.tv_index:
			break;

		case R.id.img_font_text:
			Intent intentA = new Intent(getApplicationContext(),
					IChapListActivity.class);
			intentA.putExtra("current_index_chap", currentChapIndex);
			intentA.putExtra("option_view", IChapListActivity.FONT);
			startActivityForResult(intentA, 100);
			break;
		}		
	}
	
	private Chapter getCurrentChapter(String chapId) {
		for (int i = 0; i < ISettings.getInstance().getChapListContents()
				.size(); i++) {
			if (ISettings.getInstance().getChapListContents().get(i)._id.equals(chapId)) {
				return ISettings.getInstance().getChapListContents().get(i);
			}
		}

		return null;
	}
	
	/**
	 * @param id is chapId*/
	private void loadChapContent(String id) {
		Log.v(TAG, "loadChapContent "+id+" "+currentChapIndex);
//		showLoading();

		chapTitle = getCurrentChapter(id).title;
		
//		Log.d(TAG, "Chap Title "+chapTitle);
//		if(IKaraDbHelper.getInstance(getApplicationContext()).getChapContent(bookId, 0) != null && IKaraDbHelper.getInstance(getApplicationContext()).getChapContent(bookId, 0).length() > 0){
//			if(isOpenBook){
//				isOpenBook = false;
////				processOpenWithIndex();
//			}
//			
//			String content = IKaraDbHelper.getInstance(getApplicationContext()).getChapContent(bookId, currentChapIndex);
//			try {
//				content = Server.decompress(content);
//				JSONObject json = new JSONObject(content);
//				content = json.optString("content");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
////			processChapContent(content);
////			hideLoading();
//			
//			chapId = id;
//		}else{
			
			GetChapterRequest request = new GetChapterRequest();
			if (isOpenBook) {
				isOpenBook = false;
//				processOpenWithIndex();
			} else {
				chapId = id;
			}
			request.chapterId = chapId;
			request.language = "vi";
			new IGetChapterRequest(new IChapterPostCallBack() {

				@Override
				public void onResultChapterPostPost(GetChapterResponse statusObj) {
					// TODO Auto-generated method stub
					Log.v(TAG, "onResuktChapterPost "+statusObj.chapter.title);
//					processChapContent(statusObj.chapter.content);
//					
//					processOpenWithIndex();
//					hideLoading();
					KaraUtils.saveChapContent2SDCard(bookId, currentChapIndex+1, statusObj.chapter.content);
					String path = KaraUtils.getChapContentFromSdcard(bookId, currentChapIndex+1);
					myBook = myFBReaderApp.Collection.getBookByFile(BookUtil.getBookFileFromSDCard(path));
					myFBReaderApp.openBook(myBook, null, null);
				}

				@Override
				public void fail() {
					// TODO Auto-generated method stub

				}
			}, request).execute();
//		}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		backPress();
	}
}