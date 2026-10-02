package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends Activity {

    private FirebaseAuth auth;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    private EditText campo(String dica, boolean senha) {
        EditText campo = new EditText(this);

        campo.setHint(dica);
        campo.setHintTextColor(Color.rgb(120, 120, 130));
        campo.setTextColor(Color.WHITE);
        campo.setTextSize(15);
        campo.setSingleLine(true);
        campo.setPadding(dp(18), 0, dp(18), 0);
        campo.setBackground(fundo(Color.rgb(22, 22, 28), 14));

        if (senha) {
            campo.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        } else {
            campo.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(55));

        params.setMargins(0, dp(7), 0, dp(7));

        campo.setLayoutParams(params);

        return campo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(Gravity.CENTER_HORIZONTAL);
        tela.setPadding(dp(28), dp(25), dp(28), dp(25));
        tela.setBackgroundColor(Color.BLACK);

        TextView robo = new TextView(this);
        robo.setText("🤖");
        robo.setTextSize(58);
        robo.setGravity(Gravity.CENTER);

        tela.addView(robo, new LinearLayout.LayoutParams(
                -1, dp(85)
        ));

        TextView titulo = new TextView(this);
        titulo.setText("DaNikeAI");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(30);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(Gravity.CENTER);

        tela.addView(titulo, new LinearLayout.LayoutParams(
                -1, dp(50)
        ));

        TextView subtitulo = new TextView(this);
        subtitulo.setText("CRIAR NOVA CONTA");
        subtitulo.setTextColor(Color.rgb(70, 220, 255));
        subtitulo.setTextSize(13);
        subtitulo.setTypeface(Typeface.DEFAULT_BOLD);
        subtitulo.setGravity(Gravity.CENTER);

        tela.addView(subtitulo, new LinearLayout.LayoutParams(
                -1, dp(35)
        ));

        EditText email = campo("E-MAIL", false);
        EditText usuario = campo("USUÁRIO", false);
        EditText senha = campo("SENHA", true);
        EditText confirmarSenha = campo("CONFIRMAR SENHA", true);

        tela.addView(email);
        tela.addView(usuario);
        tela.addView(senha);
        tela.addView(confirmarSenha);

        Button cadastrar = new Button(this);
        cadastrar.setText("CADASTRAR");
        cadastrar.setTextColor(Color.BLACK);
        cadastrar.setTextSize(14);
        cadastrar.setTypeface(Typeface.DEFAULT_BOLD);
        cadastrar.setAllCaps(false);
        cadastrar.setBackground(fundo(
                Color.rgb(70, 220, 255), 16
        ));

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(-1, dp(55));

        botaoParams.setMargins(0, dp(15), 0, dp(10));

        tela.addView(cadastrar, botaoParams);

        TextView mensagem = new TextView(this);
        mensagem.setText("");
        mensagem.setTextColor(Color.rgb(255, 80, 80));
        mensagem.setTextSize(12);
        mensagem.setGravity(Gravity.CENTER);

        tela.addView(mensagem, new LinearLayout.LayoutParams(
                -1, dp(45)
        ));

        TextView voltar = new TextView(this);
        voltar.setText("Já tenho uma conta • VOLTAR AO LOGIN");
        voltar.setTextColor(Color.LTGRAY);
        voltar.setTextSize(12);
        voltar.setGravity(Gravity.CENTER);
        voltar.setPadding(0, dp(10), 0, dp(10));

        tela.addView(voltar, new LinearLayout.LayoutParams(
                -1, dp(45)
        ));

        cadastrar.setOnClickListener(v -> {

            String valorEmail =
                    email.getText().toString().trim();

            String valorUsuario =
                    usuario.getText().toString().trim();

            String valorSenha =
                    senha.getText().toString();

            String valorConfirmar =
                    confirmarSenha.getText().toString();

            if (valorEmail.isEmpty() ||
                    valorUsuario.isEmpty() ||
                    valorSenha.isEmpty() ||
                    valorConfirmar.isEmpty()) {

                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "Preencha todos os campos."
                );

                return;
            }

            if (!valorEmail.contains("@")) {
                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "Digite um e-mail válido."
                );

                return;
            }

            if (valorSenha.length() < 6) {
                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "A senha deve ter pelo menos 6 caracteres."
                );

                return;
            }

            if (!valorSenha.equals(valorConfirmar)) {
                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "As senhas não conferem."
                );

                return;
            }

            cadastrar.setEnabled(false);

            mensagem.setTextColor(
                    Color.rgb(120, 200, 255)
            );

            mensagem.setText(
                    "Criando sua conta..."
            );

            auth.createUserWithEmailAndPassword(
                    valorEmail,
                    valorSenha
            ).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    SharedPreferences dados =
                            getSharedPreferences(
                                    "DaNikeAI_Dados",
                                    MODE_PRIVATE
                            );

                    dados.edit()
                            .putString("email", valorEmail)
                            .putString("usuario", valorUsuario)
                            .remove("senha")
                            .apply();

                    mensagem.setTextColor(
                            Color.rgb(80, 255, 150)
                    );

                    mensagem.setText(
                            "USUÁRIO cadastrado com sucesso ✔️"
                    );

                    new android.os.Handler().postDelayed(() -> {

                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class
                        );

                        intent.putExtra(
                                "cadastro_sucesso",
                                true
                        );

                        startActivity(intent);
                        finish();

                    }, 1200);

                } else {

                    cadastrar.setEnabled(true);

                    mensagem.setTextColor(
                            Color.rgb(255, 80, 80)
                    );

                    mensagem.setText(
                            "Não foi possível criar a conta."
                    );
                }
            });
        });

        voltar.setOnClickListener(v -> {
            finish();
        });

        setContentView(tela);
    }
}
