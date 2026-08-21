#!/usr/bin/env python3
"""Benchmark determinístico do índice de estudos usado pelo MX.

O benchmark não chama modelos nem cria dados artificiais: lê o knowledge_chunks.jsonl
versionado e executa consultas representativas sobre o conteúdo importado.
"""

from __future__ import annotations

import argparse
import json
import statistics
import time
from pathlib import Path


DEFAULT_QUERIES = [
    "RAG agentes recuperação avaliação",
    "engenharia de software testes TDD qualidade",
    "Docker infraestrutura segurança observabilidade",
    "ensino aprendizagem adaptativa produto",
    "machine learning dados MLOps",
]


def tokens(value: str) -> set[str]:
    return {
        token.strip(".,;:!?()[]{}\"'`_/-").lower()
        for token in value.split()
        if len(token.strip(".,;:!?()[]{}\"'`_/-")) >= 3
    }


def load_chunks(path: Path) -> list[dict]:
    chunks: list[dict] = []
    with path.open("r", encoding="utf-8") as stream:
        for line_number, line in enumerate(stream, 1):
            if not line.strip():
                continue
            value = json.loads(line)
            if not isinstance(value, dict) or not isinstance(value.get("text"), str):
                raise ValueError(f"Invalid chunk at line {line_number}")
            chunks.append(value)
    if not chunks:
        raise ValueError("The chunk index is empty")
    return chunks


def retrieve(chunks: list[dict], query: str, limit: int = 4) -> list[dict]:
    query_tokens = tokens(query)
    scored: list[tuple[int, dict]] = []
    for chunk in chunks:
        text_tokens = tokens(
            " ".join(
                [
                    str(chunk.get("heading", "")),
                    " ".join(str(domain) for domain in chunk.get("domains", [])),
                    str(chunk.get("text", "")),
                ]
            )
        )
        overlap = query_tokens & text_tokens
        if overlap:
            score = len(overlap) * 10
            if query_tokens and overlap == query_tokens:
                score += 5
            scored.append((score, chunk))
    scored.sort(key=lambda item: (-item[0], str(item[1].get("id", ""))))
    return [chunk for _, chunk in scored[:limit]]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "index",
        nargs="?",
        type=Path,
        default=Path("core-service/src/main/resources/knowledge/estudos/knowledge_chunks.jsonl"),
    )
    parser.add_argument("--iterations", type=int, default=100)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if args.iterations <= 0:
        raise SystemExit("--iterations must be positive")

    load_started = time.perf_counter()
    chunks = load_chunks(args.index)
    load_ms = (time.perf_counter() - load_started) * 1000

    samples: list[float] = []
    result_counts: list[int] = []
    for _ in range(args.iterations):
        for query in DEFAULT_QUERIES:
            started = time.perf_counter()
            result_counts.append(len(retrieve(chunks, query)))
            samples.append((time.perf_counter() - started) * 1000)

    report = {
        "index": str(args.index),
        "chunkCount": len(chunks),
        "queryCount": len(DEFAULT_QUERIES),
        "iterations": args.iterations,
        "loadMs": round(load_ms, 3),
        "retrievalMs": {
            "min": round(min(samples), 3),
            "median": round(statistics.median(samples), 3),
            "p95": round(sorted(samples)[int(len(samples) * 0.95) - 1], 3),
            "max": round(max(samples), 3),
        },
        "resultCountRange": [min(result_counts), max(result_counts)],
    }
    rendered = json.dumps(report, indent=2, ensure_ascii=False) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(rendered, encoding="utf-8")
    print(rendered, end="")


if __name__ == "__main__":
    main()
