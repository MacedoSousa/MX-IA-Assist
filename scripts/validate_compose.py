#!/usr/bin/env python3
"""Validate the MX Compose document as YAML and check required services."""

from pathlib import Path
import sys

import yaml


REQUIRED_SERVICES = {"postgres", "redis", "ollama", "open-webui", "mx-core", "mx-web"}


def main() -> None:
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("infrastructure/docker/compose/docker-compose.yml")
    with path.open("r", encoding="utf-8") as stream:
        document = yaml.safe_load(stream)
    if not isinstance(document, dict) or not isinstance(document.get("services"), dict):
        raise SystemExit("Compose document must contain a services mapping")
    services = set(document["services"])
    missing = REQUIRED_SERVICES - services
    if missing:
        raise SystemExit(f"Missing required services: {sorted(missing)}")
    ollama = document["services"]["ollama"]
    if ollama.get("restart") != "unless-stopped":
        raise SystemExit("Ollama must remain always-on with restart unless-stopped")
    if not str(ollama.get("ports", [""])[0]).startswith("127.0.0.1:"):
        raise SystemExit("Ollama must not be published on all host interfaces")
    print({"status": "ok", "services": sorted(services), "ollamaPort": ollama["ports"][0]})


if __name__ == "__main__":
    main()
