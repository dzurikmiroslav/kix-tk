package org.kixlabs.tk.service.so;

import java.io.Serializable;

public class SourceSO implements Serializable {

	private static final long serialVersionUID = -2227418032664819131L;

	private long id;

	private String name;

	public SourceSO(long id, String name) {
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
