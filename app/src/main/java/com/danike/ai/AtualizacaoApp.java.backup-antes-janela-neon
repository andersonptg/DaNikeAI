package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Map;

public final class AtualizacaoApp {

    private static final int BRANCO = Color.rgb(248, 250, 255);
    private static final int CINZA = Color.rgb(174, 187, 207);
    private static final int AZUL = Color.rgb(0, 204, 255);
    private static final int FUNDO = Color.rgb(10, 15, 27);

    private AtualizacaoApp() {}

    private static int dp(Activity activity, float valor) {
        return Math.round(
                valor * activity.getResources()
                        .getDisplayMetrics().density
        );
    }

    public static void verificar(Activity activity) {
        if (activity == null
                || activity.isFinishing()
                || activity.isDestroyed()) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("config")
                .document("update")
                .get()
                .addOnSuccessListener(doc -> {
                    if (activity.isFinishing()
                            || activity.isDestroyed()
                            || !doc.exists()) {
                        return;
                    }

                    Map<String, Object> dados = doc.getData();

                    if (dados == null) return;

                    Object enabledObj = dados.get("enabled");

                    if (!(enabledObj instanceof Boolean)
                            || !((Boolean) enabledObj)) {
                        return;
                    }

                    Object codigoObj = dados.get("versionCode");

                    if (!(codigoObj instanceof Number)) return;

                    long versaoRemota =
                            ((Number) codigoObj).longValue();

                    if (versaoRemota <= BuildConfig.VERSION_CODE) {
                        return;
                    }

                    String versionName = obterTexto(
                            dados,
                            "versionName",
                            "nova versão"
                    );

                    String titulo = obterTexto(
                            dados,
                            "title",
                            "Uma nova versão chegou!"
                    );

                    String mensagem = obterTexto(
                            dados,
                            "message",
                            "Confira as melhorias da nova versão."
                    );

                    String apkUrl = obterTexto(
                            dados,
                            "apkUrl",
                            ""
                    );

                    Object mandatoryObj = dados.get("mandatory");

                    boolean obrigatoria =
                            mandatoryObj instanceof Boolean
                                    && (Boolean) mandatoryObj;

                    mostrarDialogoPremium(
                            activity,
                            titulo,
                            versionName,
                            mensagem,
                            apkUrl,
                            obrigatoria
                    );
                });
    }

    private static String obterTexto(
            Map<String, Object> dados,
            String chave,
            String padrao
    ) {
        Object valor = dados.get(chave);

        if (valor == null) return padrao;

        String texto = String.valueOf(valor).trim();

        return texto.isEmpty() ? padrao : texto;
    }

