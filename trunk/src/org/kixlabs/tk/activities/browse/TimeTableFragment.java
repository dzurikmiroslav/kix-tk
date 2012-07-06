package org.kixlabs.tk.activities.browse;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kixlabs.tk.DayFlags;
import org.kixlabs.tk.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TimeTableFragment extends SherlockFragment {

	private BrowseWorkFragment mWorkFragment;

	private ListView mRowsListView;

	private TextView mNearestTextView;

	private ScheduledThreadPoolExecutor mUpdateNearestExecutor = new ScheduledThreadPoolExecutor(1);

	private ScheduledFuture<?> mUpdateNearestScheduledFuture;

	private Runnable nUpdateNearestAction = new Runnable() {
		@Override
		public void run() {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateNearest();
				}
			});
		}
	};

	public static class NoteDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "NoteDialogFragment";

		private static final String MESSAGE_KEY = "message";

		public static NoteDialogFragment newInstance(String message) {
			NoteDialogFragment fragment = new NoteDialogFragment();
			Bundle args = new Bundle();
			args.putString(MESSAGE_KEY, message);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setTitle(R.string.note_title);
			dialog.setMessage(Html.fromHtml(getArguments().getString(MESSAGE_KEY)));
			dialog.setNeutralButton(android.R.string.cancel, null);
			return dialog.create();
		}
	}

	public static class SortDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "SortDialogFragment";

		private static final String SORT_CHECKS_KEY = "line-sort-checks";

		private boolean[] mSortChecks;

		public static SortDialogFragment newInstance() {
			SortDialogFragment fragment = new SortDialogFragment();
			return fragment;
		}

		@Override
		public void onSaveInstanceState(Bundle bundle) {
			bundle.putBooleanArray(SORT_CHECKS_KEY, mSortChecks);
			super.onSaveInstanceState(bundle);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final BrowseWorkFragment workFragment = (BrowseWorkFragment) getFragmentManager()
					.findFragmentByTag(BrowseWorkFragment.TAG);

			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(R.string.sort_dialog_title);

			if (savedInstanceState != null && savedInstanceState.containsKey(SORT_CHECKS_KEY)) {
				mSortChecks = savedInstanceState.getBooleanArray(SORT_CHECKS_KEY);
			} else {
				mSortChecks = new boolean[3];
				mSortChecks[0] = (workFragment.getSortFlag() & DayFlags.WORK_DAYS_SCHOOL_YEAR) > 0;
				mSortChecks[1] = (workFragment.getSortFlag() & DayFlags.WORK_DAYS_HOLIDAY) > 0;
				mSortChecks[2] = (workFragment.getSortFlag() & DayFlags.WEEKENDS_FERIAE_DAYS) > 0;
			}

			dialog.setMultiChoiceItems(R.array.entries_sorts, mSortChecks, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				}
			});
			dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int sortFlag = 0;
					if (mSortChecks[0])
						sortFlag = DayFlags.WORK_DAYS_SCHOOL_YEAR;
					if (mSortChecks[1])
						sortFlag |= DayFlags.WORK_DAYS_HOLIDAY;
					if (mSortChecks[2])
						sortFlag |= DayFlags.WEEKENDS_FERIAE_DAYS;
					workFragment.onSelectSortFrag(sortFlag);
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
		mWorkFragment.setTimeTableFragment(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mUpdateNearestScheduledFuture != null) {
			mUpdateNearestScheduledFuture.cancel(false);
			mUpdateNearestScheduledFuture = null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.table, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_filter:
			showSortDialog();
			break;
		case R.id.menu_note:
			showNoteDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showSortDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(SortDialogFragment.newInstance(), SortDialogFragment.TAG);
		ft.commitAllowingStateLoss();
	}

	public void showNoteDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(NoteDialogFragment.newInstance(mWorkFragment.getCurrentSourceNote()), NoteDialogFragment.TAG);
		ft.commitAllowingStateLoss();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.table, container, false);
		mNearestTextView = (TextView) view.findViewById(R.id.nearest_text_view);
		mRowsListView = (ListView) view.findViewById(R.id.list_wiew);
		return view;
	}

	public void refresh() {
		if (mWorkFragment.getSelectedSource() == null) {
			mRowsListView.setAdapter(null);
			mNearestTextView.setText("");
			if (mUpdateNearestScheduledFuture != null) {
				mUpdateNearestScheduledFuture.cancel(false);
				mUpdateNearestScheduledFuture = null;
			}
		} else {
			mRowsListView.setAdapter(new TableRowAdapter(getActivity(), mWorkFragment.getTableRows()));
			updateNearest();
			if (mUpdateNearestScheduledFuture == null) {
				mUpdateNearestScheduledFuture = mUpdateNearestExecutor.scheduleAtFixedRate(nUpdateNearestAction, 60 - Calendar
						.getInstance().get(Calendar.SECOND), 60, TimeUnit.SECONDS);
			}
		}
	}

	private void updateNearest() {
		mNearestTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		if (mWorkFragment.getNearest() == null) {
			mNearestTextView.setText(R.string.maybe_tomorow);
			mNearestTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.trollface, 0);
		} else {
			Calendar now = Calendar.getInstance();
			if (mWorkFragment.getNearest().before(now))
				mWorkFragment.updateNearest();

			int dHours = mWorkFragment.getNearest().get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY);
			int dMinutes = mWorkFragment.getNearest().get(Calendar.MINUTE) - now.get(Calendar.MINUTE);
			if (dMinutes < 0) {
				dMinutes += 60;
				dHours--;
			}
			
			if (dHours == 0 && dMinutes <= 1)
				mNearestTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.kiddingface, 0);
				
			StringBuilder builder = new StringBuilder();
			builder.append(String.format("%02d:%02d", dHours, dMinutes));
			builder.append(String.format(" (%1$tH:%1$tM)", mWorkFragment.getNearest()));
			mNearestTextView.setText(String.format(getString(R.string.nearest_bus), builder.toString()));
		}
	}

}
