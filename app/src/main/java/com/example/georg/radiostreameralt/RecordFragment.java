package com.example.georg.radiostreameralt;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
	private BottomBarTab recNowTab;
    private int recordings = 0;

    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("SIMPLE_RECORDING_ADDED")){
                recNowTab.setBadgeCount(recordings++);
            }
            else if(intent.getAction().equals("RECORDING_STOPPED")){
                recordings--;
                if(recordings==0){
                    recNowTab.removeBadge();
                }
                else recNowTab.setBadgeCount(recordings);
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


        if (serviceReceiver != null) {
            getActivity().registerReceiver(serviceReceiver, new IntentFilter("RECORDING_ADDED"));
            getActivity().registerReceiver(serviceReceiver, new IntentFilter("RECORDING_STOPPED"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);
	
		BottomBar bottomBar = (BottomBar) view.findViewById(R.id.bottomBar);
        recNowTab = bottomBar.getTabWithId(R.id.tab_recording_now);

        //First Page Immediate transaction without animation
        final FragmentManager manager = getFragmentManager();
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
                else if(tabId == R.id.tab_scheduled_recordings){
                    manager.beginTransaction()
                            //.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                            .replace(R.id.record_layout_for_fragments, schRecord).commit();
                }
                else if(tabId == R.id.tab_folder_recordings){
                    manager.beginTransaction()
                            //.setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                            .replace(R.id.record_layout_for_fragments, folderRecordings).commit();
                }
            }
        });

        return view;
    }

}
