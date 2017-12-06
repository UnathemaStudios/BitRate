package com.unathemastudios.bitrate;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.Date;
import java.util.HashMap;

public class MainService extends Service {
	
	public final static int WIFI = 1;
	public final static int MOBILE = 2;
	public final static int RECORDING = 0;
	public final static int NOTRECORDING = 1;
	private static final int notificationID = 8888;
	private static final int STOPPED = 0;
	private static final int LOADING = 1;
	private static final int PLAYING = 2;
	public static int activeRecordings = 0;
	private static Integer key;
	public boolean firstTime = true;
	private boolean notificationExists = false;
	private boolean stoppedByUser = true;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Recording> rec = new HashMap<>();
	private android.os.Handler recordingHandler = new android.os.Handler();
	private Runnable recordingRunnable = new Runnable() {
		@Override
		public void run() {
			Intent intent = new Intent();
			intent.setAction("recList");
			intent.putExtra("recHashMap", rec);
			intent.putExtra("numberOfRecordings", activeRecordings);
			sendBroadcast(intent);
			recordingHandler.postDelayed(this, 500);
		}
	};
	private SimpleExoPlayer player;
	private DataSource.Factory dataSourceFactory;
	private ExtractorsFactory extractorsFactory;
	private MediaSource mediaSource;
	private int finger = -1;
	private int playerStatus = STOPPED;
	private int sleepMinutes = -1;
	private String playerUrl = null;
	private android.os.Handler sleepTimerHandler = new android.os.Handler();
	private AudioManager audioManager;
    
	private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_GAIN:
					if (!stoppedByUser) {
						if (playerStatus == STOPPED) {
							play(playerUrl);
						} else {
							player.setVolume((float)1);
						}
					}
					break;

