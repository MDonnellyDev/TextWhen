package com.mddev.l8text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;

public class OutgoingText implements Comparable<OutgoingText> {
	static enum RECURRENCE {
		NONE, DAILY, WEEKDAY, WEEKEND, WEEKLY, MONTHLY, YEARLY;

		static RECURRENCE fromInt(int recurrence) {

			switch (recurrence) {
				case 0:
					return NONE;
				case 1:
					return DAILY;
				case 2:
					return WEEKDAY;
				case 3:
					return WEEKEND;
				case 4:
					return WEEKLY;
				case 5:
					return MONTHLY;
				case 6:
					return YEARLY;
				default:
					return NONE;
			}
		}

		public static List<OutgoingText.RECURRENCE> recurrenceTypes() {
			List<OutgoingText.RECURRENCE> recurrenceList = new ArrayList<OutgoingText.RECURRENCE>();
			recurrenceList.add(NONE);
			recurrenceList.add(DAILY);
			// WEEKDAY and WEEKEND have no associated logic yet
			// recurrenceList.add(WEEKDAY);
			// recurrenceList.add(WEEKEND);
			recurrenceList.add(WEEKLY);
			recurrenceList.add(MONTHLY);
			recurrenceList.add(YEARLY);
			return recurrenceList;
		}
	};

	static enum DELIVERY_STATUS {
		PENDING, SENT, DELIVERED
	};

	String					recipient;
	String					messageContent;
	String					subject;
	Calendar				scheduledDate;
	Calendar				modifiedDate;
	long						gmtOffset;
	RECURRENCE			recurrence;
	long						key;
	DELIVERY_STATUS	status;
	static int			sortAscending	= 1;

	public OutgoingText(String recipient, String subject, String messageContent,
			Calendar scheduledDate, long gmtOffset) {
		this(recipient, subject, messageContent, scheduledDate, Calendar
				.getInstance(), gmtOffset);
	}

	public OutgoingText(String recipient, String subject, String messageContent,
			Calendar scheduledDate, Calendar modifiedDate, long gmtOffset) {
		this.recipient = recipient;
		this.subject = subject;
		this.messageContent = messageContent;
		this.scheduledDate = scheduledDate;
		this.recurrence = RECURRENCE.NONE;
		if (null == modifiedDate) {
			modifiedDate = Calendar.getInstance();
		} else {
			this.modifiedDate = modifiedDate;
		}
		this.gmtOffset = gmtOffset;
	}

	public OutgoingText updateText(String recipient, String subject,
			String messageContent, Calendar scheduledDate, long gmtOffset) {

		this.recipient = recipient;
		this.subject = subject;
		this.messageContent = messageContent;
		this.scheduledDate = scheduledDate;
		this.recurrence = RECURRENCE.NONE;
		this.modifiedDate = Calendar.getInstance();
		this.gmtOffset = gmtOffset;
		this.key = -1;
		return this;
	}

	public String getRecipient() {
		return this.recipient;
	}

	public String getSubject() {
		return this.subject;
	}

	public String getMessageContent() {
		return this.messageContent;
	}

	public Calendar getScheduledDate() {
		return this.scheduledDate;
	}

	public Long getScheduledDateAsLong() {
		return this.scheduledDate.getTimeInMillis();
	}

	public Calendar getModifiedDate() {
		return this.modifiedDate;
	}

	public Long getModifiedDateAsLong() {
		return this.modifiedDate.getTimeInMillis();
	}

	public long getGmtOffset() {
		return this.gmtOffset;
	}

	public RECURRENCE getRecurrence() {
		return this.recurrence;
	}

	//
	// public void setGmtOffset(int gmtOffset) {
	// this.gmtOffset = gmtOffset;
	// }

	public int hashCode() {
		return (super.hashCode() + this.scheduledDate.hashCode() + this.recipient
				.hashCode());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.scheduledDate.getTime()).append(":\n");
		sb.append(this.recipient).append('>');
		if (messageContent.length() > 19) {
			sb.append(messageContent.substring(0, 20));
		} else {
			sb.append(messageContent);
		}
		return sb.toString();
	}

	public long getKey() {
		return this.key;
	}

	public void setKey(long key) {
		this.key = key;
		return;
	}

	public int compareTo(OutgoingText other) {
		OutgoingText otherText = other;
		// if (this.toString().charAt(0) < otherText.toString().charAt(0)) {
		// return (-1 * OutgoingText.sortAscending);
		// } else
		// return (1 * OutgoingText.sortAscending);
		if (this.scheduledDate != null && otherText.scheduledDate != null) {
			if (this.scheduledDate.before(otherText.scheduledDate))
				return (-1 * OutgoingText.sortAscending);
			else if (this.scheduledDate.after(otherText.scheduledDate))
				return (1 * OutgoingText.sortAscending);
		}
		if (this.modifiedDate != null && otherText.modifiedDate != null) {
			if (this.modifiedDate.before(otherText.modifiedDate))
				return (-1 * OutgoingText.sortAscending);
			else if (this.modifiedDate.before(otherText.modifiedDate))
				return (1 * OutgoingText.sortAscending);
		}
		return 0;
	}

	// setAlarm : set or cancel alarm
	// updateText : update already existing text
	public Intent toIntent(Context context, boolean setAlarm, boolean updateText,
			Class<?> cls) {
		if (cls == null) {
			cls = AlarmService.class;
		}
		Intent intent = new Intent(context, cls);
		intent.putExtra("recipient", this.getRecipient());
		intent.putExtra("subject", this.getSubject());
		intent.putExtra("body", this.getMessageContent());
		intent.putExtra("date", this.getScheduledDateAsLong());
		intent.putExtra("modified", this.getModifiedDateAsLong());
		intent.putExtra("key", this.getKey());
		intent.putExtra("gmtOffset", this.getGmtOffset());
		intent.putExtra("recurrence", this.getRecurrence());

		intent.putExtra("setAlarm", setAlarm);
		intent.putExtra("update", updateText);
		return intent;
	}

	public static OutgoingText fromIntent(Intent intent) {
		String recipient = intent.getStringExtra("recipient");
		String subject = intent.getStringExtra("subject");
		String body = intent.getStringExtra("body");
		long date = intent.getLongExtra("date", 0);
		long modified = intent.getLongExtra("modified", 0);
		long gmtOffset = intent.getLongExtra("gmtOffset", 0);
		RECURRENCE recurrence = RECURRENCE.fromInt(intent.getIntExtra("recurrence",
				0));
		long key = intent.getLongExtra("key", -1);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		OutgoingText text = new OutgoingText(recipient, subject, body, cal,
				gmtOffset);
		text.setKey(key);
		return text;
	}

}
