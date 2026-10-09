import os
import json
import urllib.request
import urllib.error
from cloudinary_auth import usuario_e_proprietario
from cloudinary_delete import excluir_foto_cloudinary
from datetime import datetime
from http.server import BaseHTTPRequestHandler, HTTPServer

ENV = os.path.expanduser("~/.config/danikeai.env")

HOST = "0.0.0.0"
PORT = int(os.getenv("PORT", "8766"))

MODELOS = [
    "gemini-3.5-flash-lite",
    "gemini-3.8-flash",
]

INSTRUCAO = (
    "Responda em português do Brasil. "
    "Seja clara, natural e direta. "
    "Não use emojis, markdown, asteriscos, hashtags, listas com símbolos ou links, "
    "a menos que o usuário peça. "
    "Escreva de forma confortável para leitura em voz alta, usando vírgulas e pontos. ""Quando a pergunta depender de informações atuais, recentes, notícias, preços, resultados, eventos, pessoas, empresas, locais ou qualquer dado que possa ter mudado, use a Pesquisa Google para verificar a informação antes de responder. ""Para perguntas que não precisam de informação atual, responda normalmente sem pesquisar."
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
        "tools": [{"google_search": {}}],"generationConfig": {
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


def carregar_tmdb_token():
    try:
        with open(ENV, "r", encoding="utf-8") as f:
            for linha in f:
                linha = linha.strip()
                if linha.startswith("TMDB_ACCESS_TOKEN="):
                    return linha.split("=", 1)[1].strip()
    except Exception:
        pass

    return os.getenv("TMDB_ACCESS_TOKEN", "").strip()




def buscar_catalogo_have():
    token = carregar_tmdb_token()

    if not token:
        return {"results": [], "erro": "TMDB não configurado"}

    # Principais serviços que o Have pode exibir.
    # Os IDs são os IDs oficiais do TMDB para provedores.
    provedores = {
        "Netflix": 8,
        "Disney+": 337,
        "Globoplay": 307,
        "Prime Video": 119,
        "Max": 1899,
    }

    resultados = []

    for nome, provider_id in provedores.items():
        url = (
            "https://api.themoviedb.org/3/discover/movie"
            "?watch_region=BR"
            "&with_watch_providers=" + str(provider_id) +
            "&with_watch_monetization_types=flatrate"
            "&sort_by=popularity.desc"
            "&language=pt-BR"
            "&page=1"
        )

        req = urllib.request.Request(
            url,
            headers={
                "Authorization": "Bearer " + token,
                "Accept": "application/json"
            }
        )

        try:
            with urllib.request.urlopen(req, timeout=20) as resposta:
                dados = json.loads(
                    resposta.read().decode("utf-8")
                )

            for filme in dados.get("results", [])[:10]:
                filme["have_provider"] = nome
                filme["have_provider_id"] = provider_id
                resultados.append(filme)

        except Exception as e:
            print("[TMDB]", nome, e)

    # Remove duplicados mantendo a primeira ocorrência.
    unicos = {}
    for filme in resultados:
        mid = filme.get("id")
        if mid and mid not in unicos:
            unicos[mid] = filme

    return {
        "results": list(unicos.values()),
        "total": len(unicos),
        "image_base": "https://image.tmdb.org/t/p/w780"
    }

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
        if self.path == "/have/catalog":
            self.enviar_json(200, buscar_catalogo_have())
            return

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


    def do_delete_team_photo(self):
        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
            if tamanho <= 0 or tamanho > 16384:
                self.enviar_json(400, {"erro": "Solicitação inválida."})
                return

            autorizacao = self.headers.get("Authorization", "")
            if not autorizacao.startswith("Bearer "):
                self.enviar_json(401, {"erro": "Autenticação necessária."})
                return

            token = autorizacao[7:].strip()
            try:
                proprietario = usuario_e_proprietario(token)
            except Exception:
                proprietario = False

            if not proprietario:
                self.enviar_json(403, {"erro": "Acesso não autorizado."})
                return

            dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
            public_id = str(dados.get("publicId", "")).strip()

            try:
                resultado = excluir_foto_cloudinary(public_id)
            except ValueError:
                self.enviar_json(400, {"erro": "Identificador de foto inválido."})
                return
            except Exception as erro:
                print("[EQUIPE] Exclusão Cloudinary falhou:", type(erro).__name__)
                self.enviar_json(502, {"erro": "Não foi possível confirmar a exclusão no Cloudinary."})
                return

            self.enviar_json(200, {"ok": True, "result": resultado})

        except Exception as erro:
            print("[EQUIPE] Solicitação inválida:", type(erro).__name__)
            self.enviar_json(400, {"erro": "Solicitação inválida."})

    def do_POST(self):
        if self.path == "/equipe/delete-photo":
            self.do_delete_team_photo()
            return
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
                        "DIAGNOSTICO:",
                        modelo,
                        "falhou HTTP",
                        codigo,
                        "detalhes:",
                        resultado.get("error", resultado.get("erro", "sem detalhes"))
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
