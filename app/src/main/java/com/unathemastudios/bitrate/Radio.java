package com.unathemastudios.bitrate;


class Radio {
    private int bitRate;
    private String name, url, logo, description, genre;
    private boolean isRecorded, isMadeByUser;

    Radio (String name, String url, String logo, boolean isMadeByUser, String description){
        this.name = name;
        this.url = url;
		this.logo = logo;
        this.isRecorded = false;
		this.isMadeByUser = isMadeByUser;
        this.description = description;
    }

    Radio (String name, String url, boolean isMadeByUser, String description, int bitRate, String
            genre){
        this.name = name;
        this.url = url;
        this.genre = genre;
        this.bitRate = bitRate;
        this.isRecorded = false;
        this.isMadeByUser = isMadeByUser;
        this.description = description;
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

    void setRecorded(boolean recorded) {
        isRecorded = recorded;
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
}
