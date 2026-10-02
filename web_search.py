import os
import json
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer

ENV = os.path.expanduser("~/.config/danikeai.env")

def carregar_chave():
    with open(ENV, "r", encoding="utf-8") as f:
        for linha in f:
            if linha.startswith("TAVILY_API_KEY="):
                return linha.strip().split("=", 1)[1]
    return ""

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/search":
            self.send_response(404)
            self.end_headers()
            return

        tamanho = int(self.headers.get("Content-Length", 0))
        dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))

        consulta = dados.get("query", "").strip()

        payload = json.dumps({
            "query": consulta,
            "search_depth": "basic",
            "max_results": 5,
            "include_answer": True
        }).encode("utf-8")

        req = urllib.request.Request(
            "https://api.tavily.com/search",
            data=payload,
            headers={
                "Authorization": "Bearer " + carregar_chave(),
                "Content-Type": "application/json"
            },
            method="POST"
        )

        try:
            with urllib.request.urlopen(req, timeout=30) as resposta:
                resultado = resposta.read()
                self.send_response(200)
                self.send_header("Content-Type", "application/json; charset=utf-8")
                self.end_headers()
                self.wfile.write(resultado)
        except Exception as e:
            erro = json.dumps({"erro": str(e)}).encode("utf-8")
            self.send_response(500)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(erro)

print("DaNikeAI Web Search ativo em http://127.0.0.1:8765")
HTTPServer(("127.0.0.1", 8765), Handler).serve_forever()
