package com.example.georg.radiostreameralt;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {

	private RecordingNow recordingNow;
	private SchRecord schRecord;
	private FolderRecordings folderRecordings;
	private FragmentManager manager;
	private BottomBarTab recNowTab;
	private BottomBar bottomBar;

	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("recList")){
				setBadgeCount(intent.getIntExtra("numberOfRecordings", -1));
			}
		}
	};


	public RecordFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		recordingNow = new RecordingNow();
		schRecord = new SchRecord();
		folderRecordings = new FolderRecordings();

		if(serviceReceiver!=null){
			getActivity().registerReceiver(serviceReceiver, new IntentFilter("recList"));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_record, container, false);

		bottomBar = (BottomBar) view.findViewById(R.id.bottomBar);
		recNowTab = bottomBar.getTabWithId(R.id.tab_recording_now);


		//First Page Immediate transaction without animation
		manager = getFragmentManager();
		manager.beginTransaction()
				.replace(R.id.record_layout_for_fragments, recordingNow).commit();


		bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
			@Override
			public void onTabSelected(@IdRes int tabId) {
				if (tabId == R.id.tab_recording_now) {
					manager.beginTransaction()
							//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
							.replace(R.id.record_layout_for_fragments, recordingNow).commit();
				}
				else if (tabId == R.id.tab_scheduled_recordings) {
					manager.beginTransaction()
							//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
							.replace(R.id.record_layout_for_fragments, schRecord).commit();
				}
				else if (tabId == R.id.tab_folder_recordings) {
					folderTransaction();
				}
			}
		});


		return view;
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(serviceReceiver);
		super.onDestroy();
	}

	public void folderTransaction(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
					PackageManager.PERMISSION_GRANTED)
			{
				Log.w("Folder", "Permission (Read external storage) already granted");
				manager.beginTransaction()
						//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
						.replace(R.id.record_layout_for_fragments, folderRecordings).commit();
			}
			else
			{
				String permissionRequested[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
				requestPermissions(permissionRequested, 13);
			}
		}
		else
		{
			manager.beginTransaction()
					//.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
					.replace(R.id.record_layout_for_fragments, folderRecordings).commit();
		}
	}

	public void setBadgeCount(int recordings){
		if(recordings==0){
			recNowTab.removeBadge();
		}
		else recNowTab.setBadgeCount(recordings);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 13){
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				Log.w("Folder", "Permission (Read external storage) just granted");
				folderTransaction();
			}
			else
			{
				Log.w("Folder", "Permission (Read external storage) denied");
				bottomBar.selectTabWithId(R.id.tab_recording_now);
			}
		}
	}

}
