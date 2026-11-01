package com.danike.ai;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cliente isolado do suporte Central NEXO 1.1.0. Nunca recebe credenciais ADM do app. */
public final class NexoSuporteApi {
    private static final String ENDPOINT =
            "https://little-nexo-core-hub.base44.app/functions/nexoApi";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    public interface Callback {
        void sucesso(JSONObject corpo);
        void erro(String mensagem);
    }

    private NexoSuporteApi() {}

    public static void requisitar(String metodo, String rota, JSONObject corpo,
                                  Callback callback) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            callback.erro("Entre na sua conta para acessar o suporte.");
            return;
        }
        usuario.getIdToken(false).addOnSuccessListener(resultado -> {
            String token = resultado.getToken();
            if (token == null || token.isEmpty()) {
                callback.erro("Não foi possível autenticar sua sessão.");
                return;
            }
            EXECUTOR.execute(() -> executar(metodo, rota, corpo, token, callback));
        }).addOnFailureListener(erro -> callback.erro("Sessão indisponível. Faça login novamente."));
    }

    private static void executar(String metodo, String rota, JSONObject corpo,
                                 String token, Callback callback) {
        HttpURLConnection conexao = null;
        try {
            // O Base44 usa ?route=/api/v1/support e os parâmetros extras na query.
            String caminho = rota;
            String parametros = "";
            int separador = rota.indexOf('?');
            if (separador >= 0) {
                caminho = rota.substring(0, separador);
                parametros = rota.substring(separador + 1);
            }
            if (!caminho.startsWith("/api/v1/support")) {
                callback.erro("Rota de suporte inválida.");
                return;
            }
            String endereco = ENDPOINT + "?route="
                    + URLEncoder.encode(caminho, "UTF-8")
                    + (parametros.isEmpty() ? "" : "&" + parametros);
            conexao = (HttpURLConnection) new URL(endereco).openConnection();
            conexao.setRequestMethod(metodo);
            conexao.setConnectTimeout(12000);
            conexao.setReadTimeout(18000);
            conexao.setUseCaches(false);
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setRequestProperty("Accept", "application/json");
            if ("POST".equals(metodo)) {
                conexao.setDoOutput(true);
                conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] dados = (corpo == null ? "{}" : corpo.toString())
                        .getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conexao.getOutputStream()) { os.write(dados); }
            }
            int codigo = conexao.getResponseCode();
            InputStream fluxo = codigo >= 200 && codigo < 300
                    ? conexao.getInputStream() : conexao.getErrorStream();
            StringBuilder texto = new StringBuilder();
            if (fluxo != null) {
                try (BufferedReader leitor = new BufferedReader(
                        new InputStreamReader(fluxo, StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = leitor.readLine()) != null) texto.append(linha);
                }
            }
            JSONObject json = texto.length() == 0
                    ? new JSONObject() : new JSONObject(texto.toString());
            if (codigo >= 200 && codigo < 300 && json.optBoolean("ok", false)) {
                callback.sucesso(json);
            } else {
                String erro = json.optString("error", "Falha ao acessar a Central.");
                if (codigo == 401) erro = "Sessão expirada. Entre novamente.";
                else if (codigo == 403) erro = "Você não tem permissão para esta ação.";
                else if (codigo == 429) erro = "Muitas tentativas. Tente novamente em um minuto.";
                callback.erro(erro + " (HTTP " + codigo + ")");
            }
        } catch (Exception erro) {
            callback.erro("Não foi possível conectar à Central NEXO.");
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }
}
