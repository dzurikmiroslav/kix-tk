package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Set;

public class DownloaderLine implements Serializable {

	private static final long serialVersionUID = 3690288630587702029L;

	private long id = -1;

	private String name;

	private String url;

	private Calendar validFrom;

	private Calendar validTo;

	private DownloaderTableCity city;

	private DownloaderLineSort lineSort;

	private Set<DownloaderBusStop> busStops;

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

	public DownloaderTableCity getCity() {
		return city;
	}

	public void setCity(DownloaderTableCity city) {
		this.city = city;
	}

	public DownloaderLineSort getLineSort() {
		return lineSort;
	}

	public void setLineSort(DownloaderLineSort lineSort) {
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

	public Set<DownloaderBusStop> getBusStops() {
		return busStops;
	}

	public void setBusStops(Set<DownloaderBusStop> busStops) {
		this.busStops = busStops;
	}

}
