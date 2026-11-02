package com.danike.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

/** Convite contextual na Home. Não solicita câmera/microfone sem uso. */
public final class NexoPermissoesHome {
    private static final String PREF = "nexo_permissoes_home_v10";
    private static final String MOSTRADO = "convite_notificacoes_exibido";
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static boolean agendado = false;
    private NexoPermissoesHome() {}

    public static void verificar(Activity atividade) {
        if (atividade == null || agendado) return;
        agendado = true;
        HANDLER.postDelayed(() -> {
            agendado = false;
            tentarMostrar(atividade, 0);
        }, 1700L);
    }

    private static boolean ativadas(Activity a) {
        NotificationManager nm = (NotificationManager)
                a.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean sistemaAtivo = Build.VERSION.SDK_INT < 24 || nm == null
                || nm.areNotificationsEnabled();
        boolean autorizou = Build.VERSION.SDK_INT < 33
                || a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        return sistemaAtivo && autorizou;
    }

    private static void tentarMostrar(Activity a, int tentativas) {
        if (a.isFinishing() || (Build.VERSION.SDK_INT >= 17 && a.isDestroyed())) return;
        if (ativadas(a)) return;
        if (a.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean(MOSTRADO, false)) return;

        // Evitar que o diálogo fique escondido por cima da tela de atualização/login.
        if (!a.hasWindowFocus()) {
            if (tentativas < 6) HANDLER.postDelayed(
                    () -> tentarMostrar(a, tentativas + 1), 2000L);
            return;
        }
        try {
            AlertDialog dialog = new AlertDialog.Builder(a)
                    .setTitle("Ativar notificações NEXO")
                    .setMessage("Receba mensagens das Conexões e avisos no celular. "
                            + "Câmera e microfone só serão solicitados quando você usar esses recursos.")
                    .setPositiveButton("ATIVAR", (d, w) -> {
                        if (Build.VERSION.SDK_INT >= 33 &&
                                a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                    != PackageManager.PERMISSION_GRANTED) {
                            a.requestPermissions(
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 4168);
                        } else {
                            abrirConfiguracoes(a);
                        }
                    })
                    .setNegativeButton("AGORA NÃO", (d, w) -> d.dismiss())
                    .create();
            dialog.setOnShowListener(d -> {
                a.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                        .edit().putBoolean(MOSTRADO, true).apply();
                Log.i("NEXO_PERMISSOES", "Convite de notificações exibido na Home");
            });
            dialog.show();
        } catch (Exception e) {
            Log.w("NEXO_PERMISSOES", "Convite indisponível: " + e.getClass().getSimpleName());
        }
    }

    private static void abrirConfiguracoes(Activity a) {
        try {
            Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            i.putExtra(Settings.EXTRA_APP_PACKAGE, a.getPackageName());
            a.startActivity(i);
        } catch (Exception e) {
            a.startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }
}
