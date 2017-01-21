package com.example.georg.radiostreameralt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {


    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        Button btRecordNow = (Button)view.findViewById(R.id.record_now_button);
        Button btSchedule = (Button)view.findViewById(R.id.schedule_button);
        Button btFolder = (Button)view.findViewById(R.id.folder_button);

        btRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordingNow recordingNow = new RecordingNow();
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                        .replace(R.id.record_layout_for_fragments, recordingNow).commit();
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
            }
        });


        return view;
    }

}
