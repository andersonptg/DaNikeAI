package com.danike.ai;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class UsuariosTracker {

    private UsuariosTracker() {}

    private static final long HEARTBEAT =
            30_000L;

    private static Handler handler;
    private static Runnable tarefa;

    /**
     * Mantém compatibilidade com o LoginActivity atual.
     *
     * Ao entrar:
     * - garante o perfil Firebase;
     * - marca online;
     * - inicia heartbeat.
     */
    public static void registrarEntrada(
            String nome
    ) {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (user == null) {
            return;
        }

        String nomeFinal =
                nome == null
                        ? ""
                        : nome.trim();

        if (nomeFinal.isEmpty()) {

            nomeFinal =
                    user.getDisplayName();

            if (nomeFinal == null) {
                nomeFinal = "";
            }
        }

        final String nomePerfil =
                nomeFinal;

        PerfilFirebase.garantirPerfil(
                obterContextoSeguro(),
                nomePerfil,
                new PerfilFirebase.Callback() {

                    @Override
                    public void sucesso() {

                        PerfilFirebase.marcarOnline();

                        iniciarHeartbeat();
                    }

                    @Override
                    public void erro(
                            String mensagem
                    ) {

                        /*
                         * Mesmo que a atualização
                         * complementar do perfil falhe,
                         * tentamos registrar presença.
                         */
                        PerfilFirebase.marcarOnline();

                        iniciarHeartbeat();
                    }
                }
        );
    }

    /**
     * Marca o usuário como offline.
     */
    public static void marcarOffline() {

        pararHeartbeat();

        if (
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        == null
        ) {
            return;
        }

        PerfilFirebase.marcarOffline();
    }

    /**
     * Marca o usuário como online.
     */
    public static void marcarOnline() {

        if (
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        == null
        ) {
            return;
        }

        PerfilFirebase.marcarOnline();

        iniciarHeartbeat();
    }

    /**
     * Heartbeat da presença.
     *
     * Atualiza a cada 30 segundos enquanto
     * o usuário estiver dentro do aplicativo.
     */
    public static void iniciarHeartbeat() {

        if (
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser()
                        == null
        ) {
            return;
        }

        if (handler == null) {

            handler =
                    new Handler(
                            Looper.getMainLooper()
                    );
        }

        pararHeartbeat();

        tarefa = new Runnable() {

            @Override
            public void run() {

                FirebaseUser user =
                        FirebaseAuth
                                .getInstance()
                                .getCurrentUser();

                if (user == null) {
                    pararHeartbeat();
                    return;
                }

                PerfilFirebase.atualizarPresenca(
                        true,
                        "online"
                );

                if (handler != null) {

                    handler.postDelayed(
                            this,
                            HEARTBEAT
                    );
                }
            }
        };

        handler.post(tarefa);
    }

    /**
     * Para o heartbeat sem alterar imediatamente
     * o estado do Firebase.
     *
     * Útil durante troca de Activity.
     */
    public static void pararHeartbeat() {

        if (
                handler != null
                        && tarefa != null
        ) {

            handler.removeCallbacks(
                    tarefa
            );
        }

        tarefa = null;
    }

    /**
     * Chamado quando o usuário volta ao aplicativo.
     */
    public static void entrouEmCena() {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (user == null) {
            return;
        }

        PerfilFirebase.marcarOnline();

        iniciarHeartbeat();
    }

    /**
     * Chamado quando o aplicativo deixa
     * de estar em primeiro plano.
     */
    public static void saiuDeCena() {

        FirebaseUser user =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (user == null) {
            return;
        }

        pararHeartbeat();

        PerfilFirebase.marcarOffline();
    }

    /**
     * O tracker agora recebe o contexto apenas
     * para manter compatibilidade com a camada
     * central de perfil.
     *
     * O contexto da aplicação será fornecido
     * pelo Android através do FirebaseApp.
     */
    private static android.content.Context
    obterContextoSeguro() {

        try {

            return com.google.firebase.FirebaseApp
                    .getInstance()
                    .getApplicationContext();

        } catch (Exception e) {

            return null;
        }
    }
}
