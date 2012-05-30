package org.kixlabs.tk.downloaderservice;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.kixlabs.tk.downloaderservice.exception.InterruptDownloadingException;
import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;

public class ParserService {

	private ParseServiceDatabaseHelper databaseHelper;

	private Downloader downloader = new ZoznamDownloader();

	public ParserService(ParseServiceDatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
		this.databaseHelper.initDBForInserting();
	}

	public void setParseNotes(boolean parseNotes) {
		downloader.setParseNotes(parseNotes);
	}

	public void interruptLinesParsing() {
		downloader.interruptDownloading();
	}

	public void downloadLine(final DownloaderLineSO line, final FetcherNotificator notificator) throws IOException {
		try {
			downloader.downloadLineData(line, notificator);
			if (notificator != null)
				notificator.onStartDatabaseStoring(line.getBusStops().size());
			databaseHelper.createLine(line, notificator);
		} catch (InterruptDownloadingException e) {
		}
	}

	/**
	 * Linky ktore sa daju stiahnut
	 * 
	 * @param city
	 * @param sort
	 * @return
	 * @throws Exception
	 */
	public List<DownloaderLineSO> getUnfetchedLines(DownloaderCitySO city, DownloaderLineSortSO lineSort) throws IOException {
		List<DownloaderLineSO> lines = downloader.downloadLines(city, lineSort);
		databaseHelper.getLinesIdsIfExists(lines);
		Collection<DownloaderLineSO> exists = new HashSet<DownloaderLineSO>();
		for (DownloaderLineSO l : lines)
			if (l.getId() != -1)
				exists.add(l);
		lines.removeAll(exists);
		return lines;
	}

	public List<DownloaderLineSortSO> getLineSorts(DownloaderCitySO city) throws IOException {
		List<DownloaderLineSortSO> linesSorts = downloader.downloadLinesSorts(city);
		databaseHelper.getLinesSortsIdsIfExists(linesSorts);
		return linesSorts;
	}

	public List<DownloaderCitySO> getCities() throws IOException {
		List<DownloaderCitySO> cities = downloader.downloadCities();
		databaseHelper.getCitiesIdsIfExists(cities);
		return cities;
	}

}
