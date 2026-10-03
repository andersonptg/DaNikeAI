import os
import json
import urllib.request
import urllib.error
from datetime import datetime
from http.server import BaseHTTPRequestHandler, HTTPServer

ENV = os.path.expanduser("~/.config/danikeai.env")

HOST = "0.0.0.0"
PORT = int(os.getenv("PORT", "8766"))

MODELOS = [
    "gemini-3.5-flash-lite",
    "gemini-2.5-flash",
]

INSTRUCAO = (
    "Responda em português do Brasil. "
    "Seja clara, natural e direta. "
    "Não use emojis, markdown, asteriscos, hashtags, listas com símbolos ou links, "
    "a menos que o usuário peça. "
    "Escreva de forma confortável para leitura em voz alta, usando vírgulas e pontos."
)


def data_hora_atual():
    return datetime.now().astimezone().strftime("%Y-%m-%d %H:%M:%S %Z")

def carregar_chave():
    try:
        with open(ENV, "r", encoding="utf-8") as f:
            for linha in f:
                linha = linha.strip()
                if linha.startswith("GEMINI_API_KEY="):
                    return linha.split("=", 1)[1].strip()
    except Exception:
        pass

    return os.getenv("GEMINI_API_KEY", "").strip()


def extrair_texto(resultado):
    try:
        candidatos = resultado.get("candidates", [])

        if not candidatos:
            return ""

        partes = candidatos[0].get("content", {}).get("parts", [])

        textos = []

        for parte in partes:
            texto = parte.get("text")
            if texto:
                textos.append(texto)

        return "\n".join(textos).strip()

    except Exception:
        return ""


def chamar_modelo(modelo, pergunta, chave):
    payload = json.dumps({
        "contents": [
            {
                "role": "user",
                "parts": [
                    {
                        "text": (
                            INSTRUCAO
                            + "\n\nDATA E HORA ATUAIS DO SERVIDOR: "
                            + data_hora_atual()
                            + "\nUse esta data e hora como referência atual. "
                            + "Não invente outra data ou hora. "
                            + "\n\nPergunta do usuário:\n"
                            + pergunta
                        )
                    }
                ]
            }
        ],
        "generationConfig": {
            "maxOutputTokens": 1200
        }
    }).encode("utf-8")

    url = (
        "https://generativelanguage.googleapis.com/"
        "v1beta/models/"
        + modelo
        + ":generateContent"
    )

    req = urllib.request.Request(
        url,
        data=payload,
        headers={
            "x-goog-api-key": chave,
            "Content-Type": "application/json",
            "Accept": "application/json"
        },
        method="POST"
    )

    try:
        with urllib.request.urlopen(req, timeout=60) as resposta:
            bruto = resposta.read().decode("utf-8")

            return (
                resposta.status,
                json.loads(bruto)
            )

    except urllib.error.HTTPError as e:
        bruto = e.read().decode("utf-8", errors="replace")

        try:
            dados = json.loads(bruto)
        except Exception:
            dados = {"erro": bruto}

        return e.code, dados

    except Exception as e:
        return 0, {"erro": str(e)}


class Handler(BaseHTTPRequestHandler):

    def log_message(self, formato, *args):
        print("[HTTP]", formato % args)

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

    def do_GET(self):
        if self.path == "/":
            self.enviar_json(
                200,
                {
                    "status": "online",
                    "servico": "DaNikeAI Gemini Server"
                }
            )
            return

        if self.path == "/health":
            self.enviar_json(
                200,
                {"status": "ok"}
            )
            return

        self.enviar_json(
            404,
            {"erro": "Endpoint não encontrado"}
        )

    def do_POST(self):
        if self.path != "/ask":
            self.enviar_json(
                404,
                {"erro": "Endpoint não encontrado"}
            )
            return

        try:
            tamanho = int(
                self.headers.get("Content-Length", "0")
            )

            corpo = self.rfile.read(tamanho)

            dados = json.loads(
                corpo.decode("utf-8")
            )

            pergunta = str(
                dados.get(
                    "prompt",
                    dados.get("query", "")
                )
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
                    {
                        "erro": "Chave Gemini não encontrada."
                    }
                )
                return

            for modelo in MODELOS:
                codigo, resultado = chamar_modelo(
                    modelo,
                    pergunta,
                    chave
                )

                if codigo == 200:
                    texto = extrair_texto(resultado)

                    if texto:
                        self.enviar_json(
                            200,
                            {
                                "text": texto,
                                "model": modelo
                            }
                        )
                        return

                    continue

                if codigo in (
                    404,
                    429,
                    500,
                    502,
                    503,
                    504
                ):
                    print(
                        "Modelo",
                        modelo,
                        "falhou HTTP",
                        codigo,
                        "- tentando próximo."
                    )
                    continue

                erro = resultado.get(
                    "error",
                    resultado.get(
                        "erro",
                        "Erro desconhecido."
                    )
                )

                self.enviar_json(
                    codigo if codigo > 0 else 502,
                    {
                        "erro": "Gemini HTTP " + str(codigo),
                        "detalhes": erro,
                        "model": modelo
                    }
                )
                return

            self.enviar_json(
                429,
                {
                    "erro": "Limite ou indisponibilidade do Gemini.",
                    "detalhes": (
                        "A ponte tentou os modelos configurados. "
                        "Verifique a cota, faturamento ou disponibilidade "
                        "da API Gemini."
                    )
                }
            )

        except Exception as e:
            self.enviar_json(
                500,
                {
                    "erro": "Falha interna na ponte Gemini.",
                    "detalhes": str(e)
                }
            )


if __name__ == "__main__":
    print("======================================")
    print("DaNikeAI Gemini Server")
    print("Modelos:", ", ".join(MODELOS))
    print(
        "Endpoint: http://"
        + HOST
        + ":"
        + str(PORT)
        + "/ask"
    )
    print("======================================")

    servidor = HTTPServer(
        (HOST, PORT),
        Handler
    )

    try:
        servidor.serve_forever()

    except KeyboardInterrupt:
        print("\nServidor encerrado.")

    finally:
        servidor.server_close()
