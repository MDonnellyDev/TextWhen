package com.mddev.l8text;

import java.net.URI;
import java.util.Date;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SmsService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		OutgoingText text = null;

		text = OutgoingText.fromIntent(intent);
		TextDbAdapter db = new TextDbAdapter(this);

		db.open();
		db.removeEntry(db.getEntryRow(text));
		db.close();

		SmsManager sms = SmsManager.getDefault();

		for (String recipient : text.getRecipient().split(",")) {
			sms.sendTextMessage(recipient, null, text.getMessageContent(), null, null);
			
			ContentValues cv = new ContentValues();

			cv.put("address", recipient);
			cv.put("date", (new Date()).getTime());
			cv.put("type", 2);
//			cv.put("subject", null);
			cv.put("body", text.getMessageContent());
			
			
			this.getContentResolver().insert(Uri.parse("content://sms/sent"), cv);
		}
		
		return Service.START_NOT_STICKY;

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
