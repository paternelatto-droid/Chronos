package com.latto.chronos.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ReminderModel {

    @SerializedName("id")
    private int id;

    @SerializedName("event_id")
    private int eventId;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("minutes_before")
    private int minutesBefore;

    @SerializedName("event_date")
    private Date eventDate; // date_debut convertie en Date via Gson

    // Getter & Setter
    public int getId() { return id; }
    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public int getMinutesBefore() { return minutesBefore; }
    public Date getEventDate() { return eventDate; }

    public void setId(int id) { this.id = id; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setMinutesBefore(int minutesBefore) { this.minutesBefore = minutesBefore; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }
}
