package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.inputmethod.EditorInfo;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * NEXO WATCH PARTY — V1 Android (visual premium inspirado na referência).
 * Não altera a reprodução convencional, grupos NEXO ou a Nuvem NEXO.
 * Usa salas_have já existente; regras desse acervo precisam ser endurecidas
 * antes de uma disponibilização pública da funcionalidade.
 */
public class NexoWatchPartyActivity extends Activity {
    public static final String EXTRA_VIDEO_TITLE = "nexo_party_title";
    public static final String EXTRA_VIDEO_URL = "nexo_party_url";
    public static final String EXTRA_ROOM_ID = "nexo_party_room_id";
    public static final String EXTRA_START_MS = "nexo_party_start_ms";
    public static final String EXTRA_SERIES_ID = "nexo_party_series_id";
    public static final String EXTRA_SERIES_TITLE = "nexo_party_series_title";
    // NEXO_WATCH_CINEMA_V2 — nova tela cinema flutuante + URL automática.

    private static final int FUNDO = Color.BLACK;
    private static final int CARTAO = Color.rgb(6, 11, 16);
    private static final int BRANCO = Color.rgb(248, 250, 255);
    private static final int SUAVE = Color.rgb(166, 184, 200);
    private static final int AZUL = Color.rgb(0, 191, 255);
    private static final int ROXO = Color.rgb(0, 115, 160);
    private static final int ROSA = Color.rgb(0, 155, 220);
    private static final int VERDE = Color.rgb(48, 236, 157);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FirebaseFirestore db;
    private FirebaseUser usuario;
    private String uid;
    private String apelido;
    private DocumentReference sala;
    private String salaId;
    private boolean souLider;
    private boolean aplicandoEstadoRemoto;
    private boolean destruida;
    private boolean primeiraSala = true;
    private String ultimaMensagemRenderizada = "";
    private String ultimosParticipantes = "";
    private long ultimaReacaoVista = -1L;
    private ExoPlayer player;
    private PlayerView playerView;
    private ImageView capaDemonstrativa;
    private TextView chipSincronia;
    private TextView chipTitulo;
    private TextView chipNumero;
    private TextView chipCriador;
    private TextView reacaoVisual;
    private TextView textoVotos;
    private LinearLayout listaParticipantes;
    private LinearLayout listaMensagens;
    private ScrollView rolagemChat;
    private EditText campoMensagem;
    private TextView botaoPlay;
    private TextView botaoPronto;
    private ListenerRegistration salaListener;
    private ListenerRegistration lobbyListener;
    private String tituloSelecionado = "";
    private String urlSelecionada = "";
    private String serieIdSelecionada = "";
    private String serieTituloSelecionada = "";
    private String urlAtualPlayer = "";
    private TextView avisoCapaAtual;
    private TextView chipEpisodioAtual;
    private String textoRespondendo = "";
    private NexoWatchCinemaOverlayV2 cinema;
    private final NexoWatchMessageTracker rastreadorMensagens = new NexoWatchMessageTracker();
    private List<?> ultimasMensagens = new ArrayList<>();
    private boolean enviandoMensagem;
    private int quantidadeParticipantes = 1;
    private DocumentSnapshot ultimaSalaSnapshot;
    private boolean compactandoChat;

    private final Runnable publicadorEstado = new Runnable() {
        @Override public void run() {
            if (!destruida && sala != null && souLider && player != null) {
                if (player.isPlaying()) publicarEstado();
                handler.postDelayed(this, 6500L);
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(FUNDO);
        getWindow().setNavigationBarColor(FUNDO);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            Toast.makeText(this, "Entre na sua conta NEXO para usar as salas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        uid = usuario.getUid();
        String nome = usuario.getDisplayName();
        // Nunca exibir e-mail como identidade pública.
        if (nome == null || nome.trim().isEmpty() || nome.contains("@"))
            nome = "Usuário NEXO";
        apelido = nome.length() > 26 ? nome.substring(0, 26) : nome;
        db = FirebaseFirestore.getInstance();
        // O nome cadastrado no perfil público é a fonte de identidade.
        db.collection("perfis_publicos").document(uid).get()
                .addOnSuccessListener(perfil -> {
                    String publico = perfil.getString("nome");
                    if (publico != null && !publico.trim().isEmpty()
                            && !publico.contains("@")) {
                        apelido = publico.trim().length() > 26
                                ? publico.trim().substring(0, 26) : publico.trim();
                        if (lobbyNomePerfil != null) lobbyNomePerfil.setText(
                                nomePublicoSeguro(apelido, "Membro NEXO"));
                        String fotoPerfil = valor(perfil.getString("fotoUrl"));
                        if (!fotoPerfil.startsWith("https://"))
                            fotoPerfil = valor(perfil.getString("avatarUrl"));
                        if (lobbyPerfilAvatar != null && fotoPerfil.startsWith("https://")) {
                            lobbyPerfilAvatar.setTag(fotoPerfil);
                            lobbyCarregarImagem(fotoPerfil, lobbyPerfilAvatar);
                        }
                        if (sala != null) {
                            Map<String,Object> dadosPerfil = new HashMap<>();
                            dadosPerfil.put("nomes." + uid, apelido);
                            sala.update(dadosPerfil).addOnFailureListener(e ->
                                android.util.Log.w("NEXO_WATCH", "perfil", e));
                        }
                    }
                });
        tituloSelecionado = valor(getIntent().getStringExtra(EXTRA_VIDEO_TITLE));
        urlSelecionada = valor(getIntent().getStringExtra(EXTRA_VIDEO_URL));
        serieIdSelecionada = valor(getIntent().getStringExtra(EXTRA_SERIES_ID));
        serieTituloSelecionada = valor(getIntent().getStringExtra(EXTRA_SERIES_TITLE));
        String id = valor(getIntent().getStringExtra(EXTRA_ROOM_ID));
        if (!id.isEmpty()) entrarSala(id);
        else if (!tituloSelecionado.isEmpty()) {
            // Ao vir do catálogo, cria AUTOMATICAMENTE a sala com o vídeo do título.
            // Nunca solicita que o usuário copie/cole a URL do episódio.
            if (!urlSelecionada.startsWith("https://")) {
                toast("Não há vídeo válido cadastrado neste episódio.");
                finish();
            } else {
                String nomeSalaAutomatica="Sala " + tituloSelecionado;
                if(nomeSalaAutomatica.length()>75)
                    nomeSalaAutomatica=nomeSalaAutomatica.substring(0,75);
                criarSala(nomeSalaAutomatica,tituloSelecionado,urlSelecionada);
            }
        }
        else montarLobby();
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
    private int larguraDp() {
        return Math.round(getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density);
    }
    private String valor(String s) { return s == null ? "" : s.trim(); }
    private String txt(Object v, String fallback) {
        return v == null ? fallback : String.valueOf(v);
    }
    private GradientDrawable gradiente(int a, int b, int borda, int raio) {
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] { a, b });
        bg.setCornerRadius(dp(raio));
        bg.setStroke(dp(1), borda);
        return bg;
    }
    private GradientDrawable fundo(int cor, int borda, int raio) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cor);
        bg.setCornerRadius(dp(raio));
        if (borda != Color.TRANSPARENT) bg.setStroke(dp(1), borda);
        return bg;
    }
    private TextView texto(String conteudo, float sp, int cor, boolean forte) {
        TextView t = new TextView(this);
        t.setText(conteudo);
        t.setTextColor(cor);
        t.setTextSize(sp);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (forte) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private TextView botao(String titulo, int c1, int c2, int borda) {
        TextView t = texto(titulo, 12.5f, BRANCO, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(13), dp(10), dp(13), dp(10));
        t.setMinHeight(dp(43));
        // Contorno discreto e fundo preto; azul neon somente nas ações.
        int preenchimento = (c1 == Color.rgb(25, 145, 244) || c1 == Color.rgb(30, 90, 189))
                ? Color.rgb(0, 75, 116) : Color.rgb(8, 15, 22);
        t.setBackground(fundo(preenchimento, Color.rgb(0, 111, 157), 15));
        t.setClickable(true);
        t.setFocusable(true);
        NexoWatchIconsV2.aplicar(this,t,titulo);
        return t;
    }
    private LinearLayout coluna() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }
    private LinearLayout linha() {
        LinearLayout l = new LinearLayout(this);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }
    private LinearLayout painel(int borda) {
        LinearLayout l = coluna();
        l.setPadding(dp(13), dp(14), dp(13), dp(14));
        l.setBackground(fundo(CARTAO, Color.rgb(21, 45, 59), 17));
        l.setClipToPadding(false);
        return l;
    }
    private LinearLayout.LayoutParams margens(int width, int height, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(0, dp(top), 0, dp(bottom));
        return p;
    }
    private LinearLayout base(LinearLayout externo) {
        LinearLayout corpo = coluna();
        corpo.setPadding(dp(14), dp(13), dp(14), dp(25));
        int larguraMax = larguraDp() >= 900 ? dp(1100) : -1;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(larguraMax, -2);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        externo.addView(corpo, lp);
        return corpo;
    }
    private LinearLayout montarRolagem() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(FUNDO);
        LinearLayout externo = coluna();
        externo.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(externo, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
        return base(externo);
    }
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void erro(String acao, Exception e) {
        android.util.Log.w("NEXO_WATCH", acao, e);
        toast(acao + ": " + (e == null ? "verifique a conexão" : "verifique acesso e internet"));
    }

    private void topo(LinearLayout corpo, String titulo, String descricao) {
        LinearLayout barra = linha();
        TextView marca = texto("NEXO", 21, BRANCO, true);
        marca.setShadowLayer(dp(5), 0, 0, AZUL);
        barra.addView(marca);
        TextView voltar = botao("‹", Color.rgb(23, 28, 63), Color.rgb(27, 20, 57), AZUL);
        voltar.setTextSize(24);
        LinearLayout.LayoutParams voltaLp = new LinearLayout.LayoutParams(dp(43), dp(43));
        voltaLp.setMargins(dp(13), 0, dp(11), 0);
        barra.addView(voltar, voltaLp);
        voltar.setOnClickListener(v -> onBackPressed());
        LinearLayout textos = coluna();
        textos.addView(texto(titulo, larguraDp() < 410 ? 15 : 19, BRANCO, true));
        textos.addView(texto(descricao, 11.5f, SUAVE, false));
        barra.addView(textos, new LinearLayout.LayoutParams(0, -2, 1));
        corpo.addView(barra, margens(-1, -2, 3, 14));
    }

    // NEXO_LOBBY_RAVE_V1 — somente a interface de listagem das salas.
    private TextView lobbyNomePerfil;
    private ImageView lobbyPerfilAvatar;
    private LinearLayout lobbyListagem;
    private TextView lobbyContagem;
    private final List<DocumentSnapshot> lobbyDocumentos = new ArrayList<>();
    private String lobbyFiltro = "Todas";
    private final Map<String, TextView> lobbyBotoesFiltro = new HashMap<>();
    private final Map<String, String> lobbyFotos = new HashMap<>();
    private final java.util.Set<String> lobbyFotosBuscadas = new java.util.HashSet<>();
    private final java.util.concurrent.ExecutorService lobbyImagemPool =
            java.util.concurrent.Executors.newFixedThreadPool(3);
    private final android.util.LruCache<String, android.graphics.Bitmap> lobbyCacheImagens =
            new android.util.LruCache<>(32);
    private int lobbyConsultasPerfil = 0;

    // NEXO_CAPAS_REAIS_FALLBACK_V2 — somente artes reais da sala/catálogo.
    private final Map<String, ImageView> lobbyCapasVisiveis = new HashMap<>();
    private final Map<String, String> lobbyCapasCatalogo = new HashMap<>();
    private boolean lobbyCatalogoConsultado = false;

    // NEXO_PREVIAS_AO_VIVO_V1 — miniaturas mudas sincronizadas com a sala real.
    // Maximo de duas decodificacoes simultaneas; o restante usa a capa ate ficar visivel.
    private static final int LOBBY_MAX_PREVIAS = 2;
    private final Map<String, LobbyPrevia> lobbyPrevias = new java.util.LinkedHashMap<>();
    private String lobbyAssinaturaVisual = "";
    private ScrollView lobbyScroll;
    private final Runnable lobbyPreviewJob = this::lobbyAtualizarPrevias;

    private static class LobbyPrevia {
        final String id;
        final String url;
        final FrameLayout quadro;
        final ImageView capa;
        final PlayerView video;
        ExoPlayer motor;
        boolean falhou;

        LobbyPrevia(String id, String url, FrameLayout quadro,
                    ImageView capa, PlayerView video) {
            this.id = id;
            this.url = url;
            this.quadro = quadro;
            this.capa = capa;
            this.video = video;
        }
    }

    private void lobbyAgendarPrevias() {
        handler.removeCallbacks(lobbyPreviewJob);
        if (!destruida && sala == null && lobbyScroll != null)
            handler.postDelayed(lobbyPreviewJob, 180L);
    }

    private void lobbyPararPrevia(LobbyPrevia item) {
        if (item == null) return;
        ExoPlayer antigo = item.motor;
        item.motor = null;
        item.video.setPlayer(null);
        item.video.setAlpha(0f);
        item.video.setVisibility(View.GONE);
        if (item.capa != null) item.capa.setVisibility(View.VISIBLE);
        if (antigo != null) {
            antigo.stop();
            antigo.release();
        }
    }

    private void lobbyEncerrarPrevias() {
        handler.removeCallbacks(lobbyPreviewJob);
        for (LobbyPrevia item : lobbyPrevias.values()) lobbyPararPrevia(item);
    }

    private long lobbyPosicaoDaSala(DocumentSnapshot doc) {
        Long pos = doc.getLong("posicaoMs");
        long atual = pos == null ? 0L : Math.max(0L, pos);
        if (Boolean.TRUE.equals(doc.getBoolean("reproduzindo"))) {
            Timestamp ref = doc.getTimestamp("sincronizadoEm");
            if (ref != null) {
                long atraso = System.currentTimeMillis() - ref.toDate().getTime();
                // Mesmo limite usado na sincronizacao do player interno.
                atual += Math.max(0L, Math.min(20000L, atraso));
            }
        }
        return atual;
    }

