package com.unathemastudios.bitrate;

class RecordingRadio {

    private String name;
    private int id;
    private long time;
    private int size;
    private long duration;
//    private boolean stopped;

    RecordingRadio(String name, int id, long time, int size, long duration){
        this.name = name;
        this.id = id;
        this.time = time;
        this.size = size;
        this.duration = duration;
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

    public long getDuration() {
        return duration;
    }
}
