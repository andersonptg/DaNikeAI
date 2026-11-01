package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;

/** Área administrativa NEXO; ações protegidas por regras Firestore do grupo. */
public final class NexoGrupoPainel {
    private NexoGrupoPainel() {}

    private static String uid() {
        return FirebaseAuth.getInstance().getCurrentUser() == null ? "" :
            FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    private static void aviso(Activity a,String s) {
        if(!a.isFinishing()) Toast.makeText(a,s,Toast.LENGTH_LONG).show();
    }
    private static int dp(Activity a,int n) {
        return (int)(n*a.getResources().getDisplayMetrics().density+.5f);
    }
    public static void abrir(Activity a,FirebaseFirestore db,String grupoId,Runnable aoSair) {
        if(uid().isEmpty()||grupoId==null||grupoId.isEmpty()) return;
        DocumentReference grupo=db.collection("grupos").document(grupoId);
        grupo.get().addOnSuccessListener(doc -> {
            if(!doc.exists()){aviso(a,"Grupo indisponível.");return;}
            String tipo=doc.getString("tipo");
            String servico=doc.getString("servicoHave");
            if("HAVE".equalsIgnoreCase(tipo)||(servico!=null&&!servico.isEmpty())){
                aviso(a,"Salas HAVE são gerenciadas pela área HAVE.");return;
            }
            String admin=doc.getString("administradorUid");
            boolean souAdm=uid().equals(admin);
            String nome=doc.getString("nome");
            String[] opcoes = souAdm ? new String[]{
                "👥 Ver participantes", "✏️ Editar nome e descrição",
                "➕ Adicionar participante", "🗑 Apagar grupo definitivamente"
            } : new String[]{"👥 Ver participantes", "🚪 Sair do grupo"};
            new AlertDialog.Builder(a)
                .setTitle((nome==null?"Grupo":nome)+(souAdm?" · Você é ADM":""))
                .setMessage((doc.getString("descricao")==null?"":doc.getString("descricao")))
                .setItems(opcoes,(dialog,pos)->{
                    if(pos==0) mostrarMembros(a,db,grupoId);
                    else if(souAdm&&pos==1) editarGrupo(a,db,grupoId);
                    else if(souAdm&&pos==2) adicionarMembro(a,db,grupoId);
                    else if(souAdm&&pos==3) confirmarApagar(a,db,grupoId,aoSair);
                    else if(!souAdm&&pos==1) sairDoGrupo(a,db,grupoId,aoSair);
                }).setNegativeButton("Fechar",null).show();
        }).addOnFailureListener(e->aviso(a,"Erro ao abrir grupo: "+e.getMessage()));
    }

    private static void mostrarMembros(Activity a,FirebaseFirestore db,String id) {
        db.collection("grupos").document(id).get().addOnSuccessListener(grupo -> {
            List<?> participantes=(List<?>)grupo.get("participantes");
            if(participantes==null){aviso(a,"Sem participantes cadastrados.");return;}
            String admin=grupo.getString("administradorUid");
            boolean souAdm=uid().equals(admin);
            ScrollView sc=new ScrollView(a);
            LinearLayout lista=new LinearLayout(a);lista.setOrientation(LinearLayout.VERTICAL);
            lista.setPadding(dp(a,12),dp(a,8),dp(a,12),dp(a,8));
            sc.addView(lista);
            for(Object obj:participantes) {
                if(!(obj instanceof String))continue;
                String membro=(String)obj;
                TextView item=new TextView(a);item.setTextColor(Color.WHITE);
                item.setTextSize(16);item.setPadding(dp(a,14),dp(a,16),dp(a,14),dp(a,16));
                item.setText((membro.equals(admin)?"👑 ":"👤 ") +
                     (membro.equals(uid())?"Você":membro.substring(0,Math.min(10,membro.length())))+
                     (membro.equals(admin)?" · ADM":""));
                GradientDrawable fundo=new GradientDrawable();
                fundo.setColor(Color.rgb(12,28,50));fundo.setCornerRadius(dp(a,12));
                fundo.setStroke(dp(a,1),Color.rgb(24,103,189));item.setBackground(fundo);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
                lp.bottomMargin=dp(a,7);lista.addView(item,lp);
                db.collection("perfis_publicos").document(membro).get()
                    .addOnSuccessListener(perfil -> {
                        String nome=perfil.getString("nome");
                        if(nome!=null&&!nome.trim().isEmpty()) item.setText(
                          (membro.equals(admin)?"👑 ":"👤 ")+nome+
                          (membro.equals(uid())?" (você)":"")+
                          (membro.equals(admin)?" · ADM":""));
                    });
                if(souAdm&&!membro.equals(admin)){
                    item.setOnClickListener(v->new AlertDialog.Builder(a)
                        .setTitle("Remover participante?")
                        .setMessage("Esse usuário perderá acesso ao grupo.")
                        .setNegativeButton("Cancelar",null)
                        .setPositiveButton("Remover",(d,w)-> {
                            db.collection("grupos").document(id).update(
                               "participantes", FieldValue.arrayRemove(membro))
                               .addOnSuccessListener(x->{aviso(a,"Participante removido.");mostrarMembros(a,db,id);})
                               .addOnFailureListener(e->aviso(a,"Falha ao remover: "+e.getMessage()));
                        }).show());
                }
            }
            AlertDialog dlg=new AlertDialog.Builder(a)
                .setTitle("👥 Participantes · "+participantes.size())
                .setView(sc).setPositiveButton("Fechar",null).create();
            dlg.show();
            if(dlg.getWindow()!=null)dlg.getWindow().setLayout(
                 Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(a,26),dp(a,550)),
                 -2);
        }).addOnFailureListener(e->aviso(a,"Não foi possível carregar participantes."));
    }

