package com.danike.ai;

import java.util.Map;

import com.google.firebase.Timestamp;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import java.io.File;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private MediaRecorder audioRecorder;
    private MediaPlayer audioPlayer;
    private File arquivoAudioTemporario;
    private TextView botaoMicrofone;
    private boolean gravandoAudio = false;
    private long inicioGravacao = 0L;
    private static final int REQUEST_AUDIO_PERMISSION = 9201;
    private static final int REQUEST_IMAGEM_GRUPO = 9202;
    private LinearLayout tecladoGrupoNexo;
    private ScrollView scrollGrupo;
    private boolean tecladoGrupoAberto = false;

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

    private void abrirSeletorFundo() {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow();

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(14), dp(14), dp(14), dp(14));
        raiz.setBackgroundColor(Color.rgb(3, 7, 14));

        LinearLayout topoFundo = new LinearLayout(this);
        topoFundo.setGravity(Gravity.CENTER_VERTICAL);

        TextView tituloFundo = texto(
                "FUNDO DA CONVERSA",
                18,
                Color.WHITE
        );
        tituloFundo.setTypeface(Typeface.DEFAULT_BOLD);

        topoFundo.addView(
                tituloFundo,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        TextView cancelar = texto("✕", 25, Color.WHITE);
        cancelar.setGravity(Gravity.CENTER);
        topoFundo.addView(
                cancelar,
                new LinearLayout.LayoutParams(dp(50), dp(55))
        );

        raiz.addView(topoFundo);

        TextView preview = texto(
                "PRÉVIA DO FUNDO",
                13,
                Color.LTGRAY
        );
        preview.setGravity(Gravity.CENTER);
        preview.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable previewBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 18, 35),
                        Color.rgb(2, 6, 16)
                }
        );
        preview.setBackground(previewBg);

        raiz.addView(
                preview,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(230)
                )
        );

        TextView escolher = texto(
                "🖼  ESCOLHER DA GALERIA",
                14,
                Color.WHITE
        );
        escolher.setGravity(Gravity.CENTER);
        escolher.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable escolherBg = new GradientDrawable();
        escolherBg.setColor(Color.rgb(15, 25, 40));
        escolherBg.setCornerRadius(dp(14));
        escolherBg.setStroke(dp(1), Color.rgb(45, 130, 220));
        escolher.setBackground(escolherBg);

        LinearLayout.LayoutParams lpEscolher =
                new LinearLayout.LayoutParams(-1, dp(52));
        lpEscolher.topMargin = dp(12);

        raiz.addView(escolher, lpEscolher);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setGravity(Gravity.CENTER_VERTICAL);

        TextView voltar = texto("✕  CANCELAR", 14, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);

        TextView salvar = texto("✓  SALVAR", 14, Color.WHITE);
        salvar.setGravity(Gravity.CENTER);

        botoes.addView(
                voltar,
                new LinearLayout.LayoutParams(0, dp(55), 1)
        );
        botoes.addView(
                salvar,
                new LinearLayout.LayoutParams(0, dp(55), 1)
        );

        LinearLayout.LayoutParams lpBotoes =
                new LinearLayout.LayoutParams(-1, dp(60));
        lpBotoes.topMargin = dp(10);

        raiz.addView(botoes, lpBotoes);

        voltar.setOnClickListener(v -> dialog.dismiss());
        cancelar.setOnClickListener(v -> dialog.dismiss());

        escolher.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Galeria será adicionada no próximo passo.",
                    Toast.LENGTH_SHORT
            ).show();
        });

        salvar.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Fundo salvo.",
                    Toast.LENGTH_SHORT
            ).show();
            dialog.dismiss();
        });

        dialog.setContentView(raiz);

        Window janela = dialog.getWindow();
        if (janela != null) {
            janela.setBackgroundDrawableResource(
                    android.R.color.transparent
            );
            janela.setLayout(
                    -1,
                    -1
            );
        }

        dialog.show();

        janela = dialog.getWindow();
        if (janela != null) {
            janela.setLayout(-1, -1);
        }
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

        // NEXO_RESPONSIVO_V4: título do grupo quebra em até 2 linhas.
        titulo.setSingleLine(false);
        titulo.setMaxLines(2);
        informacoes.addView(titulo,
                new LinearLayout.LayoutParams(-1, -2));

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
        // NEXO_PREMIUM_V3: toque no cabeçalho mostra membros e administração.
        informacoes.setOnClickListener(v -> abrirPainelGrupoNexo());

        topo.addView(informacoes,
                new LinearLayout.LayoutParams(0, -2, 1));

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

        menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menu);

            // Sem botão fictício de silenciar: requer configuração de push.
            popup.getMenu().add("PESQUISAR");
            popup.getMenu().add("FUNDO DA CONVERSA");
            popup.getMenu().add("👥 PARTICIPANTES E ADM");
            popup.getMenu().add("☑ SELECIONAR MENSAGENS");

            popup.setOnMenuItemClickListener(item -> {
                String opcao = item.getTitle().toString();

                if ("👥 PARTICIPANTES E ADM".equals(opcao)) {
                    abrirPainelGrupoNexo();
                    return true;
                }
                if ("☑ SELECIONAR MENSAGENS".equals(opcao)) {
                    selecionarMensagensGrupoNexo();
                    return true;
                }
                if ("FUNDO DA CONVERSA".equals(opcao)) {
                    escolherFundoGrupoNexo();
                    return true;
                }

                if ("SILENCIAR".equals(opcao)) {
                    Toast.makeText(
                            this,
                            "Silenciar será configurado aqui.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return true;
                }

                if ("PESQUISAR".equals(opcao)) {
                    buscarMensagemGrupoNexo();
                    return true;
                }

                return false;
            });

            popup.show();
        });

        topo.setMinimumHeight(dp(74));
        raiz.addView(topo,
                new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll =
                new ScrollView(this);
        scrollGrupo = scroll;

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

        FundoConversaHelper.aplicar(this, listaMensagens, grupoId);

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

        anexo.setContentDescription("Anexar imagem do grupo");
        anexo.setOnClickListener(v -> abrirImagemGrupo());
        areaEnviar.addView(
                anexo,
                new LinearLayout.LayoutParams(dp(43), dp(52))
        );
        TextView teclaNexo = texto("⌨", 23, Color.rgb(62, 190, 255));
        teclaNexo.setGravity(Gravity.CENTER);
        teclaNexo.setContentDescription("Abrir ou fechar teclado oficial NEXO");
        areaEnviar.addView(teclaNexo,
                new LinearLayout.LayoutParams(dp(39), dp(52)));
        teclaNexo.setOnClickListener(v -> alternarTecladoGrupoNexo());

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


        botaoMicrofone = texto(
                "🎙️",
                21,
                Color.WHITE
        );

        botaoMicrofone.setGravity(
                Gravity.CENTER
        );

        GradientDrawable fundoMicrofone =
                new GradientDrawable();

        fundoMicrofone.setShape(
                GradientDrawable.OVAL
        );

        fundoMicrofone.setColor(
                Color.rgb(15, 45, 70)
        );

        botaoMicrofone.setBackground(
                fundoMicrofone
        );

        botaoMicrofone.setOnClickListener(
                v -> alternarGravacaoAudio()
        );

        areaEnviar.addView(
                botaoMicrofone,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
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

        // O campo de digitação fica ACIMA do teclado NEXO, como no chat individual.
        tecladoGrupoNexo = new LinearLayout(this);
        tecladoGrupoNexo.setVisibility(View.GONE);
        tecladoGrupoNexo.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(tecladoGrupoNexo, new LinearLayout.LayoutParams(-1, -2));
        setContentView(raiz);
    }

    private void alternarTecladoGrupoNexo() {
        tecladoGrupoAberto = !tecladoGrupoAberto;
        tecladoGrupoNexo.setVisibility(tecladoGrupoAberto ? View.VISIBLE : View.GONE);
        campoMensagem.setShowSoftInputOnFocus(!tecladoGrupoAberto);
        if (tecladoGrupoAberto) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(campoMensagem.getWindowToken(), 0);
            campoMensagem.requestFocus();
            NexoTecladoGrupo.render(this, tecladoGrupoNexo, campoMensagem,
                    () -> { tecladoGrupoAberto = false;
                        tecladoGrupoNexo.setVisibility(View.GONE);
                        campoMensagem.setShowSoftInputOnFocus(true);
                        campoMensagem.requestFocus();
                        InputMethodManager ime = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (ime != null) ime.showSoftInput(campoMensagem,
                                InputMethodManager.SHOW_IMPLICIT);
                    });
        }
    }

    private void abrirPainelGrupoNexo() {
        NexoGrupoPainel.abrir(this, db, grupoId, () -> finish());
    }

    private void selecionarMensagensGrupoNexo() {
        if (auth.getCurrentUser() != null) {
            NexoMensagensLote.selecionar(this, db, "grupos/" + grupoId,
                    auth.getCurrentUser().getUid());
        }
    }

    private void escolherFundoGrupoNexo() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Papel de parede · Grupo NEXO")
            .setItems(FundoConversaHelper.NOMES, (d,i) -> {
                FundoConversaHelper.salvar(this,FundoConversaHelper.CHAVES[i],grupoId);
                FundoConversaHelper.aplicar(this,listaMensagens,grupoId);
                Toast.makeText(this,"Fundo escolhido para este grupo.",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Cancelar",null).show();
    }

    private boolean contemTextoGrupo(View v,String alvo) {
        if(v instanceof TextView && ((TextView)v).getText().toString()
                .toLowerCase(java.util.Locale.ROOT).contains(alvo)) return true;
        if(v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g=(android.view.ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) if(contemTextoGrupo(g.getChildAt(i),alvo)) return true;
        }
        return false;
    }

    private void buscarMensagemGrupoNexo() {
        EditText busca=new EditText(this); busca.setSingleLine(true);
        busca.setHint("Pesquisar texto no grupo");
        new android.app.AlertDialog.Builder(this).setTitle("Buscar mensagens")
            .setView(busca).setNegativeButton("Cancelar",null)
            .setPositiveButton("Buscar",(d,w)->{
                String alvo=busca.getText().toString().trim()
                        .toLowerCase(java.util.Locale.ROOT);
                if(alvo.isEmpty())return;
                for(int i=0;i<listaMensagens.getChildCount();i++) {
                    View linha=listaMensagens.getChildAt(i);
                    if(contemTextoGrupo(linha,alvo)) {
                        final View achada=linha;
                        if(scrollGrupo!=null)scrollGrupo.post(()->
                            scrollGrupo.smoothScrollTo(0,achada.getTop()));
                        achada.animate().alpha(.5f).setDuration(180).withEndAction(()->
                            achada.animate().alpha(1f).setDuration(200).start()).start();
                        return;
                    }
                }
                Toast.makeText(this,"Nenhuma mensagem encontrada nesta conversa.",
                        Toast.LENGTH_SHORT).show();
            }).show();
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

                                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                        Object apagado = doc.get("apagadoPara");
                                        if (apagado instanceof java.util.List &&
                                                auth.getCurrentUser() != null &&
                                                ((java.util.List<?>) apagado).contains(
                                                        auth.getCurrentUser().getUid())) continue;
                                        adicionarMensagem(doc);
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

        String tipo =
                doc.getString("tipo");

        TextView mensagem;

        if ("imagem".equals(tipo)) {
            mensagem=texto("🖼  IMAGEM  •  Toque para ampliar" +
                    (texto.isEmpty()?"":"\n"+texto),15,Color.WHITE);
            String path=doc.getString("imagemPath");
            mensagem.setOnClickListener(v->abrirImagemRecebidaGrupo(path));
        } else if ("audio".equals(tipo)) {
            mensagem =
                    texto(
                            "🎙️  Mensagem de voz\n▶️  Toque para reproduzir",
                            14,
                            Color.WHITE
                    );

            String audioUrl =
                    doc.getString("audioUrl");

            mensagem.setOnClickListener(
                    v -> reproduzirAudio(
                            audioUrl,
                            mensagem
                    )
            );

        } else {
            mensagem =
                    texto(
                            texto,
                            15,
                            Color.WHITE
                    );
        }

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


    private void alternarGravacaoAudio() {
        if (gravandoAudio) {
            finalizarGravacaoAudio(true);
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_AUDIO_PERMISSION
            );

            return;
        }

        iniciarGravacaoAudio();
    }

    private void iniciarGravacaoAudio() {
        try {
            arquivoAudioTemporario =
                    new File(
                            getCacheDir(),
                            "grupo_audio_"
                                    + System.currentTimeMillis()
                                    + ".m4a"
                    );

            audioRecorder =
                    new MediaRecorder();

            audioRecorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC
            );

            audioRecorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
            );

            audioRecorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
            );

            audioRecorder.setAudioEncodingBitRate(
                    96000
            );

            audioRecorder.setAudioSamplingRate(
                    44100
            );

            audioRecorder.setOutputFile(
                    arquivoAudioTemporario.getAbsolutePath()
            );

            audioRecorder.prepare();
            audioRecorder.start();

            gravandoAudio = true;
            inicioGravacao =
                    SystemClock.elapsedRealtime();

            botaoMicrofone.setText("⏹️");

            Toast.makeText(
                    this,
                    "Gravando áudio...",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            android.util.Log.e(
                    "DaNikeAI_AUDIO",
                    "Erro grupo",
                    e
            );

            gravandoAudio = false;

            Toast.makeText(
                    this,
                    "Não foi possível gravar.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void finalizarGravacaoAudio(boolean enviar) {
        if (!gravandoAudio) {
            return;
        }

        try {
            audioRecorder.stop();
        } catch (Exception ignored) {
        }

        try {
            audioRecorder.release();
        } catch (Exception ignored) {
        }

        audioRecorder = null;
        gravandoAudio = false;

        botaoMicrofone.setText("🎙️");

        if (!enviar ||
                arquivoAudioTemporario == null ||
                !arquivoAudioTemporario.exists()) {

            if (arquivoAudioTemporario != null) {
                arquivoAudioTemporario.delete();
            }

            return;
        }

        enviarAudioGrupo();
    }

    private void enviarAudioGrupo() {
        if (auth.getCurrentUser() == null
                || grupoId == null
                || grupoId.trim().isEmpty()) {
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        String nome =
                System.currentTimeMillis()
                        + "_"
                        + uid
                        + ".m4a";

        StorageReference ref =
                FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child("group_audio")
                        .child(grupoId)
                        .child(uid)
                        .child(nome);

        Toast.makeText(
                this,
                "Enviando áudio...",
                Toast.LENGTH_SHORT
        ).show();

        ref.putFile(
                Uri.fromFile(
                        arquivoAudioTemporario
                )
        ).addOnSuccessListener(v ->
                ref.getDownloadUrl()
                        .addOnSuccessListener(url -> {
                            Map<String, Object> mensagem =
                                    ChatModelo.mensagem(
                                            uid,
                                            "🎙️ Mensagem de voz",
                                            "audio"
                                    );

                            mensagem.put("autorUid", uid);
                            mensagem.put(
                                    "audioUrl",
                                    url.toString()
                            );

                            mensagem.put(
                                    "duracaoMs",
                                    SystemClock.elapsedRealtime()
                                            - inicioGravacao
                            );

                            db.collection("grupos")
                                    .document(grupoId)
                                    .collection("mensagens")
                                    .add(mensagem)
                                    .addOnSuccessListener(x -> {
                                        arquivoAudioTemporario.delete();

                                        Toast.makeText(
                                                this,
                                                "Áudio enviado.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                        })
        ).addOnFailureListener(e ->
                Toast.makeText(
                        this,
                        "Não foi possível enviar o áudio.",
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    private void reproduzirAudio(
            String url,
            TextView botao
    ) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        try {
            if (audioPlayer != null) {
                audioPlayer.release();
            }

            audioPlayer =
                    new MediaPlayer();

            audioPlayer.setDataSource(url);

            audioPlayer.setOnPreparedListener(
                    mp -> {
                        mp.start();
                        botao.setText(
                                "⏸️  Reproduzindo..."
                        );
                    }
            );

            audioPlayer.setOnCompletionListener(
                    mp -> {
                        botao.setText(
                                "🎙️  Mensagem de voz\\n▶️  Toque para reproduzir"
                        );
                        mp.release();
                        audioPlayer = null;
                    }
            );

            audioPlayer.prepareAsync();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Áudio indisponível.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_AUDIO_PERMISSION
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {

            iniciarGravacaoAudio();
        }
    }

    private void abrirImagemGrupo() {
        android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");startActivityForResult(intent,REQUEST_IMAGEM_GRUPO);
    }
    @Override protected void onActivityResult(int codigo,int resultado,android.content.Intent dados) {
        super.onActivityResult(codigo,resultado,dados);
        if(codigo==REQUEST_IMAGEM_GRUPO&&resultado==RESULT_OK&&dados!=null)
            enviarImagemGrupo(dados.getData());
    }
    private void enviarImagemGrupo(Uri uri) {
        if(uri==null||grupoId.isEmpty()||auth.getCurrentUser()==null)return;
        String mime=getContentResolver().getType(uri);
        if(mime==null||!mime.startsWith("image/"))return;
        try(android.database.Cursor c=getContentResolver().query(uri,
            new String[]{android.provider.OpenableColumns.SIZE},null,null,null)){
            if(c!=null&&c.moveToFirst()){
                int n=c.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if(n>=0&&!c.isNull(n)&&c.getLong(n)>8L*1024*1024){
                    Toast.makeText(this,"Limite de 8 MB.",Toast.LENGTH_LONG).show();return;
                }
            }
        }catch(Exception ignored){}
        String uid=auth.getCurrentUser().getUid();
        String path="group_images/"+grupoId+"/"+uid+"/"+
            java.util.UUID.randomUUID().toString()+".jpg";
        StorageReference ref=FirebaseStorage.getInstance().getReference().child(path);
        ref.putFile(uri,new com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType(mime).build())
            .addOnSuccessListener(x->{
                Map<String,Object> mensagem=ChatModelo.mensagem(uid,"","imagem");
                mensagem.put("autorUid",uid);
                mensagem.put("imagemPath",path);
                db.collection("grupos").document(grupoId).collection("mensagens")
                  .add(mensagem)
                  .addOnSuccessListener(v->Toast.makeText(this,"Imagem enviada.",Toast.LENGTH_SHORT).show())
                  .addOnFailureListener(e->{ref.delete();Toast.makeText(this,
                    "Mensagem de imagem recusada pelo Firestore.",Toast.LENGTH_LONG).show();});
            }).addOnFailureListener(e->Toast.makeText(this,
                "Erro no upload de imagem.",Toast.LENGTH_LONG).show());
    }
    private void abrirImagemRecebidaGrupo(String path) {
        if(path==null||!path.startsWith("group_images/"+grupoId+"/"))return;
        FirebaseStorage.getInstance().getReference().child(path).getBytes(8L*1024*1024)
            .addOnSuccessListener(bytes->{
                android.graphics.BitmapFactory.Options opts=new android.graphics.BitmapFactory.Options();
                opts.inJustDecodeBounds=true;
                android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                int max=Math.max(opts.outWidth,opts.outHeight);
                opts.inSampleSize=1;while(max/opts.inSampleSize>1400)opts.inSampleSize*=2;
                opts.inJustDecodeBounds=false;
                android.graphics.Bitmap bmp=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                if(bmp==null)return;
                Dialog dialog=new Dialog(this);ImageView foto=new ImageView(this);
                foto.setImageBitmap(bmp);foto.setBackgroundColor(Color.rgb(2,9,22));
                foto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                dialog.setContentView(foto);dialog.show();
                Window window=dialog.getWindow();if(window!=null)window.setLayout(-1,-1);
                foto.setOnClickListener(v->dialog.dismiss());
            }).addOnFailureListener(e->Toast.makeText(this,
                "Imagem indisponível.",Toast.LENGTH_SHORT).show());
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

        mensagem.put("autorUid", meuUid);
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
