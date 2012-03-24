package com.odev.l8text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class TextDbAdapter {
	public static int						KEY_COUNT							= 0;
	private static final String	DB_NAME								= "TextData.db";
	private static final String	DB_TABLE							= "TextTable";
	private static final int		DB_VERSION						= 1;

	private static final String	KEY_ID								= "_id";
	private static final int		KEY_COLUMN						= 0;

	private static final String	KEY_RECIPIENT					= "Recipient";
	private static final int		RECIPIENT_COLUMN			= 1;

	private static final String	KEY_SUBJECT						= "Subject";
	private static final int		SUBJECT_COLUMN				= 2;

	private static final String	KEY_BODY							= "Body";
	private static final int		BODY_COLUMN						= 3;

	private static final String	KEY_SCHEDULED					= "ScheduleTime";
	private static final int		SCHEDULEDTIME_COLUMN	= 4;

	private static final String	KEY_MODIFIED					= "ModifiedTime";
	private static final int		MODIFIEDTIME_COLUMN		= 5;

	private static final String	KEY_GMT_OFFSET				= "GmtOffset";
	private static final int		GMT_COLUMN						= 6;

	private static final String	DB_CREATE							= (new StringBuilder()
																												.append("create table ")
																												.append(DB_TABLE)
																												.append(" (")
																												.append(KEY_ID)
																												.append(
																														" integer primary key autoincrement, ")
																												.append(KEY_RECIPIENT).append(
																														" text not null, ").append(
																														KEY_SUBJECT).append(" text, ")
																												.append(KEY_BODY).append(
																														" text not null, ").append(
																														KEY_SCHEDULED).append(" long, ")
																												.append(KEY_MODIFIED)
																												.append(" long, ").append(
																														KEY_GMT_OFFSET).append(" long);"))
																												.toString();

	private SQLiteDatabase			db;
	private final Context				context;
	private TextOpenHelper			dbHelper;

	public TextDbAdapter(Context context) {
		this.context = context;
		dbHelper = new TextOpenHelper(context, DB_NAME, null, DB_VERSION);
	}

	public TextDbAdapter open() throws SQLException {
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			db = dbHelper.getReadableDatabase();
		}
		return this;
	}

	public void close() {
		db.close();
	}

	public long insertEntry(OutgoingText text) {
		StringBuilder whereBuilder = new StringBuilder();
		whereBuilder.append(KEY_RECIPIENT).append(" = \"").append(text.getRecipient());
		whereBuilder.append("\" and ").append(KEY_BODY).append(" = \"").append(
				text.getMessageContent()).append("\"");
		Cursor cursor = db.query(DB_TABLE, new String[] { KEY_ID }, whereBuilder.toString(), null,
				null, null, null);
		if (cursor.moveToFirst()) {
			cursor.close();
			return -1;
		}
		ContentValues textValues = new ContentValues();
		textValues.put(KEY_RECIPIENT, text.getRecipient());
		textValues.put(KEY_SUBJECT, text.getSubject());
		textValues.put(KEY_BODY, text.getMessageContent());
		textValues.put(KEY_SCHEDULED, text.getScheduledDateAsLong());
		textValues.put(KEY_MODIFIED, text.getModifiedDateAsLong());
		textValues.put(KEY_GMT_OFFSET, text.getGmtOffset());
		cursor.close();
		return db.insert(DB_TABLE, null, textValues);
	}

	public long updateEntry(OutgoingText text, long rowId) {
		ContentValues textValues = new ContentValues();
		textValues.put(KEY_RECIPIENT, text.getRecipient());
		textValues.put(KEY_SUBJECT, text.getSubject());
		textValues.put(KEY_BODY, text.getMessageContent());
		textValues.put(KEY_SCHEDULED, text.getScheduledDateAsLong());
		textValues.put(KEY_MODIFIED, text.getModifiedDateAsLong());
		textValues.put(KEY_GMT_OFFSET, text.getGmtOffset());
		return db.update(DB_TABLE, textValues, KEY_ID + "=" + rowId, null);
	}

	public long getEntryRow(OutgoingText text) {
		String recipient = text.getRecipient();
		String sub = text.getSubject();
		String message = text.getMessageContent();
		Long date = text.getScheduledDateAsLong();
		String where = KEY_RECIPIENT + " = '" + text.getRecipient() + "' AND " + KEY_SUBJECT
				+ "= '" + text.getSubject() + "' OR " + KEY_SUBJECT + " IS NULL" + " AND " + KEY_BODY
				+ "= '" + text.getMessageContent() + "' AND " + KEY_SCHEDULED + "= '"
				+ text.getScheduledDateAsLong() + "'";
		Cursor cursor = db.query(true, DB_TABLE, new String[] { KEY_ID }, where, null, null, null,
				null, null);

		if (cursor.moveToFirst()) {
			Long row = Long.parseLong(cursor.getString(0));
			cursor.close();
			return row;
		} else {
			cursor.close();
			return -1;
		}
	}

	public boolean removeEntry(long rowIndex) {
		return db.delete(DB_TABLE, KEY_ID + "=" + rowIndex, null) > 0;
	}

	public Cursor getAllEntries() {
		return db.query(DB_TABLE, new String[] { KEY_ID, KEY_RECIPIENT, KEY_SUBJECT, KEY_BODY,
				KEY_SCHEDULED, KEY_MODIFIED, KEY_GMT_OFFSET }, null, null, null, null, null);
	}

	public List<OutgoingText> getAllEntriesList() {
		List<OutgoingText> textList = new ArrayList<OutgoingText>();
		Cursor cursor = db.query(true, DB_TABLE, new String[] { KEY_ID, KEY_RECIPIENT,
				KEY_SUBJECT, KEY_BODY, KEY_SCHEDULED, KEY_MODIFIED, KEY_GMT_OFFSET }, null, null,
				null, null, KEY_SCHEDULED, null);
		if (cursor.moveToFirst()) {
			do {
				String recipient = cursor.getString(RECIPIENT_COLUMN);
				String subject = cursor.getString(SUBJECT_COLUMN);
				String body = cursor.getString(BODY_COLUMN);
				Calendar scheduled = Calendar.getInstance();
				scheduled.setTimeInMillis(cursor.getLong(SCHEDULEDTIME_COLUMN));
				Calendar modified = Calendar.getInstance();
				modified.setTimeInMillis(cursor.getLong(MODIFIEDTIME_COLUMN));
				long gmtOffset = cursor.getInt(GMT_COLUMN);
				try {
					textList.add(new OutgoingText(recipient, subject, body, scheduled, modified,
							gmtOffset));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
			cursor.close();
			return textList;
		} else {
			cursor.close();
			return new ArrayList<OutgoingText>();
		}
	}

	public Cursor setCursorToEntry(long rowIndex) {
		Cursor result = db.query(true, DB_TABLE, new String[] { KEY_ID, KEY_RECIPIENT,
				KEY_SUBJECT, KEY_BODY, KEY_SCHEDULED, KEY_MODIFIED }, KEY_ID + "=" + rowIndex, null,
				null, null, null, null);
		if (result.getCount() == 0 || !result.moveToFirst()) {
			throw new SQLException("No entry found for row " + rowIndex);
		} else {
			return result;
		}
	}

	public OutgoingText getEntry(long rowIndex) {
		Cursor cursor = db.query(true, DB_TABLE, new String[] { KEY_ID, KEY_RECIPIENT,
				KEY_SUBJECT, KEY_BODY, KEY_SCHEDULED, KEY_MODIFIED }, KEY_ID + "=" + rowIndex, null,
				null, null, null, null);
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			throw new SQLException("No entry found for row " + rowIndex);
		} else {
			String recipient = cursor.getString(RECIPIENT_COLUMN);
			String subject = cursor.getString(SUBJECT_COLUMN);
			String body = cursor.getString(BODY_COLUMN);
			Calendar scheduled = Calendar.getInstance();
			scheduled.setTimeInMillis(cursor.getLong(SCHEDULEDTIME_COLUMN));
			Calendar modified = Calendar.getInstance();
			scheduled.setTimeInMillis(cursor.getLong(MODIFIEDTIME_COLUMN));
			long gmtOffset = cursor.getInt(GMT_COLUMN);
			try {
				cursor.close();
				return new OutgoingText(recipient, subject, body, scheduled, modified, gmtOffset);
			} catch (Exception e) {
				e.printStackTrace();
			}
			cursor.close();
			return null;
		}

	}

	//
	// public boolean updateEntry(long rowIndex, OutgoingText text) {
	// return true;
	// }

	private static class TextOpenHelper extends SQLiteOpenHelper {

		public TextOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("DbAdapter", "Upgrading from version " + oldVersion + " to version " + newVersion
					+ ", which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
			onCreate(db);
		}

	}

}
