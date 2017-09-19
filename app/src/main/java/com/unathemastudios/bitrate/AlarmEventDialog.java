package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class AlarmEventDialog extends DialogFragment {
	
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(String url);
	}
	public AlarmEventDialog.NoticeDialogListener mListener;
	
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.choose_link_layout, null);
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView)
				.setTitle("Choose a link")
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						ChooseLinkDialog.this.getDialog().cancel();
					}
				});
//		 Create the AlertDialog object and return it
		
//		ExtarctLinksFromPls extarctLinksFromPls = new ExtarctLinksFromPls(getContext(), textEntryView, id, mListener, this);
//		extarctLinksFromPls.execute();
		
		return builder.create();
	}
}
