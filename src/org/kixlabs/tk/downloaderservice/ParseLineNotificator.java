package org.kixlabs.tk.downloaderservice;

public interface ParseLineNotificator {

	public void onParseBusStop(String sourceName, String destinationName, int position, int count);

	public void onParseLine(String lineName);

}
