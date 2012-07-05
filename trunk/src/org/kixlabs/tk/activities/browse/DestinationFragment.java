package org.kixlabs.tk.activities.browse;

import org.kixlabs.tk.browseservice.so.Destination;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class DestinationFragment extends SherlockListFragment {

	private BrowseWorkFragment mWorkFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWorkFragment = (BrowseWorkFragment) getFragmentManager().findFragmentByTag(BrowseWorkFragment.TAG);
		mWorkFragment.setDestinationFragment(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		refresh();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Destination item = (Destination) l.getItemAtPosition(position);
		mWorkFragment.setSelectedDestinationPosition(position);
		mWorkFragment.onDestinationSelected(item);
	}

	public void refresh() {
		if (mWorkFragment.getSelectedLine() == null) {
			setListAdapter(null);
		} else {
			setListAdapter(new ArrayAdapter<Destination>(getActivity(), android.R.layout.simple_list_item_1,
					mWorkFragment.getDestinations()));
			if (mWorkFragment.getSelectedDestinationPosition() != -1)
				getListView().setItemChecked(mWorkFragment.getSelectedDestinationPosition(), true);
		}
	}

}
