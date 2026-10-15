from pathlib import Path
from datetime import datetime
import shutil
import sys

p = Path("app/src/main/java/com/danike/ai/PerfilFirebase.java")
s = p.read_text(encoding="utf-8")

inicio_upload = s.find('String fotoUrl = json.optString("secure_url", "");')
fim_upload = s.find('                    } catch (Exception erro) {', inicio_upload)

if inicio_upload < 0 or fim_upload < 0:
    sys.exit("ERRO: bloco de upload não localizado. Nenhuma alteração aplicada.")

bloco_antigo = s[inicio_upload:fim_upload]

if 'dadosPrivados.put("fotoUrl", urlFinal);' not in bloco_antigo:
    sys.exit("ERRO: bloco de upload diferente do esperado. Nenhuma alteração aplicada.")

novo_upload = r'''String fotoUrl = json.optString("secure_url", "");
                        String publicId = json.optString("public_id", "").trim();

                        if (!fotoUrl.startsWith("https://")) {
                            throw new java.io.IOException(
                                "O servidor não retornou uma URL válida para a foto.");
                        }

                        String prefixo = "perfis/" + user.getUid() + "/";
                        if (publicId.isEmpty() || !publicId.startsWith(prefixo)
                                || publicId.contains("..")
                                || publicId.contains("\\") ) {
                            throw new java.io.IOException(
                                "O servidor retornou um identificador de foto inválido.");
                        }

                        android.os.Handler principal =
                            new android.os.Handler(android.os.Looper.getMainLooper());
                        String urlFinal = fotoUrl;
                        String publicIdFinal = publicId;

                        principal.post(() -> {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection(COLECAO_PRIVADA)
                                .document(user.getUid())
                                .get()
                                .addOnSuccessListener(documento -> {
                                    String fotoAntiga = documento.getString("fotoPublicId");

                                    Map<String, Object> dadosPrivados = new HashMap<>();
                                    dadosPrivados.put("fotoUrl", urlFinal);
                                    dadosPrivados.put("fotoPublicId", publicIdFinal);
                                    dadosPrivados.put("fotoAtualizadaEm",
                                        FieldValue.serverTimestamp());

                                    db.collection(COLECAO_PRIVADA)
                                        .document(user.getUid())
                                        .set(dadosPrivados, SetOptions.merge())
                                        .addOnSuccessListener(v -> {
                                            Map<String, Object> dadosPublicos = new HashMap<>();
                                            dadosPublicos.put("fotoUrl", urlFinal);
                                            dadosPublicos.put("atualizadoEm",
                                                FieldValue.serverTimestamp());

                                            db.collection(COLECAO_PUBLICA)
                                                .document(user.getUid())
                                                .set(dadosPublicos, SetOptions.merge())
                                                .addOnSuccessListener(v2 -> {
                                                    if (fotoAntiga != null
                                                            && !fotoAntiga.trim().isEmpty()
                                                            && !fotoAntiga.equals(publicIdFinal)) {
                                                        excluirFotoCloudinaryRemota(
                                                            user, fotoAntiga, new Callback() {
                                                                @Override
                                                                public void sucesso() {}

                                                                @Override
                                                                public void erro(String mensagem) {}
                                                            });
                                                    }
                                                    callback.sucesso();
                                                })
                                                .addOnFailureListener(e ->
                                                    callback.erro(
                                                        "A foto foi enviada, mas não foi possível atualizar o perfil público."));
                                        })
                                        .addOnFailureListener(e ->
                                            callback.erro(
                                                "A foto foi enviada, mas não foi possível salvar o perfil."));
                                })
                                .addOnFailureListener(e ->
                                    callback.erro(
                                        "Não foi possível consultar a foto anterior."));
                        });

'''

# Substitui o upload somente depois de validar o bloco original.
s_novo = s[:inicio_upload] + novo_upload + s[fim_upload:]

inicio_remover = s_novo.find("    public static void removerFoto(")
marcador_fim = '    /**\n     * Marca o usuário como online.'
fim_remover = s_novo.find(marcador_fim, inicio_remover)

if inicio_remover < 0 or fim_remover < 0:
    sys.exit("ERRO: método removerFoto ou limite final não localizado. Arquivo original preservado.")

