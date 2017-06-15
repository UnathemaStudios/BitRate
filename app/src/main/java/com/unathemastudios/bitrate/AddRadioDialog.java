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
        public void onDialogPositiveClick(String name, String url);
    }
    private NoticeDialogListener mListener;
    private EditText etName, etUrl;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View textEntryView = factory.inflate(R.layout.add_radio_dialog_layout, null);
        etName = (EditText)textEntryView.findViewById(R.id.etName);
        etUrl = (EditText)textEntryView.findViewById(R.id.etUrl);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.add_radio_dialog_title)
                .setView(textEntryView)
                .setPositiveButton(R.string.add_radio_dialog_add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(!(etName.getText().toString().equals("")||etUrl
                                .getText().toString().equals(""))) {
                            mListener.onDialogPositiveClick(etName.getText().toString(), etUrl
                                    .getText().toString());
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
}
