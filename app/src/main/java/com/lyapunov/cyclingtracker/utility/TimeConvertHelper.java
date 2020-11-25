package com.lyapunov.cyclingtracker.utility;

public class TimeConvertHelper {
    public String convertSecondToDay(int time) {
        StringBuilder builder = new StringBuilder();
        int day = time / 86400;
        if (day > 0) {
            builder.append(day);
            builder.append(" Day ");
            time = time % 86400;
        }
        int hour = time / 3600;
        if (day > 0 || hour > 0) {
            builder.append(hour);
            builder.append(" Hr ");
            time = time % 3600;
        }
        int min = time / 60;
        if (day > 0 || hour > 0 || min > 0) {
            builder.append(min);
            builder.append(" Min ");
            time = time % 60;
        }
        int second = time;
        if (day > 0 || hour > 0 || min > 0 || second > 0) {
            builder.append(second);
            builder.append(" S ");
        }
        return builder.toString();
    }
}
