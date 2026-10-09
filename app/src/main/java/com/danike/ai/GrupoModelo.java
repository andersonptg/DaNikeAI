package com.danike.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GrupoModelo {

    private GrupoModelo() {}

    public static Map<String, Object> novoGrupo(
            String nome,
            String descricao,
            String fotoUrl,
            String administradorUid,
            List<String> participantes
    ) {
        Map<String, Object> dados = new HashMap<>();

        dados.put("nome", nome == null ? "" : nome.trim());
        dados.put("descricao", descricao == null ? "" : descricao.trim());
        dados.put("fotoUrl", fotoUrl == null ? "" : fotoUrl);
        dados.put("administradorUid",
                administradorUid == null ? "" : administradorUid);

        dados.put(
                "participantes",
                participantes == null
                        ? new ArrayList<>()
                        : new ArrayList<>(participantes)
        );

        dados.put("criadoEm",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        return dados;
    }
}
