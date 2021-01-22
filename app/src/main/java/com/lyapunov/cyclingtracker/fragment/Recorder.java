package com.lyapunov.cyclingtracker.fragment;

public class Recorder {
    private static Recorder recorder = new Recorder();
    private Recorder(){};
    public static Recorder getRecorder() {
        return recorder;
    }

    private volatile StringBuffer record = new StringBuffer();


    public void updateRecord(String latlng) {
        record.append('|').append(latlng);
    }

    public String getFinalRecord() {
        return record.toString();
    }

}
