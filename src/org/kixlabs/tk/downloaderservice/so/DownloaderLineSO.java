package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class DownloaderLineSO implements Serializable {

	private static final long serialVersionUID = -4385845056863719184L;

	private long id = -1;

	private String name;

	private String url;

	private Calendar validFrom;

	private Calendar validTo;

	private DownloaderCitySO city;

	private DownloaderLineSortSO lineSort;

	private Set<DownloaderBusStopSO> busStops = new HashSet<DownloaderBusStopSO>();

	@Override
	public String toString() {
		return name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public DownloaderCitySO getCity() {
		return city;
	}

	public void setCity(DownloaderCitySO city) {
		this.city = city;
	}

	public DownloaderLineSortSO getLineSort() {
		return lineSort;
	}

	public void setLineSort(DownloaderLineSortSO lineSort) {
		this.lineSort = lineSort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Calendar getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Calendar validFrom) {
		this.validFrom = validFrom;
	}

	public Calendar getValidTo() {
		return validTo;
	}

	public void setValidTo(Calendar validTo) {
		this.validTo = validTo;
	}

	public Set<DownloaderBusStopSO> getBusStops() {
		return busStops;
	}

	public void setBusStops(Set<DownloaderBusStopSO> busStops) {
		this.busStops = busStops;
	}

}
