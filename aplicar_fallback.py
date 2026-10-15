from pathlib import Path
from datetime import datetime
import shutil
import subprocess
import sys

p = Path("gemini_server.py")
s = p.read_text(encoding="utf-8")

marcador = '''            self.enviar_json(
                429,
                {
                    "erro": "Limite ou indisponibilidade do Gemini.",'''

if "chave_or = os.getenv(" in s:
    print("O fallback já parece estar instalado. Nenhuma alteração feita.")
    sys.exit(0)

if marcador not in s:
    print("ERRO: trecho final do Gemini não encontrado.")
    print("Nenhuma alteração feita.")
    sys.exit(1)

backup = Path(
    "gemini_server.py.backup_fallback_" +
    datetime.now().strftime("%Y%m%d_%H%M%S")
)
shutil.copy2(p, backup)

fallback = '''            chave_or = os.getenv("OPENROUTER_API_KEY", "").strip()
            if chave_or:
                codigo_or, dados_or = chamar_openrouter(
                    pergunta, chave_or
                )
                texto_or = ""

                if codigo_or == 200 and isinstance(dados_or, dict):
                    escolhas = dados_or.get("choices", [])
                    if escolhas and isinstance(escolhas[0], dict):
                        mensagem = escolhas[0].get("message", {})
                        if isinstance(mensagem, dict):
                            conteudo = mensagem.get("content", "")
                            if isinstance(conteudo, str):
                                texto_or = conteudo.strip()
                            elif isinstance(conteudo, list):
                                partes = []
                                for item in conteudo:
                                    if isinstance(item, dict):
                                        trecho = item.get("text", "")
                                        if isinstance(trecho, str):
                                            partes.append(trecho)
                                texto_or = "\\\\n".join(partes).strip()

                if texto_or:
                    self.enviar_json(
                        200,
                        {
                            "text": texto_or,
                            "model": dados_or.get(
                                "model", "openrouter/free"
                            )
                        }
                    )
                    return

                print(
                    "OpenRouter falhou ou retornou resposta vazia. HTTP:",
                    codigo_or
                )

'''

s = s.replace(marcador, fallback + marcador, 1)
p.write_text(s, encoding="utf-8")

teste = subprocess.run(
    [sys.executable, "-m", "py_compile", str(p)],
    capture_output=True,
    text=True
)

if teste.returncode:
    shutil.copy2(backup, p)
    print("ERRO DE SINTAXE. Backup restaurado:", backup.name)
    print(teste.stderr)
    sys.exit(1)

print("FALLBACK ADICIONADO. SINTAXE OK.")
print("Backup:", backup.name)
