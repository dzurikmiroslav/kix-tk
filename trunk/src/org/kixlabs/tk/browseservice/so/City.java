package org.kixlabs.tk.browseservice.so;

import java.io.Serializable;

public class City implements Serializable {

	private static final long serialVersionUID = 4723642524372511160L;

	private long id;

	private String name;

	public City(long id, String name) {
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
