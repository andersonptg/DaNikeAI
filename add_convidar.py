import pathlib
f = pathlib.Path("app/src/main/java/com/danike/ai/SalaHAVEActivity.java")
s = f.read_text()
if "Intent.ACTION_SEND" in s:
    print("JA TEM CODIGO")
else:
    code = '''
        findViewById(R.id.btnConvidar).setOnClickListener(v -> {
            String link = "https://have.danike.ai/sala/" + System.currentTimeMillis();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "Entra na minha sala HAVE e vamos assistir juntos: " + link);
            startActivity(Intent.createChooser(share, "Convidar amigos"));
        });
'''
    # insere depois do setContentView
    if "setContentView" in s:
        s = s.replace("setContentView", code + "\n setContentView", 1)
        # move o código para depois na verdade, truque:
        s = s.replace(code + "\n setContentView", "setContentView")
        parts = s.split("setContentView")
        s = parts[0] + "setContentView" + parts[1] + code + "".join(parts[2:])
    f.write_text(s)
    print("INJETADO OK")
