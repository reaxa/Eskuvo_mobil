package com.example.eskuvo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("notification_message");
        if (message == null) message = "Rendelés állapota frissült";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ORDER_STATUS_CHANNEL")
                .setSmallIcon(R.drawable.placeholder)  // ugyanaz az ikon legyen mint fent
                .setContentTitle("Esküvői webshop")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }
}
