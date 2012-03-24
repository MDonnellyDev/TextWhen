package com.odev.l8text;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class TextEditor extends Activity {
	TextDbAdapter							db;
	DatePicker								datePicker;
	TimePicker								timePicker;
	Button										newBtn;
	Button										contactPickerBtn;
	Button										sendBtn;
	Button										cancelBtn;
	EditText									editRecipient;
	EditText									editSubject;
	EditText									editBody;
	Intent										incomingIntent;
	Bundle										savedInstanceState;

	private static final int	CONTACT_PICKER_RESULT	= 1001;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editview);
		this.incomingIntent = this.getIntent();
		if (db == null) {
			db = new TextDbAdapter(this);
			db.open();
		}
		int rotation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay()
				.getOrientation();
		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
			setContentView(R.layout.editview);
		} else {
			setContentView(R.layout.editview_landscape);
		}

		datePicker = (DatePicker) findViewById(R.id.DatePicker);
		timePicker = (TimePicker) findViewById(R.id.TimePicker);
		editRecipient = (EditText) findViewById(R.id.EditTextRecipient);
		editBody = (EditText) findViewById(R.id.EditTextBody);
		contactPickerBtn = (Button) findViewById(R.id.ContactPicker);
		newBtn = (Button) findViewById(R.id.ButtonAddNew);
		cancelBtn = (Button) findViewById(R.id.ButtonCancel);
		sendBtn = (Button) findViewById(R.id.ButtonSendNow);

		if (incomingIntent.getBooleanExtra("update", false)) {
			editRecipient.setText(incomingIntent.getStringExtra("recipient"));
			editBody.setText(incomingIntent.getStringExtra("body"));
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(incomingIntent.getLongExtra("date", 0));
			datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
					.get(Calendar.DAY_OF_MONTH));
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
		}

		initializeButtons();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		datePicker.updateDate(savedInstanceState.getInt("year"), savedInstanceState
				.getInt("month"), savedInstanceState.getInt("date"));
		timePicker.setCurrentHour(savedInstanceState.getInt("hour"));
		timePicker.setCurrentMinute(savedInstanceState.getInt("minute"));
		editBody.setText(savedInstanceState.getString("body"));
		String recipient = editRecipient.getText().toString();
		if (recipient.length() == 0) {
			editRecipient.setText(savedInstanceState.getString("recipient"));
		}

	}

	private void initializeButtons() {
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TextEditor.this.finish();
			}
		});

		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (incomingIntent.getBooleanExtra("update", false)) {
					OutgoingText text = OutgoingText.fromIntent(incomingIntent);
					db.removeEntry(text.getKey());
					Intent intent = text.toIntent(TextEditor.this, false, true);
					startService(intent);
					Calendar c = Calendar.getInstance();
					text.updateText(text.recipient, text.subject, text.messageContent, c, c
							.getTimeZone().getOffset(c.getTimeInMillis()));
					intent = text.toIntent(TextEditor.this, true, false);
					startService(intent);
				} else {
					if (editRecipient.getText().toString().length() >= 5) {
						OutgoingText text = new OutgoingText(editRecipient.getText().toString(), null,
								editBody.getText().toString(), Calendar.getInstance(), 0);
						Intent intent = text.toIntent(TextEditor.this, true, false);
						startService(intent);
					} else {
						Toast.makeText(v.getContext(), "Please check the recipient field again.",
								Toast.LENGTH_LONG).show();
					}
				}

				TextEditor.this.finish();
			}
		});

		contactPickerBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
			}
		});

		newBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (editRecipient.getText() != null
						&& editRecipient.getText().toString().length() >= 5) {
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, datePicker.getYear());
					cal.set(Calendar.HOUR_OF_DAY, datePicker.getMonth());
					cal.set(Calendar.HOUR_OF_DAY, datePicker.getDayOfMonth());
					cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
					cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
					cal.set(Calendar.SECOND, 0);
					int gmtOffset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
					if (cal.after(Calendar.getInstance())) {
						String s = editBody.getText().toString();
						OutgoingText text = new OutgoingText(editRecipient.getText().toString(), null,
								editBody.getText().toString(), cal, gmtOffset);

						Intent passedIntent;
						boolean update = incomingIntent.getBooleanExtra("update", false);
						boolean setAlarm = incomingIntent.getBooleanExtra("setAlarm", true);
						if (update) {
							text.setKey(incomingIntent.getLongExtra("key", 0));
							passedIntent = text.toIntent(TextEditor.this, false, update);
							db.updateEntry(text, text.getKey());
							TextEditor.this.startService(passedIntent);

							passedIntent = text.toIntent(TextEditor.this, true, update);
							TextEditor.this.startService(passedIntent);
						} else {
							long key = db.insertEntry(text);
							text.setKey(key);
							passedIntent = text.toIntent(TextEditor.this, setAlarm, update);
							TextEditor.this.startService(passedIntent);
						}

						Toast.makeText(v.getContext(), "Saved Message: " + text.toString(),
								Toast.LENGTH_LONG).show();

						TextEditor.this.finish();

					} else {

						Toast.makeText(v.getContext(),
								"Sending immediately--time or date has passed already.", Toast.LENGTH_LONG)
								.show();

						if (incomingIntent.getBooleanExtra("update", false)) {
							OutgoingText text = OutgoingText.fromIntent(incomingIntent);
							db.removeEntry(text.getKey());
							Intent intent = text.toIntent(TextEditor.this, false, true);
							startService(intent);
							Calendar c = Calendar.getInstance();
							text.updateText(text.recipient, text.subject, text.messageContent, c, c
									.getTimeZone().getOffset(c.getTimeInMillis()));
							intent = text.toIntent(TextEditor.this, true, false);
							TextEditor.this.startService(intent);
						} else {
							OutgoingText text = new OutgoingText(editRecipient.getText().toString(), null,
									editBody.getText().toString(), Calendar.getInstance(), 0);
							Intent intent = text.toIntent(TextEditor.this, true, false);
							TextEditor.this.startService(intent);
						}

						TextEditor.this.finish();
					}

				} else {

					Toast
							.makeText(
									v.getContext(),
									"Message cannot be saved as is.  Please ensure date, time, recipient, and message are filled in.",
									Toast.LENGTH_LONG).show();
				}
			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CONTACT_PICKER_RESULT:
				Cursor cursor = null;
				String phone = "";
				try {
					Uri result = data.getData();

					// get the contact id from the Uri
					String id = result.getLastPathSegment();

					// query for mobile phone
					cursor = getContentResolver().query(Phone.CONTENT_URI, null,
							Phone.CONTACT_ID + "=?" + "AND " + Phone.TYPE + "=?",
							new String[] { id, String.valueOf(Phone.TYPE_MOBILE) }, null);

					int phoneIdx = cursor.getColumnIndex(Phone.NUMBER);

					if (cursor.moveToFirst()) {
						phone = cursor.getString(phoneIdx);
					} else {
					}
				} catch (Exception e) {
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					if (phone.length() == 0) {
						Toast.makeText(this, "No phone number found for contact.", Toast.LENGTH_LONG)
								.show();
					} else {
						if (editRecipient.getText().toString().length() > 0) {
							editRecipient.setText(editRecipient.getText().toString() + "," + phone);
						} else {
							editRecipient.setText(phone);
						}
					}
				}
				break;
			}
		}
	}

	@Override
	public void finish() {
		super.finish();
		if (!(db == null)) {
			db.close();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		TextEditor.this.finish();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("month", datePicker.getMonth());
		savedInstanceState.putInt("date", datePicker.getDayOfMonth());
		savedInstanceState.putInt("year", datePicker.getYear());
		savedInstanceState.putInt("hour", timePicker.getCurrentHour());
		savedInstanceState.putInt("minute", timePicker.getCurrentMinute());
		savedInstanceState.putString("recipient", editRecipient.getText().toString());
		savedInstanceState.putString("body", editBody.getText().toString());
		long key = incomingIntent.getLongExtra("key", 0);
		if (key != 0) {
			savedInstanceState.putLong("key", key);
		}
	}

}