    private static void editarGrupo(Activity a,FirebaseFirestore db,String id) {
        DocumentReference ref=db.collection("grupos").document(id);
        ref.get().addOnSuccessListener(doc->{
            if(!uid().equals(doc.getString("administradorUid"))){aviso(a,"Apenas o ADM pode editar.");return;}
            LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);
            l.setPadding(dp(a,18),dp(a,6),dp(a,18),dp(a,6));
            EditText nome=new EditText(a);nome.setHint("Nome do grupo");
            nome.setSingleLine(true);nome.setText(doc.getString("nome"));l.addView(nome);
            EditText desc=new EditText(a);desc.setHint("Descrição do grupo");
            desc.setMinLines(2);desc.setText(doc.getString("descricao"));l.addView(desc);
            new AlertDialog.Builder(a).setTitle("Editar grupo · ADM")
                .setView(l).setNegativeButton("Cancelar",null)
                .setPositiveButton("Salvar",(d,w)->{
                    String novo=nome.getText().toString().trim();
                    if(novo.isEmpty()){aviso(a,"O grupo precisa de um nome.");return;}
                    ref.update("nome",novo,"descricao",desc.getText().toString().trim())
                       .addOnSuccessListener(x->aviso(a,"Grupo atualizado. Reabra para ver o novo nome."))
                       .addOnFailureListener(e->aviso(a,"Erro ao editar: "+e.getMessage()));
                }).show();
        });
    }

    private static void adicionarMembro(Activity a,FirebaseFirestore db,String id) {
        DocumentReference grupo=db.collection("grupos").document(id);
        grupo.get().addOnSuccessListener(g -> {
            if(!uid().equals(g.getString("administradorUid"))){aviso(a,"Somente ADM adiciona membros.");return;}
            List<?> atuais=(List<?>)g.get("participantes");
            db.collection("perfis_publicos").limit(150).get()
              .addOnSuccessListener(perfis -> {
                  ArrayList<String> ids=new ArrayList<>();
                  ArrayList<String> nomes=new ArrayList<>();
                  for(QueryDocumentSnapshot p:perfis) {
                      if(p.getId().equals(uid())||(atuais!=null&&atuais.contains(p.getId())))continue;
                      if(Boolean.FALSE.equals(p.getBoolean("perfilPublico")))continue;
                      ids.add(p.getId());
                      String nome=p.getString("nome");
                      nomes.add(nome==null||nome.isEmpty()?p.getId():nome);
                  }
                  if(ids.isEmpty()){aviso(a,"Nenhum outro perfil público disponível.");return;}
                  new AlertDialog.Builder(a).setTitle("Adicionar participante · ADM")
                      .setItems(nomes.toArray(new String[0]),(d,index)->
                          new AlertDialog.Builder(a)
                          .setTitle("Adicionar "+nomes.get(index)+"?")
                          .setNegativeButton("Cancelar",null)
                          .setPositiveButton("Adicionar",(di,w)->grupo.update(
                              "participantes",FieldValue.arrayUnion(ids.get(index)))
                              .addOnSuccessListener(x->aviso(a,"Participante adicionado."))
                              .addOnFailureListener(e->aviso(a,"Erro ao adicionar: "+e.getMessage())))
                          .show())
                      .setNegativeButton("Cancelar",null).show();
              }).addOnFailureListener(e->aviso(a,"Não foi possível pesquisar perfis."));
        });
    }

    private static void sairDoGrupo(Activity a,FirebaseFirestore db,String id,Runnable concluir) {
        new AlertDialog.Builder(a).setTitle("Sair do grupo?")
            .setMessage("Você deixará de receber mensagens deste grupo.")
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Sair",(d,w)->db.collection("grupos").document(id)
                .update("participantes",FieldValue.arrayRemove(uid()))
                .addOnSuccessListener(x->{aviso(a,"Você saiu do grupo.");if(concluir!=null)concluir.run();})
                .addOnFailureListener(e->aviso(a,"Não foi possível sair: "+e.getMessage())))
            .show();
    }

    private static void confirmarApagar(Activity a,FirebaseFirestore db,String id,Runnable concluir) {
        EditText confirmar=new EditText(a);confirmar.setSingleLine(true);
        confirmar.setHint("Digite APAGAR");
        confirmar.setPadding(dp(a,20),dp(a,8),dp(a,20),dp(a,8));
        new AlertDialog.Builder(a).setTitle("EXCLUIR GRUPO DEFINITIVAMENTE?")
          .setMessage("Somente o ADM pode excluir. As mensagens do Firestore serão apagadas " +
                  "para todos; o processo é irreversível. Arquivos antigos no Storage " +
                  "podem exigir limpeza separada.")
          .setView(confirmar).setNegativeButton("Cancelar",null)
          .setPositiveButton("Excluir",(d,w)->{
              if(!"APAGAR".equals(confirmar.getText().toString().trim())) {
                  aviso(a,"Exclusão cancelada: digite APAGAR exatamente.");return;
              }
              DocumentReference grupo=db.collection("grupos").document(id);
              grupo.get().addOnSuccessListener(doc -> {
                  if(!uid().equals(doc.getString("administradorUid"))){
                      aviso(a,"Só o administrador pode excluir o grupo.");return;
                  }
                  aviso(a,"Excluindo mensagens e grupo…");
                  apagarPagina(a,db,grupo,0,concluir);
              }).addOnFailureListener(e->aviso(a,"Grupo indisponível: "+e.getMessage()));
          }).show();
    }
    private static void apagarPagina(Activity a,FirebaseFirestore db,
                                    DocumentReference grupo,int pagina,Runnable concluido) {
        if(pagina>300){aviso(a,"Limite de páginas. Conclua a limpeza antes de excluir.");return;}
        grupo.collection("mensagens").limit(150).get().addOnSuccessListener(snap->{
            if(snap.isEmpty()){
                grupo.delete().addOnSuccessListener(x->{
                    aviso(a,"Grupo e mensagens excluídos do Firestore.");
                    if(concluido!=null)concluido.run();
                }).addOnFailureListener(e->aviso(a,"Falha ao excluir grupo: "+e.getMessage()));
                return;
            }
            WriteBatch batch=db.batch();
            for(DocumentSnapshot msg:snap.getDocuments())batch.delete(msg.getReference());
            batch.commit().addOnSuccessListener(x->apagarPagina(a,db,grupo,pagina+1,concluido))
              .addOnFailureListener(e->aviso(a,"Limpeza interrompida: "+e.getMessage()));
        }).addOnFailureListener(e->aviso(a,"Não foi possível buscar mensagens: "+e.getMessage()));
    }
}
