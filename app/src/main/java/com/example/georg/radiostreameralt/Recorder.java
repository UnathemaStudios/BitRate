package com.example.georg.radiostreameralt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.georg.radiostreameralt.Recorder.RECORDING;
import static com.example.georg.radiostreameralt.Recorder.STOPPED;

public class Recorder extends Service
{
	public final static int RECORDING = 1;
	public final static int STOPPED = 2;
	public final static int UNLISTED = 3;
	public final static int FIRSTRECORDING = 0;
	public final static int LASTRECOEDING = 1;
	public final static int FIRSTANDLASTRECORDING = 2;
	
	private NotificationManager notificationManager;
	private int notifID = 4321;
	private void showNotification(boolean first)
	{
		Notification notification = new Notification.Builder(this)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_launchersmall)  // the status icon
				.setContentTitle("Recording now...   " + activeRecordings)  // the label of
				// the entry
				.build();
		if (first)
		{
			startForeground(notifID, notification);
		}
		else
		{
			notificationManager.notify(notifID, notification);
		}
	}

	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Recording> rec = new HashMap<>();
	private static Integer key;
	public static int activeRecordings = 0;


	private android.os.Handler myHandler = new android.os.Handler();

	private Runnable BANANA = new Runnable() {
		public void run() {
			int j = 0;
			for (int i=0;i<rec.size();i++)
			{
				if (rec.get(i) != null)
				{
					if (rec.get(i).getStatus() != STOPPED)
					{
						j++;
						if (rec.get(i).getDuration() != -1)
						{
							Log.w(Long.toString(System.currentTimeMillis()), "ID:" + Integer.toString(i)
									+ " Time:" + rec.get(i).getCurrentRecordingTimeInSeconds() + "s/"
									+ rec.get(i).getDuration() + "s Size:"
									+ rec.get(i).getCurrentSizeInKB() + "KB");
						}
						else
						{
							Log.w(Long.toString(System.currentTimeMillis()), "ID:" + Integer.toString(i)
									+ " Time:" + rec.get(i).getCurrentRecordingTimeInSeconds() + "s Size:"
									+ rec.get(i).getCurrentSizeInKB() + "KB");
						}
						if(activeRecordings==1){
							broadcastRecording("RECORDING_ADDED", i, rec.get(i).getName(), rec.get(i)
									.getCurrentRecordingTimeInSeconds(), rec.get(i)
									.getCurrentSizeInKB(), FIRSTANDLASTRECORDING);
						}
						else if(j==1){
							broadcastRecording("RECORDING_ADDED", i, rec.get(i).getName(), rec.get(i)
									.getCurrentRecordingTimeInSeconds(), rec.get(i)
									.getCurrentSizeInKB(), FIRSTRECORDING);
						}
						else if(j==activeRecordings){
							broadcastRecording("RECORDING_ADDED", i, rec.get(i).getName(), rec.get(i)
									.getCurrentRecordingTimeInSeconds(), rec.get(i)
									.getCurrentSizeInKB(), LASTRECOEDING);
						}
						else broadcastRecording("RECORDING_ADDED", i, rec.get(i).getName(), rec.get(i)
								.getCurrentRecordingTimeInSeconds(), rec.get(i)
								.getCurrentSizeInKB(), 999);
					}
				}
			}
			myHandler.postDelayed(this, 500);
		}
	};

	@Override
	public void onCreate()
	{
		myHandler.postDelayed(BANANA,100);
		key = 0;
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification(true);
		Log.w("Recorder", "service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		String action = intent.getStringExtra("Action");
		switch (action)
		{
			case "RECORD":
			{
				String urlString = intent.getStringExtra("urlString");

				rec.put(key, new Recording(date(), urlString, intent.getLongExtra("duration", -1)
						,intent.getStringExtra("name")));
				rec.get(key).start();
				broadcastRecording("SIMPLE_RECORDING_ADDED"); //send  main the key for hash address
				//Log.w("Recorder", "REC " + key + " START");
				key++;
				activeRecordings++;
				showNotification(false);
				Log.w("activeRecordings", String.valueOf(activeRecordings));
				break;
			}
			case "STOP":
			{
				int passedKey = intent.getIntExtra("key", -1);
				rec.get(passedKey).stop();
				broadcastRecording("RECORDING_STOPPED", passedKey);
				//Log.w("Recorder", "REC " + passedKey + " END");
				while (rec.get(passedKey).getStatus() != STOPPED);
				Log.w("activeRecordings", String.valueOf(activeRecordings));
				//rec.remove(passedKey);
				showNotification(false);
				if (activeRecordings == 0)
				{
					myHandler.removeCallbacks(BANANA);
					rec.clear();
					Log.w("Recorder", "service destroyed");
					stopSelf();
				}
				break;
			}
		}
		return START_STICKY;
	}

	private String date()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd-kkmmss");
			return sdf.format(calendar.getTime());
		}
		else
		{
			Date d = new Date();
			CharSequence s = DateFormat.format("yyMMdd-kkmmss", d.getTime());
			return (String)s;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	//function to broadcast hash key of current Recording
	public void broadcastRecording(String action, int key, String name, long currentTime, int
			sizeInKb, int position)
	{
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra("key", key);
		intent.putExtra("name", name);
		intent.putExtra("time", currentTime);
		intent.putExtra("size", sizeInKb);
		intent.putExtra("position", position);
		sendBroadcast(intent);
	}
	public void broadcastRecording(String action, int key)
	{
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra("key", key);
		sendBroadcast(intent);
	}
	public void broadcastRecording(String action){
		Intent intent = new Intent();
		intent.setAction(action);
		sendBroadcast(intent);
	}
}

