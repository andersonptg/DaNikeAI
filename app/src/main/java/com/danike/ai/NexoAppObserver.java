package com.danike.ai;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NexoAppObserver
        implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "NexoAppObserver";

    private static final long LIMITE_TELA_LENTA_MS = 3000L;
    private static final long LIMITE_MEMORIA_ALTA_MB = 200L;

    private final Map<Activity, Long> entradas =
            new WeakHashMap<>();

    private volatile String telaAtual = "Application";

    private static final AtomicBoolean INICIADO =
            new AtomicBoolean(false);

    private NexoAppObserver() {
    }

    public static void iniciar(Application application) {

        if (!INICIADO.compareAndSet(false, true)) {
            return;
        }

        final NexoAppObserver observer =
                new NexoAppObserver();

        try {
            application.registerActivityLifecycleCallbacks(
                    observer
            );
        } catch (Throwable e) {
            Log.e(TAG,
                    "Não foi possível registrar lifecycle.",
                    e);
        }

        instalarCrashHandler(application, observer);

        registrar(
                application,
                "APP_START",
                "INFO",
                "APP",
                "applicationStart",
                "Aplicativo iniciado.",
                0
        );

        registrarMemoria(application);
    }

    private static void instalarCrashHandler(
            Application application,
            NexoAppObserver observer
    ) {

        final Thread.UncaughtExceptionHandler anterior =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, throwable) -> {

                    try {

                        String tipo = throwable == null
                                ? "UnknownException"
                                : throwable
                                .getClass()
                                .getSimpleName();

                        String mensagem = throwable == null
                                ? "Exceção desconhecida."
                                : throwable.getMessage();

                        registrar(
                                application,
                                "UNKNOWN_ERROR",
                                "ERROR",
                                "APP",
                                "uncaughtException",
                                tipo + ": "
                                        + limpar(mensagem),
                                0
                        );

                    } catch (Throwable ignorado) {
                        Log.d(
                                TAG,
                                "Falha ao registrar crash."
                        );
                    }

                    if (anterior != null) {
                        anterior.uncaughtException(
                                thread,
                                throwable
                        );
                    }
                }
        );
    }

    public static void evento(
            Application application,
            String tipo,
            String severidade,
            String componente,
            String acao,
            String mensagem
    ) {
        registrar(
                application,
                tipo,
                severidade,
                componente,
                acao,
                mensagem,
                0
        );
    }

    public static void erro(
            Application application,
            String componente,
            String acao,
            Throwable erro
    ) {

        String tipo = erro == null
                ? "UnknownException"
                : erro.getClass().getSimpleName();

        String mensagem = erro == null
                ? "Erro desconhecido."
                : erro.getMessage();

        registrar(
                application,
                "ERROR",
                "ERROR",
                componente,
                acao,
                tipo + ": " + limpar(mensagem),
                0
        );
    }

    public static void operacaoLenta(
            Application application,
            String componente,
            String acao,
            long duracaoMs
    ) {

        if (duracaoMs < LIMITE_TELA_LENTA_MS) {
            return;
        }

        registrar(
                application,
                "SLOW_OPERATION",
                "WARNING",
                componente,
                acao,
                "Operação acima do limite esperado.",
                duracaoMs
        );
    }

    private static void registrar(
            Application application,
            String tipo,
            String severidade,
            String componente,
            String acao,
            String mensagem,
            long duracao
    ) {

        try {

            String tela = "Application";

            if (application != null) {
                tela = obterTelaAtual(application);
            }

            NexoDiagnostic.registrar(
                    application,
                    tela,
                    tipo,
                    severidade,
                    componente,
                    acao,
                    null,
                    "",
                    limpar(mensagem),
                    duracao
            );

        } catch (Throwable ignorado) {

            Log.d(
                    TAG,
                    "Diagnóstico indisponível."
            );
        }
    }

    private static String obterTelaAtual(
            Application application
    ) {
        return "Application";
    }

    private static void registrarMemoria(
            Application application
    ) {

        try {

            Runtime runtime =
                    Runtime.getRuntime();

            long usada =
                    (runtime.totalMemory()
                            - runtime.freeMemory())
                            / (1024L * 1024L);

            long max =
                    runtime.maxMemory()
                            / (1024L * 1024L);

            String mensagem =
                    "Memória usada=" + usada
                            + "MB; limite=" + max + "MB.";

            registrar(
                    application,
                    "APP_MEMORY",
                    usada >= LIMITE_MEMORIA_ALTA_MB
                            ? "WARNING"
                            : "INFO",
                    "APP",
                    "memorySnapshot",
                    mensagem,
                    0
            );

        } catch (Throwable ignorado) {
            Log.d(
                    TAG,
                    "Não foi possível medir memória."
            );
        }
    }

    @Override
    public void onActivityCreated(
            Activity activity,
            Bundle savedInstanceState
    ) {

        telaAtual =
                activity.getClass().getSimpleName();

        registrar(
                activity.getApplication(),
                "SCREEN_OPEN",
                "INFO",
                "UI",
                "activityCreated",
                "Tela criada.",
                0L
        );
    }

    @Override
    public void onActivityStarted(
            Activity activity
    ) {

        telaAtual =
                activity.getClass().getSimpleName();

        entradas.put(
                activity,
                SystemClock.elapsedRealtime()
        );

        registrar(
                activity.getApplication(),
                "SCREEN_START",
                "INFO",
                "UI",
                "activityStarted",
                "Tela iniciada.",
                0L
        );
    }

    @Override
    public void onActivityResumed(
            Activity activity
    ) {

        telaAtual =
                activity.getClass().getSimpleName();

        registrar(
                activity.getApplication(),
                "SCREEN_RESUME",
                "INFO",
                "UI",
                "activityResumed",
                "Tela em primeiro plano.",
                0L
        );
    }

    @Override
    public void onActivityPaused(
            Activity activity
    ) {

        Long inicio =
                entradas.get(activity);

        if (inicio == null) {
            return;
        }

        long duracao =
                SystemClock.elapsedRealtime()
                        - inicio;

        if (duracao >= LIMITE_TELA_LENTA_MS) {

            registrar(
                    activity.getApplication(),
                    "SCREEN_SLOW",
                    "WARNING",
                    "UI",
                    "activityDuration",
                    "Tela permaneceu ativa por tempo elevado.",
                    duracao
            );
        }
    }

    @Override
    public void onActivityStopped(
            Activity activity
    ) {

        Long inicio =
                entradas.get(activity);

        long duracao = inicio == null
                ? 0
                : SystemClock.elapsedRealtime()
                - inicio;

        registrar(
                activity.getApplication(),
                "SCREEN_STOP",
                "INFO",
                "UI",
                "activityStopped",
                "Tela saiu do primeiro plano.",
                duracao
        );

        entradas.remove(activity);
    }

    @Override
    public void onActivitySaveInstanceState(
            Activity activity,
            Bundle outState
    ) {
    }

    @Override
    public void onActivityDestroyed(
            Activity activity
    ) {

        entradas.remove(activity);
    }

    private static String limpar(
            String mensagem
    ) {

        if (mensagem == null
                || mensagem.trim().isEmpty()) {

            return "Sem mensagem.";
        }

        String texto =
                mensagem
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replaceAll(
                                "(?i)bearer\\s+[A-Za-z0-9._~+/=-]+",
                                "Bearer [REDACTED]"
                        )
                        .replaceAll(
                                "(?i)(x-[a-z0-9-]*secret|api[_-]?key|token)\\s*[:=]\\s*[^\\s,;]+",
                                "$1=[REDACTED]"
                        )
                        .trim();

        if (texto.length() > 500) {
            texto =
                    texto.substring(0, 500);
        }

        return texto;
    }
}
