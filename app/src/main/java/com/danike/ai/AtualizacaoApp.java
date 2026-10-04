package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

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

        final AlertDialog dialog = new AlertDialog.Builder(activity).create();

        LinearLayout principal = new LinearLayout(activity);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(34, 28, 34, 24);
        principal.setGravity(Gravity.CENTER_HORIZONTAL);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(8, 10, 18));
        fundo.setCornerRadius(28);
        fundo.setStroke(2, Color.rgb(0, 220, 255));
        principal.setBackground(fundo);

        TextView logo = new TextView(activity);
        logo.setText("⚡ DaNikeAI");
        logo.setTextColor(Color.rgb(0, 235, 255));
        logo.setTextSize(25);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tituloView = new TextView(activity);
        tituloView.setText("ATUALIZAÇÃO DISPONÍVEL");
        tituloView.setTextColor(Color.WHITE);
        tituloView.setTextSize(20);
        tituloView.setGravity(Gravity.CENTER);
        tituloView.setTypeface(null, android.graphics.Typeface.BOLD);
        tituloView.setPadding(0, 16, 0, 4);

        TextView versaoView = new TextView(activity);
        versaoView.setText("DaNikeAI " + versionName);
        versaoView.setTextColor(Color.rgb(170, 80, 255));
        versaoView.setTextSize(17);
        versaoView.setGravity(Gravity.CENTER);
        versaoView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView mensagemView = new TextView(activity);
        mensagemView.setText(
                mensagem == null || mensagem.trim().isEmpty()
                        ? "Uma nova versão está disponível para você."
                        : mensagem
        );
        mensagemView.setTextColor(Color.LTGRAY);
        mensagemView.setTextSize(15);
        mensagemView.setGravity(Gravity.CENTER);
        mensagemView.setPadding(10, 18, 10, 22);

        Button atualizar = new Button(activity);
        atualizar.setText("ATUALIZAR");
        atualizar.setTextColor(Color.WHITE);
        atualizar.setTextSize(15);
        atualizar.setAllCaps(false);
        atualizar.setTypeface(null, android.graphics.Typeface.BOLD);

        GradientDrawable fundoAtualizar = new GradientDrawable();
        fundoAtualizar.setColor(Color.rgb(25, 80, 105));
        fundoAtualizar.setCornerRadius(18);
        fundoAtualizar.setStroke(2, Color.rgb(0, 235, 255));
        atualizar.setBackground(fundoAtualizar);

        LinearLayout.LayoutParams pBotao =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        58
                );
        pBotao.setMargins(0, 4, 0, 10);
        principal.addView(logo);
        principal.addView(tituloView);
        principal.addView(versaoView);
        principal.addView(mensagemView);
        principal.addView(atualizar, pBotao);

        if (!obrigatoria) {
            Button depois = new Button(activity);
            depois.setText("LEMBRAR MAIS TARDE");
            depois.setTextColor(Color.LTGRAY);
            depois.setTextSize(14);
            depois.setAllCaps(false);

            GradientDrawable fundoDepois = new GradientDrawable();
            fundoDepois.setColor(Color.TRANSPARENT);
            fundoDepois.setCornerRadius(18);
            fundoDepois.setStroke(1, Color.rgb(90, 90, 110));
            depois.setBackground(fundoDepois);

            principal.addView(depois, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 52));

            depois.setOnClickListener(v -> dialog.dismiss());
        }

        atualizar.setOnClickListener(v -> {
            if (apkUrl == null || apkUrl.trim().isEmpty()) {
                Toast.makeText(
                        activity,
                        "Link da atualização não configurado.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            atualizar.setText("BAIXANDO...");
            atualizar.setEnabled(false);

            AppDistribuicao.baixarApk(activity, apkUrl);
        });

        dialog.setView(principal);
        dialog.setCanceledOnTouchOutside(!obrigatoria);
        if (obrigatoria) {
            dialog.setCancelable(false);
        }

        dialog.show();
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
