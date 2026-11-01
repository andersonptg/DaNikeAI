package com.danike.ai;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Teclado de emojis NEXO dentro do app; preserva IME do sistema como opção. */
public final class NexoPremiumEmojiView extends LinearLayout {
    public interface AoEscolher { void escolher(String emoji); }
    private final AoEscolher aoEscolher;
    private final LinearLayout botoesCategoria;
    private final LinearLayout grade;
    private final EditText pesquisa;
    private final List<Emoji> emojis = new ArrayList<>();
    private String categoria = "Recentes";
    private final Set<String> recentes = new java.util.LinkedHashSet<>();

    private static class Emoji {
        final String simbolo, categoria, nome;
        Emoji(String simbolo, String categoria, String nome) {
            this.simbolo=simbolo; this.categoria=categoria; this.nome=nome;
        }
    }
    private int dp(int valor) { return (int)(getResources().getDisplayMetrics().density*valor+.5f); }
    private GradientDrawable fundo(int cor,int borda,int raio) {
        GradientDrawable d=new GradientDrawable(); d.setColor(cor); d.setCornerRadius(dp(raio));
        d.setStroke(dp(1),borda); return d;
    }
    private TextView rotulo(String txt,int sp,int cor) {
        TextView t=new TextView(getContext()); t.setText(txt); t.setTextSize(sp);
        t.setTextColor(cor); t.setGravity(Gravity.CENTER); return t;
    }

