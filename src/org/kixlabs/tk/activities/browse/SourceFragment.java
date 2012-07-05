package org.kixlabs.tk.activities.browse;

import org.kixlabs.tk.browseservice.so.Source;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SourceFragment extends SherlockListFragment {

	private BrowseWorkFragment mWorkFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWorkFragment = (BrowseWorkFragment) getFragmentManager().findFragmentByTag(BrowseWorkFragment.TAG);
		mWorkFragment.setSourceFragment(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		//getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		refresh();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Source item = (Source) l.getItemAtPosition(position);
		mWorkFragment.setSelectedSourcePosition(position);
		mWorkFragment.onSourceSelected(item);
	}

	public void refresh() {
		if (mWorkFragment.getSelectedDestination() == null) {
			setListAdapter(null);
		} else {
			setListAdapter(new ArrayAdapter<Source>(getActivity(), android.R.layout.simple_list_item_1,
					mWorkFragment.getSources()));
			if (mWorkFragment.getSelectedSourcePosition() != -1)
				getListView().setItemChecked(mWorkFragment.getSelectedSourcePosition(), true);
		}
	}

}
