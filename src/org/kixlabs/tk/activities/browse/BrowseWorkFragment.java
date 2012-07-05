package org.kixlabs.tk.activities.browse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kixlabs.tk.DayFlags;
import org.kixlabs.tk.R;
import org.kixlabs.tk.TkApplication;
import org.kixlabs.tk.browseservice.BrowseService;
import org.kixlabs.tk.browseservice.so.City;
import org.kixlabs.tk.browseservice.so.Destination;
import org.kixlabs.tk.browseservice.so.Line;
import org.kixlabs.tk.browseservice.so.LineSort;
import org.kixlabs.tk.browseservice.so.Source;
import org.kixlabs.tk.browseservice.so.TableRow;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class BrowseWorkFragment extends SherlockFragment {

	public static final String TAG = "BrowseWorkFragment";

	private BrowseService mBrowseService;

	private BrowseActivity mActivity;

	private List<City> mCities;
	private City mSelectedCity;

	private List<LineSort> mLinesSorts;
	private List<LineSort> mSelectedLinesSorts;

	private List<Line> mLines;
	private Line mSelectedLine;
	private int mSelectedLinePosition = -1;

	private List<Destination> mDestinations;
	private Destination mSelectedDestination;
	private int mSelectedDestinationPosition = -1;

	private List<Source> mSources;
	private Source mSelectedSource;
	private int mSelectedSourcePosition = -1;

	private List<TableRow> mTableRows;
	private Calendar mNearest;
	private int mSortFlag;

	private static final String SELECTED_CITY_KEY = "selected-city";
	private static final String SELECTED_LINE_SORT_KEY = "selected-lines-sort";
	private static final String SELECTED_LINE_SORTS_COUNT_KEY = "selected-line-sorts-count";

	private LineFragment mLineFragment;
	private DestinationFragment mDestinationFragment;
	private SourceFragment mSourceFragment;
	private TimeTableFragment mTimeTableFragment;

	private boolean mRealyExit = false;

	public void setRealyExitFalse() {
		mRealyExit = false;
	}
	
	public List<Line> getLines() {
		return mLines;
	}

	public List<City> getCities() {
		return mCities;
	}

	public List<LineSort> getLinesSorts() {
		return mLinesSorts;
	}

	public List<LineSort> getSelectedLinesSorts() {
		return mSelectedLinesSorts;
	}

	public City getSelectedCity() {
		return mSelectedCity;
	}

	public Line getSelectedLine() {
		return mSelectedLine;
	}

	public int getSelectedLinePosition() {
		return mSelectedLinePosition;
	}

	public void setSelectedLinePosition(int selectedLinePosition) {
		this.mSelectedLinePosition = selectedLinePosition;
	}

	public List<Destination> getDestinations() {
		return mDestinations;
	}

	public Destination getSelectedDestination() {
		return mSelectedDestination;
	}

	public int getSelectedDestinationPosition() {
		return mSelectedDestinationPosition;
	}

	public void setSelectedDestinationPosition(int selectedDestinationPosition) {
		this.mSelectedDestinationPosition = selectedDestinationPosition;
	}

	public int getSelectedSourcePosition() {
		return mSelectedSourcePosition;
	}

	public void setSelectedSourcePosition(int selectedSourcePosition) {
		this.mSelectedSourcePosition = selectedSourcePosition;
	}

	public List<Source> getSources() {
		return mSources;
	}

	public Source getSelectedSource() {
		return mSelectedSource;
	}

	public int getSortFlag() {
		return mSortFlag;
	}

	public List<TableRow> getTableRows() {
		return mTableRows;
	}

	public Calendar getNearest() {
		return mNearest;
	}

	public void updateNearest() {
		mNearest = mBrowseService.getNearest(mSelectedSource, mSortFlag);
	}

	public void setLineFragment(LineFragment lineFragment) {
		this.mLineFragment = lineFragment;
	}

	public void setDestinationFragment(DestinationFragment destinationFragment) {
		this.mDestinationFragment = destinationFragment;
	}

	public void setSourceFragment(SourceFragment sourceFragment) {
		this.mSourceFragment = sourceFragment;
	}

	public void setTimeTableFragment(TimeTableFragment timeTableFragment) {
		this.mTimeTableFragment = timeTableFragment;
	}

	public String getCurrentSourceNote() {
		return mBrowseService.getNote(mSelectedSource);
	}

	/**
	 * 
	 * @param city
	 */
	public void onCitySelected(City city) {
		if (mSelectedCity != city) {
			mSelectedCity = city;
			mLinesSorts = mBrowseService.getLinesSorts(mSelectedCity);
			mSelectedLinesSorts = new ArrayList<LineSort>(mLinesSorts);
			mSelectedLinePosition = -1;
			mLines = mBrowseService.getLines(mSelectedCity, mSelectedLinesSorts);
			if (mLineFragment != null)
				mLineFragment.refresh();
			onLineSelected(null);
		}
	}

	/**
	 * 
	 */
	public void onLineSortsChanged() {
		mSelectedLinePosition = -1;
		mLines = mBrowseService.getLines(mSelectedCity, mSelectedLinesSorts);
		mLineFragment.refresh();
		onLineSelected(null);
	}

	/**
	 * 
	 * @param line
	 */
	public void onLineSelected(Line line) {
		if (mSelectedLine != line) {
			mSelectedLine = line;
			if (mSelectedLine == null)
				mDestinations = null;
			else
				mDestinations = mBrowseService.getDestinations(mSelectedLine);
			mSelectedDestination = null;
			mSelectedDestinationPosition = -1;

			if (mDestinationFragment != null)
				mDestinationFragment.refresh();
		}
		if (mSelectedLine != null)
			mActivity.slideToPage(1);
		mRealyExit = false;
	}

	/**
	 * 
	 * @param destination
	 */
	public void onDestinationSelected(Destination destination) {
		if (mSelectedDestination != destination) {
			mSelectedDestination = destination;
			if (mSelectedDestination == null)
				mSources = null;
			else
				mSources = mBrowseService.getSources(mSelectedDestination);
			mSelectedSource = null;
			mSelectedSourcePosition = -1;

			if (mSourceFragment != null)
				mSourceFragment.refresh();
		}
		if (mSelectedDestination != null)
			mActivity.slideToPage(2);
	}

	/**
	 * 
	 * @param source
	 */
	public void onSourceSelected(Source source) {
		if (mSelectedSource != source) {
			mSelectedSource = source;
			if (mSelectedSource == null) {
				mNearest = null;
				mTableRows = null;
			} else {
				mNearest = mBrowseService.getNearest(mSelectedSource, mSortFlag);
				mTableRows = mBrowseService.getTable(mSelectedSource, mSortFlag);
			}

			if (mTimeTableFragment != null)
				mTimeTableFragment.refresh();
		}
		if (mSelectedSource != null)
			mActivity.slideToPage(3);
	}

	/**
	 * 
	 * @param sortFlag
	 */
	public void onSelectSortFrag(int sortFlag) {
		if (mSortFlag != sortFlag) {
			mSortFlag = sortFlag;
			if (mSelectedSource != null) {
				mNearest = mBrowseService.getNearest(mSelectedSource, mSortFlag);
				mTableRows = mBrowseService.getTable(mSelectedSource, mSortFlag);

				if (mTimeTableFragment != null)
					mTimeTableFragment.refresh();
			}
		}
	}

	public boolean onBackPressed() {
		if (mRealyExit) {
			return true;
		} else {
			if (mActivity.getCurrentPage() == 0) {
				mRealyExit = true;
				Toast.makeText(getActivity(), R.string.press_back_again, Toast.LENGTH_SHORT).show();
			} else {
				mActivity.slideToPreviosPage();
			}
			return false;
		}
	}

	public String makeTimeTableTitle() {
		if (mSelectedSource != null)
			return String.format(getActivity().getString(R.string.time_table_title), mSelectedLine.getName(),
					mSelectedSource.getName(), mSelectedDestination.getName());
		return "";
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mRealyExit = false;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		synchronized (this) {
			mActivity = (BrowseActivity) activity;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TkApplication app = (TkApplication) mActivity.getApplication();
		mBrowseService = new BrowseService(app.getDatabaseHelper());

		mCities = mBrowseService.getCities();

		SharedPreferences preferences = getActivity().getPreferences(Context.MODE_WORLD_READABLE);
		long cityId = preferences.getLong(SELECTED_CITY_KEY, -1);
		if (cityId != -1) {
			for (City c : mCities) {
				if (c.getId() == cityId) {
					mSelectedCity = c;
					break;
				}
			}
			if (mSelectedCity != null) {
				mLinesSorts = mBrowseService.getLinesSorts(mSelectedCity);
				mSelectedLinesSorts = new ArrayList<LineSort>();
				int count = preferences.getInt(SELECTED_LINE_SORTS_COUNT_KEY, 0);
				Set<Long> contains = new HashSet<Long>();
				for (int i = 0; i < count; i++)
					contains.add(preferences.getLong(SELECTED_LINE_SORT_KEY + i, 0));
				for (LineSort ls : mLinesSorts)
					if (contains.contains(ls.getId()))
						mSelectedLinesSorts.add(ls);
			}
		}
		if (mSelectedCity == null) {
			if (!mCities.isEmpty())
				mSelectedCity = mCities.get(0);
			mLinesSorts = mBrowseService.getLinesSorts(mSelectedCity);
			mSelectedLinesSorts = new ArrayList<LineSort>(mLinesSorts);
		}
		mLines = mBrowseService.getLines(mSelectedCity, mSelectedLinesSorts);

		mSortFlag = DayFlags.getCurrentDayFlag();

		setRetainInstance(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Editor editor = mActivity.getPreferences(Context.MODE_WORLD_WRITEABLE).edit();
		if (mSelectedCity != null) {
			editor.putLong(SELECTED_CITY_KEY, mSelectedCity.getId());
			editor.putInt(SELECTED_LINE_SORTS_COUNT_KEY, mSelectedLinesSorts.size());
			int i = 0;
			for (LineSort ls : mSelectedLinesSorts)
				editor.putLong(SELECTED_LINE_SORT_KEY + i++, ls.getId());
		} else {
			editor.putLong(SELECTED_CITY_KEY, -1);
			editor.putInt(SELECTED_LINE_SORTS_COUNT_KEY, 0);
		}
		editor.commit();
	}

}