				case AudioManager.AUDIOFOCUS_LOSS:
					if (playerStatus != STOPPED) {
						stop();
					}
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					if (playerStatus != STOPPED) {
						stop();
					}
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					if (playerStatus == PLAYING) {
						player.setVolume((float)0.1);
					}
					break;
			}
		}
	};
	
	private Runnable sleepTimerRunnable = new Runnable() {
		public void run() {
			if (sleepMinutes != -1) {
				sleepMinutes--;
				if (sleepMinutes == 0) {
					stop();
					stoppedByUser = true;
				}
				send("timeRemaining", sleepMinutes);
			}
			sleepTimerHandler.postDelayed(this, 60000);
		}
	};
	
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
				if (intent.getIntExtra("state", -1) == 0) {
					Log.w("HEADSET", "UNPLUGGED");
					if (playerStatus != STOPPED) {
						if (!firstTime) {
							stop();
						} else firstTime = false;
					}
				} else {
					Log.w("HEADSET", "PLUGGED");
					if (playerStatus == STOPPED && !stoppedByUser) {
						play(playerUrl);
					}
				}
			}
		}
	};
	
	
	@Override
	public void onCreate() {
		BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
		extractorsFactory = new DefaultExtractorsFactory();
		TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
		dataSourceFactory = new DefaultDataSourceFactory(
				this,
				Util.getUserAgent(this, "BitRate"),
				defaultBandwidthMeter);
		player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
		player.addListener(new ExoPlayer.EventListener() {
			@Override
			public void onTimelineChanged(Timeline timeline, Object manifest) {
				
			}
			
			@Override
			public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
				
			}
			
			@Override
			public void onLoadingChanged(boolean isLoading) {
				//Log.w("onLoadingChanged", String.valueOf(isLoading));
			}
			
			@Override
			public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
				if(playbackState == ExoPlayer.STATE_READY){
					playerStatus = PLAYING;
					send(Integer.toString(playerStatus));
					buildNotification();
					//Log.w("onPlayerStateChanged", "STATE_READY");
				} else if (playbackState == ExoPlayer.STATE_BUFFERING){
					playerStatus = LOADING;
					send(Integer.toString(playerStatus));
					buildNotification();
					//Log.w("onPlayerStateChanged", "STATE_BUFFERING");
				} 
				/*else if (playbackState == ExoPlayer.STATE_ENDED){
					//Log.w("onPlayerStateChanged", "STATE_ENDED");
				} else if (playbackState == ExoPlayer.STATE_IDLE){					
					//Log.w("onPlayerStateChanged", "STATE_IDLE");
				}*/
			}
			
			@Override
			public void onPlayerError(ExoPlaybackException error) {
				//Log.w("onPlayerError", error.getCause().getMessage());
				//Log.w("onPlayerError", error.getCause().getCause().getMessage());
				player.prepare(mediaSource);
			}
			
			@Override
			public void onPositionDiscontinuity() {
				
			}
			
			@Override
			public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
				
			}
		});
		
		key = 0;
		if (serviceReceiver != null) {
			registerReceiver(serviceReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			registerReceiver(serviceReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		}
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.getAction()!=null) {
			switch (intent.getAction()) {
				case "PLAYER_PLAY": {
					if (intent.hasExtra("url")) {
						playerUrl = intent.getStringExtra("url");
						finger = intent.getIntExtra("finger", -1);
					}
					play(playerUrl);
					stoppedByUser = false;
					break;
				}
				case "SET_SERVICE_FINGER": {
					finger = intent.getIntExtra("finger", -2);
					if (finger == -1) {
						close();
					}
					break;
				}
				case "PLAYER_STOP": {
					stop();
					stoppedByUser = true;
					break;
				}
				case "REQUEST_PLAYER_STATUS": {
					send("SET_FINGER", finger);
					send(Integer.toString(playerStatus));
					send("timeRemaining", sleepMinutes);
					break;
				}
				case "CLOSE": {
					close();
					break;
				}
				case "SLEEPTIMER": {
					sleepMinutes = intent.getIntExtra("sleepTime", -1);
					startSleepTimer();
					break;
				}
				case "RECORD": {
					String urlString = intent.getStringExtra("urlString");
					rec.put(key, new Recording(date(), urlString, (long) intent.getIntExtra
							("duration", -1) * 60, intent.getStringExtra("name"), getApplicationContext(), key));
					rec.get(key).start();
					if (activeRecordings == 0) {
						startRecordingBroadcast();
					}
					key++;
					activeRecordings++;
					Log.w("activeRecordings", String.valueOf(activeRecordings));
					buildNotification();
					break;
				}
				case "STOP_RECORD": {
					int passedKey = intent.getIntExtra("key", -1);
					rec.get(passedKey).stop();
					break;
				}
				case "RECORDING_STOPPED": {
					rec.remove(intent.getIntExtra("hashKey", -1));
					Log.w("activeRecordings", String.valueOf(activeRecordings));
					if (activeRecordings == 0) {
						stopRecordingBroadcast();
						zeroRecordingBroadcast();
						rec.clear();
						Log.w("Recorder", "No Recordings");
					}
					buildNotification();
					break;
				}
			}
		}
		return START_STICKY;
	}
	
	public void play(String urlString) {
		if (playerStatus != STOPPED) {
			stop();
		}
		
		if (checkNetworkConnection() == WIFI || checkNetworkConnection() == MOBILE) {
			int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				
				mediaSource = new ExtractorMediaSource(
						Uri.parse(urlString),
						dataSourceFactory,
						extractorsFactory,
						null,
						null);
				
				player.prepare(mediaSource);
				player.setPlayWhenReady(true);
			}
		} else {
			playerStatus = STOPPED;
			send(Integer.toString(playerStatus));
			buildNotification();
			Toast.makeText(getApplicationContext(), "Enable Wifi or Mobile Data access first", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stop() {
		
		player.stop();
		player.setPlayWhenReady(false);
		
		sleepMinutes = -1;
		stopSleepTimer();
		playerStatus = STOPPED;
		send(Integer.toString(playerStatus));
		buildNotification();
	}
	
	public void close() {
		stop();
		stoppedByUser = true;
		player.release();
		if (activeRecordings == 0) {
			stopForeground(true);
			stopSelf();
		}
	}
	
	public void startSleepTimer() {
		sleepTimerHandler.postDelayed(sleepTimerRunnable, 60000);
	}
	
	public void stopSleepTimer() {
		sleepTimerHandler.removeCallbacks(sleepTimerRunnable);
	}
	
	public void startRecordingBroadcast() {
		recordingHandler.postDelayed(recordingRunnable, 0);
	}
	
	public void stopRecordingBroadcast() {
		recordingHandler.removeCallbacks(recordingRunnable);
	}
	
	public void zeroRecordingBroadcast() {
		Intent intent = new Intent();
		intent.setAction("recList");
		intent.putExtra("recHashMap", rec);
		intent.putExtra("numberOfRecordings", activeRecordings);
		sendBroadcast(intent);
	}
	
	private void buildNotification() {
		//intent to call mainactivity but not a new one
		Intent intent = new Intent(this, MainActivity.class).addCategory(Intent.CATEGORY_LAUNCHER).setAction(Intent.ACTION_MAIN);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//notification PLAY button
		Intent playIntent = new Intent(getApplicationContext(), MainService.class);
		playIntent.setAction("PLAYER_PLAY");
		PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.ic_play, "Play", playPendingIntent);
		
		//notification STOP button
		Intent stopIntent = new Intent(getApplicationContext(), MainService.class);
		stopIntent.setAction("PLAYER_STOP");
		PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.ic_media_stop, "Stop", stopPendingIntent);
		
		//notification EXIT button
		Intent closeIntent = new Intent(getApplicationContext(), MainService.class);
		closeIntent.setAction("CLOSE");
		PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Action closeAction = new NotificationCompat.Action(R.drawable.poweroff, "Exit", closePendingIntent);
		
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder.setSmallIcon(R.drawable.ic_stat_name);
		
		String recordingNotificationText;
		String divider;
		
		if (activeRecordings != 0) {
			if (activeRecordings == 1) {
				recordingNotificationText = activeRecordings + " Recording";
				divider = "   /   ";
			} else {
				recordingNotificationText = activeRecordings + " Recordings";
				divider = "   /   ";
			}
		} else {
			recordingNotificationText = "";
			divider = "";
		}
		
		if (playerStatus == LOADING) {
			notificationBuilder.setContentTitle("Loading" + divider + recordingNotificationText);
		} else if (playerStatus == PLAYING) {
			notificationBuilder.setContentTitle("Playing" + divider + recordingNotificationText);
			notificationBuilder.addAction(stopAction);
		} else if(playerStatus==STOPPED&&finger==-1){
			notificationBuilder.setContentTitle(recordingNotificationText);
		} else if (playerStatus == STOPPED) {
			notificationBuilder.setContentTitle("Stopped" + divider + recordingNotificationText);
			notificationBuilder.addAction(playAction);
		}
		notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setOnlyAlertOnce(true);
		notificationBuilder.setContentIntent(pendingIntent);
		
		if (activeRecordings == 0) {
			notificationBuilder.addAction(closeAction);
		}
		
		if (notificationExists) {
			assert notificationManager != null;
			notificationManager.notify(notificationID, notificationBuilder.build());
		} else {
			startForeground(notificationID, notificationBuilder.build());
			notificationExists = true;
		}
	}
	
	private String date() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddkkmmss");
			return sdf.format(calendar.getTime());
		} else {
			Date d = new Date();
			CharSequence s = DateFormat.format("yyMMddkkmmss", d.getTime());
			return (String) s;
		}
	}
	
	public void send(String actionToSend) {
		Intent intent = new Intent();
		intent.setAction(actionToSend);
		sendBroadcast(intent);
	}
	
	public void send(String actionToSend, int variable) {
		if (actionToSend.equals("timeRemaining")) {
			Intent intent = new Intent();
			intent.setAction(actionToSend);
			intent.putExtra("timeRemainingInt", variable);
			sendBroadcast(intent);
		} else {
			Intent intent = new Intent();
			intent.setAction(actionToSend);
			intent.putExtra("finger", variable);
			sendBroadcast(intent);
		}
	}
	
	private int checkNetworkConnection() {
		boolean wifiConnected;
		boolean mobileConnected;
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		assert connMgr != null;
		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
			if (wifiConnected) {
				return 1;
			} else if (mobileConnected) {
				return 2;
			}
		} else {
			return 0;
		}
		return -1;
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(serviceReceiver);
		audioManager.abandonAudioFocus(afChangeListener);
		super.onDestroy();
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
