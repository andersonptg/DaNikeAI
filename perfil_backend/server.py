import hashlib
import hmac
import json
import os
import re
import time
import uuid
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import jwt

FIREBASE_PROJECT_ID = os.getenv(
    "FIREBASE_PROJECT_ID", "da-nike-films"
).strip()

FIREBASE_CERTS_URL = (
    "https://www.googleapis.com/robot/v1/metadata/x509/"
    "securetoken@system.gserviceaccount.com"
)

MAX_UPLOAD = 8 * 1024 * 1024
UID_RE = re.compile(r"^[A-Za-z0-9_-]{1,128}$")
PUBLIC_ID_RE = re.compile(
    r"^perfis/[A-Za-z0-9_-]{1,128}/[A-Za-z0-9_-]{1,128}$"
)

ALLOWED_TYPES = {
    "image/jpeg": b"\xff\xd8\xff",
    "image/png": b"\x89PNG\r\n\x1a\n",
}

def validar_uid(uid):
    return isinstance(uid, str) and bool(UID_RE.fullmatch(uid))


def validar_public_id(public_id, uid):
    if not validar_uid(uid) or not isinstance(public_id, str):
        return False
    if not PUBLIC_ID_RE.fullmatch(public_id):
        return False
    return public_id.startswith("perfis/" + uid + "/")


def assinatura_cloudinary(parametros, segredo):
    texto = "&".join(
        f"{chave}={parametros[chave]}"
        for chave in sorted(parametros)
        if parametros[chave] is not None
    )
    return hashlib.sha1(
        (texto + segredo).encode("utf-8")
    ).hexdigest()


def verificar_token_firebase(token):
    if not token or len(token) > 10000:
        raise ValueError("Token inválido")

    pedido = urllib.request.Request(
        FIREBASE_CERTS_URL,
        headers={"Accept": "application/json"},
    )
    with urllib.request.urlopen(pedido, timeout=15) as resposta:
        certificados = json.loads(resposta.read().decode("utf-8"))

    cabecalho = jwt.get_unverified_header(token)
    if cabecalho.get("alg") != "RS256":
        raise ValueError("Algoritmo inválido")

    certificado = certificados.get(cabecalho.get("kid"))
    if not certificado:
        raise ValueError("Certificado não encontrado")

    usuario = jwt.decode(
        token,
        certificado,
        algorithms=["RS256"],
        audience=FIREBASE_PROJECT_ID,
        issuer="https://securetoken.google.com/" + FIREBASE_PROJECT_ID,
        options={"require": ["exp", "iat", "sub"]},
    )

    uid = usuario.get("sub")
    if not validar_uid(uid):
        raise ValueError("UID inválido")

    return usuario


def cloudinary_config():
    cloud = os.getenv("CLOUDINARY_CLOUD_NAME", "").strip()
    api_key = os.getenv("CLOUDINARY_API_KEY", "").strip()
    segredo = os.getenv("CLOUDINARY_API_SECRET", "").strip()

    if not cloud or not api_key or not segredo:
        raise RuntimeError("Cloudinary não configurado")

    return cloud, api_key, segredo


def cloudinary_post(caminho, campos, arquivo=None):
    cloud, api_key, segredo = cloudinary_config()
    timestamp = str(int(time.time()))

    parametros = dict(campos)
    parametros["timestamp"] = timestamp

    assinatura = assinatura_cloudinary(parametros, segredo)
    dados = dict(parametros)
    dados["api_key"] = api_key
    dados["signature"] = assinatura

    limite = "----DaNikePerfil" + uuid.uuid4().hex
    partes = []

    for nome, valor in dados.items():
        partes.append(
            ("--" + limite + "\r\n").encode()
            + (
                f'Content-Disposition: form-data; name="{nome}"'
                "\r\n\r\n"
            ).encode()
            + str(valor).encode()
            + b"\r\n"
        )

    if arquivo is not None:
        conteudo, tipo, nome_arquivo = arquivo
        partes.append(
            ("--" + limite + "\r\n").encode()
            + (
                'Content-Disposition: form-data; name="file"; '
                f'filename="{nome_arquivo}"\r\n'
                f"Content-Type: {tipo}\r\n\r\n"
            ).encode()
            + conteudo
            + b"\r\n"
        )

    partes.append(("--" + limite + "--\r\n").encode())
    corpo = b"".join(partes)

    url = (
        f"https://api.cloudinary.com/v1_1/{cloud}/"
        f"image/{caminho}"
    )
    pedido = urllib.request.Request(
        url,
        data=corpo,
        headers={
            "Content-Type": f"multipart/form-data; boundary={limite}",
            "Accept": "application/json",
        },
        method="POST",
    )

    with urllib.request.urlopen(pedido, timeout=45) as resposta:
        resultado = json.loads(resposta.read().decode("utf-8"))

    return resultado


