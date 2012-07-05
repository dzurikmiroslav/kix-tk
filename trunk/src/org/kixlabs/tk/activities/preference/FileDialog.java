package org.kixlabs.tk.activities.preference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * 
 * @author alexander.ponomarev.1
 * 
 */
public class FileDialog extends SherlockFragmentActivity {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";
	private static final String ROOT = "/";

	public static final String START_PATH = "start-path";
	public static final String RESULT_PATH = "result-path";
	public static final String SELECTION_MODE = "selection-mode";

	public static final int SELECTION_MODE_CREATE = 0;
	public static final int SELECTION_MODE_OPEN = 1;

	private List<String> mPath = null;
	private TextView myPath;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button mSelectButton;
	private ListView mListView;

	private LinearLayout mLayoutSelect;
	private LinearLayout mLayoutCreate;
	private InputMethodManager mInputManager;
	private String mParentPath;
	private String mCurrentPath = ROOT;

	private int mSelectionMode = SELECTION_MODE_CREATE;

	private File mSelectedFile;
	private HashMap<String, Integer> mLastPositions = new HashMap<String, Integer>();

	public static class FileErrorDialogFragment extends SherlockDialogFragment {

		public static final String TAG = "FileErrorDialogFragment";

		private static final String FILE_NAME_KEY = "file-name";

		public static FileErrorDialogFragment newInstance(String fileName) {
			FileErrorDialogFragment fragment = new FileErrorDialogFragment();
			Bundle args = new Bundle();
			args.putString(FILE_NAME_KEY, fileName);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Builder dialog = new AlertDialog.Builder(getActivity());
			dialog.setIcon(android.R.drawable.ic_dialog_alert);
			dialog.setTitle(android.R.string.dialog_alert_title);
			dialog.setMessage(String.format(getString(R.string.cant_read_folder), getArguments().getString(FILE_NAME_KEY)));
			dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			return dialog.create();
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(TkApplication.getAppTheme());
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);
		mFileName = (EditText) findViewById(R.id.fdEditTextFile);

		mInputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				selectFile(v, position);
			}
		});

		mSelectButton = (Button) findViewById(R.id.fdButtonSelect);
		mSelectButton.setEnabled(false);
		mSelectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSelectedFile != null) {
					getIntent().putExtra(RESULT_PATH, mSelectedFile.getPath());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		final Button newButton = (Button) findViewById(R.id.fdButtonNew);
		newButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCreateVisible(v);

				mFileName.setText("");
				mFileName.requestFocus();
			}
		});

		mSelectionMode = getIntent().getIntExtra(SELECTION_MODE, SELECTION_MODE_CREATE);
		if (mSelectionMode == SELECTION_MODE_OPEN) {
			newButton.setEnabled(false);
		}

		mLayoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		mLayoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		mLayoutCreate.setVisibility(View.GONE);

		final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectVisible(v);
			}

		});
		final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFileName.getText().length() > 0) {
					getIntent().putExtra(RESULT_PATH, mCurrentPath + "/" + mFileName.getText());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		String startPath = getIntent().getStringExtra(START_PATH);
		if (startPath != null) {
			getDir(startPath);
		} else {
			getDir(ROOT);
		}

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return false;
	}

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < mCurrentPath.length();

		Integer position = mLastPositions.get(mParentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			mListView.setSelection(position);
		}

	}

	private void getDirImpl(final String dirPath) {

		mCurrentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		mPath = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(mCurrentPath);
		File[] files = f.listFiles();
		if (files == null) {
			mCurrentPath = ROOT;
			f = new File(mCurrentPath);
			files = f.listFiles();
		}
		myPath.setText(getText(R.string.location) + ": " + mCurrentPath);

		if (!mCurrentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.folder);
			mPath.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder);
			mPath.add(f.getParent());
			mParentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				filesMap.put(file.getName(), file.getName());
				filesPathMap.put(file.getName(), file.getPath());
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		mPath.addAll(dirsPathMap.tailMap("").values());
		mPath.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList, R.layout.file_dialog_row, new String[] { ITEM_KEY, ITEM_IMAGE },
				new int[] { R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}

		fileList.notifyDataSetChanged();

		mListView.setAdapter(fileList);
	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	private void selectFile(View v, int position) {

		File file = new File(mPath.get(position));

		setSelectVisible(v);

		if (file.isDirectory()) {
			mSelectButton.setEnabled(false);
			if (file.canRead()) {
				mLastPositions.put(mCurrentPath, position);
				getDir(mPath.get(position));
			} else {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(FileErrorDialogFragment.newInstance(file.getName()), FileErrorDialogFragment.TAG);
				ft.commitAllowingStateLoss();
			}
		} else {
			mSelectedFile = file;
			v.setSelected(true);
			mSelectButton.setEnabled(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			mSelectButton.setEnabled(false);

			if (mLayoutCreate.getVisibility() == View.VISIBLE) {
				mLayoutCreate.setVisibility(View.GONE);
				mLayoutSelect.setVisibility(View.VISIBLE);
			} else {
				if (!mCurrentPath.equals(ROOT)) {
					getDir(mParentPath);
				} else {
					return super.onKeyDown(keyCode, event);
				}
			}

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void setCreateVisible(View v) {
		mLayoutCreate.setVisibility(View.VISIBLE);
		mLayoutSelect.setVisibility(View.GONE);

		mInputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		mSelectButton.setEnabled(false);
	}

	private void setSelectVisible(View v) {
		mLayoutCreate.setVisibility(View.GONE);
		mLayoutSelect.setVisibility(View.VISIBLE);

		mInputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		mSelectButton.setEnabled(false);
	}
}