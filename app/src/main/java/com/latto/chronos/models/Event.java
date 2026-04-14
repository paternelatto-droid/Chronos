package com.latto.chronos.models;


import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {
    public int id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("event_type_id")
    public Integer eventTypeId;
    public String title;
    public String description;
    @SerializedName("date_debut")
    public String dateDebut;    // yyyy-MM-dd for DB
    @SerializedName("date_fin")
    public String dateFin;
    public String location;
    public int color;
    public List<Integer> reminders;
    public String visibility;
    public String status;

    // client-only (not in DB): userIds and notifications to send
    @SerializedName("userIds")
    public List<Integer> userIds;

    @SerializedName("notifications")
    public List<Notification> notifications;

    // getters / setters si tu veux, ou utilises champs publics

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Integer eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }



        public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Integer> getReminders() {
        return reminders;
    }

    public void setReminders(List<Integer> reminders) {
        this.reminders = reminders;
    }

}