    private String lobbyAssinaturaDados() {
        StringBuilder chave = new StringBuilder(lobbyFiltro);
        // Ignorar posicaoMs/sincronizadoEm para NAO reconstruir todos os cards
        // a cada atualizacao de reproduccao vinda do Firestore.
        for (DocumentSnapshot d : lobbyDocumentos) {
            chave.append('|').append(d.getId())
                 .append('|').append(d.get("nomeSala"))
                 .append('|').append(d.get("filmeTitulo"))
                 .append('|').append(d.get("videoUrl"))
                 .append('|').append(d.get("serieTitulo"))
                 .append('|').append(d.get("tipoConteudo"))
                 .append('|').append(d.get("participantes"))
                 .append('|').append(d.get("nomes"))
                 .append('|').append(d.get("fotos"))
                 .append('|').append(lobbyImagemDocumento(d));
        }
        return chave.toString();
    }

    private void lobbyAtualizarPrevias() {
        if (destruida || isFinishing() || sala != null || lobbyScroll == null) {
            lobbyEncerrarPrevias();
            return;
        }
        Map<String, DocumentSnapshot> documentos = new HashMap<>();
        for (DocumentSnapshot d : lobbyDocumentos) documentos.put(d.getId(), d);
        int simultaneas = 0;
        android.graphics.Rect area = new android.graphics.Rect();
        for (LobbyPrevia item : lobbyPrevias.values()) {
            DocumentSnapshot doc = documentos.get(item.id);
            boolean visivel = item.quadro.isShown()
                    && item.quadro.getGlobalVisibleRect(area)
                    && area.height() >= dp(35) && area.width() >= dp(50);
            boolean tocar = doc != null && visivel && !item.falhou
                    && simultaneas < LOBBY_MAX_PREVIAS;
            if (!tocar) {
                if (item.motor != null) lobbyPararPrevia(item);
                continue;
            }
            simultaneas++;
            boolean rodando = Boolean.TRUE.equals(doc.getBoolean("reproduzindo"));
            long alvo = lobbyPosicaoDaSala(doc);
            if (item.motor == null) {
                try {
                    ExoPlayer motor = new ExoPlayer.Builder(this).build();
                    item.motor = motor;
                    motor.setVolume(0f); // Audio absolutamente mutado no lobby.
                    item.video.setUseController(false);
                    item.video.setControllerAutoShow(false);
                    // A surface precisa estar VISIBLE para renderizar o primeiro frame.
                    // Alpha=0 evita uma tela preta sobre a capa durante o carregamento.
                    item.video.setAlpha(0f);
                    item.video.setVisibility(View.VISIBLE);
                    item.video.setPlayer(motor);
                    motor.addListener(new Player.Listener() {
                        @Override public void onRenderedFirstFrame() {
                            if (lobbyPrevias.get(item.id) == item && item.motor == motor
                                    && !destruida && sala == null) {
                                item.video.setAlpha(1f);
                                if (item.capa != null) item.capa.setVisibility(View.INVISIBLE);
                            }
                        }
                        @Override public void onPlayerError(PlaybackException erroMidia) {
                            if (item.motor == motor) {
                                item.falhou = true;
                                lobbyPararPrevia(item); // Se a URL falhar, mantem a capa.
                                android.util.Log.w("NEXO_WATCH", "Previa indisponivel " + item.id,
                                        erroMidia);
                            }
                        }
                    });
                    motor.setMediaItem(MediaItem.fromUri(item.url));
                    motor.prepare();
                    motor.seekTo(alvo);
                    motor.setPlayWhenReady(rodando);
                } catch (Exception e) {
                    item.falhou = true;
                    lobbyPararPrevia(item);
                    android.util.Log.w("NEXO_WATCH", "Falha ao abrir previa " + item.id, e);
                }
            } else {
                ExoPlayer motor = item.motor;
                if (Math.abs(motor.getCurrentPosition() - alvo) > 6500L)
                    motor.seekTo(alvo);
                if (motor.getPlayWhenReady() != rodando)
                    motor.setPlayWhenReady(rodando);
                motor.setVolume(0f);
            }
        }
    }

