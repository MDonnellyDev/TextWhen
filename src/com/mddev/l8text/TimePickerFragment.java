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
	
  private TextEditor	textEditor;

	@Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
		textEditor = (TextEditor)this.getActivity();
		Time t = textEditor.getTime();
      // Create a new instance of TimePickerDialog and return it
      return new TimePickerDialog(this.getActivity(), this, t.getHours(), t.getMinutes(),
              DateFormat.is24HourFormat(this.getActivity()));
  }

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
		Time t = new Time(hourOfDay, minute, 0);
		
		textEditor.setTime(t);
		
	}

}
