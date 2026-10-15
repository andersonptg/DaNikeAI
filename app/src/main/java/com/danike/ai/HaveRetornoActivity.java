package com.danike.ai;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class HaveRetornoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        abrirSala(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        abrirSala(intent);
    }

    private void abrirSala(Intent intent) {
        Uri uri = intent != null ? intent.getData() : null;

        if (uri == null) {
            Toast.makeText(
                    this,
                    "Retorno HAVE inválido.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        String grupoId = uri.getQueryParameter("grupoId");

        if (grupoId == null || grupoId.trim().isEmpty()) {
            grupoId = uri.getLastPathSegment();
        }

        if (grupoId == null || grupoId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Sala HAVE não encontrada.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        Intent sala = new Intent(
                this,
                ChatGrupoActivity.class
        );

        sala.putExtra("grupoId", grupoId);
        sala.putExtra(
                "nomeGrupo",
                "Sala HAVE"
        );

        startActivity(sala);
        finish();
    }
}
