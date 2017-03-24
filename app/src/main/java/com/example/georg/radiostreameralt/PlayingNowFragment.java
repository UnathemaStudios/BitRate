package com.example.georg.radiostreameralt;


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

public class PlayingNowFragment extends Fragment implements SleepTimerDialog.NoticeDialogListener
{
	private static final int STOPPED = 0;
	private static final int PLAYING = 2;
	private int playerStatus = STOPPED;
	private boolean visible;
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
		return inflater.inflate(R.layout.fragment_playing_now, container, false);
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

		ibPPButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(playerStatus==PLAYING){
					((MainActivity)getActivity()).stop();
				}
				else ((MainActivity)getActivity()).play();
			}
		});
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
			this.visible = visible;
			setupPage();
		}
		else this.visible = false;
	}
	
	private void setupPage()
	{
		ivRadio.setImageResource(((MainActivity) getActivity()).getPlayerDrawable());
		tvRadioName.setText(((MainActivity) getActivity()).getPlayerName());
		setPPButtonDrawable();

		//-//--/--/--/--/BackGround/--/--/--/--//-//
		Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
				((MainActivity)getActivity()).getPlayerDrawable());
        int h = getView().getHeight();
        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setShader(new LinearGradient(0, 0, 200, h-600,
				manipulateColor(getDominantColor(icon),0.5f),
				Color.parseColor("#0f191e"),
				Shader.TileMode.CLAMP));
        mDrawable.setAlpha(255);
        getView().setBackgroundDrawable(mDrawable);
	}
	
	public static int manipulateColor(int color, float factor) {
		int a = Color.alpha(color);
		int r = Math.round(Color.red(color) * factor);
		int g = Math.round(Color.green(color) * factor);
		int b = Math.round(Color.blue(color) * factor);
		return Color.argb(a,
				Math.min(r,255),
				Math.min(g,255),
				Math.min(b,255));
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

	public void setPPButtonStatus(int playerStatus){
		this.playerStatus = playerStatus;
		this.disableButtons(false);
		if(visible) {
			this.setPPButtonDrawable();
		}
	}

	public void disableButtons(boolean state){
		ibSleepTimer.setEnabled(!state);
		ibPPButton.setEnabled(!state);
		recordCurrentRadio.setEnabled(!state);
	}

	private void setPPButtonDrawable(){
		if(playerStatus==PLAYING){
			ibPPButton.setImageResource(R.drawable.ic_stop);
		}
		else ibPPButton.setImageResource(R.drawable.ic_play_circle_outline);

	}
}

