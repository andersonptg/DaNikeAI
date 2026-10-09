package com.danike.ai;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PerfilFirebase {

    private PerfilFirebase() {}

    private static final String COLECAO_PRIVADA = "usuarios";
    private static final String COLECAO_PUBLICA = "perfis_publicos";
    private static final String PASTA_FOTOS = "fotos_perfil";

    public interface Callback {
        void sucesso();
        void erro(String mensagem);
    }

    public interface PerfilCallback {
        void sucesso(DocumentSnapshot documento);
        void erro(String mensagem);
    }

    public static FirebaseUser usuarioAtual() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String uidAtual() {
        FirebaseUser user = usuarioAtual();
        return user == null ? "" : user.getUid();
    }

    public static boolean estaLogado() {
        return usuarioAtual() != null;
    }

    /**
     * Cria/atualiza o registro privado da conta e o perfil público.
     *
     * O UID e o e-mail ficam SOMENTE em usuarios/{uid}.
     * A coleção perfis_publicos não recebe esses dados internos.
     */
    public static void garantirPerfil(
            Context context,
            String nome,
            Callback callback
    ) {
        garantirPerfil(context, nome, "", callback);
    }

    /**
     * Cria/atualiza o perfil incluindo o telefone.
     *
     * O telefone fica somente em usuarios/{uid}.
     * A coleção perfis_publicos não recebe o número.
     */
    public static void garantirPerfil(
            Context context,
            String nome,
            String telefone,
            Callback callback
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Nenhuma conta autenticada.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = user.getUid();
        String email = user.getEmail() == null ? "" : user.getEmail();

        String nomeFinal = nome == null ? "" : nome.trim();

        if (nomeFinal.isEmpty()) {
            nomeFinal = "Usuário";
        }

        String telefoneFinal = telefone == null ? "" : telefone.trim();
        String telefoneNormalizado =
                telefoneFinal.replaceAll("\\D", "");

        final String nomePerfil = nomeFinal;
        final String telefonePerfil = telefoneFinal;
        final String telefoneBusca = telefoneNormalizado;

        db.collection(COLECAO_PRIVADA)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String identificador =
                            doc.getString("identificadorPublico");

                    if (identificador == null
                            || identificador.trim().isEmpty()) {

                        identificador = gerarIdentificador();
                    }

                    final String idPublico = identificador;

                    Map<String, Object> privado =
                            new HashMap<>();

                    privado.put("uid", uid);
                    privado.put("email", email);
                    privado.put("nome", nomePerfil);

                    if (!telefoneBusca.isEmpty()) {
                        privado.put("telefone", telefonePerfil);
                        privado.put("telefoneNormalizado", telefoneBusca);
                    }

                    privado.put(
                            "identificadorPublico",
                            idPublico
                    );

                    if (!doc.contains("criadoEm")) {
                        privado.put(
                                "criadoEm",
                                FieldValue.serverTimestamp()
                        );
                    }

                    privado.put(
                            "atualizadoEm",
                            FieldValue.serverTimestamp()
                    );

                    privado.put(
                            "online",
                            true
                    );

                    privado.put(
                            "ultimoAcesso",
                            FieldValue.serverTimestamp()
                    );

                    privado.put(
                            "ultimoStatus",
                            "online"
                    );

                    if ("lipesanderson@gmail.com"
                            .equalsIgnoreCase(email)) {

                        privado.put(
                                "tipo",
                                "proprietario"
                        );

                        privado.put(
                                "ehProprietario",
                                true
                        );

                    } else {

                        privado.put(
                                "tipo",
                                "usuario"
                        );

                        privado.put(
                                "ehProprietario",
                                false
                        );
                    }

                    Map<String, Object> publico =
                            new HashMap<>();

                    publico.put(
                            "nome",
                            nomePerfil
                    );

                    publico.put(
                            "identificadorPublico",
                            idPublico
                    );

                    publico.put(
                            "online",
                            true
                    );

                    publico.put(
                            "ultimoStatus",
                            "online"
                    );

                    publico.put(
                            "ultimoAcesso",
                            FieldValue.serverTimestamp()
                    );

                    publico.put(
                            "atualizadoEm",
                            FieldValue.serverTimestamp()
                    );

                    if (doc.contains("fotoUrl")) {
                        String fotoUrl =
                                doc.getString("fotoUrl");

                        if (fotoUrl != null
                                && !fotoUrl.trim().isEmpty()) {

                            publico.put(
                                    "fotoUrl",
                                    fotoUrl
                            );
                        }
                    }

                    Map<String, Object> finalPrivado =
                            privado;

                    Map<String, Object> finalPublico =
                            publico;

                    db.collection(COLECAO_PRIVADA)
                            .document(uid)
                            .set(
                                    finalPrivado,
                                    SetOptions.merge()
                            )
                            .addOnSuccessListener(v -> {

                                db.collection(COLECAO_PUBLICA)
                                        .document(uid)
                                        .set(
                                                finalPublico,
                                                SetOptions.merge()
                                        )
                                        .addOnSuccessListener(v2 -> {

                                            context
                                                    .getSharedPreferences(
                                                            "DaNikeAI_Dados",
                                                            Context.MODE_PRIVATE
                                                    )
                                                    .edit()
                                                    .putString(
                                                            "nome",
                                                            nomePerfil
                                                    )
                                                    .putString(
                                                            "identificador_publico",
                                                            idPublico
                                                    )
                                                    .putString(
                                                            "telefone",
                                                            telefonePerfil
                                                    )
                                                    .putString(
                                                            "telefoneNormalizado",
                                                            telefoneBusca
                                                    )
                                                    .apply();

                                            callback.sucesso();

                                        })
                                        .addOnFailureListener(e ->
                                                callback.erro(
                                                        "Perfil público: "
                                                                + mensagemErro(e)
                                                )
                                        );

                            })
                            .addOnFailureListener(e ->
                                    callback.erro(
                                            "Perfil da conta: "
                                                    + mensagemErro(e)
                                    )
                            );

                })
                .addOnFailureListener(e ->
                        callback.erro(
                                "Não foi possível carregar o perfil: "
                                        + mensagemErro(e)
                        )
                );
    }

    /**
     * Carrega o perfil privado da própria conta.
     * Nunca deve ser usado para exibir UID a outros usuários.
     */
    public static void carregarMeuPerfil(
            PerfilCallback callback
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(COLECAO_PRIVADA)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(callback::sucesso)
                .addOnFailureListener(e ->
                        callback.erro(
                                "Não foi possível carregar seu perfil."
                        )
                );
    }

    /**
     * Atualiza nome nas duas visões do perfil.
     */
    
