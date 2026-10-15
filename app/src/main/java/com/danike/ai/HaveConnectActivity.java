package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.Locale;

public class HaveConnectActivity extends Activity {
    private LinearLayout lista;
    private EditText busca;

    private final String[][] servicos = {
        {"NETFLIX","Filmes e séries","#E50914","https://www.netflix.com"},
        {"HBO MAX","Filmes e séries","#7466FF","https://www.max.com"},
        {"AMAZON PRIME","Prime Video","#39A9F8","https://www.primevideo.com"},
        {"DISNEY+","Filmes e séries","#3976E8","https://www.disneyplus.com"},
        {"YOUTUBE","Vídeos e trailers","#FF4242","https://www.youtube.com"},
        {"APPLE TV+","Filmes e originais","#C7C9D8","https://tv.apple.com"},
        {"PARAMOUNT+","Filmes e séries","#547BFF","https://www.paramountplus.com"},
        {"GLOBOPLAY","TV e entretenimento","#FF7549","https://globoplay.globo.com"},
        {"CRUNCHYROLL","Anime","#FF9D32","https://www.crunchyroll.com"},
        {"PLUTO TV","Canais gratuitos","#A58AFF","https://pluto.tv"},
        {"PLEX","Filmes e mídia","#F5C64D","https://www.plex.tv"},
        {"MUBI","Cinema selecionado","#8BB5FF","https://mubi.com"},
        {"TWITCH","Transmissões ao vivo","#B68AFF","https://www.twitch.tv"},
        {"CLARO TV+","TV e entretenimento","#FF555D","https://www.clarotvmais.com.br"},
        {"TELECINE","Filmes","#FF6684","https://www.telecine.com.br"},
        {"DISCOVERY+","Documentários","#57B9F5","https://www.discoveryplus.com"}
    };

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fundo(int cor, int raio, int borda) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(cor);
        d.setCornerRadius(dp(raio));
        if (borda != 0) d.setStroke(dp(1), borda);
        return d;
    }

    private TextView texto(String valor, int tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 17));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 17));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(5, 6, 17));
        raiz.setPadding(dp(18), dp(10), dp(18), dp(8));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setOrientation(LinearLayout.HORIZONTAL);

        TextView voltar = texto("‹", 38, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        topo.addView(voltar, new LinearLayout.LayoutParams(dp(42), dp(48)));
        voltar.setOnClickListener(v -> finish());

        LinearLayout titulos = new LinearLayout(this);
        titulos.setOrientation(LinearLayout.VERTICAL);
        TextView titulo = texto("HAVE CONNECT", 21, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView subtitulo = texto("Seus streamings em um só lugar", 12, Color.rgb(150, 180, 220));
        titulos.addView(titulo);
        titulos.addView(subtitulo);
        topo.addView(titulos, new LinearLayout.LayoutParams(0, -2, 1));
        raiz.addView(topo);

        View linha = new View(this);
        linha.setBackgroundColor(Color.rgb(35, 170, 255));
        LinearLayout.LayoutParams linhaParams = new LinearLayout.LayoutParams(-1, dp(1));
        linhaParams.topMargin = dp(10);
        linhaParams.bottomMargin = dp(18);
        raiz.addView(linha, linhaParams);

        TextView escolha = texto("Escolha um aplicativo", 19, Color.WHITE);
        escolha.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        raiz.addView(escolha);

        TextView detalhe = texto("Entre no serviço desejado para continuar.", 13, Color.rgb(165, 175, 205));
        LinearLayout.LayoutParams detalheParams = new LinearLayout.LayoutParams(-1, -2);
        detalheParams.topMargin = dp(5);
        detalheParams.bottomMargin = dp(14);
        raiz.addView(detalhe, detalheParams);

        busca = new EditText(this);
        busca.setSingleLine(true);
        busca.setTextSize(14);
        busca.setTextColor(Color.WHITE);
        busca.setHintTextColor(Color.rgb(130, 145, 175));
        busca.setHint("⌕  Buscar aplicativos...");
        busca.setPadding(dp(14), 0, dp(14), 0);
        busca.setBackground(fundo(Color.rgb(15, 20, 38), 14, Color.rgb(45, 85, 145)));
        raiz.addView(busca, new LinearLayout.LayoutParams(-1, dp(48)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listaParams = new LinearLayout.LayoutParams(-1, -2);
        listaParams.topMargin = dp(15);
        scroll.addView(lista);
        raiz.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView rodape = texto("HAVE  •  CONECTE-SE AO SEU ENTRETENIMENTO", 10, Color.rgb(100, 130, 175));
        rodape.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rodapeParams = new LinearLayout.LayoutParams(-1, dp(30));
        rodapeParams.topMargin = dp(5);
        raiz.addView(rodape, rodapeParams);

        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarLista(s.toString());
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        setContentView(raiz);
        atualizarLista("");
    }

    private void atualizarLista(String filtro) {
        lista.removeAllViews();
        String termo = filtro.toLowerCase(Locale.ROOT).trim();
        LinearLayout linha = null;
        int encontrados = 0;

        for (String[] app : servicos) {
            if (!app[0].toLowerCase(Locale.ROOT).contains(termo)
                    && !app[1].toLowerCase(Locale.ROOT).contains(termo)) continue;

            if (encontrados % 2 == 0) {
                linha = new LinearLayout(this);
                linha.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
                rowParams.bottomMargin = dp(10);
                lista.addView(linha, rowParams);
            }

            linha.addView(criarCartao(app), new LinearLayout.LayoutParams(0, dp(112), 1));
            if (encontrados % 2 == 0) {
                linha.addView(new View(this), new LinearLayout.LayoutParams(dp(10), 1));
            }
            encontrados++;
        }

        if (encontrados == 0) {
            TextView vazio = texto("Nenhum aplicativo encontrado.", 14, Color.rgb(165, 180, 210));
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(0, dp(28), 0, dp(28));
            lista.addView(vazio);
        } else if (encontrados % 2 != 0 && linha != null) {
            linha.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        }
    }

    private View criarCartao(String[] app) {
        LinearLayout cartao = new LinearLayout(this);
        cartao.setOrientation(LinearLayout.VERTICAL);
        cartao.setGravity(Gravity.CENTER_VERTICAL);
        cartao.setPadding(dp(12), dp(10), dp(8), dp(9));
        int cor = Color.parseColor(app[2]);
        cartao.setBackground(fundo(Color.rgb(13, 18, 34), 15, Color.rgb(42, 62, 100)));

        TextView nome = texto(app[0], 13, cor);
        nome.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nome.setMaxLines(1);
        cartao.addView(nome);

        TextView descricao = texto(app[1], 11, Color.rgb(165, 178, 205));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(5);
        cartao.addView(descricao, descParams);

        TextView abrir = texto("ABRIR  ›", 11, Color.rgb(60, 195, 255));
        abrir.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams abrirParams = new LinearLayout.LayoutParams(-1, -2);
        abrirParams.topMargin = dp(9);
        cartao.addView(abrir, abrirParams);

        cartao.setOnClickListener(v -> {
            Intent i = new Intent(HaveConnectActivity.this, HaveServicoActivity.class);
            i.putExtra("nome", app[0]);
            i.putExtra("url", app[3]);
            startActivity(i);
        });
        return cartao;
    }
}
