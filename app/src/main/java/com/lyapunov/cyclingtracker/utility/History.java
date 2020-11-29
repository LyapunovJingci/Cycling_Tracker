package com.lyapunov.cyclingtracker.utility;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class History implements Parcelable {
    private long duration;
    private double distance;
    private double avg_speed;
    private double high_speed;
    private double rate;
    private Timestamp time;

    public History(long duration, double distance, double avg_speed, double high_speed, double rate, Timestamp time) {
        this.duration = duration;
        this.distance = distance;
        this.avg_speed = avg_speed;
        this.high_speed = high_speed;
        this.rate = rate;
        this.time = time;
    }

    protected History(Parcel in) {
        duration = in.readInt();
        distance = in.readDouble();
        avg_speed = in.readDouble();
        high_speed = in.readDouble();
        rate = in.readDouble();
        time = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<History> CREATOR = new Creator<History>() {
        @Override
        public History createFromParcel(Parcel in) {
            return new History(in);
        }

        @Override
        public History[] newArray(int size) {
            return new History[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(duration);
        parcel.writeDouble(distance);
        parcel.writeDouble(avg_speed);
        parcel.writeDouble(high_speed);
        parcel.writeDouble(rate);
        parcel.writeParcelable(time, i);
    }
    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAvg_speed() {
        return avg_speed;
    }

    public void setAvg_speed(double avg_speed) {
        this.avg_speed = avg_speed;
    }

    public double getHigh_speed() {
        return high_speed;
    }

    public void setHigh_speed(double high_speed) {
        this.high_speed = high_speed;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
