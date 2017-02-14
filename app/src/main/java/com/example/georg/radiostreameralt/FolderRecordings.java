package com.example.georg.radiostreameralt;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FolderRecordings extends Fragment {

    private ArrayList<String> recFiles;
    private TextView tvRecordingsName;
    private ListView lvFolderRecordings;

    public FolderRecordings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recFiles = new ArrayList<>();

        //Example
        recFiles.add("Example 1");
        recFiles.add("Example 2");
        recFiles.add("Example 3");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_folder_recordings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initializing the adapter and the dictionary
        tvRecordingsName = (TextView)getActivity().findViewById(R.id.tvFolderRecordingsName);
        lvFolderRecordings = (ListView)getActivity().findViewById(R.id.lvFolderRecordings);

        BindDictionary<String> dictionary = new BindDictionary<>();
        dictionary.addStringField(R.id.tvFolderRecordingsName, new StringExtractor<String>() {
            @Override
            public String getStringValue(String item, int position) {
                return item;
            }
        });

        FunDapter adapter = new FunDapter(getContext(), recFiles, R.layout.rec_files_layout,
                dictionary);
        lvFolderRecordings.setAdapter(adapter);

        //Setting onItemClickListener
        lvFolderRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //do something knowing the recFolder.get(position)
            }
        });
    }
}
