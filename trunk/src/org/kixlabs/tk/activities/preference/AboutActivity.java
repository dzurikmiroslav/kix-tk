package org.kixlabs.tk.activities.preference;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AboutActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		setTheme(TkApplication.getDialogTheme());
		super.onCreate(arg0);
		setContentView(R.layout.about);
	}
}
