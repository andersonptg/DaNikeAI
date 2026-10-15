package com.danike.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class HaveOverlayService extends Service {

    private WindowManager windowManager;
    private View balao;

    private String grupoId = "";
    private String nomeGrupo = "Sala HAVE";
    private String servicoHave = "Netflix";

    private float toqueX;
    private float toqueY;
    private int inicioX;
    private int inicioY;

    private static final String CANAL_ID = "have_overlay";
    private static final int NOTIFICACAO_ID = 7421;

    @Override
    public void onCreate() {
        super.onCreate();
        iniciarForeground();
    }

    private void iniciarForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "HAVE",
                    NotificationManager.IMPORTANCE_LOW
            );

            canal.setDescription("Bolha flutuante da sala HAVE");
            canal.setShowBadge(false);

            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (nm != null) {
                nm.createNotificationChannel(canal);
            }
        }

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CANAL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle("HAVE")
                .setContentText("Sala HAVE ativa")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE);

        startForeground(NOTIFICACAO_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            grupoId = intent.getStringExtra("grupoId");
            nomeGrupo = intent.getStringExtra("nomeGrupo");
            servicoHave = intent.getStringExtra("servicoHave");

            if (grupoId == null) {
                grupoId = "";
            }

            if (nomeGrupo == null || nomeGrupo.trim().isEmpty()) {
                nomeGrupo = "Sala HAVE";
            }

            if (servicoHave == null || servicoHave.trim().isEmpty()) {
                servicoHave = "Netflix";
            }
        }

        if (balao == null) {
            mostrarBalao();
        }

        return START_STICKY;
    }

    private void mostrarBalao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {

            stopSelf();
            return;
        }

        if (balao != null) {
            return;
        }

        try {

            windowManager =
                    (WindowManager) getSystemService(WINDOW_SERVICE);

            if (windowManager == null) {
                stopSelf();
                return;
            }

            TextView view = new TextView(this);

            view.setText("HAVE");
            view.setTextColor(Color.WHITE);
            view.setTextSize(11);
            view.setGravity(Gravity.CENTER);
            view.setPadding(dp(12), dp(7), dp(12), dp(7));

            GradientDrawable fundo = new GradientDrawable();
            fundo.setColor(Color.rgb(18, 10, 35));
            fundo.setCornerRadius(dp(22));
            fundo.setStroke(dp(2), Color.rgb(180, 70, 255));

            view.setBackground(fundo);
            view.setElevation(dp(8));

            view.setOnTouchListener((v, event) -> {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        toqueX = event.getRawX();
                        toqueY = event.getRawY();

                        WindowManager.LayoutParams params =
                                (WindowManager.LayoutParams) balao.getLayoutParams();

                        inicioX = params.x;
                        inicioY = params.y;

                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float deslocamentoX =
                                event.getRawX() - toqueX;

                        float deslocamentoY =
                                event.getRawY() - toqueY;

                        WindowManager.LayoutParams movimento =
                                (WindowManager.LayoutParams) balao.getLayoutParams();

                        movimento.x =
                                inicioX - (int) deslocamentoX;

                        movimento.y =
                                inicioY + (int) deslocamentoY;

                        try {
                            windowManager.updateViewLayout(
                                    balao,
                                    movimento
                            );
                        } catch (Exception ignored) {
                        }

                        return true;

                    case MotionEvent.ACTION_UP:

                        float distanciaX =
                                Math.abs(event.getRawX() - toqueX);

                        float distanciaY =
                                Math.abs(event.getRawY() - toqueY);

                        if (distanciaX < dp(12)
                                && distanciaY < dp(12)) {

                            abrirSala();
                        }

                        return true;
                }

                return false;
            });

            balao = view;

            int tipo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tipo =
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                tipo =
                        WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                            dp(62),
                            dp(42),
                            tipo,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                            PixelFormat.TRANSLUCENT
                    );

            params.gravity = Gravity.TOP | Gravity.END;
            params.x = dp(12);
            params.y = dp(90);

            windowManager.addView(balao, params);

        } catch (Exception e) {

            balao = null;
            stopSelf();
        }
    }

    private void abrirSala() {

        Intent voltar =
                new Intent(this, SalaHAVEActivity.class);

        voltar.putExtra("grupoId", grupoId);
        voltar.putExtra("nomeGrupo", nomeGrupo);
        voltar.putExtra("servicoHave", servicoHave);

        voltar.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        try {
            startActivity(voltar);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {

        if (windowManager != null && balao != null) {

            try {
                windowManager.removeView(balao);
            } catch (Exception ignored) {
            }
        }

        balao = null;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int dp(int valor) {
        return (int) (
                valor
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }
}
