from pathlib import Path
from datetime import datetime
import shutil
import re
import sys

ROOT = Path.home() / "da_nike_ai"
JAVA = ROOT / "app/src/main/java/com/danike/ai/PerfilFirebase.java"
SERVER = ROOT / "gemini_server.py"
DELETE = ROOT / "cloudinary_delete.py"

for p in (JAVA, SERVER, DELETE):
    if not p.is_file():
        sys.exit("ERRO: arquivo não encontrado: " + str(p))

j = JAVA.read_text(encoding="utf-8")
s = SERVER.read_text(encoding="utf-8")
d = DELETE.read_text(encoding="utf-8")

if 'if self.path == "/profile/delete-photo":' in s:
    sys.exit("A rota de exclusão de perfil já existe. Nada foi alterado.")

if 'public static void enviarFoto(' not in j:
    sys.exit("Método enviarFoto não encontrado.")
if 'public static void removerFoto(' not in j:
    sys.exit("Método removerFoto não encontrado.")
if 'def excluir_foto_cloudinary(public_id):' not in d:
    sys.exit("Função Cloudinary não encontrada.")
if 'if self.path == "/profile/upload-photo":' not in s:
    sys.exit("Rota de upload não encontrada.")

# Prepara a validação de prefixo mantendo equipe/ como padrão.
d2 = d.replace(
    'def excluir_foto_cloudinary(public_id):',
    'def excluir_foto_cloudinary(public_id, allowed_prefix="equipe/"):',
    1
)
alvo = '    public_id = str(public_id or "").strip()\n    partes = public_id.split("/")'
novo = '''    public_id = str(public_id or "").strip()
    import re
    if allowed_prefix != "equipe/" and not re.fullmatch(
        r"perfis/[A-Za-z0-9_-]{1,128}/", str(allowed_prefix)
    ):
        raise ValueError("Pasta de foto inválida.")
    partes = public_id.split("/")'''
if alvo not in d2:
    sys.exit("Validação do public_id não encontrada.")
d2 = d2.replace(alvo, novo, 1)
if 'not public_id.startswith("equipe/")' not in d2:
    sys.exit("Proteção da pasta equipe não encontrada.")
d2 = d2.replace(
    'not public_id.startswith("equipe/")',
    'not public_id.startswith(allowed_prefix)',
    1
)

# Adiciona endpoint de exclusão autenticada do próprio perfil.
rota = '    def do_POST(self):\n'
if rota not in s:
    sys.exit("Método do_POST não encontrado no servidor.")

metodo = '''
    def do_delete_profile_photo(self):
        """Exclui somente fotos da pasta do UID autenticado."""
        try:
            tamanho = int(self.headers.get("Content-Length", "0"))
            if tamanho <= 0 or tamanho > 16384:
                self.enviar_json(400, {"erro": "Solicitação inválida."})
                return

            auth = self.headers.get("Authorization", "")
            if not auth.startswith("Bearer "):
                self.enviar_json(401, {"erro": "Autenticação necessária."})
                return
            try:
                usuario = verificar_token_firebase(auth[7:].strip())
            except Exception:
                self.enviar_json(401, {"erro": "Token inválido ou expirado."})
                return

            uid = usuario.get("uid") or usuario.get("user_id") or usuario.get("sub")
            if not isinstance(uid, str) or not re.fullmatch(r"[A-Za-z0-9_-]{1,128}", uid):
                self.enviar_json(401, {"erro": "Conta inválida."})
                return

            try:
                dados = json.loads(self.rfile.read(tamanho).decode("utf-8"))
            except Exception:
                self.enviar_json(400, {"erro": "Dados inválidos."})
                return

            public_id = str(dados.get("publicId", "")).strip()
            prefixo = "perfis/" + uid + "/"
            if not public_id.startswith(prefixo):
                self.enviar_json(403, {"erro": "A foto não pertence a esta conta."})
                return

            try:
                resultado = excluir_foto_cloudinary(
                    public_id, allowed_prefix=prefixo
                )
            except ValueError:
                self.enviar_json(400, {"erro": "Identificador de foto inválido."})
                return
            except Exception as erro:
                print("[PERFIL] Exclusão Cloudinary falhou:", type(erro).__name__)
                self.enviar_json(502, {"erro": "Não foi possível excluir a foto agora."})
                return

            self.enviar_json(200, {"ok": True, "result": resultado})
        except Exception as erro:
            print("[PERFIL] Solicitação inválida:", type(erro).__name__)
            self.enviar_json(400, {"erro": "Solicitação inválida."})

'''
s2 = s.replace(
    rota,
    rota + '''    if self.path == "/profile/delete-photo":
        self.do_delete_profile_photo()
        return
''',
    1
)
marcador = '    def do_upload_profile_photo(self):'
if marcador not in s2:
    sys.exit("Método de upload do servidor não encontrado.")
