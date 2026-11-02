package com.danike.ai;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * NEXO Watch Party Cinema V4.
 * Usa o mesmo ExoPlayer da sala; so o botao [ ] abre a tela cheia.
 * Enquanto assiste: imagem limpa. Ao tocar: controles por 4s.
 * Pinça: zoom proporcional, preenchimento sem esticar, opção de ver tudo.
 * Mensagens novas: bolhas translucidas que sobem e desaparecem.
 */
public final class NexoWatchCinemaOverlayV2 {
    public interface Resultado { void concluido(boolean sucesso); }
    public interface Mensagem { void enviar(String texto, Resultado resultado); }
    public interface Perfil { void abrir(String uid, View ancora); }
    public interface Acoes { void executar(String nome); }

    private static final int AZUL = 0xFF00BFFF;
    private static final int BRANCO = 0xFFF7FBFF;
    private final Activity a;
    private final ExoPlayer player;
    private final PlayerView origem;
    private final Mensagem enviar;
    private final Runnable publicarEstado;
    private final Perfil perfil;
    private final Acoes acoes;
    private final String titulo;
    private final boolean contemTemporadas;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean lider;
    private boolean aberto;
    private boolean controlesVisiveis;
    private boolean arrastandoProgresso;
    private ScaleGestureDetector detectorZoom;
    private boolean gestoDePinca;
    private float zoomVideo = 1f; // 1x = preencher; abaixo de 1x = encaixar
    private TextView indicadorZoom;
    private Runnable esconderIndicadorZoom;
    private int orientacaoAnterior;
    private Dialog dialog;
    private PlayerView tela;
    private FrameLayout raiz;
    private LinearLayout barraSuperior, barraInferior, centroControles, bolhas, historico, painelChat;
    private ScrollView scrollHistorico;
    private ImageView botaoCentral;
    private TextView tempo, chipLider, chipPessoas;
    private SeekBar progresso;
    private PopupWindow popup;
    private Runnable esconderControles, atualizarProgresso;
    private int quantidadeParticipantes = 1;
    private final List<String> pessoasUid = new ArrayList<>();
    private final List<String> pessoasNome = new ArrayList<>();
    private String liderUid = "";

    public NexoWatchCinemaOverlayV2(Activity atividade, ExoPlayer motor, PlayerView origem,
                                    boolean souLider, String nome, boolean serie,
                                    Mensagem enviarMensagem, Runnable publicar,
                                    Perfil abrirPerfil, Acoes executarAcao) {
        a = atividade;
        player = motor;
        this.origem = origem;
        lider = souLider;
        titulo = nome;
        contemTemporadas = serie;
        enviar = enviarMensagem;
        publicarEstado = publicar;
        perfil = abrirPerfil;
        acoes = executarAcao;
    }

