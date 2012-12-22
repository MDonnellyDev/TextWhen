package com.mddev.l8text;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import android.widget.TextView;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {

	private TextEditor	textEditor;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		textEditor = (TextEditor) this.getActivity();
		Calendar cal = textEditor.getSchedule();
		return new DatePickerDialog(this.getActivity(), 0, this,
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar cal = textEditor.getSchedule();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DATE, dayOfMonth);

		textEditor.refreshDate(false, true);
	}

}
