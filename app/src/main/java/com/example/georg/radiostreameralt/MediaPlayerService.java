package com.example.georg.radiostreameralt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

public class MediaPlayerService extends Service
{
	private Handler myHandler = new Handler(); //handler for time update
	private String url;
	private MediaPlayer streamPlayer;
	private int status = 0; //0 STOPPED, 1 LOADING, 2 PLAYING, 3 PAUSED
	
	//Broadcast Receiver
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("PLAY_STREAM")) {play(url);}
			else if (intent.getAction().equals("PAUSE_STREAM")) {pause();}
			else if (intent.getAction().equals("RESUME_STREAM")) {resume();}
			else if (intent.getAction().equals("STOP_STREAM")) {stop();}
			else if (intent.getAction().equals("CLOSE")) {close();}
			else if (intent.getAction().equals("REQUEST_STATUS")) {send(Integer.toString(status));}
		}
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) //when service starts
	{
		
		url = intent.getStringExtra("urlString");
		
		Intent notifIntent = new Intent(MediaPlayerService.this, MainNotification.class);
		startService(notifIntent); //start notification service
		
		//start listening for broadcasts
		if (serviceReceiver != null)
		{
			registerReceiver(serviceReceiver, new IntentFilter("PLAY_STREAM"));
			registerReceiver(serviceReceiver, new IntentFilter("PAUSE_STREAM"));
			registerReceiver(serviceReceiver, new IntentFilter("RESUME_STREAM"));
			registerReceiver(serviceReceiver, new IntentFilter("STOP_STREAM"));
			registerReceiver(serviceReceiver, new IntentFilter("CLOSE"));
			registerReceiver(serviceReceiver, new IntentFilter("REQUEST_STATUS"));
		}
		
		return START_STICKY;
	}
	
	//play pause resume stop close swap functions
	public void play(String urlString)
	{
		status = 1; //LOADING
		send(Integer.toString(status)); //broadcast media player status for main and notification
		
		//mediaplayer
		streamPlayer = new MediaPlayer();
		streamPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//		streamPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener()
//		{
//			@Override
//			public void onBufferingUpdate(MediaPlayer streamPlayer, int percent) {
//				Log.w("media", "Buffering: " + percent);
//			}
//		});
//		streamPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//			@Override
//			public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
//				Log.w("asd", "MediaPlayer.OnInfoListener: " + i);
//				return false;
//			}
//		});
		try {
			streamPlayer.setDataSource(urlString);
			streamPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
			{
				@Override
				public void onPrepared(MediaPlayer mediaPlayer)
				{
					mediaPlayer.start();
					
					myHandler.postDelayed(UpdateSongTime,100); //start time updater
					
					status=2; //PLAYING
					send(Integer.toString(status)); //broadcast media player status for main and notification
				}
			});
			streamPlayer.prepareAsync();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void pause()
	{
		streamPlayer.pause();
		status = 3; //PAUSED
		send(Integer.toString(status)); //broadcast media player status for main and notification
	}
	
	public void resume()
	{
		streamPlayer.start();
		status = 2; //PLAYING
		send(Integer.toString(status)); //broadcast media player status for main and notification
	}
	
	public void stop()
	{
		myHandler.removeCallbacks(UpdateSongTime); // stop time updater
		streamPlayer.stop();//needed?
		streamPlayer.reset();		
		streamPlayer.release();
		status = 0; //STOPPED
		send(Integer.toString(status)); //broadcast media player status for main and notification
		
	}
	
	public void close()
	{
		if (status != 0)
		{
			stop();
		}
		unregisterReceiver(serviceReceiver); //stop listening for broadcasts
		send("CLOSENOTIF"); //broadcast CLOSENOTIF for notification to close
		stopSelf(); //stop media player service
	}
	
	
	//handler for time update
	private Runnable UpdateSongTime = new Runnable() {
		public void run() {
			double time;
			time = streamPlayer.getCurrentPosition();
			sendTime(time);
			myHandler.postDelayed(this, 100);
		}
	};
	
	//send function to broadcast an action
	public void send(String actionToSend)
	{
		Intent intent = new Intent();
		intent.setAction(actionToSend);
		sendBroadcast(intent);
	}
	
	//sendTime function to broadcast string and time
	public void sendTime(double time)
	{
		Intent intent = new Intent();
		intent.setAction("TIME_UPDATE");
		intent.putExtra("time", time);
		sendBroadcast(intent);
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}

//Toast.makeText(getApplicationContext(), "this is my Toast message!!! =)",
//		Toast.LENGTH_LONG).show();
