package com.danike.ai;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
public class HavePlataformasActivity extends AppCompatActivity {
 @Override protected void onCreate(Bundle b) {
  super.onCreate(b);
  setContentView(R.layout.activity_plataformas_have);
  findViewById(R.id.cardNetflix).setOnClickListener(v -> {
   Intent i = new Intent(this, SalaHAVEActivity.class);
   i.putExtra("url","https://www.netflix.com/browse");
   startActivity(i);
  });
 }
}
