package com.danike.ai;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.util.Locale;

public class SalaHaveActivity extends Activity {

    private LinearLayout raiz;
    private LinearLayout conteudo;
    private LinearLayout chat;
    private EditText mensagem;
    private TextView filmeSelecionado;
    private TextView statusFilme;

    private String grupoId = "";
    private String nomeGrupo = "Sala HAVE";
    private String servico = "Netflix";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        grupoId = getIntent().getStringExtra("grupoId");
        nomeGrupo = getIntent().getStringExtra("nomeGrupo");
        servico = getIntent().getStringExtra("servicoHave");

        if (grupoId == null) grupoId = "";
        if (nomeGrupo == null || nomeGrupo.trim().isEmpty())
            nomeGrupo = "Sala HAVE";
        if (servico == null || servico.trim().isEmpty())
            servico = "Netflix";

        montarWatchParty();
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fundo(int cor, int borda, int raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        if (borda != 0)
            g.setStroke(dp(1), borda);
        return g;
    }

    private TextView texto(String t, float tamanho, int cor) {
        TextView v = new TextView(this);
        v.setText(t);
        v.setTextSize(tamanho);
        v.setTextColor(cor);
        return v;
    }

    private Button botao(String t) {
        Button b = new Button(this);
        b.setText(t);
        b.setTextColor(Color.WHITE);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setBackground(
                fundo(Color.rgb(30, 17, 65),
                        Color.rgb(105, 50, 240), 18));
        return b;
    }

    private void montarWatchParty() {

        raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(3, 4, 10));