s2 = s2.replace(marcador, metodo + marcador, 1)

# Atualiza o upload Android para salvar também o public_id.
inicio = '                        org.json.JSONObject json = new org.json.JSONObject(resposta);'
fim = '                } catch (Exception erro) {'
i = j.find(inicio)
k = j.find(fim, i + 1)
if i < 0 or k < 0:
    sys.exit("Bloco de resposta do upload Android não encontrado.")

bloco = '''                        org.json.JSONObject json = new org.json.JSONObject(resposta);
                        String fotoUrl = json.optString("secure_url", "");
                        String idNovo = json.optString("public_id", "");
                        if (!fotoUrl.startsWith("https://")
                                || !idNovo.startsWith("perfis/" + user.getUid() + "/")) {
                            throw new java.io.IOException(
                                    "O servidor não retornou dados válidos da foto.");
                        }

                        android.os.Handler principal = new android.os.Handler(
                                android.os.Looper.getMainLooper());
                        principal.post(() -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(COLECAO_PRIVADA).document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(anterior -> {
                                        String idAntigo = anterior.getString("fotoPublicId");
                                        Map<String, Object> privado = new HashMap<>();
                                        privado.put("fotoUrl", fotoUrl);
                                        privado.put("fotoPublicId", idNovo);
                                        privado.put("fotoAtualizadaEm",
                                                FieldValue.serverTimestamp());

                                        db.collection(COLECAO_PRIVADA).document(user.getUid())
                                                .set(privado, SetOptions.merge())
                                                .addOnSuccessListener(v -> {
                                                    Map<String, Object> publico = new HashMap<>();
                                                    publico.put("fotoUrl", fotoUrl);
                                                    publico.put("atualizadoEm",
                                                            FieldValue.serverTimestamp());
                                                    db.collection(COLECAO_PUBLICA)
                                                            .document(user.getUid())
                                                            .set(publico, SetOptions.merge())
                                                            .addOnSuccessListener(v2 -> {
                                                                if (idAntigo != null
                                                                        && !idAntigo.isEmpty()
                                                                        && !idAntigo.equals(idNovo)
                                                                        && idAntigo.startsWith(
                                                                                "perfis/" + user.getUid() + "/")) {
                                                                    excluirFotoCloudinaryRemota(
                                                                            user, idAntigo,
                                                                            new Callback() {
                                                                                @Override
                                                                                public void sucesso() {}
                                                                                @Override
                                                                                public void erro(String mensagem) {}
                                                                            });
                                                                }
                                                                callback.sucesso();
                                                            })
                                                            .addOnFailureListener(e ->
                                                                    callback.erro("Foto enviada, mas não foi possível atualizar o perfil público."));
                                                })
                                                .addOnFailureListener(e ->
                                                        callback.erro("Foto enviada, mas não foi possível salvar o perfil."));
                                    })
                                    .addOnFailureListener(e ->
                                            callback.erro("Não foi possível consultar a foto anterior."));
                        });
'''
j2 = j[:i] + bloco + j[k:]

# Substitui a remoção atual e inclui chamada autenticada ao servidor.
padrao = re.compile(
    r'    public static void removerFoto\(\s*Callback callback\s*\)\s*\{.*?(?=\n\s*/\*\*)',
    re.S
)
if not padrao.search(j2):
    sys.exit("Método removerFoto não encontrado para substituição.")

