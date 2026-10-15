from pathlib import Path
from datetime import datetime
import re
import shutil
import subprocess
import sys

ROOT = Path.home() / "da_nike_ai"
PESSOAL = ROOT / "app/src/main/java/com/danike/ai/PerfilFirebase.java"
ADM = ROOT / "app/src/main/java/com/danike/ai/AdmActivity.java"
MAIN = ROOT / "app/src/main/java/com/danike/ai/MainActivity.java"
BACKEND = ROOT / "perfil_backend/server.py"

MARCA = datetime.now().strftime("%Y%m%d_%H%M%S")
RELATORIO = ROOT / f"RELATORIO_FOTOS_{MARCA}.txt"

def titulo(texto):
    print("\n" + "=" * 72)
    print(texto)
    print("=" * 72)

def ler(caminho):
    if not caminho.is_file():
        print(f"ARQUIVO NÃO ENCONTRADO: {caminho}")
        return ""
    return caminho.read_text(encoding="utf-8", errors="replace")

def mostrar_contexto(nome, conteudo, termos, antes=5, depois=9):
    linhas = conteudo.splitlines()
    encontrados = []
    for i, linha in enumerate(linhas):
        if any(re.search(t, linha, re.I) for t in termos):
            inicio = max(0, i - antes)
            fim = min(len(linhas), i + depois + 1)
            if encontrados and inicio <= encontrados[-1][1]:
                encontrados[-1] = (encontrados[-1][0], max(fim, encontrados[-1][1]))
            else:
                encontrados.append((inicio, fim))
    saida = [f"\n--- {nome} ---"]
    if not encontrados:
        saida.append("Nenhum trecho correspondente encontrado.")
    for inicio, fim in encontrados:
        saida.extend(
            f"{n + 1:5}: {linhas[n]}"
            for n in range(inicio, fim)
        )
    return "\n".join(saida)

def executar(comando):
    try:
        r = subprocess.run(
            comando, cwd=ROOT, text=True,
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            timeout=120
        )
        return r.returncode, r.stdout
    except Exception as e:
        return 1, str(e)

def main():
    if not ROOT.is_dir():
        print("ERRO: projeto ~/da_nike_ai não encontrado.")
        sys.exit(1)

    if not PESSOAL.is_file() or not ADM.is_file():
        print("ERRO: não encontrei os arquivos essenciais. Nada foi alterado.")
        sys.exit(1)

    pessoal = ler(PESSOAL)
    adm = ler(ADM)
    main_java = ler(MAIN)
    backend = ler(BACKEND)

    titulo("1. BACKUP DO PERFIL PESSOAL")
    backup = PESSOAL.with_name(
        PESSOAL.name + f".backup_comparacao_{MARCA}"
    )
    shutil.copy2(PESSOAL, backup)
    print(f"Backup criado: {backup}")
    print("ADM.java será somente lido, nunca editado por este script.")

    blocos = []
    blocos.append("PROJETO: DaNikeAI")
    blocos.append(f"DATA: {MARCA}")
    blocos.append(f"BACKUP: {backup}")
    blocos.append("\nOBSERVAÇÃO: análise automática; sem alterações especulativas.")

    titulo("2. COMPARAÇÃO DAS ROTAS DE FOTO")
    termos = [
        r"FirebaseStorage", r"getReference", r"putFile", r"putBytes",
        r"getDownloadUrl", r"delete\(", r"Cloudinary",
        r"upload-photo", r"delete-photo", r"fotoUrl",
        r"photoUrl", r"publicId", r"public_id", r"secure_url",
        r"getIdToken", r"FirebaseFirestore", r"collection\("
    ]

    trechos = [
        mostrar_contexto("PERFIL PESSOAL — PerfilFirebase.java",
                         pessoal, termos),
        mostrar_contexto("REFERÊNCIA ADM — AdmActivity.java",
                         adm, termos),
        mostrar_contexto("SINCRONIZAÇÃO — MainActivity.java",
                         main_java, termos),
        mostrar_contexto("SERVIÇO INDEPENDENTE — server.py",
                         backend, termos),
    ]
    blocos.extend(trechos)
    for trecho in trechos:
        print(trecho)

    titulo("3. VERIFICAÇÕES AUTOMÁTICAS")
    verificacoes = {
        "Perfil pessoal usa Firebase Storage": bool(
            re.search(r"FirebaseStorage|getDownloadUrl|putFile|putBytes", pessoal)
        ),
        "Perfil pessoal chama serviço independente": (
            "danikeai-perfil.onrender.com" in pessoal
        ),
        "Perfil pessoal ainda chama serviço antigo": (
            "danikeai.onrender.com/profile/" in pessoal
        ),
        "ADM contém referências de armazenamento de fotos": bool(
            re.search(r"FirebaseStorage|Cloudinary|putFile|putBytes|fotoUrl", adm, re.I)
        ),
        "Backend tem rota de upload": (
            "/profile/upload-photo" in backend
        ),
        "Backend tem rota de exclusão": (
            "/profile/delete-photo" in backend
        ),
        "Perfil pessoal grava referências de foto": bool(
            re.search(r"fotoUrl|photoUrl|publicId|public_id", pessoal, re.I)
        ),
    }
    for nome, ok in verificacoes.items():
        print(f"[{'OK' if ok else 'VERIFICAR'}] {nome}")

    blocos.append("\n\n===== VERIFICAÇÕES =====")
    blocos.extend(
        f"[{'OK' if ok else 'VERIFICAR'}] {nome}"
        for nome, ok in verificacoes.items()
    )

    titulo("4. TESTES LOCAIS DO BACKEND")
    if (ROOT / "perfil_backend").is_dir():
        codigo, saida = executar([
            sys.executable, "-m", "unittest", "discover",
            "-s", "perfil_backend", "-p", "test*.py", "-v"
        ])
        print(saida or "(sem saída)")
        blocos.append("\n===== TESTES BACKEND =====\n" + saida)
        print("Código de saída dos testes:", codigo)
    else:
        print("Pasta perfil_backend não encontrada.")

    titulo("5. SEGURANÇA DAS ALTERAÇÕES")
    print("Nenhuma edição automática foi feita no código.")
    print("Não é seguro copiar código do ADM por busca de palavras:")
    print("os métodos podem depender de permissões, coleções e regras distintas.")
    print("A correção precisa preservar o formato real dos métodos pessoais.")
    print("O backup permite recuperar PerfilFirebase.java se necessário.")

    blocos.append(
        "\n\nNenhum arquivo-fonte foi modificado por este script.\n"
        "O ADM e os dados da equipe foram preservados."
    )
    RELATORIO.write_text("\n".join(blocos), encoding="utf-8")

    titulo("6. RESULTADO")
    print(f"Relatório completo: {RELATORIO}")
    print(f"Backup pessoal: {backup}")
    print("\nA comparação foi coletada em uma execução.")
    print("Não foi aplicada uma correção às cegas que pudesse quebrar o perfil.")
    print("Envie o conteúdo do relatório para gerar a correção definitiva.")

if __name__ == "__main__":
    main()
