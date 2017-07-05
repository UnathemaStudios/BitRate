package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by georg on 4/7/2017.
 */

public class ChooseLinkDialog extends DialogFragment {

	String id;

	public interface NoticeDialogListener {
		public void onDialogPositiveClick(String url);
	}
	public ChooseLinkDialog.NoticeDialogListener mListener;


	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
						ChooseLinkDialog.this.getDialog().cancel();
					}
				});
		// Create the AlertDialog object and return it

		ExtarctLinksFromPls extarctLinksFromPls = new ExtarctLinksFromPls(getContext(),
				textEntryView, id, mListener, this);
		extarctLinksFromPls.execute();

		return builder.create();
	}

	public void close(){
		ChooseLinkDialog.this.dismiss();
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
			mListener = (ChooseLinkDialog.NoticeDialogListener) getTargetFragment();
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

	public void show(FragmentManager manager, String tag, String id) {
		super.show(manager, tag);

		this.id = id;
	}

}

class ExtarctLinksFromPls extends AsyncTask<Void, Void, ArrayList<String>>{

	private String id;
	private Context con;
	private ArrayList<String> links;
	private View view;
	private ChooseLinkDialog.NoticeDialogListener mListener;
	private ChooseLinkDialog dialog;

	public ExtarctLinksFromPls(Context context, View view, String id, ChooseLinkDialog.NoticeDialogListener mListener, ChooseLinkDialog dialog) {
		this.con = context;
		this.view = view;
		this.id = id;
		this.mListener = mListener;
		this.dialog = dialog;
		links = new ArrayList<>();
	}
	@Override
	protected ArrayList<String> doInBackground(Void...params) {

		BufferedReader reader = null;
		try {
			URLConnection urlConnection = new URL("http://yp.shoutcast.com/sbin/tunein-station" +
					".pls?id=" + id).openConnection();
			reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String line;
			while((line = reader.readLine()) != null){
				if(!(line.startsWith("["))) {
					line = line.trim();
					String[] splitted = line.split("=");
					if (splitted[1].startsWith("http://")) {
						links.add(splitted[1]);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			assert reader != null;
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return links;
	}

	@Override
	protected void onPostExecute(final ArrayList<String> strings) {
		super.onPostExecute(strings);

		BindDictionary<String> dictionary = new BindDictionary<>();
		
		if (strings.size() == 1){
			
			mListener.onDialogPositiveClick(strings.get(0));
			dialog.dismiss();
		}
		else{
			
			dictionary.addStringField(R.id.tv_choose_link_pls, new StringExtractor<String>() {
				@Override
				public String getStringValue(String item, int position) {
					return "Link" + (position + 1) + ": " + item;
				}
			});
			
			FunDapter<String> adapter = new FunDapter<>(con, strings, R.layout
					.choose_link_listview_layout, dictionary);
			
			ListView listView = (ListView)view.findViewById(R.id.lv_pls_links);
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mListener.onDialogPositiveClick(strings.get(position));
				}
			});
		}		
	}
}