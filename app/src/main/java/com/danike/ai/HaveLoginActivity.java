package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;

public class HaveLoginActivity extends Activity {

    private FirebaseAuth auth;
    private EditText email;
    private EditText senha;

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }

    private TextView texto(String valor, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(valor);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(Gravity.CENTER_HORIZONTAL);
        tela.setPadding(dp(24), dp(40), dp(24), dp(24));
        tela.setBackgroundColor(Color.BLACK);

        TextView logo = texto("HAVE", 42, Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(-1, dp(70));
        tela.addView(logo, logoParams);

        TextView subtitulo = texto(
                "Assista juntos. Converse. Compartilhe.",
                15,
                Color.rgb(170, 190, 205)
        );
        subtitulo.setGravity(Gravity.CENTER);

        tela.addView(
                subtitulo,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        Space espaco = new Space(this);
        tela.addView(
                espaco,
                new LinearLayout.LayoutParams(1, dp(20))
        );

        email = new EditText(this);
        email.setHint("E-mail");
        email.setHintTextColor(Color.rgb(120, 130, 145));
        email.setTextColor(Color.WHITE);
        email.setSingleLine(true);
        email.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setPadding(dp(18), 0, dp(18), 0);
        email.setBackground(fundo(Color.rgb(22, 22, 27), 18));

        tela.addView(
                email,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        Space espaco2 = new Space(this);
        tela.addView(
                espaco2,
                new LinearLayout.LayoutParams(1, dp(12))
        );

        senha = new EditText(this);
        senha.setHint("Senha");
        senha.setHintTextColor(Color.rgb(120, 130, 145));
        senha.setTextColor(Color.WHITE);
        senha.setSingleLine(true);
        senha.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        senha.setPadding(dp(18), 0, dp(18), 0);
        senha.setBackground(fundo(Color.rgb(22, 22, 27), 18));

        tela.addView(
                senha,
                new LinearLayout.LayoutParams(-1, dp(58))
        );

        TextView esqueci = texto(
                "Esqueci minha senha",
                14,
                Color.rgb(0, 220, 255)
        );
        esqueci.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        esqueci.setPadding(0, dp(8), dp(4), dp(8));

        tela.addView(
                esqueci,
                new LinearLayout.LayoutParams(-1, dp(48))
        );

        Button entrar = new Button(this);
        entrar.setText("ENTRAR");
        entrar.setTextSize(16);
        entrar.setTextColor(Color.BLACK);
        entrar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        entrar.setAllCaps(false);
        entrar.setBackground(
                fundo(Color.rgb(0, 230, 255), 18)
        );

        LinearLayout.LayoutParams entrarParams =
                new LinearLayout.LayoutParams(-1, dp(58));
        entrarParams.setMargins(0, dp(8), 0, dp(14));
        tela.addView(entrar, entrarParams);

        TextView cadastro = texto(
                "Ainda não tem uma conta?  CRIAR CONTA",
                15,
                Color.WHITE
        );
        cadastro.setGravity(Gravity.CENTER);
        cadastro.setPadding(0, dp(12), 0, dp(12));

        tela.addView(
                cadastro,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        TextView voltar = texto(
                "← Voltar",
                14,
                Color.rgb(130, 145, 160)
        );
        voltar.setGravity(Gravity.CENTER);

        tela.addView(
                voltar,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        setContentView(tela);

        entrar.setOnClickListener(v -> realizarLogin());

        esqueci.setOnClickListener(v -> recuperarSenha());

        cadastro.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                HaveLoginActivity.this,
                                HaveRegisterActivity.class
                        )
                )
        );

        voltar.setOnClickListener(v -> finish());
    }

    private void realizarLogin() {

        String e = email.getText().toString().trim();
        String s = senha.getText().toString();

        if (e.isEmpty()) {
            email.setError("Digite seu e-mail");
            email.requestFocus();
            return;
        }

        if (s.isEmpty()) {
            senha.setError("Digite sua senha");
            senha.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Entrando no Have...",
                Toast.LENGTH_SHORT
        ).show();

        auth.signInWithEmailAndPassword(e, s)
                .addOnSuccessListener(resultado -> {

                    Toast.makeText(
                            this,
                            "Login realizado ✔️",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            HaveLoginActivity.this,
                            HaveHomeActivity.class
                    );
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(erro -> {

                    Toast.makeText(
                            this,
                            "Não foi possível entrar: "
                                    + erro.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void recuperarSenha() {

        String e = email.getText().toString().trim();

        if (e.isEmpty()) {
            email.setError(
                    "Digite seu e-mail para recuperar a senha"
            );
            email.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(e)
                .addOnSuccessListener(v ->
                        Toast.makeText(
                                this,
                                "E-mail de recuperação enviado ✔️",
                                Toast.LENGTH_LONG
                        ).show()
                )
                .addOnFailureListener(erro ->
                        Toast.makeText(
                                this,
                                "Não foi possível enviar: "
                                        + erro.getLocalizedMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}
