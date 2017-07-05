package com.unathemastudios.bitrate;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by georg on 18/2/2017.
 */

public class AddRadioDialog extends DialogFragment {

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(String name, String url, String description);
		public void onDialogPositiveClick(String name, String url, String description, int pos);
		public void onDialogPositiveClick(String name, String url, String description, boolean fromShoutcast);
    }
    private NoticeDialogListener mListener;
    private EditText etName, etUrl, etDescription;
    private Radio radio = null;
	private boolean edit = false;
	private int pos;
	private String positiveButton;
	private boolean fromShoutcast = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View textEntryView = factory.inflate(R.layout.add_radio_dialog_layout, null);
        etName = (EditText)textEntryView.findViewById(R.id.etName);
        etUrl = (EditText)textEntryView.findViewById(R.id.etUrl);
        etDescription = (EditText)textEntryView.findViewById(R.id.etDescription);
        if(radio!=null){
            etName.setText(radio.getName());
            etUrl.setText(radio.getUrl());
            etDescription.setText(radio.getDescription());
        }
        if(edit) positiveButton = "Edit";
		else positiveButton = "Add";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.add_radio_dialog_title)
                .setView(textEntryView)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(!(etName.getText().toString().equals("")||etUrl.getText().toString().equals("")))
                        {
							 String tempUrl = "";
							if (etUrl.getText().toString().startsWith("http://")) {
								tempUrl = etUrl.getText().toString();
							}
							else 
							{
								tempUrl = "http://" + etUrl.getText().toString();
							}
							
							if(edit){
								mListener.onDialogPositiveClick(etName.getText().toString(),
										tempUrl, etDescription.getText().toString(), pos);
							}
							else if(fromShoutcast){
								mListener.onDialogPositiveClick(etName.getText().toString(),
										tempUrl, etDescription.getText().toString(), true);
							}
							else {
								mListener.onDialogPositiveClick(etName.getText().toString(),
										tempUrl, etDescription.getText().toString());
							}
							AddRadioDialog.this.dismiss();
                        }
                        else Toast.makeText(getContext(), "Name and URL can not be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.add_radio_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddRadioDialog.this.getDialog().cancel();
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

    public void show(FragmentManager manager, String tag, Radio radio) {
        this.radio = radio;
        super.show(manager, tag);
    }
	
	public void show(FragmentManager manager, String tag, Radio radio, boolean edit, int pos) {
		this.radio = radio;
		this.edit = edit;
		this.pos = pos;
		super.show(manager, tag);
	}
	
	public void show(FragmentManager manager, String tag, Radio radio, boolean fromShoutcast) {
		this.radio = radio;
		this.fromShoutcast = fromShoutcast;
		super.show(manager, tag);
	}
}