novo_metodo = r'''    private static void excluirFotoCloudinaryRemota(
            FirebaseUser user,
            String publicId,
            Callback callback
    ) {
        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        String prefixo = "perfis/" + user.getUid() + "/";
        if (publicId == null || !publicId.startsWith(prefixo)
                || publicId.contains("..") || publicId.contains("\\")) {
            callback.erro("A foto não pertence à sua conta.");
            return;
        }

        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String token = tokenResult.getToken();
            if (token == null || token.isEmpty()) {
                callback.erro("Não foi possível validar sua sessão.");
                return;
            }

            new Thread(() -> {
                java.net.HttpURLConnection conexao = null;
                String mensagemErro = null;
                boolean sucesso = false;

                try {
                    java.net.URL url = new java.net.URL(
                        "https://danikeai.onrender.com/profile/delete-photo");
                    conexao = (java.net.HttpURLConnection) url.openConnection();
                    conexao.setRequestMethod("POST");
                    conexao.setConnectTimeout(15000);
                    conexao.setReadTimeout(20000);
                    conexao.setDoOutput(true);
                    conexao.setRequestProperty("Authorization", "Bearer " + token);
                    conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    org.json.JSONObject corpo = new org.json.JSONObject();
                    corpo.put("publicId", publicId);

                    byte[] bytes = corpo.toString().getBytes(
                        java.nio.charset.StandardCharsets.UTF_8);
                    try (java.io.OutputStream saida = conexao.getOutputStream()) {
                        saida.write(bytes);
                    }

                    int status = conexao.getResponseCode();
                    java.io.InputStream fluxo = status >= 200 && status < 300
                        ? conexao.getInputStream() : conexao.getErrorStream();

                    StringBuilder resposta = new StringBuilder();
                    if (fluxo != null) {
                        try (java.io.BufferedReader leitor =
                                new java.io.BufferedReader(
                                    new java.io.InputStreamReader(
                                        fluxo, java.nio.charset.StandardCharsets.UTF_8))) {
                            String linha;
                            while ((linha = leitor.readLine()) != null) {
                                resposta.append(linha);
                            }
                        }
                    }

                    sucesso = status >= 200 && status < 300;
                    if (!sucesso) {
                        mensagemErro = "Servidor recusou a exclusão (HTTP "
                            + status + ").";
                    }
                } catch (Exception e) {
                    mensagemErro = "Falha ao excluir a foto na nuvem: "
                        + (e.getMessage() == null ? "erro de conexão" : e.getMessage());
                } finally {
                    if (conexao != null) conexao.disconnect();
                }

                final boolean resultadoFinal = sucesso;
                final String erroFinal = mensagemErro;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (resultadoFinal) callback.sucesso();
                    else callback.erro(erroFinal == null
                        ? "Não foi possível excluir a foto na nuvem." : erroFinal);
                });
            }, "danike-excluir-foto").start();
        }).addOnFailureListener(e ->
            callback.erro("Não foi possível validar sua sessão."));
    }

    public static void removerFoto(Callback callback) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLECAO_PRIVADA)
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documento -> {
                String publicId = documento.getString("fotoPublicId");

                Runnable limparFirestore = () -> {
                    Map<String, Object> privados = new HashMap<>();
                    privados.put("fotoUrl", FieldValue.delete());
                    privados.put("fotoPublicId", FieldValue.delete());
                    privados.put("fotoAtualizadaEm", FieldValue.serverTimestamp());

                    db.collection(COLECAO_PRIVADA)
                        .document(user.getUid())
                        .set(privados, SetOptions.merge())
                        .addOnSuccessListener(v -> {
                            Map<String, Object> publicos = new HashMap<>();
                            publicos.put("fotoUrl", FieldValue.delete());
                            publicos.put("atualizadoEm", FieldValue.serverTimestamp());

                            db.collection(COLECAO_PUBLICA)
                                .document(user.getUid())
                                .set(publicos, SetOptions.merge())
                                .addOnSuccessListener(v2 -> callback.sucesso())
                                .addOnFailureListener(e ->
                                    callback.erro("Foto pública: " + mensagemErro(e)));
                        })
                        .addOnFailureListener(e ->
                            callback.erro("Foto da conta: " + mensagemErro(e)));
                };

                if (publicId == null || publicId.trim().isEmpty()) {
                    // Compatibilidade com perfis antigos sem public_id salvo.
                    limparFirestore.run();
                    return;
                }

                excluirFotoCloudinaryRemota(user, publicId, new Callback() {
                    @Override
                    public void sucesso() {
                        limparFirestore.run();
                    }

                    @Override
                    public void erro(String mensagem) {
                        callback.erro(mensagem);
                    }
                });
            })
            .addOnFailureListener(e ->
                callback.erro("Não foi possível consultar a foto: " + mensagemErro(e)));
    }

'''

# O script só grava depois de validar todos os pontos de substituição.
backup = p.with_name(
    p.name + ".backup_foto_cloudinary_" + datetime.now().strftime("%Y%m%d_%H%M%S")
)
shutil.copy2(p, backup)
p.write_text(s_novo[:inicio_remover] + novo_metodo + s_novo[fim_remover:], encoding="utf-8")

print("ALTERAÇÃO ANDROID APLICADA")
print("Backup:", backup)
print("Upload salva fotoPublicId somente no documento privado.")
print("Remoção remota usa token Firebase e rota /profile/delete-photo.")
