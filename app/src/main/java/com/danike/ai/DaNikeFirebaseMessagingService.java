package com.danike.ai;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.concurrent.TimeUnit;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.app.Person;
import com.danike.ai.NexoAppObserver;

import com.danike.ai.NexoDiagnostic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        // NEXO_NOTIFICACAO_VISUAL_V1
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
        if (remetenteUid == null || remetenteUid.trim().isEmpty()) {
            remetenteUid = dados.get("remetente_uid");
        }

        if ((titulo == null || titulo.trim().isEmpty())
                && mensagem.getNotification() != null) {
            titulo = mensagem.getNotification().getTitle();
        }
        if ((texto == null || texto.trim().isEmpty())
                && mensagem.getNotification() != null) {
            texto = mensagem.getNotification().getBody();
        }
        if (titulo == null || titulo.trim().isEmpty()) titulo = "NEXO";
        if (texto == null || texto.trim().isEmpty()) {
            texto = "Você recebeu uma nova notificação.";
        }

        // Um identificador fixo para o aviso e para a atualizacao visual
        // impede que a mesma mensagem apareca duplicada.
        String referencia = dados.get("fila_id");
        if (referencia == null || referencia.trim().isEmpty()) {
            referencia = mensagem.getMessageId();
        }
        if (referencia == null || referencia.trim().isEmpty()) {
            referencia = String.valueOf(System.currentTimeMillis());
        }
        int avisoId = ("NEXO:" + referencia).hashCode() & 0x7fffffff;
        // NEXO_FCM_V10: só registrar como entregue se o Android puder mostrar avisos.
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.util.Log.w("NEXO_FCM", "FCM recebido, POST_NOTIFICATIONS negada");
            return;
        }
        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 24 && nm != null && !nm.areNotificationsEnabled()) {
            android.util.Log.w("NEXO_FCM", "FCM recebido, notificações do aplicativo bloqueadas");
            return;
        }
        android.util.Log.i("NEXO_FCM", "FCM_RECEBIDO tipo=" + tipo
                + " id=" + (dados.containsKey("fila_id") ? "fila" : "transport"));

        // NEXO_FCM_IDEMPOTENCIA_V9: impedir reaviso do mesmo evento/fila
        // por horas ou dias. Mensagens diferentes, mesmo texto, continuam validas.
        String idEstavel = dados.get("fila_id");
        if (idEstavel == null || idEstavel.trim().isEmpty()) idEstavel = dados.get("evento_id");
        if (idEstavel == null || idEstavel.trim().isEmpty()) idEstavel = mensagem.getMessageId();
        if (idEstavel != null && !idEstavel.trim().isEmpty()
                && jaNotificadoAntes(idEstavel.trim())) {
            android.util.Log.i("NEXO_FCM", "Reentrega ignorada por identificador de evento.");
            return;
        }


        boolean conversa = "mensagem".equals(tipo)
                && remetenteUid != null
                && !remetenteUid.trim().isEmpty();

        // PRIMEIRO mostra imediatamente o aviso, mesmo se Firestore/foto
        // estiverem lentos ou indisponiveis. Nao mexe no envio FCM.
        String tituloSeguro = conversa ? "NEXO" : titulo;
        mostrarNotificacao(tipo, tituloSeguro, texto, remetenteUid,
                null, avisoId, false);

        // DEPOIS enriquece o MESMO aviso com o perfil.
        // O callback do FCM tem tempo limitado: buscamos com prazo curto
        // dentro da propria thread de recebimento, sem callbacks soltos.
        if (conversa) {
            buscarIdentidadeNotificacao(
                    remetenteUid.trim(), tipo, tituloSeguro, texto, avisoId);
        }
    }


    private void buscarIdentidadeNotificacao(
            String remetenteUid,
            String tipo,
            String tituloOriginal,
            String texto,
            int avisoId
    ) {
        try {
            DocumentSnapshot doc = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("perfis_publicos")
                            .document(remetenteUid)
                            .get(),
                    2200,
                    TimeUnit.MILLISECONDS
            );

            if (doc == null || !doc.exists()) return;
            String nome = doc.getString("nome");
            String fotoUrl = doc.getString("fotoUrl");
            if (fotoUrl == null || fotoUrl.trim().isEmpty())
                fotoUrl = doc.getString("avatarUrl");

            String nomeFinal = nome != null
                    && !nome.trim().isEmpty()
                    && !nome.contains("@")
                    ? nome.trim() : "NEXO";
            Bitmap foto = null;
            if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
                foto = baixarFotoNotificacao(fotoUrl.trim());
            }

            if (!nomeFinal.equals(tituloOriginal) || foto != null) {
                // Atualiza, mas nao toca/avisa pela segunda vez.
                mostrarNotificacao(tipo, nomeFinal, texto, remetenteUid,
                        foto, avisoId, true);
            }
        } catch (Exception erro) {
            // A notificacao basica ja foi mostrada: falhar na foto
            // nao pode prejudicar a entrega.
            android.util.Log.w("NEXO_FCM", "Perfil indisponivel para notificacao");
        }
    }


    private Bitmap baixarFotoNotificacao(String fotoUrl) {
        HttpURLConnection conexao = null;
        try {
            URL url = new URL(fotoUrl);
            if (!"https".equalsIgnoreCase(url.getProtocol())) return null;
            conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(1300);
            conexao.setReadTimeout(1300);
            conexao.setInstanceFollowRedirects(true);
            conexao.setDoInput(true);
            if (conexao.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            // Limita o arquivo para nao gastar memoria demais.
            try (InputStream entrada = conexao.getInputStream();
                 java.io.ByteArrayOutputStream memoria =
                         new java.io.ByteArrayOutputStream()) {
                byte[] bloco = new byte[8192];
                int n, total = 0;
                while ((n = entrada.read(bloco)) != -1) {
                    total += n;
                    if (total > 1024 * 1024) return null;
                    memoria.write(bloco, 0, n);
                }
                byte[] bytes = memoria.toByteArray();
                BitmapFactory.Options opcoes = new BitmapFactory.Options();
                opcoes.inSampleSize = 2;
                Bitmap original = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length, opcoes);
                if (original == null) return null;
                Bitmap pequena = Bitmap.createScaledBitmap(
                        original, 128, 128, true);
                if (pequena != original) original.recycle();
                return pequena;
            }
        } catch (Exception ignorada) {
            return null;
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }




    /** Evita alertas repetidos de um mesmo fila_id, mesmo apos reiniciar o app. */
    private boolean jaNotificadoAntes(String eventoId) {
        synchronized (DaNikeFirebaseMessagingService.class) {
            try {
                java.security.MessageDigest sha = java.security.MessageDigest.getInstance("SHA-256");
                byte[] digest = sha.digest(eventoId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder chave = new StringBuilder("fcm_");
                for (byte b : digest) chave.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
                android.content.SharedPreferences prefs = getSharedPreferences(
                        "nexo_fcm_eventos_v9", Context.MODE_PRIVATE);
                long agora = System.currentTimeMillis();
                long anterior = prefs.getLong(chave.toString(), 0L);
                if (anterior > 0L && anterior <= agora
                        && agora - anterior < 7L * 24L * 60L * 60L * 1000L) return true;
                android.content.SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(chave.toString(), agora);
                if (prefs.getAll().size() > 512) {
                    long limite = agora - 7L * 24L * 60L * 60L * 1000L;
                    for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
                        Object valor = e.getValue();
                        if (valor instanceof Long && (Long) valor < limite) editor.remove(e.getKey());
                    }
                }
                editor.apply();
            } catch (Exception e) {
                android.util.Log.w("NEXO_FCM", "Deduplicacao indisponivel: " + e.getClass().getSimpleName());
            }
            return false;
        }
    }

    private void mostrarNotificacao(
            String tipo,
            String titulo,
            String texto,
            String remetenteUid,
            Bitmap fotoPerfil,
            int avisoId,
            boolean atualizacaoVisual
    ) {
        NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID, "Mensagens e notificações NEXO",
                    NotificationManager.IMPORTANCE_HIGH);
            canal.setDescription("Mensagens, amizades e avisos do NEXO");
            canal.enableVibration(true);
            canal.setVibrationPattern(new long[]{0, 250, 100, 250});
            canal.setShowBadge(true);
            manager.createNotificationChannel(canal);
        }

        Intent intent;
        boolean conversa = "mensagem".equals(tipo)
                && remetenteUid != null && !remetenteUid.trim().isEmpty();
        if (conversa) {
            intent = new Intent(this, ChatIndividualActivity.class);
            intent.putExtra("uid", remetenteUid.trim());
        } else {
            intent = new Intent(this, MainActivity.class);
            if (tipo != null && !tipo.trim().isEmpty()) {
                intent.putExtra("notificacao_tipo", tipo);
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, avisoId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

        Bitmap iconeGrande = fotoPerfil;
        if (iconeGrande == null) {
            iconeGrande = BitmapFactory.decodeResource(
                    getResources(), R.drawable.nexo_icon);
        }

        NotificationCompat.Builder notificacao =
                new NotificationCompat.Builder(this, CANAL_ID)
                        // Icone pequeno: N monocromatico do NEXO.
                        .setSmallIcon(R.drawable.ic_stat_danike)
                        // Avatar do contato ou icone do proprio aplicativo.
                        .setLargeIcon(iconeGrande)
                        .setContentTitle(titulo)
                        .setContentText(texto)
                        .setColor(android.graphics.Color.rgb(15, 135, 255))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(conversa
                                ? NotificationCompat.CATEGORY_MESSAGE
                                : NotificationCompat.CATEGORY_SOCIAL)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setContentIntent(pendingIntent);

        if (conversa) {
            // MessagingStyle recebe o USUARIO LOCAL ('Você'), e cada
            // mensagem deve ser atribuida a OUTRA PESSOA (remetente).
            Person usuarioLocal = new Person.Builder()
                    .setName("Você")
                    .build();
            Person.Builder pessoaBuilder = new Person.Builder()
                    .setName(titulo)
                    .setKey(remetenteUid.trim());
            if (fotoPerfil != null) {
                pessoaBuilder.setIcon(
                        IconCompat.createWithBitmap(fotoPerfil));
            }
            Person remetente = pessoaBuilder.build();
            NotificationCompat.MessagingStyle estilo =
                    new NotificationCompat.MessagingStyle(usuarioLocal)
                            .addMessage(texto, System.currentTimeMillis(),
                                    remetente)
                            .setGroupConversation(false);
            notificacao.setStyle(estilo);
        } else {
            notificacao.setStyle(
                    new NotificationCompat.BigTextStyle().bigText(texto));
        }

        // Evitar duas vibracoes/toques ao buscar a foto e o nome.
        if (atualizacaoVisual) notificacao.setSilent(true);
        manager.notify(avisoId, notificacao.build());
    }


}
