package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public final class AtualizacaoApp {
    private AtualizacaoApp() {}

    public static void verificar(Activity activity) {
        if (activity == null || activity.isFinishing()) return;

        FirebaseFirestore.getInstance()
                .collection("config")
                .document("update")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Map<String, Object> dados = doc.getData();
                    if (dados == null) return;

                    Object enabledObj = dados.get("enabled");
                    if (!(enabledObj instanceof Boolean) || !((Boolean) enabledObj)) return;

                    Object versionCodeObj = dados.get("versionCode");
                    long versaoRemota;
                    if (versionCodeObj instanceof Number) {
                        versaoRemota = ((Number) versionCodeObj).longValue();
                    } else return;

                    long versaoAtual = BuildConfig.VERSION_CODE;
                    if (versaoRemota <= versaoAtual) return;

                    String versionName = String.valueOf(dados.getOrDefault("versionName", "nova versão"));
                    String titulo = String.valueOf(dados.getOrDefault("title", "Nova atualização disponível"));
                    String mensagem = String.valueOf(dados.getOrDefault("message", "Uma nova versão do DaNikeAI está disponível com melhorias incríveis."));
                    String apkUrl = String.valueOf(dados.getOrDefault("apkUrl", ""));
                    boolean obrigatoria = false;
                    Object mandatoryObj = dados.get("mandatory");
                    if (mandatoryObj instanceof Boolean) obrigatoria = (Boolean) mandatoryObj;

                    mostrarDialogoPremium(activity, titulo, versionName, mensagem, apkUrl, obrigatoria);
                });
    }

    private static void mostrarDialogoPremium(Activity activity, String titulo, String versionName, String mensagem, String apkUrl, boolean obrigatoria) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        final AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_Translucent_NoTitleBar).create();
        
        // Fundo blur escuro por fora
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(Color.parseColor("#CC080A12"));

        // Card principal
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(28, 28, 28, 24);
        GradientDrawable fundoCard = new GradientDrawable();
        fundoCard.setColors(new int[]{Color.parseColor("#141A2E"), Color.parseColor("#0B1120")});
        fundoCard.setOrientation(GradientDrawable.Orientation.TL_BR);
        fundoCard.setCornerRadius(32f);
        fundoCard.setStroke(2, Color.parseColor("#00D9FF"));
        card.setBackground(fundoCard);
        card.setElevation(20f);

        // LOGO TOPO
        TextView logo = new TextView(activity);
        logo.setText("⚡ DaNikeAI");
        logo.setTextColor(Color.parseColor("#00E5FF"));
        logo.setTextSize(22);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(null, android.graphics.Typeface.BOLD);
        logo.setLetterSpacing(0.05f);

        // TITULO
        TextView tituloView = new TextView(activity);
        tituloView.setText(titulo.toUpperCase());
        tituloView.setTextColor(Color.WHITE);
        tituloView.setTextSize(19);
        tituloView.setGravity(Gravity.CENTER);
        tituloView.setTypeface(null, android.graphics.Typeface.BOLD);
        tituloView.setPadding(0, 20, 0, 6);
        tituloView.setLetterSpacing(0.03f);

        // BADGE VERSAO
        TextView badge = new TextView(activity);
        badge.setText("v" + versionName + " • NOVO");
        badge.setTextColor(Color.parseColor("#00E5FF"));
        badge.setTextSize(12);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(null, android.graphics.Typeface.BOLD);
        badge.setPadding(22, 8, 22, 8);
        GradientDrawable bgBadge = new GradientDrawable();
        bgBadge.setColor(Color.parseColor("#102A4A"));
        bgBadge.setCornerRadius(50f);
        bgBadge.setStroke(1, Color.parseColor("#00BFFF"));
        badge.setBackground(bgBadge);
        LinearLayout.LayoutParams pBadge = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pBadge.gravity = Gravity.CENTER;
        pBadge.setMargins(0, 4, 0, 18);

        // SCROLL DO CHANGELOG
        ScrollView scroll = new ScrollView(activity);
        LinearLayout lista = new LinearLayout(activity);
        lista.setOrientation(LinearLayout.VERTICAL);

        // Quebra mensagem em itens
        String[] linhas = mensagem.split("\\n");
        for (String linha : linhas) {
            if (linha.trim().isEmpty()) continue;
            LinearLayout item = new LinearLayout(activity);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setPadding(0, 7, 0, 7);
            item.setGravity(Gravity.CENTER_VERTICAL);

            TextView dot = new TextView(activity);
            dot.setText("•");
            dot.setTextColor(Color.parseColor("#00E5FF"));
            dot.setTextSize(18);
            dot.setPadding(0, 0, 12, 0);

            TextView txt = new TextView(activity);
            String clean = linha.replace("-", "").replace("•", "").trim();
            txt.setText(clean);
            txt.setTextColor(Color.parseColor("#D0D8E8"));
            txt.setTextSize(14);
            
            item.addView(dot);
            item.addView(txt);
            lista.addView(item);
        }

        scroll.addView(lista);
        LinearLayout.LayoutParams pScroll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pScroll.setMargins(0, 0, 0, 22);
        pScroll.height = 300; // limite

        // BOTAO ATUALIZAR
        Button btnAtualizar = new Button(activity);
        btnAtualizar.setText("ATUALIZAR AGORA  →");
        btnAtualizar.setTextColor(Color.WHITE);
        btnAtualizar.setTextSize(15);
        btnAtualizar.setAllCaps(false);
        btnAtualizar.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAtualizar.setLetterSpacing(0.02f);
        GradientDrawable bgBtn = new GradientDrawable();
        bgBtn.setColors(new int[]{Color.parseColor("#00BFFF"), Color.parseColor("#0066FF")});
        bgBtn.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        bgBtn.setCornerRadius(16f);
        btnAtualizar.setBackground(bgBtn);
        btnAtualizar.setElevation(8f);
        LinearLayout.LayoutParams pBtn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 132);
        pBtn.setMargins(0, 8, 0, 10);

        card.addView(logo);
        card.addView(tituloView);
        card.addView(badge, pBadge);
        card.addView(scroll, pScroll);
        card.addView(btnAtualizar, pBtn);

        if (!obrigatoria) {
            Button btnDepois = new Button(activity);
            btnDepois.setText("Lembrar depois");
            btnDepois.setTextColor(Color.parseColor("#8A93A8"));
            btnDepois.setTextSize(13);
            btnDepois.setAllCaps(false);
            GradientDrawable bgDepois = new GradientDrawable();
            bgDepois.setColor(Color.TRANSPARENT);
            bgDepois.setCornerRadius(16f);
            bgDepois.setStroke(1, Color.parseColor("#2A344A"));
            btnDepois.setBackground(bgDepois);
            card.addView(btnDepois, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110));
            btnDepois.setOnClickListener(v -> dialog.dismiss());
        }

        root.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.setOnClickListener(v -> { if (!obrigatoria) dialog.dismiss(); });

        btnAtualizar.setOnClickListener(v -> {
            if (apkUrl == null || apkUrl.trim().isEmpty()) {
                Toast.makeText(activity, "Link não configurado.", Toast.LENGTH_LONG).show();
                return;
            }
            btnAtualizar.setText("BAIXANDO...");
            btnAtualizar.setEnabled(false);
            AppDistribuicao.baixarApk(activity, apkUrl);
        });

        dialog.setView(root);
        dialog.setCanceledOnTouchOutside(!obrigatoria);
        if (obrigatoria) dialog.setCancelable(false);
        dialog.show();
        
        // Deixa o dialog ocupar largura total
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
