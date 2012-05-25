package org.kixlabs.tk.downloaderservice;

import java.util.List;

import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;

public interface Downloader {

	public void downloadLineData(DownloaderLineSO line, FetcherNotificator notificator) throws Exception;
	
	public List<DownloaderCitySO> downloadCities() throws Exception;
	
	public List<DownloaderLineSortSO> downloadLinesSorts(DownloaderCitySO city) throws Exception;
	
	public List<DownloaderLineSO> downloadLines(DownloaderCitySO city, DownloaderLineSortSO lineSort) throws Exception;
	
	public void setParseNotes(boolean parseNotes);
	
	public void interruptDownloading();
	
}
