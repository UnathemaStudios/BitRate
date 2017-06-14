package com.unathemastudios.bitrate;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.unathemastudios.bitrate.bitrate.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchRecord extends Fragment {


    public SchRecord() {
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
        return inflater.inflate(R.layout.fragment_sch_record, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button buton = (Button)getActivity().findViewById(R.id.alarmManagerbutton);
        final AlarmManager manager = (AlarmManager)getContext().getSystemService(Context
                .ALARM_SERVICE);

        final Intent intent = new Intent(getContext(), MainService.class);
        intent.setAction("PLAYER_PLAY");
        intent.putExtra("url", "http://s10.voscast.com:9940/;");
        intent.putExtra("finger", 3);


        buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingIntent pIntent;



//                ((MainActivity)getActivity()).startService(intent);
                pIntent = PendingIntent.getService(getContext(), 1, intent, 0);

                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 10*1000, pIntent);

            }
        });

        Button disableButton = (Button)getActivity().findViewById(R.id.disableAlarm);
        disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent2 = new Intent(getContext(), MainService.class).setAction("PLAYER_PLAY");
//                intent2.putExtra("url", "http://s10.voscast.com:9940/;");

                PendingIntent displayIntent = PendingIntent.getService(getContext(), 1,new Intent
                                (getContext(), MainService.class).setAction("PLAYER_PLAY"),
                        PendingIntent.FLAG_NO_CREATE);
                manager.cancel(displayIntent);
//                displayIntent.cancel();
            }
        });
    }
}
