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

		/**
		 * Get a List of available Recurrence types.
		 * @return
		 */
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

	private String					recipient;
	private String					messageContent;
	private String					subject;
	private Calendar				scheduledDate;
	private Calendar				modifiedDate;
	private long						gmtOffset;
	private RECURRENCE			recurrence;
	long										key;
	private DELIVERY_STATUS	status;
	private static int			sortAscending	= 1;

	public OutgoingText(String recipient, String subject, String messageContent,
			Calendar scheduledDate, long gmtOffset) {
		this(recipient, subject, messageContent, scheduledDate, Calendar
				.getInstance(), gmtOffset);
	}

	public OutgoingText(String recipient, String subject, String messageContent,
			Calendar scheduledDate, Calendar modifiedDate, long gmtOffset) {
		this.setRecipient(recipient);
		this.setSubject(subject);
		this.setMessageContent(messageContent);
		this.setScheduledDate(scheduledDate);
		this.setRecurrence(RECURRENCE.NONE);
		if (null == modifiedDate) {
			modifiedDate = Calendar.getInstance();
		} else {
			this.setModifiedDate(modifiedDate);
		}
		this.setGmtOffset(gmtOffset);
	}

	/**
	 * Update various parameters of an OutgoingText.  The object is not persisted to the db.
	 * @param recipient
	 * @param subject
	 * @param messageContent
	 * @param scheduledDate
	 * @param gmtOffset
	 * @return The updated object.
	 */
	public OutgoingText updateText(String recipient, String subject,
			String messageContent, Calendar scheduledDate, Long gmtOffset) {

		if (recipient != null)
			this.setRecipient(recipient);
		if (subject != null)
			this.setSubject(subject);
		if (messageContent != null)
			this.setMessageContent(messageContent);
		if (scheduledDate != null)
			this.setScheduledDate(scheduledDate);
		this.setRecurrence(RECURRENCE.NONE);
		this.setModifiedDate(Calendar.getInstance());
		this.setGmtOffset(gmtOffset);
		this.key = -1;
		return this;
	}

	public int hashCode() {
		return (super.hashCode() + this.getScheduledDate().hashCode() + this
				.getRecipient().hashCode());
	}

	/**
	 * Text representation of OutgoingText object.
	 * Uses Scheduled Date, Recipient, and MessageContent (up to 20 characters)
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getScheduledDate().getTime()).append(":\n");
		sb.append(this.getRecipient()).append('>');
		if (getMessageContent().length() > 19) {
			sb.append(getMessageContent().substring(0, 20));
		} else {
			sb.append(getMessageContent());
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
		if (this.getScheduledDate() != null && otherText.getScheduledDate() != null) {
			if (this.getScheduledDate().before(otherText.getScheduledDate()))
				return (-1 * OutgoingText.getSortAscending());
			else if (this.getScheduledDate().after(otherText.getScheduledDate()))
				return (1 * OutgoingText.getSortAscending());
		}
		if (this.getModifiedDate() != null && otherText.getModifiedDate() != null) {
			if (this.getModifiedDate().before(otherText.getModifiedDate()))
				return (-1 * OutgoingText.getSortAscending());
			else if (this.getModifiedDate().before(otherText.getModifiedDate()))
				return (1 * OutgoingText.getSortAscending());
		}
		return 0;
	}
	
	/**
	 * Convert the OutgoingText object into an Intent
	 * 
	 * @param context
	 * @param setAlarm
	 *          <b>true</b> Sets the Alarm. <b>false</b> Cancels the Alarm.
	 * @param updateText
	 *          Updates an existing OutgoingText
	 * @return
	 */
	public Intent toIntent(Context context, boolean setAlarm, boolean updateText) {
		return this.toIntent(context, setAlarm, updateText, AlarmService.class);
	}

	/**
	 * Convert the OutgoingText object into an Intent
	 * 
	 * @param context
	 * @param setAlarm
	 *          <i>true</i> Sets the Alarm. <i>false</i> Cancels the Alarm.
	 * @param updateText
	 *          Updates an existing OutgoingText
	 * @param cls
	 *          Class to receive the Intent.
	 * @return
	 */
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

	/**
	 * Generates an OutgoingText object based on an Intent.
	 * @param intent
	 * @return
	 */
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

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Calendar getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Calendar scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Long getScheduledDateAsLong() {
		return this.scheduledDate.getTimeInMillis();
	}

	public Calendar getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Calendar modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Long getModifiedDateAsLong() {
		return this.modifiedDate.getTimeInMillis();
	}

	public long getGmtOffset() {
		return gmtOffset;
	}

	public void setGmtOffset(long gmtOffset) {
		this.gmtOffset = gmtOffset;
	}

	public RECURRENCE getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(RECURRENCE recurrence) {
		this.recurrence = recurrence;
	}

	public DELIVERY_STATUS getStatus() {
		return status;
	}

	public void setStatus(DELIVERY_STATUS status) {
		this.status = status;
	}

	public static int getSortAscending() {
		return sortAscending;
	}

	public static void setSortAscending(int sortAscending) {
		OutgoingText.sortAscending = sortAscending;
	}

}
