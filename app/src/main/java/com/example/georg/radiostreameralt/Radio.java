package com.example.georg.radiostreameralt;


public class Radio {
    private String name, url;
    private int icon;
    private boolean isRecorded;

    public Radio (String name, String url, int icon){
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.isRecorded = false;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isRecorded() {
        return isRecorded;
    }

    public void setRecorded(boolean recorded) {
        isRecorded = recorded;
    }
}
