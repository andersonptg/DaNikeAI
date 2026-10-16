package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatIndividualActivity extends Activity {

    private FrameLayout raiz;
    private LinearLayout listaMensagens;
    private EditText campoMensagem;
    private ScrollView scroll;
    private FirebaseFirestore db;
    private ListenerRegistration listenerMensagens;
    private ListenerRegistration listenerDigitando;

    private String meuUid = "";
    private String outroUid = "";
    private String nomeOutro = "Usuário";
private ImageView fotoTopo;
private TextView statusTopo;
private boolean euEstouDigitando = false;
    private boolean outroEstaDigitando = false;
    private boolean outroEstaOnline = false;
private View indicadorDigitando;
private TextView pontosDigitando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

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
        iniciarPresencaOutroUsuario();
        prepararConversa();
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

        fotoTopo = new ImageView(this);

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

        fotoTopo.setBackground(fundoFoto);
        fotoTopo.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        topo.addView(
                fotoTopo,
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

        statusTopo =
                texto(
                        "● OFFLINE",
                        12,
                        Color.rgb(255, 55, 80)
                );

        blocoNome.addView(
                nome,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        blocoNome.addView(
                statusTopo,
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

        campoMensagem.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        boolean digitando =
                                s != null &&
                                s.toString().trim().length() > 0;

                        if (euEstouDigitando != digitando) {
                    euEstouDigitando = digitando;
                    atualizarEstadoDigitando(digitando);
                }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
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

        raiz.setOnApplyWindowInsetsListener((v, insets) -> {
            int teclado = insets.getInsets(
                    WindowInsets.Type.ime()
            ).bottom;

            barra.setTranslationY(-teclado);

            return insets;
        });

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
                    String fotoUrl = doc.getString("fotoUrl");

                    if (nome != null && !nome.trim().isEmpty()) {
                        nomeOutro = nome;
                    }

                    if (fotoTopo != null
                            && fotoUrl != null
                            && !fotoUrl.trim().isEmpty()) {
                        carregarImagemUrl(fotoTopo, fotoUrl.trim());
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

    private void iniciarPresencaOutroUsuario() {
        if (outroUid == null || outroUid.trim().isEmpty()) return;

        db.collection("usuarios")
                .document(outroUid)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null || !doc.exists()) {
                        atualizarStatusTopo(false);
                        return;
                    }

                    Boolean online = doc.getBoolean("online");
                    com.google.firebase.Timestamp ultimo =
                            doc.getTimestamp("ultimoAcesso");

                    boolean estaOnline = false;

                    if (Boolean.TRUE.equals(online) && ultimo != null) {
                        long idade =
                                System.currentTimeMillis()
                                - ultimo.toDate().getTime();

                        estaOnline = idade >= 0 && idade <= 90_000L;
                    }

                    atualizarStatusTopo(estaOnline);
                });
    }

    private void atualizarStatusTopo(boolean online) {
        if (statusTopo == null) return;

        if (online) {
            statusTopo.setText("● ONLINE");
            statusTopo.setTextColor(
                    Color.rgb(50, 255, 145)
            );
        } else {
            statusTopo.setText("● OFFLINE");
            statusTopo.setTextColor(
                    Color.rgb(255, 55, 80)
            );
        }
    }

    private void atualizarEstadoDigitando(boolean digitando) {
        if (meuUid == null || meuUid.trim().isEmpty()) {
            return;
        }

        String conversaId = gerarIdConversa();

        if (digitando) {
            db.collection("chats")
                    .document(conversaId)
                    .set(
                            new HashMap<String, Object>() {{
                                put(
                                        "digitandoPor",
                                        FieldValue.arrayUnion(meuUid)
                                );
                            }},
                            SetOptions.merge()
                    );
        } else {
            db.collection("chats")
                    .document(conversaId)
                    .set(
                            new HashMap<String, Object>() {{
                                put(
                                        "digitandoPor",
                                        FieldValue.arrayRemove(meuUid)
                                );
                            }},
                            SetOptions.merge()
                    );
        }
    }

    private String gerarIdConversa() {

        if (meuUid.compareTo(outroUid) < 0) {
            return meuUid + "_" + outroUid;
        }

        return outroUid + "_" + meuUid;
    }


    private void carregarImagemUrl(
            ImageView imageView,
            String url
    ) {
        new Thread(() -> {
            android.graphics.Bitmap bitmap = null;
            HttpURLConnection conexao = null;
            InputStream entrada = null;

            try {
                URL endereco = new URL(url);
                conexao = (HttpURLConnection)
                        endereco.openConnection();

                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(8000);
                conexao.setInstanceFollowRedirects(true);
                conexao.connect();

                if (conexao.getResponseCode()
                        == HttpURLConnection.HTTP_OK) {

                    entrada = conexao.getInputStream();

                    bitmap =
                            android.graphics.BitmapFactory
                                    .decodeStream(entrada);
                }

            } catch (Exception ignored) {

            } finally {
                try {
                    if (entrada != null) {
                        entrada.close();
                    }
                } catch (Exception ignored) {
                }

                if (conexao != null) {
                    conexao.disconnect();
                }
            }

            final android.graphics.Bitmap resultado = bitmap;

            runOnUiThread(() -> {
                if (resultado != null
                        && !isFinishing()
                        && !isDestroyed()
                        && imageView != null) {

                    imageView.setImageBitmap(
                            criarBitmapCircular(resultado)
                    );
                }
            });
        }).start();
    }

    private Bitmap criarBitmapCircular(Bitmap original) {
        if (original == null) {
            return null;
        }

        int tamanho = Math.min(
                original.getWidth(),
                original.getHeight()
        );

        int esquerda =
                (original.getWidth() - tamanho) / 2;

        int topo =
                (original.getHeight() - tamanho) / 2;

        Bitmap quadrado = Bitmap.createBitmap(
                original,
                esquerda,
                topo,
                tamanho,
                tamanho
        );

        Bitmap circular = Bitmap.createBitmap(
                tamanho,
                tamanho,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(circular);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(
                new BitmapShader(
                        quadrado,
                        Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP
                )
        );

        float raio = tamanho / 2f;

        canvas.drawCircle(
                raio,
                raio,
                raio,
                paint
        );

        return circular;
    }

    private void iniciarListenerDigitando() {
        String conversaId = gerarIdConversa();

        listenerDigitando =
                db.collection("chats")
                        .document(conversaId)
                        .addSnapshotListener((doc, error) -> {

                            if (error != null
                                    || doc == null
                                    || !doc.exists()) {
                                return;
                            }

                            Object valor =
                                    doc.get("digitandoPor");

                            boolean outroDigitando = false;

                            if (valor instanceof List) {
                                outroDigitando =
                                        ((List<?>) valor)
                                                .contains(outroUid);
                            }

                            atualizarIndicadorDigitandoOutro(
                                    outroDigitando
                            );
                        });
    }

    private void atualizarIndicadorDigitandoOutro(
            boolean digitando
    ) {
        outroEstaDigitando = digitando;

        mostrarIndicadorDigitando(digitando);

        if (statusTopo == null) {
            return;
        }

        if (digitando) {
            statusTopo.setText("● DIGITANDO...");
            statusTopo.setTextColor(
                    Color.rgb(70, 210, 255)
            );
        } else {
            atualizarStatusTopo(outroEstaOnline);
        }
    }

    private void prepararConversa() {
        if (meuUid == null || meuUid.trim().isEmpty()
                || outroUid == null || outroUid.trim().isEmpty()
                || meuUid.equals(outroUid)) {
            Toast.makeText(
                    this,
                    "Não foi possível identificar os participantes.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        String conversaId = gerarIdConversa();
        List<String> participantes = new ArrayList<>();

        if (meuUid.compareTo(outroUid) < 0) {
            participantes.add(meuUid);
            participantes.add(outroUid);
        } else {
            participantes.add(outroUid);
            participantes.add(meuUid);
        }

        com.google.firebase.firestore.DocumentReference ref =
                db.collection("chats").document(conversaId);

        // Primeiro verifica se a conversa já existe.
        // Não tenta regravar um documento existente ao abrir o chat.
        ref.get()
                .addOnSuccessListener(documento -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (documento.exists()) {
                        Object valor = documento.get("participantes");

                        if (valor instanceof List
                                && participantes.equals(valor)
                                && participantes.contains(meuUid)) {
                            iniciarMensagens();
                        } else {
                            Toast.makeText(
                                    this,
                                    "A conversa existente tem dados incompatíveis. "
                                            + "Ela não foi alterada.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        return;
                    }

                    Map<String, Object> dadosConversa = new HashMap<>();
                    dadosConversa.put("participantes", participantes);

                    // Criação apenas quando o documento ainda não existe.
                    ref.set(dadosConversa)
                            .addOnSuccessListener(v -> {
                                if (!isFinishing() && !isDestroyed()) {
                                    iniciarMensagens();
                                }
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e(
                                        "DaNikeAI",
                                        "Falha ao criar conversa " + conversaId,
                                        e
                                );
                                Toast.makeText(
                                        this,
                                        "Não foi possível criar a conversa. "
                                                + "Verifique se os dois usuários "
                                                + "estão conectados.",
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(
                            "DaNikeAI",
                            "Falha ao consultar conversa " + conversaId,
                            e
                    );
                    Toast.makeText(
                            this,
                            "Não foi possível verificar a conversa. "
                                    + "Confira sua conexão e permissões.",
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void iniciarMensagens() {
        iniciarListenerDigitando();

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

                            indicadorDigitando = null;
                            pontosDigitando = null;

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
                                        doc,
                                        texto,
                                        meuUid.equals(remetente)
                                );
                            }

                            if (outroEstaDigitando) {
                                mostrarIndicadorDigitando(true);
                            }

                            scroll.post(() ->
                                    scroll.fullScroll(
                                            View.FOCUS_DOWN
                                    )
                            );
                        }
                        );
    }

    private View criarIndicadorDigitando() {
        LinearLayout bolha = new LinearLayout(this);
        bolha.setGravity(Gravity.CENTER);
        bolha.setPadding(
                dp(14),
                dp(7),
                dp(14),
                dp(7)
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setColor(
                Color.rgb(8, 20, 34)
        );

        fundo.setCornerRadius(
                dp(18)
        );

        fundo.setStroke(
                dp(1),
                Color.rgb(45, 190, 255)
        );

        bolha.setBackground(fundo);

        TextView pontos = texto(
                "• • •",
                18,
                Color.rgb(70, 210, 255)
        );

        pontos.setGravity(Gravity.CENTER);
        bolha.addView(
                pontos,
                new LinearLayout.LayoutParams(
                        -2,
                        dp(28)
                )
        );

        pontosDigitando = pontos;

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -2,
                        dp(42)
                );

        params.gravity = Gravity.LEFT;

        params.setMargins(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        bolha.setVisibility(View.GONE);

        listaMensagens.addView(
                bolha,
                params
        );

        indicadorDigitando = bolha;

        return bolha;
    }

    private void animarPontosDigitando() {
        if (pontosDigitando == null) {
            return;
        }

        AlphaAnimation animacao =
                new AlphaAnimation(0.25f, 1.0f);

        animacao.setDuration(550);
        animacao.setRepeatMode(
                Animation.REVERSE
        );
        animacao.setRepeatCount(
                Animation.INFINITE
        );

        pontosDigitando.startAnimation(animacao);
    }

    private void mostrarIndicadorDigitando(boolean mostrar) {
        if (listaMensagens == null) {
            return;
        }

        if (mostrar) {
            if (indicadorDigitando == null
                    || indicadorDigitando.getParent() == null) {
                criarIndicadorDigitando();
            }

            indicadorDigitando.setVisibility(View.VISIBLE);

            if (pontosDigitando != null) {
                pontosDigitando.clearAnimation();
                animarPontosDigitando();
            }

            if (scroll != null) {
                scroll.post(() ->
                        scroll.fullScroll(View.FOCUS_DOWN)
                );
            }
        } else {
            if (pontosDigitando != null) {
                pontosDigitando.clearAnimation();
            }

            if (indicadorDigitando != null) {
                indicadorDigitando.setVisibility(View.GONE);
            }
        }
    }

    private void adicionarMensagem(
            DocumentSnapshot doc,
            String textoMensagem,
            boolean minha
    ) {
        Object apagadoParaObj = doc.get("apagadoPara");

        if (apagadoParaObj instanceof List
                && ((List<?>) apagadoParaObj).contains(meuUid)) {
            return;
        }

        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        linha.setGravity(
                minha ? Gravity.RIGHT : Gravity.LEFT
        );

        TextView mensagem = texto(
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

        GradientDrawable fundo = new GradientDrawable();
        fundo.setCornerRadius(dp(18));

        if (minha) {
            fundo.setColor(Color.rgb(20, 65, 105));
            fundo.setStroke(
                    dp(1),
                    Color.rgb(55, 160, 235)
            );
        } else {
            fundo.setColor(Color.rgb(24, 27, 38));
            fundo.setStroke(
                    dp(1),
                    Color.rgb(75, 85, 110)
            );
        }

        mensagem.setBackground(fundo);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-2, -2);

        params.setMargins(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        linha.addView(mensagem, params);

        mensagem.setOnLongClickListener(v -> {
            mostrarAcoesMensagem(
                    doc,
                    textoMensagem,
                    minha
            );
            return true;
        });

        listaMensagens.addView(
                linha,
                new LinearLayout.LayoutParams(-1, -2)
        );
    }

    private void mostrarAcoesMensagem(
            DocumentSnapshot doc,
            String textoMensagem,
            boolean minha
    ) {
        List<String> opcoes = new ArrayList<>();

        opcoes.add("📋 Copiar");
        opcoes.add("🗑️ Apagar para mim");

        if (minha) {
            opcoes.add("🗑️ Apagar para todos");
        }

        opcoes.add("📌 Marcar mensagem");

        new AlertDialog.Builder(this)
                .setTitle("Ações da mensagem")
                .setItems(
                        opcoes.toArray(new String[0]),
                        (dialog, which) -> {
                            String acao = opcoes.get(which);

                            if (acao.contains("Copiar")) {
                                copiarMensagem(textoMensagem);
                            } else if (acao.contains("Apagar para mim")) {
                                apagarParaMim(doc);
                            } else if (acao.contains("Apagar para todos")) {
                                apagarParaTodos(doc, minha);
                            } else if (acao.contains("Marcar mensagem")) {
                                marcarMensagem(doc);
                            }
                        }
                )
                .show();
    }

    private void copiarMensagem(String textoMensagem) {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (clipboard != null) {
            clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                            "Mensagem",
                            textoMensagem
                    )
            );

            Toast.makeText(
                    this,
                    "Mensagem copiada.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void apagarParaMim(DocumentSnapshot doc) {
        doc.getReference()
                .update(
                        "apagadoPara",
                        FieldValue.arrayUnion(meuUid)
                )
                .addOnSuccessListener(v ->
                        Toast.makeText(
                                this,
                                "Mensagem apagada para você.",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível apagar a mensagem.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void apagarParaTodos(
            DocumentSnapshot doc,
            boolean minha
    ) {
        if (!minha) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Apagar para todos?")
                .setMessage(
                        "Esta mensagem será removida para os participantes."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(
                        "Apagar",
                        (dialog, which) ->
                                doc.getReference()
                                        .delete()
                                        .addOnSuccessListener(v ->
                                                Toast.makeText(
                                                        this,
                                                        "Mensagem apagada para todos.",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        )
                                        .addOnFailureListener(e ->
                                                Toast.makeText(
                                                        this,
                                                        "Não foi possível apagar.",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        )
                )
                .show();
    }

    private void marcarMensagem(DocumentSnapshot doc) {
        boolean atual = Boolean.TRUE.equals(
                doc.getBoolean("fixada")
        );

        doc.getReference()
                .update("fixada", !atual)
                .addOnSuccessListener(v ->
                        Toast.makeText(
                                this,
                                atual
                                        ? "Mensagem desmarcada."
                                        : "Mensagem marcada.",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível marcar a mensagem.",
                                Toast.LENGTH_SHORT
                        ).show()
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

        atualizarEstadoDigitando(false);

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

        if (listenerDigitando != null) {
            listenerDigitando.remove();
        }

        if (euEstouDigitando) {
            atualizarEstadoDigitando(false);
            euEstouDigitando = false;
        }

        super.onDestroy();
    }
}
