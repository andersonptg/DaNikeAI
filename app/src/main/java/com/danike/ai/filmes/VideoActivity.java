package com.danike.ai.filmes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoActivity extends Activity {

    private FrameLayout raiz;
    private WebView webView;
    private String videoId;
    private String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        videoId = getIntent().getStringExtra("VIDEO_ID");
        videoTitle = getIntent().getStringExtra("VIDEO_TITLE");

        if (videoId == null || videoId.trim().isEmpty()) {
            finish();
            return;
        }

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.BLACK);

        criarPlayer();
        criarBarraSuperior();
        criarBarraInferior();

        setContentView(raiz);
    }

    private void criarPlayer() {

        webView = new WebView(this);

        webView.setBackgroundColor(Color.BLACK);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        String html =
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                "<style>" +
                "html,body{margin:0;padding:0;background:#000;width:100%;height:100%;overflow:hidden;}" +
                "#player{width:100%;height:100%;}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div id='player'></div>" +
                "<script>" +
                "var tag=document.createElement('script');" +
                "tag.src='https://www.youtube.com/iframe_api';" +
                "document.head.appendChild(tag);" +
                "var player;" +
                "function onYouTubeIframeAPIReady(){" +
                "player=new YT.Player('player',{" +
                "height:'100%'," +
                "width:'100%'," +
                "videoId:'" + videoId + "'," +
                "playerVars:{" +
                "autoplay:1," +
                "controls:1," +
                "rel:0," +
                "playsinline:1," +
                "modestbranding:1," +
                "fs:1" +
                "}" +
                "});" +
                "}" +
                "</script>" +
                "</body>" +
                "</html>";

        webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                html,
                "text/html",
                "UTF-8",
                null
        );

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );

        raiz.addView(webView, params);
    }

    private void criarBarraSuperior() {

        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.HORIZONTAL);
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setPadding(dp(10), dp(8), dp(10), dp(8));

        barra.setBackgroundColor(Color.argb(210, 5, 8, 15));

        TextView voltar = botao("‹");
        voltar.setTextSize(32);
        voltar.setOnClickListener(v -> finish());

        barra.addView(
                voltar,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                )
        );

        TextView titulo = new TextView(this);
        titulo.setText(
                videoTitle == null || videoTitle.trim().isEmpty()
                        ? "DaNike Filmes"
                        : videoTitle
        );
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(16);
        titulo.setGravity(Gravity.CENTER_VERTICAL);
        titulo.setSingleLine(true);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        barra.addView(titulo, tituloParams);

        TextView compartilhar = botao("↗");
        compartilhar.setTextSize(22);
        compartilhar.setOnClickListener(v -> compartilhar());

        barra.addView(
                compartilhar,
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                )
        );

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(64),
                        Gravity.TOP
                );

        raiz.addView(barra, params);
    }

    private void criarBarraInferior() {

        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.HORIZONTAL);
        barra.setGravity(Gravity.CENTER);
        barra.setPadding(dp(8), dp(8), dp(8), dp(8));

        barra.setBackgroundColor(Color.argb(220, 5, 8, 15));

        TextView lista = botao("＋\nMinha lista");
        lista.setOnClickListener(v -> {
            getPreferences(MODE_PRIVATE)
                    .edit()
                    .putBoolean("favorito_" + videoId, true)
                    .apply();

            lista.setText("✓\nNa lista");
        });

        TextView info = botao("ⓘ\nDetalhes");
        info.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle(videoTitle == null ? "Filme" : videoTitle)
                    .setMessage("Vídeo reproduzido pelo player oficial incorporado.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        TextView compartilhar = botao("↗\nCompartilhar");
        compartilhar.setOnClickListener(v -> compartilhar());

        TextView tela = botao("⛶\nTela cheia");
        tela.setOnClickListener(v -> telaCheia());

        barra.addView(lista, peso());
        barra.addView(info, peso());
        barra.addView(compartilhar, peso());
        barra.addView(tela, peso());

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(72),
                        Gravity.BOTTOM
                );

        raiz.addView(barra, params);
    }

    private TextView botao(String texto) {

        TextView t = new TextView(this);

        t.setText(texto);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(4), dp(2), dp(4), dp(2));
        t.setBackgroundColor(Color.TRANSPARENT);

        return t;
    }

    private LinearLayout.LayoutParams peso() {

        return new LinearLayout.LayoutParams(
                0,
                -1,
                1
        );
    }

    private void compartilhar() {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Assista no DaNikeAI: https://www.youtube.com/watch?v=" + videoId
        );

        startActivity(
                Intent.createChooser(
                        intent,
                        "Compartilhar filme"
                )
        );
    }

    private void telaCheia() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}
