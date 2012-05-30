package org.kixlabs.tk.service;

import java.util.Date;
import java.util.List;

import org.kixlabs.tk.service.so.CitySO;
import org.kixlabs.tk.service.so.DestinationSO;
import org.kixlabs.tk.service.so.LineSO;
import org.kixlabs.tk.service.so.LineSortSO;
import org.kixlabs.tk.service.so.SourceSO;
import org.kixlabs.tk.service.so.TableRowSO;

public interface ServiceDatabaseHelper {

	public List<CitySO> getCities();

	public List<LineSortSO> getLinesSorts(CitySO city);

	public List<LineSO> getLines(CitySO city, List<LineSortSO> linesSorts);

	public List<DestinationSO> getDestinations(LineSO line);

	public List<SourceSO> getSources(DestinationSO destination);

	public List<TableRowSO> getTable(SourceSO source, int sortFlag);

	public Date getNearest(SourceSO source, int sortFlag);

	public String getNote(SourceSO source);
}
