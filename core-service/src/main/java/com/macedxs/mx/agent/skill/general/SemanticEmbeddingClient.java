package com.macedxs.mx.agent.skill.general;

import java.util.List;

/** Local-only semantic vector provider; implementations must not log input text or vectors. */
public interface SemanticEmbeddingClient {
    List<double[]> embed(List<String> inputs);
}
