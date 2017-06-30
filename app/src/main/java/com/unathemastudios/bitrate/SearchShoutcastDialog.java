package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import java.util.ArrayList;

/**
 * Created by georg on 30/6/2017.
 */

public class SearchShoutcastDialog extends DialogFragment {

	public interface NoticeDialogListener {
		public void onDialogPositiveClick(String name, String url);
	}
	private AddRadioDialog.NoticeDialogListener mListener;

	private ImageButton ibClose;
	private ArrayList<String> searchTable;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.search_shoutcast_dialog, null);
		ibClose = (ImageButton)textEntryView.findViewById(R.id.ibCloseSearchDialog);
		searchTable = new ArrayList<>();
		FunDapter<String> adapter;

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView);
		// Create the AlertDialog object and return it

		BindDictionary<String> bindDictionary = new BindDictionary<>();

		bindDictionary.addStringField(R.id.search_term_name, new StringExtractor<String>() {
			@Override
			public String getStringValue(String item, int position) {
				return item;
			}
		});

		adapter = new FunDapter(getContext(),searchTable, R.layout.search_terms_layout,
				bindDictionary);
		ListView listView = (ListView)textEntryView.findViewById(R.id.search_results_list_view);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);

		ibClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchShoutcastDialog.this.getDialog().cancel();
			}
		});

		return builder.create();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	// Use this instance of the interface to deliver action event

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (AddRadioDialog.NoticeDialogListener) getTargetFragment();
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
