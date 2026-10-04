package com.danike.ai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class AppDistribuicao {

    private AppDistribuicao() {}

    public static void compartilhar(Context context, String link) {
        Intent compartilhar = new Intent(Intent.ACTION_SEND);
        compartilhar.setType("text/plain");
        compartilhar.putExtra(
                Intent.EXTRA_TEXT,
                "🚀 Conheça o DaNikeAI!\n\nBaixe o aplicativo:\n" + link
        );
        context.startActivity(
                Intent.createChooser(compartilhar, "Compartilhar DaNikeAI")
        );
    }

    public static void abrirLink(Context context, String link) {
        try {
            context.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(link))
            );
        } catch (Exception e) {
            Toast.makeText(
                    context,
                    "Não foi possível abrir o endereço.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public static void baixarApk(Context context, String apkUrl) {
        Toast.makeText(
                context,
                "Atualização iniciada...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {
            File arquivo = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    "DaNikeAI-atualizacao.apk"
            );

            HttpURLConnection conexao = null;

            try {
                URL url = new URL(apkUrl);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setInstanceFollowRedirects(true);
                conexao.setConnectTimeout(20000);
                conexao.setReadTimeout(60000);
                conexao.setRequestMethod("GET");
                conexao.setRequestProperty("User-Agent", "DaNikeAI-Updater");

                conexao.connect();

                int codigo = conexao.getResponseCode();

                if (codigo < 200 || codigo >= 300) {
                    throw new Exception(
                            "Servidor retornou HTTP " + codigo
                    );
                }

                long tamanho = conexao.getContentLengthLong();

                try (
                        InputStream entrada = new BufferedInputStream(
                                conexao.getInputStream()
                        );
                        FileOutputStream saida = new FileOutputStream(arquivo)
                ) {
                    byte[] buffer = new byte[8192];
                    int lidos;
                    long total = 0;

                    while ((lidos = entrada.read(buffer)) != -1) {
                        saida.write(buffer, 0, lidos);
                        total += lidos;
                    }

                    saida.flush();

                    if (tamanho > 0 && total != tamanho) {
                        throw new Exception(
                                "Download incompleto: " + total +
                                " de " + tamanho + " bytes."
                        );
                    }
                }

                if (!arquivo.exists() || arquivo.length() < 1024 * 1024) {
                    throw new Exception(
                            "APK baixado está inválido ou incompleto."
                    );
                }

                final File apkFinal = arquivo;

                new android.os.Handler(
                        context.getMainLooper()
                ).post(() -> abrirInstalador(context, apkFinal));

            } catch (Exception e) {
                android.util.Log.e(
                        "DaNikeAI",
                        "Erro no download da atualização",
                        e
                );

                String detalhe = e.getMessage();

                if (detalhe == null || detalhe.trim().isEmpty()) {
                    detalhe = e.getClass().getSimpleName();
                }

                final String erroFinal = detalhe;

                new android.os.Handler(
                        context.getMainLooper()
                ).post(() ->
                        Toast.makeText(
                                context,
                                "Falha no download: " + erroFinal,
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    private static void abrirInstalador(
            Context context,
            File arquivo
    ) {
        try {
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    arquivo
            );

            Intent instalar = new Intent(Intent.ACTION_VIEW);
            instalar.setDataAndType(
                    apkUri,
                    "application/vnd.android.package-archive"
            );
            instalar.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
            instalar.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(instalar);

        } catch (Exception e) {
            android.util.Log.e(
                    "DaNikeAI",
                    "Erro ao abrir instalador",
                    e
            );

            Toast.makeText(
                    context,
                    "Não foi possível abrir o instalador.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
