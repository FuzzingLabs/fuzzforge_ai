"""
Hadolint Infrastructure Security Module

This module uses Hadolint to scan Dockerfiles for security best practices
and potential vulnerabilities.
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
class HadolintModule(BaseModule):
    """Hadolint Dockerfile security scanning module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="hadolint",
            version="2.12.0",
            description="Dockerfile security linting and best practices validation",
            author="FuzzForge Team",
            category="infrastructure",
            tags=["dockerfile", "docker", "security", "best-practices", "linting"],
            input_schema={
                "type": "object",
                "properties": {
                    "severity": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["error", "warning", "info", "style"]},
                        "default": ["error", "warning", "info", "style"],
                        "description": "Minimum severity levels to report"
                    },
                    "ignored_rules": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Hadolint rules to ignore"
                    },
                    "trusted_registries": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of trusted Docker registries"
                    },
                    "allowed_maintainers": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of allowed maintainer emails"
                    },
                    "dockerfile_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["**/Dockerfile", "**/*.dockerfile", "**/Containerfile"],
                        "description": "Patterns to find Dockerfile-like files"
                    },
                    "strict": {
                        "type": "boolean",
                        "default": False,
                        "description": "Enable strict mode (fail on any issue)"
                    },
                    "no_fail": {
                        "type": "boolean",
                        "default": True,
                        "description": "Don't fail on lint errors (useful for reporting)"
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
                                "rule": {"type": "string"},
                                "severity": {"type": "string"},
                                "message": {"type": "string"},
                                "file_path": {"type": "string"},
                                "line": {"type": "integer"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        severity_levels = config.get("severity", ["error", "warning", "info", "style"])
        valid_severities = ["error", "warning", "info", "style"]

        for severity in severity_levels:
            if severity not in valid_severities:
                raise ValueError(f"Invalid severity level: {severity}. Valid: {valid_severities}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Hadolint Dockerfile security scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running Hadolint Dockerfile scan on {workspace}")

            # Find all Dockerfiles
            dockerfiles = self._find_dockerfiles(workspace, config)
            if not dockerfiles:
                logger.info("No Dockerfiles found for Hadolint analysis")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "files_scanned": 0}
                )

            logger.info(f"Found {len(dockerfiles)} Dockerfile(s) to analyze")

            # Process each Dockerfile
            all_findings = []
            for dockerfile in dockerfiles:
                findings = await self._scan_dockerfile(dockerfile, workspace, config)
                all_findings.extend(findings)

            # Create summary
            summary = self._create_summary(all_findings, len(dockerfiles))

            logger.info(f"Hadolint found {len(all_findings)} issues across {len(dockerfiles)} Dockerfiles")

            return self.create_result(
                findings=all_findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Hadolint module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _find_dockerfiles(self, workspace: Path, config: Dict[str, Any]) -> List[Path]:
        """Find Dockerfile-like files in workspace"""
        patterns = config.get("dockerfile_patterns", [
            "**/Dockerfile", "**/*.dockerfile", "**/Containerfile"
        ])

        # Debug logging
        logger.info(f"Hadolint searching in workspace: {workspace}")
        logger.info(f"Workspace exists: {workspace.exists()}")
        if workspace.exists():
            all_files = list(workspace.rglob("*"))
            logger.info(f"All files in workspace: {all_files}")

        dockerfiles = []
        for pattern in patterns:
            matches = list(workspace.glob(pattern))
            logger.info(f"Pattern '{pattern}' found: {matches}")
            dockerfiles.extend(matches)

        logger.info(f"Final dockerfiles list: {dockerfiles}")
        return list(set(dockerfiles))  # Remove duplicates

    async def _scan_dockerfile(self, dockerfile: Path, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Scan a single Dockerfile with Hadolint"""
        findings = []

        try:
            # Build hadolint command
            cmd = ["hadolint", "--format", "json"]

            # Add severity levels
            severity_levels = config.get("severity", ["error", "warning", "info", "style"])
            if "error" not in severity_levels:
                cmd.append("--no-error")
            if "warning" not in severity_levels:
                cmd.append("--no-warning")
            if "info" not in severity_levels:
                cmd.append("--no-info")
            if "style" not in severity_levels:
                cmd.append("--no-style")

            # Add ignored rules
            ignored_rules = config.get("ignored_rules", [])
            for rule in ignored_rules:
                cmd.extend(["--ignore", rule])

            # Add trusted registries
            trusted_registries = config.get("trusted_registries", [])
            for registry in trusted_registries:
                cmd.extend(["--trusted-registry", registry])

            # Add strict mode
            if config.get("strict", False):
                cmd.append("--strict-labels")

            # Add the dockerfile
            cmd.append(str(dockerfile))

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run hadolint
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            if process.returncode == 0 or config.get("no_fail", True):
                findings = self._parse_hadolint_output(
                    stdout.decode(), dockerfile, workspace
                )
            else:
                error_msg = stderr.decode()
                logger.warning(f"Hadolint failed for {dockerfile}: {error_msg}")
                # Continue with other files even if one fails

        except Exception as e:
            logger.warning(f"Error scanning {dockerfile}: {e}")

        return findings

    def _parse_hadolint_output(self, output: str, dockerfile: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Hadolint JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            # Hadolint outputs JSON array
            issues = json.loads(output)

            for issue in issues:
                # Extract information
                rule = issue.get("code", "unknown")
                message = issue.get("message", "")
                level = issue.get("level", "warning").lower()
                line = issue.get("line", 0)
                column = issue.get("column", 0)

                # Make file path relative to workspace
                try:
                    rel_path = dockerfile.relative_to(workspace)
                    file_path = str(rel_path)
                except ValueError:
                    file_path = str(dockerfile)

                # Map Hadolint level to our severity
                severity = self._map_severity(level)

                # Get category based on rule
                category = self._get_category(rule, message)

                # Create finding
                finding = self.create_finding(
                    title=f"Dockerfile issue: {rule}",
                    description=message or f"Hadolint rule {rule} violation",
                    severity=severity,
                    category=category,
                    file_path=file_path,
                    line_start=line if line > 0 else None,
                    recommendation=self._get_recommendation(rule, message),
                    metadata={
                        "rule": rule,
                        "hadolint_level": level,
                        "column": column,
                        "file": str(dockerfile)
                    }
                )

                findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Hadolint output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Hadolint results: {e}")

        return findings

    def _map_severity(self, hadolint_level: str) -> str:
        """Map Hadolint severity to our standard severity levels"""
        severity_map = {
            "error": "high",
            "warning": "medium",
            "info": "low",
            "style": "info"
        }
        return severity_map.get(hadolint_level.lower(), "medium")

    def _get_category(self, rule: str, message: str) -> str:
        """Determine finding category based on rule and message"""
        rule_lower = rule.lower()
        message_lower = message.lower()

        # Security-related categories
        if any(term in rule_lower for term in ["dl3", "dl4"]):
            if "user" in message_lower or "root" in message_lower:
                return "privilege_escalation"
            elif "secret" in message_lower or "password" in message_lower:
                return "secrets_management"
            elif "version" in message_lower or "pin" in message_lower:
                return "dependency_management"
            elif "add" in message_lower or "copy" in message_lower:
                return "file_operations"
            else:
                return "security_best_practices"
        elif any(term in rule_lower for term in ["dl1", "dl2"]):
            return "syntax_errors"
        elif "3001" in rule or "3002" in rule:
            return "user_management"
        elif "3008" in rule or "3009" in rule:
            return "privilege_escalation"
        elif "3014" in rule or "3015" in rule:
            return "port_management"
        elif "3020" in rule or "3021" in rule:
            return "copy_operations"
        else:
            return "dockerfile_best_practices"

    def _get_recommendation(self, rule: str, message: str) -> str:
        """Generate recommendation based on Hadolint rule"""
        recommendations = {
            # Security-focused recommendations
            "DL3002": "Create a non-root user and switch to it before running the application.",
            "DL3008": "Pin package versions to ensure reproducible builds and avoid supply chain attacks.",
            "DL3009": "Clean up package manager cache after installation to reduce image size and attack surface.",
            "DL3020": "Use COPY instead of ADD for local files to avoid unexpected behavior.",
            "DL3025": "Use JSON format for CMD and ENTRYPOINT to avoid shell injection vulnerabilities.",
            "DL3059": "Use multi-stage builds to reduce final image size and attack surface.",
            "DL4001": "Don't use sudo in Dockerfiles as it's unnecessary and can introduce vulnerabilities.",
            "DL4003": "Use a package manager instead of downloading and installing manually.",
            "DL4004": "Don't use SSH in Dockerfiles as it's a security risk.",
            "DL4005": "Use SHELL instruction to specify shell for RUN commands instead of hardcoding paths.",
        }

        if rule in recommendations:
            return recommendations[rule]

        # Generic recommendations based on patterns
        message_lower = message.lower()
        if "user" in message_lower and "root" in message_lower:
            return "Avoid running containers as root user. Create and use a non-privileged user."
        elif "version" in message_lower or "pin" in message_lower:
            return "Pin package versions to specific versions to ensure reproducible builds."
        elif "cache" in message_lower or "clean" in message_lower:
            return "Clean up package manager caches to reduce image size and potential security issues."
        elif "secret" in message_lower or "password" in message_lower:
            return "Don't include secrets in Dockerfiles. Use build arguments or runtime secrets instead."
        else:
            return f"Follow Dockerfile best practices to address rule {rule}."

    def _create_summary(self, findings: List[ModuleFinding], total_files: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        rule_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by rule
            rule = finding.metadata.get("rule", "unknown")
            rule_counts[rule] = rule_counts.get(rule, 0) + 1

        return {
            "total_findings": len(findings),
            "files_scanned": total_files,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_rules": dict(sorted(rule_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "files_with_issues": len(set(f.file_path for f in findings if f.file_path))
        }