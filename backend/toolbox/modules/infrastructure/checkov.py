"""
Checkov Infrastructure Security Module

This module uses Checkov to scan Infrastructure as Code (IaC) files for
security misconfigurations and compliance violations.
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
class CheckovModule(BaseModule):
    """Checkov Infrastructure as Code security scanning module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="checkov",
            version="3.1.34",
            description="Infrastructure as Code security scanning for Terraform, CloudFormation, Kubernetes, and more",
            author="FuzzForge Team",
            category="infrastructure",
            tags=["iac", "terraform", "cloudformation", "kubernetes", "security", "compliance"],
            input_schema={
                "type": "object",
                "properties": {
                    "frameworks": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["terraform", "cloudformation", "kubernetes"],
                        "description": "IaC frameworks to scan"
                    },
                    "checks": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific checks to run"
                    },
                    "skip_checks": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Checks to skip"
                    },
                    "severity": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"]},
                        "default": ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"],
                        "description": "Minimum severity levels to report"
                    },
                    "compact": {
                        "type": "boolean",
                        "default": False,
                        "description": "Use compact output format"
                    },
                    "quiet": {
                        "type": "boolean",
                        "default": False,
                        "description": "Suppress verbose output"
                    },
                    "soft_fail": {
                        "type": "boolean",
                        "default": True,
                        "description": "Return exit code 0 even when issues are found"
                    },
                    "include_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "File patterns to include"
                    },
                    "exclude_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "File patterns to exclude"
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
                                "check_id": {"type": "string"},
                                "check_name": {"type": "string"},
                                "severity": {"type": "string"},
                                "file_path": {"type": "string"},
                                "line_range": {"type": "array"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        frameworks = config.get("frameworks", [])
        supported_frameworks = [
            "terraform", "cloudformation", "kubernetes", "dockerfile",
            "ansible", "helm", "serverless", "bicep", "github_actions"
        ]

        for framework in frameworks:
            if framework not in supported_frameworks:
                raise ValueError(f"Unsupported framework: {framework}. Supported: {supported_frameworks}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Checkov IaC security scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running Checkov IaC scan on {workspace}")

            # Check if there are any IaC files
            iac_files = self._find_iac_files(workspace, config.get("frameworks", []))
            if not iac_files:
                logger.info("No Infrastructure as Code files found")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "files_scanned": 0}
                )

            # Build checkov command
            cmd = ["checkov", "-d", str(workspace)]

            # Add output format
            cmd.extend(["--output", "json"])

            # Add frameworks
            frameworks = config.get("frameworks", ["terraform", "cloudformation", "kubernetes"])
            cmd.extend(["--framework"] + frameworks)

            # Add specific checks
            if config.get("checks"):
                cmd.extend(["--check", ",".join(config["checks"])])

            # Add skip checks
            if config.get("skip_checks"):
                cmd.extend(["--skip-check", ",".join(config["skip_checks"])])

            # Add compact flag
            if config.get("compact", False):
                cmd.append("--compact")

            # Add quiet flag
            if config.get("quiet", False):
                cmd.append("--quiet")

            # Add soft fail
            if config.get("soft_fail", True):
                cmd.append("--soft-fail")

            # Add include patterns
            if config.get("include_patterns"):
                for pattern in config["include_patterns"]:
                    cmd.extend(["--include", pattern])

            # Add exclude patterns
            if config.get("exclude_patterns"):
                for pattern in config["exclude_patterns"]:
                    cmd.extend(["--exclude", pattern])

            # Disable update checks and telemetry
            cmd.extend(["--no-guide", "--skip-download"])

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run Checkov
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            findings = []
            if process.returncode == 0 or config.get("soft_fail", True):
                findings = self._parse_checkov_output(stdout.decode(), workspace, config)
            else:
                error_msg = stderr.decode()
                logger.error(f"Checkov failed: {error_msg}")
                return self.create_result(
                    findings=[],
                    status="failed",
                    error=f"Checkov execution failed: {error_msg}"
                )

            # Create summary
            summary = self._create_summary(findings, len(iac_files))

            logger.info(f"Checkov found {len(findings)} security issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Checkov module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _find_iac_files(self, workspace: Path, frameworks: List[str]) -> List[Path]:
        """Find Infrastructure as Code files in workspace"""
        iac_patterns = {
            "terraform": ["*.tf", "*.tfvars"],
            "cloudformation": ["*.yaml", "*.yml", "*.json", "*template*"],
            "kubernetes": ["*.yaml", "*.yml"],
            "dockerfile": ["Dockerfile", "*.dockerfile"],
            "ansible": ["*.yaml", "*.yml", "playbook*"],
            "helm": ["Chart.yaml", "values.yaml", "*.yaml"],
            "bicep": ["*.bicep"],
            "github_actions": [".github/workflows/*.yaml", ".github/workflows/*.yml"]
        }

        found_files = []
        for framework in frameworks:
            patterns = iac_patterns.get(framework, [])
            for pattern in patterns:
                found_files.extend(workspace.rglob(pattern))

        return list(set(found_files))  # Remove duplicates

    def _parse_checkov_output(self, output: str, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse Checkov JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            data = json.loads(output)

            # Get severity filter
            allowed_severities = set(s.upper() for s in config.get("severity", ["CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"]))

            # Process failed checks
            failed_checks = data.get("results", {}).get("failed_checks", [])

            for check in failed_checks:
                # Extract information
                check_id = check.get("check_id", "unknown")
                check_name = check.get("check_name", "")
                severity = check.get("severity", "MEDIUM").upper()
                file_path = check.get("file_path", "")
                file_line_range = check.get("file_line_range", [])
                resource = check.get("resource", "")
                description = check.get("description", "")
                guideline = check.get("guideline", "")

                # Apply severity filter
                if severity not in allowed_severities:
                    continue

                # Make file path relative to workspace
                if file_path:
                    try:
                        rel_path = Path(file_path).relative_to(workspace)
                        file_path = str(rel_path)
                    except ValueError:
                        pass

                # Map severity to our standard levels
                finding_severity = self._map_severity(severity)

                # Create finding
                finding = self.create_finding(
                    title=f"IaC Security Issue: {check_name}",
                    description=description or f"Checkov check {check_id} failed for resource {resource}",
                    severity=finding_severity,
                    category=self._get_category(check_id, check_name),
                    file_path=file_path if file_path else None,
                    line_start=file_line_range[0] if file_line_range and len(file_line_range) > 0 else None,
                    line_end=file_line_range[1] if file_line_range and len(file_line_range) > 1 else None,
                    recommendation=self._get_recommendation(check_id, check_name, guideline),
                    metadata={
                        "check_id": check_id,
                        "check_name": check_name,
                        "checkov_severity": severity,
                        "resource": resource,
                        "guideline": guideline,
                        "bc_category": check.get("bc_category", ""),
                        "benchmarks": check.get("benchmarks", {}),
                        "fixed_definition": check.get("fixed_definition", "")
                    }
                )

                findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Checkov output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Checkov results: {e}")

        return findings

    def _map_severity(self, checkov_severity: str) -> str:
        """Map Checkov severity to our standard severity levels"""
        severity_map = {
            "CRITICAL": "critical",
            "HIGH": "high",
            "MEDIUM": "medium",
            "LOW": "low",
            "INFO": "info"
        }
        return severity_map.get(checkov_severity.upper(), "medium")

    def _get_category(self, check_id: str, check_name: str) -> str:
        """Determine finding category based on check"""
        check_lower = f"{check_id} {check_name}".lower()

        if any(term in check_lower for term in ["encryption", "encrypt", "kms", "ssl", "tls"]):
            return "encryption"
        elif any(term in check_lower for term in ["access", "iam", "rbac", "permission"]):
            return "access_control"
        elif any(term in check_lower for term in ["network", "security group", "firewall", "vpc"]):
            return "network_security"
        elif any(term in check_lower for term in ["logging", "monitor", "audit"]):
            return "logging_monitoring"
        elif any(term in check_lower for term in ["storage", "s3", "bucket", "database"]):
            return "data_protection"
        elif any(term in check_lower for term in ["secret", "password", "key", "credential"]):
            return "secrets_management"
        elif any(term in check_lower for term in ["backup", "snapshot", "versioning"]):
            return "backup_recovery"
        else:
            return "infrastructure_security"

    def _get_recommendation(self, check_id: str, check_name: str, guideline: str) -> str:
        """Generate recommendation based on check"""
        if guideline:
            return f"Follow the guideline: {guideline}"

        # Generic recommendations based on common patterns
        check_lower = f"{check_id} {check_name}".lower()

        if "encryption" in check_lower:
            return "Enable encryption for sensitive data at rest and in transit using appropriate encryption algorithms."
        elif "access" in check_lower or "iam" in check_lower:
            return "Review and tighten access controls. Follow the principle of least privilege."
        elif "network" in check_lower or "security group" in check_lower:
            return "Restrict network access to only necessary ports and IP ranges."
        elif "logging" in check_lower:
            return "Enable comprehensive logging and monitoring for security events."
        elif "backup" in check_lower:
            return "Implement proper backup and disaster recovery procedures."
        else:
            return f"Review and fix the security configuration issue identified by check {check_id}."

    def _create_summary(self, findings: List[ModuleFinding], total_files: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        check_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by check
            check_id = finding.metadata.get("check_id", "unknown")
            check_counts[check_id] = check_counts.get(check_id, 0) + 1

        return {
            "total_findings": len(findings),
            "files_scanned": total_files,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_checks": dict(sorted(check_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "files_with_issues": len(set(f.file_path for f in findings if f.file_path))
        }