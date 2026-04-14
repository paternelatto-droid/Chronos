package com.latto.chronos.models;

public class EventAccess {
    private int id;
    private int eventId;
    private int userId;

    public EventAccess() {}

    public EventAccess(int eventId, int userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
