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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PlayingNowFragment extends Fragment implements SleepTimerDialog.NoticeDialogListener
{
	
	private ImageButton recordCurrentRadio;
	private ImageButton ibPPButton;
	private ImageButton ibSleepTimer;
	private TextView tvTimeRemaining;
	private ImageView ivRadio;
	private TextView tvRadioName;

	
	public PlayingNowFragment()
	{
		// Required empty public constructor
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_playing_now, container, false);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		
		ibPPButton = (ImageButton) getActivity().findViewById(R.id.ibPPPLayingNow);
		ibSleepTimer = (ImageButton) getActivity().findViewById(R.id.ibSleepTimer);
		recordCurrentRadio = (ImageButton) getActivity().findViewById(R.id.ibRecordCurrentRadio);
		ivRadio = (ImageView) getActivity().findViewById(R.id.ivPlayingNowExtended);
		tvRadioName = (TextView) getActivity().findViewById(R.id.tvPlayingNowName);
		tvTimeRemaining = (TextView) getActivity().findViewById(R.id.tvTimeRemaining);
		
		ivRadio.setImageResource(((MainActivity) getActivity()).getPlayerDrawable());
		tvRadioName.setText(((MainActivity) getActivity()).getPlayerName());
		ibSleepTimer.setImageResource(R.drawable.ic_snooze);
		
		recordCurrentRadio.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((MainActivity) getActivity()).recordCurrentRadio(-1);
			}
		});
		
		ibSleepTimer.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String text = tvTimeRemaining.getText().toString();
				if (text.equals("") || text.equals("-1") || text.equals("0"))
				{
					SleepTimerDialog sleepTimerDialog = new SleepTimerDialog();
					sleepTimerDialog.setTargetFragment(PlayingNowFragment.this, 2);
					sleepTimerDialog.show(getFragmentManager(), "rockets");
				}
				else
				{
					((MainActivity) getActivity()).tellServiceT("SLEEPTIMER", -1);
					setSleepText(0);
				}
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
	
	
	@Nullable
	@Override
	public View getView()
	{
		return super.getView();
	}
	
	@Override
	public void setMenuVisibility(final boolean visible)
	{
		super.setMenuVisibility(visible);
		if (visible)
		{
			setupPage();
		}
	}
	
	private void setupPage()
	{
		ivRadio.setImageResource(((MainActivity) getActivity()).getPlayerDrawable());
		tvRadioName.setText(((MainActivity) getActivity()).getPlayerName());
		Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
				((MainActivity)getActivity()).getPlayerDrawable());
        int h = getView().getHeight();
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, h-400, getDominantColor(icon),
                Color.parseColor
                ("#0f191e"), Shader.TileMode.CLAMP));
        mDrawable.setAlpha(250);
        getView().setBackgroundDrawable(mDrawable);
	}
	
	public int getDominantColor(Bitmap bitmap)
	{
		Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 3, 3, true);
		final int color = newBitmap.getPixel(1, 1);
		newBitmap.recycle();
		return color;
	}
	
	@Override
	public void onDialogPositiveClick(int minutes)
	{
		if (((MainActivity) getActivity()).getPlaying() != 0)
		{
			((MainActivity) getActivity()).tellServiceT("SLEEPTIMER", minutes);
			setSleepText(minutes);
		}
		else Toast.makeText(getContext(), "Player is already stopped", Toast.LENGTH_SHORT).show();
	}
	
	public void setSleepText(int time)
	{
		if (time == 0 || time == -1)
		{
			tvTimeRemaining.setText("");
		}
		else tvTimeRemaining.setText(time + " min");
	}
}

