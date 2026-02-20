#!/usr/bin/env python3
"""Generate a markdown vulnerability triage summary from OWASP Dependency-Check JSON output."""

from __future__ import annotations

import argparse
import datetime as dt
import json
from pathlib import Path
from typing import Any


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate dependency vulnerability triage summary.")
    parser.add_argument(
        "--input",
        default="target/dependency-vulnerability/dependency-check-report.json",
        help="Path to dependency-check JSON report.",
    )
    parser.add_argument(
        "--output",
        default="target/dependency-vulnerability/triage-summary.md",
        help="Path where markdown summary should be written.",
    )
    parser.add_argument(
        "--max-findings",
        type=int,
        default=50,
        help="Maximum number of findings to list in detail.",
    )
    return parser.parse_args()


def severity_rank(severity: str | None) -> int:
    order = {
        "CRITICAL": 5,
        "HIGH": 4,
        "MEDIUM": 3,
        "LOW": 2,
        "INFO": 1,
    }
    if severity is None:
        return 0
    return order.get(severity.upper(), 0)


def extract_score(vuln: dict[str, Any]) -> float:
    for key in ("cvssv4", "cvssv3", "cvssv2"):
        item = vuln.get(key)
        if isinstance(item, dict):
            score = item.get("baseScore")
            if isinstance(score, (int, float)):
                return float(score)
    if isinstance(vuln.get("cvssScore"), (int, float)):
        return float(vuln["cvssScore"])
    return 0.0


def write_missing_report(output_path: Path, input_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    now = dt.datetime.now(dt.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    text = f"""# Dependency Vulnerability Triage Summary

Generated at: {now}

## Status
No dependency-check JSON report was found at `{input_path}`.

## Likely causes
- Dependency scan did not run.
- Dependency scan failed before report generation.
- NVD API access/data refresh failed and no local fallback report was produced.

## Next action
- Configure `NVD_API_KEY` and re-run the scan in CI.
"""
    output_path.write_text(text, encoding="utf-8")


def generate_summary(report: dict[str, Any], max_findings: int) -> str:
    now = dt.datetime.now(dt.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")

    deps = report.get("dependencies", [])
    findings: list[dict[str, Any]] = []

    for dep in deps:
        file_name = dep.get("fileName", "unknown")
        package_path = dep.get("filePath", "unknown")
        vulnerabilities = dep.get("vulnerabilities") or []
        for vuln in vulnerabilities:
            severity = str(vuln.get("severity", "UNKNOWN")).upper()
            findings.append(
                {
                    "id": vuln.get("name", "UNKNOWN"),
                    "severity": severity,
                    "score": extract_score(vuln),
                    "source": vuln.get("source", "UNKNOWN"),
                    "file_name": file_name,
                    "file_path": package_path,
                }
            )

    severity_counts: dict[str, int] = {}
    for item in findings:
        key = item["severity"]
        severity_counts[key] = severity_counts.get(key, 0) + 1

    findings.sort(key=lambda x: (severity_rank(x["severity"]), x["score"]), reverse=True)

    lines: list[str] = []
    lines.append("# Dependency Vulnerability Triage Summary")
    lines.append("")
    lines.append(f"Generated at: {now}")
    lines.append("")
    lines.append("## Totals")
    lines.append(f"- Dependencies scanned: {len(deps)}")
    lines.append(f"- Vulnerability findings: {len(findings)}")

    if findings:
        ordered_severities = sorted(severity_counts.keys(), key=severity_rank, reverse=True)
        lines.append("- By severity:")
        for sev in ordered_severities:
            lines.append(f"  - {sev}: {severity_counts[sev]}")
    else:
        lines.append("- By severity: no findings")

    lines.append("")
    lines.append("## Detailed Findings")
    if not findings:
        lines.append("No vulnerability entries were present in the report.")
        return "\n".join(lines) + "\n"

    lines.append("")
    lines.append("| Vulnerability | Severity | Score | Source | Dependency |")
    lines.append("|---|---:|---:|---|---|")
    for item in findings[:max_findings]:
        score = f"{item['score']:.1f}" if item["score"] else "-"
        lines.append(
            f"| {item['id']} | {item['severity']} | {score} | {item['source']} | {item['file_name']} |"
        )

    if len(findings) > max_findings:
        lines.append("")
        lines.append(
            f"Only the first {max_findings} findings are shown. "
            "Use the full JSON report for complete triage."
        )

    lines.append("")
    lines.append("## Notes")
    lines.append("- Use the suppressions file only for confirmed false positives.")
    lines.append("- Record disposition decisions in the triage log document.")

    return "\n".join(lines) + "\n"


def main() -> int:
    args = parse_args()
    input_path = Path(args.input)
    output_path = Path(args.output)

    if not input_path.exists():
        write_missing_report(output_path, input_path)
        return 0

    try:
        report = json.loads(input_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as exc:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(
            "# Dependency Vulnerability Triage Summary\n\n"
            f"Failed to parse report `{input_path}`: {exc}\n",
            encoding="utf-8",
        )
        return 1

    summary = generate_summary(report, args.max_findings)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(summary, encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
