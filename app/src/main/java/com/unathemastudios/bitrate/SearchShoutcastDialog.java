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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class SearchShoutcastDialog extends DialogFragment {
	
	public SearchShoutcastDialog.NoticeDialogListener mListener;
	private ImageButton ibClose;
	private ArrayList<Radio> searchTable;
	private EditText etTerm;
	private TextView tvGenre;
	private String searchTerm;
	private Button btnPrv, btnNxt, btnGenres;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		LayoutInflater factory = LayoutInflater.from(getContext());
		final View textEntryView = factory.inflate(R.layout.search_shoutcast_dialog, null);
		ibClose = (ImageButton) textEntryView.findViewById(R.id.ibCloseSearchDialog);
		etTerm = (EditText) textEntryView.findViewById(R.id.etSearchTerm);
		btnPrv = (Button) textEntryView.findViewById(R.id.btnPrevious);
		btnNxt = (Button) textEntryView.findViewById(R.id.btnNext);
		tvGenre = (TextView) textEntryView.findViewById(R.id.tv_search_by_genre);
		textEntryView.findViewById(R.id.loadingStationsLayout).setVisibility(View.GONE);
		textEntryView.findViewById(R.id.no_stations_found).setVisibility(View.GONE);
		
		btnNxt.setVisibility(View.GONE);
		btnPrv.setVisibility(View.GONE);
		searchTable = new ArrayList<>();
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(textEntryView);
		// Create the AlertDialog object and return it
		
		etTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchTerm = etTerm.getText().toString().replace(" ", "+");
					SearchByName searchByName = new SearchByName(getContext(), textEntryView,
							searchTerm, mListener, 0);
					searchByName.execute();
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(textEntryView.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
		
		etTerm.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					searchTerm = etTerm.getText().toString().replace(" ", "+");
					SearchByName searchByName = new SearchByName(getContext(), textEntryView,
							searchTerm, mListener, 0);
					searchByName.execute();
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(textEntryView.getWindowToken(), 0);
				}
				return false;
			}
		});
		
		
		ibClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchShoutcastDialog.this.getDialog().cancel();
			}
		});
		
		tvGenre.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BrowseByPrimaryGenre browseByPrimaryGenre = new BrowseByPrimaryGenre(getContext(), textEntryView, mListener);
				browseByPrimaryGenre.execute();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(textEntryView.getWindowToken(), 0);
			}
		});
		
		return builder.create();
	}
	
	public void close() {
		SearchShoutcastDialog.this.dismiss();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
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
	
	// Use this instance of the interface to deliver action event
	
	@Override
	public void show(FragmentManager manager, String tag) {
		super.show(manager, tag);
	}
	
	
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(Radio radio);
	}
}

class Genre {
	private String name;
	private boolean hasChildren;
	private int id;
	
	public Genre(String name, int id, boolean haschildren) {
		this.name = name;
		this.id = id;
		this.hasChildren = haschildren;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isHasChildren() {
		return hasChildren;
	}
	
	public int getId() {
		return id;
	}
}

class SearchByName extends AsyncTask<Void, Void, ArrayList<Radio>> {
	
	private Context con;
	private String searchTerm;
	private View view;
	private SearchShoutcastDialog.NoticeDialogListener mListener;
	private int pageNumber;
	private boolean hasNext;
	
	
	SearchByName(Context context, View view, String searchTerm, SearchShoutcastDialog
			.NoticeDialogListener mListener, int pageNumber) {
		this.con = context;
		this.view = view;
		this.searchTerm = searchTerm;
		this.mListener = mListener;
		this.pageNumber = pageNumber;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.GONE);
		view.findViewById(R.id.btnNext).setVisibility(View.GONE);
		view.findViewById(R.id.btnPrevious).setVisibility(View.GONE);
		view.findViewById(R.id.no_stations_found).setVisibility(View.GONE);
	}
	
