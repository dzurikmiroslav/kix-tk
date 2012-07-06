package org.kixlabs.tk.activities.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.downloaderservice.DownloaderNotificator;
import org.kixlabs.tk.downloaderservice.DownloaderService;
import org.kixlabs.tk.downloaderservice.so.DownloaderLine;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSort;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;

public class DownloadWorkFragment extends SherlockFragment {

	public static final String TAG = "DownloadWorkFragment";

	private Boolean mDownloadingCities = false;
	private List<DownloaderTableCity> mCities;
	private int mSelectedCityPosition = -1;
	private Spinner mCitiesSpinner;
	private ProgressBar mCitiesProgressBar;

	private Boolean mDownloadingLineSorts = false;
	private List<DownloaderLineSort> mLinesSorts;
	private int mSelectedLineSortPosition = -1;
	private Spinner mLineSortsSpinner;
	private ProgressBar mLinesSortsProgressBar;

	private Boolean mDownloadingLines = false;
	private List<DownloaderLine> mLines;
	private String[] mLinesNames;
	private boolean[] mLinesChecks;
	private Button mLinesButton;
	private ProgressBar mLinesProgressBar;

	private CheckBox mParseNotesCheckBox;

	private Button mDownloadButton;

	private DownloaderService mDownloaderService;

	private FragmentActivity mActivity;

	private static final int NOTIFICATION_ID = 1;

	private NotificationManager mNotificationManager;
	private Notification mNotification;

	private DownloadAndStroreLinesTask mDownloadStroreLinesTask;

