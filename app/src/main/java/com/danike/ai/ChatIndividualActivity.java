package com.danike.ai;

import android.app.Activity;
import android.widget.PopupMenu;
import android.widget.GridLayout;
import android.graphics.drawable.Drawable;
import android.app.Dialog;
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
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.URLSpan;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import androidx.core.app.NotificationCompat;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.SystemClock;
import java.io.File;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.StorageMetadata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
    private MediaRecorder audioRecorder;
    private MediaPlayer audioPlayer;
    private File arquivoAudioTemporario;
    private TextView botaoMicrofone;
    private boolean gravandoAudio = false;
    private long inicioGravacao = 0L;
    private static final int REQUEST_AUDIO_PERMISSION = 9101;

    private String nomeOutro = "Usuário";
private ImageView fotoTopo;
private TextView statusTopo;
private boolean euEstouDigitando = false;
    private boolean outroEstaDigitando = false;
    private boolean outroEstaOnline = false;
    private boolean enviandoMensagem = false;
    private boolean mensagensCarregadas = false;
    private String ultimaMensagemNotificada = "";

    private Dialog dialogFundo;
    private TextView previewFundo;
    private String fundoTemporario;
private View indicadorDigitando;
private TextView pontosDigitando;
    private String ultimaDataExibida = "";
    private String respostaTextoSelecionado = "";
    private String respostaUidSelecionado = "";
    private LinearLayout tecladoNexo;
    private boolean tecladoNexoAberto = false;
    private boolean tecladoSimbolos = false;
    private boolean painelEmojisNexo = false;
    private boolean tecladoMaiusculo = false;
    private boolean audioTravado = false;
    private float toqueAudioX, toqueAudioY;
    private boolean audioGestoCancelado = false;
    private static final int COR_NEXO = Color.rgb(75, 172, 255);

    private LinearLayout painelResposta;
    private TextView textoResposta;

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

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if(requestCode==8102) {
            if(resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
                prepararImagemChat(data.getData());
            return;
        }
        if (requestCode != 8001 ||
                resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        Uri uri = data.getData();

        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        fundoTemporario = "uri:" + uri.toString();

        if (previewFundo != null) {

            Drawable fundo =
                    FundoConversaHelper.criar(
                            this,
                            fundoTemporario
                    );

            if (fundo != null) {
                previewFundo.setBackground(fundo);
                previewFundo.setText(
                        "PRÉVIA  •  IMAGEM DA GALERIA"
                );
            }
        }
    }

    private void abrirSeletorFundo() {

        final Dialog dialog = new Dialog(this);
        dialogFundo = dialog;

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(14), dp(10), dp(14), dp(10));
        raiz.setBackgroundColor(Color.rgb(3, 7, 14));

        LinearLayout topoFundo = new LinearLayout(this);
        topoFundo.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setText("FUNDO DA CONVERSA");
        titulo.setTextSize(18);
        titulo.setTextColor(Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);

        topoFundo.addView(titulo,
                new LinearLayout.LayoutParams(0, dp(55), 1));

        TextView fechar = new TextView(this);
        fechar.setText("✕");
        fechar.setTextSize(25);
        fechar.setTextColor(Color.WHITE);
        fechar.setGravity(Gravity.CENTER);

        topoFundo.addView(fechar,
                new LinearLayout.LayoutParams(dp(50), dp(55)));

        raiz.addView(topoFundo);

        TextView preview = new TextView(this);
        previewFundo = preview;
        preview.setText("PRÉVIA DO FUNDO");
        preview.setTextSize(14);
        preview.setTextColor(Color.WHITE);
        preview.setGravity(Gravity.CENTER);
        preview.setTypeface(Typeface.DEFAULT_BOLD);

        Drawable inicial = FundoConversaHelper.criar(
                this,
                FundoConversaHelper.obter(this, gerarIdConversa())
        );

        if (inicial != null) {
            preview.setBackground(inicial);
        }

        raiz.addView(preview,
                new LinearLayout.LayoutParams(-1, dp(170)));

        ScrollView scrollFundos = new ScrollView(this);

        GridLayout grade = new GridLayout(this);
        grade.setColumnCount(3);

        fundoTemporario =
                FundoConversaHelper.obter(this, gerarIdConversa());

        for (int i = 0; i < FundoConversaHelper.CHAVES.length; i++) {

            final String chave = FundoConversaHelper.CHAVES[i];
            final String nome = FundoConversaHelper.NOMES[i];

            TextView item = new TextView(this);
            item.setText(nome);
            item.setTextSize(11);
            item.setTextColor(Color.WHITE);
            item.setGravity(Gravity.CENTER);
            item.setTypeface(Typeface.DEFAULT_BOLD);

            Drawable fundoItem =
                    FundoConversaHelper.criar(this, chave);

            if (fundoItem != null) {
                item.setBackground(fundoItem);
            }

            GridLayout.LayoutParams lp =
                    new GridLayout.LayoutParams();

            lp.width = dp(105);
            lp.height = dp(70);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));

            grade.addView(item, lp);

            item.setOnClickListener(v -> {
                fundoTemporario = chave;

                Drawable fundo =
                        FundoConversaHelper.criar(this, chave);

                if (fundo != null) {
                    preview.setBackground(fundo);
                }

                preview.setText("PRÉVIA  •  " + nome);
            });
        }

        scrollFundos.addView(grade);

        raiz.addView(scrollFundos,
                new LinearLayout.LayoutParams(-1, 0, 1));

        TextView galeria = new TextView(this);
        galeria.setText("🖼  ESCOLHER DA GALERIA");
        galeria.setTextSize(14);
        galeria.setTextColor(Color.WHITE);
        galeria.setGravity(Gravity.CENTER);

        GradientDrawable fundoGaleria =
                new GradientDrawable();

        fundoGaleria.setColor(Color.rgb(15, 25, 40));
        fundoGaleria.setCornerRadius(dp(14));
        fundoGaleria.setStroke(
                dp(1),
                Color.rgb(45, 130, 220)
        );

        galeria.setBackground(fundoGaleria);

        raiz.addView(
                galeria,
                new LinearLayout.LayoutParams(-1, dp(50))
        );

        LinearLayout botoes = new LinearLayout(this);
        botoes.setGravity(Gravity.CENTER_VERTICAL);

        TextView cancelar = new TextView(this);
        cancelar.setText("✕  CANCELAR");
        cancelar.setTextSize(14);
        cancelar.setTextColor(Color.WHITE);
        cancelar.setGravity(Gravity.CENTER);

        TextView salvar = new TextView(this);
        salvar.setText("✓  SALVAR");
        salvar.setTextSize(14);
        salvar.setTextColor(Color.WHITE);
        salvar.setGravity(Gravity.CENTER);

        botoes.addView(
                cancelar,
                new LinearLayout.LayoutParams(0, dp(52), 1)
        );

        botoes.addView(
                salvar,
                new LinearLayout.LayoutParams(0, dp(52), 1)
        );

        raiz.addView(
                botoes,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        fechar.setOnClickListener(v -> dialog.dismiss());
        cancelar.setOnClickListener(v -> dialog.dismiss());

        galeria.setOnClickListener(v -> {

            Intent intent =
                    new Intent(Intent.ACTION_OPEN_DOCUMENT);

            intent.addCategory(
                    Intent.CATEGORY_OPENABLE
            );

            intent.setType("image/*");

            startActivityForResult(intent, 8001);
        });

        salvar.setOnClickListener(v -> {

            FundoConversaHelper.salvar(
                    this,
                    fundoTemporario,
                    gerarIdConversa()
            );

            FundoConversaHelper.aplicar(
                    this,
                    listaMensagens,
                    gerarIdConversa()
            );

            Toast.makeText(
                    this,
                    "Fundo salvo apenas nesta conversa.",
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
        }

        dialog.show();

        janela = dialog.getWindow();

        if (janela != null) {
            janela.setLayout(-1, -1);
        }
    }

    private void montarInterface() {

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(2, 8, 22));

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

        topo.setOrientation(LinearLayout.HORIZONTAL);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(
                dp(10),
                dp(8),
                dp(8),
                dp(8)
        );

        GradientDrawable fundoTopo =
                new GradientDrawable();

        fundoTopo.setColor(Color.rgb(4, 16, 37));
        fundoTopo.setCornerRadius(dp(22));
        fundoTopo.setStroke(
                dp(1),
                Color.rgb(40, 125, 205)
        );

        topo.setBackground(fundoTopo);

        TextView voltar = new TextView(this);
        voltar.setText("‹");
        voltar.setTextSize(34);
        voltar.setTextColor(
                Color.rgb(95, 205, 255)
        );
        voltar.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lpVoltar =
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(54)
                );

        topo.addView(voltar, lpVoltar);

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
                Color.rgb(15, 27, 45)
        );

        fundoFoto.setStroke(
                dp(2),
                Color.rgb(55, 175, 245)
        );

        fotoTopo.setBackground(fundoFoto);
        fotoTopo.setPadding(
                dp(4),
                dp(4),
                dp(4),
                dp(4)
        );

        LinearLayout.LayoutParams lpFoto =
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                );

        lpFoto.rightMargin = dp(8);

        topo.addView(fotoTopo, lpFoto);
        fotoTopo.setContentDescription("Abrir perfil do contato");
        fotoTopo.setOnClickListener(v -> abrirPerfilContato());

        LinearLayout blocoNome =
                new LinearLayout(this);

        blocoNome.setOrientation(
                LinearLayout.VERTICAL
        );

        blocoNome.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView nome =
                texto(
                        nomeOutro,
                        16,
                        Color.WHITE
                );

        nome.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

        statusTopo =
                texto(
                        "● OFFLINE",
                        12,
                        Color.rgb(255, 55, 80)
                );

        // NEXO_RESPONSIVO_V4: nome ajustado à janela e fonte do Android.
        nome.setSingleLine(false);
        nome.setMaxLines(2);
        nome.setEllipsize(android.text.TextUtils.TruncateAt.END);
        blocoNome.addView(nome, new LinearLayout.LayoutParams(-1, -2));

        statusTopo.setTextSize(12);
        statusTopo.setSingleLine(true);
        statusTopo.setMaxLines(1);
        statusTopo.setEllipsize(null);
        statusTopo.setGravity(Gravity.CENTER_VERTICAL);
        statusTopo.setIncludeFontPadding(true);

        blocoNome.addView(
                statusTopo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(24)
                )
        );

        blocoNome.setOnClickListener(v -> abrirPerfilContato());
        topo.addView(
                blocoNome,
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                )
        );

        TextView menu =
                new TextView(this);

        menu.setText("⋮");
        menu.setTextSize(30);
        menu.setTextColor(Color.WHITE);
        menu.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lpMenu =
                new LinearLayout.LayoutParams(
                        dp(44),
                        dp(54)
                );

        topo.addView(menu, lpMenu);

        menu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, menu);
            popup.getMenu().add("👤  Ver perfil");
            popup.getMenu().add("🔎  Buscar na conversa");
            popup.getMenu().add("🎨  Papel de parede");
            popup.getMenu().add("☑  Selecionar mensagens");
            popup.getMenu().add("🧹  Limpar conversa para mim");
            popup.getMenu().add("📋  Copiar ID da conversa");
            popup.setOnMenuItemClickListener(item -> {
                String opcao = item.getTitle().toString();
                if (opcao.contains("Ver perfil")) abrirPerfilContato();
                else if (opcao.contains("Buscar")) buscarNaConversa();
                else if (opcao.contains("Papel")) abrirSeletorFundo();
                else if (opcao.contains("Selecionar mensagens")) selecionarMensagensNexo();
                else if (opcao.contains("Limpar conversa")) limparConversaNexo();
                else if (opcao.contains("Copiar ID")) {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("ID da conversa", gerarIdConversa()));
                    Toast.makeText(this, "ID copiado", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            popup.show();
        });

        LinearLayout.LayoutParams lpTopo =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(80)
                );

        lpTopo.setMargins(
                dp(8),
                dp(10),
                dp(8),
                dp(6)
        );

        principal.addView(topo, lpTopo);

        scroll = new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setClipChildren(false);
        scroll.setClipToPadding(false);

        listaMensagens =
                new LinearLayout(this);

        listaMensagens.setOrientation(
                LinearLayout.VERTICAL
        );
        listaMensagens.setClipChildren(false);
        listaMensagens.setClipToPadding(false);

        listaMensagens.setPadding(
                dp(12),
                dp(14),
                dp(12),
                dp(14)
        );

        FundoConversaHelper.aplicar(
                this,
                listaMensagens,
                gerarIdConversa()
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

        barra.setOrientation(LinearLayout.HORIZONTAL);
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setPadding(dp(8), dp(7), dp(8), dp(7));

        GradientDrawable fundoBarra = new GradientDrawable();
        fundoBarra.setColor(Color.rgb(4, 17, 38));
        fundoBarra.setCornerRadius(dp(24));
        fundoBarra.setStroke(dp(1), Color.rgb(43, 120, 190));
        barra.setBackground(fundoBarra);

        TextView emoji = new TextView(this);
        emoji.setText("⌨");
        emoji.setContentDescription("Abrir teclado NEXO");
        emoji.setTextSize(22);
        emoji.setGravity(Gravity.CENTER);
        emoji.setTextColor(Color.rgb(115, 210, 255));
        emoji.setOnClickListener(v -> alternarTecladoNexo());

        barra.addView(
                emoji,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(52)
                )
        );

        TextView anexo=texto("⊕",25,Color.rgb(91,201,255));
        anexo.setGravity(Gravity.CENTER);
        anexo.setContentDescription("Anexar imagem");
        barra.addView(anexo,new LinearLayout.LayoutParams(dp(37),dp(52)));
        anexo.setOnClickListener(v->abrirGaleriaChat());
        campoMensagem = new EditText(this);

        campoMensagem.setHint("Digite uma mensagem...");
        campoMensagem.setHintTextColor(Color.rgb(100, 115, 140));
        campoMensagem.setTextColor(Color.WHITE);
        campoMensagem.setTextSize(15);
        campoMensagem.setSingleLine(false);
        campoMensagem.setMaxLines(6);
        campoMensagem.setMinLines(1);
        campoMensagem.setPadding(
                dp(13),
                dp(9),
                dp(13),
                dp(9)
        );

        GradientDrawable fundoCampo = new GradientDrawable();
        fundoCampo.setColor(Color.rgb(11, 29, 57));
        fundoCampo.setCornerRadius(dp(22));
        fundoCampo.setStroke(dp(1), Color.rgb(35, 85, 135));
        campoMensagem.setBackground(fundoCampo);

        campoMensagem.setOnLongClickListener(v -> {
            PopupMenu menuFormatacao = new PopupMenu(this, campoMensagem);

            menuFormatacao.getMenu().add("𝐁  Negrito");
            menuFormatacao.getMenu().add("𝐼  Itálico");
            menuFormatacao.getMenu().add("S̶  Tachado");
            menuFormatacao.getMenu().add("🔗  Link");

            menuFormatacao.setOnMenuItemClickListener(item -> {
                String opcao = item.getTitle().toString();

                if (opcao.contains("Negrito")) {
                    aplicarFormatacaoMensagem("negrito");
                } else if (opcao.contains("Itálico")) {
                    aplicarFormatacaoMensagem("italico");
                } else if (opcao.contains("Tachado")) {
                    aplicarFormatacaoMensagem("tachado");
                } else if (opcao.contains("Link")) {
                    aplicarFormatacaoMensagem("link");
                }

                return true;
            });

            menuFormatacao.show();
            return true;
        });

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
                                s != null
                                        && s.toString().trim().length() > 0;

                        if (euEstouDigitando != digitando) {
                            euEstouDigitando = digitando;
                            atualizarEstadoDigitando(digitando);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );

        barra.addView(campoMensagem,
                new LinearLayout.LayoutParams(0, -2, 1));


        botaoMicrofone = new TextView(this);
        botaoMicrofone.setText("🎙️");
        botaoMicrofone.setTextSize(21);
        botaoMicrofone.setGravity(Gravity.CENTER);
        botaoMicrofone.setTextColor(Color.rgb(70, 190, 255));

        GradientDrawable fundoMicrofone = new GradientDrawable();
        fundoMicrofone.setColor(Color.rgb(9, 30, 50));
        fundoMicrofone.setShape(GradientDrawable.OVAL);
        fundoMicrofone.setStroke(dp(1), Color.rgb(55, 160, 235));
        botaoMicrofone.setBackground(fundoMicrofone);

        LinearLayout.LayoutParams lpMicrofone =
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                );
        lpMicrofone.leftMargin = dp(5);

        barra.addView(botaoMicrofone, lpMicrofone);

        botaoMicrofone.setContentDescription("Segure para gravar; arraste para cima para travar ou para a esquerda para cancelar");
        configurarGestoMicrofone();

        TextView enviar = new TextView(this);
        enviar.setText("➤");
        enviar.setTextSize(23);
        enviar.setTextColor(Color.rgb(80, 190, 255));
        enviar.setGravity(Gravity.CENTER);

        GradientDrawable fundoEnviar = new GradientDrawable();
        fundoEnviar.setColor(Color.rgb(9, 30, 50));
        fundoEnviar.setShape(GradientDrawable.OVAL);
        fundoEnviar.setStroke(
                dp(1),
                Color.rgb(55, 160, 235)
        );
        enviar.setBackground(fundoEnviar);

        LinearLayout.LayoutParams lpEnviar =
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                );

        lpEnviar.leftMargin = dp(7);

        barra.addView(enviar, lpEnviar);

        enviar.setOnClickListener(
                v -> enviarMensagem()
        );

        LinearLayout.LayoutParams lpBarra =
                new LinearLayout.LayoutParams(-1, -2);

        lpBarra.setMargins(
                dp(8),
                dp(6),
                dp(8),
                dp(8)
        );

        painelResposta = new LinearLayout(this);
        painelResposta.setOrientation(LinearLayout.HORIZONTAL);
        painelResposta.setGravity(Gravity.CENTER_VERTICAL);
        painelResposta.setPadding(dp(12), dp(7), dp(8), dp(7));
        painelResposta.setVisibility(View.GONE);

        GradientDrawable fundoResposta = new GradientDrawable();
        fundoResposta.setColor(Color.rgb(8, 18, 30));
        fundoResposta.setCornerRadius(dp(14));
        fundoResposta.setStroke(dp(1), Color.rgb(45, 125, 190));
        painelResposta.setBackground(fundoResposta);

        textoResposta = texto("", 12, Color.rgb(205, 225, 240));
        textoResposta.setMaxLines(Integer.MAX_VALUE);
        textoResposta.setSingleLine(false);
        textoResposta.setEllipsize(null);

        LinearLayout.LayoutParams lpTextoResposta =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        painelResposta.addView(textoResposta, lpTextoResposta);

        TextView fecharResposta = texto("✕", 18, Color.rgb(120, 190, 235));
        fecharResposta.setGravity(Gravity.CENTER);
        fecharResposta.setPadding(dp(8), 0, dp(4), 0);
        fecharResposta.setOnClickListener(v -> cancelarResposta());

        painelResposta.addView(
                fecharResposta,
                new LinearLayout.LayoutParams(
                        dp(36),
                        dp(40)
                )
        );

        LinearLayout.LayoutParams lpPainelResposta =
                new LinearLayout.LayoutParams(
                        -1,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        lpPainelResposta.setMargins(dp(8), 0, dp(8), dp(2));

        // Barra e citação DEVEM ficar ACIMA do teclado NEXO.
        principal.addView(painelResposta, lpPainelResposta);
        principal.addView(barra, lpBarra);
        criarTecladoNexo();

        raiz.setOnApplyWindowInsetsListener((v, insets) -> {
            int teclado = insets.getInsets(
                    WindowInsets.Type.ime()
            ).bottom;

            // O modo adjustResize já reposiciona a barra; não duplicar deslocamento.
            barra.setTranslationY(0f);
            // Android 15: área segura acima/abaixo das barras de sistema.
            if (android.os.Build.VERSION.SDK_INT >= 35) {
                int superior=insets.getInsets(WindowInsets.Type.statusBars()).top;
                int inferior=insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                // Respeitar a altura do teclado Android e a barra de navegação.
                // O teclado NEXO mantém sua altura original.
                principal.setPadding(
                    0,
                    superior,
                    0,
                    Math.max(inferior, teclado)
                );
            }

            listaMensagens.setPadding(
                    dp(12),
                    dp(14),
                    dp(12),
                    dp(20)
            );

            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));

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

                boolean estaOnline = Boolean.TRUE.equals(online);
            outroEstaOnline = estaOnline;

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
        if (meuUid == null || meuUid.trim().isEmpty()
                || outroUid == null || outroUid.trim().isEmpty()) {
            return;
        }

        PerfilFirebase.atualizarStatusDigitando(digitando);

        String conversaId = gerarIdConversa();

        Map<String, Object> atualizacao = new HashMap<>();

        atualizacao.put(
                "digitandoPor",
                digitando
                        ? FieldValue.arrayUnion(meuUid)
                        : FieldValue.arrayRemove(meuUid)
        );

        db.collection("chats")
                .document(conversaId)
                .update(atualizacao)
                .addOnFailureListener(e ->
                        android.util.Log.d(
                                "DaNikeAI",
                                "Estado digitando não atualizado: "
                                        + e.getMessage()
                        )
                );
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

        Map<String, Object> dadosConversa =
                new HashMap<>();

        dadosConversa.put(
                "participantes",
                participantes
        );

        db.collection("chats")
                .document(conversaId)
                .set(
                        dadosConversa,
                        SetOptions.merge()
                )
                .addOnSuccessListener(v -> {

                    db.collection("chats")
                            .document(conversaId)
                            .update(
                                    "naoLidas_" + meuUid,
                                    0
                            )
                            .addOnCompleteListener(
                                    resultado -> iniciarMensagens()
                            );
                })
                .addOnFailureListener(e -> {

                    android.util.Log.e(
                            "DaNikeAI",
                            "Falha ao preparar conversa " + conversaId,
                            e
                    );

                    Toast.makeText(
                            this,
                            "Não foi possível preparar a conversa. "
                                    + "Tente novamente.",
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
                            ultimaDataExibida = "";

                            indicadorDigitando = null;
                            pontosDigitando = null;

                            for (DocumentSnapshot doc :
                                    snapshot.getDocuments()) {

                                String remetente =
                                        doc.getString(
                                                "remetenteUid"
                                        );

                                String tipoMensagem =
                                        doc.getString("tipo");

                                String texto =
                                        doc.getString("texto");

                                if (texto == null) {
                                    texto = "";
                                }

                                boolean mensagemMinha =
                                        meuUid.equals(remetente);

                                if (!mensagensCarregadas
                                        && !mensagemMinha) {
                                    ultimaMensagemNotificada =
                                            doc.getId();
                                }

                                if (mensagensCarregadas
                                        && !mensagemMinha
                                        && !doc.getId().equals(
                                                ultimaMensagemNotificada)) {

                                    ultimaMensagemNotificada =
                                            doc.getId();

                                    android.util.Log.d(
                                            "DaNikeNotificacao",
                                            "NOVA_MENSAGEM_RECEBIDA: "
                                                    + ("audio".equals(tipoMensagem)
                                                    ? "Áudio recebido"
                                                    : texto)
                                    );

                                }

                                adicionarMensagem(
                                        doc,
                                        texto,
                                        mensagemMinha
                                );
                            }

                            mensagensCarregadas = true;

                            if (outroEstaDigitando) {
                                mostrarIndicadorDigitando(true);
                            }

                            marcarMensagensComoLidas();
                            scroll.post(() -> {
                                scroll.fullScroll(View.FOCUS_DOWN);

                                scroll.postDelayed(() ->
                                        scroll.fullScroll(View.FOCUS_DOWN),
                                        120
                                );
                            });
                        }
                        );
    }

    private void marcarMensagensComoLidas() {
        if (meuUid == null || meuUid.trim().isEmpty()) {
            return;
        }

        String conversaId = gerarIdConversa();

        db.collection("chats")
                .document(conversaId)
                .collection("mensagens")
                .whereEqualTo("destinatarioUid", meuUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = db.batch();
                    boolean encontrou = false;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Boolean lida = doc.getBoolean("lida");

                        if (!Boolean.TRUE.equals(lida)) {
                            batch.update(
                                    doc.getReference(),
                                    "lida",
                                    true
                            );
                            encontrou = true;
                        }
                    }

                    if (encontrou) {
                        batch.commit();
                    }
                });
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

    private CharSequence formatarTextoMensagem(String textoOriginal) {
        if (textoOriginal == null || textoOriginal.isEmpty()) {
            return "";
        }

        SpannableString texto = new SpannableString(textoOriginal);

        java.util.regex.Pattern padraoNegrito =
                java.util.regex.Pattern.compile("\\*\\*(.+?)\\*\\*");

        java.util.regex.Matcher matcher =
                padraoNegrito.matcher(textoOriginal);

        while (matcher.find()) {
            int inicio = matcher.start();
            int fim = matcher.end();

            texto.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    inicio + 2,
                    fim - 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        java.util.regex.Pattern padraoTachado =
                java.util.regex.Pattern.compile("~~(.+?)~~");

        matcher = padraoTachado.matcher(textoOriginal);

        while (matcher.find()) {
            texto.setSpan(
                    new StrikethroughSpan(),
                    matcher.start() + 2,
                    matcher.end() - 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        java.util.regex.Pattern padraoItalico =
                java.util.regex.Pattern.compile("(?<!\\*)\\*([^*]+?)\\*(?!\\*)");

        matcher = padraoItalico.matcher(textoOriginal);

        while (matcher.find()) {
            texto.setSpan(
                    new StyleSpan(Typeface.ITALIC),
                    matcher.start() + 1,
                    matcher.end() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        java.util.regex.Pattern padraoLink =
                java.util.regex.Pattern.compile(
                        "\\[([^]]+?)\\]\\((https?://[^)]+)\\)"
                );

        matcher = padraoLink.matcher(textoOriginal);

        while (matcher.find()) {
            texto.setSpan(
                    new URLSpan(matcher.group(2)),
                    matcher.start(1),
                    matcher.end(1),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return texto;
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
        linha.setGravity(minha ? Gravity.RIGHT : Gravity.LEFT);

        LinearLayout bolha = new LinearLayout(this);
        bolha.setOrientation(LinearLayout.VERTICAL);
        bolha.setPadding(dp(12), dp(8), dp(10), dp(6));

        GradientDrawable fundo = new GradientDrawable();
        fundo.setCornerRadius(dp(18));

        if (minha) {
            fundo.setColor(Color.rgb(7, 65, 132));
            fundo.setStroke(dp(1), Color.rgb(55, 160, 235));
        } else {
            fundo.setColor(Color.rgb(25, 38, 61));
            fundo.setStroke(dp(1), Color.rgb(75, 85, 110));
        }

        bolha.setBackground(fundo);

        String respostaTexto = doc.getString("respostaTexto");
        String respostaUid = doc.getString("respostaUid");

        if (respostaTexto != null && !respostaTexto.trim().isEmpty()) {
            LinearLayout blocoResposta = new LinearLayout(this);
            blocoResposta.setOrientation(LinearLayout.VERTICAL);
            blocoResposta.setPadding(
                    dp(9),
                    dp(6),
                    dp(9),
                    dp(6)
            );

            GradientDrawable fundoResposta = new GradientDrawable();
            fundoResposta.setColor(
                    minha
                            ? Color.rgb(13, 48, 78)
                            : Color.rgb(16, 20, 30)
            );
            fundoResposta.setCornerRadius(dp(10));
            fundoResposta.setStroke(
                    dp(1),
                    minha
                            ? Color.rgb(65, 165, 225)
                            : Color.rgb(65, 105, 145)
            );
            blocoResposta.setBackground(fundoResposta);

            TextView tituloResposta = texto(
                    "↩ Resposta",
                    10,
                    Color.rgb(120, 205, 255)
            );
            tituloResposta.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            TextView textoCitado = texto(
                    respostaTexto.trim(),
                    12,
                    Color.rgb(210, 225, 238)
            );
            // NEXO_CORRECAO_RESPOSTA_COMPLETA_V1: mostrar a citação toda.
            textoCitado.setSingleLine(false);
            textoCitado.setHorizontallyScrolling(false);
            textoCitado.setMaxLines(Integer.MAX_VALUE);
            textoCitado.setEllipsize(null);
            textoCitado.setBreakStrategy(
                    android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
            );
            textoCitado.setIncludeFontPadding(true);

            blocoResposta.addView(
                    tituloResposta,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    )
            );

            LinearLayout.LayoutParams lpCitado =
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    );
            lpCitado.topMargin = dp(2);

            blocoResposta.addView(
                    textoCitado,
                    lpCitado
            );

            LinearLayout.LayoutParams lpResposta =
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    );
            lpResposta.bottomMargin = dp(6);

            // A bolha externa receberá uma largura estável para respostas.
            blocoResposta.setMinimumWidth(dp(120));
            bolha.addView(
                    blocoResposta,
                    lpResposta
            );
        }

        String tipoMensagem =
                doc.getString("tipo");

        if ("imagem".equals(tipoMensagem)) {
            String path=doc.getString("imagemPath");
            if(path!=null&&path.startsWith("chat_images/"+gerarIdConversa()+"/")){
                ImageView imagem=imagemMensagemChat(path);
                bolha.addView(imagem,new LinearLayout.LayoutParams(
                    Math.min(larguraMaximaBolha()-dp(30),dp(310)),dp(200)));
            }
            if(textoMensagem!=null&&!textoMensagem.trim().isEmpty()){
                TextView legenda=texto(textoMensagem,15,Color.WHITE);
                legenda.setPadding(0,dp(7),0,0);
                bolha.addView(legenda,new LinearLayout.LayoutParams(-2,-2));
            }
        } else if ("audio".equals(tipoMensagem)) {
            View audio = criarBolhaAudio(
                    doc,
                    minha
            );

            bolha.addView(
                    audio,
                    new LinearLayout.LayoutParams(
                            dp(245),
                            -2
                    )
            );
        } else {

        TextView mensagem = texto(
                textoMensagem,
                15,
                Color.WHITE
        );

        mensagem.setText(formatarTextoMensagem(textoMensagem));

        mensagem.setPadding(0, 0, 0, dp(2));
        mensagem.setHorizontallyScrolling(false);
        mensagem.setSingleLine(false);
        mensagem.setIncludeFontPadding(true);
        mensagem.setLineSpacing(0f, 1.05f);
        mensagem.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY);
        mensagem.setMaxWidth(
                larguraMaximaBolha()
        );

        LinearLayout.LayoutParams mensagemParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        mensagemParams.gravity = Gravity.START;

        bolha.addView(mensagem, mensagemParams);

        }

        // Reacoes persistidas no Firestore: reacao_<uid>.
        StringBuilder reacoesVisiveis = new StringBuilder();
        for (String campo : doc.getData() == null
                ? java.util.Collections.<String>emptySet()
                : doc.getData().keySet()) {
            if (campo.startsWith("reacao_") && doc.get(campo) instanceof String) {
                String emoji = (String) doc.get(campo);
                if (emoji != null && !emoji.isEmpty()) {
                    if (reacoesVisiveis.length() > 0) reacoesVisiveis.append("  ");
                    reacoesVisiveis.append(emoji);
                }
            }
        }

        com.google.firebase.Timestamp criadoEm =
                doc.getTimestamp("criadoEm");

        if (criadoEm != null) {
            String separadorData = obterSeparadorData(criadoEm);

            if (!separadorData.equals(ultimaDataExibida)) {
                TextView separador = texto(
                        separadorData,
                        11,
                        Color.rgb(120, 190, 235)
                );

                separador.setGravity(Gravity.CENTER);
                separador.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                separador.setPadding(
                        dp(10),
                        dp(8),
                        dp(10),
                        dp(8)
                );

                listaMensagens.addView(
                        separador,
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        )
                );

                ultimaDataExibida = separadorData;
            }
        }

        TextView horario = texto("", 10, Color.rgb(175, 205, 225));
        horario.setGravity(Gravity.RIGHT);

        if (criadoEm != null) {
            String hora = new SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
            ).format(criadoEm.toDate());

            horario.setText(hora);
        }

        horario.setIncludeFontPadding(true);
        horario.setMinHeight(dp(14));
        horario.setPadding(0, dp(1), 0, 0);

        LinearLayout.LayoutParams horarioParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        horarioParams.gravity = Gravity.RIGHT;

        LinearLayout linhaHorario = new LinearLayout(this);
        linhaHorario.setOrientation(LinearLayout.HORIZONTAL);
        linhaHorario.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        linhaHorario.addView(
                horario,
                horarioParams
        );

        if (minha) {
            boolean mensagemLida =
                    Boolean.TRUE.equals(doc.getBoolean("lida"));

            TextView leitura = texto(
                    "✓✓",
                    10,
                    mensagemLida
                            ? Color.rgb(45, 185, 255)
                            : Color.rgb(145, 165, 180)
            );

            leitura.setGravity(Gravity.CENTER);
            leitura.setIncludeFontPadding(true);
            leitura.setPadding(dp(4), 0, 0, 0);

            linhaHorario.addView(
                    leitura,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );
}

        // Mensagens normais continuam com largura proporcional ao texto.
        // Uma mensagem que cita outra recebe largura suficiente para não
        // comprimir a prévia em uma faixa estreita.
        boolean possuiResposta = respostaTexto != null
                && !respostaTexto.trim().isEmpty();
        LinearLayout.LayoutParams paramsBolha =
            new LinearLayout.LayoutParams(
                possuiResposta ? larguraMaximaBolha() : -2,
                -2
            );

        LinearLayout colunaMensagem = new LinearLayout(this);
        colunaMensagem.setOrientation(LinearLayout.VERTICAL);
        colunaMensagem.setGravity(minha ? Gravity.RIGHT : Gravity.LEFT);

        colunaMensagem.addView(bolha, paramsBolha);
        if (reacoesVisiveis.length() > 0) {
            TextView reacaoExterna = texto(reacoesVisiveis.toString(), 17, Color.WHITE);
            reacaoExterna.setGravity(Gravity.CENTER);
            reacaoExterna.setPadding(dp(9), dp(3), dp(9), dp(3));
            GradientDrawable selo = new GradientDrawable();
            selo.setColor(Color.rgb(10, 21, 38));
            selo.setCornerRadius(dp(18));
            selo.setStroke(dp(1), Color.rgb(55, 140, 210));
            reacaoExterna.setBackground(selo);
            LinearLayout.LayoutParams lpReacao = new LinearLayout.LayoutParams(-2, -2);
            lpReacao.gravity = minha ? Gravity.RIGHT : Gravity.LEFT;
            lpReacao.topMargin = -dp(10);
            lpReacao.leftMargin = dp(12);
            lpReacao.rightMargin = dp(12);
            lpReacao.bottomMargin = dp(3);
            colunaMensagem.setClipChildren(false);
            colunaMensagem.setClipToPadding(false);
            reacaoExterna.setElevation(dp(5));
            colunaMensagem.addView(reacaoExterna, lpReacao);
            final long[] ultimoToqueReacao = {0L};
            final Runnable[] selecaoPendente = {null};
            reacaoExterna.setOnClickListener(v -> {
                long agora = SystemClock.uptimeMillis();
                if (agora - ultimoToqueReacao[0] <= 420L) {
                    if (selecaoPendente[0] != null) {
                        reacaoExterna.removeCallbacks(selecaoPendente[0]);
                        selecaoPendente[0] = null;
                    }
                    ultimoToqueReacao[0] = 0L;
                    removerMinhaReacao(doc);
                } else {
                    ultimoToqueReacao[0] = agora;
                    Runnable acao = () -> {
                        if (ultimoToqueReacao[0] == agora) reagirMensagem(doc);
                    };
                    selecaoPendente[0] = acao;
                    reacaoExterna.postDelayed(acao, 435L);
                }
            });
            reacaoExterna.setOnLongClickListener(v -> {removerMinhaReacao(doc);return true;});
        }

        colunaMensagem.addView(
            linhaHorario,
            new LinearLayout.LayoutParams(-2, -2)
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(-2, -2);

        params.gravity = minha ? Gravity.RIGHT : Gravity.LEFT;
        // Reserva espaço para a reação pendurada na borda.
        params.setMargins(dp(4), dp(4), dp(4), dp(13));

        linha.addView(colunaMensagem, params);

        prepararGestoResposta(
                bolha,
                doc,
                textoMensagem,
                minha
        );

        listaMensagens.addView(
                linha,
                new LinearLayout.LayoutParams(-1, -2)
        );
    }

    private String obterSeparadorData(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        Calendar dataMensagem = Calendar.getInstance();
        dataMensagem.setTime(timestamp.toDate());

        Calendar hoje = Calendar.getInstance();

        if (mesmoDia(dataMensagem, hoje)) {
            return "HOJE";
        }

        Calendar ontem = (Calendar) hoje.clone();
        ontem.add(Calendar.DAY_OF_YEAR, -1);

        if (mesmoDia(dataMensagem, ontem)) {
            return "ONTEM";
        }

        Calendar amanha = (Calendar) hoje.clone();
        amanha.add(Calendar.DAY_OF_YEAR, 1);

        if (mesmoDia(dataMensagem, amanha)) {
            return "AMANHÃ";
        }

        return new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(timestamp.toDate());
    }

    private boolean mesmoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private void mostrarAcoesMensagem(
            DocumentSnapshot doc,
            String textoMensagem,
            boolean minha
    ) {
        List<String> opcoes = new ArrayList<>();

        String tipo = doc.getString("tipo");

        if ("texto".equals(tipo) && minha) {
            opcoes.add("✏️ Editar");
        }

        opcoes.add("↩️ Responder");
        opcoes.add("❤️ Reagir");
        opcoes.add("📋 Copiar");
        opcoes.add("📌 Fixar");
        opcoes.add("☑️ Selecionar várias");
        opcoes.add("🗑️ Apagar para mim");

        if (minha) {
            opcoes.add("🗑️ Apagar para todos");
        }

        AlertDialog menuMensagem = new AlertDialog.Builder(this)
                .setTitle("Ações da mensagem")
                .setItems(
                        opcoes.toArray(new String[0]),
                        (dialog, which) -> {
                            String acao = opcoes.get(which);

                            if (acao.contains("Editar")) {
                                editarMensagem(doc, textoMensagem);
                            } else if (acao.contains("Responder")) {
                                selecionarMensagemParaResposta(
                                        doc,
                                        textoMensagem
                                );
                            } else if (acao.contains("Reagir")) {
                                reagirMensagem(doc);
                            } else if (acao.contains("Copiar")) {
                                copiarMensagem(textoMensagem);
                            } else if (acao.contains("Fixar")) {
                                marcarMensagem(doc);
                            } else if (acao.contains("Selecionar várias")) {
                                selecionarMensagensNexo();
                            } else if (acao.contains("Apagar para mim")) {
                                apagarParaMim(doc);
                            } else if (acao.contains("Apagar para todos")) {
                                apagarParaTodos(doc, minha);
                            }
                        }
                )
                .create();
        menuMensagem.setOnShowListener(v -> {
            if (menuMensagem.getWindow() != null) {
                GradientDrawable painel = new GradientDrawable();
                painel.setColor(Color.argb(242, 12, 20, 35));
                painel.setCornerRadius(dp(20));
                painel.setStroke(dp(1), Color.rgb(52, 148, 215));
                menuMensagem.getWindow().setBackgroundDrawable(painel);
                menuMensagem.getWindow().setDimAmount(0.55f);
                menuMensagem.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        });
        menuMensagem.show();
    }


    private void editarMensagem(
            DocumentSnapshot doc,
            String atual
    ) {
        final EditText campo = new EditText(this);
        campo.setText(atual);
        campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(Color.GRAY);
        campo.setSingleLine(false);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Editar mensagem")
                        .setView(campo)
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton(
                                "Salvar",
                                null
                        )
                        .create();

        dialog.setOnShowListener(v -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(btn -> {
                        String novo =
                                campo.getText()
                                        .toString()
                                        .trim();

                        if (novo.isEmpty()) {
                            campo.setError("Digite uma mensagem.");
                            return;
                        }

                        doc.getReference()
                                .update(
                                        "texto",
                                        novo,
                                        "editada",
                                        true,
                                        "editadaEm",
                                        FieldValue.serverTimestamp()
                                )
                                .addOnSuccessListener(x -> {
                                    dialog.dismiss();
                                    Toast.makeText(
                                            this,
                                            "Mensagem editada.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                this,
                                                "Não foi possível editar.",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                );
                    });
        });

        dialog.show();
    }

    private void reagirMensagem(DocumentSnapshot doc) {
        final Dialog dialog=new Dialog(this);
        NexoPremiumEmojiView painel=new NexoPremiumEmojiView(this,emoji->{
            aplicarReacaoNexo(doc,emoji);dialog.dismiss();
        });
        dialog.setContentView(painel);
        dialog.show();
        Window win=dialog.getWindow();
        if(win!=null){
            win.setBackgroundDrawableResource(android.R.color.transparent);
            win.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-dp(18),dp(650)),
                Math.min(getResources().getDisplayMetrics().heightPixels-dp(90),dp(580)));
        }
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
            if (audioRecorder != null) {
                audioRecorder.release();
                audioRecorder = null;
            }

            arquivoAudioTemporario =
                    new File(
                            getCacheDir(),
                            "nexo_audio_"
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
            inicioGravacao = SystemClock.elapsedRealtime();

            botaoMicrofone.setText("🔴");
            botaoMicrofone.setTextColor(
                    Color.rgb(255, 90, 90)
            );

            Toast.makeText(
                    this,
                    "Gravando: ↑ trava  •  ← cancela",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {
            android.util.Log.e(
                    "DaNikeAI_AUDIO",
                    "Erro ao iniciar gravação",
                    e
            );

            gravandoAudio = false;

            Toast.makeText(
                    this,
                    "Não foi possível iniciar o áudio.",
                    Toast.LENGTH_LONG
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
            audioRecorder.reset();
            audioRecorder.release();
        } catch (Exception ignored) {
        }

        audioRecorder = null;
        gravandoAudio = false;

        botaoMicrofone.setText("🎙️");
        botaoMicrofone.setTextColor(
                Color.rgb(70, 190, 255)
        );

        if (!enviar ||
                arquivoAudioTemporario == null ||
                !arquivoAudioTemporario.exists() ||
                arquivoAudioTemporario.length() <= 0) {

            if (arquivoAudioTemporario != null) {
                arquivoAudioTemporario.delete();
            }

            return;
        }

        enviarAudioParaFirebase(
                arquivoAudioTemporario
        );
    }

    private void enviarAudioParaFirebase(File arquivo) {
        if (meuUid == null ||
                meuUid.trim().isEmpty() ||
                outroUid == null ||
                outroUid.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Conversa inválida.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (arquivo.length() > 10L * 1024L * 1024L) {
            Toast.makeText(
                    this,
                    "Áudio muito grande. Limite de 10 MB.",
                    Toast.LENGTH_LONG
            ).show();
            arquivo.delete();
            return;
        }

        String conversaId = gerarIdConversa();

        String nomeArquivo =
                System.currentTimeMillis()
                        + "_"
                        + meuUid
                        + ".m4a";

        StorageReference referencia =
                FirebaseStorage
                        .getInstance()
                        .getReference()
                        .child("chat_audio")
                        .child(conversaId)
                        .child(meuUid)
                        .child(nomeArquivo);

        Toast.makeText(
                this,
                "Enviando áudio...",
                Toast.LENGTH_SHORT
        ).show();

        UploadTask tarefa =
                referencia.putFile(
                        Uri.fromFile(arquivo),
                        new StorageMetadata.Builder()
                                .setContentType("audio/mp4")
                                .build()
                );

        tarefa.addOnSuccessListener(v ->
                referencia.getDownloadUrl()
                        .addOnSuccessListener(url ->
                                salvarMensagemAudio(
                                        conversaId,
                                        url.toString()
                                )
                        )
                        .addOnFailureListener(e -> {
                            arquivo.delete();
                            Toast.makeText(
                                    this,
                                    "Não foi possível obter o áudio.",
                                    Toast.LENGTH_LONG
                            ).show();
                        })
        ).addOnFailureListener(e -> {
            android.util.Log.e(
                    "DaNikeAI_AUDIO",
                    "Falha no upload",
                    e
            );

            Toast.makeText(
                    this,
                    "Não foi possível enviar o áudio.",
                    Toast.LENGTH_LONG
            ).show();

            arquivo.delete();
        });
    }

    private void salvarMensagemAudio(
            String conversaId,
            String audioUrl
    ) {
        List<String> participantes =
                new ArrayList<>();

        if (meuUid.compareTo(outroUid) < 0) {
            participantes.add(meuUid);
            participantes.add(outroUid);
        } else {
            participantes.add(outroUid);
            participantes.add(meuUid);
        }

        Map<String, Object> conversa =
                new HashMap<>();

        conversa.put(
                "participantes",
                participantes
        );

        Map<String, Object> dados =
                new HashMap<>();

        dados.put("remetenteUid", meuUid);
        dados.put("destinatarioUid", outroUid);
        dados.put("tipo", "audio");
        dados.put("audioUrl", audioUrl);
        dados.put("duracaoMs",
                SystemClock.elapsedRealtime() - inicioGravacao);
        dados.put("lida", false);
        dados.put(
                "criadoEm",
                FieldValue.serverTimestamp()
        );

        DocumentReference conversaRef =
                db.collection("chats")
                        .document(conversaId);

        conversaRef
                .set(conversa, SetOptions.merge())
                .addOnSuccessListener(v ->
                        conversaRef
                                .collection("mensagens")
                                .add(dados)
                                .addOnSuccessListener(msg -> {
                                    conversaRef.update(
                                            "naoLidas_" + outroUid,
                                            FieldValue.increment(1)
                                    );

                                    Toast.makeText(
                                            this,
                                            "Áudio enviado.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    if (arquivoAudioTemporario != null) {
                                        arquivoAudioTemporario.delete();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                this,
                                                "Áudio enviado, mas a mensagem falhou.",
                                                Toast.LENGTH_LONG
                                        ).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível preparar o áudio.",
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void reproduzirAudio(String url, TextView botao) {
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            }

            audioPlayer = new MediaPlayer();
            audioPlayer.setDataSource(url);
            audioPlayer.setOnPreparedListener(mp -> {
                mp.start();
                botao.setText("⏸️");
            });

            audioPlayer.setOnCompletionListener(mp -> {
                botao.setText("▶️");
                mp.release();
                audioPlayer = null;
            });

            audioPlayer.setOnErrorListener((mp, what, extra) -> {
                botao.setText("▶️");
                mp.release();
                audioPlayer = null;

                Toast.makeText(
                        this,
                        "Não foi possível reproduzir o áudio.",
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            });

            audioPlayer.prepareAsync();

        } catch (Exception e) {
            android.util.Log.e(
                    "DaNikeAI_AUDIO",
                    "Erro de reprodução",
                    e
            );

            Toast.makeText(
                    this,
                    "Áudio indisponível.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private View criarBolhaAudio(
            DocumentSnapshot doc,
            boolean minha
    ) {
        LinearLayout caixa = new LinearLayout(this);
        caixa.setOrientation(LinearLayout.HORIZONTAL);
        caixa.setGravity(Gravity.CENTER_VERTICAL);
        caixa.setPadding(
                dp(8),
                dp(5),
                dp(8),
                dp(5)
        );

        TextView tocar = texto(
                "▶️",
                20,
                Color.WHITE
        );
        tocar.setGravity(Gravity.CENTER);

        GradientDrawable fundoBotao =
                new GradientDrawable();
        fundoBotao.setShape(
                GradientDrawable.OVAL
        );
        fundoBotao.setColor(
                minha
                        ? Color.rgb(35, 100, 150)
                        : Color.rgb(40, 45, 60)
        );

        tocar.setBackground(fundoBotao);

        String url = doc.getString("audioUrl");

        tocar.setOnClickListener(v -> {
            if (audioPlayer != null
                    && audioPlayer.isPlaying()) {
                audioPlayer.pause();
                tocar.setText("▶️");
            } else if (url != null
                    && !url.trim().isEmpty()) {
                reproduzirAudio(url, tocar);
            }
        });

        caixa.addView(
                tocar,
                new LinearLayout.LayoutParams(
                        dp(44),
                        dp(44)
                )
        );

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(10), 0, dp(8), 0);

        TextView titulo = texto(
                "Mensagem de voz",
                14,
                Color.WHITE
        );
        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView subtitulo = texto(
                "Áudio • toque para reproduzir",
                10,
                Color.rgb(175, 200, 220)
        );

        info.addView(titulo);
        info.addView(subtitulo);

        caixa.addView(
                info,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        return caixa;
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
        if (enviandoMensagem) return;

        String textoMensagem =
                campoMensagem.getText()
                        .toString()
                        .trim();

        if (textoMensagem.isEmpty()) {
            return;
        }

        if (meuUid == null || meuUid.trim().isEmpty()
                || outroUid == null || outroUid.trim().isEmpty()
                || meuUid.equals(outroUid)) {

            Toast.makeText(
                    this,
                    "Conversa inválida.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        enviandoMensagem = true;

        String conversaId = gerarIdConversa();

        List<String> participantes =
                new ArrayList<>();

        if (meuUid.compareTo(outroUid) < 0) {
            participantes.add(meuUid);
            participantes.add(outroUid);
        } else {
            participantes.add(outroUid);
            participantes.add(meuUid);
        }

        Map<String, Object> dadosConversa =
                new HashMap<>();

        dadosConversa.put(
                "participantes",
                participantes
        );

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
                "lida",
                false
        );

        if (!respostaTextoSelecionado.isEmpty()) {
            dados.put(
                    "respostaTexto",
                    respostaTextoSelecionado
            );

            dados.put(
                    "respostaUid",
                    respostaUidSelecionado
            );
        }

        dados.put(
                "criadoEm",
                FieldValue.serverTimestamp()
        );

        DocumentReference conversaRef =
                db.collection("chats")
                        .document(conversaId);

        conversaRef
                .set(
                        dadosConversa,
                        SetOptions.merge()
                )
                .addOnSuccessListener(v -> {

                    atualizarEstadoDigitando(false);

                    conversaRef
                            .collection("mensagens")
                            .add(dados)
                            .addOnSuccessListener(mensagem -> {

                                Map<String, Object> notificacao =
                                        new HashMap<>();

                                notificacao.put(
                                        "uid",
                                        outroUid
                                );

                                notificacao.put(
                                        "tipo",
                                        "mensagem"
                                );

                                notificacao.put(
                                        "titulo",
                                        "Nova mensagem"
                                );

                                notificacao.put(
                                        "texto",
                                        textoMensagem
                                );

                                notificacao.put(
                                        "remetenteUid",
                                        meuUid
                                );

                                notificacao.put(
                                        "enviada",
                                        false
                                );

                                notificacao.put(
                                        "criadoEm",
                                        FieldValue.serverTimestamp()
                                );

                                DocumentReference filaRef =
                                        db.collection(
                                                "fila_notificacoes"
                                        ).document();

                                filaRef.set(notificacao);

                                DaNikeNotificacoes
                                        .enviarNotificacaoImediata(
                                                outroUid,
                                                "mensagem",
                                                "Nova mensagem",
                                                textoMensagem,
                                                meuUid,
                                                filaRef.getId()
                                        );

                                conversaRef.update(
                                        "naoLidas_" + outroUid,
                                        FieldValue.increment(1)
                                );

                                campoMensagem.setText("");

                                cancelarResposta();

                                enviandoMensagem = false;

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
                            .addOnFailureListener(e -> {

                                enviandoMensagem = false;

                                if (campoMensagem.getText()
                                        .toString()
                                        .trim()
                                        .isEmpty()) {

                                    campoMensagem.setText(
                                            textoMensagem
                                    );

                                    campoMensagem.setSelection(
                                            campoMensagem.length()
                                    );
                                }

                                android.util.Log.e(
                                        "DaNikeAI",
                                        "Falha ao gravar mensagem",
                                        e
                                );

                                Toast.makeText(
                                        this,
                                        "Não foi possível enviar a mensagem.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    enviandoMensagem = false;

                    if (campoMensagem.getText()
                            .toString()
                            .trim()
                            .isEmpty()) {

                        campoMensagem.setText(
                                textoMensagem
                        );

                        campoMensagem.setSelection(
                                campoMensagem.length()
                        );
                    }

                    android.util.Log.e(
                            "DaNikeAI",
                            "Falha ao preparar chat",
                            e
                    );

                    Toast.makeText(
                            this,
                            "Não foi possível preparar a conversa.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

private void prepararGestoResposta(
            View bolha, DocumentSnapshot doc, String textoMensagem, boolean minha
    ) {
        final float[] inicioX = {0f}, inicioY = {0f};
        final boolean[] arrastou = {false}, longPress = {false}, respondeu = {false};
        final Runnable abrirMenu = () -> {
            if (!arrastou[0] && !respondeu[0]) {
                longPress[0] = true;
                bolha.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                mostrarAcoesMensagem(doc, textoMensagem, minha);
            }
        };
        bolha.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    inicioX[0] = event.getRawX();
                    inicioY[0] = event.getRawY();
                    arrastou[0] = false;
                    longPress[0] = false;
                    respondeu[0] = false;
                    v.postDelayed(abrirMenu, android.view.ViewConfiguration.getLongPressTimeout());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - inicioX[0];
                    float dy = event.getRawY() - inicioY[0];
                    if (Math.abs(dx) > dp(10) || Math.abs(dy) > dp(10)) {
                        arrastou[0] = true;
                        v.removeCallbacks(abrirMenu);
                    }
                    if (longPress[0] || respondeu[0]) return true;
                    float limite = minha ? -dp(70) : dp(70);
                    if ((minha && dx < limite) || (!minha && dx > limite)) {
                        respondeu[0] = true;
                        selecionarMensagemParaResposta(doc, textoMensagem);
                        v.setTranslationX(0f);
                    } else if (Math.abs(dx) > Math.abs(dy)) {
                        v.setTranslationX(Math.max(-dp(45), Math.min(dp(45), dx)));
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.removeCallbacks(abrirMenu);
                    v.animate().translationX(0f).setDuration(120).start();
                    return true;
            }
            return true;
        });
    }

        private void selecionarMensagemParaResposta(
            DocumentSnapshot doc,
            String textoMensagem
    ) {
        respostaTextoSelecionado = textoMensagem == null
                ? ""
                : textoMensagem.trim();

        respostaUidSelecionado = doc.getString("remetenteUid");

        if (respostaUidSelecionado == null) {
            respostaUidSelecionado = "";
        }

        if (textoResposta != null) {
            textoResposta.setText(
                    "↩ Responder: " + respostaTextoSelecionado
            );
        }

        if (painelResposta != null) {
            painelResposta.setVisibility(View.VISIBLE);
        }
    }

    // NEXO_PREMIUM_ETAPA1: utilitários de interface responsiva.
    private int larguraMaximaBolha() {
        int dpJanela = getResources().getConfiguration().screenWidthDp;
        if (dpJanela <= 0) dpJanela = (int) (getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density);
        return dp(Math.min(520, Math.max(220, (int) (dpJanela * .83f))));
    }

    private void abrirPerfilContato() {
        if(outroUid==null||outroUid.trim().isEmpty())return;
        db.collection("perfis_publicos").document(outroUid).get()
            .addOnSuccessListener(perfil->{
                if(isFinishing()||isDestroyed())return;
                String nome=perfil.getString("nome");
                String handle=perfil.getString("handle");
                String bio=perfil.getString("descricao");
                String foto=perfil.getString("fotoUrl");
                if(nome==null||nome.trim().isEmpty())nome=nomeOutro;
                if(handle==null)handle="";
                if(bio==null||bio.trim().isEmpty())bio="Perfil sem descrição.";
                final Dialog dialog=new Dialog(this);
                LinearLayout card=new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setGravity(Gravity.CENTER_HORIZONTAL);
                card.setPadding(dp(20),dp(18),dp(20),dp(20));
                GradientDrawable fundo=new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.rgb(4,17,41),Color.rgb(9,34,73),Color.rgb(4,16,35)});
                fundo.setCornerRadius(dp(24));fundo.setStroke(dp(1),Color.rgb(42,170,255));
                card.setBackground(fundo);
                TextView cab=texto("NEXO  •  PERFIL",12,Color.rgb(104,203,255));
                cab.setGravity(Gravity.CENTER);cab.setLetterSpacing(.13f);
                card.addView(cab,new LinearLayout.LayoutParams(-1,dp(36)));
                ImageView avatar=new ImageView(this);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GradientDrawable moldura=new GradientDrawable();moldura.setShape(GradientDrawable.OVAL);
                moldura.setColor(Color.rgb(9,37,80));moldura.setStroke(dp(2),Color.rgb(37,166,255));
                avatar.setBackground(moldura);avatar.setClipToOutline(true);
                LinearLayout.LayoutParams lpA=new LinearLayout.LayoutParams(dp(98),dp(98));
                lpA.gravity=Gravity.CENTER;lpA.topMargin=dp(6);lpA.bottomMargin=dp(11);
                card.addView(avatar,lpA);
                if(foto!=null&&!foto.trim().isEmpty())carregarImagemUrl(avatar,foto.trim());
                TextView nomeView=texto(nome,22,Color.WHITE);
                nomeView.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
                nomeView.setGravity(Gravity.CENTER);nomeView.setMaxLines(2);
                card.addView(nomeView,new LinearLayout.LayoutParams(-1,-2));
                TextView usuario=texto(handle.isEmpty()?"":("@"+handle.replaceFirst("^@","")),14,Color.rgb(156,183,216));
                usuario.setGravity(Gravity.CENTER);card.addView(usuario,new LinearLayout.LayoutParams(-1,dp(29)));
                TextView status=texto(outroEstaOnline?"● Online agora":"● Offline",14,
                        outroEstaOnline?Color.rgb(38,226,142):Color.rgb(154,169,187));
                status.setGravity(Gravity.CENTER);card.addView(status,new LinearLayout.LayoutParams(-1,dp(30)));
                TextView descricao=texto(bio,15,Color.rgb(228,237,248));
                descricao.setPadding(dp(14),dp(14),dp(14),dp(14));
                descricao.setBackground(fundoPainelNexo());
                LinearLayout.LayoutParams lpD=new LinearLayout.LayoutParams(-1,-2);
                lpD.topMargin=dp(13);lpD.bottomMargin=dp(17);card.addView(descricao,lpD);
                LinearLayout acoes=new LinearLayout(this);acoes.setGravity(Gravity.CENTER);
                TextView fechar=texto("FECHAR",14,Color.rgb(170,218,255));
                fechar.setGravity(Gravity.CENTER);fechar.setBackground(fundoPainelNexo());
                TextView conversar=texto("💬  CONVERSAR",14,Color.WHITE);
                conversar.setGravity(Gravity.CENTER);
                GradientDrawable azul=new GradientDrawable();azul.setColor(Color.rgb(5,98,225));
                azul.setCornerRadius(dp(16));azul.setStroke(dp(1),Color.rgb(43,183,255));
                conversar.setBackground(azul);
                LinearLayout.LayoutParams lf=new LinearLayout.LayoutParams(0,dp(48),1);
                lf.rightMargin=dp(7);acoes.addView(fechar,lf);
                acoes.addView(conversar,new LinearLayout.LayoutParams(0,dp(48),1));
                card.addView(acoes,new LinearLayout.LayoutParams(-1,-2));
                fechar.setOnClickListener(v->dialog.dismiss());
                conversar.setOnClickListener(v->{dialog.dismiss();campoMensagem.requestFocus();});
                dialog.setContentView(card);
                Window win=dialog.getWindow();
                if(win!=null)win.setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
                win=dialog.getWindow();if(win!=null)win.setLayout(
                    Math.min(getResources().getDisplayMetrics().widthPixels-dp(32),dp(470)),-2);
            }).addOnFailureListener(e->Toast.makeText(this,
                "Não foi possível abrir o perfil.",Toast.LENGTH_SHORT).show());
    }


    // NEXO_PREMIUM_V3: seleção múltipla real e limpeza para o usuário logado.
    private void selecionarMensagensNexo() {
        NexoMensagensLote.selecionar(this, db,
                "chats/" + gerarIdConversa(), meuUid);
    }

    private void limparConversaNexo() {
        NexoMensagensLote.confirmarLimpeza(this, db,
                "chats/" + gerarIdConversa(), meuUid, null);
    }

    private void buscarNaConversa() {
        EditText consulta = new EditText(this);
        consulta.setSingleLine(true);
        consulta.setHint("Buscar nas mensagens carregadas");
        new AlertDialog.Builder(this)
                .setTitle("🔎 Buscar nesta conversa")
                .setView(consulta)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Buscar", (d, i) -> {
                    String alvo = consulta.getText().toString().trim().toLowerCase(Locale.ROOT);
                    if (alvo.isEmpty()) return;
                    for (int j = 0; j < listaMensagens.getChildCount(); j++) {
                        View filho = listaMensagens.getChildAt(j);
                        if (contemTextoVisivel(filho, alvo)) {
                            final View encontrado = filho;
                            scroll.post(() -> scroll.smoothScrollTo(0, encontrado.getTop()));
                            encontrado.animate().alpha(.55f).setDuration(200)
                                    .withEndAction(() -> encontrado.animate().alpha(1f).setDuration(200).start())
                                    .start();
                            return;
                        }
                    }
                    Toast.makeText(this, "Não encontrado entre as mensagens carregadas.",
                            Toast.LENGTH_SHORT).show();
                }).show();
    }

    private boolean contemTextoVisivel(View v, String texto) {
        if (v instanceof TextView && ((TextView) v).getText().toString()
                .toLowerCase(Locale.ROOT).contains(texto)) return true;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++)
                if (contemTextoVisivel(g.getChildAt(i), texto)) return true;
        }
        return false;
    }

    private void alternarTecladoNexo() {
        if (tecladoNexo == null) return;
        tecladoNexoAberto = !tecladoNexoAberto;
        tecladoNexo.setVisibility(tecladoNexoAberto ? View.VISIBLE : View.GONE);
        campoMensagem.setShowSoftInputOnFocus(!tecladoNexoAberto);
        if (tecladoNexoAberto) {
            InputMethodManager ime=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (ime!=null) ime.hideSoftInputFromWindow(campoMensagem.getWindowToken(),0);
            campoMensagem.requestFocus();
            atualizarTeclasNexo();
        } else {
            painelEmojisNexo=false;
            campoMensagem.setShowSoftInputOnFocus(true);
        }
    }

    private void criarTecladoNexo() {
        tecladoNexo = new LinearLayout(this);
        tecladoNexo.setOrientation(LinearLayout.VERTICAL);
        tecladoNexo.setVisibility(View.GONE);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(3, 12, 29), Color.rgb(7, 26, 56), Color.rgb(2, 9, 25)});
        bg.setCornerRadius(dp(18));bg.setStroke(dp(1),COR_NEXO);
        tecladoNexo.setBackground(bg);
        LinearLayout principal=(LinearLayout)raiz.getChildAt(0);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(dp(7),0,dp(7),dp(7));
        principal.addView(tecladoNexo,lp);
        atualizarTeclasNexo();
    }

    private void atualizarTeclasNexo() {
        if (tecladoNexo == null) return;
        tecladoNexo.removeAllViews();
        LinearLayout topoTeclado = new LinearLayout(this);
        topoTeclado.setGravity(Gravity.CENTER_VERTICAL);
        TextView marca=texto("N E X O  ✦  T E C L A D O",14,Color.rgb(70,196,255));
        marca.setGravity(Gravity.CENTER);
        marca.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        marca.setShadowLayer(dp(5),0,0,Color.rgb(0,110,255));
        topoTeclado.addView(marca,new LinearLayout.LayoutParams(0,dp(37),1));
        TextView emojis=texto(painelEmojisNexo?"ABC":"☺",23,Color.WHITE);
        emojis.setGravity(Gravity.CENTER);
        topoTeclado.addView(emojis,new LinearLayout.LayoutParams(dp(56),dp(37)));
        emojis.setOnClickListener(v->{painelEmojisNexo=!painelEmojisNexo;atualizarTeclasNexo();});
        tecladoNexo.addView(topoTeclado,new LinearLayout.LayoutParams(-1,dp(38)));
        if (painelEmojisNexo) {
            NexoPremiumEmojiView painel = new NexoPremiumEmojiView(this, this::inserirNoCampo);
            int altura = getResources().getConfiguration().screenHeightDp;
            int dpAltura = Math.min(395, Math.max(245, (int)(altura*.41f)));
            tecladoNexo.addView(painel,new LinearLayout.LayoutParams(-1,dp(dpAltura)));
            return;
        }
        String[][] linhas = tecladoSimbolos
            ? new String[][]{
                {"1","2","3","4","5","6","7","8","9","0"},
                {"@","#","$","%","&","-","+","(",")"},
                {"*","\"",":",";","!","?","/","⌫"},
                {"ABC", "🌐", "☺", "espaço", ".", "↵"}}
            : new String[][]{
                {"q","w","e","r","t","y","u","i","o","p"},
                {"a","s","d","f","g","h","j","k","l"},
                {"⇧","z","x","c","v","b","n","m","⌫"},
                {"123","🌐","☺","espaço",",",".","↵"}};
        int alturaJanela=getResources().getConfiguration().screenHeightDp;
        int linhaAltura=alturaJanela>=760?56:49;
        for(String[] teclas:linhas){
            LinearLayout linha=new LinearLayout(this);linha.setGravity(Gravity.CENTER);
            tecladoNexo.addView(linha,new LinearLayout.LayoutParams(-1,dp(linhaAltura)));
            for(String tecla:teclas){
                TextView b=texto(tecla,"espaço".equals(tecla)?12:19,Color.WHITE);
                b.setGravity(Gravity.CENTER);
                GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(14,37,70));
                bg.setCornerRadius(dp(9));bg.setStroke(dp(1),Color.rgb(32,101,169));
                b.setBackground(bg);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(linhaAltura-6),
                        "espaço".equals(tecla)?3f:1f);
                lp.setMargins(dp(2),dp(2),dp(2),dp(2));linha.addView(b,lp);
                b.setOnClickListener(v->pressionarTeclaNexo(tecla));
                if(tecla.length()==1 && "aeiounc".contains(tecla)){
                    b.setOnLongClickListener(v->{
                        String variantes;
                        switch(tecla){
                            case "a":variantes="áàâãä";break;
                            case "e":variantes="éêèë";break;
                            case "i":variantes="íìîï";break;
                            case "o":variantes="óôõòö";break;
                            case "u":variantes="úùûü";break;
                            case "c":variantes="ç";break;
                            default:variantes="ñ";
                        }
                        String[] acentos=variantes.split("");
                        new AlertDialog.Builder(this).setTitle("Acentos NEXO")
                            .setItems(acentos,(d,i)->inserirNoCampo(acentos[i])).show();return true;
                    });
                }
            }
        }
    }

    private void pressionarTeclaNexo(String tecla) {
        if ("123".equals(tecla) || "ABC".equals(tecla)) {
            tecladoSimbolos = !tecladoSimbolos;
            atualizarTeclasNexo();
            return;
        }
        if ("🌐".equals(tecla)) {
            alternarTecladoNexo();
            campoMensagem.requestFocus();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(campoMensagem, InputMethodManager.SHOW_IMPLICIT);
            return;
        }
        if ("☺".equals(tecla) || "😊".equals(tecla)) {
            painelEmojisNexo=true;
            atualizarTeclasNexo();
            return;
        }
        if ("⌫".equals(tecla)) {
            int a = Math.max(0, campoMensagem.getSelectionStart());
            int b = Math.max(0, campoMensagem.getSelectionEnd());
            if (a != b) campoMensagem.getText().delete(Math.min(a,b), Math.max(a,b));
            else if (a > 0) {
                CharSequence anterior=campoMensagem.getText();
                int comeco=Character.offsetByCodePoints(anterior,a,-1);
                campoMensagem.getText().delete(comeco,a);
            }
            return;
        }
        if ("⇧".equals(tecla)) {
            tecladoMaiusculo = !tecladoMaiusculo;
            atualizarTeclasNexo();
            return;
        }
        boolean letra = tecla.length() == 1 && Character.isLetter(tecla.charAt(0));
        inserirNoCampo("espaço".equals(tecla) ? " " : "↵".equals(tecla) ? "\n" :
                (letra && tecladoMaiusculo ? tecla.toUpperCase(Locale.ROOT) : tecla));
        if (letra && tecladoMaiusculo) {
            tecladoMaiusculo = false;
            atualizarTeclasNexo();
        }
    }

    private void inserirNoCampo(String conteudo) {
        int i = Math.max(0, campoMensagem.getSelectionStart());
        int f = Math.max(0, campoMensagem.getSelectionEnd());
        campoMensagem.getText().replace(Math.min(i,f), Math.max(i,f), conteudo);
    }

    private void configurarGestoMicrofone() {
        botaoMicrofone.setOnTouchListener((v, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (audioTravado && gravandoAudio) {
                        // A gravação travada é finalizada pelo toque e só será
                        // enviada após ouvir e confirmar na prévia.
                        audioTravado=false;pararAudioParaPrevia();return true;
                    }
                    toqueAudioX = ev.getRawX();
                    toqueAudioY = ev.getRawY();
                    audioGestoCancelado = false;
                    if (android.os.Build.VERSION.SDK_INT >= 23 &&
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
                        audioGestoCancelado = true;
                        return true;
                    }
                    iniciarGravacaoAudio();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!gravandoAudio || audioGestoCancelado || audioTravado) return true;
                    float dx = ev.getRawX() - toqueAudioX;
                    float dy = ev.getRawY() - toqueAudioY;
                    if (dx < -dp(85)) {
                        audioGestoCancelado = true;
                        finalizarGravacaoAudio(false);
                        botaoMicrofone.setText("🎙️");
                        Toast.makeText(this, "Gravação cancelada", Toast.LENGTH_SHORT).show();
                    } else if (dy < -dp(75)) {
                        audioTravado = true;
                        botaoMicrofone.setText("⏹");
                        Toast.makeText(this, "Gravação travada. Toque no microfone para prévia ou arraste para esquerda para cancelar.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (gravandoAudio && !audioGestoCancelado && !audioTravado)
                        pararAudioParaPrevia();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    if (gravandoAudio && !audioTravado) finalizarGravacaoAudio(false);
                    return true;
            }
            return true;
        });
    }

    private void pararAudioParaPrevia() {
        if (!gravandoAudio || audioRecorder == null) return;
        boolean ok = true;
        try { audioRecorder.stop(); } catch (Exception e) { ok = false; }
        try { audioRecorder.reset(); audioRecorder.release(); } catch (Exception ignored) {}
        audioRecorder = null;
        gravandoAudio = false;
        audioTravado = false;
        botaoMicrofone.setText("🎙️");
        botaoMicrofone.setTextColor(COR_NEXO);
        final File arquivo = arquivoAudioTemporario;
        if (!ok || arquivo == null || !arquivo.exists() || arquivo.length() <= 0 ||
                SystemClock.elapsedRealtime() - inicioGravacao < 500) {
            if (arquivo != null) arquivo.delete();
            Toast.makeText(this, "Áudio muito curto; grave novamente.", Toast.LENGTH_SHORT).show();
            return;
        }
        String duracao = String.format(Locale.getDefault(), "%d s",
                Math.max(1, (SystemClock.elapsedRealtime() - inicioGravacao) / 1000));
        AlertDialog previa = new AlertDialog.Builder(this)
                .setTitle("🎙️ Áudio NEXO  •  " + duracao)
                .setMessage("Ouça a gravação antes de enviar.\nCancelar apaga o arquivo.")
                .setNeutralButton("▶ Ouvir", null)
                .setNegativeButton("🗑 Cancelar", (d, i) -> arquivo.delete())
                .setPositiveButton("➤ Enviar", (d, i) -> enviarAudioParaFirebase(arquivo))
                .setOnCancelListener(d -> arquivo.delete())
                .create();
        previa.setOnShowListener(v -> previa.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener(b -> {
                    try {
                        if (audioPlayer != null) audioPlayer.release();
                        audioPlayer = new MediaPlayer();
                        audioPlayer.setDataSource(arquivo.getAbsolutePath());
                        audioPlayer.prepare();
                        audioPlayer.start();
                        audioPlayer.setOnCompletionListener(mp -> {
                            mp.release();
                            audioPlayer = null;
                        });
                    } catch (Exception e) {
                        Toast.makeText(this, "Não foi possível ouvir este áudio.", Toast.LENGTH_SHORT).show();
                    }
                }));
        previa.setOnDismissListener(d -> {
            if (audioPlayer != null) {
                try { audioPlayer.release(); } catch (Exception ignored) {}
                audioPlayer = null;
            }
        });
        previa.show();
    }

    // NEXO_PREMIUM_V2_LAYOUT_SEGURO — visual, reações e anexos.
    private GradientDrawable fundoPainelNexo() {
        GradientDrawable d=new GradientDrawable();
        d.setColor(Color.rgb(10,28,55));d.setCornerRadius(dp(16));
        d.setStroke(dp(1),Color.rgb(29,107,190));return d;
    }
    private void removerMinhaReacao(DocumentSnapshot doc) {
        String atual=doc.getString("reacao_"+meuUid);
        if(atual==null||atual.isEmpty()){
            Toast.makeText(this,"Você ainda não reagiu a esta mensagem.",Toast.LENGTH_SHORT).show();return;
        }
        doc.getReference().update("reacao_"+meuUid,FieldValue.delete())
            .addOnFailureListener(e->Toast.makeText(this,"Não foi possível remover a reação.",Toast.LENGTH_SHORT).show());
    }
    private void aplicarReacaoNexo(DocumentSnapshot doc,String emoji) {
        String atual=doc.getString("reacao_"+meuUid);
        Object valor=emoji.equals(atual)?FieldValue.delete():emoji;
        doc.getReference().update("reacao_"+meuUid,valor)
            .addOnFailureListener(e->Toast.makeText(this,"Não foi possível atualizar a reação.",Toast.LENGTH_SHORT).show());
    }
    private void abrirGaleriaChat() {
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent,8102);
    }
    private void prepararImagemChat(Uri uri) {
        if(uri==null||meuUid.isEmpty()||outroUid.isEmpty())return;
        String mime=getContentResolver().getType(uri);
        if(mime==null||!mime.startsWith("image/")){
            Toast.makeText(this,"Selecione uma imagem válida.",Toast.LENGTH_SHORT).show();return;
        }
        long tamanho=-1;
        try(android.database.Cursor c=getContentResolver().query(uri,
            new String[]{android.provider.OpenableColumns.SIZE},null,null,null)){
            if(c!=null&&c.moveToFirst()){
                int idx=c.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if(idx>=0&&!c.isNull(idx))tamanho=c.getLong(idx);
            }
        }catch(Exception ignored){}
        if(tamanho>8L*1024*1024){
            Toast.makeText(this,"Imagem muito grande (máximo 8 MB).",Toast.LENGTH_LONG).show();return;
        }
        // Prévia local com amostragem antes de transmitir a imagem.
        LinearLayout visual=new LinearLayout(this);visual.setOrientation(LinearLayout.VERTICAL);
        visual.setPadding(dp(17),dp(7),dp(17),dp(7));
        ImageView miniatura=new ImageView(this);
        miniatura.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try {
            android.graphics.BitmapFactory.Options opts=new android.graphics.BitmapFactory.Options();
            opts.inJustDecodeBounds=true;
            try(InputStream in=getContentResolver().openInputStream(uri)){
                android.graphics.BitmapFactory.decodeStream(in,null,opts);
            }
            int maior=Math.max(opts.outWidth,opts.outHeight);
            opts.inSampleSize=1;
            while(maior/opts.inSampleSize>900)opts.inSampleSize*=2;
            opts.inJustDecodeBounds=false;
            try(InputStream in=getContentResolver().openInputStream(uri)){
                android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeStream(in,null,opts);
                if(b!=null)miniatura.setImageBitmap(b);
            }
        } catch(Exception | OutOfMemoryError ignored) {}
        visual.addView(miniatura,new LinearLayout.LayoutParams(-1,dp(190)));
        EditText legenda=new EditText(this);
        legenda.setHint("Legenda (opcional)");legenda.setTextColor(Color.WHITE);
        legenda.setHintTextColor(Color.rgb(135,155,185));
        visual.addView(legenda,new LinearLayout.LayoutParams(-1,dp(55)));
        new AlertDialog.Builder(this).setTitle("🖼️ Prévia da imagem NEXO")
            .setView(visual).setNegativeButton("Cancelar",null)
            .setPositiveButton("Enviar",(d,i)->enviarImagemChat(uri,mime,
                legenda.getText().toString().trim())).show();
    }
    private void enviarImagemChat(Uri uri,String mime,String legenda) {
        String id=gerarIdConversa();
        String extensao="image/png".equals(mime)?"png":"image/webp".equals(mime)?"webp":"jpg";
        String path="chat_images/"+id+"/"+meuUid+"/"+
                java.util.UUID.randomUUID().toString()+"."+extensao;
        StorageReference ref=FirebaseStorage.getInstance().getReference().child(path);
        Toast.makeText(this,"Enviando imagem...",Toast.LENGTH_SHORT).show();
        ref.putFile(uri,new StorageMetadata.Builder().setContentType(mime).build())
            .addOnSuccessListener(task->{
                Map<String,Object> dados=new HashMap<>();
                dados.put("remetenteUid",meuUid);
                dados.put("destinatarioUid",outroUid);
                dados.put("texto",legenda);
                dados.put("tipo","imagem");
                dados.put("imagemPath",path);
                dados.put("lida",false);
                dados.put("criadoEm",FieldValue.serverTimestamp());
                if(!respostaTextoSelecionado.isEmpty()){
                    dados.put("respostaTexto",respostaTextoSelecionado);
                    dados.put("respostaUid",respostaUidSelecionado);
                }
                db.collection("chats").document(id).collection("mensagens").add(dados)
                    .addOnSuccessListener(doc->{
                        cancelarResposta();
                        db.collection("chats").document(id).update(
                            "naoLidas_"+outroUid,FieldValue.increment(1));
                        // A fila só dispara após gravação, evitando corrida.
                        Map<String,Object> notificacao=new HashMap<>();
                        notificacao.put("uid",outroUid);notificacao.put("tipo","mensagem");
                        notificacao.put("titulo","Nova imagem");
                        notificacao.put("texto","🖼️ Enviou uma imagem");
                        notificacao.put("remetenteUid",meuUid);
                        notificacao.put("enviada",false);
                        notificacao.put("criadoEm",FieldValue.serverTimestamp());
                        DocumentReference fila=db.collection("fila_notificacoes").document();
                        fila.set(notificacao).addOnSuccessListener(v->
                            DaNikeNotificacoes.enviarNotificacaoImediata(
                                outroUid,"mensagem","Nova imagem","🖼️ Enviou uma imagem",
                                meuUid,fila.getId()));
                        Toast.makeText(this,"Imagem enviada!",Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e->{
                        // Não deixar arquivo órfão quando Firestore recusou.
                        ref.delete();
                        Toast.makeText(this,"Firestore não aceitou imagem. Revise as regras.",Toast.LENGTH_LONG).show();
                    });
            }).addOnFailureListener(e->Toast.makeText(this,
                "Falha ao enviar imagem: verifique Firebase Storage.",Toast.LENGTH_LONG).show());
    }
    private ImageView imagemMensagemChat(String caminho) {
        ImageView foto=new ImageView(this);foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setBackground(fundoPainelNexo());
        foto.setContentDescription("Imagem da conversa; toque para ampliar");
        if(caminho==null||!caminho.startsWith("chat_images/"+gerarIdConversa()+"/"))return foto;
        StorageReference ref=FirebaseStorage.getInstance().getReference().child(caminho);
        ref.getBytes(8L*1024L*1024L).addOnSuccessListener(bytes->{
            try{
                android.graphics.BitmapFactory.Options opts=new android.graphics.BitmapFactory.Options();
                opts.inJustDecodeBounds=true;
                android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                int max=Math.max(opts.outWidth,opts.outHeight);
                opts.inSampleSize=1;while(max/opts.inSampleSize>1280)opts.inSampleSize*=2;
                opts.inJustDecodeBounds=false;
                android.graphics.Bitmap bmp=android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,opts);
                if(bmp!=null){
                    foto.setImageBitmap(bmp);
                    foto.setOnClickListener(v->{
                        Dialog dialog=new Dialog(this);
                        ImageView tela=new ImageView(this);tela.setImageBitmap(bmp);
                        tela.setBackgroundColor(Color.rgb(3,9,20));
                        tela.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        dialog.setContentView(tela);dialog.show();
                        Window win=dialog.getWindow();if(win!=null)win.setLayout(-1,-1);
                        tela.setOnClickListener(x->dialog.dismiss());
                    });
                }
            }catch(OutOfMemoryError|RuntimeException e){
                Toast.makeText(this,"Imagem grande demais para exibir.",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e->foto.setContentDescription("Imagem indisponível"));
        return foto;
    }
    private void aplicarFormatacaoMensagem(String tipo) {
        if (campoMensagem == null) {
            return;
        }

        int inicio = campoMensagem.getSelectionStart();
        int fim = campoMensagem.getSelectionEnd();

        if (inicio < 0 || fim < 0 || inicio == fim) {
            Toast.makeText(
                    this,
                    "Selecione o trecho que deseja formatar.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (inicio > fim) {
            int temp = inicio;
            inicio = fim;
            fim = temp;
        }

        Editable texto = campoMensagem.getText();
        String selecionado = texto.subSequence(inicio, fim).toString();

        if (selecionado.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Selecione um trecho de texto.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if ("link".equals(tipo)) {
            final int inicioLink = inicio;
            final int fimLink = fim;
            final String selecionadoLink = selecionado;

            EditText campoLink = new EditText(this);
            campoLink.setHint("https://exemplo.com");
            campoLink.setSingleLine(true);
            campoLink.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT
                            | android.text.InputType.TYPE_TEXT_VARIATION_URI
            );
            campoLink.setPadding(
                    dp(14),
                    dp(10),
                    dp(14),
                    dp(10)
            );

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Adicionar link")
                    .setView(campoLink)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Aplicar", (dialog, which) -> {
                        String url = campoLink.getText()
                                .toString()
                                .trim();

                        if (url.isEmpty()) {
                            return;
                        }

                        if (!url.startsWith("http://")
                                && !url.startsWith("https://")) {
                            url = "https://" + url;
                        }

                        String formatado =
                                "[" + selecionadoLink + "](" + url + ")";

                        texto.replace(inicioLink, fimLink, formatado);

                        campoMensagem.setSelection(
                                Math.min(
                                        inicioLink + formatado.length(),
                                        campoMensagem.length()
                                )
                        );
                    })
                    .show();

            return;
        }

        String antes;
        String depois;

        switch (tipo) {
            case "negrito":
                antes = "**";
                depois = "**";
                break;

            case "italico":
                antes = "*";
                depois = "*";
                break;

            case "tachado":
                antes = "~~";
                depois = "~~";
                break;

            default:
                return;
        }

        String formatado = antes + selecionado + depois;

        texto.replace(inicio, fim, formatado);

        campoMensagem.setSelection(
                Math.min(
                        inicio + formatado.length(),
                        campoMensagem.length()
                )
        );
    }
    private void cancelarResposta() {
        respostaTextoSelecionado = "";
        respostaUidSelecionado = "";

        if (painelResposta != null) {
            painelResposta.setVisibility(View.GONE);
        }
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

            Toast.makeText(this,
                    "Permissão concedida. Segure o microfone novamente para gravar.",
                    Toast.LENGTH_SHORT).show();

        } else if (requestCode == REQUEST_AUDIO_PERMISSION) {

            Toast.makeText(
                    this,
                    "Permissão do microfone é necessária para gravar áudio.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (audioRecorder != null) {
                audioRecorder.release();
                audioRecorder = null;
            }

            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            }
        } catch (Exception ignored) {
        }



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
