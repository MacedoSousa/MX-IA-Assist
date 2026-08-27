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


if __name__ == "__main__":
    unittest.main()
