package com.latto.chronos.WorkManager;

import static com.latto.chronos.Utils.getCurrentUserId;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.latto.chronos.api.ApiClient;
import com.latto.chronos.api.ApiService;
import com.latto.chronos.models.ReminderModel;
import com.latto.chronos.response.ReminderResponse;
import com.latto.chronos.utils.NotificationUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Worker périodique qui vérifie les rappels côté serveur et :
 * - déclenche immédiatement les notifications pour les rappels dus,
 * - ou programme un OneTimeWorkRequest (SingleNotificationWorker) pour les rappels futurs.
 */
public class ReminderWorker extends Worker {

    private static final String TAG = "ReminderWorker";

    // Keys pour Data transmis au SingleNotificationWorker
    public static final String KEY_NOTIF_ID = "notif_id";
    public static final String KEY_EVENT_ID = "event_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";

    private final ApiService apiService;
    private final Context context;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getService(this.context);
    }

    @NonNull
    @Override
    public Result doWork() {
        int userId = getCurrentUserId(context); // ta méthode utilitaire
        if (userId <= 0) {
            Log.w(TAG, "Utilisateur invalide (userId <= 0). Abort.");
            return Result.failure();
        }

        Log.d(TAG, "doWork started for userId=" + userId);

        try {
            Call<ReminderResponse> call = apiService.checkReminders(userId);
            Response<ReminderResponse> response = call.execute();

            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "API checkReminders failed: code=" + (response != null ? response.code() : "null"));
                return Result.retry();
            }

            ReminderResponse rr = response.body();
            if (!rr.isSuccess()) {
                Log.d(TAG, "API returned success=false for checkReminders");
                return Result.success(); // rien à faire
            }

            List<ReminderModel> reminders = rr.getReminders();
            if (reminders == null || reminders.isEmpty()) {
                Log.d(TAG, "Aucun rappel reçu");
                return Result.success();
            }

            long now = System.currentTimeMillis();

            for (ReminderModel r : reminders) {
                if (r == null) continue;

                // On suppose que ReminderModel.getEventDate() renvoie java.util.Date
                // (tu avais déjà utilisé ça dans ton code précédent). Si tu as String, adapte.
                if (r.getEventDate() == null) {
                    Log.w(TAG, "ReminderModel.eventDate null pour notif id=" + r.getId());
                    continue;
                }

                long eventTime = r.getEventDate().getTime();
                long reminderTime = eventTime - (r.getMinutesBefore() * 60L * 1000L);

                if (reminderTime <= now) {
                    // dû → afficher maintenant
                    Log.d(TAG, "Reminder due NOW for notifId=" + r.getId() + " (eventId=" + r.getEventId() + ")");
                    NotificationUtils.showNotification(
                            context,
                            r.getId(),
                            r.getTitle(),
                            r.getMessage(),
                            r.getEventId()
                    );
                    // Optionnel : appeler endpoint pour marquer envoyé si tu veux
                } else {
                    // futur → programmer un OneTimeWorkRequest unique
                    long delayMs = reminderTime - now;
                    scheduleSingleNotification(r, delayMs);
                }
            }

            Log.d(TAG, "doWork finished normally");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Erreur doWork: ", e);
            // retry si erreur réseau / exception non prévue
            return Result.retry();
        }
    }

    private void scheduleSingleNotification(ReminderModel r, long delayMs) {
        try {
            Log.d(TAG, "Scheduling reminder (notifId=" + r.getId() + ") in " + (delayMs / 1000) + "s");

            Data input = new Data.Builder()
                    .putInt(KEY_NOTIF_ID, r.getId())
                    .putInt(KEY_EVENT_ID, r.getEventId())
                    .putString(KEY_TITLE, r.getTitle())
                    .putString(KEY_MESSAGE, r.getMessage())
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SingleNotificationWorker.class)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(input)
                    .addTag("notify_" + r.getId())
                    .build();

            // Utilise un nom unique pour éviter duplications (si rerun)
            WorkManager.getInstance(context).enqueueUniqueWork(
                    "notify_unique_" + r.getId(),
                    ExistingWorkPolicy.KEEP,
                    request
            );
        } catch (Exception ex) {
            Log.e(TAG, "Failed to schedule SingleNotificationWorker for notifId=" + r.getId(), ex);
        }
    }
}
