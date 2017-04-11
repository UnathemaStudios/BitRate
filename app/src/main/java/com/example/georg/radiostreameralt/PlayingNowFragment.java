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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayingNowFragment extends Fragment implements SleepTimerDialog
		.NoticeDialogListener, RecordRadioDialog.NoticeDialogListener {
	private static final int STOPPED = 0;
	//	private static final int LOADING = 1;
	private static final int PLAYING = 2;
	private int playerStatus = STOPPED;
	private boolean visible;
	private boolean isRecorded;
	private ImageButton recordCurrentRadio;
	private ImageButton ibPPButton;
	private ImageButton ibSleepTimer;
	private TextView tvTimeRemaining;
	private ImageView ivRadio;
	private TextView tvRadioName;
	private TextView tvRadioMetadata;
	private TextView tvIsRecorded;

	private final Handler metadataHandler = new Handler();
	private Runnable metadataRunnable;

	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("metadataBroadcast")) {
				tvRadioMetadata.setText(intent.getStringExtra("streamTitle"));
			}
		}
	};

	public PlayingNowFragment() {
		// Required empty public constructor
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(serviceReceiver);
		metadataHandler.removeCallbacks(metadataRunnable);
		super.onDestroy();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_playing_now, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.w("PLAYING NOW FRAGMENT", "ON VIEW CREATED");

		ibPPButton = (ImageButton) getActivity().findViewById(R.id.ibPPPLayingNow);
		ibSleepTimer = (ImageButton) getActivity().findViewById(R.id.ibSleepTimer);
		recordCurrentRadio = (ImageButton) getActivity().findViewById(R.id.ibRecordCurrentRadio);
		ivRadio = (ImageView) getActivity().findViewById(R.id.ivPlayingNowExtended);
		tvRadioName = (TextView) getActivity().findViewById(R.id.tvPlayingNowName);
		tvTimeRemaining = (TextView) getActivity().findViewById(R.id.tvTimeRemaining);
		tvIsRecorded = (TextView) getActivity().findViewById(R.id.tvIsRecorded);
		tvRadioMetadata = (TextView) getActivity().findViewById(R.id.tvRadioMetadata);

		ivRadio.setImageResource(((MainActivity) getActivity()).getPlayerDrawable());
		tvRadioName.setText(((MainActivity) getActivity()).getPlayerName());
		ibSleepTimer.setImageResource(R.drawable.ic_snooze);


		if (serviceReceiver != null) {
			getActivity().registerReceiver(serviceReceiver, new IntentFilter("metadataBroadcast"));
		}

		metadataRunnable = new Runnable() {
			@Override
			public void run() {
				new MetadataThread(((MainActivity) getActivity()).getPlayerUrl(), getContext()).start();
				metadataHandler.postDelayed(this, 10000);
			}
		};

		recordCurrentRadio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordRadioDialog recordRadioDialog = new RecordRadioDialog();
				recordRadioDialog.setTargetFragment(PlayingNowFragment.this, 3);
				recordRadioDialog.show(getFragmentManager(), "torpido");
			}
		});

		ibSleepTimer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((MainActivity) getActivity()).getPlaying() != 0) {
					String text = tvTimeRemaining.getText().toString();
					if (text.equals("") || text.equals("-1") || text.equals("0")) {
						SleepTimerDialog sleepTimerDialog = new SleepTimerDialog();
						sleepTimerDialog.setTargetFragment(PlayingNowFragment.this, 2);
						sleepTimerDialog.show(getFragmentManager(), "rockets");
					}
					else {
						((MainActivity) getActivity()).tellServiceT("SLEEPTIMER", -1);
						setSleepText(0);
					}
				}
				else
					Toast.makeText(getContext(), "Player is already stopped", Toast.LENGTH_SHORT).show();

			}
		});

		ibPPButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (playerStatus == PLAYING) {
					((MainActivity) getActivity()).stop();
				}
				else ((MainActivity) getActivity()).play();
			}
		});
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
			this.visible = true;
			metadataHandler.postDelayed(metadataRunnable, 300);
			setupPage();
		}
		else {
			metadataHandler.removeCallbacks(metadataRunnable);
			this.visible = false;
		}
	}

	private void setupPage() {
		ivRadio.setImageResource(((MainActivity) getActivity()).getPlayerDrawable());
		tvRadioName.setText(((MainActivity) getActivity()).getPlayerName());
		setPPButtonDrawable();
		setRecCurrentRadioUI();
		tvRadioMetadata.setSelected(true);

		//-//--/--/--/--/BackGround/--/--/--/--//-//
		Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
				((MainActivity) getActivity()).getPlayerDrawable());
		int h = getView().getHeight();
		int w = getView().getWidth();
		ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
		mDrawable.getPaint().setShader(new LinearGradient(0, 0, w, h * 2 / 3,
				manipulateColor(getDominantColor(icon), 0.4f),
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
				Math.min(r, 255),
				Math.min(g, 255),
				Math.min(b, 255));
	}

	public static int getDominantColor(Bitmap bitmap) {
		List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
		List<Palette.Swatch> swatches = new ArrayList<Palette.Swatch>(swatchesTemp);
		Collections.sort(swatches, new Comparator<Palette.Swatch>() {
			@Override
			public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
				return swatch2.getPopulation() - swatch1.getPopulation();
			}
		});
		return swatches.size() > 0 ? swatches.get(0).getRgb() : 1;
	}

	@Override
	public void onDialogPositiveClick(int minutes) {
		if (((MainActivity) getActivity()).getPlaying() != 0) {
			((MainActivity) getActivity()).tellServiceT("SLEEPTIMER", minutes);
			setSleepText(minutes);
		}
		else Toast.makeText(getContext(), "Player is already stopped", Toast.LENGTH_SHORT).show();
	}

	public void setSleepText(int time) {
		if (time == 0 || time == -1) {
			tvTimeRemaining.setText("");
		}
		else tvTimeRemaining.setText(time + " min");
	}

	public void setPPButtonStatus(int playerStatus, boolean isRecorded) {
		this.playerStatus = playerStatus;
		this.isRecorded = isRecorded;
		this.disableButtons(false);
		if (visible) {
			this.setPPButtonDrawable();
			this.setRecCurrentRadioUI();
		}
	}

	public void disableButtons(boolean state) {
		ibSleepTimer.setEnabled(!state);
		ibPPButton.setEnabled(!state);
		recordCurrentRadio.setEnabled(!state);
	}

	private void setPPButtonDrawable() {
		if (playerStatus == PLAYING) {
			getActivity().findViewById(R.id.loadingLayoutBig).setVisibility(View.GONE);
			ibPPButton.setVisibility(View.VISIBLE);
			ibPPButton.setImageResource(R.drawable.ic_stop_circle_outline);
		}
		else if (playerStatus == STOPPED) {
			getActivity().findViewById(R.id.loadingLayoutBig).setVisibility(View.GONE);
			ibPPButton.setVisibility(View.VISIBLE);
			ibPPButton.setImageResource(R.drawable.ic_play_rounded_circle_outline);
		}
		else {
			ibPPButton.setVisibility(View.INVISIBLE);
			getActivity().findViewById(R.id.loadingLayoutBig).setVisibility(View.VISIBLE);
		}
	}

	private void setRecCurrentRadioUI() {
		if (isRecorded) {
			recordCurrentRadio.setEnabled(false);
			recordCurrentRadio.setColorFilter(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
			tvIsRecorded.setText("Rec");
		}
		else {
			recordCurrentRadio.setEnabled(true);
			recordCurrentRadio.setColorFilter(ContextCompat.getColor(getContext(), R.color.RED));
			tvIsRecorded.setText("");
		}
	}



	@Override
	public void onDialogPositiveClick(int hour, int minute) {
		if (hour == -1) {
			((MainActivity) getActivity()).recordCurrentRadio(0);
		}
		else {
			int duration = (hour * 60) + minute;
			((MainActivity) getActivity()).recordCurrentRadio(duration);
		}
		((MainActivity) getActivity()).setIsRecordedStatus(true);
		isRecorded = true;
		setRecCurrentRadioUI();
	}
}

