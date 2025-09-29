"""
Nuclei Penetration Testing Module

This module uses Nuclei to perform fast and customizable vulnerability scanning
using community-powered templates.
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
class NucleiModule(BaseModule):
    """Nuclei fast vulnerability scanner module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="nuclei",
            version="3.1.0",
            description="Fast and customizable vulnerability scanner using community-powered templates",
            author="FuzzForge Team",
            category="penetration_testing",
            tags=["vulnerability", "scanner", "web", "network", "templates"],
            input_schema={
                "type": "object",
                "properties": {
                    "targets": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of targets (URLs, domains, IP addresses)"
                    },
                    "target_file": {
                        "type": "string",
                        "description": "File containing targets to scan"
                    },
                    "templates": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific templates to use"
                    },
                    "template_directory": {
                        "type": "string",
                        "description": "Directory containing custom templates"
                    },
                    "tags": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Template tags to include"
                    },
                    "exclude_tags": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Template tags to exclude"
                    },
                    "severity": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["critical", "high", "medium", "low", "info"]},
                        "default": ["critical", "high", "medium"],
                        "description": "Severity levels to include"
                    },
                    "concurrency": {
                        "type": "integer",
                        "default": 25,
                        "description": "Number of concurrent threads"
                    },
                    "rate_limit": {
                        "type": "integer",
                        "default": 150,
                        "description": "Rate limit (requests per second)"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 10,
                        "description": "Timeout for requests (seconds)"
                    },
                    "retries": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of retries for failed requests"
                    },
                    "update_templates": {
                        "type": "boolean",
                        "default": False,
                        "description": "Update templates before scanning"
                    },
                    "disable_clustering": {
                        "type": "boolean",
                        "default": False,
                        "description": "Disable template clustering"
                    },
                    "no_interactsh": {
                        "type": "boolean",
                        "default": True,
                        "description": "Disable interactsh server for OAST testing"
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
                                "template_id": {"type": "string"},
                                "name": {"type": "string"},
                                "severity": {"type": "string"},
                                "host": {"type": "string"},
                                "matched_at": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        targets = config.get("targets", [])
        target_file = config.get("target_file")

        if not targets and not target_file:
            raise ValueError("Either 'targets' or 'target_file' must be specified")

        severity_levels = config.get("severity", [])
        valid_severities = ["critical", "high", "medium", "low", "info"]
        for severity in severity_levels:
            if severity not in valid_severities:
                raise ValueError(f"Invalid severity: {severity}. Valid: {valid_severities}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Nuclei vulnerability scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Nuclei vulnerability scan")

            # Update templates if requested
            if config.get("update_templates", False):
                await self._update_templates(workspace)

            # Prepare target file
            target_file = await self._prepare_targets(config, workspace)
            if not target_file:
                logger.info("No targets specified for scanning")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "targets_scanned": 0}
                )

            # Run Nuclei scan
            findings = await self._run_nuclei_scan(target_file, config, workspace)

            # Create summary
            summary = self._create_summary(findings, len(config.get("targets", [])))

            logger.info(f"Nuclei found {len(findings)} vulnerabilities")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Nuclei module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _update_templates(self, workspace: Path):
        """Update Nuclei templates"""
        try:
            logger.info("Updating Nuclei templates...")
            cmd = ["nuclei", "-update-templates"]

            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            if process.returncode == 0:
                logger.info("Templates updated successfully")
            else:
                logger.warning(f"Template update failed: {stderr.decode()}")

        except Exception as e:
            logger.warning(f"Error updating templates: {e}")

    async def _prepare_targets(self, config: Dict[str, Any], workspace: Path) -> Path:
        """Prepare target file for scanning"""
        targets = config.get("targets", [])
        target_file = config.get("target_file")

        if target_file:
            # Use existing target file
            target_path = workspace / target_file
            if target_path.exists():
                return target_path
            else:
                raise FileNotFoundError(f"Target file not found: {target_file}")

        if targets:
            # Create temporary target file
            target_path = workspace / "nuclei_targets.txt"
            with open(target_path, 'w') as f:
                for target in targets:
                    f.write(f"{target}\n")
            return target_path

        return None

    async def _run_nuclei_scan(self, target_file: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Nuclei scan"""
        findings = []

        try:
            # Build nuclei command
            cmd = ["nuclei", "-l", str(target_file)]

            # Add output format
            cmd.extend(["-json"])

            # Add templates
            templates = config.get("templates", [])
            if templates:
                cmd.extend(["-t", ",".join(templates)])

            # Add template directory
            template_dir = config.get("template_directory")
            if template_dir:
                cmd.extend(["-t", template_dir])

            # Add tags
            tags = config.get("tags", [])
            if tags:
                cmd.extend(["-tags", ",".join(tags)])

            # Add exclude tags
            exclude_tags = config.get("exclude_tags", [])
            if exclude_tags:
                cmd.extend(["-exclude-tags", ",".join(exclude_tags)])

            # Add severity
            severity_levels = config.get("severity", ["critical", "high", "medium"])
            cmd.extend(["-severity", ",".join(severity_levels)])

            # Add concurrency
            concurrency = config.get("concurrency", 25)
            cmd.extend(["-c", str(concurrency)])

            # Add rate limit
            rate_limit = config.get("rate_limit", 150)
            cmd.extend(["-rl", str(rate_limit)])

            # Add timeout
            timeout = config.get("timeout", 10)
            cmd.extend(["-timeout", str(timeout)])

            # Add retries
            retries = config.get("retries", 1)
            cmd.extend(["-retries", str(retries)])

            # Add other flags
            if config.get("disable_clustering", False):
                cmd.append("-no-color")

            if config.get("no_interactsh", True):
                cmd.append("-no-interactsh")

            # Add silent flag for JSON output
            cmd.append("-silent")

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run nuclei
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            if process.returncode == 0 or stdout:
                findings = self._parse_nuclei_output(stdout.decode(), workspace)
            else:
                error_msg = stderr.decode()
                logger.error(f"Nuclei scan failed: {error_msg}")

        except Exception as e:
            logger.warning(f"Error running Nuclei scan: {e}")

        return findings

    def _parse_nuclei_output(self, output: str, workspace: Path) -> List[ModuleFinding]:
        """Parse Nuclei JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            # Parse each line as JSON (JSONL format)
            for line in output.strip().split('\n'):
                if not line.strip():
                    continue

                result = json.loads(line)

                # Extract information
                template_id = result.get("template-id", "")
                template_name = result.get("info", {}).get("name", "")
                severity = result.get("info", {}).get("severity", "medium")
                host = result.get("host", "")
                matched_at = result.get("matched-at", "")
                description = result.get("info", {}).get("description", "")
                reference = result.get("info", {}).get("reference", [])
                classification = result.get("info", {}).get("classification", {})
                extracted_results = result.get("extracted-results", [])

                # Map severity to our standard levels
                finding_severity = self._map_severity(severity)

                # Get category based on template
                category = self._get_category(template_id, template_name, classification)

                # Create finding
                finding = self.create_finding(
                    title=f"Nuclei Detection: {template_name}",
                    description=description or f"Vulnerability detected using template {template_id}",
                    severity=finding_severity,
                    category=category,
                    file_path=None,  # Nuclei scans network targets
                    recommendation=self._get_recommendation(template_id, template_name, reference),
                    metadata={
                        "template_id": template_id,
                        "template_name": template_name,
                        "nuclei_severity": severity,
                        "host": host,
                        "matched_at": matched_at,
                        "classification": classification,
                        "reference": reference,
                        "extracted_results": extracted_results
                    }
                )
                findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Nuclei output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Nuclei results: {e}")

        return findings

    def _map_severity(self, nuclei_severity: str) -> str:
        """Map Nuclei severity to our standard severity levels"""
        severity_map = {
            "critical": "critical",
            "high": "high",
            "medium": "medium",
            "low": "low",
            "info": "info"
        }
        return severity_map.get(nuclei_severity.lower(), "medium")

    def _get_category(self, template_id: str, template_name: str, classification: Dict) -> str:
        """Determine finding category based on template and classification"""
        template_lower = f"{template_id} {template_name}".lower()

        # Use classification if available
        cwe_id = classification.get("cwe-id")
        if cwe_id:
            # Map common CWE IDs to categories
            if cwe_id in ["CWE-79", "CWE-80"]:
                return "cross_site_scripting"
            elif cwe_id in ["CWE-89"]:
                return "sql_injection"
            elif cwe_id in ["CWE-22", "CWE-23"]:
                return "path_traversal"
            elif cwe_id in ["CWE-352"]:
                return "csrf"
            elif cwe_id in ["CWE-601"]:
                return "redirect"

        # Analyze template content
        if any(term in template_lower for term in ["xss", "cross-site"]):
            return "cross_site_scripting"
        elif any(term in template_lower for term in ["sql", "injection"]):
            return "sql_injection"
        elif any(term in template_lower for term in ["lfi", "rfi", "file", "path", "traversal"]):
            return "file_inclusion"
        elif any(term in template_lower for term in ["rce", "command", "execution"]):
            return "remote_code_execution"
        elif any(term in template_lower for term in ["auth", "login", "bypass"]):
            return "authentication_bypass"
        elif any(term in template_lower for term in ["disclosure", "exposure", "leak"]):
            return "information_disclosure"
        elif any(term in template_lower for term in ["config", "misconfiguration"]):
            return "misconfiguration"
        elif any(term in template_lower for term in ["cve-"]):
            return "known_vulnerability"
        else:
            return "web_vulnerability"

    def _get_recommendation(self, template_id: str, template_name: str, references: List) -> str:
        """Generate recommendation based on template"""
        # Use references if available
        if references:
            ref_text = ", ".join(references[:3])  # Limit to first 3 references
            return f"Review the vulnerability and apply appropriate fixes. References: {ref_text}"

        # Generate based on template type
        template_lower = f"{template_id} {template_name}".lower()

        if "xss" in template_lower:
            return "Implement proper input validation and output encoding to prevent XSS attacks."
        elif "sql" in template_lower:
            return "Use parameterized queries and input validation to prevent SQL injection."
        elif "lfi" in template_lower or "rfi" in template_lower:
            return "Validate and sanitize file paths. Avoid dynamic file includes with user input."
        elif "rce" in template_lower:
            return "Sanitize user input and avoid executing system commands with user-controlled data."
        elif "auth" in template_lower:
            return "Review authentication mechanisms and implement proper access controls."
        elif "exposure" in template_lower or "disclosure" in template_lower:
            return "Restrict access to sensitive information and implement proper authorization."
        elif "cve-" in template_lower:
            return "Update the affected software to the latest version to patch known vulnerabilities."
        else:
            return f"Review and remediate the security issue identified by template {template_id}."

    def _create_summary(self, findings: List[ModuleFinding], targets_count: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        template_counts = {}
        host_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by template
            template_id = finding.metadata.get("template_id", "unknown")
            template_counts[template_id] = template_counts.get(template_id, 0) + 1

            # Count by host
            host = finding.metadata.get("host", "unknown")
            host_counts[host] = host_counts.get(host, 0) + 1

        return {
            "total_findings": len(findings),
            "targets_scanned": targets_count,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_templates": dict(sorted(template_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "affected_hosts": len(host_counts),
            "host_counts": dict(sorted(host_counts.items(), key=lambda x: x[1], reverse=True)[:10])
        }