package com.danike.ai.filmes;

import com.danike.ai.BuildConfig;

import android.content.Context;

import java.util.ArrayList;

public class FilmesRepository {

    private final FilmesApi api;

    public FilmesRepository(Context context) {
        api = new FilmesApi(BuildConfig.YOUTUBE_API_KEY);
    }

    public ArrayList<Filme> buscar(String consulta) throws Exception {
        return api.buscarFilmes(consulta);
    }
}
