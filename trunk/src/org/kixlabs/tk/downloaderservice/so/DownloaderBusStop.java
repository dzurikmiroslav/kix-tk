package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DownloaderBusStop implements Serializable {

	private static final long serialVersionUID = -2014653418607759831L;

	private int orderNuber;

	private String destination;

	private String source;

	private String note;

	private Set<DownloaderRow> rows = new HashSet<DownloaderRow>();

	public int getOrderNuber() {
		return orderNuber;
	}

	public void setOrderNuber(int orderNuber) {
		this.orderNuber = orderNuber;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Set<DownloaderRow> getRows() {
		return rows;
	}

	public void setRows(Set<DownloaderRow> rows) {
		this.rows = rows;
	}

}
