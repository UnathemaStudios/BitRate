package com.unathemastudios.bitrate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class Alarm {
	
	private Radio alarmRadio;
	private long timestamp;
	private boolean isActive, isRecord, hasSpecificDate;
	private int fingerPosition;
	private Context context;
	private PendingIntent pIntent;
	
	
	public Alarm(Radio radio, long timestamp, boolean isActive, boolean isRecord, boolean
			hasSpecificDate, int fingerPosition) {
		this.alarmRadio = radio;
		this.timestamp = timestamp;
		this.isActive = isActive;
		this.isRecord = isRecord;
		this.hasSpecificDate = hasSpecificDate;
		this.fingerPosition = fingerPosition;
	}

	public String toggleState(){
		if(!this.isActive) {
			if (this.timestamp < System.currentTimeMillis()) {
				do {
					this.timestamp += 86400000;
				} while (timestamp < System.currentTimeMillis());
			}
			this.isActive = true;
			this.setAlarm();
			return "Alarm is set " + ((timestamp - System.currentTimeMillis()) / 1000 / 60 / 60) +
					" Hours & "
					+ (
					(timestamp -
							System.currentTimeMillis()) / 1000 / 60 % 60)+" Minutes from now";
		}
		else{
			this.isActive = false;
			this.cancelAlarm();
			return null;
		}
	}

	public void setAlarm(){
		//TODO: Add Activation Here
		Intent intent = new Intent(context, MainService.class);
		intent.setAction("PLAYER_PLAY");
		intent.putExtra("url", alarmRadio.getUrl());
		intent.putExtra("finger", fingerPosition);

		AlarmManager manager = (AlarmManager)context.getSystemService(Context
				.ALARM_SERVICE);
		pIntent = PendingIntent.getService(context, fingerPosition, intent, 0);
		manager.set(AlarmManager.RTC_WAKEUP, timestamp, pIntent);
		Log.w("TimeStamp", timestamp +"");
		Log.w("COntext2", context.toString());
		Log.w("Set Event", "SET");
	}

	public void cancelAlarm(){
		//TODO: Add Cancelation Here

		AlarmManager manager = (AlarmManager)context.getSystemService(Context
				.ALARM_SERVICE);
		
		PendingIntent displayIntent = pIntent;
		manager.cancel(displayIntent);
		
		Log.w("Canceled Event", "Canceled");
	}
	
	public void setContext(Context con){
		this.context = con;
	}

	public Radio getAlarmRadio() {
		return alarmRadio;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isActive() {
		return isActive;
	}
	
	public boolean isRecord() {
		return isRecord;
	}

	public boolean isHasSpecificDate() {
		return hasSpecificDate;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public int getFingerPosition() {
		return fingerPosition;
	}

	public void setFingerPosition(int fingerPosition) {
		this.fingerPosition = fingerPosition;
	}
}
