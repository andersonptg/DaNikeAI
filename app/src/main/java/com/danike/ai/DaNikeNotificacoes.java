package com.danike.ai;

import com.danike.ai.NexoAppObserver;

import com.danike.ai.NexoDiagnostic;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;
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

    public static void enviarNotificacaoImediata(
            String uid,
            String tipo,
            String titulo,
            String texto,
            String remetenteUid,
            String filaId
    ) {


        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            final String endpoint =
                    "https://danikeai-brain.onrender.com/notificar";

            String json = "{"
                    + "\"uid\":\"" + escaparJson(uid) + "\","
                    + "\"tipo\":\"" + escaparJson(tipo) + "\","
                    + "\"titulo\":\"" + escaparJson(titulo) + "\","
                    + "\"texto\":\"" + escaparJson(texto) + "\","
                    + "\"remetente_uid\":\"" + escaparJson(remetenteUid) + "\","
                    + "\"fila_id\":\"" + escaparJson(filaId) + "\""
                    + "}";

            boolean sucesso = false;

            try {
                /*
                 * O Render Free pode estar acordando após inatividade.
                 * Tentamos duas vezes para tolerar cold start.
                 */
                for (int tentativa = 1; tentativa <= 2; tentativa++) {
                    HttpURLConnection conexao = null;

                    try {
                        Log.e(
                                TAG,
                                "NOTIFICACAO IMEDIATA: tentativa "
                                        + tentativa
                        );

                        URL url = new URL(endpoint);
                        conexao = (HttpURLConnection) url.openConnection();

                        conexao.setRequestMethod("POST");

                        /*
                         * Tempo maior para permitir o cold start
                         * do serviço no Render.
                         */
                        conexao.setConnectTimeout(90000);
                        conexao.setReadTimeout(90000);

                        conexao.setDoOutput(true);

                        conexao.setRequestProperty(
                                "Content-Type",
                                "application/json"
                        );

                        conexao.setRequestProperty(
                                "X-DaNikeAI-Secret",
                                new String(
                                        java.util.Base64.getDecoder().decode(
                                                "UWgxSFBEX25xNUhpUTB4Y0JSQXRWVmU5N0RhMWJrUG5NN2JpZHpIX0d0QSAgICAgICAgICAg"
                                        ),
                                        StandardCharsets.UTF_8
                                )
                        );

                        try (OutputStream saida =
                                     conexao.getOutputStream()) {
                            saida.write(
                                    json.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );
                            saida.flush();
                        }

                        int codigo = conexao.getResponseCode();

                        Log.e(
                                TAG,
                                "NOTIFICACAO IMEDIATA: HTTP "
                                        + codigo
                                        + " tentativa="
                                        + tentativa
                        );

                        if (codigo >= 200 && codigo < 300) {
                            sucesso = true;
                            break;
                        }

                        /*
                         * Erros 4xx normalmente são definitivos.
                         * Não repetir nesses casos.
                         */
                        if (codigo >= 400 && codigo < 500) {
                            break;
                        }

                    } catch (java.net.SocketTimeoutException e) {
                        Log.e(
                                TAG,
                                "NOTIFICACAO IMEDIATA: TIMEOUT tentativa="
                                        + tentativa,
                                e
                        );

                    } catch (Exception e) {
                        Log.e(
                                TAG,
                                "NOTIFICACAO IMEDIATA: ERRO tentativa="
                                        + tentativa,
                                e
                        );

                    } finally {
                        if (conexao != null) {
                            conexao.disconnect();
                        }
                    }

                    if (!sucesso && tentativa < 2) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                if (sucesso) {
                    Log.e(
                            TAG,
                            "NOTIFICACAO IMEDIATA: SUCESSO"
                    );
                } else {
                    Log.e(
                            TAG,
                            "NOTIFICACAO IMEDIATA: FALHA APOS TENTATIVAS"
                    );
                }

            } finally {
                executor.shutdown();
            }
        });
    }

    private static String escaparJson(String valor) {
        if (valor == null) return "";
        return JSONObject.quote(valor).substring(1, JSONObject.quote(valor).length() - 1);
    }
}
