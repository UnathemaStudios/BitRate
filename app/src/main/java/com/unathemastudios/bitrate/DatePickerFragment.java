package com.unathemastudios.bitrate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

/**
 * Created by georg on 20/9/2017.
 */

public  class DatePickerFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener {

	public interface NoticeDialogListener {
		public void onDialogPositiveClick(int year, int month, int day);
	}
	public DatePickerFragment.NoticeDialogListener mListener;

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (DatePickerFragment.NoticeDialogListener) getTargetFragment();
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {
		mListener.onDialogPositiveClick(year, month, day);
	}
}
