package com.example.georg.radiostreameralt;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
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

        ivRadio.setBackgroundResource(((MainActivity)getActivity()).getPlayerDrawable());
        tvRadioName.setText(((MainActivity)getActivity()).getPlayerName());

        recordCurrentRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("REC_CURRENT");
            }
        });


    }

    //send function to broadcast an action
    public void send(String actionToSend) {
        Intent intent = new Intent();
        intent.setAction(actionToSend);
        getActivity().sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            setupPage();
        }
    }

    private void setupPage(){
        ivRadio.setBackgroundResource(((MainActivity)getActivity()).getPlayerDrawable());
        tvRadioName.setText(((MainActivity)getActivity()).getPlayerName());
        /*Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
                ((MainActivity)getActivity()).getPlayerDrawable());
        int h = getView().getHeight();
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h, getDominantColor(icon), Color.parseColor
                ("#000000"), Shader.TileMode.REPEAT));
        getView().setBackgroundDrawable(mDrawable);*/
    }

    public int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }
}

