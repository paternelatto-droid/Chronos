package com.latto.chronos.WorkManager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.utils.NotificationUtils;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Ce Worker est déclenché EXACTEMENT à l’heure prévue.
 * 1. Affiche la notification.
 * 2. Appelle l’API : reminders/mark-sent/{notifId}
 */
public class SingleNotificationWorker extends Worker {

    private static final String TAG = "SingleNotifWorker";
    private final ApiService apiService;
    private final Context context;

    public SingleNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getService(this.context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SingleNotificationWorker triggered");

        Data input = getInputData();
        int notifId = input.getInt(ReminderWorker.KEY_NOTIF_ID, -1);
        int eventId = input.getInt(ReminderWorker.KEY_EVENT_ID, -1);
        String title = input.getString(ReminderWorker.KEY_TITLE);
        String message = input.getString(ReminderWorker.KEY_MESSAGE);

        if (notifId <= 0) {
            Log.e(TAG, "notifId invalide, abort");
            return Result.failure();
        }

        // ✅ 1. Afficher la notification
        NotificationUtils.showNotification(context, notifId, title, message, eventId);
        Log.d(TAG, "Notification displayed for notifId=" + notifId);

        // ✅ 2. Appeler l’API pour marquer comme envoyé
        try {
            Call<Void> call = apiService.markReminderAsSent(notifId);
            Response<Void> response = call.execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "markReminderAsSent OK for notifId=" + notifId);
            } else {
                Log.w(TAG, "markReminderAsSent FAILED for notifId=" + notifId + " code=" + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur appel API markReminderAsSent", e);
        }

        return Result.success();
    }
}
