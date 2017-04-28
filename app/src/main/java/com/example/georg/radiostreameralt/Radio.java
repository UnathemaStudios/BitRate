package com.example.georg.radiostreameralt;


class Radio {
    private String name, url;
    private int icon;
    private boolean isRecorded;

    Radio (String name, String url, int icon){
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.isRecorded = false;
    }

    public String getName() {
        return name;
    }

    String getUrl() {
        return url;
    }

    public int getIcon() {
        return icon;
    }

    boolean isRecorded() {
        return isRecorded;
    }

    void setRecorded(boolean recorded) {
        isRecorded = recorded;
    }
}
