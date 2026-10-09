import json
import urllib.request
import jwt

FIREBASE_PROJECT_ID = "da-nike-films"
OWNER_EMAIL = "lipesanderson@gmail.com"
FIREBASE_CERTS_URL = (
    "https://www.googleapis.com/robot/v1/metadata/x509/"
    "securetoken@system.gserviceaccount.com"
)


def verificar_token_firebase(token):
    requisicao = urllib.request.Request(
        FIREBASE_CERTS_URL,
        headers={"Accept": "application/json"},
    )

    with urllib.request.urlopen(requisicao, timeout=15) as resposta:
        certificados = json.loads(resposta.read().decode("utf-8"))

    cabecalho = jwt.get_unverified_header(token)
    certificado = certificados.get(cabecalho.get("kid"))

    if not certificado:
        raise ValueError("Certificado Firebase não encontrado.")

    usuario = jwt.decode(
        token,
        certificado,
        algorithms=["RS256"],
        audience=FIREBASE_PROJECT_ID,
        issuer="https://securetoken.google.com/" + FIREBASE_PROJECT_ID,
        options={"require": ["exp", "iat", "sub"]},
    )

    if not usuario.get("email"):
        raise ValueError("Token sem e-mail.")

    return usuario


def usuario_e_proprietario(token):
    usuario = verificar_token_firebase(token)
    return (
        str(usuario.get("email", "")).strip().lower()
        == OWNER_EMAIL
    )
