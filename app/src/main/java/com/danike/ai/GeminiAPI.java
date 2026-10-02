package com.danike.ai;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GeminiAPI {

    public interface Callback {
        void sucesso(String resposta);
        void erro(String mensagem);
    }

    public static void perguntar(String pergunta, Callback callback) {

        new Thread(() -> {
            try {

                String perguntaEscapada =
                    pergunta.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r");

                String json =
                    "{\"prompt\":\""
                    + perguntaEscapada
                    + "\"}";

                URL endereco =
                    new URL("http://127.0.0.1:8766/ask");

                HttpURLConnection conexao =
                    (HttpURLConnection) endereco.openConnection();

                conexao.setRequestMethod("POST");
                conexao.setConnectTimeout(3000);
                conexao.setReadTimeout(60000);
                conexao.setDoOutput(true);
                conexao.setUseCaches(false);

                conexao.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                );

                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(
                        json.getBytes(StandardCharsets.UTF_8)
                    );
                }

                int codigo = conexao.getResponseCode();

                InputStream fluxo;

                if (codigo >= 200 && codigo < 300) {
                    fluxo = conexao.getInputStream();
                } else {
                    fluxo = conexao.getErrorStream();
                }

                String respostaJson = ler(fluxo);

                if (codigo < 200 || codigo >= 300) {
                    callback.erro(
                        "Erro na ponte Gemini HTTP "
                        + codigo
                        + ": "
                        + respostaJson
                    );
                    return;
                }

                String resposta = extrairTexto(respostaJson);

                if (resposta.isEmpty()) {
                    callback.erro(
                        "A ponte Gemini não retornou texto."
                    );
                    return;
                }

                callback.sucesso(resposta);

            } catch (Exception e) {
                callback.erro(
                    "Erro ao conectar à ponte Gemini: "
                    + e.getMessage()
                );
            }
        }).start();
    }

    private static String carregarChave() {

        String[] arquivos = {
            System.getProperty("user.home")
                + "/.config/danikeai.env",
            "/data/data/com.termux/files/home/.config/danikeai.env"
        };

        for (String caminho : arquivos) {

            try {

                BufferedReader br =
                    new BufferedReader(
                        new FileReader(caminho)
                    );

                String linha;

                while ((linha = br.readLine()) != null) {

                    if (linha.startsWith("GEMINI_API_KEY=")) {

                        br.close();

                        return linha.substring(
                            "GEMINI_API_KEY=".length()
                        ).trim();
                    }
                }

                br.close();

            } catch (Exception ignored) {
            }
        }

        return "";
    }

    private static String ler(InputStream fluxo)
        throws Exception {

        if (fluxo == null) {
            return "";
        }

        BufferedReader br =
            new BufferedReader(
                new InputStreamReader(
                    fluxo,
                    StandardCharsets.UTF_8
                )
            );

        StringBuilder resultado =
            new StringBuilder();

        String linha;

        while ((linha = br.readLine()) != null) {
            resultado.append(linha);
        }

        br.close();

        return resultado.toString();
    }

    private static String extrairTexto(String json) {

        String marcador = "\"text\":";

        int inicio = json.indexOf(marcador);

        if (inicio == -1) {
            return "";
        }

        inicio += marcador.length();

        while (
            inicio < json.length()
            && Character.isWhitespace(json.charAt(inicio))
        ) {
            inicio++;
        }

        if (
            inicio >= json.length()
            || json.charAt(inicio) != '"'
        ) {
            return "";
        }

        inicio++;

        StringBuilder texto =
            new StringBuilder();

        boolean escape = false;

        for (
            int i = inicio;
            i < json.length();
            i++
        ) {

            char c = json.charAt(i);

            if (escape) {

                if (c == 'n') {
                    texto.append('\n');
                } else if (c == 'r') {
                    texto.append('\r');
                } else if (c == 't') {
                    texto.append('\t');
                } else {
                    texto.append(c);
                }

                escape = false;

            } else if (c == '\\') {

                escape = true;

            } else if (c == '"') {

                break;

            } else {

                texto.append(c);
            }
        }

        return texto.toString().trim();
    }
}
