"""
OpenGrep Static Analysis Module

This module uses OpenGrep (open-source version of Semgrep) for pattern-based
static analysis across multiple programming languages.
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
import tempfile
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class OpenGrepModule(BaseModule):
    """OpenGrep static analysis module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="opengrep",
            version="1.45.0",
            description="Open-source pattern-based static analysis tool for security vulnerabilities",
            author="FuzzForge Team",
            category="static_analysis",
            tags=["sast", "pattern-matching", "multi-language", "security"],
            input_schema={
                "type": "object",
                "properties": {
                    "config": {
                        "type": "string",
                        "enum": ["auto", "p/security-audit", "p/owasp-top-ten", "p/cwe-top-25"],
                        "default": "auto",
                        "description": "Rule configuration to use"
                    },
                    "custom_rules_path": {
                        "type": "string",
                        "description": "Path to a directory containing custom OpenGrep rules"
                    },
                    "languages": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Specific languages to analyze"
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
                    },
                    "max_target_bytes": {
                        "type": "integer",
                        "default": 1000000,
                        "description": "Maximum file size to analyze (bytes)"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 300,
                        "description": "Analysis timeout in seconds"
                    },
                    "severity": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["ERROR", "WARNING", "INFO"]},
                        "default": ["ERROR", "WARNING", "INFO"],
                        "description": "Minimum severity levels to report"
                    },
                    "confidence": {
                        "type": "array",
                        "items": {"type": "string", "enum": ["HIGH", "MEDIUM", "LOW"]},
                        "default": ["HIGH", "MEDIUM", "LOW"],
                        "description": "Minimum confidence levels to report"
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
                                "severity": {"type": "string"},
                                "confidence": {"type": "string"},
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
        timeout = config.get("timeout", 300)
        if not isinstance(timeout, int) or timeout < 30 or timeout > 3600:
            raise ValueError("Timeout must be between 30 and 3600 seconds")

        max_bytes = config.get("max_target_bytes", 1000000)
        if not isinstance(max_bytes, int) or max_bytes < 1000 or max_bytes > 10000000:
            raise ValueError("max_target_bytes must be between 1000 and 10000000")

        custom_rules_path = config.get("custom_rules_path")
        if custom_rules_path:
            if not Path(custom_rules_path).is_dir():
                raise ValueError(f"Custom rules path must be a valid directory: {custom_rules_path}")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute OpenGrep static analysis"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info(f"Running OpenGrep analysis on {workspace}")

            # Build opengrep command
            cmd = ["opengrep", "scan", "--json"]

            # Add configuration
            custom_rules_path = config.get("custom_rules_path")
            use_custom_rules = False
            if custom_rules_path:
                cmd.extend(["--config", custom_rules_path])
                use_custom_rules = True
            else:
                config_type = config.get("config", "auto")
                if config_type == "auto":
                    cmd.extend(["--config", "auto"])
                else:
                    cmd.extend(["--config", config_type])

            # Add timeout
            cmd.extend(["--timeout", str(config.get("timeout", 300))])

            # Add max target bytes
            cmd.extend(["--max-target-bytes", str(config.get("max_target_bytes", 1000000))])

            # Add languages if specified (but NOT when using custom rules, as rules define their own languages)
            if config.get("languages") and not use_custom_rules:
                langs = ",".join(config["languages"])
                cmd.extend(["--lang", langs])

            # Add include patterns
            if config.get("include_patterns"):
                for pattern in config["include_patterns"]:
                    cmd.extend(["--include", pattern])

            # Add exclude patterns
            if config.get("exclude_patterns"):
                for pattern in config["exclude_patterns"]:
                    cmd.extend(["--exclude", pattern])

            # Add severity filter only if a single level is requested.
            severity_levels = config.get("severity", ["ERROR", "WARNING", "INFO"])
            if severity_levels and len(severity_levels) == 1:
                cmd.extend(["--severity", severity_levels[0]])

            # Add confidence filter (if supported in this version)
            confidence_levels = config.get("confidence", ["HIGH", "MEDIUM"])
            if confidence_levels and len(confidence_levels) < 3:  # Only if not all levels
                # Note: confidence filtering might need to be done post-processing
                pass

            # Disable metrics collection
            cmd.append("--disable-version-check")
            cmd.append("--no-git-ignore")

            # Add target directory
            cmd.append(str(workspace))

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run OpenGrep
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            findings = []
            if process.returncode in [0, 1]:  # 0 = no findings, 1 = findings found
                findings = self._parse_opengrep_output(stdout.decode(), workspace, config)
            else:
                error_msg = stderr.decode()
                logger.error(f"OpenGrep failed: {error_msg}")
                return self.create_result(
                    findings=[],
                    status="failed",
                    error=f"OpenGrep execution failed: {error_msg}"
                )

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"OpenGrep found {len(findings)} potential issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"OpenGrep module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    def _parse_opengrep_output(self, output: str, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse OpenGrep JSON output into findings"""
        findings = []

        if not output.strip():
            return findings

        try:
            data = json.loads(output)
            results = data.get("results", [])
            logger.debug(f"OpenGrep returned {len(results)} raw results")

            # Get filtering criteria
            allowed_severities = set(config.get("severity", ["ERROR", "WARNING", "INFO"]))
            allowed_confidences = set(config.get("confidence", ["HIGH", "MEDIUM", "LOW"]))

            for result in results:
                # Extract basic info
                rule_id = result.get("check_id", "unknown")
                message = result.get("message", "")
                extra = result.get("extra", {})
                severity = extra.get("severity", "INFO").upper()

                # File location info
                path_info = result.get("path", "")
                start_line = result.get("start", {}).get("line", 0)
                end_line = result.get("end", {}).get("line", 0)
                start_col = result.get("start", {}).get("col", 0)
                end_col = result.get("end", {}).get("col", 0)

                # Code snippet
                lines = extra.get("lines", "")

                # Metadata
                rule_metadata = extra.get("metadata", {})
                cwe = rule_metadata.get("cwe", [])
                owasp = rule_metadata.get("owasp", [])
                confidence = extra.get("confidence", rule_metadata.get("confidence", "MEDIUM")).upper()

                # Apply severity filter
                if severity not in allowed_severities:
                    continue

                # Apply confidence filter
                if confidence not in allowed_confidences:
                    continue

                # Make file path relative to workspace
                if path_info:
                    try:
                        rel_path = Path(path_info).relative_to(workspace)
                        path_info = str(rel_path)
                    except ValueError:
                        pass

                # Map severity to our standard levels
                finding_severity = self._map_severity(severity)

                # Create finding
                finding = self.create_finding(
                    title=f"Security issue: {rule_id}",
                    description=message or f"OpenGrep rule {rule_id} triggered",
                    severity=finding_severity,
                    category=self._get_category(rule_id, extra),
                    file_path=path_info if path_info else None,
                    line_start=start_line if start_line > 0 else None,
                    line_end=end_line if end_line > 0 and end_line != start_line else None,
                    code_snippet=lines.strip() if lines else None,
                    recommendation=self._get_recommendation(rule_id, extra),
                    metadata={
                        "rule_id": rule_id,
                        "opengrep_severity": severity,
                        "confidence": confidence,
                        "cwe": cwe,
                        "owasp": owasp,
                        "fix": extra.get("fix", ""),
                        "impact": extra.get("impact", ""),
                        "likelihood": extra.get("likelihood", ""),
                        "references": extra.get("references", [])
                    }
                )

                findings.append(finding)

        except json.JSONDecodeError as e:
            logger.warning(f"Failed to parse OpenGrep output: {e}. Output snippet: {output[:200]}...")
        except Exception as e:
            logger.warning(f"Error processing OpenGrep results: {e}")

        return findings

    def _map_severity(self, opengrep_severity: str) -> str:
        """Map OpenGrep severity to our standard severity levels"""
        severity_map = {
            "ERROR": "high",
            "WARNING": "medium",
            "INFO": "low"
        }
        return severity_map.get(opengrep_severity.upper(), "medium")

    def _get_category(self, rule_id: str, extra: Dict[str, Any]) -> str:
        """Determine finding category based on rule and metadata"""
        rule_metadata = extra.get("metadata", {})
        cwe_list = rule_metadata.get("cwe", [])
        owasp_list = rule_metadata.get("owasp", [])

        # Check for common security categories
        if any("injection" in rule_id.lower() for x in [rule_id]):
            return "injection"
        elif any("xss" in rule_id.lower() for x in [rule_id]):
            return "xss"
        elif any("csrf" in rule_id.lower() for x in [rule_id]):
            return "csrf"
        elif any("auth" in rule_id.lower() for x in [rule_id]):
            return "authentication"
        elif any("crypto" in rule_id.lower() for x in [rule_id]):
            return "cryptography"
        elif cwe_list:
            return f"cwe-{cwe_list[0]}"
        elif owasp_list:
            return f"owasp-{owasp_list[0].replace(' ', '-').lower()}"
        else:
            return "security"

    def _get_recommendation(self, rule_id: str, extra: Dict[str, Any]) -> str:
        """Generate recommendation based on rule and metadata"""
        fix_suggestion = extra.get("fix", "")
        if fix_suggestion:
            return fix_suggestion

        # Generic recommendations based on rule type
        if "injection" in rule_id.lower():
            return "Use parameterized queries or prepared statements to prevent injection attacks."
        elif "xss" in rule_id.lower():
            return "Properly encode/escape user input before displaying it in web pages."
        elif "crypto" in rule_id.lower():
            return "Use cryptographically secure algorithms and proper key management."
        elif "hardcode" in rule_id.lower():
            return "Remove hardcoded secrets and use secure configuration management."
        else:
            return "Review this security issue and apply appropriate fixes based on your security requirements."

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0}
        category_counts = {}
        rule_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by rule
            rule_id = finding.metadata.get("rule_id", "unknown")
            rule_counts[rule_id] = rule_counts.get(rule_id, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "top_rules": dict(sorted(rule_counts.items(), key=lambda x: x[1], reverse=True)[:10]),
            "files_analyzed": len(set(f.file_path for f in findings if f.file_path))
        }
