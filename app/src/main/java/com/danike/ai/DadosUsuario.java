package com.danike.ai;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public final class DadosUsuario {

    private DadosUsuario() {}

    public static void nome(Context context, Callback callback) {

        String local = context
                .getSharedPreferences("DaNikeAI_Dados", Context.MODE_PRIVATE)
                .getString("nome", "")
                .trim();

        if (!local.isEmpty()) {
            callback.receber(local);
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.receber("Usuário");
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String nome = doc.getString("nome");

                    if (nome == null || nome.trim().isEmpty()) {
                        nome = "Usuário";
                    }

                    context.getSharedPreferences(
                            "DaNikeAI_Dados",
                            Context.MODE_PRIVATE
                    ).edit()
                            .putString("nome", nome.trim())
                            .apply();

                    callback.receber(nome.trim());
                })
                .addOnFailureListener(e ->
                        callback.receber("Usuário")
                );
    }

    public static void salvarNome(Context context, String nome) {

        String valor = nome == null ? "" : nome.trim();

        if (valor.isEmpty()) {
            valor = "Usuário";
        }

        context.getSharedPreferences(
                "DaNikeAI_Dados",
                Context.MODE_PRIVATE
        ).edit()
                .putString("nome", valor)
                .apply();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String uid = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

            FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(uid)
                    .set(
                            java.util.Collections.singletonMap("nome", valor),
                            com.google.firebase.firestore.SetOptions.merge()
                    );
        }
    }

    public interface Callback {
        void receber(String nome);
    }
}
