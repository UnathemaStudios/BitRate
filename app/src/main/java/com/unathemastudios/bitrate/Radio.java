package com.unathemastudios.bitrate;


import java.util.ArrayList;

class Radio {
    private int bitRate;
    private String name, url, logo, description, genre, id;
    private boolean isRecorded, isMadeByUser;
    public ArrayList<Alarm> alarms;

    Radio (String name, String url, String logo, boolean isMadeByUser, String description){
        this.name = name;
        this.url = url;
		this.logo = logo;
        this.isRecorded = false;
		this.isMadeByUser = isMadeByUser;
        this.description = description;
        this.bitRate = 0;
        this.genre = "";
        this.id = "";
        alarms = new ArrayList<>();
    }

    Radio (String name, String url, boolean isMadeByUser, String description, int bitRate, String
            genre){
        this.name = name;
        this.url = url;
        this.logo = "default";
        this.genre = genre;
        this.bitRate = bitRate;
        this.isRecorded = false;
        this.isMadeByUser = isMadeByUser;
        this.description = description;
        alarms = new ArrayList<>();
    }

    Radio (String name, String url, boolean isMadeByUser, String description, int bitRate, String
            genre, String id){
        this.name = name;
        this.url = url;
        this.logo = "default";
        this.genre = genre;
        this.bitRate = bitRate;
        this.isRecorded = false;
        this.isMadeByUser = isMadeByUser;
        this.description = description;
        this.id = id;
        alarms = new ArrayList<>();
    }
    
    public void addAlarm(Alarm alarm){
        alarms.add(alarm);
    }
    
    public int getNoEvents(){
        return alarms.size();
    }
    
    public void fixAlarmPosition(){
        for(Alarm al : alarms){
            al.setFingerPosition(al.getFingerPosition()-1);
            al.cancelAlarm();
            al.setAlarm();
        }
    }
    
    public void deleteAlarms(){
        for(Alarm al : alarms){
            al.cancelAlarm();
        }
    }
    
    public void fireAlarm(){
        alarms.get(alarms.size()-1).setAlarm();
    }
	
	void setRecorded(boolean recorded) {
		isRecorded = recorded;
	}
	
	public void setBitRate(int bitRate) {
		this.bitRate = bitRate;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setLogo(String logo) {
		this.logo = logo;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setMadeByUser(boolean madeByUser) {
		isMadeByUser = madeByUser;
	}
	
	public String getName() {
        return name;
    }

    String getUrl() {
        return url;
    }

    public String getLogo() {
        return logo;
    }

    boolean isRecorded() {
        return isRecorded;
    }
    
    boolean isMadeByUser(){
		return isMadeByUser;
	}

    public String getDescription() {
        return description;
    }

    public int getBitRate() {
        return bitRate;
    }

    public String getGenre() {
        return genre;
    }

    public String getId() {
        return id;
    }
}
