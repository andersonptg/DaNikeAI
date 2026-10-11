package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MagnexpActivity extends Activity {
    private WebView webView;
    private boolean falando = false;

    private final class MagnexpBridge {
        @JavascriptInterface
        public void onStateChanged(boolean ativo) {
            runOnUiThread(() -> falando = ativo);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(3, 7, 17));
        getWindow().setNavigationBarColor(Color.rgb(3, 7, 17));

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(3, 7, 17));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        webView.setBackgroundColor(Color.rgb(3, 7, 17));
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new MagnexpBridge(), "AndroidMagnexp");

        raiz.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(raiz);
        webView.loadUrl("file:///android_asset/magnexp/index.html");
    }

    public void definirFalando(boolean ativo) {
        falando = ativo;
        if (webView == null) return;

        final String comando = ativo ? "true" : "false";
        webView.post(() -> webView.evaluateJavascript(
                "if(window.DaNikeMagnexp){" +
                "window.DaNikeMagnexp.setSpeaking(" + comando + ");}", null));
    }

    public boolean estaFalando() {
        return falando;
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onBackPressed();
    }
}
