package org.kixlabs.tk.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

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

	private static final String TAG = "DatabaseHelper";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION_NUMBER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Creating the database");

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
			Log.e(TAG, "Cant import db from file " + e.toString());
			// openDatabase();
			throw e;
		}
		Log.i(TAG, "Succesfull import db from file \"" + fileName + '"');
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

}
