package org.kixlabs.tk.browseservice.so;

import java.io.Serializable;
import java.util.Calendar;

public class Line implements Serializable {

	private static final long serialVersionUID = -8804240128673859564L;

	private long id;

	private String name;

	private Calendar validFrom;

	private Calendar validTo;

	public Line(long id, String name, Calendar validFrom, Calendar validTo) {
		super();
		this.id = id;
		this.name = name;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public Calendar getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Calendar validFrom) {
		this.validFrom = validFrom;
	}

	public Calendar getValidTo() {
		return validTo;
	}

	public void setValidTo(Calendar validTo) {
		this.validTo = validTo;
	}

	public boolean isValid() {
		boolean valid = true;
		Calendar now = Calendar.getInstance();
		if (validFrom != null && (validFrom.before(now)))
			valid = false;
		if (validTo != null && (validTo.before(now)))
			valid = false;
		return valid;
	}

	public boolean hasValidFrom() {
		return validFrom != null && validFrom.getTimeInMillis() != 0;
	}

	public boolean hasValidTo() {
		return validTo != null && validTo.getTimeInMillis() != 0;
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
