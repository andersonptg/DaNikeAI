import json
import urllib.parse
import urllib.request
import time

BUSCA = "mediatype:movies AND rights:\"Public Domain\""

params = urllib.parse.urlencode({
    "q": BUSCA,
    "fl[]": ["identifier", "title", "description", "year"],
    "rows": 100,
    "output": "json"
})

url = "https://archive.org/advancedsearch.php?" + params

with urllib.request.urlopen(url, timeout=30) as r:
    dados = json.load(r)

docs = dados["response"]["docs"]
filmes = []

for item in docs:
    identificador = item.get("identifier")
    if not identificador:
        continue

    try:
        with urllib.request.urlopen(
            "https://archive.org/metadata/" + identificador,
            timeout=20
        ) as r:
            meta = json.load(r).get("metadata", {})
    except Exception:
        continue

    direitos = str(meta.get("rights", "")).lower()
    licenca = str(meta.get("licenseurl", "")).lower()

    if "public domain" not in direitos and "publicdomain" not in licenca:
        continue

    filmes.append({
        "id": len(filmes) + 1,
        "titulo": meta.get("title") or item.get("title") or identificador,
        "identificador": identificador,
        "ano": meta.get("year") or item.get("year"),
        "direitos": meta.get("rights"),
        "licenca": meta.get("licenseurl"),
        "url": "https://archive.org/details/" + identificador
    })

    if len(filmes) >= 50:
        break

catalogo = {
    "versao": 5,
    "nome": "NEXO",
    "categorias": [
        "DESTAQUES",
        "AÇÃO",
        "COMÉDIA",
        "TERROR",
        "CLÁSSICOS",
        "SÉRIES"
    ],
    "filmes": filmes
}

with open(
    "app/src/main/assets/catalogo_filmes/catalogo.json",
    "w",
    encoding="utf-8"
) as f:
    json.dump(catalogo, f, ensure_ascii=False, indent=2)

print("CATALOGO_NEXO_FILTRADO")
print("FILMES_PUBLIC_DOMAIN:", len(filmes))
