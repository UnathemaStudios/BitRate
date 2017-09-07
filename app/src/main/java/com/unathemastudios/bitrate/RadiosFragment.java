package com.unathemastudios.bitrate;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.amigold.fundapter.interfaces.StaticImageLoader;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getbase.floatingactionbutton.FloatingActionButton;


import static android.view.View.*;

public class RadiosFragment extends Fragment implements AddRadioDialog.NoticeDialogListener,
		SearchShoutcastDialog.NoticeDialogListener, ChooseLinkDialog.NoticeDialogListener, ConfirmationDialog.NoticeDialogListener
{
	private FloatingActionsMenu fabAddRadio;
	private FloatingActionButton addCustom;
	private FloatingActionButton addShoutcast;
	private FunDapter adapter;
	private SharedPreferences pref;
	private Radio tempRadio;
	private SearchShoutcastDialog searchShoutcastDialog = null;
	private ChooseLinkDialog chooseLinkDialog = null;

	public RadiosFragment()
	{
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_radios, container, false);
		pref = getActivity().getPreferences(Context.MODE_PRIVATE);

		fabAddRadio = (FloatingActionsMenu) view.findViewById(R.id.fabAddRadio);
		addCustom = (FloatingActionButton) view.findViewById(R.id.fab_add_custom);
		addShoutcast = (FloatingActionButton) view.findViewById(R.id.fab_add_shoutcast);
		addCustom.setVisibility(GONE);
		addShoutcast.setVisibility(GONE);

		addCustom.setTitle("Add Custom Station");
		addShoutcast.setTitle("Import Station from Shoutcast.com");

		BindDictionary<Radio> dictionary = new BindDictionary<>();
		
		dictionary.addStringField(R.id.tvName, new StringExtractor<Radio>()
		{
			@Override
			public String getStringValue(Radio item, int position)
			{
				return item.getName();
			}
		});
		
		dictionary.addStaticImageField(R.id.ivLogo, new StaticImageLoader<Radio>()
		{
			@Override
			public void loadImage(Radio item, ImageView imageView, int position)
			{
				imageView.setImageResource(getResources().getIdentifier(item.getLogo(),"raw",getContext().getPackageName()));
			}
		});
		dictionary.addStaticImageField(R.id.radioInfo, new StaticImageLoader() {
			@Override
			public void loadImage(Object item, ImageView imageView, int position) {
				imageView.setImageResource(R.drawable.ic_info_black);
			}
		}).onClick(new ItemClickListener<Radio>() {
			@Override
			public void onClick(Radio item, int position, View view) {
				InfoDialog infoDialog = new InfoDialog();
				infoDialog.show(getFragmentManager(), "bazooka", item.getName(), item.getDescription());
			}
		});
		
		adapter = new FunDapter(getContext(), ((MainActivity) getActivity()).radiosList, R.layout.grid_view_layout, dictionary);
		
		final ListView radiosGrid = (ListView) view.findViewById(R.id.gridView);
		radiosGrid.setAdapter(adapter);
		registerForContextMenu(radiosGrid);
		
		radiosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				((MainActivity) getActivity()).tellServiceP("PLAYER_PLAY", ((MainActivity) getActivity()).radiosList.get(position).getUrl(), position);
				((MainActivity) getActivity()).disableButtons();
				((MainActivity) getActivity()).setFinger(position);
				SharedPreferences.Editor editor = pref.edit();
				editor.putInt("lastfinger", position);
				editor.apply();
			}
		});
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		fabAddRadio.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
			@Override
			public void onMenuExpanded() {
				FabVisibilityTask fabVisibilityTask = new FabVisibilityTask(addCustom,
						addShoutcast, true);
				fabVisibilityTask.execute();
			}

			@Override
			public void onMenuCollapsed() {
				FabVisibilityTask fabVisibilityTask = new FabVisibilityTask(addCustom,
						addShoutcast, false);
				fabVisibilityTask.execute();
			}
		});

		addCustom.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AddRadioDialog addRadioDialog = new AddRadioDialog();
				addRadioDialog.setTargetFragment(RadiosFragment.this, 1);
				addRadioDialog.show(getFragmentManager(), "missiles");
				fabAddRadio.collapse();
			}
		});

		addShoutcast.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(checkNetworkConnection()==1||checkNetworkConnection()==2) {
					searchShoutcastDialog = new SearchShoutcastDialog();
					searchShoutcastDialog.setTargetFragment(RadiosFragment.this, 20);
					searchShoutcastDialog.show(getFragmentManager(), "teaser");
					fabAddRadio.collapse();
				}
				else{
					Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
					fabAddRadio.collapse();
				}
			}
		});


		if(((MainActivity)getActivity()).radiosList.isEmpty()){
			getActivity().findViewById(R.id.noStations).setVisibility(VISIBLE);
		}
		else getActivity().findViewById(R.id.noStations).setVisibility(GONE);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.gridView)
		{
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.radios_context_menu, menu);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId())
		{
			case R.id.delete_radios:
				if (((MainActivity)getActivity()).radiosList.get(info.position).isMadeByUser())
				{
					
					if (!((MainActivity)getActivity()).radiosList.get(info.position).isRecorded())
					{
						ConfirmationDialog confirmationDialog = new ConfirmationDialog();
						confirmationDialog.setTargetFragment(RadiosFragment.this, 90);
						confirmationDialog.show(getFragmentManager(), "sushi", info.position);
					}
					else Toast.makeText(getContext(),"You cannot delete a station that is currently being recorded.", Toast.LENGTH_LONG).show();
				}
				else Toast.makeText(getContext(),"You cannot delete prebuilt stations.", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.edit_radios:
				if (((MainActivity)getActivity()).radiosList.get(info.position).isMadeByUser()) {
					AddRadioDialog addRadioDialog = new AddRadioDialog();
					addRadioDialog.setTargetFragment(RadiosFragment.this, 224);
					addRadioDialog.show(getFragmentManager(), "potato", ((MainActivity) getActivity()).radiosList.get(info.position), true, info.position);
				}
				else Toast.makeText(getContext(),"You cannot edit prebuilt stations.", Toast.LENGTH_LONG).show();
				
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override //Custom add
	public void onDialogPositiveClick(String name, String url, String description)
	{
		boolean found = false;
		for(Radio item : ((MainActivity)getActivity()).radiosList){
			if(item.getName().equals(name)) found = true;
		}

		if(!found) {
			((MainActivity) getActivity()).radiosList.add(new Radio(name, url, "defaultradio",
					true, description));
			if(((MainActivity)getActivity()).radiosList.isEmpty()){
				getActivity().findViewById(R.id.noStations).setVisibility(VISIBLE);
				}
			else getActivity().findViewById(R.id.noStations).setVisibility(GONE);
			adapter.updateData(((MainActivity) getActivity()).radiosList);
			((MainActivity)getActivity()).loadUserRadiosToXML();
		}
		else Toast.makeText(getContext(),name + " already exists", Toast.LENGTH_SHORT).show();
		
	}
	
	@Override //Edit
	public void onDialogPositiveClick(String name, String url, String description, int pos) {
		((MainActivity)getActivity()).radiosList.get(pos).setName(name);
		((MainActivity)getActivity()).radiosList.get(pos).setUrl(url);
		((MainActivity)getActivity()).radiosList.get(pos).setDescription(description);
		adapter.updateData(((MainActivity)getActivity()).radiosList);
	}
	
	@Override // Add from shoutcast
	public void onDialogPositiveClick(String name, String url, String description, boolean fromShoutcast) {
		boolean found = false;
		for(Radio item : ((MainActivity)getActivity()).radiosList){
			if(item.getName().equals(name)) found = true;
		}
		
		if(!found) {
			//Add and play radio
			Radio cRadio = new Radio(name, url, "shoutcast_logo", true, description);
			((MainActivity) getActivity()).radiosList.add(cRadio);
			((MainActivity) getActivity()).tellServiceP("PLAYER_PLAY", cRadio.getUrl(), (
					(MainActivity)getActivity()).radiosList.size()-1);
			((MainActivity) getActivity()).disableButtons();
			((MainActivity) getActivity()).setFinger(((MainActivity) getActivity()).radiosList.size()-1);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("lastfinger", ((MainActivity)getActivity()).radiosList.size()-1);
			editor.apply();
			((MainActivity)getActivity()).pageSelector(0);

			if(((MainActivity)getActivity()).radiosList.isEmpty()){
				getActivity().findViewById(R.id.noStations).setVisibility(VISIBLE);
			}
			else getActivity().findViewById(R.id.noStations).setVisibility(GONE);
			adapter.updateData(((MainActivity) getActivity()).radiosList);
			((MainActivity)getActivity()).loadUserRadiosToXML();
		}
		else Toast.makeText(getContext(),name + " already exists", Toast.LENGTH_SHORT).show();

		if(chooseLinkDialog!=null) chooseLinkDialog.close();
		if(searchShoutcastDialog!=null) searchShoutcastDialog.close();
	}
	
	@Override // From SearchShoutcastDialog
	public void onDialogPositiveClick(Radio radio) {
		tempRadio = radio;
		chooseLinkDialog = new ChooseLinkDialog();
		chooseLinkDialog.setTargetFragment(RadiosFragment.this, 25);
		chooseLinkDialog.show(getFragmentManager(), "rice", radio.getId());
	}

	@Override // From Choose link Dialog
	public void onDialogPositiveClick(String url) {
		Radio radio = new Radio(tempRadio.getName(), url, true, "Genre: " + tempRadio.getGenre()
				+ "\nBitrate: " + tempRadio
				.getBitRate(),
				tempRadio.getBitRate(), tempRadio.getGenre());
		AddRadioDialog addRadioDialog = new AddRadioDialog();
		addRadioDialog.setTargetFragment(RadiosFragment.this, 10);
		addRadioDialog.show(getFragmentManager(), "oil", radio, true);
	}
	
	
	@Override // Confirmation Dialog - Delete
	public void onDialogPositiveClick(int pos) {
		((MainActivity) getActivity()).radiosList.remove(pos);
		if (((MainActivity)getActivity()).finger == pos)
		{
			if (((MainActivity)getActivity()).isMyServiceRunning(MainService.class))
			{
				((MainActivity) getActivity()).tellServicePF("SET_SERVICE_FINGER", -1);
			}
			((MainActivity)getActivity()).setFinger(-1);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("lastfinger",-1);
			editor.apply();
		}
		else if (((MainActivity)getActivity()).finger > pos)
		{
			((MainActivity)getActivity()).finger --;
			if (((MainActivity)getActivity()).isMyServiceRunning(MainService.class))
			{
				((MainActivity) getActivity()).tellServicePF("SET_SERVICE_FINGER", ((MainActivity) getActivity()).finger);
			}
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt("lastfinger",((MainActivity)getActivity()).finger);
			editor.apply();
		}
		if(((MainActivity)getActivity()).radiosList.isEmpty()){
			getActivity().findViewById(R.id.noStations).setVisibility(VISIBLE);
		}
		else getActivity().findViewById(R.id.noStations).setVisibility(GONE);
		adapter.updateData(((MainActivity) getActivity()).radiosList);
		((MainActivity)getActivity()).loadUserRadiosToXML();
	}

	private int checkNetworkConnection() {
		boolean wifiConnected;
		boolean mobileConnected;
		ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context
				.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
			if (wifiConnected) {
				return 1;
			} else if (mobileConnected) {
				return 2;
			}
		} else {
			return 0;
		}
		return -1;
	}
}

class FabVisibilityTask extends AsyncTask<Void, Void, Void> {

	private FloatingActionButton fabButton1, fabButton2;
	private boolean open;

	public FabVisibilityTask(FloatingActionButton fabButton1, FloatingActionButton fabButton2,
							 boolean open){
		this.fabButton1 = fabButton1;
		this.fabButton2 = fabButton2;
		this.open = open;
	}

	@Override
	protected Void doInBackground(Void... params) {

		SystemClock.sleep(60);

		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		super.onPostExecute(aVoid);

		if(open) {
			fabButton1.setVisibility(View.VISIBLE);
			fabButton2.setVisibility(View.VISIBLE);
		}
		else{
			fabButton1.setVisibility(View.GONE);
			fabButton2.setVisibility(View.GONE);
		}
	}
}