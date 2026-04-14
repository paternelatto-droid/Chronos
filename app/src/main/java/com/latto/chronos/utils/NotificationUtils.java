package com.latto.chronos.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.latto.chronos.MainActivity;
import com.latto.chronos.R;

public final class NotificationUtils {

    private static final String CHANNEL_ID = "reminder_channel";
    private static final String CHANNEL_NAME = "Event Reminders";
    private static final String CHANNEL_DESC = "Notifications pour les rappels d'événements";

    private NotificationUtils() {}

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel c = nm.getNotificationChannel(CHANNEL_ID);
            if (c == null) {
                c = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                c.setDescription(CHANNEL_DESC);
                c.setShowBadge(true);
                nm.createNotificationChannel(c);
            }
        }
    }

    public static void showNotification(Context ctx, int notifId, String title, String message, int eventId) {
        ensureChannel(ctx);

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra("eventId", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo1)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notifId, builder.build());
    }
}
