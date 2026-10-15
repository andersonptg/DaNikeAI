import os, json, urllib.request, urllib.error, urllib.parse

p = os.path.expanduser("~/.config/danikeai.env")
with open(p, encoding="utf-8") as f:
linhas = f.read().splitlines()

token = next((x.split("=", 1)[1].strip() for x in linhas
if x.strip().startswith("TMDB_ACCESS_TOKEN=") and "=" in x), "")

if not token:
print("Token não encontrado")
raise SystemExit(1)

params = urllib.parse.urlencode({
"watch_region": "BR",
"with_watch_providers": "8",
"with_watch_monetization_types": "flatrate",
"sort_by": "popularity.desc",
"language": "pt-BR",
"page": "1"
})
url = "https://api.themoviedb.org/3/discover/movie?" + params
req = urllib.request.Request(url, headers={
"Authorization": "Bearer " + token,
"Accept": "application/json"
})

try:
with urllib.request.urlopen(req, timeout=20) as r:
dados = json.loads(r.read().decode("utf-8"))
print("HTTP TMDB:", r.status)
print("Filmes recebidos:", len(dados.get("results", [])))
print("Mensagem API:", dados.get("status_message", "nenhuma"))
except urllib.error.HTTPError as e:
print("HTTP TMDB:", e.code)
print("Resposta:", e.read().decode("utf-8", "replace")[:500])
except Exception as e:
print("Falha:", type(e).name, str(e))