	private class DownloadCitiesTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				mCities = mDownloaderService.getCities();
			} catch (IOException e) {
				Log.e(TAG, "Probably have connection problem");
				showNetworkErrorDialog();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			synchronized (this) {
				if (mCitiesSpinner != null) {
					mCitiesSpinner.setEnabled(false);
					mCitiesProgressBar.setVisibility(View.VISIBLE);
					mLineSortsSpinner.setEnabled(false);
					mLinesButton.setEnabled(false);
					mDownloadButton.setEnabled(false);
				}
				mDownloadingCities = true;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			synchronized (this) {
				if (mCitiesSpinner != null && mCities != null) {
					mCitiesSpinner.setAdapter(new ArrayAdapter<DownloaderTableCity>(mActivity, android.R.layout.simple_spinner_item,
							mCities) {
						{
							setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						}
					});
					mSelectedCityPosition = -1;
					mCitiesSpinner.setEnabled(true);
					mCitiesProgressBar.setVisibility(View.INVISIBLE);
				}
				mDownloadingCities = false;
			}
		}
	}

	private class DownloadLinesSortsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				mLinesSorts = mDownloaderService.getLineSorts(mCities.get(mSelectedCityPosition));
			} catch (IOException e) {
				Log.e(TAG, "Probably have connection problem");
				showNetworkErrorDialog();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			synchronized (this) {
				if (mLineSortsSpinner != null) {
					mLineSortsSpinner.setEnabled(false);
					mLinesSortsProgressBar.setVisibility(View.VISIBLE);
					mLinesButton.setEnabled(false);
					mDownloadButton.setEnabled(false);
				}
				mDownloadingLineSorts = true;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			synchronized (this) {
				if (mDownloadingLineSorts != null && mLinesSorts != null) {
					mLineSortsSpinner.setAdapter(new ArrayAdapter<DownloaderLineSort>(mActivity, android.R.layout.simple_spinner_item,
							mLinesSorts) {
						{
							setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						}
					});
					mSelectedLineSortPosition = -1;
					mLineSortsSpinner.setEnabled(true);
					mLinesSortsProgressBar.setVisibility(View.INVISIBLE);
				}
				mDownloadingLineSorts = false;
			}
		}
	}

	private class DownloadLinesTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				mLines = mDownloaderService.getUnfetchedLines(mCities.get(mSelectedCityPosition),
						mLinesSorts.get(mSelectedLineSortPosition));
				mLinesNames = new String[mLines.size()];
				mLinesChecks = new boolean[mLines.size()];
				int i = 0;
				for (DownloaderLine l : mLines)
					mLinesNames[i++] = l.getName();
			} catch (IOException e) {
				Log.e(TAG, "Probably have connection problem");
				showNetworkErrorDialog();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			synchronized (this) {
				if (mLinesButton != null) {
					mLinesButton.setEnabled(false);
					mLinesProgressBar.setVisibility(View.VISIBLE);
					mDownloadButton.setEnabled(false);
				}
				mDownloadingLines = true;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			synchronized (this) {
				if (mLinesButton != null) {
					mLinesButton.setEnabled(true);
					mLinesProgressBar.setVisibility(View.INVISIBLE);
					mDownloadButton.setEnabled(true);
				}
				mDownloadingLines = false;
			}
		}
	}

	private class DownloadAndStroreLinesTask extends AsyncTask<DownloaderLine, Object, Integer> {

		private int mCurrentLine;

		private String mCurrentLineName;

		private int mLineCount;

		private int mBusStopCount;

		@Override
		protected void onProgressUpdate(Object... values) {
			int value = (Integer) values[0];

			FragmentManager fm = mActivity.getSupportFragmentManager();

			ProgressDialogFragment fetctchingDialog = (ProgressDialogFragment) fm.findFragmentByTag(ProgressDialogFragment.TAG);
			if (fetctchingDialog != null)
				fetctchingDialog.setProgress(value);

			mNotification.contentView.setProgressBar(R.id.progessBar, mBusStopCount, value, false);
			mNotification.contentView.setTextViewText(R.id.text, Math.round((((1 + value) / (float) mBusStopCount) * 100)) + "%");
			mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		}

		@Override
		protected Integer doInBackground(DownloaderLine... lines) {
			mCurrentLine = 0;
			mLineCount = lines.length;

			DownloaderNotificator notificator = new DownloaderNotificator() {
				@Override
				public void onDownloadBusStop(int position) {
					publishProgress(position, true);
				}

				@Override
				public void onStororeBusStop(int position) {
					publishProgress(position, false);
				}

				@Override
				public void onStartDownloading(int busStopsCount) {
					mBusStopCount = busStopsCount;
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String message = String.format(getString(R.string.fetching_line_message), mCurrentLineName,
									mCurrentLine + 1, mLineCount);
							synchronized (DownloadWorkFragment.this) {
								FragmentManager fm = mActivity.getSupportFragmentManager();
								FragmentTransaction ft = fm.beginTransaction();

								Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.TAG);
								if (prev != null)
									ft.remove(prev);
								ProgressDialogFragment newFragment = ProgressDialogFragment.newInstance(message,
										getString(R.string.downloading_line_title), mBusStopCount);
								ft.add(newFragment, ProgressDialogFragment.TAG);
								ft.commitAllowingStateLoss();
							}

							mNotification.contentView.setTextViewText(R.id.title, message);
							mNotification.contentView.setTextViewText(R.id.text, "0%");
							mNotification.tickerText = message;
							mNotificationManager.notify(NOTIFICATION_ID, mNotification);
						}
					});
				}

				@Override
				public void onStartDatabaseStoring(int busStopsCount) {
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String message = String.format(getString(R.string.storing_line_message), mCurrentLineName,
									mCurrentLine + 1, mLineCount);

							synchronized (DownloadWorkFragment.this) {
								FragmentManager fm = mActivity.getSupportFragmentManager();
								FragmentTransaction ft = fm.beginTransaction();

								Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.TAG);
								if (prev != null)
									ft.remove(prev);
								DialogFragment newFragment = ProgressDialogFragment.newInstance(message,
										getString(R.string.storing_line_title), mBusStopCount);
								ft.add(newFragment, ProgressDialogFragment.TAG);
								ft.commitAllowingStateLoss();
							}

							mNotification.contentView.setTextViewText(R.id.title, message);
							mNotification.contentView.setTextViewText(R.id.text, "0%");
							mNotification.tickerText = message;
							mNotificationManager.notify(NOTIFICATION_ID, mNotification);
						}
					});
				}
			};

			try {
				for (DownloaderLine line : lines) {
					mCurrentLineName = line.getName();
					mDownloaderService.downloadLine(line, notificator, mParseNotesCheckBox.isChecked());
					mCurrentLine++;
					if (mCurrentLine == 1)
						getActivity().setResult(DownloadActivity.RESULT_LINES_DOWNLOADED);
				}
			} catch (IOException e) {
				Log.e(TAG, "Probably have connection problem");
				showNetworkErrorDialog();
			} catch (InterruptedException e) {
				Log.d(TAG, "Downloading was interupted");
			}
			return mCurrentLine;
		}

		@Override
		protected void onPreExecute() {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity);
			builder.setSmallIcon(android.R.drawable.stat_sys_download);
			Intent intent = new Intent(mActivity.getApplicationContext(), DownloadActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(mActivity.getApplicationContext(), 0, intent, 0);
			builder.setContentIntent(contentIntent);
			builder.setAutoCancel(true);

			mNotification = builder.getNotification();
			mNotification.contentView = new RemoteViews(TkApplication.class.getPackage().getName(), R.layout.notification);
			mNotification.flags = mNotification.flags | Notification.FLAG_ONGOING_EVENT;
		}

		@Override
		protected void onPostExecute(Integer result) {
			onFinished();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			onFinished();
		}

		private void onFinished() {
			synchronized (DownloadWorkFragment.this) {
				mDownloadStroreLinesTask = null;

				FragmentManager fm = mActivity.getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(ProgressDialogFragment.TAG);
				if (fragment != null)
					ft.remove(fragment);
				ft.commitAllowingStateLoss();
			}

			if (mCurrentLine > 0) {
				new DownloadLinesTask().execute();
			}

			String title = getString(R.string.successfuly_downloaded_title);
			String message = String.format(getString(R.string.successfuly_downloaded_message), mCurrentLine);
			Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();

			NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity);
			builder.setSmallIcon(android.R.drawable.stat_sys_download);
			Intent intent = new Intent(mActivity, DownloadActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			PendingIntent contentIntent = PendingIntent.getActivity(mActivity, 0, intent, 0);
			builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
			builder.setContentIntent(contentIntent);
			builder.setTicker(title);
			builder.setContentTitle(title);
			builder.setContentText(message);
			builder.setAutoCancel(true);

			mNotification = builder.getNotification();
			mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		}
	}

	public static class ProgressDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "ProgressDialogFragment";

		private static final String MESSAGE = "mesage";

		private static final String TITLE = "title";

		private static final String MAX = "max";

		public static ProgressDialogFragment newInstance(String message, String title, int max) {
			ProgressDialogFragment fragment = new ProgressDialogFragment();
			Bundle args = new Bundle();
			args.putString(MESSAGE, message);
			args.putString(TITLE, title);
			args.putInt(MAX, max);
			fragment.setArguments(args);
			fragment.setCancelable(false);
			return fragment;
		}

		public void setProgress(int value) {
			if (getDialog() != null) // ak sa este nestihol vytvorit view ? :-/
				((ProgressDialog) getDialog()).setProgress(value);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setMessage(getArguments().getString(MESSAGE));
			dialog.setTitle(getArguments().getString(TITLE));
			dialog.setMax(getArguments().getInt(MAX));
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DownloadWorkFragment workerFragemnt = (DownloadWorkFragment) getActivity().getSupportFragmentManager()
									.findFragmentByTag(DownloadWorkFragment.TAG);
							workerFragemnt.cancelDownloading();
						}
					});
			return dialog;
		}
	}

	public static class NewtowrkErrorDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "NewtowrkErrorDialogFragment";

		public static NewtowrkErrorDialogFragment newInstance() {
			return new NewtowrkErrorDialogFragment();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setTitle(android.R.string.dialog_alert_title);
			dialog.setMessage(R.string.network_error);
			dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					getActivity().finish();
				}
			});
			return dialog.create();
		}
	}

	public static class SelectLinesDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "SelectLinesDialogFragment";

		public static SelectLinesDialogFragment newInstance() {
			return new SelectLinesDialogFragment();
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			DownloadWorkFragment workFragment = (DownloadWorkFragment) getFragmentManager()
					.findFragmentByTag(DownloadWorkFragment.TAG);
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setTitle(R.string.select_lines);
			if (workFragment.mLinesNames.length > 0) {
				dialog.setMultiChoiceItems(workFragment.mLinesNames, workFragment.mLinesChecks,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							}
						});
			} else {
				dialog.setMessage(R.string.this_lines_already_downloaded);
			}
			dialog.setPositiveButton(android.R.string.ok, null);
			return dialog.create();
		}
	}

	private void showNetworkErrorDialog() {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(NewtowrkErrorDialogFragment.newInstance(), NewtowrkErrorDialogFragment.TAG);
				ft.commitAllowingStateLoss();
			}
		});
	}

	private void cancelDownloading() {
		if (mDownloadStroreLinesTask != null) {
			mDownloadStroreLinesTask.cancel(true);
		}
	}

	public void selectCity(int position) {
		if (mSelectedCityPosition != position) {
			mSelectedCityPosition = position;
			new DownloadLinesSortsTask().execute();
		}
	}

	public void selectLineSort(int position) {
		if (mSelectedLineSortPosition != position) {
			mSelectedLineSortPosition = position;
			new DownloadLinesTask().execute();
		}
	}

	public void selectLines() {
		if (mLines == null)
			return;
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(SelectLinesDialogFragment.newInstance(), SelectLinesDialogFragment.TAG);
		ft.commitAllowingStateLoss();
	}

	public void downloadSelectedLines() {
		if (mDownloadStroreLinesTask == null) {
			List<DownloaderLine> selected = new ArrayList<DownloaderLine>();
			int i = 0;
			for (DownloaderLine p : mLines)
				if (mLinesChecks[i++])
					selected.add(p);
			if (selected.isEmpty()) {
				Toast.makeText(mActivity, R.string.no_line_to_download, Toast.LENGTH_LONG).show();
			} else {
				mDownloadStroreLinesTask = new DownloadAndStroreLinesTask();
				mDownloadStroreLinesTask.execute(selected.toArray(new DownloaderLine[selected.size()]));
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TkApplication app = (TkApplication) mActivity.getApplication();
		mDownloaderService = new DownloaderService(app.getDatabaseHelper());

		mNotificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

		setRetainInstance(true);

		new DownloadCitiesTask().execute();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		synchronized (this) {
			mActivity = (FragmentActivity) activity;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		synchronized (this) {
			mDownloadButton = (Button) getTargetFragment().getView().findViewById(R.id.downloadButton);
			mDownloadButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					downloadSelectedLines();
				}
			});

			mLinesButton = (Button) getTargetFragment().getView().findViewById(R.id.linesButton);
			mLinesButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectLines();
				}
			});
			if (mDownloadingLines) {
				mLinesButton.setEnabled(false);
				mLinesProgressBar.setVisibility(View.VISIBLE);
				mDownloadButton.setEnabled(false);
			}

			mLineSortsSpinner = (Spinner) getTargetFragment().getView().findViewById(R.id.linesSortsSpinner);
			mLineSortsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					selectLineSort(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			mLinesSortsProgressBar = (ProgressBar) getTargetFragment().getView().findViewById(R.id.linesSortsProgressBar);
			if (mDownloadingLineSorts) {
				mLineSortsSpinner.setEnabled(false);
				mLinesSortsProgressBar.setVisibility(View.VISIBLE);
				mLinesButton.setEnabled(false);
				mDownloadButton.setEnabled(false);
			} else {
				mLinesSortsProgressBar.setVisibility(View.INVISIBLE);
				if (mLinesSorts != null) {
					mLineSortsSpinner.setAdapter(new ArrayAdapter<DownloaderLineSort>(mActivity, android.R.layout.simple_spinner_item,
							mLinesSorts) {
						{
							setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						}
					});
				}
			}
			mLineSortsSpinner.setSelection(mSelectedLineSortPosition);

			mCitiesSpinner = (Spinner) getTargetFragment().getView().findViewById(R.id.citiesSpinner);
			mCitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					selectCity(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			mCitiesProgressBar = (ProgressBar) getTargetFragment().getView().findViewById(R.id.citiesProgressBar);
			if (mDownloadingCities) {
				mCitiesSpinner.setEnabled(false);
				mCitiesProgressBar.setVisibility(View.VISIBLE);
				mLineSortsSpinner.setEnabled(false);
				mLinesButton.setEnabled(false);
				mDownloadButton.setEnabled(false);
			} else {
				mCitiesProgressBar.setVisibility(View.INVISIBLE);
				if (mCities != null) {
					mCitiesSpinner.setAdapter(new ArrayAdapter<DownloaderTableCity>(mActivity, android.R.layout.simple_spinner_item,
							mCities) {
						{
							setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						}
					});
				}
			}
			mCitiesSpinner.setSelection(mSelectedCityPosition);
		}

		mLinesButton = (Button) getTargetFragment().getView().findViewById(R.id.linesButton);
		mLinesProgressBar = (ProgressBar) getTargetFragment().getView().findViewById(R.id.linesProgressBar);

		mDownloadButton = (Button) getTargetFragment().getView().findViewById(R.id.downloadButton);

		mParseNotesCheckBox = (CheckBox) getTargetFragment().getView().findViewById(R.id.parseNotesCheckBox);
	}
}
