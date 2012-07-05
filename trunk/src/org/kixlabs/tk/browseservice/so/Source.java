package org.kixlabs.tk.browseservice.so;

import java.io.Serializable;

public class Source implements Serializable {

	private static final long serialVersionUID = -2227418032664819131L;

	private long id;

	private String name;

	public Source(long id, String name) {
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
