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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends Activity {

    private FirebaseAuth auth;

    private int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    private GradientDrawable fundoNeon(
            int cor,
            int borda,
            float raio
    ) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        g.setStroke(dp(1.5f), borda);
        return g;
    }

    private EditText campo(
            String dica,
            boolean senha
    ) {
        EditText campo = new EditText(this);

        campo.setHint(dica);
        campo.setHintTextColor(
                Color.rgb(100, 150, 180)
        );
        campo.setTextColor(Color.WHITE);
        campo.setTextSize(16);
        campo.setSingleLine(true);

        campo.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        campo.setBackground(
                fundoNeon(
                        Color.rgb(8, 18, 30),
                        Color.rgb(35, 150, 255),
                        16
                )
        );

        campo.setElevation(dp(7));

        if (senha) {
            campo.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
        } else {
            campo.setInputType(
                    InputType.TYPE_CLASS_TEXT
            );
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );

        params.setMargins(
                0,
                dp(8),
                0,
                dp(8)
        );

        campo.setLayoutParams(params);

        return campo;
    }

    private TextView textoNeon(String texto) {
        TextView t = new TextView(this);

        t.setText(texto);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return t;
    }

    private void mostrarErro(
            TextView mensagem,
            String texto
    ) {
        mensagem.setTextColor(
                Color.rgb(255, 80, 100)
        );
        mensagem.setText(texto);
    }

    private void mostrarSucesso(
            TextView mensagem,
            String texto
    ) {
        mensagem.setTextColor(
                Color.rgb(70, 255, 160)
        );
        mensagem.setText(texto);
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout tela =
                new LinearLayout(this);

        tela.setOrientation(
                LinearLayout.VERTICAL
        );

        tela.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        tela.setPadding(
                dp(24),
                dp(18),
                dp(24),
                dp(18)
        );

        tela.setBackground(
                new LoginBackgroundDrawable(
                        android.graphics.BitmapFactory.decodeResource(
                                getResources(),
                                com.danike.ai.R.drawable.login_background
                        )
                )
        );

        TextView robo =
                new TextView(this);

        robo.setText("🤖");
        robo.setTextSize(58);
        robo.setGravity(Gravity.CENTER);

        robo.setShadowLayer(
                dp(18),
                0,
                0,
                Color.rgb(0, 170, 255)
        );

        tela.addView(
                robo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(78)
                )
        );

        TextView titulo =
                new TextView(this);

        titulo.setText("DaNikeAI");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(29);

        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titulo.setGravity(Gravity.CENTER);

        titulo.setShadowLayer(
                dp(12),
                0,
                0,
                Color.rgb(40, 180, 255)
        );

        tela.addView(
                titulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        TextView subtitulo =
                new TextView(this);

        subtitulo.setText(
                "CRIAR NOVA CONTA"
        );

        subtitulo.setTextColor(
                Color.rgb(70, 220, 255)
        );

        subtitulo.setTextSize(13);

        subtitulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        subtitulo.setGravity(Gravity.CENTER);

        tela.addView(
                subtitulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        TextView indicador =
                textoNeon("●  ETAPA 1 DE 4");

        indicador.setTextColor(
                Color.rgb(70, 220, 255)
        );

        tela.addView(
                indicador,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                )
        );

        TextView pergunta =
                textoNeon("Qual é o seu nome?");

        pergunta.setTextSize(15);
        pergunta.setTextColor(Color.WHITE);

        tela.addView(
                pergunta,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        EditText campoAtual =
                campo("NOME", false);

        tela.addView(campoAtual);

        Button continuar =
                new Button(this);

        continuar.setText(
                "CONTINUAR  →"
        );

        continuar.setTextColor(Color.BLACK);
        continuar.setTextSize(14);

        continuar.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        continuar.setAllCaps(false);

        continuar.setBackground(
                fundoNeon(
                        Color.rgb(70, 220, 255),
                        Color.rgb(150, 240, 255),
                        18
                )
        );

        continuar.setElevation(dp(14));

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56)
                );

        botaoParams.setMargins(
                0,
                dp(15),
                0,
                dp(10)
        );

        tela.addView(
                continuar,
                botaoParams
        );

        TextView mensagem =
                new TextView(this);

        mensagem.setText("");
        mensagem.setTextSize(12);
        mensagem.setGravity(Gravity.CENTER);

        tela.addView(
                mensagem,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        TextView voltar =
                new TextView(this);

        voltar.setText(
                "Já tenho uma conta  •  VOLTAR AO LOGIN"
        );

        voltar.setTextColor(
                Color.rgb(110, 190, 230)
        );

        voltar.setTextSize(12);

        voltar.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        voltar.setGravity(Gravity.CENTER);

        tela.addView(
                voltar,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        setContentView(tela);

        final EditText[] campos =
                new EditText[4];

        campos[0] = campoAtual;
        campos[1] = campo(
                "E-MAIL",
                false
        );
        campos[2] = campo(
                "SENHA",
                true
        );
        campos[3] = campo(
                "CONFIRMAR SENHA",
                true
        );

        final int[] etapa = {0};

        Runnable atualizarEtapa = () -> {

            indicador.setText(
                    "●  ETAPA "
                            + (etapa[0] + 1)
                            + " DE 4"
            );

            if (etapa[0] == 0) {
                pergunta.setText(
                        "Qual é o seu nome?"
                );
            } else if (etapa[0] == 1) {
                pergunta.setText(
                        "Qual é o seu e-mail?"
                );
            } else if (etapa[0] == 2) {
                pergunta.setText(
                        "Crie uma senha segura."
                );
            } else {
                pergunta.setText(
                        "Confirme sua senha."
                );
            }

            for (int i = 0; i < campos.length; i++) {
                if (i != etapa[0]
                        && campos[i].getParent() != null) {
                    tela.removeView(campos[i]);
                }
            }

            if (campos[etapa[0]].getParent() == null) {
                tela.addView(
                        campos[etapa[0]],
                        tela.indexOfChild(continuar)
                );
            }

            mensagem.setText("");

            campos[etapa[0]].requestFocus();
        };

        Runnable avancar = () -> {

            String valor =
                    campos[etapa[0]]
                            .getText()
                            .toString()
                            .trim();

            if (valor.isEmpty()) {

                mostrarErro(
                        mensagem,
                        "Preencha este campo para continuar."
                );

                campos[etapa[0]].requestFocus();

                return;
            }

            if (etapa[0] == 1
                    && !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(valor)
                    .matches()) {

                mostrarErro(
                        mensagem,
                        "Digite um e-mail válido."
                );

                campos[etapa[0]].requestFocus();

                return;
            }

            if (etapa[0] == 2
                    && valor.length() < 6) {

                mostrarErro(
                        mensagem,
                        "A senha deve ter pelo menos 6 caracteres."
                );

                campos[etapa[0]].requestFocus();

                return;
            }

            if (etapa[0] == 3) {

                criarConta(
                        campos,
                        continuar,
                        mensagem
                );

                return;
            }

            etapa[0]++;
            atualizarEtapa.run();
        };

        continuar.setOnClickListener(
                v -> avancar.run()
        );

        for (EditText campo : campos) {

            campo.setOnEditorActionListener(
                    (v, actionId, event) -> {

                        boolean enter =
                                actionId
                                        == EditorInfo.IME_ACTION_DONE
                                || actionId
                                        == EditorInfo.IME_ACTION_NEXT
                                || (
                                        event != null
                                                && event.getKeyCode()
                                                == KeyEvent.KEYCODE_ENTER
                                );

                        if (enter) {
                            avancar.run();
                            return true;
                        }

                        return false;
                    }
            );
        }

        voltar.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    )
            );

            finish();
        });

        atualizarEtapa.run();
    }

    private void criarConta(
            EditText[] campos,
            Button cadastrar,
            TextView mensagem
    ) {

        String nome =
                campos[0]
                        .getText()
                        .toString()
                        .trim();

        String email =
                campos[1]
                        .getText()
                        .toString()
                        .trim();

        String senha =
                campos[2]
                        .getText()
                        .toString();

        String confirmacao =
                campos[3]
                        .getText()
                        .toString();

        if (!senha.equals(confirmacao)) {

            mostrarErro(
                    mensagem,
                    "As senhas não são iguais."
            );

            return;
        }

        cadastrar.setEnabled(false);
        cadastrar.setText(
                "CRIANDO CONTA..."
        );

        mensagem.setTextColor(
                Color.rgb(80, 200, 255)
        );

        mensagem.setText(
                "Conectando ao Firebase..."
        );

        auth.createUserWithEmailAndPassword(
                email,
                senha
        ).addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {

                cadastrar.setEnabled(true);

                cadastrar.setText(
                        "CRIAR MINHA CONTA  ✓"
                );

                String erro =
                        task.getException() != null
                                ? task.getException()
                                .getMessage()
                                : "";

                if (erro != null
                        && erro.contains(
                        "already in use"
                )) {

                    erro =
                            "Este e-mail já está cadastrado.";

                } else if (
                        erro != null
                                && erro.contains(
                                "badly formatted"
                )) {

                    erro =
                            "E-mail inválido.";

                } else if (
                        erro != null
                                && erro.contains(
                                "weak-password"
                )) {

                    erro =
                            "A senha é muito fraca.";

                } else {

                    erro =
                            "Não foi possível criar a conta.";
                }

                mostrarErro(
                        mensagem,
                        erro
                );

                return;
            }

            mensagem.setTextColor(
                    Color.rgb(80, 200, 255)
            );

            mensagem.setText(
                    "Salvando seu perfil..."
            );

            PerfilFirebase.garantirPerfil(
                    RegisterActivity.this,
                    nome,
                    new PerfilFirebase.Callback() {

                        @Override
                        public void sucesso() {

                            SharedPreferences dados =
                                    getSharedPreferences(
                                            "DaNikeAI_Dados",
                                            MODE_PRIVATE
                                    );

                            dados.edit()
                                    .putString(
                                            "email",
                                            email
                                    )
                                    .putString(
                                            "usuario",
                                            nome
                                    )
                                    .putString(
                                            "nome",
                                            nome
                                    )
                                    .putBoolean(
                                            "perfil_firebase_criado",
                                            true
                                    )
                                    .remove("senha")
                                    .apply();

                            mostrarSucesso(
                                    mensagem,
                                    "●  LOGIN SALVO COM SUCESSO"
                            );

                            cadastrar.setText(
                                    "CONTA CRIADA  ✓"
                            );

                            new android.os.Handler()
                                    .postDelayed(
                                            () -> {

                                                auth.signOut();

                                                Intent intent =
                                                        new Intent(
                                                                RegisterActivity.this,
                                                                LoginActivity.class
                                                        );

                                                intent.putExtra(
                                                        "cadastro_sucesso",
                                                        true
                                                );

                                                startActivity(intent);
                                                finish();

                                            },
                                            1600
                                    );
                        }

                        @Override
                        public void erro(
                                String erro
                        ) {

                            cadastrar.setEnabled(true);

                            cadastrar.setText(
                                    "CRIAR MINHA CONTA  ✓"
                            );

                            mostrarErro(
                                    mensagem,
                                    "Conta criada, mas houve um erro ao salvar o perfil."
                            );
                        }
                    }
            );
        });
    }
}
