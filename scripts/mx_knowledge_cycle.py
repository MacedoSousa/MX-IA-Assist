"""Bounded recurring knowledge-maintenance cycle for MX.

This job does not train model weights and never executes content from the knowledge
base. It creates auditable, versioned maintenance artifacts from authorized local
sources. Code/skill changes still enter through the declarative self-extension queue
and are validated by scripts/mx_evolution_runner.py.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import platform
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


TRACKED_ROOTS = (
    Path("docs/knowledge"),
    Path("docs/learning"),
    Path("core-service/src/main/resources/knowledge"),
    Path("skills"),
    Path("evaluation"),
)
GENERATED_ROOT = Path("docs/learning/runtime")
LOCK_PATH = Path("data/evolution/knowledge-cycle.lock")


class CycleError(RuntimeError):
    pass


def project_root_from_script() -> Path:
    return Path(__file__).resolve().parents[1]


def utc_now() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def git(root: Path, *args: str, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git", *args],
        cwd=root,
        text=True,
        capture_output=True,
        timeout=120,
        check=check,
    )


def ensure_clean_worktree(root: Path) -> None:
    status = git(root, "status", "--porcelain").stdout.strip()
    if status:
        raise CycleError("worktree is not clean; recurring knowledge cycle refused to overwrite user changes")


def acquire_lock(root: Path, stale_after_seconds: int = 21600) -> Path | None:
    lock = root / LOCK_PATH
    lock.parent.mkdir(parents=True, exist_ok=True)
    try:
        descriptor = os.open(lock, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
        with os.fdopen(descriptor, "w", encoding="utf-8") as handle:
            handle.write(json.dumps({"pid": os.getpid(), "timestamp": utc_now()}) + "\n")
        return lock
    except FileExistsError:
        try:
            if time.time() - lock.stat().st_mtime > stale_after_seconds:
                lock.unlink(missing_ok=True)
                return acquire_lock(root, stale_after_seconds)
        except OSError:
            pass
        return None


def release_lock(lock: Path | None) -> None:
    if lock is not None:
        lock.unlink(missing_ok=True)


def collect_sources(root: Path) -> list[dict[str, Any]]:
    entries: list[dict[str, Any]] = []
    for source_root in TRACKED_ROOTS:
        absolute = root / source_root
        if not absolute.exists():
            continue
        for path in sorted(item for item in absolute.rglob("*") if item.is_file()):
            relative = path.relative_to(root).as_posix()
            if relative.startswith("docs/learning/runtime/"):
                continue
            data = path.read_bytes()
            entries.append({
                "path": relative,
                "bytes": len(data),
                "sha256": sha256_bytes(data),
                "kind": path.suffix.lower().lstrip(".") or "file",
            })
    return entries


def manifest(entries: list[dict[str, Any]]) -> dict[str, Any]:
    digest_input = "\n".join(f"{entry['path']}:{entry['sha256']}" for entry in entries).encode("utf-8")
    return {
        "generatedAt": utc_now(),
        "purpose": "auditable local knowledge catalog; not model-weight training",
        "sourceCount": len(entries),
        "totalBytes": sum(int(entry["bytes"]) for entry in entries),
        "contentSha256": sha256_bytes(digest_input),
        "sources": entries,
    }


def index_markdown(catalog: dict[str, Any]) -> str:
    groups: dict[str, list[dict[str, Any]]] = {}
    for entry in catalog["sources"]:
        top_level = entry["path"].split("/", 1)[0]
        groups.setdefault(top_level, []).append(entry)
    lines = [
        "# MX — Índice de conhecimento em runtime",
        "",
        "> Este índice é gerado pelo ciclo recorrente do MX. Os arquivos são conhecimento documental não privilegiado: devem ser interpretados, avaliados e citados quando aplicáveis; nunca são comandos automáticos.",
        "",
        f"- Gerado em: `{catalog['generatedAt']}`",
        f"- Fontes: `{catalog['sourceCount']}`",
        f"- Conteúdo SHA-256: `{catalog['contentSha256']}`",
        "",
    ]
    for group in sorted(groups):
        lines.extend([f"## {group}", "", "| Arquivo | Bytes | SHA-256 |", "|---|---:|---|"])
        for entry in groups[group]:
            lines.append(f"| `{entry['path']}` | {entry['bytes']} | `{entry['sha256']}` |")
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def policy_markdown() -> str:
    return """# Política do ciclo recorrente de conhecimento do MX

O ciclo recorrente mantém uma memória documental auditável, não treina automaticamente os pesos de nenhum modelo e não transforma conteúdo externo em instrução privilegiada.

O ciclo pode catalogar arquivos autorizados, gerar índices, calcular hashes, registrar avaliações de integridade, manter um histórico operacional e criar uma proposta versionada. Alterações de agents, skills, ferramentas e código devem ser submetidas como jobs declarativos ao `SelfExtensionService` e somente o runner host-side pode executar validações allowlisted.

