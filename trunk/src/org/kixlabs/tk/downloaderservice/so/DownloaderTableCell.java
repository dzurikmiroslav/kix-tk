package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;

public class DownloaderTableCell implements Serializable {

	private static final long serialVersionUID = -5660473155484278113L;

	private byte minute;

	private byte note;

	private short sort;

	public byte getMinute() {
		return minute;
	}

	public void setMinute(byte minute) {
		this.minute = minute;
	}

	public byte getNote() {
		return note;
	}

	public void setNote(byte note) {
		this.note = note;
	}

	public short getSort() {
		return sort;
	}

	public void setSort(short sort) {
		this.sort = sort;
	}

}
