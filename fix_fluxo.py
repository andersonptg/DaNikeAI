import pathlib
# 1. Na Home, o botão + tem que ir para Plataformas, não pra Sala direto
f_home = pathlib.Path("app/src/main/java/com/danike/ai/HomeHAVEActivity.java")
if f_home.exists():
    s = f_home.read_text()
    # Faz o btnCriarSala ir pra Plataformas
    if "PlataformasHAVEActivity" not in s:
        s = s.replace("SalaHAVEActivity", "PlataformasHAVEActivity")
        f_home.write_text(s)
        print("HOME -> agora vai pra PLATAFORMAS")

# 2. Na Plataformas, clicar no Netflix tem que ir pra Sala já com a URL
f_plat = pathlib.Path("app/src/main/java/com/danike/ai/PlataformasHAVEActivity.java")
if f_plat.exists():
    s = f_plat.read_text()
    if "putExtra(\"url\"" not in s:
        # adiciona o putExtra
        s = s.replace("new Intent(this, SalaHAVEActivity.class)", "Intent i = new Intent(this, SalaHAVEActivity.class);\n                i.putExtra(\"url\", \"https://www.netflix.com/browse\");\n                startActivity(i);\n                Intent dummy = new Intent(this, SalaHAVEActivity.class")
        # limpa gambiarra se duplicou
        if "dummy" in s:
            s = f_plat.read_text() # reseta
            s = s.replace("startActivity(new Intent(this, SalaHAVEActivity.class))", "Intent intent = new Intent(this, SalaHAVEActivity.class);\n                intent.putExtra(\"url\", \"https://www.netflix.com/browse\");\n                startActivity(intent)")
            f_plat.write_text(s)
            print("PLATAFORMAS -> agora manda URL pra SALA")
