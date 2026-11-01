package com.danike.ai;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Teclado NEXO interno do chat de grupos; o teclado Android continua disponível. */
public final class NexoTecladoGrupo {
    private final Activity a;
    private final LinearLayout container;
    private final EditText entrada;
    private final Runnable sistema;
    private boolean emojis = false, simbolos = false, maiusculas = false;

    private NexoTecladoGrupo(Activity a,LinearLayout c,EditText e,Runnable s) {
        this.a=a;container=c;entrada=e;sistema=s;
    }
    public static void render(Activity a,LinearLayout c,EditText e,Runnable sistema) {
        new NexoTecladoGrupo(a,c,e,sistema).montar();
    }
    private int dp(int n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int cor,int borda,int r){
        GradientDrawable d=new GradientDrawable();d.setColor(cor);d.setCornerRadius(dp(r));
        d.setStroke(dp(1),borda);return d;
    }
    private TextView t(String x,int tamanho){
        TextView b=new TextView(a);b.setText(x);b.setTextColor(Color.WHITE);
        b.setTextSize(tamanho);b.setGravity(Gravity.CENTER);return b;
    }
    private void inserir(String v) {
        int s=Math.max(0,entrada.getSelectionStart());
        entrada.getText().insert(s,v);
    }
    private void montar() {
        container.removeAllViews();
        container.setPadding(dp(7),dp(6),dp(7),dp(8));
        container.setBackground(bg(Color.rgb(4,16,37),Color.rgb(30,123,209),16));
        LinearLayout topo=new LinearLayout(a);topo.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=t("N E X O  ✦  T E C L A D O",14);
        logo.setTextColor(Color.rgb(77,201,255));logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        logo.setShadowLayer(dp(4),0,0,Color.rgb(3,115,255));
        topo.addView(logo,new LinearLayout.LayoutParams(0,dp(37),1));
        TextView alternar=t(emojis?"ABC":"☺",21);
        topo.addView(alternar,new LinearLayout.LayoutParams(dp(52),dp(37)));
        alternar.setOnClickListener(v->{emojis=!emojis;montar();});
        TextView globo=t("🌐",19);topo.addView(globo,new LinearLayout.LayoutParams(dp(44),dp(37)));
        globo.setOnClickListener(v->sistema.run());
        container.addView(topo,new LinearLayout.LayoutParams(-1,dp(38)));
        if (emojis) {
            NexoPremiumEmojiView painel = new NexoPremiumEmojiView(a,this::inserir);
            int altura=a.getResources().getConfiguration().screenHeightDp;
            container.addView(painel,new LinearLayout.LayoutParams(-1,dp(
                Math.min(395,Math.max(245,(int)(altura*.40f))))));
            return;
        }
        String[][] linhas = simbolos
            ? new String[][]{{"1","2","3","4","5","6","7","8","9","0"},
                    {"@","#","$","%","&","-","+","(",")"},
                    {"*","\"",":",";","!","?","/","⌫"},
                    {"ABC","☺","🌐","espaço",",",".","↵"}}
            : new String[][]{{"q","w","e","r","t","y","u","i","o","p"},
                    {"a","s","d","f","g","h","j","k","l"},
                    {"⇧","z","x","c","v","b","n","m","⌫"},
                    {"123","☺","🌐","espaço",",",".","↵"}};
        int altura=a.getResources().getConfiguration().screenHeightDp>=760?54:47;
        for(String[] teclas:linhas) {
            LinearLayout linha=new LinearLayout(a);linha.setGravity(Gravity.CENTER);
            container.addView(linha,new LinearLayout.LayoutParams(-1,dp(altura)));
            for(String tecla:teclas) {
                TextView b=t("espaço".equals(tecla)?"espaço":
                    (maiusculas && tecla.length()==1?tecla.toUpperCase():tecla),
                    "espaço".equals(tecla)?12:19);
                b.setBackground(bg(Color.rgb(15,37,68),Color.rgb(34,100,167),8));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(altura-5),
                     "espaço".equals(tecla)?3f:1f);
                lp.setMargins(dp(2),dp(2),dp(2),dp(2));linha.addView(b,lp);
                b.setOnClickListener(v->pressionar(tecla));
            }
        }
    }
    private void pressionar(String tecla) {
        if("123".equals(tecla)||"ABC".equals(tecla)){simbolos=!simbolos;montar();return;}
        if("☺".equals(tecla)){emojis=true;montar();return;}
        if("🌐".equals(tecla)){sistema.run();return;}
        if("⇧".equals(tecla)){maiusculas=!maiusculas;montar();return;}
        if("⌫".equals(tecla)){
            int e=Math.max(0,entrada.getSelectionStart());
            int s=Math.max(0,entrada.getSelectionEnd());
            if(e!=s)entrada.getText().delete(Math.min(e,s),Math.max(e,s));
            else if(e>0){int i=Character.offsetByCodePoints(entrada.getText(),e,-1);
                entrada.getText().delete(i,e);}return;
        }
        if("↵".equals(tecla)){inserir("\n");return;}
        if("espaço".equals(tecla)){inserir(" ");return;}
        inserir(maiusculas?tecla.toUpperCase():tecla);
    }
}
