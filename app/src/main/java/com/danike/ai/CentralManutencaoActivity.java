package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class CentralManutencaoActivity extends Activity {

    private static final String OWNER_EMAIL = "lipesanderson@gmail.com";

    private final int FUNDO = Color.rgb(2, 5, 14);
    private final int CARD = Color.rgb(7, 13, 27);
    private final int CARD2 = Color.rgb(10, 18, 35);

    private final int AZUL = Color.rgb(0, 215, 255);
    private final int VERDE = Color.rgb(40, 255, 150);
    private final int VERMELHO = Color.rgb(255, 45, 95);
    private final int ROXO = Color.rgb(185, 75, 255);
    private final int ROSA = Color.rgb(255, 55, 180);
    private final int DOURADO = Color.rgb(255, 195, 55);
    private final int BRANCO = Color.rgb(235, 245, 255);
    private final int CINZA = Color.rgb(125, 145, 170);

    private FirebaseFirestore db;

    private LinearLayout raiz;
    private LinearLayout manutencoesAtivasContainer;

    private TextView manutencaoGeral;
    private TextView manutencaoIA;
    private TextView manutencaoFilmes;
    private TextView manutencaoHistorico;
    private TextView manutencaoPerfil;
    private TextView manutencaoHave;
    private TextView manutencaoConexoes;

    private final Map<String, Boolean> estados = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isOwner()) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        montarTela();
        carregarEstados();
    }

    private boolean isOwner() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return false;
        }

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        return email != null &&
                OWNER_EMAIL.equalsIgnoreCase(email);
    }

    private void montarTela() {

        raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(16), dp(16), dp(16), dp(20));
        raiz.setBackgroundColor(FUNDO);

        TextView topoLinha = new TextView(this);
        topoLinha.setText("DANIKEAI  /  ADMIN CONTROL");
        topoLinha.setTextColor(AZUL);
        topoLinha.setTextSize(9);
        topoLinha.setLetterSpacing(0.18f);
        topoLinha.setGravity(Gravity.CENTER);
        topoLinha.setTypeface(null, Typeface.BOLD);

        raiz.addView(
                topoLinha,
                new LinearLayout.LayoutParams(-1, dp(24))
        );

        TextView titulo = new TextView(this);
        titulo.setText("CENTRAL DE\nMANUTENÇÃO");
        titulo.setTextColor(BRANCO);
        titulo.setTextSize(27);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, Typeface.BOLD);
        titulo.setLetterSpacing(0.06f);

        raiz.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, dp(78))
        );

        TextView linha = new TextView(this);
        linha.setText("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        linha.setTextColor(AZUL);
        linha.setTextSize(8);
        linha.setGravity(Gravity.CENTER);
        linha.setAlpha(0.55f);

        raiz.addView(
                linha,
                new LinearLayout.LayoutParams(-1, dp(24))
        );

        TextView geralLabel = tituloSecao(
                "CONTROLE GLOBAL",
                DOURADO
        );

        raiz.addView(geralLabel);

        manutencaoGeral = cardGeral();

        raiz.addView(
                manutencaoGeral,
                new LinearLayout.LayoutParams(-1, dp(82))
        );

        TextView areasLabel = tituloSecao(
                "MÓDULOS DO SISTEMA",
                AZUL
        );

        raiz.addView(areasLabel);

        LinearLayout linha1 = linhaCards();

        manutencaoIA = cardModulo(
                "IA",
                "CORE INTELLIGENCE",
                AZUL
        );

        manutencaoFilmes = cardModulo(
                "FILMES",
                "MEDIA CENTER",
                ROSA
        );

        linha1.addView(manutencaoIA);
        linha1.addView(manutencaoFilmes);

        raiz.addView(linha1);

        LinearLayout linha2 = linhaCards();

        manutencaoHistorico = cardModulo(
                "HISTÓRICO",
                "ACTIVITY LOG",
                DOURADO
        );

        manutencaoPerfil = cardModulo(
                "PERFIL",
                "USER CENTER",
                ROXO
        );

        linha2.addView(manutencaoHistorico);
        linha2.addView(manutencaoPerfil);

        raiz.addView(linha2);

        LinearLayout linha3 = linhaCards();

        manutencaoHave = cardModulo(
                "HAVE",
                "SOCIAL HUB",
                VERDE
        );

        manutencaoConexoes = cardModulo(
                "CONEXÕES",
                "NETWORK HUB",
                AZUL
        );

        linha3.addView(manutencaoHave);
        linha3.addView(manutencaoConexoes);

        raiz.addView(linha3);

        TextView ativosLabel = tituloSecao(
                "MANUTENÇÕES ATIVAS",
                VERMELHO
        );

        LinearLayout.LayoutParams ativosLp =
                new LinearLayout.LayoutParams(-1, dp(38));

        ativosLp.setMargins(0, dp(8), 0, 0);

        raiz.addView(ativosLabel, ativosLp);

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);

        manutencoesAtivasContainer =
                new LinearLayout(this);

        manutencoesAtivasContainer.setOrientation(
                LinearLayout.HORIZONTAL
        );

        manutencoesAtivasContainer.setGravity(
                Gravity.CENTER_VERTICAL
        );

        manutencoesAtivasContainer.setPadding(
                dp(2), dp(5), dp(18), dp(8)
        );

        scroll.addView(
                manutencoesAtivasContainer,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        dp(105)
                )
        );

        raiz.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(112)
                )
        );

        configurarCliques();

        setContentView(raiz);
    }

    private TextView tituloSecao(
            String texto,
            int cor
    ) {

        TextView t = new TextView(this);

        t.setText(texto);
        t.setTextColor(cor);
        t.setTextSize(10);
        t.setTypeface(null, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setLetterSpacing(0.12f);

        t.setPadding(
                dp(4),
                dp(7),
                dp(4),
                dp(2)
        );

        return t;
    }

    private LinearLayout linhaCards() {

        LinearLayout l =
                new LinearLayout(this);

        l.setOrientation(
                LinearLayout.HORIZONTAL
        );

        l.setGravity(Gravity.CENTER);

        l.setPadding(
                0,
                dp(3),
                0,
                dp(3)
        );

        return l;
    }

    private TextView cardGeral() {

        TextView card = new TextView(this);

        card.setText(
                "◈   MANUTENÇÃO GERAL\n" +
                "     SISTEMA COMPLETO"
        );

        card.setTextColor(BRANCO);
        card.setTextSize(13);
        card.setGravity(Gravity.CENTER);
        card.setTypeface(null, Typeface.BOLD);
        card.setLetterSpacing(0.04f);

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                Color.rgb(18, 15, 8)
        );

        fundo.setStroke(
                dp(2),
                DOURADO
        );

        fundo.setCornerRadius(
                dp(18)
        );

        card.setBackground(fundo);

        return card;
    }

    private TextView cardModulo(
            String titulo,
            String subtitulo,
            int cor
    ) {

        TextView card = new TextView(this);

        card.setText(
                "◉\n" +
                titulo +
                "\n" +
                subtitulo
        );

        card.setTextColor(BRANCO);
        card.setTextSize(12);
        card.setGravity(Gravity.CENTER);
        card.setTypeface(null, Typeface.BOLD);
        card.setLetterSpacing(0.025f);

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(CARD);

        fundo.setStroke(
                dp(1),
                cor
        );

        fundo.setCornerRadius(
                dp(20)
        );

        card.setBackground(fundo);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(112),
                        1
                );

        lp.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        card.setLayoutParams(lp);

        return card;
    }

    private void configurarCliques() {

        manutencaoGeral.setOnClickListener(
                v -> alternarGeral()
        );

        manutencaoIA.setOnClickListener(
                v -> alternarArea("ia", "IA")
        );

        manutencaoFilmes.setOnClickListener(
                v -> alternarArea("filmes", "FILMES")
        );

        manutencaoHistorico.setOnClickListener(
                v -> alternarArea(
                        "historico",
                        "HISTÓRICO"
                )
        );

        manutencaoPerfil.setOnClickListener(
                v -> alternarArea(
                        "perfil",
                        "PERFIL"
                )
        );

        manutencaoHave.setOnClickListener(
                v -> alternarArea(
                        "have",
                        "HAVE"
                )
        );

        manutencaoConexoes.setOnClickListener(
                v -> alternarArea(
                        "conexoes",
                        "CONEXÕES"
                )
        );
    }

    private void carregarEstados() {

        db.collection("config")
                .document("areas")
                .get()
                .addOnSuccessListener(doc -> {

                    estados.put(
                            "ia",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_ia"
                                    )
                            )
                    );

                    estados.put(
                            "filmes",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_filmes"
                                    )
                            )
                    );

                    estados.put(
                            "historico",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_historico"
                                    )
                            )
                    );

                    estados.put(
                            "perfil",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_perfil"
                                    )
                            )
                    );

                    estados.put(
                            "have",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_have"
                                    )
                            )
                    );

                    estados.put(
                            "conexoes",
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao_conexoes"
                                    )
                            )
                    );

                    atualizarTodos();

                })
                .addOnFailureListener(e -> {

                    estados.put("ia", false);
                    estados.put("filmes", false);
                    estados.put("historico", false);
                    estados.put("perfil", false);
                    estados.put("have", false);
                    estados.put("conexoes", false);

                    atualizarTodos();
                });

        db.collection("config")
                .document("app")
                .get()
                .addOnSuccessListener(doc -> {

                    boolean ativa =
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao"
                                    )
                            );

                    atualizarGeral(ativa);
                });
    }

    private void atualizarTodos() {

        atualizarArea(
                manutencaoIA,
                "IA",
                "ia",
                AZUL
        );

        atualizarArea(
                manutencaoFilmes,
                "FILMES",
                "filmes",
                ROSA
        );

        atualizarArea(
                manutencaoHistorico,
                "HISTÓRICO",
                "historico",
                DOURADO
        );

        atualizarArea(
                manutencaoPerfil,
                "PERFIL",
                "perfil",
                ROXO
        );

        atualizarArea(
                manutencaoHave,
                "HAVE",
                "have",
                VERDE
        );

        atualizarArea(
                manutencaoConexoes,
                "CONEXÕES",
                "conexoes",
                AZUL
        );

        atualizarManutencoesAtivas();
    }

    private void atualizarArea(
            TextView card,
            String titulo,
            String area,
            int cor
    ) {

        boolean ativa =
                Boolean.TRUE.equals(
                        estados.get(area)
                );

        card.setText(
                "◉\n" +
                titulo +
                "\n" +
                (ativa
                        ? "EM MANUTENÇÃO"
                        : "DISPONÍVEL")
        );

        card.setTextColor(
                ativa
                        ? VERMELHO
                        : BRANCO
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                ativa
                        ? Color.rgb(24, 8, 18)
                        : CARD
        );

        fundo.setStroke(
                dp(ativa ? 2 : 1),
                ativa
                        ? VERMELHO
                        : cor
        );

        fundo.setCornerRadius(
                dp(20)
        );

        card.setBackground(fundo);

        iniciarPulso(
                card,
                ativa
                        ? VERMELHO
                        : cor
        );
    }

    private void atualizarGeral(
            boolean ativa
    ) {

        manutencaoGeral.setText(
                ativa
                        ? "◈   MANUTENÇÃO GERAL\n" +
                          "     SISTEMA BLOQUEADO"
                        : "◈   MANUTENÇÃO GERAL\n" +
                          "     SISTEMA OPERACIONAL"
        );

        manutencaoGeral.setTextColor(
                ativa
                        ? VERMELHO
                        : DOURADO
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                ativa
                        ? Color.rgb(24, 8, 18)
                        : Color.rgb(18, 15, 8)
        );

        fundo.setStroke(
                dp(2),
                ativa
                        ? VERMELHO
                        : DOURADO
        );

        fundo.setCornerRadius(
                dp(18)
        );

        manutencaoGeral.setBackground(fundo);

        iniciarPulso(
                manutencaoGeral,
                ativa
                        ? VERMELHO
                        : DOURADO
        );
    }

    private void atualizarManutencoesAtivas() {

        if (manutencoesAtivasContainer == null) {
            return;
        }

        manutencoesAtivasContainer.removeAllViews();

        adicionarManutencaoAtiva(
                "ia",
                "IA",
                AZUL
        );

        adicionarManutencaoAtiva(
                "filmes",
                "FILMES",
                ROSA
        );

        adicionarManutencaoAtiva(
                "historico",
                "HISTÓRICO",
                DOURADO
        );

        adicionarManutencaoAtiva(
                "perfil",
                "PERFIL",
                ROXO
        );

        adicionarManutencaoAtiva(
                "have",
                "HAVE",
                VERDE
        );

        adicionarManutencaoAtiva(
                "conexoes",
                "CONEXÕES",
                AZUL
        );

        if (manutencoesAtivasContainer
                .getChildCount() == 0) {

            TextView vazio =
                    new TextView(this);

            vazio.setText(
                    "NENHUM MÓDULO EM MANUTENÇÃO"
            );

            vazio.setTextColor(CINZA);
            vazio.setTextSize(10);
            vazio.setGravity(Gravity.CENTER);
            vazio.setTypeface(
                    null,
                    Typeface.BOLD
            );

            manutencoesAtivasContainer.addView(
                    vazio,
                    new LinearLayout.LayoutParams(
                            dp(250),
                            dp(82)
                    )
            );
        }
    }

    private void adicionarManutencaoAtiva(
            String area,
            String titulo,
            int cor
    ) {

        if (!Boolean.TRUE.equals(
                estados.get(area)
        )) {
            return;
        }

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                Color.rgb(20, 7, 18)
        );

        fundo.setStroke(
                dp(1),
                cor
        );

        fundo.setCornerRadius(
                dp(18)
        );

        card.setBackground(fundo);

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                        dp(118),
                        dp(82)
                );

        cp.setMargins(
                dp(4),
                0,
                dp(4),
                0
        );

        card.setLayoutParams(cp);

        TextView icone =
                new TextView(this);

        icone.setText("◉");
        icone.setTextColor(cor);
        icone.setTextSize(18);
        icone.setGravity(Gravity.CENTER);

        card.addView(
                icone,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(27)
                )
        );

        TextView nome =
                new TextView(this);

        nome.setText(titulo);
        nome.setTextColor(BRANCO);
        nome.setTextSize(10);
        nome.setGravity(Gravity.CENTER);
        nome.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(
                nome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(22)
                )
        );

        TextView status =
                new TextView(this);

        status.setText("ATIVA");
        status.setTextColor(VERMELHO);
        status.setTextSize(8);
        status.setGravity(Gravity.CENTER);
        status.setTypeface(
                null,
                Typeface.BOLD
        );

        card.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(18)
                )
        );

        iniciarPulso(
                icone,
                cor
        );

        manutencoesAtivasContainer.addView(card);
    }

    private void iniciarPulso(
            View view,
            int cor
    ) {

        AlphaAnimation anim =
                new AlphaAnimation(
                        0.45f,
                        1.0f
                );

        anim.setDuration(900);
        anim.setRepeatMode(
                AlphaAnimation.REVERSE
        );
        anim.setRepeatCount(
                AlphaAnimation.INFINITE
        );

        view.startAnimation(anim);
    }

    private void alternarArea(
            String area,
            String titulo
    ) {

        boolean atual =
                Boolean.TRUE.equals(
                        estados.get(area)
                );

        boolean novoEstado = !atual;

        new AlertDialog.Builder(this)
                .setTitle(
                        novoEstado
                                ? "ATIVAR MANUTENÇÃO"
                                : "DESATIVAR MANUTENÇÃO"
                )
                .setMessage(
                        novoEstado
                                ? titulo +
                                  " ficará indisponível para usuários comuns."
                                : titulo +
                                  " voltará ao funcionamento normal."
                )
                .setNegativeButton(
                        "CANCELAR",
                        null
                )
                .setPositiveButton(
                        "CONFIRMAR",
                        (d, w) -> {

                            String campo =
                                    "manutencao_" +
                                    area;

                            db.collection("config")
                                    .document("areas")
                                    .set(
                                            java.util.Collections
                                                    .singletonMap(
                                                            campo,
                                                            novoEstado
                                                    ),
                                            SetOptions.merge()
                                    )
                                    .addOnSuccessListener(v -> {

                                        estados.put(
                                                area,
                                                novoEstado
                                        );

                                        atualizarTodos();

                                        Toast.makeText(
                                                this,
                                                titulo +
                                                (
                                                        novoEstado
                                                                ? " em manutenção."
                                                                : " disponível novamente."
                                                ),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(
                                                    this,
                                                    "Falha Firebase: " +
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show()
                                    );
                        }
                )
                .show();
    }

    private void alternarGeral() {

        final boolean[] atual = {false};

        db.collection("config")
                .document("app")
                .get()
                .addOnSuccessListener(doc -> {

                    atual[0] =
                            Boolean.TRUE.equals(
                                    doc.getBoolean(
                                            "manutencao"
                                    )
                            );

                    boolean novoEstado =
                            !atual[0];

                    new AlertDialog.Builder(this)
                            .setTitle(
                                    novoEstado
                                            ? "ATIVAR MANUTENÇÃO GERAL"
                                            : "DESATIVAR MANUTENÇÃO GERAL"
                            )
                            .setMessage(
                                    novoEstado
                                            ? "O aplicativo inteiro ficará em manutenção."
                                            : "O aplicativo inteiro voltará ao funcionamento normal."
                            )
                            .setNegativeButton(
                                    "CANCELAR",
                                    null
                            )
                            .setPositiveButton(
                                    "CONFIRMAR",
                                    (d, w) -> {

                                        db.collection("config")
                                                .document("app")
                                                .set(
                                                        java.util.Collections
                                                                .singletonMap(
                                                                        "manutencao",
                                                                        novoEstado
                                                                ),
                                                        SetOptions.merge()
                                                )
                                                .addOnSuccessListener(v -> {

                                                    atualizarGeral(
                                                            novoEstado
                                                    );

                                                    Toast.makeText(
                                                            this,
                                                            novoEstado
                                                                    ? "Manutenção geral ativada."
                                                                    : "Manutenção geral desativada.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(
                                                                this,
                                                                "Falha Firebase: " +
                                                                e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                                );
                                    }
                            )
                            .show();
                });
    }

    private int dp(int valor) {
        return (int) (
                valor *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
