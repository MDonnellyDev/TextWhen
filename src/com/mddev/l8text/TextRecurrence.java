package com.mddev.l8text;

import java.util.Date;

public class TextRecurrence {
	
	public static Date getNextRecurrence(Date d, OutgoingText.RECURRENCE recurrence){
		switch (recurrence){
			case DAILY:
				return getNextDailyRecurrence(d);
			case WEEKLY:
				return getNextWeeklyRecurrence(d);
			case MONTHLY:
				return getNextMonthlyRecurrence(d);
			case YEARLY:
				return getNextYearlyRecurrence(d);
			case NONE:
			default:
				return null;
		}
	}
	
	private static Date getNextDailyRecurrence(Date d){
		
		return d;		
	}
	
	private static Date getNextWeeklyRecurrence(Date d){
		
		return d;		
	}
	
	private static Date getNextMonthlyRecurrence(Date d){
		
		return d;		
	}
	
	private static Date getNextYearlyRecurrence(Date d){
		
		return d;		
	}
	
	
}
