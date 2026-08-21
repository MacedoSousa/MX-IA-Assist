package com.macedxs.mx.agent.skill.general;

import java.util.List;

/**
 * Porta de consulta de fontes públicas. A implementação deve retornar apenas dados,
 * nunca instruções executáveis ou credenciais.
 */
public interface ExternalSearchClient {

    List<SearchHit> search(String query, int maxResults);

    record SearchHit(String title, String url, String snippet) {
    }
}
