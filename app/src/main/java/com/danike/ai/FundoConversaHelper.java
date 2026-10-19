package com.danike.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;

import java.io.InputStream;

public class FundoConversaHelper {

    private static final String PREFS = "preferencias_conversa";
    private static final String CHAVE = "fundo_conversa";

    public static final String PADRAO = "danike";

    public static final String[] CHAVES = {
            "danike",
            "azul",
            "roxo",
            "neon",
            "oceano",
            "esmeralda",
            "vermelho",
            "rosa",
            "dourado",
            "galaxia",
            "noturno",
            "cinza"
    };

    public static final String[] NOMES = {
            "DaNikeAI",
            "Azul Elétrico",
            "Roxo Neon",
            "Neon",
            "Oceano",
            "Esmeralda",
            "Vermelho",
            "Rosa",
            "Dourado",
            "Galáxia",
            "Noturno",
            "Cinza"
    };

    public static void salvar(Context context, String valor) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(CHAVE, valor)
                .apply();
    }

    public static String obter(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(CHAVE, PADRAO);
    }

    public static void aplicar(Context context, View view) {
        Drawable drawable = criar(context, obter(context));

        if (drawable != null) {
            view.setBackground(drawable);
        }
    }

    public static Drawable criar(Context context, String valor) {

        if (valor == null || valor.trim().isEmpty()) {
            valor = PADRAO;
        }

        if (valor.startsWith("uri:")) {
            try {
                Uri uri = Uri.parse(valor.substring(4));

                InputStream input =
                        context.getContentResolver()
                                .openInputStream(uri);

                if (input != null) {
                    Drawable drawable =
                            Drawable.createFromStream(input, "fundo");

                    input.close();

                    if (drawable != null) {
                        return drawable;
                    }
                }
            } catch (Exception ignored) {
            }

            valor = PADRAO;
        }

        if ("danike".equals(valor)) {
            try {
                return context.getResources()
                        .getDrawable(R.drawable.chat_fundo);
            } catch (Exception ignored) {
            }
        }

        int[] cores;

        switch (valor) {

            case "azul":
                cores = new int[]{
                        Color.rgb(0, 45, 100),
                        Color.rgb(0, 145, 255),
                        Color.rgb(2, 6, 16)
                };
                break;

            case "roxo":
                cores = new int[]{
                        Color.rgb(55, 5, 100),
                        Color.rgb(145, 20, 210),
                        Color.rgb(8, 3, 25)
                };
                break;

            case "neon":
                cores = new int[]{
                        Color.rgb(0, 90, 80),
                        Color.rgb(0, 230, 180),
                        Color.rgb(5, 5, 20)
                };
                break;

            case "oceano":
                cores = new int[]{
                        Color.rgb(0, 30, 70),
                        Color.rgb(0, 110, 150),
                        Color.rgb(0, 10, 30)
                };
                break;

            case "esmeralda":
                cores = new int[]{
                        Color.rgb(0, 65, 45),
                        Color.rgb(0, 170, 100),
                        Color.rgb(2, 15, 12)
                };
                break;

            case "vermelho":
                cores = new int[]{
                        Color.rgb(70, 0, 15),
                        Color.rgb(190, 10, 55),
                        Color.rgb(12, 2, 8)
                };
                break;

            case "rosa":
                cores = new int[]{
                        Color.rgb(75, 0, 65),
                        Color.rgb(220, 20, 150),
                        Color.rgb(12, 2, 12)
                };
                break;

            case "dourado":
                cores = new int[]{
                        Color.rgb(80, 50, 0),
                        Color.rgb(220, 150, 20),
                        Color.rgb(15, 9, 2)
                };
                break;

            case "galaxia":
                cores = new int[]{
                        Color.rgb(35, 0, 80),
                        Color.rgb(10, 50, 120),
                        Color.rgb(2, 2, 15)
                };
                break;

            case "cinza":
                cores = new int[]{
                        Color.rgb(65, 70, 80),
                        Color.rgb(25, 28, 35),
                        Color.rgb(5, 6, 9)
                };
                break;

            case "noturno":
            default:
                cores = new int[]{
                        Color.rgb(20, 25, 38),
                        Color.rgb(8, 11, 18),
                        Color.rgb(2, 4, 8)
                };
                break;
        }

        GradientDrawable fundo =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        cores
                );

        return fundo;
    }
}
