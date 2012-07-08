package org.kixlabs.tk.activities.browse;

import org.kixlabs.tk.R;
import org.kixlabs.tk.browseservice.so.City;
import org.kixlabs.tk.browseservice.so.Line;
import org.kixlabs.tk.browseservice.so.LineSort;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class LineFragment extends SherlockListFragment {

	private BrowseWorkFragment mWorkFragment;

	public static class CityDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "CityDialogFragment";

		private static final String CITY_NAMES = "city-names";
		private static final String CITY_POSITION = "city-position";

		private String[] mCityNames;
		private int mCityPosition;

		public static CityDialogFragment newInstance() {
			CityDialogFragment fragment = new CityDialogFragment();
			return fragment;
		}

		@Override
		public void onSaveInstanceState(Bundle bundle) {
			bundle.putInt(CITY_POSITION, mCityPosition);
			bundle.putStringArray(CITY_NAMES, mCityNames);
			super.onSaveInstanceState(bundle);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final BrowseWorkFragment workFragment = (BrowseWorkFragment) getFragmentManager()
					.findFragmentByTag(BrowseWorkFragment.TAG);

			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(R.string.select_city);

			if (savedInstanceState != null && savedInstanceState.containsKey(CITY_NAMES)) {
				mCityPosition = savedInstanceState.getInt(CITY_POSITION);
				mCityNames = savedInstanceState.getStringArray(CITY_NAMES);
			} else {
				mCityNames = new String[workFragment.getCities().size()];
				mCityPosition = workFragment.getCities().indexOf(workFragment.getSelectedCity());
				int i = 0;
				for (City c : workFragment.getCities()) {
					mCityNames[i++] = c.getName();
				}
			}

			dialog.setSingleChoiceItems(mCityNames, mCityPosition, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					workFragment.onCitySelected(workFragment.getCities().get(which));
					dialog.dismiss();
				}
			});
			return dialog.create();
		}
	}

	public static class LineSortDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "LineSortDialogFragment";

		private static final String LINE_SORT_NAMES = "line-sort-names";
		private static final String LINE_SORT_CHECKS = "line-sort-checks";

		private String[] mLineSortNames;
		private boolean[] mLineSortChecks;

		public static LineSortDialogFragment newInstance() {
			LineSortDialogFragment fragment = new LineSortDialogFragment();
			return fragment;
		}

		@Override
		public void onSaveInstanceState(Bundle bundle) {
			bundle.putBooleanArray(LINE_SORT_CHECKS, mLineSortChecks);
			bundle.putStringArray(LINE_SORT_NAMES, mLineSortNames);
			super.onSaveInstanceState(bundle);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final BrowseWorkFragment workFragment = (BrowseWorkFragment) getFragmentManager()
					.findFragmentByTag(BrowseWorkFragment.TAG);

			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(R.string.select_sorts);

			if (savedInstanceState != null && savedInstanceState.containsKey(LINE_SORT_NAMES)) {
				mLineSortChecks = savedInstanceState.getBooleanArray(LINE_SORT_CHECKS);
				mLineSortNames = savedInstanceState.getStringArray(LINE_SORT_NAMES);
			} else {
				int lineSortSize = workFragment.getLinesSorts().size();
				mLineSortNames = new String[lineSortSize];
				mLineSortChecks = new boolean[lineSortSize];
				int i = 0;
				for (LineSort ls : workFragment.getLinesSorts()) {
					mLineSortNames[i] = ls.getName();
					if (workFragment.getSelectedLinesSorts().contains(ls))
						mLineSortChecks[i] = true;
					i++;
				}
			}

			dialog.setMultiChoiceItems(mLineSortNames, mLineSortChecks, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				}
			});
			dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					workFragment.getSelectedLinesSorts().clear();
					for (int i = 0; i < mLineSortChecks.length; i++) {
						if (mLineSortChecks[i])
							workFragment.getSelectedLinesSorts().add(workFragment.getLinesSorts().get(i));
					}
					workFragment.onLineSortsChanged();
				}
			});
			return dialog.create();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mWorkFragment = (BrowseWorkFragment) getFragmentManager().findFragmentByTag(BrowseWorkFragment.TAG);
		mWorkFragment.setLineFragment(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		refresh();
	}

	public void refresh() {
		setListAdapter(new ArrayAdapter<Line>(getActivity(), android.R.layout.simple_list_item_1, mWorkFragment.getLines()));
		if (mWorkFragment.getSelectedLinePosition() != -1)
			getListView().setItemChecked(mWorkFragment.getSelectedLinePosition(), true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Line item = (Line) l.getItemAtPosition(position);
		mWorkFragment.setSelectedLinePosition(position);
		mWorkFragment.onLineSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.lines, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_city:
			showCitydialog();
			break;
		case R.id.menu_line_sort:
			showLineSortDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showCitydialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(CityDialogFragment.newInstance(), CityDialogFragment.TAG);
		ft.commitAllowingStateLoss();
	}

	public void showLineSortDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(LineSortDialogFragment.newInstance(), LineSortDialogFragment.TAG);
		ft.commitAllowingStateLoss();
	}
}
