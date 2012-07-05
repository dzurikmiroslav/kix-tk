package org.kixlabs.tk.downloaderservice;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.kixlabs.tk.downloaderservice.downloader.Downloader;
import org.kixlabs.tk.downloaderservice.downloader.ZoznamDownloader;
import org.kixlabs.tk.downloaderservice.so.DownloaderBusStop;
import org.kixlabs.tk.downloaderservice.so.DownloaderLine;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSort;
import org.kixlabs.tk.downloaderservice.so.DownloaderRow;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCell;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DownloaderService {

	private SQLiteDatabase mDatabase;

	// TODO vyber z viacerych downloaderov ? :-/ ?
	private Downloader downloader = new ZoznamDownloader();

	private InsertHelper mInsertBusStopHelper;
	private int mBusStopOrderNumberIndex, mBusStopLineIndex, mBusStopSrcBusStopNameIndex, mBusStopDstBusStopNameIndex;

	private InsertHelper mInsertCellHelper;
	private int mCellMinuteIndex, mCellSortIndex, mCellNoteIndex, mCellRowIndex;

	private InsertHelper mInsertBusStopNameHelper;
	private int mBusStopNameNameIndex;

	private InsertHelper mInsertRowHelper;
	private int mRowHourIndex, mRowBusStopIndex;

	public DownloaderService(SQLiteOpenHelper databaseHelper) {
		mDatabase = databaseHelper.getWritableDatabase();
		// TODO kto potom uzavre mDatabase.close() ?

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

	public void downloadLine(final DownloaderLine line, final DownloaderNotificator notificator, boolean parseNotes)
			throws IOException, InterruptedException {
		downloader.downloadLineData(line, notificator, parseNotes);
		if (notificator != null)
			notificator.onStartDatabaseStoring(line.getBusStops().size());
		createLine(line, notificator);
	}

	private long insertOrGetBusStopNameId(String name) {
		Cursor cursor = mDatabase.query("BusStopName", new String[] { "Id" }, "Name = ?", new String[] { name }, null, null, null);
		if (cursor.moveToFirst()) {
			return cursor.getLong(0);
		} else {
			mInsertBusStopNameHelper.prepareForInsert();
			mInsertBusStopNameHelper.bind(mBusStopNameNameIndex, name);
			return mInsertBusStopNameHelper.execute();
		}
	}

	private long insertBusStop(long lineId, long sourceId, long destinationId, int order) {
		mInsertBusStopHelper.prepareForInsert();
		mInsertBusStopHelper.bind(mBusStopOrderNumberIndex, order);
		mInsertBusStopHelper.bind(mBusStopLineIndex, lineId);
		mInsertBusStopHelper.bind(mBusStopSrcBusStopNameIndex, sourceId);
		mInsertBusStopHelper.bind(mBusStopDstBusStopNameIndex, destinationId);
		return mInsertBusStopHelper.execute();
	}

	private long insertLine(String name, long cityId, long sortId, long validFrom, long validTo) {
		ContentValues initialValues = new ContentValues();
		initialValues.put("Name", name);
		initialValues.put("City", cityId);
		initialValues.put("LineSort", sortId);
		initialValues.put("ValidFrom", validFrom);
		initialValues.put("ValidTo", validTo);
		return mDatabase.insert("Line", null, initialValues);
	}

	/**
	 * Linky ktore este nie su stiahnute
	 * 
	 * @param city
	 * @param lineSort
	 * @return
	 * @throws IOException
	 */
	public List<DownloaderLine> getUnfetchedLines(DownloaderTableCity city, DownloaderLineSort lineSort) throws IOException {
		List<DownloaderLine> lines = downloader.downloadLines(city, lineSort);
		for (DownloaderLine l : lines) {
			long id = -1;
			Cursor cursor = mDatabase.query("Line", new String[] { "Id" }, "Name = ? AND City = ?",
					new String[] { l.getName(), Long.toString(l.getCity().getId()) }, null, null, null);
			if (cursor.moveToFirst())
				id = cursor.getLong(0);
			l.setId(id);
		}
		Collection<DownloaderLine> exists = new HashSet<DownloaderLine>();
		for (DownloaderLine l : lines)
			if (l.getId() != -1)
				exists.add(l);
		lines.removeAll(exists);
		return lines;
	}

	public List<DownloaderLineSort> getLineSorts(DownloaderTableCity city) throws IOException {
		List<DownloaderLineSort> linesSorts = downloader.downloadLinesSorts(city);
		for (DownloaderLineSort ls : linesSorts) {
			long id = -1;
			Cursor cursor = mDatabase.query("LineSort", new String[] { "Id" }, "Name = ?", new String[] { ls.getName() }, null, null,
					null);
			if (cursor.moveToFirst())
				id = cursor.getLong(0);
			ls.setId(id);
		}
		return linesSorts;
	}

	public List<DownloaderTableCity> getCities() throws IOException {
		List<DownloaderTableCity> cities = downloader.downloadCities();
		for (DownloaderTableCity c : cities) {
			long id = -1;
			Cursor cursor = mDatabase.query("City", new String[] { "Id" }, "Name = ?", new String[] { c.getName() }, null, null, null);
			if (cursor.moveToFirst())
				id = cursor.getLong(0);
			c.setId(id);
		}
		return cities;
	}

	private long createCity(String name) {
		ContentValues values = new ContentValues();
		values.put("Name", name);
		return mDatabase.insert("City", null, values);
	}

	/**
	 * TODO rychlejsie by to ficalo v transakcii
	 * 
	 * TODO vyriesit prerusenie transakciou, pada na prekrocenie heap size :-(
	 * 
	 * @param line
	 * @param noficator
	 * @throws InterruptedException
	 */
	public void createLine(DownloaderLine line, DownloaderNotificator noficator) throws InterruptedException {
		// mDatabase.beginTransaction();
		// try {
		long newCityId = -1;
		long newLineSortId = -1;
		long lineId = -1;
		try {
			long cityId = line.getCity().getId();
			if (cityId == -1) {
				newCityId = cityId = createCity(line.getCity().getName());
				line.getCity().setId(cityId);
			}
			long lineSortId = line.getLineSort().getId();
			if (lineSortId == -1) {
				ContentValues values = new ContentValues();
				values.put("Name", line.getLineSort().getName());
				newLineSortId = lineSortId = mDatabase.insert("LineSort", null, values);
				line.getLineSort().setId(lineSortId);
			}
			Thread.sleep(0); // vyhodi InterruptedException ak bolo vlakno
								// ukoncene
			lineId = insertLine(line.getName(), cityId, lineSortId, line.getValidFrom().getTimeInMillis(), line.getValidTo()
					.getTimeInMillis());
			line.setId(lineId);

			int position = 0;
			for (DownloaderBusStop bs : line.getBusStops()) {
				Thread.sleep(0); // vyhodi InterruptedException ak bolo vlakno
									// ukoncene
				if (noficator != null)
					noficator.onStororeBusStop(position++);
				long destinationId = insertOrGetBusStopNameId(bs.getDestination());
				long sourceId = insertOrGetBusStopNameId(bs.getSource());
				long busStopId = insertBusStop(lineId, sourceId, destinationId, bs.getOrderNuber());

				for (DownloaderRow r : bs.getRows()) {
					long rowId = createOrGetHour(busStopId, r.getHour());
					for (DownloaderTableCell c : r.getCells()) {
						Thread.sleep(0); // vyhodi InterruptedException ak bolo
											// vlakno ukoncene
						addCell(rowId, c.getMinute(), c.getNote(), c.getSort());
					}
				}
				if (bs.getNote() != null) {
					ContentValues values = new ContentValues();
					values.put("Note", bs.getNote());
					mDatabase.update("BusStop", values, "Id = ?", new String[] { Long.toString(busStopId) });
				}
			}
		} catch (InterruptedException e) {
			if (newCityId != -1)
				mDatabase.delete("City", "Id = ?", new String[] { Long.toString(newCityId) });
			if (newLineSortId != -1)
				mDatabase.delete("LineSort", "Id = ?", new String[] { Long.toString(newLineSortId) });
			if (lineId != -1)
				mDatabase.delete("Line", "Id = ?", new String[] { Long.toString(lineId) });
			throw e;
		}
		// mDatabase.setTransactionSuccessful();
		// } finally {
		// mDatabase.endTransaction();
		// }
	}

	private long createOrGetHour(long busStopId, byte hour) {
		Cursor cursor = mDatabase.query("Row", new String[] { "Id" }, "BusStop = ? AND Hour = ?",
				new String[] { Long.toString(busStopId), Integer.toString(hour) }, null, null, null);
		if (cursor.moveToLast()) {
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
	}

}
