"""Validate Ollama's local embedding endpoint without printing embeddings or input text."""

from __future__ import annotations

import json
from urllib.request import Request, urlopen

payload = json.dumps({"model": "nomic-embed-text", "input": "MX semantic retrieval validation"}).encode("utf-8")
request = Request(
    "http://ollama:11434/api/embed",
    data=payload,
    headers={"Content-Type": "application/json"},
    method="POST",
)
with urlopen(request, timeout=60) as response:
    result = json.loads(response.read().decode("utf-8"))

embeddings = result.get("embeddings", [])
dimension = len(embeddings[0]) if embeddings and isinstance(embeddings[0], list) else 0
print(json.dumps({"embeddingEndpoint": "PASS", "model": result.get("model"), "vectors": len(embeddings), "dimensions": dimension}, sort_keys=True))
