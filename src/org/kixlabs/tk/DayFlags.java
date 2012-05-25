package org.kixlabs.tk;

import java.util.Calendar;

public class DayFlags {

	public static final short WEEKENDS_FERIAE_DAYS = 1 << 0;

	public static final short WORK_DAYS_HOLIDAY = 1 << 1;

	public static final short WORK_DAYS_SCHOOL_YEAR = 1 << 2;

	public static final int getCurrentDayFlag() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int month = calendar.get(Calendar.MONTH);
		// sobota, nedela, sviatok
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			return DayFlags.WEEKENDS_FERIAE_DAYS;
		
		// TODO ostatne sviatky

		// prac. den skolske prazdniny
		if (month == Calendar.JULY || month == Calendar.AUGUST)
			return DayFlags.WORK_DAYS_HOLIDAY;

		// prac. den skolesky rok
		return DayFlags.WORK_DAYS_SCHOOL_YEAR;
	}
}
