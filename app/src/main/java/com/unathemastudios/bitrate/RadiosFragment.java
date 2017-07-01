package com.unathemastudios.bitrate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.amigold.fundapter.interfaces.StaticImageLoader;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class RadiosFragment extends Fragment implements AddRadioDialog.NoticeDialogListener,
		SearchShoutcastDialog.NoticeDialogListener
{
	private FloatingActionsMenu fabAddRadio;
	private FloatingActionButton addCustom;
	private FloatingActionButton addShoutcast;
	private FunDapter adapter;
	private SharedPreferences pref;

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
		addCustom.setVisibility(View.GONE);
		addShoutcast.setVisibility(View.GONE);

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
				addCustom.setVisibility(View.VISIBLE);
				addShoutcast.setVisibility(View.VISIBLE);
			}

			@Override
			public void onMenuCollapsed() {
				addCustom.setVisibility(View.GONE);
				addShoutcast.setVisibility(View.GONE);
			}
		});

		addCustom.setOnClickListener(new View.OnClickListener()
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

		addShoutcast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SearchShoutcastDialog searchShoutcastDialog = new SearchShoutcastDialog();
				searchShoutcastDialog.show(getFragmentManager(), "taser");
				fabAddRadio.collapse();
			}
		});


		if(((MainActivity)getActivity()).radiosList.isEmpty()){
			getActivity().findViewById(R.id.noStations).setVisibility(View.VISIBLE);
		}
		else getActivity().findViewById(R.id.noStations).setVisibility(View.GONE);
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
						((MainActivity) getActivity()).radiosList.remove(info.position);
						if (((MainActivity)getActivity()).finger == info.position)
						{
							if (((MainActivity)getActivity()).isMyServiceRunning(MainService.class))
							{
								((MainActivity) getActivity()).tellServiceP("PLAYER_PLAY", "no url", -1);
								((MainActivity) getActivity()).tellServiceP("CLOSE");
							}
							((MainActivity)getActivity()).setFinger(-1);
							SharedPreferences.Editor editor = pref.edit();
							editor.putInt("lastfinger",-1);
							editor.apply();
						}
						else if (((MainActivity)getActivity()).finger > info.position)
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
							getActivity().findViewById(R.id.noStations).setVisibility(View.VISIBLE);
						}
						else getActivity().findViewById(R.id.noStations).setVisibility(View.GONE);
						adapter.updateData(((MainActivity) getActivity()).radiosList);
						((MainActivity)getActivity()).loadUserRadiosToXML();
					}
					else Toast.makeText(getContext(),"You cannot delete a station that is currently being recorded.", Toast.LENGTH_LONG).show();
				}
				else Toast.makeText(getContext(),"You cannot delete prebuilt stations.", Toast.LENGTH_SHORT).show();
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
	
	@Override
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
				getActivity().findViewById(R.id.noStations).setVisibility(View.VISIBLE);
				}
			else getActivity().findViewById(R.id.noStations).setVisibility(View.GONE);
			adapter.updateData(((MainActivity) getActivity()).radiosList);
			((MainActivity)getActivity()).loadUserRadiosToXML();
		}
		else Toast.makeText(getContext(),name + " already exists", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDialogPositiveClick(Radio radio) {
		AddRadioDialog addRadioDialog = new AddRadioDialog();
		addRadioDialog.setTargetFragment(RadiosFragment.this, 10);
		addRadioDialog.show(getFragmentManager(), "oil", radio);
	}
}
