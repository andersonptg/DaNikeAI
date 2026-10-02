package com.danike.ai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemoriaDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "danike_memoria.db";
    private static final int DB_VERSION = 2;

    public MemoriaDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE memorias (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "texto TEXT NOT NULL," +
            "categoria TEXT DEFAULT 'geral'," +
            "criado_em INTEGER NOT NULL)"
        );

        db.execSQL(
            "CREATE TABLE conversas (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "pergunta TEXT NOT NULL," +
            "resposta TEXT," +
            "criado_em INTEGER NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Mantém as memórias e conversas existentes.
        }
    }

    public long salvarMemoria(String texto, String categoria) {
        return inserirMemoria(texto, categoria);
    }

    public long salvarMemoriaConfirmada(String texto, String categoria) {
        return inserirMemoria(texto, categoria);
    }

    private long inserirMemoria(String texto, String categoria) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("texto", texto);
        valores.put("categoria", categoria);
        valores.put("criado_em", System.currentTimeMillis());

        return db.insert("memorias", null, valores);
    }

    public long salvarConversa(String pergunta, String resposta) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("pergunta", pergunta);
        valores.put("resposta", resposta);
        valores.put("criado_em", System.currentTimeMillis());

        return db.insert("conversas", null, valores);
    }

    public Cursor listarMemorias() {
        return getReadableDatabase().query(
            "memorias",
            new String[]{"id", "texto", "categoria", "criado_em"},
            null,
            null,
            null,
            null,
            "id DESC"
        );
    }

    public Cursor listarMemoriasConfirmadas() {
        return getReadableDatabase().query(
            "memorias",
            new String[]{"id", "texto", "categoria", "criado_em"},
            "categoria = ?",
            new String[]{"confirmada pelo usuário"},
            null,
            null,
            "id DESC"
        );
    }

    public Cursor listarConversas() {
        return getReadableDatabase().query(
            "conversas",
            new String[]{"id", "pergunta", "resposta", "criado_em"},
            null,
            null,
            null,
            null,
            "id DESC"
        );
    }

    public boolean atualizarMemoria(long id, String texto) {
        ContentValues valores = new ContentValues();
        valores.put("texto", texto);

        return getWritableDatabase().update(
            "memorias",
            valores,
            "id = ?",
            new String[]{String.valueOf(id)}
        ) > 0;
    }

    public boolean apagarMemoria(long id) {
        return getWritableDatabase().delete(
            "memorias",
            "id = ?",
            new String[]{String.valueOf(id)}
        ) > 0;
    }

    public void apagarMemorias() {
        getWritableDatabase().delete("memorias", null, null);
    }

    public void apagarConversas() {
        getWritableDatabase().delete("conversas", null, null);
    }
}
