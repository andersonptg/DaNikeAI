package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public final class AtualizacaoApp {

    private AtualizacaoApp() {}

    public static void verificar(Activity activity) {

        if (activity == null || activity.isFinishing()) {
            return;
        }

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

                    Object enabledObj = dados.get("enabled");

                    if (!(enabledObj instanceof Boolean)
                            || !((Boolean) enabledObj)) {
                        return;
                    }

                    Object versionCodeObj = dados.get("versionCode");

                    if (!(versionCodeObj instanceof Long)) {
                        return;
                    }

                    long versaoRemota =
                            (Long) versionCodeObj;

                    long versaoAtual =
                            BuildConfig.VERSION_CODE;

                    if (versaoRemota <= versaoAtual) {
                        return;
                    }

                    String versionName =
                            String.valueOf(
                                    dados.getOrDefault(
                                            "versionName",
                                            "nova versão"
                                    )
                            );

                    String titulo =
                            String.valueOf(
                                    dados.getOrDefault(
                                            "title",
                                            "Nova atualização"
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

                    boolean obrigatoria = false;

                    Object mandatoryObj =
                            dados.get("mandatory");

                    if (mandatoryObj instanceof Boolean) {
                        obrigatoria =
                                (Boolean) mandatoryObj;
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

        if (activity == null
                || activity.isFinishing()
                || activity.isDestroyed()) {
            return;
        }

        String texto =
                mensagem
                        + "\n\n"
                        + "DaNikeAI "
                        + versionName;

        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity)
                        .setTitle(titulo)
                        .setMessage(texto)
                        .setPositiveButton(
                                "ATUALIZAR AGORA",
                                (dialog, which) -> {

                                    if (apkUrl == null
                                            || apkUrl.trim().isEmpty()) {

                                        Toast.makeText(
                                                activity,
                                                "Link da atualização não configurado.",
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

    private static void baixarAtualizacao(
            Activity activity,
            String apkUrl
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(apkUrl)
                    );

            activity.startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    activity,
                    "Não foi possível abrir a atualização.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
