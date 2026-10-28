package com.danike.ai;

import com.danike.ai.NexoAppObserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.ui.PlayerView;
import android.widget.MediaController;


public class HaveHomeActivity extends Activity {

    /**
     * Fundo decorativo de Halloween da HAVE.
     * Elementos de baixa opacidade para preservar a leitura do catálogo.
     */
    private class HalloweenHaveBackground extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        HalloweenHaveBackground() {
            super(HaveHomeActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setClickable(false);
            setFocusable(false);
        }

        private void cor(Canvas c, int cor, float alpha) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(cor);
            paint.setAlpha((int)(alpha * 255));
        }

        private void linha(Canvas c, float x1, float y1,
                           float x2, float y2, int cor, float alpha,
                           float largura) {
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(largura));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(cor);
            paint.setAlpha((int)(alpha * 255));
            c.drawLine(x1, y1, x2, y2, paint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;

            // Névoa fria e suave nas laterais.
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new RadialGradient(
                    w * 0.12f, h * 0.23f, w * 0.55f,
                    new int[] {
                            Color.argb(19, 80, 100, 150),
                            Color.argb(8, 60, 70, 120),
                            Color.TRANSPARENT
                    },
                    null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            paint.setShader(new RadialGradient(
                    w * 0.88f, h * 0.57f, w * 0.43f,
                    new int[] {
                            Color.argb(13, 125, 65, 35),
                            Color.argb(5, 100, 45, 25),
                            Color.TRANSPARENT
                    },
                    null, Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);

            // Abóbora fantasmagórica, discreta, no fundo superior direito.
            float cx = w * 0.83f;
            float cy = h * 0.17f;
            float r = Math.min(w * 0.105f, dp(43));

            cor(canvas, Color.rgb(100, 48, 25), 0.12f);
            canvas.drawOval(cx - r, cy - r * 0.77f,
                    cx + r, cy + r * 0.78f, paint);

            cor(canvas, Color.rgb(160, 75, 28), 0.10f);
            canvas.drawOval(cx - r * 0.72f, cy - r * 0.86f,
                    cx - r * 0.18f, cy + r * 0.75f, paint);
            canvas.drawOval(cx - r * 0.12f, cy - r * 0.86f,
                    cx + r * 0.48f, cy + r * 0.75f, paint);

            // Cabo da abóbora.
            linha(canvas, cx, cy - r * 0.72f,
                    cx + r * 0.12f, cy - r * 1.02f,
                    Color.rgb(70, 100, 58), 0.16f, 3);

            // Olhos inclinados e expressão irritada.
            Path olhoE = new Path();
            olhoE.moveTo(cx - r * 0.72f, cy - r * 0.20f);
            olhoE.lineTo(cx - r * 0.18f, cy - r * 0.05f);
            olhoE.lineTo(cx - r * 0.35f, cy + r * 0.20f);
            olhoE.close();

            Path olhoD = new Path();
            olhoD.moveTo(cx + r * 0.20f, cy - r * 0.05f);
            olhoD.lineTo(cx + r * 0.72f, cy - r * 0.22f);
            olhoD.lineTo(cx + r * 0.40f, cy + r * 0.20f);
            olhoD.close();

            cor(canvas, Color.rgb(255, 145, 45), 0.20f);
            canvas.drawPath(olhoE, paint);
            canvas.drawPath(olhoD, paint);

            // Brilho muito fraco dos olhos.
            paint.setShader(new RadialGradient(
                    cx, cy + r * 0.02f, r * 0.85f,
                    new int[] {
                            Color.argb(16, 255, 115, 25),
                            Color.TRANSPARENT
                    }, null, Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(cx, cy, r * 0.85f, paint);
            paint.setShader(null);

            // Boca angular quase invisível.
            Path boca = new Path();
            boca.moveTo(cx - r * 0.43f, cy + r * 0.39f);
            boca.lineTo(cx - r * 0.20f, cy + r * 0.52f);
            boca.lineTo(cx, cy + r * 0.42f);
            boca.lineTo(cx + r * 0.20f, cy + r * 0.52f);
            boca.lineTo(cx + r * 0.43f, cy + r * 0.34f);
            linha(canvas, cx - r * 0.43f, cy + r * 0.39f,
                    cx + r * 0.43f, cy + r * 0.34f,
                    Color.rgb(190, 100, 48), 0.12f, 1.3f);
            paint.setShader(null);

            // Teias finas nos cantos superiores.
            float tw = w * 0.22f;
            float th = h * 0.10f;
            for (int i = 1; i <= 5; i++) {
                float x = tw * i / 5f;
                linha(canvas, 0, 0, x, th, Color.rgb(165, 180, 205),
                        0.12f, 0.65f);
                linha(canvas, 0, 0, th * i / 5f, th,
                        Color.rgb(165, 180, 205), 0.09f, 0.65f);
            }
            for (int arco = 1; arco <= 4; arco++) {
                Path teia = new Path();
                teia.moveTo(tw * arco / 5f, th);
                teia.quadTo(tw * arco / 5f, th * arco / 5f,
                        th * arco / 5f, th);
                paint.setShader(null);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(0.6f));
                paint.setColor(Color.rgb(165, 180, 205));
                paint.setAlpha(20);
                canvas.drawPath(teia, paint);
            }

            // Algumas aranhas pequenas, suspensas nas laterais.
            desenharAranha(canvas, w * 0.12f, h * 0.31f, dp(5));
            desenharAranha(canvas, w * 0.91f, h * 0.38f, dp(4));
            desenharAranha(canvas, w * 0.08f, h * 0.70f, dp(3.5f));

            // Fios verticais quase imperceptíveis.
            linha(canvas, w * 0.12f, 0, w * 0.12f, h * 0.31f,
                    Color.rgb(160, 175, 195), 0.10f, 0.6f);
            linha(canvas, w * 0.91f, 0, w * 0.91f, h * 0.38f,
                    Color.rgb(160, 175, 195), 0.09f, 0.6f);

            // Névoa baixa: faixas suaves, sem animação pesada.
            for (int i = 0; i < 3; i++) {
                float fy = h * (0.78f + i * 0.055f);
                paint.setShader(new RadialGradient(
                        w * (0.25f + i * 0.25f), fy, w * 0.48f,
                        new int[] {
                                Color.argb(9, 150, 160, 185),
                                Color.TRANSPARENT
                        }, null, Shader.TileMode.CLAMP
                ));
                canvas.drawRect(0, fy - dp(25), w, fy + dp(25), paint);
            }
            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
        }

        private void desenharAranha(Canvas c, float x, float y, float r) {
            linha(c, x, 0, x, y - r, Color.rgb(175, 185, 205),
                    0.14f, 0.6f);

            cor(c, Color.rgb(155, 165, 185), 0.22f);
            c.drawCircle(x, y, r, paint);
            c.drawCircle(x, y - r * 0.85f, r * 0.55f, paint);

            for (int lado = -1; lado <= 1; lado += 2) {
                for (int perna = 0; perna < 4; perna++) {
                    float yy = y - r * 0.55f + perna * r * 0.38f;
                    float x2 = x + lado * r * (1.8f + (perna % 2) * 0.5f);
                    float y2 = yy + (perna - 1.5f) * r * 0.7f;
                    linha(c, x, yy, x2, y2,
                            Color.rgb(155, 165, 185), 0.20f, 0.65f);
                }
            }
        }
    }


    private boolean modoConviteHave = false;
    private String salaConviteId = "";
    private String salaConviteNome = "";
    private String salaConviteServico = "";
    private boolean telaAmigosAberta = false;

    private LinearLayout conteudoPerfilHave;


    private LinearLayout heroHave;
    private ImageView imagemHeroHave;
    private TextView tituloHeroHave;
    private TextView provedorHeroHave;
    private TextView descricaoHeroHave;
    private LinearLayout indicadoresHeroHave;
    private JSONArray catalogoHeroHave;
    private JSONArray catalogoNexoHave;
    private LinearLayout areaNexoFilmes;
    private EditText campoLinkUniversalHave;
    private int indiceHeroHave = 0;
    private final Handler handlerHeroHave =
            new Handler(Looper.getMainLooper());



    int roxo = Color.rgb(145, 40, 255);
    int azul = Color.rgb(35, 100, 255);
    int ciano = Color.rgb(0, 220, 255);

    int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    TextView texto(String s, float tamanho, int cor) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);
        return t;
    }

    GradientDrawable fundo(int cor, float raio) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(cor);
        g.setCornerRadius(dp(raio));
        return g;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent entrada = getIntent();
        modoConviteHave = entrada.getBooleanExtra("modoConviteHave", false);

        if (modoConviteHave) {
            salaConviteId = entrada.getStringExtra("salaId");
            salaConviteNome = entrada.getStringExtra("salaNome");
            salaConviteServico = entrada.getStringExtra("salaServico");

            if (salaConviteId == null) salaConviteId = "";
            if (salaConviteNome == null) salaConviteNome = "Sala HAVE";
            if (salaConviteServico == null) salaConviteServico = "Netflix";
        }

