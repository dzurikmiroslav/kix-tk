package org.kixlabs.tk.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.downloaderservice.FetcherNotificator;
import org.kixlabs.tk.downloaderservice.ParserService;
import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class ParseActivity extends SherlockActivity {

	private static final String CITIES_KEY = "mCities";

	private static final String SELECTED_CITY_POSITION_KEY = "mSelectedCityPosition";

	private static final String LINES_SORTS_KEY = "mLinesSorts";

	private static final String SELECTED_LINES_SORT_KEY = "mSelectedLineSortPosition";

	private static final String LINES_KEY = "mLines";

	private static final String LINES_NAMES_KEY = "mLinesNames";

	private static final String LINES_CHECKS_KEY = "mLinesChecks";

	private static final int DIALOG_FETCHING_DATA = 1;

	private static final int DIALOG_FETCHING_LINE = 2;

	private static final int DIALOG_STORING_LINE = 3;

	private ParserService mParserService;

	private TkApplication mApplication;

	private List<DownloaderCitySO> mCities;

	private int mSelectedCityPosition;

	private List<DownloaderLineSortSO> mLinesSorts;

	private int mSelectedLineSortPosition;

	private List<DownloaderLineSO> mLines;

	private String[] mLinesNames;

	private boolean[] mLinesChecks;

	private static ProgressDialog mFetchingDataDialog;

	private static ProgressDialog mFetchingLineDialog;

	private static ProgressDialog mStoringLineDialog;

	private Spinner mCitiesSpinner;

	private Spinner mLinesSortsSpinner;

	private Runnable mShowProgressDialog = new Runnable() {
		@Override
		public void run() {
			showDialog(DIALOG_FETCHING_DATA);
		}
	};

	private Runnable mDismissProgressDialog = new Runnable() {
		@Override
		public void run() {
			if (mFetchingDataDialog.isShowing())
				mFetchingDataDialog.dismiss();
		}
	};

	class UiTask extends AsyncTask<Runnable, Void, Void> {
		@Override
		protected Void doInBackground(Runnable... params) {
			params[0].run();
			return null;
		}
	};

	private Runnable mDownloadLinesSorts = new Runnable() {
		@Override
		public void run() {
			try {
				runOnUiThread(mShowProgressDialog);
				mLinesSorts = mParserService.getLineSorts(mCities.get(mSelectedCityPosition));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mLinesSortsSpinner.setAdapter(new ArrayAdapter<DownloaderLineSortSO>(ParseActivity.this,
								android.R.layout.simple_spinner_item, mLinesSorts) {
							{
								setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							}
						});
					}
				});
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				runOnUiThread(mDismissProgressDialog);
			}
		}
	};

	private Runnable mDownloadLines = new Runnable() {
		@Override
		public void run() {
			try {
				runOnUiThread(mShowProgressDialog);
				mLines = mParserService.getUnfetchedLines(mCities.get(mSelectedCityPosition),
						mLinesSorts.get(mSelectedLineSortPosition));
				mLinesNames = new String[mLines.size()];
				mLinesChecks = new boolean[mLines.size()];
				int i = 0;
				for (DownloaderLineSO l : mLines)
					mLinesNames[i++] = l.getName();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				runOnUiThread(mDismissProgressDialog);
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_FETCHING_DATA:
			mFetchingDataDialog = new ProgressDialog(this);
			mFetchingDataDialog.setTitle(R.string.fetching_title);
			mFetchingDataDialog.setMessage(getString(R.string.fetching_message));
			mFetchingDataDialog.setCancelable(false);
			return mFetchingDataDialog;
		case DIALOG_FETCHING_LINE:
			mFetchingLineDialog = new ProgressDialog(this);
			mFetchingLineDialog.setTitle(getString(R.string.fetching_title));
			mFetchingLineDialog.setMessage(getString(R.string.fetching_message));
			mFetchingLineDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mFetchingLineDialog.setCancelable(true);
			return mFetchingLineDialog;
		case DIALOG_STORING_LINE:
			mStoringLineDialog = new ProgressDialog(this);
			mStoringLineDialog.setTitle(R.string.storing_line);
			mStoringLineDialog.setCancelable(false);
			mStoringLineDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			return mStoringLineDialog;
		}
		return null;
	}

	private void setupCitiesAndLinesSorts() {
		mCitiesSpinner
				.setAdapter(new ArrayAdapter<DownloaderCitySO>(ParseActivity.this, android.R.layout.simple_spinner_item, mCities) {
					{
						setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					}
				});
		mCitiesSpinner.setSelection(mSelectedCityPosition);

		mLinesSortsSpinner.setAdapter(new ArrayAdapter<DownloaderLineSortSO>(ParseActivity.this, android.R.layout.simple_spinner_item,
				mLinesSorts) {
			{
				setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			}
		});
		mLinesSortsSpinner.setSelection(mSelectedLineSortPosition);

		mCitiesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (mSelectedCityPosition != position) {
					mSelectedCityPosition = position;
					new UiTask().execute(mDownloadLinesSorts, null);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		mLinesSortsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (mSelectedLineSortPosition != position) {
					mSelectedLineSortPosition = position;
					new UiTask().execute(mDownloadLines, null);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
		// WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
		// WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
		// WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.parse);

		mApplication = (TkApplication) getApplication();
		mParserService = new ParserService(mApplication.getDatabaseHelper());

		mCitiesSpinner = (Spinner) findViewById(R.id.citiesSpinner);
		mLinesSortsSpinner = (Spinner) findViewById(R.id.linesSortsSpinner);
		final Button linesButton = (Button) findViewById(R.id.linesButton);
		final Button parseButton = (Button) findViewById(R.id.parseButton);
		final CheckBox parseNotesCheckBox = (CheckBox) findViewById(R.id.parseNotesCheckBox);

		// sFetchLock.lock();
		if (savedInstanceState != null) {
			mCities = (List<DownloaderCitySO>) savedInstanceState.getSerializable(CITIES_KEY);
			mSelectedCityPosition = savedInstanceState.getInt(SELECTED_CITY_POSITION_KEY);
			mLinesSorts = (List<DownloaderLineSortSO>) savedInstanceState.getSerializable(LINES_SORTS_KEY);
			mSelectedLineSortPosition = savedInstanceState.getInt(SELECTED_LINES_SORT_KEY);
			mLines = (List<DownloaderLineSO>) savedInstanceState.getSerializable(LINES_KEY);
			mLinesNames = savedInstanceState.getStringArray(LINES_NAMES_KEY);
			mLinesChecks = savedInstanceState.getBooleanArray(LINES_CHECKS_KEY);
		}

		if (mCities == null) {
			new UiTask().execute(new Runnable() {
				@Override
				public void run() {
					runOnUiThread(mShowProgressDialog);
					try {
						mCities = mParserService.getCities();
						if (!mCities.isEmpty()) {
							mSelectedCityPosition = 0;
							mLinesSorts = mParserService.getLineSorts(mCities.get(mSelectedCityPosition));
							if (!mLinesSorts.isEmpty()) {
								mSelectedLineSortPosition = 0;
								mLines = mParserService.getUnfetchedLines(mCities.get(mSelectedCityPosition),
										mLinesSorts.get(mSelectedLineSortPosition));
								mLinesNames = new String[mLines.size()];
								mLinesChecks = new boolean[mLines.size()];
								int i = 0;
								for (DownloaderLineSO l : mLines)
									mLinesNames[i++] = l.getName();
							} else {
								mSelectedLineSortPosition = -1;
								mLinesNames = new String[0];
								mLinesChecks = new boolean[0];
							}
						} else {
							mSelectedCityPosition = -1;
							mSelectedLineSortPosition = -1;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setupCitiesAndLinesSorts();
							if (mFetchingDataDialog.isShowing())
								mFetchingDataDialog.dismiss();
						}
					});
				}
			}, null);

		} else {
			setupCitiesAndLinesSorts();
		}

		linesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLines == null)
					return;
				AlertDialog.Builder dialog = new AlertDialog.Builder(ParseActivity.this);
				dialog.setTitle(R.string.select_lines);
				dialog.setMultiChoiceItems(mLinesNames, mLinesChecks, new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					}
				});
				dialog.setPositiveButton(android.R.string.ok, null);
				dialog.show();
			}
		});
		parseNotesCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mParserService.setParseNotes(isChecked);
			}
		});
		parseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<DownloaderLineSO> selected = new ArrayList<DownloaderLineSO>();
				int i = 0;
				for (DownloaderLineSO p : mLines)
					if (mLinesChecks[i++])
						selected.add(p);
				ParseLinesTask fetchTask = new ParseLinesTask();
				fetchTask.execute(selected);
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(CITIES_KEY, (Serializable) mCities);
		outState.putInt(SELECTED_CITY_POSITION_KEY, mSelectedCityPosition);
		outState.putSerializable(LINES_SORTS_KEY, (Serializable) mLinesSorts);
		outState.putInt(SELECTED_LINES_SORT_KEY, mSelectedLineSortPosition);
		outState.putSerializable(LINES_KEY, (Serializable) mLines);
		outState.putStringArray(LINES_NAMES_KEY, mLinesNames);
		outState.putBooleanArray(LINES_CHECKS_KEY, mLinesChecks);
	}

	private class ParseLinesTask extends AsyncTask<List<DownloaderLineSO>, Object, Integer> {

		private String mLineName;

		private boolean mFetchingPhase;

		private boolean canceled = false;

		private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				System.out.println("DialogInterface.OnCancelListener onCancel");
				ParseLinesTask.this.cancel(false);
			}
		};

		public ParseLinesTask() {
		}

		@Override
		protected Integer doInBackground(List<DownloaderLineSO>... params) {
			int counter = 0;
			try {
				FetcherNotificator notificator = new FetcherNotificator() {
					@Override
					public void onDownloadBusStop(String source, String destination, int position) {
						publishProgress(source, destination, position);
					}

					@Override
					public void onStartDownloading(final int busStopsCount) {
						mFetchingPhase = true;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showDialog(DIALOG_FETCHING_LINE);
								mFetchingLineDialog.setMax(busStopsCount);
								mFetchingLineDialog.setOnCancelListener(cancelListener);
							}
						});
					}

					@Override
					public void onStartDatabaseStoring(final int busStopsCount) {
						mFetchingPhase = false;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mFetchingLineDialog.isShowing())
									mFetchingLineDialog.dismiss();
								showDialog(DIALOG_STORING_LINE);
								mStoringLineDialog.setMax(busStopsCount);
							}
						});
					}

					@Override
					public void onStororeBusStop(int position) {
						publishProgress(position);
					}
				};
				for (DownloaderLineSO line : params[0]) {
					if (canceled)
						break;
					mLineName = line.getName();
					mParserService.downloadLine(line, notificator);
					counter++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mDownloadLines.run();
			return counter;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			if (mFetchingPhase) {
				mFetchingLineDialog.setMessage(String.format(getString(R.string.fetching_line), mLineName, (String) values[0],
						(String) values[1]));
				mFetchingLineDialog.setProgress((Integer) values[2]);
			} else {
				mStoringLineDialog.setProgress((Integer) values[0]);
			}
		}

		@Override
		protected void onCancelled() {
			System.out.println("AsyncTask onCancelled");
			canceled = true;
			mParserService.interruptLinesParsing();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (mStoringLineDialog != null && mStoringLineDialog.isShowing())
				mStoringLineDialog.dismiss();
			Toast.makeText(ParseActivity.this, String.format(getString(R.string.successfuly_fetched), result), Toast.LENGTH_LONG);
		}

	}

}
