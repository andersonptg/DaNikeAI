package com.danike.ai.filmes;

public class Filme {

    private String id;
    private String titulo;
    private String descricao;
    private String thumbnail;

    public Filme(String id, String titulo, String descricao, String thumbnail) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getYoutubeUrl() {
        return "https://www.youtube.com/watch?v=" + id;
    }
}
