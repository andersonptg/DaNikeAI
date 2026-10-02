package com.danike.ai;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public final class AppDistribuicao {

    private AppDistribuicao() {}

    public static void compartilhar(Context context, String link) {
        Intent compartilhar = new Intent(Intent.ACTION_SEND);
        compartilhar.setType("text/plain");
        compartilhar.putExtra(
                Intent.EXTRA_TEXT,
                "🚀 Conheça o DaNikeAI!\n\n" +
                "Baixe o aplicativo:\n" + link
        );

        Intent chooser = Intent.createChooser(
                compartilhar,
                "Compartilhar DaNikeAI"
        );

        context.startActivity(chooser);
    }

    public static void abrirLink(Context context, String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(
                    context,
                    "Não foi possível abrir o endereço.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public static void baixarApk(Context context, String apkUrl) {
        try {
            DownloadManager.Request request =
                    new DownloadManager.Request(Uri.parse(apkUrl));

            request.setTitle("DaNikeAI");
            request.setDescription("Baixando atualização do DaNikeAI...");
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "DaNikeAI-atualizacao.apk"
            );

            request.setMimeType("application/vnd.android.package-archive");
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            DownloadManager manager =
                    (DownloadManager) context.getSystemService(
                            Context.DOWNLOAD_SERVICE
                    );

            if (manager != null) {
                manager.enqueue(request);

                Toast.makeText(
                        context,
                        "Download iniciado. Verifique a pasta Downloads.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {
            Toast.makeText(
                    context,
                    "Erro ao iniciar o download.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
