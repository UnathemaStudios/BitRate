package com.example.georg.radiostreameralt;

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
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class MainService extends Service {
    
    //  .oooooo.    oooooooooooo ooooo      ooo oooooooooooo ooooooooo.         .o.       ooooo
    // d8P'  `Y8b   `888'     `8 `888b.     `8' `888'     `8 `888   `Y88.      .888.      `888'
    //888            888          8 `88b.    8   888          888   .d88'     .8"888.      888
    //888            888oooo8     8   `88b.  8   888oooo8     888ooo88P'     .8' `888.     888
    //888     ooooo  888    "     8     `88b.8   888    "     888`88b.      .88ooo8888.    888
    //`88.    .88'   888       o  8       `888   888       o  888  `88b.   .8'     `888.   888       o
    // `Y8bood8P'   o888ooooood8 o8o        `8  o888ooooood8 o888o  o888o o88o     o8888o o888ooooood8

    private static final int notificationID = 8888;
    private boolean notificationExists = false;
	public final static int NO_NETWORK = 0;
	public final static int WIFI = 1;
	public final static int MOBILE = 2;
	public final static int ERROR = -1;
	private boolean stoppedByUser = true;
	
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG))
			{
				if (intent.getIntExtra("state", -1)==0)
				{
					Log.w("HEADSET", "UNPLUGGED");
					if (playerStatus != STOPPED)
					{
						stop();
					}
				}
				else
				{
					Log.w("HEADSET", "PLUGGED");
					if (playerStatus == STOPPED && !stoppedByUser)
					{
						play(playerUrl);
					}
				}
			}
			else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) 
			{
				if (checkNetworkConnection() == NO_NETWORK)
				{
					Log.w("Network", "No Network");
					if (playerStatus != STOPPED)
					{
						stop();
					}
				}
				else if (checkNetworkConnection() == WIFI)
				{
					Log.w("Network", "WIFI");
				}
				else if (checkNetworkConnection() == MOBILE)
				{
					Log.w("Network", "MOBILE");
				}
				else if (checkNetworkConnection() == ERROR)
				{
					Log.w("Network", "ERROR");
				}
			}
		}
	};

    //ooooooooo.   oooooooooooo   .oooooo.     .oooooo.   ooooooooo.   oooooooooo.   oooooooooooo ooooooooo.
    //`888   `Y88. `888'     `8  d8P'  `Y8b   d8P'  `Y8b  `888   `Y88. `888'   `Y8b  `888'     `8 `888   `Y88.
    // 888   .d88'  888         888          888      888  888   .d88'  888      888  888          888   .d88'
    // 888ooo88P'   888oooo8    888          888      888  888ooo88P'   888      888  888oooo8     888ooo88P'
    // 888`88b.     888    "    888          888      888  888`88b.     888      888  888    "     888`88b.
    // 888  `88b.   888       o `88b    ooo  `88b    d88'  888  `88b.   888     d88'  888       o  888  `88b.
    //o888o  o888o o888ooooood8  `Y8bood8P'   `Y8bood8P'  o888o  o888o o888bood8P'   o888ooooood8 o888o  o888o
	
    public final static int RECORDING = 0;
    public final static int NOTRECORDING = 1;
