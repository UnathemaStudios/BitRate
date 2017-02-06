package com.example.georg.radiostreameralt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

public class MainNotification extends Service
{
	private static final int uniqueID = 54321; //and ID for notification
	
	//Broadcast Receiver
	private BroadcastReceiver serviceReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("0")) {notifModify("AFTER_STOPPED");} //if STOPPED is received modify notification
			else if (intent.getAction().equals("1")) {notifModify("AFTER_LOADING");} //if LOADING is received modify notification
			else if (intent.getAction().equals("2")) {notifModify("AFTER_PLAYING");} //if PLAYING is received modify notification
			else if (intent.getAction().equals("3")) {notifModify("AFTER_PAUSED");} //if PAUSED is received modify notification
			else if (intent.getAction().equals("CLOSENOTIF")) {notifModify("CLOSE");} //if CLOSENOTIF is received modify notification
		}
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		
		//start listening for broadcasts
		if (serviceReceiver != null)
		{
			IntentFilter stopFilter = new IntentFilter("0"); //STOPPED STATUS
			IntentFilter loadingFilter = new IntentFilter("1"); //LOADING STATUS
			IntentFilter playingFilter = new IntentFilter("2"); //PLAYING STATUS
			IntentFilter pausedFilter = new IntentFilter("3"); //PAUSED STATUS
			IntentFilter closeFilter = new IntentFilter("CLOSENOTIF");
			registerReceiver(serviceReceiver, stopFilter);
			registerReceiver(serviceReceiver, loadingFilter);
			registerReceiver(serviceReceiver, playingFilter);
			registerReceiver(serviceReceiver, pausedFilter);
			registerReceiver(serviceReceiver, closeFilter);
		}
		
		send("PLAY_STREAM");
		//return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	public void notifModify(String flag)
	{
		Notification notification;
		
		//intent to call mainactivity but not a new one
		Intent intent = new Intent(this, MainActivity.class).addCategory(Intent.CATEGORY_LAUNCHER).setAction(Intent.ACTION_MAIN);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//notification PLAY button
		Intent playIntent = new Intent("PLAY_STREAM");
		PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
		NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", playPendingIntent);
		
		//notification PAUSE button
		Intent pauseIntent = new Intent("PAUSE_STREAM");
		PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
		NotificationCompat.Action pauseAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent);
		
		//notification RESUME button
		Intent resumeIntent = new Intent("RESUME_STREAM");
		PendingIntent resumePendingIntent = PendingIntent.getBroadcast(this, 0, resumeIntent, 0);
		NotificationCompat.Action resumeAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Resume", resumePendingIntent);
		
		//notification STOP button
		Intent stopIntent = new Intent("STOP_STREAM");
		PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
		NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.ic_media_stop, "Stop", stopPendingIntent);
		
		//notification EXIT button
		Intent closeIntent = new Intent("CLOSE");
		PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
		NotificationCompat.Action closeAction = new NotificationCompat.Action(R.drawable.poweroff, "Exit", closePendingIntent);
		
		switch (flag)
		{
			case "AFTER_LOADING":
			{
				notification = new NotificationCompat.Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launchersmall)
						.setOngoing(true)
						.setContentTitle("Stream Player")
						.setContentText("Loading")
						.addAction(closeAction)
						.setContentIntent(pendingIntent)
						.build();
				
				//make impossible for system to stop service and start notification
				startForeground(uniqueID,notification);
				break;
			}
			case "AFTER_PLAYING":
			{
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
				notification = new NotificationCompat.Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launchersmall)
						.setOngoing(true)
						.setContentTitle("Stream Player")
						.setContentText("Playing")
						.addAction(pauseAction)
						.addAction(stopAction)
						.addAction(closeAction)
						.setContentIntent(pendingIntent)
						.build();
				
				//modify notification
				mNotificationManager.notify(uniqueID, notification);
				break;
			}
			case "AFTER_PAUSED":
			{
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
				notification = new NotificationCompat.Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launchersmall)
						.setOngoing(true)
						.setContentTitle("Stream Player")
						.setContentText("Paused")
						.addAction(resumeAction)
						.addAction(stopAction)
						.addAction(closeAction)
						.setContentIntent(pendingIntent)
						.build();
				
				//modify notification
				mNotificationManager.notify(uniqueID, notification);
				break;
			}
			case "AFTER_STOPPED":
			{
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				
				notification = new NotificationCompat.Builder(getApplicationContext())
						.setSmallIcon(R.drawable.ic_launchersmall)
						.setOngoing(true)
						.setContentTitle("Stream Player")
						.setContentText("Stopped")
						.addAction(playAction)
						.addAction(closeAction)
						.setContentIntent(pendingIntent)
						.build();
				
				//modify notification
				mNotificationManager.notify(uniqueID, notification);
				break;
			}
			case "CLOSE":
			{
				unregisterReceiver(serviceReceiver); //stop listening for broadcasts
				stopForeground(true); //let the system close notification service and close notification
				stopSelf(); //stop notification service
				break;
			}
		}
	}
	
	//send function to broadcast an action
	public void send(String actionToSend)
	{
		Intent intent = new Intent();
		intent.setAction(actionToSend);
		sendBroadcast(intent);
	}
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