    public NexoPremiumEmojiView(Context c, AoEscolher callback) {
        super(c); this.aoEscolher=callback;
        setOrientation(VERTICAL);setPadding(dp(9),dp(6),dp(9),dp(8));
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(3,12,30),Color.rgb(7,25,53),Color.rgb(2,9,25)}));
        TextView titulo=rotulo("N E X O  ✦  E M O J I S",14,Color.rgb(70,194,255));
        titulo.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        titulo.setShadowLayer(dp(5),0,0,Color.rgb(0,104,255));
        addView(titulo,new LayoutParams(-1,dp(30)));
        pesquisa=new EditText(c); pesquisa.setSingleLine(true); pesquisa.setTextSize(15);
        pesquisa.setHint("Pesquisar emojis por nome ou símbolo...");
        pesquisa.setTextColor(Color.WHITE); pesquisa.setHintTextColor(Color.rgb(132,157,185));
        pesquisa.setBackground(fundo(Color.rgb(10,25,48),Color.rgb(29,113,185),17));
        pesquisa.setPadding(dp(13),0,dp(13),0);
        LayoutParams lpPesquisa=new LayoutParams(-1,dp(44));lpPesquisa.bottomMargin=dp(6);
        addView(pesquisa,lpPesquisa);
        HorizontalScrollView barra=new HorizontalScrollView(c);barra.setHorizontalScrollBarEnabled(false);
        botoesCategoria=new LinearLayout(c);botoesCategoria.setOrientation(HORIZONTAL);
        barra.addView(botoesCategoria);addView(barra,new LayoutParams(-1,dp(48)));
        ScrollView sc=new ScrollView(c);sc.setFillViewport(true);
        sc.setVerticalScrollBarEnabled(true);
        grade=new LinearLayout(c);grade.setOrientation(VERTICAL);
        sc.addView(grade);addView(sc,new LayoutParams(-1,0,1));
        popularEmojis();
        pesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int count,int after) {}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) { atualizarGrade(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        atualizarCategorias();atualizarGrade();
    }
    private void acrescentar(String categoria,String texto) {
        for (String emoji:texto.trim().split("\\s+")) {
            if (!emoji.isEmpty()) emojis.add(new Emoji(emoji,categoria,"") );
        }
    }
    private void faixa(String categoria,int inicio,int fim) {
        for(int i=inicio;i<=fim;i++) {
            if (!Character.isDefined(i) || Character.getType(i)==Character.UNASSIGNED) continue;
            if ((i>=0x1F3FB && i<=0x1F3FF) || (i>=0x1F1E6 && i<=0x1F1FF)) continue;
            String nome="";
            if(Build.VERSION.SDK_INT>=24) {
                try { nome=android.icu.lang.UCharacter.getName(i); } catch(Exception ignored) {}
                if(nome==null) nome="";
            }
            if (nome.contains("RESERVED") || nome.contains("UNASSIGNED")) continue;
            String simbolo=new String(Character.toChars(i));
            emojis.add(new Emoji(simbolo,categoria,nome));
        }
    }
    private void popularEmojis() {
        acrescentar("Recentes","😂 ❤️ 😍 👍 🔥 🙏 🎉 😎 😢 🤣 😊 🥰 😘 😁 🥳 😭 😮 😡 💙 💚 😴 👏 💯 👀 🎂");
        acrescentar("Emoções","😀 😃 😄 😁 😆 😅 🤣 😂 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 ☺️ 😚 😙 🥲 😋 😛 😜 🤪 😝 🤑 🤗 🤭 🫢 🫣 🤫 🤔 🫡 🤐 🤨 😐 😑 😶 🫥 😏 😒 🙄 😬 🤥 😌 😔 😪 🤤 😴 😷 🤒 🤕 🤢 🤮 🤧 🥵 🥶 🥴 😵 🤯 🤠 🥸 😎 🤓 🧐 😕 🫤 😟 🙁 ☹️ 😮 😯 😲 😳 🥺 🥹 😦 😧 😨 😰 😥 😢 😭 😱 😖 😣 😞 😓 😩 😫 🥱 😤 😡 😠 🤬 😈 👿 💀 ☠️ 💩 🤡 👹 👺 👻 👽 👾 🤖 🎃");
        acrescentar("Pessoas","👋 🤚 🖐️ ✋ 🖖 🫱 🫲 🫳 🫴 🫷 🫸 👌 🤌 🤏 ✌️ 🤞 🫰 🤟 🤘 🤙 👈 👉 👆 🖕 👇 ☝️ 🫵 👍 👎 ✊ 👊 🤛 🤜 👏 🙌 🫶 👐 🤲 🤝 🙏 ✍️ 💅 🤳 💪 🦾 🦿 🦵 🦶 👂 🦻 👃 🧠 🫀 🫁 🦷 🦴 👀 👁️ 👅 👄 🫦 👶 👧 🧒 👦 👩 🧑 👨 👵 🧓 👴 🧔 👱 👸 🤴 🥷 👲 👳 👮 👷 💂 🕵️ 👩‍⚕️ 👨‍⚕️ 👩‍💻 👨‍💻 👩‍🚀 👨‍🚀 💃 🕺 🧘");
        acrescentar("Animais","🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐻‍❄️ 🐨 🐯 🦁 🐮 🐷 🐽 🐸 🐵 🙈 🙉 🙊 🐒 🐔 🐧 🐦 🐤 🐣 🦆 🦅 🦉 🦇 🐺 🐗 🐴 🦄 🐝 🪱 🐛 🦋 🐌 🐞 🐜 🪲 🪳 🦟 🦗 🕷️ 🕸️ 🦂 🐢 🐍 🦎 🐙 🦑 🦐 🦞 🦀 🐟 🐠 🐡 🐬 🦈 🐳 🐋 🦭 🐊 🐅 🐆 🦓 🦍 🦧 🐘 🦛 🦏 🐪 🦒 🦘 🦥 🦦 🦨 🦫 🦔 🐇 🐿️ 🦩 🦚 🦜 🦢 🦃 🐓 🪶 🪺 🐾 🌸 🌻 🌹 🌺 🌷 🌿 🌴 🌳 🍀 🌈 ⭐ 🌟 🌙 ☀️ 🌊 ❄️");
        acrescentar("Comidas","🍎 🍏 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🫐 🍈 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🍆 🥑 🥦 🥬 🥒 🌶️ 🫑 🌽 🥕 🫒 🧄 🧅 🥔 🍠 🫘 🥐 🥯 🍞 🥖 🥨 🧀 🥚 🍳 🧈 🥞 🧇 🥓 🥩 🍗 🍖 🌭 🍔 🍟 🍕 🥪 🥙 🧆 🌮 🌯 🫔 🥗 🥘 🫕 🥫 🍝 🍜 🍲 🍛 🍣 🍱 🥟 🦪 🍤 🍙 🍚 🍘 🍥 🥠 🍢 🍡 🍧 🍨 🍦 🥧 🧁 🍰 🎂 🍮 🍭 🍬 🍫 🍿 🍩 🍪 🥜 🌰 ☕ 🍵 🧃 🥤 🧋 🍺 🍻 🥂 🍷 🍸 🍹 🧉 🧊");
        acrescentar("Viagens","🚗 🚕 🚙 🚌 🚎 🏎️ 🚓 🚑 🚒 🚐 🛻 🚚 🚛 🚜 🛵 🏍️ 🛺 🚲 🛴 🚏 🛣️ 🛤️ 🚦 🚥 🚧 ⚓ ⛵ 🛶 🚤 🛳️ ⛴️ 🛥️ 🚢 ✈️ 🛫 🛬 🪂 💺 🚁 🚟 🚠 🚡 🚀 🛸 🚉 🚊 🚝 🚄 🚅 🚈 🚞 🚂 🚆 🚇 🚋 🏖️ 🏝️ 🏜️ 🌋 ⛰️ 🏔️ 🗻 🏕️ 🏡 🏠 🏘️ 🏢 🏬 🏥 🏦 🏨 🏪 🏫 🏰 🗼 🗽 ⛪ 🕌 🛕 🕍 ⛩️ 🕋 ⛲ ⛺ 🌁 🌃 🌆 🌇 🌉 🌌 🌠 🎆 🎇");
        acrescentar("Atividades","⚽ 🏀 🏈 ⚾ 🥎 🎾 🏐 🏉 🥏 🎱 🪀 🏓 🏸 🏒 🏑 🥍 🏏 🪃 🥅 ⛳ 🪁 🛝 🏹 🎣 🤿 🥊 🥋 🎽 🛹 🛼 🛷 ⛸️ 🥌 🎿 ⛷️ 🏂 🪂 🏋️ 🤼 🤸 ⛹️ 🤺 🤾 🏌️ 🏇 🧘 🏄 🏊 🤽 🚣 🧗 🚴 🚵 🏆 🥇 🥈 🥉 🎖️ 🏅 🎗️ 🎫 🎟️ 🎪 🎭 🎨 🎬 🎤 🎧 🎼 🎹 🥁 🎷 🎺 🎸 🪕 🎻 🎲 ♟️ 🎯 🎳 🎮 🎰 🧩 🧸 🪅 🎉 🎊 🎈 🎁");
        acrescentar("Objetos","📱 💻 🖥️ 🖨️ ⌨️ 🖱️ 🖲️ 💽 💾 💿 📀 📷 📸 📹 🎥 📞 ☎️ 📟 📠 📺 📻 🎙️ 🎚️ 🎛️ 🧭 ⏰ ⌚ ⏱️ ⏲️ ⌛ 📡 🔋 🪫 🔌 💡 🔦 🕯️ 🪔 🧯 🛢️ 💸 💵 💴 💶 💷 🪙 💰 💳 💎 ⚖️ 🪜 🧰 🔧 🔨 ⚒️ 🛠️ ⛏️ 🪓 🪚 🔩 ⚙️ 🧱 ⛓️ 🧲 🔫 💣 🪃 🗡️ ⚔️ 🛡️ 🚬 ⚰️ ⚱️ 🪦 🔮 🧿 🪬 💈 ⚗️ 🔭 🔬 🕳️ 💊 💉 🩹 🩺 🚪 🪑 🚽 🚿 🛁 🛏️ 🛋️ 🪞 🪟 🛍️ 🛒 🎁 📦 ✉️ 📩 📨 📧 📥 📤 📜 📃 📄 📑 📊 📈 📉 🗒️ 🗓️ 📆 📅 📋 📌 📍 ✂️ 🖊️ 🖋️ ✒️ 🖌️ 📝 ✏️ 🔎 🔍 🔒 🔓 🔏 🔐 🔑 🗝️ 🪪");
        acrescentar("Símbolos","❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❤️‍🔥 ❤️‍🩹 ❣️ 💕 💞 💓 💗 💖 💘 💝 💟 ☮️ ✝️ ☪️ 🕉️ ☸️ ✡️ 🔯 🕎 ☯️ ☦️ 🛐 ⛎ ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓ 🆔 ⚛️ 🉑 ☢️ ☣️ 📴 📳 🈶 🈚 🈸 🈺 🈷️ ✴️ 🆚 💮 🉐 ㊙️ ㊗️ 🈴 🈵 🈹 🈲 🅰️ 🅱️ 🆎 🆑 🅾️ 🆘 ❌ ⭕ 🛑 ⛔ 📛 🚫 💯 🔞 🚷 🚯 🚳 🚱 🚭 📵 🔕 🔇 🔈 🔉 🔊 🔔 📢 📣 ✅ ☑️ ✔️ ❎ ➕ ➖ ➗ ✖️ ♾️ ‼️ ⁉️ ❓ ❔ ❕ ❗ 💤 💭 🗯️ 💬 🗨️ 🕐 🕑 🕒 🕓 🕔 🕕 🕖 🕗 🕘 🕙 🕚 🕛");
        acrescentar("Bandeiras","🏁 🚩 🎌 🏴 🏳️ 🏳️‍🌈 🏳️‍⚧️ 🇧🇷 🇵🇹 🇺🇸 🇨🇦 🇲🇽 🇦🇷 🇨🇱 🇨🇴 🇵🇪 🇫🇷 🇬🇧 🇪🇸 🇩🇪 🇮🇹 🇳🇱 🇧🇪 🇨🇭 🇸🇪 🇳🇴 🇫🇮 🇩🇰 🇵🇱 🇺🇦 🇷🇺 🇨🇳 🇯🇵 🇰🇷 🇮🇳 🇦🇺 🇳🇿 🇿🇦 🇪🇬 🇹🇷 🇬🇷 🇮🇪 🇮🇱 🇸🇦 🇦🇪 🇸🇬 🇹🇭 🇻🇳 🇵🇭 🇮🇩 🇲🇾");
        // Faixas Unicode estendidas (exibidas conforme os glifos do Android).
        faixa("Emoções",0x1F600,0x1F64F);
        faixa("Pessoas",0x1F466,0x1F487);
        faixa("Animais",0x1F400,0x1F43E);
        faixa("Viagens",0x1F680,0x1F6C5);
        faixa("Atividades",0x1F3A0,0x1F3FA);
        faixa("Objetos",0x1F4A0,0x1F4FF);
        faixa("Emoções",0x1F910,0x1F92F);
        faixa("Pessoas",0x1F930,0x1F93F);
        faixa("Animais",0x1F980,0x1F9A2);
        faixa("Objetos",0x1F9B0,0x1F9FF);
        faixa("Símbolos",0x1FA70,0x1FAFF);
    }
    private void atualizarCategorias() {
        botoesCategoria.removeAllViews();
        String[] tipos={"Recentes","Emoções","Pessoas","Animais","Comidas","Viagens","Atividades","Objetos","Símbolos","Bandeiras"};
        String[] icones={"◷","☺","♙","🐾","🍴","✈","⚽","💡","♥","⚑"};
        for(int i=0;i<tipos.length;i++) {
            final String tipo=tipos[i];
            TextView b=rotulo(icones[i]+"  "+tipo,12,Color.WHITE);
            int cor=tipo.equals(categoria)?Color.rgb(9,82,172):Color.rgb(10,27,53);
            b.setBackground(fundo(cor,Color.rgb(23,102,186),14));
            LayoutParams lp=new LayoutParams(-2,dp(37));lp.setMargins(dp(2),dp(3),dp(4),dp(3));
            b.setPadding(dp(12),0,dp(12),0);botoesCategoria.addView(b,lp);
            b.setOnClickListener(v->{categoria=tipo;atualizarCategorias();atualizarGrade();});
        }
    }
    private void atualizarGrade() {
        grade.removeAllViews();
        String q=pesquisa.getText().toString().trim().toUpperCase(Locale.ROOT);
        Set<String> vistos=new HashSet<>();
        List<String> filtrados=new ArrayList<>();
        if(categoria.equals("Recentes")) for(String e:recentes) { filtrados.add(e); vistos.add(e); }
        for(Emoji e:emojis) {
            boolean matchCategoria=categoria.equals(e.categoria) || (categoria.equals("Recentes") && recentes.isEmpty() && e.categoria.equals("Recentes"));
            boolean matchBusca=!q.isEmpty() && (e.nome.toUpperCase(Locale.ROOT).contains(q) || e.simbolo.equals(q));
            if((q.isEmpty()?matchCategoria:matchBusca) && vistos.add(e.simbolo)) filtrados.add(e.simbolo);
        }
        if(filtrados.isEmpty()) { TextView vazio=rotulo("Nenhum emoji encontrado",14,Color.LTGRAY); grade.addView(vazio,new LayoutParams(-1,dp(65)));return; }
        int largura=getResources().getConfiguration().screenWidthDp;
        int colunas=largura>=840?12:largura>=600?10:8;
        int linhas=(filtrados.size()+colunas-1)/colunas;
        for(int row=0;row<linhas;row++) {
            LinearLayout linha=new LinearLayout(getContext());linha.setGravity(Gravity.CENTER);
            grade.addView(linha,new LayoutParams(-1,dp(46)));
            for(int c=0;c<colunas;c++) {
                int idx=row*colunas+c;
                if(idx>=filtrados.size()) break;
                String emoji=filtrados.get(idx);
                TextView b=rotulo(emoji,25,Color.WHITE);
                b.setContentDescription("Emoji "+emoji);
                linha.addView(b,new LayoutParams(0,dp(44),1));
                b.setOnClickListener(v->{recentes.remove(emoji);recentes.add(emoji);aoEscolher.escolher(emoji);});
            }
        }
    }
}
