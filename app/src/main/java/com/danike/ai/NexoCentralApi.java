package com.danike.ai;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NexoCentralApi {

    private static final String TAG = "NexoCentralApi";

    private static final String BASE_URL =
            "https://little-nexo-core-hub.base44.app/functions/nexoApi";

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private NexoCentralApi() {
    }

    public interface Callback {
        void sucesso(int codigo, String resposta);
        void erro(Exception erro);
    }

    public static void verificarAutenticacao(
            Context context,
            Callback callback
    ) {
        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            if (callback != null) {
                callback.erro(
                        new IllegalStateException(
                                "Usuário não autenticado."
                        )
                );
            }
            return;
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {

                    String token = resultado.getToken();

                    if (token == null || token.trim().isEmpty()) {
                        if (callback != null) {
                            callback.erro(
                                    new IllegalStateException(
                                            "Token Firebase vazio."
                                    )
                            );
                        }
                        return;
                    }

                    executar(
                            "GET",
                            "/api/v1/auth/verify",
                            token,
                            null,
                            callback
                    );
                })
                .addOnFailureListener(erro -> {
                    if (callback != null) {
                        callback.erro(erro);
                    }
                });
    }

    private static void executar(
            String metodo,
            String rota,
            String token,
            JSONObject corpo,
            Callback callback
    ) {
        EXECUTOR.execute(() -> {

            HttpURLConnection conexao = null;

            try {
                String endereco =
                        BASE_URL
                                + "?route="
                                + java.net.URLEncoder.encode(
                                        rota,
                                        "UTF-8"
                                );

                URL url = new URL(endereco);

                conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setRequestMethod(metodo);
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);
                conexao.setUseCaches(false);

                conexao.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                if (corpo != null) {
                    conexao.setDoOutput(true);

                    conexao.setRequestProperty(
                            "Content-Type",
                            "application/json; charset=UTF-8"
                    );

                    try (OutputStream saida =
                                 conexao.getOutputStream()) {

                        saida.write(
                                corpo.toString()
                                        .getBytes(
                                                StandardCharsets.UTF_8
                                        )
                        );
                    }
                }

                int codigo =
                        conexao.getResponseCode();

                InputStream entrada;

                if (codigo >= 200 && codigo < 400) {
                    entrada = conexao.getInputStream();
                } else {
                    entrada = conexao.getErrorStream();
                }

                String resposta =
                        lerResposta(entrada);

                Log.d(
                        TAG,
                        "Central HTTP "
                                + codigo
                                + " rota="
                                + rota
                );

                if (callback != null) {
                    callback.sucesso(
                            codigo,
                            resposta
                    );
                }

            } catch (Exception erro) {

                Log.w(
                        TAG,
                        "Falha ao acessar Central: "
                                + erro.getClass()
                                        .getSimpleName()
                );

                if (callback != null) {
                    callback.erro(erro);
                }

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        });
    }

    public static void listarChamadosSuporte(Callback callback) {
        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            if (callback != null) {
                callback.erro(
                        new IllegalStateException(
                                "Usuário não autenticado."
                        )
                );
            }
            return;
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {
                    String token = resultado.getToken();

                    if (token == null || token.trim().isEmpty()) {
                        if (callback != null) {
                            callback.erro(
                                    new IllegalStateException(
                                            "Token Firebase vazio."
                                    )
                            );
                        }
                        return;
                    }

                    executar(
                            "GET",
                            "/api/v1/support",
                            token,
                            null,
                            callback
                    );
                })
                .addOnFailureListener(erro -> {
                    if (callback != null) {
                        callback.erro(erro);
                    }
                });
    }

    public static void criarChamadoSuporte(
            String assunto,
            String descricao,
            Callback callback
    ) {
        FirebaseUser usuario =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            if (callback != null) {
                callback.erro(
                        new IllegalStateException(
                                "Usuário não autenticado."
                        )
                );
            }
            return;
        }

        usuario.getIdToken(true)
                .addOnSuccessListener(resultado -> {
                    String token = resultado.getToken();

                    if (token == null || token.trim().isEmpty()) {
                        if (callback != null) {
                            callback.erro(
                                    new IllegalStateException(
                                            "Token Firebase vazio."
                                    )
                            );
                        }
                        return;
                    }

                    try {
                        String nome = usuario.getDisplayName();

                        if (nome == null || nome.trim().isEmpty()) {
                            nome = usuario.getEmail();
                        }

                        JSONObject corpo = new JSONObject();

                        corpo.put(
                                "uid",
                                usuario.getUid()
                        );

                        corpo.put(
                                "senderName",
                                nome == null ? "Usuário NEXO" : nome
                        );

                        corpo.put(
                                "body",
                                "Assunto: "
                                        + assunto.trim()
                                        + "\n\n"
                                        + descricao.trim()
                        );

                        executar(
                                "POST",
                                "/api/v1/support/messages",
                                token,
                                corpo,
                                callback
                        );

                    } catch (Exception erro) {
                        if (callback != null) {
                            callback.erro(erro);
                        }
                    }
                })
                .addOnFailureListener(erro -> {
                    if (callback != null) {
                        callback.erro(erro);
                    }
                });
    }

    private static String lerResposta(
            InputStream entrada
    ) throws Exception {

        if (entrada == null) {
            return "";
        }

        StringBuilder resultado =
                new StringBuilder();

        try (
                BufferedReader leitor =
                        new BufferedReader(
                                new InputStreamReader(
                                        entrada,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            String linha;

            while ((linha = leitor.readLine()) != null) {
                resultado.append(linha);
            }
        }

        return resultado.toString();
    }
}
