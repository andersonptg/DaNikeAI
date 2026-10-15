from pathlib import Path
from datetime import datetime
import shutil
import subprocess
import sys

p = Path("gemini_server.py")
s = p.read_text(encoding="utf-8")

marcador = "def data_hora_atual():"

if "def chamar_openrouter(" in s:
    print("A função OpenRouter já existe. Nenhuma alteração feita.")
    sys.exit(0)

if marcador not in s:
    print("ERRO: ponto de inserção não encontrado.")
    sys.exit(1)

backup = Path(
    "gemini_server.py.backup_seguro_" +
    datetime.now().strftime("%Y%m%d_%H%M%S")
)
shutil.copy2(p, backup)

funcao = r'''
def chamar_openrouter(pergunta, chave):
    payload = json.dumps({
        "model": "openrouter/free",
        "messages": [
            {"role": "system", "content": INSTRUCAO},
            {"role": "user", "content": pergunta}
        ],
        "max_tokens": 500
    }).encode("utf-8")

    req = urllib.request.Request(
        "https://openrouter.ai/api/v1/chat/completions",
        data=payload,
        headers={
            "Authorization": "Bearer " + chave,
            "Content-Type": "application/json",
            "Accept": "application/json",
            "X-Title": "DaNikeAI"
        },
        method="POST"
    )

    try:
        with urllib.request.urlopen(req, timeout=60) as resposta:
            return resposta.status, json.loads(
                resposta.read().decode("utf-8")
            )
    except urllib.error.HTTPError as e:
        try:
            dados = json.loads(
                e.read().decode("utf-8", errors="replace")
            )
        except Exception:
            dados = {"erro": "Resposta inválida do OpenRouter"}
        return e.code, dados
    except Exception as e:
        return 0, {"erro": type(e).__name__}


'''

p.write_text(s.replace(marcador, funcao + marcador, 1), encoding="utf-8")

teste = subprocess.run(
    [sys.executable, "-m", "py_compile", str(p)],
    capture_output=True,
    text=True
)

if teste.returncode:
    shutil.copy2(backup, p)
    print("ERRO: backup restaurado:", backup.name)
    print(teste.stderr)
    sys.exit(1)

print("FUNÇÃO ADICIONADA E SINTAXE OK.")
print("Backup:", backup.name)
