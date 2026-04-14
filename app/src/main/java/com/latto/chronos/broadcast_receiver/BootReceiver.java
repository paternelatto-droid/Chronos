package com.latto.chronos.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.latto.chronos.WorkManager.ReminderWorker;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "📱 Boot completed detected, rescheduling ReminderWorker...");

            // Constraints identiques à App.java
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(false)
                    .build();

            PeriodicWorkRequest periodicFetchRequest =
                    new PeriodicWorkRequest.Builder(ReminderWorker.class, 15, TimeUnit.MINUTES)
                            .addTag("FETCH_REMINDERS_WORKER")
                            .setConstraints(constraints)
                            .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "FETCH_REMINDERS_WORKER",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicFetchRequest
            );

            Log.d(TAG, "⏳ ReminderWorker rescheduled after boot");
        }
    }
}

