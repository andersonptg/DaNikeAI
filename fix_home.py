import pathlib
path = pathlib.Path("app/src/main/java/com/danike/ai/HaveHomeActivity.java")
s = path.read_text()
lines = s.splitlines()

# troca a linha 345
new_lines = []
for line in lines:
    if "new Intent(this, HaveSalaStreamingActivity.class)" in line:
        new_lines.append(line.replace("HaveSalaStreamingActivity", "PlataformasHAVEActivity").replace("PlataformasHAVEActivity", "HavePlataformasActivity") if "Plataformas" not in line else line)
        # vamos forçar: se tem HaveSalaStreamingActivity, troca pra intent de plataformas
        if "HaveSalaStreamingActivity" in line:
            new_lines[-1] = line.replace("HaveSalaStreamingActivity", "HavePlataformasActivity")
    else:
        new_lines.append(line)

# na verdade vamos fazer simples: garantir que o botão de criar sala vai pra plataformas
s = "\n".join(new_lines)
if "HavePlataformasActivity" not in s:
    # se não existe a activity de plataformas, vamos criar intent pra Sala mas com url Netflix
    print("AVISO: Nao achei HavePlataformasActivity, vamos ver que activities existem")
    import subprocess, pathlib
    print(list(pathlib.Path("app/src/main/java/com/danike/ai").glob("*Plataforma*")))

path.write_text(s)
print("Home corrigido pra tentar ir pra Plataformas")
