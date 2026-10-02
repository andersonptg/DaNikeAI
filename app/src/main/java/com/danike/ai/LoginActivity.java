package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
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

public class LoginActivity extends Activity {

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
        campo.setBackground(
                fundo(Color.rgb(22, 22, 28), 14)
        );

        if (senha) {
            campo.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
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
        tela.setPadding(
                dp(28),
                dp(25),
                dp(28),
                dp(25)
        );
        tela.setBackground(new LoginBackgroundDrawable(android.graphics.BitmapFactory.decodeResource(getResources(), com.danike.ai.R.drawable.ferrari_login)));

        
        // ===== CABEÇALHO FIXO DO LOGIN =====
        LinearLayout topoLogin = new LinearLayout(this);
        topoLogin.setOrientation(LinearLayout.HORIZONTAL);
        topoLogin.setGravity(Gravity.CENTER_VERTICAL);
        topoLogin.setPadding(dp(10), 0, dp(8), 0);

        android.graphics.drawable.GradientDrawable fundoTopo =
                new android.graphics.drawable.GradientDrawable();
        fundoTopo.setColor(Color.argb(190, 8, 8, 28));
        fundoTopo.setCornerRadius(dp(28));
        fundoTopo.setStroke(dp(1), Color.rgb(170, 35, 255));
        topoLogin.setBackground(fundoTopo);

        topoLogin.setClickable(true);
        topoLogin.setFocusable(true);
        topoLogin.setOnClickListener(v -> {
            try {
                Intent app = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("instagram://user?username=anderson_lopes._ofc"));
                startActivity(app);
            } catch (Exception e) {
                Intent web = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/anderson_lopes._ofc?stkn=amN5ZXBlYnhwYjIy"));
                startActivity(web);
            }
        });

        LoginInstagramIcon instagramIcon = new LoginInstagramIcon(this);
        topoLogin.addView(instagramIcon,
                new LinearLayout.LayoutParams(dp(27), dp(27)));

        TextView instagramNome = new TextView(this);
        instagramNome.setText("anderson_lopes._ofc");
        instagramNome.setTextColor(Color.WHITE);
        instagramNome.setTextSize(11);
        instagramNome.setGravity(Gravity.CENTER_VERTICAL);
        instagramNome.setSingleLine(true);

        LinearLayout.LayoutParams nomeParams =
                new LinearLayout.LayoutParams(-2, -1);
        nomeParams.leftMargin = dp(7);
        topoLogin.addView(instagramNome, nomeParams);

        LoginVerifiedBadge selo = new LoginVerifiedBadge(this);
        topoLogin.addView(selo,
                new LinearLayout.LayoutParams(dp(22), dp(22)));

        LinearLayout.LayoutParams topoParams =
                new LinearLayout.LayoutParams(dp(315), dp(43));
        topoParams.gravity = Gravity.START;
        topoParams.bottomMargin = dp(10);

        tela.addView(topoLogin, 0, topoParams);

TextView robo = new TextView(this);
        robo.setText(""); robo.setBackgroundResource(com.danike.ai.R.drawable.danike_logo);
        robo.setTextSize(1);
        robo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(dp(105), dp(78));
        logoParams.gravity = Gravity.CENTER;
        tela.addView(robo, logoParams);

        TextView titulo = new TextView(this);
        titulo.setText("DaNikeAI");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(30);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(Gravity.CENTER);

