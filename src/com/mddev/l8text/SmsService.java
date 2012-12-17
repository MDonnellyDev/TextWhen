package com.mddev.l8text;

import java.util.Calendar;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SmsService extends Service {

	public static final String	SMS_SENT			= "SMS_SENT";
	public static final String	SMS_DELIVERED	= "SMS_DELIVERED";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		OutgoingText text = null;

		text = OutgoingText.fromIntent(intent);

		SmsManager sms = SmsManager.getDefault();

		PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				SMS_SENT), 0);

		PendingIntent deliveryIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(SMS_DELIVERED), 0);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT)
								.show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(getBaseContext(), "SMS failed to be sent",
								Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT)
								.show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT)
								.show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT)
								.show();
						break;
				}
			}
		}, new IntentFilter(SMS_SENT));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(getBaseContext(), "SMS delivered",
								Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						Toast.makeText(getBaseContext(), "SMS delivery failed",
								Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter(SMS_DELIVERED));

		int i = 0;
		for (String recipient : text.getRecipient().split(",")) {
			sms.sendTextMessage(recipient, null, text.getMessageContent(),
					sentIntent, deliveryIntent);
		}


		
		if(!text.getRecurrence().equals(OutgoingText.RECURRENCE.NONE)){
			if(!rescheduleText(text)){
				TextDbAdapter db = new TextDbAdapter(this);
				db.open();
				db.removeEntry(db.getEntryRow(text));
				db.close();
			}
		}
		
		return Service.START_NOT_STICKY;
	}

	private boolean rescheduleText(OutgoingText text) {
		Calendar newSchedule = TextRecurrence.getNextRecurrence(text.getScheduledDate(), text.getRecurrence());
		if(newSchedule.equals(text.getScheduledDate())){
			return false;			
		}else{
			text.updateText(null, null, null, newSchedule, null);
			SmsService.this.startService(text.toIntent(SmsService.this, true, false));			
			return true;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