public static void salvarDadosPerfil(
        Context context,
        String handle,
        String descricao,
        boolean publico,
        Callback callback) {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
        callback.erro("Você precisa estar conectado à sua conta.");
        return;
    }

    String uid = user.getUid();
    String handleFinal = handle == null ? "" :
        handle.trim().replaceFirst("^@", "")
            .toLowerCase(Locale.ROOT);
    String descricaoFinal = descricao == null ? "" : descricao.trim();

    if (!handleFinal.matches("[a-z0-9.]{3,20}")
            || handleFinal.startsWith(".")
            || handleFinal.endsWith(".")
            || handleFinal.contains("..")) {
        callback.erro("O @usuário informado é inválido.");
        return;
    }

    if (descricaoFinal.length() > 160) {
        callback.erro("A descrição excede 160 caracteres.");
        return;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    db.collection(COLECAO_PUBLICA)
        .whereEqualTo("handle", handleFinal)
        .get()
        .addOnSuccessListener(resultado -> {
            for (DocumentSnapshot encontrado : resultado.getDocuments()) {
                if (!uid.equals(encontrado.getId())) {
                    callback.erro("Esse @usuário já está em uso.");
                    return;
                }
            }

            Map<String, Object> campos = new HashMap<>();
            campos.put("handle", handleFinal);
            campos.put("descricao", descricaoFinal);
            campos.put("perfilPublico", publico);
            campos.put("atualizadoEm", FieldValue.serverTimestamp());

            db.collection(COLECAO_PRIVADA).document(uid)
                .set(campos, SetOptions.merge())
                .addOnSuccessListener(v ->
                    db.collection(COLECAO_PUBLICA).document(uid)
                        .set(campos, SetOptions.merge())
                        .addOnSuccessListener(v2 -> callback.sucesso())
                        .addOnFailureListener(e ->
                            callback.erro("Falha ao atualizar o perfil público.")))
                .addOnFailureListener(e ->
                    callback.erro("Falha ao salvar os dados da conta."));
        })
        .addOnFailureListener(e ->
            callback.erro("Não foi possível verificar se o @usuário está disponível."));
}

