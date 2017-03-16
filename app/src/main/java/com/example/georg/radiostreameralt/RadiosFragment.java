package com.example.georg.radiostreameralt;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.StaticImageLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RadiosFragment extends Fragment implements AddRadioDialog.NoticeDialogListener
{
	private FloatingActionButton fabAddRadio;
	private FunDapter adapter;
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_radios, container, false);

		
		ImageView ivLogo = (ImageView) view.findViewById(R.id.ivLogo);
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		fabAddRadio = (FloatingActionButton) view.findViewById(R.id.fabAddRadio);
		
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
				imageView.setBackgroundResource(item.getIcon());
			}
		});
		
		adapter = new FunDapter(getContext(), ((MainActivity)getActivity()).radiosList, R.layout
				.grid_view_layout,
				dictionary);
		
		final ListView radiosGrid = (ListView) view.findViewById(R.id.gridView);
		radiosGrid.setAdapter(adapter);
		registerForContextMenu(radiosGrid);
		
		radiosGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				((MainActivity)getActivity()).tellServiceP("PLAYER_PLAY",
						((MainActivity) getActivity()).radiosList.get(position).getUrl(),position);
				((MainActivity)getActivity()).disableButtons();
				((MainActivity)getActivity()).setFinger(position);
			}
		});

		return view;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		fabAddRadio.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AddRadioDialog addRadioDialog = new AddRadioDialog();
				addRadioDialog.setTargetFragment(RadiosFragment.this, 1);
				addRadioDialog.show(getFragmentManager(), "missiles");
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.gridView) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.folder_context_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case R.id.delete:
				((MainActivity)getActivity()).radiosList.remove(info.position);
				adapter.updateData(((MainActivity)getActivity()).radiosList);
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
	public void onDialogPositiveClick(String name, String url)
	{
		((MainActivity)getActivity()).radiosList.add(new Radio(name, url, R.drawable.ic_radio));
		adapter.updateData(((MainActivity)getActivity()).radiosList);
	}
}