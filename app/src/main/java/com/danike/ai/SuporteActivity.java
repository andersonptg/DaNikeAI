// NEXO_SUPORTE_1_1_ANDROID
package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** NEXO_VISUAL_ADM_NEON_V2: layout ADM neon. Autorizacao conferida pelo servidor. */
public class SuporteActivity extends Activity {
    private static final int FUNDO = Color.rgb(3, 6, 20);
    private static final int PAINEL = Color.rgb(12, 21, 47);
    private static final int AZUL = Color.rgb(65, 174, 255);
    private static final int ROXO = Color.rgb(167, 105, 248);
    private static final int CIANO = Color.rgb(33, 205, 255);
    private static final int VERDE = Color.rgb(38, 228, 150);
    private static final int VERMELHO = Color.rgb(255, 95, 118);
    private static final int CINZA = Color.rgb(166, 184, 213);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService imagens = Executors.newFixedThreadPool(2);
    private LinearLayout raiz, conteudo, lista, mensagens, barraEnvio;
    private TextView tituloTela, subtitulo, statusTela, statusChamado;
    private EditText campoMensagem;
    private Button enviar;
    private LinearLayout perfilAdm, indicadores;
    private TextView nomeAdm, contadorIndicador, conexaoIndicador;
    private ImageView fotoAdm;
    private TextView etiquetaStatusAtual;
    private boolean perfilConsultado = false;
    private boolean ativo = false;
    private boolean ocupada = false;
    private boolean operacao = false;
    private boolean adm = false;
    private String ticketAtual = null;
    private String respostaListaAnterior = "";
    private boolean listaRenderizada = false;
    private String mensagensAnteriores = "";
    private JSONObject chamadoAtual;
    private String idEnvioPendente = "";
    private String textoEnvioPendente = "";
    private final Runnable atualizar = new Runnable() {
        @Override public void run() {
            if (!ativo) return;
            if (!ocupada && !operacao) {
                if (ticketAtual == null) carregarChamados(false);
                else carregarConversa(false);
            }
            handler.postDelayed(this, ticketAtual == null ? 10000L : 6000L);
        }
    };

