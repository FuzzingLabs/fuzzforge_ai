"""
Go-Fuzz Module

This module uses go-fuzz for coverage-guided fuzzing of Go packages.
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
class GoFuzzModule(BaseModule):
    """Go-Fuzz Go language fuzzing module"""

    def get_metadata(self) -> ModuleMetadata:
        """Get module metadata"""
        return ModuleMetadata(
            name="go_fuzz",
            version="1.2.0",
            description="Coverage-guided fuzzing for Go packages using go-fuzz",
            author="FuzzForge Team",
            category="fuzzing",
            tags=["go", "golang", "coverage-guided", "packages"],
            input_schema={
                "type": "object",
                "properties": {
                    "package_path": {
                        "type": "string",
                        "description": "Path to Go package to fuzz"
                    },
                    "fuzz_function": {
                        "type": "string",
                        "default": "Fuzz",
                        "description": "Name of the fuzz function"
                    },
                    "workdir": {
                        "type": "string",
                        "default": "go_fuzz_workdir",
                        "description": "Working directory for go-fuzz"
                    },
                    "procs": {
                        "type": "integer",
                        "default": 1,
                        "description": "Number of parallel processes"
                    },
                    "timeout": {
                        "type": "integer",
                        "default": 600,
                        "description": "Total fuzzing timeout (seconds)"
                    },
                    "race": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable race detector"
                    },
                    "minimize": {
                        "type": "boolean",
                        "default": true,
                        "description": "Minimize crashers"
                    },
                    "sonar": {
                        "type": "boolean",
                        "default": false,
                        "description": "Enable sonar mode"
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
                                "crash_type": {"type": "string"},
                                "crash_file": {"type": "string"},
                                "stack_trace": {"type": "string"}
                            }
                        }
                    }
                }
            }
        )

    def validate_config(self, config: Dict[str, Any]) -> bool:
        """Validate configuration"""
        package_path = config.get("package_path")
        if not package_path:
            raise ValueError("package_path is required")

        return True

    async def execute(self, config: Dict[str, Any], workspace: Path) -> ModuleResult:
        """Execute go-fuzz fuzzing"""
        self.start_timer()

        try:
            # Validate inputs
            self.validate_config(config)
            self.validate_workspace(workspace)

            logger.info("Running go-fuzz Go fuzzing")

            # Check installation
            await self._check_go_fuzz_installation()

            # Setup
            package_path = workspace / config["package_path"]
            workdir = workspace / config.get("workdir", "go_fuzz_workdir")

            # Build and run
            findings = await self._run_go_fuzz(package_path, workdir, config, workspace)

            # Create summary
            summary = self._create_summary(findings)

            logger.info(f"go-fuzz found {len(findings)} issues")

            return self.create_result(
                findings=findings,
                status="success",
                summary=summary
            )

        except Exception as e:
            logger.error(f"go-fuzz module failed: {e}")
            return self.create_result(
                findings=[],
                status="failed",
                error=str(e)
            )

    async def _check_go_fuzz_installation(self):
        """Check if go-fuzz is installed"""
        try:
            process = await asyncio.create_subprocess_exec(
                "go-fuzz", "--help",
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE
            )
            await process.communicate()

            if process.returncode != 0:
                # Try building
                process = await asyncio.create_subprocess_exec(
                    "go", "install", "github.com/dvyukov/go-fuzz/go-fuzz@latest",
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE
                )
                await process.communicate()

        except Exception as e:
            raise RuntimeError(f"go-fuzz installation failed: {e}")

    async def _run_go_fuzz(self, package_path: Path, workdir: Path, config: Dict[str, Any], workspace: Path) -> List[ModuleFinding]:
        """Run go-fuzz"""
        findings = []

        try:
            # Create workdir
            workdir.mkdir(exist_ok=True)

            # Build
            await self._build_go_fuzz(package_path, config)

            # Run fuzzing
            cmd = ["go-fuzz", "-bin", f"{package_path.name}-fuzz.zip", "-workdir", str(workdir)]

            if config.get("procs", 1) > 1:
                cmd.extend(["-procs", str(config["procs"])])

            if config.get("race", False):
                cmd.append("-race")

            if config.get("sonar", False):
                cmd.append("-sonar")

            timeout = config.get("timeout", 600)

            try:
                process = await asyncio.create_subprocess_exec(
                    *cmd,
                    stdout=asyncio.subprocess.PIPE,
                    stderr=asyncio.subprocess.PIPE,
                    cwd=package_path.parent
                )

                try:
                    stdout, stderr = await asyncio.wait_for(
                        process.communicate(), timeout=timeout
                    )
                except asyncio.TimeoutError:
                    process.terminate()
                    await process.wait()

                # Parse results
                findings = self._parse_go_fuzz_results(workdir, workspace, config)

            except Exception as e:
                logger.warning(f"Error running go-fuzz: {e}")

        except Exception as e:
            logger.warning(f"Error in go-fuzz execution: {e}")

        return findings

    async def _build_go_fuzz(self, package_path: Path, config: Dict[str, Any]):
        """Build go-fuzz binary"""
        cmd = ["go-fuzz-build"]
        if config.get("race", False):
            cmd.append("-race")

        process = await asyncio.create_subprocess_exec(
            *cmd,
            cwd=package_path,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE
        )
        stdout, stderr = await process.communicate()

        if process.returncode != 0:
            raise RuntimeError(f"go-fuzz-build failed: {stderr.decode()}")

    def _parse_go_fuzz_results(self, workdir: Path, workspace: Path, config: Dict[str, Any]) -> List[ModuleFinding]:
        """Parse go-fuzz results"""
        findings = []

        try:
            # Look for crashers
            crashers_dir = workdir / "crashers"
            if crashers_dir.exists():
                for crash_file in crashers_dir.iterdir():
                    if crash_file.is_file() and not crash_file.name.startswith("."):
                        finding = self._create_crash_finding(crash_file, workspace)
                        if finding:
                            findings.append(finding)

            # Look for suppressions (potential issues)
            suppressions_dir = workdir / "suppressions"
            if suppressions_dir.exists():
                for supp_file in suppressions_dir.iterdir():
                    if supp_file.is_file():
                        finding = self._create_suppression_finding(supp_file, workspace)
                        if finding:
                            findings.append(finding)

        except Exception as e:
            logger.warning(f"Error parsing go-fuzz results: {e}")

        return findings

    def _create_crash_finding(self, crash_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from crash file"""
        try:
            # Read crash output
            crash_content = ""
            if crash_file.name.endswith(".output"):
                crash_content = crash_file.read_text()

            # Determine crash type
            crash_type = "panic"
            if "runtime error" in crash_content:
                crash_type = "runtime_error"
            elif "race" in crash_content:
                crash_type = "race_condition"

            try:
                rel_path = crash_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(crash_file)

            finding = self.create_finding(
                title=f"go-fuzz {crash_type.title()}",
                description=f"go-fuzz discovered a {crash_type} in the Go code",
                severity=self._get_crash_severity(crash_type),
                category=self._get_crash_category(crash_type),
                file_path=file_path,
                recommendation=self._get_crash_recommendation(crash_type),
                metadata={
                    "crash_type": crash_type,
                    "crash_file": str(crash_file),
                    "stack_trace": crash_content[:1000],
                    "fuzzer": "go_fuzz"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating crash finding: {e}")
            return None

    def _create_suppression_finding(self, supp_file: Path, workspace: Path) -> ModuleFinding:
        """Create finding from suppression file"""
        try:
            try:
                rel_path = supp_file.relative_to(workspace)
                file_path = str(rel_path)
            except ValueError:
                file_path = str(supp_file)

            finding = self.create_finding(
                title="go-fuzz Potential Issue",
                description="go-fuzz identified a potential issue that was suppressed",
                severity="low",
                category="potential_issue",
                file_path=file_path,
                recommendation="Review suppressed issue to determine if it requires attention.",
                metadata={
                    "suppression_file": str(supp_file),
                    "fuzzer": "go_fuzz"
                }
            )

            return finding

        except Exception as e:
            logger.warning(f"Error creating suppression finding: {e}")
            return None

    def _get_crash_severity(self, crash_type: str) -> str:
        """Get crash severity"""
        if crash_type == "race_condition":
            return "high"
        elif crash_type == "runtime_error":
            return "high"
        else:
            return "medium"

    def _get_crash_category(self, crash_type: str) -> str:
        """Get crash category"""
        if crash_type == "race_condition":
            return "race_condition"
        elif crash_type == "runtime_error":
            return "runtime_error"
        else:
            return "program_crash"

    def _get_crash_recommendation(self, crash_type: str) -> str:
        """Get crash recommendation"""
        if crash_type == "race_condition":
            return "Fix race condition by adding proper synchronization (mutexes, channels, etc.)"
        elif crash_type == "runtime_error":
            return "Fix runtime error by adding bounds checking and proper error handling"
        else:
            return "Analyze the crash and fix the underlying issue"

    def _create_summary(self, findings: List[ModuleFinding]) -> Dict[str, Any]:
        """Create analysis summary"""
        severity_counts = {"critical": 0, "high": 0, "medium": 0, "low": 0, "info": 0}
        category_counts = {}

        for finding in findings:
            severity_counts[finding.severity] += 1
            category_counts[finding.category] = category_counts.get(finding.category, 0) + 1

        return {
            "total_findings": len(findings),
            "severity_counts": severity_counts,
            "category_counts": category_counts
        }