    private android.graphics.drawable.Drawable fundoLobby() {
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[] {Color.rgb(50, 17, 85), Color.rgb(8, 13, 42),
                        Color.rgb(5, 12, 44), Color.rgb(35, 9, 67)});
    }

    private String nomePublicoSeguro(String nome, String padrao) {
        String n = valor(nome);
        return n.isEmpty() || n.contains("@") ? padrao : n;
    }

    private TextView rotuloNeon(String valor, float tamanho, int cor, boolean negrito) {
        TextView t = texto(valor, tamanho, cor, negrito);
        t.setIncludeFontPadding(false);
        return t;
    }

    private void montarLobby() {
        // Remover a assinatura anterior ao reconstruir a tela (evita listeners duplicados).
        if (lobbyListener != null) { lobbyListener.remove(); lobbyListener = null; }
        liberarSala();
        lobbyEncerrarPrevias();
        lobbyPrevias.clear();
        lobbyAssinaturaVisual = "";
        lobbyDocumentos.clear();
        lobbyFiltro = "Todas";
        lobbyBotoesFiltro.clear();

        LinearLayout corpo = montarRolagem();
        View pai = (View) corpo.getParent();
        View rolagem = (View) pai.getParent();
        rolagem.setBackground(fundoLobby());
        lobbyScroll = (ScrollView) rolagem;
        lobbyScroll.setOnScrollChangeListener((View.OnScrollChangeListener)
                (v, x, y, anteriorX, anteriorY) -> lobbyAgendarPrevias());
        corpo.setPadding(dp(10), dp(12), dp(10), dp(35));
        montarTopoLobby(corpo);
        montarBannerLobby(corpo);
        lobbyConsultarCatalogoCapas();

        LinearLayout codigoLinha = linha();
        EditText campoCodigo = new EditText(this);
        campoCodigo.setSingleLine(true);
        campoCodigo.setTextSize(12.5f);
        campoCodigo.setTextColor(BRANCO);
        campoCodigo.setHintTextColor(SUAVE);
        campoCodigo.setHint("#  Código da sala para entrar...");
        campoCodigo.setPadding(dp(15), 0, dp(12), 0);
        campoCodigo.setBackground(gradiente(Color.rgb(18, 23, 61),
                Color.rgb(19, 22, 63), ROXO, 18));
        codigoLinha.addView(campoCodigo, new LinearLayout.LayoutParams(0, dp(50), 1));
        TextView entrar = botao("▶  ENTRAR", Color.rgb(114, 28, 201),
                Color.rgb(14, 122, 233), AZUL);
        entrar.setTextSize(12);
        LinearLayout.LayoutParams entrarP = new LinearLayout.LayoutParams(-2, dp(50));
        entrarP.setMargins(dp(8), 0, 0, 0);
        codigoLinha.addView(entrar, entrarP);
        View.OnClickListener entrarPorCodigo = v -> {
            String codigo = valor(campoCodigo.getText().toString());
            if (codigo.isEmpty()) toast("Informe o código da sala.");
            else entrarSala(codigo);
        };
        entrar.setOnClickListener(entrarPorCodigo);
        campoCodigo.setOnEditorActionListener((v, actionId, event) -> {
            entrarPorCodigo.onClick(campoCodigo);
            return true;
        });
        corpo.addView(codigoLinha, margens(-1, -2, 12, 17));

        LinearLayout tituloLinha = linha();
        TextView salasTitulo = rotuloNeon("♧  SALAS DISPONÍVEIS", 16, BRANCO, true);
        tituloLinha.addView(salasTitulo, new LinearLayout.LayoutParams(0, -2, 1));
        TextView tempoReal = rotuloNeon("●  AO VIVO", 10, VERDE, true);
        tituloLinha.addView(tempoReal);
        corpo.addView(tituloLinha, margens(-1, -2, 0, 12));

        HorizontalScrollView filtrosScroll = new HorizontalScrollView(this);
        filtrosScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout filtros = linha();
        for (String opcao : new String[]{"Todas", "Filmes", "Séries", "Amigos"}) {
            TextView b = rotuloNeon(opcao, 11.5f, BRANCO, true);
            b.setGravity(Gravity.CENTER);
            b.setMinHeight(dp(40));
            b.setPadding(dp(17), dp(9), dp(17), dp(9));
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-2, dp(42));
            fp.setMargins(0, 0, dp(8), 0);
            filtros.addView(b, fp);
            lobbyBotoesFiltro.put(opcao, b);
            b.setOnClickListener(v -> {
                lobbyFiltro = opcao;
                atualizarFiltrosLobby();
                mostrarSalasLobby();
                if ("Amigos".equals(opcao))
                    toast("Exibindo salas em que você participa ou que estão marcadas para amigos.");
            });
        }
        filtrosScroll.addView(filtros);
        corpo.addView(filtrosScroll, margens(-1, -2, 0, 13));
        atualizarFiltrosLobby();

        lobbyListagem = coluna();
        lobbyContagem = rotuloNeon("Procurando salas...", 12, SUAVE, false);
        corpo.addView(lobbyContagem, margens(-1, -2, 0, 5));
        corpo.addView(lobbyListagem);
        lobbyListagem.addView(rotuloNeon("Carregando as salas reais do NEXO...", 12, SUAVE, false));

        lobbyListener = db.collection("salas_have")
                .whereEqualTo("tipo", "NEXO_WATCH")
                .limit(30)
                .addSnapshotListener((resultado, falha) -> {
                    if (destruida || isFinishing() || sala != null || lobbyListagem == null) return;
                    lobbyDocumentos.clear();
                    if (falha != null) {
                        lobbyContagem.setText("Conexão indisponível");
                        lobbyContagem.setVisibility(View.VISIBLE);
                        lobbyListagem.removeAllViews();
                        lobbyListagem.addView(rotuloNeon(
                                "Não foi possível carregar salas. Verifique as permissões do Firebase.",
                                12.5f, SUAVE, false));
                        return;
                    }
                    if (resultado != null) for (QueryDocumentSnapshot doc : resultado)
                        if (!Boolean.FALSE.equals(doc.getBoolean("aberta"))) lobbyDocumentos.add(doc);
                    String assinaturaAtual = lobbyAssinaturaDados();
                    if (!assinaturaAtual.equals(lobbyAssinaturaVisual))
                        mostrarSalasLobby();
                    else lobbyAgendarPrevias();
                });
    }

    private void montarTopoLobby(LinearLayout corpo) {
        LinearLayout topo = linha();
        TextView voltar = botao("‹", Color.rgb(23, 26, 77), Color.rgb(45, 23, 92), ROXO);
        voltar.setTextSize(26);
        topo.addView(voltar, new LinearLayout.LayoutParams(dp(39), dp(42)));
        voltar.setOnClickListener(v -> onBackPressed());

        TextView marca = rotuloNeon("NEXO", larguraDp() < 420 ? 17 : 21, BRANCO, true);
        marca.setShadowLayer(dp(5), 0, 0, ROSA);
        LinearLayout.LayoutParams marcaP = new LinearLayout.LayoutParams(-2, -2);
        marcaP.setMargins(dp(9), 0, dp(8), 0);
        topo.addView(marca, marcaP);

        TextView divisoria = rotuloNeon("│", 24, Color.rgb(117, 107, 176), false);
        topo.addView(divisoria);
        TextView claquete = rotuloNeon("▣", 21, AZUL, true);
        LinearLayout.LayoutParams clapP = new LinearLayout.LayoutParams(-2, -2);
        clapP.setMargins(dp(5), 0, dp(6), 0);
        topo.addView(claquete, clapP);

        LinearLayout titulos = coluna();
        titulos.addView(rotuloNeon("SALAS NEXO", larguraDp() < 420 ? 12.5f : 15, BRANCO, true));
        if (larguraDp() >= 420)
            titulos.addView(rotuloNeon("Assistir filmes e séries juntos", 9, SUAVE, false));
        topo.addView(titulos, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout perfil = linha();
        perfil.setPadding(dp(5), dp(4), dp(7), dp(4));
        perfil.setBackground(gradiente(Color.rgb(12, 34, 89), Color.rgb(30, 18, 76), ROXO, 16));
        FrameLayout avatarCirculo = new FrameLayout(this);
        avatarCirculo.setBackground(gradiente(Color.rgb(6, 39, 91),
                Color.rgb(45, 16, 110), AZUL, 24));
        avatarCirculo.setClipToOutline(true);
        TextView avatar = rotuloNeon("♛", 17, AZUL, true);
        avatar.setGravity(Gravity.CENTER);
        avatarCirculo.addView(avatar, new FrameLayout.LayoutParams(-1, -1));
        lobbyPerfilAvatar = new ImageView(this);
        lobbyPerfilAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarCirculo.addView(lobbyPerfilAvatar, new FrameLayout.LayoutParams(-1, -1));
        if (usuario != null && usuario.getPhotoUrl() != null) {
            String foto = usuario.getPhotoUrl().toString();
            lobbyPerfilAvatar.setTag(foto);
            lobbyCarregarImagem(foto, lobbyPerfilAvatar);
        }
        perfil.addView(avatarCirculo, new LinearLayout.LayoutParams(dp(30), dp(30)));
        LinearLayout perfilTexto = coluna();
        lobbyNomePerfil = rotuloNeon(nomePublicoSeguro(apelido, "Membro NEXO"),
                larguraDp() < 420 ? 10.5f : 12, BRANCO, true);
        lobbyNomePerfil.setSingleLine(true);
        lobbyNomePerfil.setEllipsize(TextUtils.TruncateAt.END);
        perfilTexto.addView(lobbyNomePerfil);
        boolean adminVisual = usuario != null && usuario.isEmailVerified()
                && "lipesanderson@gmail.com".equalsIgnoreCase(valor(usuario.getEmail()));
        perfilTexto.addView(rotuloNeon(adminVisual ? "♛ ADM" : "PERFIL",
                9, AZUL, true));
        LinearLayout.LayoutParams perfilTp = new LinearLayout.LayoutParams(
                larguraDp() < 420 ? dp(58) : dp(76), -2);
        perfilTp.setMargins(dp(5), 0, 0, 0);
        perfil.addView(perfilTexto, perfilTp);
        LinearLayout.LayoutParams pf = new LinearLayout.LayoutParams(-2, -2);
        pf.setMargins(dp(5), 0, 0, 0);
        topo.addView(perfil, pf);
        corpo.addView(topo, margens(-1, -2, 0, 17));
    }

    private void montarBannerLobby(LinearLayout corpo) {
        int largura = Math.min(larguraDp() - 20, 1080);
        int altura = larguraDp() < 420 ? 206 : 224;
        FrameLayout borda = new FrameLayout(this);
        borda.setBackground(gradiente(Color.rgb(112, 35, 202),
                Color.rgb(5, 168, 246), AZUL, 23));
        borda.setPadding(dp(2), dp(2), dp(2), dp(2));
        borda.setElevation(dp(5));
        FrameLayout interior = new FrameLayout(this);
        interior.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(59, 18, 101), Color.rgb(9, 18, 58),
                        Color.rgb(17, 15, 59)}));
        interior.setClipToOutline(true);
        borda.addView(interior, new FrameLayout.LayoutParams(-1, -1));

        ImageView ilustracao = new ImageView(this);
        ilustracao.setImageResource(R.drawable.nexo_watch_banner_art);
        ilustracao.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams imagemP = new FrameLayout.LayoutParams(
                dp(Math.max(155, (int)(largura * .54f))), -1,
                Gravity.END | Gravity.CENTER_VERTICAL);
        interior.addView(ilustracao, imagemP);

        LinearLayout textos = coluna();
        textos.addView(rotuloNeon("ASSISTIR JUNTO", larguraDp() < 420 ? 21 : 24, BRANCO, true));
        TextView descricao = rotuloNeon("Crie uma sala, convide amigos e assista ao mesmo vídeo com chat e sincronia em tempo real.",
                larguraDp() < 420 ? 11.8f : 13, Color.rgb(214, 214, 241), false);
        descricao.setLineSpacing(dp(2), 1f);
        textos.addView(descricao, margens(-1, -2, 9, 0));
        FrameLayout.LayoutParams textosP = new FrameLayout.LayoutParams(
                dp((int)(largura * (larguraDp() < 420 ? .60f : .57f))), -2,
                Gravity.START | Gravity.TOP);
        textosP.setMargins(dp(17), dp(18), 0, 0);
        interior.addView(textos, textosP);

        TextView criar = botao("＋  CRIAR SALA", Color.rgb(165, 30, 238),
                Color.rgb(6, 167, 246), ROSA);
        criar.setTextSize(13.5f);
        criar.setElevation(dp(5));
        criar.setOnClickListener(v -> mostrarCriacao("", ""));
        FrameLayout.LayoutParams criarP = new FrameLayout.LayoutParams(
                dp((int)(largura * .61f)), dp(47), Gravity.START | Gravity.BOTTOM);
        criarP.setMargins(dp(14), 0, 0, dp(16));
        interior.addView(criar, criarP);
        corpo.addView(borda, margens(-1, dp(altura), 0, 0));
    }

    private void atualizarFiltrosLobby() {
        for (Map.Entry<String, TextView> f : lobbyBotoesFiltro.entrySet()) {
            boolean ativo = f.getKey().equals(lobbyFiltro);
            f.getValue().setBackground(gradiente(
                    ativo ? Color.rgb(151, 29, 240) : Color.rgb(22, 23, 65),
                    ativo ? Color.rgb(7, 159, 232) : Color.rgb(25, 29, 82),
                    ativo ? AZUL : Color.rgb(96, 83, 177), 25));
            f.getValue().setElevation(dp(ativo ? 4 : 0));
        }
    }

    private boolean lobbyCombinaFiltro(DocumentSnapshot doc) {
        if ("Todas".equals(lobbyFiltro)) return true;
        String serie = valor(doc.getString("serieTitulo"));
        String classe = valor(doc.getString("tipoConteudo"));
        boolean ehSerie = !serie.isEmpty() || "serie".equalsIgnoreCase(classe)
                || "série".equalsIgnoreCase(classe);
        if ("Séries".equals(lobbyFiltro)) return ehSerie;
        if ("Filmes".equals(lobbyFiltro)) return !ehSerie;
        List<?> pessoas = (List<?>) doc.get("participantes");
        List<?> amigos = (List<?>) doc.get("amigos");
        return (pessoas != null && pessoas.contains(uid))
                || (amigos != null && amigos.contains(uid));
    }

    private void mostrarSalasLobby() {
        if (lobbyListagem == null || destruida || isFinishing()) return;
        lobbyEncerrarPrevias();
        lobbyPrevias.clear();
        lobbyCapasVisiveis.clear();
        lobbyAssinaturaVisual = lobbyAssinaturaDados();
        lobbyListagem.removeAllViews();
        int visiveis = 0;
        for (DocumentSnapshot doc : lobbyDocumentos) {
            if (!lobbyCombinaFiltro(doc)) continue;
            visiveis++;
            montarCartaoLobby(doc, lobbyListagem);
        }
        lobbyContagem.setText(lobbyDocumentos.size() +
                (lobbyDocumentos.size() == 1 ? " sala disponível" : " salas disponíveis"));
        lobbyContagem.setVisibility(View.GONE); // O título já mostra o contexto.
        if (visiveis == 0) {
            TextView vazio = rotuloNeon(lobbyDocumentos.isEmpty()
                    ? "Ainda não há salas abertas. Crie a primeira!"
                    : "Nenhuma sala encontrada neste filtro.", 12.5f, SUAVE, false);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(dp(12), dp(28), dp(12), dp(28));
            vazio.setBackground(gradiente(CARTAO, Color.rgb(26, 18, 60), ROXO, 18));
            lobbyListagem.addView(vazio);
        }
        lobbyAgendarPrevias();
    }

    private String lobbyNormalizarTitulo(String titulo) {
        String s = valor(titulo);
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ").trim();
        if (s.startsWith("sala ")) s = s.substring(5);
        return s;
    }

    private String lobbyUrlCapa(String origem) {
        String u = valor(origem);
        if (u.isEmpty() || u.length() > 1000) return "";
        if (u.startsWith("https://")) return u;
        if (u.contains(":") || u.startsWith("/") || u.startsWith("\\")
                || u.contains("..") || !u.contains("/")) return "";
        // As capas da Nuvem NEXO podem vir como chaves R2, não como URLs.
        return "https://delicate-sunset-c8b5.lipesanderson.workers.dev/file?key="
                + android.net.Uri.encode(u);
    }

    private String lobbyCapaCampo(DocumentSnapshot doc) {
        for (String campo : new String[]{"posterUrl", "poster_url", "capaUrl", "capa_url",
                "thumbUrl", "thumbnailUrl", "imagemUrl", "imagem_url",
                "poster", "capa", "bannerUrl", "banner_url", "banner"}) {
            Object bruto = doc.get(campo);
            if (!(bruto instanceof String)) continue;
            String url = lobbyUrlCapa((String) bruto);
            if (!url.isEmpty()) return url;
        }
        return "";
    }

    private String lobbyCapaDaSalaOuCatalogo(DocumentSnapshot doc) {
        String direta = lobbyCapaCampo(doc);
        if (!direta.isEmpty()) return direta;
        // Série: procurar a capa da série, NÃO capa aleatória do episódio.
        String serie = lobbyNormalizarTitulo(doc.getString("serieTitulo"));
        if (!serie.isEmpty() && lobbyCapasCatalogo.containsKey(serie))
            return lobbyCapasCatalogo.get(serie);
        String filme = lobbyNormalizarTitulo(doc.getString("filmeTitulo"));
        if (lobbyCapasCatalogo.containsKey(filme)) return lobbyCapasCatalogo.get(filme);
        String titulo = lobbyNormalizarTitulo(doc.getString("nomeSala"));
        if (lobbyCapasCatalogo.containsKey(titulo)) return lobbyCapasCatalogo.get(titulo);
        return "";
    }

    private String lobbyImagemDocumento(DocumentSnapshot doc) {
        return lobbyCapaDaSalaOuCatalogo(doc);
    }

    private void lobbyMostrarCapaReal(ImageView imagem, String url) {
        if (imagem == null || url.isEmpty()) return;
        if (url.equals(imagem.getTag())) return;
        imagem.setTag(url);
        lobbyCarregarImagem(url, imagem);
    }

    private void lobbyLerCapas(org.json.JSONArray lista, Map<String, String> destino,
                               java.util.Set<String> nomesDuplicados) {
        if (lista == null) return;
        for (int i = 0; i < lista.length(); i++) {
            org.json.JSONObject item = lista.optJSONObject(i);
            if (item == null) continue;
            String nome = lobbyNormalizarTitulo(item.optString("titulo",
                    item.optString("title", "")));
            if (nome.isEmpty()) continue;
            String url = "";
            for (String chave : new String[]{"capa_url", "poster_url", "capa", "poster",
                    "banner_url", "banner"}) {
                url = lobbyUrlCapa(item.optString(chave, ""));
                if (!url.isEmpty()) break;
            }
            if (url.isEmpty()) continue;
            if (destino.containsKey(nome) && !destino.get(nome).equals(url)) {
                nomesDuplicados.add(nome); // evita capa errada para títulos ambíguos
            } else if (!nomesDuplicados.contains(nome)) {
                destino.put(nome, url);
            }
        }
    }

    private void lobbyConsultarCatalogoCapas() {
        if (lobbyCatalogoConsultado || lobbyImagemPool.isShutdown()) return;
        lobbyCatalogoConsultado = true;
        lobbyImagemPool.execute(() -> {
            java.net.HttpURLConnection c = null;
            try {
                java.net.URL url = new java.net.URL(
                    "https://delicate-sunset-c8b5.lipesanderson.workers.dev/file?key=dados%2Fcatalogo.json");
                c = (java.net.HttpURLConnection) url.openConnection();
                c.setConnectTimeout(9000);
                c.setReadTimeout(12000);
                if (c.getResponseCode() != 200) return;
                java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
                try (java.io.InputStream in = c.getInputStream()) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        if (b.size() + n > 5_000_000) return;
                        b.write(buf, 0, n);
                    }
                }
                org.json.JSONObject raiz = new org.json.JSONObject(b.toString("UTF-8"));
                Map<String, String> capas = new HashMap<>();
                java.util.Set<String> ambiguidade = new java.util.HashSet<>();
                lobbyLerCapas(raiz.optJSONArray("filmes"), capas, ambiguidade);
                lobbyLerCapas(raiz.optJSONArray("series"), capas, ambiguidade);
                lobbyLerCapas(raiz.optJSONArray("conteudos"), capas, ambiguidade);
                for (String repetido : ambiguidade) capas.remove(repetido);
                runOnUiThread(() -> {
                    if (destruida || isFinishing() || sala != null) return;
                    lobbyCapasCatalogo.putAll(capas);
                    for (DocumentSnapshot doc : lobbyDocumentos) {
                        ImageView foto = lobbyCapasVisiveis.get(doc.getId());
                        if (foto == null) continue;
                        String real = lobbyCapaDaSalaOuCatalogo(doc);
                        if (!real.isEmpty()) lobbyMostrarCapaReal(foto, real);
                    }
                });
            } catch (Exception e) {
                android.util.Log.d("NEXO_WATCH", "Catálogo de capas indisponível");
            } finally {
                if (c != null) c.disconnect();
            }
        });
    }

    private void montarCartaoLobby(DocumentSnapshot doc, LinearLayout destino) {
        String titulo = nomePublicoSeguro(doc.getString("nomeSala"), "Sala NEXO");
        String filme = valor(doc.getString("filmeTitulo"));
        if (filme.isEmpty()) filme = "Sessão com amigos";
        LinearLayout card = linha();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(9), dp(8), dp(9));
        card.setBackground(gradiente(Color.rgb(31, 19, 79),
                Color.rgb(9, 27, 75), Color.rgb(86, 105, 237), 19));
        card.setElevation(dp(3));
        card.setOnClickListener(v -> entrarSala(doc.getId()));
        // NEXO_V8: toque longo na sala FORA do player oferece exclusao ao criador.
        card.setLongClickable(true);
        card.setOnLongClickListener(v -> {
            mostrarMenuSalaLobby(doc);
            return true; // Nao entrar na sala ao soltar o dedo.
        });

        FrameLayout poster = new FrameLayout(this);
        poster.setBackground(gradiente(Color.rgb(44, 25, 99),
                Color.rgb(11, 41, 91), Color.rgb(80, 156, 236), 13));
        poster.setClipToOutline(true);
        TextView posterVazio = rotuloNeon("▣", 32, AZUL, true);
        posterVazio.setGravity(Gravity.CENTER);
        poster.addView(posterVazio, new FrameLayout.LayoutParams(-1, -1));
        // Capa real primeiro, sem arte de filme inventada e sem ícone de Play.
        ImageView capaReal = new ImageView(this);
        capaReal.setScaleType(ImageView.ScaleType.CENTER_CROP);
        poster.addView(capaReal, new FrameLayout.LayoutParams(-1, -1));
        lobbyCapasVisiveis.put(doc.getId(), capaReal);
        String fotoFilme = lobbyCapaDaSalaOuCatalogo(doc);
        if (!fotoFilme.isEmpty()) lobbyMostrarCapaReal(capaReal, fotoFilme);
        // A capa fica como fallback enquanto a previa silenciosa carrega.
        // O video real e exibido no lugar da imagem, sem botao de play.
        String urlPrevia = valor(doc.getString("videoUrl"));
        if (urlPrevia.startsWith("https://") && urlPrevia.length() < 2000) {
            PlayerView miniatura = new PlayerView(this);
            miniatura.setUseController(false);
            miniatura.setControllerAutoShow(false);
            miniatura.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
            miniatura.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            miniatura.setClickable(false);
            miniatura.setFocusable(false);
            miniatura.setVisibility(View.GONE);
            poster.addView(miniatura, new FrameLayout.LayoutParams(-1, -1));
            ImageView capaFallback = null;
            // O quadro/capa anterior continua visivel ate o primeiro frame.
            if (poster.getChildCount() > 1) {
                View ultimo = poster.getChildAt(poster.getChildCount() - 2);
                if (ultimo instanceof ImageView) capaFallback = (ImageView) ultimo;
            }
            lobbyPrevias.put(doc.getId(), new LobbyPrevia(
                    doc.getId(), urlPrevia, poster, capaFallback, miniatura));
        }
        int posterW = larguraDp() < 410 ? 85 : 102;
        LinearLayout.LayoutParams posterP = new LinearLayout.LayoutParams(
                dp(posterW), dp(94));
        posterP.setMargins(0, 0, dp(10), 0);
        card.addView(poster, posterP);

        LinearLayout direita = coluna();
        LinearLayout cabecalho = linha();
        TextView nome = rotuloNeon(titulo, larguraDp() < 410 ? 12.3f : 13.5f, BRANCO, true);
        nome.setMaxLines(2);
        nome.setEllipsize(TextUtils.TruncateAt.END);
        cabecalho.addView(nome, new LinearLayout.LayoutParams(0, -2, 1));
        List<?> participantes = (List<?>) doc.get("participantes");
        int n = participantes == null ? 0 : participantes.size();
        if (larguraDp() >= 440) {
            TextView contador = rotuloNeon("♧ " + n + " na sala", 9, SUAVE, false);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.setMargins(dp(4), 0, 0, 0);
            cabecalho.addView(contador, cp);
        }
        direita.addView(cabecalho);
        TextView subtitulo = rotuloNeon(filme, 10.5f, SUAVE, false);
        subtitulo.setSingleLine(true);
        subtitulo.setEllipsize(TextUtils.TruncateAt.END);
        direita.addView(subtitulo, margens(-1, -2, 3, 0));

        LinearLayout rodape = linha();
        LinearLayout fotos = linha();
        Map<?, ?> nomes = doc.get("nomes") instanceof Map ? (Map<?, ?>) doc.get("nomes") : null;
        Map<?, ?> fotosDoDoc = doc.get("fotos") instanceof Map ? (Map<?, ?>) doc.get("fotos") : null;
        if (participantes != null) for (int i = 0; i < Math.min(4, participantes.size()); i++) {
            String pessoaUid = String.valueOf(participantes.get(i));
            String nm = nomePublicoSeguro(nomes == null ? "" : txt(nomes.get(pessoaUid), ""), "Membro");
            fotos.addView(lobbyAvatar(pessoaUid, nm,
                    fotosDoDoc == null ? "" : txt(fotosDoDoc.get(pessoaUid), "")));
        }
        if (n > 4) {
            TextView mais = rotuloNeon("+" + (n - 4), 10, BRANCO, true);
            mais.setGravity(Gravity.CENTER);
            mais.setBackground(fundo(Color.rgb(31, 26, 88), ROXO, 24));
            fotos.addView(mais, new LinearLayout.LayoutParams(dp(27), dp(27)));
        }
        if (n == 0) fotos.addView(rotuloNeon("Seja o primeiro", 10, SUAVE, false));
        rodape.addView(fotos, new LinearLayout.LayoutParams(0, -2, 1));
        TextView entrar = botao("▶ ENTRAR", Color.rgb(10, 129, 242),
                Color.rgb(166, 32, 220), ROSA);
        entrar.setTextSize(10.5f);
        entrar.setPadding(dp(10), dp(7), dp(10), dp(7));
        entrar.setMinHeight(dp(33));
        entrar.setOnClickListener(v -> entrarSala(doc.getId()));
        rodape.addView(entrar, new LinearLayout.LayoutParams(-2, dp(35)));
        direita.addView(rodape, margens(-1, -2, 8, 0));
        card.addView(direita, new LinearLayout.LayoutParams(0, -2, 1));
        destino.addView(card, margens(-1, -2, 0, 12));
    }

    /** Acao de toque longo nas salas da lista, nunca em cima do filme. */
    private void mostrarMenuSalaLobby(DocumentSnapshot resumo) {
        if (resumo == null || !"NEXO_WATCH".equals(resumo.getString("tipo"))) return;
        if (!uid.equals(resumo.getString("criadorUid"))) {
            toast("Somente quem criou a sala pode apagá-la.");
            return;
        }
        final String id = resumo.getId();
        final String titulo = nomePublicoSeguro(resumo.getString("nomeSala"), "Sala NEXO");
        new AlertDialog.Builder(this)
                .setTitle("Gerenciar sala")
                .setItems(new String[]{"Abrir sala", "Apagar sala"}, (janela, escolha) -> {
                    if (escolha == 0) entrarSala(id);
                    else confirmarExclusaoSalaLobby(id, titulo);
                }).show();
    }

    private void confirmarExclusaoSalaLobby(String id, String titulo) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar esta sala?")
                .setMessage("A sala “" + titulo + "” será excluída definitivamente. "
                        + "Os participantes perderão o acesso. Esta ação não pode ser desfeita.")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("APAGAR SALA", (janela, escolha) -> {
                    DocumentReference ref = db.collection("salas_have").document(id);
                    // Verificar novamente dentro da transacao antes de apagar.
                    db.runTransaction(tx -> {
                        DocumentSnapshot atual = tx.get(ref);
                        if (!atual.exists())
                            throw new IllegalStateException("A sala já foi apagada.");
                        if (!"NEXO_WATCH".equals(atual.getString("tipo"))
                                || !uid.equals(atual.getString("criadorUid")))
                            throw new SecurityException("Somente o criador pode apagar esta sala.");
                        tx.delete(ref);
                        return null;
                    }).addOnSuccessListener(ok -> {
                        if (!destruida) toast("Sala apagada.");
                        // O listener da lista atualiza os cartoes automaticamente.
                    }).addOnFailureListener(e -> erro("Não foi possível apagar a sala", e));
                }).show();
    }

    private View lobbyAvatar(String pessoaUid, String nome, String fotoDoc) {
        FrameLayout moldura = new FrameLayout(this);
        moldura.setBackground(fundo(Color.rgb(6, 34, 50), AZUL, 30));
        moldura.setClipToOutline(true);
        TextView inicial = rotuloNeon(nome.isEmpty() ? "?"
                : nome.substring(0, 1).toUpperCase(Locale.ROOT), 11, BRANCO, true);
        inicial.setGravity(Gravity.CENTER);
        moldura.addView(inicial, new FrameLayout.LayoutParams(-1, -1));
        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setBackground(fundo(Color.TRANSPARENT, Color.TRANSPARENT, 30));
        foto.setClipToOutline(true);
        moldura.addView(foto, new FrameLayout.LayoutParams(-1, -1));
        String conhecida = lobbyFotos.get(pessoaUid);
        String fotoUrl = valor(fotoDoc);
        if (!fotoUrl.startsWith("https://")) fotoUrl = conhecida == null ? "" : conhecida;
        if (fotoUrl.startsWith("https://")) {
            foto.setTag(fotoUrl);
            lobbyCarregarImagem(fotoUrl, foto);
        } else if (!lobbyFotosBuscadas.contains(pessoaUid) && lobbyConsultasPerfil < 24) {
            lobbyFotosBuscadas.add(pessoaUid);
            lobbyConsultasPerfil++;
            db.collection("perfis_publicos").document(pessoaUid).get()
                    .addOnSuccessListener(perfil -> {
                        String url = valor(perfil.getString("fotoUrl"));
                        if (!url.startsWith("https://")) url = valor(perfil.getString("avatarUrl"));
                        lobbyFotos.put(pessoaUid, url);
                        if (url.startsWith("https://") && !destruida) {
                            foto.setTag(url);
                            lobbyCarregarImagem(url, foto);
                        }
                    })
                    .addOnFailureListener(e -> android.util.Log.d("NEXO_WATCH", "Sem foto pública"));
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(27), dp(27));
        p.setMargins(0, 0, dp(3), 0);
        moldura.setLayoutParams(p);
        return moldura;
    }

    private void lobbyCarregarImagem(String url, ImageView imagem) {
        if (!url.startsWith("https://") || url.length() > 1000) return;
        android.graphics.Bitmap pronta;
        synchronized (lobbyCacheImagens) { pronta = lobbyCacheImagens.get(url); }
        if (pronta != null) { imagem.setImageBitmap(pronta); return; }
        if (lobbyImagemPool.isShutdown()) return;
        lobbyImagemPool.execute(() -> {
            java.net.HttpURLConnection conexao = null;
            try {
                conexao = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conexao.setConnectTimeout(7000);
                conexao.setReadTimeout(7000);
                if (conexao.getResponseCode() != 200) return;
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                try (java.io.InputStream in = conexao.getInputStream()) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                        if (out.size() > 2_000_000) return;
                    }
                }
                byte[] bytes = out.toByteArray();
                android.graphics.BitmapFactory.Options opt = new android.graphics.BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt);
                if (opt.outWidth < 1 || opt.outHeight < 1) return;
                opt.inJustDecodeBounds = false;
                opt.inSampleSize = 1;
                while (opt.outWidth / opt.inSampleSize > 480 ||
                        opt.outHeight / opt.inSampleSize > 480) opt.inSampleSize *= 2;
                android.graphics.Bitmap bm = android.graphics.BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length, opt);
                if (bm == null) return;
                synchronized (lobbyCacheImagens) { lobbyCacheImagens.put(url, bm); }
                runOnUiThread(() -> {
                    if (!destruida && url.equals(imagem.getTag())) imagem.setImageBitmap(bm);
                });
            } catch (Exception ignorar) {
                android.util.Log.d("NEXO_WATCH", "Imagem indisponível");
            } finally { if (conexao != null) conexao.disconnect(); }
        });
    }

    private void mostrarCriacao(String tituloInicial, String urlInicial) {
        LinearLayout conteudo = coluna();
        conteudo.setPadding(dp(18), dp(10), dp(18), dp(4));
        final EditText nomeSala = entrada("Nome da sala (ex.: Sala Jumanji)", "Sala NEXO");
        final EditText titulo = entrada("Título do filme", tituloInicial);
        final EditText url = entrada("Link HTTPS direto do vídeo (MP4 / HLS)", urlInicial);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        conteudo.addView(nomeSala);
        conteudo.addView(titulo);
        conteudo.addView(url);
        TextView dica = texto("Escolha um filme no catálogo para preencher o vídeo automaticamente. " +
                "Link de página de streaming não funciona como URL de vídeo. " +
                "Use apenas conteúdo que você tem direito de transmitir.", 12, SUAVE, false);
        dica.setPadding(0, dp(10), 0, 0);
        conteudo.addView(dica);
        new AlertDialog.Builder(this)
                .setTitle("Criar sala • NEXO")
                .setView(conteudo)
                .setNegativeButton("Cancelar", (d, w) -> {
                    if (sala == null && tituloSelecionado.isEmpty()) montarLobby();
                    else if (sala == null) finish();
                })
                .setPositiveButton("CRIAR", (d, w) -> {
                    String link = valor(url.getText().toString());
                    if (!link.isEmpty() && !link.startsWith("https://")) {
                        toast("O vídeo precisa usar uma URL HTTPS.");
                        mostrarCriacao(titulo.getText().toString(), link);
                        return;
                    }
                    String nome = valor(nomeSala.getText().toString());
                    String filme = valor(titulo.getText().toString());
                    if (nome.isEmpty()) nome = "Sala NEXO";
                    if (filme.isEmpty()) filme = "Sessão com amigos";
                    criarSala(nome, filme, link);
                }).show();
    }

    private EditText entrada(String hint, String valorInicial) {
        EditText t = new EditText(this);
        t.setSingleLine(true);
        t.setTextSize(14);
        t.setTextColor(BRANCO);
        t.setHintTextColor(Color.rgb(150, 160, 190));
        t.setHint(hint);
        t.setText(valorInicial);
        t.setPadding(dp(4), dp(12), dp(4), dp(12));
        return t;
    }

    private void criarSala(String nome, String filme, String url) {
        DocumentReference ref = db.collection("salas_have").document();
        Map<String, Object> dados = new HashMap<>();
        dados.put("tipo", "NEXO_WATCH");
        dados.put("nomeSala", nome);
        dados.put("filmeTitulo", filme);
        dados.put("videoUrl", url);
        if (!serieTituloSelecionada.isEmpty()) {
            dados.put("serieId", serieIdSelecionada);
            dados.put("serieTitulo", serieTituloSelecionada);
            dados.put("episodioAtual", filme);
        }
        dados.put("criadorUid", uid);
        dados.put("liderUid", uid); // primeiro líder; independente da conta ADM global
        dados.put("criadorNome", apelido);
        dados.put("aberta", true);
        dados.put("participantes", Arrays.asList(uid));
        Map<String, String> nomes = new HashMap<>();
        nomes.put(uid, apelido);
        dados.put("nomes", nomes);
        dados.put("mensagens", new ArrayList<>());
        dados.put("reproduzindo", false);
        long posicaoInicial = Math.max(0L, Math.min(24L*60*60*1000,
                getIntent().getLongExtra(EXTRA_START_MS,0L)));
        dados.put("posicaoMs", posicaoInicial);
        dados.put("sincronizadoEm", FieldValue.serverTimestamp());
        dados.put("criadoEm", FieldValue.serverTimestamp());
        dados.put("votosPausa", new ArrayList<>());
        ref.set(dados).addOnSuccessListener(ok -> entrarSala(ref.getId()))
                .addOnFailureListener(e -> erro("Não foi possível criar a sala", e));
    }

    private void entrarSala(String id) {
        if (id.length() > 150 || id.contains("/")) { toast("Código de sala inválido."); return; }
        if (lobbyListener != null) { lobbyListener.remove(); lobbyListener = null; }
        lobbyEncerrarPrevias();
        db.collection("salas_have").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || !"NEXO_WATCH".equals(doc.getString("tipo"))) {
                        toast("Sala não encontrada.");
                        if (sala == null) montarLobby();
                        return;
                    }
                    if (Boolean.FALSE.equals(doc.getBoolean("aberta"))) {
                        toast("Esta sala foi encerrada.");
                        if (sala == null) montarLobby();
                        return;
                    }
                    salaId = id;
                    sala = doc.getReference();
                    primeiraSala = true;
                    // Entrada atômica: a lista preserva a ordem de chegada para sucessão.
                    DocumentReference refEntrada = sala;
                    db.runTransaction(tx -> {
                        DocumentSnapshot atual = tx.get(refEntrada);
                        if (!atual.exists() || !"NEXO_WATCH".equals(atual.getString("tipo"))
                                || Boolean.FALSE.equals(atual.getBoolean("aberta")))
                            throw new IllegalStateException("Sala não disponível");
                        List<String> pessoas = new ArrayList<>();
                        List<?> existentes = (List<?>) atual.get("participantes");
                        if (existentes != null) for (Object pessoa : existentes)
                            if (pessoa != null && !pessoas.contains(String.valueOf(pessoa)))
                                pessoas.add(String.valueOf(pessoa));
                        if (!pessoas.contains(uid)) pessoas.add(uid);
                        String lider = atual.getString("liderUid");
                        if (lider == null || !pessoas.contains(lider)) lider = pessoas.get(0);
                        Map<String,Object> mudancas = new HashMap<>();
                        mudancas.put("participantes", pessoas);
                        mudancas.put("liderUid", lider);
                        Map<String,Object> nomes = (Map<String,Object>) atual.get("nomes");
                        nomes = nomes == null ? new HashMap<>() : new HashMap<>(nomes);
                        nomes.put(uid, apelido);
                        mudancas.put("nomes", nomes);
                        tx.update(refEntrada, mudancas);
                        return null;
                    }).addOnSuccessListener(ignorado -> {
                        if (destruida || sala != refEntrada) return;
                        refEntrada.get().addOnSuccessListener(atual -> {
                            if (destruida || sala != refEntrada || !atual.exists()) return;
                            souLider = uid.equals(txt(atual.get("liderUid"),
                                    txt(atual.get("criadorUid"), "")));
                            construirSala(atual);
                            salaListener = refEntrada.addSnapshotListener((snapshot, erro) -> {
                                if (destruida || sala != refEntrada) return;
                                if (erro != null) { erro("Falha na sincronia da sala", erro); return; }
                                if (snapshot == null || !snapshot.exists()) {
                                    toast("A sala foi encerrada."); montarLobby(); return;
                                }
                                atualizarSala(snapshot);
                            });
                        }).addOnFailureListener(e -> erro("Erro ao carregar sala", e));
                    }).addOnFailureListener(e -> {
                        erro("Não foi possível entrar na sala", e);
                        if (sala == refEntrada) { sala = null; salaId = null; montarLobby(); }
                    });
                }).addOnFailureListener(e -> erro("Erro ao entrar na sala", e));
    }

    private void construirSala(DocumentSnapshot dados) {
        if (lobbyListener != null) { lobbyListener.remove(); lobbyListener = null; }
        LinearLayout corpo = montarRolagem();
        String nome = txt(dados.get("nomeSala"), "Sala NEXO");
        topo(corpo, nome.toUpperCase(Locale.ROOT), "Assistindo com amigos");

        // Ações rápidas à direita do título, refluem no mobile.
        HorizontalScrollView menuScroll = new HorizontalScrollView(this);
        menuScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout botoesTopo = linha();
        TextView convite = botao("♧  CONVIDAR", Color.rgb(22, 42, 92), Color.rgb(25, 52, 104), AZUL);
        convite.setOnClickListener(v -> convidar());
        TextView ajustes = botao("⚙  AJUSTES", Color.rgb(30, 32, 71), Color.rgb(27, 22, 59), ROXO);
        ajustes.setOnClickListener(v -> ajustesSala());
        TextView sair = botao("↪  SAIR", Color.rgb(45, 14, 69), Color.rgb(23, 12, 40), ROSA);
        sair.setOnClickListener(v -> confirmarSaida());
        for (TextView b : new TextView[]{convite, ajustes, sair}) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(44));
            p.setMargins(0, 0, dp(9), 0);
            botoesTopo.addView(b, p);
        }
        menuScroll.addView(botoesTopo);
        corpo.addView(menuScroll, margens(-1, dp(48), 0, 10));

        FrameLayout moldura = new FrameLayout(this);
        moldura.setBackgroundColor(Color.BLACK);
        moldura.setPadding(0,0,0,0);
        moldura.setElevation(0);
        FrameLayout areaPlayer = new FrameLayout(this);
        areaPlayer.setBackgroundColor(Color.BLACK);
        areaPlayer.setClipToOutline(false);
        moldura.addView(areaPlayer, new FrameLayout.LayoutParams(-1, -1));
        playerView = new PlayerView(this);
        playerView.setUseController(false);
        playerView.setControllerAutoShow(false);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setKeepScreenOn(true);
        areaPlayer.addView(playerView, new FrameLayout.LayoutParams(-1, -1));
        playerView.setUseController(false);
        // O vídeo permanece na sala. Apenas o botão [ ] abre a tela cheia.

        capaDemonstrativa = new ImageView(this);
        // Fundo neutro de espera — nunca exibir um filme de exemplo como se fosse o selecionado.
        capaDemonstrativa.setImageDrawable(fundo(Color.BLACK,Color.TRANSPARENT,0));
        capaDemonstrativa.setScaleType(ImageView.ScaleType.CENTER_CROP);
        areaPlayer.addView(capaDemonstrativa, new FrameLayout.LayoutParams(-1, -1));
        TextView avisoCapa = texto("▶  AGUARDANDO SESSÃO", 13, BRANCO, true);
        avisoCapaAtual = avisoCapa;
        avisoCapa.setGravity(Gravity.CENTER);
        avisoCapa.setBackgroundColor(Color.argb(135, 3, 6, 22));
        areaPlayer.addView(avisoCapa, new FrameLayout.LayoutParams(-1, -1));
        avisoCapa.setTag("overlay");
        reacaoVisual = texto("", 39, BRANCO, true);
        reacaoVisual.setVisibility(View.GONE);
        reacaoVisual.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams reactionLp = new FrameLayout.LayoutParams(dp(72), dp(72), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        reactionLp.setMargins(0, 0, dp(18), 0);
        areaPlayer.addView(reacaoVisual, reactionLp);

        chipSincronia = texto("●  SINCRONIZANDO", 11, VERDE, true);
        chipSincronia.setPadding(dp(11), dp(7), dp(11), dp(7));
        chipSincronia.setBackground(fundo(Color.argb(225, 9, 20, 41), AZUL, 30));
        FrameLayout.LayoutParams sinLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.LEFT);
        sinLp.setMargins(dp(12), dp(12), 0, 0);
        areaPlayer.addView(chipSincronia, sinLp);

        // NEXO_WATCH_CINEMA_V2 — player cinema sem borda/corte + chat pop.
        ImageView telaCheia = new ImageView(this);
        telaCheia.setImageDrawable(NexoWatchIconsV2.icon("fullscreen",Color.WHITE));
        telaCheia.setPadding(dp(11),dp(11),dp(11),dp(11));
        telaCheia.setContentDescription("Assistir em tela cheia com chat flutuante");
        telaCheia.setBackground(fundo(0xE6000000,AZUL,15));
        FrameLayout.LayoutParams telaLp=new FrameLayout.LayoutParams(
                dp(45),dp(45),Gravity.TOP|Gravity.RIGHT);
        telaLp.setMargins(0,dp(10),dp(10),0);
        areaPlayer.addView(telaCheia,telaLp);
        telaCheia.setOnClickListener(v->abrirCinema());
        playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);

        int conteudoLargura = Math.min(larguraDp() - 28, 1100);
        int alturaPlayer = Math.round(dp(conteudoLargura * 9f / 16f));
        corpo.addView(moldura, margens(-1, alturaPlayer, 0, 14));

        LinearLayout informacoes = coluna();
        informacoes.setPadding(dp(5),dp(7),dp(5),dp(9));
        chipTitulo = texto(nome, 17, BRANCO, true);
        informacoes.addView(chipTitulo);
        chipCriador = texto("Líder da sala", 12, SUAVE, false);
        chipNumero = texto("◉  1 participante", 12, AZUL, true);
        informacoes.addView(chipCriador, margens(-1, -2, 5, 0));
        informacoes.addView(chipNumero, margens(-1, -2, 5, 0));
        String serieAtual = valor(dados.getString("serieTitulo"));
        if (!serieAtual.isEmpty()) {
            chipEpisodioAtual = texto("EPISÓDIO ATUAL  •  " +
                    txt(dados.get("episodioAtual"), ""), 12, AZUL, true);
            informacoes.addView(chipEpisodioAtual, margens(-1,-2,8,0));
            TextView episodiosBotao = botao(souLider
                    ? "TEMPORADAS E EPISÓDIOS  •  GERENCIAR"
                    : "TEMPORADAS E EPISÓDIOS  •  VER",
                    Color.rgb(25,45,100),Color.rgb(68,25,122),ROXO);
            episodiosBotao.setOnClickListener(v -> abrirTemporadas());
            informacoes.addView(episodiosBotao, margens(-1,dp(46),12,0));
        }
        corpo.addView(informacoes, margens(-1, -2, 0, 15));

        // Tablet: participantes e chat em colunas. Celular: empilhados.
        boolean tablet = larguraDp() >= 680;
        LinearLayout conteudos = new LinearLayout(this);
        conteudos.setOrientation(tablet ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        LinearLayout esquerda = coluna();
        LinearLayout direita = coluna();
        if (tablet) {
            LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, -2, 0.42f);
            a.setMargins(0, 0, dp(12), 0);
            conteudos.addView(esquerda, a);
            conteudos.addView(direita, new LinearLayout.LayoutParams(0, -2, 0.58f));
        } else {
            conteudos.addView(direita, new LinearLayout.LayoutParams(-1, -2));
            conteudos.addView(esquerda, margens(-1, -2, 12, 0));
        }
        corpo.addView(conteudos);

        LinearLayout participantes = painel(AZUL);
        participantes.addView(texto("PARTICIPANTES", 16, BRANCO, true));
        listaParticipantes = linha();
        HorizontalScrollView rolagemPessoas = new HorizontalScrollView(this);
        rolagemPessoas.setHorizontalScrollBarEnabled(false);
        rolagemPessoas.addView(listaParticipantes);
        participantes.addView(rolagemPessoas, margens(-1, -2, 8, 0));
        esquerda.addView(participantes);

        LinearLayout chat = painel(AZUL);
        chat.addView(texto("CHAT AO VIVO", 17, BRANCO, true));
        rolagemChat = new ScrollView(this);
        rolagemChat.setFillViewport(false);
        rolagemChat.setVerticalScrollBarEnabled(true);
        listaMensagens = coluna();
        listaMensagens.setPadding(0, dp(8), 0, dp(4));
        rolagemChat.addView(listaMensagens);
        chat.addView(rolagemChat, margens(-1, dp(tablet ? 220 : 165), 10, 5));
        LinearLayout enviarLinha = linha();
        campoMensagem = new EditText(this);
        campoMensagem.setHint("Digite uma mensagem...");
        campoMensagem.setHintTextColor(SUAVE);
        campoMensagem.setTextColor(BRANCO);
        campoMensagem.setTextSize(13);
        campoMensagem.setSingleLine(true);
        campoMensagem.setImeOptions(EditorInfo.IME_ACTION_SEND);
        campoMensagem.setMinHeight(dp(44));
        campoMensagem.setPadding(dp(11), dp(8), dp(11), dp(8));
        campoMensagem.setBackground(fundo(Color.rgb(8, 19, 29), Color.rgb(33, 90, 116), 18));
        enviarLinha.addView(campoMensagem, new LinearLayout.LayoutParams(0, -2, 1));
        TextView enviar = botao("➤", Color.rgb(30, 90, 189), Color.rgb(111, 27, 195), AZUL);
        LinearLayout.LayoutParams enviarP = new LinearLayout.LayoutParams(dp(52), dp(47));
        enviarP.setMargins(dp(8), 0, 0, 0);
        enviarLinha.addView(enviar, enviarP);
        enviar.setOnClickListener(v -> enviarMensagem());
        campoMensagem.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_SEND) { enviarMensagem(); return true; }
            return false;
        });
        chat.addView(enviarLinha, margens(-1, -2, 8, 0));
        direita.addView(chat);

        HorizontalScrollView acoesScroll = new HorizontalScrollView(this);
        acoesScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout acoes = linha();
        TextView reagir = botao("♡  REAGIR", Color.rgb(39, 29, 88), Color.rgb(61, 24, 100), ROSA);
        TextView microfone = botao("♩  MICROFONE", Color.rgb(24, 30, 75), Color.rgb(31, 24, 67), ROXO);
        TextView camera = botao("▣  CÂMERA", Color.rgb(24, 30, 75), Color.rgb(31, 24, 67), ROXO);
        botaoPronto = botao("▶  PRONTO PARA ASSISTIR", Color.rgb(25, 145, 244), Color.rgb(133, 27, 235), ROSA);
        for (TextView btn : new TextView[] { reagir, microfone, camera, botaoPronto }) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(49));
            p.setMargins(0, 0, dp(8), 0);
            acoes.addView(btn, p);
        }
        reagir.setOnClickListener(v -> escolherReacao());
        microfone.setOnClickListener(v -> toast("Microfone em grupo: requer integração de chamadas em tempo real."));
        camera.setOnClickListener(v -> toast("Câmera em grupo: requer integração de chamadas em tempo real."));
        botaoPronto.setOnClickListener(v -> {
            if (!souLider) { toast("Somente o líder controla a reprodução."); return; }
            if (player == null) { toast("Escolha um vídeo para começar."); return; }
            if (player.isPlaying()) player.pause(); else player.play();
            publicarEstado();
        });
        acoesScroll.addView(acoes);
        corpo.addView(acoesScroll, margens(-1, dp(52), 17, 12));

        // As acoes secundarias ficam em um pop-up compacto em vez de varias caixas.
        TextView opcoes = botao("CONTROLES  ·  CHAT  ·  PARTICIPANTES  ·  SINCRONIA",
                Color.rgb(8,22,33), Color.rgb(8,22,33), AZUL);
        opcoes.setOnClickListener(v -> mostrarOpcoesSala());
        corpo.addView(opcoes, margens(-1, dp(42), 6, 12));

        String url = valor(dados.getString("videoUrl"));
        if (!url.isEmpty() && url.startsWith("https://")) {
            iniciarPlayer(url, avisoCapa);
            Long inicio = dados.getLong("posicaoMs");
            if(inicio != null && inicio > 0 && player != null)player.seekTo(inicio);
        } else {
            chipSincronia.setText("●  AGUARDANDO VÍDEO");
            if (souLider) avisoCapa.setOnClickListener(v -> escolherVideo());
        }
        if (souLider) {
            handler.removeCallbacks(publicadorEstado);
            handler.postDelayed(publicadorEstado, 6500L);
        }
    }

    private void iniciarPlayer(String videoUrl, TextView aviso) {
        if (player != null) { playerView.setPlayer(null); player.release(); }
        if (!videoUrl.startsWith("https://")) { toast("Somente vídeos HTTPS são aceitos."); return; }
        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10_000L)
                .setSeekForwardIncrementMs(10_000L)
                .build();
        urlAtualPlayer = videoUrl;
        playerView.setPlayer(player);
        playerView.setUseController(false);
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int estado) {
                if (estado == Player.STATE_READY) {
                    capaDemonstrativa.setVisibility(View.GONE);
                    aviso.setVisibility(View.GONE);
                }
            }
            @Override public void onIsPlayingChanged(boolean estaTocando) {
                if (chipSincronia != null)
                    chipSincronia.setVisibility(estaTocando ? View.GONE : View.VISIBLE);
                if (souLider && !aplicandoEstadoRemoto) publicarEstado();
                if (botaoPronto != null) botaoPronto.setText(estaTocando ? "PAUSAR SESSÃO" : "PRONTO PARA ASSISTIR");
                NexoWatchIconsV2.aplicar(NexoWatchPartyActivity.this,botaoPronto,botaoPronto.getText().toString());
            }
            @Override public void onPlayerError(PlaybackException e) {
                chipSincronia.setText("●  ERRO NA MÍDIA");
                toast("Vídeo não disponível. Confira o link e as permissões de reprodução.");
            }
        });
        player.setMediaItem(MediaItem.fromUri(videoUrl));
        player.prepare();
        // Aguarde o estado do Firestore; não reproduza antes da sincronização.
        player.setPlayWhenReady(false);
    }

    private void atualizarSala(DocumentSnapshot doc) {
        if (chipTitulo == null) return;
        ultimaSalaSnapshot = doc;
        String titulo = txt(doc.get("nomeSala"), "Sala NEXO");
        String liderAtual = txt(doc.get("liderUid"), txt(doc.get("criadorUid"), ""));
        boolean novoLider = uid.equals(liderAtual);
        if (souLider != novoLider) {
            souLider = novoLider;
            if (cinema != null) cinema.atualizarLider(souLider);
            if (botaoPronto != null) botaoPronto.setText(souLider
                    ? "PRONTO PARA ASSISTIR" : "ASSISTINDO COM O LÍDER");
            handler.removeCallbacks(publicadorEstado);
            if (souLider) handler.postDelayed(publicadorEstado, 6500L);
        }
        String criador = txt(doc.get("criadorNome"), "Criador");
        chipTitulo.setText(titulo);
        chipCriador.setText(souLider ? "VOCÊ É O LÍDER DA SALA"
                : "LÍDER DA SALA  •  " + nomeLider(doc, liderAtual, criador));
        if (chipEpisodioAtual != null)
            chipEpisodioAtual.setText("EPISÓDIO ATUAL  •  " +
                    txt(doc.get("episodioAtual"), titulo));
        // Troca real da mídia no MESMO documento/sala para todos os participantes.
        String novaUrl = valor(doc.getString("videoUrl"));
        if (!novaUrl.isEmpty() && novaUrl.startsWith("https://")
                && !novaUrl.equals(urlAtualPlayer) && playerView != null) {
            if (cinema != null) { cinema.fechar(); cinema = null; }
            iniciarPlayer(novaUrl, avisoCapaAtual);
        }
        List<?> pessoas = (List<?>) doc.get("participantes");
        quantidadeParticipantes = pessoas == null ? 0 : pessoas.size();
        chipNumero.setText("•  " + quantidadeParticipantes + " participantes na sala");
        if (cinema != null) atualizarPessoasCinema(doc);
        Object participanteObj = doc.get("participantes");
        String listaAtual = String.valueOf(participanteObj)
                + String.valueOf(doc.get("nomes")) + liderAtual;
        if (!listaAtual.equals(ultimosParticipantes)) {
            ultimosParticipantes = listaAtual;
            desenharParticipantes(doc);
        }
        List<?> mensagensAtuais = (List<?>) doc.get("mensagens");
        if (mensagensAtuais != null && mensagensAtuais.size() > 80 && souLider
                && !compactandoChat && sala != null) compactarChat();
        List<Map<?, ?>> novas = rastreadorMensagens.atualizar(mensagensAtuais);
        ultimasMensagens = mensagensAtuais == null ? new ArrayList<>() : new ArrayList<>(mensagensAtuais);
        String listaMens = String.valueOf(mensagensAtuais);
        if (!listaMens.equals(ultimaMensagemRenderizada)) {
            ultimaMensagemRenderizada = listaMens;
            desenharMensagens(doc);
        }
        if (cinema != null && cinema.estaAberto()) for (Map<?, ?> m : novas) {
            cinema.novaMensagem(txt(m.get("nome"), "Amigo"),
                    txt(m.get("texto"), ""), txt(m.get("horario"), ""));
        }
        List<?> votos = (List<?>) doc.get("votosPausa");
        if (textoVotos != null) textoVotos.setText((votos == null ? 0 : votos.size()) + " votos para pausar");
        Map<String, Object> reacao = (Map<String, Object>) doc.get("reacaoUltima");
        if (reacao != null && reacao.get("ts") instanceof Number) {
            long nova = ((Number) reacao.get("ts")).longValue();
            if (nova != ultimaReacaoVista) {
                ultimaReacaoVista = nova;
                if (!primeiraSala) animarReacao(txt(reacao.get("emoji"), "♡"));
            }
        }
        primeiraSala = false;
        if (!Boolean.FALSE.equals(doc.getBoolean("aberta"))) aplicarSincronia(doc);
        else { toast("O criador encerrou a sessão."); montarLobby(); }
    }

    private String nomeLider(DocumentSnapshot doc, String lider, String criador) {
        Map<String,Object> nomes = (Map<String,Object>) doc.get("nomes");
        return nomes != null ? txt(nomes.get(lider), criador) : criador;
    }

    private void atualizarPessoasCinema(DocumentSnapshot doc) {
        if (cinema == null) return;
        List<?> ids = (List<?>) doc.get("participantes");
        Map<String,Object> nomes = (Map<String,Object>)doc.get("nomes");
        String lider = txt(doc.get("liderUid"), txt(doc.get("criadorUid"), ""));
        cinema.atualizarPessoas(ids, nomes, lider);
    }

    private void desenharParticipantes(DocumentSnapshot doc) {
        if (listaParticipantes == null) return;
        listaParticipantes.removeAllViews();
        List<?> ids = (List<?>)doc.get("participantes");
        Map<String,Object> nomes = (Map<String,Object>)doc.get("nomes");
        String lider = txt(doc.get("liderUid"), txt(doc.get("criadorUid"), ""));
        if (ids == null) return;
        for (Object obj : ids) {
            final String id = String.valueOf(obj);
            final String nome = nomes == null ? "Membro NEXO" : txt(nomes.get(id), "Membro NEXO");
            LinearLayout tile = coluna();
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            View retrato = lobbyAvatar(id, nome, "");
            tile.addView(retrato, new LinearLayout.LayoutParams(dp(43), dp(43)));
            TextView rotulo = texto(nome.length()>13 ? nome.substring(0,12)+"…" : nome,
                    10.5f, BRANCO, false);
            rotulo.setGravity(Gravity.CENTER);
            rotulo.setMaxLines(1);
            tile.addView(rotulo);
            TextView cargo = texto(id.equals(lider) ? "LÍDER" : "NA SALA", 9,
                    id.equals(lider) ? AZUL : VERDE, true);
            cargo.setGravity(Gravity.CENTER);
            tile.addView(cargo);
            tile.setPadding(dp(5), dp(3), dp(5), dp(3));
            tile.setOnClickListener(v -> abrirPerfilPublico(id, tile));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(91), dp(87));
            p.rightMargin = dp(5);
            listaParticipantes.addView(tile, p);
        }
    }

    /** Perfil publico em popup: nome, identificador, descricao e foto cadastrados. */
    private void abrirPerfilPublico(String membroUid, View ancora) {
        if (membroUid == null || membroUid.isEmpty() || db == null) return;
        LinearLayout painel = coluna();
        painel.setPadding(dp(15), dp(13), dp(15), dp(13));
        painel.setBackground(fundo(0xF5060C14, AZUL, 16));
        TextView titulo = texto("PERFIL PÚBLICO", 13, AZUL, true);
        painel.addView(titulo);
        FrameLayout avatar = new FrameLayout(this);
        avatar.setBackground(fundo(0xFF07314A, AZUL, 32));
        avatar.setClipToOutline(true);
        TextView inicial = texto("N", 20, BRANCO, true);
        inicial.setGravity(Gravity.CENTER);
        avatar.addView(inicial, new FrameLayout.LayoutParams(-1, -1));
        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        foto.setClipToOutline(true);
        avatar.addView(foto, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(56), dp(56));
        alp.gravity = Gravity.CENTER_HORIZONTAL;
        alp.topMargin = dp(9);alp.bottomMargin = dp(9);
        painel.addView(avatar, alp);
        TextView nome = texto("Carregando perfil...", 15, BRANCO, true);
        nome.setGravity(Gravity.CENTER);
        painel.addView(nome);
        TextView handle = texto("", 12, AZUL, false);
        handle.setGravity(Gravity.CENTER);
        painel.addView(handle);
        TextView descricao = texto("", 12, SUAVE, false);
        descricao.setGravity(Gravity.CENTER);
        descricao.setPadding(0,dp(7),0,0);
        painel.addView(descricao);
        PopupWindow pop = new PopupWindow(painel, dp(288), -2, true);
        pop.setBackgroundDrawable(fundo(0xF5060C14, AZUL, 16));
        pop.setOutsideTouchable(true);
        pop.setElevation(dp(9));
        // Usar a janela do cinema quando o perfil vier da tela cheia.
        View janela = ancora != null ? ancora : getWindow().getDecorView();
        pop.showAtLocation(janela, Gravity.CENTER, 0, 0);
        db.collection("perfis_publicos").document(membroUid).get()
                .addOnSuccessListener(perfil -> {
                    if (!pop.isShowing()) return;
                    String n = valor(perfil.getString("nome"));
                    if (n.isEmpty() || n.contains("@")) n = "Membro NEXO";
                    nome.setText(n);
                    inicial.setText(n.substring(0,1).toUpperCase(Locale.ROOT));
                    String h = valor(perfil.getString("handle"));
                    if (!h.isEmpty()) handle.setText(h.startsWith("@") ? h : "@" + h);
                    String d = valor(perfil.getString("descricao"));
                    descricao.setText(d.isEmpty() ? "Perfil sem descrição pública" : d);
                    String url = valor(perfil.getString("fotoUrl"));
                    if (!url.startsWith("https://")) url = valor(perfil.getString("avatarUrl"));
                    if (url.startsWith("https://")) {
                        foto.setTag(url);
                        lobbyCarregarImagem(url, foto);
                    }
                }).addOnFailureListener(e -> {
                    if (pop.isShowing()) nome.setText("Perfil indisponível");
                });
    }

    private void mostrarOpcoesSala() {
        String[] escolhas = {"Chat", "Participantes e perfis", "Reagir", "Votar para pausar",
                "Convidar", "Sincronizar", "Tela cheia [ ]"};
        new AlertDialog.Builder(this).setTitle("Opções da sala")
                .setItems(escolhas, (d, i) -> {
                    switch(i) {
                        case 0: if (campoMensagem!=null) campoMensagem.requestFocus(); break;
                        case 1: if (listaParticipantes!=null) listaParticipantes.requestFocus(); break;
                        case 2: escolherReacao(); break;
                        case 3: votarPausa(); break;
                        case 4: convidar(); break;
                        case 5: sincronizarManual(); break;
                        case 6: abrirCinema(); break;
                    }
                }).show();
    }

    private void desenharMensagens(DocumentSnapshot doc) {
        if (listaMensagens == null) return;
        boolean acompanhar = rolagemChat != null &&
                rolagemChat.getScrollY() + rolagemChat.getHeight()
                    >= listaMensagens.getHeight() - dp(55);
        listaMensagens.removeAllViews();
        List<?> mensagens = (List<?>) doc.get("mensagens");
        if (mensagens == null || mensagens.isEmpty()) {
            listaMensagens.addView(texto("Conversem aqui enquanto assistem.", 12, SUAVE, false));
            return;
        }
        for (Object msg : mensagens) {
            if (!(msg instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) msg;
            boolean minha = uid.equals(txt(m.get("uid"), ""));
            LinearLayout bolha = coluna();
            bolha.setPadding(dp(10), dp(9), dp(10), dp(9));
            bolha.setBackground(fundo(minha ? Color.rgb(11, 34, 49) : Color.rgb(16, 27, 35),
                    minha ? Color.rgb(0, 108, 158) : Color.rgb(27, 56, 72), 15));
            String horario = txt(m.get("horario"), "");
            bolha.addView(texto(txt(m.get("nome"), "Amigo") + "  " + horario,
                    11, minha ? AZUL : SUAVE, true));
            String citacao = txt(m.get("resposta"), "");
            if (!citacao.isEmpty()) {
                TextView citada = texto("↩ " + citacao, 11, SUAVE, false);
                citada.setPadding(dp(5), dp(4), dp(5), dp(4));
                citada.setBackground(fundo(Color.rgb(17, 24, 51), ROXO, 8));
                bolha.addView(citada, margens(-1, -2, 5, 2));
            }
            String conteudo = txt(m.get("texto"), "");
            bolha.addView(texto(conteudo, 13.5f, BRANCO, false), margens(-1, -2, 4, 0));
            bolha.setOnLongClickListener(v -> {
                textoRespondendo = conteudo.length() > 80 ? conteudo.substring(0, 80) : conteudo;
                campoMensagem.setHint("↩ Respondendo: " + textoRespondendo);
                campoMensagem.requestFocus();
                return true;
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.setMargins(minha ? dp(35) : 0, dp(4), minha ? 0 : dp(35), dp(5));
            listaMensagens.addView(bolha, p);
        }
        if (acompanhar) rolagemChat.post(() -> {
            if (rolagemChat != null) rolagemChat.fullScroll(View.FOCUS_DOWN);
        });
    }

    private void abrirCinema() {
        if (player==null || playerView==null || sala==null) {
            toast("O vídeo ainda não está carregado nesta sala.");return;
        }
        if (cinema!=null && cinema.estaAberto()) return;
        String nome=chipTitulo==null?"Sala NEXO":chipTitulo.getText().toString();
        if (ultimaSalaSnapshot != null) {
            String tituloMidia = valor(ultimaSalaSnapshot.getString("filmeTitulo"));
            if (!tituloMidia.isEmpty()) nome = tituloMidia;
        }
        boolean salaDeSerie = ultimaSalaSnapshot != null &&
                !valor(ultimaSalaSnapshot.getString("serieTitulo")).isEmpty();
        cinema = new NexoWatchCinemaOverlayV2(this, player, playerView, souLider,
                nome, salaDeSerie,
                (mensagem, resultado) -> enviarMensagemTexto(mensagem, resultado),
                this::publicarEstado, this::abrirPerfilPublico,
                acao -> {
                    if ("reagir".equals(acao)) escolherReacao();
                    else if ("convidar".equals(acao)) convidar();
                    else if ("sincronizar".equals(acao)) sincronizarManual();
                    else if ("temporadas".equals(acao)) {
                        if (cinema != null) cinema.fechar();
                        abrirTemporadas();
                    }
                });
        cinema.abrir();
        cinema.atualizarParticipantes(quantidadeParticipantes);
        cinema.carregarHistorico(ultimasMensagens);
        if (ultimaSalaSnapshot != null) atualizarPessoasCinema(ultimaSalaSnapshot);
    }

    private void enviarMensagem() {
        if (campoMensagem == null) return;
        final String texto = valor(campoMensagem.getText().toString());
        if (texto.isEmpty()) return;
        campoMensagem.setText(""); // resposta visual imediata, sem esperar a rede
        enviarMensagemTexto(texto, sucesso -> {
            if (!sucesso && campoMensagem != null && campoMensagem.getText().length() == 0) {
                campoMensagem.setText(texto);
                campoMensagem.setSelection(campoMensagem.length());
            }
        });
    }

    /** Gravação atômica sem transação de leitura; Firestore atualiza o chat local na hora. */
    private void enviarMensagemTexto(String msg, NexoWatchCinemaOverlayV2.Resultado resultado) {
        if (sala == null || msg == null || msg.trim().isEmpty()) {
            resultado.concluido(false); return;
        }
        msg = msg.trim();
        if (msg.length() > 900) {
            toast("Mensagem muito longa (máximo 900 caracteres).");
            resultado.concluido(false); return;
        }
        Map<String,Object> item = new HashMap<>();
        item.put("id", java.util.UUID.randomUUID().toString());
        item.put("uid", uid);
        item.put("nome", apelido);
        item.put("texto", msg);
        item.put("horario", new java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new java.util.Date()));
        item.put("resposta", textoRespondendo);
        item.put("ts", System.currentTimeMillis());
        textoRespondendo = "";
        if (campoMensagem != null) campoMensagem.setHint("Digite uma mensagem...");
        // arrayUnion não precisa ler documento nem bloquear outras mensagens simultâneas.
        // O listener da sala renderiza os dados locais antes da confirmação do servidor.
        sala.update("mensagens", FieldValue.arrayUnion(item))
                .addOnSuccessListener(ignorado -> {
                    android.util.Log.d("NEXO_CHAT", "MENSAGEM_CONFIRMADA_FIRESTORE");
                    resultado.concluido(true);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.w("NEXO_CHAT", "MENSAGEM_FALHOU_FIRESTORE", e);
                    erro("Não foi possível enviar mensagem", e);
                    resultado.concluido(false);
                });
        // A interface recebe a mensagem pelo snapshot local, sem esperar a rede.
        // O callback de sucesso so ocorre quando a gravacao foi confirmada.
    }

    private void compactarChat() {
        if (sala == null || compactandoChat) return;
        compactandoChat = true;
        DocumentReference ref = sala;
        db.runTransaction(tx -> {
            DocumentSnapshot doc = tx.get(ref);
            List<?> todas = (List<?>) doc.get("mensagens");
            if (todas != null && todas.size() > 80) {
                // Ultimas 60, sem perder mensagens enviadas enquanto a transação tenta confirmar.
                tx.update(ref, "mensagens", new ArrayList<>(todas.subList(todas.size()-60,todas.size())));
            }
            return null;
        }).addOnCompleteListener(ok -> compactandoChat = false);
    }

    private void aplicarSincronia(DocumentSnapshot doc) {
        if (player == null) return;
        if (souLider) {
            chipSincronia.setText("●  CONTROLE DO LÍDER");
            return;
        }
        Boolean tocando = doc.getBoolean("reproduzindo");
        Long posicao = doc.getLong("posicaoMs");
        Timestamp tempo = doc.getTimestamp("sincronizadoEm");
        if (posicao == null || tocando == null) return;
        long destino = Math.max(0L, posicao);
        if (tocando && tempo != null) {
            long atraso = System.currentTimeMillis() - tempo.toDate().getTime();
            destino += Math.max(0L, Math.min(20000L, atraso));
        }
        if (player.getDuration() > 0 && destino > player.getDuration()) destino = player.getDuration();
        aplicandoEstadoRemoto = true;
        if (Math.abs(player.getCurrentPosition() - destino) > 2200L) player.seekTo(destino);
        if (tocando && !player.isPlaying()) player.play();
        if (!tocando && player.isPlaying()) player.pause();
        aplicandoEstadoRemoto = false;
        chipSincronia.setText("✓  SINCRONIZADO");
    }

    private void publicarEstado() {
        if (sala == null || player == null || !souLider || destruida) return;
        Map<String, Object> estado = new HashMap<>();
        estado.put("reproduzindo", player.isPlaying());
        estado.put("posicaoMs", player.getCurrentPosition());
        estado.put("sincronizadoEm", FieldValue.serverTimestamp());
        sala.update(estado).addOnFailureListener(e -> android.util.Log.w("NEXO_WATCH", "sync", e));
    }

    private void sincronizarManual() {
        if (sala != null) sala.get().addOnSuccessListener(this::aplicarSincronia)
                .addOnFailureListener(e -> erro("Não foi possível sincronizar", e));
    }
    private void escolherVideo() {
        if (!souLider || sala == null) return;
        EditText url = entrada("https://.../video.mp4", "");
        new AlertDialog.Builder(this).setTitle("Vídeo da sala")
                .setView(url).setNegativeButton("Cancelar", null)
                .setPositiveButton("ATUALIZAR", (d,w) -> {
                    String link = valor(url.getText().toString());
                    if (!link.startsWith("https://")) { toast("Use uma URL HTTPS direta."); return; }
                    sala.update("videoUrl", link, "posicaoMs", 0,
                            "reproduzindo", false, "sincronizadoEm", FieldValue.serverTimestamp())
                            .addOnSuccessListener(ok -> {
                                String idAtual = salaId;
                                liberarSala(false);
                                entrarSala(idAtual);
                            })
                            .addOnFailureListener(e -> erro("Falha ao atualizar vídeo", e));
                }).show();
    }

    // NEXO_V8_TEMPORADAS_PREMIUM — seletor de temporada e cards de episodios.
    private static final class EpisodioSala {
        int temporada, numero;
        String nome, descricao, imagem, videoUrl, duracao;
        EpisodioSala(int t, int n, String titulo, String sinopse, String capa,
                     String url, String tempo) {
            temporada=t; numero=n; nome=titulo; descricao=sinopse;
            imagem=capa; videoUrl=url; duracao=tempo;
        }
        String etiqueta() { return "T"+temporada+" · E"+numero+"  "+nome; }
    }
    private static final class TemporadaSala {
        int numero;
        final List<EpisodioSala> episodios=new ArrayList<>();
        TemporadaSala(int n) { numero=n; }
    }
    private static String campoCatalogo(org.json.JSONObject item, String... chaves) {
        if (item==null) return "";
        for (String chave:chaves) {
            String valor=item.optString(chave, "").trim();
            if (!valor.isEmpty() && !"null".equalsIgnoreCase(valor)) return valor;
        }
        return "";
    }

    /** Troca episodio no documento atual da sala; nao cria outra sala. */
    private void selecionarEpisodioSala(DocumentReference refAtual, String serie,
                                        EpisodioSala ep, android.app.Dialog janela) {
        if (!souLider) { toast("Somente o líder pode trocar episódios."); return; }
        if (sala != refAtual || destruida) return;
        if (!ep.videoUrl.startsWith("https://")) {
            toast("Este episódio ainda não tem vídeo publicado."); return;
        }
        Map<String,Object> dados=new HashMap<>();
        dados.put("videoUrl",ep.videoUrl);
        dados.put("filmeTitulo",serie+"  •  "+ep.etiqueta());
        dados.put("episodioAtual",ep.etiqueta());
        dados.put("posicaoMs",0L);
        dados.put("reproduzindo",false);
        dados.put("sincronizadoEm",FieldValue.serverTimestamp());
        refAtual.update(dados).addOnSuccessListener(ok -> {
            if (janela != null && janela.isShowing()) janela.dismiss();
            if (!destruida) toast("Agora: "+ep.etiqueta());
        }).addOnFailureListener(e -> erro("Não foi possível trocar o episódio", e));
    }

    /** Desenha apenas os episodios da temporada selecionada, em lista rolavel. */
    private void desenharEpisodiosSala(LinearLayout destino, TemporadaSala temporada,
                                      String episodioAtual, String nomeSerie,
                                      DocumentReference refAtual, android.app.Dialog janela) {
        destino.removeAllViews();
        TextView cabecalho = texto("TEMPORADA " + temporada.numero + "  •  "
                + temporada.episodios.size() + " EPISÓDIOS", 12, AZUL, true);
        cabecalho.setPadding(dp(3), dp(10), 0, dp(12));
        destino.addView(cabecalho);
        if (temporada.episodios.isEmpty()) {
            destino.addView(texto("Não há episódios publicados nesta temporada.", 13, SUAVE, false));
            return;
        }
        for (EpisodioSala ep : temporada.episodios) {
            boolean atual = ep.etiqueta().equals(episodioAtual)
                    || episodioAtual.startsWith("T"+ep.temporada+" · E"+ep.numero+"  ");
            LinearLayout cartao = linha();
            cartao.setGravity(Gravity.CENTER_VERTICAL);
            cartao.setPadding(dp(8), dp(10), dp(8), dp(10));
            cartao.setBackground(fundo(0xFF09131C, atual ? AZUL : 0xFF244557, 13));
            LinearLayout.LayoutParams cartaoLp=margens(-1,-2,0,9);
            destino.addView(cartao, cartaoLp);

            FrameLayout miniatura=new FrameLayout(this);
            miniatura.setBackground(fundo(0xFF101F2C, 0xFF31546B, 10));
            miniatura.setClipToOutline(true);
            ImageView imagem=new ImageView(this);
            imagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            miniatura.addView(imagem,new FrameLayout.LayoutParams(-1,-1));
            String urlImagem=lobbyUrlCapa(ep.imagem);
            if (!urlImagem.isEmpty()) {
                imagem.setTag(urlImagem);
                lobbyCarregarImagem(urlImagem,imagem);
            } else {
                TextView semCapa=texto("EP  "+ep.numero, 13, SUAVE, true);
                semCapa.setGravity(Gravity.CENTER);
                miniatura.addView(semCapa,new FrameLayout.LayoutParams(-1,-1));
            }
            TextView play=texto(ep.videoUrl.isEmpty()?"—":"▶",18, BRANCO, true);
            play.setGravity(Gravity.CENTER);
            play.setBackground(fundo(0xA9040B11,Color.TRANSPARENT,26));
            FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(33),dp(33),Gravity.CENTER);
            miniatura.addView(play,pp);
            int larguraMiniatura=larguraDp()<390?dp(102):dp(125);
            LinearLayout.LayoutParams pMini=new LinearLayout.LayoutParams(larguraMiniatura,dp(80));
            pMini.rightMargin=dp(10);
            cartao.addView(miniatura,pMini);

            LinearLayout descricao=coluna();
            TextView nome=texto("E"+ep.numero+"  "+ep.nome,13.5f,BRANCO,true);
            nome.setMaxLines(2);
            nome.setEllipsize(TextUtils.TruncateAt.END);
            descricao.addView(nome);
            if (!ep.descricao.isEmpty()) {
                TextView resumo=texto(ep.descricao,11.5f,SUAVE,false);
                resumo.setMaxLines(3);
                resumo.setEllipsize(TextUtils.TruncateAt.END);
                descricao.addView(resumo,margens(-1,-2,5,0));
            } else {
                TextView semResumo=texto("Descrição não cadastrada",11,SUAVE,false);
                descricao.addView(semResumo,margens(-1,-2,5,0));
            }
            TextView status=texto(atual?"EM REPRODUÇÃO":
                    (ep.videoUrl.isEmpty()?"INDISPONÍVEL":
                            (ep.duracao.isEmpty()?"TOQUE PARA ASSISTIR":ep.duracao)),
                    10,atual?AZUL:SUAVE,true);
            descricao.addView(status,margens(-1,-2,7,0));
            cartao.addView(descricao,new LinearLayout.LayoutParams(0,-2,1));
            cartao.setAlpha(ep.videoUrl.isEmpty()?.62f:1f);
            cartao.setOnClickListener(v -> selecionarEpisodioSala(refAtual,nomeSerie,ep,janela));
        }
    }

    private void mostrarTemporadasPremium(DocumentReference refAtual, String nomeSerie,
                                          String episodioAtual, List<TemporadaSala> temporadas) {
        if (destruida || sala != refAtual || isFinishing()) return;
        if (temporadas.isEmpty()) {
            toast("Nenhuma temporada cadastrada nesta série."); return;
        }
        android.app.Dialog janela=new android.app.Dialog(this);
        LinearLayout caixa=coluna();
        caixa.setPadding(dp(15),dp(15),dp(15),dp(12));
        caixa.setBackground(fundo(0xFF04090E,AZUL,18));
        LinearLayout cab=linha();cab.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titulos=coluna();
        TextView titulo=texto("TEMPORADAS E EPISÓDIOS",19,BRANCO,true);
        titulos.addView(titulo);
        TextView subtitulo=texto(nomeSerie,12,SUAVE,false);
        titulos.addView(subtitulo,margens(-1,-2,4,0));
        cab.addView(titulos,new LinearLayout.LayoutParams(0,-2,1));
        TextView fechar=texto("✕",21,BRANCO,true);
        fechar.setGravity(Gravity.CENTER);
        fechar.setBackground(fundo(0xFF111C25,0xFF305A6D,12));
        fechar.setOnClickListener(v -> janela.dismiss());
        cab.addView(fechar,new LinearLayout.LayoutParams(dp(43),dp(43)));
        caixa.addView(cab,margens(-1,-2,0,14));

        TextView seletor=texto("",14,BRANCO,true);
        seletor.setGravity(Gravity.CENTER_VERTICAL);
        seletor.setPadding(dp(14),0,dp(12),0);
        seletor.setBackground(fundo(0xFF091B28,AZUL,12));
        caixa.addView(seletor,margens(-1,dp(49),0,9));
        ScrollView rolagem=new ScrollView(this);
        rolagem.setVerticalScrollBarEnabled(true);
        rolagem.setFillViewport(false);
        LinearLayout lista=coluna();
        rolagem.addView(lista,new ScrollView.LayoutParams(-1,-2));
        caixa.addView(rolagem,new LinearLayout.LayoutParams(-1,0,1));

        // Seleciona inicialmente a temporada do episodio atual, se identificavel.
        int inicial=0;
        for (int i=0;i<temporadas.size();i++) {
            if (episodioAtual.startsWith("T"+temporadas.get(i).numero+" · E")) { inicial=i;break; }
        }
        final int[] escolha={inicial};
        Runnable renderizar=()->{
            TemporadaSala escolhida=temporadas.get(escolha[0]);
            seletor.setText("Temporada "+escolhida.numero+"   ▾");
            desenharEpisodiosSala(lista,escolhida,episodioAtual,nomeSerie,refAtual,janela);
            rolagem.post(() -> rolagem.scrollTo(0,0));
        };
        renderizar.run();
        seletor.setOnClickListener(v -> {
            ScrollView menuRolavel=new ScrollView(this);
            menuRolavel.setBackground(fundo(0xF907151E,AZUL,13));
            LinearLayout opcoes=coluna();
            opcoes.setPadding(dp(5),dp(5),dp(5),dp(5));
            menuRolavel.addView(opcoes);
            android.widget.PopupWindow menu=new android.widget.PopupWindow(menuRolavel,
                    Math.max(dp(210),seletor.getWidth()),
                    Math.min(dp(280),dp(56)*temporadas.size()+dp(10)),true);
            menu.setBackgroundDrawable(fundo(0xFF07131D,AZUL,13));
            menu.setOutsideTouchable(true);
            menu.setElevation(dp(8));
            for (int i=0;i<temporadas.size();i++) {
                final int pos=i;
                TemporadaSala t=temporadas.get(i);
                TextView opcao=texto("Temporada "+t.numero+"  •  "+t.episodios.size()+" episódios",
                        13,i==escolha[0]?AZUL:BRANCO,i==escolha[0]);
                opcao.setPadding(dp(12),0,dp(10),0);
                opcoes.addView(opcao,new LinearLayout.LayoutParams(-1,dp(53)));
                opcao.setOnClickListener(w -> {
                    escolha[0]=pos;
                    menu.dismiss();
                    renderizar.run();
                });
            }
            menu.showAsDropDown(seletor,0,dp(3));
        });
        TextView dica=texto(souLider?"Escolha um episódio para reproduzir nesta sala.":
                "Somente o líder pode trocar o episódio.",11,SUAVE,false);
        caixa.addView(dica,margens(-1,-2,10,0));
        janela.setContentView(caixa);
        android.view.Window window=janela.getWindow();
        if (window!=null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params=window.getAttributes();
            params.dimAmount=.82f;
            window.setAttributes(params);
        }
        janela.show();
        if (janela.getWindow()!=null) {
            int largura=Math.min(dp(750),getResources().getDisplayMetrics().widthPixels-dp(22));
            int altura=Math.min(dp(680),(int)(getResources().getDisplayMetrics().heightPixels*.85f));
            janela.getWindow().setLayout(largura,altura);
            janela.getWindow().setGravity(Gravity.CENTER);
        }
    }

    /** Usa o catalogo ja existente, sem inventar thumbnails ou descricoes. */
    private void abrirTemporadas() {
        if (sala==null) return;
        final DocumentReference refAtual=sala;
        refAtual.get().addOnSuccessListener(dados -> {
            if (!dados.exists() || sala != refAtual) return;
            String idSerie=valor(dados.getString("serieId"));
            String nomeSerie=valor(dados.getString("serieTitulo"));
            String episodioAtual=valor(dados.getString("episodioAtual"));
            if (nomeSerie.isEmpty()) { toast("Esta sala não é de uma série."); return; }
            toast("Carregando temporadas...");
            new Thread(() -> {
                java.net.HttpURLConnection conexao=null;
                try {
                    java.net.URL url=new java.net.URL(
                      "https://delicate-sunset-c8b5.lipesanderson.workers.dev/file?key=dados%2Fcatalogo.json");
                    conexao=(java.net.HttpURLConnection)url.openConnection();
                    conexao.setConnectTimeout(15000);
                    conexao.setReadTimeout(20000);
                    if (conexao.getResponseCode()!=200) throw new Exception("Catálogo indisponível");
                    java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();
                    try (java.io.InputStream in=conexao.getInputStream()) {
                        byte[] buffer=new byte[8192];int n;
                        while((n=in.read(buffer))!=-1) {
                            if(bytes.size()+n>5_000_000) throw new Exception("Catálogo muito grande");
                            bytes.write(buffer,0,n);
                        }
                    }
                    org.json.JSONObject raizJson=new org.json.JSONObject(bytes.toString("UTF-8"));
                    org.json.JSONArray series=raizJson.optJSONArray("series");
                    if (series==null) series=raizJson.optJSONArray("conteudos");
                    if (series==null) throw new Exception("Séries não encontradas");
                    org.json.JSONObject serie=null;
                    for (int i=0;i<series.length();i++) {
                        org.json.JSONObject item=series.optJSONObject(i);
                        if (item==null) continue;
                        if ((!idSerie.isEmpty() && idSerie.equals(item.optString("id")))
                                || nomeSerie.equalsIgnoreCase(item.optString("titulo"))) {
                            serie=item;break;
                        }
                    }
                    if (serie==null) throw new Exception("Série não encontrada");
                    org.json.JSONArray temporadasJson=serie.optJSONArray("temporadas");
                    if (temporadasJson==null) throw new Exception("Sem temporadas cadastradas");
                    List<TemporadaSala> temporadas=new ArrayList<>();
                    String fotoSerie=campoCatalogo(serie,"banner_url","capa_url","poster_url","capa","poster");
                    for (int ti=0;ti<temporadasJson.length();ti++) {
                        org.json.JSONObject t=temporadasJson.optJSONObject(ti);
                        if (t==null) continue;
                        int numTemporada=t.optInt("numero",ti+1);
                        TemporadaSala temporada=new TemporadaSala(numTemporada);
                        org.json.JSONArray eps=t.optJSONArray("episodios");
                        if (eps==null) continue;
                        String fotoTemporada=campoCatalogo(t,"banner_url","capa_url","poster_url","capa");
                        for (int ei=0;ei<eps.length();ei++) {
                            org.json.JSONObject ep=eps.optJSONObject(ei);
                            if (ep==null) continue;
                            String video=campoCatalogo(ep,"video_url","urlReproducao");
                            if (video.isEmpty()) {
                                String key=campoCatalogo(ep,"videoKey");
                                if (!key.isEmpty()) video="https://delicate-sunset-c8b5.lipesanderson.workers.dev/video?key="
                                        +android.net.Uri.encode(key);
                            }
                            if (!video.startsWith("https://")) video="";
                            int numEp=ep.optInt("numero",ei+1);
                            String nome=campoCatalogo(ep,"titulo","title","nome");
                            if (nome.isEmpty()) nome="Episódio "+numEp;
                            String descricao=campoCatalogo(ep,"sinopse","descricao","resumo","overview");
                            String imagem=campoCatalogo(ep,"thumbnail_url","thumb_url","imagem_url",
                                    "capa_url","still_url","poster_url","miniatura","capa","banner_url");
                            if (imagem.isEmpty()) imagem=fotoTemporada.isEmpty()?fotoSerie:fotoTemporada;
                            String duracao=campoCatalogo(ep,"duracao","duracaoMinutos","tempo");
                            if (duracao.matches("[0-9]{1,3}")) duracao += " min";
                            temporada.episodios.add(new EpisodioSala(numTemporada,numEp,nome,
                                    descricao,imagem,video,duracao));
                        }
                        temporadas.add(temporada);
                    }
                    runOnUiThread(() -> {
                        if (sala == refAtual && !destruida && !isFinishing())
                            mostrarTemporadasPremium(refAtual,nomeSerie,episodioAtual,temporadas);
                    });
                } catch (Exception erro) {
                    android.util.Log.w("NEXO_WATCH","temporadas premium",erro);
                    runOnUiThread(() -> { if (!destruida) toast("Não foi possível carregar as temporadas."); });
                } finally { if (conexao!=null) conexao.disconnect(); }
            },"NEXO-temporadas").start();
        }).addOnFailureListener(e -> erro("Falha ao consultar a sala",e));
    }
    private void votarPausa() {
        if (sala == null) return;
        sala.update("votosPausa", FieldValue.arrayUnion(uid))
                .addOnSuccessListener(ok -> toast("Seu voto foi registrado."))
                .addOnFailureListener(e -> erro("Falha ao votar", e));
    }
    private void escolherReacao() {
        String[] emojis = {"❤️", "😂", "👏", "😱", "🔥", "🍿"};
        new AlertDialog.Builder(this).setTitle("Reagir à sessão")
                .setItems(emojis, (d, which) -> {
                    if (sala == null) return;
                    Map<String, Object> reacao = new HashMap<>();
                    reacao.put("uid", uid);
                    reacao.put("emoji", emojis[which]);
                    reacao.put("ts", System.currentTimeMillis());
                    sala.update("reacaoUltima", reacao).addOnFailureListener(e -> erro("Falha ao reagir", e));
                }).show();
    }
    private void animarReacao(String emoji) {
        if (reacaoVisual == null) return;
        reacaoVisual.setText(emoji);
        reacaoVisual.setVisibility(View.VISIBLE);
        reacaoVisual.setAlpha(1f);
        reacaoVisual.setTranslationY(dp(18));
        reacaoVisual.animate().alpha(0f).translationY(-dp(35)).setDuration(1500)
                .withEndAction(() -> reacaoVisual.setVisibility(View.GONE)).start();
    }
    private void convidar() {
        if (salaId == null) return;
        Intent envio = new Intent(Intent.ACTION_SEND);
        envio.setType("text/plain");
        envio.putExtra(Intent.EXTRA_TEXT, "Entre na minha sala NEXO para assistir junto!\nCódigo: " + salaId +
                "\nAbra Filmes e Séries > Assistir Junto > Entrar com código.");
        startActivity(Intent.createChooser(envio, "Convidar para a sala NEXO"));
    }
    private void ajustesSala() {
        if (!souLider) { toast("Somente o líder pode configurar a sala."); return; }
        new AlertDialog.Builder(this)
                .setTitle("Configurações da sala")
                .setItems(new String[] {"Alterar vídeo", "Encerrar sala para todos"}, (d, pos) -> {
                    if (pos == 0) escolherVideo();
                    else new AlertDialog.Builder(this).setTitle("Encerrar a sessão?")
                            .setMessage("Todos os participantes serão desconectados da sala.")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("ENCERRAR", (w, p) -> sala.update("aberta", false)
                                    .addOnSuccessListener(ok -> montarLobby())
                                    .addOnFailureListener(e -> erro("Não foi possível encerrar", e)))
                            .show();
                }).show();
    }
    private void confirmarSaida() {
        new AlertDialog.Builder(this).setTitle("Sair da sala?")
                .setMessage("Você poderá entrar novamente enquanto a sala estiver aberta.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("SAIR", (d,w) -> montarLobby()).show();
    }
    private void liberarSala() { liberarSala(true); }
    private void liberarSala(boolean registrarSaida) {
        if(cinema!=null){cinema.fechar();cinema=null;}
        rastreadorMensagens.reiniciar();
        ultimasMensagens = new ArrayList<>();
        ultimaSalaSnapshot = null;
        compactandoChat = false;
        enviandoMensagem = false;
        handler.removeCallbacks(publicadorEstado);
        if (salaListener != null) { salaListener.remove(); salaListener = null; }
        if (player != null) {
            player.stop();
            if (playerView != null) playerView.setPlayer(null);
            player.release();
            player = null;
        }
        if (sala != null) {
            DocumentReference antiga = sala;
            if (registrarSaida) {
                // Remoção e transferência de liderança na MESMA transação.
                db.runTransaction(tx -> {
                    DocumentSnapshot doc = tx.get(antiga);
                    if (!doc.exists()) return null;
                    List<?> atuais = (List<?>) doc.get("participantes");
                    List<String> restantes = new ArrayList<>();
                    if (atuais != null) for (Object pessoa : atuais) {
                        String id = String.valueOf(pessoa);
                        if (!id.equals(uid) && !restantes.contains(id)) restantes.add(id);
                    }
                    String lider = txt(doc.get("liderUid"), txt(doc.get("criadorUid"), ""));
                    Map<String,Object> mudancas = new HashMap<>();
                    mudancas.put("participantes", restantes);
                    if (uid.equals(lider)) mudancas.put("liderUid",
                            restantes.isEmpty() ? "" : restantes.get(0));
                    tx.update(antiga, mudancas);
                    return null;
                }).addOnFailureListener(e -> android.util.Log.w("NEXO_WATCH", "exit", e));
            }
            sala = null;
        }
        salaId = null;
        souLider = false;
        chipTitulo = null;
        listaMensagens = null;
        listaParticipantes = null;
        ultimaMensagemRenderizada = "";
        ultimosParticipantes = "";
        reacaoVisual = null;
        playerView = null;
        urlAtualPlayer = "";
        avisoCapaAtual = null;
        chipEpisodioAtual = null;
        capaDemonstrativa = null;
        primeiraSala = true;
    }
    @Override public void onBackPressed() {
        if (sala != null) confirmarSaida();
        else super.onBackPressed();
    }
    // Somente o lobby tem miniaturas: pausar ao sair da tela, sem tocar no player da sala.
    @Override protected void onStart() {
        super.onStart();
        if (sala == null) lobbyAgendarPrevias();
    }
    @Override protected void onStop() {
        lobbyEncerrarPrevias();
        super.onStop();
    }
    @Override protected void onDestroy() {
        destruida = true;
        lobbyEncerrarPrevias();
        if (lobbyListener != null) { lobbyListener.remove(); lobbyListener = null; }
        lobbyImagemPool.shutdownNow();
        liberarSala();
        super.onDestroy();
    }
}
