package com.danike.ai.filmes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagemLoader {

    private static final ExecutorService executor =
            Executors.newFixedThreadPool(4);

    private static final Handler handler =
            new Handler(Looper.getMainLooper());

    public static void carregar(String urlString, ImageView imageView) {

        executor.execute(() -> {

            Bitmap bitmap = null;

            try {
                URL url = new URL(urlString);

                HttpURLConnection conexao =
                        (HttpURLConnection) url.openConnection();

                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);
                conexao.setDoInput(true);
                conexao.connect();

                InputStream entrada = conexao.getInputStream();

                bitmap = BitmapFactory.decodeStream(entrada);

                entrada.close();
                conexao.disconnect();

            } catch (Exception ignored) {
            }

            Bitmap resultado = bitmap;

            handler.post(() -> {
                if (resultado != null) {
                    imageView.setImageBitmap(resultado);
                }
            });
        });
    }
}
