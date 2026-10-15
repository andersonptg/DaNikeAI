package com.danike.ai;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class HaveServicoActivity extends Activity {

    private String pacoteOficial(String nome) {
        if (nome == null) return null;
        nome = nome.toUpperCase();

        if (nome.contains("NETFLIX")) return "com.netflix.mediaclient";
        if (nome.contains("HBO") || nome.contains("MAX")) return "com.wbd.stream";
        if (nome.contains("PRIME")) return "com.amazon.avod.thirdpartyclient";
        if (nome.contains("DISNEY")) return "com.disney.disneyplus";
        if (nome.contains("YOUTUBE")) return "com.google.android.youtube";
        if (nome.contains("GLOBO")) return "com.globo.gloplay";
        if (nome.contains("PARAMOUNT")) return "com.cbs.app";
        if (nome.contains("CRUNCHYROLL")) return "com.crunchyroll.crunchyroid";
        if (nome.contains("PLUTO")) return "tv.pluto.android";
        if (nome.contains("TWITCH")) return "tv.twitch.android.app";

        return null;
    }

    private void abrirStreaming(String nome, String url) {
        String pacote = pacoteOficial(nome);

        if (pacote != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(pacote);
                startActivity(intent);
                Toast.makeText(this, nome + " aberto!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            } catch (ActivityNotFoundException ignored) {
            } catch (Exception ignored) {
            }

            try {
                Intent launch = getPackageManager().getLaunchIntentForPackage(pacote);
                if (launch != null) {
                    startActivity(launch);
                    finish();
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        try {
            Intent externo = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(externo);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir " + nome, Toast.LENGTH_LONG).show();
        }

        finish();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        String nome = getIntent().getStringExtra("nome");
        String url = getIntent().getStringExtra("url");

        if (nome == null || nome.trim().isEmpty()) nome = "Streaming";
        if (url == null || url.trim().isEmpty()) url = "https://www.netflix.com";

        final String servico = nome.trim();
        final String link = url.trim();

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) {
            Toast.makeText(this, "Faça login para criar uma sala HAVE.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = usuario.getUid();
        String nomeUsuario = usuario.getDisplayName();
        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            nomeUsuario = "danike";
        }

        String nomeSala = "HAVE • " + servico + " • " + nomeUsuario;
        String grupoId = FirebaseFirestore.getInstance()
                .collection("grupos").document().getId();

        Map<String, Object> grupo = new HashMap<>();
        grupo.put("grupoId", grupoId);
        grupo.put("nome", nomeSala);
        grupo.put("nomeGrupo", nomeSala);
        grupo.put("descricao", "Sala HAVE • " + servico);
        grupo.put("administradorUid", uid);
        grupo.put("participantes", Collections.singletonList(uid));
        grupo.put("servicoHave", servico);
        grupo.put("tipo", "HAVE");
        grupo.put("publico", true);
        grupo.put("criadoEm", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("grupos")
                .document(grupoId)
                .set(grupo)
                .addOnSuccessListener(resultado -> {
                    if (isFinishing() || isDestroyed()) return;

                    Toast.makeText(
                            HaveServicoActivity.this,
                            "Sala HAVE criada! Abrindo " + servico + "...",
                            Toast.LENGTH_LONG
                    ).show();

                    abrirStreaming(servico, link);
                })
                .addOnFailureListener(erro -> {
                    if (isFinishing() || isDestroyed()) return;

                    Toast.makeText(
                            HaveServicoActivity.this,
                            "Não foi possível criar a sala HAVE. Verifique sua conexão e as permissões.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                });
    }
}
