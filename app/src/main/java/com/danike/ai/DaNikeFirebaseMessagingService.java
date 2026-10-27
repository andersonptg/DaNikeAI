package com.danike.ai;

import com.danike.ai.NexoAppObserver;

import com.danike.ai.NexoDiagnostic;

import android.graphics.BitmapFactory;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class DaNikeFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CANAL_ID = "danike_notificacoes";

    @Override
    public void onNewToken(String token) {
        long nexoInicio = android.os.SystemClock.elapsedRealtime();
        NexoAppObserver.evento(
                getApplication(),
                "NEXO_FCM_TOKEN_REFRESH",
                "INFO",
                "FCM",
                "onNewToken",
                "Token FCM renovado."
        );

        super.onNewToken(token);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        Map<String, Object> dados = new HashMap<>();
        dados.put("token", token);
        dados.put("plataforma", "android");
        dados.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("dispositivos")
                .document(token)
                .set(dados);
    }

    @Override
    public void onMessageReceived(RemoteMessage mensagem) {
        long nexoInicioMensagem = android.os.SystemClock.elapsedRealtime();
        NexoAppObserver.evento(
                getApplication(),
                "NEXO_FCM_MESSAGE_RECEIVED",
                "INFO",
                "FCM",
                "onMessageReceived",
                "Mensagem FCM recebida."
        );

        super.onMessageReceived(mensagem);

        Map<String, String> dados = mensagem.getData();

        String tipo = dados.get("tipo");
        String titulo = dados.get("titulo");
        String texto = dados.get("texto");
        String remetenteUid = dados.get("remetenteUid");

        if (titulo == null || titulo.trim().isEmpty()) {
            if (mensagem.getNotification() != null) {
                titulo = mensagem.getNotification().getTitle();
            }
        }

        if (texto == null || texto.trim().isEmpty()) {
            if (mensagem.getNotification() != null) {
                texto = mensagem.getNotification().getBody();
            }
        }

        if (titulo == null || titulo.trim().isEmpty()) {
            titulo = "NEXO";
        }

        if (texto == null || texto.trim().isEmpty()) {
            texto = "Você recebeu uma nova notificação.";
        }

        mostrarNotificacao(tipo, titulo, texto, remetenteUid);
    }

    private void buscarIdentidadeNotificacao(
            String remetenteUid,
            String tipo,
            String titulo,
            String texto
    ) {
        final String tituloOriginal = titulo;
        final String textoFinal = texto;

        FirebaseFirestore.getInstance()
                .collection("perfis_publicos")
                .document(remetenteUid)
                .get()
                .addOnSuccessListener(doc -> {
                    String nome = doc.getString("nome");
                    String tituloFinal = tituloOriginal;

                    if (nome != null && !nome.trim().isEmpty()) {
                        tituloFinal = nome.trim();
                    }

                    mostrarNotificacao(
                            tipo,
                            tituloFinal,
                            textoFinal,
                            remetenteUid
                    );
                })
                .addOnFailureListener(e ->
                        mostrarNotificacao(
                                tipo,
                                tituloOriginal,
                                textoFinal,
                                remetenteUid
                        )
                );
    }

    private void mostrarNotificacao(
            String tipo,
            String titulo,
            String texto,
            String remetenteUid
    ) {

        NotificationManager manager =
                (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Notificações DaNikeAI",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Notificações instantâneas do DaNikeAI");
            canal.enableVibration(true);
            canal.setVibrationPattern(new long[]{0, 250, 100, 250});
            canal.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            canal.setShowBadge(true);
            // Configurações aplicadas para velocidade máxima

            manager.createNotificationChannel(canal);
        }

        Intent intent;

        if ("mensagem".equals(tipo)
                && remetenteUid != null
                && !remetenteUid.trim().isEmpty()) {

            intent = new Intent(
                    this,
                    ChatIndividualActivity.class
            );

            intent.putExtra(
                    "uid",
                    remetenteUid.trim()
            );

        } else {

            intent = new Intent(
                    this,
                    MainActivity.class
            );

            if (tipo != null && !tipo.trim().isEmpty()) {
                intent.putExtra(
                        "notificacao_tipo",
                        tipo
                );
            }
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder notificacao =
                new NotificationCompat.Builder(
                        this,
                        CANAL_ID
                )
                        .setSmallIcon(R.drawable.ic_stat_danike)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.nexo_icon))
                                        .setContentTitle(titulo)
                        .setContentText(texto)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(texto)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        manager.notify(
                (int) System.currentTimeMillis(),
                notificacao.build()
        );
    }
}
