package org.kixlabs.tk.activities;

import java.util.ArrayList;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.activities.fragments.DestinationFragment;
import org.kixlabs.tk.activities.fragments.LineFragment;
import org.kixlabs.tk.activities.fragments.SourceFragment;
import org.kixlabs.tk.activities.fragments.TableFragment;
import org.kixlabs.tk.service.ServiceDatabaseHelper;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.LineSO;
import org.kixlabs.tk.service.so.SourceSO;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {

	private ViewPager mViewPager;

	private TabsAdapter mTabsAdapter;

	private LineFragment mLineFragment;

	private DestinationFragment mDestinationFragment;

	private SourceFragment mSourceFragment;

	private TableFragment mTableFragment;

	private LineSO mLine;

	private DestinationSO mDestination;

	private SourceSO mSource;

	static final private String LINE_KEY = "line";

	static final private String DESTINATION_KEY = "destination";

	static final private String SOURCE_KEY = "source";

	public ViewPager getViewPager() {
		return mViewPager;
	}

	public void setViewPager(ViewPager viewPager) {
		this.mViewPager = viewPager;
	}

	public LineFragment getLineFragment() {
		return mLineFragment;
	}

	public void setLineFragment(LineFragment lineFragment) {
		this.mLineFragment = lineFragment;
	}

	public DestinationFragment getDestinationFragment() {
		return mDestinationFragment;
	}

	public void setDestinationFragment(DestinationFragment destinationFragment) {
		this.mDestinationFragment = destinationFragment;
	}

	public SourceFragment getSourceFragment() {
		return mSourceFragment;
	}

	public void setmSourceFragment(SourceFragment sourceFragment) {
		this.mSourceFragment = sourceFragment;
	}

	public TableFragment getTableFragment() {
		return mTableFragment;
	}

	public void setTableFragment(TableFragment tableFragment) {
		this.mTableFragment = tableFragment;
	}

	public void onSelectLine(LineSO line) {
		mLine = line;
		if (mLine != null) {
			mDestinationFragment.setDisplayData(mLine);
			if (mSourceFragment != null)
				mSourceFragment.clearDisplayData();
			if (mTableFragment != null)
				mTableFragment.clearDisplayData();
			mViewPager.setCurrentItem(1);
		} else {
			if (mDestinationFragment != null)
				mDestinationFragment.clearDisplayData();
			if (mSourceFragment != null)
				mSourceFragment.clearDisplayData();
			if (mTableFragment != null)
				mTableFragment.clearDisplayData();
		}
	}

	public void onSelectDestination(DestinationSO destination) {
		this.mDestination = destination;
		mSourceFragment.setDisplayData(mDestination);
		if (mTableFragment != null)
			mTableFragment.clearDisplayData();
		mViewPager.setCurrentItem(2);
	}

	public void onSelectSource(SourceSO source) {
		mSource = source;
		mTableFragment.setDisplayData(mLine, mDestination, mSource);
		mViewPager.setCurrentItem(3);
	}

	public ServiceDatabaseHelper getService() {
		return ((TkApplication) getApplication()).getDatabaseHelper();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("MainActivity", "onCreate");

		if (savedInstanceState != null) {
			mLine = (LineSO) savedInstanceState.getSerializable(LINE_KEY);
			mDestination = (DestinationSO) savedInstanceState.getSerializable(DESTINATION_KEY);
			mSource = (SourceSO) savedInstanceState.getSerializable(SOURCE_KEY);
		}

		setContentView(R.layout.browse);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);

		mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
		mTabsAdapter.addTab(getSupportActionBar().newTab().setText(R.string.line_title), LineFragment.class);
		mTabsAdapter.addTab(getSupportActionBar().newTab().setText(R.string.destination_title), DestinationFragment.class);
		mTabsAdapter.addTab(getSupportActionBar().newTab().setText(R.string.source_title), SourceFragment.class);
		mTabsAdapter.addTab(getSupportActionBar().newTab().setText(R.string.table_title), TableFragment.class);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(LINE_KEY, mLine);
		outState.putSerializable(DESTINATION_KEY, mDestination);
		outState.putSerializable(SOURCE_KEY, mSource);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("MainActivity", "onDestroy");
	}

	private void showPreferences() {
		Intent intent = new Intent(this, PreferenceActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void showDownloadDataActivity() {
		Log.i("MainActivity", "showDownloadDataActivity");
		Intent intent = new Intent(this, ParseActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			showPreferences();
			break;
		case R.id.menu_download_data:
			showDownloadDataActivity();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			showPreferences();
			break;
		case R.id.menu_download_data:
			showDownloadDataActivity();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class TabsAdapter extends android.support.v4.app.FragmentPagerAdapter implements ViewPager.OnPageChangeListener,
			ActionBar.TabListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<String> mTabs = new ArrayList<String>();

		public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss) {
			mTabs.add(clss.getName());
			mActionBar.addTab(tab.setTabListener(this));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			return Fragment.instantiate(mContext, mTabs.get(position), null);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}
	}

}
