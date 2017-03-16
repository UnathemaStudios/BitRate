package com.example.georg.radiostreameralt;

public class RecordingRadio {

    private String name;
    private int id;
    private long time;
    private int size;
//    private boolean stopped;

    RecordingRadio(String name, int id, long time, int size){
        this.name = name;
        this.id = id;
        this.time = time;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    int getSize() {
        return size;
    }
}
