package org.kixlabs.tk.downloaderservice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kixlabs.tk.DayFlags;
import org.kixlabs.tk.downloaderservice.exception.InterruptDownloadingException;
import org.kixlabs.tk.downloaderservice.so.DownloaderBusStopSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderCellSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderRowSO;

import android.util.Log;

public class ZoznamDownloader implements Downloader {

	private boolean parseNotes = true;

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

	private Semaphore cancelSemaphore = new Semaphore(0);

	private Document jsoupConnectWithInterrupt(String url) throws InterruptDownloadingException, IOException {
		if (cancelSemaphore.tryAcquire()) {
			Log.i(TAG, "interrup donwloading");
			throw new InterruptDownloadingException();
		}
		return jsoupConnect(url);
	}

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

	private void downloadBusStopData(DownloaderBusStopSO busStop, String url) throws IOException, InterruptDownloadingException {
		Log.i(TAG, "download busstop data " + url);

		Document document = jsoupConnectWithInterrupt(url);
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
			short sort = sorts.get(sortCounter++);
			Elements rows = t.select(".cp_odchody");
			for (Element r : rows) {
				byte hour = Byte.valueOf(r.select(".cp_hodina").get(0).text().trim());
				DownloaderRowSO row = null;
				for (DownloaderRowSO r2 : busStop.getRows()) {
					if (r2.getHour() == hour) {
						row = r2;
						break;
					}
				}
				if (row == null) {
					row = new DownloaderRowSO();
					row.setHour(hour);
					busStop.getRows().add(row);
				}
				Elements minutes = r.select("td:not(.cp_hodina, .cp_odchody_doplnenie)");
				for (Element m : minutes) {
					Matcher matcher = mMinuteCellPattern.matcher(m.text());
					if (matcher.find()) {
						byte minute = Byte.valueOf(matcher.group(1));
						byte note = 0;
						if (matcher.group(2) != null)
							note = (byte) matcher.group(2).charAt(0);
						DownloaderCellSO cell = null;
						for (DownloaderCellSO c : row.getCells()) {
							if (c.getMinute() == minute && c.getNote() == note) {
								cell = c;
								break;
							}
						}
						if (cell == null) {
							cell = new DownloaderCellSO();
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
	public void downloadLineData(DownloaderLineSO line, FetcherNotificator notificator) throws IOException,
			InterruptDownloadingException {
		Log.i(TAG, "download line data " + line.getUrl());
		Document document = jsoupConnectWithInterrupt(line.getUrl());
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
				notificator.onDownloadBusStop(source, destination, progressCounter++);
			DownloaderBusStopSO busStop = new DownloaderBusStopSO();
			busStop.setDestination(destination);
			busStop.setSource(source);
			busStop.setOrderNuber(counter++);
			downloadBusStopData(busStop, url + s.attr("href"));
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
					notificator.onDownloadBusStop(source, destination, progressCounter++);
				DownloaderBusStopSO busStop = new DownloaderBusStopSO();
				busStop.setDestination(destination);
				busStop.setSource(source);
				busStop.setOrderNuber(counter++);
				downloadBusStopData(busStop, url + s.attr("href"));
				line.getBusStops().add(busStop);
			}
		}
	}

	@Override
	public List<DownloaderCitySO> downloadCities() throws IOException {
		List<DownloaderCitySO> citiesSo = new ArrayList<DownloaderCitySO>();
		Document document = jsoupConnect(this.url + "/transport/mhd.html");
		Elements cities = document.select("#mesto ul ul a");
		for (Element c : cities) {
			String name = c.text().trim();
			String url = this.url + c.attr("href");
			url = url.replace("mhd.html", "cestovne-poriadky.html");
			DownloaderCitySO city = new DownloaderCitySO();
			// city.setId(mDbHelper.getCityId(name));
			city.setName(name);
			city.setUrl(url);
			citiesSo.add(city);
		}
		return citiesSo;
	}

	@Override
	public List<DownloaderLineSortSO> downloadLinesSorts(DownloaderCitySO city) throws IOException {
		List<DownloaderLineSortSO> sortsSo = new ArrayList<DownloaderLineSortSO>();
		Document document = jsoupConnect(city.getUrl());
		Element table = document.select("table").get(0);
		Elements sorts = table.select("h2");
		for (Element s : sorts) {
			DownloaderLineSortSO lineSortSO = new DownloaderLineSortSO();
			lineSortSO.setName(s.text().trim());
			sortsSo.add(lineSortSO);
		}
		return sortsSo;
	}

	@Override
	public List<DownloaderLineSO> downloadLines(DownloaderCitySO city, DownloaderLineSortSO lineSort) throws IOException {
		List<DownloaderLineSO> lineInfos = new ArrayList<DownloaderLineSO>();
		Document document = jsoupConnect(city.getUrl());
		Element table = document.select("table").get(0);
		Elements rows = table.select("tr");

		for (Element r : rows) {
			if (lineSort.getName().equals(r.select("h2").text().trim())) {
				Elements lines = r.select("a");
				for (Element l : lines) {
					DownloaderLineSO line = new DownloaderLineSO();
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

	@Override
	public void setParseNotes(boolean parseNotes) {
		this.parseNotes = parseNotes;
	}

	@Override
	public void interruptDownloading() {
		cancelSemaphore.release();
	}

}
