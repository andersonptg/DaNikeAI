package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import android.database.Cursor;

public class MemoriasActivity extends Activity {

    LinearLayout lista;
    boolean escuro;

    int fundo() {
        return escuro ? Color.rgb(2,10,20) : Color.rgb(245,248,250);
    }

    int texto() {
        return escuro ? Color.WHITE : Color.rgb(20,25,30);
    }

    int card() {
        return escuro ? Color.rgb(5,22,35) : Color.WHITE;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        escuro = getSharedPreferences(
                "danike_config", MODE_PRIVATE
        ).getBoolean("modo_escuro", true);

        montar();
    }

    void montar() {
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(fundo());

        TextView titulo = new TextView(this);
        titulo.setText("🧠  Memórias da DaNike");
        titulo.setTextColor(texto());
        titulo.setTextSize(22);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);
        titulo.setPadding(10,35,10,25);

        raiz.addView(titulo);

        ScrollView scroll = new ScrollView(this);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(12,5,12,20);

        carregar();

        scroll.addView(lista);
        raiz.addView(scroll,new LinearLayout.LayoutParams(
                -1,0,1
        ));

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setAllCaps(false);
        voltar.setOnClickListener(v -> finish());

        raiz.addView(voltar,new LinearLayout.LayoutParams(
                -1,60
        ));

        setContentView(raiz);
    }

    void carregar() {
        lista.removeAllViews();

        MemoriaDB banco = new MemoriaDB(this);
        Cursor c = banco.listarMemoriasConfirmadas();

        try {
            if (!c.moveToFirst()) {
                TextView vazio = new TextView(this);
                vazio.setText(
                        "Nenhuma memória confirmada ainda.\n\n" +
                        "Diga para a DaNike:\n" +
                        "\"lembre que...\""
                );
                vazio.setTextColor(texto());
                vazio.setTextSize(16);
                vazio.setPadding(15,30,15,30);
                lista.addView(vazio);
                return;
            }

            do {
                long id = c.getLong(
                        c.getColumnIndexOrThrow("id")
                );

                String memoria = c.getString(
                        c.getColumnIndexOrThrow("texto")
                );

                adicionarMemoria(id, memoria);

            } while (c.moveToNext());

        } finally {
            c.close();
            banco.close();
        }
    }

    void adicionarMemoria(long id, String memoria) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(18,14,18,14);
        item.setBackgroundColor(card());

        TextView t = new TextView(this);
        t.setText("🧠  " + memoria);
        t.setTextColor(texto());
        t.setTextSize(16);

        item.addView(t);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setGravity(Gravity.RIGHT);

        Button editar = new Button(this);
        editar.setText("EDITAR");
        editar.setAllCaps(false);

        editar.setOnClickListener(v ->
                editarMemoria(id, memoria)
        );

        Button apagar = new Button(this);
        apagar.setText("APAGAR");
        apagar.setAllCaps(false);

        apagar.setOnClickListener(v ->
                apagarMemoria(id)
        );

        botoes.addView(editar);
        botoes.addView(apagar);

        item.addView(botoes);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1,-2);

        lp.setMargins(0,0,0,12);

        lista.addView(item,lp);
    }

    void editarMemoria(long id,String atual) {

        EditText campo = new EditText(this);
        campo.setText(atual);
        campo.setTextColor(texto());
        campo.setSingleLine(false);

        new AlertDialog.Builder(this)
                .setTitle("✏️ Editar memória")
                .setView(campo)
                .setNegativeButton("CANCELAR",null)
                .setPositiveButton("SALVAR",(d,w) -> {

                    String novo = campo.getText()
                            .toString().trim();

                    if (novo.isEmpty()) return;

                    MemoriaDB banco =
                            new MemoriaDB(this);

                    banco.atualizarMemoria(id,novo);
                    banco.close();

                    carregar();
                })
                .show();
    }

    void apagarMemoria(long id) {

        new AlertDialog.Builder(this)
                .setTitle("🗑️ Apagar memória?")
                .setMessage(
                        "Essa memória será removida permanentemente."
                )
                .setNegativeButton("CANCELAR",null)
                .setPositiveButton("APAGAR",(d,w) -> {

                    MemoriaDB banco =
                            new MemoriaDB(this);

                    banco.apagarMemoria(id);
                    banco.close();

                    carregar();
                })
                .show();
    }
}
