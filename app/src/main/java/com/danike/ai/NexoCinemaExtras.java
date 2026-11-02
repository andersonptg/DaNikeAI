package com.danike.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.media3.ui.PlayerView;
import org.json.JSONArray;
import org.json.JSONObject;

/** Componentes independentes do catálogo NEXO, sem mudanças no tema global. */
public final class NexoCinemaExtras {
    // NEXO_CINEMA_PREMIUM_V4
    private static final int BG = Color.rgb(9, 12, 28);
    private static final int PANEL = Color.rgb(21, 26, 52);
    private static final int PURPLE = Color.rgb(170, 72, 247);
    private static final int CYAN = Color.rgb(54, 185, 255);
    private static final int TEXT_MUTED = Color.rgb(177, 186, 212);
    private NexoCinemaExtras() {}

    private static int dp(Context c, float n) {
        return Math.round(n * c.getResources().getDisplayMetrics().density);
    }
    private static GradientDrawable fundo(Context c, int a, int b, int borda, int raio) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a,b});
        d.setCornerRadius(dp(c,raio));
        if (borda != Color.TRANSPARENT) d.setStroke(Math.max(1,dp(c,1)), borda);
        return d;
    }
    private static TextView texto(Context c, String valor, int tamanho, int cor) {
        TextView t = new TextView(c);
        t.setText(valor); t.setTextSize(tamanho); t.setTextColor(cor);
        return t;
    }

    public interface OnSeason { void selecionar(int indice); }

    /** Modal central, com rolagem interna e sem girar/ocultar a tela de detalhes. */
    public static void escolherTemporada(Activity activity, JSONArray temporadas,
                                        int atual, OnSeason onChoose) {
        if (temporadas == null || temporadas.length() == 0) return;
        Dialog dialog = new Dialog(activity);
        LinearLayout caixa = new LinearLayout(activity);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setPadding(dp(activity,20), dp(activity,19), dp(activity,20), dp(activity,16));
        caixa.setBackground(fundo(activity,BG,PANEL,PURPLE,23));

        TextView titulo = texto(activity,"Selecionar temporada",21,Color.WHITE);
        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null, android.graphics.Typeface.BOLD);
        caixa.addView(titulo, new LinearLayout.LayoutParams(-1, dp(activity,43)));
        TextView subtitulo = texto(activity,"Escolha qual temporada deseja assistir",12,TEXT_MUTED);
        subtitulo.setGravity(Gravity.CENTER);
        caixa.addView(subtitulo);

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout lista = new LinearLayout(activity);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(0,dp(activity,12),0,dp(activity,4));
        for (int i=0; i<temporadas.length();i++) {
            final int indice=i;
            JSONObject temporada=temporadas.optJSONObject(i);
            if (temporada == null) continue;
            int n=temporada.optInt("numero",i+1);
            JSONArray episodios=temporada.optJSONArray("episodios");
            boolean selecionada = indice == atual;
            LinearLayout item=new LinearLayout(activity);
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(activity,16),0,dp(activity,14),0);
            item.setBackground(fundo(activity,
                    selecionada ? Color.rgb(64,32,115) : Color.rgb(19,25,49),
                    selecionada ? Color.rgb(30,76,134) : Color.rgb(19,25,49),
                    selecionada ? PURPLE : Color.rgb(46,59,89),14));
            LinearLayout txt=new LinearLayout(activity); txt.setOrientation(LinearLayout.VERTICAL);
            TextView nm=texto(activity,"Temporada " + n,16,Color.WHITE);
            nm.setTypeface(null,android.graphics.Typeface.BOLD);
            txt.addView(nm);
            TextView ep=texto(activity,(episodios!=null?episodios.length():0)+" episódios",11,TEXT_MUTED);
            txt.addView(ep);
            item.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
            TextView check=texto(activity,selecionada?"✓":"›",20,selecionada?CYAN:TEXT_MUTED);
            check.setGravity(Gravity.CENTER);
            item.addView(check,new LinearLayout.LayoutParams(dp(activity,27),-2));
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(-1,dp(activity,66));
            itemLp.setMargins(0,dp(activity,5),0,dp(activity,5));
            lista.addView(item,itemLp);
            item.setOnClickListener(v->{ onChoose.selecionar(indice); dialog.dismiss(); });
        }
        scroll.addView(lista);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, -2);
        int limite = Math.min(dp(activity,365), (int)(activity.getResources().getDisplayMetrics().heightPixels*.50f));
        scroll.setLayoutParams(slp);
        caixa.addView(scroll,slp);
        scroll.post(() -> {
            if (scroll.getHeight()>limite) {
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)scroll.getLayoutParams();
                lp.height=limite; scroll.setLayoutParams(lp);
            }
        });
        TextView fechar=texto(activity,"FECHAR",13,TEXT_MUTED);
        fechar.setGravity(Gravity.CENTER);
        fechar.setPadding(0,dp(activity,13),0,dp(activity,6));
        caixa.addView(fechar);
        fechar.setOnClickListener(v->dialog.dismiss());
        dialog.setContentView(caixa);
        Window win=dialog.getWindow();
        if (win != null) {
            win.setBackgroundDrawableResource(android.R.color.transparent);
            win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams wa=win.getAttributes();wa.dimAmount=.76f;win.setAttributes(wa);
        }
        dialog.show();
        if(dialog.getWindow()!=null){
            int largura = Math.min(dp(activity,430),activity.getResources().getDisplayMetrics().widthPixels-dp(activity,36));
            dialog.getWindow().setLayout(largura,-2);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    /** Avaliação com estrelas vetoriais, resultado individual salvo localmente. */
    public static void avaliar(Activity activity, String titulo,
                               SharedPreferences prefs, String chave) {
        final int[] nota={Math.max(0,Math.min(5,prefs.getInt(chave,0)))};
        final String[] rotulos={"Toque para avaliar","Não gostei","Regular","Gostei","Muito bom","Excelente"};
        Dialog dialog=new Dialog(activity);
        LinearLayout caixa=new LinearLayout(activity);
        caixa.setOrientation(LinearLayout.VERTICAL);
        caixa.setPadding(dp(activity,22),dp(activity,22),dp(activity,22),dp(activity,18));
        caixa.setBackground(fundo(activity,BG,PANEL,PURPLE,22));
        TextView cab=texto(activity,"Avalie este título",21,Color.WHITE);
        cab.setTypeface(null,android.graphics.Typeface.BOLD);
        cab.setGravity(Gravity.CENTER);
        caixa.addView(cab);
        TextView nome=texto(activity,titulo,14,TEXT_MUTED);
        nome.setGravity(Gravity.CENTER);nome.setMaxLines(2);
        LinearLayout.LayoutParams nlp=new LinearLayout.LayoutParams(-1,-2);
        nlp.setMargins(0,dp(activity,8),0,dp(activity,17));caixa.addView(nome,nlp);
        LinearLayout estrelas=new LinearLayout(activity);
        estrelas.setGravity(Gravity.CENTER);
        final ImageView[] icones=new ImageView[5];
        TextView descricao=texto(activity,rotulos[nota[0]],13,Color.rgb(228,196,255));
        descricao.setGravity(Gravity.CENTER);
        for(int i=0;i<5;i++){
            final int valor=i+1;
            ImageView star=new ImageView(activity);
            star.setImageDrawable(new StarDrawable(nota[0]>=valor));
            star.setContentDescription(valor+" estrela"+(valor==1?"":"s"));
            star.setClickable(true); star.setFocusable(true);
            star.setPadding(dp(activity,4),dp(activity,4),dp(activity,4),dp(activity,4));
            LinearLayout.LayoutParams stlp=new LinearLayout.LayoutParams(dp(activity,48),dp(activity,48));
            estrelas.addView(star,stlp);icones[i]=star;
            star.setOnClickListener(v->{
                nota[0]=valor;
                descricao.setText(rotulos[valor]);
                for(int k=0;k<5;k++)icones[k].setImageDrawable(new StarDrawable(k<valor));
            });
        }
        caixa.addView(estrelas);
        LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(activity,38));
        descricao.setGravity(Gravity.CENTER); caixa.addView(descricao,dlp);
        TextView dica=texto(activity,"Sua nota fica salva neste aparelho.",11,TEXT_MUTED);
        dica.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintLp=new LinearLayout.LayoutParams(-1,-2);
        hintLp.setMargins(0,dp(activity,5),0,dp(activity,13));caixa.addView(dica,hintLp);
        TextView salvar=texto(activity,"SALVAR AVALIAÇÃO",14,Color.WHITE);
        salvar.setTypeface(null,android.graphics.Typeface.BOLD);
        salvar.setGravity(Gravity.CENTER);
        salvar.setBackground(fundo(activity,Color.rgb(144,38,226),Color.rgb(43,98,221),PURPLE,15));
        caixa.addView(salvar,new LinearLayout.LayoutParams(-1,dp(activity,51)));
        salvar.setOnClickListener(v->{
            if(nota[0]==0){descricao.setText("Selecione de 1 a 5 estrelas");return;}
            prefs.edit().putInt(chave,nota[0]).apply();dialog.dismiss();
            android.widget.Toast.makeText(activity,"Avaliação salva: "+nota[0]+"/5 estrelas",android.widget.Toast.LENGTH_SHORT).show();
        });
        dialog.setContentView(caixa);
        dialog.show();
        if(dialog.getWindow()!=null){
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(Math.min(dp(activity,420),activity.getResources().getDisplayMetrics().widthPixels-dp(activity,30)),-2);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    private static final class StarDrawable extends Drawable {
        private final boolean filled;
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        StarDrawable(boolean filled){this.filled=filled;}
        @Override public void draw(Canvas c){
            android.graphics.Rect b=getBounds();
            c.save();c.translate(b.left,b.top);c.scale(b.width()/100f,b.height()/100f);
            p.setColor(filled?Color.rgb(255,190,61):Color.rgb(149,155,188));
            p.setStyle(filled?Paint.Style.FILL:Paint.Style.STROKE);p.setStrokeWidth(6);
            p.setStrokeJoin(Paint.Join.ROUND);
            Path path=new Path();
            for(int i=0;i<10;i++){
                double a=-Math.PI/2+i*Math.PI/5;
                float r=i%2==0?43:19;
                float x=50+(float)Math.cos(a)*r;float y=50+(float)Math.sin(a)*r;
                if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
            }
            path.close();c.drawPath(path,p);c.restore();
        }
        @Override public void setAlpha(int alpha){p.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);invalidateSelf();}
        @Override public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }

    /**
     * Controles discretos de gesto: lado esquerdo volume, lado direito brilho.
     * Eles nunca modificam configurações globais de brilho do telefone.
     * Um toque mostra os controles nativos do ExoPlayer, sem controles permanentes.
     */
    public static void adicionarGestos(Activity activity, FrameLayout raiz,
                                        PlayerView playerView, Dialog dialog) {
        adicionarGestos(activity, raiz, playerView, dialog, playerView::showController);
    }

    /** O cinema NEXO pode usar controles próprios sem abrir os controles nativos. */
    public static void adicionarGestos(Activity activity, FrameLayout raiz,
                                        PlayerView playerView, Dialog dialog, Runnable aoToque) {
        final AudioManager audio=(AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        final int volumeMax=audio!=null?audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC):15;
        final int largura=Math.max(dp(activity,64), (int)(activity.getResources().getDisplayMetrics().widthPixels*.15f));
        for(int lado=0;lado<2;lado++){
            final boolean brilho=lado==1;
            FrameLayout borda=new FrameLayout(activity);
            borda.setBackgroundColor(Color.TRANSPARENT);
            borda.setContentDescription(brilho?"Deslize para brilho":"Deslize para volume");
            FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(largura,-1,
                    Gravity.TOP|(brilho?Gravity.END:Gravity.START));
            raiz.addView(borda,lp);
            final float[] inicio={0};final int[] valorInicio={0};final boolean[] gesto={false};
            final float[] brilhoOriginal={-1f};
            final TextView aviso=texto(activity,"",14,Color.WHITE);
            aviso.setGravity(Gravity.CENTER);
            aviso.setPadding(dp(activity,12),dp(activity,9),dp(activity,12),dp(activity,9));
            aviso.setBackground(fundo(activity,Color.argb(215,21,25,52),Color.argb(215,31,22,60),PURPLE,15));
            aviso.setVisibility(View.GONE);
            aviso.setClickable(false);
            FrameLayout.LayoutParams avisolp=new FrameLayout.LayoutParams(dp(activity,132),dp(activity,46),
                    Gravity.CENTER_VERTICAL|(brilho?Gravity.END:Gravity.START));
            avisolp.setMargins(dp(activity,12),0,dp(activity,12),0);
            raiz.addView(aviso,avisolp);
            final Runnable esconder=()->aviso.setVisibility(View.GONE);
            borda.setOnTouchListener((v,e)->{
                switch(e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        inicio[0]=e.getY(); gesto[0]=false;
                        if(brilho){
                            Window win=dialog.getWindow();
                            float val=win!=null?win.getAttributes().screenBrightness:-1;
                            if(val<0)val=.5f;
                            brilhoOriginal[0]=val;
                        }else valorInicio[0]=audio!=null?audio.getStreamVolume(AudioManager.STREAM_MUSIC):0;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float desloc=(inicio[0]-e.getY())/Math.max(1f,borda.getHeight());
                        if(Math.abs(inicio[0]-e.getY())>dp(activity,12))gesto[0]=true;
                        if(!gesto[0])return true;
                        if(brilho){
                            float novo=Math.max(.05f,Math.min(1f,brilhoOriginal[0]+desloc*1.35f));
                            Window win=dialog.getWindow();
                            if(win!=null){WindowManager.LayoutParams wa=win.getAttributes();
                                wa.screenBrightness=novo;win.setAttributes(wa);}
                            aviso.setText("Brilho  "+Math.round(novo*100)+"%");
                        }else if(audio!=null){
                            int novo=Math.max(0,Math.min(volumeMax,valorInicio[0]+Math.round(desloc*volumeMax*1.5f)));
                            audio.setStreamVolume(AudioManager.STREAM_MUSIC,novo,0);
                            aviso.setText("Volume  "+Math.round(novo*100f/Math.max(1,volumeMax))+"%");
                        }
                        aviso.setVisibility(View.VISIBLE);
                        aviso.removeCallbacks(esconder);aviso.postDelayed(esconder,950);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!gesto[0]){
                            if (aoToque != null) aoToque.run();
                        }else{
                            aviso.removeCallbacks(esconder);aviso.postDelayed(esconder,850);
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        aviso.removeCallbacks(esconder);aviso.setVisibility(View.GONE);return true;
                }
                return true;
            });
        }
    }
}
