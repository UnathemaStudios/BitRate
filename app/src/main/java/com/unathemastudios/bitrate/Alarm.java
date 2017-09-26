package com.unathemastudios.bitrate;

import android.util.Log;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class Alarm {
	
	private Radio alarmRadio;
	private long timestamp;
	private boolean isActive, isRecord, hasSpecificDate;
	
	
	public Alarm(Radio radio, long timestamp, boolean isActive, boolean isRecord, boolean
			hasSpecificDate) {
		this.alarmRadio = radio;
		this.timestamp = timestamp;
		this.isActive = isActive;
		this.isRecord = isRecord;
		this.hasSpecificDate = hasSpecificDate;
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
		Log.w("Set Event", "SET");
	}

	public void cancelAlarm(){
		//TODO: Add Cancelation Here
		Log.w("Canceled Event", "Canceled");
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
}