    private int dp(float v) { return Math.round(v * a.getResources().getDisplayMetrics().density); }
    private GradientDrawable fundo(int cor, int borda, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(cor);
        d.setCornerRadius(dp(raio));
        if (borda != Color.TRANSPARENT) d.setStroke(Math.max(1, dp(1)), borda);
        return d;
    }
    private TextView texto(String conteudo, float sp, int cor) {
        TextView t = new TextView(a);
        t.setText(conteudo);
        t.setTextColor(cor);
        t.setTextSize(sp);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }
    private ImageView icone(String nome, String descricao, int lado) {
        ImageView b = new ImageView(a);
        b.setImageDrawable(NexoWatchIconsV2.icon(nome, BRANCO));
        b.setPadding(dp(11), dp(11), dp(11), dp(11));
        b.setBackground(fundo(0xBB02070D, 0x8C087FAF, lado / 2));
        b.setContentDescription(descricao);
        b.setClickable(true);
        b.setFocusable(true);
        return b;
    }
    private static String limitar(String valor, int max) {
        if (valor == null) return "";
        String s = valor.trim();
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
    public boolean estaAberto() { return aberto && dialog != null && dialog.isShowing(); }

    public void atualizarLider(boolean novo) {
        lider = novo;
        if (botaoCentral != null) botaoCentral.setAlpha(lider ? 1f : 0.52f);
        if (progresso != null) progresso.setEnabled(lider);
        if (chipLider != null) chipLider.setVisibility(lider ? View.VISIBLE : View.GONE);
    }
    public void atualizarParticipantes(int n) {
        quantidadeParticipantes = Math.max(0, n);
        if (chipPessoas != null) chipPessoas.setText(String.valueOf(quantidadeParticipantes));
    }
    public void atualizarPessoas(List<?> ids, Map<String, Object> nomes, String uidLider) {
        pessoasUid.clear(); pessoasNome.clear();
        liderUid = uidLider == null ? "" : uidLider;
        if (ids != null) for (Object o : ids) {
            if (o == null) continue;
            String id = String.valueOf(o);
            pessoasUid.add(id);
            Object nome = nomes == null ? null : nomes.get(id);
            pessoasNome.add(limitar(nome == null ? "Membro NEXO" : String.valueOf(nome), 40));
        }
        atualizarParticipantes(pessoasUid.size());
    }

    public void abrir() {
        if (estaAberto() || player == null || origem == null) return;
        orientacaoAnterior = a.getRequestedOrientation();
        dialog = new Dialog(a, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        detectorZoom = new ScaleGestureDetector(a, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScaleBegin(ScaleGestureDetector d) {
                if (painelChat != null && painelChat.getVisibility() == View.VISIBLE) return false;
                gestoDePinca = true;
                if (esconderControles != null) handler.removeCallbacks(esconderControles);
                return true;
            }
            @Override public boolean onScale(ScaleGestureDetector d) {
                if (!gestoDePinca) return false;
                float fator = d.getScaleFactor();
                if (Float.isNaN(fator) || Float.isInfinite(fator)) return false;
                zoomVideo = Math.max(0.85f, Math.min(3.0f, zoomVideo * fator));
                aplicarZoom();
                mostrarIndicadorZoom();
                return true;
            }
            @Override public void onScaleEnd(ScaleGestureDetector d) {
                gestoDePinca = false;
                reagendarOcultar();
            }
        });
        raiz = new FrameLayout(a) {
            private float downX, downY;
            private boolean houveMultitoque;
            private boolean toqueEmBotoes;
            @Override public boolean dispatchTouchEvent(MotionEvent e) {
                final int acao = e.getActionMasked();
                if (acao == MotionEvent.ACTION_DOWN) {
                    downX = e.getX(); downY = e.getY();
                    houveMultitoque = false;
                    // Evita esconder controles quando o usuario toca nos botoes.
                    toqueEmBotoes = controlesVisiveis && (
                            downY < dp(56) || downY > getHeight()-dp(100) ||
                            (Math.abs(downX-getWidth()/2f)<dp(115) &&
                             Math.abs(downY-getHeight()/2f)<dp(52)));
                }
                if (e.getPointerCount() > 1 || acao == MotionEvent.ACTION_POINTER_DOWN) {
                    houveMultitoque = true;
                }
                // Processar toda a sequência da pinça antes dos controles do vídeo.
                if (detectorZoom != null) detectorZoom.onTouchEvent(e);
                if (houveMultitoque) {
                    if (acao == MotionEvent.ACTION_UP || acao == MotionEvent.ACTION_CANCEL) {
                        houveMultitoque = false;
                    }
                    return true;
                }
                // Os controles devem reaparecer em qualquer toque curto na imagem,
                // sem limitar a area central do filme (bordas/tela grande/tablet).
                // Primeiro despachar aos botoes reais: Play, seek, chat e menu.
                boolean consumiu = super.dispatchTouchEvent(e);
                boolean toqueCurto = acao == MotionEvent.ACTION_UP
                        && Math.abs(e.getX() - downX) < dp(24)
                        && Math.abs(e.getY() - downY) < dp(24);
                if (toqueCurto && !gestoDePinca && !toqueEmBotoes
                        && (painelChat == null || painelChat.getVisibility() != View.VISIBLE)
                        && (popup == null || !popup.isShowing())) {
                    android.util.Log.d("NEXO_CINEMA_UI", "TOQUE_RECEBIDO");
                    alternarControles();
                    return true;
                }
                return consumiu;
            }
        };
        raiz.setBackgroundColor(Color.BLACK);
        // Garantir ACTION_DOWN e ACTION_UP mesmo quando o PlayerView nao consome o toque.
        raiz.setClickable(true);
        tela = new PlayerView(a);
        tela.setUseController(false);
        tela.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        // Preencher a tela mantendo a proporção: sem barras pretas no modo cinema.
        // Isso recorta a imagem quando a proporção do vídeo não coincide com a tela.
        tela.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        tela.setKeepScreenOn(true);
        origem.setPlayer(null);
        tela.setPlayer(player);
        raiz.addView(tela, new FrameLayout.LayoutParams(-1, -1));
        aplicarZoom();

        // Gestos de brilho/volume nos cantos, sem abrir controles nativos.
        // Os toques nas bordas sao processados pelo dispatchTouchEvent da raiz.
        // Nao executar duas vezes: gesto de borda + callback + dispatch na raiz.
        NexoCinemaExtras.adicionarGestos(a, raiz, tela, dialog, () -> {});

        // NEXO_CINEMA_CONTROLES_CENTRAIS_V6
        // Tres botoes independentes e compactos, no centro EXATO da tela.
        // Nenhuma moldura larga sobre o filme: cada toque tem uma area circular.
        centroControles = new LinearLayout(a);
        centroControles.setOrientation(LinearLayout.HORIZONTAL);
        centroControles.setGravity(Gravity.CENTER);
        centroControles.setClipChildren(false);
        centroControles.setClipToPadding(false);
        centroControles.setVisibility(View.GONE); // aparecem apenas ao tocar

        // RESGATE_PRE_NETFLIX: ícones nativos do Android; nenhum vetor NEXO personalizado.
        ImageView voltar10 = icone("rewind10", "Voltar 10 segundos", 48);
        botaoCentral = icone("pause", "Reproduzir ou pausar", 56);
        ImageView avancar10 = icone("forward10", "Avançar 10 segundos", 48);
        voltar10.setImageResource(android.R.drawable.ic_media_rew);
        botaoCentral.setImageResource(android.R.drawable.ic_media_pause);
        avancar10.setImageResource(android.R.drawable.ic_media_ff);
        voltar10.setColorFilter(Color.WHITE);
        botaoCentral.setColorFilter(Color.WHITE);
        avancar10.setColorFilter(Color.WHITE);
        voltar10.setPadding(dp(9), dp(9), dp(9), dp(9));
        botaoCentral.setPadding(dp(11), dp(11), dp(11), dp(11));
        avancar10.setPadding(dp(9), dp(9), dp(9), dp(9));
        voltar10.setBackgroundColor(Color.TRANSPARENT);
        botaoCentral.setBackgroundColor(Color.TRANSPARENT);
        avancar10.setBackgroundColor(Color.TRANSPARENT);
        voltar10.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avancar10.setScaleType(ImageView.ScaleType.FIT_CENTER);
        botaoCentral.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams lado1 = new LinearLayout.LayoutParams(dp(48), dp(48));
        lado1.rightMargin = dp(22);
        centroControles.addView(voltar10, lado1);
        centroControles.addView(botaoCentral,
                new LinearLayout.LayoutParams(dp(56), dp(56)));
        LinearLayout.LayoutParams lado2 = new LinearLayout.LayoutParams(dp(48), dp(48));
        lado2.leftMargin = dp(22);
        centroControles.addView(avancar10, lado2);
        // WRAP_CONTENT evita que a faixa central invisivel bloqueie o video.
        FrameLayout.LayoutParams controlesCentro = new FrameLayout.LayoutParams(
                -2, dp(74), Gravity.CENTER);
        raiz.addView(centroControles, controlesCentro);

        voltar10.setOnClickListener(v -> {
            pular(-10000L);
            mostrarControles();
        });
        avancar10.setOnClickListener(v -> {
            pular(10000L);
            mostrarControles();
        });
        botaoCentral.setOnClickListener(v -> {
            if (!lider) {
                Toast.makeText(a, "Só o líder controla o filme", Toast.LENGTH_SHORT).show();
                mostrarControles();
                return;
            }
            if (player.isPlaying()) player.pause(); else player.play();
            publicarEstado.run();
            atualizarBarra();
            mostrarControles();
        });

        barraSuperior = new LinearLayout(a);
        barraSuperior.setGravity(Gravity.CENTER_VERTICAL);
        barraSuperior.setPadding(dp(10), dp(5), dp(10), dp(5));
        barraSuperior.setBackgroundColor(0x9C000000);
        ImageView voltar = icone("back", "Voltar à sala", 39);
        barraSuperior.addView(voltar, new LinearLayout.LayoutParams(dp(40), dp(40)));
        voltar.setOnClickListener(v -> fechar());
        TextView marca = texto("NEXO", 16, AZUL);
        marca.setTypeface(null, android.graphics.Typeface.BOLD);
        marca.setPadding(dp(9), 0, dp(7), 0);
        barraSuperior.addView(marca);
        TextView nome = texto(titulo, 13.5f, BRANCO);
        nome.setGravity(Gravity.CENTER);
        nome.setSingleLine(true);
        nome.setEllipsize(TextUtils.TruncateAt.END);
        barraSuperior.addView(nome, new LinearLayout.LayoutParams(0, -2, 1));
        chipLider = texto("LÍDER", 11, AZUL);
        chipLider.setPadding(dp(7), dp(4), dp(7), dp(4));
        chipLider.setBackground(fundo(0xC0000000, 0xBB00BFFF, 10));
        barraSuperior.addView(chipLider);
        ImageView transmitir = icone("tv", "Espelhar tela na TV", 38);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(40), dp(40));
        tp.leftMargin = dp(6);
        barraSuperior.addView(transmitir, tp);
        transmitir.setOnClickListener(v -> abrirEspelhamento());
        ImageView chat = icone("chat", "Chat", 38);
        barraSuperior.addView(chat, new LinearLayout.LayoutParams(dp(40), dp(40)));
        chat.setOnClickListener(v -> alternarChat());
        ImageView pessoas = icone("users", "Participantes e perfis", 38);
        barraSuperior.addView(pessoas, new LinearLayout.LayoutParams(dp(40), dp(40)));
        pessoas.setOnClickListener(this::mostrarParticipantes);
        chipPessoas = texto(String.valueOf(quantidadeParticipantes), 11, BRANCO);
        chipPessoas.setPadding(dp(2), 0, dp(5), 0);
        barraSuperior.addView(chipPessoas);
        ImageView mais = icone("more", "Mais opções", 38);
        barraSuperior.addView(mais, new LinearLayout.LayoutParams(dp(40), dp(40)));
        mais.setOnClickListener(this::mostrarAcoes);
        raiz.addView(barraSuperior, new FrameLayout.LayoutParams(-1, dp(51), Gravity.TOP));

        barraInferior = new LinearLayout(a);
        barraInferior.setOrientation(LinearLayout.VERTICAL);
        barraInferior.setPadding(dp(12), dp(4), dp(12), dp(9));
        barraInferior.setBackgroundColor(0xA5000000);
        progresso = new SeekBar(a);
        progresso.setMax(1000);
        progresso.setProgressTintList(android.content.res.ColorStateList.valueOf(AZUL));
        progresso.setThumbTintList(android.content.res.ColorStateList.valueOf(AZUL));
        progresso.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0x888A9BA9));
        progresso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar s) { arrastandoProgresso = true; }
            @Override public void onProgressChanged(SeekBar s, int p, boolean usuario) {
                if (usuario) reagendarOcultar();
            }
            @Override public void onStopTrackingTouch(SeekBar s) {
                arrastandoProgresso = false;
                long total = player.getDuration();
                if (lider && total > 0) {
                    player.seekTo((long)s.getProgress() * total / 1000L);
                    publicarEstado.run();
                }
                reagendarOcultar();
            }
        });
        barraInferior.addView(progresso, new LinearLayout.LayoutParams(-1, dp(31)));
        LinearLayout linhaTempo = new LinearLayout(a);
        linhaTempo.setGravity(Gravity.CENTER_VERTICAL);
        tempo = texto("00:00 / --:--", 12, BRANCO);
        linhaTempo.addView(tempo, new LinearLayout.LayoutParams(0, dp(35), 1));
        ImageView som = icone("volume", "Volume", 34);
        linhaTempo.addView(som, new LinearLayout.LayoutParams(dp(36), dp(35)));
        som.setOnClickListener(this::mostrarVolume);
        ImageView sair = icone("fullscreen", "Sair da tela cheia", 34);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(36), dp(35));
        sp.leftMargin = dp(7);
        linhaTempo.addView(sair, sp);
        sair.setOnClickListener(v -> fechar());
        barraInferior.addView(linhaTempo);
        raiz.addView(barraInferior, new FrameLayout.LayoutParams(-1, dp(89), Gravity.BOTTOM));

        // So as mensagens novas aparecem sobre o filme, independentemente dos controles.
        bolhas = new LinearLayout(a);
        bolhas.setOrientation(LinearLayout.VERTICAL);
        bolhas.setGravity(Gravity.BOTTOM);
        bolhas.setClickable(false);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(
                Math.min(dp(300), (int)(a.getResources().getDisplayMetrics().widthPixels * .36f)),
                dp(175), Gravity.BOTTOM | Gravity.LEFT);
        bp.setMargins(dp(16), 0, 0, dp(80));
        raiz.addView(bolhas, bp);

        // Indicação discreta do zoom, visível só durante a pinça.
        indicadorZoom = texto("", 12, BRANCO);
        indicadorZoom.setGravity(Gravity.CENTER);
        indicadorZoom.setPadding(dp(12), dp(6), dp(12), dp(6));
        indicadorZoom.setBackground(fundo(0xBE06131F, 0x9900BFFF, 12));
        indicadorZoom.setVisibility(View.GONE);
        FrameLayout.LayoutParams iz = new FrameLayout.LayoutParams(-2, dp(34),
                Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        iz.topMargin = dp(66);
        raiz.addView(indicadorZoom, iz);

        painelChat = new LinearLayout(a);
        painelChat.setOrientation(LinearLayout.VERTICAL);
        painelChat.setPadding(dp(10), dp(9), dp(10), dp(10));
        painelChat.setBackground(fundo(0xE9060C14, 0x9900BFFF, 16));
        painelChat.setVisibility(View.GONE);
        LinearLayout cabChat = new LinearLayout(a);
        cabChat.setGravity(Gravity.CENTER_VERTICAL);
        TextView tituloChat = texto("CHAT AO VIVO", 13, AZUL);
        tituloChat.setTypeface(null, android.graphics.Typeface.BOLD);
        cabChat.addView(tituloChat, new LinearLayout.LayoutParams(0, dp(32), 1));
        ImageView fecharChat = icone("close", "Fechar chat", 32);
        cabChat.addView(fecharChat, new LinearLayout.LayoutParams(dp(34), dp(34)));
        fecharChat.setOnClickListener(v -> alternarChat());
        painelChat.addView(cabChat);
        scrollHistorico = new ScrollView(a);
        scrollHistorico.setFillViewport(false);
        scrollHistorico.setVerticalScrollBarEnabled(true);
        historico = new LinearLayout(a);
        historico.setOrientation(LinearLayout.VERTICAL);
        scrollHistorico.addView(historico);
        painelChat.addView(scrollHistorico, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout entrada = new LinearLayout(a);
        entrada.setGravity(Gravity.CENTER_VERTICAL);
        EditText campo = new EditText(a);
        campo.setTextSize(13);
        campo.setTextColor(BRANCO);
        campo.setHint("Mensagem...");
        campo.setHintTextColor(0xFFB4C3D4);
        campo.setSingleLine(true);
        campo.setImeOptions(EditorInfo.IME_ACTION_SEND);
        campo.setPadding(dp(10), dp(7), dp(10), dp(7));
        campo.setBackground(fundo(0xFF101922, 0x8900BFFF, 12));
        entrada.addView(campo, new LinearLayout.LayoutParams(0, dp(40), 1));
        ImageView mandar = icone("send", "Enviar mensagem", 38);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(dp(40), dp(40));
        mlp.leftMargin = dp(6);
        entrada.addView(mandar, mlp);
        mandar.setOnClickListener(v -> {
            final String mensagem = campo.getText().toString().trim();
            if (mensagem.isEmpty()) return;
            campo.setText(""); // feedback imediato; atualizacao local do Firestore e instantanea
            enviar.enviar(mensagem, ok -> {
                if (!ok && estaAberto() && campo.getText().length() == 0) {
                    campo.setText(mensagem); campo.setSelection(campo.length());
                }
            });
        });
        campo.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_SEND) { mandar.performClick(); return true; }
            return false;
        });
        painelChat.addView(entrada, new LinearLayout.LayoutParams(-1, -2));
        int larguraPop = Math.min(dp(330), Math.max(dp(238),
                (int)(a.getResources().getDisplayMetrics().widthPixels * .33f)));
        int alturaPop = Math.min(dp(350),
                (int)(a.getResources().getDisplayMetrics().heightPixels * .66f));
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(larguraPop, alturaPop,
                Gravity.TOP | Gravity.RIGHT);
        cp.setMargins(0, dp(56), dp(12), 0);
        raiz.addView(painelChat, cp);

        dialog.setContentView(raiz);
        dialog.setOnDismissListener(d -> {
            aberto = false;
            handler.removeCallbacksAndMessages(null);
            if (popup != null) { popup.dismiss(); popup = null; }
            if (tela != null) {
                tela.setScaleX(1f); tela.setScaleY(1f);
                tela.setPlayer(null); tela = null;
            }
            detectorZoom = null;
            indicadorZoom = null;
            zoomVideo = 1f;
            gestoDePinca = false;
            if (origem != null && player != null) origem.setPlayer(player);
            a.setRequestedOrientation(orientacaoAnterior);
            dialog = null; raiz = null;
            barraSuperior = null; barraInferior = null; centroControles = null;
            bolhas = null; historico = null; painelChat = null;
            botaoCentral = null; tempo = null; progresso = null;
            chipLider = null; chipPessoas = null;
        });
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(-1, -1);
            w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        aberto = true;
        atualizarLider(lider);
        // A expansao pelo botao [ ] e a primeira interacao: mostrar por 4s.
        mostrarControles();
        android.util.Log.d("NEXO_CINEMA_UI", "ABRIU_CINEMA_COM_CONTROLES");
        atualizarProgresso = new Runnable() {
            @Override public void run() {
                if (!estaAberto()) return;
                atualizarBarra();
                handler.postDelayed(this, 850L);
            }
        };
        handler.post(atualizarProgresso);
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    /** O mesmo player é usado para filmes e episódios; zoom não altera o vídeo nem a sincronia. */
    private void aplicarZoom() {
        if (tela == null) return;
        if (zoomVideo < 0.96f) {
            // Pinçar para dentro mostra a imagem completa (pode haver barras pretas).
            tela.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            tela.setScaleX(1f);
            tela.setScaleY(1f);
        } else {
            // Preenche em tela cheia sem distorção; ampliação extra centrada.
            tela.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            float ampliacao = Math.max(1f, zoomVideo);
            tela.setScaleX(ampliacao);
            tela.setScaleY(ampliacao);
        }
    }

    private void mostrarIndicadorZoom() {
        if (indicadorZoom == null) return;
        indicadorZoom.setText(zoomVideo < 0.96f
                ? "Imagem inteira" : String.format(Locale.getDefault(), "Zoom %.1f×", Math.max(1f, zoomVideo)));
        indicadorZoom.setAlpha(1f);
        indicadorZoom.setVisibility(View.VISIBLE);
        indicadorZoom.animate().cancel();
        if (esconderIndicadorZoom != null) handler.removeCallbacks(esconderIndicadorZoom);
        esconderIndicadorZoom = () -> {
            if (indicadorZoom != null) indicadorZoom.animate().alpha(0f).setDuration(220L)
                    .withEndAction(() -> { if (indicadorZoom != null) indicadorZoom.setVisibility(View.GONE); })
                    .start();
        };
        handler.postDelayed(esconderIndicadorZoom, 1050L);
    }

    private void pular(long delta) {
        if (!lider) { Toast.makeText(a, "Só o líder controla o filme", Toast.LENGTH_SHORT).show(); return; }
        long destino = Math.max(0, player.getCurrentPosition() + delta);
        if (player.getDuration() > 0) destino = Math.min(destino, player.getDuration());
        player.seekTo(destino);
        publicarEstado.run();
        reagendarOcultar();
    }
    private static String relogio(long ms) {
        long segundos = Math.max(0, ms / 1000L);
        return String.format(Locale.US, "%02d:%02d:%02d", segundos / 3600L,
                (segundos / 60L) % 60L, segundos % 60L);
    }
    private void atualizarBarra() {
        if (player == null || progresso == null || tempo == null || botaoCentral == null) return;
        long total = player.getDuration();
        if (total > 0 && !arrastandoProgresso) {
            progresso.setProgress((int)Math.min(1000L, player.getCurrentPosition() * 1000L / total));
        }
        tempo.setText(relogio(player.getCurrentPosition()) + " / "
                + (total > 0 ? relogio(total) : "--:--"));
        botaoCentral.setImageResource(player.isPlaying()
                ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        botaoCentral.setColorFilter(Color.WHITE);
    }
    private void alternarControles() {
        if (gestoDePinca) return;
        if (painelChat != null && painelChat.getVisibility() == View.VISIBLE) return;
        if (popup != null && popup.isShowing()) return;
        // Um toque alterna: visiveis -> ocultos, ocultos -> visiveis.
        // O temporizador de 4s continua valendo apos a exibicao.
        if (controlesVisiveis) {
            if (esconderControles != null) handler.removeCallbacks(esconderControles);
            ocultarControles();
        } else mostrarControles();
    }
    private void mostrarControles() {
        if (!estaAberto()) return;
        android.util.Log.d("NEXO_CINEMA_UI", "CONTROLES_VISIVEIS");
        controlesVisiveis = true;
        if (barraSuperior != null) barraSuperior.setVisibility(View.VISIBLE);
        if (barraInferior != null) barraInferior.setVisibility(View.VISIBLE);
        if (centroControles != null) {
            centroControles.animate().cancel();
            centroControles.setAlpha(1f);
            centroControles.setVisibility(View.VISIBLE);
        }
        reagendarOcultar();
    }
    private void reagendarOcultar() {
        if (esconderControles != null) handler.removeCallbacks(esconderControles);
        esconderControles = () -> {
            if (!gestoDePinca && !arrastandoProgresso && (painelChat == null || painelChat.getVisibility() != View.VISIBLE)
                    && (popup == null || !popup.isShowing())) ocultarControles();
        };
        handler.postDelayed(esconderControles, 4000L);
    }
    private void ocultarControles() {
        if (!estaAberto()) return;
        android.util.Log.d("NEXO_CINEMA_UI", "CONTROLES_OCULTOS");
        controlesVisiveis = false;
        if (barraSuperior != null) barraSuperior.setVisibility(View.GONE);
        if (barraInferior != null) barraInferior.setVisibility(View.GONE);
        if (centroControles != null) centroControles.setVisibility(View.GONE);
    }
    private void alternarChat() {
        if (painelChat == null) return;
        boolean mostrar = painelChat.getVisibility() != View.VISIBLE;
        painelChat.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        if (bolhas != null) bolhas.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        if (mostrar) mostrarControles(); else reagendarOcultar();
    }

    private void abrirEspelhamento() {
        Toast.makeText(a, "Selecione sua TV no espelhamento do Android", Toast.LENGTH_SHORT).show();
        try { a.startActivity(new Intent(Settings.ACTION_CAST_SETTINGS)); }
        catch (Exception e) {
            Toast.makeText(a, "Espelhamento não disponível neste aparelho", Toast.LENGTH_LONG).show();
        }
    }
    private void mostrarPop(View ancora, LinearLayout painel, int largura, int altura) {
        if (!estaAberto()) return;
        if (popup != null) { popup.dismiss(); popup = null; }
        popup = new PopupWindow(painel, dp(largura), altura, true);
        popup.setBackgroundDrawable(fundo(0xF4060C13, 0xAA00BFFF, 14));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(9));
        popup.setOnDismissListener(() -> { popup = null; reagendarOcultar(); });
        popup.showAtLocation(raiz, Gravity.RIGHT | Gravity.TOP, dp(14), dp(59));
        mostrarControles();
    }
    private LinearLayout caixaPop(String titulo) {
        LinearLayout caixa = new LinearLayout(a);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setPadding(dp(10), dp(10), dp(10), dp(10));
        TextView cab = texto(titulo, 13, AZUL);
        cab.setTypeface(null, android.graphics.Typeface.BOLD);
        caixa.addView(cab, new LinearLayout.LayoutParams(-1, dp(30)));
        return caixa;
    }
    private void mostrarParticipantes(View ancora) {
        LinearLayout caixa = caixaPop("PARTICIPANTES NA SALA (" + quantidadeParticipantes + ")");
        ScrollView rolavel = new ScrollView(a);
        rolavel.setVerticalScrollBarEnabled(true);
        LinearLayout lista = new LinearLayout(a);
        lista.setOrientation(LinearLayout.VERTICAL);
        if (pessoasUid.isEmpty()) {
            lista.addView(texto("Nenhum participante disponível", 12, BRANCO));
        }
        for (int i = 0; i < pessoasUid.size(); i++) {
            final String id = pessoasUid.get(i);
            final String nome = pessoasNome.get(i);
            LinearLayout item = new LinearLayout(a);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(7), dp(6), dp(7), dp(6));
            TextView avatar = texto(nome.isEmpty() ? "?" : nome.substring(0,1).toUpperCase(Locale.ROOT),
                    14, BRANCO);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(fundo(0xFF07304A, 0xAA00BFFF, 24));
            item.addView(avatar, new LinearLayout.LayoutParams(dp(34), dp(34)));
            TextView apelido = texto("  " + nome + (id.equals(liderUid) ? "  • Líder" : ""), 12, BRANCO);
            apelido.setSingleLine(true); apelido.setEllipsize(TextUtils.TruncateAt.END);
            item.addView(apelido, new LinearLayout.LayoutParams(0, -2, 1));
            TextView abrir = texto("›", 21, AZUL);
            item.addView(abrir);
            item.setOnClickListener(v -> {
                if (popup != null) popup.dismiss();
                if (perfil != null) perfil.abrir(id, ancora);
            });
            lista.addView(item);
        }
        rolavel.addView(lista);
        // Popup limitado: muitos participantes nunca cobrem o filme inteiro.
        caixa.addView(rolavel, new LinearLayout.LayoutParams(-1,
                pessoasUid.size() > 4 ? dp(220) : -2));
        TextView nota = texto("Toque no nome para ver o perfil público", 10.5f, 0xFF8FAABC);
        nota.setPadding(0, dp(8), 0, 0); caixa.addView(nota);
        mostrarPop(ancora, caixa, 295, -2);
    }
    private void itemMenu(LinearLayout caixa, String rotulo, Runnable aoClicar) {
        TextView b = texto(rotulo, 13, BRANCO);
        b.setPadding(dp(9), dp(8), dp(9), dp(8));
        b.setBackground(fundo(0xD5081721, Color.TRANSPARENT, 9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(37));
        lp.bottomMargin = dp(4);
        caixa.addView(b, lp);
        b.setOnClickListener(v -> {
            if (popup != null) popup.dismiss();
            aoClicar.run();
        });
    }
    private void mostrarAcoes(View ancora) {
        LinearLayout caixa = caixaPop("OPÇÕES DA SESSÃO");
        itemMenu(caixa, "Chat ao vivo", this::alternarChat);
        itemMenu(caixa, "Participantes e perfis", () -> mostrarParticipantes(ancora));
        if (contemTemporadas) itemMenu(caixa, "Temporadas e episódios",
                () -> { if (acoes != null) acoes.executar("temporadas"); });
        itemMenu(caixa, "Reagir", () -> { if (acoes != null) acoes.executar("reagir"); });
        itemMenu(caixa, "Convidar", () -> { if (acoes != null) acoes.executar("convidar"); });
        itemMenu(caixa, "Sincronizar", () -> { if (acoes != null) acoes.executar("sincronizar"); });
        itemMenu(caixa, "Espelhar na TV", this::abrirEspelhamento);
        itemMenu(caixa, zoomVideo < 0.96f ? "Preencher tela (sem barras)" : "Ver imagem inteira",
                () -> {
                    zoomVideo = zoomVideo < 0.96f ? 1f : 0.85f;
                    aplicarZoom();
                    mostrarIndicadorZoom();
                });
        itemMenu(caixa, "Sair da tela cheia", this::fechar);
        mostrarPop(ancora, caixa, 235, -2);
    }
    private void mostrarVolume(View ancora) {
        android.media.AudioManager audio = (android.media.AudioManager)a.getSystemService(Activity.AUDIO_SERVICE);
        if (audio == null) return;
        LinearLayout caixa = caixaPop("VOLUME DO APARELHO");
        SeekBar barra = new SeekBar(a);
        barra.setMax(audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC));
        barra.setProgress(audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC));
        barra.setProgressTintList(android.content.res.ColorStateList.valueOf(AZUL));
        barra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean usuario) {
                if (usuario) audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, p, 0);
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
        caixa.addView(barra);
        mostrarPop(ancora, caixa, 225, -2);
    }

    private void adicionarHistorico(String nome, String frase, String hora) {
        if (historico == null) return;
        boolean acompanhar = scrollHistorico == null ||
                scrollHistorico.getScrollY() + scrollHistorico.getHeight()
                        >= historico.getHeight() - dp(45);
        TextView item = texto((hora == null || hora.isEmpty() ? nome : nome + "  " + hora)
                + "\n" + frase, 12.5f, BRANCO);
        item.setPadding(dp(9), dp(7), dp(9), dp(7));
        item.setBackground(fundo(0xD31A2934, 0x4700BFFF, 13));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(6);
        historico.addView(item, p);
        while (historico.getChildCount() > 60) historico.removeViewAt(0);
        if (acompanhar && scrollHistorico != null) scrollHistorico.post(() -> {
            if (scrollHistorico != null) scrollHistorico.fullScroll(View.FOCUS_DOWN);
        });
    }
    public void carregarHistorico(List<?> mensagens) {
        if (!estaAberto() || historico == null) return;
        historico.removeAllViews();
        if (mensagens == null) return;
        for (Object o : mensagens) {
            if (!(o instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) o;
            adicionarHistorico(limitar(m.get("nome") == null ? "Amigo" : String.valueOf(m.get("nome")), 35),
                    limitar(m.get("texto") == null ? "" : String.valueOf(m.get("texto")), 900),
                    limitar(m.get("horario") == null ? "" : String.valueOf(m.get("horario")), 12));
        }
    }
    public void novaMensagem(String nomeAutor, String texto, String horario) {
        if (!estaAberto()) return;
        String nome = limitar(nomeAutor == null ? "Amigo" : nomeAutor, 26);
        String frase = limitar(texto, 170);
        adicionarHistorico(nome, frase, horario);
        if (bolhas == null || (painelChat != null && painelChat.getVisibility() == View.VISIBLE)) return;
        TextView bolha = texto(nome + "\n" + frase, 12.5f, BRANCO);
        bolha.setMaxLines(4);
        bolha.setPadding(dp(12), dp(7), dp(12), dp(7));
        bolha.setBackground(fundo(0xAD081F2B, 0xB000BFFF, 16));
        bolha.setClickable(false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(6);
        if (bolhas.getChildCount() >= 3) bolhas.removeViewAt(0);
        bolhas.addView(bolha, p);
        bolha.setAlpha(0f);
        bolha.setTranslationY(dp(42));
        bolha.animate().alpha(1f).translationY(0).setDuration(220L).start();
        handler.postDelayed(() -> {
            if (bolhas == null || bolha.getParent() != bolhas) return;
            bolha.animate().alpha(0f).translationY(-dp(82)).setDuration(2450L)
                    .withEndAction(() -> {
                        if (bolhas != null && bolha.getParent() == bolhas) bolhas.removeView(bolha);
                    }).start();
        }, 1300L);
    }
    public void fechar() { if (dialog != null && dialog.isShowing()) dialog.dismiss(); }
}
