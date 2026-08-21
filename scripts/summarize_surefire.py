#!/usr/bin/env python3
"""Summarize JUnit XML reports emitted by Maven Surefire."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def main() -> None:
    report_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("core-service/target/surefire-reports")
    values = []
    for path in sorted(report_dir.glob("TEST-*.xml")):
        root = ET.parse(path).getroot()
        values.append(
            {
                "tests": int(root.attrib.get("tests", "0")),
                "failures": int(root.attrib.get("failures", "0")),
                "errors": int(root.attrib.get("errors", "0")),
                "skipped": int(root.attrib.get("skipped", "0")),
            }
        )
    print(
        {
            "suites": len(values),
            "tests": sum(item["tests"] for item in values),
            "failures": sum(item["failures"] for item in values),
            "errors": sum(item["errors"] for item in values),
            "skipped": sum(item["skipped"] for item in values),
        }
    )


if __name__ == "__main__":
    main()
