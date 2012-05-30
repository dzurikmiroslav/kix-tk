package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;

public class DownloaderLineSortSO implements Serializable {

	private static final long serialVersionUID = -6761234286335716770L;

	private long id = -1;

	private String name;

	@Override
	public String toString() {
		return name;
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

}
