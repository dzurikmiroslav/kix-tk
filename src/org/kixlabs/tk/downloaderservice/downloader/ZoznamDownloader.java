package org.kixlabs.tk.downloaderservice.downloader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kixlabs.tk.DayFlags;
import org.kixlabs.tk.downloaderservice.DownloaderNotificator;
import org.kixlabs.tk.downloaderservice.so.DownloaderBusStop;
import org.kixlabs.tk.downloaderservice.so.DownloaderLine;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSort;
import org.kixlabs.tk.downloaderservice.so.DownloaderRow;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCell;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCity;

import android.util.Log;

public class ZoznamDownloader implements Downloader {

	private String url = "http://" + "imhd" + '.' + "zoznam" + '.' + "sk/";

	private static final String TAG = "ZoznamDownloader";

	private Pattern mWorkDaysSchoolYearPattern = Pattern.compile(".*školský\\s+rok.*", Pattern.CASE_INSENSITIVE);

	private Pattern mWorkDaysSchoolHolidayPattern = Pattern.compile(".*školské\\s+prázdniny.*", Pattern.CASE_INSENSITIVE);

	private Pattern mWorkDays = Pattern.compile("(.*pondelok.*piatok.*)|(.*Pracovné\\s+dni.*)", Pattern.CASE_INSENSITIVE);

	private Pattern mWeekendsFeriaeDays = Pattern.compile("(.*sobota.*nedeľa.*)|(.*Voľné\\s+dni.*)", Pattern.CASE_INSENSITIVE);

	private Pattern mMinuteCellPattern = Pattern.compile("\\s*(\\d\\d)([a-zA-Z])?\\s*");

	private Pattern mLineValidFromPattern = Pattern.compile(".*od\\s+([\\d\\.]+).*");

	private Pattern mLineValidToPattern = Pattern.compile(".*do\\s+([\\d\\.]+).*");

	private DateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy");

	private Document jsoupConnect(String url) throws IOException {
		try {
			return Jsoup.connect(url).timeout(10 * 10000).get();
		} catch (java.net.SocketTimeoutException e1) {
			try {
				return Jsoup.connect(url).timeout(20 * 10000).get();
			} catch (java.net.SocketTimeoutException e2) {
				return Jsoup.connect(url).timeout(30 * 10000).get();
			}
		}
	}

	private void downloadBusStopData(DownloaderBusStop busStop, String url, boolean parseNotes) throws IOException,
			InterruptedException {
		Log.i(TAG, "download busstop data " + url);

		Thread.sleep(0); // vyhodi InterruptedException ak bolo vlakno ukoncene
		Document document = jsoupConnect(url);
		List<Short> sorts = new LinkedList<Short>();
		Elements days = document.select(".nazov_dna");

		for (Element d : days) {
			String daysSort = d.text().trim();
			if (mWorkDaysSchoolYearPattern.matcher(daysSort).matches()) {
				sorts.add(DayFlags.WORK_DAYS_SCHOOL_YEAR);
			} else if (mWorkDaysSchoolHolidayPattern.matcher(daysSort).matches()) {
				sorts.add(DayFlags.WORK_DAYS_HOLIDAY);
			} else if (mWorkDays.matcher(daysSort).matches()) {
				sorts.add((short) (DayFlags.WORK_DAYS_HOLIDAY | DayFlags.WORK_DAYS_SCHOOL_YEAR));
			} else if (mWeekendsFeriaeDays.matcher(daysSort).matches()) {
				sorts.add(DayFlags.WEEKENDS_FERIAE_DAYS);
			} else {
				sorts.add((short) (DayFlags.WORK_DAYS_HOLIDAY | DayFlags.WORK_DAYS_SCHOOL_YEAR | DayFlags.WORK_DAYS_SCHOOL_YEAR));
			}
		}

		Elements tables = document.select(".cp_odchody_tabulka_max");
		int sortCounter = 0;
		for (Element t : tables) {
			Thread.sleep(0); // vyhodi InterruptedException ak bolo vlakno
								// ukoncene
			short sort = sorts.get(sortCounter++);
			Elements rows = t.select(".cp_odchody");
			for (Element r : rows) {
				byte hour = Byte.valueOf(r.select(".cp_hodina").get(0).text().trim());
				DownloaderRow row = null;
				for (DownloaderRow r2 : busStop.getRows()) {
					if (r2.getHour() == hour) {
						row = r2;
						break;
					}
				}
				if (row == null) {
					row = new DownloaderRow();
					row.setHour(hour);
					busStop.getRows().add(row);
				}
				Elements minutes = r.select("td:not(.cp_hodina, .cp_odchody_doplnenie)");
				for (Element m : minutes) {
					Thread.sleep(0); // vyhodi InterruptedException ak bolo
										// vlakno ukoncene
					Matcher matcher = mMinuteCellPattern.matcher(m.text());
					if (matcher.find()) {
						byte minute = Byte.valueOf(matcher.group(1));
						byte note = 0;
						if (matcher.group(2) != null)
							note = (byte) matcher.group(2).charAt(0);
						DownloaderTableCell cell = null;
						for (DownloaderTableCell c : row.getCells()) {
							if (c.getMinute() == minute && c.getNote() == note) {
								cell = c;
								break;
							}
						}
						if (cell == null) {
							cell = new DownloaderTableCell();
							cell.setMinute(minute);
							cell.setNote(note);
							cell.setSort(sort);
							row.getCells().add(cell);
						} else {
							cell.setSort((short) (cell.getSort() | sort));
						}
					}
				}
			}
		}
		if (parseNotes) {
			Element note = document.select(".poznamky td").get(0);
			busStop.setNote(note.html().trim());
		}
	}

