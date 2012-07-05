package org.kixlabs.tk;

import org.kixlabs.tk.database.DatabaseHelper;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TkApplication extends Application {

	private static int sAppTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light;
	private static int sDialogTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light_Dialog;
	
	private static final String THEME_KEY = "theme";

	private DatabaseHelper mDatabaseHelper;

	public void setThemeName(String themeName) {
		if ("dark".equals(themeName)) {
			sAppTheme = com.actionbarsherlock.R.style.Theme_Sherlock;
			sDialogTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Dialog;
		} else if ("lihgt_dark_ab".equals(themeName)) {
			sAppTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light_DarkActionBar;
			sDialogTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light_Dialog;
		} else {
			sAppTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light;
			sDialogTheme = com.actionbarsherlock.R.style.Theme_Sherlock_Light_Dialog;
		}
	}

	public DatabaseHelper getDatabaseHelper() {
		return mDatabaseHelper;
	}
	
	public static int getAppTheme() {
		return sAppTheme;
	} 

	public static int getDialogTheme() {
		return sDialogTheme;		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDatabaseHelper = new DatabaseHelper(this);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		setThemeName(prefs.getString(THEME_KEY, "light"));
	}

}
