package com.danike.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

public class HaveRegisterActivity extends Activity {

    private FirebaseAuth auth;
    private EditText nome;
    private EditText email;
    private EditText senha;
    private EditText confirmar;

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
        tela.setPadding(dp(24), dp(30), dp(24), dp(24));
        tela.setBackgroundColor(Color.BLACK);

        TextView logo = texto("HAVE", 40, Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setGravity(Gravity.CENTER);

        tela.addView(
                logo,
                new LinearLayout.LayoutParams(-1, dp(65))
        );

        TextView titulo = texto(
                "Criar sua conta",
                23,
                Color.WHITE
        );
        titulo.setGravity(Gravity.CENTER);

        tela.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        TextView explicacao = texto(
                "Seu nome será usado nas salas e no chat.",
                14,
                Color.rgb(160, 180, 195)
        );
        explicacao.setGravity(Gravity.CENTER);

        tela.addView(
                explicacao,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        nome = campo(
                "Nome para as salas",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );
        adicionar(tela, nome);

        email = campo(
                "E-mail",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        adicionar(tela, email);

        senha = campo(
                "Senha",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        adicionar(tela, senha);

        confirmar = campo(
                "Confirmar senha",
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        adicionar(tela, confirmar);

        Button criar = new Button(this);
        criar.setText("CRIAR CONTA");
        criar.setTextSize(16);
        criar.setTextColor(Color.BLACK);
        criar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        criar.setAllCaps(false);
        criar.setBackground(
                fundo(Color.rgb(0, 230, 255), 18)
        );

        LinearLayout.LayoutParams criarParams =
                new LinearLayout.LayoutParams(-1, dp(58));

        criarParams.setMargins(
                0,
                dp(18),
                0,
                dp(12)
        );

        tela.addView(criar, criarParams);

        TextView voltar = texto(
                "Já tenho uma conta • VOLTAR AO LOGIN",
                14,
                Color.rgb(0, 220, 255)
        );

        voltar.setGravity(Gravity.CENTER);
        voltar.setPadding(0, dp(12), 0, dp(12));

        tela.addView(
                voltar,
                new LinearLayout.LayoutParams(-1, dp(55))
        );

        setContentView(tela);

        criar.setOnClickListener(v -> criarConta());

        voltar.setOnClickListener(v -> finish());
    }

    private EditText campo(String hint, int tipo) {

        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(Color.rgb(120, 130, 145));
        e.setTextColor(Color.WHITE);
        e.setSingleLine(true);
        e.setInputType(tipo);
        e.setPadding(dp(18), 0, dp(18), 0);
        e.setBackground(
                fundo(Color.rgb(22, 22, 27), 18)
        );

        return e;
    }

    private void adicionar(LinearLayout tela, EditText campo) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, dp(58));

        params.setMargins(0, 0, 0, dp(10));

        tela.addView(campo, params);
    }

    private void criarConta() {

        String n = nome.getText().toString().trim();
        String e = email.getText().toString().trim();
        String s = senha.getText().toString();
        String c = confirmar.getText().toString();

        if (n.isEmpty()) {
            nome.setError("Digite seu nome");
            nome.requestFocus();
            return;
        }

        if (e.isEmpty()) {
            email.setError("Digite seu e-mail");
            email.requestFocus();
            return;
        }

        if (s.length() < 6) {
            senha.setError(
                    "A senha precisa ter pelo menos 6 caracteres"
            );
            senha.requestFocus();
            return;
        }

        if (!s.equals(c)) {
            confirmar.setError("As senhas não são iguais");
            confirmar.requestFocus();
            return;
        }

        Toast.makeText(
                this,
                "Criando sua conta...",
                Toast.LENGTH_SHORT
        ).show();

        auth.createUserWithEmailAndPassword(e, s)
                .addOnSuccessListener(resultado -> {

                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(
                                this,
                                "Conta criada, mas usuário não encontrado.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String uid =
                            auth.getCurrentUser().getUid();

                    String haveId = "HAVE-" +
                            UUID.randomUUID().toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase(Locale.US);

                    Map<String, Object> dados =
                            new HashMap<>();

                    dados.put("nome", n);
                    dados.put("email", e);
                                                                    dados.put("haveId", haveId);
                    dados.put("criadoEm",
                            com.google.firebase.firestore.FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance()
                            .collection("have_usuarios")
                            .document(uid)
                            .set(dados)
                            .addOnSuccessListener(v -> {

                                Toast.makeText(
                                        this,
                                        "Conta Have criada ✔️",
                                        Toast.LENGTH_LONG
                                ).show();

                                startActivity(
                                        new Intent(
                                                HaveRegisterActivity.this,
                                                HaveLoginActivity.class
                                        )
                                );

                                finish();
                            })
                            .addOnFailureListener(erro -> {

                                Toast.makeText(
                                        this,
                                        "Conta criada, mas houve erro ao salvar o perfil.",
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(erro -> {

                    Toast.makeText(
                            this,
                            "Não foi possível criar: "
                                    + erro.getLocalizedMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
