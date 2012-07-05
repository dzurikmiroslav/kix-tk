package org.kixlabs.tk.activities.browse;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.activities.preference.PreferenceActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class BrowseActivity extends SherlockFragmentActivity {

	private ViewPager mViewPager;

	private BrowseWorkFragment mWorkFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(TkApplication.getAppTheme());
		super.onCreate(savedInstanceState);

		mWorkFragment = (BrowseWorkFragment) getSupportFragmentManager().findFragmentByTag(BrowseWorkFragment.TAG);
		if (mWorkFragment == null) {
			FragmentManager fm = getSupportFragmentManager();
			mWorkFragment = new BrowseWorkFragment();
			fm.beginTransaction().add(mWorkFragment, BrowseWorkFragment.TAG).commit();
		}

		setContentView(R.layout.browse);
		setTitle(R.string.line_title);
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		mViewPager = (ViewPager) findViewById(R.id.view_pager);

		new PagerAdapter(mViewPager, this, getSupportFragmentManager());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mViewPager.getCurrentItem() > 0) {
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (mWorkFragment.onBackPressed())
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			showPreferenceActivity();
			return true;
		case R.id.menu_cancel:
			finish();
			return true;
		case android.R.id.home:
			slideToPreviosPage();
			return true;
		}
		return false;
	}
	
	private void showPreferenceActivity() {
		mWorkFragment.setRealyExitFalse();
		Intent intent = new Intent(this, PreferenceActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	public void slideToPage(int page) {
		mViewPager.setCurrentItem(page);
		if (page > 0) {
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	public void slideToPreviosPage() {
		int currItem = mViewPager.getCurrentItem() - 1;
		mViewPager.setCurrentItem(currItem);
		if (currItem == 0) {
			getSupportActionBar().setHomeButtonEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	public int getCurrentPage() {
		return mViewPager.getCurrentItem();
	}

	public static class PagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

		private BrowseActivity mActivity;

		public PagerAdapter(ViewPager viewPager, BrowseActivity activity, FragmentManager fm) {
			super(fm);
			mActivity = activity;
			viewPager.setOnPageChangeListener(this);
			viewPager.setAdapter(this);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new LineFragment();
			case 1:
				return new DestinationFragment();
			case 2:
				return new SourceFragment();
			default:
				return new TimeTableFragment();
			}
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public void onPageScrollStateChanged(int position) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				mActivity.setTitle(R.string.line_title);
				break;
			case 1:
				mActivity.setTitle(R.string.destination_title);
				break;
			case 2:
				mActivity.setTitle(R.string.source_title);
				break;
			default:
				mActivity.setTitle(mActivity.mWorkFragment.makeTimeTableTitle());
				break;
			}
		}
	}
}
