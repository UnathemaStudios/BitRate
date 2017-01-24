package com.example.georg.radiostreameralt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment {

    private RecordingNow recordingNow;
    private ImageButton btRecordNow;
    private ImageButton btSchedule;
    private ImageButton btFolder;
    private TextView tvButton1;
    private TextView tvButton2;
    private TextView tvButton3;

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

        btRecordNow = (ImageButton) view.findViewById(R.id.record_now_button);
        btSchedule = (ImageButton)view.findViewById(R.id.schedule_button);
        btFolder = (ImageButton)view.findViewById(R.id.folder_button);
        tvButton1 = (TextView)view.findViewById(R.id.tvButton1);
        tvButton2 = (TextView)view.findViewById(R.id.tvButton2);
        tvButton3 = (TextView)view.findViewById(R.id.tvButton3);
        //First Page Immidiate transaction without animation
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .replace(R.id.record_layout_for_fragments, recordingNow).commit();
        this.onSelectChanges(0);

        btRecordNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_left,R.anim.slide_out_from_left)
                        .replace(R.id.record_layout_for_fragments, recordingNow).commit();
                onSelectChanges(0);
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
                onSelectChanges(1);
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
                onSelectChanges(2);
            }
        });


        return view;
    }

    private void onSelectChanges (int button){
        switch (button){
            case 0:
                DrawableCompat.setTint(btRecordNow.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .colorAccent));
                btRecordNow.setEnabled(false);
                tvButton1.setBackgroundResource(R.color.colorAccent);
                DrawableCompat.setTint(btSchedule.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btSchedule.setEnabled(true);
                tvButton2.setBackgroundResource(R.color.transparent);
                DrawableCompat.setTint(btFolder.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btFolder.setEnabled(true);
                tvButton3.setBackgroundResource(R.color.transparent);
                break;
            case 1:
                DrawableCompat.setTint(btRecordNow.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btRecordNow.setEnabled(true);
                tvButton1.setBackgroundResource(R.color.transparent);
                DrawableCompat.setTint(btSchedule.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .colorAccent));
                btSchedule.setEnabled(false);
                tvButton2.setBackgroundResource(R.color.colorAccent);
                DrawableCompat.setTint(btFolder.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btFolder.setEnabled(true);
                tvButton3.setBackgroundResource(R.color.transparent);
                break;
            case 2:
                DrawableCompat.setTint(btRecordNow.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btRecordNow.setEnabled(true);
                tvButton1.setBackgroundResource(R.color.transparent);
                DrawableCompat.setTint(btSchedule.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .textColorPrimary));
                btSchedule.setEnabled(true);
                tvButton2.setBackgroundResource(R.color.transparent);
                DrawableCompat.setTint(btFolder.getDrawable(), ContextCompat.getColor
                        (RecordFragment.this.getContext(), R.color
                                .colorAccent));
                btFolder.setEnabled(false);
                tvButton3.setBackgroundResource(R.color.colorAccent);
                break;
        }
    }

}
