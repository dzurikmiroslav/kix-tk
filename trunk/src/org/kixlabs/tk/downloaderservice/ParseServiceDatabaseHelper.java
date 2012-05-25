package org.kixlabs.tk.downloaderservice;

import java.util.List;

import org.kixlabs.tk.downloaderservice.so.DownloaderCitySO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSO;
import org.kixlabs.tk.downloaderservice.so.DownloaderLineSortSO;

public interface ParseServiceDatabaseHelper {
		
	public void getCitiesIdsIfExists(List<DownloaderCitySO> cities);
	
	public void getLinesSortsIdsIfExists(List<DownloaderLineSortSO> linesSorts);
	
	public void getLinesIdsIfExists(List<DownloaderLineSO> lines);
	
	public void createLine(DownloaderLineSO line, FetcherNotificator noficator);
	
	public void initDBForInserting();
}
