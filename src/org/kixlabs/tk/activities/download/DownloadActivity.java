package org.kixlabs.tk.activities.download;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class DownloadActivity extends SherlockFragmentActivity {

	public static class UIFragment extends SherlockFragment {
		private DownloadWorkFragment mWorkFragment;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.download, container, false);
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			FragmentManager fm = getSherlockActivity().getSupportFragmentManager();

			mWorkFragment = (DownloadWorkFragment) fm.findFragmentByTag(DownloadWorkFragment.TAG);

			if (mWorkFragment == null) {
				mWorkFragment = new DownloadWorkFragment();
				mWorkFragment.setTargetFragment(this, 0);
				fm.beginTransaction().add(mWorkFragment, DownloadWorkFragment.TAG).commit();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(TkApplication.getAppTheme());
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new UIFragment());
			ft.commit();
		}
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return false;
	}
}
