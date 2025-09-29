"""
Kubesec Infrastructure Security Module

This module uses Kubesec to scan Kubernetes manifests for security
misconfigurations and best practices violations.
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
class KubesecModule(BaseModule):
    """Kubesec Kubernetes security scanning module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="kubesec",
            version="2.14.0",
            description="Kubernetes security scanning for YAML/JSON manifests with security best practices validation",
            author="FuzzForge Team",
            category="infrastructure",
            tags=["kubernetes", "k8s", "security", "best-practices", "manifests"],
            input_schema={
                "type": "object",
                "properties": {
                    "scan_mode": {
                        "type": "string",
                        "enum": ["scan", "http"],
                        "default": "scan",
                        "description": "Kubesec scan mode (local scan or HTTP API)"
                    },
                    "threshold": {
                        "type": "integer",
                        "default": 15,
                        "description": "Minimum security score threshold"
                    },
                    "exit_code": {
                        "type": "integer",
                        "default": 0,
                        "description": "Exit code to return on failure"
                    },
                    "format": {
                        "type": "string",
                        "enum": ["json", "template"],
                        "default": "json",
                        "description": "Output format"
                    },
                    "kubernetes_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["**/*.yaml", "**/*.yml", "**/k8s/*.yaml", "**/kubernetes/*.yaml"],
                        "description": "Patterns to find Kubernetes manifest files"
                    },
                    "exclude_patterns": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Patterns to exclude from scanning"
                    },
                    "strict": {
                        "type": "boolean",
                        "default": False,
                        "description": "Enable strict mode (fail on any security issue)"
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
                                "score": {"type": "integer"},
                                "security_issues": {"type": "array"},
                                "file_path": {"type": "string"},
                                "manifest_kind": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        scan_mode = config.get("scan_mode", "scan")
        if scan_mode not in ["scan", "http"]:
            raise ValueError(f"Invalid scan mode: {scan_mode}. Valid: ['scan', 'http']")

        threshold = config.get("threshold", 0)
        if not isinstance(threshold, int):
            raise ValueError(f"Threshold must be an integer, got: {type(threshold)}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Kubesec Kubernetes security scanning"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running Kubesec Kubernetes scan on {workspace}")

            # Find all Kubernetes manifests
            k8s_files = self._find_kubernetes_files(workspace, config)
            if not k8s_files:
                logger.info("No Kubernetes manifest files found")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "files_scanned": 0}
                )

            logger.info(f"Found {len(k8s_files)} Kubernetes manifest file(s) to analyze")

            # Process each manifest file
            all_findings = []
            for k8s_file in k8s_files:
                findings = await self._scan_manifest(k8s_file, workspace, config)
                all_findings.extend(findings)

            # Create summary
            summary = self._create_summary(all_findings, len(k8s_files))

            logger.info(f"Kubesec found {len(all_findings)} security issues across {len(k8s_files)} manifests")

            return self.create_result(
                findings=all_findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Kubesec module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _find_kubernetes_files(self, workspace: Path, config: Dict[str, Any]) -> List[Path]:
        """Find Kubernetes manifest files in workspace"""
        patterns = config.get("kubernetes_patterns", [
            "**/*.yaml", "**/*.yml", "**/k8s/*.yaml", "**/kubernetes/*.yaml"
        ])
        exclude_patterns = config.get("exclude_patterns", [])

        k8s_files = []
        for pattern in patterns:
            files = workspace.glob(pattern)
            for file in files:
                # Check if file contains Kubernetes resources
                if self._is_kubernetes_manifest(file):
                    # Check if file should be excluded
                    should_exclude = False
                    for exclude_pattern in exclude_patterns:
                        if file.match(exclude_pattern):
                            should_exclude = True
                            break
                    if not should_exclude:
                        k8s_files.append(file)

        return list(set(k8s_files))  # Remove duplicates

    def _is_kubernetes_manifest(self, file: Path) -> bool:
        """Check if a file is a Kubernetes manifest"""
        try:
            content = file.read_text(encoding='utf-8')
            # Simple heuristic: check for common Kubernetes fields
            k8s_indicators = [
                "apiVersion:", "kind:", "metadata:", "spec:",
                "Deployment", "Service", "Pod", "ConfigMap",
                "Secret", "Ingress", "PersistentVolume"
            ]
            return any(indicator in content for indicator in k8s_indicators)
        except Exception:
            return False

    async def _scan_manifest(self, manifest_file: Path, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Scan a single Kubernetes manifest with Kubesec"""
        findings = []

        try:
            # Build kubesec command
            cmd = ["kubesec", "scan"]

            # Add format
            format_type = config.get("format", "json")
            if format_type == "json":
                cmd.append("-f")
                cmd.append("json")

            # Add the manifest file
            cmd.append(str(manifest_file))

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run kubesec
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            if process.returncode == 0:
                findings = self._parse_kubesec_output(
                    stdout.decode(), manifest_file, workspace, config
                )
            else:
                error_msg = stderr.decode()
                logger.warning(f"Kubesec failed for {manifest_file}: {error_msg}")

        except Exception as e:
            logger.warning(f"Error scanning {manifest_file}: {e}")

        return findings

    def _parse_kubesec_output(self, output: str, manifest_file: Path, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse Kubesec JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            # Kubesec outputs JSON array
            results = json.loads(output)
            if not isinstance(results, list):
                results = [results]

            threshold = config.get("threshold", 0)

            for result in results:
                score = result.get("score", 0)
                object_name = result.get("object", "Unknown")
                valid = result.get("valid", True)
                message = result.get("message", "")

                # Make file path relative to workspace
                try:
                    rel_path = manifest_file.relative_to(workspace)
                    file_path = str(rel_path)
                except ValueError:
                    file_path = str(manifest_file)

                # Process scoring and advise sections
                advise = result.get("advise", [])
                scoring = result.get("scoring", {})

                # Create findings for low scores
                if score < threshold or not valid:
                    severity = "high" if score < 0 else "medium" if score < 5 else "low"

                    finding = self.create_finding(
                        title=f"Kubernetes Security Score Low: {object_name}",
                        description=message or f"Security score {score} below threshold {threshold}",
                        severity=severity,
                        category="kubernetes_security",
                        file_path=file_path,
                        recommendation=self._get_score_recommendation(score, advise),
                        metadata={
                            "score": score,
                            "threshold": threshold,
                            "object": object_name,
                            "valid": valid,
                            "advise_count": len(advise),
                            "scoring_details": scoring
                        }
                    )
                    findings.append(finding)

                # Create findings for each advisory
                for advisory in advise:
                    selector = advisory.get("selector", "")
                    reason = advisory.get("reason", "")
                    href = advisory.get("href", "")

                    # Determine severity based on advisory type
                    severity = self._get_advisory_severity(reason, selector)
                    category = self._get_advisory_category(reason, selector)

                    finding = self.create_finding(
                        title=f"Kubernetes Security Advisory: {selector}",
                        description=reason,
                        severity=severity,
                        category=category,
                        file_path=file_path,
                        recommendation=self._get_advisory_recommendation(reason, href),
                        metadata={
                            "selector": selector,
                            "href": href,
                            "object": object_name,
                            "advisory_type": "kubesec_advise"
                        }
                    )
                    findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Kubesec output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Kubesec results: {e}")

        return findings

    def _get_advisory_severity(self, reason: str, selector: str) -> str:
        """Determine severity based on advisory reason and selector"""
        reason_lower = reason.lower()
        selector_lower = selector.lower()

        # High severity issues
        if any(term in reason_lower for term in [
            "privileged", "root", "hostnetwork", "hostpid", "hostipc",
            "allowprivilegeescalation", "runasroot", "security", "capabilities"
        ]):
            return "high"

        # Medium severity issues
        elif any(term in reason_lower for term in [
            "resources", "limits", "requests", "readonly", "securitycontext"
        ]):
            return "medium"

        # Low severity issues
        elif any(term in reason_lower for term in [
            "labels", "annotations", "probe", "liveness", "readiness"
        ]):
            return "low"

        else:
            return "medium"

    def _get_advisory_category(self, reason: str, selector: str) -> str:
        """Determine category based on advisory"""
        reason_lower = reason.lower()

        if any(term in reason_lower for term in ["privilege", "root", "security", "capabilities"]):
            return "privilege_escalation"
        elif any(term in reason_lower for term in ["network", "host"]):
            return "network_security"
        elif any(term in reason_lower for term in ["resources", "limits"]):
            return "resource_management"
        elif any(term in reason_lower for term in ["probe", "health"]):
            return "health_monitoring"
        else:
            return "kubernetes_best_practices"

    def _get_score_recommendation(self, score: int, advise: List[Dict]) -> str:
        """Generate recommendation based on score and advisories"""
        if score < 0:
            return "Critical security issues detected. Address all security advisories immediately."
        elif score < 5:
            return "Low security score detected. Review and implement security best practices."
        elif len(advise) > 0:
            return f"Security score is {score}. Review {len(advise)} advisory recommendations for improvement."
        else:
            return "Review Kubernetes security configuration and apply security hardening measures."

    def _get_advisory_recommendation(self, reason: str, href: str) -> str:
        """Generate recommendation for advisory"""
        if href:
            return f"{reason} For more details, see: {href}"

        reason_lower = reason.lower()

        # Specific recommendations based on common patterns
        if "privileged" in reason_lower:
            return "Remove privileged: true from security context. Run containers with minimal privileges."
        elif "root" in reason_lower or "runasroot" in reason_lower:
            return "Configure runAsNonRoot: true and set runAsUser to a non-root user ID."
        elif "allowprivilegeescalation" in reason_lower:
            return "Set allowPrivilegeEscalation: false to prevent privilege escalation."
        elif "resources" in reason_lower:
            return "Define resource requests and limits to prevent resource exhaustion."
        elif "readonly" in reason_lower:
            return "Set readOnlyRootFilesystem: true to prevent filesystem modifications."
        elif "capabilities" in reason_lower:
            return "Drop unnecessary capabilities and add only required ones."
        elif "probe" in reason_lower:
            return "Add liveness and readiness probes for better health monitoring."
        else:
            return f"Address the security concern: {reason}"

    def _create_summary(self, findings: List[ModuleFinding], total_files: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        object_counts = {}
        scores = []

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by object
            obj = finding.metadata.get("object", "unknown")
            object_counts[obj] = object_counts.get(obj, 0) + 1

            # Collect scores
            score = finding.metadata.get("score")
            if score is not None:
                scores.append(score)

        return {
            "total_findings": len(findings),
            "files_scanned": total_files,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "object_counts": object_counts,
            "average_score": sum(scores) / len(scores) if scores else 0,
            "min_score": min(scores) if scores else 0,
            "max_score": max(scores) if scores else 0,
            "files_with_issues": len(set(f.file_path for f in findings if f.file_path))
        }