	@Override
	public void downloadLineData(DownloaderLine line, DownloaderNotificator notificator, boolean parseNotes) throws IOException,
			InterruptedException {
		Log.i(TAG, "download line data " + line.getUrl());
		line.setBusStops(new HashSet<DownloaderBusStop>());
		Document document = jsoupConnect(line.getUrl());
		if (notificator != null)
			notificator.onStartDownloading(document.select(".tabulka a:not(.button)").size());
		String validationStr = document.select(".h0").get(0).text();

		Matcher matcher = mLineValidFromPattern.matcher(validationStr);
		Calendar calendar = Calendar.getInstance();
		if (matcher.matches()) {
			matcher.group(0);
			try {
				calendar.setTime(mDateFormat.parse(matcher.group(1)));
			} catch (ParseException e) {
				calendar.setTimeInMillis(0);
			}
		} else {
			calendar.setTimeInMillis(0);
		}
		line.setValidFrom(calendar);

		matcher = mLineValidToPattern.matcher(validationStr);
		calendar = Calendar.getInstance();
		if (matcher.matches()) {
			matcher.group(0);
			try {
				calendar.setTime(mDateFormat.parse(matcher.group(1)));
			} catch (ParseException e) {
				calendar.setTimeInMillis(0);
			}
		} else {
			calendar.setTimeInMillis(0);
		}
		line.setValidTo(calendar);

		Element table = document.select(".tabulka").get(0);
		String destination = table.getElementsByClass("theader1").get(0).text().replace('►', ' ').trim();
		Elements sources = table.select("a:not(.button)");
		int counter = 1;
		int progressCounter = 0;
		for (Element s : sources) {
			String source = s.text().trim();
			if (notificator != null)
				notificator.onDownloadBusStop(progressCounter++);
			DownloaderBusStop busStop = new DownloaderBusStop();
			busStop.setDestination(destination);
			busStop.setSource(source);
			busStop.setOrderNuber(counter++);
			downloadBusStopData(busStop, url + s.attr("href"), parseNotes);
			line.getBusStops().add(busStop);
		}
		// druhy smer
		if (document.select(".tabulka").size() > 1) {
			table = document.select(".tabulka").get(1);
			destination = table.getElementsByClass("theader1").get(0).text().replace('►', ' ').trim();
			sources = table.select("a:not(.button)");
			counter = 1;
			for (Element s : sources) {
				String source = s.text().trim();
				if (notificator != null)
					notificator.onDownloadBusStop(progressCounter++);
				DownloaderBusStop busStop = new DownloaderBusStop();
				busStop.setDestination(destination);
				busStop.setSource(source);
				busStop.setOrderNuber(counter++);
				downloadBusStopData(busStop, url + s.attr("href"), parseNotes);
				line.getBusStops().add(busStop);
			}
		}
	}

	@Override
	public List<DownloaderTableCity> downloadCities() throws IOException {
		List<DownloaderTableCity> citiesSo = new ArrayList<DownloaderTableCity>();
		Document document = jsoupConnect(this.url + "/transport/mhd.html");
		Elements cities = document.select("#mesto ul ul a");
		for (Element c : cities) {
			String name = c.text().trim();
			String url = this.url + c.attr("href");
			url = url.replace("mhd.html", "cestovne-poriadky.html");
			DownloaderTableCity city = new DownloaderTableCity();
			// city.setId(mDbHelper.getCityId(name));
			city.setName(name);
			city.setUrl(url);
			citiesSo.add(city);
		}
		return citiesSo;
	}

	@Override
	public List<DownloaderLineSort> downloadLinesSorts(DownloaderTableCity city) throws IOException {
		List<DownloaderLineSort> sortsSo = new ArrayList<DownloaderLineSort>();
		Document document = jsoupConnect(city.getUrl());
		Element table = document.select("table").get(0);
		Elements sorts = table.select("h2");
		for (Element s : sorts) {
			DownloaderLineSort lineSortSO = new DownloaderLineSort();
			lineSortSO.setName(s.text().trim());
			sortsSo.add(lineSortSO);
		}
		return sortsSo;
	}

	@Override
	public List<DownloaderLine> downloadLines(DownloaderTableCity city, DownloaderLineSort lineSort) throws IOException {
		List<DownloaderLine> lineInfos = new ArrayList<DownloaderLine>();
		Document document = jsoupConnect(city.getUrl());
		Element table = document.select("table").get(0);
		Elements rows = table.select("tr");

		for (Element r : rows) {
			if (lineSort.getName().equals(r.select("h2").text().trim())) {
				Elements lines = r.select("a");
				for (Element l : lines) {
					DownloaderLine line = new DownloaderLine();
					line.setName(l.text().trim());
					line.setUrl(url + l.attr("href"));
					line.setCity(city);
					line.setLineSort(lineSort);
					lineInfos.add(line);
				}
			}
		}
		return lineInfos;
	}

}
