package com.mddev.l8text;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent incomingIntent, int flags, int startId) {
		Intent intent = new Intent();
		intent.putExtras(incomingIntent.getExtras());
		intent.setAction(TextAlarmReceiver.ACTION_SCHEDULED_ALARM_TEXT);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(),
				(int) incomingIntent.getLongExtra("key", 0), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		if (incomingIntent.getBooleanExtra("setAlarm", true)) {
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					incomingIntent.getLongExtra("date", 0L), pendingIntent);
		} else {
			alarmManager.cancel(pendingIntent);
		}

		return Service.START_NOT_STICKY;
	}

}
