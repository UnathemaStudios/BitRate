package com.unathemastudios.bitrate;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlarmFragment extends Fragment {
	
	private SchAlarm schAlarm;
	private SchRecord schRecord;
	private BottomBar bottomBar;
	private FragmentManager manager;
	
	
	public AlarmFragment() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		schAlarm = new SchAlarm();
		schRecord = new SchRecord();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_alarm, container, false);
		
		bottomBar = (BottomBar) view.findViewById(R.id.bottomBarAlarm);
		
		//First Page Immediate transaction without animation
		manager = getFragmentManager();
		manager.beginTransaction()
				.replace(R.id.alarm_layout_for_fragments, schAlarm).commit();
		
		bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
			@Override
			public void onTabSelected(@IdRes int tabId) {
				if (tabId == R.id.tab_alarm) {
					manager.beginTransaction()
							//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
							.replace(R.id.alarm_layout_for_fragments, schAlarm).commit();
				}
				else if (tabId == R.id.tab_sch_record) {
					manager.beginTransaction()
							//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
							.replace(R.id.alarm_layout_for_fragments, schRecord).commit();
				}
			}
		});
		
		return view;
	}
	
}