    private static GradientDrawable arredondado(
            int cor,
            float raio
    ) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(cor);
        fundo.setCornerRadius(raio);
        return fundo;
    }

    private static TextView texto(
            Activity activity,
            String conteudo,
            float tamanho,
            int cor,
            boolean negrito
    ) {
        TextView view = new TextView(activity);
        view.setText(conteudo);
        view.setTextSize(tamanho);
        view.setTextColor(cor);
        view.setGravity(Gravity.CENTER);

        if (negrito) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private static void mostrarDialogoPremium(
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

        final AlertDialog dialog =
                new AlertDialog.Builder(
                        activity,
                        android.R.style.Theme_Translucent_NoTitleBar
                ).create();

        // Camada escura atrás da janela.
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(
                dp(activity, 22),
                dp(activity, 20),
                dp(activity, 22),
                dp(activity, 20)
        );
        root.setBackgroundColor(Color.argb(220, 3, 6, 14));

        // Cartão Premium.
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(
                dp(activity, 23),
                dp(activity, 25),
                dp(activity, 23),
                dp(activity, 19)
        );

        GradientDrawable fundoCard = new GradientDrawable();
        fundoCard.setColors(new int[]{
                Color.rgb(20, 29, 48),
                Color.rgb(9, 14, 26)
        });
        fundoCard.setOrientation(
                GradientDrawable.Orientation.TL_BR
        );
        fundoCard.setCornerRadius(dp(activity, 25));
        fundoCard.setStroke(
                dp(activity, 1),
                Color.rgb(0, 153, 204)
        );

        card.setBackground(fundoCard);
        card.setElevation(dp(activity, 12));

        // Identidade visual.
        TextView logo = texto(
                activity,
                "⚡ DaNikeAI",
                23,
                AZUL,
                true
        );
        logo.setLetterSpacing(0.025f);

        TextView subtitulo = texto(
                activity,
                "ATUALIZAÇÃO OFICIAL",
                10,
                CINZA,
                true
        );
        subtitulo.setLetterSpacing(0.16f);

        LinearLayout.LayoutParams pSubtitulo =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        pSubtitulo.topMargin = dp(activity, 7);
        pSubtitulo.bottomMargin = dp(activity, 18);

        // Título informado pelo ADM.
        TextView tituloView = texto(
                activity,
                titulo,
                20,
                BRANCO,
                true
        );
        tituloView.setGravity(Gravity.CENTER);
        tituloView.setLineSpacing(dp(activity, 2), 1.0f);

        LinearLayout.LayoutParams pTitulo =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        pTitulo.bottomMargin = dp(activity, 15);

        // Identificação da versão.
        TextView badge = texto(
                activity,
                "VERSÃO " + versionName + "  •  NOVA",
                11,
                AZUL,
                true
        );
        badge.setPadding(
                dp(activity, 14),
                dp(activity, 8),
                dp(activity, 14),
                dp(activity, 8)
        );
        badge.setBackground(arredondado(
                Color.rgb(16, 39, 60),
                dp(activity, 30)
        ));

        LinearLayout.LayoutParams pBadge =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        pBadge.bottomMargin = dp(activity, 19);

        // Área rolável para as melhorias.
        TextView cabecalho = texto(
                activity,
                "O QUE TEM DE NOVO",
                11,
                AZUL,
                true
        );
        cabecalho.setGravity(Gravity.START);
        cabecalho.setLetterSpacing(0.08f);

        LinearLayout.LayoutParams pCabecalho =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        pCabecalho.bottomMargin = dp(activity, 8);

        LinearLayout lista = new LinearLayout(activity);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(
                dp(activity, 13),
                dp(activity, 8),
                dp(activity, 13),
                dp(activity, 8)
        );
        lista.setBackground(arredondado(
                Color.rgb(12, 20, 35),
                dp(activity, 13)
        ));

        String[] linhas = mensagem.split("\\n");

        for (String linha : linhas) {
            String limpa = linha.trim();

            if (limpa.isEmpty()) continue;

            if (limpa.startsWith("-")
                    || limpa.startsWith("•")
                    || limpa.startsWith("–")) {
                limpa = limpa.substring(1).trim();
            }

            if (limpa.isEmpty()) continue;

            LinearLayout item = new LinearLayout(activity);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.TOP);
            item.setPadding(
                    0,
                    dp(activity, 5),
                    0,
                    dp(activity, 5)
            );

            TextView ponto = texto(
                    activity,
                    "•",
                    17,
                    AZUL,
                    true
            );
            ponto.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            ponto.setMinWidth(dp(activity, 23));

            TextView descricao = new TextView(activity);
            descricao.setText(limpa);
            descricao.setTextSize(13);
            descricao.setTextColor(CINZA);
            descricao.setGravity(Gravity.START);
            descricao.setLineSpacing(dp(activity, 3), 1.0f);

            item.addView(ponto);

            item.addView(
                    descricao,
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f
                    )
            );

            lista.addView(item);
        }

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scroll.addView(lista);

        LinearLayout.LayoutParams pScroll =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(activity, 185)
                );
        pScroll.bottomMargin = dp(activity, 19);

        // Botão principal.
        TextView btnAtualizar = texto(
                activity,
                "ATUALIZAR AGORA   →",
                14,
                Color.WHITE,
                true
        );
        btnAtualizar.setMinHeight(dp(activity, 52));
        btnAtualizar.setPadding(
                dp(activity, 12),
                dp(activity, 13),
                dp(activity, 12),
                dp(activity, 13)
        );

        GradientDrawable fundoBotao = new GradientDrawable();
        fundoBotao.setColors(new int[]{
                Color.rgb(0, 176, 230),
                Color.rgb(0, 93, 220)
        });
        fundoBotao.setOrientation(
                GradientDrawable.Orientation.LEFT_RIGHT
        );
        fundoBotao.setCornerRadius(dp(activity, 13));
        btnAtualizar.setBackground(fundoBotao);
        btnAtualizar.setElevation(dp(activity, 3));
        btnAtualizar.setClickable(true);
        btnAtualizar.setFocusable(true);

        LinearLayout.LayoutParams pBotao =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        pBotao.bottomMargin = dp(activity, 8);

        card.addView(logo);
        card.addView(subtitulo, pSubtitulo);
        card.addView(tituloView, pTitulo);
        card.addView(badge, pBadge);
        card.addView(cabecalho, pCabecalho);
        card.addView(scroll, pScroll);
        card.addView(btnAtualizar, pBotao);

        // Opção de adiar somente se a atualização for opcional.
        if (!obrigatoria) {
            TextView btnDepois = texto(
                    activity,
                    "Lembrar depois",
                    13,
                    CINZA,
                    false
            );
            btnDepois.setPadding(
                    dp(activity, 12),
                    dp(activity, 11),
                    dp(activity, 12),
                    dp(activity, 11)
            );
            btnDepois.setBackground(arredondado(
                    Color.TRANSPARENT,
                    dp(activity, 12)
            ));
            btnDepois.setClickable(true);
            btnDepois.setFocusable(true);

            card.addView(
                    btnDepois,
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );

            btnDepois.setOnClickListener(v -> dialog.dismiss());
        }

        int larguraTela = activity.getResources()
                .getDisplayMetrics().widthPixels;

        int larguraMaxima = dp(activity, 440);
        int larguraDisponivel = larguraTela - dp(activity, 44);
        int larguraCard = Math.min(
                larguraMaxima,
                larguraDisponivel
        );

        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        Math.max(dp(activity, 250), larguraCard),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        root.setOnClickListener(v -> {
            if (!obrigatoria) dialog.dismiss();
        });

        // O mecanismo de download existente é preservado.
        btnAtualizar.setOnClickListener(v -> {
            if (apkUrl == null || apkUrl.trim().isEmpty()) {
                Toast.makeText(
                        activity,
                        "Link da atualização não configurado no ADM.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            btnAtualizar.setText("INICIANDO DOWNLOAD...");
            btnAtualizar.setEnabled(false);

            AppDistribuicao.baixarApk(activity, apkUrl);

            // Evita deixar o botão travado caso o download falhe.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dialog.isShowing()
                        && btnAtualizar.getParent() != null) {
                    btnAtualizar.setText("TENTAR ATUALIZAR NOVAMENTE  →");
                    btnAtualizar.setEnabled(true);
                }
            }, 4000);
        });

        dialog.setView(root);
        dialog.setCanceledOnTouchOutside(!obrigatoria);

        if (obrigatoria) {
            dialog.setCancelable(false);
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            dialog.getWindow().setDimAmount(0.75f);

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }
}
