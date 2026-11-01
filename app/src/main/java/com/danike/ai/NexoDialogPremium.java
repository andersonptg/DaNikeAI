package com.danike.ai;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Explicacoes no padrao NEXO Premium, sem exibir dados internos privados. */
public final class NexoDialogPremium {
    private NexoDialogPremium() { }
    public static void explicar(Activity a, String titulo, String mensagem) {
        Dialog dialogo = new Dialog(a);
        LinearLayout painel = new LinearLayout(a);
        painel.setOrientation(LinearLayout.VERTICAL);
        int m = NexoTemaGlobal.dp(a, 20);
        painel.setPadding(m, m, m, m);
        painel.setBackground(NexoTemaGlobal.superficie(a, 24));

        TextView emblema = new TextView(a);
        emblema.setText("✦  NEXO");
        emblema.setTextSize(14);
        emblema.setTextColor(NexoTemaGlobal.destaque(a));
        emblema.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        painel.addView(emblema);

        TextView cabecalho = new TextView(a);
        cabecalho.setText(titulo);
        cabecalho.setTextSize(21);
        cabecalho.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cabecalho.setTextColor(NexoTemaGlobal.texto(a));
        cabecalho.setPadding(0, NexoTemaGlobal.dp(a, 14), 0, NexoTemaGlobal.dp(a, 10));
        painel.addView(cabecalho);

        TextView descricao = new TextView(a);
        descricao.setText(mensagem);
        descricao.setTextSize(15);
        descricao.setLineSpacing(NexoTemaGlobal.dp(a, 3), 1f);
        descricao.setTextColor(NexoTemaGlobal.secundario(a));
        painel.addView(descricao);

        TextView ok = new TextView(a);
        ok.setText("✓  ENTENDI");
        ok.setTextSize(15);
        ok.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        ok.setTextColor(NexoTemaGlobal.texto(a));
        ok.setGravity(Gravity.CENTER);
        ok.setBackground(NexoTemaGlobal.superficie(a, 14));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, NexoTemaGlobal.dp(a, 52));
        bp.topMargin = NexoTemaGlobal.dp(a, 20);
        painel.addView(ok, bp);
        ok.setOnClickListener(v -> dialogo.dismiss());

        dialogo.setContentView(painel);
        Window w = dialogo.getWindow();
        if (w != null) w.setBackgroundDrawableResource(android.R.color.transparent);
        dialogo.show();
        w = dialogo.getWindow();
        if (w != null) {
            int largura = Math.min(a.getResources().getDisplayMetrics().widthPixels - NexoTemaGlobal.dp(a, 36), NexoTemaGlobal.dp(a, 440));
            w.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}
