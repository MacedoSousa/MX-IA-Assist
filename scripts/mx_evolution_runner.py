#!/usr/bin/env python3
"""Bounded host-side runner for MX self-extension jobs.

The Java core only writes declarative JSON jobs. This process is the only component
allowed to invoke Maven, npm, Docker and Git, and it maps validation identifiers to
fixed commands instead of accepting shell strings from the model.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import platform
import re
import shutil
import subprocess
import sys
import tempfile
import time
from dataclasses import dataclass
from pathlib import Path, PurePosixPath
from typing import Any


ALLOWED_PREFIXES = (
    "core-service/src/main/",
    "core-service/src/test/",
    "clients/mx-app/",
    "skills/",
    "scripts/",
    "docs/",
    "infrastructure/docker/",
)
ALLOWED_VALIDATIONS = {
    "maven_test",
    "web_typecheck",
    "web_export",
    "compose_config",
    "docker_build_core",
    "docker_build_web",
}
SECRET_RE = re.compile(
    r"(?i)(^|/)(\.env(?:\..*)?|.*(?:secret|token|password|credential|private.?key).*)$"
)
SLUG_RE = re.compile(r"[a-z0-9]+(?:-[a-z0-9]+){0,5}")
COMMIT_RE = re.compile(r"^[\wÀ-ÿ][\wÀ-ÿ ._:/()+'-]{2,119}$")


@dataclass(frozen=True)
class CommandSpec:
    name: str
    args: tuple[str, ...]
    cwd: Path


class RunnerError(RuntimeError):
    pass


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def project_root_from_script() -> Path:
    return Path(__file__).resolve().parents[1]


def posix_path(value: Any) -> str:
    if not isinstance(value, str) or not value.strip():
        raise RunnerError("file path must be a non-empty string")
    raw = value.strip().replace("\\", "/")
    candidate = PurePosixPath(raw)
    if candidate.is_absolute() or "\x00" in raw:
        raise RunnerError(f"unsafe absolute or NUL file path: {value}")
    parts = candidate.parts
    if not parts or any(part in {"", ".", ".."} for part in parts):
        raise RunnerError(f"unsafe file path: {value}")
    normalized = "/".join(parts)
    if ".git" in parts or SECRET_RE.search(normalized):
        raise RunnerError(f"protected file path: {value}")
    if not normalized.startswith(ALLOWED_PREFIXES):
        raise RunnerError(f"file path outside allowlist: {value}")
    return normalized


def validate_job(job: dict[str, Any], max_files: int, max_file_bytes: int, max_total_bytes: int) -> tuple[dict[str, Any], list[tuple[str, bytes]]]:
    if not isinstance(job, dict):
        raise RunnerError("job must be a JSON object")
    submission = job.get("submission")
    if not isinstance(submission, dict):
        raise RunnerError("job submission is missing")
    job_id = str(job.get("jobId", "")).strip()
    if not re.fullmatch(r"[0-9a-fA-F-]{36}", job_id):
        raise RunnerError("job id is invalid")
    extension_type = str(submission.get("type", "")).upper()
    if extension_type not in {"AGENT", "SKILL", "TOOL"}:
        raise RunnerError("extension type must be AGENT, SKILL or TOOL")
    slug = str(submission.get("slug", "")).strip()
    if not SLUG_RE.fullmatch(slug):
        raise RunnerError("extension slug must be lowercase kebab-case")
    description = str(submission.get("description", "")).strip()
    if not description or len(description) > 4000:
        raise RunnerError("extension description is missing or too long")
    files = submission.get("files")
    if not isinstance(files, list) or not files or len(files) > max_files:
        raise RunnerError("extension files count is outside the allowed range")
    changes: list[tuple[str, bytes]] = []
    seen_paths: set[str] = set()
    total = 0
    for file_change in files:
        if not isinstance(file_change, dict):
            raise RunnerError("each extension file must be an object")
        relative = posix_path(file_change.get("path"))
        if relative in seen_paths:
            raise RunnerError(f"duplicate extension file path: {relative}")
        seen_paths.add(relative)
        content = file_change.get("content")
        if not isinstance(content, str):
            raise RunnerError(f"content is not text for {relative}")
        encoded = content.encode("utf-8")
        if len(encoded) > max_file_bytes:
            raise RunnerError(f"file is too large: {relative}")
        total += len(encoded)
        if total > max_total_bytes:
            raise RunnerError("total extension payload is too large")
        changes.append((relative, encoded))
    validations = submission.get("validations")
    if not isinstance(validations, list) or not validations:
        raise RunnerError("at least one validation is required")
    normalized_validations = [str(value).strip().lower() for value in validations]
    unknown = set(normalized_validations) - ALLOWED_VALIDATIONS
    if unknown:
        raise RunnerError(f"unsupported validations: {sorted(unknown)}")
    commit_message = str(submission.get("commitMessage", "")).strip()
    if "\n" in commit_message or "\r" in commit_message or not COMMIT_RE.fullmatch(commit_message):
        raise RunnerError("commit message is invalid")
    if bool(submission.get("allowPush", False)) and os.getenv("MX_EVOLUTION_ALLOW_PUSH", "false").lower() != "true":
        raise RunnerError("push is disabled; set MX_EVOLUTION_ALLOW_PUSH=true outside the job to enable it")
    return {
        "job_id": job_id,
        "type": extension_type,
        "slug": slug,
        "description": description,
        "validations": normalized_validations,
        "commit_message": commit_message,
        "allow_push": bool(submission.get("allowPush", False)),
    }, changes


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
    result = git(root, "status", "--porcelain")
    if result.stdout.strip():
        raise RunnerError("worktree is not clean; refusing to overwrite user changes")


def command_specs(root: Path, validation: str) -> CommandSpec:
    windows = platform.system().lower().startswith("win")
    compose_dir = root / "infrastructure" / "docker" / "compose"
    core_dir = root / "core-service"
    web_dir = root / "clients" / "mx-app"
    if validation == "maven_test":
        return CommandSpec("maven_test", ("mvnw.cmd", "-q", "test") if windows else ("bash", "./mvnw", "-q", "test"), core_dir)
    if validation == "web_typecheck":
        return CommandSpec("web_typecheck", ("npm.cmd", "run", "typecheck") if windows else ("npm", "run", "typecheck"), web_dir)
    if validation == "web_export":
        return CommandSpec("web_export", ("npx.cmd", "expo", "export", "--platform", "web") if windows else ("npx", "expo", "export", "--platform", "web"), web_dir)
    compose = ("docker-compose.exe",) if windows else ("docker", "compose")
    if validation == "compose_config":
        return CommandSpec("compose_config", compose + ("config",), compose_dir)
    if validation == "docker_build_core":
        return CommandSpec("docker_build_core", compose + ("build", "mx-core"), compose_dir)
    if validation == "docker_build_web":
        return CommandSpec("docker_build_web", compose + ("build", "mx-web"), compose_dir)
    raise RunnerError(f"unsupported validation: {validation}")


def run_validation(root: Path, validation: str, timeout_seconds: int) -> dict[str, Any]:
    spec = command_specs(root, validation)
    started = time.monotonic()
    try:
        completed = subprocess.run(
            list(spec.args),
            cwd=spec.cwd,
            text=True,
            capture_output=True,
            timeout=timeout_seconds,
            check=False,
        )
    except FileNotFoundError as exc:
        raise RunnerError(f"command unavailable for {validation}: {spec.args[0]}") from exc
    except subprocess.TimeoutExpired as exc:
        raise RunnerError(f"validation timed out: {validation}") from exc
    duration_ms = int((time.monotonic() - started) * 1000)
    output = ((completed.stdout or "") + (completed.stderr or "")).strip()
    output_tail = output[-4000:]
    result = {
        "validation": validation,
        "command": list(spec.args),
        "cwd": str(spec.cwd),
        "exitCode": completed.returncode,
        "durationMs": duration_ms,
        "outputTail": output_tail,
    }
    if completed.returncode != 0:
        raise RunnerError(json.dumps(result, ensure_ascii=False))
    return result


def write_changes(root: Path, changes: list[tuple[str, bytes]]) -> dict[str, bytes | None]:
    backups: dict[str, bytes | None] = {}
    for relative, content in changes:
        target = root.joinpath(*PurePosixPath(relative).parts).resolve()
        if root.resolve() not in target.parents:
            raise RunnerError(f"resolved path escaped project root: {relative}")
        backups[relative] = target.read_bytes() if target.exists() else None
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
    return backups


def restore_changes(root: Path, backups: dict[str, bytes | None]) -> None:
    for relative, original in backups.items():
        target = root.joinpath(*PurePosixPath(relative).parts).resolve()
        if original is None:
            target.unlink(missing_ok=True)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(original)


def evidence_path(root: Path, job_id: str) -> Path:
    return root / "docs" / "evidence" / "executions" / f"self-extension-{job_id}.md"


def write_evidence(root: Path, normalized: dict[str, Any], validation_results: list[dict[str, Any]], file_hashes: dict[str, str], commit: str | None, status: str) -> Path:
    target = evidence_path(root, normalized["job_id"])
    target.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        f"# MX Self-Extension Job {normalized['job_id']}",
        "",
        f"- Status: `{status}`",
        f"- Type: `{normalized['type']}`",
        f"- Slug: `{normalized['slug']}`",
        f"- Description: {normalized['description']}",
        f"- Commit: `{commit or 'created after validation by runner'}`",
        "",
        "## Validations",
        "",
        "| Validation | Exit code | Duration (ms) |",
        "|---|---:|---:|",
    ]
    for result in validation_results:
        lines.append(f"| `{result['validation']}` | {result['exitCode']} | {result['durationMs']} |")
    lines += [
        "",
        "## SHA-256",
        "",
        "| File | SHA-256 |",
        "|---|---|",
    ]
    for relative, digest in file_hashes.items():
        lines.append(f"| `{relative}` | `{digest}` |")
    lines += [
        "",
        "The runner maps identifiers to fixed commands, never executes a model-provided shell string, and keeps the Docker daemon outside the Java container.",
        "",
    ]
    target.write_text("\n".join(lines), encoding="utf-8")
    return target


def commit_changes(root: Path, relative_paths: list[str], evidence: Path, message: str) -> str:
    paths = [*relative_paths, evidence.relative_to(root).as_posix()]
    git(root, "add", "--", *paths)
    staged = git(root, "diff", "--cached", "--name-only").stdout.splitlines()
    expected = set(paths)
    if set(staged) != expected:
        git(root, "reset", "--", *paths, check=False)
        raise RunnerError("staged paths differ from the declared allowlist")
    git(root, "diff", "--cached", "--check")
    commit_result = git(root, "commit", "-m", message)
    commit_hash = git(root, "rev-parse", "HEAD").stdout.strip()
    return commit_hash


def create_validation_worktree(root: Path, changes: list[tuple[str, bytes]]) -> tuple[Path, Path]:
    base = root / "data" / "evolution" / "staging"
    base.mkdir(parents=True, exist_ok=True)
    temporary_root = Path(tempfile.mkdtemp(prefix="job-", dir=base))
    staging_root = temporary_root / "repo"
    try:
        git(root, "worktree", "add", "--detach", str(staging_root), "HEAD")
        write_changes(staging_root, changes)
        return temporary_root, staging_root
    except Exception:
        git(root, "worktree", "remove", "--force", str(staging_root), check=False)
        shutil.rmtree(temporary_root, ignore_errors=True)
        raise


def remove_validation_worktree(root: Path, temporary_root: Path | None, staging_root: Path | None) -> None:
    if staging_root is not None:
        git(root, "worktree", "remove", "--force", str(staging_root), check=False)
    if temporary_root is not None:
        shutil.rmtree(temporary_root, ignore_errors=True)


def install_frontend_dependencies(root: Path, timeout_seconds: int) -> dict[str, Any]:
    web_dir = root / "clients" / "mx-app"
    if (web_dir / "node_modules").exists():
        return {"validation": "web_dependencies_cached", "exitCode": 0, "durationMs": 0, "command": [], "cwd": str(web_dir), "outputTail": ""}
    executable = "npm.cmd" if platform.system().lower().startswith("win") else "npm"
    started = time.monotonic()
    completed = subprocess.run(
        [executable, "ci", "--ignore-scripts"],
        cwd=web_dir,
        text=True,
        capture_output=True,
        timeout=timeout_seconds,
        check=False,
    )
    result = {
        "validation": "web_dependencies_install",
        "command": [executable, "ci", "--ignore-scripts"],
        "cwd": str(web_dir),
        "exitCode": completed.returncode,
        "durationMs": int((time.monotonic() - started) * 1000),
        "outputTail": ((completed.stdout or "") + (completed.stderr or "")).strip()[-4000:],
    }
    if completed.returncode != 0:
        raise RunnerError(json.dumps(result, ensure_ascii=False))
    return result


def process_job(root: Path, job_path: Path, args: argparse.Namespace) -> dict[str, Any]:
    raw_job = json.loads(job_path.read_text(encoding="utf-8"))
    normalized, changes = validate_job(raw_job, args.max_files, args.max_file_bytes, args.max_total_bytes)
    file_hashes = {relative: sha256_bytes(content) for relative, content in changes}
    ensure_clean_worktree(root)
    if args.dry_run:
        return {"jobId": normalized["job_id"], "status": "DRY_RUN", "files": [p for p, _ in changes], "validations": normalized["validations"], "sha256": file_hashes}

    running_path = root / "data" / "evolution" / "jobs" / "running" / job_path.name
    running_path.parent.mkdir(parents=True, exist_ok=True)
    shutil.move(str(job_path), str(running_path))
    backups: dict[str, bytes | None] = {}
    validation_results: list[dict[str, Any]] = []
    evidence: Path | None = None
    commit_hash: str | None = None
    temporary_root: Path | None = None
    staging_root: Path | None = None
    try:
        head_before = git(root, "rev-parse", "HEAD").stdout.strip()
        temporary_root, staging_root = create_validation_worktree(root, changes)
        if any(validation in {"web_typecheck", "web_export"} for validation in normalized["validations"]):
            validation_results.append(install_frontend_dependencies(staging_root, args.timeout_seconds))
        for validation in normalized["validations"]:
            validation_results.append(run_validation(staging_root, validation, args.timeout_seconds))
        remove_validation_worktree(root, temporary_root, staging_root)
        temporary_root = None
        staging_root = None
        ensure_clean_worktree(root)
        head_after = git(root, "rev-parse", "HEAD").stdout.strip()
        if head_before != head_after:
            raise RunnerError("main worktree changed while the job was being validated")
        backups = write_changes(root, changes)
        evidence = write_evidence(root, normalized, validation_results, file_hashes, None, "VALIDATED")
        commit_hash = commit_changes(root, [relative for relative, _ in changes], evidence, normalized["commit_message"])
        if normalized["allow_push"]:
            if os.getenv("MX_EVOLUTION_ALLOW_PUSH", "false").lower() != "true":
                raise RunnerError("push was requested but MX_EVOLUTION_ALLOW_PUSH is not true")
            git(root, "push")
        final = {
            **raw_job,
            "status": "COMPLETED",
            "result": {
                "commit": commit_hash,
                "files": [relative for relative, _ in changes],
                "sha256": file_hashes,
                "validations": validation_results,
                "evidence": evidence.relative_to(root).as_posix(),
            },
        }
        completed_path = root / "data" / "evolution" / "jobs" / "completed" / job_path.name
        completed_path.parent.mkdir(parents=True, exist_ok=True)
        completed_path.write_text(json.dumps(final, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        running_path.unlink(missing_ok=True)
        return final
    except Exception as exc:
        if commit_hash is None:
            restore_changes(root, backups)
            if evidence is not None:
                evidence.unlink(missing_ok=True)
            git(root, "reset", "--", *[relative for relative, _ in changes], check=False)
        failed = {
            **raw_job,
            "status": "FAILED",
            "result": {
                "error": str(exc),
                "sha256": file_hashes,
                "validations": validation_results,
                "commit": commit_hash,
            },
        }
        failed_path = root / "data" / "evolution" / "jobs" / "failed" / job_path.name
        failed_path.parent.mkdir(parents=True, exist_ok=True)
        failed_path.write_text(json.dumps(failed, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        running_path.unlink(missing_ok=True)
        return failed
    finally:
        if temporary_root is not None or staging_root is not None:
            remove_validation_worktree(root, temporary_root, staging_root)


def append_audit(root: Path, result: dict[str, Any]) -> None:
    audit = root / "data" / "evolution" / "audit.jsonl"
    audit.parent.mkdir(parents=True, exist_ok=True)
    with audit.open("a", encoding="utf-8") as handle:
        handle.write(json.dumps({
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
            "jobId": result.get("jobId"),
            "status": result.get("status"),
            "result": result.get("result", {}),
        }, ensure_ascii=False) + "\n")


def run_once(root: Path, args: argparse.Namespace) -> int:
    pending = root / "data" / "evolution" / "jobs" / "pending"
    pending.mkdir(parents=True, exist_ok=True)
    jobs = sorted(pending.glob("*.json"))
    if not jobs:
        return 0
    for job_path in jobs:
        try:
            result = process_job(root, job_path, args)
        except Exception as exc:
            result = {"jobId": job_path.stem, "status": "REJECTED", "result": {"error": str(exc)}}
            failed = root / "data" / "evolution" / "jobs" / "failed" / job_path.name
            failed.parent.mkdir(parents=True, exist_ok=True)
            shutil.move(str(job_path), str(failed))
            failed.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        append_audit(root, result)
        print(json.dumps({"jobId": result.get("jobId"), "status": result.get("status")}, ensure_ascii=False))
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Run bounded MX self-extension jobs")
    parser.add_argument("--root", type=Path, default=Path(os.getenv("MX_PROJECT_ROOT", project_root_from_script())))
    parser.add_argument("--once", action="store_true", help="process pending jobs once and exit")
    parser.add_argument("--watch", action="store_true", help="poll pending jobs continuously")
    parser.add_argument("--interval-seconds", type=int, default=int(os.getenv("MX_EVOLUTION_INTERVAL_SECONDS", "10")))
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--timeout-seconds", type=int, default=int(os.getenv("MX_EVOLUTION_TIMEOUT_SECONDS", "1800")))
    parser.add_argument("--max-files", type=int, default=int(os.getenv("MX_EVOLUTION_MAX_FILES", "20")))
    parser.add_argument("--max-file-bytes", type=int, default=int(os.getenv("MX_EVOLUTION_MAX_FILE_BYTES", "262144")))
    parser.add_argument("--max-total-bytes", type=int, default=int(os.getenv("MX_EVOLUTION_MAX_TOTAL_BYTES", "1048576")))
    args = parser.parse_args()
    root = args.root.resolve()
    if not (root / ".git").exists():
        print("error: --root must point to an MX Git worktree", file=sys.stderr)
        return 2
    if args.watch:
        while True:
            run_once(root, args)
            time.sleep(max(1, args.interval_seconds))
    return run_once(root, args)


if __name__ == "__main__":
    raise SystemExit(main())
