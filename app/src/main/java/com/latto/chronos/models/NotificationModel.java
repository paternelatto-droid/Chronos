package com.latto.chronos.models;

import java.util.Locale;

public class NotificationModel {

    private int id;
    private int eventId;
    private String time;
    String message;

    public NotificationModel(int eventId, String time) {
        this.eventId = eventId;
        this.time = time;
    }

    public NotificationModel(String time) {
        this.time = time;
    }

    public NotificationModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Permet de définir le time à partir d’un label lisible
    public void setTimeFromLabel(String label) {
        this.time = parseReminderLabelToTime(label);
    }

    // Retourne un label lisible pour affichage
    public String getDisplayLabel() {
        return parseTimeToReminderLabel(this.time);
    }

    // --- Conversion Label → Time (pour BD) ---
    private String parseReminderLabelToTime(String label) {
        if (label.contains("minute")) {
            int minutes = Integer.parseInt(label.replaceAll("\\D+", ""));
            return String.format(Locale.FRENCH, "00:%02d:00", minutes);
        } else if (label.contains("heure")) {
            int heures = Integer.parseInt(label.replaceAll("\\D+", ""));
            return String.format(Locale.FRENCH, "%02d:00:00", heures);
        } else if (label.contains("jour")) {
            int jours = Integer.parseInt(label.replaceAll("\\D+", ""));
            return String.format(Locale.FRENCH, "%02d:00:00", jours * 24);
        }
        // valeur par défaut
        return "00:00:00";
    }

    // --- Conversion Time → Label (pour affichage) ---
    private String parseTimeToReminderLabel(String time) {
        try {
            String[] parts = time.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);

            if (h == 0 && m > 0) {
                return m + " minutes avant";
            } else if (h > 0 && m == 0) {
                return h + " heure" + (h > 1 ? "s" : "") + " avant";
            } else if (h % 24 == 0 && m == 0) {
                int jours = h / 24;
                return jours + " jour" + (jours > 1 ? "s" : "") + " avant";
            } else if (h == 0 && m == 0 && s == 0) {
                return "Au moment de l'événement";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Au moment de l'événement";
    }

}
