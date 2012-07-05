package org.kixlabs.tk.activities.preference;

import java.io.IOException;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.activities.download.DownloadActivity;
import org.kixlabs.tk.database.DatabaseHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PreferenceActivity extends SherlockPreferenceActivity {

	public static final String TAG = "PreferenceActivity";

	public static final int RESULT_DB_CHANGED = 47200;

	private static final int REQUEST_EXPORT = 47201;
	private static final int REQUEST_IMPORT = 47202;
	private static final int REQUEST_DOWNLOAD = 47203;

	private DatabaseHelper mDatabaseHelper;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(TkApplication.getAppTheme());
		super.onCreate(savedInstanceState);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mDatabaseHelper = ((TkApplication) getApplication()).getDatabaseHelper();

		addPreferencesFromResource(R.xml.preferences);

		// export db
		Preference preference = findPreference("export_db");
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(PreferenceActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
				intent.putExtra(FileDialog.SELECTION_MODE, FileDialog.SELECTION_MODE_CREATE);
				PreferenceActivity.this.startActivityForResult(intent, REQUEST_EXPORT);
				return false;
			}
		});

		// import db
		preference = findPreference("import_db");
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(PreferenceActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
				PreferenceActivity.this.startActivityForResult(intent, REQUEST_IMPORT);
				intent.putExtra(FileDialog.SELECTION_MODE, FileDialog.SELECTION_MODE_OPEN);
				return false;
			}
		});

		// download db
		preference = findPreference("download_db");
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(PreferenceActivity.this, DownloadActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivityForResult(intent, REQUEST_DOWNLOAD);
				setResult(RESULT_DB_CHANGED);
				return false;
			}
		});

		// theme
		preference = findPreference("theme");
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				((TkApplication) getApplication()).setThemeName((String) newValue);
				return true;
			}
		});

		// theme
		preference = findPreference("about");
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showAboutActivity();
				return false;
			}
		});
	}

	private void showAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case REQUEST_EXPORT:
			if (resultCode == Activity.RESULT_OK) {
				String fileName = data.getStringExtra(FileDialog.RESULT_PATH);
				try {
					mDatabaseHelper.exportData(fileName);
					Toast.makeText(PreferenceActivity.this, R.string.database_exported, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Log.e(TAG, "Cant export db to file", e);
					// TODO fragmentdialog
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setTitle((android.R.string.dialog_alert_title));
					dialog.setMessage(e.toString());
					dialog.setNegativeButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
				}
			}
			break;
		case REQUEST_IMPORT:
			if (resultCode == Activity.RESULT_OK) {
				String fileName = data.getStringExtra(FileDialog.RESULT_PATH);
				try {
					mDatabaseHelper.importData(fileName);
					setResult(RESULT_DB_CHANGED);
					Toast.makeText(PreferenceActivity.this, R.string.database_imported, Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Log.e(TAG, "Cant import db from file", e);
					// TODO fragmentdialog
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setTitle(android.R.string.dialog_alert_title);
					dialog.setMessage(e.toString());
					dialog.setNegativeButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
					e.printStackTrace();
				}
			}
			break;
		case REQUEST_DOWNLOAD:
			if (resultCode == DownloadActivity.RESULT_LINES_DOWNLOADED) {
				setResult(RESULT_DB_CHANGED);
			}
			break;
		default:
			break;
		}

	}
}
