package com.latto.chronos.models;

public class PastorAvailabilityRequest {
    public int user_id;
    public String day_of_week; // optional
    public String specific_date; // optional
    public String start_time; // "HH:mm" or "HH:mm:ss"
    public String end_time;

    public PastorAvailabilityRequest(int userId, String dayOfWeek, String specificDate, String startTime, String endTime) {
        this.user_id = userId;
        this.day_of_week = dayOfWeek;
        this.specific_date = specificDate;
        this.start_time = startTime;
        this.end_time = endTime;
    }
}
