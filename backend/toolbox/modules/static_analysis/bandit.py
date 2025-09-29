"""
Bandit Static Analysis Module

This module uses Bandit to detect security vulnerabilities in Python code.
"""
# Copyright (c) 2025 FuzzingLabs
#
# Licensed under the Business Source License 1.1 (BSL). See the LICENSE file
# at the root of this repository for details.
#
# After the Change Date (four years from publication), this version of the
# Licensed Work will be made available under the Apache License, Version 2.0.
# See the LICENSE-APACHE file or http://www.apache.org/licenses/LICENSE-2.0
#
# Additional attribution and requirements are provided in the NOTICE file.


import asyncio
import json
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class BanditModule(BaseModule):
    """Bandit Python security analysis module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="bandit",
            version="1.7.5",
            description="Python-specific security issue identifier using Bandit",
            author="FuzzForge Team",
            category="static_analysis",
            tags=["python", "sast", "security", "vulnerabilities"],
            input_schema={
                "type": "object",
                "properties": {
                    "confidence": {
                        "type": "string",
                        "enum": ["LOW", "MEDIUM", "HIGH"],
                        "default": "LOW",
                        "description": "Minimum confidence level for reported issues"
                    },
                    "severity": {
                        "type": "string",
                        "enum": ["LOW", "MEDIUM", "HIGH"],
                        "default": "LOW",
                        "description": "Minimum severity level for reported issues"
                    },
                    "tests": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific test IDs to run"
                    },
                    "skips": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Test IDs to skip"
                    },
                    "exclude_dirs": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["tests", "test", ".git", "__pycache__"],
                        "description": "Directories to exclude from analysis"
                    },
                    "include_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["*.py"],
                        "description": "File patterns to include"
                    },
                    "aggregate": {
                        "type": "string",
                        "enum": ["file", "vuln"],
                        "default": "file",
                        "description": "How to aggregate results"
                    },
                    "context_lines": {
                        "type": "integer",
                        "default": 3,
                        "description": "Number of context lines to show"
                    }
                }
            },
            output_schema={
                "type": "object",
                "properties": {
                    "findings": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "test_id": {"type": "string"},
                                "test_name": {"type": "string"},
                                "confidence": {"type": "string"},
                                "severity": {"type": "string"},
                                "file_path": {"type": "string"},
                                "line_number": {"type": "integer"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        confidence = config.get("confidence", "LOW")
        # Handle both string and list formats
        if isinstance(confidence, list):
            confidence = confidence[0] if confidence else "MEDIUM"
        if confidence not in ["LOW", "MEDIUM", "HIGH"]:
            raise ValueError("confidence must be LOW, MEDIUM, or HIGH")

        severity = config.get("severity", "LOW")
        # Handle both string and list formats
        if isinstance(severity, list):
            severity = severity[0] if severity else "MEDIUM"
        if severity not in ["LOW", "MEDIUM", "HIGH"]:
            raise ValueError("severity must be LOW, MEDIUM, or HIGH")

        context_lines = config.get("context_lines", 3)
        if not isinstance(context_lines, int) or context_lines < 0 or context_lines > 10:
            raise ValueError("context_lines must be between 0 and 10")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Bandit security analysis"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running Bandit analysis on {workspace}")

            # Check if there are any Python files
            python_files = list(workspace.rglob("*.py"))
            if not python_files:
                logger.info("No Python files found for Bandit analysis")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "files_scanned": 0}
                )

            # Build bandit command
            cmd = ["bandit", "-f", "json"]

            # Add confidence level
            confidence = config.get("confidence", "LOW")
            # Handle both string and list formats
            if isinstance(confidence, list):
                confidence = confidence[0] if confidence else "MEDIUM"
            cmd.extend(["--confidence-level", self._get_confidence_levels(confidence)])

            # Add severity level
            severity = config.get("severity", "LOW")
            # Handle both string and list formats
            if isinstance(severity, list):
                severity = severity[0] if severity else "MEDIUM"
            cmd.extend(["--severity-level", self._get_severity_levels(severity)])

            # Add tests to run
            if config.get("tests"):
                cmd.extend(["-t", ",".join(config["tests"])])

            # Add tests to skip
            if config.get("skips"):
                cmd.extend(["-s", ",".join(config["skips"])])

            # Add exclude directories
            exclude_dirs = config.get("exclude_dirs", ["tests", "test", ".git", "__pycache__"])
            if exclude_dirs:
                cmd.extend(["-x", ",".join(exclude_dirs)])

            # Add aggregate mode
            aggregate = config.get("aggregate", "file")
            cmd.extend(["-a", aggregate])

            # Add context lines
            context_lines = config.get("context_lines", 3)
            cmd.extend(["-n", str(context_lines)])

            # Add recursive flag and target
            cmd.extend(["-r", str(workspace)])

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run Bandit
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            findings = []
            if process.returncode in [0, 1]:  # 0 = no issues, 1 = issues found
                findings = self._parse_bandit_output(stdout.decode(), workspace)
            else:
                error_msg = stderr.decode()
                logger.error(f"Bandit failed: {error_msg}")
                return self.create_result(
                    findings=[],
                    status="failed",
                    error=f"Bandit execution failed: {error_msg}"
                )

            # Create summary
            summary = self._create_summary(findings, len(python_files))

            logger.info(f"Bandit found {len(findings)} security issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Bandit module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _get_confidence_levels(self, min_confidence: str) -> str:
        """Get minimum confidence level for Bandit"""
        return min_confidence.lower()

    def _get_severity_levels(self, min_severity: str) -> str:
        """Get minimum severity level for Bandit"""
        return min_severity.lower()

    def _parse_bandit_output(self, output: str, workspace: Path) -> List[ModuleFinding]:
        """Parse Bandit JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            data = json.loads(output)
            results = data.get("results", [])

            for result in results:
                # Extract information
                test_id = result.get("test_id", "unknown")
                test_name = result.get("test_name", "")
                issue_confidence = result.get("issue_confidence", "MEDIUM")
                issue_severity = result.get("issue_severity", "MEDIUM")
                issue_text = result.get("issue_text", "")

                # File location
                filename = result.get("filename", "")
                line_number = result.get("line_number", 0)
                line_range = result.get("line_range", [])

                # Code context
                code = result.get("code", "")

                # Make file path relative to workspace
                if filename:
                    try:
                        rel_path = Path(filename).relative_to(workspace)
                        filename = str(rel_path)
                    except ValueError:
                        pass

                # Map Bandit severity to our levels
                finding_severity = self._map_severity(issue_severity)

                # Determine category based on test_id
                category = self._get_category(test_id, test_name)

                # Create finding
                finding = self.create_finding(
                    title=f"Python security issue: {test_name}",
                    description=issue_text or f"Bandit test {test_id} detected a security issue",
                    severity=finding_severity,
                    category=category,
                    file_path=filename if filename else None,
                    line_start=line_number if line_number > 0 else None,
                    line_end=line_range[-1] if line_range and len(line_range) > 1 else None,
                    code_snippet=code.strip() if code else None,
                    recommendation=self._get_recommendation(test_id, test_name),
                    metadata={
                        "test_id": test_id,
                        "test_name": test_name,
                        "bandit_confidence": issue_confidence,
                        "bandit_severity": issue_severity,
                        "line_range": line_range,
                        "more_info": result.get("more_info", "")
                    }
                )

                findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Bandit output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Bandit results: {e}")

        return findings

    def _map_severity(self, bandit_severity: str) -> str:
        """Map Bandit severity to our standard severity levels"""
        severity_map = {
            "HIGH": "high",
            "MEDIUM": "medium",
            "LOW": "low"
        }
        return severity_map.get(bandit_severity.upper(), "medium")

    def _get_category(self, test_id: str, test_name: str) -> str:
        """Determine finding category based on Bandit test"""
        # Map common Bandit test categories
        if "sql" in test_id.lower() or "injection" in test_name.lower():
            return "injection"
        elif "crypto" in test_id.lower() or "hash" in test_name.lower():
            return "cryptography"
        elif "shell" in test_id.lower() or "subprocess" in test_name.lower():
            return "command_injection"
        elif "hardcode" in test_id.lower() or "password" in test_name.lower():
            return "hardcoded_secrets"
        elif "pickle" in test_id.lower() or "deserial" in test_name.lower():
            return "deserialization"
        elif "request" in test_id.lower() or "http" in test_name.lower():
            return "web_security"
        elif "random" in test_id.lower():
            return "weak_randomness"
        elif "path" in test_id.lower() or "traversal" in test_name.lower():
            return "path_traversal"
        else:
            return "python_security"

    def _get_recommendation(self, test_id: str, test_name: str) -> str:
        """Generate recommendation based on Bandit test"""
        recommendations = {
            # SQL Injection
            "B608": "Use parameterized queries instead of string formatting for SQL queries.",
            "B703": "Use parameterized queries with Django ORM or raw SQL.",

            # Cryptography
            "B101": "Remove hardcoded passwords and use secure configuration management.",
            "B105": "Remove hardcoded passwords and use environment variables or secret management.",
            "B106": "Remove hardcoded passwords from function arguments.",
            "B107": "Remove hardcoded passwords from default function arguments.",
            "B303": "Use cryptographically secure hash functions like SHA-256 or better.",
            "B324": "Use strong cryptographic algorithms instead of deprecated ones.",
            "B413": "Use secure encryption algorithms and proper key management.",

            # Command Injection
            "B602": "Validate and sanitize input before using in subprocess calls.",
            "B603": "Avoid using subprocess with shell=True. Use array form instead.",
            "B605": "Avoid starting processes with shell=True.",

            # Deserialization
            "B301": "Avoid using pickle for untrusted data. Use JSON or safer alternatives.",
            "B302": "Avoid using marshal for untrusted data.",
            "B506": "Use safe YAML loading methods like yaml.safe_load().",

            # Web Security
            "B501": "Validate SSL certificates in requests to prevent MITM attacks.",
            "B401": "Import and use telnetlib carefully, prefer SSH for remote connections.",

            # Random
            "B311": "Use cryptographically secure random generators like secrets module.",

            # Path Traversal
            "B108": "Validate file paths to prevent directory traversal attacks."
        }

        return recommendations.get(test_id,
            f"Review the {test_name} security issue and apply appropriate security measures.")

    def _create_summary(self, findings: List[ModuleFinding], total_files: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"high": 0, "medium": 0, "low": 0}
        category_counts = {}
        test_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by test
            test_id = finding.metadata.get("test_id", "unknown")
            test_counts[test_id] = test_counts.get(test_id, 0) + 1

        return {
            "total_findings": len(findings),
            "files_scanned": total_files,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_tests": dict(sorted(test_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "files_with_issues": len(set(f.file_path for f in findings if f.file_path))
        }