package org.kixlabs.tk.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.kixlabs.tk.downloaderservice.FetcherNotificator;
import org.kixlabs.tk.downloaderservice.ParseServiceDatabaseHelper;
import org.kixlabs.tk.downloaderservice.so.DownloaderBusStopSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderCellSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderRowSO;
import org.kixlabs.tk.service.ServiceDatabaseHelper;
import org.kixlabs.tk.service.so.CitySO;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.LineSO;
import org.kixlabs.tk.service.so.LineSortSO;
import org.kixlabs.tk.service.so.SourceSO;
import org.kixlabs.tk.service.so.TableCellSO;
import org.kixlabs.tk.service.so.TableRowSO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper implements ServiceDatabaseHelper, ParseServiceDatabaseHelper {

	private static final String DB_NAME = "kix_tk.sqlite";

	private static final String DB_PATH = Environment.getDataDirectory() + "/data/org.kixlabs.tk/databases/";

	private static final int DB_VERSION_NUMBER = 1;

	private static final String TABLE_CREATE_CITY = "CREATE TABLE City (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	Name TEXT NOT NULL UNIQUE\n" + ");\n";

	private static final String INDEX_CREATE_CITY = "CREATE INDEX CityIndex ON City(Name);\n";

	private static final String TABLE_CREATE_LINE_SORT = "CREATE TABLE LineSort (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	Name TEXT NOT NULL UNIQUE\n" + ");\n";

	private static final String INDEX_CREATE_LINE_SORT = "CREATE INDEX LineSortIndex ON LineSort(Name);\n";

	private static final String TABLE_CREATE_LINE = "CREATE TABLE Line (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	Name TEXT NOT NULL,\n" + "	ValidFrom DATE,\n" + "	ValidTo DATE,\n"
			+ " City INTEGER REFERENCES City(Id) ON DELETE CASCADE,\n"
			+ " LineSort INTEGER REFERENCES LineSort(Id) ON DELETE CASCADE\n" + ");\n";

	private static final String INDEX_CREATE_LINE = "CREATE INDEX LineIndex ON Line(Name);\n";

	private static final String TRIGGER_CREATE_LINE_CASCADE = "CREATE TRIGGER LineCascade DELETE ON City\n" + "FOR EACH ROW \n"
			+ "BEGIN\n" + "	DELETE FROM Line WHERE City = OLD.Id;\n" + "END;\n";

	private static final String TABLE_CREATE_BUS_STOP_NAME = "CREATE TABLE BusStopName (\n"
			+ "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n" + "	Name TEXT NOT NULL\n" + ");\n"
			+ "CREATE INDEX IndexBusStopName ON BusStopName(Name);\n";

	private static final String INDEX_CREATE_BUS_STOP_NAME = "CREATE INDEX BusStopNameIndex ON BusStopName(Name);\n";

	private static final String TABLE_CREATE_BUS_STOP = "CREATE TABLE BusStop (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	OrderNumber INT NOT NULL,\n" + "	Note TEXT,\n" + "	Line INTEGER NOT NULL REFERENCES Line(Id) ON DELETE CASCADE,\n"
			+ "	SrcBusStopName BIGINT NOT NULL REFERENCES BusStopName(Id),\n"
			+ "	DstBusStopName BIGINT NOT NULL REFERENCES BusStopName(Id)\n" + ");\n";

	private static final String INDEX_CREATE_BUS_STOP = "CREATE INDEX BusStopIndex ON BusStop(OrderNumber);\n";

	private static final String TRIGGER_CREATE_BUS_STOP_CASCADE = "CREATE TRIGGER BusStopCascade DELETE ON Line\n" + "FOR EACH ROW \n"
			+ "BEGIN\n" + "	DELETE FROM BusStop WHERE Line = OLD.Id;\n" + "END;\n";

	private static final String TABLE_CREATE_ROW = "CREATE TABLE Row (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	Hour INT NOT NULL,\n" + "	BusStop INTEGER NOT NULL REFERENCES BusStop(Id) ON DELETE CASCADE\n" + ");";

	private static final String INDEX_CREATE_ROW = "CREATE INDEX RowIndex ON Row(Hour);\n";

	private static final String TRIGGER_CREATE_ROW_CASCADE = "CREATE TRIGGER RowCascade DELETE ON BusStop\n" + "FOR EACH ROW \n"
			+ "BEGIN\n" + "	DELETE FROM Row WHERE BusStop = OLD.Id;\n" + "END;\n";

	private static final String TABLE_CREATE_CELL = "CREATE TABLE Cell (\n" + "	Id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "	Minute TINYINT NOT NULL,\n" + "	Sort TINYINT NOT NULL,\n" + "	Note TINYINT DEFAULT 0,\n"
			+ "	Row INTEGER NOT NULL REFERENCES Row(Id) ON DELETE CASCADE\n" + ");\n";

	private static final String TRIGGER_CREATE_CELL_CASCADE = "CREATE TRIGGER CellCascade DELETE ON Row\n" + "FOR EACH ROW \n"
			+ "BEGIN\n" + "	DELETE FROM Cell WHERE Row = OLD.Id;\n" + "END;\n";

	private static final String INDEX_CREATE_CELL = "CREATE INDEX CellIndex ON Cell(Minute, Sort, Note, Row);\n";

	private SQLiteDatabase mDatabase = null;

	private static final String TAG = "DatabaseHelper";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION_NUMBER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Creating the database...");
		db.execSQL(TABLE_CREATE_CITY);
		db.execSQL(INDEX_CREATE_CITY);

		db.execSQL(TABLE_CREATE_LINE_SORT);
		db.execSQL(INDEX_CREATE_LINE_SORT);

		db.execSQL(TABLE_CREATE_LINE);
		db.execSQL(INDEX_CREATE_LINE);
		db.execSQL(TRIGGER_CREATE_LINE_CASCADE);

		db.execSQL(TABLE_CREATE_BUS_STOP_NAME);
		db.execSQL(INDEX_CREATE_BUS_STOP_NAME);

		db.execSQL(TABLE_CREATE_BUS_STOP);
		db.execSQL(INDEX_CREATE_BUS_STOP);
		db.execSQL(TRIGGER_CREATE_BUS_STOP_CASCADE);

		db.execSQL(TABLE_CREATE_ROW);
		db.execSQL(TRIGGER_CREATE_ROW_CASCADE);
		db.execSQL(INDEX_CREATE_ROW);

		db.execSQL(TABLE_CREATE_CELL);
		db.execSQL(TRIGGER_CREATE_CELL_CASCADE);
		db.execSQL(INDEX_CREATE_CELL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void openDatabase() throws SQLException {
		Log.i(TAG, "Opening database");
		if (mDatabase == null) {
			Log.i("openDB", "Creating new database");
			mDatabase = getWritableDatabase();
		} else {
			Log.i(TAG, "Database alredy exists");
		}
	}

	public void closeDatabase() {
		Log.i(TAG, "Closing database...");
		if (this.mDatabase != null) {
			if (this.mDatabase.isOpen())
				this.mDatabase.close();
		}
	}

	public void importData(String fileName) throws IOException {
		Log.i(TAG, "Importing db from file \"" + fileName + '"');
		try {
			// closeDatabase();
			File dbFile = new File(DB_PATH + DB_NAME);

			File importFile = new File(fileName);

			InputStream in = new FileInputStream(importFile);
			OutputStream out = new FileOutputStream(dbFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			in.close();
			out.close();

			// openDatabase();
		} catch (IOException e) {
			Log.e("DatabaseHelper", "Cant import db from file " + e.toString());
			// openDatabase();
			throw e;
		}
		Log.i("DatabaseHelper", "Succesfull import db from file \"" + fileName + '"');
	}

	public void exportData(String fileName) throws IOException {
		Log.i(TAG, "Exporting db to file \"" + fileName + '"');
		try {
			File dbFile = new File(DB_PATH + DB_NAME);

			File exportFile = new File(fileName);
			if (!exportFile.exists())
				exportFile.createNewFile();

			InputStream in = new FileInputStream(dbFile);
			OutputStream out = new FileOutputStream(exportFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			in.close();
			out.close();
		} catch (IOException e) {
			Log.i(TAG, "Cant export db to file " + e.toString());
			throw e;
		}
		Log.i(TAG, "Succesfull export db to file \"" + fileName + '"');
	}

	void copyFile(File src, File dst) throws IOException {
		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	// Metody servisu

	private Calendar milisecondsToCalendar(long milliseconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		return calendar;
	}

	@Override
	public List<CitySO> getCities() {
		List<CitySO> list = new ArrayList<CitySO>();
		Cursor cursor = mDatabase.query("City", new String[] { "Id", "Name" }, null, null, null, null, "Name");
		while (cursor.moveToNext())
			list.add(new CitySO(cursor.getLong(0), cursor.getString(1)));
		return list;
	}

	@Override
	public List<LineSortSO> getLinesSorts(CitySO city) {
		List<LineSortSO> list = new ArrayList<LineSortSO>();
		if (city != null) {
			Cursor cursor = mDatabase.query(true, "LineSort JOIN City JOIN Line", new String[] { "LineSort.Id", "LineSort.Name" },
					"City.Id = ? AND City.Id = Line.City AND LineSort.Id = Line.LineSort",
					new String[] { Long.toString(city.getId()) }, null, null, "LineSort.Name", null);
			while (cursor.moveToNext())
				list.add(new LineSortSO(cursor.getLong(0), cursor.getString(1)));
		}
		return list;
	}

	@Override
	public List<LineSO> getLines(CitySO city, List<LineSortSO> linesSorts) {
		List<LineSO> list = new ArrayList<LineSO>();
		if (city != null) {
			for (LineSortSO ls : linesSorts) {
				Cursor cursor = mDatabase.query("Line", new String[] { "Id", "Name", "ValidFrom", "ValidTo" },
						"City = ? AND LineSort = ?", new String[] { Long.toString(city.getId()), Long.toString(ls.getId()) }, null,
						null, "ABS(Name), Name");
				while (cursor.moveToNext())
					list.add(new LineSO(cursor.getLong(0), cursor.getString(1), milisecondsToCalendar(cursor.getLong(2)),
							milisecondsToCalendar(cursor.getLong(3))));
			}
		}
		return list;
	}

	@Override
	public List<DestinationSO> getDestinations(LineSO line) {
		ArrayList<DestinationSO> list = new ArrayList<DestinationSO>();
		Cursor cursor = mDatabase.query(true, "BusStop JOIN BusStopName", new String[] { "DstBusStopName AS Id", "Name" },
				"Line = ? AND BusStopName.Id = DstBusStopName", new String[] { String.valueOf(line.getId()) }, null, null, "Name",
				null);
		while (cursor.moveToNext())
			list.add(new DestinationSO(cursor.getLong(0), cursor.getString(1), line.getId()));
		return list;
	}

	@Override
	public List<SourceSO> getSources(DestinationSO destination) {
		ArrayList<SourceSO> list = new ArrayList<SourceSO>();
		Cursor cursor = mDatabase.query(false, "BusStop JOIN BusStopName", new String[] { "BusStop.Id AS Id", "BusStopName.Name" },
				"Line = ? AND BusStopName.Id = SrcBusStopName AND DstBusStopName = ?",
				new String[] { String.valueOf(destination.getLineId()), String.valueOf(destination.getId()) }, null, null,
				"OrderNumber", null);
		while (cursor.moveToNext())
			list.add(new SourceSO(cursor.getLong(0), cursor.getString(1)));
		return list;
	}

	@Override
	public List<TableRowSO> getTable(SourceSO source, int sortFlag) {
		ArrayList<TableRowSO> list = new ArrayList<TableRowSO>();
		Map<Integer, ArrayList<TableCellSO>> map = new TreeMap<Integer, ArrayList<TableCellSO>>();
		Cursor cursor = mDatabase.query(false, "Row JOIN Cell", new String[] { "Hour, Minute, Note, Sort" },
				"Cell.Row = Row.Id AND Row.BusStop = ? AND Cell.Sort & ? > 0",
				new String[] { String.valueOf(source.getId()), String.valueOf(sortFlag) }, null, null, "Hour, Minute", null);
		while (cursor.moveToNext()) {
			int hour = cursor.getInt(0);
			if (map.get(hour) == null)
				map.put(hour, new ArrayList<TableCellSO>());
			map.get(hour).add(new TableCellSO(cursor.getInt(1), (char) cursor.getInt(2)));
		}

		Iterator<Entry<Integer, ArrayList<TableCellSO>>> it = map.entrySet().iterator();
		if (it.hasNext()) {
			Entry<Integer, ArrayList<TableCellSO>> ent = it.next();
			list.add(new TableRowSO(ent.getKey(), ent.getValue()));
		}
		Iterator<Entry<Integer, ArrayList<TableCellSO>>> prevIt = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ArrayList<TableCellSO>> prevEnt = prevIt.next();
			Entry<Integer, ArrayList<TableCellSO>> ent = it.next();
			for (int i = prevEnt.getKey() + 1; i < ent.getKey(); ++i)
				list.add(new TableRowSO(i));
			list.add(new TableRowSO(ent.getKey(), ent.getValue()));
		}

		return list;
	}

	@Override
	public Calendar getNearest(SourceSO source, int sortFlag) {
		Calendar calendar = Calendar.getInstance();
		Cursor cursor = mDatabase.query(false, "Row JOIN Cell", new String[] { "Hour", "Minute" },
				"Cell.Row = Row.Id AND Row.BusStop = ? AND Sort & ? AND ((Hour = ? AND Minute > ?) OR (Hour > ?))",
				new String[] { String.valueOf(source.getId()), String.valueOf(sortFlag), String.valueOf(calendar.get(Calendar.HOUR)),
						String.valueOf(calendar.get(Calendar.MINUTE)), String.valueOf(calendar.get(Calendar.HOUR)) }, null, null,
				"Hour, Minute", "1");
		if (cursor.moveToFirst()) {
			calendar.set(Calendar.HOUR, cursor.getInt(0));
			calendar.set(Calendar.MINUTE, cursor.getInt(1));
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar = null;
		}
		return calendar;
	}

	@Override
	public String getNote(SourceSO source) {
		if (source != null) {
			Cursor cursor = mDatabase.query("BusStop", new String[] { "Note" }, "Id = ?",
					new String[] { String.valueOf(source.getId()) }, null, null, null);
			if (cursor.moveToFirst())
				return cursor.getString(0) == null ? "" : cursor.getString(0);
			else
				return "";
		}
		return "";
	}

	/* Parser functions */

	// Linky

	private InsertHelper mInsertBusStopHelper;
	private int mBusStopOrderNumberIndex, mBusStopLineIndex, mBusStopSrcBusStopNameIndex, mBusStopDstBusStopNameIndex;

	private InsertHelper mInsertCellHelper;
	private int mCellMinuteIndex, mCellSortIndex, mCellNoteIndex, mCellRowIndex;

	private InsertHelper mInsertBusStopNameHelper;
	private int mBusStopNameNameIndex;

	private InsertHelper mInsertRowHelper;
	private int mRowHourIndex, mRowBusStopIndex;

	@Override
	public void initDBForInserting() {
		if (mInsertBusStopHelper == null) {
			// busstop
			mInsertBusStopHelper = new InsertHelper(mDatabase, "BusStop");
			mBusStopOrderNumberIndex = mInsertBusStopHelper.getColumnIndex("OrderNumber");
			mBusStopLineIndex = mInsertBusStopHelper.getColumnIndex("Line");
			mBusStopSrcBusStopNameIndex = mInsertBusStopHelper.getColumnIndex("SrcBusStopName");
			mBusStopDstBusStopNameIndex = mInsertBusStopHelper.getColumnIndex("DstBusStopName");
			// cell
			mInsertCellHelper = new InsertHelper(mDatabase, "Cell");
			mCellMinuteIndex = mInsertCellHelper.getColumnIndex("Minute");
			mCellSortIndex = mInsertCellHelper.getColumnIndex("Sort");
			mCellNoteIndex = mInsertCellHelper.getColumnIndex("Note");
			mCellRowIndex = mInsertCellHelper.getColumnIndex("Row");
			// busstop name
			mInsertBusStopNameHelper = new InsertHelper(mDatabase, "BusStopName");
			mBusStopNameNameIndex = mInsertBusStopNameHelper.getColumnIndex("Name");
			// row
			mInsertRowHelper = new InsertHelper(mDatabase, "Row");
			mRowHourIndex = mInsertRowHelper.getColumnIndex("Hour");
			mRowBusStopIndex = mInsertRowHelper.getColumnIndex("BusStop");
		}
	}

	private void deleteLine(long lineId) {
		mDatabase.delete("Line", "Id = ?", new String[] { Long.toString(lineId) });
	}

	private boolean isLineOutOfDate(long lineId, Date date) {
		Cursor cursor = mDatabase.query("Line", new String[] { "ValidTo" }, "Id = ?", new String[] { Long.toString(lineId) }, null,
				null, null);
		if (cursor.moveToFirst()) {
			long validTo = cursor.getLong(0);
			if (validTo != 0)
				return cursor.getLong(0) < date.getTime();
			else
				return true;
		} else {
			return true;
		}
	}

	private long getLineId(String name, long cityId) {
		Cursor cursor = mDatabase.query("Line", new String[] { "Id" }, "Name = ? AND City = ?",
				new String[] { name, Long.toString(cityId) }, null, null, null);
		if (cursor.moveToFirst())
			return cursor.getLong(0);
		else
			return -1;
	}

	private long createLine(String name, long cityId, long sortId, long validFrom, long validTo) {
		ContentValues initialValues = new ContentValues();
		initialValues.put("Name", name);
		initialValues.put("City", cityId);
		initialValues.put("LineSort", sortId);
		initialValues.put("ValidFrom", validFrom);
		initialValues.put("ValidTo", validTo);
		return mDatabase.insert("Line", null, initialValues);
	}

	// Mesta

	private long getCityId(String name) {
		Cursor cursor = mDatabase.query("City", new String[] { "Id" }, "Name = ?", new String[] { name }, null, null, null);
		if (cursor.moveToFirst())
			return cursor.getLong(0);
		else
			return -1;
	}

	private long createCity(String name) {
		ContentValues values = new ContentValues();
		values.put("Name", name);
		return mDatabase.insert("City", null, values);
	}

	// Druhy liniek

	private long getLineSortId(String name) {
		Cursor cursor = mDatabase.query("LineSort", new String[] { "Id" }, "Name = ?", new String[] { name }, null, null, null);
		if (cursor.moveToFirst())
			return cursor.getLong(0);
		else
			return -1;
	}

	private long createLineSort(String name) {
		ContentValues values = new ContentValues();
		values.put("Name", name);
		return mDatabase.insert("LineSort", null, values);
	}

	private long createOrGetBusStopNameId(String name) {
		Cursor cursor = mDatabase.query("BusStopName", new String[] { "Id" }, "Name = ?", new String[] { name }, null, null, null);
		if (cursor.moveToFirst()) {
			return cursor.getLong(0);
		} else {
			mInsertBusStopNameHelper.prepareForInsert();
			mInsertBusStopNameHelper.bind(mBusStopNameNameIndex, name);
			return mInsertBusStopNameHelper.execute();
		}
	}

	private long createBusStop(long lineId, long sourceId, long destinationId, int order) {
		mInsertBusStopHelper.prepareForInsert();
		mInsertBusStopHelper.bind(mBusStopOrderNumberIndex, order);
		mInsertBusStopHelper.bind(mBusStopLineIndex, lineId);
		mInsertBusStopHelper.bind(mBusStopSrcBusStopNameIndex, sourceId);
		mInsertBusStopHelper.bind(mBusStopDstBusStopNameIndex, destinationId);
		return mInsertBusStopHelper.execute();
	}

	private void setBusStopNote(long busStopId, String note) {
		ContentValues values = new ContentValues();
		values.put("Note", note);
		mDatabase.update("BusStop", values, "Id = ?", new String[] { Long.toString(busStopId) });
	}

	/*
	 * private void addBusStopCell(long busStopId, int hour, int minute, int
	 * sort, char note) { Cursor cursor = mDatabase.query("Row", new String[] {
	 * "Id" }, "BusStop = ? AND Hour = ?", new String[] {
	 * Long.toString(busStopId), Integer.toString(hour) }, null, null, null);
	 * long rowId = -1; if (cursor.moveToFirst()) { rowId = cursor.getLong(0); }
	 * else { ContentValues values = new ContentValues(); values.put("Hour",
	 * hour); values.put("BusStop", busStopId); rowId = mDatabase.insert("Row",
	 * null, values); } cursor = mDatabase.query("Cell", new String[] { "Id" },
	 * "Row = ? AND Minute = ? AND Note = ?", new String[] {
	 * Long.toString(rowId), Integer.toString(minute), Integer.toString(note) },
	 * null, null, null, null); if (cursor.moveToFirst()) { long cellId =
	 * cursor.getLong(0);
	 * mDatabase.execSQL("UPDATE Cell SET Sort = (Sort | ?) WHERE Id = ?", new
	 * String[] { Integer.toString(sort), Long.toString(cellId) }); } else {
	 * ContentValues values = new ContentValues(); values.put("Minute", minute);
	 * values.put("Sort", sort); values.put("Note", (short) note);
	 * values.put("Row", rowId); mDatabase.insert("Cell", null, values); } }
	 */
	// ///////////////////////
	// nove111
	// /////////////////////////
	@Override
	public void getCitiesIdsIfExists(List<DownloaderCitySO> cities) {
		for (DownloaderCitySO c : cities)
			c.setId(getCityId(c.getName()));
	}

	@Override
	public void getLinesSortsIdsIfExists(List<DownloaderLineSortSO> linesSorts) {
		for (DownloaderLineSortSO ls : linesSorts)
			ls.setId(getLineSortId(ls.getName()));
	}

	@Override
	public void getLinesIdsIfExists(List<DownloaderLineSO> lines) {
		for (DownloaderLineSO l : lines)
			l.setId(getLineId(l.getName(), l.getCity().getId()));
	}

	// TODO transaction?
	@Override
	public void createLine(DownloaderLineSO line, FetcherNotificator noficator) {
		Log.i(TAG, "creating line " + line.getName());
		long cityId = line.getCity().getId();
		if (cityId == -1) {
			cityId = createCity(line.getCity().getName());
			line.getCity().setId(cityId);
		}
		long lineSortId = line.getLineSort().getId();
		if (lineSortId == -1) {
			lineSortId = createLineSort(line.getLineSort().getName());
			line.getLineSort().setId(lineSortId);
		}
		long lineId = createLine(line.getName(), cityId, lineSortId, line.getValidFrom().getTimeInMillis(), line.getValidTo()
				.getTimeInMillis());
		line.setId(lineId);

		int position = 0;
		for (DownloaderBusStopSO bs : line.getBusStops()) {
			if (noficator != null)
				noficator.onStororeBusStop(position++);
			long destinationId = createOrGetBusStopNameId(bs.getDestination());
			long sourceId = createOrGetBusStopNameId(bs.getSource());
			long busStopId = createBusStop(lineId, sourceId, destinationId, bs.getOrderNuber());

			for (DownloaderRowSO r : bs.getRows()) {
				long rowId = createOrGetHour(busStopId, r.getHour());
				for (DownloaderCellSO c : r.getCells())
					addCell(rowId, c.getMinute(), c.getNote(), c.getSort());
			}
			if (bs.getNote() != null)
				setBusStopNote(busStopId, bs.getNote());
		}
	}

	private long createOrGetHour(long busStopId, byte hour) {
		Cursor cursor = mDatabase.query("Row", new String[] { "Id" }, "BusStop = ? AND Hour = ?",
				new String[] { Long.toString(busStopId), Integer.toString(hour) }, null, null, null);
		if (cursor.moveToFirst()) {
			return cursor.getLong(0);
		} else {
			mInsertRowHelper.prepareForInsert();
			mInsertRowHelper.bind(mRowHourIndex, hour);
			mInsertRowHelper.bind(mRowBusStopIndex, busStopId);
			return mInsertRowHelper.execute();
		}
	}

	private void addCell(long rowId, byte minute, byte note, short sort) {
		mInsertCellHelper.prepareForInsert();
		mInsertCellHelper.bind(mCellMinuteIndex, minute);
		mInsertCellHelper.bind(mCellSortIndex, sort);
		mInsertCellHelper.bind(mCellNoteIndex, note);
		mInsertCellHelper.bind(mCellRowIndex, rowId);
		mInsertCellHelper.execute();
		/*
		 * ContentValues values = new ContentValues(); values.put("Minute",
		 * minute); values.put("Sort", sort); values.put("Note", note);
		 * values.put("Row", rowId); mDatabase.insert("Cell", null, values);
		 */
	}

}
