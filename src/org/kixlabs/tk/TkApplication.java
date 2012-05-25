package org.kixlabs.tk;

import org.kixlabs.tk.database.DatabaseHelper;

import android.app.Application;

public class TkApplication extends Application {

	private DatabaseHelper mDatabaseHelper;

	public DatabaseHelper getDatabaseHelper() {
		return mDatabaseHelper;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDatabaseHelper = new DatabaseHelper(this);
		mDatabaseHelper.openDatabase();
	}

}
