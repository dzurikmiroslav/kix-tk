package org.kixlabs.tk.activities;

import java.io.IOException;

import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.R;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.lamerman.FileDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class PreferenceActivity extends SherlockPreferenceActivity {

	private final int REQUEST_EXPORT = 213847239;

	private final int REQUEST_IMPORT = 213847238;

	private TkApplication mApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (TkApplication) getApplication();

		setPreferenceScreen(createPreferenceHierarchy());
	}

	@Override
	protected synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			String fileName = data.getStringExtra(FileDialog.RESULT_PATH);
			if (requestCode == REQUEST_EXPORT) {
				try {
					mApp.getDatabaseHelper().exportData(fileName);
				} catch (IOException e) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setTitle("Error");
					dialog.setMessage(e.toString());
					dialog.setNegativeButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					dialog.show();
					e.printStackTrace();
				}
			} else if (requestCode == REQUEST_IMPORT) {
				try {
					mApp.getDatabaseHelper().importData(fileName);
				} catch (IOException e) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setTitle("Error");
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

		}
	}

	private PreferenceScreen createPreferenceHierarchy() {
		// Root
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		PreferenceCategory category = new PreferenceCategory(this);
		category.setTitle(R.string.pref_database);
		root.addPreference(category);

		// import db
		Preference preference = new Preference(this);
		preference.setTitle(R.string.pref_export_file);
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(PreferenceActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				PreferenceActivity.this.startActivityForResult(intent, REQUEST_EXPORT);
				return false;
			}
		});
		category.addPreference(preference);

		// export db
		preference = new Preference(this);
		preference.setTitle(R.string.pref_import_file);
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(PreferenceActivity.this.getBaseContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				PreferenceActivity.this.startActivityForResult(intent, REQUEST_IMPORT);
				return false;
			}
		});
		category.addPreference(preference);

		// parse
		preference = new Preference(this);
		preference.setTitle(R.string.pref_parse);
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(PreferenceActivity.this, ParseActivity.class));
				return false;
			}
		});
		category.addPreference(preference);

		return root;
	}

}