        verificarNomeObrigatorio();
    }

    private void verificarNomeObrigatorio() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            montarHome();
            return;
        }

        String uid = usuario.getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String nome = doc.exists() ? doc.getString("nome") : null;
                    String foto = doc.exists() ? doc.getString("fotoUrl") : null;

                    if (nome != null && !nome.trim().isEmpty()
                            && !nome.trim().equalsIgnoreCase("Usuário")) {
                        String nomeValido = nome.trim();
                        getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE)
                                .edit().putString("nome", nomeValido).apply();
                        sincronizarNomeHAVE(uid, nomeValido, foto, this::montarHome);
                    } else {
                        mostrarDialogNomeObrigatorio(uid);
                    }
                })
                .addOnFailureListener(e -> new AlertDialog.Builder(this)
                        .setTitle("NÃO FOI POSSÍVEL VERIFICAR")
                        .setMessage("Não conseguimos consultar seu perfil. Verifique a conexão e tente novamente.")
                        .setCancelable(false)
                        .setPositiveButton("TENTAR NOVAMENTE",
                                (d, w) -> verificarNomeObrigatorio())
                        .show());
    }

    private void mostrarDialogNomeObrigatorio(String uid) {
        LinearLayout painel = new LinearLayout(this);
        painel.setOrientation(LinearLayout.VERTICAL);
        painel.setPadding(dp(24), dp(18), dp(24), dp(12));
        painel.setBackgroundColor(Color.rgb(5, 8, 20));

        TextView titulo = texto("ANTES DE CONTINUAR", 20, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setGravity(Gravity.CENTER);
        painel.addView(titulo);

        TextView mensagem = texto(
                "Para usar o HAVE, precisamos do seu nome público.",
                15, Color.rgb(220, 230, 245));
        mensagem.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, -2);
        mp.setMargins(0, dp(16), 0, dp(8));
        painel.addView(mensagem, mp);

        TextView dica = texto(
                "Esse nome aparecerá nos seus perfis do DaNikeAI.",
                12, Color.rgb(135, 165, 195));
        dica.setGravity(Gravity.CENTER);
        painel.addView(dica);

        EditText campo = new EditText(this);
        campo.setSingleLine(true);
        campo.setTextSize(16);
        campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(Color.rgb(120, 140, 165));
        campo.setHint("Digite seu nome");
        campo.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable borda = fundo(Color.rgb(10, 17, 35), 12);
        borda.setStroke(dp(1), Color.rgb(0, 220, 255));
        campo.setBackground(borda);

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, dp(20), 0, dp(4));
        painel.addView(campo, cp);
        painel.addView(texto("Campo obrigatório", 11, Color.rgb(0, 220, 255)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(painel)
                .setPositiveButton("SALVAR E CONTINUAR", null)
                .create();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(ignorado -> {
            Button salvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            salvar.setTextColor(Color.rgb(0, 180, 230));
            salvar.setOnClickListener(v -> {
                String nome = campo.getText().toString().trim();

                if (nome.length() < 2) {
                    campo.setError("Digite seu nome para continuar");
                    campo.requestFocus();
                    return;
                }

                salvar.setEnabled(false);
                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome);

                FirebaseFirestore.getInstance()
                        .collection("usuarios").document(uid)
                        .set(dados, SetOptions.merge())
                        .addOnSuccessListener(ok -> {
                            getSharedPreferences("DaNikeAI_Dados", MODE_PRIVATE)
                                    .edit().putString("nome", nome).apply();

                            FirebaseUser atual =
                                    FirebaseAuth.getInstance().getCurrentUser();
                            String foto = atual != null && atual.getPhotoUrl() != null
                                    ? atual.getPhotoUrl().toString() : null;

                            dialog.dismiss();
                            sincronizarNomeHAVE(uid, nome, foto, this::montarHome);
                        })
                        .addOnFailureListener(e -> {
                            salvar.setEnabled(true);
                            Toast.makeText(this,
                                    "Não foi possível salvar. Tente novamente.",
                                    Toast.LENGTH_LONG).show();
                        });
            });
        });
        dialog.show();
    }

    private void sincronizarNomeHAVE(
            String uid, String nome, String fotoUrl, Runnable concluir) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", nome);
        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            dados.put("fotoUrl", fotoUrl.trim());
        }
        dados.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("have_usuarios").document(uid)
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(ok ->
                        db.collection("have_perfis_publicos").document(uid)
                                .set(dados, SetOptions.merge())
                                .addOnSuccessListener(ok2 -> concluir.run())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this,
                                            "Perfil público não sincronizado; confira a conexão.",
                                            Toast.LENGTH_LONG).show();
                                    concluir.run();
                                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Perfil HAVE não sincronizado; confira a conexão.",
                            Toast.LENGTH_LONG).show();
                    concluir.run();
                });
    }


    void montarHome() {
        telaAmigosAberta = false;

        FrameLayout raizHalloween = new FrameLayout(this);
        raizHalloween.setBackgroundColor(Color.rgb(3, 4, 15));

        HalloweenHaveBackground decoracaoHalloween =
                new HalloweenHaveBackground();
        raizHalloween.addView(
                decoracaoHalloween,
                new FrameLayout.LayoutParams(-1, -1)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.setClipToPadding(false);
        scroll.setFillViewport(true);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(12), dp(12), dp(12), dp(30));

        scroll.addView(
                conteudo,
                new ScrollView.LayoutParams(-1, -2)
        );

        raizHalloween.addView(
                scroll,
                new FrameLayout.LayoutParams(-1, -1)
        );
        setContentView(raizHalloween);

        criarHero(conteudo);

        // =========================================================
        // PESQUISA DE FILMES — ABAIXO DO CARROSSEL HAVE
        // =========================================================
        EditText campoPesquisaFilmes = new EditText(this);
        campoPesquisaFilmes.setSingleLine(true);
        campoPesquisaFilmes.setHint("🔎  Pesquisar filmes...");
        campoPesquisaFilmes.setHintTextColor(Color.rgb(145, 160, 190));
        campoPesquisaFilmes.setTextColor(Color.WHITE);
        campoPesquisaFilmes.setTextSize(15);
        campoPesquisaFilmes.setSingleLine(true);
        campoPesquisaFilmes.setGravity(Gravity.CENTER_VERTICAL);
        campoPesquisaFilmes.setPadding(
                dp(18),
                0,
                dp(18),
                0
        );

        GradientDrawable fundoPesquisa = new GradientDrawable();
        fundoPesquisa.setColor(Color.rgb(7, 11, 26));
        fundoPesquisa.setCornerRadius(dp(26));
        fundoPesquisa.setStroke(
                dp(2),
                Color.rgb(95, 55, 235)
        );
        campoPesquisaFilmes.setBackground(fundoPesquisa);
        campoPesquisaFilmes.setElevation(dp(5));

        LinearLayout.LayoutParams pesquisaParams =
                new LinearLayout.LayoutParams(-1, dp(56));

        pesquisaParams.setMargins(
                dp(4),
                dp(10),
                dp(4),
                dp(16)
        );

        conteudo.addView(
                campoPesquisaFilmes,
                pesquisaParams
        );

        campoPesquisaFilmes.addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        filtrarCatalogoNexoHave(
                                s == null ? "" : s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );

        areaNexoFilmes = new LinearLayout(this);
        areaNexoFilmes.setOrientation(LinearLayout.VERTICAL);
        areaNexoFilmes.setPadding(0, dp(5), 0, dp(20));

        conteudo.addView(
                areaNexoFilmes,
                new LinearLayout.LayoutParams(-1, -2)
        );

        carregarCatalogoNexoHave();
    }

    void mostrarCentralApps() {
      startActivity(new Intent(this, HaveConnectActivity.class));
  }

  void adicionarServicoCentral(
          LinearLayout pai, String nome, String descricao,
          int cor, String pacote, String site
  ) {
      LinearLayout linha = new LinearLayout(this);
      linha.setGravity(Gravity.CENTER_VERTICAL);
      linha.setPadding(dp(10), dp(9), dp(10), dp(9));

      GradientDrawable bg = fundo(Color.rgb(13, 15, 34), 16);
      bg.setStroke(dp(1), Color.rgb(48, 42, 82));
      linha.setBackground(bg);

      TextView simbolo = texto("▶", 19, Color.WHITE);
      simbolo.setGravity(Gravity.CENTER);
      simbolo.setBackground(fundo(cor, 13));
      linha.addView(simbolo, new LinearLayout.LayoutParams(dp(46), dp(46)));

      LinearLayout textos = new LinearLayout(this);
      textos.setOrientation(LinearLayout.VERTICAL);
      textos.setPadding(dp(12), 0, dp(6), 0);

      TextView titulo = texto(nome, 15, Color.WHITE);
      titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
      TextView detalhe = texto(descricao, 12, Color.rgb(155, 165, 195));
      textos.addView(titulo);
      textos.addView(detalhe);

      linha.addView(textos, new LinearLayout.LayoutParams(0, -2, 1));
      TextView seta = texto("›", 28, Color.rgb(175, 105, 255));
      linha.addView(seta);

      linha.setOnClickListener(v -> abrirAplicativo(pacote, site));

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              -1, -2
      );
      params.bottomMargin = dp(8);
      pai.addView(linha, params);
  }

    void criarTopo(LinearLayout pai) {

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView logo = texto("▶ HAVE", 31, Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        logo.setShadowLayer(dp(15), 0, 0, roxo);

        TextView subtitulo = texto(
                "  WATCH TOGETHER",
                11,
                Color.rgb(170, 180, 215)
        );

        LinearLayout marca = new LinearLayout(this);
        marca.setOrientation(LinearLayout.VERTICAL);
        marca.addView(logo);
        marca.addView(subtitulo);

        topo.addView(marca, new LinearLayout.LayoutParams(
                0, -2, 1
        ));

        TextView busca = botaoTopo("⌕");

        LinearLayout perfilTopo = new LinearLayout(this);
        perfilTopo.setOrientation(LinearLayout.VERTICAL);
        perfilTopo.setGravity(Gravity.CENTER);
        perfilTopo.setPadding(dp(2), dp(2), dp(2), 0);

        TextView avatar = texto("●", 25, Color.rgb(190, 100, 255));
        avatar.setGravity(Gravity.CENTER);

        GradientDrawable fundoAvatar = fundo(
                Color.argb(170, 12, 13, 35),
                50
        );
        fundoAvatar.setStroke(
                dp(2),
                Color.rgb(145, 45, 255)
        );
        avatar.setBackground(fundoAvatar);

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(dp(50), dp(50));
        perfilTopo.addView(avatar, avatarParams);

        TextView nomeTopo = texto(
                "Perfil",
                10,
                Color.rgb(210, 200, 240)
        );
        nomeTopo.setGravity(Gravity.CENTER);
        nomeTopo.setMaxLines(1);
        perfilTopo.addView(nomeTopo, new LinearLayout.LayoutParams(
                dp(62), dp(18)
        ));

        busca.setOnClickListener(v -> abrirBuscaHave());

        perfilTopo.setOnClickListener(v -> abrirPerfilHave());

        FirebaseUser usuarioTopo =
                FirebaseAuth.getInstance().getCurrentUser();

        if (usuarioTopo != null) {
            FirebaseFirestore.getInstance()
                    .collection("have_usuarios")
                    .document(usuarioTopo.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nome = doc.getString("nome");

                            if (nome != null && !nome.trim().isEmpty()) {
                                nomeTopo.setText(nome.trim());
                                String inicial = nome.trim()
                                        .substring(0, 1)
                                        .toUpperCase();
                                avatar.setText(inicial);
                            }
                        }
                    });
        }

        topo.addView(busca);

        LinearLayout.LayoutParams perfilParams =
                new LinearLayout.LayoutParams(dp(68), dp(76));
        perfilParams.setMargins(dp(4), 0, 0, 0);
        topo.addView(perfilTopo, perfilParams);

        pai.addView(topo);
    }

    TextView botaoTopo(String s) {

        TextView t = texto(s, 25, Color.WHITE);
        t.setGravity(Gravity.CENTER);

        GradientDrawable g = fundo(
                Color.argb(150, 12, 13, 35),
                50
        );

        g.setStroke(
                dp(1),
                Color.argb(110, 130, 50, 255)
        );

        t.setBackground(g);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        dp(50), dp(50)
                );

        p.setMargins(dp(5), 0, 0, 0);

        t.setLayoutParams(p);

        return t;
    }

    private String urlNexoArquivo(String rota, String chave) {
        if (chave == null || chave.trim().isEmpty()) {
            return "";
        }

        String valor = chave.trim();
        if (valor.startsWith("https://") || valor.startsWith("http://")) {
            return valor;
        }

        return "https://delicate-sunset-c8b5.lipesanderson.workers.dev/"
                + rota + "?key=" + android.net.Uri.encode(valor);
    }

    private void carregarCatalogoNexoHave() {
        new Thread(() -> {
            HttpURLConnection conexao = null;

            try {
                URL url = new URL(
                        "https://delicate-sunset-c8b5.lipesanderson.workers.dev/file?key=dados%2Fcatalogo.json"
                );

                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);
                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                int codigo = conexao.getResponseCode();

                InputStream entrada =
                        codigo >= 200 && codigo < 300
                                ? conexao.getInputStream()
                                : conexao.getErrorStream();

                if (entrada == null) {
                    throw new Exception(
                            "Resposta vazia da Nuvem NEXO"
                    );
                }

                java.io.ByteArrayOutputStream buffer =
                        new java.io.ByteArrayOutputStream();

                byte[] dados = new byte[4096];
                int quantidade;

                while ((quantidade = entrada.read(dados)) != -1) {
                    buffer.write(dados, 0, quantidade);
                }

                entrada.close();

                JSONObject resposta =
                        new JSONObject(buffer.toString("UTF-8"));

                if (!resposta.optBoolean("sucesso", false) && !resposta.has("filmes") && !resposta.has("conteudos")) {
                    throw new Exception(
                            resposta.optString(
                                    "erro",
                                    "Falha ao carregar Nuvem NEXO"
                            )
                    );
                }

                // HAVE_NEXO_SERIES_PATCH_V1
                // Combina filmes e séries do catálogo remoto.
                JSONArray origem = new JSONArray();
                JSONArray filmesOrigem = resposta.optJSONArray("filmes");
                JSONArray seriesOrigem = resposta.optJSONArray("series");

                if (filmesOrigem == null) {
                    filmesOrigem = resposta.optJSONArray("conteudos");
                }

                if (filmesOrigem != null) {
                    for (int i = 0; i < filmesOrigem.length(); i++) {
                        JSONObject original = filmesOrigem.optJSONObject(i);
                        if (original != null) {
                            JSONObject copia = new JSONObject(original.toString());
                            copia.put("_haveTipoCatalogo", "filme");
                            origem.put(copia);
                        }
                    }
                }

                if (seriesOrigem != null) {
                    for (int i = 0; i < seriesOrigem.length(); i++) {
                        JSONObject original = seriesOrigem.optJSONObject(i);
                        if (original != null) {
                            JSONObject copia = new JSONObject(original.toString());
                            copia.put("_haveTipoCatalogo", "serie");
                            origem.put(copia);
                        }
                    }
                }

                JSONArray itens = new JSONArray();

                for (int i = 0; i < origem.length(); i++) {

                    JSONObject item =
                            origem.optJSONObject(i);

                    if (item == null) {
                        continue;
                    }

                    JSONObject filme =
                            new JSONObject();

                    filme.put(
                            "id",
                            item.optString("id", "")
                    );

                    filme.put(
                            "titulo",
                            item.optString("titulo", "Sem título")
                    );

                    filme.put(
                            "title",
                            item.optString("titulo", "Sem título")
                    );

                    filme.put(
                            "descricao",
                            item.optString("descricao", item.optString("sinopse", ""))
                    );

                    filme.put(
                            "description",
                            item.optString("descricao", item.optString("sinopse", ""))
                    );

                    filme.put(
                            "ano",
                            item.optInt("ano", 0)
                    );

                    filme.put(
                            "categoria",
                            item.optString("categoria", "")
                    );

                    filme.put(
                            "tipo",
                            item.optString(
                                    "tipo",
                                    item.optString("_haveTipoCatalogo", "filme")
                            )
                    );

                    filme.put("estado", item.optString("estado", ""));
                    if (item.has("temporadas")) {
                        filme.put("temporadas", item.getJSONArray("temporadas"));
                    }
                    if (item.has("episodios")) {
                        filme.put("episodios", item.getJSONArray("episodios"));
                    }

                    filme.put(
                            "destaque",
                            item.optBoolean("destaque", false)
                    );

                    filme.put(
                            "duracao",
                            item.optLong("duracao", 0)
                    );

                    filme.put(
                            "capa",
                            urlNexoArquivo("file", item.optString("capa_url", item.optString("capa", "")))
                    );

                    filme.put(
                            "poster_url",
                            urlNexoArquivo("file", item.optString("capa_url", item.optString("capa", "")))
                    );

                    filme.put(
                            "poster",
                            urlNexoArquivo("file", item.optString("capa_url", item.optString("capa", "")))
                    );

                    filme.put(
                            "banner",
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", "")))
                    );

                    filme.put(
                            "banner_url",
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", "")))
                    );

                    filme.put(
                            "backdrop",
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", "")))
                    );

                    filme.put(
                            "video_url",
                            urlNexoArquivo("file", (item.optString("video_url", "").trim().isEmpty() ? item.optString("videoKey", "") : item.optString("video_url", "")))
                    );

                    itens.put(filme);
                }

                catalogoNexoHave = itens;

                final JSONArray itensFinais = itens;

                runOnUiThread(() ->
                        montarCatalogoNexoHave(itensFinais)
                );

            } catch (Exception e) {

                catalogoNexoHave =
                        new JSONArray();

                runOnUiThread(() -> {

                    if (areaNexoFilmes != null) {
                        areaNexoFilmes.removeAllViews();
                    }
                });

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private void filtrarCatalogoNexoHave(String busca) {

        if (catalogoNexoHave == null) {
            return;
        }

        String termo = busca == null
                ? ""
                : busca.trim().toLowerCase(java.util.Locale.ROOT);

        if (termo.isEmpty()) {
            montarCatalogoNexoHave(catalogoNexoHave);
            return;
        }

        JSONArray resultados = new JSONArray();

        for (int i = 0; i < catalogoNexoHave.length(); i++) {

            JSONObject filme =
                    catalogoNexoHave.optJSONObject(i);

            if (filme == null) {
                continue;
            }

            String titulo =
                    filme.optString("titulo",
                            filme.optString("title", ""));

            String descricao =
                    filme.optString("descricao",
                            filme.optString("overview", ""));

            String categoria =
                    filme.optString("categoria", "");

            String textoBusca =
                    (titulo + " " + descricao + " " + categoria)
                            .toLowerCase(java.util.Locale.ROOT);

            if (textoBusca.contains(termo)) {
                resultados.put(filme);
            }
        }

        montarCatalogoNexoHave(resultados);
    }

    private void montarCatalogoNexoHave(JSONArray itens) {
        if (areaNexoFilmes == null) return;

        areaNexoFilmes.removeAllViews();

        TextView tituloPrincipal = texto("NEXO FILMES", 25, Color.WHITE);
        tituloPrincipal.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        tituloPrincipal.setPadding(0, dp(8), 0, dp(3));
        areaNexoFilmes.addView(tituloPrincipal,
                new LinearLayout.LayoutParams(-1, -2));

        TextView subtitulo = texto(
                "Filmes e séries disponíveis para assistir",
                13, Color.rgb(145, 155, 185));
        subtitulo.setPadding(0, 0, 0, dp(12));
        areaNexoFilmes.addView(subtitulo,
                new LinearLayout.LayoutParams(-1, -2));

        // Categorias NEXO — faixa horizontal rolável na Home.
        HorizontalScrollView categoriasScroll = new HorizontalScrollView(this);
        categoriasScroll.setHorizontalScrollBarEnabled(false);
        categoriasScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout categoriasLinha = new LinearLayout(this);
        categoriasLinha.setOrientation(LinearLayout.HORIZONTAL);
        categoriasLinha.setGravity(Gravity.CENTER_VERTICAL);

        String[] categoriasNexo = {
                "TODOS",
                "AÇÃO",
                "AVENTURA",
                "COMÉDIA",
                "DRAMA",
                "TERROR",
                "ROMANCE",
                "FICÇÃO",
                "SUSPENSE",
                "ANIMAÇÃO",
                "ANIME",
                "INFANTIL",
                "DOCUMENTÁRIO"
        };

        for (String categoria : categoriasNexo) {
            TextView botaoCategoria = texto(
                    categoria,
                    12,
                    categoria.equals("TODOS")
                            ? Color.WHITE
                            : Color.rgb(175, 185, 210)
            );
            botaoCategoria.setGravity(Gravity.CENTER);
            botaoCategoria.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            botaoCategoria.setPadding(
                    dp(16), dp(9), dp(16), dp(9)
            );
            botaoCategoria.setBackground(
                    fundo(
                            categoria.equals("TODOS")
                                    ? Color.rgb(30, 35, 58)
                                    : Color.rgb(14, 16, 30),
                            40
                    )
            );

            LinearLayout.LayoutParams categoriaLp =
                    new LinearLayout.LayoutParams(-2, dp(38));
            categoriaLp.setMargins(
                    dp(3), dp(2), dp(5), dp(8)
            );

            categoriasLinha.addView(botaoCategoria, categoriaLp);

            final String categoriaFinal = categoria;

            botaoCategoria.setOnClickListener(v -> {
                String categoriaFiltro =
                        "TODOS".equalsIgnoreCase(categoriaFinal)
                                ? ""
                                : categoriaFinal;

                abrirCatalogoVerticalNexo(
                        "filme",
                        categoriaFiltro
                );
            });
        }

        categoriasScroll.addView(categoriasLinha,
                new HorizontalScrollView.LayoutParams(-2, -2));

        areaNexoFilmes.addView(
                categoriasScroll,
                new LinearLayout.LayoutParams(-1, -2)
        );

        if (itens == null || itens.length() == 0) {
            TextView vazio = texto(
                    "Nenhum título publicado ainda.",
                    14, Color.rgb(170, 180, 205));
            vazio.setPadding(dp(4), dp(10), 0, dp(20));
            areaNexoFilmes.addView(vazio);
            return;
        }

        adicionarSecaoNexo("DESTAQUES", itens, true);

        JSONArray filmes = new JSONArray();
        JSONArray series = new JSONArray();

        for (int i = 0; i < itens.length(); i++) {
            JSONObject item = itens.optJSONObject(i);
            if (item == null || item.optBoolean("destaque", false)) continue;

            String tipo = item.optString("_haveTipoCatalogo", "").trim();
            if (tipo.isEmpty()) tipo = item.optString("tipo", "filme").trim();

            boolean ehSerie =
                    "serie".equalsIgnoreCase(tipo)
                    || "série".equalsIgnoreCase(tipo)
                    || "series".equalsIgnoreCase(tipo)
                    || "séries".equalsIgnoreCase(tipo);

            if (ehSerie) series.put(item);
            else filmes.put(item);
        }

        adicionarSecaoNexo("FILMES", filmes, false);
        adicionarSecaoNexo("SÉRIES", series, false);
    }

    private void adicionarSecaoNexo(
            String nome,
            JSONArray filmes,
            boolean somenteDestaques
    ) {

        JSONArray lista = new JSONArray();

        for (int i = 0; i < filmes.length(); i++) {

            JSONObject filme = filmes.optJSONObject(i);

            if (filme == null) {
                continue;
            }

            if (somenteDestaques &&
                    !filme.optBoolean("destaque", false)) {
                continue;
            }

            lista.put(filme);
        }

        if (lista.length() == 0) {
            return;
        }

        TextView titulo = texto(
                nome,
                19,
                Color.WHITE
        );

        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        titulo.setPadding(
                0,
                dp(14),
                0,
                dp(8)
        );

        areaNexoFilmes.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, -2)
        );

        HorizontalScrollView horizontal =
                new HorizontalScrollView(this);

        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setClipToPadding(false);
        horizontal.setPadding(0, 0, 0, dp(5));

        LinearLayout linha =
                new LinearLayout(this);

        linha.setOrientation(
                LinearLayout.HORIZONTAL
        );

        horizontal.addView(
                linha,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        -2
                )
        );

        for (int i = 0; i < lista.length(); i++) {

            JSONObject filme =
                    lista.optJSONObject(i);

            if (filme == null) {
                continue;
            }

            View card = criarCardNexo(filme);

            LinearLayout.LayoutParams cp =
                    new LinearLayout.LayoutParams(
                            dp(158),
                            dp(300)
                    );

            cp.setMargins(
                    0,
                    0,
                    dp(10),
                    dp(5)
            );

            linha.addView(card, cp);
        }

        areaNexoFilmes.addView(
                horizontal,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );
    }



    private void abrirCatalogoVerticalNexo(String filtroTipo, String filtroCategoria) {
        final android.app.Dialog dialog = new android.app.Dialog(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar
        );

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));
        raiz.setPadding(dp(16), dp(14), dp(16), dp(10));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = texto("NEXO FILMES", 22, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView fechar = texto("✕", 22, Color.WHITE);
        fechar.setGravity(Gravity.CENTER);

        topo.addView(titulo,
                new LinearLayout.LayoutParams(0, dp(48), 1f));

        topo.addView(fechar,
                new LinearLayout.LayoutParams(dp(48), dp(48)));

        fechar.setOnClickListener(v -> dialog.dismiss());

        raiz.addView(topo);

        LinearLayout tipos = new LinearLayout(this);
        tipos.setOrientation(LinearLayout.HORIZONTAL);

        String[] tiposNexo = {"FILMES", "SÉRIES"};

        for (String tipo : tiposNexo) {
            TextView botao = texto(
                    tipo,
                    13,
                    tipo.equalsIgnoreCase(filtroTipo)
                            ? Color.WHITE
                            : Color.rgb(160, 170, 195)
            );
            botao.setGravity(Gravity.CENTER);
            botao.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            botao.setPadding(dp(18), 0, dp(18), 0);
            botao.setBackground(
                    fundo(
                            tipo.equalsIgnoreCase(filtroTipo)
                                    ? Color.rgb(30, 35, 58)
                                    : Color.rgb(12, 14, 28),
                            30
                    )
            );

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, dp(40));
            lp.setMargins(0, 0, dp(8), dp(8));

            tipos.addView(botao, lp);

            final String tipoFinal =
                    tipo.equalsIgnoreCase("SÉRIES") ? "serie" : "filme";

            botao.setOnClickListener(v -> {
                dialog.dismiss();
                abrirCatalogoVerticalNexo(tipoFinal, filtroCategoria);
            });
        }

        raiz.addView(tipos);

        HorizontalScrollView categoriasScroll =
                new HorizontalScrollView(this);
        categoriasScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout categorias =
                new LinearLayout(this);
        categorias.setOrientation(LinearLayout.HORIZONTAL);

        String[] categoriasNexo = {
                "TODOS", "AÇÃO", "AVENTURA", "COMÉDIA", "DRAMA",
                "TERROR", "ROMANCE", "FICÇÃO", "SUSPENSE",
                "ANIMAÇÃO", "ANIME", "INFANTIL", "DOCUMENTÁRIO"
        };

        for (String categoria : categoriasNexo) {
            TextView botao = texto(
                    categoria,
                    12,
                    categoria.equalsIgnoreCase(filtroCategoria)
                            || ("TODOS".equals(categoria)
                            && (filtroCategoria == null
                            || filtroCategoria.trim().isEmpty()))
                            ? Color.WHITE
                            : Color.rgb(160, 170, 195)
            );

            botao.setGravity(Gravity.CENTER);
            botao.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            botao.setPadding(dp(15), 0, dp(15), 0);
            botao.setBackground(
                    fundo(
                            categoria.equalsIgnoreCase(filtroCategoria)
                                    || ("TODOS".equals(categoria)
                                    && (filtroCategoria == null
                                    || filtroCategoria.trim().isEmpty()))
                                    ? Color.rgb(30, 35, 58)
                                    : Color.rgb(12, 14, 28),
                            30
                    )
            );

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(-2, dp(38));
            lp.setMargins(0, 0, dp(7), dp(10));

            categorias.addView(botao, lp);

            final String categoriaFinal = categoria;

            botao.setOnClickListener(v -> {
                String novaCategoria =
                        "TODOS".equalsIgnoreCase(categoriaFinal)
                                ? ""
                                : categoriaFinal;

                abrirCatalogoVerticalNexo(
                        filtroTipo,
                        novaCategoria
                );
            });
        }

        categoriasScroll.addView(
                categorias,
                new HorizontalScrollView.LayoutParams(-2, -2)
        );

        raiz.addView(
                categoriasScroll,
                new LinearLayout.LayoutParams(-1, -2)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout grade = new LinearLayout(this);
        grade.setOrientation(LinearLayout.VERTICAL);
        grade.setPadding(0, dp(4), 0, dp(30));

        JSONArray origem = catalogoNexoHave;

        if (origem != null) {
            for (int i = 0; i < origem.length(); i++) {
                JSONObject item = origem.optJSONObject(i);
                if (item == null) continue;

                String tipo = item.optString(
                        "_haveTipoCatalogo",
                        item.optString("tipo", "filme")
                ).trim();

                boolean ehSerie =
                        "serie".equalsIgnoreCase(tipo)
                        || "série".equalsIgnoreCase(tipo)
                        || "series".equalsIgnoreCase(tipo)
                        || "séries".equalsIgnoreCase(tipo);

                boolean tipoOk =
                        "serie".equalsIgnoreCase(filtroTipo)
                                ? ehSerie
                                : !ehSerie;

                if (!tipoOk) continue;

                if (filtroCategoria != null &&
                        !filtroCategoria.trim().isEmpty()) {

                    String categoriaItem = item.optString(
                            "categoria",
                            item.optString("genero", "")
                    ).trim();

                    if (!categoriaItem.toUpperCase()
                            .contains(filtroCategoria.toUpperCase())) {
                        continue;
                    }
                }

                View card = criarCardNexoVertical(item);

                LinearLayout.LayoutParams cardLp =
                        new LinearLayout.LayoutParams(
                                -1,
                                dp(186)
                        );
                cardLp.setMargins(0, 0, 0, dp(12));

                grade.addView(card, cardLp);
            }
        }

        if (grade.getChildCount() == 0) {
            TextView vazio = texto(
                    "Nenhum título encontrado nesta categoria.",
                    15,
                    Color.rgb(170, 180, 205)
            );
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(0, dp(50), 0, dp(50));
            grade.addView(vazio);
        }

        scroll.addView(grade);
        raiz.addView(
                scroll,
                new LinearLayout.LayoutParams( -1, 0, 1f)
        );

        dialog.setContentView(raiz);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    -1,
                    -1
            );
        }
    }

    void criarHero(LinearLayout pai) {
    
            heroHave = new LinearLayout(this);
            heroHave.setOrientation(LinearLayout.VERTICAL);
            heroHave.setPadding(dp(0), dp(0), dp(0), dp(0));
            heroHave.setClipToOutline(true);
    
            GradientDrawable fundoHero = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{
                            Color.rgb(32, 7, 58),
                            Color.rgb(7, 13, 35),
                            Color.rgb(3, 25, 45)
                    }
            );
            fundoHero.setCornerRadius(dp(28));
            fundoHero.setStroke(dp(1), Color.rgb(105, 45, 220));
            heroHave.setBackground(fundoHero);
            heroHave.setElevation(dp(8));
    
            FrameLayout imagemArea = new FrameLayout(this);
    
            imagemHeroHave = new ImageView(this);
            imagemHeroHave.setScaleType(ImageView.ScaleType.CENTER_CROP);
    
            imagemArea.addView(
                    imagemHeroHave,
                    new FrameLayout.LayoutParams(-1, dp(245))
            );
    
            GradientDrawable degradado = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            Color.TRANSPARENT,
                            Color.argb(225, 5, 8, 20)
                    }
            );
    
            View camada = new View(this);
            camada.setBackground(degradado);
    
            imagemArea.addView(
                    camada,
                    new FrameLayout.LayoutParams(-1, dp(245))
            );
    
            provedorHeroHave = texto(
                    "HAVE",
                    12,
                    Color.WHITE
            );
            provedorHeroHave.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            provedorHeroHave.setGravity(Gravity.CENTER);
            provedorHeroHave.setPadding(dp(12), dp(5), dp(12), dp(5));
    
            GradientDrawable fundoProvedor = new GradientDrawable();
            fundoProvedor.setColor(Color.argb(210, 0, 0, 0));
            fundoProvedor.setCornerRadius(dp(20));
            provedorHeroHave.setBackground(fundoProvedor);
    
            FrameLayout.LayoutParams provedorParams =
                    new FrameLayout.LayoutParams(
                            -2,
                            dp(34),
                            Gravity.TOP | Gravity.START
                    );
            provedorParams.setMargins(dp(15), dp(15), 0, 0);
    
            imagemArea.addView(provedorHeroHave, provedorParams);
    
            heroHave.addView(
                    imagemArea,
                    new LinearLayout.LayoutParams(-1, dp(245))
            );
    
            LinearLayout informacoes = new LinearLayout(this);
            informacoes.setOrientation(LinearLayout.VERTICAL);
            informacoes.setPadding(
                    dp(20),
                    dp(14),
                    dp(20),
                    dp(20)
            );
    
            TextView etiqueta = texto(
                    "EM DESTAQUE",
                    11,
                    Color.rgb(190, 105, 255)
            );
            etiqueta.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    
            tituloHeroHave = texto(
                    "Filmes, séries e muita resenha.",
                    25,
                    Color.WHITE
            );
            tituloHeroHave.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tituloHeroHave.setPadding(0, dp(5), 0, dp(6));
    
            descricaoHeroHave = texto(
                    "Descobrindo novidades para assistir juntos.",
                    14,
                    Color.rgb(190, 205, 235)
            );
            descricaoHeroHave.setMaxLines(3);
            descricaoHeroHave.setEllipsize(
                    android.text.TextUtils.TruncateAt.END
            );
    
            informacoes.addView(etiqueta);
            informacoes.addView(tituloHeroHave);
            informacoes.addView(descricaoHeroHave);
    
            indicadoresHeroHave = new LinearLayout(this);
            indicadoresHeroHave.setOrientation(LinearLayout.HORIZONTAL);
            indicadoresHeroHave.setGravity(Gravity.CENTER_VERTICAL);
            indicadoresHeroHave.setPadding(0, dp(5), 0, dp(8));
            informacoes.addView(
                    indicadoresHeroHave,
                    new LinearLayout.LayoutParams(-1, dp(20))
            );
            heroHave.addView(informacoes);
    
            LinearLayout.LayoutParams hp =
                    new LinearLayout.LayoutParams(-1, -2);
    
            hp.setMargins(0, dp(20), 0, dp(15));
    
            pai.addView(heroHave, hp);
    
            carregarCatalogoHave();
        }

    void abrirAplicativo(String pacote, String site) {
    
            try {
    
                Intent intent =
                        getPackageManager()
                                .getLaunchIntentForPackage(pacote);
    
                if (intent != null) {
                    startActivity(intent);
                    return;
                }
    
            } catch (Exception ignored) {}
    
            try {
    
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(site)
                ));
    
            } catch (Exception e) {
    
                Toast.makeText(
                        this,
                        "Não foi possível abrir o serviço.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

    void abrirBuscaHave() {
            final LinearLayout raiz = new LinearLayout(this);
            raiz.setOrientation(LinearLayout.VERTICAL);
            raiz.setPadding(dp(20), dp(24), dp(20), dp(24));
            raiz.setBackgroundColor(Color.rgb(3, 4, 15));
    
            TextView titulo = texto("⌕ BUSCAR NO HAVE", 24, Color.WHITE);
            titulo.setTypeface(Typeface.DEFAULT_BOLD);
    
            TextView subtitulo = texto(
                    "Encontre pessoas pelo ID HAVE",
                    14,
                    Color.rgb(150, 165, 190)
            );
            subtitulo.setPadding(0, dp(6), 0, dp(22));
    
            EditText campo = new EditText(this);
            campo.setHint("Ex.: HAVE-AVN4SX12");
            campo.setHintTextColor(Color.rgb(100, 115, 140));
            campo.setTextColor(Color.WHITE);
            campo.setSingleLine(true);
            campo.setTextSize(15);
            campo.setPadding(dp(16), 0, dp(16), 0);
    
            GradientDrawable fundoCampo = fundo(
                    Color.rgb(10, 14, 32),
                    16
            );
            fundoCampo.setStroke(
                    dp(1),
                    Color.rgb(0, 210, 255)
            );
            campo.setBackground(fundoCampo);
    
            raiz.addView(titulo);
            raiz.addView(subtitulo);
            raiz.addView(
                    campo,
                    new LinearLayout.LayoutParams(-1, dp(54))
            );
    
            Button pesquisar = new Button(this);
            pesquisar.setText("🔎 PESQUISAR");
            pesquisar.setTextColor(Color.WHITE);
            pesquisar.setTextSize(14);
            pesquisar.setAllCaps(false);
    
            GradientDrawable fundoBotao = fundo(
                    Color.rgb(35, 18, 65),
                    16
            );
            fundoBotao.setStroke(
                    dp(1),
                    Color.rgb(145, 45, 255)
            );
            pesquisar.setBackground(fundoBotao);
    
            LinearLayout.LayoutParams botaoParams =
                    new LinearLayout.LayoutParams(-1, dp(54));
            botaoParams.topMargin = dp(14);
            raiz.addView(pesquisar, botaoParams);
    
            LinearLayout resultadoArea = new LinearLayout(this);
            resultadoArea.setOrientation(LinearLayout.VERTICAL);
            resultadoArea.setPadding(dp(16), dp(18), dp(16), dp(18));
            resultadoArea.setVisibility(View.GONE);
    
            GradientDrawable resultadoBg = fundo(
                    Color.rgb(8, 12, 28),
                    18
            );
            resultadoBg.setStroke(
                    dp(1),
                    Color.rgb(55, 75, 110)
            );
            resultadoArea.setBackground(resultadoBg);
    
            LinearLayout.LayoutParams resultadoParams =
                    new LinearLayout.LayoutParams(-1, -2);
            resultadoParams.topMargin = dp(18);
    
            TextView resultado = texto(
                    "",
                    15,
                    Color.rgb(220, 225, 240)
            );
    
            resultadoArea.addView(resultado);
    
            Button adicionar = new Button(this);
            adicionar.setText("➕ ADICIONAR AMIGO");
            adicionar.setTextColor(Color.WHITE);
            adicionar.setTextSize(14);
            adicionar.setAllCaps(false);
            adicionar.setVisibility(View.GONE);
    
            GradientDrawable adicionarBg = fundo(
                    Color.rgb(25, 55, 70),
                    16
            );
            adicionarBg.setStroke(
                    dp(1),
                    Color.rgb(40, 255, 145)
            );
            adicionar.setBackground(adicionarBg);
    
            LinearLayout.LayoutParams adicionarParams =
                    new LinearLayout.LayoutParams(-1, dp(52));
            adicionarParams.topMargin = dp(14);
    
            resultadoArea.addView(adicionar, adicionarParams);
            raiz.addView(resultadoArea, resultadoParams);
    
            TextView fechar = texto(
                    "← VOLTAR",
                    14,
                    Color.rgb(0, 210, 255)
            );
            fechar.setGravity(Gravity.CENTER);
            fechar.setPadding(0, dp(18), 0, dp(10));
    
            raiz.addView(
                    fechar,
                    new LinearLayout.LayoutParams(-1, dp(54))
            );
    
            setContentView(raiz);
    
            final String[] uidEncontrado = {""};
            final String[] nomeEncontrado = {""};
            final String[] haveIdEncontrado = {""};
    
            pesquisar.setOnClickListener(v -> {
                String id = campo.getText()
                        .toString()
                        .trim()
                        .toUpperCase(Locale.US);
    
                if (id.isEmpty()) {
                    campo.setError("Digite um ID HAVE");
                    campo.requestFocus();
                    return;
                }
    
                resultadoArea.setVisibility(View.VISIBLE);
                adicionar.setVisibility(View.GONE);
    
                resultado.setText("🔎 Procurando " + id + "...");
    
                FirebaseFirestore.getInstance()
                        .collection("have_perfis_publicos")
                        .whereEqualTo("haveId", id)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(snapshot -> {
    
                            if (snapshot.isEmpty()) {
                                uidEncontrado[0] = "";
                                resultado.setText(
                                        "❌ Usuário não encontrado.\n\n" +
                                        "Confira o ID HAVE e tente novamente."
                                );
                                return;
                            }
    
                            com.google.firebase.firestore.DocumentSnapshot doc =
                                    snapshot.getDocuments().get(0);
    
                            String uid = doc.getId();
                            String nome = doc.getString("nome");
                            String haveId = doc.getString("haveId");
                            String status = doc.getString("status");
    
                            if (nome == null || nome.isEmpty()) {
                                nome = "Usuário HAVE";
                            }
    
                            if (haveId == null || haveId.isEmpty()) {
                                haveId = id;
                            }
    
                            if (status == null || status.isEmpty()) {
                                status = "offline";
                            }
    
                            uidEncontrado[0] = uid;
                            nomeEncontrado[0] = nome;
                            haveIdEncontrado[0] = haveId;
    
                            FirebaseUser atual =
                                    FirebaseAuth.getInstance().getCurrentUser();
    
                            if (atual != null &&
                                    atual.getUid().equals(uid)) {
    
                                resultado.setText(
                                        "🟣 ESTE É SEU PERFIL\n\n" +
                                        "👤 " + nome + "\n" +
                                        "🆔 " + haveId + "\n" +
                                        "● " + status.toUpperCase(Locale.US)
                                );
    
                                adicionar.setVisibility(View.GONE);
                                return;
                            }
    
                            resultado.setText(
                                    "● PERFIL ENCONTRADO\n\n" +
                                    "👤 " + nome + "\n" +
                                    "🆔 " + haveId + "\n" +
                                    "● " + status.toUpperCase(Locale.US)
                            );
    
                            adicionar.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> {
                            uidEncontrado[0] = "";
    
                            resultado.setText(
                                    "⚠️ Não foi possível realizar a busca.\n\n" +
                                    "Verifique sua conexão e tente novamente."
                            );
                        });
            });
    
            adicionar.setOnClickListener(v -> {
    
                if (uidEncontrado[0].isEmpty()) {
                    Toast.makeText(
                            this,
                            "Pesquise um usuário HAVE primeiro.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
    
                Toast.makeText(
                        this,
                        "Usuário encontrado. Próximo passo: criar amizade HAVE.",
                        Toast.LENGTH_SHORT
                ).show();
            });
    
            fechar.setOnClickListener(v -> montarHome());
        }

    void abrirPerfilHave() {
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    
            if (usuario == null) {
                Toast.makeText(this, "Faça login no HAVE primeiro.", Toast.LENGTH_SHORT).show();
                return;
            }
    
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = usuario.getUid();
    
            db.collection("have_usuarios")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            exibirPerfilHave(
                                    doc.getString("nome"),
                                    doc.getString("haveId"),
                                    usuario.getEmail()
                            );
                            sincronizarPerfilPublico(
                                    uid,
                                    doc.getString("nome"),
                                    doc.getString("haveId")
                            );
                            return;
                        }
    
                        String email = usuario.getEmail();
                        String nome = usuario.getDisplayName();
    
                        if (nome == null || nome.trim().isEmpty()) {
                            nome = "Usuário HAVE";
                        }
    
                        String haveId = "HAVE-" +
                                UUID.randomUUID()
                                        .toString()
                                        .replace("-", "")
                                        .substring(0, 8)
                                        .toUpperCase(Locale.US);
    
                        Map<String, Object> dados = new HashMap<>();
                        dados.put("nome", nome);
                        dados.put("email", email == null ? "" : email);
                        dados.put("haveId", haveId);
                        dados.put("criadoEm", FieldValue.serverTimestamp());
    
                        final String nomeFinal = nome;
                        final String haveIdFinal = haveId;
                        final String emailFinal = email == null ? "" : email;
    
                        db.collection("have_usuarios")
                                .document(uid)
                                .set(dados)
                                .addOnSuccessListener(v -> {
                                    sincronizarPerfilPublico(
                                            uid,
                                            nomeFinal,
                                            haveIdFinal
                                    );
    
                                    Toast.makeText(
                                            this,
                                            "Perfil HAVE recuperado com sucesso.",
                                            Toast.LENGTH_SHORT
                                    ).show();
    
                                    exibirPerfilHave(
                                            nomeFinal,
                                            haveIdFinal,
                                            emailFinal
                                    );
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                this,
                                                "Não foi possível criar seu perfil: "
                                                        + e.getLocalizedMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Erro ao carregar perfil: "
                                            + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        }



private void carregarCatalogoHave() {
        new Thread(() -> {
            HttpURLConnection conexao = null;

            try {
                URL url = new URL(
                        "https://delicate-sunset-c8b5.lipesanderson.workers.dev/file?key=dados%2Fcatalogo.json"
                );

                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);
                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                int codigo = conexao.getResponseCode();

                InputStream entrada =
                        codigo >= 200 && codigo < 300
                                ? conexao.getInputStream()
                                : conexao.getErrorStream();

                if (entrada == null) {
                    throw new Exception("Resposta vazia da Nuvem NEXO");
                }

                java.io.ByteArrayOutputStream buffer =
                        new java.io.ByteArrayOutputStream();

                byte[] dados = new byte[4096];
                int quantidade;

                while ((quantidade = entrada.read(dados)) != -1) {
                    buffer.write(dados, 0, quantidade);
                }

                entrada.close();

                JSONObject resposta =
                        new JSONObject(buffer.toString("UTF-8"));

                if (!resposta.optBoolean("sucesso", false) && !resposta.has("filmes") && !resposta.has("conteudos")) {
                    throw new Exception(
                            resposta.optString(
                                    "erro",
                                    "Falha ao carregar Nuvem NEXO"
                            )
                    );
                }

                JSONArray origem =
                        resposta.optJSONArray("filmes");

                if (origem == null) {
                    origem = resposta.optJSONArray("conteudos");
                }

                if (origem == null) {
                    origem = new JSONArray();
                }

                JSONArray destaques = new JSONArray();

                for (int i = 0; i < origem.length(); i++) {
                    JSONObject item =
                            origem.optJSONObject(i);

                    if (item == null) {
                        continue;
                    }

                    if (!item.optBoolean("destaque", false)) {
                        continue;
                    }

                    String banner =
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", ""))).trim();

                    if (banner.isEmpty()) {
                        continue;
                    }

                    JSONObject filme =
                            new JSONObject();

                    filme.put(
                            "title",
                            item.optString("titulo", "Filme em destaque")
                    );

                    filme.put(
                            "name",
                            item.optString("titulo", "Filme em destaque")
                    );

                    filme.put(
                            "descricao",
                            item.optString("descricao", item.optString("sinopse", ""))
                    );

                    filme.put(
                            "overview",
                            item.optString("descricao", item.optString("sinopse", ""))
                    );

                    filme.put(
                            "banner",
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", "")))
                    );

                    filme.put(
                            "backdrop",
                            urlNexoArquivo("file", item.optString("banner_url", item.optString("banner", "")))
                    );

                    filme.put(
                            "capa",
                            urlNexoArquivo("file", item.optString("capa_url", item.optString("capa", "")))
                    );

                    filme.put(
                            "have_provider",
                            "Nuvem NEXO"
                    );

                    destaques.put(filme);
                }

                if (destaques.length() == 0) {
                    return;
                }

                catalogoHeroHave = destaques;
                indiceHeroHave = 0;

                runOnUiThread(() -> {
                    try {
                        atualizarHeroHave(
                                catalogoHeroHave.getJSONObject(0)
                        );
                        iniciarCarrosselHeroHave();
                    } catch (Exception ignored) {
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Destaques Nuvem NEXO temporariamente indisponíveis",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

private void sincronizarPerfilPublico(
            String uid,
            String nome,
            String haveId
    ) {
        if (nome == null || nome.trim().isEmpty()) {
            nome = "Usuário HAVE";
        }

        if (haveId == null || haveId.trim().isEmpty()) {
            return;
        }

        Map<String, Object> publico = new HashMap<>();
        publico.put("nome", nome);
        publico.put("haveId", haveId);
        publico.put("status", "online");
        publico.put("atualizadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("have_perfis_publicos")
                .document(uid)
                .set(publico, SetOptions.merge());
    }

private void exibirPerfilHave(
        String nome,
        String haveId,
        String email
) {
    if (nome == null || nome.trim().isEmpty()) nome = "Usuário HAVE";
    if (haveId == null || haveId.trim().isEmpty()) haveId = "HAVE-SEM-ID";
    if (email == null) email = "";

    final String nomeInicial = nome;
    final String emailInicial = email;
    final String haveIdCopia = haveId;

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(Color.rgb(2, 4, 15));

    LinearLayout tela = new LinearLayout(this);
    tela.setOrientation(LinearLayout.VERTICAL);
    tela.setPadding(dp(22), dp(24), dp(22), dp(30));

    TextView titulo = texto("◉  PERFIL HAVE", 25, Color.WHITE);
    titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    tela.addView(titulo);

    TextView subtitulo = texto(
            "Seu espaço pessoal • seguro • privado",
            13,
            Color.rgb(115, 210, 255)
    );
    subtitulo.setPadding(0, dp(5), 0, dp(24));
    tela.addView(subtitulo);

    TextView avatar = texto("◉", 52, Color.rgb(90, 220, 255));
    avatar.setGravity(Gravity.CENTER);
    GradientDrawable avatarBg = new GradientDrawable();
    avatarBg.setShape(GradientDrawable.OVAL);
    avatarBg.setColor(Color.TRANSPARENT);
    avatarBg.setStroke(dp(2), Color.rgb(70, 210, 255));
    avatar.setBackground(avatarBg);

    LinearLayout.LayoutParams avatarParams =
            new LinearLayout.LayoutParams(dp(100), dp(100));
    avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
    tela.addView(avatar, avatarParams);

    TextView trocarFoto = itemConfiguracaoHave(
            "📷",
            "Trocar foto",
            "Adicionar ou alterar sua foto de perfil"
    );
    trocarFoto.setOnClickListener(v ->
            Toast.makeText(
                    this,
                    "A foto será conectada ao armazenamento na próxima etapa.",
                    Toast.LENGTH_SHORT
            ).show()
    );
    tela.addView(trocarFoto);

    TextView nomeView = texto(nome, 23, Color.WHITE);
    nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    nomeView.setGravity(Gravity.CENTER);
    nomeView.setPadding(0, dp(10), 0, dp(2));
    tela.addView(nomeView);

    TextView editarNome = itemConfiguracaoHave(
            "✎",
            "Editar nome",
            "Alterar o nome exibido no seu perfil"
    );
    editarNome.setOnClickListener(v -> {
        final EditText campoNome = new EditText(this);
        campoNome.setSingleLine(true);
        campoNome.setText(nomeInicial);
        campoNome.setTextColor(Color.WHITE);
        campoNome.setHintTextColor(Color.rgb(120, 140, 175));
        campoNome.setHint("Novo nome");

        new android.app.AlertDialog.Builder(this)
                .setTitle("✎ EDITAR NOME")
                .setView(campoNome)
                .setPositiveButton("SALVAR", (dialog, which) -> {
                    String novoNome = campoNome.getText().toString().trim();

                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Digite um nome válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser usuarioAtual =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (usuarioAtual == null) {
                        Toast.makeText(this, "Sessão HAVE não encontrada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = usuarioAtual.getUid();

                    Map<String, Object> dadosNome = new HashMap<>();
                    dadosNome.put("nome", novoNome);

                    FirebaseFirestore.getInstance()
                            .collection("have_usuarios")
                            .document(uid)
                            .update(dadosNome)
                            .addOnSuccessListener(v2 -> {
                                sincronizarPerfilPublico(uid, novoNome, haveIdCopia);
                                Toast.makeText(
                                        this,
                                        "Nome atualizado com sucesso.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                exibirPerfilHave(
                                        novoNome,
                                        haveIdCopia,
                                        emailInicial
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Não foi possível atualizar o nome.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    });
    tela.addView(editarNome);

    TextView status = texto(
            "● ONLINE",
            13,
            Color.rgb(55, 235, 155)
    );
    status.setGravity(Gravity.CENTER);
    status.setPadding(0, dp(8), 0, dp(20));
    tela.addView(status);

    TextView config = texto("⚙  CONFIGURAÇÕES", 16, Color.WHITE);
    config.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    config.setPadding(0, dp(8), 0, dp(5));
    tela.addView(config);

    TextView privacidade = itemConfiguracaoHave(
            "🔐",
            "Privacidade",
            "Controle o que outras pessoas podem ver"
    );
    privacidade.setOnClickListener(v -> abrirPrivacidadeHave());
    tela.addView(privacidade);

    TextView seguranca = itemConfiguracaoHave(
            "🛡",
            "Segurança",
            "Proteção e segurança da sua conta"
    );
    seguranca.setOnClickListener(v -> abrirSegurancaHave());
    tela.addView(seguranca);

    TextView aparencia = itemConfiguracaoHave(
            "🎨",
            "Aparência",
            "Personalize o visual do HAVE"
    );
    aparencia.setOnClickListener(v -> abrirAparenciaHave());
    tela.addView(aparencia);

    TextView notificacoes = itemConfiguracaoHave(
            "🔔",
            "Notificações",
            "Controle os avisos do HAVE"
    );
    notificacoes.setOnClickListener(v -> abrirNotificacoesHave());
    tela.addView(notificacoes);

    TextView conexoes = texto("👥  CONEXÕES", 16, Color.WHITE);
    conexoes.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    conexoes.setPadding(0, dp(26), 0, dp(5));
    tela.addView(conexoes);

    TextView meuId = itemConfiguracaoHave(
            "🔗",
            "Meu HAVE ID",
            "Copiar, compartilhar ou revogar seu ID"
    );
    meuId.setOnClickListener(v -> abrirMeuHaveId(nomeInicial, haveIdCopia));
    tela.addView(meuId);

    TextView bloqueados = itemConfiguracaoHave(
            "🚫",
            "Usuários bloqueados",
            "Gerenciar pessoas que você bloqueou"
    );
    bloqueados.setOnClickListener(v -> abrirBloqueadosHave());
    tela.addView(bloqueados);

    TextView conta = texto("✉  E-mail privado", 13, Color.rgb(115, 130, 165));
    conta.setPadding(0, dp(26), 0, dp(4));
    tela.addView(conta);

    TextView emailPrivado = texto(
            "Este e-mail não aparece no perfil público.",
            12,
            Color.rgb(90, 105, 140)
    );
    tela.addView(emailPrivado);

    TextView voltar = itemConfiguracaoHave(
            "←",
            "Voltar",
            "Retornar ao início do HAVE"
    );
    voltar.setOnClickListener(v -> montarHome());
    tela.addView(voltar);

    scroll.addView(tela);
    setContentView(scroll);
}



private void atualizarHeroHave(JSONObject filme) {

        try {

            String titulo = filme.optString(
                    "title",
                    filme.optString(
                            "name",
                            "Filme em destaque"
                    )
            );

            String descricao = filme.optString(
                    "overview",
                    "Descubra este título e assista com seus amigos."
            );

            String provedor =
                    filme.optString(
                            "have_provider",
                            "Nuvem NEXO"
                    );

            String imagem =
                    filme.optString(
                            "banner",
                            ""
                    );

            if (
                    imagem == null
                    || imagem.trim().isEmpty()
            ) {

                imagem =
                        filme.optString(
                                "backdrop",
                                ""
                        );
            }

            if (
                    imagem == null
                    || imagem.trim().isEmpty()
            ) {

                imagem =
                        filme.optString(
                                "capa",
                                ""
                        );
            }

            tituloHeroHave.setText(titulo);

            descricaoHeroHave.setText(
                    descricao == null ||
                    descricao.trim().isEmpty()
                            ? "Descubra este título e assista com seus amigos."
                            : descricao
            );

            provedorHeroHave.setText(provedor);
            atualizarIndicadoresHeroHave();

            if (imagem != null &&
                    !imagem.trim().isEmpty()) {

                String endereco;

                if (imagem.startsWith("http://") ||
                        imagem.startsWith("https://")) {

                    endereco = imagem;

                } else {

                    endereco =
                            "https://image.tmdb.org/t/p/w1280"
                            + imagem;
                }

                carregarImagemHero(endereco);
            }

        } catch (Exception e) {

            android.util.Log.e(
                    "HAVE_HERO",
                    "Erro ao atualizar hero",
                    e
            );
        }
    }

private void iniciarCarrosselHeroHave() {
        handlerHeroHave.removeCallbacksAndMessages(null);

        handlerHeroHave.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (catalogoHeroHave != null &&
                                catalogoHeroHave.length() > 0) {

                            indiceHeroHave++;

                            if (indiceHeroHave >=
                                    catalogoHeroHave.length()) {
                                indiceHeroHave = 0;
                            }

                            try {
                                atualizarHeroHave(
                                        catalogoHeroHave.getJSONObject(
                                                indiceHeroHave
                                        )
                                );
                            } catch (Exception ignored) {
                            }
                        }

                        handlerHeroHave.postDelayed(
                                this,
                                6500
                        );
                    }
                },
                6500
        );
    }

private TextView itemConfiguracaoHave(
            String icone,
            String titulo,
            String descricao
    ) {
        TextView item = texto(
                icone + "  " + titulo + "\n" + descricao,
                15,
                Color.WHITE
        );

        item.setPadding(dp(4), dp(14), dp(4), dp(14));
        item.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable linha = new GradientDrawable();
        linha.setColor(Color.TRANSPARENT);
        linha.setStroke(dp(1), Color.rgb(35, 70, 105));
        linha.setCornerRadius(dp(14));
        item.setBackground(linha);

        item.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                item.setAlpha(0.65f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                    || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                item.setAlpha(1f);
            }
            return false;
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(5);
        params.bottomMargin = dp(5);
        item.setLayoutParams(params);

        return item;
    }

private void abrirPrivacidadeHave() {
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(dp(24), dp(10), dp(24), dp(10));

        TextView titulo = texto(
                "🔒 PRIVACIDADE",
                21,
                Color.WHITE
        );
        titulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView explicacao = texto(
                "Escolha quais informações do seu perfil podem "
                        + "ser exibidas para outras pessoas.",
                13,
                Color.rgb(155, 165, 195)
        );
        explicacao.setPadding(0, dp(8), 0, dp(18));

        android.widget.Switch foto = new android.widget.Switch(this);
        foto.setText("📷 Mostrar minha foto");
        foto.setTextColor(Color.WHITE);
        foto.setChecked(getPreferences(0)
                .getBoolean("priv_foto", true));

        android.widget.Switch status = new android.widget.Switch(this);
        status.setText("🟢 Mostrar status online");
        status.setTextColor(Color.WHITE);
        status.setChecked(getPreferences(0)
                .getBoolean("priv_status", true));

        TextView idInfo = texto(
                "🆔 HAVE ID\n"
                        + "Seu HAVE ID permanece público para "
                        + "permitir que outras pessoas encontrem você.",
                13,
                Color.rgb(195, 205, 225)
        );
        idInfo.setPadding(0, dp(12), 0, dp(12));

        TextView emailInfo = texto(
                "📧 E-mail\n"
                        + "Seu e-mail é sempre privado e não aparece "
                        + "no perfil público.",
                13,
                Color.rgb(195, 205, 225)
        );
        emailInfo.setPadding(0, dp(8), 0, dp(12));

        conteudo.addView(titulo);
        conteudo.addView(explicacao);
        conteudo.addView(foto);
        conteudo.addView(status);
        conteudo.addView(idInfo);
        conteudo.addView(emailInfo);

        android.app.AlertDialog dialog = builder
                .setView(conteudo)
                .setPositiveButton("SALVAR", null)
                .setNegativeButton("CANCELAR", null)
                .create();

        dialog.setOnShowListener(v -> {
            dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(x -> {
                getPreferences(0)
                        .edit()
                        .putBoolean("priv_foto", foto.isChecked())
                        .putBoolean("priv_status", status.isChecked())
                        .apply();

                Toast.makeText(
                        this,
                        "Privacidade atualizada.",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            });
        });

        dialog.show();
    }

private void abrirSegurancaHave() {
    final EditText email = new EditText(this);
    email.setSingleLine(true);
    email.setHint("E-mail da conta HAVE");
    email.setTextColor(Color.WHITE);
    email.setHintTextColor(Color.rgb(120, 140, 175));
    email.setInputType(
            android.text.InputType.TYPE_CLASS_TEXT |
            android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    );

    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
    if (usuario != null && usuario.getEmail() != null) {
        email.setText(usuario.getEmail());
    }

    new android.app.AlertDialog.Builder(this)
            .setTitle("🛡 SEGURANÇA")
            .setMessage(
                    "Digite o e-mail da sua conta. " +
                    "O Firebase enviará o link oficial para criar uma nova senha."
            )
            .setView(email)
            .setNegativeButton("FECHAR", null)
            .setPositiveButton("ENVIAR", (dialog, which) -> {
                String endereco = email.getText().toString().trim();

                if (endereco.isEmpty() ||
                        !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(endereco).matches()) {
                    Toast.makeText(
                            this,
                            "Digite um e-mail válido.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(endereco)
                        .addOnSuccessListener(v ->
                                Toast.makeText(
                                        this,
                                        "E-mail de redefinição enviado. Verifique também o spam.",
                                        Toast.LENGTH_LONG
                                ).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(
                                        this,
                                        "Não foi possível enviar o e-mail.",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
            })
            .show();
}

private void abrirAparenciaHave() {
    final String[] opcoes = {
            "☀  Modo claro",
            "🌙  Modo escuro",
            "⚙  Automático"
    };

    android.content.SharedPreferences pref =
            getSharedPreferences("have_config", MODE_PRIVATE);

    String atual = pref.getString("tema", "claro");

    int marcado = 0;
    if ("escuro".equals(atual)) marcado = 1;
    if ("automatico".equals(atual)) marcado = 2;

    final int[] escolha = {marcado};

    new android.app.AlertDialog.Builder(this)
            .setTitle("🎨 APARÊNCIA")
            .setSingleChoiceItems(
                    opcoes,
                    marcado,
                    (dialog, which) -> escolha[0] = which
            )
            .setNegativeButton("CANCELAR", null)
            .setPositiveButton("APLICAR", (dialog, which) -> {
                String tema =
                        escolha[0] == 1
                                ? "escuro"
                                : escolha[0] == 2
                                ? "automatico"
                                : "claro";

                pref.edit()
                        .putString("tema", tema)
                        .apply();

                Toast.makeText(
                        this,
                        "Tema salvo: " +
                                (tema.equals("claro")
                                        ? "Modo claro"
                                        : tema.equals("escuro")
                                        ? "Modo escuro"
                                        : "Automático"),
                        Toast.LENGTH_SHORT
                ).show();

                aplicarTemaHave(tema);
            })
            .show();
}

private void abrirNotificacoesHave() {
        android.widget.Switch avisos =
                new android.widget.Switch(this);

        avisos.setText("🔔 Permitir notificações do HAVE");
        avisos.setTextColor(Color.WHITE);
        avisos.setChecked(
                getPreferences(0)
                        .getBoolean("notificacoes_have", true)
        );

        new android.app.AlertDialog.Builder(this)
                .setTitle("🔔 NOTIFICAÇÕES")
                .setView(avisos)
                .setPositiveButton("SALVAR", (dialog, which) -> {
                    getPreferences(0)
                            .edit()
                            .putBoolean(
                                    "notificacoes_have",
                                    avisos.isChecked()
                            )
                            .apply();

                    Toast.makeText(
                            this,
                            "Preferência de notificações salva.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

private void abrirMeuHaveId(String nome, String haveId) {
    TextView id = texto(haveId, 22, Color.rgb(75, 220, 255));
    id.setGravity(Gravity.CENTER);
    id.setPadding(0, dp(18), 0, dp(18));

    LinearLayout box = new LinearLayout(this);
    box.setOrientation(LinearLayout.VERTICAL);
    box.setPadding(dp(18), dp(8), dp(18), dp(4));
    box.addView(id);

    Button copiar = pequenoBotao("📋  COPIAR ID", Color.rgb(70, 190, 255));
    copiar.setOnClickListener(v -> {
        android.content.ClipboardManager cm =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(
                android.content.ClipData.newPlainText("HAVE ID", haveId)
        );
        Toast.makeText(this, "HAVE ID copiado.", Toast.LENGTH_SHORT).show();
    });
    box.addView(copiar);

    Button compartilhar = pequenoBotao(
            "📤  COMPARTILHAR",
            Color.rgb(170, 75, 255)
    );
    compartilhar.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                "Meu HAVE ID é " + haveId
        );
        startActivity(Intent.createChooser(intent, "Compartilhar HAVE ID"));
    });
    box.addView(compartilhar);

    Button revogar = pequenoBotao(
            "🔄  REVOGAR ID",
            Color.rgb(255, 80, 120)
    );
    revogar.setOnClickListener(v -> {
        new android.app.AlertDialog.Builder(this)
                .setTitle("REVOGAR HAVE ID")
                .setMessage(
                        "O ID atual deixará de funcionar para novas buscas. " +
                        "Um novo HAVE ID será criado."
                )
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("REVOGAR", (dialog, which) -> {
                    FirebaseUser usuario =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (usuario == null) {
                        Toast.makeText(
                                this,
                                "Sessão HAVE não encontrada.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String novoId =
                            "HAVE-" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 8)
                                    .toUpperCase(Locale.US);

                    String uid = usuario.getUid();

                    Map<String, Object> dados = new HashMap<>();
                    dados.put("haveId", novoId);

                    FirebaseFirestore db =
                            FirebaseFirestore.getInstance();

                    db.collection("have_usuarios")
                            .document(uid)
                            .update(dados)
                            .addOnSuccessListener(v2 -> {
                                sincronizarPerfilPublico(
                                        uid,
                                        nome,
                                        novoId
                                );

                                Toast.makeText(
                                        this,
                                        "Novo HAVE ID criado com sucesso.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                exibirPerfilHave(
                                        nome,
                                        novoId,
                                        usuario.getEmail()
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Não foi possível revogar o ID.",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .show();
    });
    box.addView(revogar);

    new android.app.AlertDialog.Builder(this)
            .setTitle("🔗 MEU HAVE ID")
            .setView(box)
            .setNegativeButton("FECHAR", null)
            .show();
}

private void abrirBloqueadosHave() {
    FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

    if (usuario == null) {
        Toast.makeText(this, "Sessão HAVE não encontrada.", Toast.LENGTH_SHORT).show();
        return;
    }

    TextView info = texto(
            "Aqui aparecerão os usuários que você bloquear.\n\n" +
            "O bloqueio ficará vinculado à sua conta HAVE.",
            14,
            Color.rgb(185, 195, 220)
    );
    info.setPadding(dp(8), dp(10), dp(8), dp(10));

    new android.app.AlertDialog.Builder(this)
            .setTitle("🚫 USUÁRIOS BLOQUEADOS")
            .setView(info)
            .setPositiveButton("FECHAR", null)
            .show();
}



private void atualizarIndicadoresHeroHave() {
        if (indicadoresHeroHave == null) return;

        indicadoresHeroHave.removeAllViews();

        if (catalogoHeroHave == null || catalogoHeroHave.length() <= 1) {
            return;
        }

        for (int i = 0; i < catalogoHeroHave.length(); i++) {
            GradientDrawable ponto = new GradientDrawable();
            ponto.setShape(GradientDrawable.RECTANGLE);
            ponto.setCornerRadius(dp(10));
            ponto.setColor(i == indiceHeroHave
                    ? Color.rgb(0, 220, 255)
                    : Color.rgb(90, 75, 125));

            View indicador = new View(this);
            indicador.setBackground(ponto);

            int largura = i == indiceHeroHave ? dp(23) : dp(7);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(largura, dp(7));
            lp.setMargins(dp(3), 0, dp(3), 0);
            indicadoresHeroHave.addView(indicador, lp);
        }
    }

private void carregarImagemHero(
            String endereco
    ) {

        new Thread(() -> {

            HttpURLConnection conexao = null;
            InputStream entrada = null;

            try {

                URL url = new URL(endereco);

                conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(20000);
                conexao.setInstanceFollowRedirects(true);

                int codigo =
                        conexao.getResponseCode();

                if (codigo < 200 || codigo >= 300) {

                    throw new Exception(
                            "Imagem HTTP " + codigo
                    );
                }

                entrada =
                        conexao.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                entrada
                        );

                if (bitmap == null) {

                    throw new Exception(
                            "Bitmap nulo"
                    );
                }

                runOnUiThread(() -> {

                    imagemHeroHave
                            .setImageBitmap(bitmap);

                    imagemHeroHave
                            .setAlpha(0f);

                    imagemHeroHave
                            .animate()
                            .alpha(1f)
                            .setDuration(450)
                            .start();
                });

            } catch (Exception e) {

                android.util.Log.e(
                        "HAVE_HERO",
                        "Falha ao carregar imagem: "
                                + endereco,
                        e
                );

            } finally {

                try {

                    if (entrada != null) {
                        entrada.close();
                    }

                } catch (Exception ignored) {}

                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

private void aplicarTemaHave(String tema) {
    boolean escuro = "escuro".equals(tema);

    if ("automatico".equals(tema)) {
        int modo =
                getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        escuro =
                modo ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    if (escuro) {
        getWindow().setStatusBarColor(Color.rgb(2, 4, 15));
        getWindow().setNavigationBarColor(Color.rgb(2, 4, 15));
    } else {
        getWindow().setStatusBarColor(Color.rgb(245, 248, 255));
        getWindow().setNavigationBarColor(Color.rgb(245, 248, 255));
    }
}

Button pequenoBotao(String textoBotao, int cor) {
        Button b = new Button(this);
        b.setText(textoBotao);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        GradientDrawable g = fundo(Color.rgb(10, 14, 32), 14);
        g.setStroke(dp(2), cor);
        b.setBackground(g);
        return b;
    }

    private View criarCardNexo(JSONObject filme) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, dp(8));

        GradientDrawable fundoCard = fundo(
                Color.rgb(9, 10, 22),
                16
        );
        fundoCard.setStroke(dp(1), Color.rgb(42, 38, 72));
        card.setBackground(fundoCard);
        card.setElevation(dp(4));

        final ImageView poster = new ImageView(this);
        poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        poster.setBackgroundColor(Color.rgb(9, 10, 22));

        LinearLayout.LayoutParams posterLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(245)
                );

        card.addView(poster, posterLp);

        String posterUrl = filme.optString("poster_url", "");

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("capa", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("poster", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("banner_url", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("banner", "");
        }

        if (!posterUrl.trim().isEmpty()) {
            carregarImagemNexo(poster, posterUrl);
        }

        TextView titulo = texto(
                filme.optString("titulo", "Sem título"),
                14,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER_VERTICAL);
        titulo.setMaxLines(2);
        titulo.setEllipsize(null);
        titulo.setPadding(dp(8), dp(7), dp(8), 0);

        card.addView(
                titulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        card.setOnClickListener(v ->
                abrirDetalhesNexo(filme)
        );

        return card;
    }

    private View criarCardNexoVertical(JSONObject filme) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));

        GradientDrawable fundoCard = fundo(
                Color.rgb(9, 10, 22),
                16
        );
        fundoCard.setStroke(dp(1), Color.rgb(42, 38, 72));
        card.setBackground(fundoCard);
        card.setElevation(dp(3));

        ImageView poster = new ImageView(this);
        poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        poster.setBackgroundColor(Color.rgb(9, 10, 22));

        LinearLayout.LayoutParams posterLp =
                new LinearLayout.LayoutParams(dp(115), dp(170));
        posterLp.setMargins(0, 0, dp(12), 0);

        card.addView(poster, posterLp);

        String posterUrl = filme.optString("poster_url", "");

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("capa", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("poster", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("banner_url", "");
        }

        if (posterUrl.trim().isEmpty()) {
            posterUrl = filme.optString("banner", "");
        }

        if (!posterUrl.trim().isEmpty()) {
            carregarImagemNexo(poster, posterUrl);
        }

        LinearLayout informacoes = new LinearLayout(this);
        informacoes.setOrientation(LinearLayout.VERTICAL);
        informacoes.setGravity(Gravity.CENTER_VERTICAL);

        TextView titulo = texto(
                filme.optString("titulo", "Sem título"),
                17,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView meta = texto(
                filme.optString("categoria", "Sem categoria")
                        + "  •  "
                        + (filme.optInt("ano", 0) > 0
                        ? String.valueOf(filme.optInt("ano", 0))
                        : "N/A"),
                12,
                Color.rgb(145, 155, 185)
        );

        String tipo = filme.optString(
                "_haveTipoCatalogo",
                filme.optString("tipo", "filme")
        );

        TextView tipoTexto = texto(
                "serie".equalsIgnoreCase(tipo)
                        || "série".equalsIgnoreCase(tipo)
                        ? "SÉRIE"
                        : "FILME",
                11,
                Color.rgb(120, 220, 255)
        );
        tipoTexto.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        informacoes.addView(
                titulo,
                new LinearLayout.LayoutParams(-1, -2)
        );
        informacoes.addView(
                meta,
                new LinearLayout.LayoutParams(-1, -2)
        );

        LinearLayout.LayoutParams tipoLp =
                new LinearLayout.LayoutParams(-2, -2);
        tipoLp.setMargins(0, dp(8), 0, 0);

        informacoes.addView(tipoTexto, tipoLp);

        card.addView(
                informacoes,
                new LinearLayout.LayoutParams(0, -1, 1f)
        );

        card.setOnClickListener(v ->
                abrirDetalhesNexo(filme)
        );

        return card;
    }

    private void carregarImagemNexo(
            ImageView imagem,
            String endereco
    ) {

        new Thread(() -> {

            try {

                URL url =
                        new URL(endereco);

                HttpURLConnection conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(15000);

                InputStream entrada =
                        conexao.getInputStream();

                android.graphics.Bitmap bitmap =
                        android.graphics.BitmapFactory
                                .decodeStream(entrada);

                entrada.close();
                conexao.disconnect();

                if (bitmap != null) {

                    runOnUiThread(() ->
                            imagem.setImageBitmap(bitmap)
                    );
                }

            } catch (Exception ignored) {
            }

        }).start();
    }

    private String chaveSagaNexo(String titulo) {
        if (titulo == null) return "";

        String base = titulo
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\b(19|20)\\d{2}\\b", " ")
                .replaceAll("\\btemporada\\s*\\d+\\b", " ")
                .replaceAll("\\b(parte|part)\\s*\\d+\\b", " ")
                .replaceAll("\\b(ep|epis[oó]dio)\\s*\\d+\\b", " ")
                .replaceAll("\\b\\d+\\b", " ")
                .replaceAll("[\\:\\-\\|\\(\\)\\[\\],.!?]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (base.isEmpty()) return "";

        String[] palavras = base.split("\\s+");

        /*
         * Para títulos com várias palavras, usamos as duas primeiras
         * palavras significativas como identificação da franquia.
         *
         * Exemplos:
         * Jumanji Bem Vindo ... -> "jumanji bem"
         * Jumanji Proxima Fase -> "jumanji proxima"
         *
         * Para franquias que mudam completamente o subtítulo,
         * usamos somente a primeira palavra como fallback.
         */
        if (palavras.length == 1) {
            return palavras[0];
        }

        String primeira = palavras[0];

        /*
         * Nomes de franquias conhecidas que podem ter subtítulos
         * diferentes continuam sendo agrupados pela primeira palavra.
         */
        String[] franquias = {
                "jumanji",
                "john wick",
                "matrix",
                "avatar",
                "vingadores",
                "avengers",
                "transformers",
                "jurassic",
                "toy story",
                "carros",
                "shrek",
                "harry potter",
                "homem aranha",
                "homem-aranha",
                "spider man",
                "spider-man"
        };

        String inicioDuas = palavras[0] + " " + palavras[1];

        for (String franquia : franquias) {
            if (base.startsWith(franquia + " ")
                    || base.equals(franquia)) {
                return franquia;
            }
        }

        return inicioDuas;
    }

    private int numeroSagaNexo(String titulo) {
        if (titulo == null) return Integer.MAX_VALUE;

        java.util.regex.Matcher m =
                java.util.regex.Pattern
                        .compile("\\b(\\d+)\\b")
                        .matcher(titulo);

        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (Exception ignored) {
            }
        }

        String t = titulo.toLowerCase(Locale.ROOT);

        String[] romanos = {
                " i", " ii", " iii", " iv", " v",
                " vi", " vii", " viii", " ix", " x"
        };

        for (int i = 0; i < romanos.length; i++) {
            if (t.endsWith(romanos[i])) {
                return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private void abrirDetalhesNexo(JSONObject filme) {
        final android.app.Dialog dialog = new android.app.Dialog(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
        );

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(3, 4, 15));

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout conteudo = new LinearLayout(this);
        conteudo.setOrientation(LinearLayout.VERTICAL);
        conteudo.setPadding(0, 0, 0, dp(40));

        // ----------------------------------------------------
        // BANNER PRINCIPAL
        // ----------------------------------------------------
        FrameLayout bannerBox = new FrameLayout(this);

        ImageView banner = new ImageView(this);
        banner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        banner.setBackgroundColor(Color.rgb(8, 9, 18));

        bannerBox.addView(
                banner,
                new FrameLayout.LayoutParams(
                        -1,
                        dp(300),
                        Gravity.TOP
                )
        );

        String bannerUrl = filme.optString("banner", "");

        if (bannerUrl.trim().isEmpty()) {
            bannerUrl = filme.optString("backdrop", "");
        }

        if (bannerUrl.trim().isEmpty()) {
            bannerUrl = filme.optString("banner_url", "");
        }

        if (bannerUrl.trim().isEmpty()) {
            bannerUrl = filme.optString("capa", "");
        }

        if (!bannerUrl.trim().isEmpty()) {
            carregarImagemNexo(banner, bannerUrl);
        }

        // Gradiente escuro sobre o banner
        View sombra = new View(this);
        android.graphics.drawable.GradientDrawable gradiente =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{
                                Color.argb(20, 0, 0, 0),
                                Color.argb(100, 3, 4, 15),
                                Color.rgb(3, 4, 15)
                        }
                );
        sombra.setBackground(gradiente);

        bannerBox.addView(
                sombra,
                new FrameLayout.LayoutParams(
                        -1,
                        dp(300),
                        Gravity.TOP
                )
        );

        // Botão voltar
        TextView voltar = texto("‹", 38, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        voltar.setBackground(fundo(Color.argb(100, 0, 0, 0), 50));
        voltar.setOnClickListener(v -> dialog.dismiss());

        FrameLayout.LayoutParams voltarLp =
                new FrameLayout.LayoutParams(
                        dp(48),
                        dp(48),
                        Gravity.TOP | Gravity.START
                );
        voltarLp.setMargins(dp(16), dp(18), 0, 0);

        bannerBox.addView(voltar, voltarLp);

        conteudo.addView(
                bannerBox,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(300)
                )
        );

        // ----------------------------------------------------
        // BLOCO DE INFORMAÇÕES
        // ----------------------------------------------------
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.HORIZONTAL);
        info.setPadding(dp(20), 0, dp(20), 0);

        ImageView capa = new ImageView(this);
        capa.setScaleType(ImageView.ScaleType.CENTER_CROP);
        capa.setBackgroundColor(Color.rgb(10, 11, 20));

        String capaUrl = filme.optString("capa", "");

        if (capaUrl.trim().isEmpty()) {
            capaUrl = filme.optString("poster", "");
        }

        if (capaUrl.trim().isEmpty()) {
            capaUrl = filme.optString("poster_url", "");
        }

        if (!capaUrl.trim().isEmpty()) {
            carregarImagemNexo(capa, capaUrl);
        }

        LinearLayout.LayoutParams capaLp =
                new LinearLayout.LayoutParams(
                        dp(132),
                        dp(198)
                );
        capaLp.setMargins(0, -dp(62), dp(18), 0);

        info.addView(capa, capaLp);

        LinearLayout dados = new LinearLayout(this);
        dados.setOrientation(LinearLayout.VERTICAL);
        dados.setGravity(Gravity.BOTTOM);
        dados.setPadding(0, 0, 0, dp(8));

        String titulo = filme.optString(
                "titulo",
                filme.optString("title", "Sem título")
        );

        TextView tituloView = texto(
                titulo,
                22,
                Color.WHITE
        );
        tituloView.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        );
        tituloView.setMaxLines(3);

        dados.addView(
                tituloView,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        String ano = filme.optString("ano", "");
        String duracao = filme.optString("duracao", "");
        String categoria = filme.optString("categoria", "");

        StringBuilder meta = new StringBuilder();

        if (!ano.isEmpty()) {
            meta.append(ano);
        }

        if (!duracao.isEmpty() && !duracao.equals("0")) {
            int minutos = 0;
            try {
                minutos = Integer.parseInt(duracao);
            } catch (Exception ignored) {
            }

            if (minutos > 0) {
                if (meta.length() > 0) meta.append(" • ");
                meta.append(minutos).append(" min");
            }
        }

        if (!categoria.isEmpty()) {
            if (meta.length() > 0) meta.append(" • ");
            meta.append(categoria);
        }

        if (meta.length() > 0) {
            TextView metaView = texto(
                    meta.toString(),
                    13,
                    Color.rgb(190, 190, 205)
            );
            metaView.setPadding(0, dp(6), 0, 0);

            dados.addView(
                    metaView,
                    new LinearLayout.LayoutParams(
                            -1,
                            -2
                    )
            );
        }

        info.addView(
                dados,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1f
                )
        );

        conteudo.addView(
                info,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // ----------------------------------------------------
        // BOTÃO ASSISTIR
        // ----------------------------------------------------
        String videoUrlResolvido = filme.optString(
                "video_url", ""
        ).trim();

        if (videoUrlResolvido.isEmpty()) {
            String videoKey = filme.optString("videoKey", "").trim();
            if (!videoKey.isEmpty()) {
                videoUrlResolvido = urlNexoArquivo("video", videoKey);
            }
        }

        final String videoUrl = videoUrlResolvido;

        TextView assistir = texto(
                "▶  ASSISTIR AGORA",
                16,
                Color.WHITE
        );
        assistir.setGravity(Gravity.CENTER);
        assistir.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        );
        android.graphics.drawable.GradientDrawable fundoAssistir =
                new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{
                                Color.rgb(0, 112, 235),
                                Color.rgb(0, 195, 255)
                        }
                );
        fundoAssistir.setCornerRadius(dp(12));
        assistir.setBackground(fundoAssistir);
        assistir.setElevation(dp(5));
        assistir.setLetterSpacing(0.035f);

        LinearLayout.LayoutParams assistirLp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );
        assistirLp.setMargins(
                dp(20),
                dp(20),
                dp(20),
                dp(18)
        );

        conteudo.addView(assistir, assistirLp);

        // Ação do botão assistir para filmes.
        assistir.setOnClickListener(v -> {
            if (videoUrl.isEmpty()) {
                android.widget.Toast.makeText(
                        this,
                        "Este título ainda não possui vídeo cadastrado.",
                        android.widget.Toast.LENGTH_SHORT
                ).show();
                return;
            }

              abrirPlayerNexo(titulo, videoUrl, filme);
        });


        // ----------------------------------------------------
        // DESCRIÇÃO
        // ----------------------------------------------------
        TextView descricaoTitulo = texto(
                "SOBRE O FILME",
                13,
                Color.rgb(150, 150, 170)
        );
        descricaoTitulo.setTypeface(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams descricaoTituloLp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );
        descricaoTituloLp.setMargins(
                dp(20),
                dp(4),
                dp(20),
                dp(8)
        );

        conteudo.addView(
                descricaoTitulo,
                descricaoTituloLp
        );

        String descricao = filme.optString(
                "descricao",
                filme.optString(
                        "description",
                        filme.optString(
                                "overview",
                                "Sem descrição."
                        )
                )
        );

        TextView descricaoView = texto(
                descricao,
                15,
                Color.rgb(215, 215, 225)
        );
        descricaoView.setLineSpacing(dp(2), 1.05f);

        LinearLayout.LayoutParams descricaoLp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );
        descricaoLp.setMargins(
                dp(20),
                0,
                dp(20),
                dp(24)
        );

        conteudo.addView(
                descricaoView,
                descricaoLp
        );

        // ----------------------------------------------------
        // MAIS DA SAGA — horizontal e rolável
        // ----------------------------------------------------
        String chaveAtualSaga = chaveSagaNexo(titulo);

        if (!chaveAtualSaga.isEmpty() && catalogoNexoHave != null) {
            java.util.ArrayList<JSONObject> relacionados =
                    new java.util.ArrayList<>();

            for (int si = 0; si < catalogoNexoHave.length(); si++) {
                JSONObject candidato =
                        catalogoNexoHave.optJSONObject(si);

                if (candidato == null) continue;
                if (candidato == filme) continue;

                String tituloCandidato =
                        candidato.optString("titulo", "").trim();

                if (tituloCandidato.isEmpty()) continue;

                String chaveCandidato =
                        chaveSagaNexo(tituloCandidato);

                if (chaveAtualSaga.equals(chaveCandidato)) {
                    relacionados.add(candidato);
                }
            }

            if (!relacionados.isEmpty()) {
                java.util.Collections.sort(
                        relacionados,
                        (a, b) -> {
                            int na = numeroSagaNexo(
                                    a.optString("titulo", "")
                            );
                            int nb = numeroSagaNexo(
                                    b.optString("titulo", "")
                            );

                            if (na != nb) {
                                return Integer.compare(na, nb);
                            }

                            return a.optString("titulo", "")
                                    .compareToIgnoreCase(
                                            b.optString("titulo", "")
                                    );
                        }
                );

                TextView sagaTitulo = texto(
                        "MAIS DA SAGA",
                        17,
                        Color.WHITE
                );
                sagaTitulo.setTypeface(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                );
                sagaTitulo.setPadding(
                        dp(20), dp(2), dp(20), dp(8)
                );

                conteudo.addView(sagaTitulo);

                HorizontalScrollView sagaScroll =
                        new HorizontalScrollView(this);
                sagaScroll.setHorizontalScrollBarEnabled(false);
                sagaScroll.setOverScrollMode(
                        View.OVER_SCROLL_NEVER
                );

                LinearLayout sagaLinha =
                        new LinearLayout(this);
                sagaLinha.setOrientation(
                        LinearLayout.HORIZONTAL
                );
                sagaLinha.setPadding(
                        dp(20), 0, dp(20), dp(14)
                );

                for (JSONObject relacionado : relacionados) {
                    View cardSaga =
                            criarCardNexo(relacionado);

                    LinearLayout.LayoutParams sagaLp =
                            new LinearLayout.LayoutParams(
                                    dp(158),
                                    dp(240)
                            );
                    sagaLp.setMargins(
                            0, 0, dp(10), 0
                    );

                    sagaLinha.addView(
                            cardSaga,
                            sagaLp
                    );
                }

                sagaScroll.addView(
                        sagaLinha,
                        new HorizontalScrollView.LayoutParams(
                                -2,
                                -2
                        )
                );

                conteudo.addView(
                        sagaScroll,
                        new LinearLayout.LayoutParams(
                                -1,
                                -2
                        )
                );
            }
        }

        // Séries: mostrar temporadas e episódios preservados do catálogo R2.
        boolean ehSerie = "serie".equalsIgnoreCase(
                filme.optString("tipo", "filme")
        ) || "série".equalsIgnoreCase(filme.optString("tipo", ""));

        if (ehSerie) {
            assistir.setVisibility(View.GONE);

            TextView temporadasTitulo = texto(
                    "TEMPORADAS E EPISÓDIOS",
                    17,
                    Color.WHITE
            );
            temporadasTitulo.setTypeface(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD
            );
            temporadasTitulo.setPadding(dp(20), dp(8), dp(20), dp(12));
            conteudo.addView(temporadasTitulo);

            JSONArray temporadas = filme.optJSONArray("temporadas");
            if (temporadas == null || temporadas.length() == 0) {
                TextView vazio = texto(
                        "Nenhuma temporada cadastrada ainda.",
                        14,
                        Color.LTGRAY
                );
                vazio.setPadding(dp(20), dp(4), dp(20), dp(20));
                conteudo.addView(vazio);
            } else {
                for (int ti = 0; ti < temporadas.length(); ti++) {
                    JSONObject temporada = temporadas.optJSONObject(ti);
                    if (temporada == null) continue;

                    int numeroTemporada = temporada.optInt("numero", ti + 1);
                    TextView nomeTemporada = texto(
                            "Temporada " + numeroTemporada,
                            16,
                            Color.rgb(100, 190, 255)
                    );
                    nomeTemporada.setTypeface(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                    );
                    nomeTemporada.setPadding(
                            dp(20), dp(12), dp(20), dp(8)
                    );
                    conteudo.addView(nomeTemporada);

                    JSONArray episodios = temporada.optJSONArray("episodios");
                    if (episodios == null || episodios.length() == 0) {
                        TextView semEpisodios = texto(
                                "Nenhum episódio cadastrado.",
                                14,
                                Color.LTGRAY
                        );
                        semEpisodios.setPadding(
                                dp(28), dp(4), dp(20), dp(12)
                        );
                        conteudo.addView(semEpisodios);
                        continue;
                    }

                    for (int ei = 0; ei < episodios.length(); ei++) {
                        JSONObject episodio = episodios.optJSONObject(ei);
                        if (episodio == null) continue;

                        String nomeEpisodio = episodio.optString(
                                "titulo", "Episódio " + (ei + 1)
                        );
                        String estadoEpisodio = episodio.optString(
                                "estado", ""
                        );
                        boolean publicado = "publicado".equalsIgnoreCase(
                                estadoEpisodio
                        );

                        String idSerie = filme.optString(
                                "id",
                                filme.optString("titulo", filme.optString("title", "serie"))
                        );
                        final String chaveVisualizacaoFinal = "have_visto_" +
                                Integer.toHexString((
                                        idSerie + "|" + numeroTemporada + "|" +
                                        ei + "|" + nomeEpisodio
                                ).hashCode());

                        boolean jaVisto = getSharedPreferences(
                                "have_config", MODE_PRIVATE
                        ).getBoolean(chaveVisualizacaoFinal, false);

                        String legenda = publicado
                                ? nomeEpisodio
                                : nomeEpisodio + " — Aguardando publicação";

                        LinearLayout linhaEpisodio = new LinearLayout(this);
                        linhaEpisodio.setOrientation(LinearLayout.HORIZONTAL);
                        linhaEpisodio.setGravity(Gravity.CENTER_VERTICAL);
                        linhaEpisodio.setPadding(
                                dp(10), dp(8), dp(12), dp(8)
                        );
                        linhaEpisodio.setBackground(
                                fundo(Color.rgb(12, 14, 28), 12)
                        );

                        ImageView previaEpisodio = new ImageView(this);
                        previaEpisodio.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        previaEpisodio.setBackgroundColor(Color.rgb(20, 22, 38));

                        LinearLayout.LayoutParams previaLp =
                                new LinearLayout.LayoutParams(dp(108), dp(64));
                        previaLp.setMargins(0, 0, dp(12), 0);
                        linhaEpisodio.addView(previaEpisodio, previaLp);

                        String imagemEpisodio =
                                episodio.optString("imagem", "").trim();

                        if (!imagemEpisodio.isEmpty()) {
                            String urlImagemEpisodio =
                                    urlNexoArquivo("file", imagemEpisodio);
                            carregarImagemNexo(previaEpisodio, urlImagemEpisodio);
                        }

                        LinearLayout blocoTexto = new LinearLayout(this);
                        blocoTexto.setOrientation(LinearLayout.VERTICAL);
                        blocoTexto.setGravity(Gravity.CENTER_VERTICAL);

                        TextView tituloEpisodioView = texto(
                                legenda,
                                14,
                                publicado
                                        ? Color.WHITE
                                        : Color.rgb(170, 170, 185)
                        );

                        TextView numeroView = texto(
                                "Episódio " + (ei + 1) +
                                        "  •  " +
                                        temporada.optString("titulo",
                                                "Temporada " + numeroTemporada),
                                11,
                                Color.rgb(150, 153, 175)
                        );

                        blocoTexto.addView(
                                tituloEpisodioView,
                                new LinearLayout.LayoutParams(-1, -2)
                        );
                        blocoTexto.addView(
                                numeroView,
                                new LinearLayout.LayoutParams(-1, -2)
                        );

                        LinearLayout.LayoutParams textoLp =
                                new LinearLayout.LayoutParams(0, -2, 1f);
                        linhaEpisodio.addView(blocoTexto, textoLp);

                        final TextView check = texto(
                                jaVisto ? "✓" : "",
                                18,
                                jaVisto
                                        ? Color.rgb(0, 220, 255)
                                        : Color.rgb(120, 125, 145)
                        );
                        check.setGravity(Gravity.CENTER);
                        check.setBackground(
                                fundo(
                                        jaVisto
                                                ? Color.rgb(20, 45, 55)
                                                : Color.rgb(22, 24, 38),
                                        50
                                )
                        );

                        LinearLayout.LayoutParams checkLp =
                                new LinearLayout.LayoutParams(dp(34), dp(34));
                        checkLp.setMargins(dp(8), 0, 0, 0);
                        linhaEpisodio.addView(check, checkLp);

                        check.setOnClickListener(v -> {
                            boolean estadoAtual = getSharedPreferences(
                                    "have_config", MODE_PRIVATE
                            ).getBoolean(chaveVisualizacaoFinal, false);

                            boolean novoEstado = !estadoAtual;

                            getSharedPreferences(
                                    "have_config", MODE_PRIVATE
                            ).edit()
                                    .putBoolean(chaveVisualizacaoFinal, novoEstado)
                                    .apply();

                            check.setText(novoEstado ? "✓" : "");
                            check.setTextColor(
                                    novoEstado
                                            ? Color.rgb(0, 220, 255)
                                            : Color.rgb(120, 125, 145)
                            );
                            check.setBackground(
                                    fundo(
                                            novoEstado
                                                    ? Color.rgb(20, 45, 55)
                                                    : Color.rgb(22, 24, 38),
                                            50
                                    )
                            );
                        });

                        LinearLayout.LayoutParams episodioLp =
                                new LinearLayout.LayoutParams(-1, dp(80));
                        episodioLp.setMargins(
                                dp(20), dp(4), dp(20), dp(5)
                        );
                        conteudo.addView(linhaEpisodio, episodioLp);

                        if (publicado) {
                            final JSONObject episodioFinal = episodio;
                            final String tituloEpisodio = nomeEpisodio;
                            linhaEpisodio.setOnClickListener(v -> {
                                String chave = episodioFinal.optString(
                                        "videoKey", ""
                                );
                                String endereco = episodioFinal.optString(
                                        "video_url", ""
                                );

                                if (endereco.trim().isEmpty()) {
                                    endereco = episodioFinal.optString(
                                            "urlReproducao", ""
                                    );
                                }

                                if (endereco.trim().isEmpty()
                                        && !chave.trim().isEmpty()) {
                                    endereco = urlNexoArquivo("video", chave);
                                }

                                if (endereco.trim().isEmpty()) {
                                    Toast.makeText(
                                            this,
                                            "Este episódio ainda não possui vídeo.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                dialog.dismiss();
                                abrirPlayerNexo(tituloEpisodio, endereco, filme);
                            });
                        }
                    }
                }
            }
        }

        // Exibe a tela de detalhes do catálogo.
        scroll.addView(
                conteudo,
                new android.widget.ScrollView.LayoutParams(-1, -2)
        );
        raiz.addView(
                scroll,
                new FrameLayout.LayoutParams(-1, -1)
        );
        dialog.setContentView(raiz);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(-1, -1);
        }
    }

    private void abrirPlayerNexo(String titulo, String videoUrl) {
        abrirPlayerNexo(titulo, videoUrl, null);
    }

    private void abrirPlayerNexo(String titulo, String videoUrl, JSONObject detalhesRetorno) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Toast.makeText(this, "Este título ainda não possui vídeo.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String endereco = videoUrl.trim();
        final String chave = "have_progress_" +
                Integer.toHexString((titulo + "|" + endereco).hashCode());

        final android.content.SharedPreferences prefs =
                getSharedPreferences("have_config", MODE_PRIVATE);
        final long posicaoSalva = prefs.getLong(chave, 0L);
        final boolean[] posicaoRestaurada = {false};

        final android.app.Dialog dialog = new android.app.Dialog(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen
        );

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.BLACK);

        PlayerView playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        playerView.setControllerHideOnTouch(true);
        playerView.setShowBuffering(
                PlayerView.SHOW_BUFFERING_WHEN_PLAYING
        );
        playerView.setKeepScreenOn(true);
        playerView.setResizeMode(
                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        );

        raiz.addView(playerView, new FrameLayout.LayoutParams(-1, -1));

        final android.widget.ImageButton botaoTransmitir = new android.widget.ImageButton(this);
        botaoTransmitir.setImageResource(R.drawable.ic_cast_nexo);
        botaoTransmitir.setScaleType(android.widget.ImageView.ScaleType.CENTER);
        botaoTransmitir.setPadding(dp(12), dp(12), dp(12), dp(12));
        botaoTransmitir.setBackgroundColor(Color.TRANSPARENT);
        botaoTransmitir.setClickable(true);
        botaoTransmitir.setFocusable(true);
        botaoTransmitir.setContentDescription("Transmitir para TV");

        FrameLayout.LayoutParams transmitirParams =
                new FrameLayout.LayoutParams(dp(54), dp(54));
        transmitirParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        transmitirParams.setMargins(0, dp(18), dp(18), 0);
        raiz.addView(botaoTransmitir, transmitirParams);

        botaoTransmitir.setOnClickListener(v -> {
            try {
                startActivity(new android.content.Intent(
                        android.provider.Settings.ACTION_CAST_SETTINGS
                ));
            } catch (Exception erro) {
                android.util.Log.e(
                        "HAVE_PLAYER",
                        "Não foi possível abrir a tela de transmissão.",
                        erro
                );
                Toast.makeText(
                        HaveHomeActivity.this,
                        "A transmissão não está disponível neste aparelho.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        final DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        30_000,
                        120_000,
                        2_000,
                        5_000
                )
                .build();

        final ExoPlayer player = new ExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .setSeekBackIncrementMs(10_000L)
                .setSeekForwardIncrementMs(10_000L)
                .build();
        playerView.setPlayer(player);

        final float[] zoom = {1.0f};
        final android.view.ScaleGestureDetector pinch =
                new android.view.ScaleGestureDetector(
                        this,
                        new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            @Override
                            public boolean onScale(android.view.ScaleGestureDetector detector) {
                                zoom[0] = Math.max(1.0f, Math.min(
                                        2.4f, zoom[0] * detector.getScaleFactor()
                                ));
                                playerView.setResizeMode(
                                        zoom[0] > 1.08f
                                                ? androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                : androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                );
                                return true;
                            }
                        }
                );

        playerView.setOnTouchListener((v, event) -> {
            pinch.onTouchEvent(event);
            return false;
        });

        final android.os.Handler handler =
                new android.os.Handler(android.os.Looper.getMainLooper());

        final Runnable[] salvar = new Runnable[1];
        salvar[0] = () -> {
            if (!dialog.isShowing()) return;

            if (player.getPlaybackState() == Player.STATE_ENDED) {
                prefs.edit().remove(chave).apply();
            } else if (player.getCurrentPosition() > 0L) {
                prefs.edit().putLong(chave, player.getCurrentPosition()).apply();
            }
            handler.postDelayed(salvar[0], 1500L);
        };

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    long duracao = player.getDuration();

                    if (!posicaoRestaurada[0] &&
                            posicaoSalva > 0L &&
                            (duracao <= 0L || posicaoSalva < duracao - 15_000L)) {
                        posicaoRestaurada[0] = true;
                        player.seekTo(posicaoSalva);
                    } else if (!posicaoRestaurada[0] &&
                            duracao > 0L &&
                            posicaoSalva >= duracao - 15_000L) {
                        posicaoRestaurada[0] = true;
                        prefs.edit().remove(chave).apply();
                    }

                } else if (state == Player.STATE_ENDED) {
                    prefs.edit().remove(chave).apply();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                android.util.Log.e("HAVE_PLAYER", "Falha ao reproduzir: " + titulo, error);
                Toast.makeText(
                        HaveHomeActivity.this,
                        "Não foi possível reproduzir este vídeo. Verifique a conexão.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        player.setMediaItem(MediaItem.fromUri(endereco));
        player.setPlayWhenReady(true);
        player.prepare();

        final boolean[] voltarParaDetalhes = {false};

        dialog.setContentView(raiz);
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK &&
                    event.getAction() == android.view.KeyEvent.ACTION_UP) {
                voltarParaDetalhes[0] = true;
                dialog.dismiss();
                return true;
            }
            return false;
        });

        dialog.setOnDismissListener(d -> {
            if (player.getPlaybackState() != Player.STATE_ENDED &&
                    player.getCurrentPosition() > 0L) {
                prefs.edit().putLong(chave, player.getCurrentPosition()).apply();
            }
            handler.removeCallbacks(salvar[0]);
            playerView.setPlayer(null);
            player.release();


        });

        dialog.show();

        if (dialog.getWindow() != null) {
            android.view.Window janela = dialog.getWindow();
            janela.setLayout(-1, -1);
            janela.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
            janela.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        handler.postDelayed(salvar[0], 1500L);
        playerView.postDelayed(playerView::showController, 300L);
    }

}
