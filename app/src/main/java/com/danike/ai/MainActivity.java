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
    private View pontoIA;
    private View pontoFilmes;
    private View pontoHistorico;
    private View pontoPerfil;
    private View pontoHave;



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
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setPadding(dp(18), dp(12), dp(18), dp(12));

        NeonCardDrawable fundo = new NeonCardDrawable(titulo);
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

        boolean onboardingConcluido =
                getSharedPreferences(
                        "DaNikeAI_Onboarding",
                        MODE_PRIVATE
                ).getBoolean("concluido", false);

        if (!onboardingConcluido) {
            startActivity(
                    new Intent(this, OnboardingActivity.class)
            );
            finish();
            return;
        }

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

        String nomeUsuario = prefs.getString("nome", "Usuário").trim();
        if (nomeUsuario.isEmpty()) nomeUsuario = "Usuário";

        TextView saudacao = txt(
                "Olá, " + nomeUsuario + " 👋",
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

        FrameLayout containerIA = new FrameLayout(this);
        containerIA.setClipChildren(false);
        containerIA.addView(ia, new FrameLayout.LayoutParams(-1, -1));

        pontoIA = criarIndicadorManutencao();

        FrameLayout.LayoutParams pontoIAParams =
                new FrameLayout.LayoutParams(dp(14), dp(14),
                        Gravity.TOP | Gravity.END);
        pontoIAParams.setMargins(0, dp(2), dp(2), 0);
        containerIA.addView(pontoIA, pontoIAParams);

        conteudo.addView(containerIA, iaParams);

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

        FrameLayout containerFilmes = new FrameLayout(this);
        containerFilmes.setClipChildren(false);
        containerFilmes.addView(filmes, new FrameLayout.LayoutParams(-1, -1));

        pontoFilmes = criarIndicadorManutencao();

        FrameLayout.LayoutParams pontoFilmesParams =
                new FrameLayout.LayoutParams(dp(14), dp(14),
                        Gravity.TOP | Gravity.END);
        pontoFilmesParams.setMargins(0, dp(2), dp(2), 0);
        containerFilmes.addView(pontoFilmes, pontoFilmesParams);

        conteudo.addView(containerFilmes, filmesParams);

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

        FrameLayout containerHistorico = new FrameLayout(this);
        containerHistorico.setClipChildren(false);
        containerHistorico.addView(historico, new FrameLayout.LayoutParams(-1, -1));

        pontoHistorico = criarIndicadorManutencao();

        FrameLayout.LayoutParams pontoHistoricoParams =
                new FrameLayout.LayoutParams(dp(14), dp(14),
                        Gravity.TOP | Gravity.END);
        pontoHistoricoParams.setMargins(0, dp(2), dp(2), 0);
        containerHistorico.addView(pontoHistorico, pontoHistoricoParams);

        conteudo.addView(containerHistorico, historicoParams);

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

        FrameLayout containerPerfil = new FrameLayout(this);
        containerPerfil.setClipChildren(false);
        containerPerfil.addView(perfil, new FrameLayout.LayoutParams(-1, -1));

        pontoPerfil = criarIndicadorManutencao();

        FrameLayout.LayoutParams pontoPerfilParams =
                new FrameLayout.LayoutParams(dp(14), dp(14),
                        Gravity.TOP | Gravity.END);
        pontoPerfilParams.setMargins(0, dp(2), dp(2), 0);
        containerPerfil.addView(pontoPerfil, pontoPerfilParams);

        conteudo.addView(containerPerfil, perfilParams);

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
                "Have",
                "Acessar",
                "▶"
        );

        LinearLayout.LayoutParams raveParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(135)
                );

        raveParams.setMargins(0, 0, 0, dp(20));

        FrameLayout containerHave = new FrameLayout(this);
        containerHave.setClipChildren(false);
        containerHave.addView(rave, new FrameLayout.LayoutParams(-1, -1));

        pontoHave = criarIndicadorManutencao();

        FrameLayout.LayoutParams pontoHaveParams =
                new FrameLayout.LayoutParams(dp(14), dp(14),
                        Gravity.TOP | Gravity.END);
        pontoHaveParams.setMargins(0, dp(2), dp(2), 0);
        containerHave.addView(pontoHave, pontoHaveParams);

        conteudo.addView(containerHave, raveParams);

        // ==============================
        // COMPARTILHAR APP
        // ==============================
        Button compartilharApp = card(
                "Compartilhar App",
                "Envie para seus amigos",
                "📲"
        );

        LinearLayout.LayoutParams compartilharParams =
                new LinearLayout.LayoutParams(-1, dp(135));
        compartilharParams.setMargins(0, 0, 0, dp(20));
        conteudo.addView(compartilharApp, compartilharParams);

        compartilharApp.setOnClickListener(v -> {
            try {
                String versao = getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionName;

                String link = "https://play.google.com/store/apps/details?id="
                        + getPackageName();

                String mensagem =
                        "🚀 Baixe o DaNikeAI!\n\n"
                        + "Versão atual: " + versao + "\n\n"
                        + link;

                Intent compartilhar = new Intent(Intent.ACTION_SEND);
                compartilhar.setType("text/plain");
                compartilhar.putExtra(Intent.EXTRA_TEXT, mensagem);

                startActivity(Intent.createChooser(
                        compartilhar,
                        "Compartilhar DaNikeAI"
                ));

            } catch (Exception e) {
                Toast.makeText(
                        MainActivity.this,
                        "Não foi possível compartilhar o app.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

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

        carregarIndicadoresManutencao(
                pontoIA,
                pontoFilmes,
                pontoHistorico,
                pontoPerfil,
                pontoHave
        );

        // =========================
        // CLIQUES
        // =========================

        ia.setOnClickListener(v -> {
            abrirAreaComManutencao(
                    "ia",
                    "IA",
                    () -> {
                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        IAActivity.class
                                );
                        startActivity(intent);
                    }
            );
        });

        filmes.setOnClickListener(v -> {
            abrirAreaComManutencao(
                    "filmes",
                    "FILMES",
                    () -> {
                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        com.danike.ai.filmes.FilmesActivity.class
                                );
                        startActivity(intent);
                    }
            );
        });

        historico.setOnClickListener(v -> {
            abrirAreaComManutencao(
                    "historico",
                    "HISTÓRICO",
                    () -> {
                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        HistoricoActivity.class
                                );
                        startActivity(intent);
                    }
            );
        });

        perfil.setOnClickListener(v -> {
            abrirAreaComManutencao(
                    "perfil",
                    "PERFIL",
                    () -> {
                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        PerfilActivity.class
                                );
                        startActivity(intent);
                    }
            );
        });

        rave.setOnClickListener(v -> {
            abrirAreaComManutencao(
                    "have",
                    "HAVE",
                    () -> {
                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        HaveLoginActivity.class
                                );
                        startActivity(intent);
                    }
            );
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

    // =========================
    // MANUTENÇÃO INDIVIDUAL
    // =========================

    private boolean ehDonoDaNike() {
        try {
            com.google.firebase.auth.FirebaseUser u =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

            return u != null
                    && "lipesanderson@gmail.com".equalsIgnoreCase(
                            String.valueOf(u.getEmail())
                    );
        } catch (Exception e) {
            return false;
        }
    }

    private void abrirAreaComManutencao(
            String area,
            String titulo,
            Runnable abrirNormal
    ) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("config")
                .document("areas")
                .get()
                .addOnSuccessListener(doc -> {

                    Boolean valor = doc.getBoolean("manutencao_" + area);
                    boolean emManutencao = valor != null && valor;

                    if (emManutencao && !ehDonoDaNike()) {
                        mostrarTelaManutencao(titulo);
                    } else {
                        abrirNormal.run();
                    }
                })
                .addOnFailureListener(e -> {
                    // Se o Firebase não responder, não bloqueia o usuário.
                    abrirNormal.run();
                });
    }

    private View criarIndicadorManutencao() {
        View ponto = new View(this);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setShape(GradientDrawable.OVAL);
        fundo.setColor(Color.rgb(0, 255, 100));
        fundo.setStroke(dp(2), Color.WHITE);

        ponto.setBackground(fundo);
        ponto.setElevation(dp(12));

        android.animation.ObjectAnimator pulsar =
                android.animation.ObjectAnimator.ofFloat(
                        ponto,
                        "alpha",
                        1f, 0.35f, 1f
                );

        pulsar.setDuration(900);
        pulsar.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        pulsar.setInterpolator(
                new android.view.animation.AccelerateDecelerateInterpolator()
        );
        pulsar.start();

        return ponto;
    }

    private void definirCorIndicador(View ponto, boolean manutencao) {
        if (ponto == null) return;

        GradientDrawable fundo = new GradientDrawable();
        fundo.setShape(GradientDrawable.OVAL);

        if (manutencao) {
            fundo.setColor(Color.rgb(255, 30, 55));
        } else {
            fundo.setColor(Color.rgb(0, 255, 100));
        }

        fundo.setStroke(dp(2), Color.WHITE);
        ponto.setBackground(fundo);
    }

    private void carregarIndicadoresManutencao(
            View pontoIA,
            View pontoFilmes,
            View pontoHistorico,
            View pontoPerfil,
            View pontoHave
    ) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("config")
                .document("areas")
                .get()
                .addOnSuccessListener(doc -> {
                    definirCorIndicador(
                            pontoIA,
                            Boolean.TRUE.equals(doc.getBoolean("manutencao_ia"))
                    );

                    definirCorIndicador(
                            pontoFilmes,
                            Boolean.TRUE.equals(doc.getBoolean("manutencao_filmes"))
                    );

                    definirCorIndicador(
                            pontoHistorico,
                            Boolean.TRUE.equals(doc.getBoolean("manutencao_historico"))
                    );

                    definirCorIndicador(
                            pontoPerfil,
                            Boolean.TRUE.equals(doc.getBoolean("manutencao_perfil"))
                    );

                    definirCorIndicador(
                            pontoHave,
                            Boolean.TRUE.equals(doc.getBoolean("manutencao_have"))
                    );
                })
                .addOnFailureListener(e -> {
                    // Sem resposta do Firebase, mantém os indicadores verdes.
                });
    }

    private void mostrarTelaManutencao(String titulo) {
        final android.app.Dialog dialog =
                new android.app.Dialog(this);

        LinearLayout fundo = new LinearLayout(this);
        fundo.setOrientation(LinearLayout.VERTICAL);
        fundo.setGravity(Gravity.CENTER);
        fundo.setPadding(dp(28), dp(30), dp(28), dp(30));

        GradientDrawable fundoNeon = new GradientDrawable();
        fundoNeon.setColor(Color.rgb(3, 8, 20));
        fundoNeon.setCornerRadius(dp(24));
        fundoNeon.setStroke(dp(2), Color.rgb(0, 220, 255));
        fundo.setBackground(fundoNeon);

        TextView ferramentas = new TextView(this);
        ferramentas.setText("🔧        🔧");
        ferramentas.setTextSize(42);
        ferramentas.setGravity(Gravity.CENTER);
        ferramentas.setTextColor(Color.rgb(0, 230, 255));

        TextView tituloView = new TextView(this);
        tituloView.setText(titulo + " EM MANUTENÇÃO");
        tituloView.setTextSize(23);
        tituloView.setGravity(Gravity.CENTER);
        tituloView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tituloView.setTextColor(Color.WHITE);
        tituloView.setPadding(0, dp(18), 0, dp(10));

        TextView mensagem = new TextView(this);
        mensagem.setText(
                "Estamos realizando melhorias nesta área.\n\n" +
                "Ela estará disponível novamente em breve."
        );
        mensagem.setTextSize(15);
        mensagem.setGravity(Gravity.CENTER);
        mensagem.setTextColor(Color.rgb(170, 210, 230));
        mensagem.setPadding(0, dp(5), 0, dp(22));

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setTextColor(Color.WHITE);
        voltar.setTextSize(14);
        voltar.setAllCaps(false);
        voltar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable botao = new GradientDrawable();
        botao.setColor(Color.rgb(5, 15, 32));
        botao.setCornerRadius(dp(16));
        botao.setStroke(dp(2), Color.rgb(0, 220, 255));
        voltar.setBackground(botao);

        fundo.addView(
                ferramentas,
                new LinearLayout.LayoutParams(-1, dp(70))
        );
        fundo.addView(
                tituloView,
                new LinearLayout.LayoutParams(-1, dp(70))
        );
        fundo.addView(
                mensagem,
                new LinearLayout.LayoutParams(-1, dp(95))
        );
        fundo.addView(
                voltar,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        voltar.setOnClickListener(v -> dialog.dismiss());

        android.view.animation.RotateAnimation giro1 =
                new android.view.animation.RotateAnimation(
                        0, 360,
                        android.view.animation.Animation.RELATIVE_TO_SELF, .5f,
                        android.view.animation.Animation.RELATIVE_TO_SELF, .5f
                );
        giro1.setDuration(1800);
        giro1.setRepeatCount(android.view.animation.Animation.INFINITE);
        giro1.setInterpolator(new android.view.animation.LinearInterpolator());

        ferramentas.startAnimation(giro1);

        dialog.setContentView(fundo);
        android.view.Window janela = dialog.getWindow();

        if (janela != null) {
            janela.setBackgroundDrawableResource(android.R.color.transparent);
            janela.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * .90f),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * .90f),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pontoIA != null) {
            carregarIndicadoresManutencao(
                    pontoIA,
                    pontoFilmes,
                    pontoHistorico,
                    pontoPerfil,
                    pontoHave
            );
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