def enviar_foto(uid, imagem, tipo):
    if not validar_uid(uid):
        raise ValueError("Usuário inválido")

    if tipo == "image/webp":
        assinatura_ok = (
            len(imagem) >= 12
            and imagem[:4] == b"RIFF"
            and imagem[8:12] == b"WEBP"
        )
    else:
        assinatura_ok = (
            tipo in ALLOWED_TYPES
            and imagem.startswith(ALLOWED_TYPES[tipo])
        )

    if not assinatura_ok:
        raise ValueError("Formato ou conteúdo de imagem inválido")

    nome = uuid.uuid4().hex
    resultado = cloudinary_post(
        "upload",
        {
            "folder": f"perfis/{uid}",
            "public_id": nome,
            "overwrite": "false",
        },
        arquivo=(imagem, tipo, nome),
    )

    public_id = resultado.get("public_id", "")
    secure_url = resultado.get("secure_url", "")

    if not validar_public_id(public_id, uid):
        raise RuntimeError("Identificador inesperado retornado")

    if not isinstance(secure_url, str) or not secure_url.startswith(
        "https://"
    ):
        raise RuntimeError("URL de imagem inválida")

    return {"ok": True, "secure_url": secure_url, "public_id": public_id}


def excluir_foto(uid, public_id):
    if not validar_public_id(public_id, uid):
        raise ValueError("A foto não pertence a este usuário")

    resultado = cloudinary_post(
        "destroy",
        {"public_id": public_id},
    )
    status = resultado.get("result")

    if status not in ("ok", "not found"):
        raise RuntimeError("Falha na exclusão")

    return status


class PerfilHandler(BaseHTTPRequestHandler):
    server_version = "DaNikeAI-Perfil"
    sys_version = ""

    def enviar_json(self, status, dados):
        corpo = json.dumps(
            dados, ensure_ascii=False
        ).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(corpo)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(corpo)

    def autenticar(self):
        cabecalho = self.headers.get("Authorization", "")
        if not cabecalho.startswith("Bearer "):
            return None

        token = cabecalho[7:].strip()
        try:
            usuario = verificar_token_firebase(token)
            uid = usuario.get("sub")
            if not validar_uid(uid):
                return None
            return uid
        except Exception as erro:
            # Nunca imprimir token, chave ou segredo.
            print(
                "[PERFIL_AUTH] Falha: " + type(erro).__name__,
                flush=True,
            )
            return None

    def do_GET(self):
        if self.path == "/health":
            self.enviar_json(200, {"ok": True, "servico": "DaNikeAI-perfil"})
        else:
            self.enviar_json(404, {"erro": "Rota não encontrada."})

    def do_POST(self):
        if self.path not in (
            "/profile/upload-photo",
            "/profile/delete-photo",
        ):
            self.enviar_json(404, {"erro": "Rota não encontrada."})
            return

        uid = self.autenticar()
        if not uid:
            self.enviar_json(401, {"erro": "Token inválido ou expirado."})
            return

        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
        except (TypeError, ValueError):
            tamanho = 0

        if self.path == "/profile/upload-photo":
            if tamanho < 1 or tamanho > MAX_UPLOAD:
                self.enviar_json(
                    413, {"erro": "A foto deve ter até 8 MB."}
                )
                return

            tipo = (
                self.headers.get("Content-Type", "")
                .split(";")[0].strip().lower()
            )
            if tipo not in (
                "image/jpeg", "image/png", "image/webp"
            ):
                self.enviar_json(
                    415, {"erro": "Formato inválido. Use JPG, PNG ou WebP."}
                )
                return

            try:
                imagem = self.rfile.read(tamanho)
                if len(imagem) != tamanho:
                    self.enviar_json(400, {"erro": "Upload incompleto."})
                    return

                resultado = enviar_foto(uid, imagem, tipo)
                self.enviar_json(200, resultado)
            except ValueError as erro:
                mensagem = str(erro)
                self.enviar_json(415, {"erro": mensagem})
            except Exception as erro:
                print(
                    "[PERFIL_UPLOAD] Falha: " + type(erro).__name__,
                    flush=True,
                )
                self.enviar_json(
                    502, {"erro": "Não foi possível enviar a foto."}
                )
            return

        if tamanho < 1 or tamanho > 16384:
            self.enviar_json(400, {"erro": "Solicitação inválida."})
            return

        try:
            dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
            public_id = dados.get("publicId", "")
            if not isinstance(public_id, str):
                raise ValueError("Identificador inválido")
            public_id = public_id.strip()

            if not validar_public_id(public_id, uid):
                self.enviar_json(
                    403, {"erro": "A foto não pertence a este usuário."}
                )
                return

            resultado = excluir_foto(uid, public_id)
            self.enviar_json(
                200, {"ok": True, "resultado": resultado}
            )
        except (UnicodeDecodeError, json.JSONDecodeError):
            self.enviar_json(400, {"erro": "JSON inválido."})
        except ValueError:
            self.enviar_json(403, {"erro": "Identificador inválido."})
        except Exception as erro:
            print(
                "[PERFIL_DELETE] Falha: " + type(erro).__name__,
                flush=True,
            )
            self.enviar_json(
                502, {"erro": "Não foi possível excluir a foto."}
            )

    def log_message(self, formato, *args):
        # Não registra cabeçalhos de autenticação.
        print("[PERFIL_HTTP] " + (formato % args), flush=True)


if __name__ == "__main__":
    host = "0.0.0.0"
    port = int(os.getenv("PORT", "10000"))
    print("[PERFIL] Servidor iniciado na porta " + str(port), flush=True)
    ThreadingHTTPServer((host, port), PerfilHandler).serve_forever()
