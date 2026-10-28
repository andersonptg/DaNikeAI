import os
import json
import urllib.request
import urllib.parse
import urllib.error
from cloudinary_auth import usuario_e_proprietario, verificar_token_firebase
from cloudinary_delete import excluir_foto_cloudinary, excluir_foto_perfil_cloudinary
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

ENV = os.path.expanduser("~/.config/danikeai.env")

def carregar_env_local_cloudinary():
    """Carrega variáveis locais sem substituir as já configuradas."""
    try:
        with open(ENV, "r", encoding="utf-8") as arquivo_env:
            for linha in arquivo_env:
                linha = linha.strip()

                if not linha or linha.startswith("#") or "=" not in linha:
                    continue

                chave, valor = linha.split("=", 1)
                chave = chave.strip()
                valor = valor.strip()

                if not chave or not all(
                    c.isalnum() or c == "_" for c in chave
                ):
                    continue

                if len(valor) >= 2 and valor[0] == valor[-1] and valor[0] in ("'", '"'):
                    valor = valor[1:-1]

                if not os.environ.get(chave, "").strip():
                    os.environ[chave] = valor
    except FileNotFoundError:
        pass
    except OSError as erro:
        print("[ENV] Não foi possível ler o arquivo local:", type(erro).__name__)


carregar_env_local_cloudinary()


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


