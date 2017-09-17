package com.unathemastudios.bitrate;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class Alarm {
	
	private Radio alarmRadio;
	private int hour, minute;
	private boolean isActive, isRecord;
	
	
	public Alarm(Radio radio, int hour, int minute, boolean isActive, boolean isRecord) {
		this.alarmRadio = radio;
		this.hour = hour;
		this.minute = minute;
		this.isActive = isActive;
		this.isRecord = isRecord;
	}
	
	public Radio getAlarmRadio() {
		return alarmRadio;
	}
	
	public int getHour() {
		return hour;
	}
	
	public int getMinute() {
		return minute;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public boolean isRecord() {
		return isRecord;
	}
	
	public void setActive(boolean active) {
		isActive = active;
	}
}
