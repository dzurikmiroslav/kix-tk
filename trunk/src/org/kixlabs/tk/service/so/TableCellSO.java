package org.kixlabs.tk.service.so;

import java.io.Serializable;

public class TableCellSO implements Serializable {

	private static final long serialVersionUID = 8039134624804922020L;

	private int minute;

	private char note;

	public TableCellSO(int minute, char note) {
		super();
		this.minute = minute;
		this.note = note;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public char getNote() {
		return note;
	}

	public void setNote(char note) {
		this.note = note;
	}

	// private int sort; ??

}
