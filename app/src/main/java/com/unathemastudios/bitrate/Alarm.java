package com.unathemastudios.bitrate;

/**
 * Created by Thanos on 17-Sep-17.
 */

public class Alarm {
	
	private Radio alarmRadio;
	private long timestamp;
	private boolean isActive, isRecord;
	
	
	public Alarm(Radio radio, long timestamp, boolean isActive, boolean isRecord) {
		this.alarmRadio = radio;
		this.timestamp = timestamp;
		this.isActive = isActive;
		this.isRecord = isRecord;
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
	
	public void setActive(boolean active) {
		isActive = active;
	}
}
