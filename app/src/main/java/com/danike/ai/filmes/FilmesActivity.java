package com.danike.ai.filmes;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FilmesActivity extends Activity {

    private LinearLayout lista;
    private Button buscarBtn;
    private EditText busca;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Color.rgb(4, 6, 12));

        // TOPO
        LinearLayout topo = new LinearLayout(this);
        topo.setGravity(Gravity.CENTER_VERTICAL);
        topo.setPadding(dp(18), dp(16), dp(18), dp(10));

        TextView logo = new TextView(this);
        logo.setText("DaNikeAI");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(23);
        logo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView lupa = new TextView(this);
        lupa.setText("  🔍");
        lupa.setTextColor(Color.CYAN);
        lupa.setTextSize(22);

        topo.addView(logo, new LinearLayout.LayoutParams(
                0, dp(50), 1
        ));
        topo.addView(lupa);

        raiz.addView(topo);

        // TÍTULO
        TextView titulo = new TextView(this);
        titulo.setText("FILMES");
        titulo.setTextColor(Color.CYAN);
        titulo.setTextSize(28);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setPadding(dp(18), dp(5), dp(18), dp(2));

        raiz.addView(titulo);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("Filmes completos disponíveis no YouTube");
        subtitulo.setTextColor(Color.GRAY);
        subtitulo.setTextSize(13);
        subtitulo.setPadding(dp(18), 0, dp(18), dp(12));

        raiz.addView(subtitulo);

        // CATEGORIAS
        HorizontalScrollView categoriasScroll =
                new HorizontalScrollView(this);

        categoriasScroll.setHorizontalScrollBarEnabled(false);

        LinearLayout categorias = new LinearLayout(this);
        categorias.setPadding(dp(14), dp(4), dp(14), dp(12));

        String[] nomesCategorias = {
                "Todos",
                "Ação",
                "Comédia",
                "Drama",
                "Ficção",
                "Terror",
                "Romance",
                "Animação"
        };

        for (String categoria : nomesCategorias) {
            Button botao = new Button(this);
            botao.setText(categoria);
            botao.setTextColor(Color.WHITE);
            botao.setTextSize(12);
            botao.setAllCaps(false);
            botao.setPadding(dp(14), 0, dp(14), 0);

            LinearLayout.LayoutParams cp =
                    new LinearLayout.LayoutParams(
                            dp(105), dp(48)
                    );

            cp.setMargins(dp(4), 0, dp(4), 0);

            categorias.addView(botao, cp);

            botao.setOnClickListener(v ->
                    carregarFilmes(((Button) v).getText().toString())
            );
        }

        categoriasScroll.addView(categorias);
        raiz.addView(categoriasScroll);

        // BUSCA
        LinearLayout linhaBusca = new LinearLayout(this);
        linhaBusca.setPadding(dp(16), 0, dp(16), dp(10));

        busca = new EditText(this);
        busca.setHint("Pesquisar filme...");
        busca.setHintTextColor(Color.GRAY);
        busca.setTextColor(Color.WHITE);
        busca.setSingleLine(true);
        busca.setTextSize(14);

        buscarBtn = new Button(this);
        buscarBtn.setText("BUSCAR");
        buscarBtn.setTextColor(Color.CYAN);
        buscarBtn.setAllCaps(false);

        linhaBusca.addView(busca, new LinearLayout.LayoutParams(
                0, dp(55), 1
        ));

        linhaBusca.addView(buscarBtn, new LinearLayout.LayoutParams(
                dp(105), dp(55)
        ));

        raiz.addView(linhaBusca);

        buscarBtn.setOnClickListener(v -> {
            String texto = busca.getText().toString().trim();

            if (!texto.isEmpty()) {
                carregarFilmes(texto);
            }
        });

        // CONTEÚDO
        ScrollView scroll = new ScrollView(this);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(dp(12), dp(5), dp(12), dp(30));

        scroll.addView(lista);

        raiz.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1
        ));

        setContentView(raiz);

        // CARREGA AUTOMATICAMENTE
        carregarFilmes("filmes completos");
    }

    private void carregarFilmes(String consulta) {

        lista.removeAllViews();

        TextView carregando = new TextView(this);
        carregando.setText("🔎 Carregando filmes...");
        carregando.setTextColor(Color.CYAN);
        carregando.setTextSize(16);
        carregando.setGravity(Gravity.CENTER);
        carregando.setPadding(0, dp(30), 0, dp(30));

        lista.addView(carregando);

        buscarBtn.setEnabled(false);

        executor.execute(() -> {

            try {

                FilmesRepository repository =
                        new FilmesRepository(this);

                ArrayList<Filme> filmes =
                        repository.buscar(consulta);

                runOnUiThread(() -> mostrarFilmes(filmes));

            } catch (Exception e) {

                runOnUiThread(() -> {

                    lista.removeAllViews();

                    TextView erro = new TextView(this);
                    erro.setText(
                            "Não foi possível carregar os filmes.\n\n" +
                            "Verifique sua conexão e a configuração da API."
                    );

                    erro.setTextColor(Color.RED);
                    erro.setTextSize(15);
                    erro.setGravity(Gravity.CENTER);
                    erro.setPadding(dp(20), dp(30), dp(20), dp(30));

                    lista.addView(erro);
                    buscarBtn.setEnabled(true);
                });
            }
        });
    }

    private void mostrarFilmes(ArrayList<Filme> filmes) {

        lista.removeAllViews();
        buscarBtn.setEnabled(true);

        if (filmes == null || filmes.isEmpty()) {

            TextView vazio = new TextView(this);
            vazio.setText("Nenhum filme encontrado.");
            vazio.setTextColor(Color.GRAY);
            vazio.setTextSize(16);
            vazio.setGravity(Gravity.CENTER);

            lista.addView(vazio);
            return;
        }

        TextView destaque = new TextView(this);
        destaque.setText("🔥 FILMES EM DESTAQUE");
        destaque.setTextColor(Color.WHITE);
        destaque.setTextSize(20);
        destaque.setTypeface(Typeface.DEFAULT_BOLD);
        destaque.setPadding(dp(6), dp(12), 0, dp(12));

        lista.addView(destaque);

        LinearLayout grade = null;

        for (int i = 0; i < filmes.size(); i++) {

            if (i % 2 == 0) {
                grade = new LinearLayout(this);
                grade.setOrientation(LinearLayout.HORIZONTAL);
                grade.setGravity(Gravity.CENTER);
                lista.addView(grade);
            }

            Filme filme = filmes.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(5), dp(5), dp(5), dp(12));

            ImageView capa = new ImageView(this);
            capa.setScaleType(ImageView.ScaleType.CENTER_CROP);
            capa.setBackgroundColor(Color.rgb(15, 18, 28));

            TextView tituloFilme = new TextView(this);
            tituloFilme.setText(filme.getTitulo());
            tituloFilme.setTextColor(Color.WHITE);
            tituloFilme.setTextSize(12);
            tituloFilme.setTypeface(Typeface.DEFAULT_BOLD);
            tituloFilme.setMaxLines(2);
            tituloFilme.setGravity(Gravity.CENTER);
            tituloFilme.setPadding(dp(4), dp(6), dp(4), 0);

            card.addView(capa, new LinearLayout.LayoutParams(
                    -1, dp(220)
            ));

            card.addView(tituloFilme, new LinearLayout.LayoutParams(
                    -1, dp(48)
            ));

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(275),
                            1
                    );

            cardParams.setMargins(dp(4), dp(4), dp(4), dp(4));

            grade.addView(card, cardParams);

            ImagemLoader.carregar(filme.getThumbnail(), capa);

            card.setOnClickListener(v -> abrirFilme(filme));
        }
    }

    private void abrirFilme(Filme filme) {

        android.content.Intent intent =
                new android.content.Intent(
                        this,
                        VideoActivity.class
                );

        intent.putExtra("VIDEO_ID", filme.getId());
        intent.putExtra("VIDEO_TITLE", filme.getTitulo());

        startActivity(intent);
    }

    private int dp(int valor) {
        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
