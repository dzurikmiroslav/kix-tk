package org.kixlabs.tk.service.so;

import java.io.Serializable;
import java.util.ArrayList;


public class TableRowSO implements Serializable {

	private static final long serialVersionUID = -7681668243313843259L;

	private int hour;
	
	private ArrayList<TableCellSO> cells;
	
	public TableRowSO(int hour, ArrayList<TableCellSO> cells) {
		super();
		this.hour = hour;
		this.cells = cells;
	}

	public TableRowSO(int hour) {
		super();
		this.hour = hour;
	}
	
	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public ArrayList<TableCellSO> getCells() {
		return cells;
	}
	
}
