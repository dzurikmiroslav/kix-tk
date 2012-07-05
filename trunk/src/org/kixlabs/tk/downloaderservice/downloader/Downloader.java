package org.kixlabs.tk.downloaderservice.downloader;

import java.io.IOException;
import java.util.List;

import org.kixlabs.tk.downloaderservice.DownloaderNotificator;
import org.kixlabs.tk.downloaderservice.so.DownloaderLine;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSort;
import org.kixlabs.tk.downloaderservice.so.DownloaderTableCity;

public interface Downloader {

	public void downloadLineData(DownloaderLine line, DownloaderNotificator notificator, boolean parseNotes) throws IOException,
			InterruptedException;

	public List<DownloaderTableCity> downloadCities() throws IOException;

	public List<DownloaderLineSort> downloadLinesSorts(DownloaderTableCity city) throws IOException;

	public List<DownloaderLine> downloadLines(DownloaderTableCity city, DownloaderLineSort lineSort) throws IOException;

}
