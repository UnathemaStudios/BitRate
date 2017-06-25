package com.unathemastudios.bitrate;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class Recording implements Serializable
{
	private String name;
	private String date;
	private String urlString;
	private transient Context context;
	private int hashKey;
	private long duration;
	private boolean stopped;
	private int status;
	private int bytesRead;
	private long startTimeInSeconds;
	
	Recording(String date, String urlString, long duration, String name, Context context, int hashKey)
	{
		this.date = date;
		this.urlString = urlString;
		this.duration = duration;
		this.name = name;
		this.context = context;
		this.hashKey = hashKey;
		stopped = false;
		bytesRead = 0;
		startTimeInSeconds = System.currentTimeMillis() / 1000;
	}
	
	void start()
	{
		status = MainService.RECORDING;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean connectionLost;
				boolean append = false;
				status = MainService.RECORDING;
				FileOutputStream fileOutputStream = null;
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
				
				File outputSource = new File(streamsDir, "!" + name + date + ".mp3");
				File outputSourceComplete = new File(streamsDir,name + date + ".mp3");
				
				do
				{
					try
					{
						fileOutputStream = new FileOutputStream(outputSource, append);
					} catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					append = true;
					try
					{
						//ICY 200 OK ERROR FIX FOR KITKAT
						if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
						{
							OkHttpClient client = new OkHttpClient();
							Request request = new Request.Builder().url(urlString).build();
							Response response = client.newCall(request).execute();
							InputStream inputStream = response.body().byteStream();
							connectionLost = false;
							int c;
							while (!stopped && (duration == -60 || ((System.currentTimeMillis() / 1000) < (startTimeInSeconds + duration))) && ((c = inputStream.read()) != -1))
							{
								
								assert fileOutputStream != null;
								fileOutputStream.write(c);
								bytesRead++;
							}
						}
						else
						{
							URL url = new URL(urlString);
							InputStream inputStream = url.openStream();
							connectionLost = false;
							int c;
							Log.w("duration", (Long.toString(duration)));
							while (!stopped && (duration == -60 || ((System.currentTimeMillis() / 1000) < (startTimeInSeconds + duration))) && ((c = inputStream.read()) != -1) )
							{
								assert fileOutputStream != null;
								fileOutputStream.write(c);
								bytesRead++;
							}
						}
					} catch (IOException e)
					{
						connectionLost = true;
						try
						{
							long retryMilliseconds = 2000;
							long timeLeftInSeconds = (startTimeInSeconds + duration) - (System.currentTimeMillis()/1000);
							if (timeLeftInSeconds < 2 && timeLeftInSeconds > -1)
							{
								retryMilliseconds = (timeLeftInSeconds + 1) * 1000;
							}
							Log.w("Connection Lost", "Retrying in " + retryMilliseconds / 1000 + " seconds");
							Thread.sleep(retryMilliseconds);
						} catch (InterruptedException e1)
						{
							e1.printStackTrace();
						}
						e.printStackTrace();
					}
				} while (connectionLost && (!stopped && (duration == -60 || ((System.currentTimeMillis() / 1000) < (startTimeInSeconds + duration)))));
				
				Log.w("Recorder", String.valueOf(bytesRead / 1024) + " KBs downloaded.");
				
				try
				{
					if (fileOutputStream!=null)
					{
						fileOutputStream.close();
					}
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				outputSource.renameTo(outputSourceComplete);
				MainService.activeRecordings--;
				status = MainService.NOTRECORDING;
				tellServiceRecordingRecordingStopped();
			}
		}).start();
	}
	
	private void tellServiceRecordingRecordingStopped()
	{
		Intent intent = new Intent(context, MainService.class);
		intent.setAction("RECORDING_STOPPED");
		intent.putExtra("hashKey", hashKey);
		context.startService(intent);
	}
	
	int getCurrentSizeInKB()
	{
		return (bytesRead / 1024);
	}
	
	long getCurrentRecordingTimeInSeconds()
	{
		return (System.currentTimeMillis() / 1000) - startTimeInSeconds;
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
	
	public String getName()
	{
		return name;
	}
}
