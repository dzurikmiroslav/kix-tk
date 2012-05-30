package org.kixlabs.tk.service.so;

import java.io.Serializable;

public class LineSortSO implements Serializable {

	private static final long serialVersionUID = 4438728101111689372L;

	private long id;

	private String name;

	public LineSortSO(long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

}
