package com.example.georg.radiostreameralt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by georg on 27/4/2017.
 */

public class ConfirmationDialog extends DialogFragment {

	private int position;

	public interface NoticeDialogListener {
		public void onDialogPositiveClick(int pos);
	}
	private ConfirmationDialog.NoticeDialogListener mListener;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Are you sure?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mListener.onDialogPositiveClick(position);
						ConfirmationDialog.this.dismiss();
					}
				})
				.setNegativeButton(R.string.add_radio_dialog_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						ConfirmationDialog.this.getDialog().cancel();
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
			mListener = (ConfirmationDialog.NoticeDialogListener) getTargetFragment();
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	public void show(FragmentManager manager, String tag, int position) {
		super.show(manager, tag);
		this.position = position;
	}
}
