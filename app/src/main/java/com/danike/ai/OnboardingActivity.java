package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends Activity {

    private EditText nomeInput;
    private Button continuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(Gravity.CENTER_HORIZONTAL);
        tela.setPadding(32, 40, 32, 32);
        tela.setBackgroundColor(Color.rgb(3, 7, 18));

        TextView robo = new TextView(this);
        robo.setText("🤖");
        robo.setTextSize(78);
        robo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams roboParams =
                new LinearLayout.LayoutParams(-1, 150);
        roboParams.bottomMargin = 15;
        tela.addView(robo, roboParams);

        TextView titulo = new TextView(this);
        titulo.setText("OLÁ 👋");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(30);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);

        tela.addView(titulo, new LinearLayout.LayoutParams(-1, 55));

        TextView mensagem = new TextView(this);
        mensagem.setText(
                "SOU UMA ASSISTENTE PROGRAMADA PARA TE ACOMPANHAR\n" +
                "DURANTE O USO DO APLICATIVO 🙂"
        );
        mensagem.setTextColor(Color.rgb(180, 235, 255));
        mensagem.setTextSize(16);
        mensagem.setGravity(Gravity.CENTER);
        mensagem.setPadding(10, 10, 10, 25);

        tela.addView(mensagem, new LinearLayout.LayoutParams(-1, -2));

        TextView pergunta = new TextView(this);
        pergunta.setText("Como posso te chamar?");
        pergunta.setTextColor(Color.WHITE);
        pergunta.setTextSize(20);
        pergunta.setGravity(Gravity.CENTER);

        tela.addView(pergunta, new LinearLayout.LayoutParams(-1, 60));

        nomeInput = new EditText(this);
        nomeInput.setHint("Digite seu nome");
        nomeInput.setHintTextColor(Color.rgb(130, 170, 190));
        nomeInput.setTextColor(Color.WHITE);
        nomeInput.setTextSize(17);
        nomeInput.setSingleLine(true);
        nomeInput.setPadding(28, 0, 28, 0);

        GradientDrawable campo = new GradientDrawable();
        campo.setColor(Color.argb(150, 5, 20, 42));
        campo.setCornerRadius(35);
        campo.setStroke(3, Color.rgb(0, 210, 255));
        nomeInput.setBackground(campo);

        LinearLayout.LayoutParams nomeParams =
                new LinearLayout.LayoutParams(-1, 62);
        nomeParams.topMargin = 12;
        nomeParams.bottomMargin = 25;

        tela.addView(nomeInput, nomeParams);

        continuar = new Button(this);
        continuar.setText("CONTINUAR");
        continuar.setTextColor(Color.WHITE);
        continuar.setTextSize(17);
        continuar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        continuar.setAllCaps(false);

        GradientDrawable botao = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.rgb(0, 110, 255),
                        Color.rgb(0, 220, 255)
                }
        );
        botao.setCornerRadius(35);
        botao.setStroke(2, Color.rgb(100, 245, 255));
        continuar.setBackground(botao);
        continuar.setElevation(12);

        tela.addView(continuar,
                new LinearLayout.LayoutParams(-1, 62));

        setContentView(tela);

        continuar.setOnClickListener(v -> salvarNome());
    }

    private void salvarNome() {
        String nome = nomeInput.getText().toString().trim();

        if (nome.isEmpty()) {
            nomeInput.setError("Digite seu nome para continuar");
            nomeInput.requestFocus();
            return;
        }

        continuar.setEnabled(false);
        continuar.setText("SALVANDO...");

        getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE)
                .edit()
                .putString("nome", nome)
                .putBoolean("onboarding_concluido", true)
                .apply();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> dados = new HashMap<>();
            dados.put("nome", nome);
            dados.put("onboardingConcluido", true);
            dados.put("ultimoAcesso", FieldValue.serverTimestamp());
            dados.put("online", true);

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .set(dados, com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener(task -> abrirInicio());
        } else {
            abrirInicio();
        }
    }

    private void abrirInicio() {
        UsuariosTracker.registrarEntrada(
                nomeInput.getText().toString().trim()
        );

        startActivity(new android.content.Intent(
                this, MainActivity.class
        ));
        finish();
    }
}
