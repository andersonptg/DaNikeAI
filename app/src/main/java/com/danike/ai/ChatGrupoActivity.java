package com.danike.ai;

import java.util.Map;

import com.google.firebase.Timestamp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatGrupoActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String grupoId = "";
    private String nomeGrupo = "Grupo";

    private LinearLayout listaMensagens;
    private EditText campoMensagem;
    private ListenerRegistration listenerMensagens;

    private int fundo = Color.rgb(4, 6, 11);
    private int card = Color.rgb(13, 17, 25);
    private int azul = Color.rgb(65, 165, 255);
    private int recebido = Color.rgb(18, 25, 36);
    private int enviado = Color.rgb(20, 55, 82);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getIntent() != null) {
            grupoId = getIntent().getStringExtra("grupoId");
            String nome =
                    getIntent().getStringExtra("nomeGrupo");

            if (nome != null && !nome.trim().isEmpty()) {
                nomeGrupo = nome;
            }
        }

        if (grupoId == null) {
            grupoId = "";
        }

        montarTela();
        ouvirMensagens();
    }

    private void montarTela() {

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(fundo);

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(
                dp(8),
                dp(8),
                dp(12),
                dp(8)
        );

        GradientDrawable fundoTopo =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(7, 12, 22),
                                Color.rgb(13, 9, 25)
                        }
                );

        fundoTopo.setStroke(
                dp(1),
                Color.rgb(45, 90, 145)
        );

        topo.setBackground(fundoTopo);

        TextView voltar =
                texto("‹", 38, Color.WHITE);

        voltar.setGravity(Gravity.CENTER);

        voltar.setOnClickListener(
                v -> finish()
        );

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(
                        dp(50),
                        dp(58)
                )
        );

        LinearLayout informacoes =
                new LinearLayout(this);

        informacoes.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titulo =
                texto(
                        nomeGrupo,
                        18,
                        Color.WHITE
                );

        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        informacoes.addView(
                titulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        TextView subtitulo =
                texto(
                        "Grupo",
                        12,
                        Color.rgb(110, 180, 235)
                );

        informacoes.addView(
                subtitulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(22)
                )
        );

        topo.addView(
                informacoes,
                new LinearLayout.LayoutParams(
                        0,
                        dp(58),
                        1
                )
        );

        TextView menu =
                texto(
                        "⋮",
                        30,
                        Color.WHITE
                );

        menu.setGravity(Gravity.CENTER);

        topo.addView(
                menu,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(58)
                )
        );

        raiz.addView(
                topo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(74)
                )
        );

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);

        listaMensagens =
                new LinearLayout(this);

        listaMensagens.setOrientation(
                LinearLayout.VERTICAL
        );

        listaMensagens.setPadding(
                dp(12),
                dp(14),
                dp(12),
                dp(14)
        );

        scroll.addView(listaMensagens);

        raiz.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout areaEnviar =
                new LinearLayout(this);

        areaEnviar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        areaEnviar.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );

        areaEnviar.setBackgroundColor(
                Color.rgb(7, 10, 16)
        );

        TextView anexo =
                texto(
                        "📎",
                        22,
                        Color.WHITE
                );

        anexo.setGravity(
                Gravity.CENTER
        );

        areaEnviar.addView(
                anexo,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(52)
                )
        );

        campoMensagem =
                new EditText(this);

        campoMensagem.setHint(
                "Digite uma mensagem..."
        );

        campoMensagem.setHintTextColor(
                Color.rgb(130, 140, 155)
        );

        campoMensagem.setTextColor(
                Color.WHITE
        );

        campoMensagem.setTextSize(15);

        campoMensagem.setSingleLine(false);

        campoMensagem.setMaxLines(4);

        campoMensagem.setPadding(
                dp(16),
                dp(8),
                dp(16),
                dp(8)
        );

        GradientDrawable fundoCampo =
                new GradientDrawable();

        fundoCampo.setColor(card);
        fundoCampo.setCornerRadius(
                dp(22)
        );

        fundoCampo.setStroke(
                dp(1),
                Color.rgb(45, 85, 125)
        );

        campoMensagem.setBackground(
                fundoCampo
        );

        areaEnviar.addView(
                campoMensagem,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView enviar =
                texto(
                        "➤",
                        25,
                        Color.WHITE
                );

        enviar.setGravity(
                Gravity.CENTER
        );

        GradientDrawable fundoEnviar =
                new GradientDrawable();

        fundoEnviar.setShape(
                GradientDrawable.OVAL
        );

        fundoEnviar.setColor(azul);

        enviar.setBackground(
                fundoEnviar
        );

        enviar.setOnClickListener(
                v -> enviarMensagem()
        );

        LinearLayout.LayoutParams enviarParams =
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                );

        enviarParams.setMargins(
                dp(7),
                0,
                0,
                0
        );

        areaEnviar.addView(
                enviar,
                enviarParams
        );

        raiz.addView(
                areaEnviar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(68)
                )
        );

        setContentView(raiz);
    }

    private void ouvirMensagens() {

        if (grupoId.isEmpty()) {
            return;
        }

        listenerMensagens =
                db.collection("grupos")
                        .document(grupoId)
                        .collection("mensagens")
                        .orderBy(
                                "criadoEm",
                                Query.Direction.ASCENDING
                        )
                        .addSnapshotListener(
                                (snapshot, erro) -> {

                                    if (erro != null ||
                                            snapshot == null) {
                                        return;
                                    }

                                    listaMensagens
                                            .removeAllViews();

                                    for (
                                            DocumentSnapshot doc
                                            : snapshot.getDocuments()
                                    ) {

                                        adicionarMensagem(
                                                doc
                                        );
                                    }
                                }
                        );
    }

    private void adicionarMensagem(
            DocumentSnapshot doc
    ) {

        String texto =
                doc.getString("texto");

        if (texto == null) {
            texto = "";
        }

        String remetenteUid =
                doc.getString("remetenteUid");

        String meuUid =
                auth.getCurrentUser() == null
                        ? ""
                        : auth.getCurrentUser()
                                .getUid();

        boolean minha =
                meuUid.equals(remetenteUid);

        LinearLayout linha =
                new LinearLayout(this);

        linha.setGravity(
                minha
                        ? Gravity.RIGHT
                        : Gravity.LEFT
        );

        TextView mensagem =
                texto(
                        texto,
                        15,
                        Color.WHITE
                );

        mensagem.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(7)
        );

        GradientDrawable fundoMensagem =
                new GradientDrawable();

        fundoMensagem.setColor(
                minha
                        ? enviado
                        : recebido
        );

        fundoMensagem.setCornerRadius(
                dp(18)
        );

        fundoMensagem.setStroke(
                dp(1),
                minha
                        ? Color.rgb(40, 115, 170)
                        : Color.rgb(45, 60, 80)
        );

        mensagem.setBackground(
                fundoMensagem
        );

        LinearLayout caixa =
                new LinearLayout(this);

        caixa.setOrientation(
                LinearLayout.VERTICAL
        );

        caixa.setGravity(
                minha
                        ? Gravity.RIGHT
                        : Gravity.LEFT
        );

        caixa.addView(
                mensagem,
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                )
        );

        Object criadoEm =
                doc.get("criadoEm");

        if (criadoEm instanceof Timestamp) {

            Date data =
                    ((Timestamp) criadoEm)
                            .toDate();

            SimpleDateFormat formato =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    );

            TextView horario =
                    texto(
                            formato.format(data),
                            10,
                            Color.rgb(
                                    145,
                                    160,
                                    180
                            )
                    );

            horario.setPadding(
                    dp(10),
                    dp(2),
                    dp(10),
                    dp(4)
            );

            caixa.addView(
                    horario,
                    new LinearLayout.LayoutParams(
                            -2,
                            dp(22)
                    )
            );
        }

        linha.addView(
                caixa,
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                )
        );

        LinearLayout.LayoutParams
                linhaParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        linhaParams.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        listaMensagens.addView(
                linha,
                linhaParams
        );
    }

    private void enviarMensagem() {

        String texto =
                campoMensagem
                        .getText()
                        .toString()
                        .trim();

        if (texto.isEmpty()) {
            return;
        }

        if (grupoId.isEmpty()) {
            return;
        }

        if (auth.getCurrentUser() == null) {
            return;
        }

        String meuUid =
                auth.getCurrentUser().getUid();

        Map<String, Object> mensagem =
                ChatModelo.mensagem(
                        meuUid,
                        texto,
                        "texto"
                );

        db.collection("grupos")
                .document(grupoId)
                .collection("mensagens")
                .add(mensagem)
                .addOnSuccessListener(
                        v -> {

                            campoMensagem
                                    .setText("");

                            campoMensagem
                                    .requestFocus();

                            InputMethodManager teclado =
                                    (InputMethodManager)
                                            getSystemService(
                                                    Context.INPUT_METHOD_SERVICE
                                            );

                            if (teclado != null) {
                                teclado.showSoftInput(
                                        campoMensagem,
                                        InputMethodManager
                                                .SHOW_IMPLICIT
                                );
                            }
                        }
                );
    }

    private TextView texto(
            String valor,
            float tamanho,
            int cor
    ) {

        TextView view =
                new TextView(this);

        view.setText(valor);
        view.setTextSize(tamanho);
        view.setTextColor(cor);

        return view;
    }

    private int dp(int valor) {

        return (int) (
                valor *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {

        if (listenerMensagens != null) {
            listenerMensagens.remove();
        }

        super.onDestroy();
    }
}
