package com.unathemastudios.bitrate;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.unathemastudios.bitrate.bitrate.R;

import java.util.Locale;

public class RecordRadioDialog extends DialogFragment {

    interface NoticeDialogListener {
        public void onDialogPositiveClick(int hour, int minute);
    }
    private NoticeDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View textEntryView = factory.inflate(R.layout.record_radio_dialog_layout, null);
        final NumberPicker npHour = (NumberPicker)textEntryView.findViewById(R.id.npHour);
        final NumberPicker npMinute = (NumberPicker)textEntryView.findViewById(R.id.npMinute);
        final RadioButton rbUnlimited = (RadioButton)textEntryView.findViewById(R.id.rbUnlimitedTime);
        final RadioButton rbLimitedTime = (RadioButton)textEntryView.findViewById(R.id.rbLimitedTime);
        npHour.setMaxValue(23);
        npHour.setMinValue(0);
        npHour.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format(Locale.US, "%02d", i);
            }
        });
        npMinute.setMaxValue(59);
        npMinute.setMinValue(1);
        npMinute.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format(Locale.US, "%02d", i);
            }
        });
		npHour.setEnabled(false);
		npMinute.setEnabled(false);
		RadioGroup radioGroup = (RadioGroup)textEntryView.findViewById(R.id.rbGroup);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId)
			{
				if(checkedId == R.id.rbLimitedTime)
				{
					npHour.setEnabled(true);
					npMinute.setEnabled(true);
				}
				else
				{
					npHour.setEnabled(false);
					npMinute.setEnabled(false);
				}
			}
		});
				
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(textEntryView)
				.setTitle("Start a new recording")
				.setPositiveButton("START", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(rbUnlimited.isChecked()){
                            mListener.onDialogPositiveClick(-1, -1);
                        }
                        else mListener.onDialogPositiveClick(npHour.getValue(), npMinute.getValue());
                        RecordRadioDialog.this.dismiss();
                    }
                })
                .setNegativeButton(R.string.add_radio_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RecordRadioDialog.this.getDialog().cancel();
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
            mListener = (NoticeDialogListener) getTargetFragment();
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