        tela.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, dp(50))
        );

        TextView subtitulo = new TextView(this);
        subtitulo.setText("ENTRE NA SUA CONTA");
        subtitulo.setTextColor(Color.rgb(70, 220, 255));
        subtitulo.setTextSize(13);
        subtitulo.setTypeface(Typeface.DEFAULT_BOLD);
        subtitulo.setGravity(Gravity.CENTER);

        tela.addView(
                subtitulo,
                new LinearLayout.LayoutParams(-1, dp(35))
        );

        EditText usuario = campo("E-MAIL OU USUÁRIO", false);
        EditText senha = campo("SENHA", true);

        tela.addView(usuario);
        tela.addView(senha);

        TextView esqueci = new TextView(this);
        esqueci.setText("Esqueceu a senha?");
        esqueci.setTextColor(Color.rgb(255, 70, 70));
        esqueci.setTextSize(12);
        esqueci.setGravity(Gravity.CENTER);
        esqueci.setPadding(0, dp(4), 0, dp(4));

        tela.addView(
                esqueci,
                new LinearLayout.LayoutParams(-1, dp(35))
        );

        Button entrar = new Button(this);
        entrar.setText("ENTRAR");
        entrar.setTextColor(Color.BLACK);
        entrar.setTextSize(14);
        entrar.setTypeface(Typeface.DEFAULT_BOLD);
        entrar.setAllCaps(false);
        entrar.setBackground(
                fundo(Color.rgb(70, 220, 255), 16)
        );

        LinearLayout.LayoutParams entrarParams =
                new LinearLayout.LayoutParams(-1, dp(55));

        entrarParams.setMargins(
                0,
                dp(10),
                0,
                dp(14)
        );

        tela.addView(entrar, entrarParams);

        LinearLayout cadastroLinha = new LinearLayout(this);
        cadastroLinha.setOrientation(LinearLayout.HORIZONTAL);
        cadastroLinha.setGravity(Gravity.CENTER);

        TextView cadastroTexto = new TextView(this);
        cadastroTexto.setText("Ainda não tem uma conta? ");
        cadastroTexto.setTextColor(Color.LTGRAY);
        cadastroTexto.setTextSize(12);
        cadastroTexto.setGravity(Gravity.CENTER);

        TextView cadastroBotao = new TextView(this);
        cadastroBotao.setText("CADASTRE-SE");
        cadastroBotao.setTextColor(Color.rgb(70, 220, 255));
        cadastroBotao.setTextSize(12);
        cadastroBotao.setTypeface(Typeface.DEFAULT_BOLD);
        cadastroBotao.setGravity(Gravity.CENTER);

        cadastroLinha.addView(cadastroTexto);
        cadastroLinha.addView(cadastroBotao);

        tela.addView(
                cadastroLinha,
                new LinearLayout.LayoutParams(-1, dp(40))
        );

        TextView mensagem = new TextView(this);
        mensagem.setText("");
        mensagem.setTextColor(Color.rgb(255, 80, 80));
        mensagem.setTextSize(12);
        mensagem.setGravity(Gravity.CENTER);

        tela.addView(
                mensagem,
                new LinearLayout.LayoutParams(-1, dp(45))
        );

        TextView rodape = new TextView(this);
        rodape.setText(
                "DaNikeAI • Assistente pessoal de inteligência artificial"
        );
        rodape.setTextColor(Color.rgb(85, 85, 95));
        rodape.setTextSize(9);
        rodape.setGravity(Gravity.CENTER);

        tela.addView(
                rodape,
                new LinearLayout.LayoutParams(-1, dp(35))
        );

        entrar.setOnClickListener(v -> {

            String entrada =
                    usuario.getText().toString().trim();

            String pass =
                    senha.getText().toString();

            if (entrada.isEmpty() || pass.isEmpty()) {

                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "Digite e-mail/usuário e senha."
                );

                return;
            }

            String emailLogin = entrada;

            if (!entrada.contains("@")) {

                SharedPreferences dados =
                        getSharedPreferences(
                                "DaNikeAI_Dados",
                                MODE_PRIVATE
                        );

                String usuarioSalvo =
                        dados.getString("usuario", "");

                String emailSalvo =
                        dados.getString("email", "");

                if (entrada.equals(usuarioSalvo) &&
                        !emailSalvo.isEmpty()) {

                    emailLogin = emailSalvo;

                } else {

                    mensagem.setTextColor(
                            Color.rgb(255, 80, 80)
                    );

                    mensagem.setText(
                            "Usuário não encontrado neste dispositivo."
                    );

                    return;
                }
            }

            entrar.setEnabled(false);

            mensagem.setTextColor(
                    Color.rgb(120, 200, 255)
            );

            mensagem.setText(
                    "Autenticando..."
            );

            final String emailFinal = emailLogin;

            auth.signInWithEmailAndPassword(
                    emailFinal,
                    pass
            ).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    UsuariosTracker.registrarEntrada("");

                    mensagem.setTextColor(
                            Color.rgb(80, 255, 150)
                    );

                    mensagem.setText(
                            "Login realizado com sucesso ✔️"
                    );

                    new android.os.Handler().postDelayed(() -> {

                        ControleApp.verificar((manutencao, mensagemManutencao) -> {

                            if (manutencao) {

                                Intent intent = new Intent(
                                        LoginActivity.this,
                                        ManutencaoActivity.class
                                );

                                intent.putExtra(
                                        "MENSAGEM",
                                        mensagemManutencao
                                );

                                startActivity(intent);

                            new android.os.Handler(
                                    android.os.Looper.getMainLooper()
                            ).postDelayed(
                                    () -> AtualizacaoApp.verificar(LoginActivity.this),
                                    1200
                            );
                                finish();

                            } else {
                            String emailAtual = "";
                            if (FirebaseAuth.getInstance().getCurrentUser() != null
                                    && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                                emailAtual = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            }

                            boolean proprietario =
                                    "lipesanderson@gmail.com".equalsIgnoreCase(emailAtual);

                            android.content.SharedPreferences dados =
                                    getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE);

                            String nomeSalvo = dados.getString("nome", "").trim();
                            boolean onboardingConcluido =
                                    dados.getBoolean("onboarding_concluido", false);

                            Class<?> destino;

                            if (proprietario || onboardingConcluido || !nomeSalvo.isEmpty()) {
                                destino = MainActivity.class;
                            } else {
                                destino = OnboardingActivity.class;
                            }

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    destino
                            );

                            startActivity(intent);
                            finish();
                        }
                    });
                }, 700);

            } else {

                entrar.setEnabled(true);

                mensagem.setTextColor(
                        Color.rgb(255, 80, 80)
                );

                mensagem.setText(
                        "E-mail ou senha incorretos."
                );
            }
        });
    });

        esqueci.setOnClickListener(v -> {

            final EditText campoEmail =
                    campo("E-MAIL", false);

            SharedPreferences dados =
                    getSharedPreferences(
                            "DaNikeAI_Dados",
                            MODE_PRIVATE
                    );

            String emailSalvo =
                    dados.getString("email", "");

            if (!emailSalvo.isEmpty()) {
                campoEmail.setText(emailSalvo);
                campoEmail.setSelection(
                        campoEmail.getText().length()
                );
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("RECUPERAR SENHA")
                    .setMessage(
                            "Digite o e-mail da sua conta. " +
                            "Enviaremos um link para criar uma nova senha."
                    )
                    .setView(campoEmail)
                    .setNegativeButton(
                            "CANCELAR",
                            null
                    )
                    .setPositiveButton(
                            "ENVIAR",
                            null
                    )
                    .create();

            dialog.setOnShowListener(d -> {

                Button enviar =
                        dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                        );

                enviar.setOnClickListener(view -> {

                    String email =
                            campoEmail.getText()
                                    .toString()
                                    .trim();

                    if (email.isEmpty() ||
                            !email.contains("@")) {

                        mensagem.setTextColor(
                                Color.rgb(255, 80, 80)
                        );

                        mensagem.setText(
                                "Digite um e-mail válido."
                        );

                        return;
                    }

                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {

                                if (task.isSuccessful()) {

                                    mensagem.setTextColor(
                                            Color.rgb(80, 255, 150)
                                    );

                                    mensagem.setText(
                                            "E-mail de recuperação enviado ✔️"
                                    );

                                    dialog.dismiss();

                                } else {

                                    mensagem.setTextColor(
                                            Color.rgb(255, 80, 80)
                                    );

                                    mensagem.setText(
                                            "Não foi possível enviar o e-mail."
                                    );
                                }
                            });
                });
            });

            dialog.show();
        });

        cadastroBotao.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        if (getIntent().getBooleanExtra(
                "cadastro_sucesso",
                false
        )) {

            mensagem.setTextColor(
                    Color.rgb(80, 255, 150)
            );

            mensagem.setText(
                    "USUÁRIO cadastrado com sucesso ✔️"
            );
        }

        setContentView(tela);
        aplicarNeonLogin(tela);
    }

    

    private void aplicarNeonLogin(android.view.ViewGroup raiz) {
        aplicarNeonRecursivo(raiz);
    }

    private void aplicarNeonRecursivo(android.view.ViewGroup grupo) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            android.view.View v = grupo.getChildAt(i);

            if (v instanceof android.widget.EditText) {
                android.widget.EditText e = (android.widget.EditText)v;
                String hint = e.getHint() == null ? "" : e.getHint().toString().toUpperCase();

                android.graphics.drawable.GradientDrawable fundo =
                        new android.graphics.drawable.GradientDrawable();
                fundo.setColor(Color.argb(205, 4, 12, 42));
                fundo.setCornerRadius(dp(28));
                fundo.setStroke(dp(2), Color.rgb(0, 170, 255));
                e.setBackground(fundo);
                e.setTextColor(Color.WHITE);
                e.setHintTextColor(Color.rgb(190, 195, 215));
                e.setTextSize(16);
                e.setPadding(dp(22), 0, dp(22), 0);

                if (hint.contains("SENHA")) {
                    e.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    e.setTransformationMethod(
                            android.text.method.PasswordTransformationMethod.getInstance());

                    e.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, android.R.drawable.ic_menu_view, 0);
                    e.setCompoundDrawablePadding(dp(8));

                    e.setOnTouchListener((view, event) -> {
                        if (event.getAction() == android.view.MotionEvent.ACTION_UP &&
                                event.getX() >= e.getWidth() - dp(70)) {

                            boolean ocultando =
                                    e.getTransformationMethod() != null;

                            int pos = e.getSelectionStart();

                            if (ocultando) {
                                e.setTransformationMethod(
                                        android.text.method.HideReturnsTransformationMethod.getInstance());
                            } else {
                                e.setTransformationMethod(
                                        android.text.method.PasswordTransformationMethod.getInstance());
                            }

                            e.setSelection(Math.max(0, pos));
                            return true;
                        }
                        return false;
                    });
                }
            }

            if (v instanceof android.widget.Button) {
                android.widget.Button b = (android.widget.Button)v;

                android.graphics.drawable.GradientDrawable botao =
                        new android.graphics.drawable.GradientDrawable(
                                android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                                new int[]{
                                        Color.rgb(0, 105, 255),
                                        Color.rgb(0, 210, 255)
                                });

                botao.setCornerRadius(dp(30));
                botao.setStroke(dp(2), Color.rgb(40, 245, 255));
                b.setBackground(botao);
                b.setTextColor(Color.WHITE);
                b.setTextSize(17);
                b.setAllCaps(false);
                b.setElevation(dp(8));
            }

            if (v instanceof android.widget.TextView) {
                android.widget.TextView t = (android.widget.TextView)v;
                String texto = t.getText() == null ? "" : t.getText().toString();

                if (texto.contains("Esqueceu a senha")) {
                    t.setTextColor(Color.rgb(255, 75, 90));
                    t.setTextSize(14);
                }

                if (texto.contains("CADASTRE-SE")) {
                    t.setTextColor(Color.rgb(0, 230, 255));
                }
            }

            if (v instanceof android.view.ViewGroup) {
                aplicarNeonRecursivo((android.view.ViewGroup)v);
            }
        }
    }

    // Ícone Instagram desenhado localmente para manter o visual neon.
    private class LoginInstagramIcon extends android.view.View {
        private final android.graphics.Paint p = new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG);

        LoginInstagramIcon(android.content.Context c) {
            super(c);
        }

        @Override
        protected void onDraw(android.graphics.Canvas c) {
            super.onDraw(c);

            float w = getWidth();
            float h = getHeight();

            p.setStyle(android.graphics.Paint.Style.STROKE);
            p.setStrokeWidth(dp(2.2f));

            android.graphics.LinearGradient grad =
                    new android.graphics.LinearGradient(
                            0, 0, w, h,
                            new int[]{
                                    Color.rgb(255, 40, 140),
                                    Color.rgb(170, 50, 255),
                                    Color.rgb(40, 180, 255),
                                    Color.rgb(255, 210, 40)
                            },
                            null,
                            android.graphics.Shader.TileMode.CLAMP);

            p.setShader(grad);

            android.graphics.RectF r =
                    new android.graphics.RectF(
                            dp(3), dp(3), w - dp(3), h - dp(3));

            c.drawRoundRect(r, dp(7), dp(7), p);
            c.drawCircle(w / 2f, h / 2f, dp(5), p);

            p.setStyle(android.graphics.Paint.Style.FILL);
            c.drawCircle(w - dp(7), dp(7), dp(1.7f), p);

            p.setShader(null);
        }
    }

    // Selo azul visual personalizado.
    private class LoginVerifiedBadge extends android.view.View {
        private final android.graphics.Paint p = new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG);

        LoginVerifiedBadge(android.content.Context c) {
            super(c);
        }

        @Override
        protected void onDraw(android.graphics.Canvas c) {
            super.onDraw(c);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float raio = Math.min(getWidth(), getHeight()) / 2f - dp(2);

            p.setStyle(android.graphics.Paint.Style.FILL);
            p.setColor(Color.rgb(0, 170, 255));
            c.drawCircle(cx, cy, raio, p);

            p.setStyle(android.graphics.Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            p.setColor(Color.WHITE);

            android.graphics.Path check =
                    new android.graphics.Path();
            check.moveTo(cx - dp(5), cy);
            check.lineTo(cx - dp(1), cy + dp(4));
            check.lineTo(cx + dp(6), cy - dp(5));

            c.drawPath(check, p);
        }
    }

}
