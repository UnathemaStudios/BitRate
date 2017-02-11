package com.example.georg.radiostreameralt;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayingNowFragment extends Fragment {

    private ImageButton recordCurrentRadio;
    private ImageButton ibPPButton;
    private ImageButton ibSleepTimer;
    private ImageView ivRadio;
    private TextView tvRadioName;
    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("PLAYING_NOW_STATUS")){
                ivRadio.setBackgroundResource(intent.getIntExtra("drawable", R.color.transparent));
                tvRadioName.setText(intent.getStringExtra("name"));
            }
        }
    };

    public PlayingNowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(serviceReceiver !=null){
            getActivity().registerReceiver(serviceReceiver, new IntentFilter
                    ("PLAYING_NOW_STATUS"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playing_now, container, false);
        send("PLAYING_NOW_UPDATE");
        Toast.makeText(getContext(), "onCreateView", Toast.LENGTH_SHORT).show();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ibPPButton = (ImageButton)getActivity().findViewById(R.id.ibPPPLayingNow);
        ibSleepTimer = (ImageButton)getActivity().findViewById(R.id.ibSleepTimer);
        recordCurrentRadio = (ImageButton)getActivity().findViewById(R.id.ibRecordCurrentRadio);
        ivRadio = (ImageView)getActivity().findViewById(R.id.ivPlayingNowExtended);
        tvRadioName = (TextView)getActivity().findViewById(R.id.tvPlayingNowName);

        send("PLAYING_NOW_UPDATE");

        recordCurrentRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("REC_CURRENT");
            }
        });


    }

    //send function to broadcast an action
    public void send(String actionToSend)
    {
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        getActivity().sendBroadcast(intent);
    }
}

