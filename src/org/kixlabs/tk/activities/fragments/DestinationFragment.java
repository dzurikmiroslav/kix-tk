package org.kixlabs.tk.activities.fragments;

import java.io.Serializable;
import java.util.List;

import org.kixlabs.tk.activities.MainActivity;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.LineSO;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class DestinationFragment extends SherlockListFragment {

	private MainActivity mActivity;

	private LineSO mLine;

	private int mSelectedPosition = -1;

	private List<DestinationSO> mData;

	static final private String LINE_KEY = "line";

	static final private String SELECTED_POSITION_KEY = "selected-position";

	static final private String DATA_KEY = "data";

	public void setDisplayData(LineSO line) {
		if (mLine != line) {
			mLine = line;
			mSelectedPosition = -1;
			mData = mActivity.getService().getDestinations(mLine);
			setListAdapter(new ArrayAdapter<DestinationSO>(getActivity(), android.R.layout.simple_list_item_single_choice, mData));
		}
	}

	public void clearDisplayData() {
		mLine = null;
		mData = null;
		mSelectedPosition = -1;
		setListAdapter(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (mData != null) {
			setListAdapter(new ArrayAdapter<DestinationSO>(getActivity(), android.R.layout.simple_list_item_single_choice, mData));
			if (mSelectedPosition != -1)
				getListView().setItemChecked(mSelectedPosition, true);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mLine = (LineSO) savedInstanceState.getSerializable(LINE_KEY);
			mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY, -1);
			mData = (List<DestinationSO>) savedInstanceState.getSerializable(DATA_KEY);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(LINE_KEY, mLine);
		outState.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
		outState.putSerializable(DATA_KEY, (Serializable) mData);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.setDestinationFragment(this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mSelectedPosition = position;
		DestinationSO item = (DestinationSO) l.getItemAtPosition(position);
		mActivity.onSelectDestination(item);
	}

}