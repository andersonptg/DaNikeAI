package com.danike.ai;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Fonte UNICA das preferencias de aparencia do NEXO.
 * IMPORTANTE: telas com cores fixas ainda exigem migracao individual para a paleta.
 */
public final class NexoTemaGlobal {
    public static final int ESCURO = 0;
    public static final int CLARO = 1;
    public static final int AUTOMATICO = 2;
    private static final String PREF = "NEXO_APARENCIA";
    private static final String CHAVE = "nexo_tema_modo";
    private NexoTemaGlobal() { }

    public static int modo(Context contexto) {
        SharedPreferences p = contexto.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if (p.contains(CHAVE)) return p.getInt(CHAVE, ESCURO);
        SharedPreferences antigo = contexto.getSharedPreferences("DaNikeAI_Dados", Context.MODE_PRIVATE);
        SharedPreferences config = contexto.getSharedPreferences("danike_config", Context.MODE_PRIVATE);
        boolean escuro = antigo.contains("modo_escuro")
                ? antigo.getBoolean("modo_escuro", true)
                : config.getBoolean("modo_escuro", true);
        int modo = escuro ? ESCURO : CLARO;
        p.edit().putInt(CHAVE, modo).apply();
        return modo;
    }

    public static boolean escuro(Context contexto) {
        int modo = modo(contexto);
        if (modo == AUTOMATICO) {
            return (contexto.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        }
        return modo != CLARO;
    }

    public static void definir(Context contexto, int novoModo) {
        if (novoModo < ESCURO || novoModo > AUTOMATICO) return;
        contexto.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putInt(CHAVE, novoModo).apply();
        // Compatibilidade com as telas legadas durante a migracao gradual.
        boolean escuro = novoModo == AUTOMATICO
                ? ((contexto.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                : novoModo == ESCURO;
        contexto.getSharedPreferences("DaNikeAI_Dados", Context.MODE_PRIVATE)
                .edit().putBoolean("modo_escuro", escuro).apply();
        contexto.getSharedPreferences("danike_config", Context.MODE_PRIVATE)
                .edit().putBoolean("modo_escuro", escuro).apply();
    }

    public static int fundo(Context c) {
        return escuro(c) ? Color.rgb(4, 10, 25) : Color.rgb(244, 248, 255);
    }
    public static int cartao(Context c) {
        return escuro(c) ? Color.rgb(10, 27, 52) : Color.WHITE;
    }
    public static int texto(Context c) {
        return escuro(c) ? Color.rgb(242, 247, 255) : Color.rgb(21, 33, 55);
    }
    public static int secundario(Context c) {
        return escuro(c) ? Color.rgb(170, 192, 218) : Color.rgb(76, 94, 118);
    }
    public static int destaque(Context c) {
        return escuro(c) ? Color.rgb(70, 190, 255) : Color.rgb(3, 92, 178);
    }
    public static int borda(Context c) {
        return escuro(c) ? Color.rgb(36, 112, 186) : Color.rgb(145, 191, 229);
    }
    public static GradientDrawable superficie(Context c, int raioDp) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                escuro(c) ? new int[]{Color.rgb(12, 35, 65), Color.rgb(6, 14, 33)}
                          : new int[]{Color.WHITE, Color.rgb(232, 242, 255)});
        d.setCornerRadius(dp(c, raioDp));
        d.setStroke(dp(c, 1), borda(c));
        return d;
    }
    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static void aplicarJanela(Activity a) {
        Window w = a.getWindow();
        boolean escuro = escuro(a);
        w.setStatusBarColor(fundo(a));
        w.setNavigationBarColor(fundo(a));
        if (Build.VERSION.SDK_INT >= 23) {
            View decor = w.getDecorView();
            int flags = decor.getSystemUiVisibility();
            if (escuro) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) {
                if (escuro) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                else flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decor.setSystemUiVisibility(flags);
        }
    }

    public static void selecionar(Activity a, Runnable atualizado) {
        final Dialog dialogo = new Dialog(a);
        LinearLayout corpo = new LinearLayout(a);
        corpo.setOrientation(LinearLayout.VERTICAL);
        corpo.setPadding(dp(a, 20), dp(a, 18), dp(a, 20), dp(a, 20));
        corpo.setBackground(superficie(a, 24));
        TextView titulo = linha(a, "✦  APARÊNCIA DO NEXO", 19, true);
        corpo.addView(titulo);
        TextView desc = linha(a, "Escolha o tema para o aplicativo. O modo automático segue a aparência do aparelho.", 13, false);
        desc.setTextColor(secundario(a));
        desc.setPadding(0, dp(a, 6), 0, dp(a, 12));
        corpo.addView(desc);
        opcao(a, corpo, dialogo, "🌙  Escuro", ESCURO, atualizado);
        opcao(a, corpo, dialogo, "☀️  Claro", CLARO, atualizado);
        opcao(a, corpo, dialogo, "◐  Automático (sistema)", AUTOMATICO, atualizado);
        dialogo.setContentView(corpo);
        Window w = dialogo.getWindow();
        if (w != null) w.setBackgroundDrawableResource(android.R.color.transparent);
        dialogo.show();
        w = dialogo.getWindow();
        if (w != null) {
            int largura = Math.min(a.getResources().getDisplayMetrics().widthPixels - dp(a, 36), dp(a, 440));
            w.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private static TextView linha(Context c, String texto, int tamanho, boolean negrito) {
        TextView t = new TextView(c);
        t.setText(texto);
        t.setTextColor(texto(c));
        t.setTextSize(tamanho);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (negrito) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private static void opcao(Activity a, LinearLayout corpo, Dialog dialogo,
                              String nome, int valor, Runnable atualizado) {
        TextView item = linha(a, (modo(a) == valor ? "✓  " : "    ") + nome, 16, modo(a) == valor);
        item.setPadding(dp(a, 14), dp(a, 13), dp(a, 12), dp(a, 13));
        item.setBackground(superficie(a, 14));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(a, 7);
        corpo.addView(item, p);
        item.setOnClickListener(v -> {
            definir(a, valor);
            dialogo.dismiss();
            atualizado.run();
        });
    }
}
