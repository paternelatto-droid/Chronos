package com.latto.chronos.models;

public class PastorAvailability {
    private int id;
    private int user_id;
    private String day_of_week; // "Lundi" etc or null
    private String specific_date; // "YYYY-MM-DD" or null
    private String start_time; // "HH:mm:ss"
    private String end_time;   // "HH:mm:ss"

    // getters
    public int getId() { return id; }
    public int getUser_id() { return user_id; }
    public String getDay_of_week() { return day_of_week; }
    public String getSpecific_date() { return specific_date; }
    public String getStart_time() { return start_time; }
    public String getEnd_time() { return end_time; }
}
