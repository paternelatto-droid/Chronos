package com.latto.chronos.response;

import com.google.gson.annotations.SerializedName;
import com.latto.chronos.models.ReminderModel;

import java.util.List;

public class ReminderResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("reminders")
    private List<ReminderModel> reminders;

    public boolean isSuccess() { return success; }
    public List<ReminderModel> getReminders() { return reminders; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setReminders(List<ReminderModel> reminders) { this.reminders = reminders; }
}
