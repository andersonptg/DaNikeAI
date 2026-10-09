package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatIndividualActivity extends Activity {

    private FrameLayout raiz;
    private LinearLayout listaMensagens;
    private EditText campoMensagem;
    private ScrollView scroll;
    private FirebaseFirestore db;
    private ListenerRegistration listenerMensagens;

    private String meuUid = "";
    private String outroUid = "";
    private String nomeOutro = "Usuário";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            meuUid = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();
        }

        outroUid = getIntent().getStringExtra("uid");

        if (outroUid == null || outroUid.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Conexão inválida.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        montarInterface();
        carregarPerfil();
        iniciarMensagens();
    }

    private void montarInterface() {

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(
                Color.rgb(2, 3, 8)
        );

        LinearLayout principal =
                new LinearLayout(this);

        principal.setOrientation(
                LinearLayout.VERTICAL
        );

        raiz.addView(
                principal,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        LinearLayout topo =
                new LinearLayout(this);

        topo.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topo.setGravity(
                Gravity.CENTER_VERTICAL
        );

        topo.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        GradientDrawable fundoTopo =
                new GradientDrawable();

        fundoTopo.setColor(
                Color.rgb(8, 14, 27)
        );

        fundoTopo.setStroke(
                dp(1),
                Color.rgb(35, 100, 170)
        );

        topo.setBackground(fundoTopo);

        TextView voltar = new TextView(this);
        voltar.setText("‹");
        voltar.setTextSize(34);
        voltar.setTextColor(
                Color.rgb(90, 190, 255)
        );
        voltar.setGravity(Gravity.CENTER);

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(60)
                )
        );

        voltar.setOnClickListener(
                v -> finish()
        );

        ImageView foto = new ImageView(this);

        GradientDrawable fundoFoto =
                new GradientDrawable();

        fundoFoto.setShape(
                GradientDrawable.OVAL
        );

        fundoFoto.setColor(
                Color.rgb(18, 30, 48)
        );

        fundoFoto.setStroke(
                dp(2),
                Color.rgb(60, 180, 255)
        );

        foto.setBackground(fundoFoto);
        foto.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        topo.addView(
                foto,
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                )
        );

        LinearLayout blocoNome =
                new LinearLayout(this);

        blocoNome.setOrientation(
                LinearLayout.VERTICAL
        );

        blocoNome.setPadding(
                dp(10),
                0,
                0,
                0
        );

        TextView nome =
                texto(
                        nomeOutro,
                        16,
                        Color.WHITE
                );

        nome.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView status =
                texto(
                        "● conversando",
                        12,
                        Color.rgb(70, 220, 150)
                );

        blocoNome.addView(
                nome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        blocoNome.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(22)
                )
        );

        topo.addView(
                blocoNome,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        principal.addView(
                topo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                )
        );

        scroll = new ScrollView(this);

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

        principal.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout barra =
                new LinearLayout(this);

        barra.setOrientation(
                LinearLayout.HORIZONTAL
        );

        barra.setGravity(
                Gravity.CENTER_VERTICAL
        );

        barra.setPadding(
                dp(10),
                dp(8),
                dp(10),
                dp(8)
        );

        GradientDrawable fundoBarra =
                new GradientDrawable();

        fundoBarra.setColor(
                Color.rgb(7, 12, 22)
        );

        fundoBarra.setStroke(
                dp(1),
                Color.rgb(30, 80, 130)
        );

        barra.setBackground(fundoBarra);

        campoMensagem =
                new EditText(this);

        campoMensagem.setHint(
                "Digite uma mensagem..."
        );

        campoMensagem.setHintTextColor(
                Color.rgb(100, 115, 140)
        );

        campoMensagem.setTextColor(
                Color.WHITE
        );

        campoMensagem.setTextSize(15);

        campoMensagem.setSingleLine(false);

        campoMensagem.setPadding(
                dp(15),
                dp(10),
                dp(15),
                dp(10)
        );

        GradientDrawable fundoCampo =
                new GradientDrawable();

        fundoCampo.setColor(
                Color.rgb(14, 22, 38)
        );

        fundoCampo.setCornerRadius(
                dp(24)
        );

        fundoCampo.setStroke(
                dp(1),
                Color.rgb(45, 105, 165)
        );

        campoMensagem.setBackground(
                fundoCampo
        );

        barra.addView(
                campoMensagem,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        TextView enviar =
                new TextView(this);

        enviar.setText("➤");
        enviar.setTextSize(24);
        enviar.setTextColor(
                Color.rgb(80, 190, 255)
        );

        enviar.setGravity(Gravity.CENTER);

        barra.addView(
                enviar,
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(52)
                )
        );

        enviar.setOnClickListener(
                v -> enviarMensagem()
        );

        principal.addView(
                barra,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        setContentView(raiz);
    }

    private void carregarPerfil() {

        db.collection("perfis_publicos")
                .document(outroUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        return;
                    }

                    String nome = doc.getString("nome");

                    if (nome != null &&
                            !nome.trim().isEmpty()) {
                        nomeOutro = nome;
                    }

                    if (raiz != null) {
                        atualizarNomeTopo();
                    }
                });
    }

    private void atualizarNomeTopo() {

        if (raiz == null) {
            return;
        }

        TextView nome = encontrarNomeTopo();

        if (nome != null) {
            nome.setText(nomeOutro);
        }
    }

    private TextView encontrarNomeTopo() {

        if (!(raiz.getChildAt(0)
                instanceof LinearLayout)) {
            return null;
        }

        LinearLayout principal =
                (LinearLayout) raiz.getChildAt(0);

        if (principal.getChildCount() == 0) {
            return null;
        }

        View topo = principal.getChildAt(0);

        if (!(topo instanceof LinearLayout)) {
            return null;
        }

        LinearLayout barra =
                (LinearLayout) topo;

        if (barra.getChildCount() < 3) {
            return null;
        }

        View bloco = barra.getChildAt(2);

        if (!(bloco instanceof LinearLayout)) {
            return null;
        }

        LinearLayout nomes =
                (LinearLayout) bloco;

        if (nomes.getChildCount() == 0) {
            return null;
        }

        View primeiro =
                nomes.getChildAt(0);

        if (primeiro instanceof TextView) {
            return (TextView) primeiro;
        }

        return null;
    }

    private String gerarIdConversa() {

        if (meuUid.compareTo(outroUid) < 0) {
            return meuUid + "_" + outroUid;
        }

        return outroUid + "_" + meuUid;
    }

    private void iniciarMensagens() {

        String conversaId =
                gerarIdConversa();

        listenerMensagens =
                db.collection("chats")
                        .document(conversaId)
                        .collection("mensagens")
                        .orderBy(
                                "criadoEm",
                                Query.Direction.ASCENDING
                        )
                        .addSnapshotListener(
                                (snapshot, error) -> {

                            if (error != null ||
                                    snapshot == null) {
                                return;
                            }

                            listaMensagens.removeAllViews();

                            for (DocumentSnapshot doc :
                                    snapshot.getDocuments()) {

                                String remetente =
                                        doc.getString(
                                                "remetenteUid"
                                        );

                                String texto =
                                        doc.getString("texto");

                                if (texto == null) {
                                    texto = "";
                                }

                                adicionarMensagem(
                                        texto,
                                        meuUid.equals(remetente)
                                );
                            }

                            scroll.post(() ->
                                    scroll.fullScroll(
                                            View.FOCUS_DOWN
                                    )
                            );
                        }
                        );
    }

    private void adicionarMensagem(
            String textoMensagem,
            boolean minha
    ) {

        LinearLayout linha =
                new LinearLayout(this);

        linha.setOrientation(
                LinearLayout.HORIZONTAL
        );

        linha.setGravity(
                minha
                        ? Gravity.RIGHT
                        : Gravity.LEFT
        );

        TextView mensagem =
                texto(
                        textoMensagem,
                        15,
                        Color.WHITE
                );

        mensagem.setPadding(
                dp(15),
                dp(10),
                dp(15),
                dp(10)
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setCornerRadius(dp(18));

        if (minha) {

            fundo.setColor(
                    Color.rgb(20, 65, 105)
            );

            fundo.setStroke(
                    dp(1),
                    Color.rgb(55, 160, 235)
            );

        } else {

            fundo.setColor(
                    Color.rgb(24, 27, 38)
            );

            fundo.setStroke(
                    dp(1),
                    Color.rgb(75, 85, 110)
            );
        }

        mensagem.setBackground(fundo);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                );

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        linha.addView(
                mensagem,
                params
        );

        listaMensagens.addView(
                linha,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }

    private void enviarMensagem() {

        String textoMensagem =
                campoMensagem
                        .getText()
                        .toString()
                        .trim();

        if (textoMensagem.isEmpty()) {
            return;
        }

        if (meuUid.isEmpty()) {

            Toast.makeText(
                    this,
                    "Faça login para enviar mensagens.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String conversaId =
                gerarIdConversa();

        Map<String, Object> dados =
                new HashMap<>();

        dados.put(
                "remetenteUid",
                meuUid
        );

        dados.put(
                "destinatarioUid",
                outroUid
        );

        dados.put(
                "texto",
                textoMensagem
        );

        dados.put(
                "tipo",
                "texto"
        );

        dados.put(
                "criadoEm",
                FieldValue.serverTimestamp()
        );

        db.collection("chats")
                .document(conversaId)
                .collection("mensagens")
                .add(dados)
                .addOnSuccessListener(v -> {

                    campoMensagem.setText("");

                    campoMensagem.clearFocus();

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {
                        imm.hideSoftInputFromWindow(
                                campoMensagem.getWindowToken(),
                                0
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível enviar a mensagem.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private TextView texto(
            String texto,
            float tamanho,
            int cor
    ) {

        TextView t =
                new TextView(this);

        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

        return t;
    }

    private int dp(int valor) {

        return (int) (
                valor *
                getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
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
