package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.R.attr.duration;

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
	private EditText etTerm;
	private ImageButton ibSearch;
	private String searchTerm;
	private FileOutputStream fileOutputStream;
	private File outputSource;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.search_shoutcast_dialog, null);
		ibClose = (ImageButton)textEntryView.findViewById(R.id.ibCloseSearchDialog);
		etTerm = (EditText)textEntryView.findViewById(R.id.etSearchTerm);
		ibSearch = (ImageButton)textEntryView.findViewById(R.id.ibSearch);
		searchTable = new ArrayList<>();
		final FunDapter<String> adapter;


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

		ibSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchTerm = etTerm.getText().toString().replace(" ", "+");


				new Thread(new Runnable() {
					@Override
					public void run() {

						fileOutputStream = null;
						File internalDir = new File(getContext().getFilesDir()+"");
						outputSource = new File(internalDir,"temp.xml");
						try
						{
							fileOutputStream = new FileOutputStream(outputSource);
						} catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}

						URL url = null;
						try {
							url = new URL("http://api.shoutcast.com/legacy/stationsearch?k=B9nzm2JVUDlEwjIV&search=" + searchTerm);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						InputStream inputStream = null;
						try {
							inputStream = url.openStream();
						} catch (IOException e) {
							e.printStackTrace();
						}
						int c;
						Log.w("duration", (Long.toString(duration)));
						try {
							while ((c = inputStream.read()) != -1)
							{
								assert fileOutputStream != null;
								fileOutputStream.write(c);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (outputSource.exists())
						{
							try {
								XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
								XmlPullParser xmlPullParser = xppFactory.newPullParser();
								xmlPullParser.setInput(new FileInputStream(outputSource), "utf-8");

								String name = null;
								int eventType = xmlPullParser.getEventType();
								while (eventType != XmlPullParser.END_DOCUMENT) {
									switch (eventType) {
										case XmlPullParser.START_TAG:

											if (xmlPullParser.getName().equals("station")) {
												name = xmlPullParser.nextText();
												searchTable.add(name);
												Log.w("red", name);
											}
											break;
										case XmlPullParser.END_TAG:
											break;
										default:
											break;
									} // end switch

									// Move forward the parsing "cursor", or you can stop parsing
									eventType = xmlPullParser.next();

								} // end whiles


							} catch (XmlPullParserException | IOException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			}
		});



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