class Recording
{
	private String name;
	private String date;
	private String urlString;
	private long duration;
	private boolean stopped;
	private int status;
	private int bytesRead;
	private long startTimeInSeconds;


	Recording(String date, String urlString, long duration, String name)
	{
		this.date = date;
		this.urlString = urlString;
		this.duration = duration;
		this.name = name;
		stopped = false;
		bytesRead = 0;
		startTimeInSeconds = System.currentTimeMillis()/1000;
	}
	
	void start()
	{
		status = RECORDING;
		new Thread(new Runnable(){
			@Override
			public void run() {
				try
				{
					status = RECORDING;
					FileOutputStream fileOutputStream;
					File streamsDir = new File(Environment.getExternalStorageDirectory() + "/Streams");
					if (!streamsDir.exists())
					{
						boolean directoryCreated = streamsDir.mkdirs();
						Log.w("Recorder", "Created directory");
						if (!directoryCreated)
						{
							Log.w("Recorder", "Failed to create directory");
						}
					}
					File outputSource = new File(streamsDir,name+date + ".mp3");
					fileOutputStream = new FileOutputStream(outputSource);
					
					//ICY 200 OK ERROR FIX FOR KITKAT
					if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
					{
						Log.w("Version", String.valueOf(Build.VERSION.SDK_INT));
						OkHttpClient client = new OkHttpClient();
						Request request = new Request.Builder()
								.url(urlString)
								.build();
						Response response = client.newCall(request).execute();
						InputStream inputStream = response.body().byteStream();
						
						int c;
						while (((c = inputStream.read()) != -1) && !stopped && (duration == -1 || ((System.currentTimeMillis()/1000) < (startTimeInSeconds+duration))))
						{
							fileOutputStream.write(c);
							bytesRead++;
						}
					}
					else
					{
						Log.w("Version", String.valueOf(Build.VERSION.SDK_INT));
						URL url = new URL(urlString);
						InputStream inputStream = url.openStream();
						
						int c;
						while (((c = inputStream.read()) != -1) && !stopped && (duration == -1 || ((System.currentTimeMillis()/1000) < (startTimeInSeconds+duration))))
						{
							fileOutputStream.write(c);
							bytesRead++;
						}
					}
					
					Log.w("Recorder", String.valueOf(bytesRead/1024) + " KBs downloaded.");
					
					fileOutputStream.close();
					
					//Log.w("Recorder", "finished");
					Recorder.activeRecordings--;
					status = STOPPED;
				} catch (IOException e){e.printStackTrace();}
			}
		}).start();
	}

	int getCurrentSizeInKB()
	{
		return (bytesRead/1024);
	}

	long getCurrentRecordingTimeInSeconds()
	{
		return (System.currentTimeMillis()/1000) - startTimeInSeconds;
	}

	int getStatus()
	{
		return status;
	}

	long getDuration()
	{
		return duration;
	}

	void stop()
	{
		stopped = true;
	}

	public String getName() {
		return name;
	}
}