	@Override
	protected ArrayList<Radio> doInBackground(Void... params) {
		
		ArrayList<Radio> searchTable = new ArrayList<>();
		
		URLConnection urlConnection = null;
		try {
			urlConnection = new URL("http://api.shoutcast.com/legacy/stationsearch?k=" + con.getString(R.string.shoutcast) + "&search=" + searchTerm + "&limit=" + pageNumber*20  + "," + 21).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = xppFactory.newPullParser();
			assert urlConnection != null;
			xmlPullParser.setInput(urlConnection.getInputStream(), "utf-8");
			
			String stationName,id;
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
							id = "";
							for (int i = 0; i < size; i++) {
								if (xmlPullParser.getAttributeName(i).equals("name")) {
									stationName = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("br")) {
									bitRate = Integer.parseInt(xmlPullParser
											.getAttributeValue(i));
								}
								if (xmlPullParser.getAttributeName(i).equals("genre")) {
									genre = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("genre2")) {
									genre2 = " / "+ xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("genre3")) {
									genre3 = " / " + xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("id")) {
									id = xmlPullParser.getAttributeValue(i);
								}
							}
							searchTable.add(new Radio(stationName, "", true, "", bitRate, genre + genre2 + genre3, id));
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
		
		if(searchTable.size() == 21)
		{
			hasNext = true;
			searchTable.remove(20);
		}
		else hasNext = false;
		
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
		
		adapter = new FunDapter(con, strings, R.layout.search_terms_layout, bindDictionary);
		ListView listView = (ListView) view.findViewById(R.id.search_results_list_view);
		listView.setAdapter(null);
		if(strings.size()!=0) {
			listView.setAdapter(adapter);
		}
		else{
			view.findViewById(R.id.no_stations_found).setVisibility(View.VISIBLE);
		}
		view.findViewById(R.id.search_results_list_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.GONE);
		Button btnPrv, btnNxt;
		btnPrv = (Button) view.findViewById(R.id.btnPrevious);
		btnNxt = (Button) view.findViewById(R.id.btnNext);
		
		btnNxt.setVisibility(View.VISIBLE);
		btnPrv.setVisibility(View.VISIBLE);
		
		btnNxt.setEnabled(hasNext);
		btnPrv.setEnabled(pageNumber!=0);
		
		btnNxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchByName searchByName = new SearchByName(con, view, searchTerm, mListener, pageNumber+1);
				searchByName.execute();
			}
		});
		
		btnPrv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchByName searchByName = new SearchByName(con, view, searchTerm, mListener, pageNumber-1);
				searchByName.execute();
			}
		});
		
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onDialogPositiveClick(strings.get(position));
			}
		});
	}
}

class BrowseByPrimaryGenre extends AsyncTask<Void, Void, ArrayList<Genre>> {
	
	Context con;
	View view;
	SearchShoutcastDialog.NoticeDialogListener mListener = null;
	
	public BrowseByPrimaryGenre(Context context, View view, SearchShoutcastDialog.NoticeDialogListener mListener) {
		this.con = context;
		this.view = view;
		this.mListener = mListener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.GONE);
		view.findViewById(R.id.btnNext).setVisibility(View.GONE);
		view.findViewById(R.id.btnPrevious).setVisibility(View.GONE);
		view.findViewById(R.id.no_stations_found).setVisibility(View.GONE);

	}
	
	@Override
	protected ArrayList<Genre> doInBackground(Void... params) {
		
		ArrayList<Genre> genresTable = new ArrayList<>();
		
		URLConnection urlConnection = null;
		
		try {
			urlConnection = new URL("http://api.shoutcast.com/genre/primary?k=" + con.getString(R.string.shoutcast) + "&f=xml").openConnection();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = xppFactory.newPullParser();
			assert urlConnection != null;
			xmlPullParser.setInput(urlConnection.getInputStream(), "utf-8");
			
			String genreName = "";
			int id = -1;
			boolean hasChild = false;
			
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						
						if (xmlPullParser.getName().equals("genre")) {
							int size = xmlPullParser.getAttributeCount();
							for (int i = 0; i < size; i++) {
								if (xmlPullParser.getAttributeName(i).equals("name")) {
									genreName = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("id")) {
									id = Integer.parseInt(xmlPullParser.getAttributeValue(i));
								}
								if (xmlPullParser.getAttributeName(i).equals("haschildren")) {
									hasChild = Boolean.parseBoolean(xmlPullParser.getAttributeValue(i));
								}
							}
							genresTable.add(new Genre(genreName, id, hasChild));
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
		
		
		return genresTable;
	}
	
	@Override
	protected void onPostExecute(final ArrayList<Genre> genres) {
		super.onPostExecute(genres);
		
		FunDapter<Genre> adapter;
		BindDictionary<Genre>bindDictionary = new BindDictionary<>();
		
		bindDictionary.addStringField(R.id.genre_name, new StringExtractor<Genre>() {
			@Override
			public String getStringValue(Genre item, int position) {
				return item.getName();
			}
		});
		
		
		adapter = new FunDapter(con, genres, R.layout.genre_layout, bindDictionary);
		ListView listView = (ListView) view.findViewById(R.id.search_results_list_view);
		listView.setAdapter(adapter);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.GONE);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
				if(genres.get(position).isHasChildren()){
					BrowseBySecondaryGenre browseBySecondaryGenre = new BrowseBySecondaryGenre(con, view, genres.get(position).getId(), mListener);
					browseBySecondaryGenre.execute();
				}
				else{
					SearchByGenreID searchByGenreID = new SearchByGenreID(con, view, genres.get(position).getId(), mListener, 0);
					searchByGenreID.execute();
				}
			}
		});
		
	}
}

