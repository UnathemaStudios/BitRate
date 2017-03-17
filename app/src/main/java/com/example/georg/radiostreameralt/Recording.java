package com.example.georg.radiostreameralt;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.georg.radiostreameralt.MainService.NOTRECORDING;
import static com.example.georg.radiostreameralt.MainService.RECORDING;

class Recording implements Serializable{
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
        startTimeInSeconds = System.currentTimeMillis() / 1000;
    }

    void start()
    {
        status = RECORDING;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
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
                    File outputSource = new File(streamsDir, name + date + ".mp3");
                    fileOutputStream = new FileOutputStream(outputSource);

                    //ICY 200 OK ERROR FIX FOR KITKAT
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
                    {
                        Log.w("Version", String.valueOf(Build.VERSION.SDK_INT));
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url(urlString).build();
                        Response response = client.newCall(request).execute();
                        InputStream inputStream = response.body().byteStream();

                        int c;
                        while (((c = inputStream.read()) != -1) && !stopped && (duration == -1 || ((System.currentTimeMillis() / 1000) < (startTimeInSeconds + duration))))
                        {
                            fileOutputStream.write(c);
                            bytesRead++;
                        }
                    } else
                    {
                        Log.w("Version", String.valueOf(Build.VERSION.SDK_INT));
                        URL url = new URL(urlString);
                        InputStream inputStream = url.openStream();

                        int c;
                        while (((c = inputStream.read()) != -1) && !stopped && (duration == -1 || ((System.currentTimeMillis() / 1000) < (startTimeInSeconds + duration))))
                        {
                            fileOutputStream.write(c);
                            bytesRead++;
                        }
                    }

                    Log.w("Recorder", String.valueOf(bytesRead / 1024) + " KBs downloaded.");

                    fileOutputStream.close();

                    //Log.w("Recorder", "finished");
                    MainService.activeRecordings--;
                    status = NOTRECORDING;
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
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

