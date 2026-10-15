import pathlib
p = pathlib.Path("app/src/main/java/com/danike/ai/HaveHomeActivity.java")
s = p.read_text()
s = s.replace("HaveSalaStreamingActivity", "HavePlataformasActivity")
s = s.replace("HavePlataformasActivity.class", "HavePlataformasActivity.class") # ja ta
# se tiver alguma linha ainda chamando SalaHAVE direto no botao de criar, troca
s = s.replace("new Intent(this, SalaHAVEActivity.class)", "new Intent(this, HavePlataformasActivity.class)")
p.write_text(s)
print("Home -> Plataformas corrigido")