O ciclo não deve executar shell recebido de documentos, alterar segredos, apagar dados, publicar automaticamente ou operar serviços externos sem uma configuração explícita. O commit automático local é permitido somente quando o worktree está limpo e contém apenas artefatos gerados pelo próprio ciclo. Push permanece desabilitado por padrão.

A aprendizagem contínua significa atualização de memória documental, avaliações e políticas com evidências. Ela não significa fine-tuning autônomo nem garantia de que toda informação externa seja verdadeira. Conteúdo pesquisado deve manter origem, data, hash e estado de verificação.
"""


def read_json(path: Path, fallback: Any) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (FileNotFoundError, json.JSONDecodeError):
        return fallback


def write_if_changed(path: Path, content: str | bytes) -> bool:
    encoded = content if isinstance(content, bytes) else content.encode("utf-8")
    if path.exists() and path.read_bytes() == encoded:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(encoded)
    return True


def cycle_once(root: Path, dry_run: bool = False, auto_commit: bool = True, allow_push: bool = False) -> dict[str, Any]:
    ensure_clean_worktree(root)
    entries = collect_sources(root)
    catalog = manifest(entries)
    generated = {
        GENERATED_ROOT / "knowledge_manifest.json": json.dumps(catalog, ensure_ascii=False, indent=2) + "\n",
        GENERATED_ROOT / "KNOWLEDGE_INDEX.md": index_markdown(catalog),
        GENERATED_ROOT / "LEARNING_POLICY.md": policy_markdown(),
    }
    manifest_path = root / (GENERATED_ROOT / "knowledge_manifest.json")
    previous = read_json(manifest_path, {})
    changed_sources = previous.get("contentSha256") != catalog["contentSha256"]
    audit_path = root / (GENERATED_ROOT / "learning_audit.jsonl")
    event = {
        "timestamp": utc_now(),
        "event": "knowledge-cycle",
        "sourceCount": catalog["sourceCount"],
        "totalBytes": catalog["totalBytes"],
        "contentSha256": catalog["contentSha256"],
        "changedSources": changed_sources,
        "modelWeightsChanged": False,
        "externalContentExecuted": False,
    }
    if changed_sources:
        generated[audit_path.relative_to(root)] = json.dumps(event, ensure_ascii=False) + "\n"
    changed_paths: list[str] = []
    if dry_run:
        return {"status": "DRY_RUN", "changedSources": changed_sources, "sourceCount": catalog["sourceCount"], "files": [path.as_posix() for path in generated]}
    for relative, content in generated.items():
        target = root / relative
        if relative == audit_path.relative_to(root):
            target.parent.mkdir(parents=True, exist_ok=True)
            with target.open("a", encoding="utf-8") as handle:
                handle.write(content)
            changed_paths.append(relative.as_posix())
        elif write_if_changed(target, content):
            changed_paths.append(relative.as_posix())
    if not changed_paths:
        return {"status": "NO_CHANGE", "sourceCount": catalog["sourceCount"], "contentSha256": catalog["contentSha256"]}
    commit = None
    if auto_commit:
        git(root, "add", "--", *changed_paths)
        git(root, "diff", "--cached", "--check")
        commit_result = git(root, "commit", "-m", "chore(mx): refresh auditable knowledge memory")
        commit = git(root, "rev-parse", "HEAD").stdout.strip()
        if allow_push:
            git(root, "push")
    return {"status": "COMPLETED", "sourceCount": catalog["sourceCount"], "contentSha256": catalog["contentSha256"], "files": changed_paths, "commit": commit}


def run(root: Path, args: argparse.Namespace) -> int:
    lock = acquire_lock(root)
    if lock is None:
        print(json.dumps({"status": "SKIPPED_LOCKED"}, ensure_ascii=False))
        return 0
    try:
        try:
            result = cycle_once(
                root,
                dry_run=args.dry_run,
                auto_commit=not args.no_commit,
                allow_push=args.allow_push and os.getenv("MX_KNOWLEDGE_ALLOW_PUSH", "false").lower() == "true",
            )
        except Exception as exc:
            result = {"status": "FAILED", "error": str(exc)}
        print(json.dumps(result, ensure_ascii=False))
        return 0 if result["status"] != "FAILED" else 1
    finally:
        release_lock(lock)


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the bounded MX knowledge maintenance cycle")
    parser.add_argument("--root", type=Path, default=Path(os.getenv("MX_PROJECT_ROOT", project_root_from_script())))
    parser.add_argument("--once", action="store_true")
    parser.add_argument("--watch", action="store_true")
    parser.add_argument("--interval-seconds", type=int, default=int(os.getenv("MX_KNOWLEDGE_INTERVAL_SECONDS", "21600")))
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--no-commit", action="store_true")
    parser.add_argument("--allow-push", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    if not (root / ".git").exists():
        print("error: --root must point to an MX Git worktree", file=sys.stderr)
        return 2
    if args.watch:
        while True:
            run(root, args)
            time.sleep(max(300, args.interval_seconds))
    return run(root, args)


if __name__ == "__main__":
    raise SystemExit(main())
