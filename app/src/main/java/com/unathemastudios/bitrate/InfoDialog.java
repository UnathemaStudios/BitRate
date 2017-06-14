package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by georg on 13/6/2017.
 */

public class InfoDialog extends DialogFragment {

	private String description;
	private String name;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(description)
				.setTitle(name)
				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						InfoDialog.this.dismiss();
					}
				});
		// Create the AlertDialog object and return it
		return builder.create();

	}

	public void show(FragmentManager manager, String tag, String name, String description) {
		super.show(manager, tag);
		this.description = description;
		this.name = name;
	}
}
