package com.unathemastudios.bitrate;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.BooleanExtractor;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import com.amigold.fundapter.interfaces.StaticImageLoader;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchAlarm extends Fragment {
	
	private TextView tvTime, tvDescription;
	private Switch aSwitch;
	private ImageView ibBin;
	private FunDapter<Alarm> funDapter;
	
	
	public SchAlarm() {
		// Required empty public constructor
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_sch_alarm, container, false);
		
		tvTime = (TextView) view.findViewById(R.id.tvtime_for_alarm);
		tvDescription = (TextView) view.findViewById(R.id.tvDescription_for_alarm);
		aSwitch = (Switch) view.findViewById(R.id.sw_for_alarm);
		ibBin = (ImageView) view.findViewById(R.id.ibBin_for_alarm);
		
		
		
		
		BindDictionary<Alarm> bindDictionary = new BindDictionary<>();
		
		bindDictionary.addStringField(R.id.tvtime_for_alarm, new StringExtractor<Alarm>() {
			@Override
			public String getStringValue(Alarm item, int position) {
				return item.getHour() + ":" + item.getMinute();
			}
		});
		
		bindDictionary.addStringField(R.id.tvDescription_for_alarm, new StringExtractor<Alarm>() {
			@Override
			public String getStringValue(Alarm item, int position) {
				return item.getAlarmRadio().getName();
			}
		});
		
		bindDictionary.addBaseField(R.id.ibBin_for_alarm).onClick(new ItemClickListener<Alarm>() {
			@Override
			public void onClick(Alarm item, int position, View view) {
				if(item.isActive()){
					//Stop Alarm
				}
				((MainActivity)getActivity()).alarmList.remove(position);
				funDapter.updateData(((MainActivity)getActivity()).alarmList);
			}
		});
		
		
		bindDictionary.addCheckableField(R.id.sw_for_alarm, new BooleanExtractor<Alarm>() {
			@Override
			public boolean getBooleanValue(Alarm item, int position) {
				return item.isActive();
			}
		}).onClick(new ItemClickListener<Alarm>() {
			@Override
			public void onClick(Alarm item, int position, View view) {
				item.setActive(!item.isActive());
			}
		});
		
		funDapter = new FunDapter(getContext(), ((MainActivity)getActivity()).alarmList, R.layout.alarm_item_layout, bindDictionary);
		
		ListView listView = (ListView) view.findViewById(R.id.lv_for_alarm);
		listView.setAdapter(funDapter);
		
		return view;
	}
	
}
