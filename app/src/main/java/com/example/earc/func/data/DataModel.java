package com.example.earc.func.data;

public class DataModel {
    private long timestamp;
    private int decibel;

    public DataModel(int timestamp, int decibel) {
        this.timestamp = timestamp;
        this.decibel = decibel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getDecibel() {
        return decibel;
    }

    public void setDecibel(int decibel) {
        this.decibel = decibel;
    }
}
