package com.danike.ai;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class UsuariosTracker {

    private UsuariosTracker() {}

    public static void registrarEntrada(String nome) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = user.getUid();
        String email = user.getEmail() == null ? "" : user.getEmail();

        Map<String, Object> dados = new HashMap<>();
        dados.put("uid", uid);
        dados.put("email", email);
        dados.put("nome", nome == null ? "" : nome);
        dados.put("online", true);
        dados.put("ultimoAcesso", FieldValue.serverTimestamp());
        dados.put("ultimoStatus", "online");

        if ("lipesanderson@gmail.com".equalsIgnoreCase(email)) {
            dados.put("tipo", "proprietario");
            dados.put("ehProprietario", true);
        } else {
            dados.put("tipo", "usuario");
            dados.put("ehProprietario", false);
        }

        db.collection("usuarios")
                .document(uid)
                .set(dados, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    db.collection("usuarios")
                            .document(uid)
                            .update(
                                    "totalAcessos",
                                    FieldValue.increment(1),
                                    "ultimoAcesso",
                                    FieldValue.serverTimestamp(),
                                    "online",
                                    true
                            );
                });
    }

    public static void marcarOffline() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.getUid())
                .update(
                        "online", false,
                        "ultimoStatus", "offline",
                        "ultimaSaida", FieldValue.serverTimestamp()
                );
    }
}
