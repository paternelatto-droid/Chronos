package com.latto.chronos;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.latto.chronos.WorkManager.ReminderWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("App", "🚀 Application started, scheduling ReminderWorker...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scheduleReminderFetcherWorker(); // ✅ Ce worker va planifier les SingleNotificationWorker
        }
    }

    private void scheduleReminderFetcherWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false)
                .build();

        PeriodicWorkRequest periodicFetchRequest =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, 15, TimeUnit.MINUTES)
                        .addTag("FETCH_REMINDERS_WORKER")
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "FETCH_REMINDERS_WORKER",
                ExistingPeriodicWorkPolicy.UPDATE, // ⚠ Important pour recharger quand tu modifies le Worker
                periodicFetchRequest
        );

        Log.d("App", "⏳ ReminderWorker programmé toutes les 15 min pour planifier les SingleNotificationWorker");
    }
}
