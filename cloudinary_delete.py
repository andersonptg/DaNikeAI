import os
import time
import hashlib
import urllib.parse
import urllib.request
import urllib.error
import json


def _excluir_foto_cloudinary_com_prefixo(public_id, prefixo_permitido):
    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME", "").strip()
    api_key = os.getenv("CLOUDINARY_API_KEY", "").strip()
    api_secret = os.getenv("CLOUDINARY_API_SECRET", "").strip()

    if not cloud_name or not api_key or not api_secret:
        raise RuntimeError("Cloudinary não configurado no servidor.")

    public_id = str(public_id or "").strip()
    partes = public_id.split("/")
    if (
        not public_id.startswith(prefixo_permitido)
        or len(public_id) > 500
        or not all(partes)
        or any(parte in (".", "..") for parte in partes)
        or "\\" in public_id
        or not all(
            caractere.isascii()
            and (caractere.isalnum() or caractere in "_-./")
            for caractere in public_id
        )
        or len(partes) < 2
    ):
        raise ValueError("Identificador de foto inválido.")

    timestamp = str(int(time.time()))
    assinatura_base = (
        "public_id=" + public_id
        + "&timestamp=" + timestamp
        + api_secret
    )
    assinatura = hashlib.sha1(
        assinatura_base.encode("utf-8")
    ).hexdigest()

    url = (
        "https://api.cloudinary.com/v1_1/"
        + urllib.parse.quote(cloud_name, safe="")
        + "/image/destroy"
    )
    formulario = urllib.parse.urlencode({
        "public_id": public_id,
        "timestamp": timestamp,
        "api_key": api_key,
        "signature": assinatura,
    }).encode("utf-8")

    requisicao = urllib.request.Request(
        url,
        data=formulario,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(requisicao, timeout=25) as resposta:
            resultado = json.loads(resposta.read().decode("utf-8"))
    except urllib.error.HTTPError as erro:
        raise RuntimeError(
            "Cloudinary recusou a exclusão (HTTP "
            + str(erro.code) + ")."
        ) from None

    status = str(resultado.get("result", "")).lower()
    if status not in ("ok", "not found"):
        raise RuntimeError("Cloudinary não confirmou a exclusão.")

    return status


def excluir_foto_cloudinary(public_id):
    """Exclusão restrita à pasta da equipe, preservando o comportamento atual."""
    return _excluir_foto_cloudinary_com_prefixo(public_id, "equipe/")


def excluir_foto_perfil_cloudinary(public_id, uid):
    """Exclui somente uma foto dentro da pasta do próprio usuário."""
    import re

    uid = str(uid or "").strip()
    if not re.fullmatch(r"[A-Za-z0-9_-]{1,128}", uid):
        raise ValueError("Identificador de usuário inválido.")

    prefixo = "perfis/" + uid + "/"
    public_id = str(public_id or "").strip()
    if not public_id.startswith(prefixo):
        raise ValueError("A foto não pertence a este usuário.")

    return _excluir_foto_cloudinary_com_prefixo(public_id, prefixo)
