package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public final class AtualizacaoApp {

    private AtualizacaoApp() {}

    public static void verificar(Activity activity) {

        FirebaseFirestore.getInstance()
                .collection("config")
                .document("update")
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        return;
                    }

                    Map<String, Object> dados = documento.getData();

                    if (dados == null) {
                        return;
                    }

                    Boolean habilitada =
                            (Boolean) dados.get("enabled");

                    if (habilitada == null || !habilitada) {
                        return;
                    }

                    Long versionCodeRemoto =
                            (Long) dados.get("versionCode");

                    if (versionCodeRemoto == null) {
                        return;
                    }

                    int versaoAtual =
                            BuildConfig.VERSION_CODE;

                    if (versionCodeRemoto <= versaoAtual) {
                        return;
                    }

                    String versionName =
                            String.valueOf(
                                    dados.get("versionName")
                            );

                    String titulo =
                            String.valueOf(
                                    dados.getOrDefault(
                                            "title",
                                            "ATUALIZAÇÃO DISPONÍVEL"
                                    )
                            );

                    String mensagem =
                            String.valueOf(
                                    dados.getOrDefault(
                                            "message",
                                            "Uma nova versão do DaNikeAI está disponível."
                                    )
                            );

                    String apkUrl =
                            String.valueOf(
                                    dados.getOrDefault(
                                            "apkUrl",
                                            ""
                                    )
                            );

                    Boolean obrigatoria =
                            (Boolean) dados.get("mandatory");

                    if (obrigatoria == null) {
                        obrigatoria = false;
                    }

                    mostrarDialogo(
                            activity,
                            titulo,
                            versionName,
                            mensagem,
                            apkUrl,
                            obrigatoria
                    );
                })
                .addOnFailureListener(e ->
                        android.util.Log.e(
                                "AtualizacaoApp",
                                "Erro ao verificar atualização",
                                e
                        )
                );
    }

    private static void mostrarDialogo(
            Activity activity,
            String titulo,
            String versionName,
            String mensagem,
            String apkUrl,
            boolean obrigatoria
    ) {

        if (activity.isFinishing()) {
            return;
        }

        String texto =
                mensagem +
                "\n\nNova versão: DaNikeAI " +
                versionName;

        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity)
                        .setTitle(titulo)
                        .setMessage(texto)
                        .setPositiveButton(
                                "ATUALIZAR AGORA",
                                (dialog, which) -> {

                                    if (apkUrl == null ||
                                            apkUrl.trim().isEmpty()) {

                                        Toast.makeText(
                                                activity,
                                                "O link da atualização ainda não foi configurado.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        return;
                                    }

                                    AppDistribuicao.baixarApk(
                                            activity,
                                            apkUrl
                                    );
                                }
                        );

        if (obrigatoria) {

            builder.setCancelable(false);

        } else {

            builder.setNegativeButton(
                    "LEMBRAR DEPOIS",
                    null
            );
        }

        builder.show();
    }
}
