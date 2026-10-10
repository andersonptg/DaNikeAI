package com.danike.ai;
import android.app.Dialog; import android.content.Context; import android.content.Intent; import android.graphics.Color; import android.graphics.drawable.ColorDrawable; import android.net.Uri; import android.os.Handler; import android.os.Looper; import android.view.*; import android.widget.*; import androidx.core.content.FileProvider;
import java.io.*; import java.net.*; import java.util.concurrent.Executors;
public class AtualizacaoTelefoneDialog {
 public static void show(Context ctx, String versao, String apkUrl, String changelog) {
  Dialog d = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
  View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_atualizacao_premium, null);
  d.setContentView(v);
  d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
  d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
  d.setCancelable(false);
  TextView tvV = v.findViewById(R.id.txtVersao); TextView tvC = v.findViewById(R.id.txtChangelog);
  Button btnA = v.findViewById(R.id.btnAtualizar); Button btnD = v.findViewById(R.id.btnDepois);
  ProgressBar pb = v.findViewById(R.id.progressBar); ProgressBar pc = v.findViewById(R.id.progressCirculo); TextView ti = v.findViewById(R.id.txtProgressInfo);
  tvV.setText("v" + versao + " • NOVO");
  if (changelog!= null &&!changelog.trim().isEmpty()) tvC.setText(changelog);
  btnD.setOnClickListener(x -> d.dismiss());
  btnA.setOnClickListener(x -> {
   btnA.setEnabled(false); btnD.setVisibility(View.GONE);
   pb.setVisibility(View.VISIBLE); pc.setVisibility(View.VISIBLE); ti.setVisibility(View.VISIBLE);
   Executors.newSingleThreadExecutor().execute(() -> {
    try {
     File apk = new File(ctx.getExternalFilesDir(null), "DaNikeAI-" + versao + ".apk");
     HttpURLConnection c = (HttpURLConnection) new URL(apkUrl).openConnection(); c.connect();
     int total = c.getContentLength(); InputStream in = c.getInputStream(); FileOutputStream out = new FileOutputStream(apk);
     byte[] b = new byte[8192]; int len; long dw = 0; Handler m = new Handler(Looper.getMainLooper()); long start = System.currentTimeMillis();
     while ((len = in.read(b))!= -1) { out.write(b,0,len); dw+=len; int p = total>0?(int)(dw*100/total):0;
      long el = (System.currentTimeMillis()-start)/1000; long rem = p>0?(el*100/p)-el:0;
      m.post(() -> { pb.setProgress(p); pc.setProgress(p); btnA.setText(p+"%"); ti.setText("Baixando atualização... "+p+"% • ~"+rem+"s restantes"); });
     }
     out.close(); in.close();
     m.post(() -> { d.dismiss(); Intent i = new Intent(Intent.ACTION_VIEW); Uri u = FileProvider.getUriForFile(ctx, ctx.getPackageName()+".provider", apk); i.setDataAndType(u, "application/vnd.android.package-archive"); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_NEW_TASK); ctx.startActivity(i); });
    } catch (Exception e){ new Handler(Looper.getMainLooper()).post(() -> { Toast.makeText(ctx,"Erro: "+e.getMessage(),Toast.LENGTH_LONG).show(); btnA.setEnabled(true); btnA.setText("Tentar novamente"); pb.setVisibility(View.GONE); pc.setVisibility(View.GONE); ti.setVisibility(View.GONE); btnD.setVisibility(View.VISIBLE); }); }
   });
  });
  d.show();
 }
}