class BrowseBySecondaryGenre extends AsyncTask<Void, Void, ArrayList<Genre>> {
	
	Context con;
	View view;
	int genreID;
	SearchShoutcastDialog.NoticeDialogListener mListener = null;
	
	public BrowseBySecondaryGenre(Context context, View view, int genreID, SearchShoutcastDialog.NoticeDialogListener mListener) {
		this.con = context;
		this.view = view;
		this.genreID = genreID;
		this.mListener = mListener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.GONE);
		view.findViewById(R.id.btnNext).setVisibility(View.GONE);
		view.findViewById(R.id.btnPrevious).setVisibility(View.GONE);
		view.findViewById(R.id.no_stations_found).setVisibility(View.GONE);

	}
	
	@Override
	protected ArrayList<Genre> doInBackground(Void... params) {
		
		ArrayList<Genre> genresTable = new ArrayList<>();
		
		URLConnection urlConnection = null;
		
		try {
			urlConnection = new URL("http://api.shoutcast.com/genre/secondary?parentid=" + genreID + "&k=" + con.getString(R.string.shoutcast) + "&f=xml ").openConnection();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = xppFactory.newPullParser();
			assert urlConnection != null;
			xmlPullParser.setInput(urlConnection.getInputStream(), "utf-8");
			
			String genreName = "";
			int id = -1;
			boolean hasChild = false;
			
			
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						
						if (xmlPullParser.getName().equals("genre")) {
							int size = xmlPullParser.getAttributeCount();
							for (int i = 0; i < size; i++) {
								if (xmlPullParser.getAttributeName(i).equals("name")) {
									genreName = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("id")) {
									id = Integer.parseInt(xmlPullParser.getAttributeValue(i));
								}
								if (xmlPullParser.getAttributeName(i).equals("haschildren")) {
									hasChild = Boolean.parseBoolean(xmlPullParser.getAttributeValue(i));
								}
							}
							if(!hasChild) {
								genresTable.add(new Genre(genreName, id, false));
							}
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
		
		
		return genresTable;
	}
	
	@Override
	protected void onPostExecute(final ArrayList<Genre> genres) {
		super.onPostExecute(genres);
		
		FunDapter<Genre> adapter;
		BindDictionary<Genre>bindDictionary = new BindDictionary<>();
		
		bindDictionary.addStringField(R.id.genre_name, new StringExtractor<Genre>() {
			@Override
			public String getStringValue(Genre item, int position) {
				return item.getName();
			}
		});
		
		
		adapter = new FunDapter(con, genres, R.layout.genre_layout, bindDictionary);
		ListView listView = (ListView) view.findViewById(R.id.search_results_list_view);
		listView.setAdapter(adapter);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.GONE);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
				SearchByGenreID searchByGenreID = new SearchByGenreID(con, view, genres.get(position).getId(), mListener, 0);
				searchByGenreID.execute();
			}
		});
	}
}

