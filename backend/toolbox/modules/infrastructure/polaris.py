"""
Polaris Infrastructure Security Module

This module uses Polaris to validate Kubernetes resources against security
and best practice policies.
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
class PolarisModule(BaseModule):
    """Polaris Kubernetes best practices validation module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="polaris",
            version="8.5.0",
            description="Kubernetes best practices validation and policy enforcement using Polaris",
            author="FuzzForge Team",
            category="infrastructure",
            tags=["kubernetes", "k8s", "policy", "best-practices", "validation"],
            input_schema={
                "type": "object",
                "properties": {
                    "audit_path": {
                        "type": "string",
                        "description": "Path to audit (defaults to workspace)"
                    },
                    "config_file": {
                        "type": "string",
                        "description": "Path to Polaris config file"
                    },
                    "only_show_failed_tests": {
                        "type": "boolean",
                        "default": True,
                        "description": "Show only failed validation tests"
                    },
                    "severity_threshold": {
                        "type": "string",
                        "enum": ["error", "warning", "info"],
                        "default": "info",
                        "description": "Minimum severity level to report"
                    },
                    "format": {
                        "type": "string",
                        "enum": ["json", "yaml", "pretty"],
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
                        "description": "File patterns to exclude"
                    },
                    "disable_checks": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of check names to disable"
                    },
                    "enable_checks": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "List of check names to enable (if using custom config)"
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
                                "check_name": {"type": "string"},
                                "severity": {"type": "string"},
                                "category": {"type": "string"},
                                "file_path": {"type": "string"},
                                "resource_name": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        severity_threshold = config.get("severity_threshold", "warning")
        valid_severities = ["error", "warning", "info"]
        if severity_threshold not in valid_severities:
            raise ValueError(f"Invalid severity threshold: {severity_threshold}. Valid: {valid_severities}")

        format_type = config.get("format", "json")
        valid_formats = ["json", "yaml", "pretty"]
        if format_type not in valid_formats:
            raise ValueError(f"Invalid format: {format_type}. Valid: {valid_formats}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Polaris Kubernetes validation"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running Polaris Kubernetes validation on {workspace}")

            # Find all Kubernetes manifests
            k8s_files = self._find_kubernetes_files(workspace, config)
            if not k8s_files:
                logger.info("No Kubernetes manifest files found")
                return self.create_result(
                    findings=[],
                    status="success",
                    summary={"total_findings": 0, "files_scanned": 0}
                )

            logger.info(f"Found {len(k8s_files)} Kubernetes manifest file(s) to validate")

            # Run Polaris audit
            findings = await self._run_polaris_audit(workspace, config, k8s_files)

            # Create summary
            summary = self._create_summary(findings, len(k8s_files))

            logger.info(f"Polaris found {len(findings)} policy violations across {len(k8s_files)} manifests")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Polaris module failed: {e}")
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

    async def _run_polaris_audit(self, workspace: Path, config: Dict[str, Any], k8s_files: List[Path]) -> List[ModuleFinding]:
        """Run Polaris audit on workspace"""
        findings = []

        try:
            # Build polaris command
            cmd = ["polaris", "audit"]

            # Add audit path
            audit_path = config.get("audit_path", str(workspace))
            cmd.extend(["--audit-path", audit_path])

            # Add config file if specified
            config_file = config.get("config_file")
            if config_file:
                cmd.extend(["--config", config_file])

            # Add format
            format_type = config.get("format", "json")
            cmd.extend(["--format", format_type])

            # Add only failed tests flag
            if config.get("only_show_failed_tests", True):
                cmd.append("--only-show-failed-tests")

            # Add severity threshold
            severity_threshold = config.get("severity_threshold", "warning")
            cmd.extend(["--severity", severity_threshold])

            # Add disable checks
            disable_checks = config.get("disable_checks", [])
            for check in disable_checks:
                cmd.extend(["--disable-check", check])

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run polaris
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            if process.returncode == 0 or format_type == "json":
                findings = self._parse_polaris_output(stdout.decode(), workspace, config)
            else:
                error_msg = stderr.decode()
                logger.warning(f"Polaris audit failed: {error_msg}")

        except Exception as e:
            logger.warning(f"Error running Polaris audit: {e}")

        return findings

    def _parse_polaris_output(self, output: str, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse Polaris JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            data = json.loads(output)

            # Get severity threshold for filtering
            severity_threshold = config.get("severity_threshold", "warning")
            severity_levels = {"error": 3, "warning": 2, "info": 1}
            min_severity_level = severity_levels.get(severity_threshold, 2)

            # Process audit results
            audit_results = data.get("AuditResults", [])

            for result in audit_results:
                namespace = result.get("Namespace", "default")
                results_by_kind = result.get("Results", {})

                for kind, kind_results in results_by_kind.items():
                    for resource_name, resource_data in kind_results.items():
                        # Get container results
                        container_results = resource_data.get("ContainerResults", {})
                        pod_result = resource_data.get("PodResult", {})

                        # Process container results
                        for container_name, container_data in container_results.items():
                            self._process_container_results(
                                findings, container_data, kind, resource_name,
                                container_name, namespace, workspace, min_severity_level
                            )

                        # Process pod-level results
                        if pod_result:
                            self._process_pod_results(
                                findings, pod_result, kind, resource_name,
                                namespace, workspace, min_severity_level
                            )

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse Polaris output: {e}")
        except Exception as e:
            logger.warning(f"Error processing Polaris results: {e}")

        return findings

    def _process_container_results(self, findings: List[ModuleFinding], container_data: Dict,
                                 kind: str, resource_name: str, container_name: str,
                                 namespace: str, workspace: Path, min_severity_level: int):
        """Process container-level validation results"""
        results = container_data.get("Results", {})

        for check_name, check_result in results.items():
            severity = check_result.get("Severity", "warning")
            success = check_result.get("Success", True)
            message = check_result.get("Message", "")
            category_name = check_result.get("Category", "")

            # Skip if check passed or severity too low
            if success:
                continue

            severity_levels = {"error": 3, "warning": 2, "info": 1}
            if severity_levels.get(severity, 1) < min_severity_level:
                continue

            # Map severity to our standard levels
            finding_severity = self._map_severity(severity)
            category = self._get_category(check_name, category_name)

            finding = self.create_finding(
                title=f"Polaris Policy Violation: {check_name}",
                description=message or f"Container {container_name} in {kind} {resource_name} failed check {check_name}",
                severity=finding_severity,
                category=category,
                file_path=None,  # Polaris doesn't provide file paths in audit mode
                recommendation=self._get_recommendation(check_name, message),
                metadata={
                    "check_name": check_name,
                    "polaris_severity": severity,
                    "polaris_category": category_name,
                    "resource_kind": kind,
                    "resource_name": resource_name,
                    "container_name": container_name,
                    "namespace": namespace,
                    "context": "container"
                }
            )
            findings.append(finding)

    def _process_pod_results(self, findings: List[ModuleFinding], pod_result: Dict,
                           kind: str, resource_name: str, namespace: str,
                           workspace: Path, min_severity_level: int):
        """Process pod-level validation results"""
        results = pod_result.get("Results", {})

        for check_name, check_result in results.items():
            severity = check_result.get("Severity", "warning")
            success = check_result.get("Success", True)
            message = check_result.get("Message", "")
            category_name = check_result.get("Category", "")

            # Skip if check passed or severity too low
            if success:
                continue

            severity_levels = {"error": 3, "warning": 2, "info": 1}
            if severity_levels.get(severity, 1) < min_severity_level:
                continue

            # Map severity to our standard levels
            finding_severity = self._map_severity(severity)
            category = self._get_category(check_name, category_name)

            finding = self.create_finding(
                title=f"Polaris Policy Violation: {check_name}",
                description=message or f"{kind} {resource_name} failed check {check_name}",
                severity=finding_severity,
                category=category,
                file_path=None,  # Polaris doesn't provide file paths in audit mode
                recommendation=self._get_recommendation(check_name, message),
                metadata={
                    "check_name": check_name,
                    "polaris_severity": severity,
                    "polaris_category": category_name,
                    "resource_kind": kind,
                    "resource_name": resource_name,
                    "namespace": namespace,
                    "context": "pod"
                }
            )
            findings.append(finding)

    def _map_severity(self, polaris_severity: str) -> str:
        """Map Polaris severity to our standard severity levels"""
        severity_map = {
            "error": "high",
            "warning": "medium",
            "info": "low"
        }
        return severity_map.get(polaris_severity.lower(), "medium")

    def _get_category(self, check_name: str, category_name: str) -> str:
        """Determine finding category based on check name and category"""
        check_lower = check_name.lower()
        category_lower = category_name.lower()

        # Use Polaris category if available
        if "security" in category_lower:
            return "security_configuration"
        elif "efficiency" in category_lower:
            return "resource_efficiency"
        elif "reliability" in category_lower:
            return "reliability"

        # Fallback to check name analysis
        if any(term in check_lower for term in ["security", "privilege", "root", "capabilities"]):
            return "security_configuration"
        elif any(term in check_lower for term in ["resources", "limits", "requests"]):
            return "resource_management"
        elif any(term in check_lower for term in ["probe", "health", "liveness", "readiness"]):
            return "health_monitoring"
        elif any(term in check_lower for term in ["image", "tag", "pull"]):
            return "image_management"
        elif any(term in check_lower for term in ["network", "host"]):
            return "network_security"
        else:
            return "kubernetes_best_practices"

    def _get_recommendation(self, check_name: str, message: str) -> str:
        """Generate recommendation based on check name and message"""
        check_lower = check_name.lower()

        # Security-related recommendations
        if "privileged" in check_lower:
            return "Remove privileged: true from container security context to reduce security risks."
        elif "runasroot" in check_lower:
            return "Configure runAsNonRoot: true and specify a non-root user ID."
        elif "allowprivilegeescalation" in check_lower:
            return "Set allowPrivilegeEscalation: false to prevent privilege escalation attacks."
        elif "capabilities" in check_lower:
            return "Remove unnecessary capabilities and add only required ones using drop/add lists."
        elif "readonly" in check_lower:
            return "Set readOnlyRootFilesystem: true to prevent filesystem modifications."

        # Resource management recommendations
        elif "memory" in check_lower and "requests" in check_lower:
            return "Set memory requests to ensure proper resource allocation and scheduling."
        elif "memory" in check_lower and "limits" in check_lower:
            return "Set memory limits to prevent containers from using excessive memory."
        elif "cpu" in check_lower and "requests" in check_lower:
            return "Set CPU requests for proper resource allocation and quality of service."
        elif "cpu" in check_lower and "limits" in check_lower:
            return "Set CPU limits to prevent CPU starvation of other containers."

        # Health monitoring recommendations
        elif "liveness" in check_lower:
            return "Add liveness probes to detect and recover from container failures."
        elif "readiness" in check_lower:
            return "Add readiness probes to ensure containers are ready before receiving traffic."

        # Image management recommendations
        elif "tag" in check_lower:
            return "Use specific image tags instead of 'latest' for reproducible deployments."
        elif "pullpolicy" in check_lower:
            return "Set imagePullPolicy appropriately based on your deployment requirements."

        # Generic recommendation
        elif message:
            return f"Address the policy violation: {message}"
        else:
            return f"Review and fix the configuration issue identified by check: {check_name}"

    def _create_summary(self, findings: List[ModuleFinding], total_files: int) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        check_counts = {}
        resource_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by check
            check_name = finding.metadata.get("check_name", "unknown")
            check_counts[check_name] = check_counts.get(check_name, 0) + 1

            # Count by resource
            resource_kind = finding.metadata.get("resource_kind", "unknown")
            resource_counts[resource_kind] = resource_counts.get(resource_kind, 0) + 1

        return {
            "total_findings": len(findings),
            "files_scanned": total_files,
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_checks": dict(sorted(check_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "resource_type_counts": resource_counts,
            "unique_resources": len(set(f"{f.metadata.get('resource_kind')}:{f.metadata.get('resource_name')}" for f in findings)),
            "namespaces": len(set(f.metadata.get("namespace", "default") for f in findings))
        }