"""Tests for the bounded static-workspace preview recipe.

Run with: python3 -m unittest scripts/test_mx_evolution_runner.py
"""

from __future__ import annotations

import importlib.util
import json
import socket
import sys
import tempfile
import time
import unittest
import warnings
from pathlib import Path
from urllib.request import urlopen


RUNNER_PATH = Path(__file__).with_name("mx_evolution_runner.py")
SPEC = importlib.util.spec_from_file_location("mx_evolution_runner", RUNNER_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("Could not load mx_evolution_runner")
RUNNER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = RUNNER
SPEC.loader.exec_module(RUNNER)
warnings.filterwarnings("ignore", category=ResourceWarning, module="subprocess")


class StaticWorkspacePreviewTests(unittest.TestCase):

    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = Path(self.temp_dir.name)
        (self.root / ".git").mkdir()
        self.workspace = self.root / "workspaces"
        self.project = self.workspace / "valid-project"
        self.project.mkdir(parents=True)
        (self.project / "index.html").write_text("<h1>workspace preview proof</h1>", encoding="utf-8")
        self.request_id = "11111111-1111-1111-1111-111111111111"
        _, self.pending, self.running, self.stopped = RUNNER.workspace_preview_paths(self.root)
        self.pending.mkdir(parents=True)

    def tearDown(self) -> None:
        self._stop_if_running()
        self.temp_dir.cleanup()

    def _stop_if_running(self) -> None:
        state_path = self.running / f"{self.request_id}.state.json"
        if state_path.is_file():
            RUNNER.stop_static_preview(self.root, self.request_id)

    def _write_request(self, **overrides: object) -> Path:
        payload: dict[str, object] = {
            "requestId": self.request_id,
            "recipe": "static-http",
            "project": "valid-project",
            "host": "127.0.0.1",
            "portRange": "48000-48099",
        }
        payload.update(overrides)
        path = self.pending / f"{self.request_id}.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_rejects_invalid_json(self) -> None:
        path = self.pending / "broken.json"
        path.write_text("{not-json", encoding="utf-8")

        with self.assertRaisesRegex(RUNNER.RunnerError, "not valid JSON"):
            RUNNER.load_static_preview_request(path, self.workspace)

    def test_rejects_project_traversal_and_non_loopback_recipe(self) -> None:
        traversal = self._write_request(project="../outside")
        with self.assertRaisesRegex(RUNNER.RunnerError, "project is invalid"):
            RUNNER.load_static_preview_request(traversal, self.workspace)

        remote_host = self._write_request(host="0.0.0.0")
        with self.assertRaisesRegex(RUNNER.RunnerError, "recipe is not allowed"):
            RUNNER.load_static_preview_request(remote_host, self.workspace)

        altered_range = self._write_request(portRange="1-65535")
        with self.assertRaisesRegex(RUNNER.RunnerError, "recipe is not allowed"):
            RUNNER.load_static_preview_request(altered_range, self.workspace)

    def test_starts_and_stops_only_a_loopback_static_server(self) -> None:
        self._write_request()

        result = RUNNER.start_static_preview(self.root)

        self.assertEqual("STARTED", result["status"])
        self.assertEqual("127.0.0.1", result["host"])
        self.assertIn(result["port"], range(48000, 48100))
        state_path = self.running / f"{self.request_id}.state.json"
        self.assertTrue(state_path.is_file())
        self.assertTrue((self.running / f"{self.request_id}.json").is_file())

        response_body = b""
        for _ in range(20):
            try:
                with urlopen(result["url"], timeout=1) as response:
                    response_body = response.read()
                break
            except OSError:
                time.sleep(0.05)
        self.assertIn(b"workspace preview proof", response_body)

        stopped = RUNNER.stop_static_preview(self.root, self.request_id)
        self.assertEqual("STOPPED", stopped["status"])
        self.assertTrue((self.stopped / f"{self.request_id}.state.json").is_file())
        self.assertTrue((self.stopped / f"{self.request_id}.json").is_file())

        server_stopped = False
        for _ in range(20):
            try:
                with socket.create_connection(("127.0.0.1", result["port"]), timeout=0.25):
                    pass
            except OSError:
                server_stopped = True
                break
            time.sleep(0.05)
        self.assertTrue(server_stopped, "o servidor de preview deve liberar a porta após a parada")


class StaticWorkspaceRecipeTests(unittest.TestCase):

    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.root = Path(self.temp_dir.name)
        (self.root / ".git").mkdir()
        self.project = self.root / "workspaces" / "valid-project"
        self.project.mkdir(parents=True)
        (self.project / "index.html").write_text("<!doctype html><html><body>build proof</body></html>", encoding="utf-8")
        (self.project / "styles.css").write_text("body { color: #123; }", encoding="utf-8")
        self.request_id = "22222222-2222-2222-2222-222222222222"
        _, self.pending, self.completed, self.failed = RUNNER.workspace_recipe_paths(self.root)
        self.pending.mkdir(parents=True)

    def tearDown(self) -> None:
        self.temp_dir.cleanup()

    def _write_request(self, **overrides: object) -> Path:
        payload: dict[str, object] = {
            "requestId": self.request_id,
            "recipe": "static-validate",
            "profile": "static-html-v1",
            "project": "valid-project",
        }
        payload.update(overrides)
        path = self.pending / f"{self.request_id}.json"
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path

    def test_rejects_unknown_recipe_and_project_traversal(self) -> None:
        unknown = self._write_request(recipe="shell")
        with self.assertRaisesRegex(RUNNER.RunnerError, "not allowed"):
            RUNNER.load_static_recipe_request(unknown, self.root / "workspaces")
        traversal = self._write_request(project="../outside")
        with self.assertRaisesRegex(RUNNER.RunnerError, "project is invalid"):
            RUNNER.load_static_recipe_request(traversal, self.root / "workspaces")

    def test_validates_and_builds_only_static_project_in_staging(self) -> None:
        self._write_request()
        validated = RUNNER.process_static_recipe(self.root)
        self.assertEqual("VALIDATED", validated["status"])
        self.assertEqual(2, validated["fileCount"])
        self._write_request(recipe="static-build")
        built = RUNNER.process_static_recipe(self.root)
        self.assertEqual("BUILT", built["status"])
        staged = self.root / "workspaces" / built["staging"]
        self.assertTrue((staged / "site" / "index.html").is_file())
        self.assertTrue((staged / "manifest.json").is_file())


if __name__ == "__main__":
    unittest.main()
