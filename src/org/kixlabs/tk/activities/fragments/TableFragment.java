package org.kixlabs.tk.activities.fragments;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kixlabs.tk.DayFlags;
import org.kixlabs.tk.R;
import org.kixlabs.tk.activities.MainActivity;
import org.kixlabs.tk.activities.adapters.TableRowAdapter;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.LineSO;
import org.kixlabs.tk.service.so.SourceSO;
import org.kixlabs.tk.service.so.TableRowSO;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TableFragment extends SherlockFragment {

	private static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;

	private static final int INTERNAL_TABLE_CONTAINER_ID = 0x00ff0003;

	private static final String LINE_KEY = "line";

	private static final String DESTINATION_KEY = "destination";

	private static final String SOURCE_KEY = "source";

	private static final String TABLE_DATA_KEY = "table-data";

	private static final String SORT_FLAG_KEY = "sortFlag";

	private static final String NEAREST_KEY = "nearest";

	private MainActivity mActivity;

	private LineSO mLine;

	private DestinationSO mDestination;

	private SourceSO mSource;

	private int mSortFlag;

	private ListView mListView;

	private TextView mNearestTextView;

	private TextView mLineTextView;

	private TextView mSourceTextView;

	private TextView mDestinationTextView;

	private Date mNearest;

	private List<TableRowSO> mTableData;

	private DateFormat nTimeFormat = new SimpleDateFormat("HH:mm");

	private ScheduledThreadPoolExecutor mNearestExecutor = new ScheduledThreadPoolExecutor(3);

	private ScheduledFuture<?> mNearestScheduledFuture;

	private boolean mProgressVisible;

	private DateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy");

	private Runnable nUpdateNearestAction = new Runnable() {
		@Override
		public void run() {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateNearestCallback();
				}
			});
		}
	};

	private String formatNumber(int n) {
		if (n < 10)
			return "0" + n;
		else
			return Integer.toString(n);
	}

	private void updateNearestCallback() {
		Date now = new Date();
		if (mNearest.before(now)) {
			updateNearest();
			return;
		}
		StringBuilder builder = new StringBuilder();
		int dHours = mNearest.getHours() - now.getHours();
		int dMinutes = mNearest.getMinutes() - now.getMinutes();
		if (dMinutes < 0) {
			dMinutes += 60;
			dHours--;
		}
		builder.append(formatNumber(dHours));
		builder.append(':');
		builder.append(formatNumber(dMinutes));
		builder.append(" (");
		builder.append(nTimeFormat.format(mNearest));
		builder.append(')');
		mNearestTextView.setText(String.format(getString(R.string.nearest_bus), builder.toString()));
	}

	private void updateNearest() {
		if (mNearestScheduledFuture != null) {
			mNearestScheduledFuture.cancel(false);
			mNearestScheduledFuture = null;
		}
		mNearest = mActivity.getService().getNearest(mSource, mSortFlag);
		if (mNearest == null) {
			mNearestTextView.setText(R.string.maybe_tomorow);
		} else {
			updateNearestCallback();
			mNearestScheduledFuture = mNearestExecutor.scheduleAtFixedRate(nUpdateNearestAction, 60 - new Date().getSeconds(), 60,
					TimeUnit.SECONDS);
		}
	}

	public void showNote() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
		dialog.setTitle(R.string.note_title);
		dialog.setMessage(Html.fromHtml(mActivity.getService().getNote(mSource)));
		dialog.setNeutralButton(android.R.string.cancel, null);
		dialog.show();
	}

	public void showFilter() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
		dialog.setTitle(R.string.sort_dialog_title);
		final boolean values[] = new boolean[3];

		values[0] = (mSortFlag & DayFlags.WORK_DAYS_SCHOOL_YEAR) > 0;
		values[1] = (mSortFlag & DayFlags.WORK_DAYS_HOLIDAY) > 0;
		values[2] = (mSortFlag & DayFlags.WEEKENDS_FERIAE_DAYS) > 0;
		dialog.setMultiChoiceItems(R.array.sorts, values, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			}
		});
		dialog.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSortFlag = 0;
				if (values[0])
					mSortFlag = DayFlags.WORK_DAYS_SCHOOL_YEAR;
				if (values[1])
					mSortFlag |= DayFlags.WORK_DAYS_HOLIDAY;
				if (values[2])
					mSortFlag |= DayFlags.WEEKENDS_FERIAE_DAYS;
				if (mSource != null)
					updateDisplayData();
			}
		});
		dialog.show();
	}

	public void setDisplayData(LineSO line, DestinationSO destination, SourceSO source) {
		if (mSource != source) {
			mSource = source;
			mDestination = destination;
			mLine = line;
			updateDisplayData();
		}
		showProgress(false);
	}

	private void updateDisplayData() {
		if (mLine.hasValidFrom() && mLine.hasValidTo()) {
			mLineTextView.setText(String.format(getString(R.string.line_valid_from_to), mLine.getName(),
					mDateFormat.format(mLine.getValidFrom().getTime()), mDateFormat.format(mLine.getValidTo().getTime())));
		} else if (mLine.hasValidFrom()) {
			mLineTextView.setText(String.format(getString(R.string.line_valid_from), mLine.getName(),
					mDateFormat.format(mLine.getValidFrom().getTime())));
		} else if (mLine.hasValidTo()) {
			mLineTextView.setText(String.format(getString(R.string.line_valid_to), mLine.getName(),
					mDateFormat.format(mLine.getValidTo().getTime())));
		} else {
			mLineTextView.setText(String.format(getString(R.string.line), mLine.getName()));
		}
		mDestinationTextView.setText(String.format(getString(R.string.destination), mDestination.getName()));
		mSourceTextView.setText(String.format(getString(R.string.source), mSource.getName()));

		mTableData = mActivity.getService().getTable(mSource, mSortFlag);
		mListView.setAdapter(new TableRowAdapter(mActivity, mTableData));
		updateNearest();

		showProgress(false);
	}

	public void clearDisplayData() {
		mTableData = null;
		mNearest = null;
		mSource = null;
		mLine = null;
		mDestination = null;
		showProgress(true);
	}

	private void showProgress(boolean visible) {
		if (getView() == null)
			return;
		if (mProgressVisible != visible) {
			View progers = getView().findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
			View table = getView().findViewById(INTERNAL_TABLE_CONTAINER_ID);
			if (visible) {
				progers.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
				table.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
				progers.setVisibility(View.VISIBLE);
				table.setVisibility(View.GONE);
			} else {
				table.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
				progers.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
				table.setVisibility(View.VISIBLE);
				progers.setVisibility(View.GONE);
			}
			mProgressVisible = visible;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			mLine = (LineSO) savedInstanceState.getSerializable(LINE_KEY);
			mDestination = (DestinationSO) savedInstanceState.getSerializable(DESTINATION_KEY);
			mSource = (SourceSO) savedInstanceState.getSerializable(SOURCE_KEY);
			mSortFlag = savedInstanceState.getInt(SORT_FLAG_KEY, DayFlags.getCurrentDayFlag());
			mNearest = (Date) savedInstanceState.getSerializable(NEAREST_KEY);
			mTableData = (ArrayList<TableRowSO>) savedInstanceState.getSerializable(TABLE_DATA_KEY);
		} else {
			mSortFlag = DayFlags.getCurrentDayFlag();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(LINE_KEY, mLine);
		outState.putSerializable(DESTINATION_KEY, mDestination);
		outState.putSerializable(SOURCE_KEY, mSource);
		outState.putInt(SORT_FLAG_KEY, mSortFlag);
		outState.putSerializable(NEAREST_KEY, mNearest);
		outState.putSerializable(TABLE_DATA_KEY, (Serializable) mTableData);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mSource == null) {
			showProgress(true);
		} else {
			if (mNearestScheduledFuture == null) {
				if (mNearest == null) {
					mNearestTextView.setText(R.string.maybe_tomorow);
				} else {
					updateNearestCallback();
					mNearestScheduledFuture = mNearestExecutor.scheduleAtFixedRate(nUpdateNearestAction, 60 - new Date().getSeconds(),
							60, TimeUnit.SECONDS);
				}
				if (mTableData != null)
					mListView.setAdapter(new TableRowAdapter(mActivity, mTableData));
			}
			showProgress(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mNearestScheduledFuture != null) {
			mNearestScheduledFuture.cancel(false);
			mNearestScheduledFuture = null;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
		mActivity.setTableFragment(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.table, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_filter:
			showFilter();
			break;
		case R.id.menu_note:
			showNote();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final Context context = getActivity();

		FrameLayout root = new FrameLayout(context);

		LinearLayout pframe = new LinearLayout(context);
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		pframe.setOrientation(LinearLayout.VERTICAL);
		// pframe.setVisibility(View.GONE);
		pframe.setGravity(Gravity.CENTER);

		ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		pframe.addView(progress,
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		root.addView(pframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		FrameLayout lframe = new FrameLayout(context);
		lframe.setId(INTERNAL_TABLE_CONTAINER_ID);
		lframe.setVisibility(View.GONE);

		View v = inflater.inflate(R.layout.table, container, false);
		mListView = (ListView) v.findViewById(R.id.list_wiew);
		mNearestTextView = (TextView) v.findViewById(R.id.nearest_text_view);
		mLineTextView = (TextView) v.findViewById(R.id.line_text_view);
		mSourceTextView = (TextView) v.findViewById(R.id.source_text_view);
		mDestinationTextView = (TextView) v.findViewById(R.id.destination_text_view);

		if (mLine != null)
			updateDisplayData();

		lframe.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		mProgressVisible = true;
		return root;
	}
}