package com.danike.ai;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public final class AtualizacaoTelefoneDialog {

    public interface Callback {
        void concluido();
    }

    private AtualizacaoTelefoneDialog() {}

    public static void mostrar(Context context, boolean permitirVazio, Callback callback) {
        callback.concluido();
        if (true) return;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(context, 24), dp(context, 20), dp(context, 24), dp(context, 20));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.rgb(10, 14, 24));
        fundo.setCornerRadius(dp(context, 22));
        fundo.setStroke(dp(context, 1), Color.rgb(40, 150, 210));
        layout.setBackground(fundo);

        ImageView logo = new ImageView(context);
        logo.setImageResource(com.danike.ai.R.drawable.danike_splash_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(dp(context, 58), dp(context, 58));
        logoParams.gravity = Gravity.CENTER;
        logoParams.bottomMargin = dp(context, 6);
        layout.addView(logo, logoParams);

        TextView titulo = new TextView(context);
        titulo.setText("Atualização do seu perfil");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(19);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        tituloParams.bottomMargin = dp(context, 10);
        layout.addView(titulo, tituloParams);

        TextView texto = new TextView(context);
        texto.setText(
                "Para melhorar sua experiência e manter seu perfil completo, "
                + "precisamos adicionar seu número de celular à sua conta."
        );
        texto.setTextColor(Color.rgb(190, 200, 215));
        texto.setTextSize(14);
        texto.setGravity(Gravity.CENTER);
        texto.setLineSpacing(0, 1.15f);

        LinearLayout.LayoutParams textoParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        textoParams.bottomMargin = dp(context, 18);
        layout.addView(texto, textoParams);

        EditText telefone = new EditText(context);
        telefone.setHint("📱  SEU CELULAR");
        telefone.setHintTextColor(Color.rgb(120, 135, 155));
        telefone.setTextColor(Color.WHITE);
        telefone.setTextSize(15);
        telefone.setSingleLine(true);
        telefone.setInputType(
                android.text.InputType.TYPE_CLASS_PHONE
        );
        telefone.setPadding(
                dp(context, 15),
                0,
                dp(context, 15),
                0
        );

        GradientDrawable campoFundo = new GradientDrawable();
        campoFundo.setColor(Color.rgb(18, 24, 36));
        campoFundo.setCornerRadius(dp(context, 12));
        campoFundo.setStroke(dp(context, 1), Color.rgb(45, 90, 120));
        telefone.setBackground(campoFundo);

        LinearLayout.LayoutParams telefoneParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(context, 52)
                );
        telefoneParams.bottomMargin = dp(context, 16);
        layout.addView(telefone, telefoneParams);

        Button continuar = new Button(context);
        continuar.setText("CONTINUAR");
        continuar.setTextColor(Color.WHITE);
        continuar.setTextSize(14);
        continuar.setTypeface(null, android.graphics.Typeface.BOLD);

        GradientDrawable botaoFundo = new GradientDrawable();
        botaoFundo.setColor(Color.rgb(15, 115, 170));
        botaoFundo.setCornerRadius(dp(context, 13));
        continuar.setBackground(botaoFundo);

        layout.addView(
                continuar,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(context, 50)
                )
        );

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(layout)
                .setCancelable(false)
                .create();

        continuar.setOnClickListener(v -> {

            String telefoneDigitado =
                    telefone.getText().toString().trim();

            String normalizado =
                    telefoneDigitado.replaceAll("\\D", "");

            if (normalizado.isEmpty()) {
                if (permitirVazio) {
                    dialog.dismiss();
                    callback.concluido();
                    return;
                }

                telefone.setError("Informe seu número de celular.");
                telefone.requestFocus();
                return;
            }

            if (normalizado.length() < 10 || normalizado.length() > 11) {
                telefone.setError("Digite um celular válido.");
                telefone.requestFocus();
                return;
            }

            continuar.setEnabled(false);
            continuar.setText("SALVANDO...");

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                continuar.setEnabled(true);
                continuar.setText("CONTINUAR");
                return;
            }

            String uid = auth.getCurrentUser().getUid();

            Map<String, Object> dados = new HashMap<>();
            dados.put("telefone", telefoneDigitado);
            dados.put("telefoneNormalizado", normalizado);
            dados.put(
                    "atualizadoEm",
                    com.google.firebase.firestore.FieldValue.serverTimestamp()
            );

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .set(dados, SetOptions.merge())
                    .addOnSuccessListener(x -> {
                        dialog.dismiss();
                        callback.concluido();
                    })
                    .addOnFailureListener(e -> {
                        continuar.setEnabled(true);
                        continuar.setText("CONTINUAR");
                        telefone.setError("Não foi possível salvar. Tente novamente.");
                    });
        });

        dialog.setOnShowListener(x -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );

                int largura = (int) (context.getResources()
                        .getDisplayMetrics().widthPixels * 0.90f);

                dialog.getWindow().setLayout(
                        largura,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            int largura = (int) (context.getResources()
                    .getDisplayMetrics().widthPixels * 0.90f);

            dialog.getWindow().setLayout(
                    largura,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private static int dp(Context context, float valor) {
        return (int) (
                valor * context.getResources()
                        .getDisplayMetrics().density + 0.5f
        );
    }
}
