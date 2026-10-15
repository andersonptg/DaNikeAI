package com.danike.ai;
import android.app.Activity; import android.os.Bundle; import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
public class LimpezaActivity extends Activity {
    protected void onCreate(Bundle b){
        super.onCreate(b);
        FirebaseFirestore.getInstance().collection("salas_have").get().addOnSuccessListener(snap -> {
            int total = snap.size();
            for (int i=0;i<total;i++) snap.getDocuments().get(i).getReference().delete();
            Toast.makeText(this,"Apagadas "+total+" salas!",Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e->{
            Toast.makeText(this,"Erro: "+e.getMessage(),Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
