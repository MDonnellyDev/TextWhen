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
		Date d = textEditor.getDate();
		return new DatePickerDialog(this.getActivity(), 0, this, d.getYear(),
				d.getMonth(), d.getDate());
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {

		Date d = new Date(year, monthOfYear, dayOfMonth);
		textEditor.setDate(d);

	}

}
