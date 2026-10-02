package com.danike.ai.filmes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class FilmesApi {

    private final String apiKey;

    public FilmesApi(String apiKey) {
        this.apiKey = apiKey;
    }

    public ArrayList<Filme> buscarFilmes(String consulta) throws Exception {

        ArrayList<Filme> filmes = new ArrayList<>();

        String q = URLEncoder.encode(consulta, "UTF-8");

        String urlString =
                "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet"
                + "&type=video"
                + "&maxResults=20"
                + "&q=" + q
                + "&key=" + apiKey;

        URL url = new URL(urlString);
        HttpURLConnection conexao =
                (HttpURLConnection) url.openConnection();

        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(15000);
        conexao.setReadTimeout(15000);

        BufferedReader leitor = new BufferedReader(
                new InputStreamReader(conexao.getInputStream())
        );

        StringBuilder resposta = new StringBuilder();
        String linha;

        while ((linha = leitor.readLine()) != null) {
            resposta.append(linha);
        }

        leitor.close();
        conexao.disconnect();

        JSONObject json = new JSONObject(resposta.toString());
        JSONArray itens = json.getJSONArray("items");

        for (int i = 0; i < itens.length(); i++) {

            JSONObject item = itens.getJSONObject(i);
            JSONObject id = item.getJSONObject("id");
            JSONObject snippet = item.getJSONObject("snippet");

            String videoId = id.getString("videoId");
            String titulo = snippet.getString("title");
            String descricao = snippet.getString("description");

            JSONObject thumbnails = snippet.getJSONObject("thumbnails");
            String thumbnail;

            if (thumbnails.has("high")) {
                thumbnail = thumbnails.getJSONObject("high").getString("url");
            } else {
                thumbnail = thumbnails.getJSONObject("default").getString("url");
            }

            filmes.add(
                    new Filme(
                            videoId,
                            titulo,
                            descricao,
                            thumbnail
                    )
            );
        }

        return filmes;
    }
}
