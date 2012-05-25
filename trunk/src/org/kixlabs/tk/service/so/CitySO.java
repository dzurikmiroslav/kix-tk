package org.kixlabs.tk.service.so;

import java.io.Serializable;

public class CitySO implements Serializable {

	private static final long serialVersionUID = 4723642524372511160L;

	private long id;

	private String name;

	public CitySO(long id, String name) {
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
