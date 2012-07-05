package org.kixlabs.tk.downloaderservice;

public interface DownloaderNotificator {

	public void onDownloadBusStop(int position);

	public void onStartDownloading(int busStopsCount);

	public void onStororeBusStop(int position);

	public void onStartDatabaseStoring(int busStopsCount);

}
