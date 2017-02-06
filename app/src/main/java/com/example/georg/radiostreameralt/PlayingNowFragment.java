package com.example.georg.radiostreameralt;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.example.georg.radiostreameralt.R.id.loadingLayout;
import static com.example.georg.radiostreameralt.R.id.tvDescription;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayingNowFragment extends Fragment {

    private ImageButton recordCurrentRadio;

    public PlayingNowFragment() {
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
        View view = inflater.inflate(R.layout.fragment_playing_now, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recordCurrentRadio = (ImageButton)getActivity().findViewById(R.id.ibRecordCurrentRadio);
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

