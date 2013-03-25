package com.mtd.textwhen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TextAlarmReceiver extends BroadcastReceiver {
	public static final String	ACTION_SCHEDULED_ALARM_TEXT	= "com.mtd.textwhen.ACTION_SCHEDULED_ALARM_TEXT";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, SmsService.class);
		i.putExtras(intent);
		context.startService(i);
	}

}
