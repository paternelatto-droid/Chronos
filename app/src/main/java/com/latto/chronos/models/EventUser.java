package com.latto.chronos.models;

public class EventUser {
    public int id;
    public int event_id;
    public int user_id;
    public String role_in_event;
    public String status;

    public EventUser() {}
    public EventUser(int eventId, int userId) {
        this.event_id = eventId;
        this.user_id = userId;
    }
}
