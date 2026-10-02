import os
import json
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer

ENV = os.path.expanduser("~/.config/danikeai.env")

def carregar_chave():
    try:
        with open(ENV, "r", encoding="utf-8") as f:
            for linha in f:
                linha = linha.strip()
                if linha.startswith("GEMINI_API_KEY="):
                    return linha.split("=", 1)[1].strip()
    except Exception:
        pass

    return ""

class Handler(BaseHTTPRequestHandler):

    def enviar_json(self, codigo, dados):
        resposta = json.dumps(
            dados,
            ensure_ascii=False
        ).encode("utf-8")

        self.send_response(codigo)
        self.send_header(
            "Content-Type",
            "application/json; charset=utf-8"
        )
        self.send_header(
            "Content-Length",
            str(len(resposta))
        )
        self.end_headers()
        self.wfile.write(resposta)

    def do_POST(self):

        if self.path != "/ask":
            self.enviar_json(
                404,
                {"erro": "Endpoint não encontrado"}
            )
            return

        try:
            tamanho = int(
                self.headers.get("Content-Length", 0)
            )

            corpo = self.rfile.read(tamanho)

            dados = json.loads(
                corpo.decode("utf-8")
            )

            pergunta = dados.get(
                "prompt",
                dados.get("query", "")
            ).strip()

            if not pergunta:
                self.enviar_json(
                    400,
                    {"erro": "Pergunta vazia"}
                )
                return

            chave = carregar_chave()

            if not chave:
                self.enviar_json(
                    500,
                    {"erro": "Chave Gemini não encontrada."}
                )
                return

            payload = json.dumps({
                "contents": [
                    {
                        "parts": [
                            {
                                "text": pergunta
                            }
                        ]
                    }
                ]
            }).encode("utf-8")

            url = (
                "https://generativelanguage.googleapis.com/"
                "v1beta/models/gemini-3.5-flash:"
                "generateContent"
            )

            req = urllib.request.Request(
                url,
                data=payload,
                headers={
                    "x-goog-api-key": chave,
                    "Content-Type": "application/json"
                },
                method="POST"
            )

            with urllib.request.urlopen(
                req,
                timeout=60
            ) as resposta:

                resultado = resposta.read()

                self.send_response(200)
                self.send_header(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )
                self.send_header(
                    "Content-Length",
                    str(len(resultado))
                )
                self.end_headers()

                self.wfile.write(resultado)

        except urllib.error.HTTPError as e:

            try:
                detalhe = e.read().decode(
                    "utf-8",
                    errors="replace"
                )
            except Exception:
                detalhe = str(e)

            self.enviar_json(
                e.code,
                {
                    "erro": "Gemini HTTP",
                    "detalhes": detalhe
                }
            )

        except Exception as e:

            self.enviar_json(
                500,
                {
                    "erro": str(e)
                }
            )

    def log_message(self, formato, *args):
        print("[Gemini]", formato % args)


print("======================================")
print("DaNikeAI Gemini Server")
print("Modelo: gemini-3.5-flash")
print("Endpoint: http://127.0.0.1:8766/ask")
print("======================================")

HTTPServer(
    ("127.0.0.1", 8766),
    Handler
).serve_forever()