//	public final static int UNLISTED = 3;
    public static int activeRecordings = 0;
    private static Integer key;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Recording> rec = new HashMap<>();
    private android.os.Handler recordingHandler = new android.os.Handler();
    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setAction("recList");
            intent.putExtra("recHashMap", rec);
            sendBroadcast(intent);
            recordingHandler.postDelayed(this, 500);
        }
    };

    //ooooooooo.   ooooo              .o.       oooooo   oooo oooooooooooo ooooooooo.
    //`888   `Y88. `888'             .888.       `888.   .8'  `888'     `8 `888   `Y88.
    // 888   .d88'  888             .8"888.       `888. .8'    888          888   .d88'
    // 888ooo88P'   888            .8' `888.       `888.8'     888oooo8     888ooo88P'
    // 888          888           .88ooo8888.       `888'      888    "     888`88b.
    // 888          888       o  .8'     `888.       888       888       o  888  `88b.
    //o888o        o888ooooood8 o88o     o8888o     o888o     o888ooooood8 o888o  o888o

    private static final int STOPPED = 0;
    private static final int LOADING = 1;
    private static final int PLAYING = 2;
    private MediaPlayer streamPlayer = new MediaPlayer();
    private int finger;
    private int playerStatus = STOPPED;
    private int sleepMinutes = -1;
    private String playerUrl;
    private android.os.Handler sleepTimerHandler = new android.os.Handler();
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // resume playback
                    if(playerStatus==STOPPED)
					{
						if (!stoppedByUser)
						{
							play(playerUrl);
						}
					}
                    streamPlayer.setVolume(1.0f, 1.0f);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    if (playerStatus != STOPPED)
					{
						stop();
					}
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    stop();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    if (playerStatus==PLAYING) streamPlayer.setVolume(0.1f, 0.1f);
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

    @Override
    public void onCreate() {
        key = 0;
        if (serviceReceiver != null) {
            registerReceiver(serviceReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			registerReceiver(serviceReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case "PLAYER_PLAY": {
                if (intent.hasExtra("url")) {
                    playerUrl = intent.getStringExtra("url");
                    finger = intent.getIntExtra("finger", -1);
                }
                else 
				{
					playerUrl = "http://philae.shoutca.st:8307/stream";
					finger = 1;
				}
                play(playerUrl);
				stoppedByUser = false;
                break;
            }
            case "PLAYER_STOP": {
                stop();
				stoppedByUser = true;
                break;
            }
            case "REQUEST_PLAYER_STATUS": {
                send(Integer.toString(playerStatus));
                send("SET_FINGER", finger);
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
                rec.put(key, new Recording(date(), urlString, (long)intent.getIntExtra
                        ("duration", -1)*60, intent.getStringExtra("name"),getApplicationContext(),key));
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
			case "RECORDING_STOPPED":
			{
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
		if (playerStatus != STOPPED)
		{
			stop();
		}
		playerStatus = LOADING;
		send(Integer.toString(playerStatus));
		buildNotification();
		
		if (checkNetworkConnection() == WIFI || checkNetworkConnection() == MOBILE)
		{
			int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
			{
				streamPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				streamPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener()
				{
					@Override
					public void onBufferingUpdate(MediaPlayer streamPlayer, int percent) {
						Log.w("media", "Buffering: " + percent);
					}
				});
				streamPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
					@Override
					public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
						Log.w("asd", "MediaPlayer.OnInfoListener: " + i);
						return false;
					}
				});
				try
				{
//			streamPlayer.setDataSource(this,Uri.parse(urlString));
					streamPlayer.setDataSource(urlString);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				streamPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
				{
					@Override
					public void onPrepared(MediaPlayer mediaPlayer)
					{
						mediaPlayer.start();
						playerStatus = PLAYING;
						send(Integer.toString(playerStatus));
						buildNotification();
					}
				});
				streamPlayer.prepareAsync();
			}
		}
		else
		{
			playerStatus = STOPPED;
			send(Integer.toString(playerStatus));
			buildNotification();
			Toast.makeText(getApplicationContext(), "Enable Wifi or Mobile Data access first",Toast.LENGTH_SHORT).show();
		}
	}

    public void stop() {
        if (streamPlayer.isPlaying()) {
            streamPlayer.stop();
        }
        streamPlayer.reset();
		sleepMinutes = -1;
		stopSleepTimer();
		playerStatus = STOPPED;
		send(Integer.toString(playerStatus));
		buildNotification();
	}

    public void close() {
        if (activeRecordings == 0) {
            stop();
            streamPlayer.release();
            stopForeground(true);
            unregisterReceiver(serviceReceiver);
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
        notificationBuilder.setContentTitle("Stream Player");

        String recordingNotificationText;

        if (activeRecordings != 0) {
            if (activeRecordings == 1) {
                recordingNotificationText = "   /   " + activeRecordings + " Recording";
            }
            else {
                recordingNotificationText = "   /   " + activeRecordings + " Recordings";
            }
        }
        else {
            recordingNotificationText = "";
        }

        if (playerStatus == LOADING) {
            notificationBuilder.setContentText("Loading" + recordingNotificationText);
        }
        else if (playerStatus == PLAYING) {
            notificationBuilder.setContentText("Playing" + recordingNotificationText);
            notificationBuilder.addAction(stopAction);
        }
        else if (playerStatus == STOPPED) {
            notificationBuilder.setContentText("Stopped" + recordingNotificationText);
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
        }
        else {
            startForeground(notificationID, notificationBuilder.build());
            notificationExists = true;
        }
    }

    private String date() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddkkmmss");
            return sdf.format(calendar.getTime());
        }
        else {
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
        }
        else {
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
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if (wifiConnected) {
                return 1;
            }
            else if (mobileConnected) {
                return 2;
            }
        }
        else {
            return 0;
        }
        return -1;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
