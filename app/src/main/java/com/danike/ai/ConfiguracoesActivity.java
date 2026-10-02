package com.danike.ai;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class ConfiguracoesActivity extends Activity {

    private static final int MIC = 200;
    private static final int NOTIF = 201;
    private static final int BACKUP = 202;
    private static final int RESTORE = 203;

    LinearLayout lista;
    SharedPreferences config;

    boolean escuro;

    int fundo() {
        return escuro
                ? Color.rgb(2,10,20)
                : Color.rgb(245,248,250);
    }

    int branco() {
        return escuro
                ? Color.WHITE
                : Color.rgb(20,25,30);
    }

    int azul() {
        return escuro
                ? Color.rgb(80,210,255)
                : Color.rgb(0,100,180);
    }

    int card() {
        return escuro
                ? Color.rgb(5,22,35)
                : Color.WHITE;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        config = getSharedPreferences(
                "danike_config",
                MODE_PRIVATE
        );

        escuro = config.getBoolean(
                "modo_escuro",
                true
        );

        montar();
    }

    void montar() {

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(fundo());

        TextView titulo = texto(
                "⚙️  CONFIGURAÇÕES",
                23,
                branco()
        );

        titulo.setGravity(Gravity.CENTER);
        titulo.setTypeface(null,android.graphics.Typeface.BOLD);
        titulo.setPadding(10,30,10,25);

        raiz.addView(titulo);

        ScrollView scroll = new ScrollView(this);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(10,0,10,20);

        adicionarMemoria();
        adicionarAparencia();
        adicionarVoz();
        adicionarDispositivo();
        adicionarSegundoPlano();
        adicionarPrivacidade();

        TextView sobre = texto(
                "DaNike AI\n\nVersão 1.0.0\n\nSeu assistente inteligente 👑",
                15,
                azul()
        );

        sobre.setGravity(Gravity.CENTER);
        sobre.setPadding(10,30,10,30);

        lista.addView(sobre);

        scroll.addView(lista);

        raiz.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,0,1
                )
        );

        TextView fechar = texto(
                "FECHAR",
                16,
                Color.WHITE
        );

        fechar.setGravity(Gravity.CENTER);
        fechar.setBackgroundColor(
                Color.rgb(10,45,65)
        );

        fechar.setOnClickListener(v -> finish());

        raiz.addView(
                fechar,
                new LinearLayout.LayoutParams(-1,60)
        );

        setContentView(raiz);
    }

    void adicionarMemoria() {

        lista.addView(tituloSecao("🧠  MEMÓRIA"));

        boolean ativa = config.getBoolean(
                "memoria_ativa",
                true
        );

        Switch s = novoSwitch(
                "Memória ativa",
                "Permitir usar memórias confirmadas",
                ativa
        );

        s.setOnCheckedChangeListener((b,v) ->
                config.edit()
                        .putBoolean("memoria_ativa",v)
                        .apply()
        );

        lista.addView(s);

        lista.addView(botao(
                "🧠  Memórias importantes",
                "Ver, editar e apagar memórias",
                v -> startActivity(
                        new Intent(
                                this,
                                MemoriasActivity.class
                        )
                )
        ));

        lista.addView(botao(
                "💬  Conversas anteriores",
                "Ver o histórico da DaNikeAI",
                v -> startActivity(
                        new Intent(
                                this,
                                ConversasActivity.class
                        )
                )
        ));

        lista.addView(botao(
                "🗑️  Apagar memória",
                "Apagar todas as memórias confirmadas",
                v -> apagarTodasMemorias()
        ));
    }

    void adicionarAparencia() {

        lista.addView(tituloSecao("🎨  APARÊNCIA"));

        boolean atual = config.getBoolean(
                "modo_escuro",
                true
        );

        Switch s = novoSwitch(
                "Modo escuro",
                "Usar aparência escura nas telas de configuração",
                atual
        );

        s.setOnCheckedChangeListener((b,v) -> {

            config.edit()
                    .putBoolean("modo_escuro",v)
                    .apply();

            escuro = v;

            Toast.makeText(
                    this,
                    v
                            ? "🌙 Modo escuro ativado"
                            : "☀️ Modo claro ativado",
                    Toast.LENGTH_SHORT
            ).show();

            montar();
        });

        lista.addView(s);

        lista.addView(botao(
                "☀️  Modo claro",
                "Ativar aparência clara",
                v -> {
                    config.edit()
                            .putBoolean("modo_escuro",false)
                            .apply();
                    escuro = false;
                    montar();
                }
        ));

        lista.addView(botao(
                "🌙  Modo escuro",
                "Ativar aparência escura",
                v -> {
                    config.edit()
                            .putBoolean("modo_escuro",true)
                            .apply();
                    escuro = true;
                    montar();
                }
        ));
    }

    void adicionarVoz() {

        lista.addView(tituloSecao("🎙️  VOZ"));

        boolean voz = config.getBoolean(
                "voz_ativa",
                true
        );

        Switch s = novoSwitch(
                "Voz ativa",
                "Permitir recursos de voz",
                voz
        );

        s.setOnCheckedChangeListener((b,v) ->
                config.edit()
                        .putBoolean("voz_ativa",v)
                        .apply()
        );

        lista.addView(s);

        boolean fala = config.getBoolean(
                "fala_ativa",
                true
        );

        Switch f = novoSwitch(
                "Fala ativa",
                "A DaNikeAI pode falar as respostas",
                fala
        );

        f.setOnCheckedChangeListener((b,v) ->
                config.edit()
                        .putBoolean("fala_ativa",v)
                        .apply()
        );

        lista.addView(f);
    }

    void adicionarDispositivo() {

        lista.addView(tituloSecao("📱  DISPOSITIVO"));

        lista.addView(botao(
                "🎙️  Microfone",
                "Solicitar permissão do microfone",
                v -> pedirMicrofone()
        ));

        lista.addView(botao(
                "🔔  Notificações",
                "Permitir notificações da DaNikeAI",
                v -> pedirNotificacoes()
        ));

        lista.addView(botao(
                "♿  Acessibilidade",
                "Abrir configurações de acessibilidade",
                v -> abrir(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                )
        ));

        lista.addView(botao(
                "📊  Uso de aplicativos",
                "Abrir acesso aos dados de uso",
                v -> abrir(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                )
        ));
    }

    void adicionarSegundoPlano() {

        lista.addView(tituloSecao("🌙  SEGUNDO PLANO"));

        boolean ativo = config.getBoolean(
                "segundo_plano",
                false
        );

        Switch s = novoSwitch(
                "Segundo plano",
                "Manter a DaNikeAI disponível com notificação",
                ativo
        );

        s.setOnCheckedChangeListener((b,v) -> {

            config.edit()
                    .putBoolean("segundo_plano",v)
                    .apply();

            if (v) iniciarSegundoPlano();
            else pararSegundoPlano();
        });

        lista.addView(s);

        lista.addView(botao(
                "🗣️  Chamar DaNike",
                "Abrir a assistente rapidamente",
                v -> {
                    Intent i = new Intent(
                            this,
                            IAActivity.class
                    );
                    startActivity(i);
                }
        ));
    }

    void adicionarPrivacidade() {

        lista.addView(tituloSecao("🔐  PRIVACIDADE"));

        lista.addView(botao(
                "🔐  Permissões",
                "Abrir permissões do aplicativo",
                v -> {
                    Intent i = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse(
                                    "package:" + getPackageName()
                            )
                    );
                    startActivity(i);
                }
        ));

        lista.addView(botao(
                "💾  Backup da memória",
                "Salvar memória e histórico",
                v -> fazerBackup()
        ));

        lista.addView(botao(
                "♻️  Restaurar memória",
                "Restaurar um backup existente",
                v -> restaurarBackup()
        ));
    }

    void pedirMicrofone() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        MIC
                );

                return;
            }
        }

        Toast.makeText(
                this,
                "🎙️ Microfone já autorizado.",
                Toast.LENGTH_SHORT
        ).show();
    }

    void pedirNotificacoes() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIF
                );

                return;
            }
        }

        abrir(
                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                "android.provider.extra.APP_PACKAGE",
                getPackageName()
        );
    }

    void abrir(String acao) {
        try {
            startActivity(new Intent(acao));
        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Configuração não disponível neste aparelho.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void abrir(
            String acao,
            String extra,
            String valor
    ) {
        try {
            Intent i = new Intent(acao);
            i.putExtra(extra,valor);
            startActivity(i);
        } catch(Exception e) {
            Toast.makeText(
                    this,
                    "Não foi possível abrir essa configuração.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    void iniciarSegundoPlano() {

        try {

            Intent i = new Intent(
                    this,
                    DaNikeBackgroundService.class
            );

            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(i);
            } else {
                startService(i);
            }

            Toast.makeText(
                    this,
                    "🌙 Segundo plano ativado.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch(Exception e) {

            Toast.makeText(
                    this,
                    "Não foi possível iniciar o segundo plano.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    void pararSegundoPlano() {

        stopService(
                new Intent(
                        this,
                        DaNikeBackgroundService.class
                )
        );

        Toast.makeText(
                this,
                "🌙 Segundo plano desativado.",
                Toast.LENGTH_SHORT
        ).show();
    }

    void apagarTodasMemorias() {

        new AlertDialog.Builder(this)
                .setTitle("🗑️ Apagar memórias?")
                .setMessage(
                        "Todas as memórias confirmadas serão apagadas."
                )
                .setNegativeButton("CANCELAR",null)
                .setPositiveButton("APAGAR",(d,w) -> {

                    MemoriaDB banco =
                            new MemoriaDB(this);

                    banco.apagarMemorias();
                    banco.close();

                    Toast.makeText(
                            this,
                            "🗑️ Memórias apagadas.",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    void fazerBackup() {

        Intent i = new Intent(
                Intent.ACTION_CREATE_DOCUMENT
        );

        i.setType("application/json");
        i.putExtra(
                Intent.EXTRA_TITLE,
                "DaNikeAI_backup.json"
        );

        startActivityForResult(i,BACKUP);
    }

    void restaurarBackup() {

        Intent i = new Intent(
                Intent.ACTION_OPEN_DOCUMENT
        );

        i.setType("application/json");
        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(i,RESTORE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        if (requestCode == BACKUP) {
            salvarBackup(data.getData());
        }

        if (requestCode == RESTORE) {
            restaurarArquivo(data.getData());
        }
    }

    void salvarBackup(Uri uri) {

        try {

            JSONObject raiz = new JSONObject();

            JSONArray memorias = new JSONArray();

            MemoriaDB banco = new MemoriaDB(this);
            android.database.Cursor cm =
                    banco.listarMemorias();

            try {

                while(cm.moveToNext()) {

                    JSONObject obj = new JSONObject();

                    obj.put(
                            "texto",
                            cm.getString(
                                    cm.getColumnIndexOrThrow("texto")
                            )
                    );

                    obj.put(
                            "categoria",
                            cm.getString(
                                    cm.getColumnIndexOrThrow("categoria")
                            )
                    );

                    memorias.put(obj);
                }

            } finally {
                cm.close();
            }

            JSONArray conversas = new JSONArray();

            android.database.Cursor cc =
                    banco.listarConversas();

            try {

                while(cc.moveToNext()) {

                    JSONObject obj = new JSONObject();

                    obj.put(
                            "pergunta",
                            cc.getString(
                                    cc.getColumnIndexOrThrow("pergunta")
                            )
                    );

                    obj.put(
                            "resposta",
                            cc.getString(
                                    cc.getColumnIndexOrThrow("resposta")
                            )
                    );

                    conversas.put(obj);
                }

            } finally {
                cc.close();
                banco.close();
            }

            raiz.put("memorias",memorias);
            raiz.put("conversas",conversas);
            raiz.put("versao",1);

            OutputStream out =
                    getContentResolver()
                            .openOutputStream(uri);

            if(out == null) return;

            out.write(
                    raiz.toString(2)
                            .getBytes("UTF-8")
            );

            out.close();

            Toast.makeText(
                    this,
                    "💾 Backup salvo com sucesso.",
                    Toast.LENGTH_LONG
            ).show();

        } catch(Exception e) {

            Toast.makeText(
                    this,
                    "Erro no backup: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    void restaurarArquivo(Uri uri) {

        new AlertDialog.Builder(this)
                .setTitle("♻️ Restaurar backup?")
                .setMessage(
                        "As memórias e conversas do backup serão adicionadas ao banco atual."
                )
                .setNegativeButton("CANCELAR",null)
                .setPositiveButton("RESTAURAR",
                        (d,w) -> executarRestauracao(uri)
                )
                .show();
    }

    void executarRestauracao(Uri uri) {

        try {

            InputStream in =
                    getContentResolver()
                            .openInputStream(uri);

            if(in == null) return;

            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream();

            byte[] dados = new byte[4096];
            int n;

            while((n=in.read(dados)) != -1) {
                buffer.write(dados,0,n);
            }

            in.close();

            JSONObject raiz =
                    new JSONObject(
                            new String(
                                    buffer.toByteArray(),
                                    "UTF-8"
                            )
                    );

            MemoriaDB banco =
                    new MemoriaDB(this);

            JSONArray memorias =
                    raiz.optJSONArray("memorias");

            if(memorias != null) {

                for(int i=0;i<memorias.length();i++) {

                    JSONObject obj =
                            memorias.getJSONObject(i);

                    banco.salvarMemoriaConfirmada(
                            obj.optString("texto"),
                            obj.optString(
                                    "categoria",
                                    "confirmada pelo usuário"
                            )
                    );
                }
            }

            JSONArray conversas =
                    raiz.optJSONArray("conversas");

            if(conversas != null) {

                for(int i=0;i<conversas.length();i++) {

                    JSONObject obj =
                            conversas.getJSONObject(i);

                    banco.salvarConversa(
                            obj.optString("pergunta"),
                            obj.optString("resposta")
                    );
                }
            }

            banco.close();

            Toast.makeText(
                    this,
                    "♻️ Backup restaurado.",
                    Toast.LENGTH_LONG
            ).show();

        } catch(Exception e) {

            Toast.makeText(
                    this,
                    "Erro ao restaurar: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    TextView tituloSecao(String s) {

        TextView t = texto(
                s,
                14,
                azul()
        );

        t.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        t.setPadding(8,22,8,8);

        return t;
    }

    Switch novoSwitch(
            String titulo,
            String descricao,
            boolean ativo
    ) {

        Switch s = new Switch(this);

        s.setText(
                titulo + "\n" + descricao
        );

        s.setTextColor(branco());
        s.setTextSize(16);
        s.setChecked(ativo);
        s.setPadding(8,8,8,8);

        return s;
    }

    TextView botao(
            String titulo,
            String descricao,
            View.OnClickListener listener
    ) {

        TextView b = texto(
                titulo + "\n" + descricao + "   ›",
                16,
                branco()
        );

        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(14,12,14,12);
        b.setBackgroundColor(card());
        b.setOnClickListener(listener);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lp.setMargins(0,4,0,4);

        b.setLayoutParams(lp);

        return b;
    }

    TextView texto(
            String s,
            float tamanho,
            int cor
    ) {

        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(tamanho);
        t.setTextColor(cor);

        return t;
    }
}
