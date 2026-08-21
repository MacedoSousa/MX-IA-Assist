#!/usr/bin/env python3
"""Validate the MX OpenAPI YAML and required evolution routes."""

from pathlib import Path
import sys

import yaml


REQUIRED_PATHS = {
    "/api/v1/conversations/messages",
    "/api/v1/conversations/messages/stream",
    "/api/v1/conversations/{conversationId}/messages",
    "/api/v1/attachments",
    "/api/v1/attachments/{attachmentId}",
    "/api/v1/media/audio/{attachmentId}/transcription",
    "/api/v1/media/images",
    "/api/v1/users/me/preferences",
}


def main() -> None:
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("docs/api/mx-v1.yaml")
    with path.open("r", encoding="utf-8") as stream:
        document = yaml.safe_load(stream)
    paths = set(document.get("paths", {})) if isinstance(document, dict) else set()
    missing = REQUIRED_PATHS - paths
    if missing:
        raise SystemExit(f"Missing OpenAPI paths: {sorted(missing)}")
    schemas = document.get("components", {}).get("schemas", {})
    required_schemas = {"ChatV1Request", "AttachmentDTO", "MessagePageDTO", "TranscriptionDTO", "ImageGenerationRequest", "UserPreferenceDTO"}
    missing_schemas = required_schemas - set(schemas)
    if missing_schemas:
        raise SystemExit(f"Missing OpenAPI schemas: {sorted(missing_schemas)}")
    print({"status": "ok", "paths": len(paths), "schemas": len(schemas)})


if __name__ == "__main__":
    main()
