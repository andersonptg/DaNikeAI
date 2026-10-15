import pathlib
p = pathlib.Path("app/src/main/java/com/danike/ai/SalaHAVEActivity.java")
s = p.read_text()

# Adiciona imports se não tiver
if "import android.webkit" not in s:
    s = s.replace("import android.os.Bundle;", "import android.os.Bundle;\nimport android.webkit.WebView;\nimport android.webkit.WebViewClient;\nimport android.webkit.WebSettings;")

webview_code = '''
        WebView webView = findViewById(R.id.webViewSala);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        // user-agent de desktop pra Netflix não bloquear
        ws.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36");
        webView.loadUrl("https://www.netflix.com/browse");
'''

if "webView.loadUrl" not in s:
    # insere depois do setContentView
    if "setContentView" in s:
        parts = s.split("setContentView", 1)
        # pega o que vem depois do ;
        after = parts[1].split(";", 1)
        s = parts[0] + "setContentView" + after[0] + ";" + webview_code + after[1]
        print("WEBVIEW NETFLIX ADICIONADO")
    p.write_text(s)
else:
    print("JA TEM WEBVIEW")