remocao = '''    public static void removerFoto(Callback callback) {
        FirebaseUser user = usuarioAtual();
        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLECAO_PRIVADA).document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String publicId = doc.getString("fotoPublicId");
                    Runnable limpar = () -> {
                        Map<String, Object> privado = new HashMap<>();
                        privado.put("fotoUrl", FieldValue.delete());
                        privado.put("fotoPublicId", FieldValue.delete());
                        privado.put("fotoAtualizadaEm", FieldValue.serverTimestamp());

                        db.collection(COLECAO_PRIVADA).document(user.getUid())
                                .set(privado, SetOptions.merge())
                                .addOnSuccessListener(v -> {
                                    Map<String, Object> publico = new HashMap<>();
                                    publico.put("fotoUrl", FieldValue.delete());
                                    publico.put("atualizadoEm", FieldValue.serverTimestamp());
                                    db.collection(COLECAO_PUBLICA).document(user.getUid())
                                            .set(publico, SetOptions.merge())
                                            .addOnSuccessListener(v2 -> callback.sucesso())
                                            .addOnFailureListener(e ->
                                                    callback.erro("Foto pública: " + mensagemErro(e)));
                                })
                                .addOnFailureListener(e ->
                                        callback.erro("Foto da conta: " + mensagemErro(e)));
                    };

                    if (publicId == null || publicId.trim().isEmpty()) {
                        limpar.run();
                    } else if (!publicId.startsWith("perfis/" + user.getUid() + "/")) {
                        callback.erro("A foto salva não pertence a esta conta. Nada foi excluído.");
                    } else {
                        excluirFotoCloudinaryRemota(user, publicId, new Callback() {
                            @Override
                            public void sucesso() { limpar.run(); }
                            @Override
                            public void erro(String mensagem) {
                                callback.erro("Não foi possível excluir a foto remota. Os dados foram mantidos.");
                            }
                        });
                    }
                })
                .addOnFailureListener(e ->
                        callback.erro("Não foi possível consultar a foto: " + mensagemErro(e)));
    }

    private static void excluirFotoCloudinaryRemota(
            FirebaseUser user, String publicId, Callback callback) {
        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String token = tokenResult.getToken();
            if (token == null || token.isEmpty()) {
                callback.erro("Não foi possível validar sua sessão.");
                return;
            }
            new Thread(() -> {
                HttpURLConnection conexao = null;
                try {
                    java.net.URL url = new java.net.URL(
                            "https://danikeai.onrender.com/profile/delete-photo");
                    conexao = (HttpURLConnection) url.openConnection();
                    conexao.setRequestMethod("POST");
                    conexao.setConnectTimeout(20000);
                    conexao.setReadTimeout(30000);
                    conexao.setDoOutput(true);
                    conexao.setRequestProperty("Authorization", "Bearer " + token);
                    conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    byte[] corpo = new org.json.JSONObject()
                            .put("publicId", publicId).toString()
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    conexao.setFixedLengthStreamingMode(corpo.length);
                    try (java.io.OutputStream out = conexao.getOutputStream()) {
                        out.write(corpo);
                    }
                    int status = conexao.getResponseCode();
                    if (status < 200 || status >= 300) {
                        throw new java.io.IOException("HTTP " + status);
                    }
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(callback::sucesso);
                } catch (Exception erro) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(() -> callback.erro("Falha ao excluir a foto remota."));
                } finally {
                    if (conexao != null) conexao.disconnect();
                }
            }, "danike-delete-foto").start();
        }).addOnFailureListener(e ->
                callback.erro("Não foi possível validar sua sessão."));
    }

'''
j2 = padrao.sub(lambda _: remocao, j2, count=1)

# Confere a estrutura básica antes de salvar.
if 'fotoPublicId' not in j2 or 'excluirFotoCloudinaryRemota' not in j2:
    sys.exit("Verificação Java falhou. Nada foi gravado.")
if 'profile/delete-photo' not in s2:
    sys.exit("Verificação da rota falhou. Nada foi gravado.")

backup = ROOT / ("backup_fotos_perfil_" + datetime.now().strftime("%Y%m%d_%H%M%S"))
backup.mkdir()
for p in (JAVA, SERVER, DELETE):
    shutil.copy2(p, backup / p.name)

JAVA.write_text(j2, encoding="utf-8")
SERVER.write_text(s2, encoding="utf-8")
DELETE.write_text(d2, encoding="utf-8")

print("CORRECAO APLICADA")
print("Backup:", backup)
print("Arquivos alterados: PerfilFirebase.java, gemini_server.py, cloudinary_delete.py")
print("A exclusão da equipe continua usando equipe/ por padrão.")
