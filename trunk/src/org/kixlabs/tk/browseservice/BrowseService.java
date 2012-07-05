package org.kixlabs.tk.browseservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.kixlabs.tk.browseservice.so.City;
import org.kixlabs.tk.browseservice.so.Destination;
import org.kixlabs.tk.browseservice.so.Line;
import org.kixlabs.tk.browseservice.so.LineSort;
import org.kixlabs.tk.browseservice.so.Source;
import org.kixlabs.tk.browseservice.so.TableCell;
import org.kixlabs.tk.browseservice.so.TableRow;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BrowseService {

	private SQLiteDatabase mDatabase = null;

	public BrowseService(SQLiteOpenHelper helper) {
		mDatabase = helper.getReadableDatabase();
		// TODO kto potom uzavre mDatabase.close() ?
	}

	private Calendar milisecondsToCalendar(long milliseconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		return calendar;
	}

	public List<City> getCities() {
		List<City> list = new ArrayList<City>();
		Cursor cursor = mDatabase.query("City", new String[] { "Id", "Name" }, null, null, null, null, "Name");
		while (cursor.moveToNext())
			list.add(new City(cursor.getLong(0), cursor.getString(1)));
		return list;
	}

	public List<LineSort> getLinesSorts(City city) {
		List<LineSort> list = new ArrayList<LineSort>();
		if (city != null) {
			Cursor cursor = mDatabase.query(true, "LineSort JOIN City JOIN Line", new String[] { "LineSort.Id", "LineSort.Name" },
					"City.Id = ? AND City.Id = Line.City AND LineSort.Id = Line.LineSort",
					new String[] { Long.toString(city.getId()) }, null, null, "LineSort.Name", null);
			while (cursor.moveToNext())
				list.add(new LineSort(cursor.getLong(0), cursor.getString(1)));
		}
		return list;
	}

	public List<Line> getLines(City city, List<LineSort> linesSorts) {
		List<Line> list = new ArrayList<Line>();
		if (city != null) {
			for (LineSort ls : linesSorts) {
				Cursor cursor = mDatabase.query("Line", new String[] { "Id", "Name", "ValidFrom", "ValidTo" },
						"City = ? AND LineSort = ?", new String[] { Long.toString(city.getId()), Long.toString(ls.getId()) }, null,
						null, "ABS(Name), Name");
				while (cursor.moveToNext())
					list.add(new Line(cursor.getLong(0), cursor.getString(1), milisecondsToCalendar(cursor.getLong(2)),
							milisecondsToCalendar(cursor.getLong(3))));
			}
		}
		return list;
	}

	public List<Destination> getDestinations(Line line) {
		ArrayList<Destination> list = new ArrayList<Destination>();
		Cursor cursor = mDatabase.query(true, "BusStop JOIN BusStopName", new String[] { "DstBusStopName AS Id", "Name" },
				"Line = ? AND BusStopName.Id = DstBusStopName", new String[] { String.valueOf(line.getId()) }, null, null, "Name",
				null);
		while (cursor.moveToNext())
			list.add(new Destination(cursor.getLong(0), cursor.getString(1), line.getId()));
		return list;
	}

	public List<Source> getSources(Destination destination) {
		ArrayList<Source> list = new ArrayList<Source>();
		Cursor cursor = mDatabase.query(false, "BusStop JOIN BusStopName", new String[] { "BusStop.Id AS Id", "BusStopName.Name" },
				"Line = ? AND BusStopName.Id = SrcBusStopName AND DstBusStopName = ?",
				new String[] { String.valueOf(destination.getLineId()), String.valueOf(destination.getId()) }, null, null,
				"OrderNumber", null);
		while (cursor.moveToNext())
			list.add(new Source(cursor.getLong(0), cursor.getString(1)));
		return list;
	}

	public List<TableRow> getTable(Source source, int sortFlag) {
		ArrayList<TableRow> list = new ArrayList<TableRow>();
		Map<Integer, ArrayList<TableCell>> map = new TreeMap<Integer, ArrayList<TableCell>>();
		Cursor cursor = mDatabase.query(false, "Row JOIN Cell", new String[] { "Hour, Minute, Note, Sort" },
				"Cell.Row = Row.Id AND Row.BusStop = ? AND Cell.Sort & ? > 0",
				new String[] { String.valueOf(source.getId()), String.valueOf(sortFlag) }, null, null, "Hour, Minute", null);
		while (cursor.moveToNext()) {
			int hour = cursor.getInt(0);
			if (map.get(hour) == null)
				map.put(hour, new ArrayList<TableCell>());
			map.get(hour).add(new TableCell(cursor.getInt(1), (char) cursor.getInt(2)));
		}

		Iterator<Entry<Integer, ArrayList<TableCell>>> it = map.entrySet().iterator();
		if (it.hasNext()) {
			Entry<Integer, ArrayList<TableCell>> ent = it.next();
			list.add(new TableRow(ent.getKey(), ent.getValue()));
		}
		Iterator<Entry<Integer, ArrayList<TableCell>>> prevIt = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ArrayList<TableCell>> prevEnt = prevIt.next();
			Entry<Integer, ArrayList<TableCell>> ent = it.next();
			for (int i = prevEnt.getKey() + 1; i < ent.getKey(); ++i)
				list.add(new TableRow(i));
			list.add(new TableRow(ent.getKey(), ent.getValue()));
		}

		return list;
	}

	public Calendar getNearest(Source source, int sortFlag) {
		Calendar calendar = Calendar.getInstance();
		Cursor cursor = mDatabase.query(false, "Row JOIN Cell", new String[] { "Hour", "Minute" },
				"Cell.Row = Row.Id AND Row.BusStop = ? AND Sort & ? AND ((Hour = ? AND Minute > ?) OR (Hour > ?))", new String[] {
						String.valueOf(source.getId()), String.valueOf(sortFlag), String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
						String.valueOf(calendar.get(Calendar.MINUTE)), String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) }, null,
				null, "Hour, Minute", "1");

		if (cursor.moveToFirst()) {
			calendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(0));
			calendar.set(Calendar.MINUTE, cursor.getInt(1));
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar = null;
		}
		return calendar;
	}

	public String getNote(Source source) {
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

}
