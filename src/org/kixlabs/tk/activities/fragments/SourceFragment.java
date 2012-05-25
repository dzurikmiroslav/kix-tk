package org.kixlabs.tk.activities.fragments;

import java.io.Serializable;
import java.util.List;

import org.kixlabs.tk.activities.MainActivity;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.SourceSO;

import com.actionbarsherlock.app.SherlockListFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SourceFragment extends SherlockListFragment {

	private MainActivity mActivity;

	private DestinationSO mDestination;

	private int mSelectedPosition = -1;

	private List<SourceSO> mData;
	
	static final private String DESTINATION_KEY = "destination";
	
	static final private String SELECTED_POSITION_KEY = "selected-position";
	
	static final private String DATA_KEY = "data";
	
	public void setDisplayData(DestinationSO destination) {
		if (mDestination != destination) {
			mDestination = destination;
			mData = mActivity.getService().getSources(mDestination);
			setListAdapter(new ArrayAdapter<SourceSO>(getActivity(), android.R.layout.simple_list_item_single_choice, mData));
		}
	}

	public void clearDisplayData() {
		mDestination = null;
		mData = null;
		mSelectedPosition = -1;
		setListAdapter(null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mDestination = (DestinationSO) savedInstanceState.getSerializable(DESTINATION_KEY);
			mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION_KEY, -1);
			mData = (List<SourceSO>) savedInstanceState.getSerializable(DATA_KEY);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DESTINATION_KEY, mDestination);
		outState.putInt(SELECTED_POSITION_KEY, mSelectedPosition);
		outState.putSerializable(DATA_KEY,(Serializable) mData);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		System.out.println("mSelectedPosition "+mSelectedPosition);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if (mData != null) {
			setListAdapter(new ArrayAdapter<SourceSO>(getActivity(), android.R.layout.simple_list_item_single_choice, mData));
			if (mSelectedPosition != -1)
				getListView().setItemChecked(mSelectedPosition, true);
		}
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.setmSourceFragment(this);
	}
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mSelectedPosition = position;
		SourceSO item = (SourceSO) l.getItemAtPosition(position);
		mActivity.onSelectSource(item);
	}
}