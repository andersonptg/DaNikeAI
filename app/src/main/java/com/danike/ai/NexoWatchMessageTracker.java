package com.danike.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Distingue mensagens já existentes de mensagens novas, sem depender apenas de
 * timestamps: duas mensagens no mesmo milissegundo também são mostradas.
 * Sem dependências Android/Firebase para permitir testes unitários simples.
 */
public final class NexoWatchMessageTracker {
    private final Set<String> vistos = new HashSet<>();
    private boolean inicializado;

    public List<Map<?, ?>> atualizar(List<?> mensagens) {
        List<Map<?, ?>> novas = new ArrayList<>();
        Set<String> presentes = new HashSet<>();
        if (mensagens != null) {
            for (Object o : mensagens) {
                if (!(o instanceof Map)) continue;
                Map<?, ?> m = (Map<?, ?>) o;
                String chave = chave(m);
                presentes.add(chave);
                if (vistos.add(chave) && inicializado) novas.add(m);
            }
        }
        inicializado = true;
        // Evita acumular milhares de IDs após muitas horas na mesma sala.
        if (vistos.size() > 750) vistos.retainAll(presentes);
        return novas;
    }

    private static String chave(Map<?, ?> m) {
        Object id = m.get("id");
        if (id != null && !id.toString().isEmpty()) return "id:" + id;
        // Compatibilidade com as mensagens antigas, que não tinham UUID.
        return "old:" + m.get("uid") + "|" + m.get("ts") + "|" + m.get("texto");
    }

    public void reiniciar() { vistos.clear(); inicializado = false; }
}
