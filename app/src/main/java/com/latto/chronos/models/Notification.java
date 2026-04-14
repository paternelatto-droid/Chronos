package com.latto.chronos.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    public int id;
    @SerializedName("event_id")
    public int eventId;
    @SerializedName("user_id")
    public int userId;
    public String title;
    public String message;
    @SerializedName("is_read")
    public int isRead;
    @SerializedName("sent_at")
    public String sentAt;

    // Client-side fields for creating notifications
    @SerializedName("channelId")
    public int channelId;
    @SerializedName("time")
    public String time;
}
