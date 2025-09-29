"""
Zizmor CI/CD Security Module

This module uses Zizmor to analyze GitHub Actions workflows for security
vulnerabilities and misconfigurations.
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
class ZizmorModule(BaseModule):
    """Zizmor GitHub Actions security analysis module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="zizmor",
            version="0.2.0",
            description="GitHub Actions workflow security analyzer for detecting vulnerabilities and misconfigurations",
            author="FuzzForge Team",
            category="cicd_security",
            tags=["github-actions", "cicd", "workflow", "security", "pipeline"],
            input_schema={
                "type": "object",
                "properties": {
                    "workflow_dir": {
                        "type": "string",
                        "default": ".github/workflows",
                        "description": "Directory containing GitHub Actions workflows"
                    },
                    "workflow_files": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific workflow files to analyze"
                    },
                    "format": {
                        "type": "string",
                        "enum": ["json", "sarif", "pretty"],
                        "default": "json",
                        "description": "Output format"
                    },
                    "verbose": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable verbose output"
                    },
                    "offline": {
                        "type": "boolean",
                        "default": false,
                        "description": "Run in offline mode (no internet lookups)"
                    },
                    "no_online_audits": {
                        "type": "boolean",
                        "default": true,
                        "description": "Disable online audits for faster execution"
                    },
                    "pedantic": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable pedantic mode (more strict checking)"
                    },
                    "rules": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific rules to run"
                    },
                    "ignore_rules": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Rules to ignore"
                    },
                    "min_severity": {
                        "type": "string",
                        "enum": ["unknown", "informational", "low", "medium", "high"],
                        "default": "low",
                        "description": "Minimum severity level to report"
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
                                "rule_id": {"type": "string"},
                                "rule_name": {"type": "string"},
                                "severity": {"type": "string"},
                                "workflow_file": {"type": "string"},
                                "line_number": {"type": "integer"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        workflow_dir = config.get("workflow_dir", ".github/workflows")
        workflow_files = config.get("workflow_files", [])

        if not workflow_dir and not workflow_files:
            raise ValueError("Either workflow_dir or workflow_files must be specified")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Zizmor GitHub Actions security analysis"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Zizmor GitHub Actions security analysis")

            # Check Zizmor installation
            await self._check_zizmor_installation()

            # Find workflow files
            workflow_files = self._find_workflow_files(workspace, config)
            if not workflow_files:
                logger.info("No GitHub Actions workflow files found")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "workflows_scanned": 0}
                )

            # Run Zizmor analysis
            findings = await self._run_zizmor_analysis(workflow_files, config, workspace)

            # Create summary
            summary = self._create_summary(findings, len(workflow_files))

            logger.info(f"Zizmor found {len(findings)} CI/CD security issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Zizmor module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_zizmor_installation(self):
        """Check if Zizmor is installed"""
        try:
            process = await asyncio.create_subprocess_exec(
                "zizmor", "--version",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                raise RuntimeError("Zizmor not found. Install with: cargo install zizmor")

        except FileNotFoundError:
            raise RuntimeError("Zizmor not found. Install with: cargo install zizmor")
        except Exception as e:
            raise RuntimeError(f"Zizmor installation check failed: {e}")

    def _find_workflow_files(self, workspace: Path, config: Dict[str, Any]) -> List[Path]:
        """Find GitHub Actions workflow files"""
        workflow_files = []

        # Check for specific files
        specific_files = config.get("workflow_files", [])
        for file_path in specific_files:
            full_path = workspace / file_path
            if full_path.exists():
                workflow_files.append(full_path)

        # Check workflow directory
        if not workflow_files:
            workflow_dir = workspace / config.get("workflow_dir", ".github/workflows")
            if workflow_dir.exists():
                # Find YAML files
                for pattern in ["*.yml", "*.yaml"]:
                    workflow_files.extend(workflow_dir.glob(pattern))

        return list(set(workflow_files))  # Remove duplicates

    async def _run_zizmor_analysis(self, workflow_files: List[Path], config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Zizmor analysis on workflow files"""
        findings = []

        try:
            for workflow_file in workflow_files:
                file_findings = await self._analyze_workflow_file(workflow_file, config, workspace)
                findings.extend(file_findings)

        except Exception as e:
            logger.warning(f"Error running Zizmor analysis: {e}")

        return findings

    async def _analyze_workflow_file(self, workflow_file: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Analyze a single workflow file with Zizmor"""
        findings = []

        try:
            # Build Zizmor command
            cmd = ["zizmor"]

            # Add format
            format_type = config.get("format", "json")
            cmd.extend(["--format", format_type])

            # Add minimum severity
            min_severity = config.get("min_severity", "low")
            cmd.extend(["--min-severity", min_severity])

            # Add flags
            if config.get("verbose", False):
                cmd.append("--verbose")

            if config.get("offline", False):
                cmd.append("--offline")

            if config.get("no_online_audits", True):
                cmd.append("--no-online-audits")

            if config.get("pedantic", False):
                cmd.append("--pedantic")

            # Add specific rules
            rules = config.get("rules", [])
            for rule in rules:
                cmd.extend(["--rules", rule])

            # Add ignore rules
            ignore_rules = config.get("ignore_rules", [])
            for rule in ignore_rules:
                cmd.extend(["--ignore", rule])

            # Add workflow file
            cmd.append(str(workflow_file))

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run Zizmor
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results (even if return code is non-zero, as it may contain findings)
            if stdout.strip():
                findings = self._parse_zizmor_output(
                    stdout.decode(), workflow_file, workspace, format_type
                )
            elif stderr.strip():
                logger.warning(f"Zizmor analysis failed for {workflow_file}: {stderr.decode()}")

        except Exception as e:
            logger.warning(f"Error analyzing workflow file {workflow_file}: {e}")

        return findings

    def _parse_zizmor_output(self, output: str, workflow_file: Path, workspace: Path, format_type: str) -> List[ModuleFinding]:
        """Parse Zizmor output into findings"""
        findings = []

        try:
            if format_type == "json":
                findings = self._parse_json_output(output, workflow_file, workspace)
            elif format_type == "sarif":
                findings = self._parse_sarif_output(output, workflow_file, workspace)
            else:
                findings = self._parse_text_output(output, workflow_file, workspace)

        except Exception as e:
            logger.warning(f"Error parsing Zizmor output: {e}")

        return findings

    def _parse_json_output(self, output: str, workflow_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Zizmor JSON output"""
        findings = []

        try:
            if not output.strip():
                return findings

            data = json.loads(output)

            # Handle different JSON structures
            if isinstance(data, dict):
                # Single result
                findings.extend(self._process_zizmor_result(data, workflow_file, workspace))
            elif isinstance(data, list):
                # Multiple results
                for result in data:
                    findings.extend(self._process_zizmor_result(result, workflow_file, workspace))

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Zizmor JSON output: {e}")

        return findings

    def _parse_sarif_output(self, output: str, workflow_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Zizmor SARIF output"""
        findings = []

        try:
            data = json.loads(output)
            runs = data.get("runs", [])

            for run in runs:
                results = run.get("results", [])
                for result in results:
                    finding = self._create_sarif_finding(result, workflow_file, workspace)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing SARIF output: {e}")

        return findings

    def _parse_text_output(self, output: str, workflow_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Zizmor text output"""
        findings = []

        try:
            lines = output.strip().split('\n')
            for line in lines:
                if line.strip() and not line.startswith('#'):
                    # Create basic finding from text line
                    finding = self._create_text_finding(line, workflow_file, workspace)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing text output: {e}")

        return findings

    def _process_zizmor_result(self, result: Dict[str, Any], workflow_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Process a single Zizmor result"""
        findings = []

        try:
            # Extract rule information
            rule_id = result.get("rule", {}).get("id", "unknown")
            rule_name = result.get("rule", {}).get("desc", rule_id)
            severity = result.get("severity", "medium")
            message = result.get("message", "")

            # Extract location information
            locations = result.get("locations", [])
            if not locations:
                # Create finding without specific location
                finding = self._create_zizmor_finding(
                    rule_id, rule_name, severity, message, workflow_file, workspace
                )
                if finding:
                    findings.append(finding)
            else:
                # Create finding for each location
                for location in locations:
                    line_number = location.get("line", 0)
                    column = location.get("column", 0)

                    finding = self._create_zizmor_finding(
                        rule_id, rule_name, severity, message, workflow_file, workspace,
                        line_number, column
                    )
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error processing Zizmor result: {e}")

        return findings

    def _create_zizmor_finding(self, rule_id: str, rule_name: str, severity: str, message: str,
                              workflow_file: Path, workspace: Path, line_number: int = None, column: int = None) -> ModuleFinding:
        """Create finding from Zizmor analysis"""
        try:
            # Map Zizmor severity to our standard levels
            finding_severity = self._map_severity(severity)

            # Create relative path
            try:
                rel_path = workflow_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(workflow_file)

            # Get category and recommendation
            category = self._get_cicd_category(rule_id, rule_name)
            recommendation = self._get_cicd_recommendation(rule_id, rule_name, message)

            finding = self.create_finding(
                title=f"CI/CD Security Issue: {rule_name}",
                description=message or f"Zizmor detected a security issue: {rule_name}",
                severity=finding_severity,
                category=category,
                file_path=file_path,
                line_start=line_number if line_number else None,
                recommendation=recommendation,
                metadata={
                    "rule_id": rule_id,
                    "rule_name": rule_name,
                    "zizmor_severity": severity,
                    "workflow_file": str(workflow_file.name),
                    "line_number": line_number,
                    "column": column,
                    "tool": "zizmor"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating Zizmor finding: {e}")
            return None

    def _create_sarif_finding(self, result: Dict[str, Any], workflow_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from SARIF result"""
        try:
            rule_id = result.get("ruleId", "unknown")
            message = result.get("message", {}).get("text", "")
            severity = result.get("level", "warning")

            # Extract location
            locations = result.get("locations", [])
            line_number = None
            if locations:
                physical_location = locations[0].get("physicalLocation", {})
                region = physical_location.get("region", {})
                line_number = region.get("startLine")

            return self._create_zizmor_finding(
                rule_id, rule_id, severity, message, workflow_file, workspace, line_number
            )

        except Exception as e:
            logger.warning(f"Error creating SARIF finding: {e}")
            return None

    def _create_text_finding(self, line: str, workflow_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from text line"""
        try:
            try:
                rel_path = workflow_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(workflow_file)

            finding = self.create_finding(
                title="CI/CD Security Issue",
                description=line.strip(),
                severity="medium",
                category="workflow_security",
                file_path=file_path,
                recommendation="Review and address the workflow security issue identified by Zizmor.",
                metadata={
                    "detection_line": line.strip(),
                    "workflow_file": str(workflow_file.name),
                    "tool": "zizmor"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating text finding: {e}")
            return None

    def _map_severity(self, zizmor_severity: str) -> str:
        """Map Zizmor severity to our standard levels"""
        severity_map = {
            "high": "high",
            "medium": "medium",
            "low": "low",
            "informational": "info",
            "unknown": "low",
            "error": "high",
            "warning": "medium",
            "note": "low"
        }
        return severity_map.get(zizmor_severity.lower(), "medium")

    def _get_cicd_category(self, rule_id: str, rule_name: str) -> str:
        """Get category for CI/CD security issue"""
        rule_lower = f"{rule_id} {rule_name}".lower()

        if any(term in rule_lower for term in ["secret", "token", "credential", "password"]):
            return "secret_exposure"
        elif any(term in rule_lower for term in ["permission", "access", "privilege"]):
            return "permission_escalation"
        elif any(term in rule_lower for term in ["injection", "command", "script"]):
            return "code_injection"
        elif any(term in rule_lower for term in ["artifact", "cache", "upload"]):
            return "artifact_security"
        elif any(term in rule_lower for term in ["environment", "env", "variable"]):
            return "environment_security"
        elif any(term in rule_lower for term in ["network", "external", "download"]):
            return "network_security"
        else:
            return "workflow_security"

    def _get_cicd_recommendation(self, rule_id: str, rule_name: str, message: str) -> str:
        """Get recommendation for CI/CD security issue"""
        rule_lower = f"{rule_id} {rule_name}".lower()

        if "secret" in rule_lower or "token" in rule_lower:
            return "Store secrets securely using GitHub Secrets or environment variables. Never hardcode credentials in workflow files."
        elif "permission" in rule_lower:
            return "Follow the principle of least privilege. Grant only necessary permissions and use specific permission scopes."
        elif "injection" in rule_lower:
            return "Avoid using user input directly in shell commands. Use proper escaping, validation, or structured approaches."
        elif "artifact" in rule_lower:
            return "Secure artifact handling by validating checksums, using signed artifacts, and restricting artifact access."
        elif "environment" in rule_lower:
            return "Protect environment variables and avoid exposing sensitive information in logs or outputs."
        elif "network" in rule_lower:
            return "Use HTTPS for external connections, validate certificates, and avoid downloading from untrusted sources."
        elif message:
            return f"Address the identified issue: {message}"
        else:
            return f"Review and fix the workflow security issue: {rule_name}"

    def _create_summary(self, findings: List[ModuleFinding], workflows_count: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        rule_counts = {}
        workflow_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by rule
            rule_id = finding.metadata.get("rule_id", "unknown")
            rule_counts[rule_id] = rule_counts.get(rule_id, 0) + 1

            # Count by workflow
            workflow = finding.metadata.get("workflow_file", "unknown")
            workflow_counts[workflow] = workflow_counts.get(workflow, 0) + 1

        return {
            "total_findings": len(findings),
            "workflows_scanned": workflows_count,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_rules": dict(sorted(rule_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "workflows_with_issues": len(workflow_counts),
            "workflow_issue_counts": dict(sorted(workflow_counts.items(), key=lambda x: x[1], reverse=True)[:10])
        }