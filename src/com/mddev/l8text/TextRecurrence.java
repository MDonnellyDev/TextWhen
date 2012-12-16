package com.mddev.l8text;

import java.util.Calendar;
import java.util.Date;

public class TextRecurrence {

	public static Calendar getNextRecurrence(Calendar c,
			OutgoingText.RECURRENCE recurrence) {
		switch (recurrence) {
			case DAILY:
				return getNextDailyRecurrence(c);
			case WEEKDAY:
				return getNextWeekdayRecurrence(c);
			case WEEKEND:
				return getNextWeekendRecurrence(c);
			case WEEKLY:
				return getNextWeeklyRecurrence(c);
			case MONTHLY:
				return getNextMonthlyRecurrence(c);
			case YEARLY:
				return getNextYearlyRecurrence(c);
			case NONE:
			default:
				return null;
		}
	}

	private static Calendar getNextDailyRecurrence(Calendar c) {
		c.add(Calendar.DAY_OF_MONTH, 1);
		return c;
	}

	private static Calendar getNextWeekdayRecurrence(Calendar c) {
		return null;
	}

	private static Calendar getNextWeekendRecurrence(Calendar c) {
		return null;
	}

	private static Calendar getNextWeeklyRecurrence(Calendar c) {
		c.add(Calendar.DATE, 7);
		return c;
	}

	private static Calendar getNextMonthlyRecurrence(Calendar c) {
		c.add(Calendar.MONTH, 1);
		return c;
	}

	private static Calendar getNextYearlyRecurrence(Calendar c) {
		c.add(Calendar.YEAR, 1);
		return c;
	}

}
