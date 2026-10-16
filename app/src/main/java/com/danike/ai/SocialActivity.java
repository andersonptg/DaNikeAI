package com.danike.ai;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ViewGroup;
import androidx.core.content.FileProvider;
import java.io.File;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.content.Intent;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocialActivity extends Activity {

    private FrameLayout raiz;
    private FrameLayout conteudo;
    private TextView titulo;
    private TextView[] botoes;
    private boolean mensagensAbertas = false;
    private boolean gruposAbertos = false;
private LinearLayout barraInferior;

    private FirebaseFirestore db;
    private ListenerRegistration listenerPerfis;
    private ListenerRegistration listenerSolicitacoes;
    private ListenerRegistration listenerConversas;
    private ListenerRegistration listenerGrupos;
    private ObjectAnimator[] animacoesIndicadores = new ObjectAnimator[6];
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String meuUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(2, 3, 8));

        setContentView(raiz);

        if (meuUid == null || meuUid.trim().isEmpty()) {
            montarInterface();
            selecionar(0);
            iniciarListenerSolicitacoes();
            return;
        }

        db.collection("usuarios")
                .document(meuUid)
                .get()
                .addOnSuccessListener(documento -> {
                    boolean jaViu =
                            documento.exists()
                            && Boolean.TRUE.equals(
                                    documento.getBoolean("conexoesApresentacaoV1")
                            );

                    if (jaViu) {
                        montarInterface();
                        selecionar(0);
                        iniciarListenerSolicitacoes();
                    } else {
                        mostrarApresentacaoConexoes();
                    }
                })
                .addOnFailureListener(e -> {
                    // Se a nuvem falhar, não bloqueia o app.
                    montarInterface();
                    selecionar(0);
                    iniciarListenerSolicitacoes();
                });
    }

    private void mostrarApresentacaoConexoes() {
        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        tela.setGravity(Gravity.CENTER_HORIZONTAL);
        tela.setPadding(
                dp(24),
                dp(30),
                dp(24),
                dp(24)
        );

        GradientDrawable fundo =
                new GradientDrawable();
        fundo.setColor(Color.rgb(3, 5, 12));
        fundo.setCornerRadius(dp(24));
        fundo.setStroke(
                dp(1),
                Color.rgb(35, 75, 125)
        );
        tela.setBackground(fundo);

        TextView logo = texto("🤖", 48, Color.WHITE);
        logo.setGravity(Gravity.CENTER);

        tela.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(75)
                )
        );

        TextView tituloTela =
                texto(
                        "Bem-vindo às Conexões",
                        25,
                        Color.WHITE
                );
        tituloTela.setGravity(Gravity.CENTER);
        tituloTela.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        tela.addView(
                tituloTela,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        TextView mensagem =
                texto(
                        "Conheça pessoas, crie conexões, "
                        + "participe de grupos e converse "
                        + "com quem aceitar sua solicitação.",
                        16,
                        Color.LTGRAY
                );
        mensagem.setGravity(Gravity.CENTER);
        mensagem.setPadding(0, dp(8), 0, dp(18));

        tela.addView(
                mensagem,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(105)
                )
        );

        adicionarRegra(
                tela,
                "🔒",
                "PRIVACIDADE",
                "Seu telefone permanece privado."
        );

        adicionarRegra(
                tela,
                "💬",
                "CONVERSAS",
                "As mensagens ficam disponíveis "
                + "depois que uma conexão for aceita."
        );

        adicionarRegra(
                tela,
                "🤝",
                "RESPEITO",
                "Respeite os outros usuários e "
                + "faça conexões com responsabilidade."
        );

        TextView continuar =
                texto(
                        "CONTINUAR  →",
                        16,
                        Color.WHITE
                );
        continuar.setGravity(Gravity.CENTER);
        continuar.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        continuar.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        GradientDrawable fundoBotao =
                new GradientDrawable();
        fundoBotao.setColor(
                Color.rgb(8, 25, 45)
        );
        fundoBotao.setCornerRadius(dp(18));
        fundoBotao.setStroke(
                dp(1),
                Color.rgb(55, 130, 210)
        );
        continuar.setBackground(fundoBotao);

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
                );
        botaoParams.setMargins(
                0,
                dp(20),
                0,
                0
        );

        tela.addView(
                continuar,
                botaoParams
        );

        continuar.setOnClickListener(v -> {
            db.collection("usuarios")
                    .document(meuUid)
                    .set(
                            java.util.Collections.singletonMap(
                                    "conexoesApresentacaoV1",
                                    true
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                    )
                    .addOnSuccessListener(unused -> {
                        mostrarConfiguracaoFoto();
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(
                                this,
                                "Não foi possível salvar na nuvem.",
                                android.widget.Toast.LENGTH_LONG
                        ).show();
                    });
        });

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );
        params.setMargins(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        raiz.addView(tela, params);
    }

    private void adicionarRegra(
            LinearLayout tela,
            String icone,
            String tituloRegra,
            String textoRegra
    ) {
        LinearLayout bloco =
                new LinearLayout(this);
        bloco.setOrientation(
                LinearLayout.HORIZONTAL
        );
        bloco.setGravity(
                Gravity.CENTER_VERTICAL
        );
        bloco.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        TextView i =
                texto(icone, 25, Color.WHITE);
        i.setGravity(Gravity.CENTER);

        bloco.addView(
                i,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(65)
                )
        );

        LinearLayout textos =
                new LinearLayout(this);
        textos.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView t =
                texto(tituloRegra, 14, Color.WHITE);
        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        TextView d =
                texto(textoRegra, 13, Color.LTGRAY);

        textos.addView(t);
        textos.addView(d);

        bloco.addView(
                textos,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        tela.addView(
                bloco,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(72)
                )
        );
    }


    private static final int FOTO_GALERIA = 8101;
    private static final int FOTO_CAMERA = 8102;
    private static final int PERMISSAO_CAMERA = 8103;

    private Uri fotoSelecionadaUri = null;
    private Uri fotoCameraUri = null;
    private ImageView fotoPreview = null;
    private TextView botaoUsarFoto = null;
    private TextView botaoEntrarGlobal = null;

    private void mostrarConfiguracaoFoto() {
        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setGravity(Gravity.CENTER_HORIZONTAL);
        principal.setPadding(dp(22), dp(24), dp(22), dp(18));

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(5, 7, 14),
                        Color.rgb(8, 12, 22),
                        Color.rgb(4, 5, 10)
                }
        );
        fundo.setCornerRadius(dp(26));
        fundo.setStroke(dp(1), Color.rgb(35, 85, 145));
        principal.setBackground(fundo);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.danike_splash_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams logoParams =
                new LinearLayout.LayoutParams(dp(105), dp(105));
        logoParams.gravity = Gravity.CENTER_HORIZONTAL;
        principal.addView(logo, logoParams);

        TextView titulo = texto(
                "Configure sua foto",
                24,
                Color.WHITE
        );
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titulo.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(-1, -2);
        tituloParams.setMargins(0, dp(4), 0, dp(8));
        principal.addView(titulo, tituloParams);

        TextView descricao = texto(
                "Sua foto ajuda outras pessoas a reconhecerem você nas Conexões. "
                        + "Ela fica vinculada à sua conta e poderá ser alterada depois.",
                14,
                Color.rgb(175, 185, 200)
        );
        descricao.setGravity(Gravity.CENTER);
        descricao.setLineSpacing(0, 1.15f);

        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(-1, -2);
        descParams.setMargins(dp(8), 0, dp(8), dp(18));
        principal.addView(descricao, descParams);

        fotoPreview = new ImageView(this);
        fotoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable circulo =
                new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        circulo.setColor(Color.rgb(12, 18, 30));
        circulo.setStroke(dp(2), Color.rgb(45, 110, 190));

        fotoPreview.setBackground(circulo);
        fotoPreview.setClipToOutline(true);

        TextView mais = texto("+", 42, Color.rgb(100, 190, 255));
        mais.setGravity(Gravity.CENTER);

        FrameLayout fotoContainer = new FrameLayout(this);
        fotoContainer.setBackground(circulo);
        fotoContainer.setClipToOutline(true);
        fotoContainer.addView(
                fotoPreview,
                new FrameLayout.LayoutParams(-1, -1)
        );

        TextView simbolo = texto("+", 42, Color.rgb(100, 190, 255));
        simbolo.setGravity(Gravity.CENTER);
        fotoContainer.addView(
                simbolo,
                new FrameLayout.LayoutParams(-1, -1)
        );

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(dp(150), dp(150));
        fotoParams.gravity = Gravity.CENTER_HORIZONTAL;
        fotoParams.setMargins(0, 0, 0, dp(16));
        principal.addView(fotoContainer, fotoParams);

        TextView aviso = texto(
                "🔒 Sua foto é usada apenas para o seu perfil e suas conexões.",
                12,
                Color.rgb(135, 150, 170)
        );
        aviso.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams avisoParams =
                new LinearLayout.LayoutParams(-1, -2);
        avisoParams.setMargins(dp(8), 0, dp(8), dp(18));
        principal.addView(aviso, avisoParams);

        LinearLayout escolhas = new LinearLayout(this);
        escolhas.setOrientation(LinearLayout.HORIZONTAL);
        escolhas.setGravity(Gravity.CENTER);
        escolhas.setPadding(0, 0, 0, dp(10));

        TextView camera = texto(
                "📷  CÂMERA",
                14,
                Color.WHITE
        );
        configurarBotaoFoto(camera);

        camera.setOnClickListener(v -> abrirCamera());

        TextView galeria = texto(
                "🖼  GALERIA",
                14,
                Color.WHITE
        );
        configurarBotaoFoto(galeria);

        galeria.setOnClickListener(v -> abrirGaleria());

        LinearLayout.LayoutParams escolhaParams =
                new LinearLayout.LayoutParams(0, dp(52), 1);
        escolhaParams.setMargins(0, 0, dp(6), 0);
        escolhas.addView(camera, escolhaParams);

        LinearLayout.LayoutParams escolhaParams2 =
                new LinearLayout.LayoutParams(0, dp(52), 1);
        escolhaParams2.setMargins(dp(6), 0, 0, 0);
        escolhas.addView(galeria, escolhaParams2);

        principal.addView(
                escolhas,
                new LinearLayout.LayoutParams(-1, dp(62))
        );

        LinearLayout acoesFoto = new LinearLayout(this);
        acoesFoto.setOrientation(LinearLayout.HORIZONTAL);
        acoesFoto.setGravity(Gravity.CENTER);
        acoesFoto.setVisibility(View.GONE);

        TextView trocar = texto(
                "✕  TROCAR",
                14,
                Color.WHITE
        );
        configurarBotaoFoto(trocar);

        trocar.setOnClickListener(v -> {
            fotoSelecionadaUri = null;
            fotoCameraUri = null;
            fotoPreview.setImageDrawable(null);
            fotoPreview.setBackground(circulo);
            simbolo.setVisibility(View.VISIBLE);
            acoesFoto.setVisibility(View.GONE);
            escolhas.setVisibility(View.VISIBLE);
            botaoUsarFoto.setVisibility(View.GONE);
            botaoEntrarGlobal.setVisibility(View.GONE);
        });

        botaoUsarFoto = texto(
                "✓  USAR FOTO",
                14,
                Color.WHITE
        );
        configurarBotaoFoto(botaoUsarFoto);
        botaoUsarFoto.setVisibility(View.GONE);

        botaoUsarFoto.setOnClickListener(v -> salvarFotoSelecionada());

        LinearLayout.LayoutParams acaoParams =
                new LinearLayout.LayoutParams(0, dp(52), 1);
        acaoParams.setMargins(0, 0, dp(6), 0);
        acoesFoto.addView(trocar, acaoParams);

        LinearLayout.LayoutParams usarParams =
                new LinearLayout.LayoutParams(0, dp(52), 1);
        usarParams.setMargins(dp(6), 0, 0, 0);
        acoesFoto.addView(botaoUsarFoto, usarParams);

        principal.addView(
                acoesFoto,
                new LinearLayout.LayoutParams(-1, dp(62))
        );

        botaoEntrarGlobal = texto(
                "ENTRAR NO GLOBAL  →",
                15,
                Color.WHITE
        );
        botaoEntrarGlobal.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        botaoEntrarGlobal.setGravity(Gravity.CENTER);
        botaoEntrarGlobal.setVisibility(View.GONE);

        GradientDrawable fundoGlobal = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(20, 90, 155),
                        Color.rgb(25, 45, 95)
                }
        );
        fundoGlobal.setCornerRadius(dp(16));
        fundoGlobal.setStroke(dp(1), Color.rgb(75, 160, 235));
        botaoEntrarGlobal.setBackground(fundoGlobal);

        botaoEntrarGlobal.setOnClickListener(v -> {
            montarInterface();
            selecionar(0);
            iniciarListenerSolicitacoes();
        });

        LinearLayout.LayoutParams globalParams =
                new LinearLayout.LayoutParams(-1, dp(56));
        globalParams.setMargins(0, dp(12), 0, 0);
        principal.addView(botaoEntrarGlobal, globalParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.addView(principal);

        FrameLayout.LayoutParams scrollParams =
                new FrameLayout.LayoutParams(-1, -1);
        scrollParams.setMargins(dp(10), dp(95), dp(10), dp(10));
        raiz.addView(scroll, scrollParams);
    }

    private void configurarBotaoFoto(TextView botao) {
        botao.setGravity(Gravity.CENTER);
        botao.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        botao.setPadding(dp(8), 0, dp(8), 0);

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(10, 18, 32),
                        Color.rgb(8, 12, 22)
                }
        );
        fundo.setCornerRadius(dp(15));
        fundo.setStroke(dp(1), Color.rgb(45, 100, 170));

        botao.setBackground(fundo);
        botao.setElevation(dp(4));
    }

    private void abrirGaleria() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        i.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        try {
            startActivityForResult(i, FOTO_GALERIA);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir a galeria.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void abrirCamera() {
        if (android.os.Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            android.Manifest.permission.CAMERA
                    },
                    PERMISSAO_CAMERA
            );
            return;
        }

        iniciarCamera();
    }

    private void iniciarCamera() {
        try {
            File pasta =
                    getExternalFilesDir(
                            Environment.DIRECTORY_PICTURES
                    );

            if (pasta != null && !pasta.exists()) {
                pasta.mkdirs();
            }

            File arquivo = new File(
                    pasta,
                    "foto_perfil_" + System.currentTimeMillis() + ".jpg"
            );

            fotoCameraUri =
                    FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            arquivo
                    );

            Intent camera =
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            camera.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    fotoCameraUri
            );

            camera.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            );

            camera.setClipData(
                    ClipData.newRawUri(
                            "foto_perfil",
                            fotoCameraUri
                    )
            );

            startActivityForResult(
                    camera,
                    FOTO_CAMERA
            );

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir a câmera.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void mostrarPreviewFoto(Uri uri) {
        if (uri == null || fotoPreview == null) return;

        try {
            InputStream entrada =
                    getContentResolver().openInputStream(uri);

            Bitmap bitmap =
                    BitmapFactory.decodeStream(entrada);

            if (entrada != null) {
                entrada.close();
            }

            if (bitmap == null) {
                Toast.makeText(
                        this,
                        "Não foi possível carregar a foto.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            fotoPreview.setImageBitmap(bitmap);

            if (fotoPreview.getParent() instanceof View) {
                View parent = (View) fotoPreview.getParent();
                parent.setClipToOutline(true);
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Não foi possível visualizar a foto.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void prepararFotoSelecionada(Uri uri) {
        if (uri == null) return;

        fotoSelecionadaUri = uri;

        mostrarPreviewFoto(uri);

        if (fotoPreview != null
                && fotoPreview.getParent() instanceof ViewGroup) {

            ViewGroup grupo =
                    (ViewGroup) fotoPreview.getParent();

            if (grupo.getChildCount() > 1) {
                grupo.getChildAt(1).setVisibility(View.GONE);
            }
        }

        if (botaoUsarFoto != null) {
            botaoUsarFoto.setVisibility(View.VISIBLE);
        }

        View root = conteudo.getRootView();
        if (root != null) {
            // O estado dos botões é controlado pela própria tela.
        }
    }

    private void salvarFotoSelecionada() {
        if (fotoSelecionadaUri == null) {
            Toast.makeText(
                    this,
                    "Escolha uma foto primeiro.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (botaoUsarFoto != null) {
            botaoUsarFoto.setEnabled(false);
            botaoUsarFoto.setText("ENVIANDO...");
        }

        Toast.makeText(
                this,
                "Enviando sua foto...",
                Toast.LENGTH_SHORT
        ).show();

        PerfilFirebase.enviarFoto(
                fotoSelecionadaUri,
                new PerfilFirebase.Callback() {
                    @Override
                    public void sucesso() {
                        runOnUiThread(() -> {
                            if (botaoUsarFoto != null) {
                                botaoUsarFoto.setText("✓ FOTO SALVA");
                                botaoUsarFoto.setEnabled(false);
                            }

                            if (botaoEntrarGlobal != null) {
                                botaoEntrarGlobal.setVisibility(View.VISIBLE);
                            }

                            Toast.makeText(
                                    SocialActivity.this,
                                    "Foto salva no seu perfil.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }

                    @Override
                    public void erro(String mensagem) {
                        runOnUiThread(() -> {
                            if (botaoUsarFoto != null) {
                                botaoUsarFoto.setText("✓  USAR FOTO");
                                botaoUsarFoto.setEnabled(true);
                            }

                            Toast.makeText(
                                    SocialActivity.this,
                                    mensagem != null
                                            ? mensagem
                                            : "Não foi possível salvar a foto.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISSAO_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarCamera();
            } else {
                Toast.makeText(
                        this,
                        "Permissão da câmera não concedida.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == FOTO_GALERIA
                && data != null
                && data.getData() != null) {

            Uri uri = data.getData();

            try {
                getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (Exception ignored) {
            }

            prepararFotoSelecionada(uri);
            return;
        }

        if (requestCode == FOTO_CAMERA
                && fotoCameraUri != null) {

            prepararFotoSelecionada(fotoCameraUri);
        }
    }


    private void montarInterface() {

        LinearLayout topo = new LinearLayout(this);
        topo.setOrientation(LinearLayout.HORIZONTAL);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(16), dp(10), dp(16), dp(10));

        GradientDrawable fundoTopo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 12, 28),
                        Color.rgb(12, 7, 25),
                        Color.rgb(5, 12, 22)
                }
        );
        fundoTopo.setCornerRadius(dp(20));
        fundoTopo.setStroke(dp(1), Color.rgb(45, 100, 180));
        topo.setBackground(fundoTopo);

        TextView voltar = texto("‹", 34, Color.WHITE);
        voltar.setGravity(Gravity.CENTER);
        voltar.setOnClickListener(v -> finish());

        topo.addView(
                voltar,
                new LinearLayout.LayoutParams(dp(50), dp(60))
        );

        titulo = texto("TESTE 6 ABAS", 21, Color.WHITE);
        titulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        topo.addView(
                titulo,
                new LinearLayout.LayoutParams(0, dp(60), 1)
        );

        FrameLayout.LayoutParams topoParams =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(80),
                        Gravity.TOP
                );
        topoParams.setMargins(dp(10), dp(10), dp(10), 0);
        raiz.addView(topo, topoParams);

        conteudo = new FrameLayout(this);

        FrameLayout.LayoutParams conteudoParams =
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                );
        GradientDrawable fundoConteudo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(3, 7, 15),
                        Color.rgb(7, 14, 28),
                        Color.rgb(3, 6, 13)
                }
        );
        fundoConteudo.setCornerRadius(dp(22));
        fundoConteudo.setStroke(dp(1), Color.rgb(35, 105, 180));
        conteudo.setBackground(fundoConteudo);
        conteudo.setPadding(dp(8), dp(8), dp(8), dp(8));

        conteudoParams.setMargins(
                0,
                dp(95),
                0,
                dp(108)
        );
        raiz.addView(conteudo, conteudoParams);

        android.widget.HorizontalScrollView scrollAbas =
                new android.widget.HorizontalScrollView(this);

        scrollAbas.setHorizontalScrollBarEnabled(false);
        scrollAbas.setFillViewport(false);
        scrollAbas.setOverScrollMode(View.OVER_SCROLL_NEVER);

        barraInferior = new LinearLayout(this);
        barraInferior.setOrientation(LinearLayout.HORIZONTAL);
        barraInferior.setGravity(Gravity.CENTER_VERTICAL);
        barraInferior.setPadding(
                dp(7),
                dp(7),
                dp(7),
                dp(7)
        );

        GradientDrawable fundoBarra =
                new GradientDrawable();

        fundoBarra.setColor(
                Color.rgb(5, 8, 17)
        );

        fundoBarra.setCornerRadius(
                dp(22)
        );

        fundoBarra.setStroke(
                dp(1),
                Color.rgb(45, 105, 180)
        );

        barraInferior.setBackground(
                fundoBarra
        );

        botoes = new TextView[]{
                botao("🌎\nGLOBAL"),
                botao("👥\nCONEXÕES"),
                botao("💌\nSOLICITAÇÕES"),
                botao("💬\nMENSAGENS"),
                botao("👥\nGRUPOS"),
                botao("⭕\nSTATUS")
        };

        for (int i = 0; i < botoes.length; i++) {

            final int posicao = i;

            TextView botaoAtual = botoes[i];

            GradientDrawable fundoBotao =
                    new GradientDrawable();

            fundoBotao.setColor(
                    Color.rgb(10, 18, 32)
            );

            fundoBotao.setCornerRadius(
                    dp(16)
            );

            fundoBotao.setStroke(
                    dp(1),
                    Color.rgb(55, 135, 215)
            );

            botaoAtual.setBackground(
                    fundoBotao
            );

            botaoAtual.setTextColor(
                    Color.rgb(225, 235, 250)
            );

            botaoAtual.setTextSize(11);
            botaoAtual.setGravity(Gravity.CENTER);
            botaoAtual.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            botaoAtual.setPadding(
                    dp(8),
                    dp(4),
                    dp(8),
                    dp(4)
            );

            botaoAtual.setMinWidth(dp(108));
            botaoAtual.setMinimumWidth(dp(108));
            botaoAtual.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams botaoParams =
                    new LinearLayout.LayoutParams(
                            dp(108),
                            dp(62)
                    );

            botaoParams.setMargins(
                    dp(4),
                    0,
                    dp(4),
                    0
            );

            FrameLayout recipienteBotao =
                    new FrameLayout(this);

            recipienteBotao.setClipChildren(false);
            recipienteBotao.setClipToPadding(false);

            recipienteBotao.addView(
                    botaoAtual,
                    new FrameLayout.LayoutParams(
                            -1,
                            -1
                    )
            );

            TextView indicador =
                    new TextView(this);

            indicador.setTag(
                    "indicadorNotificacao_" + posicao
            );
            indicador.setVisibility(View.GONE);

            GradientDrawable fundoIndicador =
                    new GradientDrawable();

            fundoIndicador.setShape(
                    GradientDrawable.OVAL
            );

            fundoIndicador.setColor(
                    Color.rgb(255, 35, 65)
            );

            fundoIndicador.setStroke(
                    dp(1),
                    Color.rgb(255, 120, 135)
            );

            indicador.setBackground(
                    fundoIndicador
            );

            indicador.setTag(
                    "indicadorNotificacao_" + posicao
            );

            FrameLayout.LayoutParams indicadorParams =
                    new FrameLayout.LayoutParams(
                            dp(11),
                            dp(11),
                            Gravity.TOP | Gravity.RIGHT
                    );

            indicadorParams.setMargins(
                    0,
                    dp(-3),
                    dp(-3),
                    0
            );

            recipienteBotao.addView(
                    indicador,
                    indicadorParams
            );

            barraInferior.addView(
                    recipienteBotao,
                    botaoParams
            );

            botaoAtual.setOnClickListener(
                    v -> selecionar(posicao)
            );
        }

        scrollAbas.addView(
                barraInferior,
                new android.widget.FrameLayout.LayoutParams(
                        -2,
                        dp(76)
                )
        );

        FrameLayout.LayoutParams barraParams =
                new FrameLayout.LayoutParams(
                        -1,
                        dp(78),
                        Gravity.BOTTOM
                );

        barraParams.setMargins(
                dp(6),
                0,
                dp(6),
                dp(8)
        );

        raiz.addView(
                scrollAbas,
                barraParams
        );

        scrollAbas.setVisibility(View.VISIBLE);
        scrollAbas.bringToFront();
        scrollAbas.setZ(1000f);

    }

    private void atualizarIndicadorNotificacao(
            int indice,
            boolean mostrar
    ) {
        if (botoes == null
                || indice < 0
                || indice >= botoes.length
                || indice >= animacoesIndicadores.length) {
            return;
        }

        android.view.ViewParent parent =
                botoes[indice].getParent();

        if (!(parent instanceof FrameLayout)) {
            return;
        }

        FrameLayout recipiente =
                (FrameLayout) parent;

        View indicador =
                recipiente.findViewWithTag(
                        "indicadorNotificacao_" + indice
                );

        if (indicador == null) {
            return;
        }

        ObjectAnimator pulso =
                animacoesIndicadores[indice];

        if (mostrar) {
            indicador.setVisibility(View.VISIBLE);

            if (pulso == null) {
                pulso =
                        ObjectAnimator.ofPropertyValuesHolder(
                                indicador,
                                PropertyValuesHolder.ofFloat(
                                        View.SCALE_X,
                                        0.75f, 1.15f, 0.75f
                                ),
                                PropertyValuesHolder.ofFloat(
                                        View.SCALE_Y,
                                        0.75f, 1.15f, 0.75f
                                ),
                                PropertyValuesHolder.ofFloat(
                                        View.ALPHA,
                                        0.55f, 1.0f, 0.55f
                                )
                        );

                pulso.setDuration(900);
                pulso.setRepeatCount(
                        ValueAnimator.INFINITE
                );
                pulso.setInterpolator(
                        new AccelerateDecelerateInterpolator()
                );

                animacoesIndicadores[indice] = pulso;
            }

            if (!pulso.isStarted()) {
                pulso.start();
            }

        } else {
            if (pulso != null) {
                pulso.cancel();
            }

            indicador.setVisibility(View.GONE);
        }
    }

    private void iniciarListenerSolicitacoes() {

        if (meuUid == null || meuUid.trim().isEmpty()) {
            return;
        }

        if (listenerSolicitacoes != null) {
            listenerSolicitacoes.remove();
        }

        listenerSolicitacoes =
                db.collection("convites")
                        .document(meuUid)
                        .collection("recebidos")
                        .whereEqualTo("status", "pendente")
                        .addSnapshotListener((snapshots, erro) -> {

                            if (erro != null || snapshots == null) {
                                atualizarBadgeSolicitacoes(0);
                                return;
                            }

                            atualizarBadgeSolicitacoes(
                                    snapshots.size()
                            );
                        });
    }

    private void atualizarBadgeSolicitacoes(int quantidade) {
        atualizarIndicadorNotificacao(
                2,
                quantidade > 0
        );
    }

    private void selecionar(int posicao) {
        mensagensAbertas = (posicao == 3);
        gruposAbertos = (posicao == 4);
        for (int i = 0; i < botoes.length; i++) {
            GradientDrawable fundo = new GradientDrawable();
            fundo.setCornerRadius(dp(18));

            if (i == posicao) {
                fundo.setColor(Color.rgb(18, 42, 78));
                fundo.setStroke(dp(2), Color.rgb(70, 190, 255));
                botoes[i].setTextColor(Color.WHITE);

                botoes[i].animate()
                        .scaleX(1.04f)
                        .scaleY(1.04f)
                        .alpha(1.0f)
                        .setDuration(220)
                        .start();

            } else {
                fundo.setColor(Color.TRANSPARENT);
                fundo.setStroke(dp(1), Color.rgb(35, 75, 120));
                botoes[i].setTextColor(Color.rgb(145, 160, 185));

                botoes[i].animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(0.82f)
                        .setDuration(220)
                        .start();
            }

            botoes[i].setBackground(fundo);
        }

        if (posicao == 0) {
            titulo.setText("GLOBAL");
            pararListenerConversas();
            mostrarGlobal();

        } else if (posicao == 1) {
            titulo.setText("CONEXÕES");
            pararListenerConversas();
            mostrarConexao();

        } else if (posicao == 2) {
            atualizarIndicadorNotificacao(2, false);
            titulo.setText("SOLICITAÇÕES");
            pararListenerConversas();
            pararListenerPerfis();
            mostrarSolicitacoes();

        } else if (posicao == 3) {
            atualizarIndicadorNotificacao(3, false);
        titulo.setText("MENSAGENS");
            pararListenerPerfis();
            iniciarListenerConversas();
        } else if (posicao == 4) {
            atualizarIndicadorNotificacao(4, false);
        iniciarListenerGrupos();
        titulo.setText("GRUPOS");
            pararListenerConversas();
            pararListenerPerfis();
            mostrarGrupos();

        } else if (posicao == 5) {
            titulo.setText("STATUS");
            pararListenerConversas();
            pararListenerPerfis();
            mostrarTelaInicialStatus();
        }
    }

    private void mostrarTelaInicialStatus() {
        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(16), dp(14), dp(16), dp(18));

        TextView cabecalho = texto(
                "STATUS",
                15,
                Color.rgb(120, 210, 255)
        );
        cabecalho.setTypeface(null, android.graphics.Typeface.BOLD);
        principal.addView(cabecalho);

        TextView meuTitulo = texto(
                "MEU STATUS",
                12,
                Color.rgb(170, 180, 195)
        );
        meuTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        meuTitulo.setPadding(0, dp(16), 0, dp(8));
        principal.addView(meuTitulo);

        HorizontalScrollView rolagem =
                new HorizontalScrollView(this);
        rolagem.setHorizontalScrollBarEnabled(false);
        rolagem.setClipToPadding(false);

        LinearLayout cards =
                new LinearLayout(this);
        cards.setOrientation(LinearLayout.HORIZONTAL);
        cards.setPadding(0, 0, dp(8), 0);

        LinearLayout meuCard =
                new LinearLayout(this);
        meuCard.setOrientation(LinearLayout.VERTICAL);
        meuCard.setGravity(Gravity.CENTER);
        meuCard.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable fundoMeuCard =
                new GradientDrawable();
        fundoMeuCard.setColor(Color.rgb(9, 15, 24));
        fundoMeuCard.setCornerRadius(dp(22));
        fundoMeuCard.setStroke(
                dp(1),
                Color.rgb(0, 210, 255)
        );
        meuCard.setBackground(fundoMeuCard);

        TextView circulo =
                texto("+", 38, Color.WHITE);
        circulo.setGravity(Gravity.CENTER);
        circulo.setTypeface(null, android.graphics.Typeface.BOLD);

        GradientDrawable fundoCirculo =
                new GradientDrawable();
        fundoCirculo.setShape(GradientDrawable.OVAL);
        fundoCirculo.setColor(Color.rgb(18, 30, 43));
        fundoCirculo.setStroke(
                dp(3),
                Color.rgb(0, 210, 255)
        );
        circulo.setBackground(fundoCirculo);

        LinearLayout.LayoutParams lpCirculo =
                new LinearLayout.LayoutParams(
                        dp(112),
                        dp(112)
                );
        meuCard.addView(circulo, lpCirculo);

        TextView adicionar =
                texto(
                        "Adicionar status",
                        13,
                        Color.WHITE
                );
        adicionar.setGravity(Gravity.CENTER);
        adicionar.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        adicionar.setPadding(0, dp(10), 0, 0);
        meuCard.addView(adicionar);

        LinearLayout.LayoutParams lpMeuCard =
                new LinearLayout.LayoutParams(
                        dp(158),
                        dp(205)
                );
        lpMeuCard.setMargins(0, 0, dp(10), 0);
        cards.addView(meuCard, lpMeuCard);

        TextView dica =
                texto(
                        "Publique uma foto, vídeo ou texto\\npara compartilhar com suas conexões.",
                        12,
                        Color.rgb(150, 160, 175)
                );
        dica.setGravity(Gravity.CENTER);
        dica.setPadding(dp(8), dp(8), dp(8), dp(8));

        GradientDrawable fundoDica =
                new GradientDrawable();
        fundoDica.setColor(Color.rgb(7, 11, 18));
        fundoDica.setCornerRadius(dp(18));
        fundoDica.setStroke(
                dp(1),
                Color.rgb(45, 65, 85)
        );
        dica.setBackground(fundoDica);

        LinearLayout.LayoutParams lpDica =
                new LinearLayout.LayoutParams(
                        dp(220),
                        dp(205)
                );
        lpDica.setMargins(0, 0, dp(10), 0);
        cards.addView(dica, lpDica);

        rolagem.addView(
                cards,
                new HorizontalScrollView.LayoutParams(
                        -2,
                        dp(205)
                )
        );

        principal.addView(
                rolagem,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(215)
                )
        );

        TextView conexoes =
                texto(
                        "STATUS DAS CONEXÕES",
                        13,
                        Color.rgb(120, 210, 255)
                );
        conexoes.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        conexoes.setPadding(0, dp(18), 0, dp(8));
        principal.addView(conexoes);

        LinearLayout listaStatusConexoes =
                new LinearLayout(this);
        listaStatusConexoes.setOrientation(
                LinearLayout.HORIZONTAL
        );
        listaStatusConexoes.setPadding(
                0,
                dp(4),
                0,
                dp(8)
        );

        TextView vazio =
                texto(
                        "Nenhum status novo por enquanto.",
                        13,
                        Color.rgb(145, 155, 168)
                );
        vazio.setPadding(dp(2), 0, 0, 0);

        listaStatusConexoes.addView(
                vazio,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        principal.addView(
                listaStatusConexoes,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(210)
                )
        );

        carregarStatusDasConexoes(listaStatusConexoes);

        adicionar.setOnClickListener(v ->
                publicarStatusTexto()
        );

        circulo.setOnClickListener(v ->
                publicarStatusTexto()
        );

        conteudo.addView(
                principal,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );
    }


    private void carregarStatusDasConexoes(LinearLayout listaStatusConexoes) {
        if (meuUid == null || meuUid.trim().isEmpty()) {
            return;
        }

        db.collection("conexoes")
                .document(meuUid)
                .collection("usuarios")
                .get()
                .addOnSuccessListener(conexoes -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    for (DocumentSnapshot conexao : conexoes.getDocuments()) {

                        String uidConexao = conexao.getId();

                        if (uidConexao == null
                                || uidConexao.trim().isEmpty()
                                || uidConexao.equals(meuUid)) {
                            continue;
                        }

                        db.collection("status")
                                .whereEqualTo("autorUid", uidConexao)
                                .whereGreaterThan(
                                        "expiraEm",
                                        System.currentTimeMillis()
                                )
                                .get()
                                .addOnSuccessListener(status -> {

                                    if (isFinishing() || isDestroyed()) {
                                        return;
                                    }

                                    if (status == null || status.isEmpty()) {
                                        return;
                                    }

                                    DocumentSnapshot statusAtivo =
                                            status.getDocuments().get(0);

                                    db.collection("perfis_publicos")
                                            .document(uidConexao)
                                            .get()
                                            .addOnSuccessListener(perfil -> {

                                                if (isFinishing() || isDestroyed()) {
                                                    return;
                                                }

                                                String nome =
                                                        perfil.getString("nome");

                                                if (nome == null || nome.trim().isEmpty()) {
                                                    nome = "Conexão";
                                                }

                                                TextView card =
                                                        texto(
                                                                "● " + nome,
                                                                12,
                                                                Color.WHITE
                                                        );

                                                card.setGravity(Gravity.CENTER);
                                                card.setTypeface(
                                                        null,
                                                        android.graphics.Typeface.BOLD
                                                );
                                                card.setPadding(
                                                        dp(8),
                                                        dp(8),
                                                        dp(8),
                                                        dp(8)
                                                );

                                                GradientDrawable fundo =
                                                        new GradientDrawable();

                                                fundo.setColor(
                                                        Color.rgb(9, 15, 24)
                                                );
                                                fundo.setCornerRadius(dp(22));
                                                fundo.setStroke(
                                                        dp(2),
                                                        Color.rgb(0, 210, 255)
                                                );

                                                card.setBackground(fundo);

                                                final String nomeStatus = nome;

                                                card.setOnClickListener(v ->
                                                        Toast.makeText(
                                                                this,
                                                                "Status de " + nomeStatus,
                                                                Toast.LENGTH_SHORT
                                                        ).show()
                                                );

                                                if (listaStatusConexoes != null) {
                                                    listaStatusConexoes.addView(
                                                            card,
                                                            new LinearLayout.LayoutParams(
                                                                    dp(145),
                                                                    dp(190)
                                                            )
                                                    );
                                                }
                                            });
                                });
                    }
                });
    }


    private void publicarStatusTexto() {
        if (meuUid == null || meuUid.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Faça login novamente para publicar.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        final EditText entrada = new EditText(this);
        entrada.setHint("Escreva seu status...");
        entrada.setTextColor(Color.WHITE);
        entrada.setHintTextColor(Color.rgb(120, 135, 150));
        entrada.setTextSize(16);
        entrada.setGravity(Gravity.TOP | Gravity.START);
        entrada.setPadding(dp(16), dp(14), dp(16), dp(14));

        GradientDrawable fundoEntrada = new GradientDrawable();
        fundoEntrada.setColor(Color.rgb(9, 15, 24));
        fundoEntrada.setCornerRadius(dp(18));
        fundoEntrada.setStroke(
                dp(1),
                Color.rgb(0, 210, 255)
        );
        entrada.setBackground(fundoEntrada);

        int limite = 500;

        TextView contador = texto(
                "0/" + limite,
                11,
                Color.rgb(120, 135, 150)
        );
        contador.setGravity(Gravity.RIGHT);

        entrada.addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        contador.setText(
                                s.length() + "/" + limite
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {}
                }
        );

        LinearLayout caixa = new LinearLayout(this);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setPadding(dp(4), dp(4), dp(4), 0);
        caixa.addView(
                entrada,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(150)
                )
        );
        caixa.addView(
                contador,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        new android.app.AlertDialog.Builder(this)
                .setTitle("NOVO STATUS")
                .setView(caixa)
                .setNegativeButton(
                        "CANCELAR",
                        null
                )
                .setPositiveButton(
                        "PUBLICAR",
                        null
                )
                .create();

        android.app.AlertDialog dialog =
                new android.app.AlertDialog.Builder(this)
                        .setTitle("NOVO STATUS")
                        .setView(caixa)
                        .setNegativeButton(
                                "CANCELAR",
                                null
                        )
                        .setPositiveButton(
                                "PUBLICAR",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {
            android.widget.Button publicar =
                    dialog.getButton(
                            android.app.AlertDialog.BUTTON_POSITIVE
                    );

            publicar.setOnClickListener(v -> {

                String textoStatus =
                        entrada.getText()
                                .toString()
                                .trim();

                if (textoStatus.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Escreva algo para publicar.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                if (textoStatus.length() > limite) {
                    Toast.makeText(
                            this,
                            "O Status pode ter até 500 caracteres.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                publicar.setEnabled(false);
                publicar.setText("PUBLICANDO...");

                Map<String, Object> dados =
                        StatusModelo.novo(
                                meuUid,
                                "mensagem",
                                textoStatus,
                                "",
                                "",
                                "padrao",
                                null
                        );

                db.collection("status")
                        .add(dados)
                        .addOnSuccessListener(documento -> {
                            dialog.dismiss();

                            Toast.makeText(
                                    this,
                                    "Status publicado.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            mostrarTelaInicialStatus();
                        })
                        .addOnFailureListener(e -> {
                            publicar.setEnabled(true);
                            publicar.setText("PUBLICAR");

                            Toast.makeText(
                                    this,
                                    "Não foi possível publicar o Status.",
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });
        });

        dialog.show();
    }

    private void mostrarGlobal() {
        pararListenerPerfis();
        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(18), dp(18), dp(18), dp(12));

        TextView subtitulo = texto(
            "🌎 PESSOAS PARA CONHECER",
            14,
            Color.rgb(100, 190, 255)
        );
        subtitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        principal.addView(
            subtitulo,
            new LinearLayout.LayoutParams(-1, dp(35))
        );

        TextView explicacao = texto(
            "Pessoas que ainda não fazem parte das suas conexões.",
            13,
            Color.rgb(135, 150, 175)
        );
        explicacao.setPadding(0, 0, 0, dp(12));

        principal.addView(
            explicacao,
            new LinearLayout.LayoutParams(-1, dp(42))
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(lista);

        principal.addView(
            scroll,
            new LinearLayout.LayoutParams(-1, 0, 1)
        );

        conteudo.addView(principal);

        if (meuUid == null || meuUid.trim().isEmpty()) {
            TextView login = texto(
                "Faça login para descobrir novas pessoas.",
                15,
                Color.rgb(145, 155, 175)
            );
            login.setGravity(Gravity.CENTER);

            lista.addView(
                login,
                new LinearLayout.LayoutParams(-1, dp(160))
            );
            return;
        }

        TextView carregando = texto(
            "🌎 Procurando pessoas...",
            15,
            Color.rgb(145, 155, 175)
        );
        carregando.setGravity(Gravity.CENTER);

        lista.addView(
            carregando,
            new LinearLayout.LayoutParams(-1, dp(100))
        );

        db.collection("perfis_publicos")
            .get()
            .addOnSuccessListener(perfis -> {

                lista.removeAllViews();

                if (perfis == null || perfis.isEmpty()) {
                    TextView vazio = texto(
                        "🌎\n\n" +
                        "Nenhuma pessoa nova encontrada.",
                        15,
                        Color.rgb(145, 155, 175)
                    );
                    vazio.setGravity(Gravity.CENTER);

                    lista.addView(
                        vazio,
                        new LinearLayout.LayoutParams(-1, dp(180))
                    );
                    return;
                }

                db.collection("conexoes")
                    .document(meuUid)
                    .collection("usuarios")
                    .get()
                    .addOnSuccessListener(conexoes -> {

                        java.util.HashSet<String> bloqueados =
                            new java.util.HashSet<>();

                        bloqueados.add(meuUid);

                        if (conexoes != null) {
                            for (DocumentSnapshot conexao :
                                    conexoes.getDocuments()) {

                                String uid = conexao.getId();

                                if (uid != null &&
                                    !uid.trim().isEmpty()) {
                                    bloqueados.add(uid);
                                }
                            }
                        }

                        carregarConvitesGlobal(
                            perfis,
                            bloqueados,
                            lista
                        );
                    })
                    .addOnFailureListener(e -> {

                        lista.removeAllViews();

                        TextView erro = texto(
                            "Não foi possível verificar suas conexões.",
                            14,
                            Color.rgb(220, 130, 140)
                        );
                        erro.setGravity(Gravity.CENTER);

                        lista.addView(
                            erro,
                            new LinearLayout.LayoutParams(-1, dp(160))
                        );
                    });
            })
            .addOnFailureListener(e -> {

                lista.removeAllViews();

                TextView erro = texto(
                    "Não foi possível carregar o Global.",
                    14,
                    Color.rgb(220, 130, 140)
                );
                erro.setGravity(Gravity.CENTER);

                lista.addView(
                    erro,
                    new LinearLayout.LayoutParams(-1, dp(160))
                );
            });
    }

    private void carregarConvitesGlobal(
        com.google.firebase.firestore.QuerySnapshot perfis,
        java.util.HashSet<String> bloqueados,
        LinearLayout lista
    ) {
        db.collection("convites")
            .document(meuUid)
            .collection("recebidos")
            .get()
            .addOnSuccessListener(recebidos -> {
                if (recebidos != null) {
                    for (DocumentSnapshot convite : recebidos.getDocuments()) {
                        String uid = convite.getId();
                        String status = convite.getString("status");

                        if (uid == null || uid.trim().isEmpty()) {
                            continue;
                        }

                        if ("pendente".equals(status)
                                || "aceito".equals(status)) {
                            bloqueados.add(uid);
                        }

                        if ("aceito".equals(status)) {
                            criarConexao(uid, null, null);
                        }
                    }
                }

                verificarConvitesEnviados(perfis, 0, bloqueados, lista);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e(
                    "DaNikeAI",
                    "Falha ao consultar convites recebidos",
                    e
                );

                lista.removeAllViews();
                TextView erro = texto(
                    "Não foi possível verificar suas solicitações.\n" +
                    "Tente novamente.",
                    14,
                    Color.rgb(220, 130, 140)
                );
                erro.setGravity(Gravity.CENTER);
                lista.addView(
                    erro,
                    new LinearLayout.LayoutParams(-1, dp(160))
                );
            });
    }

    private void verificarConvitesEnviados(
        com.google.firebase.firestore.QuerySnapshot perfis,
        int indice,
        java.util.HashSet<String> bloqueados,
        LinearLayout lista
    ) {
        if (indice >= perfis.size()) {
            adicionarPessoasGlobal(perfis, bloqueados, lista);
            return;
        }

        DocumentSnapshot perfil = perfis.getDocuments().get(indice);
        String uidDestino = perfil.getId();

        if (uidDestino == null
                || uidDestino.trim().isEmpty()
                || uidDestino.equals(meuUid)) {
            verificarConvitesEnviados(
                perfis, indice + 1, bloqueados, lista
            );
            return;
        }

        // Consulta somente o convite enviado a este perfil.
        // O documento fica em /convites/{destino}/recebidos/{remetente}.
        db.collection("convites")
            .document(uidDestino)
            .collection("recebidos")
            .document(meuUid)
            .get()
            .addOnSuccessListener(convite -> {
                String status = convite.getString("status");

                if ("pendente".equals(status)
                        || "aceito".equals(status)) {
                    bloqueados.add(uidDestino);
                }

                if ("aceito".equals(status)) {
                    // Recupera o registro na lista da conta atual.
                    criarConexao(uidDestino, null, null);
                }

                verificarConvitesEnviados(
                    perfis, indice + 1, bloqueados, lista
                );
            })
            .addOnFailureListener(e -> {
                android.util.Log.e(
                    "DaNikeAI",
                    "Falha ao verificar convite para " + uidDestino,
                    e
                );

                // Continua carregando o Global mesmo se um convite falhar.
                verificarConvitesEnviados(
                    perfis, indice + 1, bloqueados, lista
                );
            });
    }

    private void adicionarPessoasGlobal(
        com.google.firebase.firestore.QuerySnapshot perfis,
        java.util.HashSet<String> bloqueados,
        LinearLayout lista
    ) {
        boolean encontrou = false;

        for (DocumentSnapshot perfil : perfis.getDocuments()) {

            String uid = perfil.getId();

            if (uid == null ||
                uid.trim().isEmpty() ||
                uid.equals(meuUid) ||
                bloqueados.contains(uid)) {
                continue;
            }

            // Perfis marcados como privados nao aparecem no GLOBAL.
            Boolean publico = perfil.getBoolean("perfilPublico");
            if (Boolean.FALSE.equals(publico)) {
                continue;
            }

            // Exibe apenas perfis com atividade nas ultimas cinco horas.
            com.google.firebase.Timestamp atividade =
                    perfil.getTimestamp("ultimoAcesso");

            if (atividade == null) {
                continue;
            }

            long idadeMs = System.currentTimeMillis()
                    - atividade.toDate().getTime();

            if (idadeMs < 0 || idadeMs > 5L * 60L * 60L * 1000L) {
                continue;
            }

            encontrou = true;

            adicionarCardGlobal(
                lista,
                uid,
                perfil
            );
        }

        if (!encontrou) {
            TextView vazio = texto(
                "🌎\n\n" +
                "Você já está conectado com todas as pessoas disponíveis.",
                15,
                Color.rgb(145, 155, 175)
            );

            vazio.setGravity(Gravity.CENTER);

            lista.addView(
                vazio,
                new LinearLayout.LayoutParams(-1, dp(190))
            );
        }
    }

    private void adicionarCardGlobal(
            LinearLayout lista,
            String uidPessoa,
            DocumentSnapshot perfil
    ) {
        String nome = perfil.getString("nome");
        String fotoUrl = perfil.getString("fotoUrl");
        Boolean onlineValor = perfil.getBoolean("online");

        boolean online = Boolean.TRUE.equals(onlineValor);

        if (nome == null || nome.trim().isEmpty()) {
            nome = "Usuário";
        }

        final String nomeFinal = nome;
        final String fotoFinal =
                fotoUrl == null ? "" : fotoUrl;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 16, 30),
                        Color.rgb(15, 10, 28),
                        Color.rgb(7, 18, 30)
                }
        );

        fundo.setCornerRadius(dp(20));
        fundo.setStroke(
                dp(1),
                online
                        ? Color.rgb(45, 145, 210)
                        : Color.rgb(55, 70, 95)
        );

        card.setBackground(fundo);
        card.setElevation(dp(5));

        // FOTO REDONDA
        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable fundoFoto =
                new GradientDrawable();

        fundoFoto.setShape(
                GradientDrawable.OVAL
        );

        fundoFoto.setColor(
                Color.rgb(20, 30, 48)
        );

        fundoFoto.setStroke(
                dp(2),
                online
                        ? Color.rgb(65, 190, 125)
                        : Color.rgb(75, 90, 115)
        );

        foto.setBackground(fundoFoto);
        foto.setClipToOutline(true);

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(
                        dp(58),
                        dp(58)
                );

        fotoParams.setMargins(
                0,
                0,
                dp(12),
                0
        );

        card.addView(foto, fotoParams);

        // ÁREA CENTRAL
        LinearLayout centro =
                new LinearLayout(this);

        centro.setOrientation(
                LinearLayout.VERTICAL
        );

        centro.setGravity(
                Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams centroParams =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        centroParams.setMargins(
                0,
                0,
                dp(8),
                0
        );

        TextView nomeTexto = texto(
                nomeFinal,
                16,
                Color.WHITE
        );

        nomeTexto.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        nomeTexto.setMaxLines(2);
        nomeTexto.setEllipsize(null);
        nomeTexto.setGravity(
                Gravity.CENTER_VERTICAL
        );

        centro.addView(
                nomeTexto,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        TextView status = texto(
                online
                        ? "● ONLINE"
                        : "● OFFLINE",
                11,
                online
                        ? Color.rgb(70, 220, 145)
                        : Color.rgb(220, 90, 100)
        );

        status.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        centro.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(20)
                )
        );

        card.addView(
                centro,
                centroParams
        );

        // BOTÃO ADICIONAR
        TextView adicionar =
                botaoAcaoSolicitacao(
                        "＋ ADICIONAR",
                        Color.rgb(55, 155, 255)
                );

        adicionar.setSingleLine(true);
        adicionar.setGravity(Gravity.CENTER);

        adicionar.setOnClickListener(v -> {
            adicionar.setEnabled(false);

            adicionar.setText(
                    "✓ SOLICITADO"
            );

            enviarConvite(
                    uidPessoa,
                    nomeFinal
            );
        });

        LinearLayout.LayoutParams adicionarParams =
                new LinearLayout.LayoutParams(
                        dp(112),
                        dp(42)
                );

        card.addView(
                adicionar,
                adicionarParams
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(84)
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        lista.addView(
                card,
                cardParams
        );

        if (fotoFinal != null
                && !fotoFinal.trim().isEmpty()) {

            carregarImagem(
                    foto,
                    fotoFinal
            );
        }
    }

    private void mostrarSolicitacoes() {

        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(16), dp(16), dp(16), dp(12));

        TextView subtitulo = texto(
                "NOVAS CONEXÕES",
                14,
                Color.rgb(100, 190, 255)
        );

        subtitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        principal.addView(
                subtitulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(40)
                )
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(lista);

        principal.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        conteudo.addView(principal);

        if (meuUid == null || meuUid.trim().isEmpty()) {

            mostrarMensagem(
                    "Faça login para visualizar suas solicitações."
            );

            return;
        }

        db.collection("convites")
                .document(meuUid)
                .collection("recebidos")
                .whereEqualTo("status", "pendente")
                .addSnapshotListener((snapshots, erro) -> {

                    if (erro != null) {

                        lista.removeAllViews();

                        TextView erroTexto = texto(
                                "Não foi possível carregar suas solicitações.",
                                15,
                                Color.rgb(190, 200, 220)
                        );

                        erroTexto.setGravity(Gravity.CENTER);

                        lista.addView(
                                erroTexto,
                                new LinearLayout.LayoutParams(
                                        -1,
                                        dp(180)
                                )
                        );

                        return;
                    }

                    lista.removeAllViews();

                    if (snapshots == null || snapshots.isEmpty()) {

                        TextView vazio = texto(
                                "💌\\n\\nNenhuma solicitação no momento.",
                                17,
                                Color.rgb(145, 160, 185)
                        );

                        vazio.setGravity(Gravity.CENTER);

                        lista.addView(
                                vazio,
                                new LinearLayout.LayoutParams(
                                        -1,
                                        dp(220)
                                )
                        );

                        return;
                    }

                    for (DocumentSnapshot convite : snapshots.getDocuments()) {

                        String uidRemetente =
                                convite.getString("uidRemetente");

                        if (uidRemetente == null ||
                                uidRemetente.trim().isEmpty()) {

                            uidRemetente = convite.getId();
                        }

                        adicionarCardSolicitacao(
                                lista,
                                convite.getId(),
                                uidRemetente
                        );
                    }
                });
    }

    private void adicionarCardSolicitacao(
            LinearLayout lista,
            String conviteId,
            String uidRemetente
    ) {

        db.collection("perfis_publicos")
                .document(uidRemetente)
                .get()
                .addOnSuccessListener(doc -> {

                    String nome = doc.getString("nome");
                    String fotoUrl = doc.getString("fotoUrl");

                    if (nome == null || nome.trim().isEmpty()) {
                        nome = "Usuário";
                    }

                    final String nomeFinal = nome;
                    final String fotoFinal =
                            fotoUrl == null ? "" : fotoUrl;

                    LinearLayout card = new LinearLayout(this);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setPadding(
                            dp(16),
                            dp(16),
                            dp(16),
                            dp(14)
                    );

                    GradientDrawable fundo = new GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            new int[]{
                                    Color.rgb(9, 17, 34),
                                    Color.rgb(18, 9, 35),
                                    Color.rgb(7, 19, 31)
                            }
                    );

                    fundo.setCornerRadius(dp(24));
                    fundo.setStroke(
                            dp(1),
                            Color.rgb(70, 150, 255)
                    );

                    card.setBackground(fundo);
                    card.setElevation(dp(8));

                    LinearLayout topoCard =
                            new LinearLayout(this);

                    topoCard.setOrientation(
                            LinearLayout.HORIZONTAL
                    );

                    topoCard.setGravity(
                            Gravity.CENTER_VERTICAL
                    );

                    ImageView foto =
                            new ImageView(this);

                    foto.setScaleType(
                            ImageView.ScaleType.CENTER_CROP
                    );

                    GradientDrawable fundoFoto =
                            new GradientDrawable();

                    fundoFoto.setShape(
                            GradientDrawable.OVAL
                    );

                    fundoFoto.setColor(
                            Color.rgb(20, 30, 48)
                    );

                    fundoFoto.setStroke(
                            dp(2),
                            Color.rgb(75, 175, 255)
                    );

                    foto.setBackground(fundoFoto);
                    foto.setClipToOutline(true);

                    topoCard.addView(
                            foto,
                            new LinearLayout.LayoutParams(
                                    dp(64),
                                    dp(64)
                            )
                    );

                    LinearLayout textos =
                            new LinearLayout(this);

                    textos.setOrientation(
                            LinearLayout.VERTICAL
                    );

                    textos.setPadding(
                            dp(12),
                            0,
                            0,
                            0
                    );

                    TextView tituloSolicitacao =
                            texto(
                                    "NOVA SOLICITAÇÃO",
                                    13,
                                    Color.rgb(80, 185, 255)
                            );

                    tituloSolicitacao.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );

                    TextView nomeTexto =
                            texto(
                                    nomeFinal,
                                    18,
                                    Color.WHITE
                            );

                    nomeTexto.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );

                    TextView descricao =
                            texto(
                                    "● Quer se conectar com você",
                                    13,
                                    Color.rgb(155, 175, 200)
                            );

                    textos.addView(tituloSolicitacao);
                    textos.addView(nomeTexto);
                    textos.addView(descricao);

                    topoCard.addView(
                            textos,
                            new LinearLayout.LayoutParams(
                                    0,
                                    -2,
                                    1
                            )
                    );

                    card.addView(
                            topoCard,
                            new LinearLayout.LayoutParams(
                                    -1,
                                    dp(78)
                            )
                    );

                    LinearLayout acoes =
                            new LinearLayout(this);

                    acoes.setOrientation(
                            LinearLayout.HORIZONTAL
                    );

                    acoes.setGravity(Gravity.CENTER);

                    TextView recusar =
                            botaoAcaoSolicitacao(
                                    "✕  RECUSAR",
                                    Color.rgb(180, 65, 95)
                            );

                    TextView aceitar =
                            botaoAcaoSolicitacao(
                                    "✓  ACEITAR",
                                    Color.rgb(45, 150, 105)
                            );

                    recusar.setOnClickListener(v ->
                            responderSolicitacao(
                                    uidRemetente,
                                    false,
                                    card
                            )
                    );

                    aceitar.setOnClickListener(v ->
                            responderSolicitacao(
                                    uidRemetente,
                                    true,
                                    card
                            )
                    );

                    acoes.addView(
                            recusar,
                            new LinearLayout.LayoutParams(
                                    0,
                                    dp(46),
                                    1
                            )
                    );

                    LinearLayout.LayoutParams aceitarParams =
                            new LinearLayout.LayoutParams(
                                    0,
                                    dp(46),
                                    1
                            );

                    aceitarParams.setMargins(
                            dp(8),
                            0,
                            0,
                            0
                    );

                    acoes.addView(
                            aceitar,
                            aceitarParams
                    );

                    card.addView(
                            acoes,
                            new LinearLayout.LayoutParams(
                                    -1,
                                    dp(46)
                            )
                    );

                    LinearLayout.LayoutParams cardParams =
                            new LinearLayout.LayoutParams(
                                    -1,
                                    dp(144)
                            );

                    cardParams.setMargins(
                            0,
                            0,
                            0,
                            dp(12)
                    );

                    lista.addView(card, cardParams);

                    if (fotoFinal != null &&
                            !fotoFinal.trim().isEmpty()) {

                        carregarImagem(
                                foto,
                                fotoFinal
                        );
                    }
                });
    }

    private TextView botaoAcaoSolicitacao(
            String textoBotao,
            int cor
    ) {

        TextView botao = texto(
                textoBotao,
                12,
                Color.WHITE
        );

        botao.setGravity(Gravity.CENTER);
        botao.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable fundo =
                new GradientDrawable();

        fundo.setCornerRadius(dp(16));
        fundo.setColor(
                Color.argb(
                        45,
                        Color.red(cor),
                        Color.green(cor),
                        Color.blue(cor)
                )
        );

        fundo.setStroke(dp(1), cor);

        botao.setBackground(fundo);

        return botao;
    }

    private void responderSolicitacao(
            String uidRemetente,
            boolean aceitar,
            View card
    ) {
        if (meuUid == null || meuUid.trim().isEmpty()
                || uidRemetente == null || uidRemetente.trim().isEmpty()) {
            return;
        }

        String novoStatus = aceitar ? "aceito" : "recusado";
        Map<String, Object> atualizacao = new HashMap<>();
        atualizacao.put("status", novoStatus);
        atualizacao.put("respondidoEm", FieldValue.serverTimestamp());

        db.collection("convites")
                .document(meuUid)
                .collection("recebidos")
                .document(uidRemetente)
                .set(atualizacao, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    if (!aceitar) {
                        Toast.makeText(
                                this,
                                "Solicitação recusada.",
                                Toast.LENGTH_SHORT
                        ).show();
                        card.setVisibility(View.GONE);
                        return;
                    }

                    criarConexao(
                            uidRemetente,
                            () -> {
                                Toast.makeText(
                                        this,
                                        "Conexão aceita!",
                                        Toast.LENGTH_SHORT
                                ).show();
                                card.setVisibility(View.GONE);
                            },
                            () -> Toast.makeText(
                                    this,
                                    "Convite aceito, mas não foi possível salvar a conexão. Tente novamente.",
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível responder à solicitação.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void criarConexao(String outroUid) {
        criarConexao(outroUid, null, null);
    }

    private void criarConexao(
            String outroUid,
            Runnable aoSucesso,
            Runnable aoFalhar
    ) {
        if (outroUid == null || outroUid.trim().isEmpty()
                || meuUid == null || meuUid.trim().isEmpty()) {
            if (aoFalhar != null) {
                aoFalhar.run();
            }
            return;
        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("uid", outroUid);
        dados.put("criadoEm", FieldValue.serverTimestamp());

        // Cada usuário grava somente a própria lista de conexões.
        db.collection("conexoes")
                .document(meuUid)
                .collection("usuarios")
                .document(outroUid)
                .set(dados, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    if (aoSucesso != null) {
                        aoSucesso.run();
                    }
                })
                .addOnFailureListener(e -> {
                    if (aoFalhar != null) {
                        aoFalhar.run();
                    } else {
                        Toast.makeText(
                                this,
                                "Não foi possível salvar a conexão.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

private void iniciarListenerGrupos() {
    if (meuUid == null || meuUid.trim().isEmpty()) {
        return;
    }

    if (listenerGrupos != null) {
        listenerGrupos.remove();
        listenerGrupos = null;
    }

    listenerGrupos =
        db.collection("grupos")
          .whereArrayContains("participantes", meuUid)
          .addSnapshotListener((snapshots, erro) -> {

              if (erro != null || snapshots == null) {
                  return;
              }

              if (gruposAbertos) {
                  return;
              }

              for (DocumentSnapshot grupo : snapshots.getDocuments()) {

                  String grupoId = grupo.getId();

                  db.collection("grupos")
                    .document(grupoId)
                    .collection("mensagens")
                    .orderBy("criadoEm", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(mensagens -> {

                        if (mensagens.isEmpty() || gruposAbertos) {
                            return;
                        }

                        DocumentSnapshot ultima =
                            mensagens.getDocuments().get(0);

                        String remetente =
                            ultima.getString("remetenteUid");

                        if (remetente != null
                                && !meuUid.equals(remetente)) {

                            atualizarIndicadorNotificacao(
                                    4,
                                    true
                            );
                        }
                    });
              }
          });
}

private void mostrarGrupos() {

        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(18), dp(18), dp(18), dp(12));

        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);

        TextView subtitulo = texto(
                "SEUS GRUPOS",
                14,
                Color.rgb(100, 190, 255)
        );
        subtitulo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        topo.addView(
                subtitulo,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        TextView criar = texto(
                "＋ CRIAR",
                12,
                Color.WHITE
        );
        criar.setGravity(Gravity.CENTER);
        criar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable fundoCriar = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(20, 83, 135),
                        Color.rgb(57, 36, 115)
                }
        );
        fundoCriar.setCornerRadius(dp(18));
        fundoCriar.setStroke(
                dp(1),
                Color.rgb(75, 165, 255)
        );
        criar.setBackground(fundoCriar);

        criar.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            SocialActivity.this,
                            CriarGrupoActivity.class
                    )
            );
        });

        topo.addView(
                criar,
                new LinearLayout.LayoutParams(
                        dp(105),
                        dp(48)
                )
        );

        principal.addView(topo);

        ScrollView scroll = new ScrollView(this);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(lista);

        principal.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        conteudo.addView(principal);

        String meuUid = "";

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            meuUid = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();
        }

        final String uidAtual = meuUid;

        if (uidAtual.isEmpty()) {

            mostrarMensagem(
                    "Faça login novamente para visualizar seus grupos."
            );

            return;
        }

        FirebaseFirestore.getInstance()
                .collection("grupos")
                .whereArrayContains("participantes", uidAtual)
                .get()
                .addOnSuccessListener(resultado -> {

                    lista.removeAllViews();

                    if (resultado.isEmpty()) {

                        TextView vazio = texto(
                                "Você ainda não participa de nenhum grupo.\\n\\n" +
                                "Crie seu primeiro grupo e escolha as pessoas " +
                                "que farão parte dele.",
                                15,
                                Color.rgb(155, 165, 185)
                        );

                        vazio.setGravity(Gravity.CENTER);
                        vazio.setPadding(
                                dp(25),
                                dp(60),
                                dp(25),
                                dp(60)
                        );

                        lista.addView(
                                vazio,
                                new LinearLayout.LayoutParams(
                                        -1,
                                        dp(220)
                                )
                        );

                        return;
                    }

                    for (QueryDocumentSnapshot doc : resultado) {
                        adicionarCardGrupo(lista, doc);
                    }
                })
                .addOnFailureListener(e -> {

                    TextView erro = texto(
                            "Não foi possível carregar seus grupos.",
                            15,
                            Color.rgb(230, 150, 150)
                    );

                    erro.setGravity(Gravity.CENTER);

                    lista.addView(
                            erro,
                            new LinearLayout.LayoutParams(
                                    -1,
                                    dp(160)
                            )
                    );
                });
    }

    private void adicionarCardGrupo(
            LinearLayout lista,
            DocumentSnapshot doc
    ) {

        String nome = doc.getString("nome");
        String descricao = doc.getString("descricao");
        String fotoUrl = doc.getString("fotoUrl");

        if (nome == null || nome.trim().isEmpty()) {
            nome = "Grupo";
        }

        if (descricao == null) {
            descricao = "";
        }

        if (fotoUrl == null) {
            fotoUrl = "";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(9, 17, 34),
                        Color.rgb(17, 10, 31),
                        Color.rgb(7, 19, 31)
                }
        );

        fundo.setCornerRadius(dp(24));
        fundo.setStroke(
                dp(1),
                Color.rgb(55, 105, 175)
        );

        card.setBackground(fundo);
        card.setElevation(dp(7));

        ImageView foto = new ImageView(this);
        foto.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        GradientDrawable fundoFoto =
                new GradientDrawable();

        fundoFoto.setShape(
                GradientDrawable.OVAL
        );

        fundoFoto.setColor(
                Color.rgb(20, 30, 48)
        );

        fundoFoto.setStroke(
                dp(2),
                Color.rgb(55, 145, 225)
        );

        foto.setBackground(fundoFoto);
        foto.setClipToOutline(true);

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(
                        dp(62),
                        dp(62)
                );

        fotoParams.setMargins(
                0,
                0,
                dp(14),
                0
        );

        card.addView(
                foto,
                fotoParams
        );

        if (!fotoUrl.trim().isEmpty()) {
            carregarImagem(
                    foto,
                    fotoUrl
            );
        }

        LinearLayout centro =
                new LinearLayout(this);

        centro.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView nomeView = texto(
                nome,
                17,
                Color.WHITE
        );

        nomeView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        centro.addView(
                nomeView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        String descricaoExibida =
                descricao.trim().isEmpty()
                        ? "Grupo do DaNikeAI"
                        : descricao.trim();

        TextView descricaoView =
                texto(
                        descricaoExibida,
                        12,
                        Color.rgb(155, 170, 190)
                );

        centro.addView(
                descricaoView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(24)
                )
        );

        Object participantesObj =
                doc.get("participantes");

        int quantidade = 0;

        if (participantesObj instanceof List) {
            quantidade =
                    ((List<?>) participantesObj).size();
        }

        TextView membros = texto(
                quantidade +
                        (quantidade == 1
                                ? " participante"
                                : " participantes"),
                11,
                Color.rgb(90, 180, 245)
        );

        centro.addView(
                membros,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(22)
                )
        );

        card.addView(
                centro,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        final String nomeGrupoFinal = nome;
        card.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SocialActivity.this,
                            ChatGrupoActivity.class
                    );

            intent.putExtra(
                    "grupoId",
                    doc.getId()
            );

            intent.putExtra(
                    "nomeGrupo",
                    nomeGrupoFinal
            );

            startActivity(intent);
        });

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(92)
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(10)
        );

        lista.addView(
                card,
                cardParams
        );
    }

    private void mostrarConexao() {

        pararListenerPerfis();
        conteudo.removeAllViews();

        LinearLayout principal = new LinearLayout(this);
        principal.setOrientation(LinearLayout.VERTICAL);
        principal.setPadding(dp(18), dp(18), dp(18), dp(12));

        TextView subtitulo = texto(
                "MINHAS CONEXÕES",
                14,
                Color.rgb(100, 190, 255)
        );

        subtitulo.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        principal.addView(
                subtitulo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(35)
                )
        );

        TextView explicacao = texto(
                "Aqui aparecem somente as pessoas que aceitaram sua conexão.",
                13,
                Color.rgb(135, 150, 175)
        );

        explicacao.setPadding(0, 0, 0, dp(12));

        principal.addView(
                explicacao,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(42)
                )
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(lista);

        principal.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        conteudo.addView(principal);

        TextView carregando = texto(
                "Carregando conexões...",
                15,
                Color.rgb(145, 155, 175)
        );

        carregando.setGravity(Gravity.CENTER);

        lista.addView(
                carregando,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(100)
                )
        );

        if (meuUid == null || meuUid.trim().isEmpty()) {

            lista.removeAllViews();

            TextView login = texto(
                    "Faça login para visualizar suas conexões.",
                    15,
                    Color.rgb(145, 155, 175)
            );

            login.setGravity(Gravity.CENTER);

            lista.addView(
                    login,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(160)
                    )
            );

            return;
        }

        listenerPerfis =
                db.collection("conexoes")
                        .document(meuUid)
                        .collection("usuarios")
                        .addSnapshotListener((snapshot, error) -> {

                            lista.removeAllViews();

                            if (error != null) {

                                TextView erro = texto(
                                        "Não foi possível carregar suas conexões.\n\n" +
                                        error.getMessage(),
                                        14,
                                        Color.rgb(220, 130, 140)
                                );

                                erro.setGravity(Gravity.CENTER);

                                lista.addView(
                                        erro,
                                        new LinearLayout.LayoutParams(
                                                -1,
                                                dp(160)
                                        )
                                );

                                return;
                            }

                            if (snapshot == null || snapshot.isEmpty()) {

                                TextView vazio = texto(
                                        "👥\n\n" +
                                        "Você ainda não possui conexões.\n\n" +
                                        "Quando alguém aceitar seu convite,\n" +
                                        "a pessoa aparecerá aqui.",
                                        15,
                                        Color.rgb(145, 155, 175)
                                );

                                vazio.setGravity(Gravity.CENTER);

                                lista.addView(
                                        vazio,
                                        new LinearLayout.LayoutParams(
                                                -1,
                                                dp(190)
                                        )
                                );

                                return;
                            }

                            for (DocumentSnapshot conexao :
                                    snapshot.getDocuments()) {

                                String uidConexao =
                                        conexao.getId();

                                if (uidConexao == null ||
                                        uidConexao.trim().isEmpty() ||
                                        uidConexao.equals(meuUid)) {
                                    continue;
                                }

                                db.collection("perfis_publicos")
                                        .document(uidConexao)
                                        .get()
                                        .addOnSuccessListener(perfil -> {

                                            if (!perfil.exists()) {
                                                return;
                                            }

                                            adicionarCardPessoa(
                                                    lista,
                                                    perfil
                                            );
                                        });
                            }
                        });
    }

    private void adicionarCardPessoa(
            LinearLayout lista,
            DocumentSnapshot doc
    ) {

        String nome = doc.getString("nome");
        String identificador = doc.getString("identificadorPublico");
        String fotoUrl = doc.getString("fotoUrl");

        Boolean onlineValor = doc.getBoolean("online");
        boolean online = Boolean.TRUE.equals(onlineValor);

        if (nome == null || nome.trim().isEmpty()) {
            nome = "Usuário";
        }

        if (identificador == null || identificador.trim().isEmpty()) {
            identificador = "DNK-------";
        }

        final String nomeFinal = nome;
        final String uidDestinoFinal = doc.getId();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(9, 17, 34),
                        Color.rgb(17, 10, 31),
                        Color.rgb(7, 19, 31)
                }
        );
        fundo.setCornerRadius(dp(24));
        fundo.setStroke(
                dp(1),
                online
                        ? Color.rgb(40, 135, 205)
                        : Color.rgb(55, 75, 110)
        );
        card.setBackground(fundo);
        card.setElevation(dp(7));

        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable fundoFoto = new GradientDrawable();
        fundoFoto.setShape(GradientDrawable.OVAL);
        fundoFoto.setColor(Color.rgb(20, 30, 48));
        fundoFoto.setStroke(dp(2), Color.rgb(55, 145, 225));
        foto.setBackground(fundoFoto);
        foto.setClipToOutline(true);

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(
                        dp(64),
                        dp(64)
                );
        fotoParams.setMargins(0, 0, dp(12), 0);

        card.addView(foto, fotoParams);

        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            carregarImagem(foto, fotoUrl);
        } else {
            foto.setImageDrawable(null);
        }

        LinearLayout centro = new LinearLayout(this);
        centro.setOrientation(LinearLayout.VERTICAL);
        centro.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams centroParams =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );
        centroParams.setMargins(0, 0, dp(8), 0);

        TextView nomeView = texto(
                nome,
                17,
                Color.WHITE
        );
        nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        centro.addView(
                nomeView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        LinearLayout linhaId = new LinearLayout(this);
        linhaId.setOrientation(LinearLayout.HORIZONTAL);
        linhaId.setGravity(Gravity.CENTER_VERTICAL);

        TextView idView = texto(
                identificador,
                12,
                Color.rgb(105, 190, 255)
        );
        idView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        linhaId.addView(
                idView,
                new LinearLayout.LayoutParams(
                        0,
                        dp(28),
                        1
                )
        );

        TextView copiar = texto(
                "📋",
                17,
                Color.WHITE
        );
        copiar.setGravity(Gravity.CENTER);

        GradientDrawable fundoCopiar = new GradientDrawable();
        fundoCopiar.setColor(Color.rgb(17, 40, 68));
        fundoCopiar.setCornerRadius(dp(12));
        fundoCopiar.setStroke(dp(1), Color.rgb(55, 130, 205));
        copiar.setBackground(fundoCopiar);

        final String identificadorFinal = identificador;
            copiar.setOnClickListener(v -> copiarPublico(identificadorFinal));

        linhaId.addView(
                copiar,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(30)
                )
        );

        centro.addView(linhaId);

        TextView status = texto(
                online ? "● Online" : "○ Offline",
                12,
                online
                        ? Color.rgb(70, 220, 145)
                        : Color.rgb(145, 155, 175)
        );

        status.setPadding(0, dp(2), 0, 0);

        centro.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(24)
                )
        );

        card.addView(centro, centroParams);

        TextView convidar = texto(
                "💬 CONVERSAR",
                10,
                Color.WHITE
        );
        convidar.setGravity(Gravity.CENTER);
        convidar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        GradientDrawable fundoConvidar = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(20, 83, 135),
                        Color.rgb(57, 36, 115)
                }
        );
        fundoConvidar.setCornerRadius(dp(16));
        fundoConvidar.setStroke(dp(1), Color.rgb(75, 165, 255));
        convidar.setBackground(fundoConvidar);

        convidar.setOnClickListener(v -> {

            Intent chat = new Intent(
                    SocialActivity.this,
                    ChatIndividualActivity.class
            );

            chat.putExtra(
                    "uid",
                    uidDestinoFinal
            );

            startActivity(chat);
        });

        card.addView(
                convidar,
                new LinearLayout.LayoutParams(
                        dp(88),
                        dp(46)
                )
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(94)
                );
        cardParams.setMargins(0, 0, 0, dp(12));

        lista.addView(card, cardParams);
    }

    private void enviarConvite(String uidDestino, String nomeDestino) {

        if (meuUid.isEmpty()) {
            Toast.makeText(
                    this,
                    "Faça login para enviar um convite.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (uidDestino == null || uidDestino.trim().isEmpty()) {
            return;
        }

        Map<String, Object> convite = new HashMap<>();
        convite.put("uidRemetente", meuUid);
        convite.put("uidDestino", uidDestino);
        convite.put("status", "pendente");
        convite.put("criadoEm", FieldValue.serverTimestamp());

        db.collection("convites")
                .document(uidDestino)
                .collection("recebidos")
                .document(meuUid)
                .set(convite, SetOptions.merge())
                .addOnSuccessListener(v ->
                        Toast.makeText(
                                this,
                                "Convite enviado para " + nomeDestino,
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Não foi possível enviar o convite.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void copiarPublico(String identificador) {

        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)
                        getSystemService(CLIPBOARD_SERVICE);

        if (clipboard != null) {
            clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText(
                            "Identificador DaNikeAI",
                            identificador
                    )
            );

            Toast.makeText(
                    this,
                    identificador + " copiado.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void carregarImagem(
            ImageView imageView,
            String url
    ) {

        executor.execute(() -> {

            android.graphics.Bitmap bitmap = null;
            HttpURLConnection conexao = null;
            InputStream entrada = null;

            try {
                URL endereco = new URL(url);
                conexao = (HttpURLConnection) endereco.openConnection();
                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(8000);
                conexao.setInstanceFollowRedirects(true);
                conexao.connect();

                if (conexao.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    entrada = conexao.getInputStream();
                    bitmap = android.graphics.BitmapFactory.decodeStream(entrada);
                }

            } catch (Exception ignored) {
            } finally {
                try {
                    if (entrada != null) entrada.close();
                } catch (Exception ignored) {
                }

                if (conexao != null) {
                    conexao.disconnect();
                }
            }

            final android.graphics.Bitmap resultado = bitmap;

            runOnUiThread(() -> {
                if (resultado != null && !isFinishing()) {
                    imageView.setImageBitmap(resultado);
                }
            });
        });
    }

    private void iniciarListenerConversas() {
        if (meuUid == null || meuUid.trim().isEmpty()) {
            return;
        }

        if (listenerConversas != null) {
            listenerConversas.remove();
            listenerConversas = null;
        }

        listenerConversas = db.collection("chats")
                .whereArrayContains("participantes", meuUid)
                .addSnapshotListener((snapshots, erro) -> {
                    if (erro != null || snapshots == null) {
                        return;
                    }

                    if (botoes == null || botoes.length < 4) {
                        return;
                    }

                    if (botoes[3].getCurrentTextColor() == Color.TRANSPARENT) {
                        return;
                    }

                    carregarListaConversas(snapshots.getDocuments());
                });
    }

    private void carregarListaConversas(List<DocumentSnapshot> documentos) {
        if (conteudo == null || isFinishing() || isDestroyed()) {
            return;
        }

        conteudo.removeAllViews();

        if (documentos == null || documentos.isEmpty()) {
            mostrarMensagem(
                    "💬\n\nNenhuma conversa ainda.\n\n" +
                    "Quando você conversar com uma conexão, " +
                    "ela aparecerá aqui."
            );
            return;
        }

        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(dp(10), dp(10), dp(10), dp(100));

        conteudo.addView(
                lista,
                new FrameLayout.LayoutParams(-1, -1)
        );

        for (DocumentSnapshot conversa : documentos) {
            Object participantesObj = conversa.get("participantes");

            if (!(participantesObj instanceof List)) {
                continue;
            }

            List<?> participantes = (List<?>) participantesObj;

            for (Object participante : participantes) {
                if (!(participante instanceof String)) {
                    continue;
                }

                String uidOutro = (String) participante;

                if (uidOutro == null || uidOutro.trim().isEmpty()
                        || meuUid.equals(uidOutro)) {
                    continue;
                }

                carregarDadosConversa(lista, conversa.getId(), uidOutro);
                break;
            }
        }
    }

    private void carregarDadosConversa(
            LinearLayout lista,
            String conversaId,
            String uidOutro
    ) {
        db.collection("perfis_publicos")
                .document(uidOutro)
                .get()
                .addOnSuccessListener(perfil -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    String nome = perfil.getString("nome");
                    String fotoUrl = perfil.getString("fotoUrl");

                    if (nome == null || nome.trim().isEmpty()) {
                        nome = "Usuário";
                    }

                    final String nomeFinal = nome;
                    final String fotoFinal = fotoUrl;

                    db.collection("chats")
                            .document(conversaId)
                            .collection("mensagens")
                            .orderBy("criadoEm", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(mensagens -> {
                                if (isFinishing() || isDestroyed()) {
                                    return;
                                }

                                DocumentSnapshot ultima =
                                        mensagens.isEmpty()
                                                ? null
                                                : mensagens.getDocuments().get(0);

                                                                if (ultima != null && !mensagensAbertas) {
                                    String remetente = ultima.getString("remetenteUid");
                                    if (remetente != null && !meuUid.equals(remetente)) {
                                        atualizarIndicadorNotificacao(3, true);
                                    }
                                }
adicionarCardConversa(
                                        lista,
                                        uidOutro,
                                        nomeFinal,
                                        fotoFinal,
                                        ultima
                                );
                            });
                });
    }

    private void adicionarCardConversa(
            LinearLayout lista,
            String uidOutro,
            String nome,
            String fotoUrl,
            DocumentSnapshot ultima
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(9, 17, 34),
                        Color.rgb(17, 10, 31),
                        Color.rgb(7, 19, 31)
                }
        );
        fundo.setCornerRadius(dp(24));
        fundo.setStroke(dp(1), Color.rgb(45, 125, 205));
        card.setBackground(fundo);
        card.setElevation(dp(7));

        ImageView foto = new ImageView(this);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        GradientDrawable fundoFoto = new GradientDrawable();
        fundoFoto.setShape(GradientDrawable.OVAL);
        fundoFoto.setColor(Color.rgb(20, 30, 48));
        fundoFoto.setStroke(dp(2), Color.rgb(55, 145, 225));
        foto.setBackground(fundoFoto);
        foto.setClipToOutline(true);

        LinearLayout.LayoutParams fotoParams =
                new LinearLayout.LayoutParams(dp(58), dp(58));
        fotoParams.setMargins(0, 0, dp(12), 0);
        card.addView(foto, fotoParams);

        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            carregarImagem(foto, fotoUrl);
        }

        LinearLayout centro = new LinearLayout(this);
        centro.setOrientation(LinearLayout.VERTICAL);
        centro.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams centroParams =
                new LinearLayout.LayoutParams(0, -2, 1);
        centroParams.setMargins(0, 0, dp(8), 0);

        TextView nomeView = texto(nome, 16, Color.WHITE);
        nomeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nomeView.setSingleLine(true);

        centro.addView(
                nomeView,
                new LinearLayout.LayoutParams(-1, dp(28))
        );

        String ultimaTexto = "Nenhuma mensagem ainda";
        String hora = "";

        if (ultima != null) {
            String textoUltima = ultima.getString("texto");

            if (textoUltima != null && !textoUltima.trim().isEmpty()) {
                ultimaTexto = textoUltima.trim();
            }

            Object criadoEm = ultima.get("criadoEm");

            if (criadoEm instanceof Timestamp) {
                hora = new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                ).format(((Timestamp) criadoEm).toDate());
            }
        }

        TextView mensagem = texto(
                ultimaTexto,
                13,
                Color.rgb(150, 170, 195)
        );
        mensagem.setSingleLine(true);
        mensagem.setEllipsize(android.text.TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams mensagemParams =
                new LinearLayout.LayoutParams(0, dp(24), 1);
        centro.addView(mensagem, mensagemParams);

        card.addView(centro, centroParams);

        TextView horaView = texto(
                hora,
                11,
                Color.rgb(105, 190, 255)
        );
        horaView.setGravity(Gravity.CENTER);
        horaView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        card.addView(
                horaView,
                new LinearLayout.LayoutParams(dp(48), dp(34))
        );

        card.setOnClickListener(v -> {
            Intent chat = new Intent(
                    SocialActivity.this,
                    ChatIndividualActivity.class
            );

            chat.putExtra("uid", uidOutro);
            startActivity(chat);
        });

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, dp(82));
        cardParams.setMargins(0, 0, 0, dp(12));

        lista.addView(card, cardParams);
    }

    private void pararListenerConversas() {
    if (listenerConversas != null) {
        listenerConversas.remove();
        listenerConversas = null;
    }
}

