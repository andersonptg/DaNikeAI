package com.danike.ai;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class AppErrorLogger {

    private static boolean iniciado = false;

    private AppErrorLogger() {}

    public static synchronized void iniciar(Context context) {
        if (iniciado) return;

        iniciado = true;

        final Thread.UncaughtExceptionHandler original =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {

            registrar(
                    context,
                    "CRASH",
                    "Exceção não capturada",
                    throwable,
                    thread.getName()
            );

            if (original != null) {
                original.uncaughtException(thread, throwable);
            }
        });
    }

    public static void registrar(
            Context context,
            String tipo,
            String mensagem,
            Throwable erro,
            String telaOuAcao
    ) {
        try {
            Map<String, Object> dados = new HashMap<>();

            FirebaseAuth auth = FirebaseAuth.getInstance();

            String email = "não autenticado";
            String uid = "—";

            if (auth.getCurrentUser() != null) {
                email = String.valueOf(auth.getCurrentUser().getEmail());
                uid = auth.getCurrentUser().getUid();
            }

            StackTraceElement local = encontrarOrigem(erro);

            dados.put("tipo", tipo);
            dados.put("mensagem", mensagem == null ? "Sem mensagem" : mensagem);
            dados.put("tela", telaOuAcao == null ? "desconhecida" : telaOuAcao);
            dados.put("arquivo", local == null ? "desconhecido" : local.getFileName());
            dados.put("metodo", local == null ? "desconhecido" : local.getMethodName());
            dados.put("linha", local == null ? -1 : local.getLineNumber());
            dados.put("thread", Thread.currentThread().getName());
            dados.put("email", email);
            dados.put("uid", uid);
            dados.put("stackTrace", stackTrace(erro));
            dados.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("diagnosticos")
                    .add(dados);

        } catch (Exception ignorado) {
            // O logger nunca deve derrubar o aplicativo.
        }
    }

    private static StackTraceElement encontrarOrigem(Throwable erro) {

        if (erro == null || erro.getStackTrace() == null) {
            return null;
        }

        for (StackTraceElement item : erro.getStackTrace()) {
            if (item.getClassName().startsWith("com.danike.ai")) {
                return item;
            }
        }

        StackTraceElement[] lista = erro.getStackTrace();

        return lista.length > 0 ? lista[0] : null;
    }

    private static String stackTrace(Throwable erro) {

        if (erro == null) {
            return "";
        }

        StringBuilder texto = new StringBuilder();

        texto.append(erro.toString()).append("\n");

        for (StackTraceElement item : erro.getStackTrace()) {
            texto.append("at ")
                    .append(item.toString())
                    .append("\n");
        }

        return texto.toString();
    }
}
