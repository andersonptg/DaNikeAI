package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import android.database.Cursor;

public class ConversasActivity extends Activity {

    LinearLayout lista;
    boolean escuro;

    int fundo() {
        return escuro ? Color.rgb(2,10,20) : Color.rgb(245,248,250);
    }

    int texto() {
        return escuro ? Color.WHITE : Color.rgb(20,25,30);
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
        titulo.setText("💬  Conversas anteriores");
        titulo.setTextColor(texto());
        titulo.setTextSize(22);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null,android.graphics.Typeface.BOLD);
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

        Button apagar = new Button(this);
        apagar.setText("🗑️ APAGAR TODO O HISTÓRICO");
        apagar.setAllCaps(false);

        apagar.setOnClickListener(v ->
                confirmarApagar()
        );

        raiz.addView(apagar);

        Button voltar = new Button(this);
        voltar.setText("VOLTAR");
        voltar.setAllCaps(false);
        voltar.setOnClickListener(v -> finish());

        raiz.addView(voltar);

        setContentView(raiz);
    }

    void carregar() {

        lista.removeAllViews();

        MemoriaDB banco = new MemoriaDB(this);
        Cursor c = banco.listarConversas();

        try {

            if (!c.moveToFirst()) {

                TextView vazio = new TextView(this);
                vazio.setText("Nenhuma conversa registrada ainda.");
                vazio.setTextColor(texto());
                vazio.setTextSize(16);
                vazio.setPadding(15,30,15,30);

                lista.addView(vazio);
                return;
            }

            do {

                String pergunta = c.getString(
                        c.getColumnIndexOrThrow("pergunta")
                );

                String resposta = c.getString(
                        c.getColumnIndexOrThrow("resposta")
                );

                TextView item = new TextView(this);

                item.setText(
                        "👤 Você:\n" + pergunta +
                        "\n\n🤖 DaNikeAI:\n" + resposta
                );

                item.setTextColor(texto());
                item.setTextSize(15);
                item.setPadding(18,18,18,18);

                lista.addView(item);

            } while(c.moveToNext());

        } finally {
            c.close();
            banco.close();
        }
    }

    void confirmarApagar() {

        new AlertDialog.Builder(this)
                .setTitle("🗑️ Apagar histórico?")
                .setMessage(
                        "Todas as conversas armazenadas serão apagadas."
                )
                .setNegativeButton("CANCELAR",null)
                .setPositiveButton("APAGAR",(d,w) -> {

                    MemoriaDB banco =
                            new MemoriaDB(this);

                    banco.apagarConversas();
                    banco.close();

                    carregar();
                })
                .show();
    }
}
