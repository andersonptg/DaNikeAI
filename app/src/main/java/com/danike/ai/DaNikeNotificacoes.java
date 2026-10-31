package com.danike.ai;

import com.danike.ai.NexoAppObserver;

import com.danike.ai.NexoDiagnostic;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DaNikeNotificacoes {

    private static final String TAG = "DaNikeFCM";

    private DaNikeNotificacoes() {
    }

    public static void registrarTokenFCM(Context context) {

        Log.e(TAG, "FCM: INICIO DO REGISTRO");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "FCM: NENHUM USUARIO AUTENTICADO");
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        Log.e(TAG, "FCM: usuario autenticado UID=" + uid);
        Log.e(TAG, "FCM: solicitando token...");

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {

                    if (token == null || token.trim().isEmpty()) {
                        Log.e(TAG, "FCM: ERRO - TOKEN VAZIO");
                        return;
                    }

                    Log.e(TAG, "FCM: TOKEN OBTIDO - tamanho=" + token.length());
                    Log.e(TAG, "FCM: salvando no Firestore...");

                    salvarToken(token, uid, context);
                    registrarNaCentral(token, context);
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "FCM: ERRO AO OBTER TOKEN");
                    Log.e(TAG, "FCM: tipo=" + e.getClass().getName());
                    Log.e(TAG, "FCM: mensagem=" + e.getMessage(), e);
                });
    }

    private static void salvarToken(String token, String uid, Context context) {

        Map<String, Object> dados = new HashMap<>();

        dados.put("token", token);
        dados.put("plataforma", "android");
        dados.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("dispositivos")
                .document(token)
                .set(dados)
                .addOnSuccessListener(unused -> {
                    Log.e(TAG, "FCM: SUCESSO - DISPOSITIVO SALVO NO FIRESTORE");
                })
                .addOnFailureListener(e -> {

                    Log.e(TAG, "FCM: ERRO AO SALVAR NO FIRESTORE");
                    Log.e(TAG, "FCM: tipo=" + e.getClass().getName());
                    Log.e(TAG, "FCM: mensagem=" + e.getMessage(), e);
                });
    }

    private static void registrarNaCentral(String fcmToken, Context context) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Log.e(TAG, "CENTRAL DEVICES: nenhum usuario autenticado");
            return;
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {
                    if (resultado == null || resultado.getToken() == null) {
                        Log.e(TAG, "CENTRAL DEVICES: Firebase ID Token vazio");
                        return;
                    }

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    executor.execute(() -> {
                        HttpURLConnection conexao = null;

                        try {
                            final String endpoint =
                                    "https://little-nexo-core-hub.base44.app/functions/nexoApi"
                                            + "?route=/api/v1/devices";

                            JSONObject json = new JSONObject();
                            json.put("token", fcmToken);
                            json.put("deviceId",
                                    android.provider.Settings.Secure.getString(
                                            context.getContentResolver(),
                                            android.provider.Settings.Secure.ANDROID_ID
                                    ));
                            json.put("platform", "android");
                            json.put("manufacturer", android.os.Build.MANUFACTURER);
                            json.put("model", android.os.Build.MODEL);
                            json.put("appVersion", BuildConfig.VERSION_NAME);
                            json.put("versionCode", BuildConfig.VERSION_CODE);

                            Log.e(TAG,
                                    "CENTRAL DEVICES: registrando dispositivo");

                            URL url = new URL(endpoint);
                            conexao = (HttpURLConnection) url.openConnection();

                            conexao.setRequestMethod("POST");
                            conexao.setConnectTimeout(15000);
                            conexao.setReadTimeout(20000);
                            conexao.setDoOutput(true);

                            conexao.setRequestProperty(
                                    "Content-Type",
                                    "application/json"
                            );

                            conexao.setRequestProperty(
                                    "Authorization",
                                    "Bearer " + resultado.getToken()
                            );

                            byte[] corpo =
                                    json.toString().getBytes(StandardCharsets.UTF_8);

                            try (OutputStream saida = conexao.getOutputStream()) {
                                saida.write(corpo);
                                saida.flush();
                            }

                            int codigo = conexao.getResponseCode();

                            Log.e(TAG,
                                    "CENTRAL DEVICES: HTTP " + codigo);

                            if (codigo >= 200 && codigo < 300) {
                                Log.e(TAG,
                                        "CENTRAL DEVICES: DISPOSITIVO REGISTRADO");
                            } else {
                                Log.e(TAG,
                                        "CENTRAL DEVICES: FALHA HTTP " + codigo);
                            }

                        } catch (Exception e) {
                            Log.e(TAG,
                                    "CENTRAL DEVICES: ERRO AO REGISTRAR",
                                    e);
                        } finally {
                            if (conexao != null) {
                                conexao.disconnect();
                            }
                            executor.shutdown();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(TAG,
                                "CENTRAL DEVICES: erro ao obter Firebase ID Token",
                                e)
                );
    }

    public static void enviarNotificacaoImediata(
            String uid,
            String tipo,
            String titulo,
            String texto,
            String remetenteUid,
            String filaId
    ) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Log.e(TAG, "NOTIFICACAO CENTRAL: nenhum usuario autenticado");
            return;
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {
                    if (resultado == null || resultado.getToken() == null) {
                        Log.e(TAG, "NOTIFICACAO CENTRAL: ID TOKEN vazio");
                        return;
                    }

                    String token = resultado.getToken();

                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    executor.execute(() -> {
                        HttpURLConnection conexao = null;

                        try {
                            final String endpoint =
                                    "https://little-nexo-core-hub.base44.app/functions/nexoApi"
                                            + "?route=/api/v1/notifications/send";

                            String json = "{"
                                    + "\"uid\":\"" + escaparJson(uid) + "\","
                                    + "\"tipo\":\"" + escaparJson(tipo) + "\","
                                    + "\"titulo\":\"" + escaparJson(titulo) + "\","
                                    + "\"texto\":\"" + escaparJson(texto) + "\","
                                    + "\"remetente_uid\":\"" + escaparJson(remetenteUid) + "\","
                                    + "\"fila_id\":\"" + escaparJson(filaId) + "\""
                                    + "}";

                            Log.e(TAG,
                                    "NOTIFICACAO CENTRAL: enviando para UID=" + uid);

                            URL url = new URL(endpoint);
                            conexao = (HttpURLConnection) url.openConnection();

                            conexao.setRequestMethod("POST");
                            conexao.setConnectTimeout(15000);
                            conexao.setReadTimeout(20000);
                            conexao.setDoOutput(true);

                            conexao.setRequestProperty(
                                    "Content-Type",
                                    "application/json"
                            );

                            conexao.setRequestProperty(
                                    "Authorization",
                                    "Bearer " + token
                            );

                            try (OutputStream saida = conexao.getOutputStream()) {
                                saida.write(
                                        json.getBytes(StandardCharsets.UTF_8)
                                );
                                saida.flush();
                            }

                            int codigo = conexao.getResponseCode();

                            Log.e(TAG,
                                    "NOTIFICACAO CENTRAL: HTTP " + codigo);

                            if (codigo >= 200 && codigo < 300) {
                                Log.e(TAG,
                                        "NOTIFICACAO CENTRAL: SUCESSO");
                            } else {
                                Log.e(TAG,
                                        "NOTIFICACAO CENTRAL: FALHA HTTP "
                                                + codigo);
                            }

                        } catch (Exception e) {
                            Log.e(TAG,
                                    "NOTIFICACAO CENTRAL: ERRO",
                                    e);
                        } finally {
                            if (conexao != null) {
                                conexao.disconnect();
                            }
                            executor.shutdown();
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(TAG,
                                "NOTIFICACAO CENTRAL: erro ao obter Firebase ID Token",
                                e)
                );
    }

    private static String escaparJson(String valor) {
        if (valor == null) return "";
        return JSONObject.quote(valor).substring(1, JSONObject.quote(valor).length() - 1);
    }
}
