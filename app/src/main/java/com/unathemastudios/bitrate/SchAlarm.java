package com.unathemastudios.bitrate;


import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.BooleanExtractor;
import com.amigold.fundapter.extractors.StringExtractor;
import com.amigold.fundapter.interfaces.ItemClickListener;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class SchAlarm extends Fragment implements AlarmEventDialog.NoticeDialogListener {
	
	private TextView tvTime, tvDescription;
	private Switch aSwitch;
	private ImageView ibBin;
	private FunDapter<Alarm> funDapter;
	private FloatingActionButton fabCreateNew;

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
		fabCreateNew = (FloatingActionButton) view.findViewById(R.id.fabAddAlarmEvent);
		
		fabCreateNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmEventDialog alarmEventDialog = new AlarmEventDialog();
				alarmEventDialog.setTargetFragment(SchAlarm.this, 51);
				alarmEventDialog.show(getFragmentManager(), "EVENTUH?");
			}
		});
		
		BindDictionary<Alarm> bindDictionary = new BindDictionary<>();
		
		bindDictionary.addStringField(R.id.tvtime_for_alarm, new StringExtractor<Alarm>() {
			@Override
			public String getStringValue(Alarm item, int position) {
				Date date = new Date(item.getTimestamp());
				String string;
				if(item.isHasSpecificDate()){
					string = date.getDay() + "/" + date.getMonth() + "/" + (date.getYear() +
							1900) + " " +
							date.getHours() + ":" + date.getMinutes();
				}
				else string = date.getHours() + ":" + date.getMinutes();
				return string;
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
					item.cancelAlarm();
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
				String message = item.toggleState();
				if(!(message == null)){
					Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		funDapter = new FunDapter(getContext(), ((MainActivity)getActivity()).alarmList, R.layout.alarm_item_layout, bindDictionary);
		
		ListView listView = (ListView) view.findViewById(R.id.lv_for_alarm);
		listView.setAdapter(funDapter);
		
		return view;
	}

	@Override
	public void onDialogPositiveClick(Alarm alarm) {
		((MainActivity)getActivity()).radiosList.get(alarm.getFingerPosition()).addAlarm(alarm);
		((MainActivity)getActivity()).radiosList.get(alarm.getFingerPosition()).alarms.get(0).setContext(getContext());
		String message = ((MainActivity)getActivity()).radiosList.get(alarm.getFingerPosition()).alarms.get(0).toggleState();
		Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
		((MainActivity)getActivity()).alarmList.add(alarm);
		funDapter.updateData(((MainActivity)getActivity()).alarmList);
	}
}
