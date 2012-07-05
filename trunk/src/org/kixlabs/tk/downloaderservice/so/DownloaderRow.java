package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DownloaderRow implements Serializable {

	private static final long serialVersionUID = -6556510244400522221L;

	private byte hour;

	private Set<DownloaderTableCell> cells = new HashSet<DownloaderTableCell>();

	public byte getHour() {
		return hour;
	}

	public void setHour(byte hour) {
		this.hour = hour;
	}

	public Set<DownloaderTableCell> getCells() {
		return cells;
	}

	public void setCells(Set<DownloaderTableCell> cells) {
		this.cells = cells;
	}

}
