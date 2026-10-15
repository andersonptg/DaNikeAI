package com.danike.ai;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GeminiAPI {

    private static final String ENDPOINT =
        "https://danikeai-brain.onrender.com/chat";

    public interface Callback {
        void sucesso(String resposta);
        void erro(String mensagem);
    }

    public static void perguntar(String pergunta, Callback callback) {
        perguntar(pergunta, "", callback);
    }

    public static void perguntar(
        String pergunta,
        String contexto,
        Callback callback
    ) {
        new Thread(() -> {
            HttpURLConnection conexao = null;

            try {
                StringBuilder prompt = new StringBuilder();

                if (contexto != null && !contexto.trim().isEmpty()) {
                    prompt.append("CONTEXTO DA CONVERSA:\n")
                        .append(contexto.trim())
                        .append("\n\n");
                }

                prompt.append("PERGUNTA ATUAL:\n")
                    .append(pergunta == null ? "" : pergunta);

                String json = "{\"mensagem\":\""
                    + escaparJson(prompt.toString()) + "\"}";

                conexao = (HttpURLConnection)
                    new URL(ENDPOINT).openConnection();

                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(20000);
                conexao.setReadTimeout(90000);
                conexao.setDoOutput(true);
                conexao.setUseCaches(false);
                conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );
                conexao.setRequestProperty(
                    "Accept",
                    "application/json"
                );

                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(json.getBytes(StandardCharsets.UTF_8));
                }

                int codigo = conexao.getResponseCode();

                InputStream fluxo = codigo >= 200 && codigo < 300
                    ? conexao.getInputStream()
                    : conexao.getErrorStream();

                String corpo = ler(fluxo);

                if (codigo < 200 || codigo >= 300) {
                    String erro = extrairCampo(corpo, "erro");

                    callback.erro(
                        "DaNikeAI Brain HTTP " + codigo + ": "
                        + (erro.isEmpty() ? corpo : erro)
                    );
                    return;
                }

                String resposta = extrairCampo(corpo, "resposta");

                if (resposta.isEmpty()) {
                    resposta = extrairCampo(corpo, "text");
                }

                if (resposta.isEmpty()) {
                    callback.erro(
                        "O DaNikeAI Brain respondeu, mas não retornou texto."
                    );
                    return;
                }

                callback.sucesso(resposta);

            } catch (Exception e) {
                callback.erro(
                    "Falha ao conectar ao DaNikeAI Brain: "
                    + e.getMessage()
                );
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private static String escaparJson(String texto) {
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);

            switch (c) {
                case '"':
                    resultado.append("\\\"");
                    break;
                case '\\':
                    resultado.append("\\\\");
                    break;
                case '\n':
                    resultado.append("\\n");
                    break;
                case '\r':
                    resultado.append("\\r");
                    break;
                case '\t':
                    resultado.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        resultado.append(String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
            }
        }

        return resultado.toString();
    }

    private static String ler(InputStream fluxo) throws Exception {
        if (fluxo == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        try (BufferedReader leitor = new BufferedReader(
            new InputStreamReader(fluxo, StandardCharsets.UTF_8)
        )) {
            String linha;

            while ((linha = leitor.readLine()) != null) {
                resultado.append(linha);
            }
        }

        return resultado.toString();
    }

    private static String extrairCampo(String json, String campo) {
        String marcador = "\"" + campo + "\"";
        int posicao = json.indexOf(marcador);

        if (posicao < 0) {
            return "";
        }

        posicao = json.indexOf(':', posicao + marcador.length());

        if (posicao < 0) {
            return "";
        }

        posicao++;

        while (posicao < json.length()
            && Character.isWhitespace(json.charAt(posicao))) {
            posicao++;
        }

        if (posicao >= json.length() || json.charAt(posicao) != '"') {
            return "";
        }

        posicao++;
        StringBuilder resultado = new StringBuilder();
        boolean escape = false;

        for (int i = posicao; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                switch (c) {
                    case 'n':
                        resultado.append('\n');
                        break;
                    case 'r':
                        resultado.append('\r');
                        break;
                    case 't':
                        resultado.append('\t');
                        break;
                    case 'b':
                        resultado.append('\b');
                        break;
                    case 'f':
                        resultado.append('\f');
                        break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            try {
                                resultado.append((char) Integer.parseInt(
                                    json.substring(i + 1, i + 5), 16
                                ));
                                i += 4;
                            } catch (NumberFormatException e) {
                                return "";
                            }
                        } else {
                            return "";
                        }
                        break;
                    default:
                        resultado.append(c);
                }

                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                return resultado.toString().trim();
            } else {
                resultado.append(c);
            }
        }

        return "";
    }
}
