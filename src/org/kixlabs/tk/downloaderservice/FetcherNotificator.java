package org.kixlabs.tk.downloaderservice;

public interface FetcherNotificator {

	public void onDownloadBusStop(String source, String destination, int position);
	
	public void onStartDownloading(int busStopsCount);
	
	public void onStororeBusStop(int position);
	
	public void onStartDatabaseStoring(int busStopsCount);
	
}
