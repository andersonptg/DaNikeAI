package com.danike.ai;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NexoDiagnostic {

    private static final String TAG = "NexoDiagnostic";
    private static final String ENDPOINT =
            "https://nippy-nexo-stream-hub.base44.app/functions/diagnosticEvent";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private NexoDiagnostic() {}

    public static void registrar(
            Context context,
            String screen,
            String eventType,
            String severity,
            String component,
            String action,
            Integer httpStatus,
            String errorCode,
            String messageSafe,
            long durationMs
    ) {
        if (context == null || BuildConfig.NEXO_DIAG_KEY == null
                || BuildConfig.NEXO_DIAG_KEY.trim().isEmpty()) {
            return;
        }

        Context appContext = context.getApplicationContext();

        EXECUTOR.execute(() -> {
            HttpURLConnection conexao = null;

            try {
                JSONObject evento = new JSONObject();

                evento.put("timestamp", System.currentTimeMillis());
                evento.put("deviceIdHash", hashDeviceId(appContext));
                evento.put("appVersion", BuildConfig.VERSION_NAME);
                evento.put("versionCode", BuildConfig.VERSION_CODE);
                evento.put("androidVersion", Build.VERSION.RELEASE);
                evento.put("deviceModel",
                        Build.MANUFACTURER + " " + Build.MODEL);
                evento.put("screen", safe(screen));
                evento.put("eventType", safe(eventType));
                evento.put("severity", safe(severity));
                evento.put("component", safe(component));
                evento.put("action", safe(action));

                if (httpStatus == null) {
                    evento.put("httpStatus", JSONObject.NULL);
                } else {
                    evento.put("httpStatus", httpStatus);
                }

                evento.put("errorCode", safe(errorCode));
                evento.put("messageSafe", safe(messageSafe));
                evento.put("durationMs", Math.max(0, durationMs));
                evento.put("environment", "production");

                URL url = new URL(ENDPOINT);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(15000);
                conexao.setDoOutput(true);
                conexao.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                conexao.setRequestProperty("Accept", "application/json");
                conexao.setRequestProperty("x-nexo-key",
                        BuildConfig.NEXO_DIAG_KEY.trim());

                byte[] dados = evento.toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(dados);
                    saida.flush();
                }

                int codigo = conexao.getResponseCode();

                Log.d(TAG, "Evento enviado. HTTP " + codigo);

            } catch (Exception erro) {
                Log.d(TAG, "Falha no envio diagnostico: "
                        + erro.getClass().getSimpleName());
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        });
    }

    private static String safe(String valor) {
        if (valor == null) {
            return "";
        }

        String limpo = valor
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();

        if (limpo.length() > 500) {
            limpo = limpo.substring(0, 500);
        }

        return limpo;
    }

    private static String hashDeviceId(Context context) {
        try {
            String androidId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            if (androidId == null || androidId.trim().isEmpty()) {
                androidId = Build.MANUFACTURER + ":" + Build.MODEL;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    androidId.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder resultado = new StringBuilder();

            for (byte b : hash) {
                resultado.append(String.format("%02x", b));
            }

            return resultado.toString();

        } catch (Exception erro) {
            return "unknown";
        }
    }
}
