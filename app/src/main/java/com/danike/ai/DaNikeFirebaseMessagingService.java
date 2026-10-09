package com.danike.ai;

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
        super.onMessageReceived(mensagem);

        String titulo = mensagem.getNotification() != null
                ? mensagem.getNotification().getTitle()
                : "DaNikeAI";

        String texto = mensagem.getNotification() != null
                ? mensagem.getNotification().getBody()
                : "Você recebeu uma nova notificação.";

        mostrarNotificacao(titulo, texto);
    }

    private void mostrarNotificacao(String titulo, String texto) {

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    "Notificações DaNikeAI",
                    NotificationManager.IMPORTANCE_HIGH
            );

            canal.setDescription("Notificações do DaNikeAI");
            manager.createNotificationChannel(canal);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificacao =
                new NotificationCompat.Builder(this, CANAL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(titulo)
                        .setContentText(texto)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(texto)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        manager.notify(
                (int) System.currentTimeMillis(),
                notificacao.build()
        );
    }
}