class SearchByGenreID extends AsyncTask<Void, Void, ArrayList<Radio>> {
	
	private Context con;
	private int genreID;
	private View view;
	private SearchShoutcastDialog.NoticeDialogListener mListener;
	private int pageNumber;
	private boolean hasNext;
	
	
	SearchByGenreID(Context context, View view, int genreID, SearchShoutcastDialog
			.NoticeDialogListener mListener, int pageNumber) {
		this.con = context;
		this.view = view;
		this.genreID = genreID;
		this.mListener = mListener;
		this.pageNumber = pageNumber;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.VISIBLE);
		view.findViewById(R.id.search_results_list_view).setVisibility(View.GONE);
		view.findViewById(R.id.btnNext).setVisibility(View.GONE);
		view.findViewById(R.id.btnPrevious).setVisibility(View.GONE);
		view.findViewById(R.id.no_stations_found).setVisibility(View.GONE);

	}
	
	@Override
	protected ArrayList<Radio> doInBackground(Void... params) {
		
		ArrayList<Radio> searchTable = new ArrayList<>();
		
		URLConnection urlConnection = null;
		try {
			urlConnection = new URL("http://api.shoutcast.com/station/advancedsearch?genre_id=" + genreID + "&limit=" + pageNumber*20  + "," + 21 + "&f=xml&k=" + con.getString(R.string.shoutcast)).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = xppFactory.newPullParser();
			assert urlConnection != null;
			xmlPullParser.setInput(urlConnection.getInputStream(), "utf-8");
			
			String stationName,id;
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
							id = "";
							for (int i = 0; i < size; i++) {
								if (xmlPullParser.getAttributeName(i).equals("name")) {
									stationName = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("br")) {
									bitRate = Integer.parseInt(xmlPullParser
											.getAttributeValue(i));
								}
								if (xmlPullParser.getAttributeName(i).equals("genre")) {
									genre = xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("genre2")) {
									genre2 = " / "+ xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("genre3")) {
									genre3 = " / " + xmlPullParser.getAttributeValue(i);
								}
								if (xmlPullParser.getAttributeName(i).equals("id")) {
									id = xmlPullParser.getAttributeValue(i);
								}
							}
							searchTable.add(new Radio(stationName, "", true, "", bitRate, genre + genre2 + genre3, id));
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
		
		if(searchTable.size() == 21)
		{
			hasNext = true;
			searchTable.remove(20);
		}
		else hasNext = false;
		
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
		
		adapter = new FunDapter(con, strings, R.layout.search_terms_layout, bindDictionary);
		ListView listView = (ListView) view.findViewById(R.id.search_results_list_view);
		listView.setAdapter(null);
		if(strings.size()!=0) {
			listView.setAdapter(adapter);
		}
		else{
			view.findViewById(R.id.no_stations_found).setVisibility(View.VISIBLE);
		}
		view.findViewById(R.id.search_results_list_view).setVisibility(View.VISIBLE);
		view.findViewById(R.id.loadingStationsLayout).setVisibility(View.GONE);
		Button btnPrv, btnNxt;
		btnPrv = (Button) view.findViewById(R.id.btnPrevious);
		btnNxt = (Button) view.findViewById(R.id.btnNext);
		
		btnNxt.setVisibility(View.VISIBLE);
		btnPrv.setVisibility(View.VISIBLE);
		
		btnNxt.setEnabled(hasNext);
		btnPrv.setEnabled(pageNumber!=0);
		
		btnNxt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchByGenreID searchByGenreID = new SearchByGenreID(con, view, genreID, mListener, pageNumber+1);
				searchByGenreID.execute();
			}
		});
		
		btnPrv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchByGenreID searchByGenreID = new SearchByGenreID(con, view, genreID, mListener, pageNumber-1);
				searchByGenreID.execute();
			}
		});
		
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onDialogPositiveClick(strings.get(position));
			}
		});
	}
}

