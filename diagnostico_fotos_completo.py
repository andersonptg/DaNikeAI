from pathlib import Path
from datetime import datetime
import subprocess
import urllib.request
import urllib.error
import re
import sys

ROOT = Path.home() / "da_nike_ai"
PERFIL = ROOT / "app/src/main/java/com/danike/ai/PerfilFirebase.java"
ADM = ROOT / "app/src/main/java/com/danike/ai/AdmActivity.java"
BACKEND = ROOT / "perfil_backend/server.py"
RELATORIO = ROOT / ("DIAGNOSTICO_FOTOS_" +
                    datetime.now().strftime("%Y%m%d_%H%M%S") + ".txt")

linhas = []

def log(s=""):
    print(s)
    linhas.append(str(s))

def executar(args, timeout=180):
    try:
        r = subprocess.run(
            args, cwd=ROOT, text=True,
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            timeout=timeout
        )
        return r.returncode, r.stdout
    except Exception as e:
        return 1, str(e)

def trecho(caminho, inicio, fim):
    conteudo = caminho.read_text(encoding="utf-8", errors="replace")
    ls = conteudo.splitlines()
    return "\n".join(
        f"{i+1}: {ls[i]}" for i in range(inicio-1, min(fim, len(ls)))
    )

def verificar_url(url):
    req = urllib.request.Request(url, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=25) as resp:
            return resp.status, resp.read(2000).decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read(2000).decode("utf-8", "replace")
    except Exception as e:
        return None, str(e)

def main():
    if not ROOT.is_dir() or not PERFIL.is_file() or not ADM.is_file():
        log("ERRO: arquivos essenciais ausentes. Nenhuma alteração feita.")
        return 1

    perfil = PERFIL.read_text(encoding="utf-8", errors="replace")
    adm = ADM.read_text(encoding="utf-8", errors="replace")
    backend = (
        BACKEND.read_text(encoding="utf-8", errors="replace")
        if BACKEND.is_file() else ""
    )

    log("===== 1. ROTAS E ARMAZENAMENTO =====")
    for nome, conteudo in [
        ("PERFIL PESSOAL", perfil),
        ("ADM — SOMENTE LEITURA", adm),
        ("BACKEND PESSOAL", backend),
    ]:
        log(f"\n--- {nome} ---")
        for i, linha in enumerate(conteudo.splitlines(), 1):
            if re.search(
                r"danikeai-perfil|upload-photo|delete-photo|"
                r"uploadFotoEquipeCloudinary|CLOUDINARY_UPLOAD_PRESET|"
                r"getDownloadUrl|putFile|putBytes|fotoUrl|fotoPublicId|"
                r"secure_url|public_id|upload_preset|folder|"
                r"COLECAO_PRIVADA|COLECAO_PUBLICA|collection\(\"equipe\"\)",
                linha, re.I
            ):
                log(f"{i}: {linha.strip()}")

    log("\n===== 2. TRECHO COMPLETO DO UPLOAD PESSOAL =====")
    log(trecho(PERFIL, 490, 715))

    log("\n===== 3. TRECHO COMPLETO DA EXCLUSÃO PESSOAL =====")
    log(trecho(PERFIL, 718, 880))

    log("\n===== 4. UPLOAD DIRETO DO ADM — NÃO ALTERAR =====")
    log(trecho(ADM, 3020, 3165))

    log("\n===== 5. TESTE DO SERVIÇO =====")
    for url in [
        "https://danikeai-perfil.onrender.com/health",
        "https://danikeai-perfil.onrender.com/profile/upload-photo",
        "https://danikeai-perfil.onrender.com/profile/delete-photo",
    ]:
        status, corpo = verificar_url(url)
        log(f"\nGET {url}")
        log(f"HTTP: {status}")
        log(f"Resposta: {corpo[:500]}")

    log("\n===== 6. TESTES DO BACKEND =====")
    if (ROOT / "perfil_backend").is_dir():
        codigo, saida = executar([
            sys.executable, "-m", "unittest", "discover",
            "-s", "perfil_backend", "-p", "test*.py", "-v"
        ])
        log(saida)
        log(f"Código de saída: {codigo}")
    else:
        log("Pasta do backend não encontrada.")

    log("\n===== 7. COMPILAÇÃO DEBUG =====")
    log("A compilação não altera o código-fonte.")
    codigo, saida = executar(
        ["./gradlew", ":app:assembleDebug"], timeout=600
    )
    log(saida[-16000:])
    log(f"Código de saída Gradle: {codigo}")

    apk = ROOT / "app/build/outputs/apk/debug/app-debug.apk"
    log(f"APK existente: {apk.is_file()}")
    if apk.is_file():
        log(f"Tamanho: {apk.stat().st_size} bytes")

    RELATORIO.write_text("\n".join(linhas), encoding="utf-8")
    log(f"\nRelatório salvo em: {RELATORIO}")
    log("\nNenhum arquivo Java foi editado. ADM e equipe preservados.")
    log("A exclusão real de imagens não foi executada.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
