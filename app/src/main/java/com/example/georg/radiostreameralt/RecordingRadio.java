package com.example.georg.radiostreameralt;

/**
 * Created by georg on 6/2/2017.
 */

public class RecordingRadio {

    private String name;
    private int id;

    public RecordingRadio(String name, int id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
