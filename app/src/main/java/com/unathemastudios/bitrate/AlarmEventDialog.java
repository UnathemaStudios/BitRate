package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;


import java.text.ParseException;
import java.util.Date;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class AlarmEventDialog extends DialogFragment implements TimePickerFragment
		.NoticeDialogListener, DatePickerFragment.NoticeDialogListener {


	public interface NoticeDialogListener {
		public void onDialogPositiveClick(Alarm alarm);
	}
	public AlarmEventDialog.NoticeDialogListener mListener;

	private TextView tvTime, tvDate;
	private CheckBox checkBox;
	private Spinner spinner;
	private Calendar calendar;
	

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.add_event_dialog, null);
		tvDate = (TextView) textEntryView.findViewById(R.id.tvDateSelected);
		tvTime = (TextView) textEntryView.findViewById(R.id.tvTimeSelectedForEvent);
		checkBox = (CheckBox) textEntryView.findViewById(R.id.cbWithDate);
		spinner = (Spinner) textEntryView.findViewById(R.id.spChooseRadio);
		tvDate.setEnabled(false);
		calendar = Calendar.getInstance();
		
		//Checkbox
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) tvDate.setEnabled(true);
				else tvDate.setEnabled(false);
			}
		});

		//Time TextBox
		tvTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
		tvTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerFragment timePickerFragment = new TimePickerFragment();
				timePickerFragment.setTargetFragment(AlarmEventDialog.this, 50);
				timePickerFragment.show(getFragmentManager(), "GIMMETIME");
			}
		});

		//Date TextBox
		tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" +( calendar.get(Calendar.MONTH) +1 ) + "/" + calendar.get(Calendar.YEAR));
		tvDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment datePickerFragment = new DatePickerFragment();
				datePickerFragment.setTargetFragment(AlarmEventDialog.this, 52);
				datePickerFragment.show(getFragmentManager(), "GIMMEDATE");
			}
		});

		//Spinner
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R
				.layout.simple_list_item_1);
		for (Radio a:((MainActivity)getActivity()).radiosList){
			arrayAdapter.add(a.getName());
		}
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(arrayAdapter);


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView)
				.setTitle("Add Alarm Event")
				.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.w("hgfhgf", tvDate.getText().toString()+" "+tvTime.getText().toString());
						
						
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy " + "HH:mm");
						Date datetoepoch = null;
						try {
							datetoepoch = simpleDateFormat.parse(tvDate.getText().toString() + " " + tvTime.getText().toString());
						} catch (ParseException e) {
							e.printStackTrace();
						}
						Log.w("date", datetoepoch + "");
						assert datetoepoch != null;
						long time = datetoepoch.getTime();
						
						Log.w("ALARM IN", ((time - System.currentTimeMillis()) / 1000 / 60 / 60)+" HOURS" + " & " + ((time - System.currentTimeMillis()) / 1000 / 60 % 60)+" MINUTES");
						mListener.onDialogPositiveClick(new Alarm(((MainActivity)getActivity())
								.radiosList.get(spinner.getSelectedItemPosition()), time, false,
								false, checkBox.isChecked(), spinner.getSelectedItemPosition()));

					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AlarmEventDialog.this.dismiss();
					}
				});

		
		return builder.create();
	}

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (AlarmEventDialog.NoticeDialogListener) getTargetFragment();
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		super.show(manager, tag);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public void onDialogPositiveClick(int hourOfTHeDay, int minute) {
		tvTime.setText(new DecimalFormat("00").format(hourOfTHeDay) + ":" + new DecimalFormat
				("00").format(minute));

		if(hourOfTHeDay<calendar.get(Calendar.HOUR_OF_DAY) || (hourOfTHeDay == calendar.get(Calendar.HOUR_OF_DAY) && minute <= calendar.get
				(Calendar.MINUTE))) {
			if (!checkBox.isChecked()) {
				tvDate.setText((calendar.get(Calendar.DAY_OF_MONTH) +1)+ "/" + (calendar.get
						(Calendar
						.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));
			}
		}
		else {
			if (!checkBox.isChecked()) {
				tvDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar
						.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));
			}
		}
	}

	@Override
	public void onDialogPositiveClick(int year, int month, int day) {
		tvDate.setText(day + "/" + (month + 1) + "/" + year);
	}
}
