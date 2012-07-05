package org.kixlabs.tk.downloaderservice.so;

import java.io.Serializable;

public class DownloaderTableCity implements Serializable {

	private static final long serialVersionUID = 1371289315079686178L;

	private long id = -1;

	private String name;

	private String url;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
