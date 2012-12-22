package com.mddev.l8text;

import java.sql.Time;
import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		OnTimeSetListener {
	
  private TextEditor	textEditor = (TextEditor) this.getActivity();

	@Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
		textEditor = (TextEditor)this.getActivity();
		Calendar cal = textEditor.getSchedule();
      // Create a new instance of TimePickerDialog and return it
      return new TimePickerDialog(this.getActivity(), this, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
              DateFormat.is24HourFormat(this.getActivity()));
  }

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		Calendar cal = textEditor.getSchedule();
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);
		
		textEditor.refreshDate(true, false);		
	}

}
