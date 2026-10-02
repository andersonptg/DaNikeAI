package com.danike.ai;

import com.google.firebase.firestore.FirebaseFirestore;

public class ControleApp {

    public interface Callback {
        void resultado(boolean manutencao, String mensagem);
    }

    public static void verificar(Callback callback) {

        FirebaseFirestore db =
                FirebaseFirestore.getInstance();

        db.collection("config")
                .document("app")
                .get()
                .addOnSuccessListener(documento -> {

                    if (!documento.exists()) {
                        callback.resultado(false, "");
                        return;
                    }

                    Boolean manutencao =
                            documento.getBoolean("manutencao");

                    String mensagem =
                            documento.getString("mensagemManutencao");

                    callback.resultado(
                            manutencao != null && manutencao,
                            mensagem == null
                                    ? "Estamos realizando melhorias no DaNikeAI."
                                    : mensagem
                    );
                })
                .addOnFailureListener(e -> {

                    // Se o Firebase estiver temporariamente
                    // indisponível, não bloqueamos o usuário.
                    callback.resultado(false, "");
                });
    }
}