private void pararListenerPerfis() {

        if (listenerPerfis != null) {
            listenerPerfis.remove();
            listenerPerfis = null;
        }
    }

    private void mostrarMensagem(String mensagem) {

        conteudo.removeAllViews();

        TextView texto = texto(
                mensagem,
                16,
                Color.rgb(145, 155, 175)
        );

        texto.setGravity(Gravity.CENTER);

        conteudo.addView(
                texto,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );
    }

    private TextView botao(String texto) {
        TextView t = texto(
                texto,
                11,
                Color.rgb(145, 160, 185)
        );

        t.setGravity(Gravity.CENTER);
        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        t.setPadding(
                dp(2),
                dp(2),
                dp(2),
                dp(2)
        );

        t.setMinHeight(dp(62));
        t.setMinimumWidth(dp(50));
        t.setVisibility(View.VISIBLE);
        t.setIncludeFontPadding(true);
        t.setLineSpacing(0, 0.95f);

        // Movimento neon suave
        t.setPivotX(0.5f);
        t.setPivotY(0.5f);

        android.animation.ObjectAnimator pulso =
                android.animation.ObjectAnimator.ofPropertyValuesHolder(
                        t,
                        android.animation.PropertyValuesHolder.ofFloat(
                                View.SCALE_X, 1.0f, 1.035f, 1.0f
                        ),
                        android.animation.PropertyValuesHolder.ofFloat(
                                View.SCALE_Y, 1.0f, 1.035f, 1.0f
                        ),
                        android.animation.PropertyValuesHolder.ofFloat(
                                View.ALPHA, 0.82f, 1.0f, 0.82f
                        )
                );

        pulso.setDuration(1800);
        pulso.setStartDelay((long) (Math.random() * 700));
        pulso.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        pulso.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        pulso.start();

        return t;
    }

    private TextView texto(
            String texto,
            float tamanho,
            int cor
    ) {

        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

        return t;
    }

    private int dp(int valor) {
        return (int) (
                valor *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    @Override
    protected void onDestroy() {
        pararListenerPerfis();
        pararListenerConversas();
        executor.shutdownNow();
        super.onDestroy();
    }
}
