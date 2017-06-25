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

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainService extends Service {
	
	//	public final static int NO_NETWORK = 0;
	public final static int WIFI = 1;
	public final static int MOBILE = 2;
	//	public final static int ERROR = -1;
	public final static int RECORDING = 0;
	public final static int NOTRECORDING = 1;
	//GENERAL
	private static final int notificationID = 8888;
	private static final int STOPPED = 0;
	private static final int LOADING = 1;
	
	//RECORDER
	private static final int PLAYING = 2;
	//	public final static int UNLISTED = 3;
	public static int activeRecordings = 0;
	private static Integer key;
	public boolean firstTime = true;
	private boolean notificationExists = false;
	private boolean stoppedByUser = true;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Recording> rec = new HashMap<>();
	
	//PLAYER
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
	private String status;
	private LibVLC mLibVLC = null;
	private MediaPlayer streamPlayer = null;
	private int finger = -1;
	private int playerStatus = STOPPED;
	private int sleepMinutes = -1;
	private String playerUrl = null;
	private String playerMetadata = "NO DATA";
	private android.os.Handler sleepTimerHandler = new android.os.Handler();
	private AudioManager audioManager;
	private int bytesRead = -1;
	private int counter = -1;
	private android.os.Handler checkIfStoppedHandler = new android.os.Handler();
	private Runnable checkIfStoppedRunnable = new Runnable() {
		public void run() {
//			Log.w("-----------------------", "-----------------------------------------------------");
			
			if (streamPlayer.getMedia().getStats() != null) {
//				Log.w("COUNTER", counter+"");
//				Log.w("BYTES READ" , bytesRead+"");
//				Log.w("PLAYER STATE", streamPlayer.getPlayerState()+"");
				if (streamPlayer.getPlayerState() == 3 || streamPlayer.getPlayerState() == 6 || streamPlayer.getPlayerState() == 5) {
					if (bytesRead == streamPlayer.getMedia().getStats().readBytes) {
						counter++;
						if (counter == 5) {
							streamPlayer.stop();
							streamPlayer.play();
							counter = 0;
						}
					} else {
						counter = 0;
					}
				}

//				Log.w("BITRATE", Math.round(streamPlayer.getMedia().getStats().demuxBitrate * 8000) + " kbps");
//				Log.w("KILOBYTES READ", Math.round(streamPlayer.getMedia().getStats().readBytes / 1024) + " KB");
				bytesRead = streamPlayer.getMedia().getStats().readBytes;
			}
//			if (streamPlayer.getMedia().getTrackCount() > 0) {
//				if (streamPlayer.getMedia().getTrack(0).codec.equals("MPEG Audio layer 1/2")) {
//					Log.w("TYPE", "MP3");
//				} else if (streamPlayer.getMedia().getTrack(0).codec.equals("MPEG AAC Audio")) {
//					Log.w("TYPE", "AAC");
//				} else if (streamPlayer.getMedia().getTrack(0).codec.equals("FLAC (Free Lossless Audio Codec)")) {
//					Log.w("TYPE", "FLAC");
//				} else {
//					Log.w("TYPE", streamPlayer.getMedia().getTrack(0).codec);
//				}
//			}
			checkIfStoppedHandler.postDelayed(this, 1000);
		}
	};
	private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_GAIN:
					// resume playback
					if (!stoppedByUser) {
						if (playerStatus == STOPPED) {
							play(playerUrl);
						} else {
							streamPlayer.play();
						}
					}
					break;
				
				case AudioManager.AUDIOFOCUS_LOSS:
					// Lost focus for an unbounded amount of time: stop playback and release media player
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
					// Lost focus for a short time, but it's ok to keep playing
					// at an attenuated level
					if (playerStatus == PLAYING) {
						streamPlayer.pause();
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
			/*else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) 
			{
				if (checkNetworkConnection() == NO_NETWORK)
				{
//					Log.w("Network", "No Network");
//					if (playerStatus != STOPPED)
//					{
//						stop();
//					}
				}
				else if (checkNetworkConnection() == WIFI)
				{
//					Log.w("Network", "WIFI");
				}
				else if (checkNetworkConnection() == MOBILE)
				{
//					Log.w("Network", "MOBILE");
				}
				else if (checkNetworkConnection() == ERROR)
				{
//					Log.w("Network", "ERROR");
				}
			}*/
		}
	};
	
	public void startCheckIfStopped() {
		checkIfStoppedHandler.postDelayed(checkIfStoppedRunnable, 1000);
//		Log.w("CHECK IF STOPPED", "STARTED");
	}
	
	public void stopCheckIfStopped() {
		checkIfStoppedHandler.removeCallbacks(checkIfStoppedRunnable);
//		Log.w("CHECK IF STOPPED", "STOPPED");
	}
	
	@Override
	public void onCreate() {
		
		final ArrayList<String> args = new ArrayList<>();
		args.add("-vv");
		args.add("--network-caching=1000");
		mLibVLC = new LibVLC(this, args);
		streamPlayer = new MediaPlayer(mLibVLC);
		
		streamPlayer.setEventListener(new MediaPlayer.EventListener() {
			@Override
			public void onEvent(MediaPlayer.Event event) {
				switch (event.type) {
					case MediaPlayer.Event.Buffering:
//						Log.w("BUFFERING", "" + (int) event.getBuffering());
						if ((int) event.getBuffering() == 100) {
							playerStatus = PLAYING;
							send(Integer.toString(playerStatus));
							buildNotification();
							status = "DONE";
//							Log.w("STATUS", status);
						}
						if (status.equals("CONNECTING") && (int) event.getBuffering() >= 2) {
							status = "BUFFERING";
//							Log.w("STATUS", status);
						}
						break;
					case MediaPlayer.Event.Opening:
//						Log.w("MEDIAPLAYER EVENT", "OPENING");
						break;
					case MediaPlayer.Event.Playing:
//						Log.w("MEDIAPLAYER EVENT", "PLAYING");
						break;
					case MediaPlayer.Event.Stopped:
//						Log.w("MEDIAPLAYER EVENT", "STOPPED");
						break;
					case MediaPlayer.Event.EncounteredError:
//						Log.w("MEDIAPLAYER EVENT", "ERROR");
						break;
					case MediaPlayer.Event.MediaChanged:
//						Log.w("MEDIAPLAYER EVENT", "MEDIACHANGED");
						break;
					case MediaPlayer.Event.EndReached:
//                        Log.w("MEDIAPLAYER EVENT", "END REACHED");
					default:
//						Log.w("UN MEDIA PLAYER EVENT",String.valueOf(event.type));
				}
			}
		});
		
		key = 0;
		if (serviceReceiver != null) {
			registerReceiver(serviceReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			registerReceiver(serviceReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		}
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}
	
	public void startMediaListener() {
		streamPlayer.getMedia().setEventListener(new Media.EventListener() {
			@Override
			public void onEvent(Media.Event event) {
				switch (event.type) {
					case Media.Event.MetaChanged:
//						if (streamPlayer.getMedia().getMeta(Media.Meta.Title) != null) {
//							Log.w("Title", streamPlayer.getMedia().getMeta(Media.Meta.Title));
//						}
//						if (streamPlayer.getMedia().getMeta(Media.Meta.Genre) != null) {
//							Log.w("Genre", streamPlayer.getMedia().getMeta(Media.Meta.Genre));
//						}
						if (streamPlayer.getMedia().getMeta(Media.Meta.NowPlaying) != null) {
//							Log.w("NowPlaying", streamPlayer.getMedia().getMeta(Media.Meta.NowPlaying));
							playerMetadata = streamPlayer.getMedia().getMeta(Media.Meta.NowPlaying);
							sendMetadata();
						} else
						
						{
							playerMetadata = "NO DATA";
							sendMetadata();
						}
						break;
					case Media.Event.StateChanged:
//						Log.w("MEDIA STATE", String.valueOf(streamPlayer.getMedia().getState()));
						break;
					default:
//						Log.w("UNKNOWN EVENT", String.valueOf(event.type));
				}
			}
		});
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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
			case "SET_SERVICE_FINGER":
			{
				finger = intent.getIntExtra("finger", -1);
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
			case "REQUEST_METADATA": {
				sendMetadata();
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
		return START_STICKY;
	}
	
	public void play(String urlString) {
		if (playerStatus != STOPPED) {
			stop();
		}
		playerStatus = LOADING;
		status = "CONNECTING";
//		Log.w("STATUS", status);
		send(Integer.toString(playerStatus));
		buildNotification();
		
		if (checkNetworkConnection() == WIFI || checkNetworkConnection() == MOBILE) {
			int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				streamPlayer.setMedia(new Media(mLibVLC, Uri.parse(urlString)));
				streamPlayer.play();
				startMediaListener();
				startCheckIfStopped();
//				playerStatus = PLAYING;
//				send(Integer.toString(playerStatus));
//				buildNotification();
			}
		} else {
			playerStatus = STOPPED;
			send(Integer.toString(playerStatus));
			buildNotification();
			Toast.makeText(getApplicationContext(), "Enable Wifi or Mobile Data access first", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void stop() {
		
		if (streamPlayer.isPlaying()) {
			streamPlayer.stop();
		}
		streamPlayer.getMedia().release();
		sleepMinutes = -1;
		stopSleepTimer();
		stopCheckIfStopped();
		playerStatus = STOPPED;
		send(Integer.toString(playerStatus));
		buildNotification();
	}
	
	public void close() {
		stop();
		if (activeRecordings == 0) {
			streamPlayer.release();
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
	
	public void sendMetadata() {
		Intent intent = new Intent();
		intent.setAction("metadataBroadcast");
		intent.putExtra("streamTitle", playerMetadata);
		sendBroadcast(intent);
	}
	
	private int checkNetworkConnection() {
		boolean wifiConnected;
		boolean mobileConnected;
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
