package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HaveSalaStreamingActivity extends Activity {
    private WebView navegador;
    private ProgressBar progresso;
    private int corNeon = Color.rgb(170, 80, 255);

    private int dp(float valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fundo(int cor, int raio) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(cor);
        d.setCornerRadius(dp(raio));
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
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);

        String nome = getIntent().getStringExtra("servico_nome");
        String url = getIntent().getStringExtra("servico_url");
        corNeon = getIntent().getIntExtra("servico_cor",
                Color.rgb(170, 80, 255));

        if (nome == null || nome.trim().isEmpty()) nome = "STREAMING";
        if (url == null || !url.startsWith("https://")) {
            Toast.makeText(this, "Endereço do serviço inválido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        getWindow().setStatusBarColor(Color.rgb(3, 4, 15));
        getWindow().setNavigationBarColor(Color.rgb(3, 4, 15));

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setPadding(dp(10), dp(8), dp(10), dp(8));
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(10), dp(8), dp(10), dp(8));

        GradientDrawable fundoTopo = fundo(Color.rgb(8, 10, 23), 14);
        fundoTopo.setStroke(dp(1), corNeon);
        topo.setBackground(fundoTopo);

        LinearLayout marca = new LinearLayout(this);
        marca.setOrientation(LinearLayout.VERTICAL);

        TextView have = texto("HAVE", 20, Color.WHITE);
        have.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView subtitulo = texto("SALA DE STREAMING", 9,
                Color.rgb(185, 135, 255));
        marca.addView(have);
        marca.addView(subtitulo);

        topo.addView(marca, new LinearLayout.LayoutParams(0, -2, 1));

        TextView fechar = texto("✕", 23, Color.WHITE);
        fechar.setGravity(Gravity.CENTER);
        fechar.setContentDescription("Fechar sala");
        topo.addView(fechar, new LinearLayout.LayoutParams(dp(42), dp(42)));
        fechar.setOnClickListener(v -> finish());

        raiz.addView(topo, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout identificacao = new LinearLayout(this);
        identificacao.setGravity(Gravity.CENTER);
        identificacao.setOrientation(LinearLayout.VERTICAL);
        identificacao.setPadding(dp(6), dp(10), dp(6), dp(10));

        TextView nomeServico = texto(nome.toUpperCase(), 15, Color.WHITE);
        nomeServico.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        identificacao.addView(nomeServico);

        TextView statusServico = texto("CONECTANDO AO SERVIÇO", 10,
                Color.rgb(175, 160, 205));
        statusServico.setPadding(0, dp(3), 0, 0);
        identificacao.addView(statusServico);

        raiz.addView(identificacao, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout moldura = new FrameLayout(this);
        GradientDrawable borda = fundo(Color.rgb(7, 8, 15), 12);
        borda.setStroke(dp(2), corNeon);
        moldura.setBackground(borda);
        moldura.setPadding(dp(2), dp(2), dp(2), dp(2));

        navegador = new WebView(this);
        navegador.setBackgroundColor(Color.BLACK);

        WebSettings configuracao = navegador.getSettings();
        configuracao.setJavaScriptEnabled(true);
        configuracao.setDomStorageEnabled(true);
        configuracao.setLoadWithOverviewMode(true);
        configuracao.setUseWideViewPort(true);
        configuracao.setMediaPlaybackRequiresUserGesture(true);

        navegador.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String endereco) {
                super.onPageFinished(view, endereco);
                progresso.setVisibility(View.GONE);
                statusServico.setText("SERVIÇO CARREGADO");
            }

            @Override
            public void onReceivedError(WebView view,
                    android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    statusServico.setText("FALHA AO CARREGAR — VERIFIQUE A CONEXÃO");
                }
            }
        });

        navegador.setWebChromeClient(new WebChromeClient());

        moldura.addView(navegador, new FrameLayout.LayoutParams(-1, -1));

        progresso = new ProgressBar(this);
        FrameLayout.LayoutParams lpProgresso = new FrameLayout.LayoutParams(
                dp(42), dp(42), Gravity.CENTER);
        moldura.addView(progresso, lpProgresso);

        raiz.addView(moldura, new LinearLayout.LayoutParams(
                -1, 0, 1f));

        TextView rodape = texto("●  HAVE  •  CONEXÃO COM O SERVIÇO",
                10, Color.rgb(165, 135, 210));
        rodape.setGravity(Gravity.CENTER);
        rodape.setPadding(0, dp(10), 0, dp(5));
        raiz.addView(rodape, new LinearLayout.LayoutParams(-1, -2));

        setContentView(raiz);
        navegador.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (navegador != null && navegador.canGoBack()) {
            navegador.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (navegador != null) {
            navegador.stopLoading();
            navegador.destroy();
            navegador = null;
        }
        super.onDestroy();
    }
}