    private int dp(int n) {
        return Math.round(getResources().getDisplayMetrics().density * n);
    }
    private GradientDrawable borda(int cor, int contorno, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(cor); d.setCornerRadius(dp(raio)); d.setStroke(dp(1), contorno);
        return d;
    }
    private TextView texto(String s, int tamanho, int cor, boolean negrito) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(tamanho); v.setTextColor(cor);
        if (negrito) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private Button botao(String s) {
        Button b = new Button(this);
        b.setText(s); b.setTextSize(12); b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(borda(Color.rgb(19, 64, 115), AZUL, 14));
        return b;
    }
    private LinearLayout vertical(int cor) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(cor);
        return l;
    }
    private void mensagemInfo(String msg) {
        if (statusTela != null) {
            statusTela.setText(msg);
            statusTela.setVisibility(msg.contains("conectad") ? View.GONE : View.VISIBLE);
        }
        if (conexaoIndicador != null && !msg.contains("conectad")) {
            conexaoIndicador.setText("●  Verificar conexão");
        }
    }
    private void erro(String msg) {
        if (!isFinishing() && !isDestroyed()) mensagemInfo(msg);
    }
    private void noUi(Runnable runnable) {
        runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) runnable.run(); });
    }

    // Impede que um e-mail recebido por engano no nome seja exibido no suporte.
    private String nomePublico(String bruto, boolean equipe) {
        String nome = bruto == null ? "" : bruto.trim();
        if (nome.isEmpty() || nome.contains("@") ||
                nome.toLowerCase(java.util.Locale.ROOT).startsWith("mailto:")) {
            return equipe ? "Administrador NEXO" : "Membro NEXO";
        }
        return nome.length() > 70 ? nome.substring(0, 70) : nome;
    }
    private GradientDrawable neon(int fundo, int fim, int bordaCor, int raio) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{fundo, fim});
        d.setCornerRadius(dp(raio));
        d.setStroke(dp(1), bordaCor);
        return d;
    }
    private TextView selo(String valor, int corTexto, int corBorda, int corFundo) {
        TextView v = texto(valor, 11, corTexto, false);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
        v.setBackground(borda(corFundo, corBorda, 14));
        return v;
    }
    private LinearLayout.LayoutParams margem(int w, int h, int esquerda, int topo,
                                               int direita, int baixo) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(esquerda), dp(topo), dp(direita), dp(baixo));
        return p;
    }
    private String dataCurta(String iso) {
        if (iso == null || iso.length() < 16) return "";
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" +
                iso.substring(0, 4) + "  •  " + iso.substring(11, 16) + " UTC";
    }
    private void visibilidadeAdm() {
        if (perfilAdm != null) perfilAdm.setVisibility(adm ? View.VISIBLE : View.GONE);
        if (adm) consultarPerfilAdm();
    }
    private void consultarPerfilAdm() {
        if (!adm || perfilConsultado || nomeAdm == null) return;
        perfilConsultado = true;
        com.google.firebase.auth.FirebaseUser u =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) return;
        nomeAdm.setText(nomePublico(u.getDisplayName(), true));
        if (u.getPhotoUrl() != null && fotoAdm != null)
            carregarAvatar(fotoAdm, u.getPhotoUrl().toString());
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("perfis_publicos").document(u.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!adm || isFinishing() || isDestroyed() || !doc.exists()) return;
                    String nome = doc.getString("nome");
                    String foto = doc.getString("fotoUrl");
                    if (nome != null && !nome.trim().isEmpty())
                        nomeAdm.setText(nomePublico(nome, true));
                    if (foto != null && fotoAdm != null) carregarAvatar(fotoAdm, foto);
                });
    }
    private void marcarConectado(int total) {
        if (contadorIndicador != null) contadorIndicador.setText(
                total + (adm ? " chamados" : " chamados seus"));
        if (conexaoIndicador != null) conexaoIndicador.setText("●  Central conectada");
        if (statusTela != null) statusTela.setVisibility(View.GONE);
    }
    private void atualizarChipStatus(String estado) {
        String rotulo = tituloStatus(estado);
        if (etiquetaStatusAtual != null) etiquetaStatusAtual.setText("●  " + rotulo);
        if (statusChamado != null) statusChamado.setText("Status: " + rotulo);
    }
    private void responderAgora() {
        if (campoMensagem == null || !campoMensagem.isEnabled()) {
            Toast.makeText(this, "Este chamado está fechado.", Toast.LENGTH_SHORT).show();
            return;
        }
        campoMensagem.requestFocus();
        android.view.inputmethod.InputMethodManager teclado =
                (android.view.inputmethod.InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        if (teclado != null) teclado.showSoftInput(campoMensagem,
                android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }
    private void menuAcoesChamado(String id, JSONObject ticket, View ancora) {
        if (!adm || id == null || id.isEmpty()) return;
        PopupMenu menu = new PopupMenu(this, ancora);
        menu.getMenu().add("Responder");
        menu.getMenu().add("Encerrar atendimento");
        menu.getMenu().add("Excluir conversa");
        menu.setOnMenuItemClickListener(item -> {
            String opcao = item.getTitle().toString();
            if ("Responder".equals(opcao)) {
                if (!id.equals(ticketAtual)) abrirConversa(id, ticket);
                responderAgora();
            } else if ("Encerrar atendimento".equals(opcao)) {
                if (!id.equals(ticketAtual)) abrirConversa(id, ticket);
                alterarStatus("fechado");
            } else if ("Excluir conversa".equals(opcao)) {
                confirmarExclusao(id, ticket);
            }
            return true;
        });
        menu.show();
    }
    private void confirmarExclusao(String id, JSONObject ticket) {
        if (!adm || operacao || id == null || id.isEmpty()) return;
        String nome = nomePublico(ticket == null ? "" : ticket.optString("name"), false);
        new AlertDialog.Builder(this)
                .setTitle("Excluir conversa do suporte?")
                .setMessage("O chamado de " + nome + " será ocultado para o membro e o ADM. "
                        + "Esta ação exige confirmação e autorização do servidor.")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("EXCLUIR", (d, w) -> {
                    operacao = true;
                    NexoSuporteApi.requisitar("DELETE", "/api/v1/support/" + id, null,
                            new NexoSuporteApi.Callback() {
                                @Override public void sucesso(JSONObject dados) {
                                    noUi(() -> {
                                        operacao = false;
                                        if (id.equals(ticketAtual)) {
                                            ticketAtual = null;
                                            chamadoAtual = null;
                                            mensagensAnteriores = "";
                                        }
                                        respostaListaAnterior = "";
                                        mostrarLista();
                                        carregarChamados(true);
                                        Toast.makeText(SuporteActivity.this,
                                                "Chamado excluído da Central.",
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                                @Override public void erro(String mensagem) {
                                    noUi(() -> {
                                        operacao = false;
                                        SuporteActivity.this.erro(mensagem);
                                        Toast.makeText(SuporteActivity.this,
                                                mensagem, Toast.LENGTH_LONG).show();
                                    });
                                }
                            });
                }).show();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(FUNDO);
        getWindow().setNavigationBarColor(FUNDO);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        construirTela();
        getWindow().getDecorView().post(this::orientarVerificacaoAdm);
    }
    @Override protected void onResume() {
        super.onResume(); ativo = true;
        handler.removeCallbacks(atualizar);
        if (ticketAtual == null) carregarChamados(true); else carregarConversa(true);
        handler.postDelayed(atualizar, 6000L);
    }
    @Override protected void onPause() {
        ativo = false; handler.removeCallbacks(atualizar); super.onPause();
    }
    @Override protected void onDestroy() {
        ativo = false; handler.removeCallbacks(atualizar); imagens.shutdownNow(); super.onDestroy();
    }

    // A verificacao e feita pelo proprio usuario. O servidor decide o acesso ADM.
    private void orientarVerificacaoAdm() {
        com.google.firebase.auth.FirebaseUser usuario =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null || usuario.getEmail() == null ||
                !"lipesanderson@gmail.com".equalsIgnoreCase(usuario.getEmail().trim())) return;
        if (usuario.isEmailVerified()) return;

        new android.app.AlertDialog.Builder(this)
                .setTitle("Verificar e-mail do administrador")
                .setMessage("Sua conta oficial ainda nao esta verificada no Firebase. "
                        + "Envie o link, abra sua caixa de entrada e confirme o endereco. "
                        + "Depois volte ao NEXO e toque em 'Ja confirmei'.")
                .setPositiveButton("Enviar link", (d, w) -> {
                    usuario.sendEmailVerification()
                            .addOnSuccessListener(v -> android.widget.Toast.makeText(
                                    this, "Link de verificacao enviado. Confira seu e-mail.",
                                    android.widget.Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> android.widget.Toast.makeText(
                                    this, "Falha ao enviar o link: " + e.getMessage(),
                                    android.widget.Toast.LENGTH_LONG).show());
                })
                .setNeutralButton("Ja confirmei", (d, w) -> atualizarVerificacaoAdm(usuario))
                .setNegativeButton("Agora nao", (d, w) -> {})
                .show();
    }

    private void atualizarVerificacaoAdm(com.google.firebase.auth.FirebaseUser usuario) {
        usuario.reload().addOnSuccessListener(v -> {
            com.google.firebase.auth.FirebaseUser atualizado =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (atualizado == null || !atualizado.isEmailVerified()) {
                android.widget.Toast.makeText(this,
                        "Ainda nao verificado. Confirme o link recebido no e-mail.",
                        android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            atualizado.getIdToken(true)
                    .addOnSuccessListener(t -> {
                        android.widget.Toast.makeText(this,
                                "E-mail verificado. Atualizando a Central NEXO...",
                                android.widget.Toast.LENGTH_SHORT).show();
                        // Consulta novamente. A API, e nao o Android, define scope=admin.
                        if (ticketAtual == null) {
                            respostaListaAnterior = "";
                            listaRenderizada = false;
                            carregarChamados(true);
                        }
                    })
                    .addOnFailureListener(e -> android.widget.Toast.makeText(
                            this, "Nao foi possivel atualizar a sessao.",
                            android.widget.Toast.LENGTH_LONG).show());
        }).addOnFailureListener(e -> android.widget.Toast.makeText(
                this, "Nao foi possivel consultar a verificacao do Firebase.",
                android.widget.Toast.LENGTH_LONG).show());
    }

    private void construirTela() {
        raiz = vertical(FUNDO);
        raiz.setBackground(neon(FUNDO, Color.rgb(5, 12, 34), FUNDO, 0));
        raiz.setPadding(dp(12), dp(7), dp(12), dp(8));

        LinearLayout marca = new LinearLayout(this);
        marca.setOrientation(LinearLayout.HORIZONTAL);
        marca.setGravity(Gravity.CENTER_VERTICAL);
        TextView voltar = texto("‹", 33, AZUL, true);
        voltar.setGravity(Gravity.CENTER);
        marca.addView(voltar, new LinearLayout.LayoutParams(dp(38), dp(43)));
        voltar.setOnClickListener(v -> voltar());
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.nexo_icon);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        marca.addView(logo, margem(dp(36), dp(36), 2, 0, 9, 0));
        TextView nomeMarca = texto("N E X O", 18, Color.WHITE, true);
        marca.addView(nomeMarca, new LinearLayout.LayoutParams(0, -2, 1));

        perfilAdm = new LinearLayout(this);
        perfilAdm.setOrientation(LinearLayout.HORIZONTAL);
        perfilAdm.setGravity(Gravity.CENTER_VERTICAL);
        perfilAdm.setPadding(dp(6), dp(3), dp(9), dp(3));
        perfilAdm.setBackground(neon(Color.rgb(13, 31, 65), Color.rgb(4, 16, 43), AZUL, 13));
        fotoAdm = new ImageView(this);
        fotoAdm.setImageResource(R.drawable.nexo_icon);
        fotoAdm.setScaleType(ImageView.ScaleType.CENTER_CROP);
        perfilAdm.addView(fotoAdm, margem(dp(34), dp(34), 0, 0, 8, 0));
        LinearLayout dadosAdm = vertical(Color.TRANSPARENT);
        nomeAdm = texto("Administrador NEXO", 11, Color.WHITE, true);
        nomeAdm.setSingleLine(true);
        nomeAdm.setMaxEms(15);
        nomeAdm.setEllipsize(android.text.TextUtils.TruncateAt.END);
        dadosAdm.addView(nomeAdm);
        dadosAdm.addView(texto("♛  ADM OFICIAL", 10, CIANO, true));
        perfilAdm.addView(dadosAdm);
        perfilAdm.setVisibility(View.GONE);
        marca.addView(perfilAdm, margem(-2, -2, 2, 0, 0, 0));
        raiz.addView(marca);

        LinearLayout cabecalho = new LinearLayout(this);
        cabecalho.setOrientation(LinearLayout.HORIZONTAL);
        cabecalho.setGravity(Gravity.CENTER_VERTICAL);
        TextView icone = texto("◉", 26, CIANO, true);
        icone.setGravity(Gravity.CENTER);
        icone.setBackground(neon(Color.rgb(10, 49, 86), Color.rgb(5, 23, 54), CIANO, 13));
        cabecalho.addView(icone, margem(dp(42), dp(42), 4, 7, 10, 7));
        LinearLayout titulos = vertical(Color.TRANSPARENT);
        tituloTela = texto("SUPORTE NEXO", 19, Color.WHITE, true);
        subtitulo = texto("Seus chamados e respostas", 12, CINZA, false);
        titulos.addView(tituloTela);
        titulos.addView(subtitulo);
        cabecalho.addView(titulos, new LinearLayout.LayoutParams(0, -2, 1));
        raiz.addView(cabecalho);
        View linha = new View(this);
        linha.setBackgroundColor(CIANO);
        raiz.addView(linha, margem(-1, dp(1), 0, 4, 0, 8));

        android.widget.HorizontalScrollView rolagem = new android.widget.HorizontalScrollView(this);
        rolagem.setHorizontalScrollBarEnabled(false);
        indicadores = new LinearLayout(this);
        indicadores.setOrientation(LinearLayout.HORIZONTAL);
        indicadores.setGravity(Gravity.CENTER_VERTICAL);
        indicadores.setPadding(dp(7), dp(7), dp(7), dp(7));
        indicadores.setBackground(neon(Color.rgb(6, 20, 42), PAINEL,
                Color.rgb(29, 75, 128), 15));
        conexaoIndicador = selo("●  Conectando...", VERDE,
                Color.rgb(16, 125, 96), Color.rgb(5, 38, 38));
        indicadores.addView(conexaoIndicador, margem(-2, -2, 0, 0, 8, 0));
        indicadores.addView(selo("⚡  Atualização ativa", CIANO,
                Color.rgb(38, 111, 176), Color.rgb(8, 33, 65)),
                margem(-2, -2, 0, 0, 8, 0));
        contadorIndicador = selo("Carregando chamados", Color.WHITE,
                Color.rgb(38, 111, 176), Color.rgb(8, 33, 65));
        indicadores.addView(contadorIndicador);
        rolagem.addView(indicadores);
        raiz.addView(rolagem, margem(-1, -2, 0, 0, 0, 4));

        statusTela = texto("Conectando à Central NEXO...", 11, CINZA, false);
        statusTela.setPadding(dp(5), dp(5), dp(5), dp(5));
        raiz.addView(statusTela);
        conteudo = vertical(Color.TRANSPARENT);
        raiz.addView(conteudo, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(raiz);
        mostrarLista();
    }

    private void limparConteudo() { conteudo.removeAllViews(); }
    private void voltar() {
        if (ticketAtual != null) {
            ticketAtual = null; chamadoAtual = null; mensagensAnteriores = "";
            mostrarLista(); carregarChamados(true);
        } else finish();
    }
    @Override public void onBackPressed() { voltar(); }

    private void mostrarLista() {
        limparConteudo();
        tituloTela.setText(adm ? "CENTRAL DE ATENDIMENTO" : "SUPORTE NEXO");
        subtitulo.setText(adm ? "Painel do administrador oficial" : "Seus chamados e respostas");
        visibilidadeAdm();
        if (adm) {
            TextView tituloLista = texto("CHAMADOS RECEBIDOS", 13, CIANO, true);
            conteudo.addView(tituloLista, margem(-1, -2, 5, 12, 0, 3));
        } else {
            Button novo = botao("＋  ABRIR NOVO CHAMADO");
            novo.setBackground(neon(Color.rgb(15, 67, 126), Color.rgb(8, 36, 79), CIANO, 15));
            conteudo.addView(novo, margem(-1, dp(54), 0, 12, 0, 12));
            novo.setOnClickListener(v -> abrirFormulario());
        }
        ScrollView rolagem = new ScrollView(this);
        rolagem.setFillViewport(true);
        lista = vertical(Color.TRANSPARENT);
        listaRenderizada = false;
        lista.setPadding(dp(1), dp(9), dp(1), dp(20));
        rolagem.addView(lista);
        conteudo.addView(rolagem, new LinearLayout.LayoutParams(-1, 0, 1));
        if (respostaListaAnterior.isEmpty()) {
            lista.addView(texto("Carregando chamados...", 14, CINZA, false));
        } else {
            try { renderizarChamados(new JSONObject(respostaListaAnterior)); }
            catch (Exception ignored) { }
        }
    }

    private void carregarChamados(boolean mostrarFalha) {
        if (ticketAtual != null || ocupada) return;
        ocupada = true;
        NexoSuporteApi.requisitar("GET", "/api/v1/support?limit=100", null,
                new NexoSuporteApi.Callback() {
                    @Override public void sucesso(JSONObject dados) {
                        noUi(() -> {
                            ocupada = false;
                            boolean admNovo = "admin".equalsIgnoreCase(dados.optString("scope"));
                            boolean mudouPapel = admNovo != adm;
                            adm = admNovo;
                            String conteudoNovo = dados.toString();
                            String listaNova = dados.optJSONArray("items") == null
                                    ? "[]" : dados.optJSONArray("items").toString();
                            String listaAntiga = "";
                            try { JSONObject velho = new JSONObject(respostaListaAnterior);
                                listaAntiga = velho.optJSONArray("items") == null ? "[]"
                                        : velho.optJSONArray("items").toString(); }
                            catch (Exception ignored) { }
                            respostaListaAnterior = conteudoNovo;
                            if (ticketAtual == null) {
                                if (mudouPapel) mostrarLista();
                                if (!listaRenderizada || !listaNova.equals(listaAntiga)) renderizarChamados(dados);
                                marcarConectado(dados.optInt("total", 0));
                            }
                        });
                    }
                    @Override public void erro(String mensagem) {
                        noUi(() -> { ocupada = false; if (mostrarFalha) SuporteActivity.this.erro(mensagem); });
                    }
                });
    }

    private void renderizarChamados(JSONObject resultado) {
        if (lista == null || ticketAtual != null) return;
        lista.removeAllViews();
        listaRenderizada = true;
        JSONArray itens = resultado.optJSONArray("items");
        if (itens == null || itens.length() == 0) {
            TextView vazio = texto(adm ? "Nenhum chamado recebido." :
                    "Você ainda não abriu chamados.", 15, CINZA, false);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(dp(12), dp(48), dp(12), dp(30));
            lista.addView(vazio);
            return;
        }
        for (int i = 0; i < itens.length(); i++) {
            JSONObject t = itens.optJSONObject(i);
            if (t == null) continue;
            String id = t.optString("ticketId", "");
            if (id.isEmpty()) continue;
            LinearLayout card = vertical(Color.TRANSPARENT);
            card.setBackground(neon(Color.rgb(10, 36, 73), Color.rgb(7, 18, 48),
                    adm ? CIANO : Color.rgb(53, 106, 175), 18));
            card.setPadding(dp(14), dp(13), dp(14), dp(13));
            card.setElevation(dp(4));
            LinearLayout cabecalho = new LinearLayout(this);
            cabecalho.setGravity(Gravity.CENTER_VERTICAL);
            if (adm) {
                ImageView avatar = new ImageView(this);
                avatar.setImageResource(R.drawable.nexo_icon);
                avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                cabecalho.addView(avatar, margem(dp(47), dp(47), 0, 0, 11, 0));
                carregarAvatar(avatar, t.optString("avatarUrl", ""));
            }
            LinearLayout dados = vertical(Color.TRANSPARENT);
            dados.addView(texto(adm ? nomePublico(t.optString("name"), false)
                    : t.optString("title", "Chamado"), 16, Color.WHITE, true));
            if (adm) dados.addView(texto(t.optString("title", "Sem assunto"), 13, CINZA, false));
            dados.addView(selo("●  " + tituloStatus(t.optString("status", "aberto")),
                    CIANO, Color.rgb(42, 97, 158), Color.rgb(12, 45, 85)),
                    margem(-2, -2, 0, 6, 0, 0));
            cabecalho.addView(dados, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(cabecalho);
            TextView ultima = texto(t.optString("lastMessage", ""), 13, CINZA, false);
            ultima.setMaxLines(2);
            ultima.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(ultima, margem(-1, -2, 0, 9, 0, 7));
            TextView rodape = texto("✉  " +
                    t.optInt(adm ? "unreadForStaff" : "unreadForUser", 0) +
                    " não lidas" + (adm ? "    •  Segure para gerenciar" : ""),
                    11, CIANO, false);
            card.addView(rodape);
            lista.addView(card, margem(-1, -2, 0, 0, 0, 12));
            card.setOnClickListener(v -> abrirConversa(id, t));
            if (adm) card.setOnLongClickListener(v -> {
                menuAcoesChamado(id, t, card);
                return true;
            });
        }
    }

    private void abrirFormulario() {
        LinearLayout formulario = vertical(PAINEL);
        formulario.setPadding(dp(16), dp(14), dp(16), dp(10));
        EditText assunto = new EditText(this);
        assunto.setSingleLine(true);
        assunto.setHint("Assunto do chamado");
        assunto.setTextColor(Color.WHITE); assunto.setHintTextColor(CINZA);
        formulario.addView(assunto, new LinearLayout.LayoutParams(-1, -2));
        EditText descricao = new EditText(this);
        descricao.setHint("Conte o que aconteceu...");
        descricao.setTextColor(Color.WHITE); descricao.setHintTextColor(CINZA);
        descricao.setMinLines(4); descricao.setMaxLines(8);
        formulario.addView(descricao, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Novo chamado NEXO")
                .setView(formulario)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("ENVIAR", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String titulo = assunto.getText().toString().trim();
                    String corpo = descricao.getText().toString().trim();
                    if (titulo.isEmpty() || corpo.isEmpty()) {
                        Toast.makeText(this, "Preencha assunto e descrição.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (titulo.length() > 120 || corpo.length() > 2000) {
                        Toast.makeText(this, "Assunto ou descrição muito longos.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (operacao) return;
                    operacao = true;
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    try {
                        JSONObject json = new JSONObject();
                        json.put("title", titulo);
                        json.put("message", corpo);
                        json.put("category", "suporte");
                        json.put("priority", "media");
                        NexoSuporteApi.requisitar("POST", "/api/v1/support", json,
                                new NexoSuporteApi.Callback() {
                                    @Override public void sucesso(JSONObject res) {
                                        noUi(() -> {
                                            operacao = false;
                                            dialog.dismiss();
                                            JSONObject t = res.optJSONObject("ticket");
                                            if (t != null && !t.optString("ticketId").isEmpty()) {
                                                abrirConversa(t.optString("ticketId"), t);
                                            } else carregarChamados(true);
                                        });
                                    }
                                    @Override public void erro(String e) {
                                        noUi(() -> {
                                            operacao = false;
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                            SuporteActivity.this.erro(e);
                                            Toast.makeText(SuporteActivity.this, e, Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                    } catch (Exception e) { operacao = false; }
                }));
        dialog.show();
    }

    private void abrirConversa(String id, JSONObject ticket) {
        ticketAtual = id;
        chamadoAtual = ticket;
        mensagensAnteriores = "";
        tituloTela.setText(adm ? "CENTRAL DE ATENDIMENTO" : "MEU ATENDIMENTO");
        subtitulo.setText(adm ? "Painel do administrador oficial" :
                ticket.optString("title", "Chamado"));
        visibilidadeAdm();
        limparConteudo();

        // Card luminoso com a identidade do membro. Nunca coloca e-mail como nome.
        LinearLayout cartao = vertical(Color.TRANSPARENT);
        cartao.setBackground(neon(Color.rgb(13, 48, 91), Color.rgb(5, 20, 50), CIANO, 18));
        cartao.setPadding(dp(14), dp(11), dp(14), dp(11));
        LinearLayout linhaPessoa = new LinearLayout(this);
        linhaPessoa.setGravity(Gravity.CENTER_VERTICAL);
        ImageView avatar = new ImageView(this);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageResource(R.drawable.nexo_icon);
        linhaPessoa.addView(avatar, margem(dp(53), dp(53), 0, 0, 13, 0));
        carregarAvatar(avatar, ticket.optString("avatarUrl", ""));
        LinearLayout identidade = vertical(Color.TRANSPARENT);
        identidade.addView(texto(adm ? nomePublico(ticket.optString("name"), false)
                : "Atendimento NEXO", 17, Color.WHITE, true));
        identidade.addView(texto(ticket.optString("title", "Chamado"), 13, CINZA, false));
        etiquetaStatusAtual = selo("●  " + tituloStatus(ticket.optString("status", "aberto")),
                CIANO, Color.rgb(43, 113, 174), Color.rgb(9, 43, 83));
        identidade.addView(etiquetaStatusAtual, margem(-2, -2, 0, 6, 0, 0));
        linhaPessoa.addView(identidade, new LinearLayout.LayoutParams(0, -2, 1));
        cartao.addView(linhaPessoa);
        conteudo.addView(cartao, margem(-1, -2, 0, 8, 0, 9));

        LinearLayout linhaInfo = new LinearLayout(this);
        linhaInfo.setOrientation(LinearLayout.HORIZONTAL);
        linhaInfo.setGravity(Gravity.CENTER_VERTICAL);
        String idCurto = id.length() > 12 ? id.substring(0, 12) + "…" : id;
        TextView referencia = texto("♧  Chamado " + idCurto, 11, CINZA, false);
        linhaInfo.addView(referencia, new LinearLayout.LayoutParams(0, -2, 1));
        if (adm) {
            Button estados = botao("STATUS ▾");
            estados.setBackground(neon(Color.rgb(11, 55, 99), Color.rgb(10, 31, 65), CIANO, 13));
            linhaInfo.addView(estados, new LinearLayout.LayoutParams(dp(104), dp(42)));
            estados.setOnClickListener(v -> mostrarMenuStatus(estados));
        }
        conteudo.addView(linhaInfo);
        statusChamado = texto("Status: " + tituloStatus(ticket.optString("status", "aberto")),
                11, CIANO, false);
        conteudo.addView(statusChamado, margem(-1, -2, 4, 2, 0, 6));

        if (adm) {
            LinearLayout acoes = new LinearLayout(this);
            acoes.setOrientation(LinearLayout.HORIZONTAL);
            acoes.setGravity(Gravity.CENTER_VERTICAL);
            Button responder = botao("Responder");
            responder.setBackground(neon(Color.rgb(14, 99, 173), Color.rgb(7, 53, 111),
                    CIANO, 12));
            acoes.addView(responder, new LinearLayout.LayoutParams(0, dp(45), 1));
            responder.setOnClickListener(v -> responderAgora());
            Button fechar = botao("Encerrar");
            fechar.setBackground(neon(Color.rgb(9, 37, 72), Color.rgb(7, 25, 57),
                    AZUL, 12));
            acoes.addView(fechar, margem(dp(102), dp(45), 7, 0, 6, 0));
            fechar.setOnClickListener(v -> alterarStatus("fechado"));
            Button excluir = botao("Excluir");
            excluir.setTextColor(VERMELHO);
            excluir.setBackground(neon(Color.rgb(51, 12, 32), Color.rgb(35, 7, 23),
                    VERMELHO, 12));
            acoes.addView(excluir, new LinearLayout.LayoutParams(dp(86), dp(45)));
            excluir.setOnClickListener(v -> confirmarExclusao(id, chamadoAtual));
            conteudo.addView(acoes, margem(-1, -2, 0, 3, 0, 9));
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        mensagens = vertical(Color.TRANSPARENT);
        mensagens.setBackground(neon(Color.rgb(6, 23, 51),
                Color.rgb(4, 11, 30), Color.rgb(32, 89, 150), 17));
        mensagens.setPadding(dp(10), dp(11), dp(10), dp(15));
        scroll.addView(mensagens);
        conteudo.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        barraEnvio = new LinearLayout(this);
        barraEnvio.setGravity(Gravity.CENTER_VERTICAL);
        barraEnvio.setPadding(dp(8), dp(6), dp(6), dp(6));
        barraEnvio.setBackground(neon(Color.rgb(8, 29, 66),
                Color.rgb(5, 18, 48), CIANO, 18));
        campoMensagem = new EditText(this);
        campoMensagem.setTextColor(Color.WHITE);
        campoMensagem.setHintTextColor(CINZA);
        campoMensagem.setHint("Escreva sua mensagem...");
        campoMensagem.setMaxLines(4);
        campoMensagem.setTextSize(14);
        campoMensagem.setBackgroundColor(Color.TRANSPARENT);
        barraEnvio.addView(campoMensagem, new LinearLayout.LayoutParams(0, -2, 1));
        enviar = botao("➤");
        enviar.setBackground(neon(Color.rgb(17, 107, 196),
                Color.rgb(5, 54, 122), CIANO, 13));
        barraEnvio.addView(enviar, new LinearLayout.LayoutParams(dp(50), dp(46)));
        enviar.setOnClickListener(v -> enviarMensagem());
        conteudo.addView(barraEnvio, margem(-1, -2, 0, 8, 0, 1));
        mensagens.addView(texto("Carregando conversa...", 14, CINZA, false));
        carregarConversa(true);
    }

    private void carregarConversa(boolean mostrarFalha) {
        if (ticketAtual == null || ocupada) return;
        String id = ticketAtual;
        ocupada = true;
        String rota = "/api/v1/support/" + id + "?limit=100";
        NexoSuporteApi.requisitar("GET", rota, null,
                new NexoSuporteApi.Callback() {
                    @Override public void sucesso(JSONObject res) {
                        noUi(() -> {
                            ocupada = false;
                            if (!id.equals(ticketAtual)) return;
                            JSONObject t = res.optJSONObject("ticket");
                            if (t != null) {
                                chamadoAtual = t;
                                atualizarChipStatus(t.optString("status", "aberto"));
                                boolean fechado = "fechado".equals(t.optString("status"));
                                if (campoMensagem != null) campoMensagem.setEnabled(!fechado);
                                if (enviar != null) enviar.setEnabled(!fechado && !operacao);
                            }
                            JSONArray dados = res.optJSONArray("messages");
                            if (dados == null) dados = res.optJSONArray("items");
                            if (dados == null) dados = new JSONArray();
                            String atual = dados.toString();
                            if (!atual.equals(mensagensAnteriores)) {
                                mensagensAnteriores = atual;
                                desenharMensagens(dados);
                            }
                            mensagemInfo("Atendimento conectado • atualização automática");
                        });
                    }
                    @Override public void erro(String e) {
                        noUi(() -> { ocupada = false; if (mostrarFalha) SuporteActivity.this.erro(e); });
                    }
                });
    }

    private void desenharMensagens(JSONArray dados) {
        if (mensagens == null) return;
        mensagens.removeAllViews();
        if (dados.length() == 0) {
            mensagens.addView(texto("Nenhuma mensagem neste chamado.", 14, CINZA, false));
            return;
        }
        String ultimoDia = "";
        for (int i = 0; i < dados.length(); i++) {
            JSONObject m = dados.optJSONObject(i);
            if (m == null) continue;
            boolean equipe = m.optBoolean("isStaff", false);
            com.google.firebase.auth.FirebaseUser local =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            boolean minha = local != null && local.getUid().equals(m.optString("senderUid"));
            boolean direita = adm ? equipe : minha;
            String nome = nomePublico(m.optString("senderName"), equipe);
            String data = m.optString("createdAt", "");
            if (data.length() >= 10 && !data.substring(0, 10).equals(ultimoDia)) {
                ultimoDia = data.substring(0, 10);
                TextView separador = selo("✦  " + dataCurta(data), CINZA,
                        Color.rgb(41, 88, 144), Color.rgb(9, 30, 62));
                separador.setGravity(Gravity.CENTER);
                mensagens.addView(separador, margem(-1, -2, 18, 2, 18, 12));
            }
            LinearLayout linha = new LinearLayout(this);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            linha.setGravity(direita ? Gravity.RIGHT : Gravity.LEFT);
            ImageView avatar = new ImageView(this);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setImageResource(R.drawable.nexo_icon);
            String foto = m.optString("senderAvatarUrl", "");
            if (!direita) {
                linha.addView(avatar, margem(dp(31), dp(31), 0, 3, 7, 0));
                carregarAvatar(avatar, foto);
            }
            LinearLayout bolha = vertical(Color.TRANSPARENT);
            bolha.setPadding(dp(12), dp(9), dp(12), dp(8));
            int inicio = equipe ? Color.rgb(11, 61, 112) : Color.rgb(18, 43, 77);
            int fim = equipe ? Color.rgb(7, 29, 70) : Color.rgb(9, 25, 55);
            bolha.setBackground(neon(inicio, fim, equipe ? CIANO : Color.rgb(76, 133, 203), 13));
            TextView emissor = texto(nome + (equipe ? "   ♛ ADM" : ""), 12,
                    equipe ? CIANO : AZUL, true);
            if (direita) emissor.setGravity(Gravity.RIGHT);
            bolha.addView(emissor);
            TextView corpo = texto(m.optString("body", ""), 15, Color.WHITE, false);
            corpo.setPadding(0, dp(5), 0, dp(4));
            bolha.addView(corpo);
            TextView horario = texto(data.length() >= 16 ? data.substring(11, 16) + " UTC" : "",
                    10, CINZA, false);
            if (direita) horario.setGravity(Gravity.RIGHT);
            bolha.addView(horario);
            int largura = Math.min(dp(490), Math.round(
                    getResources().getDisplayMetrics().widthPixels * 0.72f));
            linha.addView(bolha, new LinearLayout.LayoutParams(largura, -2));
            if (direita) {
                linha.addView(avatar, margem(dp(31), dp(31), 7, 3, 0, 0));
                carregarAvatar(avatar, foto);
            }
            mensagens.addView(linha, margem(-1, -2, 0, 4, 0, 9));
        }
        if (chamadoAtual != null && "fechado".equals(chamadoAtual.optString("status"))) {
            TextView fim = selo("✓  Atendimento finalizado  •  Fechado",
                    CIANO, Color.rgb(55, 112, 179), Color.rgb(9, 36, 75));
            fim.setGravity(Gravity.CENTER);
            mensagens.addView(fim, margem(-1, -2, 6, 18, 6, 6));
        }
        if (mensagens.getParent() instanceof ScrollView) {
            ScrollView scroll = (ScrollView) mensagens.getParent();
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void enviarMensagem() {
        if (operacao || ticketAtual == null || campoMensagem == null) return;
        String mensagem = campoMensagem.getText().toString().trim();
        if (mensagem.isEmpty()) return;
        if (mensagem.length() > 2000) {
            Toast.makeText(this, "Mensagem acima de 2.000 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        String id = ticketAtual;
        if (!mensagem.equals(textoEnvioPendente) || idEnvioPendente.isEmpty()) {
            idEnvioPendente = UUID.randomUUID().toString();
            textoEnvioPendente = mensagem;
        }
        operacao = true; enviar.setEnabled(false);
        try {
            JSONObject corpo = new JSONObject();
            corpo.put("ticketId", id);
            corpo.put("body", mensagem);
            corpo.put("clientMessageId", idEnvioPendente);
            NexoSuporteApi.requisitar("POST", "/api/v1/support/messages", corpo,
                    new NexoSuporteApi.Callback() {
                        @Override public void sucesso(JSONObject res) {
                            noUi(() -> {
                                operacao = false;
                                if (!id.equals(ticketAtual)) return;
                                campoMensagem.setText("");
                                idEnvioPendente = ""; textoEnvioPendente = "";
                                enviar.setEnabled(true);
                                mensagensAnteriores = "";
                                carregarConversa(true);
                            });
                        }
                        @Override public void erro(String erro) {
                            noUi(() -> {
                                operacao = false;
                                if (id.equals(ticketAtual)) enviar.setEnabled(true);
                                mensagemInfo(erro);
                            });
                        }
                    });
        } catch (Exception e) { operacao = false; enviar.setEnabled(true); }
    }

    private void mostrarMenuStatus(View ancora) {
        if (!adm || chamadoAtual == null || ticketAtual == null) return;
        PopupMenu menu = new PopupMenu(this, ancora);
        String[] status = {"aberto", "em_atendimento", "aguardando_usuario",
                "resolvido", "fechado"};
        for (String st : status) menu.getMenu().add(tituloStatus(st));
        menu.setOnMenuItemClickListener(item -> {
            int indice = -1;
            for (int i = 0; i < status.length; i++) {
                if (tituloStatus(status[i]).equals(item.getTitle().toString())) indice = i;
            }
            if (indice >= 0) alterarStatus(status[indice]);
            return true;
        });
        menu.show();
    }
    private void alterarStatus(String novoStatus) {
        if (!adm || ticketAtual == null || operacao) return;
        String id = ticketAtual;
        new AlertDialog.Builder(this)
                .setTitle("Alterar atendimento")
                .setMessage("Mudar o status para " + tituloStatus(novoStatus) + "?")
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("CONFIRMAR", (d, w) -> {
                    operacao = true;
                    try {
                        JSONObject corpo = new JSONObject();
                        corpo.put("ticketId", id); corpo.put("status", novoStatus);
                        String rota = "fechado".equals(novoStatus)
                                ? "/api/v1/support/" + id + "/close"
                                : "/api/v1/support/status";
                        NexoSuporteApi.requisitar("POST", rota, corpo,
                                new NexoSuporteApi.Callback() {
                                    @Override public void sucesso(JSONObject json) {
                                        noUi(() -> {
                                            operacao = false;
                                            if (id.equals(ticketAtual)) {
                                                mensagensAnteriores = "";
                                                carregarConversa(true);
                                            }
                                        });
                                    }
                                    @Override public void erro(String e) {
                                        noUi(() -> { operacao = false; SuporteActivity.this.erro(e); });
                                    }
                                });
                    } catch (Exception ex) { operacao = false; }
                }).show();
    }
    private String tituloStatus(String st) {
        switch (st) {
            case "aberto": return "Aberto";
            case "em_atendimento": return "Em atendimento";
            case "aguardando_usuario": return "Aguardando usuário";
            case "resolvido": return "Resolvido";
            case "fechado": return "Fechado";
            default: return st;
        }
    }

    private void carregarAvatar(ImageView image, String endereco) {
        if (endereco == null || !endereco.startsWith("https://")) return;
        imagens.execute(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(endereco).openConnection();
                c.setConnectTimeout(6000); c.setReadTimeout(6000);
                c.setInstanceFollowRedirects(true);
                if (c.getResponseCode() != 200 || c.getContentLength() > 1500000) return;
                Bitmap bmp;
                try (InputStream entrada = c.getInputStream()) {
                    bmp = BitmapFactory.decodeStream(entrada);
                }
                if (bmp != null) noUi(() -> image.setImageBitmap(bmp));
            } catch (Exception ignored) { }
            finally { if (c != null) c.disconnect(); }
        });
    }
}