        // =====================================================
        // TOPO FIXO
        // =====================================================

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(12), dp(8), dp(12), dp(8));

        Button voltar = botao("‹");
        voltar.setTextSize(28);
        voltar.setOnClickListener(v -> finish());

        topo.addView(voltar,
                new LinearLayout.LayoutParams(dp(52), dp(54)));

        LinearLayout titulo = new LinearLayout(this);
        titulo.setOrientation(LinearLayout.VERTICAL);

        TextView t1 = texto("HAVE", 12,
                Color.rgb(155, 100, 255));
        t1.setTypeface(Typeface.DEFAULT_BOLD);

        TextView t2 = texto(nomeGrupo, 18, Color.WHITE);
        t2.setTypeface(Typeface.DEFAULT_BOLD);

        titulo.addView(t1);
        titulo.addView(t2);

        topo.addView(titulo,
                new LinearLayout.LayoutParams(0, dp(54), 1));

        Button perfil = botao("👤");
        perfil.setOnClickListener(v ->
                Toast.makeText(this,
                        "Seu perfil HAVE",
                        Toast.LENGTH_SHORT).show());

        topo.addView(perfil,
                new LinearLayout.LayoutParams(dp(55), dp(50)));

        Button online = botao("🟢 5");
        online.setOnClickListener(v ->
                Toast.makeText(this,
                        "5 pessoas online",
                        Toast.LENGTH_SHORT).show());

        topo.addView(online,
                new LinearLayout.LayoutParams(dp(75), dp(50)));

        raiz.addView(topo);

        // =====================================================
        // CONTEÚDO ROLÁVEL
        // =====================================================

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(14), 0, dp(14), dp(25));

        scroll.addView(conteudo);
        raiz.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1));

        // =====================================================
        // SERVIÇO
        // =====================================================

        TextView serv = texto(
                "● " + servico.toUpperCase(Locale.ROOT),
                11,
                Color.rgb(100, 230, 190));
        serv.setTypeface(Typeface.DEFAULT_BOLD);

        serv.setPadding(dp(5), dp(8), 0, dp(8));
        conteudo.addView(serv);

        // =====================================================
        // FILME
        // =====================================================

        LinearLayout filme = new LinearLayout(this);
        filme.setOrientation(LinearLayout.VERTICAL);
        filme.setGravity(Gravity.CENTER);
        filme.setPadding(dp(20), dp(22), dp(20), dp(22));
        filme.setBackground(
                fundo(Color.rgb(10, 8, 23),
                        Color.rgb(105, 50, 240), 24));

        TextView etiqueta = texto(
                "🎬  FILME DA SALA",
                11,
                Color.rgb(170, 120, 255));
        etiqueta.setGravity(Gravity.CENTER);

        filmeSelecionado = texto(
                "Nenhum filme selecionado",
                24,
                Color.WHITE);
        filmeSelecionado.setGravity(Gravity.CENTER);
        filmeSelecionado.setTypeface(Typeface.DEFAULT_BOLD);
        filmeSelecionado.setPadding(0, dp(15), 0, dp(8));

        statusFilme = texto(
                "Escolha um filme para começar",
                13,
                Color.LTGRAY);
        statusFilme.setGravity(Gravity.CENTER);

        filme.addView(etiqueta);
        filme.addView(filmeSelecionado);
        filme.addView(statusFilme);

        LinearLayout acoesFilme = new LinearLayout(this);
        acoesFilme.setPadding(0, dp(18), 0, 0);

        Button escolher = botao("🎞️  ESCOLHER FILME");
        escolher.setOnClickListener(v -> abrirServico());

        Button play = botao("▶  DAR PLAY");
        play.setOnClickListener(v -> iniciarFilme());

        acoesFilme.addView(escolher,
                new LinearLayout.LayoutParams(0, dp(50), 1));

        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                0, dp(50), 1);
        pp.setMargins(dp(8), 0, 0, 0);

        acoesFilme.addView(play, pp);

        filme.addView(acoesFilme);
        conteudo.addView(filme);

        // =====================================================
        // COMPARTILHAR
        // =====================================================

        LinearLayout compartilhar = new LinearLayout(this);
        compartilhar.setOrientation(LinearLayout.VERTICAL);
        compartilhar.setPadding(dp(16), dp(16), dp(16), dp(16));
        compartilhar.setBackground(
                fundo(Color.rgb(8, 10, 20),
                        Color.rgb(35, 100, 150), 20));

        TextView cs = texto(
                "FILME SELECIONADO",
                11,
                Color.rgb(100, 210, 255));
        cs.setTypeface(Typeface.DEFAULT_BOLD);

        TextView cst = texto(
                "Compartilhe o filme com as pessoas da sala.",
                14, Color.WHITE);

        Button comp = botao("🔗  COMPARTILHAR COM A SALA");
        comp.setOnClickListener(v -> compartilharFilme());

        Button depois = botao("DEIXAR PARA DEPOIS");
        depois.setOnClickListener(v ->
                Toast.makeText(this,
                        "Você pode compartilhar depois.",
                        Toast.LENGTH_SHORT).show());

        compartilhar.addView(cs);
        compartilhar.addView(cst);

        LinearLayout ca = new LinearLayout(this);
        ca.setPadding(0, dp(12), 0, 0);

        ca.addView(comp,
                new LinearLayout.LayoutParams(0, dp(50), 1));

        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(
                0, dp(50), 1);
        dp2.setMargins(dp(8), 0, 0, 0);
        ca.addView(depois, dp2);

        compartilhar.addView(ca);
        conteudo.addView(compartilhar);

        // =====================================================
        // PARTICIPANTES
        // =====================================================

        TextView pt = texto(
                "👥  PESSOAS NA SALA",
                18, Color.WHITE);
        pt.setTypeface(Typeface.DEFAULT_BOLD);
        pt.setPadding(dp(4), dp(24), 0, dp(10));
        conteudo.addView(pt);

        LinearLayout pessoas = new LinearLayout(this);
        pessoas.setOrientation(LinearLayout.VERTICAL);
        pessoas.setPadding(dp(15), dp(12), dp(15), dp(12));
        pessoas.setBackground(
                fundo(Color.rgb(8, 10, 20),
                        Color.rgb(50, 35, 110), 18));

        pessoas.addView(pessoa("Você", "🟢 Assistindo"));
        pessoas.addView(pessoa("Participante 2", "🟢 Online"));
        pessoas.addView(pessoa("Participante 3", "🟢 Online"));

        Button convidar = botao("＋  CONVIDAR PESSOAS");
        convidar.setOnClickListener(v -> convidarPessoas());

        pessoas.addView(convidar);
        conteudo.addView(pessoas);

        // =====================================================
        // CHAT
        // =====================================================

        TextView ct = texto(
                "💬  MENSAGENS DA SALA",
                18, Color.WHITE);
        ct.setTypeface(Typeface.DEFAULT_BOLD);
        ct.setPadding(dp(4), dp(24), 0, dp(10));
        conteudo.addView(ct);

        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(dp(14), dp(14), dp(14), dp(8));
        chat.setBackground(
                fundo(Color.rgb(7, 9, 17),
                        Color.rgb(35, 35, 70), 18));

        adicionarMensagem("HAVE",
                "A sala está pronta. 🎬");
        adicionarMensagem("Sistema",
                "Escolha um filme para começar.");
        adicionarMensagem("Você",
                "Bora assistir!");

        conteudo.addView(chat);

        LinearLayout enviar = new LinearLayout(this);
        enviar.setGravity(Gravity.CENTER_VERTICAL);
        enviar.setPadding(0, dp(8), 0, 0);

        mensagem = new EditText(this);
        mensagem.setHint("Digite uma mensagem...");
        mensagem.setTextColor(Color.WHITE);
        mensagem.setHintTextColor(Color.GRAY);
        mensagem.setSingleLine(true);
        mensagem.setPadding(dp(14), 0, dp(14), 0);
        mensagem.setBackground(
                fundo(Color.rgb(15, 16, 27),
                        Color.rgb(55, 55, 90), 18));

        Button send = botao("➤");
        send.setOnClickListener(v -> enviarMensagem());

        enviar.addView(mensagem,
                new LinearLayout.LayoutParams(0, dp(52), 1));

        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                dp(60), dp(52));
        sp.setMargins(dp(8), 0, 0, 0);

        enviar.addView(send, sp);
        conteudo.addView(enviar);

        // =====================================================
        // CATÁLOGO
        // =====================================================

        TextView cat = texto(
                "🎬  MAIS FILMES",
                18, Color.WHITE);
        cat.setTypeface(Typeface.DEFAULT_BOLD);
        cat.setPadding(dp(4), dp(28), 0, dp(10));
        conteudo.addView(cat);

        adicionarCatalogo("Filmes e séries disponíveis");
        adicionarCatalogo("Lançamentos");
        adicionarCatalogo("Mais assistidos");
        adicionarCatalogo("Para assistir juntos");

        // =====================================================
        // CÓDIGO
        // =====================================================

        TextView codigo = texto(
                "🔗 Código da sala: " + grupoId,
                12, Color.LTGRAY);
        codigo.setPadding(dp(5), dp(25), 0, dp(8));
        conteudo.addView(codigo);

        Button copiar = botao("COPIAR CÓDIGO DA SALA");
        copiar.setOnClickListener(v -> copiarCodigo());
        conteudo.addView(copiar,
                new LinearLayout.LayoutParams(-1, dp(50)));

        setContentView(raiz);
    }

    private TextView pessoa(String nome, String estado) {
        TextView p = texto(
                "👤  " + nome + "     " + estado,
                14, Color.WHITE);
        p.setPadding(0, dp(9), 0, dp(9));
        return p;
    }

    private void adicionarMensagem(String nome, String msg) {
        TextView t = texto(
                nome + ":  " + msg,
                13, Color.LTGRAY);
        t.setPadding(0, dp(7), 0, dp(7));
        chat.addView(t);
    }

    private void enviarMensagem() {
        String msg = mensagem.getText().toString().trim();

        if (msg.isEmpty())
            return;

        adicionarMensagem("Você", msg);
        mensagem.setText("");
    }

    private void adicionarCatalogo(String titulo) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(15), dp(12), dp(15), dp(12));
        card.setBackground(
                fundo(Color.rgb(9, 10, 18),
                        Color.rgb(40, 30, 85), 18));

        TextView t = texto(
                "🎞️  " + titulo,
                14, Color.WHITE);

        Button abrir = botao("VER");
        abrir.setOnClickListener(v -> abrirServico());

        card.addView(t,
                new LinearLayout.LayoutParams(0, dp(58), 1));

        card.addView(abrir,
                new LinearLayout.LayoutParams(dp(70), dp(46)));

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(-1, dp(74));
        cp.setMargins(0, 0, 0, dp(8));

        conteudo.addView(card, cp);
    }

    private void compartilharFilme() {
        String texto =
                "🎬 Filme da Sala HAVE\n" +
                nomeGrupo + "\n" +
                "Serviço: " + servico + "\n" +
                "Sala: " + grupoId;

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, texto);

        startActivity(Intent.createChooser(
                i, "Compartilhar filme"));
    }

    private void iniciarFilme() {
        statusFilme.setText(
                "● SESSÃO PRONTA • Abra o " + servico);
        Toast.makeText(this,
                "Sessão HAVE preparada.",
                Toast.LENGTH_SHORT).show();
        abrirServico();
    }

    private void convidarPessoas() {
        String link =
                "https://danikeai.onrender.com/have/sala/" +
                grupoId;

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,
                "🎬 Você foi convidado para uma Sala HAVE!\n\n" +
                nomeGrupo + "\n" +
                "Código: " + grupoId + "\n" +
                link);

        startActivity(Intent.createChooser(
                i, "Convidar pessoas"));
    }

    private void copiarCodigo() {
        ClipboardManager cm =
                (ClipboardManager)getSystemService(
                        Context.CLIPBOARD_SERVICE);

        cm.setPrimaryClip(
                ClipData.newPlainText(
                        "Código HAVE", grupoId));

        Toast.makeText(this,
                "Código copiado.",
                Toast.LENGTH_SHORT).show();
    }

    private void abrirServico() {
        String pacote = "";

        if (servico.equalsIgnoreCase("Netflix"))
            pacote = "com.netflix.mediaclient";
        else if (servico.equalsIgnoreCase("Prime Video"))
            pacote = "com.amazon.avod.thirdpartyclient";
        else if (servico.equalsIgnoreCase("Disney+"))
            pacote = "com.disney.disneyplus";
        else if (servico.equalsIgnoreCase("Max"))
            pacote = "com.hbo.hbonow";
        else if (servico.equalsIgnoreCase("Globoplay"))
            pacote = "com.globo.globotv";

        try {
            Intent app =
                    getPackageManager()
                            .getLaunchIntentForPackage(pacote);

            if (app != null) {
                startActivity(app);
                return;
            }
        } catch (Exception ignored) {}

        String url;

        if (servico.equalsIgnoreCase("Netflix"))
            url = "https://www.netflix.com/browse";
        else if (servico.equalsIgnoreCase("Prime Video"))
            url = "https://www.primevideo.com/";
        else if (servico.equalsIgnoreCase("Disney+"))
            url = "https://www.disneyplus.com/";
        else if (servico.equalsIgnoreCase("Max"))
            url = "https://www.max.com/";
        else
            url = "https://globoplay.globo.com/";

        startActivity(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)));
    }
}
