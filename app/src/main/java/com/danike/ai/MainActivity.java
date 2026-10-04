package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout tela;
    boolean escuro = true;
    android.content.SharedPreferences prefs;

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    GradientDrawable bg(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    TextView txt(String s, float tam, int cor) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tam);
        t.setTextColor(cor);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    Button card(String titulo, String subtitulo, String emoji) {
        Button b = new Button(this);

        b.setText(emoji + "\n" + titulo + "\n" + subtitulo);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(4, 18, 34),
                        Color.rgb(7, 38, 64),
                        Color.rgb(3, 17, 31)
                }
        );

        fundo.setCornerRadius(dp(28));
        fundo.setStroke(dp(1), Color.rgb(0, 220, 255));

        b.setBackground(fundo);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            b.setElevation(dp(7));
        }

        b.setStateListAnimator(null);

        b.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate()
                        .scaleX(.97f)
                        .scaleY(.97f)
                        .alpha(.88f)
                        .setDuration(100)
                        .start();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(160)
                        .start();
            }
            return false;
        });

        return b;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("DaNikeAI_Dados", 0);
        escuro = prefs.getBoolean("modo_escuro", true);

        montarHome();

        AtualizacaoApp.verificar(this);
    }

    void montarHome() {

        tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(14)
        );

        NeonBackgroundView fundo =
                new NeonBackgroundView(this);

        FrameLayout raiz = new FrameLayout(this);

        raiz.addView(
                fundo,
                new FrameLayout.LayoutParams(-1, -1)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(0, dp(5), 0, dp(20));

        // =========================
        // CABEÇALHO
        // =========================

        TextView titulo = txt(
                "DaNikeAI",
                30,
                Color.WHITE
        );

        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        conteudo.addView(
                titulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                )
        );

        TextView saudacao = txt(
                "Olá, usuário 👋",
                19,
                Color.rgb(190, 225, 245)
        );

        conteudo.addView(
                saudacao,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        TextView descricao = txt(
                "Escolha uma área para continuar",
                14,
                Color.rgb(125, 165, 190)
        );

        conteudo.addView(
                descricao,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(34)
                )
        );

        // =========================
        // CAPA IA
        // =========================

        Button ia = card(
                "IA",
                "Sua assistente IZy",
                "🤖"
        );

        LinearLayout.LayoutParams iaParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(155)
                );

        iaParams.setMargins(0, dp(10), 0, dp(16));

        conteudo.addView(ia, iaParams);

        // =========================
        // CAPA FILMES
        // =========================

        Button filmes = card(
                "Filmes",
                "Explore seus filmes",
                "🎬"
        );

        LinearLayout.LayoutParams filmesParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(155)
                );

        filmesParams.setMargins(0, 0, 0, dp(16));

        conteudo.addView(filmes, filmesParams);

        // =========================
        // CAPA HISTÓRICO
        // =========================

        Button historico = card(
                "Histórico",
                "Veja suas atividades",
                "🕘"
        );

        LinearLayout.LayoutParams historicoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(155)
                );

        historicoParams.setMargins(0, 0, 0, dp(16));

        conteudo.addView(historico, historicoParams);

        // =========================
        // CAPA PERFIL
        // =========================

        Button perfil = card(
                "Perfil",
                "Seus dados e informações",
                "👤"
        );

        LinearLayout.LayoutParams perfilParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(155)
                );

        perfilParams.setMargins(0, 0, 0, dp(16));

        conteudo.addView(perfil, perfilParams);

        // =========================
        // ADM — SOMENTE DONO
        // =========================

        com.google.firebase.auth.FirebaseUser usuarioAtual = null;

        try {
            usuarioAtual =
                    com.google.firebase.auth.FirebaseAuth
                            .getInstance()
                            .getCurrentUser();
        } catch (Exception ignored) {}

        boolean ehDono =
                usuarioAtual != null
                && "lipesanderson@gmail.com".equalsIgnoreCase(
                        String.valueOf(usuarioAtual.getEmail())
                );

        Button adm = null;

        if (ehDono) {

            adm = card(
                    "ADM",
                    "Painel administrativo",
                    "⚡"
            );

            LinearLayout.LayoutParams admParams =
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(135)
                    );

            admParams.setMargins(0, dp(5), 0, dp(10));

            conteudo.addView(adm, admParams);
        }

        // =========================
        // RAVE — MANUTENÇÃO
        // =========================

        Button rave = card(
                "Tela de Rave",
                "Acessar",
                "🪩"
        );

        LinearLayout.LayoutParams raveParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(135)
                );

        raveParams.setMargins(0, 0, 0, dp(20));

        conteudo.addView(rave, raveParams);

        scroll.addView(conteudo);

        tela.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        raiz.addView(
                tela,
                new FrameLayout.LayoutParams(-1, -1)
        );

        setContentView(raiz);

        // =========================
        // CLIQUES
        // =========================

        ia.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            IAActivity.class
                    );

            startActivity(intent);
        });

        filmes.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            com.danike.ai.filmes.FilmesActivity.class
                    );

            startActivity(intent);
        });

        historico.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            HistoricoActivity.class
                    );

            startActivity(intent);
        });

        perfil.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            PerfilActivity.class
                    );

            startActivity(intent);
        });

        rave.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            ManutencaoActivity.class
                    );

            startActivity(intent);
        });

        if (adm != null) {

            adm.setOnClickListener(v -> {

                com.google.firebase.auth.FirebaseUser dono =
                        com.google.firebase.auth.FirebaseAuth
                                .getInstance()
                                .getCurrentUser();

                if (dono != null &&
                        "lipesanderson@gmail.com".equalsIgnoreCase(
                                String.valueOf(dono.getEmail())
                        )) {

                    startActivity(
                            new Intent(
                                    MainActivity.this,
                                    AdmActivity.class
                            )
                    );

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Acesso administrativo não autorizado.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        // =========================
        // ANIMAÇÃO DE ENTRADA
        // =========================

        animarEntrada(ia, 100);
        animarEntrada(filmes, 220);
        animarEntrada(historico, 340);
        animarEntrada(perfil, 460);

        if (adm != null) {
            animarEntrada(adm, 580);
            animarEntrada(rave, 700);
        } else {
            animarEntrada(rave, 580);
        }
    }

    void animarEntrada(View view, long atraso) {

        view.setAlpha(0f);
        view.setTranslationY(dp(35));
        view.setScaleX(.96f);
        view.setScaleY(.96f);

        view.animate()
                .alpha(1f)
                .translationY(0)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(atraso)
                .setDuration(420)
                .setInterpolator(
                        new DecelerateInterpolator()
                )
                .start();
    }
}
