package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

/** NEXO Premium V3: apagar APENAS para a conta conectada, sem tocar nos outros participantes. */
public final class NexoMensagensLote {
    private static final int TAMANHO = 150;
    private NexoMensagensLote() {}

    public static void selecionar(Activity a, FirebaseFirestore db, String caminho, String uid) {
        if (uid == null || uid.isEmpty() || a.isFinishing()) return;
        db.document(caminho).collection("mensagens")
          .orderBy("criadoEm", Query.Direction.DESCENDING).limit(TAMANHO).get()
          .addOnSuccessListener(snap -> {
              List<DocumentSnapshot> docs = snap.getDocuments();
              if (docs.isEmpty()) {
                  Toast.makeText(a,"Não há mensagens para selecionar.",Toast.LENGTH_SHORT).show();
                  return;
              }
              String[] itens = new String[docs.size()];
              boolean[] selecionado = new boolean[docs.size()];
              for (int i = 0; i < docs.size(); i++) {
                  DocumentSnapshot d = docs.get(i);
                  String t = d.getString("texto");
                  if (t == null || t.trim().isEmpty()) {
                      String tipo = d.getString("tipo");
                      t = "imagem".equals(tipo) ? "🖼 Imagem" : "🎤 Mensagem de voz";
                  }
                  t = t.replace('\n',' ').trim();
                  if (t.length() > 75) t = t.substring(0,75) + "…";
                  itens[i] = t;
              }
              new AlertDialog.Builder(a)
                .setTitle("Selecionar mensagens · NEXO")
                .setMultiChoiceItems(itens, selecionado, (dialog, i, marcado) -> selecionado[i] = marcado)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Selecionar todas", (dialog, which) ->
                    new AlertDialog.Builder(a)
                      .setTitle("Apagar as " + docs.size() + " mensagens exibidas?")
                      .setMessage("Somente para você; a outra pessoa continuará vendo as mensagens.")
                      .setNegativeButton("Cancelar",null)
                      .setPositiveButton("Apagar para mim", (d,w) -> apagarSelecionadas(a,db,uid,docs,null))
                      .show())
                .setPositiveButton("Apagar selecionadas", (dialog, which) -> {
                    java.util.ArrayList<DocumentSnapshot> escolhidas = new java.util.ArrayList<>();
                    for (int i=0;i<docs.size();i++) if (selecionado[i]) escolhidas.add(docs.get(i));
                    if (escolhidas.isEmpty()) {
                        Toast.makeText(a,"Marque uma ou mais mensagens.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new AlertDialog.Builder(a)
                      .setTitle("Apagar " + escolhidas.size() + " mensagens para mim?")
                      .setMessage("As mensagens continuam disponíveis para os outros participantes.")
                      .setNegativeButton("Cancelar",null)
                      .setPositiveButton("Apagar",(d,w) -> apagarSelecionadas(a,db,uid,escolhidas,null))
                      .show();
                }).show();
          }).addOnFailureListener(e -> Toast.makeText(a,
                  "Erro ao listar mensagens: " + e.getMessage(),Toast.LENGTH_LONG).show());
    }

    private static void apagarSelecionadas(Activity a,FirebaseFirestore db,String uid,
                                           List<DocumentSnapshot> docs,Runnable depois) {
        WriteBatch lote = db.batch();
        for (DocumentSnapshot d : docs) {
            lote.update(d.getReference(),"apagadoPara",FieldValue.arrayUnion(uid));
        }
        lote.commit().addOnSuccessListener(v -> {
            if (!a.isFinishing()) Toast.makeText(a,
                 docs.size()+" mensagens apagadas somente para você.",Toast.LENGTH_SHORT).show();
            if (depois != null) depois.run();
        }).addOnFailureListener(e -> Toast.makeText(a,
                "Falha ao apagar: " + e.getMessage(),Toast.LENGTH_LONG).show());
    }

    public static void confirmarLimpeza(Activity a, FirebaseFirestore db,
                                        String caminho, String uid, Runnable concluido) {
        if (uid == null || uid.isEmpty()) return;
        new AlertDialog.Builder(a).setTitle("Limpar esta conversa?")
          .setMessage("Todas as mensagens anteriores serão ocultadas APENAS para você, " +
                  "inclusive em outros aparelhos da sua conta. " +
                  "A amizade permanece na aba AMIGOS. Novas mensagens farão a conversa reaparecer.")
          .setNegativeButton("Cancelar",null)
          .setPositiveButton("Limpar para mim",(dialog, which) -> {
              Toast.makeText(a,"Limpando mensagens…",Toast.LENGTH_SHORT).show();
              limparPagina(a,db,caminho,uid,null,0,concluido);
          }).show();
    }

    private static void limparPagina(Activity a,FirebaseFirestore db,String caminho,
                                     String uid,DocumentSnapshot cursor,int paginas,Runnable concluido) {
        if (paginas > 300) { // evita processamento ilimitado por engano
            Toast.makeText(a,"Limite de páginas atingido; confira a conversa.",Toast.LENGTH_LONG).show();
            return;
        }
        Query q = db.document(caminho).collection("mensagens")
                    .orderBy(FieldPath.documentId()).limit(TAMANHO);
        if (cursor != null) q = q.startAfter(cursor);
        q.get().addOnSuccessListener(snap -> {
            List<DocumentSnapshot> docs = snap.getDocuments();
            if (docs.isEmpty()) {
                db.document(caminho).update("limpoEm_"+uid,FieldValue.serverTimestamp())
                   .addOnSuccessListener(v -> {
                       if (!a.isFinishing()) Toast.makeText(a,
                          "Conversa limpa somente para você.",Toast.LENGTH_LONG).show();
                       if(concluido!=null) concluido.run();
                   })
                   .addOnFailureListener(e -> Toast.makeText(a,
                          "Mensagens ocultadas, mas não foi possível atualizar a lista: "+e.getMessage(),
                          Toast.LENGTH_LONG).show());
                return;
            }
            WriteBatch lote = db.batch();
            for(DocumentSnapshot d:docs) lote.update(d.getReference(),
                    "apagadoPara",FieldValue.arrayUnion(uid));
            lote.commit().addOnSuccessListener(v ->
                   limparPagina(a,db,caminho,uid,docs.get(docs.size()-1),paginas+1,concluido))
                 .addOnFailureListener(e -> Toast.makeText(a,
                     "Limpeza interrompida. Tente novamente: "+e.getMessage(),Toast.LENGTH_LONG).show());
        }).addOnFailureListener(e -> Toast.makeText(a,
             "Erro ao consultar histórico: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }
}
