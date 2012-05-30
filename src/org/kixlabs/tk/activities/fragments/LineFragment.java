package org.kixlabs.tk.activities.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kixlabs.tk.R;
import org.kixlabs.tk.activities.MainActivity;
import org.kixlabs.tk.service.so.CitySO;
import org.kixlabs.tk.service.so.LineSO;
import org.kixlabs.tk.service.so.LineSortSO;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LineFragment extends SherlockListFragment {

	private MainActivity mActivity;

	private int mSelectedPosition = -1;

	private List<LineSO> mLines;

	private List<CitySO> mCities;

	private CitySO mCity;

	private List<LineSortSO> mLinesSorts;

	private List<LineSortSO> mSelectedLinesSorts;

	private static final String SELECTED_POSITION_KEY = "selected-position";

	private static final String SELECTED_CITY_KEY = "selected-city";

	private static final String SELECTED_LINE_SORT = "selected-lines-sort";

	private static final String SELECTED_LINES_SORTS_COUNT = "selected-lines-sorts-count";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.setLineFragment(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mCities = mActivity.getService().getCities();
		SharedPreferences preferences = mActivity.getPreferences(Context.MODE_WORLD_READABLE);
		long cityId = preferences.getLong(SELECTED_CITY_KEY, -1);
		if (cityId != -1) {
			for (CitySO c : mCities) {
				if (c.getId() == cityId) {
					mCity = c;
					break;
				}
			}
			if (mCity != null) {
				mLinesSorts = mActivity.getService().getLinesSorts(mCity);
				mSelectedLinesSorts = new ArrayList<LineSortSO>();
				int count = preferences.getInt(SELECTED_LINES_SORTS_COUNT, 0);
				Set<Long> contains = new HashSet<Long>();
				for (int i = 0; i < count; i++)
					contains.add(preferences.getLong(SELECTED_LINE_SORT + i, 0));
				for (LineSortSO ls : mLinesSorts)
					if (contains.contains(ls.getId()))
						mSelectedLinesSorts.add(ls);
			}
		}
		if (mCity == null) {
			if (!mCities.isEmpty())
				mCity = mCities.get(0);
			mLinesSorts = mActivity.getService().getLinesSorts(mCity);
			mSelectedLinesSorts = new ArrayList<LineSortSO>(mLinesSorts);
		}
		mLines = mActivity.getService().getLines(mCity, mSelectedLinesSorts);
		if (savedInstanceState != null) {
			mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY, -1);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Editor editor = mActivity.getPreferences(Context.MODE_WORLD_WRITEABLE).edit();
		if (mCity != null) {
			editor.putLong(SELECTED_CITY_KEY, mCity.getId());
			editor.putInt(SELECTED_LINES_SORTS_COUNT, mSelectedLinesSorts.size());
			int i = 0;
			for (LineSortSO ls : mSelectedLinesSorts)
				editor.putLong(SELECTED_LINE_SORT + i++, ls.getId());
		} else {
			editor.putLong(SELECTED_CITY_KEY, -1);
			editor.putInt(SELECTED_LINES_SORTS_COUNT, 0);
		}
		editor.commit();
	}

	private void updateLines() {
		mSelectedPosition = -1;
		mLines = mActivity.getService().getLines(mCity, mSelectedLinesSorts);
		setListAdapter(new ArrayAdapter<LineSO>(mActivity, android.R.layout.simple_list_item_single_choice, mLines));
		mActivity.onSelectLine(null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
	}

	@Override
	public void onResume() {
		Log.i("LineFragment", "onResume");
		super.onResume();
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setListAdapter(new ArrayAdapter<LineSO>(mActivity, android.R.layout.simple_list_item_single_choice, mLines));
		if (mSelectedPosition != -1)
			getListView().setItemChecked(mSelectedPosition, true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		LineSO item = (LineSO) l.getItemAtPosition(position);
		mSelectedPosition = position;
		mActivity.onSelectLine(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.lines, menu);
	}

	private void showSelectCity() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
		dialog.setTitle(R.string.select_city);
		dialog.setSingleChoiceItems(new ArrayAdapter<CitySO>(mActivity, android.R.layout.simple_list_item_single_choice, mCities),
				mCities.indexOf(mCity), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mCity = mCities.get(which);
						mLinesSorts = mActivity.getService().getLinesSorts(mCity);
						mSelectedLinesSorts = new ArrayList<LineSortSO>(mLinesSorts);
						updateLines();
						dialog.dismiss();
					}
				});
		dialog.show();
	}

	public void showSelectLinesSorts() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
		dialog.setTitle(R.string.select_sorts);
		final int linesSortsSize = mLinesSorts.size();
		String[] linesSortsNames = new String[linesSortsSize];
		final boolean[] linesSortsChecks = new boolean[linesSortsSize];
		int i = 0;
		for (LineSortSO ls : mLinesSorts) {
			linesSortsNames[i] = ls.getName();
			if (mSelectedLinesSorts.contains(ls))
				linesSortsChecks[i] = true;
			i++;
		}
		dialog.setMultiChoiceItems(linesSortsNames, linesSortsChecks, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			}
		});
		dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSelectedLinesSorts.clear();
				for (int i = 0; i < linesSortsSize; i++) {
					if (linesSortsChecks[i])
						mSelectedLinesSorts.add(mLinesSorts.get(i));
				}
				updateLines();
			}
		});
		dialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_city:
			showSelectCity();
			break;
		case R.id.menu_line_sort:
			showSelectLinesSorts();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}