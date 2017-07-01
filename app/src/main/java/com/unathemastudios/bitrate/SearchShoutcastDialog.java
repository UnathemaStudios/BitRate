package com.unathemastudios.bitrate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import java.util.ArrayList;


public class SearchShoutcastDialog extends DialogFragment {

	public interface NoticeDialogListener {
		public void onDialogPositiveClick(Radio radio);
	}
	public SearchShoutcastDialog.NoticeDialogListener mListener;

	private ImageButton ibClose;
	private ArrayList<Radio> searchTable;
	private EditText etTerm;
	private ImageButton ibSearch;
	private String searchTerm;

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


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView);
		// Create the AlertDialog object and return it

		ibSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchTerm = etTerm.getText().toString().replace(" ", "+");
				SearchByName searchByName = new SearchByName(getContext(), textEntryView,
						searchTerm, mListener);
				searchByName.execute();
				/*mListener.onDialogPositiveClick(new Radio("TEstShoutcast", "url", true,
						"description", 128, "genre"));
				SearchShoutcastDialog.this.dismiss();*/
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

	public void close(){
		SearchShoutcastDialog.this.dismiss();
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
			mListener = (SearchShoutcastDialog.NoticeDialogListener) getTargetFragment();
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


class SearchByName extends AsyncTask<Void, Void, ArrayList<Radio>> {

	private Context con;
	private String searchTerm;
	private ArrayList<Radio> searchTable;
	private View view;
	private SearchShoutcastDialog.NoticeDialogListener mListener;


	SearchByName(Context context, View view, String searchTerm, SearchShoutcastDialog
			.NoticeDialogListener mListener) {
		this.con = context;
		this.view = view;
		this.searchTerm = searchTerm;
		this.mListener = mListener;
		searchTable = new ArrayList<>();
	}

	@Override
	protected ArrayList<Radio> doInBackground(Void... params) {
		Log.w("Start", "");
		FileOutputStream fileOutputStream = null;
		File internalDir = new File(con.getFilesDir() + "");
		File outputSource = new File(internalDir, "temp.xml");
		try {
			fileOutputStream = new FileOutputStream(outputSource);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		URL url = null;
		try {
			url = new URL("http://api.shoutcast.com/legacy/stationsearch?k=" + con.getString(R.string.shoutcast) + "&search=" + searchTerm);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		InputStream inputStream = null;
		try {
			assert url != null;
			inputStream = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int c;
		try {
			assert inputStream != null;
			while ((c = inputStream.read()) != -1) {
				assert fileOutputStream != null;
				fileOutputStream.write(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (outputSource.exists()) {
			try {
				XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
				XmlPullParser xmlPullParser = xppFactory.newPullParser();
				xmlPullParser.setInput(new FileInputStream(outputSource), "utf-8");

				String stationName;
				String genre, genre2, genre3;
				int bitRate;

				int eventType = xmlPullParser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
						case XmlPullParser.START_TAG:

							if (xmlPullParser.getName().equals("station")) {
								int size = xmlPullParser.getAttributeCount();
								stationName = "";
								bitRate = 0;
								genre = "";
								genre2 = "";
								genre3 = "";
								for (int i = 0; i < size; i++) {
									if (xmlPullParser.getAttributeName(i).equals("name")) {
										Log.w("name", xmlPullParser.getAttributeValue(i));
										stationName = xmlPullParser.getAttributeValue(i);
									}
									if (xmlPullParser.getAttributeName(i).equals("br")) {
										bitRate = Integer.parseInt(xmlPullParser
												.getAttributeValue(i));
										Log.w("BR", bitRate + "");
									}
									if (xmlPullParser.getAttributeName(i).equals("genre")) {
										genre = xmlPullParser.getAttributeValue(i);
									}
									if (xmlPullParser.getAttributeName(i).equals("genre2")) {
										genre2 = xmlPullParser.getAttributeValue(i);
									}
									if (xmlPullParser.getAttributeName(i).equals("genre3")) {
										genre3 = xmlPullParser.getAttributeValue(i);
									}
								}
								searchTable.add(new Radio(stationName, "", true, "fromShoutcast",
										bitRate, genre + "/" + genre2 + "/" + genre3));
							}

							break;
						default:
							break;
					}
					eventType = xmlPullParser.next();
				}

			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
		}
		return searchTable;
	}

	@Override
	protected void onPostExecute(final ArrayList<Radio> strings) {
		super.onPostExecute(strings);

		FunDapter<Radio> adapter;
		BindDictionary<Radio> bindDictionary = new BindDictionary<>();

		bindDictionary.addStringField(R.id.search_term_name, new StringExtractor<Radio>() {
			@Override
			public String getStringValue(Radio item, int position) {
				return item.getName();
			}
		});

		bindDictionary.addStringField(R.id.search_term_bitrate, new StringExtractor<Radio>() {
			@Override
			public String getStringValue(Radio item, int position) {
				return "Bitrate: " + Integer.toString(item.getBitRate());
			}
		});

		bindDictionary.addStringField(R.id.search_term_genre, new StringExtractor<Radio>() {
			@Override
			public String getStringValue(Radio item, int position) {
				return "Genre: " + item.getGenre();
			}
		});

		for (int i = 0; i < strings.size(); i++) {
			Log.w("name", strings.get(i).getName());
		}

		adapter = new FunDapter(con, strings, R.layout.search_terms_layout, bindDictionary);
		ListView listView = (ListView) view.findViewById(R.id.search_results_list_view);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onDialogPositiveClick(strings.get(position));
			}
		});
	}
}


