package com.example.georg.radiostreameralt;

/**
 * Created by georg on 23/1/2017.
 */

public class Radio {
    private String name, url;
    private int icon;

    public Radio (String name, String url, int icon){
        this.name = name;
        this.url = url;
        this.icon = icon;
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
}
