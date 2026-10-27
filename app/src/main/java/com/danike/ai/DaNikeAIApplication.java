package com.danike.ai;

import android.app.Application;
import android.util.Log;

public class DaNikeAIApplication extends Application {

    private static final String TAG = "DaNikeAIApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Mantém o sistema de erros existente.
        try {
            AppErrorLogger.iniciar(this);
        } catch (Throwable e) {
            Log.e(TAG, "Falha ao iniciar AppErrorLogger.", e);
        }

        // Novo monitor global NEXO.
        // Nunca deve impedir o aplicativo de iniciar.
        try {
            NexoAppObserver.iniciar(this);
        } catch (Throwable e) {
            Log.e(TAG, "Falha ao iniciar NexoAppObserver.", e);
        }
    }
}
