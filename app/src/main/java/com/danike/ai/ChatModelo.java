package com.danike.ai;

import java.util.HashMap;
import java.util.Map;

public final class ChatModelo {

    private ChatModelo() {}

    public static Map<String, Object> mensagem(
            String remetenteUid,
            String texto,
            String tipo
    ) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("remetenteUid", remetenteUid);
        dados.put("texto", texto == null ? "" : texto);
        dados.put("tipo", tipo == null ? "texto" : tipo);
        dados.put("criadoEm",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        return dados;
    }
}
