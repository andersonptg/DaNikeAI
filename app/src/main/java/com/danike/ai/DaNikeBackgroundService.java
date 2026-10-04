package com.danike.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

public class DaNikeBackgroundService extends Service {

    private static final String CHANNEL = "danike_background";

    private WindowManager windowManager;
    private DaNikeAIFaceView rostoFlutuante;

    @Override
    public void onCreate() {
        super.onCreate();

        criarCanal();

        Intent abrir = new Intent(this, IAActivity.class);

        PendingIntent pi = PendingIntent.getActivity(
                this,
                10,
                abrir,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notificacao = builder
                .setContentTitle("DaNikeAI")
                .setContentText("IZy está disponível em segundo plano.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        startForeground(1001, notificacao);

        android.content.SharedPreferences prefs =
                getSharedPreferences("DaNikeAI_IZy", MODE_PRIVATE);

        boolean rostoAtivo =
                prefs.getBoolean("rosto_flutuante", false);

        if (rostoAtivo) {
            mostrarRostoFlutuante();
        }
    }

    private void mostrarRostoFlutuante() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            return;
        }

        if (rostoFlutuante != null) {
            return;
        }

        try {
            windowManager =
                    (WindowManager) getSystemService(WINDOW_SERVICE);

            if (windowManager == null) {
                return;
            }

            rostoFlutuante = new DaNikeAIFaceView(this);

            int tipo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tipo = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                tipo = WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                            dp(230),
                            dp(230),
                            tipo,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                            PixelFormat.TRANSLUCENT
                    );

            params.gravity = Gravity.CENTER;

            windowManager.addView(rostoFlutuante, params);

        } catch (Exception e) {
            rostoFlutuante = null;
        }
    }

    private void removerRostoFlutuante() {

        if (windowManager == null || rostoFlutuante == null) {
            return;
        }

        try {
            windowManager.removeView(rostoFlutuante);
        } catch (Exception ignored) {
        }

        rostoFlutuante = null;
    }

    private int dp(int valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density + 0.5f);
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
    public void onDestroy() {

        removerRostoFlutuante();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
