package com.danike.ai;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

/**
 * NEXO Watch V2: player sem bordas, modo cinema, chat pop-up e bolhas temporárias.
 * Compartilha o mesmo ExoPlayer da sala. Não cria um segundo stream.
 */
public final class NexoWatchCinemaOverlayV2 {
    public interface Mensagem { void enviar(String texto); }
    private final Activity a;
    private final ExoPlayer player;
    private final PlayerView origem;
    private final boolean dono;
    private final Mensagem enviar;
    private final Handler h=new Handler(Looper.getMainLooper());
    private final String titulo;
    private Dialog dialog;
    private PlayerView tela;
    private LinearLayout bolhas, historico;
    private LinearLayout painelChat;
    private boolean aberto;
    private int orientacaoAnterior;

    public NexoWatchCinemaOverlayV2(Activity a, ExoPlayer player,
                                   PlayerView origem, boolean dono, String titulo,
                                   Mensagem enviar) {
        this.a=a;this.player=player;this.origem=origem;this.dono=dono;
        this.titulo=titulo;this.enviar=enviar;
    }
    private int dp(float v){return Math.round(v*a.getResources().getDisplayMetrics().density);}
    private GradientDrawable bg(int c,int contorno,int raio){
        GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(raio));
        if(contorno!=Color.TRANSPARENT)d.setStroke(dp(1),contorno);return d;
    }
    private TextView botao(String texto,String icone){
        TextView t=new TextView(a);t.setText(texto);t.setTextSize(13);
        t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);
        t.setPadding(dp(11),dp(8),dp(11),dp(8));
        t.setBackground(bg(0xCC151A38,0xAA8E56EF,14));
        android.graphics.drawable.Drawable d=NexoWatchIconsV2.icon(icone,Color.WHITE);
        d.setBounds(0,0,dp(18),dp(18)); t.setCompoundDrawables(d,null,null,null);
        t.setCompoundDrawablePadding(dp(7));return t;
    }
    public boolean estaAberto(){return aberto && dialog!=null && dialog.isShowing();}

    public void abrir(){
        if(estaAberto() || player==null || origem==null) return;
        orientacaoAnterior=a.getRequestedOrientation();
        dialog=new Dialog(a,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout raiz=new FrameLayout(a);
        raiz.setBackgroundColor(Color.BLACK);
        tela=new PlayerView(a);
        tela.setUseController(dono); // somente quem criou controla a sessão
        tela.setControllerAutoShow(false);
        tela.setControllerShowTimeoutMs(3500);
        tela.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM); // preencher sem distorcer
        final float[] zoomCinema={1.0f};
        final android.view.ScaleGestureDetector pinça =
                new android.view.ScaleGestureDetector(a,
                    new android.view.ScaleGestureDetector.SimpleOnScaleGestureListener(){
                        @Override public boolean onScale(
                                android.view.ScaleGestureDetector d) {
                            zoomCinema[0]=Math.max(0.8f,Math.min(2.4f,
                                    zoomCinema[0]*d.getScaleFactor()));
                            boolean ajustar=zoomCinema[0]<0.95f;
                            tela.setResizeMode(ajustar
                                    ? AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    : AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                            android.view.View superficie=tela.getVideoSurfaceView();
                            if(superficie!=null){
                                float escala=ajustar?1f:Math.max(1f,zoomCinema[0]);
                                superficie.setScaleX(escala);
                                superficie.setScaleY(escala);
                            }
                            return true;
                        }
                    });
        tela.setOnTouchListener((v,e)->{pinça.onTouchEvent(e);return false;});
        tela.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        origem.setPlayer(null);
        tela.setPlayer(player);
        raiz.addView(tela,new FrameLayout.LayoutParams(-1,-1));

        // Controles de brilho (lado direito) e volume (lado esquerdo), já usados na V4.
        NexoCinemaExtras.adicionarGestos(a,raiz,tela,dialog);

        LinearLayout superior=new LinearLayout(a);
        superior.setGravity(Gravity.CENTER_VERTICAL);
        superior.setPadding(dp(11),dp(5),dp(11),dp(5));
        superior.setBackgroundColor(0x44000000);
        TextView fechar=botao("","back");fechar.setContentDescription("Voltar à sala");
        fechar.setOnClickListener(v->fechar());
        superior.addView(fechar,new LinearLayout.LayoutParams(dp(43),dp(42)));
        TextView nome=new TextView(a);nome.setText(titulo);nome.setTextSize(14);
        nome.setTextColor(Color.WHITE);nome.setSingleLine(true);
        nome.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nome.setPadding(dp(10),0,dp(10),0);
        superior.addView(nome,new LinearLayout.LayoutParams(0,-2,1));
        TextView tv=botao("","tv");tv.setContentDescription("Espelhar na TV");
        tv.setOnClickListener(v->{try{a.startActivity(new Intent(Settings.ACTION_CAST_SETTINGS));}
            catch(Exception e){android.widget.Toast.makeText(a,"Transmissão indisponível neste aparelho",0).show();}});
        superior.addView(tv,new LinearLayout.LayoutParams(dp(43),dp(42)));
        TextView chat=botao("","chat");chat.setContentDescription("Abrir chat flutuante");
        superior.addView(chat,new LinearLayout.LayoutParams(dp(43),dp(42)));
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(51),Gravity.TOP);
        raiz.addView(superior,sp);

        bolhas=new LinearLayout(a);bolhas.setOrientation(LinearLayout.VERTICAL);
        bolhas.setGravity(Gravity.BOTTOM);
        FrameLayout.LayoutParams b=new FrameLayout.LayoutParams(
                Math.min(dp(320),a.getResources().getDisplayMetrics().widthPixels/2),
                dp(188), Gravity.LEFT|Gravity.BOTTOM);
        b.setMargins(dp(16),0,0,dp(83));
        raiz.addView(bolhas,b);
        // Não interceptar os toques do vídeo; apenas as mensagens recebidas.
        bolhas.setClickable(false);

        painelChat=new LinearLayout(a);painelChat.setOrientation(LinearLayout.VERTICAL);
        painelChat.setPadding(dp(12),dp(10),dp(12),dp(12));
        painelChat.setBackground(bg(0xF1090D24,0xFF9855EA,20));
        painelChat.setVisibility(View.GONE);
        historico=new LinearLayout(a);historico.setOrientation(LinearLayout.VERTICAL);
        android.widget.ScrollView scroll=new android.widget.ScrollView(a);
        scroll.setFillViewport(false);scroll.addView(historico);
        painelChat.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout escrever=new LinearLayout(a);escrever.setGravity(Gravity.CENTER_VERTICAL);
        EditText campo=new EditText(a);campo.setSingleLine(true);
        campo.setTextSize(14);campo.setTextColor(Color.WHITE);
        campo.setHintTextColor(0xFFADBAD9);campo.setHint("Mensagem para a sala");
        campo.setPadding(dp(12),dp(7),dp(12),dp(7));
        campo.setBackground(bg(0xFF1A214A,0xFF356CDE,14));
        escrever.addView(campo,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView mandar=botao("","send");mandar.setContentDescription("Enviar mensagem");
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(dp(45),dp(42));mp.leftMargin=dp(7);
        escrever.addView(mandar,mp);
        mandar.setOnClickListener(v->{String texto=campo.getText().toString().trim();
            if(!texto.isEmpty()){enviar.enviar(texto);campo.setText("");}});
        painelChat.addView(escrever,new LinearLayout.LayoutParams(-1,-2));
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(
                Math.min(dp(430),a.getResources().getDisplayMetrics().widthPixels-dp(30)),
                dp(236),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        cp.setMargins(dp(12),0,dp(12),dp(16));
        raiz.addView(painelChat,cp);
        chat.setOnClickListener(v->{
            boolean mostrar=painelChat.getVisibility()!=View.VISIBLE;
            painelChat.setVisibility(mostrar?View.VISIBLE:View.GONE);
            if(mostrar)bolhas.setVisibility(View.GONE);
            else bolhas.setVisibility(View.VISIBLE);
        });
        // Toque no vídeo exibe os controles da reprodução; toque no chat abre o pop-up.
        dialog.setContentView(raiz);
        dialog.setOnDismissListener(d->{
            aberto=false;
            h.removeCallbacksAndMessages(null);
            if(tela!=null){tela.setPlayer(null);tela=null;}
            if(origem!=null && player!=null)origem.setPlayer(player);
            a.setRequestedOrientation(orientacaoAnterior);
            dialog=null;bolhas=null;historico=null;painelChat=null;
        });
        dialog.show();
        Window w=dialog.getWindow();
        if(w!=null){
            w.setLayout(-1,-1);
            w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        aberto=true;
        tela.hideController();
        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }
    public void novaMensagem(String autor,String texto){
        if(!estaAberto())return;
        String nome=autor==null?"Amigo":autor;
        String frase=texto==null?"":texto;
        if(frase.length()>170)frase=frase.substring(0,170)+"…";
        TextView item=new TextView(a);
        item.setText(nome+"  ·  "+frase);
        item.setTextSize(12.5f);item.setTextColor(Color.WHITE);
        item.setMaxLines(2);item.setPadding(dp(11),dp(8),dp(11),dp(8));
        item.setBackground(bg(0xDE192448,0xB05A8DFF,16));
        if(bolhas!=null){
            if(bolhas.getChildCount()>=3)bolhas.removeViewAt(0);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(5);bolhas.addView(item,lp);
            h.postDelayed(()->{if(bolhas!=null && item.getParent()==bolhas)bolhas.removeView(item);},4800);
        }
        if(historico!=null){
            TextView mensagem=new TextView(a);mensagem.setText(nome+": "+frase);
            mensagem.setTextColor(Color.WHITE);mensagem.setTextSize(12.5f);
            mensagem.setPadding(dp(7),dp(6),dp(7),dp(6));
            historico.addView(mensagem);
            while(historico.getChildCount()>15)historico.removeViewAt(0);
        }
    }
    public void fechar(){if(dialog!=null && dialog.isShowing())dialog.dismiss();}
}
