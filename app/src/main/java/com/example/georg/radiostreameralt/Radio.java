package com.example.georg.radiostreameralt;


class Radio {
    private String name, url, logo, description;
    private boolean isRecorded, isMadeByUser;

    Radio (String name, String url, String logo, boolean isMadeByUser, String description){
        this.name = name;
        this.url = url;
		this.logo = logo;
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
}
