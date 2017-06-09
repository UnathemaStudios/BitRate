package com.example.georg.radiostreameralt;


class Radio {
    private String name, url, logo;
    private boolean isRecorded, isMadeByUser;

    Radio (String name, String url, String logo, boolean isMadeByUser){
        this.name = name;
        this.url = url;
		this.logo = logo;
        this.isRecorded = false;
		this.isMadeByUser = isMadeByUser;
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
}
