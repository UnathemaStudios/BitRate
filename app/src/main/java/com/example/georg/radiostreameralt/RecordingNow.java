package com.example.georg.radiostreameralt;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amigold.fundapter.BindDictionary;
import com.amigold.fundapter.FunDapter;
import com.amigold.fundapter.extractors.StringExtractor;
import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingNow extends Fragment {

    private ArrayList<RecordingRadio> recordingNowRadios = new ArrayList<>();
    private FunDapter adapter;
    private TextView tvrecordingNowRadio;
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("RECORDING_ADDED")){
                if(intent.getIntExtra("position", 998) == 0 ||(intent.getIntExtra("position",
                        747) == 2)){
                    recordingNowRadios.clear();
                }
                recordingNowRadios.add(new RecordingRadio(intent.getStringExtra("name"), intent
                        .getIntExtra("key", 200), intent.getLongExtra("time", -1), intent
                        .getIntExtra("size", -1)));
                if((intent.getIntExtra("position", 747) == 1)||(intent.getIntExtra("position",
                        747) == 2)) {
                    adapter.updateData(recordingNowRadios);
                }
            }
            else if(intent.getAction().equals("RECORDING_STOPPED")){
                int keyToDelete = intent.getIntExtra("key", -1);
                for(int i=0;i<recordingNowRadios.size();i++){
                    if(recordingNowRadios.get(i).getId()==keyToDelete) recordingNowRadios.remove(i);
                }
                adapter.updateData(recordingNowRadios);
            }
        }
    };

    public RecordingNow() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (serviceReceiver != null) {
            getActivity().registerReceiver(serviceReceiver, new IntentFilter("RECORDING_ADDED"));
            getActivity().registerReceiver(serviceReceiver, new IntentFilter("RECORDING_STOPPED"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recording_now, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvrecordingNowRadio = (TextView)getActivity().findViewById(R.id.tvRecordingNow);
        if (isMyServiceRunning(Recorder.class)) {
        }

        BindDictionary<RecordingRadio> dictionary =  new BindDictionary<>();
        dictionary.addStringField(R.id.tvRecordingNow, new StringExtractor<RecordingRadio>() {
            @Override
            public String getStringValue(RecordingRadio item, int position) {
                return item.getName();
            }
        });
        dictionary.addStringField(R.id.tvRecordingNowSize, new StringExtractor<RecordingRadio>() {
            @Override
            public String getStringValue(RecordingRadio item, int position) {
                return (Integer.toString(item.getSize()) + " KB ");
            }
        });
        dictionary.addStringField(R.id.tvRecordingNowTime, new StringExtractor<RecordingRadio>() {
            @Override
            public String getStringValue(RecordingRadio item, int position) {
                return (Long.toString(item.getTime())+ " s");
            }
        });


        adapter = new FunDapter<>(view.getContext(),recordingNowRadios, R.layout
                .recording_now_layout, dictionary );

        ListView listView = (ListView)getActivity().findViewById(R.id.recordingNowListView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                send("STOP", recordingNowRadios.get(position).getId());
            }
        });
    }

    //send function to broadcast an action
    public void send(String actionToSend)
    {
        Intent serviceIntent = new Intent(getContext(), Recorder.class);
        serviceIntent.putExtra("Action", actionToSend);
        getActivity().startService(serviceIntent);
    }
    public void send(String actionToSend, int key){
        Intent serviceIntent = new Intent(getContext(), Recorder.class);
        serviceIntent.putExtra("Action", actionToSend);
        serviceIntent.putExtra("key", key);
        getActivity().startService(serviceIntent);
    }
    //function to check if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context
                .ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}
