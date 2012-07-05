package org.kixlabs.tk.browseservice.so;

import java.io.Serializable;
import java.util.ArrayList;

public class TableRow implements Serializable {

	private static final long serialVersionUID = -7681668243313843259L;

	private int hour;

	private ArrayList<TableCell> cells;

	public TableRow(int hour, ArrayList<TableCell> cells) {
		super();
		this.hour = hour;
		this.cells = cells;
	}

	public TableRow(int hour) {
		super();
		this.hour = hour;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public ArrayList<TableCell> getCells() {
		return cells;
	}

}
