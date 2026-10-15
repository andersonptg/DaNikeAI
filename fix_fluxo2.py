import pathlib
home = pathlib.Path("app/src/main/java/com/danike/ai/HaveHomeActivity.java")
sala = pathlib.Path("app/src/main/java/com/danike/ai/SalaHAVEActivity.java")
stream = pathlib.Path("app/src/main/java/com/danike/ai/HaveSalaStreamingActivity.java")

# 1. HOME tem que ir pra lista de plataformas (se tiver)
for p in [home, stream]:
    if p.exists():
        txt = p.read_text()
        print(f"--- {p.name} ---")
        # mostra onde chama a sala
        for i, line in enumerate(txt.splitlines()):
            if "Intent" in line and "Sala" in line:
                print(f"Linha {i}: {line.strip()}")

# 2. Corrige a Sala pra pegar URL da Netflix primeiro
if sala.exists():
    s = sala.read_text()
    if 'getStringExtra("url")' not in s:
        code = '\n String url = getIntent().getStringExtra("url");\n if (url == null) url = "https://www.netflix.com/browse";\n'
        if "super.onCreate" in s:
            parts = s.split("super.onCreate(savedInstanceState);",1)
            s = parts[0] + "super.onCreate(savedInstanceState);" + code + parts[1]
            # depois a webview usa essa url
            s = s.replace('webView.loadUrl("https://www.netflix.com/browse")', 'webView.loadUrl(url)')
            sala.write_text(s)
            print("FLUXO CORRIGIDO: Sala agora abre a URL que vem da Plataforma")
        else:
            print("Nao achei onCreate")
    else:
        print("Ja esta pegando URL")
