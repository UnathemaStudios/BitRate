package com.example.georg.radiostreameralt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.vstechlab.easyfonts.EasyFonts;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {

    RecordingNow recordingNow;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recordingNow = new RecordingNow();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        final ImageButton btRecordNow = (ImageButton) view.findViewById(R.id.record_now_button);
        final ImageButton btSchedule = (ImageButton)view.findViewById(R.id.schedule_button);
        final ImageButton btFolder = (ImageButton)view.findViewById(R.id.folder_button);
        //First Page Immidiate transaction without animation
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .replace(R.id.record_layout_for_fragments, recordingNow).commit();
        btRecordNow.setSelected(true);
        btRecordNow.setEnabled(false);

        btRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                        .replace(R.id.record_layout_for_fragments, recordingNow).commit();
                btRecordNow.setSelected(true);
                btRecordNow.setEnabled(false);
                btFolder.setSelected(false);
                btFolder.setEnabled(true);
                btSchedule.setSelected(false);
                btSchedule.setEnabled(true);
            }
        });

        btSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchRecord schRecord = new SchRecord();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                        .replace(R.id.record_layout_for_fragments, schRecord).commit();
                btRecordNow.setSelected(false);
                btRecordNow.setEnabled(true);
                btFolder.setSelected(false);
                btFolder.setEnabled(true);
                btSchedule.setSelected(true);
                btSchedule.setEnabled(false);
            }
        });

        btFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderRecordings folderRecordings = new FolderRecordings();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                        .replace(R.id.record_layout_for_fragments, folderRecordings).commit();
                btRecordNow.setSelected(false);
                btRecordNow.setEnabled(true);
                btFolder.setSelected(true);
                btFolder.setEnabled(false);
                btSchedule.setSelected(false);
                btSchedule.setEnabled(true);
            }
        });


        return view;
    }

}
