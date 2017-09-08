package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;


public class SleepTimerDialog extends DialogFragment {
    interface NoticeDialogListener {
        public void onDialogPositiveClick(int minutes);
    }
    private SleepTimerDialog.NoticeDialogListener mListener;
    private NumberPicker timePicker;

    @NonNull
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.sleep_timer_dialog_layout, null);
		timePicker = (NumberPicker)textEntryView.findViewById(R.id.numberPicker);
		int maxValue = 12;
		final int interval = 10;
		timePicker.setMaxValue(maxValue);
		timePicker.setMinValue(1);
		String[] values = new String[maxValue];
		for (int i = 0 ; i < maxValue ; i++)
		{
			values[i] = Integer.toString((i+1)*interval);
		}
		timePicker.setDisplayedValues(values);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Sleep Minutes")
				.setView(textEntryView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mListener.onDialogPositiveClick(timePicker.getValue()*interval);
						SleepTimerDialog.this.dismiss();
					}
				})
				.setNegativeButton(R.string.add_radio_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SleepTimerDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();

    }

    // Use this instance of the interface to deliver action event

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SleepTimerDialog.NoticeDialogListener) getTargetFragment();
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
}
