package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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


import java.sql.Timestamp;
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
	String date;
	private int hour = 0;
	private int minute = 0;
	

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
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) tvDate.setEnabled(true);
				else tvDate.setEnabled(false);
			}
		});

		//Time TextBox
		tvTime.setText(Calendar.HOUR_OF_DAY + ":" + Calendar.MINUTE);
		tvTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerFragment timePickerFragment = new TimePickerFragment();
				timePickerFragment.setTargetFragment(AlarmEventDialog.this, 50);
				timePickerFragment.show(getFragmentManager(), "GIMMETIME");
			}
		});

		//Date TextBox
		tvDate.setText(Calendar.DAY_OF_MONTH + "/" + Calendar.MONTH + "/" + Calendar.YEAR);
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
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy " +
								"HH:mm");
						Date datetoepoch = null;
						try {
							datetoepoch = simpleDateFormat.parse(date + " " + hour + ":" + minute);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						Log.w("date", datetoepoch + "");
						assert datetoepoch != null;
						long time = datetoepoch.getTime();
						long timetoepoch = time - System.currentTimeMillis();
						Timestamp stamp = new Timestamp(timetoepoch);
						Date date2 = new Date(stamp.getDate());
						Log.w("till", date2 +"");

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

	@Override
	public void onDialogPositiveClick(int hourOfTHeDay, int minute, boolean is24) {
		hour = hourOfTHeDay;
		this.minute = minute;
		tvTime.setText(hourOfTHeDay + ":" + minute);
	}

	@Override
	public void onDialogPositiveClick(int year, int month, int day) {
		this.date = day + "/" + month + "/" + year;
		tvDate.setText(this.date);
	}
}
