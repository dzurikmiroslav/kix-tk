package org.kixlabs.tk.downloaderservice;

import java.io.IOException;
import java.util.List;

import org.kixlabs.tk.downloaderservice.exception.InterruptDownloadingException;
import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;

public interface Downloader {

	public void downloadLineData(DownloaderLineSO line, FetcherNotificator notificator) throws IOException,
			InterruptDownloadingException;

	public List<DownloaderCitySO> downloadCities() throws IOException;

	public List<DownloaderLineSortSO> downloadLinesSorts(DownloaderCitySO city) throws IOException;

	public List<DownloaderLineSO> downloadLines(DownloaderCitySO city, DownloaderLineSortSO lineSort) throws IOException;

	public void setParseNotes(boolean parseNotes);

	public void interruptDownloading();

}
