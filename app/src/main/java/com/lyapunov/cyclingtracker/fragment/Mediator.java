package com.lyapunov.cyclingtracker.fragment;

public class Mediator {
    private static Mediator mediator = new Mediator();
    private Mediator(){};
    public static Mediator getMediator() {
        return mediator;
    }
    public enum speedM {MPH, KMPH, MS, SMC};
    public enum distM {METERS, KM, MILES, FT};
    public enum heightM {METERS, KM, MILES, FT};
    public enum timeM {SEC, MIN, HR, DAY};
    public enum accelerationM {MPS2, MILESPS2, FTPS2, GAL};

    private volatile speedM speedMeasure = speedM.MS;
    private volatile distM distMeasure = distM.METERS;
    private volatile heightM heightMeasure = heightM.METERS;
    private volatile timeM timeMeasure = timeM.SEC;
    private volatile accelerationM accelerationMeasure = accelerationM.MPS2;

    private volatile float font_size_speed = 21;
    private volatile float font_size = 14f;
    private volatile float font_size_multiplier = 1.0f;
    private volatile int font_type = 0;
    private volatile boolean isPaused = true;
    private volatile boolean settings_init = false;

    public speedM getSpeedMeasure() {
        return speedMeasure;
    }

    public void setSpeedMeasure(speedM speedMeasure) {
        mediator.speedMeasure = speedMeasure;
    }

    public distM getDistMeasure() {
        return distMeasure;
    }

    public void setDistMeasure(distM distMeasure) {
        mediator.distMeasure = distMeasure;
    }

    public heightM getHeightMeasure() {
        return heightMeasure;
    }

    public void setHeightMeasure(heightM heightMeasure) {
        mediator.heightMeasure = heightMeasure;
    }

    public timeM getTimeMeasure() {
        return timeMeasure;
    }

    public void setTimeMeasure(timeM timeMeasure) {
        mediator.timeMeasure = timeMeasure;
    }

    public accelerationM getAccelerationMeasure() {
        return accelerationMeasure;
    }

    public void setAccelerationMeasure(accelerationM accelerationMeasure) {
        mediator.accelerationMeasure = accelerationMeasure;
    }


    public int getFont_type() {
        return font_type;
    }

    public void setFont_type(int font_type) {
        this.font_type = font_type;
    }

    public float getFont_size_speed() {
        return font_size_speed;
    }

    public void setFont_size_speed(float font_size_speed) {
        this.font_size_speed = font_size_speed;
    }

    public float getFont_size() {
        return font_size;
    }

    public void setFont_size(float font_size) {
        this.font_size = font_size;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isSettings_init() {
        return settings_init;
    }

    public void setSettings_init(boolean settings_init) {
        this.settings_init = settings_init;
    }


    public float getFont_size_multiplier() {
        return font_size_multiplier;
    }

    public void setFont_size_multiplier(float font_size_multiplier) {
        this.font_size_multiplier = font_size_multiplier;
    }
}