def carregar_token_base44():
    """
    Lê o token administrativo do Base44 somente do ambiente do servidor.
    Nunca deve ser colocado no APK.
    """
    token = os.getenv("BASE44_ADMIN_TOKEN", "").strip()
    if token:
        return token

    try:
        with open(ENV, "r", encoding="utf-8") as f:
            for linha in f:
                linha = linha.strip()
                if linha.startswith("BASE44_ADMIN_TOKEN="):
                    return linha.split("=", 1)[1].strip()
    except Exception:
        pass

    return ""


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
    api_key = os.getenv("TMDB_API_KEY", "").strip()

    if not token and not api_key:
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

        if api_key:
            url += "&" + urllib.parse.urlencode({"api_key": api_key})
        headers = {"Accept": "application/json"}
        if not api_key:
            headers["Authorization"] = "Bearer " + token
        req = urllib.request.Request(url, headers=headers)

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


    # NEXO_R2_ROUTES_V1
    def _nexo_r2_config(self):
        import os
        endpoint = os.environ.get("NEXO_R2_ENDPOINT", "").strip()
        access = os.environ.get("NEXO_R2_ACCESS_KEY", "").strip()
        secret = os.environ.get("NEXO_R2_SECRET_KEY", "").strip()
        bucket = os.environ.get("NEXO_R2_BUCKET", "nexo-videos").strip()
        if not all((endpoint, access, secret, bucket)):
            raise RuntimeError("Configuração NEXO R2 incompleta no servidor.")
        import boto3
        client = boto3.client(
            "s3",
            endpoint_url=endpoint,
            aws_access_key_id=access,
            aws_secret_access_key=secret,
            region_name="auto",
        )
        return client, bucket

    def _nexo_r2_body(self, limite=32768):
        import json
        tamanho = int(self.headers.get("Content-Length", "0"))
        if tamanho <= 0 or tamanho > limite:
            raise ValueError("Tamanho da solicitação inválido.")
        dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
        if not isinstance(dados, dict):
            raise ValueError("Formato JSON inválido.")
        return dados

    def _nexo_r2_owner(self):
        auth = self.headers.get("Authorization", "")
        if not auth.startswith("Bearer "):
            return False
        try:
            return bool(usuario_e_proprietario(auth[7:].strip()))
        except Exception:
            return False

    def _nexo_r2_upload_url(self):
        import re, uuid
        from datetime import datetime, timezone
        try:
            if not self._nexo_r2_owner():
                self.enviar_json(403, {"sucesso": False, "erro": "Apenas o proprietário pode publicar conteúdo."})
                return
            dados = self._nexo_r2_body()
            nome = str(dados.get("nome", "video")).strip()
            mime = str(dados.get("mime", "application/octet-stream")).strip()
            tipo = str(dados.get("tipo", "video")).strip().lower()
            if tipo not in ("video", "capa", "banner"):
                self.enviar_json(400, {"sucesso": False, "erro": "Tipo de arquivo inválido."})
                return
            if not mime or len(mime) > 150 or "\n" in mime or "\r" in mime:
                self.enviar_json(400, {"sucesso": False, "erro": "Tipo MIME inválido."})
                return
            nome = re.sub(r"[^A-Za-z0-9._-]", "_", nome.split("/")[-1])[:100] or "arquivo"
            uid = uuid.uuid4().hex
            prefixo = "videos" if tipo == "video" else "imagens"
            chave = f"nexo/{prefixo}/{uid}_{nome}"
            cliente, bucket = self._nexo_r2_config()
            url = cliente.generate_presigned_url(
                "put_object",
                Params={"Bucket": bucket, "Key": chave, "ContentType": mime},
                ExpiresIn=900,
            )
            self.enviar_json(200, {
                "sucesso": True,
                "upload_url": url,
                "object_key": chave,
                "tipo": tipo,
                "mime": mime,
            })
        except ValueError as e:
            self.enviar_json(400, {"sucesso": False, "erro": str(e)})
        except Exception as e:
            print("[NEXO R2] Falha ao gerar URL:", type(e).__name__)
            self.enviar_json(502, {"sucesso": False, "erro": "Não foi possível preparar o upload R2."})

    def _nexo_r2_publicar(self):
        import json
        import uuid
        from datetime import datetime, timezone

        try:
            if not self._nexo_r2_owner():
                self.enviar_json(403, {
                    "sucesso": False,
                    "erro": "Apenas o proprietário pode publicar conteúdo."
                })
                return

            dados = self._nexo_r2_body()
            titulo = str(dados.get("titulo", "")).strip()
            video_key = str(dados.get("video_key", "")).strip()

            if not titulo or not video_key:
                self.enviar_json(400, {
                    "sucesso": False,
                    "erro": "Título e video_key são obrigatórios."
                })
                return

            if not video_key.startswith("nexo/videos/") or ".." in video_key:
                self.enviar_json(400, {
                    "sucesso": False,
                    "erro": "Chave de vídeo inválida."
                })
                return

            cliente, bucket = self._nexo_r2_config()
            cliente.head_object(Bucket=bucket, Key=video_key)

            capa_key = str(dados.get("capa_key", "")).strip()
            banner_key = str(dados.get("banner_key", "")).strip()

            for chave in (capa_key, banner_key):
                if chave:
                    if not chave.startswith("nexo/imagens/") or ".." in chave:
                        self.enviar_json(400, {
                            "sucesso": False,
                            "erro": "Chave de imagem inválida."
                        })
                        return
                    cliente.head_object(Bucket=bucket, Key=chave)

            filme_id = uuid.uuid4().hex
            filme = {
                "id": filme_id,
                "titulo": titulo[:200],
                "descricao": str(dados.get("descricao", ""))[:10000],
                "ano": str(dados.get("ano", ""))[:10],
                "categoria": str(dados.get("categoria", ""))[:500],
                "destaque": bool(dados.get("destaque", False)),
                "ativo": bool(dados.get("ativo", True)),
                "video_key": video_key,
                "capa_key": capa_key,
                "banner_key": banner_key,
                "criado_em": datetime.now(timezone.utc).isoformat(),
            }

            chave_meta = "nexo/catalogo/" + filme_id + ".json"
            cliente.put_object(
                Bucket=bucket,
                Key=chave_meta,
                Body=json.dumps(filme, ensure_ascii=False).encode("utf-8"),
                ContentType="application/json; charset=utf-8",
            )
            self.enviar_json(201, {"sucesso": True, "filme": filme})

        except ValueError as e:
            self.enviar_json(400, {"sucesso": False, "erro": str(e)})
        except Exception as e:
            print("[NEXO R2] Falha ao publicar:", type(e).__name__)
            self.enviar_json(502, {
                "sucesso": False,
                "erro": "Não foi possível confirmar a gravação no catálogo R2."
            })

    def _nexo_r2_gerenciar(self):
        import json
        import re

        try:
            if not self._nexo_r2_owner():
                self.enviar_json(403, {
                    "sucesso": False,
                    "erro": "Apenas o proprietário pode gerenciar o NEXO."
                })
                return

            dados = self._nexo_r2_body()
            acao = str(dados.get("acao", "")).strip().lower()
            filme_id = str(dados.get("id", "")).strip()

            if acao not in ("editar", "ocultar", "ativar", "excluir"):
                self.enviar_json(400, {
                    "sucesso": False,
                    "erro": "Ação de gerenciamento inválida."
                })
                return

            if not re.fullmatch(r"[a-fA-F0-9]{32}", filme_id):
                self.enviar_json(400, {
                    "sucesso": False,
                    "erro": "Identificador de filme inválido."
                })
                return

            cliente, bucket = self._nexo_r2_config()
            chave_meta = "nexo/catalogo/" + filme_id + ".json"

            try:
                obj = cliente.get_object(Bucket=bucket, Key=chave_meta)
                filme = json.loads(obj["Body"].read().decode("utf-8"))
            except Exception:
                self.enviar_json(404, {
                    "sucesso": False,
                    "erro": "Conteúdo não encontrado no catálogo."
                })
                return

            if not isinstance(filme, dict) or filme.get("id") != filme_id:
                self.enviar_json(404, {
                    "sucesso": False,
                    "erro": "Registro de conteúdo inválido."
                })
                return

            if acao == "editar":
                if "titulo" in dados:
                    titulo = str(dados["titulo"]).strip()
                    if not titulo:
                        self.enviar_json(400, {
                            "sucesso": False,
                            "erro": "O título não pode ficar vazio."
                        })
                        return
                    filme["titulo"] = titulo[:200]

                if "descricao" in dados:
                    filme["descricao"] = str(dados["descricao"])[:10000]

                if "ano" in dados:
                    filme["ano"] = str(dados["ano"])[:10]

                if "categoria" in dados:
                    filme["categoria"] = str(dados["categoria"])[:500]

                if "destaque" in dados:
                    if not isinstance(dados["destaque"], bool):
                        self.enviar_json(400, {
                            "sucesso": False,
                            "erro": "O campo destaque deve ser verdadeiro ou falso."
                        })
                        return
                    filme["destaque"] = dados["destaque"]

            elif acao == "ocultar":
                filme["ativo"] = False

            elif acao == "ativar":
                filme["ativo"] = True

            elif acao == "excluir":
                # Remove o registro do catálogo, preservando a mídia no R2.
                # A limpeza física exige verificar se outros registros usam os arquivos.
                cliente.delete_object(Bucket=bucket, Key=chave_meta)
                self.enviar_json(200, {
                    "sucesso": True,
                    "acao": "excluir",
                    "id": filme_id,
                    "midia_preservada": True
                })
                return

            cliente.put_object(
                Bucket=bucket,
                Key=chave_meta,
                Body=json.dumps(filme, ensure_ascii=False).encode("utf-8"),
                ContentType="application/json; charset=utf-8",
            )

            self.enviar_json(200, {
                "sucesso": True,
                "acao": acao,
                "filme": filme
            })

        except ValueError as e:
            self.enviar_json(400, {"sucesso": False, "erro": str(e)})
        except Exception as e:
            print("[NEXO R2] Falha no gerenciamento:", type(e).__name__)
            self.enviar_json(502, {
                "sucesso": False,
                "erro": "Não foi possível concluir a operação de gerenciamento."
            })

    def _nexo_r2_catalogo(self):
        import json
        try:
            cliente, bucket = self._nexo_r2_config()
            filmes = []
            paginador = cliente.get_paginator("list_objects_v2")
            for pagina in paginador.paginate(Bucket=bucket, Prefix="nexo/catalogo/"):
                for item in pagina.get("Contents", []):
                    chave = item.get("Key", "")
                    if not chave.endswith(".json"):
                        continue
                    try:
                        obj = cliente.get_object(Bucket=bucket, Key=chave)
                        filme = json.loads(obj["Body"].read().decode("utf-8"))
                        if not isinstance(filme, dict) or not filme.get("id"):
                            continue
                        for campo, campo_url in (
                            ("video_key", "video_url"),
                            ("capa_key", "capa_url"),
                            ("banner_key", "banner_url"),
                        ):
                            key = str(filme.get(campo, "")).strip()
                            if key:
                                filme[campo_url] = cliente.generate_presigned_url(
                                    "get_object",
                                    Params={"Bucket": bucket, "Key": key},
                                    ExpiresIn=3600,
                                )
                        if filme.get("ativo", True):
                            filmes.append(filme)
                    except Exception as e:
                        print("[NEXO R2] Registro ignorado:", type(e).__name__)
            filmes.sort(key=lambda x: str(x.get("criado_em", "")), reverse=True)
            self.enviar_json(200, {"sucesso": True, "filmes": filmes})
        except Exception as e:
            print("[NEXO R2] Falha no catálogo:", type(e).__name__)
            self.enviar_json(502, {"sucesso": False, "erro": "Não foi possível carregar o catálogo R2."})

    def do_GET(self):
        if self.path.split("?", 1)[0] == "/nexo/catalogo":
            self._nexo_r2_catalogo()
            return

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


    def do_delete_profile_photo(self):
        """Exclui a foto Cloudinary somente da pasta do usuário autenticado."""
        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
            if tamanho <= 0 or tamanho > 16384:
                self.enviar_json(400, {"erro": "Solicitação inválida."})
                return

            autorizacao = self.headers.get("Authorization", "")
            if not autorizacao.startswith("Bearer "):
                self.enviar_json(401, {"erro": "Autenticação necessária."})
                return

            try:
                usuario = verificar_token_firebase(
                    autorizacao[7:].strip()
                )
            except Exception as erro_token:
                print("[FOTOS_AUTH] Falha ao validar token: " + type(erro_token).__name__, flush=True)
                self.enviar_json(401, {"erro": "Token inválido ou expirado."})
                return

            uid = (
                usuario.get("uid")
                or usuario.get("user_id")
                or usuario.get("sub")
            )
            if not isinstance(uid, str) or not uid or "/" in uid or "\\\\" in uid:
                self.enviar_json(401, {"erro": "Usuário inválido."})
                return

            try:
                dados = json.loads(
                    self.rfile.read(tamanho).decode("utf-8")
                )
            except Exception:
                self.enviar_json(400, {"erro": "JSON inválido."})
                return

            public_id = str(dados.get("publicId", "")).strip()
            if not public_id.startswith("perfis/" + uid + "/"):
                self.enviar_json(
                    403,
                    {"erro": "A foto não pertence a este usuário."}
                )
                return

            try:
                resultado = excluir_foto_perfil_cloudinary(public_id, uid)
            except ValueError:
                self.enviar_json(403, {"erro": "Identificador de foto inválido."})
                return
            except Exception:
                self.enviar_json(
                    502,
                    {"erro": "Não foi possível excluir a foto no Cloudinary."}
                )
                return

            self.enviar_json(200, {
                "ok": True,
                "resultado": resultado
            })
        except Exception:
            self.enviar_json(500, {"erro": "Erro interno ao excluir a foto."})

    def do_upload_profile_photo(self):
        """Recebe uma foto autenticada e a envia ao Cloudinary."""
        import hashlib
        import hmac
        import mimetypes
        import secrets
        import time
        import uuid
        from urllib.parse import urlencode

        limite = 8 * 1024 * 1024
        token_header = self.headers.get("Authorization", "")
        if not token_header.startswith("Bearer "):
            self.enviar_json(401, {"erro": "Autenticação necessária."})
            return

        try:
            usuario = verificar_token_firebase(token_header[7:].strip())
            if not isinstance(usuario, dict):
                self.enviar_json(401, {"erro": "Token inválido ou expirado."})
                return
            uid = usuario.get("uid") or usuario.get("user_id") or usuario.get("sub")
            if not isinstance(uid, str) or not uid or "/" in uid or "\\" in uid:
                self.enviar_json(401, {"erro": "Não foi possível validar a conta."})
                return
        except Exception as erro_token:
            print("[FOTOS_AUTH] Falha ao validar token: " + type(erro_token).__name__, flush=True)
            self.enviar_json(401, {"erro": "Token inválido ou expirado."})
            return

        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
        except (TypeError, ValueError):
            tamanho = 0

        if tamanho < 1 or tamanho > limite:
            self.enviar_json(413, {"erro": "A foto deve ter até 8 MB."})
            return

        tipo = (self.headers.get("Content-Type", "") or "").split(";")[0].strip().lower()
        tipos_permitidos = {"image/jpeg", "image/png", "image/webp"}
        if tipo not in tipos_permitidos:
            self.enviar_json(415, {"erro": "Formato inválido. Use JPG, PNG ou WebP."})
            return

        cloud = os.getenv("CLOUDINARY_CLOUD_NAME", "").strip()
        api_key = os.getenv("CLOUDINARY_API_KEY", "").strip()
        api_secret = os.getenv("CLOUDINARY_API_SECRET", "").strip()
        if not cloud or not api_key or not api_secret:
            self.enviar_json(503, {"erro": "O serviço de fotos ainda não está configurado no servidor."})
            return

        try:
            imagem = self.rfile.read(tamanho)
            if len(imagem) != tamanho:
                self.enviar_json(400, {"erro": "Upload incompleto."})
                return

            # Confere assinatura básica do arquivo, não apenas o MIME enviado.
            assinatura_ok = (
                (tipo == "image/jpeg" and imagem.startswith(b"\xff\xd8\xff"))
                or (tipo == "image/png" and imagem.startswith(b"\x89PNG\r\n\x1a\n"))
                or (tipo == "image/webp" and len(imagem) >= 12
                    and imagem[:4] == b"RIFF" and imagem[8:12] == b"WEBP")
            )
            if not assinatura_ok:
                self.enviar_json(415, {"erro": "O conteúdo do arquivo não corresponde ao formato informado."})
                return

            timestamp = str(int(time.time()))
            pasta = "perfis/" + uid
            public_id = uuid.uuid4().hex
            parametros = {
                "folder": pasta,
                "public_id": public_id,
                "timestamp": timestamp,
            }
            texto_assinatura = "&".join(
                f"{chave}={parametros[chave]}" for chave in sorted(parametros)
            ) + api_secret
            assinatura = hashlib.sha1(texto_assinatura.encode("utf-8")).hexdigest()

            boundary = "----DaNikeAI" + secrets.token_hex(16)
            partes = []

            def campo(nome, valor):
                partes.append(
                    f"--{boundary}\r\n"
                    f'Content-Disposition: form-data; name="{nome}"\r\n\r\n'
                    f"{valor}\r\n".encode("utf-8")
                )

            campo("api_key", api_key)
            campo("timestamp", timestamp)
            campo("folder", pasta)
            campo("public_id", public_id)
            campo("signature", assinatura)

            extensao = {"image/jpeg": "jpg", "image/png": "png", "image/webp": "webp"}[tipo]
            partes.append(
                f"--{boundary}\r\n"
                f'Content-Disposition: form-data; name="file"; filename="foto.{extensao}"\r\n'
                f"Content-Type: {tipo}\r\n\r\n".encode("utf-8")
                + imagem + b"\r\n"
            )
            partes.append(f"--{boundary}--\r\n".encode("utf-8"))
            corpo = b"".join(partes)

            requisicao = urllib.request.Request(
                f"https://api.cloudinary.com/v1_1/{cloud}/image/upload",
                data=corpo,
                headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
                method="POST",
            )
            with urllib.request.urlopen(requisicao, timeout=45) as resposta:
                resultado = json.loads(resposta.read().decode("utf-8"))

            foto_url = resultado.get("secure_url")
            foto_id = resultado.get("public_id")
            if not isinstance(foto_url, str) or not foto_url.startswith("https://"):
                raise ValueError("Resposta de imagem inválida.")

            self.enviar_json(200, {
                "ok": True,
                "secure_url": foto_url,
                "public_id": foto_id,
            })

        except urllib.error.HTTPError:
            self.enviar_json(502, {"erro": "O serviço de fotos recusou o envio."})
        except Exception:
            self.enviar_json(502, {"erro": "Não foi possível enviar a foto. Tente novamente."})


    def do_notificar(self):
        import os
        import hmac

        segredo = os.environ.get("DANIKEAI_NOTIFY_SECRET", "").strip()
        recebido = self.headers.get("X-DaNikeAI-Secret", "")

        if not segredo or not hmac.compare_digest(recebido, segredo):
            self.enviar_json(401, {"erro": "Não autorizado."})
            return

        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
            if tamanho <= 0 or tamanho > 1048576:
                self.enviar_json(400, {"erro": "Corpo da requisição inválido."})
                return

            dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
            if not isinstance(dados, dict):
                self.enviar_json(400, {"erro": "JSON inválido."})
                return

            uid = str(dados.get("uid", "")).strip()
            tipo = str(dados.get("tipo", "geral")).strip() or "geral"
            titulo = str(dados.get("titulo", "DaNikeAI")).strip() or "DaNikeAI"
            texto = str(dados.get("texto", "")).strip()
            remetente_uid = str(
                dados.get("remetente_uid", dados.get("remetenteUid", ""))
            ).strip()
            fila_id = str(dados.get("fila_id", "")).strip()

            if not uid or not texto:
                self.enviar_json(400, {"erro": "uid e texto são obrigatórios."})
                return

            firebase_json = os.environ.get(
                "FIREBASE_SERVICE_ACCOUNT_JSON", ""
            ).strip()

            if not firebase_json:
                self.enviar_json(
                    503,
                    {"erro": "Firebase Admin não configurado no servidor."}
                )
                return

            import firebase_admin
            from firebase_admin import credentials, firestore, messaging

            if not firebase_admin._apps:
                credencial = credentials.Certificate(json.loads(firebase_json))
                firebase_admin.initialize_app(
                    credencial, {"projectId": "da-nike-films"}
                )

            db = firestore.client()
            referencia = (
                db.collection("usuarios")
                .document(uid)
                .collection("dispositivos")
            )

            tokens = []
            for dispositivo in referencia.stream():
                registro = dispositivo.to_dict() or {}
                token = str(registro.get("token") or dispositivo.id).strip()
                if token and token not in tokens:
                    tokens.append(token)

            if not tokens:
                self.enviar_json(200, {
                    "status": "sem_dispositivo",
                    "mensagem": "Nenhum dispositivo FCM encontrado."
                })
                return

            enviados = 0
            falhas = 0

            for token in tokens:
                try:
                    mensagem = messaging.Message(
                        data={
                            "tipo": tipo[:100],
                            "titulo": titulo[:200],
                            "texto": texto[:3000],
                            "remetenteUid": remetente_uid[:200],
                        },
                        token=token,
                        android=messaging.AndroidConfig(priority="high"),
                    )
                    messaging.send(mensagem)
                    enviados += 1
                except Exception as erro:
                    falhas += 1
                    print(
                        "[FCM] Falha ao enviar para um dispositivo:",
                        type(erro).__name__
                    )

            if enviados and fila_id:
                try:
                    db.collection("fila_notificacoes").document(fila_id).update({
                        "enviada": True,
                        "emProcessamento": False,
                        "enviadaEm": firestore.SERVER_TIMESTAMP,
                        "totalDispositivos": enviados,
                    })
                except Exception as erro:
                    print("[FCM] Não foi possível atualizar a fila:", type(erro).__name__)

            if enviados:
                self.enviar_json(200, {
                    "status": "enviado",
                    "metodo": "fcm_direto",
                    "totalDispositivos": enviados,
                    "falhas": falhas,
                })
            else:
                self.enviar_json(502, {
                    "erro": "Nenhum FCM foi enviado.",
                    "falhas": falhas,
                })

        except (ValueError, UnicodeDecodeError):
            self.enviar_json(400, {"erro": "JSON ou corpo inválido."})
        except Exception as erro:
            print("[FCM] Erro geral:", type(erro).__name__)
            self.enviar_json(500, {"erro": "Falha interna no envio FCM."})


    def do_POST(self):
        rota_nexo = self.path.split("?", 1)[0]
        if rota_nexo == "/notificar":
            self.do_notificar()
            return
        if rota_nexo == "/nexo/r2/upload-url":
            self._nexo_r2_upload_url()
            return
        if rota_nexo == "/nexo/publicar-filme":
            self._nexo_r2_publicar()
            return
        if rota_nexo == "/nexo/gerenciar-filme":
            self._nexo_r2_gerenciar()
            return

        if self.path == "/profile/delete-photo":
            self.do_delete_profile_photo()
            return
        if self.path == "/profile/upload-photo":
            self.do_upload_profile_photo()
            return
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

            chave_or = os.getenv("OPENROUTER_API_KEY", "").strip()
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
                                texto_or = "\n".join(partes).strip()

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

    servidor = ThreadingHTTPServer(
        (HOST, PORT),
        Handler
    )

    try:
        servidor.serve_forever()

    except KeyboardInterrupt:
        print("\nServidor encerrado.")

    finally:
        servidor.server_close()

# Marcador de redeploy do endpoint de fotos
