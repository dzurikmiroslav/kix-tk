package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DownloaderRowSO implements Serializable {

	private static final long serialVersionUID = -6556510244400522221L;

	private byte hour;
	
	private Set<DownloaderCellSO> cells = new HashSet<DownloaderCellSO>();

	public byte getHour() {
		return hour;
	}

	public void setHour(byte hour) {
		this.hour = hour;
	}

	public Set<DownloaderCellSO> getCells() {
		return cells;
	}

	public void setCells(Set<DownloaderCellSO> cells) {
		this.cells = cells;
	}
	
}
