package com.danike.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.widget.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IAActivity extends Activity {

    private static final int PEDIR_MICROFONE = 7001;

    TextView indicador;
    LinearLayout tela;
    DaNikeAIFaceView rostoIA;
    TextView statusIA;
    TextView resposta;
    EditText entrada;
    LinearLayout chatLista;
    ScrollView chatScroll;
    TextView tituloAssunto;
    Button botaoVozResposta;

    TextToSpeech voz;
    SpeechRecognizer reconhecedor;
    Intent intentVoz;

    boolean ouvindo = false;
    boolean falando = false;
    boolean internetDisponivel = false;
    boolean ponteDisponivel = false;
    boolean falaAutomatica = true;
    boolean modoEscutaContinua = false;

    ConnectivityManager.NetworkCallback conexaoCallback;

    ChatDB chatDB;
    long sessaoAtual = -1;
    View brilhoFala;
    TextView nomeSessao;

    int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + .5f);
    }

    GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    TextView texto(String s, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    TextView texto(String s, float tamanho, int cor, boolean negrito) {
        TextView t = texto(s, tamanho, cor);
        if (negrito) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    Button botao(String s, int cor) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(fundo(cor, 22));
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        return b;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        chatDB = new ChatDB(this);

        voz = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("pt", "BR"));
                voz.setSpeechRate(1.0f);
                voz.setPitch(1.0f);
            }
        });

        if (voz != null) {
            voz.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {
                    runOnUiThread(() -> {
                        falando = true;
                        if (rostoIA != null) rostoIA.setFalando(true);
                    });
                }

                @Override public void onDone(String utteranceId) {
                    runOnUiThread(() -> {
                        falando = false;
                        if (rostoIA != null) rostoIA.setFalando(false);
                        if (brilhoFala != null) brilhoFala.setVisibility(View.GONE);
                        if (modoEscutaContinua) iniciarEscutaContinua();
                    });
                }

                @Override public void onError(String utteranceId) {
                    runOnUiThread(() -> {
                        falando = false;
                        if (rostoIA != null) rostoIA.setFalando(false);
                        if (brilhoFala != null) brilhoFala.setVisibility(View.GONE);
                        if (modoEscutaContinua) iniciarEscutaContinua();
                    });
                }
            });
        }

        montarReconhecimentoVoz();
        montarTela();
        garantirSessao();
        carregarSessaoAtual();
        monitorarConexao();

        falaAutomatica = getSharedPreferences("danike_config", 0)
                .getBoolean("fala_automatica", true);
        atualizarBotaoVozResposta();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarConexao();
    }

    private boolean temInternetValidada() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network rede = cm.getActiveNetwork();
        if (rede == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(rede);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private void verificarPonte() {
        new Thread(() -> {
            boolean ok = false;
            HttpURLConnection conexao = null;
            try {
                URL url = new URL("http://127.0.0.1:8766/");
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setConnectTimeout(1200);
                conexao.setReadTimeout(1200);
                conexao.setUseCaches(false);
                conexao.setRequestMethod("GET");
                ok = conexao.getResponseCode() > 0;
            } catch (Exception ignored) {
                ok = false;
            } finally {
                if (conexao != null) conexao.disconnect();
            }
            ponteDisponivel = ok;
            runOnUiThread(this::atualizarStatusOnline);
        }).start();
    }

    private void atualizarStatusOnline() {
        if (statusIA == null) return;

        if (ouvindo) {
            statusIA.setText("●  OUVINDO");
            statusIA.setTextColor(Color.rgb(0, 225, 255));
        } else if (falando) {
            statusIA.setText("●  FALANDO");
            statusIA.setTextColor(Color.rgb(255, 200, 45));
        } else if (internetDisponivel && ponteDisponivel) {
            statusIA.setText("●  ONLINE");
            statusIA.setTextColor(Color.rgb(55, 255, 145));
        } else if (!internetDisponivel) {
            statusIA.setText("●  SEM INTERNET");
            statusIA.setTextColor(Color.rgb(255, 80, 90));
        } else {
            statusIA.setText("●  IA OFFLINE");
            statusIA.setTextColor(Color.rgb(255, 170, 50));
        }
    }

    private void atualizarConexao() {
        internetDisponivel = temInternetValidada();
        atualizarStatusOnline();
        verificarPonte();
    }

    private void monitorarConexao() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            atualizarConexao();
            return;
        }

        conexaoCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> atualizarConexao());
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> atualizarConexao());
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
                runOnUiThread(() -> atualizarConexao());
            }
        };

        try {
            cm.registerDefaultNetworkCallback(conexaoCallback);
        } catch (Exception ignored) {
        }

        atualizarConexao();
    }

    void montarReconhecimentoVoz() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return;

        reconhecedor = SpeechRecognizer.createSpeechRecognizer(this);

        reconhecedor.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                ouvindo = true;
                atualizarStatusOnline();
                if (indicador != null) indicador.setText("●  OUVINDO...");
            }

            @Override public void onBeginningOfSpeech() {
                ouvindo = true;
            }

            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}

            @Override public void onEndOfSpeech() {
                ouvindo = false;
            }

            @Override public void onError(int error) {
                ouvindo = false;
                if (indicador != null) indicador.setText("●  PRONTO PARA OUVIR");
                atualizarConexao();

                // Alguns aparelhos encerram a sessão de voz sozinhos.
                // Se o modo contínuo estiver ligado, recupera automaticamente.
                if (modoEscutaContinua && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    iniciarEscutaContinuaComAtraso(700);
                }
            }

            @Override public void onResults(Bundle resultados) {
                ouvindo = false;

                ArrayList<String> textos =
                        resultados.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION);

                if (textos == null || textos.isEmpty()) {
                    atualizarConexao();
                    if (modoEscutaContinua) iniciarEscutaContinuaComAtraso(300);
                    return;
                }

                String textoReconhecido = textos.get(0).trim();

                if (textoReconhecido.equalsIgnoreCase("stop")
                        || textoReconhecido.equalsIgnoreCase("parar")
                        || textoReconhecido.equalsIgnoreCase("pare")
                        || textoReconhecido.equalsIgnoreCase("parar de falar")) {
                    modoEscutaContinua = false;
                    pararFala();
                    return;
                }

                entrada.setText(textoReconhecido);
                responder();
            }

            @Override public void onPartialResults(Bundle resultados) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        intentVoz = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentVoz.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentVoz.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
        intentVoz.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
    }

    void montarTela() {
        FrameLayout raiz = new FrameLayout(this);

        NeonBackground fundoNeon = new NeonBackground(this);
        raiz.addView(fundoNeon,
                new FrameLayout.LayoutParams(-1, -1));

        tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setPadding(dp(12), dp(8), dp(12), dp(8));
        raiz.addView(tela,
                new FrameLayout.LayoutParams(-1, -1));

        // TOPO PROFISSIONAL
        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView marcaIcone = texto("◈", 28, Color.rgb(0, 225, 255), true);
        marcaIcone.setGravity(Gravity.CENTER);

        topo.addView(marcaIcone,
                new LinearLayout.LayoutParams(dp(42), dp(58)));

        LinearLayout tituloBox = new LinearLayout(this);
        tituloBox.setOrientation(LinearLayout.VERTICAL);
        tituloBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = texto("DaNike AI", 25, Color.WHITE, true);
        TextView subtitulo = texto(
                "SEU ASSISTENTE INTELIGENTE",
                9,
                Color.rgb(0, 225, 255),
                true);

        tituloBox.addView(titulo,
                new LinearLayout.LayoutParams(-1, dp(34)));
        tituloBox.addView(subtitulo,
                new LinearLayout.LayoutParams(-1, dp(19)));

        topo.addView(tituloBox,
                new LinearLayout.LayoutParams(0, dp(58), 1));

        // INSTAGRAM + SELO VISUAL
        LinearLayout instagram = criarInstagramBadge();
        topo.addView(instagram,
                new LinearLayout.LayoutParams(dp(166), dp(48)));

        Button configuracoes = botao("⚙", Color.TRANSPARENT);
        configuracoes.setTextSize(23);
        GradientDrawable cfg = fundo(Color.argb(45, 0, 160, 255), 25);
        cfg.setStroke(dp(1), Color.rgb(0, 225, 255));
        configuracoes.setBackground(cfg);
        topo.addView(configuracoes,
                new LinearLayout.LayoutParams(dp(50), dp(50)));

        tela.addView(topo,
                new LinearLayout.LayoutParams(-1, dp(62)));

        configuracoes.setOnClickListener(v ->
                startActivity(new Intent(IAActivity.this, ConfiguracoesActivity.class)));

        // STATUS
        LinearLayout statusLinha = new LinearLayout(this);
        statusLinha.setGravity(Gravity.CENTER_VERTICAL);

        statusIA = texto("●  ONLINE", 11, Color.rgb(55, 255, 145), true);
        statusIA.setGravity(Gravity.CENTER_VERTICAL);

        statusLinha.addView(statusIA,
                new LinearLayout.LayoutParams(0, dp(30), 1));

        TextView tecnologia = texto("DaNikeAI  •  Gemini", 9,
                Color.rgb(100, 160, 200), false);
        tecnologia.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        statusLinha.addView(tecnologia,
                new LinearLayout.LayoutParams(0, dp(30), 1));

        tela.addView(statusLinha,
                new LinearLayout.LayoutParams(-1, dp(30)));

        // CABEÇALHO DA IA + ROSTO ORIGINAL
        FrameLayout palco = new FrameLayout(this);

        brilhoFala = new SpeakingGlow(this);
        brilhoFala.setVisibility(View.GONE);
        palco.addView(brilhoFala,
                new FrameLayout.LayoutParams(dp(190), dp(190),
                        Gravity.RIGHT | Gravity.TOP));

        NeonFaceStage energia = new NeonFaceStage(this);
        palco.addView(energia,
                new FrameLayout.LayoutParams(dp(205), dp(205),
                        Gravity.RIGHT | Gravity.TOP));

        // NÃO ALTERAR ESTA CLASSE: rosto original da DaNikeAI
        rostoIA = new DaNikeAIFaceView(this);

        int tamanho = Math.min(
                dp(175),
                getResources().getDisplayMetrics().widthPixels - dp(55));

        FrameLayout.LayoutParams rostoParams =
                new FrameLayout.LayoutParams(
                        tamanho,
                        tamanho,
                        Gravity.RIGHT | Gravity.TOP);
        rostoParams.setMargins(0, dp(2), dp(5), 0);
        palco.addView(rostoIA, rostoParams);

        LinearLayout saudacaoBox = new LinearLayout(this);
        saudacaoBox.setOrientation(LinearLayout.VERTICAL);
        saudacaoBox.setGravity(Gravity.CENTER_VERTICAL);

        TextView saudacao = texto("Olá, Anderson.", 21, Color.WHITE, true);
        TextView ajuda = texto(
                "Estou pronta. O que vamos criar hoje?",
                12,
                Color.rgb(165, 205, 235));

        saudacaoBox.addView(saudacao,
                new LinearLayout.LayoutParams(dp(205), dp(34)));
        saudacaoBox.addView(ajuda,
                new LinearLayout.LayoutParams(dp(230), dp(27)));

        palco.addView(saudacaoBox,
                new FrameLayout.LayoutParams(
                        dp(245), dp(72),
                        Gravity.LEFT | Gravity.CENTER_VERTICAL));

        tela.addView(palco,
                new LinearLayout.LayoutParams(-1, dp(148)));

        // CHAT
        LinearLayout painelChat = new LinearLayout(this);
        painelChat.setOrientation(LinearLayout.VERTICAL);
        painelChat.setPadding(dp(8), dp(5), dp(8), dp(5));

        GradientDrawable chatFundo =
                fundo(Color.argb(135, 2, 9, 25), 20);
        chatFundo.setStroke(dp(1), Color.argb(180, 0, 190, 255));
        painelChat.setBackground(chatFundo);

        LinearLayout cabecalhoChat = new LinearLayout(this);
        cabecalhoChat.setGravity(Gravity.CENTER_VERTICAL);

        tituloAssunto = texto("💬  Conversa atual", 11,
                Color.rgb(255, 205, 60), true);
        tituloAssunto.setSingleLine(true);

        nomeSessao = texto("", 9,
                Color.rgb(110, 165, 205));
        nomeSessao.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

        cabecalhoChat.addView(tituloAssunto,
                new LinearLayout.LayoutParams(0, dp(28), 1));
        cabecalhoChat.addView(nomeSessao,
                new LinearLayout.LayoutParams(dp(125), dp(28)));

        botaoVozResposta = botao("🔊", Color.TRANSPARENT);
        botaoVozResposta.setTextSize(13);
        botaoVozResposta.setPadding(0, 0, 0, 0);
        botaoVozResposta.setBackground(
                fundo(Color.argb(45, 0, 210, 255), 16));

        cabecalhoChat.addView(botaoVozResposta,
                new LinearLayout.LayoutParams(dp(38), dp(28)));

        painelChat.addView(cabecalhoChat,
                new LinearLayout.LayoutParams(-1, dp(30)));

        chatScroll = new ScrollView(this);
        chatScroll.setFillViewport(true);
        chatScroll.setVerticalScrollBarEnabled(true);
        chatScroll.setScrollbarFadingEnabled(false);
        chatScroll.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        chatLista = new LinearLayout(this);
        chatLista.setOrientation(LinearLayout.VERTICAL);
        chatLista.setPadding(dp(2), dp(2), dp(2), dp(2));

        resposta = texto("", 1, Color.TRANSPARENT);
        resposta.setVisibility(View.GONE);

        chatScroll.addView(chatLista,
                new ScrollView.LayoutParams(-1, -2));
        painelChat.addView(chatScroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        tela.addView(painelChat,
                new LinearLayout.LayoutParams(-1, 0, 1));

        botaoVozResposta.setOnClickListener(v -> {
            falaAutomatica = !falaAutomatica;
            getSharedPreferences("danike_config", 0)
                    .edit()
                    .putBoolean("fala_automatica", falaAutomatica)
                    .apply();
            if (!falaAutomatica) pararFala();
            atualizarBotaoVozResposta();
        });

        // ENTRADA
        LinearLayout linhaMensagem = new LinearLayout(this);
        linhaMensagem.setGravity(Gravity.CENTER_VERTICAL);
        linhaMensagem.setPadding(0, dp(7), 0, dp(5));

        entrada = new EditText(this);
        entrada.setHint("Digite sua mensagem para a DaNikeAI...");
        entrada.setHintTextColor(Color.rgb(80, 125, 165));
        entrada.setTextColor(Color.WHITE);
        entrada.setTextSize(14);
        entrada.setSingleLine(true);
        entrada.setImeOptions(EditorInfo.IME_ACTION_SEND);
        entrada.setPadding(dp(15), 0, dp(10), 0);

        GradientDrawable campo =
                fundo(Color.argb(180, 2, 14, 32), 27);
        campo.setStroke(dp(1), Color.rgb(0, 185, 255));
        entrada.setBackground(campo);

        linhaMensagem.addView(entrada,
                new LinearLayout.LayoutParams(0, dp(54), 1));

        Button enviar = botao("➤", Color.rgb(3, 55, 105));
        enviar.setTextSize(22);

        GradientDrawable enviarFundo =
                fundo(Color.rgb(3, 55, 105), 28);
        enviarFundo.setStroke(dp(1), Color.rgb(0, 225, 255));
        enviar.setBackground(enviarFundo);

        LinearLayout.LayoutParams ep =
                new LinearLayout.LayoutParams(dp(56), dp(54));
        ep.setMargins(dp(7), 0, 0, 0);
        linhaMensagem.addView(enviar, ep);

        tela.addView(linhaMensagem,
                new LinearLayout.LayoutParams(-1, dp(60)));

        // NAVEGAÇÃO NOVA
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(2), dp(2), dp(2), dp(2));

        Button navChat = navButton("💬", "Chat");
        Button navConversas = navButton("◉", "Conversas");
        Button navMemoria = navButton("🧠", "Memória");
        Button navFalar = navButton("🎤", "Falar");
        Button navConfig = navButton("⚙", "Opções");

        nav.addView(navChat, new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navConversas, new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navMemoria, new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navFalar, new LinearLayout.LayoutParams(0, dp(58), 1));
        nav.addView(navConfig, new LinearLayout.LayoutParams(0, dp(58), 1));

        tela.addView(nav,
                new LinearLayout.LayoutParams(-1, dp(62)));

        setBotaoAtivo(navChat, true);

        navChat.setOnClickListener(v -> setBotaoAtivo(navChat, true));
        navConversas.setOnClickListener(v -> {
            setBotaoAtivo(navConversas, true);
            abrirConversas();
        });
        navMemoria.setOnClickListener(v -> {
            setBotaoAtivo(navMemoria, true);
            abrirMemorias();
        });
        navFalar.setOnClickListener(v -> {
            setBotaoAtivo(navFalar, true);
            alternarMicrofone();
        });
        navConfig.setOnClickListener(v -> {
            setBotaoAtivo(navConfig, true);
            startActivity(new Intent(IAActivity.this, ConfiguracoesActivity.class));
        });

        enviar.setOnClickListener(v -> responder());

        entrada.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEND
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                responder();
                return true;
            }
            return false;
        });

        setContentView(raiz);
    }

    private LinearLayout criarInstagramBadge() {
        LinearLayout box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(5), 0, dp(5), 0);

        InstagramIcon icon = new InstagramIcon(this);
        box.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView nome = texto("anderson_lopes._ofc", 8.5f,
                Color.WHITE, true);
        nome.setSingleLine(true);
        nome.setGravity(Gravity.CENTER_VERTICAL);
        box.addView(nome, new LinearLayout.LayoutParams(0, dp(42), 1));

        VerifiedBadge selo = new VerifiedBadge(this);
        box.addView(selo, new LinearLayout.LayoutParams(dp(24), dp(24)));

        GradientDrawable bg =
                fundo(Color.argb(80, 35, 0, 75), 20);
        bg.setStroke(dp(1), Color.rgb(100, 75, 255));
        box.setBackground(bg);

        return box;
    }

    private Button navButton(String icone, String label) {
        Button b = botao(icone + "\n" + label,
                Color.argb(35, 0, 25, 55));
        b.setTextSize(10);
        b.setGravity(Gravity.CENTER);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    void setBotaoAtivo(Button b, boolean ativo) {
        if (b == null) return;

        GradientDrawable g = fundo(
                ativo
                        ? Color.argb(150, 70, 45, 5)
                        : Color.argb(35, 5, 18, 35),
                17);

        g.setStroke(dp(1),
                ativo
                        ? Color.rgb(255, 195, 40)
                        : Color.rgb(0, 170, 235));

        b.setBackground(g);
        b.setTextColor(
                ativo
                        ? Color.rgb(255, 220, 100)
                        : Color.WHITE);
        b.setSelected(ativo);

        if (ativo) {
            b.animate()
                    .alpha(.72f)
                    .setDuration(180)
                    .withEndAction(() ->
                            b.animate()
                                    .alpha(1f)
                                    .setDuration(180)
                                    .start())
                    .start();
        }
    }

    void atualizarBotaoVozResposta() {
        if (botaoVozResposta == null) return;
        botaoVozResposta.setText(falaAutomatica ? "🔊" : "🔇");
        botaoVozResposta.setContentDescription(
                falaAutomatica
                        ? "Voz automática ligada"
                        : "Voz automática desligada");
    }

    private TextView criarBolha(String textoMensagem, boolean usuario) {
        TextView b = texto(textoMensagem, 14, Color.WHITE, false);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        b.setGravity(Gravity.START);
        b.setLineSpacing(0, 1.08f);

        GradientDrawable bg = fundo(
                usuario
                        ? Color.argb(120, 85, 10, 170)
                        : Color.argb(100, 0, 55, 115),
                20);

        bg.setStroke(dp(1),
                usuario
                        ? Color.rgb(195, 65, 255)
                        : Color.rgb(0, 205, 255));

        b.setBackground(bg);

        int largura = getResources()
                .getDisplayMetrics().widthPixels;

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        (int) (largura * (usuario ? .78f : .84f)),
                        -2);

        lp.gravity = usuario ? Gravity.RIGHT : Gravity.LEFT;
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        b.setLayoutParams(lp);

        return b;
    }

    private void adicionarBolhaUsuario(String pergunta) {
        if (chatLista == null) return;
        chatLista.addView(
                criarBolha("VOCÊ\n" + pergunta, true));
        rolarChatFinal();
    }

    private TextView adicionarBolhaIA(String respostaTexto) {
        if (chatLista == null) return null;
        TextView b = criarBolha(
                "DANIKEAI\n" + respostaTexto, false);
        chatLista.addView(b);
        rolarChatFinal();
        return b;
    }

    private TextView adicionarDigitando() {
        if (chatLista == null) return null;
        TextView b = criarBolha(
                "DANIKEAI\n•••  digitando...", false);
        chatLista.addView(b);
        rolarChatFinal();
        return b;
    }

    private void rolarChatFinal() {
        if (chatScroll != null) {
            chatScroll.post(() ->
                    chatScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    void responder() {
        String pergunta = entrada.getText().toString().trim();

        if (pergunta.isEmpty()) {
            Toast.makeText(this,
                    "Digite uma pergunta.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (tentarSalvarMemoria(pergunta)) {
            adicionarBolhaUsuario(pergunta);
            adicionarBolhaIA(
                    "🧠 Memória salva. Vou lembrar disso.");
            entrada.setText("");
            salvarMensagemSessao(1, pergunta);
            salvarMensagemSessao(
                    0,
                    "🧠 Memória salva. Vou lembrar disso.");
            return;
        }

        if (ouvindo && reconhecedor != null) {
            try {
                reconhecedor.stopListening();
            } catch (Exception ignored) {
            }
            ouvindo = false;
        }

        pararFala();

        if (!internetDisponivel) {
            adicionarBolhaUsuario(pergunta);
            adicionarBolhaIA(
                    "🔴 Estou sem internet no momento.");
            salvarMensagemSessao(1, pergunta);
            salvarMensagemSessao(
                    0,
                    "🔴 Estou sem internet no momento.");
            entrada.setText("");
            return;
        }

        statusIA.setText("●  PENSANDO...");
        statusIA.setTextColor(Color.rgb(255, 195, 0));

        adicionarBolhaUsuario(pergunta);
        salvarMensagemSessao(1, pergunta);

        TextView digitando = adicionarDigitando();
        entrada.setText("");

        GeminiAPI.perguntar(
                pergunta,
                new GeminiAPI.Callback() {
                    @Override
                    public void sucesso(String textoResposta) {
                        runOnUiThread(() -> {
                            if (digitando != null)
                                chatLista.removeView(digitando);

                            resposta.setText(textoResposta);
                            adicionarBolhaIA(textoResposta);
                            salvarMensagemSessao(0, textoResposta);

                            atualizarConexao();

                            if (falaAutomatica)
                                falarTexto(textoResposta);
                        });
                    }

                    @Override
                    public void erro(String mensagem) {
                        runOnUiThread(() -> {
                            if (digitando != null)
                                chatLista.removeView(digitando);

                            String erro =
                                    "Não consegui falar com o Gemini.\n\n"
                                            + mensagem;

                            resposta.setText(erro);
                            adicionarBolhaIA(erro);

                            statusIA.setText("●  ERRO NA IA");
                            statusIA.setTextColor(
                                    Color.rgb(255, 70, 70));

                            falando = false;
                            if (rostoIA != null)
                                rostoIA.setFalando(false);

                            atualizarConexao();
                        });
                    }
                });
    }

    private boolean tentarSalvarMemoria(String pergunta) {
        String textoPergunta = pergunta.trim();

        String[] gatilhos = {
                "lembre que ",
                "lembre-se que ",
                "lembre disso: ",
                "guarde que ",
                "guarde isso: ",
                "salve na memória que ",
                "salva na memória que ",
                "memorize que ",
                "memoriza que ",
                "anote que ",
                "anota que "
        };

        String memoria = null;

        for (String gatilho : gatilhos) {
            if (textoPergunta.toLowerCase(Locale.ROOT)
                    .startsWith(gatilho)) {
                memoria = textoPergunta
                        .substring(gatilho.length())
                        .trim();
                break;
            }
        }

        if (memoria == null || memoria.isEmpty())
            return false;

        MemoriaDB banco = new MemoriaDB(this);
        long id = banco.salvarMemoriaConfirmada(
                memoria,
                "confirmada pelo usuário");
        banco.close();

        if (id > 0) {
            Toast.makeText(this,
                    "🧠 Memória salva",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void falarTexto(String textoFala) {
        if (voz == null
                || textoFala == null
                || textoFala.trim().isEmpty()) {
            return;
        }

        falando = true;
        atualizarStatusOnline();

        if (rostoIA != null)
            rostoIA.setFalando(true);

        if (brilhoFala != null) {
            brilhoFala.setVisibility(View.VISIBLE);
            brilhoFala.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start();
        }

        voz.speak(
                textoFala,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "DaNikeAI");
    }

    void pararFala() {
        if (voz != null)
            voz.stop();

        falando = false;

        if (rostoIA != null)
            rostoIA.setFalando(false);

        if (brilhoFala != null)
            brilhoFala.setVisibility(View.GONE);

        atualizarConexao();
    }

    private void alternarMicrofone() {
        if (reconhecedor == null) {
            Toast.makeText(this,
                    "Reconhecimento de voz indisponível.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PEDIR_MICROFONE);
            return;
        }

        if (modoEscutaContinua || ouvindo) {
            modoEscutaContinua = false;
            try { reconhecedor.cancel(); } catch (Exception ignored) {}
            ouvindo = false;
            if (indicador != null) indicador.setText("●  PRONTO PARA OUVIR");
            atualizarConexao();
            return;
        }

        modoEscutaContinua = true;
        pararFala();
        iniciarEscutaContinua();
    }

    private void iniciarEscutaContinua() {
        if (!modoEscutaContinua || reconhecedor == null) return;
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) return;

        try {
            if (ouvindo) return;
            statusIA.setText("●  OUVINDO");
            statusIA.setTextColor(Color.rgb(0, 225, 255));
            if (indicador != null) indicador.setText("●  OUVINDO...");
            reconhecedor.startListening(intentVoz);
        } catch (Exception e) {
            iniciarEscutaContinuaComAtraso(800);
        }
    }

    private void iniciarEscutaContinuaComAtraso(long atraso) {
        if (!modoEscutaContinua) return;
        new android.os.Handler(getMainLooper()).postDelayed(
                this::iniciarEscutaContinua, atraso);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == PEDIR_MICROFONE
                && grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            alternarMicrofone();
        } else if (requestCode == PEDIR_MICROFONE) {
            Toast.makeText(this,
                    "Permissão do microfone negada.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void garantirSessao() {
        if (sessaoAtual > 0) return;

        long ultimo = chatDB.ultimaSessao();
        if (ultimo > 0) {
            sessaoAtual = ultimo;
            nomeSessao.setText(
                    chatDB.nomeSessao(sessaoAtual));
        } else {
            novaSessao();
        }
    }

    private void novaSessao() {
        sessaoAtual = chatDB.criarSessao("Nova conversa");
        chatLista.removeAllViews();
        tituloAssunto.setText("💬  Conversa atual");
        nomeSessao.setText("nova");
        rolarChatFinal();
    }

    private void salvarMensagemSessao(int tipo, String textoMensagem) {
        if (sessaoAtual <= 0)
            garantirSessao();

        chatDB.salvarMensagem(
                sessaoAtual,
                tipo,
                textoMensagem);

        if (tipo == 1
                && "Nova conversa".equals(
                chatDB.nomeSessao(sessaoAtual))) {

            String nome = textoMensagem.length() > 32
                    ? textoMensagem.substring(0, 32) + "…"
                    : textoMensagem;

            chatDB.renomearSessao(
                    sessaoAtual,
                    nome);

            nomeSessao.setText(nome);
            tituloAssunto.setText("💬  Conversa atual");
        }
    }

    private void carregarSessaoAtual() {
        if (sessaoAtual <= 0)
            garantirSessao();

        chatLista.removeAllViews();

        Cursor c = chatDB.mensagens(sessaoAtual);

        try {
            while (c.moveToNext()) {
                int tipo = c.getInt(
                        c.getColumnIndexOrThrow("tipo"));
                String msg = c.getString(
                        c.getColumnIndexOrThrow("texto"));

                if (tipo == 1)
                    adicionarBolhaUsuario(msg);
                else
                    adicionarBolhaIA(msg);
            }
        } finally {
            c.close();
        }

        nomeSessao.setText(
                chatDB.nomeSessao(sessaoAtual));

        rolarChatFinal();
    }

    private void abrirConversas() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(18), dp(18), dp(10));
        box.setBackground(
                fundo(Color.rgb(3, 10, 25), 24));

        TextView titulo = texto(
                "💬  Suas conversas",
                20,
                Color.WHITE,
                true);
        box.addView(titulo,
                new LinearLayout.LayoutParams(-1, dp(48)));

        Button nova = botao(
                "＋  Nova conversa",
                Color.rgb(25, 55, 110));
        box.addView(nova,
                new LinearLayout.LayoutParams(-1, dp(50)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        Cursor c = chatDB.sessoes();

        try {
            while (c.moveToNext()) {
                final long id = c.getLong(
                        c.getColumnIndexOrThrow("id"));
                String nome = c.getString(
                        c.getColumnIndexOrThrow("nome"));

                Button item = botao(
                        "💬  " + nome,
                        Color.argb(45, 0, 55, 105));
                item.setGravity(
                        Gravity.CENTER_VERTICAL | Gravity.LEFT);
                item.setPadding(
                        dp(14), 0, dp(10), 0);

                LinearLayout.LayoutParams ip =
                        new LinearLayout.LayoutParams(
                                -1, dp(58));
                ip.setMargins(0, dp(4), 0, dp(4));

                lista.addView(item, ip);

                item.setOnClickListener(v -> {
                    sessaoAtual = id;
                    carregarSessaoAtual();
                    ((ViewGroup) box.getParent()).requestLayout();
                });
            }
        } finally {
            c.close();
        }

        scroll.addView(lista);
        box.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(box)
                .create();

        nova.setOnClickListener(v -> {
            novaSessao();
            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int) (getResources()
                            .getDisplayMetrics()
                            .widthPixels * .92f),
                    (int) (getResources()
                            .getDisplayMetrics()
                            .heightPixels * .78f));
        }
    }

    private void abrirMemorias() {
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(dp(18), dp(16), dp(18), dp(12));
        lista.setBackground(
                fundo(Color.rgb(3, 10, 25), 24));

        TextView titulo = texto(
                "🧠  Memória da DaNikeAI",
                20,
                Color.WHITE,
                true);
        lista.addView(titulo,
                new LinearLayout.LayoutParams(-1, dp(48)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout itens = new LinearLayout(this);
        itens.setOrientation(LinearLayout.VERTICAL);

        MemoriaDB db = new MemoriaDB(this);
        Cursor c = db.listarMemorias();

        int total = 0;

        try {
            while (c.moveToNext()) {
                String m = c.getString(
                        c.getColumnIndexOrThrow("texto"));

                TextView item = texto(
                        "🧠  " + m,
                        14,
                        Color.WHITE);

                item.setPadding(
                        dp(14), dp(12),
                        dp(14), dp(12));

                GradientDrawable bg =
                        fundo(Color.argb(55, 75, 25, 120), 16);
                bg.setStroke(
                        dp(1),
                        Color.rgb(120, 70, 220));
                item.setBackground(bg);

                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(
                                -1, -2);
                lp.setMargins(
                        0, dp(4), 0, dp(4));

                itens.addView(item, lp);
                total++;
            }
        } finally {
            c.close();
            db.close();
        }

        if (total == 0) {
            TextView vazio = texto(
                    "Nenhuma memória salva.\n\nUse:\n“Lembre que...”",
                    14,
                    Color.rgb(170, 200, 230));
            vazio.setPadding(
                    dp(12), dp(20), dp(12), dp(20));
            itens.addView(vazio);
        }

        scroll.addView(itens);
        lista.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(lista)
                .setPositiveButton("FECHAR", null)
                .create();

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (reconhecedor != null) {
            reconhecedor.cancel();
            reconhecedor.destroy();
        }

        if (voz != null) {
            voz.stop();
            voz.shutdown();
        }

        if (rostoIA != null)
            rostoIA.setFalando(false);

        if (conexaoCallback != null) {
            ConnectivityManager cm =
                    (ConnectivityManager)
                            getSystemService(
                                    Context.CONNECTIVITY_SERVICE);

            if (cm != null) {
                try {
                    cm.unregisterNetworkCallback(
                            conexaoCallback);
                } catch (Exception ignored) {
                }
            }
        }

        if (chatDB != null)
            chatDB.close();

        super.onDestroy();
    }

    // ============================================================
    // BANCO DE CONVERSAS — separado da memória permanente
    // ============================================================

    static class ChatDB extends SQLiteOpenHelper {

        private static final String DB = "danike_chats.db";
        private static final int VERSION = 1;

        ChatDB(Context c) {
            super(c, DB, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE sessoes (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "nome TEXT NOT NULL," +
                            "criado_em INTEGER NOT NULL," +
                            "atualizado_em INTEGER NOT NULL)");

            db.execSQL(
                    "CREATE TABLE mensagens (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "sessao_id INTEGER NOT NULL," +
                            "tipo INTEGER NOT NULL," +
                            "texto TEXT NOT NULL," +
                            "criado_em INTEGER NOT NULL)");
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {
        }

        long criarSessao(String nome) {
            SQLiteDatabase db = getWritableDatabase();
            long agora = System.currentTimeMillis();

            android.content.ContentValues v =
                    new android.content.ContentValues();
            v.put("nome", nome);
            v.put("criado_em", agora);
            v.put("atualizado_em", agora);

            return db.insert("sessoes", null, v);
        }

        long ultimaSessao() {
            SQLiteDatabase db = getReadableDatabase();

            Cursor c = db.rawQuery(
                    "SELECT id FROM sessoes " +
                            "ORDER BY atualizado_em DESC LIMIT 1",
                    null);

            try {
                return c.moveToFirst()
                        ? c.getLong(0)
                        : -1;
            } finally {
                c.close();
            }
        }

        String nomeSessao(long id) {
            SQLiteDatabase db = getReadableDatabase();

            Cursor c = db.rawQuery(
                    "SELECT nome FROM sessoes WHERE id=?",
                    new String[]{String.valueOf(id)});

            try {
                return c.moveToFirst()
                        ? c.getString(0)
                        : "Nova conversa";
            } finally {
                c.close();
            }
        }

        void renomearSessao(long id, String nome) {
            SQLiteDatabase db = getWritableDatabase();

            android.content.ContentValues v =
                    new android.content.ContentValues();
            v.put("nome", nome);
            v.put("atualizado_em",
                    System.currentTimeMillis());

            db.update(
                    "sessoes",
                    v,
                    "id=?",
                    new String[]{String.valueOf(id)});
        }

        void salvarMensagem(
                long sessaoId,
                int tipo,
                String texto) {

            SQLiteDatabase db = getWritableDatabase();
            long agora = System.currentTimeMillis();

            android.content.ContentValues v =
                    new android.content.ContentValues();

            v.put("sessao_id", sessaoId);
            v.put("tipo", tipo);
            v.put("texto", texto);
            v.put("criado_em", agora);

            db.insert("mensagens", null, v);

            android.content.ContentValues atualizacao =
                    new android.content.ContentValues();
            atualizacao.put("atualizado_em", agora);

            db.update(
                    "sessoes",
                    atualizacao,
                    "id=?",
                    new String[]{String.valueOf(sessaoId)});
        }

        Cursor sessoes() {
            return getReadableDatabase().rawQuery(
                    "SELECT id,nome FROM sessoes " +
                            "ORDER BY atualizado_em DESC",
                    null);
        }

        Cursor mensagens(long sessaoId) {
            return getReadableDatabase().rawQuery(
                    "SELECT tipo,texto FROM mensagens " +
                            "WHERE sessao_id=? " +
                            "ORDER BY id ASC",
                    new String[]{String.valueOf(sessaoId)});
        }
    }

    // ============================================================
    // VISUAL — NÃO altera DaNikeFaceView
    // ============================================================

    class SpeakingGlow extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float pulso = 0;

        SpeakingGlow(Context c) {
            super(c);
            setAlpha(.92f);
            postInvalidateDelayed(30);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            pulso += .08f;

            float r = dp(65) + (float) Math.sin(pulso) * dp(9);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(55, 255, 190, 25));
            p.setShadowLayer(
                    dp(28), 0, 0,
                    Color.rgb(255, 185, 25));

            c.drawCircle(cx, cy, r, p);
            p.clearShadowLayer();

            postInvalidateDelayed(30);
        }
    }

    class InstagramIcon extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        InstagramIcon(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float s = Math.min(getWidth(), getHeight()) * .72f;
            float l = (getWidth() - s) / 2f;
            float t = (getHeight() - s) / 2f;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2.3f));
            p.setColor(Color.rgb(255, 255, 255));
            p.setShadowLayer(
                    dp(7), 0, 0,
                    Color.rgb(190, 60, 255));

            c.drawRoundRect(
                    new RectF(l, t, l + s, t + s),
                    dp(7), dp(7), p);

            c.drawCircle(
                    l + s / 2f,
                    t + s / 2f,
                    s * .23f,
                    p);

            p.setStyle(Paint.Style.FILL);
            c.drawCircle(
                    l + s * .76f,
                    t + s * .24f,
                    dp(2.2f), p);

            p.clearShadowLayer();
        }
    }

    class VerifiedBadge extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        VerifiedBadge(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float r = Math.min(getWidth(), getHeight()) * .34f;
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(45, 155, 255));
            p.setShadowLayer(
                    dp(7), 0, 0,
                    Color.rgb(45, 155, 255));
            c.drawCircle(cx, cy, r, p);
            p.clearShadowLayer();

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.8f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.WHITE);

            Path path = new Path();
            path.moveTo(cx - r * .48f, cy);
            path.lineTo(cx - r * .08f, cy + r * .38f);
            path.lineTo(cx + r * .55f, cy - r * .42f);

            c.drawPath(path, p);
        }
    }

    class NeonBackground extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float fase = 0;
        ArrayList<Particle> particulas = new ArrayList<>();

        NeonBackground(Context c) {
            super(c);

            for (int i = 0; i < 22; i++) {
                particulas.add(new Particle(
                        (float) Math.random(),
                        (float) Math.random(),
                        1 + (float) Math.random() * 3,
                        .4f + (float) Math.random()));
            }

            postInvalidateDelayed(30);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float w = getWidth();
            float h = getHeight();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(1, 3, 12));
            c.drawRect(0, 0, w, h, p);

            fase += .004f;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));

            for (int i = -2; i < 10; i++) {
                float y =
                        ((i * dp(110))
                                + fase * dp(430))
                                % (h + dp(200))
                                - dp(100);

                p.setColor(Color.argb(
                        42, 0, 130, 255));

                Path linha = new Path();
                linha.moveTo(-dp(80), y);
                linha.cubicTo(
                        w * .28f, y - dp(55),
                        w * .70f, y + dp(70),
                        w + dp(100), y - dp(35));
                c.drawPath(linha, p);
            }

            p.setStyle(Paint.Style.FILL);

            for (Particle q : particulas) {
                q.y -= .0011f * q.vel;
                q.x += Math.sin(fase * 3 + q.y * 8) * .0006f;

                if (q.y < 0) {
                    q.y = 1;
                    q.x = (float) Math.random();
                }

                float x = q.x * w;
                float y = q.y * h;

                p.setColor(Color.argb(
                        160, 0, 210, 255));
                p.setShadowLayer(
                        dp(7), 0, 0,
                        Color.rgb(0, 210, 255));

                c.drawCircle(
                        x, y, dp(q.tamanho), p);

                p.clearShadowLayer();
            }

            postInvalidateDelayed(30);
        }
    }

    class Particle {
        float x, y, tamanho, vel;

        Particle(float x, float y,
                 float tamanho, float vel) {
            this.x = x;
            this.y = y;
            this.tamanho = tamanho;
            this.vel = vel;
        }
    }

    class NeonFaceStage extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        float angulo = 0;

        NeonFaceStage(Context c) {
            super(c);
            postInvalidateDelayed(30);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float r = Math.min(getWidth(), getHeight()) * .39f;

            angulo += .65f;

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.5f));
            p.setColor(Color.argb(
                    155, 0, 220, 255));
            p.setShadowLayer(
                    dp(14), 0, 0,
                    Color.rgb(0, 220, 255));

            c.drawCircle(cx, cy, r, p);
            p.clearShadowLayer();

            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(
                    160, 255, 190, 30));

            c.drawCircle(
                    cx, cy, r * 1.08f, p);

            RectF arco = new RectF(
                    cx - r * 1.13f,
                    cy - r * 1.13f,
                    cx + r * 1.13f,
                    cy + r * 1.13f);

            p.setStrokeWidth(dp(3));
            p.setColor(Color.rgb(0, 220, 255));
            c.drawArc(
                    arco, angulo, 85, false, p);

            p.setColor(Color.rgb(255, 190, 30));
            c.drawArc(
                    arco, angulo + 180, 55, false, p);

            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(
                        angulo * .75 + i * 60);

                float raio = r * 1.16f;
                float x = cx
                        + (float) Math.cos(a) * raio;
                float y = cy
                        + (float) Math.sin(a) * raio;

                boolean dourado = i % 3 == 0;

                int cor = dourado
                        ? Color.rgb(255, 190, 30)
                        : Color.rgb(0, 220, 255);

                p.setStyle(Paint.Style.FILL);
                p.setColor(cor);
                p.setShadowLayer(
                        dp(9), 0, 0, cor);

                c.drawCircle(
                        x, y,
                        dp(dourado ? 4 : 3),
                        p);

                p.clearShadowLayer();
            }

            postInvalidateDelayed(30);
        }
    }
}
