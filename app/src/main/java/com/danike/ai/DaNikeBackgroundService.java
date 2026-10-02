package com.danike.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class DaNikeBackgroundService extends Service {

    private static final String CHANNEL = "danike_background";

    @Override
    public void onCreate() {
        super.onCreate();

        criarCanal();

        Intent abrir = new Intent(this, IAActivity.class);

        PendingIntent pi = PendingIntent.getActivity(
                this,
                10,
                abrir,
                PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notificacao = builder
                .setContentTitle("DaNikeAI")
                .setContentText("DaNikeAI está disponível em segundo plano.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        startForeground(1001, notificacao);
    }

    private void criarCanal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel canal =
                    new NotificationChannel(
                            CHANNEL,
                            "DaNikeAI em segundo plano",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager nm =
                    getSystemService(NotificationManager.class);

            if (nm != null) {
                nm.createNotificationChannel(canal);
            }
        }
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
