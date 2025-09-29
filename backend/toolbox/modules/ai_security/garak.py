"""
Garak AI Security Module

This module uses Garak for AI red-teaming and LLM vulnerability assessment,
testing for prompt injection, bias, jailbreaks, and other AI-specific security issues.
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
import os
from pathlib import Path
from typing import Dict, Any, List
import subprocess
import logging

from ..base import BaseModule, ModuleMetadata, ModuleFinding, ModuleResult
from . import register_module

logger = logging.getLogger(__name__)


@register_module
class GarakModule(BaseModule):
    """Garak AI red-teaming and LLM vulnerability assessment module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="garak",
            version="0.9.0",
            description="AI red-teaming framework for testing LLM vulnerabilities including prompt injection, bias, and jailbreaks",
            author="FuzzForge Team",
            category="ai_security",
            tags=["ai", "llm", "prompt-injection", "bias", "jailbreak", "red-team"],
            input_schema={
                "type": "object",
                "properties": {
                    "model_type": {
                        "type": "string",
                        "enum": ["openai", "huggingface", "anthropic", "local"],
                        "description": "Type of LLM to test"
                    },
                    "model_name": {
                        "type": "string",
                        "description": "Name/path of the model to test"
                    },
                    "api_key": {
                        "type": "string",
                        "description": "API key for cloud models (if required)"
                    },
                    "probes": {
                        "type": "array",
                        "items": {"type": "string"},
                        "default": ["encoding", "promptinject", "malwaregen", "dan"],
                        "description": "Probe types to run"
                    },
                    "generations": {
                        "type": "integer",
                        "default": 10,
                        "description": "Number of generations per probe"
                    },
                    "detectors": {
                        "type": "array",
                        "items": {"type": "string"},
                        "description": "Detectors to use for evaluation"
                    },
                    "config_file": {
                        "type": "string",
                        "description": "Path to Garak configuration file"
                    },
                    "report_prefix": {
                        "type": "string",
                        "default": "garak",
                        "description": "Prefix for report files"
                    },
                    "parallel_requests": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of parallel requests"
                    },
                    "temperature": {
                        "type": "number",
                        "default": 0.7,
                        "description": "Model temperature setting"
                    },
                    "max_tokens": {
                        "type": "integer",
                        "default": 150,
                        "description": "Maximum tokens per generation"
                    },
                    "seed": {
                        "type": "integer",
                        "description": "Random seed for reproducibility"
                    },
                    "verbose": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable verbose output"
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
                                "probe_name": {"type": "string"},
                                "vulnerability_type": {"type": "string"},
                                "success_rate": {"type": "number"},
                                "prompt": {"type": "string"},
                                "response": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        model_type = config.get("model_type")
        if not model_type:
            raise ValueError("model_type is required")

        model_name = config.get("model_name")
        if not model_name:
            raise ValueError("model_name is required")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute Garak AI security testing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running Garak AI security assessment")

            # Check Garak installation
            await self._check_garak_installation()

            # Run Garak testing
            findings = await self._run_garak_assessment(config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"Garak found {len(findings)} AI security issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"Garak module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_garak_installation(self):
        """Check if Garak is installed"""
        try:
            process = await asyncio.create_subprocess_exec(
                "python", "-c", "import garak; print(garak.__version__)",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            stdout, stderr = await process.communicate()

            if process.returncode != 0:
                # Try installing if not available
                logger.info("Garak not found, attempting installation...")
                install_process = await asyncio.create_subprocess_exec(
                    "pip", "install", "garak",
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                await install_process.communicate()

        except Exception as e:
            logger.warning(f"Garak installation check failed: {e}")

    async def _run_garak_assessment(self, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run Garak AI security assessment"""
        findings = []

        try:
            # Build Garak command
            cmd = ["python", "-m", "garak"]

            # Add model configuration
            cmd.extend(["--model_type", config["model_type"]])
            cmd.extend(["--model_name", config["model_name"]])

            # Add API key if provided
            api_key = config.get("api_key")
            if api_key:
                # Set environment variable instead of command line for security
                os.environ["GARAK_API_KEY"] = api_key

            # Add probes
            probes = config.get("probes", ["encoding", "promptinject"])
            for probe in probes:
                cmd.extend(["--probes", probe])

            # Add generations
            generations = config.get("generations", 10)
            cmd.extend(["--generations", str(generations)])

            # Add detectors if specified
            detectors = config.get("detectors", [])
            for detector in detectors:
                cmd.extend(["--detectors", detector])

            # Add parallel requests
            parallel = config.get("parallel_requests", 1)
            if parallel > 1:
                cmd.extend(["--parallel_requests", str(parallel)])

            # Add model parameters
            temperature = config.get("temperature", 0.7)
            cmd.extend(["--temperature", str(temperature)])

            max_tokens = config.get("max_tokens", 150)
            cmd.extend(["--max_tokens", str(max_tokens)])

            # Add seed for reproducibility
            seed = config.get("seed")
            if seed:
                cmd.extend(["--seed", str(seed)])

            # Add configuration file
            config_file = config.get("config_file")
            if config_file:
                config_path = workspace / config_file
                if config_path.exists():
                    cmd.extend(["--config", str(config_path)])

            # Set output directory
            output_dir = workspace / "garak_output"
            output_dir.mkdir(exist_ok=True)
            cmd.extend(["--report_prefix", str(output_dir / config.get("report_prefix", "garak"))])

            # Add verbose flag
            if config.get("verbose", False):
                cmd.append("--verbose")

            logger.debug(f"Running command: {' '.join(cmd)}")

            # Run Garak
            process = await asyncio.create_subprocess_exec(
                *cmd,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd=workspace
            )

            stdout, stderr = await process.communicate()

            # Parse results
            findings = self._parse_garak_results(output_dir, workspace, stdout.decode(), stderr.decode())

        except Exception as e:
            logger.warning(f"Error running Garak assessment: {e}")

        return findings

    def _parse_garak_results(self, output_dir: Path, workspace: Path, stdout: str, stderr: str) -> List[ModuleFinding]:
        """Parse Garak output for findings"""
        findings = []

        try:
            # Look for JSON report files
            report_files = list(output_dir.glob("*.report.jsonl"))

            for report_file in report_files:
                findings.extend(self._parse_report_file(report_file, workspace))

            # If no report files, try to parse stdout
            if not findings:
                findings = self._parse_stdout_output(stdout, stderr, workspace)

        except Exception as e:
            logger.warning(f"Error parsing Garak results: {e}")

        return findings

    def _parse_report_file(self, report_file: Path, workspace: Path) -> List[ModuleFinding]:
        """Parse Garak JSONL report file"""
        findings = []

        try:
            with open(report_file, 'r') as f:
                for line in f:
                    if line.strip():
                        data = json.loads(line)
                        finding = self._create_garak_finding(data, workspace, report_file)
                        if finding:
                            findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing report file {report_file}: {e}")

        return findings

    def _parse_stdout_output(self, stdout: str, stderr: str, workspace: Path) -> List[ModuleFinding]:
        """Parse Garak stdout output"""
        findings = []

        try:
            # Look for vulnerability indicators in output
            lines = stdout.split('\n') + stderr.split('\n')

            for line in lines:
                if any(indicator in line.lower() for indicator in [
                    "vulnerability", "injection", "jailbreak", "bias", "harmful"
                ]):
                    # Create a basic finding from the output line
                    finding = self._create_basic_finding(line, workspace)
                    if finding:
                        findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing Garak stdout: {e}")

        return findings

    def _create_garak_finding(self, data: Dict[str, Any], workspace: Path, report_file: Path) -> ModuleFinding:
        """Create finding from Garak report data"""
        try:
            # Extract information from Garak data structure
            probe_name = data.get("probe", "unknown")
            detector_name = data.get("detector", "unknown")
            passed = data.get("passed", True)
            prompt = data.get("prompt", "")[:500]  # Limit length
            response = data.get("response", "")[:500]  # Limit length

            # Only create findings for failed tests (vulnerabilities found)
            if passed:
                return None

            # Determine vulnerability type and severity
            vulnerability_type = self._get_vulnerability_type(probe_name, detector_name)
            severity = self._get_vulnerability_severity(vulnerability_type, probe_name)

            # Create relative path
            try:
                rel_path = report_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(report_file)

            finding = self.create_finding(
                title=f"AI Security Issue: {vulnerability_type}",
                description=f"Garak detected a {vulnerability_type} vulnerability using probe '{probe_name}' and detector '{detector_name}'",
                severity=severity,
                category=self._get_ai_security_category(vulnerability_type),
                file_path=file_path,
                recommendation=self._get_ai_security_recommendation(vulnerability_type, probe_name),
                metadata={
                    "probe_name": probe_name,
                    "detector_name": detector_name,
                    "vulnerability_type": vulnerability_type,
                    "prompt_preview": prompt,
                    "response_preview": response,
                    "passed": passed,
                    "fuzzer": "garak"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating Garak finding: {e}")
            return None

    def _create_basic_finding(self, line: str, workspace: Path) -> ModuleFinding:
        """Create basic finding from output line"""
        try:
            # Extract basic information from line
            vulnerability_type = "ai_security_issue"
            if "injection" in line.lower():
                vulnerability_type = "prompt_injection"
            elif "jailbreak" in line.lower():
                vulnerability_type = "jailbreak_attempt"
            elif "bias" in line.lower():
                vulnerability_type = "bias_detection"

            finding = self.create_finding(
                title=f"AI Security Detection: {vulnerability_type.replace('_', ' ').title()}",
                description=f"Garak detected potential AI security issue: {line.strip()}",
                severity="medium",
                category=self._get_ai_security_category(vulnerability_type),
                file_path=None,
                recommendation=self._get_ai_security_recommendation(vulnerability_type, "general"),
                metadata={
                    "vulnerability_type": vulnerability_type,
                    "detection_line": line.strip(),
                    "fuzzer": "garak"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating basic finding: {e}")
            return None

    def _get_vulnerability_type(self, probe_name: str, detector_name: str) -> str:
        """Determine vulnerability type from probe and detector names"""
        probe_lower = probe_name.lower()
        detector_lower = detector_name.lower()

        if "inject" in probe_lower or "inject" in detector_lower:
            return "prompt_injection"
        elif "jailbreak" in probe_lower or "dan" in probe_lower:
            return "jailbreak_attempt"
        elif "bias" in probe_lower or "bias" in detector_lower:
            return "bias_detection"
        elif "malware" in probe_lower or "harmful" in detector_lower:
            return "harmful_content_generation"
        elif "encoding" in probe_lower:
            return "encoding_vulnerability"
        elif "leak" in probe_lower:
            return "data_leakage"
        else:
            return "ai_security_vulnerability"

    def _get_vulnerability_severity(self, vulnerability_type: str, probe_name: str) -> str:
        """Determine severity based on vulnerability type"""
        if vulnerability_type in ["prompt_injection", "jailbreak_attempt"]:
            return "high"
        elif vulnerability_type in ["harmful_content_generation", "data_leakage"]:
            return "high"
        elif vulnerability_type in ["bias_detection", "encoding_vulnerability"]:
            return "medium"
        else:
            return "medium"

    def _get_ai_security_category(self, vulnerability_type: str) -> str:
        """Get category for AI security vulnerability"""
        if "injection" in vulnerability_type:
            return "prompt_injection"
        elif "jailbreak" in vulnerability_type:
            return "jailbreak_attack"
        elif "bias" in vulnerability_type:
            return "algorithmic_bias"
        elif "harmful" in vulnerability_type or "malware" in vulnerability_type:
            return "harmful_content"
        elif "leak" in vulnerability_type:
            return "data_leakage"
        elif "encoding" in vulnerability_type:
            return "input_manipulation"
        else:
            return "ai_security"

    def _get_ai_security_recommendation(self, vulnerability_type: str, probe_name: str) -> str:
        """Get recommendation for AI security vulnerability"""
        if "injection" in vulnerability_type:
            return "Implement robust input validation, prompt sanitization, and use structured prompts to prevent injection attacks. Consider implementing content filtering and output validation."
        elif "jailbreak" in vulnerability_type:
            return "Strengthen model alignment and safety measures. Implement content filtering, use constitutional AI techniques, and add safety classifiers for output validation."
        elif "bias" in vulnerability_type:
            return "Review training data for bias, implement fairness constraints, use debiasing techniques, and conduct regular bias audits across different demographic groups."
        elif "harmful" in vulnerability_type:
            return "Implement strict content policies, use safety classifiers, add human oversight for sensitive outputs, and refuse to generate harmful content."
        elif "leak" in vulnerability_type:
            return "Review data handling practices, implement data anonymization, use differential privacy techniques, and audit model responses for sensitive information disclosure."
        elif "encoding" in vulnerability_type:
            return "Normalize and validate all input encodings, implement proper character filtering, and use encoding-aware input processing."
        else:
            return f"Address the {vulnerability_type} vulnerability by implementing appropriate AI safety measures, input validation, and output monitoring."

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}
        vulnerability_counts = {}
        probe_counts = {}

        for finding in findings:
            # Count by severity
            severity_counts[finding.severity] += 1

            # Count by category
            category = finding.category
            category_counts[category] = category_counts.get(category, 0) + 1

            # Count by vulnerability type
            vuln_type = finding.metadata.get("vulnerability_type", "unknown")
            vulnerability_counts[vuln_type] = vulnerability_counts.get(vuln_type, 0) + 1

            # Count by probe
            probe = finding.metadata.get("probe_name", "unknown")
            probe_counts[probe] = probe_counts.get(probe, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts,
            "vulnerability_counts": vulnerability_counts,
            "probe_counts": probe_counts,
            "ai_security_issues": len(findings),
            "high_risk_vulnerabilities": severity_counts.get("high", 0) + severity_counts.get("critical", 0)
        }