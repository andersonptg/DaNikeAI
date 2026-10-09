package com.danike.ai;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public final class StatusModelo {

    private StatusModelo() {}

    public static Map<String, Object> novo(
            String autorUid,
            String tipo,
            String texto,
            String midiaUrl,
            String fundo,
            String fonte,
            Long expiraEm
    ) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("autorUid", autorUid == null ? "" : autorUid);
        dados.put("tipo", tipo == null ? "mensagem" : tipo);
        dados.put("texto", texto == null ? "" : texto);
        dados.put("midiaUrl", midiaUrl == null ? "" : midiaUrl);
        dados.put("fundo", fundo == null ? "" : fundo);
        dados.put("fonte", fonte == null ? "padrao" : fonte);

        dados.put("criadoEm", FieldValue.serverTimestamp());

        if (expiraEm != null) {
            dados.put("expiraEm", expiraEm);
        } else {
            dados.put("expiraEm",
                    System.currentTimeMillis() + (24L * 60L * 60L * 1000L));
        }

        dados.put("visualizadores", new HashMap<String, Object>());

        return dados;
    }
}
