"""Always-on coordinator for MX knowledge maintenance and self-extension jobs.

The coordinator only invokes fixed, repository-owned scripts. It never accepts a
shell command from a model or a document. Push remains disabled unless explicitly
enabled in the environment and the underlying runners allow it.
"""

from __future__ import annotations

import argparse
import os
import platform
import subprocess
import sys
import time
from pathlib import Path


LOCK_RELATIVE_PATH = Path("data/evolution/maintenance-agent.lock")


def project_root_from_script() -> Path:
    return Path(__file__).resolve().parents[1]


def acquire_lock(root: Path, stale_after_seconds: int = 21600) -> Path | None:
    lock = root / LOCK_RELATIVE_PATH
    lock.parent.mkdir(parents=True, exist_ok=True)
    try:
        descriptor = os.open(lock, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
        with os.fdopen(descriptor, "w", encoding="utf-8") as handle:
            handle.write(f"pid={os.getpid()}\\nstarted={time.time()}\\n")
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


def run_script(root: Path, script: str, args: list[str], timeout: int) -> int:
    executable = sys.executable
    completed = subprocess.run(
        [executable, str(root / "scripts" / script), *args],
        cwd=root,
        text=True,
        timeout=timeout,
        check=False,
    )
    return completed.returncode


def cycle(root: Path, args: argparse.Namespace) -> int:
    print("[MX] knowledge cycle", flush=True)
    knowledge_args = ["--once"]
    if args.dry_run:
        knowledge_args.append("--dry-run")
    if args.no_commit:
        knowledge_args.append("--no-commit")
    if args.allow_push:
        knowledge_args.append("--allow-push")
    knowledge_exit = run_script(root, "mx_knowledge_cycle.py", knowledge_args, args.timeout_seconds)
    if knowledge_exit != 0:
        print(f"[MX] knowledge cycle failed with exit code {knowledge_exit}", file=sys.stderr, flush=True)

    print("[MX] self-extension queue", flush=True)
    evolution_args = ["--once"]
    if args.dry_run:
        evolution_args.append("--dry-run")
    evolution_exit = run_script(root, "mx_evolution_runner.py", evolution_args, args.timeout_seconds)
    if evolution_exit != 0:
        print(f"[MX] self-extension runner failed with exit code {evolution_exit}", file=sys.stderr, flush=True)
    return 1 if knowledge_exit != 0 or evolution_exit != 0 else 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Run the bounded MX maintenance agent")
    parser.add_argument("--root", type=Path, default=Path(os.getenv("MX_PROJECT_ROOT", project_root_from_script())))
    parser.add_argument("--watch", action="store_true")
    parser.add_argument("--once", action="store_true")
    parser.add_argument("--interval-seconds", type=int, default=int(os.getenv("MX_MAINTENANCE_INTERVAL_SECONDS", "21600")))
    parser.add_argument("--timeout-seconds", type=int, default=int(os.getenv("MX_MAINTENANCE_TIMEOUT_SECONDS", "1800")))
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--no-commit", action="store_true")
    parser.add_argument("--allow-push", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    if not (root / ".git").exists():
        print("error: --root must point to an MX Git worktree", file=sys.stderr)
        return 2
    lock = acquire_lock(root)
    if lock is None:
        print("[MX] maintenance agent already running; skipping this invocation", flush=True)
        return 0
    try:
        if args.watch:
            while True:
                cycle(root, args)
                time.sleep(max(300, args.interval_seconds))
        return cycle(root, args)
    finally:
        release_lock(lock)


if __name__ == "__main__":
    raise SystemExit(main())