public static void atualizarNome(
            Context context,
            String nome,
            Callback callback
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        String nomeFinal =
                nome == null ? "" : nome.trim();

        if (nomeFinal.isEmpty()) {
            callback.erro("Digite um nome válido.");
            return;
        }

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> dados =
                new HashMap<>();

        dados.put("nome", nomeFinal);
        dados.put(
                "atualizadoEm",
                FieldValue.serverTimestamp()
        );

        db.collection(COLECAO_PRIVADA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                )
                .addOnSuccessListener(v -> {

                    db.collection(COLECAO_PUBLICA)
                            .document(user.getUid())
                            .set(
                                    dados,
                                    SetOptions.merge()
                            )
                            .addOnSuccessListener(v2 -> {

                                context
                                        .getSharedPreferences(
                                                "DaNikeAI_Dados",
                                                Context.MODE_PRIVATE
                                        )
                                        .edit()
                                        .putString(
                                                "nome",
                                                nomeFinal
                                        )
                                        .apply();

                                callback.sucesso();

                            })
                            .addOnFailureListener(e ->
                                    callback.erro(
                                            "Nome público: "
                                                    + mensagemErro(e)
                                    )
                            );

                })
                .addOnFailureListener(e ->
                        callback.erro(
                                "Nome da conta: "
                                        + mensagemErro(e)
                        )
                );
    }

    /**
     * Envia a foto original escolhida pelo usuário
     * para o Firebase Storage.
     *
     * Não fazemos compressão ou redimensionamento aqui.
     */
    public static void enviarFoto(
            Uri uri,
            Callback callback
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Você precisa estar conectado à sua conta.");
            return;
        }
        if (uri == null) {
            callback.erro("Nenhuma foto foi selecionada.");
            return;
        }

        user.getIdToken(false)
            .addOnSuccessListener(tokenResult -> {
                String token = tokenResult.getToken();
                if (token == null || token.isEmpty()) {
                    callback.erro("Não foi possível validar sua sessão.");
                    return;
                }

                new Thread(() -> {
                    HttpURLConnection conexao = null;
                    try {
                        android.content.Context contexto =
                            com.google.firebase.FirebaseApp.getInstance()
                                .getApplicationContext();

                        String tipo = contexto.getContentResolver().getType(uri);
                        if (tipo == null || tipo.trim().isEmpty()) {
                            tipo = "image/jpeg";
                        }
                        tipo = tipo.toLowerCase(Locale.ROOT);
                        if (!tipo.equals("image/jpeg")
                                && !tipo.equals("image/png")
                                && !tipo.equals("image/webp")) {
                            throw new IllegalArgumentException(
                                "Formato não aceito. Selecione JPG, PNG ou WebP.");
                        }

                        byte[] bytes;
                        try (java.io.InputStream entrada =
                                 contexto.getContentResolver().openInputStream(uri);
                             java.io.ByteArrayOutputStream buffer =
                                 new java.io.ByteArrayOutputStream()) {
                            if (entrada == null) {
                                throw new java.io.IOException(
                                    "Não foi possível abrir a foto selecionada.");
                            }
                            byte[] bloco = new byte[8192];
                            int total = 0;
                            int lidos;
                            while ((lidos = entrada.read(bloco)) != -1) {
                                total += lidos;
                                if (total > 8 * 1024 * 1024) {
                                    throw new IllegalArgumentException(
                                        "A foto deve ter até 8 MB.");
                                }
                                buffer.write(bloco, 0, lidos);
                            }
                            bytes = buffer.toByteArray();
                        }

                        java.net.URL endereco = new java.net.URL(
                            "https://danikeai.onrender.com/profile/upload-photo");
                        conexao = (HttpURLConnection) endereco.openConnection();
                        conexao.setRequestMethod("POST");
                        conexao.setConnectTimeout(20000);
                        conexao.setReadTimeout(60000);
                        conexao.setDoOutput(true);
                        conexao.setRequestProperty("Authorization", "Bearer " + token);
                        conexao.setRequestProperty("Content-Type", tipo);
                        conexao.setFixedLengthStreamingMode(bytes.length);

                        try (java.io.OutputStream saida = conexao.getOutputStream()) {
                            saida.write(bytes);
                            saida.flush();
                        }

                        int status = conexao.getResponseCode();
                        java.io.InputStream respostaStream =
                            status >= 200 && status < 400
                                ? conexao.getInputStream()
                                : conexao.getErrorStream();

                        String resposta = "";
                        if (respostaStream != null) {
                            try (java.io.InputStream entradaResposta = respostaStream;
                                 java.io.ByteArrayOutputStream bufferResposta =
                                     new java.io.ByteArrayOutputStream()) {
                                byte[] blocoResposta = new byte[4096];
                                int n;
                                while ((n = entradaResposta.read(blocoResposta)) != -1) {
                                    bufferResposta.write(blocoResposta, 0, n);
                                }
                                resposta = bufferResposta.toString("UTF-8");
                            }
                        }

                        if (status < 200 || status >= 300) {
                            String mensagem = "Falha ao enviar a foto (HTTP " + status + ").";
                            try {
                                org.json.JSONObject erroJson =
                                    new org.json.JSONObject(resposta);
                                mensagem = erroJson.optString("erro", mensagem);
                            } catch (Exception ignorado) {
                                // Mantém mensagem segura se a resposta não for JSON.
                            }
                            throw new java.io.IOException(mensagem);
                        }

                        org.json.JSONObject json = new org.json.JSONObject(resposta);
                        String fotoUrl = json.optString("secure_url", "");
                        if (!fotoUrl.startsWith("https://")) {
                            throw new java.io.IOException(
                                "O servidor não retornou uma URL válida para a foto.");
                        }

                        android.os.Handler principal =
                            new android.os.Handler(android.os.Looper.getMainLooper());
                        String urlFinal = fotoUrl;
                        principal.post(() -> {
                            Map<String, Object> dadosPrivados = new HashMap<>();
                            dadosPrivados.put("fotoUrl", urlFinal);
                            dadosPrivados.put("fotoAtualizadaEm",
                                FieldValue.serverTimestamp());

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(COLECAO_PRIVADA).document(user.getUid())
                                .set(dadosPrivados, SetOptions.merge())
                                .addOnSuccessListener(v -> {
                                    Map<String, Object> dadosPublicos = new HashMap<>();
                                    dadosPublicos.put("fotoUrl", urlFinal);
                                    dadosPublicos.put("atualizadoEm",
                                        FieldValue.serverTimestamp());

                                    db.collection(COLECAO_PUBLICA)
                                        .document(user.getUid())
                                        .set(dadosPublicos, SetOptions.merge())
                                        .addOnSuccessListener(v2 -> callback.sucesso())
                                        .addOnFailureListener(e ->
                                            callback.erro(
                                                "A foto foi enviada, mas não foi possível atualizar o perfil público."));
                                })
                                .addOnFailureListener(e ->
                                    callback.erro(
                                        "A foto foi enviada, mas não foi possível salvar o perfil."));
                        });

                    } catch (Exception erro) {
                        String mensagem = erro.getMessage();
                        if (mensagem == null || mensagem.trim().isEmpty()) {
                            mensagem = "Não foi possível enviar a foto. Tente novamente.";
                        }
                        String mensagemFinal = mensagem;
                        new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(() -> callback.erro(mensagemFinal));
                    } finally {
                        if (conexao != null) {
                            conexao.disconnect();
                        }
                    }
                }, "danike-upload-foto").start();
            })
            .addOnFailureListener(e ->
                callback.erro("Não foi possível validar sua sessão. Entre novamente."));
    }


    public static void removerFoto(
            Callback callback
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) {
            callback.erro("Usuário não autenticado.");
            return;
        }

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> dados =
                new HashMap<>();

        dados.put(
                "fotoUrl",
                FieldValue.delete()
        );

        dados.put(
                "fotoAtualizadaEm",
                FieldValue.serverTimestamp()
        );

        db.collection(COLECAO_PRIVADA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                )
                .addOnSuccessListener(v -> {

                    db.collection(COLECAO_PUBLICA)
                            .document(user.getUid())
                            .set(
                                    dados,
                                    SetOptions.merge()
                            )
                            .addOnSuccessListener(v2 ->
                                    callback.sucesso()
                            )
                            .addOnFailureListener(e ->
                                    callback.erro(
                                            "Foto pública: "
                                                    + mensagemErro(e)
                                    )
                            );

                })
                .addOnFailureListener(e ->
                        callback.erro(
                                "Foto da conta: "
                                        + mensagemErro(e)
                        )
                );
    }

    /**
     * Marca o usuário como online.
     */
    public static void marcarOnline() {
        atualizarPresenca(
                true,
                "online"
        );
    }

    /**
     * Marca o usuário como offline e registra
     * o horário da última saída.
     */
    public static void marcarOffline() {
        FirebaseUser user = usuarioAtual();

        if (user == null) return;

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> dados =
                new HashMap<>();

        dados.put("online", false);
        dados.put(
                "ultimoStatus",
                "offline"
        );
        dados.put(
                "ultimaSaida",
                FieldValue.serverTimestamp()
        );
        dados.put(
                "ultimoAcesso",
                FieldValue.serverTimestamp()
        );

        db.collection(COLECAO_PRIVADA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                );

        db.collection(COLECAO_PUBLICA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                );
    }

    /**
     * Atualiza a presença nas duas coleções.
     */
    public static void atualizarPresenca(
            boolean online,
            String status
    ) {
        FirebaseUser user = usuarioAtual();

        if (user == null) return;

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        Map<String, Object> dados =
                new HashMap<>();

        dados.put(
                "online",
                online
        );

        dados.put(
                "ultimoStatus",
                status
        );

        dados.put(
                "ultimoAcesso",
                FieldValue.serverTimestamp()
        );

        db.collection(COLECAO_PRIVADA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                );

        db.collection(COLECAO_PUBLICA)
                .document(user.getUid())
                .set(
                        dados,
                        SetOptions.merge()
                );
    }

    /**
     * Heartbeat simples para manter presença atualizada.
     * O aplicativo poderá chamar esse método periodicamente.
     */
    public static void iniciarHeartbeat() {

        final Handler handler =
                new Handler(
                        Looper.getMainLooper()
                );

        final Runnable[] tarefa =
                new Runnable[1];

        tarefa[0] = () -> {

            if (usuarioAtual() != null) {
                atualizarPresenca(
                        true,
                        "online"
                );

                handler.postDelayed(
                        tarefa[0],
                        30000
                );
            }
        };

        handler.post(tarefa[0]);
    }

    /**
     * Gera um identificador público.
     *
     * Exemplo:
     * DNK-7F42K9
     *
     * Não é o UID do Firebase.
     */
    private static String gerarIdentificador() {

        String codigo =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                6
                        )
                        .toUpperCase(
                                Locale.US
                        );

        return "DNK-" + codigo;
    }

    private static String mensagemErro(
            Exception e
    ) {
        if (e == null) {
            return "erro desconhecido";
        }

        String mensagem =
                e.getMessage();

        if (mensagem == null
                || mensagem.trim().isEmpty()) {
            return "erro desconhecido";
        }

        return mensagem;
    }
}
